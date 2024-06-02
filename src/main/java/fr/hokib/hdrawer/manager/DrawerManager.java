package fr.hokib.hdrawer.manager;

import fr.hokib.hdrawer.HDrawer;
import fr.hokib.hdrawer.config.drawer.DrawerConfig;
import fr.hokib.hdrawer.manager.access.AccessAPI;
import fr.hokib.hdrawer.manager.access.DrawerAccess;
import fr.hokib.hdrawer.manager.drawer.Drawer;
import fr.hokib.hdrawer.util.Base64ItemStack;
import fr.hokib.hdrawer.util.location.LocationUtil;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

public class DrawerManager {

    private static final String DRAWER_ID = "drawer-id";
    private static final String DRAWER_CONTENT = "drawer-content";
    private final DrawerAccess drawerAccess;
    private final List<String> unsaved = new ArrayList<>();
    private final List<String> deleted = new ArrayList<>();
    private Map<String, Drawer> drawers = new HashMap<>();

    public DrawerManager() {
        this.drawerAccess = AccessAPI.from();
    }

    public static String getId(final ItemStack itemStack) {
        if (!itemStack.hasItemMeta()) return null;
        final PersistentDataContainer container = itemStack.getItemMeta().getPersistentDataContainer();

        return container.get(new NamespacedKey(HDrawer.get(), DRAWER_ID), PersistentDataType.STRING);
    }

    public static void setId(final ItemStack itemStack, final String id) {
        if (itemStack == null || id == null) return;

        final ItemMeta meta = itemStack.getItemMeta();
        final PersistentDataContainer container = meta.getPersistentDataContainer();

        container.set(new NamespacedKey(HDrawer.get(), DRAWER_ID), PersistentDataType.STRING, id);

        itemStack.setItemMeta(meta);
    }

    public boolean canAccess(final Player player, final Location drawerLocation) {
        if (player.isOp() || this.drawerAccess == null) return true;

        return this.drawerAccess.canAccess(player, drawerLocation);
    }

    public void place(final Location location, final ItemStack drawerItem, final BlockFace face, final String id) {
        final Drawer drawer = new Drawer();
        drawer.setId(id);
        drawer.setLocation(location);
        drawer.setFace(face);
        drawer.build();

        final NamespacedKey contentKey = new NamespacedKey(HDrawer.get(), DRAWER_CONTENT);
        final ItemMeta meta = drawerItem.getItemMeta();
        final PersistentDataContainer container = meta.getPersistentDataContainer();

        final String base64 = container.get(contentKey, PersistentDataType.STRING);
        if (base64 != null) {
            drawer.setContent(Base64ItemStack.decode(base64));
        }

        this.drawers.put(LocationUtil.convert(location), drawer);
    }

    public boolean remove(final Location location) {
        final String stringLocation = LocationUtil.convert(location);
        final Drawer drawer = this.drawers.remove(stringLocation);
        if (drawer == null) return false;

        this.deleted.add(stringLocation);
        this.unsaved.remove(stringLocation);

        final DrawerConfig config = HDrawer.get().getConfiguration().getDrawerConfig(drawer.getId());
        final ItemStack toDrop = config.drawer().clone();

        //Stock in nbt inside
        if (HDrawer.get().getConfiguration().isShulkerMod()) {
            final ItemMeta meta = toDrop.getItemMeta();
            final PersistentDataContainer container = meta.getPersistentDataContainer();

            final String base64 = Base64ItemStack.encode(drawer.getContent());
            if (base64 == null) {
                this.dropDrawer(drawer, location);
                return true;
            }

            container.set(new NamespacedKey(HDrawer.get(), DRAWER_CONTENT), PersistentDataType.STRING, base64);
            toDrop.setItemMeta(meta);
        } else {
            this.dropDrawer(drawer, location);
        }

        //Drop drawer
        location.getWorld().dropItemNaturally(location, toDrop);

        drawer.delete();
        return true;
    }

    private void dropDrawer(final Drawer drawer, final Location location) {
        //Drop remaining items inside
        final World world = location.getWorld();
        for (final ItemStack content : drawer.getContent()) {
            int amount = content.getAmount();
            final ItemStack cloned = content.clone();

            while (amount > 0) {
                int current = amount;
                if (amount >= content.getMaxStackSize()) {
                    current = content.getMaxStackSize();
                    amount -= current;
                } else {
                    amount = 0;
                }

                cloned.setAmount(current);
                world.dropItemNaturally(location, cloned);
            }
        }
    }

    public void save(final Location location) {
        this.unsaved.add(LocationUtil.convert(location));
    }

    public void hideAll() {
        for (final Drawer drawer : this.drawers.values()) {
            drawer.delete();
        }
    }

    public void buildAll() {
        for (final Drawer drawer : this.drawers.values()) {
            drawer.build();
        }
    }

    public void databaseClear() {
        this.unsaved.clear();
        this.deleted.clear();
    }

    public boolean isDrawer(final Block block) {
        for (final String stringLocation : this.drawers.keySet()) {
            final Location location = LocationUtil.convert(stringLocation);
            if (location == null) continue;

            if (location.getBlock().equals(block)) return true;
        }

        return false;
    }

    public List<String> getDeleted() {
        return this.deleted;
    }

    public List<String> getUnsaved() {
        return this.unsaved;
    }

    public Drawer getDrawer(final Location location) {
        return this.drawers.get(LocationUtil.convert(location));
    }

    public Drawer getDrawer(final String location){
        return this.drawers.get(location);
    }

    public boolean exist(final Location location) {
        return this.drawers.containsKey(LocationUtil.convert(location));
    }

    public Map<String, Drawer> getDrawers() {
        return this.drawers;
    }

    public void setDrawers(final Map<String, Drawer> drawers) {
        this.drawers = drawers;

        for (final Map.Entry<String, Drawer> entry : new HashSet<>(drawers.entrySet())) {
            final String location = entry.getKey();

            if (entry.getValue() == null) {
                drawers.remove(location);
                this.deleted.add(location);
            }
        }
    }
}

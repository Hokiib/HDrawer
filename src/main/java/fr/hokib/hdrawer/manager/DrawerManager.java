package fr.hokib.hdrawer.manager;

import fr.hokib.hdrawer.HDrawer;
import fr.hokib.hdrawer.config.drawer.DrawerConfig;
import fr.hokib.hdrawer.manager.data.Drawer;
import fr.hokib.hdrawer.util.Base64ItemStack;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DrawerManager {

    private static final String DRAWER_ID = "drawer-id";
    private static final String DRAWER_CONTENT = "drawer-content";
    private final List<Location> unsaved = new ArrayList<>();
    private final List<Location> deleted = new ArrayList<>();
    private Map<Location, Drawer> drawers = new HashMap<>();

    public static String getId(final ItemStack itemStack) {
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

        this.drawers.put(location, drawer);
    }

    public void remove(final Location location) {
        final Drawer drawer = this.drawers.remove(location);
        if (drawer == null) return;

        this.deleted.add(location);
        this.unsaved.remove(location);

        final DrawerConfig config = HDrawer.get().getConfiguration().getDrawerConfig(drawer.getId());
        final ItemStack toDrop = config.drawer().clone();

        //Stock in nbt inside
        if (HDrawer.get().getConfiguration().isShulkerMod()) {
            final ItemMeta meta = toDrop.getItemMeta();
            final PersistentDataContainer container = meta.getPersistentDataContainer();

            final String base64 = Base64ItemStack.encode(drawer.getContent());
            if (base64 == null) {
                this.dropDrawer(drawer, location);
                return;
            }

            container.set(new NamespacedKey(HDrawer.get(), DRAWER_CONTENT), PersistentDataType.STRING, base64);
            toDrop.setItemMeta(meta);
        } else {
            this.dropDrawer(drawer, location);
        }

        //Drop drawer
        location.getWorld().dropItemNaturally(location, toDrop);

        drawer.delete();
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
        this.unsaved.add(location);
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
        for (final Location location : this.drawers.keySet()) {
            if (location.getBlock().equals(block)) return true;
        }

        return false;
    }

    public List<Location> getDeleted() {
        return this.deleted;
    }

    public List<Location> getUnsaved() {
        return this.unsaved;
    }

    public Drawer getDrawer(final Location location) {
        return this.drawers.get(location);
    }

    public Map<Location, Drawer> getDrawers() {
        return this.drawers;
    }

    public void setDrawers(final Map<Location, Drawer> drawers) {
        this.drawers = drawers;
    }


}

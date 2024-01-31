package fr.hokib.hdrawer.manager.hopper;

import fr.hokib.hdrawer.HDrawer;
import fr.hokib.hdrawer.config.Config;
import fr.hokib.hdrawer.manager.DrawerManager;
import fr.hokib.hdrawer.manager.drawer.Drawer;
import fr.hokib.hdrawer.util.inventory.InventoryUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Hopper;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class HopperManager implements Runnable {

    private static final BlockFace[] FACES = new BlockFace[]{BlockFace.EAST, BlockFace.WEST,
            BlockFace.NORTH, BlockFace.SOUTH, BlockFace.UP};
    private final DrawerManager manager;
    private final Config config;

    public HopperManager(final HDrawer main) {
        this.manager = main.getManager();
        this.config = main.getConfiguration();
    }

    @Override
    public void run() {
        for (final Drawer drawer : this.manager.getDrawers().values()) {
            if (!drawer.getLocation().isChunkLoaded()) continue;

            final Optional<Hopper> bottom = this.getBottom(drawer);
            bottom.ifPresent(hopper -> this.transit(drawer, hopper));

            this.transit(this.getSides(drawer), drawer);
        }
    }


    /**
     * From Drawer to bottom hopper
     */
    private void transit(final Drawer drawer, final Hopper hopper) {
        final Inventory inv = hopper.getInventory();

        final List<ItemStack> content = drawer.getContent();
        for (int i = 0; i < content.size(); i++) {
            final ItemStack itemStack = content.get(i);
            if (itemStack.getType().isAir()) continue;

            final int baseAmount = itemStack.getAmount();

            final ItemStack cloned = itemStack.clone();
            cloned.setAmount(1);

            final ItemStack remain = InventoryUtil.addItem(cloned, inv);
            if (remain == null) {
                itemStack.setAmount(baseAmount - 1);
            } else {
                final int diff = 1 - remain.getAmount();
                if (diff == 0) {
                    continue;
                }

                itemStack.setAmount(baseAmount - diff);
            }

            drawer.update(itemStack, i);
            break;
        }
    }

    /**
     * From sides hoppers to Drawer
     */
    private void transit(final List<Hopper> hoppers, final Drawer drawer) {
        final int limit = this.config.getDrawerConfig(drawer.getId()).limit();

        for (final Hopper hopper : hoppers) {
            ItemStack itemStack = null;
            for (final ItemStack content : hopper.getInventory().getContents()) {
                if (content == null) continue;

                final Material material = content.getType();
                if (material.isAir()) continue;

                if (this.config.isBlacklisted(material)) continue;

                itemStack = content;
                break;
            }
            if (itemStack == null) continue;

            final ItemStack cloned = itemStack.clone();
            cloned.setAmount(1);

            final List<ItemStack> content = drawer.getContent();
            boolean alreadyFound = false;

            for (final ItemStack drawerStack : content) {
                if (drawerStack.isSimilar(itemStack)) {
                    drawerStack.setAmount(drawerStack.getAmount() + 1);
                    itemStack.setAmount(itemStack.getAmount() - 1);

                    alreadyFound = true;
                    break;
                }
            }

            if (alreadyFound) continue;

            for (int i = 0; i < content.size(); i++) {
                final ItemStack drawerStack = content.get(i);

                final int remainingSpace = limit - drawerStack.getAmount();
                if (remainingSpace <= 0) continue;

                if (drawerStack.getType().isAir()) {
                    content.set(i, cloned);

                    itemStack.setAmount(itemStack.getAmount() - 1);
                } else {
                    if (!drawerStack.isSimilar(itemStack)) {
                        continue;
                    }

                    drawerStack.setAmount(drawerStack.getAmount() + 1);

                    itemStack.setAmount(itemStack.getAmount() - 1);
                }

                break;
            }
        }

        drawer.update();
    }

    /**
     * The one hopper where items should be moved from the drawer
     */
    private Optional<Hopper> getBottom(final Drawer drawer) {
        final Block block = drawer.getLocation().getBlock();
        final Block bottom = block.getRelative(BlockFace.DOWN);

        if (!(bottom.getBlockData() instanceof org.bukkit.block.data.type.Hopper hopper)) return Optional.empty();
        if (!hopper.isEnabled()) return Optional.empty();

        return Optional.of((Hopper) bottom.getState());
    }

    /**
     * Get sides hoppers without the face one
     */
    private List<Hopper> getSides(final Drawer drawer) {
        final Location location = drawer.getLocation();
        final Block block = location.getBlock();
        final List<Hopper> sides = new ArrayList<>();

        for (final BlockFace face : FACES) {
            //So that hoppers in the drawer face shouldn't work
            if (face.equals(drawer.getFace())) continue;

            final Block relative = block.getRelative(face);
            if (!(relative.getBlockData() instanceof org.bukkit.block.data.type.Hopper hopper)) continue;
            if (!hopper.isEnabled()) continue;

            //Hopper may face the Drawer
            if (!relative.getRelative(hopper.getFacing()).getLocation().equals(location)) {
                continue;
            }

            sides.add((Hopper) relative.getState());
        }

        return sides;
    }
}

package fr.hokib.hdrawer.manager.hopper;

import fr.hokib.hdrawer.HDrawer;
import fr.hokib.hdrawer.config.Config;
import fr.hokib.hdrawer.manager.DrawerManager;
import fr.hokib.hdrawer.manager.drawer.Drawer;
import fr.hokib.hdrawer.util.inventory.InventoryUtil;
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

        int remaining = this.config.getHopperTransit();

        final List<ItemStack> content = drawer.getContent();
        for (int i = 0; i < content.size(); i++) {
            final ItemStack itemStack = content.get(i);
            if (itemStack.getType().isAir()) continue;

            final int baseAmount = itemStack.getAmount();

            final ItemStack cloned = itemStack.clone();
            cloned.setAmount(Math.min(cloned.getAmount(), remaining));
            final int amount = cloned.getAmount();

            final ItemStack remain = InventoryUtil.addItem(cloned, inv);
            if (remain == null) {
                remaining -= amount;
                itemStack.setAmount(baseAmount - amount);

                drawer.update(itemStack, i);
            } else {
                final int diff = amount - remain.getAmount();
                if (diff == 0) continue;

                remaining -= diff;
                itemStack.setAmount(baseAmount - diff);

                drawer.update(itemStack, i);
            }

            if (remaining == 0) break;
        }
    }

    /**
     * From sides hoppers to Drawer
     */
    private void transit(final List<Hopper> hoppers, final Drawer drawer) {
        for (final Hopper hopper : hoppers) {
            int remaining = this.config.getHopperTransit();
            for (final ItemStack itemStack : hopper.getInventory().getContents()) {
                if (itemStack == null || itemStack.getType().isAir()) continue;
                final ItemStack cloned = itemStack.clone();
                cloned.setAmount(Math.min(remaining, cloned.getAmount()));
                final int amount = cloned.getAmount();

                final List<ItemStack> content = drawer.getContent();
                for (int i = 0; i < content.size(); i++) {
                    final ItemStack drawerStack = content.get(i);

                    if (drawerStack.getType().isAir()) {
                        content.set(i, cloned);
                        remaining -= amount;

                        drawer.update(cloned, i);
                        break;
                    } else {
                        if (!drawerStack.isSimilar(itemStack)) continue;

                        drawerStack.setAmount(drawerStack.getAmount() + amount);
                        remaining -= amount;

                        drawer.update(drawerStack, i);
                    }
                }

                if (remaining == 0) break;
            }
        }
    }

    /**
     * The one hopper where items should be moved from the drawer
     */
    private Optional<Hopper> getBottom(final Drawer drawer) {
        final Block block = drawer.getLocation().getBlock();
        final Block bottom = block.getRelative(BlockFace.DOWN);
        if (!bottom.getType().equals(Material.HOPPER)) return Optional.empty();

        return Optional.of((Hopper) bottom);
    }

    /**
     * Get sides hoppers without the face one
     */
    private List<Hopper> getSides(final Drawer drawer) {
        final Block block = drawer.getLocation().getBlock();
        final BlockFace opposite = drawer.getFace().getOppositeFace();

        final List<Hopper> sides = new ArrayList<>();

        for (final BlockFace face : FACES) {
            //So that hoppers in the drawer face shouldn't work
            if (face.equals(opposite)) continue;

            final Block relative = block.getRelative(face);
            if (!(relative.getBlockData() instanceof org.bukkit.block.data.type.Hopper hopper)) continue;

            //Hopper may face the Drawer
            if (relative.getRelative(hopper.getFacing()) != block) continue;

            sides.add((Hopper) relative);
        }

        return sides;
    }
}

package fr.hokib.hdrawer.manager.data.storage;

import fr.hokib.hdrawer.HDrawer;
import fr.hokib.hdrawer.config.drawer.DrawerConfig;
import fr.hokib.hdrawer.manager.data.Drawer;
import fr.hokib.hdrawer.util.Base64ItemStack;
import fr.hokib.hdrawer.util.location.LocationUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Directional;

public record DrawerData(String content, String id, String location, BlockFace face) {

    public static DrawerData from(final Drawer drawer) {

        return new DrawerData(Base64ItemStack.encode(drawer.getContent()), drawer.getId(),
                LocationUtil.convert(drawer.getLocation()),
                drawer.getFace());
    }

    public Drawer to() {
        final DrawerConfig config = HDrawer.get().getConfiguration().getDrawerConfig(this.id);
        if (config == null) return null;

        final Location location = LocationUtil.convert(this.location);
        final Material type = config.drawer().getType();
        final Block block = location.getBlock();

        Bukkit.getScheduler().runTask(HDrawer.get(), () -> {
            //If material in config has changed
            if (type != block.getType()) {
                block.setType(type);
                if (block.getBlockData() instanceof Directional directional) {
                    directional.setFacing(this.face);
                    block.setBlockData(directional);
                    block.getState().update();
                }
            }
        });


        final Drawer drawer = new Drawer();
        drawer.setId(this.id);

        drawer.setLocation(location);
        drawer.setFace(this.face);

        drawer.build();
        drawer.setContent(Base64ItemStack.decode(this.content));

        return drawer;
    }
}

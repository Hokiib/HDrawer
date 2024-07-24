package fr.hokib.hdrawer.manager.drawer.data;

import org.bukkit.Location;
import org.bukkit.block.BlockFace;

import fr.hokib.hdrawer.HDrawer;
import fr.hokib.hdrawer.config.drawer.DrawerConfig;
import fr.hokib.hdrawer.manager.drawer.Drawer;
import fr.hokib.hdrawer.scheduler.Scheduler;
import fr.hokib.hdrawer.util.Base64ItemStack;
import fr.hokib.hdrawer.util.location.LocationUtil;

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
        if(location == null || location.getWorld() == null) return null;

        final Drawer drawer = new Drawer();
        drawer.setId(this.id);
        drawer.setLocation(location);
        drawer.setFace(this.face);

        Scheduler.getScheduler().runInRegion(location, () -> {
            drawer.build();
            drawer.setContent(Base64ItemStack.decode(this.content));
        });

        return drawer;
    }
}

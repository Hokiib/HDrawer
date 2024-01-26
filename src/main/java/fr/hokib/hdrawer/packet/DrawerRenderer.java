package fr.hokib.hdrawer.packet;

import fr.hokib.hdrawer.HDrawer;
import fr.hokib.hdrawer.config.Config;
import fr.hokib.hdrawer.manager.DrawerManager;
import fr.hokib.hdrawer.manager.data.Drawer;
import fr.hokib.hdrawer.task.AsyncTask;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.ArrayList;

public class DrawerRenderer extends AsyncTask implements Listener {
    private final DrawerManager manager;
    private final Config config;

    public DrawerRenderer(final HDrawer main) {
        super(3L);
        this.manager = main.getManager();
        this.config = main.getConfiguration();
    }

    @EventHandler
    private void onQuit(PlayerQuitEvent event) {
        for (final Drawer drawer : this.manager.getDrawers().values()) {
            drawer.removeViewer(event.getPlayer());
        }
    }

    @Override
    public void run() {
        if (HDrawer.get().isDisabled()) return;
        try {
            final int distance = this.config.getDistance();

            for (final Player player : Bukkit.getOnlinePlayers()) {

                for (final Drawer drawer : new ArrayList<>(this.manager.getDrawers().values())) {
                    if (!this.canSee(player, drawer, distance)) {
                        drawer.removeViewer(player);
                        continue;
                    }
                    drawer.addViewer(player);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean canSee(final Player player, final Drawer drawer, final int distance) {
        final Location location = player.getLocation();
        final Location drawerLocation = drawer.getLocation();

        if (!location.getWorld().equals(drawerLocation.getWorld())) return false;

        return location.distance(drawerLocation) <= distance;
    }
}

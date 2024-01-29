package fr.hokib.hdrawer.manager.access;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public interface DrawerAccess {

    boolean canAccess(final Player player, final Location drawerLocation);
}

package fr.hokib.hdrawer.manager.access.impl;

import com.massivecraft.factions.*;
import fr.hokib.hdrawer.manager.access.DrawerAccess;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class FactionsAccess implements DrawerAccess {
    @Override
    public boolean canAccess(Player player, Location drawerLocation) {
        final FLocation fLocation = new FLocation(drawerLocation);
        final Faction faction = Board.getInstance().getFactionAt(fLocation);
        if (faction == null) return false;
        if (faction.isWilderness()) return true;

        final FPlayer fPlayer = FPlayers.getInstance().getByPlayer(player);
        if (fPlayer == null) return false;

        return faction.getFPlayers().contains(fPlayer);

    }
}

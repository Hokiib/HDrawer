package fr.hokib.hdrawer.manager.access.impl;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import fr.hokib.hdrawer.manager.access.DrawerAccess;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.UUID;

public class SuperiorSkyBlockAccess implements DrawerAccess {
    @Override
    public boolean canAccess(Player player, Location drawerLocation) {
        //If the player isn't recognized as a SuperiorPlayer, he may not access to the drawer
        final SuperiorPlayer superiorPlayer = SuperiorSkyblockAPI.getPlayer(player);
        if (superiorPlayer == null) return false;

        //If the island doesn't exist at this location, by default we will cancel
        final Island island = SuperiorSkyblockAPI.getIslandAt(drawerLocation);
        if (island == null) return false;

        final UUID uuid = player.getUniqueId();
        //The boolean in parameter of this method is "includeOwner"
        for (final SuperiorPlayer member : island.getIslandMembers(true)) {
            if (member.getUniqueId().equals(uuid)) return true;
        }

        return false;
    }
}

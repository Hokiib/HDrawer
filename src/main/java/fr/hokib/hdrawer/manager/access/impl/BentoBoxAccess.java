package fr.hokib.hdrawer.manager.access.impl;

import fr.hokib.hdrawer.manager.access.DrawerAccess;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.database.objects.Island;

import java.util.Optional;

public class BentoBoxAccess implements DrawerAccess {
    @Override
    public boolean canAccess(Player player, Location drawerLocation) {
        final BentoBox api = BentoBox.getInstance();

        final Optional<Island> island = api.getIslands().getIslandAt(drawerLocation);

        return island.map(value -> value.getMemberSet().contains(player.getUniqueId()))
                .orElse(false);
    }
}

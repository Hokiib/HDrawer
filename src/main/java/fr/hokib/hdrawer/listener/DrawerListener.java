package fr.hokib.hdrawer.listener;

import fr.hokib.hdrawer.HDrawer;
import fr.hokib.hdrawer.manager.DrawerManager;
import fr.hokib.hdrawer.manager.data.Drawer;
import fr.hokib.hdrawer.util.ColorUtil;
import fr.hokib.hdrawer.util.location.LocationUtil;
import fr.hokib.hdrawer.util.update.UpdateChecker;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.util.RayTraceResult;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class DrawerListener implements Listener {

    private final DrawerManager manager;
    private final Set<UUID> updateInfoSent = new HashSet<>();
    private final Set<Location> toClear = new HashSet<>();

    public DrawerListener(final HDrawer main) {
        this.manager = main.getManager();
    }

    @EventHandler
    private void onJoin(PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        if (!player.isOp() || HDrawer.get().isUpdated()) return;

        final UUID uuid = player.getUniqueId();
        if (this.updateInfoSent.contains(uuid)) return;

        player.sendMessage(ColorUtil.color("§8------ &#E747FBH&#D042FCD&#B83DFCr&#A138FDa&#8A33FEw&#722EFEe&#5B29FFr§8 ------"));
        player.sendMessage("§r");
        player.sendMessage("§8A new §bupdate §7is available !");
        player.sendMessage("§r");
        player.sendMessage("§8§l» §d" + UpdateChecker.RESOURCE_URL);
        player.sendMessage("§8--------------------");
        this.updateInfoSent.add(uuid);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onPlace(BlockPlaceEvent event) {
        if (event.isCancelled()) return;

        final Block block = event.getBlock();
        final ItemStack drawerItem = event.getItemInHand();
        final String id = DrawerManager.getId(drawerItem);
        if (id == null) return;

        final BlockFace playerFace = LocationUtil.getBlockFace(event.getPlayer().getYaw()).getOppositeFace();
        final Location location = block.getLocation();

        if (block.getBlockData() instanceof Directional directional) {
            final BlockFace face = directional.getFacing();
            if (face == BlockFace.UP || face == BlockFace.DOWN) {
                directional.setFacing(playerFace);
                block.setBlockData(directional);
                block.getState().update();
            }
        }

        this.manager.place(location, drawerItem, playerFace, id);
        this.manager.save(location);
    }

    @EventHandler
    private void onExtend(BlockPistonExtendEvent event) {
        if (event.isCancelled()) return;
        for (final Block block : event.getBlocks()) {
            if (this.manager.isDrawer(block)) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler
    private void onRetract(BlockPistonRetractEvent event) {
        if (event.isCancelled()) return;
        for (final Block block : event.getBlocks()) {
            if (this.manager.isDrawer(block)) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onBreak(BlockBreakEvent event) {
        if (event.isCancelled()) return;

        final Location location = event.getBlock().getLocation();
        this.manager.remove(location);
        this.toClear.add(location);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private void onDrop(BlockDropItemEvent event) {
        if (event.isCancelled()) return;

        if (!this.toClear.remove(event.getBlock().getLocation())) return;

        event.getItems().clear();
    }

    @EventHandler
    private void onBurn(BlockBurnEvent event) {
        if (event.isCancelled()) return;

        final Location location = event.getBlock().getLocation();
        this.manager.remove(event.getBlock().getLocation());
        this.toClear.add(location);
    }

    @EventHandler
    private void onExplode(BlockExplodeEvent event) {
        if (event.isCancelled()) return;

        final Location location = event.getBlock().getLocation();
        this.manager.remove(event.getBlock().getLocation());
        this.toClear.add(location);
    }

    @EventHandler
    private void onInteract(PlayerInteractEvent event) {
        final Block block = event.getClickedBlock();
        if (block == null) return;

        final Location location = block.getLocation();
        final Drawer drawer = this.manager.getDrawer(location);
        if (drawer == null) return;

        final Action action = event.getAction();
        final Player player = event.getPlayer();

        if (action == Action.RIGHT_CLICK_BLOCK) {
            final PlayerInventory inventory = player.getInventory();
            final Material main = inventory.getItemInMainHand().getType();
            final Material off = inventory.getItemInOffHand().getType();

            //To place block next to the drawer
            boolean cancel = player.isSneaking() && (main.isBlock() && !main.isEmpty() || (main.isEmpty() && off.isBlock()));
            event.setCancelled(!cancel);
        }

        if (event.getBlockFace() != drawer.getFace()) {
            return;
        }

        event.setCancelled(true);

        final RayTraceResult result = player.rayTraceBlocks(5);
        if (result == null) return;

        final Location point = result.getHitPosition().toLocation(player.getWorld());

        switch (action) {
            case RIGHT_CLICK_BLOCK -> {
                final ItemStack itemStack = event.getItem();
                if (itemStack == null) return;
                if (HDrawer.get().getConfiguration().isBlacklisted(itemStack.getType()))
                    return;

                if (drawer.insertContent(player, itemStack, point)) this.manager.save(location);
            }
            case LEFT_CLICK_BLOCK -> {
                //Destroy it if there is nothing else
                if (drawer.isEmpty()) {
                    event.setCancelled(false);
                    return;
                }

                final boolean removed = drawer.removeContent(player, point);
                if (removed) this.manager.save(location);

                event.setCancelled(removed);
            }
        }
    }
}

package fr.hokib.hdrawer.listener;

import fr.hokib.hdrawer.HDrawer;
import fr.hokib.hdrawer.manager.DrawerManager;
import fr.hokib.hdrawer.manager.drawer.Drawer;
import fr.hokib.hdrawer.util.ColorUtil;
import fr.hokib.hdrawer.util.location.LocationUtil;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
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
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.util.RayTraceResult;

import java.util.*;

public class DrawerListener implements Listener {

    public static HDrawer main;
    private final DrawerManager manager;
    private final Set<Location> toClear = new HashSet<>();
    private final Map<UUID, Long> lastLeftClick = new HashMap<>();

    public DrawerListener(final HDrawer main) {
        this.manager = main.getManager();
        this.main = main;
    }

    public static void sendUpdateInformation(Player player) {
        if (!player.isOp() || HDrawer.get().isUpdated()) return;

        player.sendMessage(ColorUtil.color("§8------ &#E747FBH&#D042FCD&#B83DFCr&#A138FDa&#8A33FEw&#722EFEe&#5B29FFr§8 ------"));
        player.sendMessage("§r");
        player.sendMessage("§7A new §bupdate §7is available ! §8(Version: §b" + main.updater.getVersion() + "§8)");
        player.sendMessage("§r");
        TextComponent spigotLink = new net.md_5.bungee.api.chat.TextComponent("§8§l» §dSpigot Link");
        spigotLink.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://www.spigotmc.org/resources/hdrawer.114799/"));

        player.spigot().sendMessage(spigotLink);

        TextComponent autoUpdate = new net.md_5.bungee.api.chat.TextComponent("§8§l» §dAutomatic update");
        autoUpdate.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/hdrawer update"));
        autoUpdate.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText("§b/hdrawer update")));

        player.spigot().sendMessage(autoUpdate);

        player.sendMessage("§r");
        player.sendMessage("§8--------------------");

    }

    @EventHandler
    private void onJoin(PlayerJoinEvent event) {
        final Player player = event.getPlayer();

        sendUpdateInformation(player);

    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onPlace(BlockPlaceEvent event) {
        if (event.isCancelled()) return;

        final Block block = event.getBlock();
        final ItemStack drawerItem = event.getItemInHand();
        final String id = DrawerManager.getId(drawerItem);
        if (id == null) return;

        final BlockFace playerFace = LocationUtil.getBlockFace(event.getPlayer().getLocation().getYaw()).getOppositeFace();
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
        if (this.manager.remove(location)) {
            this.toClear.add(location);
        }
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
    private void onHopper(InventoryMoveItemEvent event) {
        if (event.isCancelled()) return;

        final Location source = event.getSource().getLocation();
        if (this.manager.exist(source)) {
            event.setCancelled(true);
            return;
        }

        final Location destination = event.getDestination().getLocation();
        if (this.manager.exist(destination)) {
            event.setCancelled(true);
            return;
        }
    }

    @EventHandler
    private void onInteract(PlayerInteractEvent event) {
        final Block block = event.getClickedBlock();
        if (block == null) return;

        final Location location = block.getLocation();
        final Drawer drawer = this.manager.getDrawer(location);
        if (drawer == null) return;

        if (event.getHand() == EquipmentSlot.OFF_HAND) {
            event.setCancelled(true);
            return;
        }

        final Player player = event.getPlayer();
        final UUID uuid = player.getUniqueId();
        final long now = System.currentTimeMillis();

        if (now - this.lastLeftClick.getOrDefault(uuid, 0L) <= 55) {
            event.setCancelled(true);
            return;
        }
        this.lastLeftClick.put(player.getUniqueId(), now);

        if (!this.manager.canAccess(player, location)) {
            event.setCancelled(true);
            return;
        }

        final Action action = event.getAction();

        if (action == Action.RIGHT_CLICK_BLOCK) {
            final PlayerInventory inventory = player.getInventory();
            final Material main = inventory.getItemInMainHand().getType();
            final Material off = inventory.getItemInOffHand().getType();

            //To place block next to the drawer
            boolean cancel = player.isSneaking() && (main.isBlock() && !main.isAir() || (main.isAir() && off.isBlock()));
            event.setCancelled(!cancel);
        }

        if (event.getBlockFace() != drawer.getFace()) return;

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

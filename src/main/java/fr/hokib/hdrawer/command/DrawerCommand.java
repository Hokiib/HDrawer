package fr.hokib.hdrawer.command;

import fr.hokib.hdrawer.HDrawer;
import fr.hokib.hdrawer.config.drawer.DrawerConfig;
import fr.hokib.hdrawer.database.logger.DatabaseLogger;
import fr.hokib.hdrawer.util.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class DrawerCommand implements CommandExecutor, TabCompleter {

    private final HDrawer main;

    public DrawerCommand(final HDrawer main) {
        this.main = main;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            return this.sendHelp(sender);
        }

        switch (args[0]) {
            case "reload" -> {
                final DatabaseLogger logger = DatabaseLogger.start("Plugin reloaded");
                this.main.reload();
                logger.stop();
                sender.sendMessage(ColorUtil.color("&#FB3DF9P&#F13DF9l&#E63DFAu&#DC3DFAg&#D13CFBi&#C73CFBn &#BC3CFCr&#B23CFCe&#A73CFCl&#9D3CFDo&#923CFDa&#883BFEd&#7D3BFEe&#733BFFd &#683BFF!"));
            }
            case "save" -> {
                this.main.getDatabase().save(this.main.getManager());
                sender.sendMessage(ColorUtil.color("&#FB3DF9P&#EE3DFAl&#E03DFAu&#D33CFBg&#C63CFBi&#B83CFCn &#AB3CFCs&#9D3CFDa&#903CFDv&#833BFEe&#753BFEd &#683BFF!"));
            }
            case "give" -> {
                if (args.length == 1) return this.sendHelp(sender);
                final String id = args[1];
                final DrawerConfig config = this.main.getConfiguration().getDrawerConfig(id);
                if (config == null) {
                    sender.sendMessage("§cThis drawer doesn't exist ! (Did you reloaded the plugin ? /drawer reload)");
                    return true;
                }

                Player target = null;
                if (args.length == 2) {
                    if (!(sender instanceof Player player)) {
                        sender.sendMessage("§cYou must be a player to perform this command !");
                        return true;
                    } else {
                        target = player;
                    }
                }
                if (args.length == 3) {
                    target = Bukkit.getPlayer(args[2]);
                }

                if (target == null) {
                    sender.sendMessage("§cThis player doesn't exist !");
                    return true;
                }

                final ItemStack drawer = config.drawer().clone();
                if (!target.getInventory().addItem(drawer).isEmpty()) {
                    target.getWorld().dropItem(target.getLocation(), drawer);
                }
                sender.sendMessage("§aSuccessfully gived a drawer to §f" + target.getName() + " §a!");
            }
            default -> {
                return this.sendHelp(sender);
            }
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) return List.of("reload", "save", "give", "help");

        if (args.length >= 2 && args[0].equals("give")) {
            if (args.length == 2) return this.main.getConfiguration().getDrawersId();
            if (args.length == 3) return null;
        }

        return Collections.emptyList();
    }

    public boolean sendHelp(final CommandSender sender) {
        sender.sendMessage(" ");
        sender.sendMessage(ColorUtil.color("§8------ &#E747FBH&#D042FCD&#B83DFCr&#A138FDa&#8A33FEw&#722EFEe&#5B29FFr§8 ------"));
        sender.sendMessage(" ");
        sender.sendMessage("§8/§ddrawer §9reload");
        sender.sendMessage("§8/§ddrawer §9save");
        sender.sendMessage("§8/§ddrawer §9give §8<§9id§8> §8(§9player§8)");
        sender.sendMessage(" ");
        sender.sendMessage(ColorUtil.color("&#E350EAA &#DE4DEBp&#D94BECl&#D348EEu&#CE45EFg&#C942F0i&#C440F1n &#BE3DF3m&#B93AF4a&#B438F5d&#AF35F6e &#A932F8b&#A430F9y &#9F2DFAH&#9A2AFBo&#9427FDk&#8F25FEi&#8A22FFb"));
        sender.sendMessage(" ");
        sender.sendMessage("§8§l» §9" + this.main.getDescription().getWebsite() + " §8§l«");
        sender.sendMessage("§8--------------------");
        return true;
    }
}

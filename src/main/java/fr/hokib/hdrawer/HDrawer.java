package fr.hokib.hdrawer;

import fr.hokib.hdrawer.command.DrawerCommand;
import fr.hokib.hdrawer.config.Config;
import fr.hokib.hdrawer.config.database.DatabaseConfig;
import fr.hokib.hdrawer.database.Database;
import fr.hokib.hdrawer.database.logger.DatabaseLogger;
import fr.hokib.hdrawer.database.task.SaveTask;
import fr.hokib.hdrawer.database.type.DatabaseType;
import fr.hokib.hdrawer.listener.DrawerListener;
import fr.hokib.hdrawer.manager.DrawerManager;
import fr.hokib.hdrawer.packet.DrawerRenderer;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class HDrawer extends JavaPlugin {

    private static HDrawer instance;
    private Config config;
    private Database database;
    private SaveTask saveTask;
    private DrawerManager manager;
    private DrawerRenderer renderer;
    private boolean disabled = false;

    public static HDrawer get() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;
        this.reload();

        this.saveTask = new SaveTask(this);
        this.manager = new DrawerManager();
        this.loadDatabase();

        this.renderer = new DrawerRenderer(this);

        final DrawerCommand drawerCommand = new DrawerCommand(this);
        final PluginCommand command = this.getCommand("drawer");
        command.setExecutor(drawerCommand);
        command.setTabCompleter(drawerCommand);

        Bukkit.getPluginManager().registerEvents(new DrawerListener(this), this);
        Bukkit.getPluginManager().registerEvents(this.renderer, this);
    }

    public void reload() {
        //Load config
        if (this.config == null) {
            this.config = new Config();
        }

        this.saveDefaultConfig();
        this.reloadConfig();
        this.config.reload(this.getConfig());

        final DatabaseConfig databaseConfig = this.config.getDatabaseConfig();

        if (this.manager == null || this.database == null) return;
        this.disabled = true;

        this.manager.hideAll();

        //Unload database
        if (!DatabaseType.equals(databaseConfig.type(), this.database)) {
            Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
                this.database.save(this.manager);
                this.database.unload();

                this.manager.getDrawers().clear();

                this.loadDatabase();
                this.manager.buildAll();
                this.disabled = false;
            });
        } else {
            this.manager.buildAll();
            this.disabled = false;
        }
    }

    private void loadDatabase() {
        Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
            final DatabaseConfig databaseConfig = this.config.getDatabaseConfig();
            this.database = DatabaseType.from(databaseConfig.type());

            final DatabaseLogger logger = DatabaseLogger.start("Database loaded");
            this.database.load(databaseConfig);
            logger.stop();
        });
    }

    @Override
    public void onDisable() {
        this.disabled = true;

        if (this.manager != null) this.manager.hideAll();
        if (this.database != null) {
            this.database.save(this.manager);
            this.database.unload();
        }
        if (this.saveTask != null) this.saveTask.stop();
        if (this.renderer != null) this.renderer.stop();
        if (this.config != null) this.config.unload();
    }

    public boolean isDisabled() {
        return this.disabled;
    }

    public DrawerManager getManager() {
        return this.manager;
    }

    public Database getDatabase() {
        return this.database;
    }

    public Config getConfiguration() {
        return this.config;
    }
}

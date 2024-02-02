package fr.hokib.hdrawer;

import fr.hokib.hdrawer.command.DrawerCommand;
import fr.hokib.hdrawer.config.Config;
import fr.hokib.hdrawer.config.database.DatabaseConfig;
import fr.hokib.hdrawer.database.Database;
import fr.hokib.hdrawer.database.logger.DatabaseLogger;
import fr.hokib.hdrawer.database.task.SaveTask;
import fr.hokib.hdrawer.database.type.DatabaseType;
import fr.hokib.hdrawer.lib.HDrawerDeps;
import fr.hokib.hdrawer.listener.DrawerListener;
import fr.hokib.hdrawer.manager.DrawerManager;
import fr.hokib.hdrawer.manager.hopper.HopperManager;
import fr.hokib.hdrawer.util.update.ComparableVersion;
import fr.hokib.hdrawer.util.update.UpdateChecker;
import fr.hokib.hdrawer.util.update.Version;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class HDrawer extends JavaPlugin {

    private static HDrawer instance;
    private Config config;
    private Database database;
    private SaveTask saveTask;
    private DrawerManager manager;
    private HopperManager hopperManager;
    private boolean updated = true;

    public static HDrawer get() {
        return instance;
    }

    @Override
    public void onLoad() {
        new HDrawerDeps(this).load();
    }

    @Override
    public void onEnable() {
        instance = this;

        final Version serverVersion = Version.getCurrentVersion();

        if (serverVersion.isOlderThan(Version.V1_19_4)) {
            this.getLogger().warning("Incompatible version ! (Install 1.19.4 -> 1.20.x)");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        this.getLogger().info("Loading " + serverVersion.name());

        //Plugin version
        UpdateChecker.getVersion(version -> {
            final ComparableVersion current = new ComparableVersion(this.getDescription().getVersion());
            final ComparableVersion resource = new ComparableVersion(version);
            final int compared = current.compareTo(resource);

            this.updated = compared > 0 || compared == 0;
        });

        this.reload(true);

        this.saveTask = new SaveTask(this);
        this.manager = new DrawerManager();
        this.loadDatabase();

        this.hopperManager = new HopperManager(this);

        //3item/s so 0,33 * 20L = 6,6
        Bukkit.getScheduler().runTaskTimer(this, this.hopperManager, 0, 7L);

        final DrawerCommand drawerCommand = new DrawerCommand(this);
        final PluginCommand command = this.getCommand("drawer");
        command.setExecutor(drawerCommand);
        command.setTabCompleter(drawerCommand);

        Bukkit.getPluginManager().registerEvents(new DrawerListener(this), this);
    }

    public void reload(final boolean enabling) {
        //Load config
        if (this.config == null) {
            this.config = new Config();
        }

        this.saveDefaultConfig();
        this.reloadConfig();
        this.config.reload(this.getConfig());

        final DatabaseConfig databaseConfig = this.config.getDatabaseConfig();

        if (this.manager == null || this.database == null) return;

        if (!enabling) {
            this.manager.hideAll();

            //Unload database
            if (!DatabaseType.equals(databaseConfig.type(), this.database)) {
                Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
                    this.database.save(this.manager);
                    this.database.unload();
                    this.manager.getDrawers().clear();

                    this.loadDatabase();
                    this.manager.buildAll();
                });
            } else {
                this.manager.buildAll();
            }
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
        if (this.manager != null) this.manager.hideAll();
        if (this.database != null) {
            this.database.save(this.manager);
            this.database.unload();
        }
        if (this.saveTask != null) this.saveTask.stop();
        if (this.config != null) this.config.unload();
    }

    public boolean isUpdated() {
        return this.updated;
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

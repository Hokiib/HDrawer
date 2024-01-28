package fr.hokib.hdrawer.config;

import fr.hokib.hdrawer.config.database.DatabaseConfig;
import fr.hokib.hdrawer.config.drawer.DrawerConfig;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;

public class Config {
    private final Map<String, DrawerConfig> drawers = new HashMap<>();
    private final Set<Material> blacklistedMaterials = new HashSet<>();
    private DatabaseConfig databaseConfig;
    private float distance;
    private boolean shulkerMod;
    private boolean toggleBorder;

    public void reload(FileConfiguration config) {
        this.databaseConfig = DatabaseConfig.fromConfig(config.getConfigurationSection("database"));

        this.distance = (float) (config.getDouble("drawer-visibility") / 50);
        this.shulkerMod = config.getBoolean("shulker-mod", false);
        this.toggleBorder = config.getBoolean("toggle-border", false);

        for (final String material : config.getStringList("blacklistedMaterials")) {
            try {
                this.blacklistedMaterials.add(Material.valueOf(material.toUpperCase()));
            } catch (IllegalArgumentException ignored) {
            }
        }

        final ConfigurationSection drawersSection = config.getConfigurationSection("drawers");
        if (drawersSection == null) return;

        this.drawers.clear();
        for (final String id : drawersSection.getKeys(false)) {
            final DrawerConfig drawerConfig = DrawerConfig.fromConfig(drawersSection.getConfigurationSection(id));
            this.drawers.put(id, drawerConfig);
        }
    }

    public void unload() {
        for (final DrawerConfig drawer : this.drawers.values()) {
            drawer.removeRecipe();
        }
    }

    public DatabaseConfig getDatabaseConfig() {
        return this.databaseConfig;
    }

    public boolean isShulkerMod() {
        return this.shulkerMod;
    }

    public boolean isToggleBorder() {
        return this.toggleBorder;
    }

    public boolean isBlacklisted(final Material material) {
        return this.blacklistedMaterials.contains(material);
    }

    public DrawerConfig getDrawerConfig(final String id) {
        return this.drawers.get(id);
    }

    public List<String> getDrawersId() {
        return this.drawers.keySet().stream().toList();
    }

    public float getDistance() {
        return this.distance;
    }
}

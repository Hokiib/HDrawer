package fr.hokib.hdrawer.config;

import fr.hokib.hdrawer.config.database.DatabaseConfig;
import fr.hokib.hdrawer.config.drawer.DrawerConfig;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;

public class Config {
    private final Map<String, DrawerConfig> drawers = new HashMap<>();
    public static final HashMap<String, List<Material>> crafts = new HashMap<>();
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

        final ConfigurationSection craftsItemsSection = config.getConfigurationSection("crafts");
        if (craftsItemsSection == null) return;

        for (final String id : craftsItemsSection.getKeys(false)) {
            List<Material> materialsList = new ArrayList<>();
            List<String> materialsStringList = craftsItemsSection.getStringList(id);

            System.out.println("Processing category: " + id + " with materials: " + materialsStringList);

            for (String material : materialsStringList) {
                try {
                    Material mat = Material.valueOf(material.toUpperCase().replace(" ", ""));
                    if (!materialsList.contains(mat)) {
                        materialsList.add(mat);
                    }
                } catch (IllegalArgumentException ignored) {
                    System.out.println("Invalid material: " + material);
                }
            }

            crafts.put(id, materialsList);
            System.out.println("Added material choice: " + materialsList + " to " + id);
        }

        final ConfigurationSection drawersSection = config.getConfigurationSection("drawers");
        if (drawersSection == null) return;

        this.drawers.clear();
        for (final String id2 : drawersSection.getKeys(false)) {
            final DrawerConfig drawerConfig = DrawerConfig.fromConfig(drawersSection.getConfigurationSection(id2));
            this.drawers.put(id2, drawerConfig);

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
    public float getDistance() {
        return this.distance;
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
}

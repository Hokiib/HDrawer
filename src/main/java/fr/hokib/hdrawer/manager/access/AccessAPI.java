package fr.hokib.hdrawer.manager.access;

import fr.hokib.hdrawer.HDrawer;
import fr.hokib.hdrawer.manager.access.impl.BentoBoxAccess;
import fr.hokib.hdrawer.manager.access.impl.FactionsAccess;
import fr.hokib.hdrawer.manager.access.impl.SuperiorSkyBlockAccess;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public enum AccessAPI {

    BENTO_BOX(BentoBoxAccess.class, "BentoBox"), SUPERIOR_SKYBLOCK(SuperiorSkyBlockAccess.class, "SuperiorSkyblock2"),
    FACTIONS(FactionsAccess.class, "Factions", "FactionsUUID");

    private final Class<? extends DrawerAccess> accessClass;
    private final String[] plugins;

    AccessAPI(final Class<? extends DrawerAccess> accessClass, final String... plugins) {
        this.accessClass = accessClass;
        this.plugins = plugins;
    }

    public static DrawerAccess from() {
        try {
            for (final Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
                for (final AccessAPI value : values()) {
                    final String name = plugin.getName();
                    for (final String pluginName : value.getPlugins()) {
                        if (name.equals(pluginName)) {
                            HDrawer.get().getLogger().info("We've found a compatible dependency (" + name + ")");
                            return value.accessClass.newInstance();
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public String[] getPlugins() {
        return this.plugins;
    }
}

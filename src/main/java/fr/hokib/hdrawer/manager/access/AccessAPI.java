package fr.hokib.hdrawer.manager.access;

import fr.hokib.hdrawer.HDrawer;
import fr.hokib.hdrawer.manager.access.impl.BentoBoxAccess;
import fr.hokib.hdrawer.manager.access.impl.SuperiorSkyBlockAccess;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public enum AccessAPI {

    BENTO_BOX("BentoBox", BentoBoxAccess.class), SUPERIOR_SKYBLOCK("SuperiorSkyblock2", SuperiorSkyBlockAccess.class);

    private final String plugin;
    private final Class<? extends DrawerAccess> accessClass;

    AccessAPI(final String plugin, final Class<? extends DrawerAccess> accessClass) {
        this.plugin = plugin;
        this.accessClass = accessClass;
    }

    public static DrawerAccess from() {
        try {
            for (final Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
                for (final AccessAPI value : values()) {
                    final String name = plugin.getName();
                    if (name.equals(value.getPlugin())) {
                        HDrawer.get().getLogger().info("We've found a compatible dependency (" + name + ")");
                        return value.accessClass.newInstance();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public String getPlugin() {
        return this.plugin;
    }
}

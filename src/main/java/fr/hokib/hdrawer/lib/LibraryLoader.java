package fr.hokib.hdrawer.lib;

import com.saicone.ezlib.Ezlib;
import fr.hokib.hdrawer.logger.DrawerLogger;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;

public abstract class LibraryLoader {
    private static final String DOT_ALIAS = "{}";
    private static final String PACKAGE_ALIAS = "{package}";
    private final Ezlib ezlib;

    protected LibraryLoader(final JavaPlugin plugin) {
        this.ezlib = new Ezlib(new File(plugin.getDataFolder(), "libs")) {
            // Support for msg library
            @Override
            public File download(String dependency, String repository) throws IOException, IllegalArgumentException {
                final String[] split = dependency.split(":", 4);
                if (split.length < 3) {
                    throw new IllegalArgumentException("Invalid dependency format");
                }

                final String repo = repository.endsWith("/") ? repository : repository + "/";
                final String fileVersion;
                String version = split[2];
                String[] s = version.split("@", 2);
                if (s.length > 1) {
                    version = s[0];
                    fileVersion = s[1];
                } else {
                    fileVersion = version;
                }

                final String fileName = split[1] + "-" + fileVersion + (split.length < 4 ? "" : "-" + split[3].replace(":", "-"));
                final String url = repo + split[0].replace(".", "/") + "/" + split[1] + "/" + version + "/" + fileName + ".jar";

                if (!this.getFolder().exists()) {
                    this.getFolder().mkdirs();
                }
                final File file = new File(this.getFolder(), fileName + ".jar");
                return file.exists() ? file : this.download(url, file);
            }
        };
    }

    static String alias(String s) {
        return s.replace(DOT_ALIAS, ".").replace(PACKAGE_ALIAS, LibraryLoader.class.getPackage().getName());
    }

    public abstract Dependency[] getValues();

    public void load() {
        final DrawerLogger logger = DrawerLogger.start("Loaded dependencies");

        for (final Dependency dependency : this.getValues()) {
            try {
                String name = MessageFormat.format(dependency.getTest(), (Object[]) dependency.getRelocation().to());
                if (dependency.isInner()) {
                    Class.forName(name, true, ezlib.getClassLoader());
                } else {
                    Class.forName(name);
                }
            } catch (final ClassNotFoundException e) {
                this.ezlib.load(dependency.getPath(), dependency.getRepository(), dependency.getRelocationMap(), !dependency.isInner());
            }
        }
        logger.stop();
    }
}
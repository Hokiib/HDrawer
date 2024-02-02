package fr.hokib.hdrawer.lib;

import com.saicone.ezlib.Ezlib;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;

@Getter
public abstract class LibraryLoader {
    private static final String DOT_ALIAS = "{}";
    private static final String PACKAGE_ALIAS = "{package}";

    private final JavaPlugin plugin;
    private final Ezlib ezlib;

    protected LibraryLoader(JavaPlugin plugin) {
        this.plugin = plugin;
        this.ezlib = new Ezlib(new File(plugin.getDataFolder(), "libs")) {
            // Support for msg library
            @Override
            public File download(String dependency, String repository) throws IOException, IllegalArgumentException {
                String[] split = dependency.split(":", 4);
                if (split.length < 3) {
                    throw new IllegalArgumentException("Invalid dependency format");
                }

                String repo = repository.endsWith("/") ? repository : repository + "/";
                String version = split[2];
                String fileVersion;
                String[] s = version.split("@", 2);
                if (s.length > 1) {
                    version = s[0];
                    fileVersion = s[1];
                } else {
                    fileVersion = version;
                }

                String fileName = split[1] + "-" + fileVersion + (split.length < 4 ? "" : "-" + split[3].replace(":", "-"));
                String url = repo + split[0].replace(".", "/") + "/" + split[1] + "/" + version + "/" + fileName + ".jar";

                if (!getFolder().exists()) {
                    getFolder().mkdirs();
                }
                File file = new File(getFolder(), fileName + ".jar");
                return file.exists() ? file : download(url, file);
            }
        };
    }

    public abstract Dependency[] getValues();

    public void load() {
        for (Dependency dependency : this.getValues()) {
            try {
                String name = MessageFormat.format(dependency.getTest(), (Object[]) dependency.getRelocation().to);
                if (dependency.isInner()) {
                    Class.forName(name, true, ezlib.getClassLoader());
                } else {
                    Class.forName(name);
                }
            } catch (ClassNotFoundException e) {
                plugin.getLogger().info("Loading dependency " + dependency.getPath());
                ezlib.load(dependency.getPath(), dependency.getRepository(), dependency.getRelocationMap(), !dependency.isInner());
            }
        }
    }

    public ClassLoader getClassLoader() {
        return getEzlib().getClassLoader();
    }

    public void close() {
        ezlib.close();
    }

    static String alias(String s) {
        return s.replace(DOT_ALIAS, ".").replace(PACKAGE_ALIAS, LibraryLoader.class.getPackage().getName());
    }
}
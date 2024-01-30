package fr.hokib.hdrawer.database.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fr.hokib.hdrawer.HDrawer;
import fr.hokib.hdrawer.config.database.DatabaseConfig;
import fr.hokib.hdrawer.database.Database;
import fr.hokib.hdrawer.manager.DrawerManager;
import fr.hokib.hdrawer.manager.drawer.Drawer;
import fr.hokib.hdrawer.manager.drawer.data.DrawerData;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class JsonDatabase implements Database {

    private static final Gson GSON = new GsonBuilder()
            .serializeNulls()
            .create();

    @Override
    public void load(DatabaseConfig config) {
        this.createFolder();

        final Path path = this.getPath();

        final Map<String, Drawer> drawers = new HashMap<>();

        try (final Stream<Path> stream = Files.list(path)) {
            for (final Path minePath : stream.toList()) {
                try (final Reader reader = Files.newBufferedReader(minePath)) {
                    final String stringLocation = minePath.getFileName().toString().replace(".json", "");
                    final DrawerData data = GSON.fromJson(reader, DrawerData.class);
                    if (data == null) continue;

                    drawers.put(stringLocation, data.to());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        HDrawer.get().getManager().setDrawers(drawers);
    }

    @Override
    public void save(DrawerManager manager) {

        for (final String location : manager.getDeleted()) {
            if (manager.getUnsaved().contains(location)) continue;

            final Path path = Path.of(this.getPath().toString(), location + ".json");

            if (Files.notExists(path)) continue;
            try {
                Files.delete(path);
            } catch (NoSuchFileException ignored) {
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        for (final String location : manager.getUnsaved()) {
            final Drawer drawer = manager.getDrawer(location);
            if (drawer == null) continue;

            final Path path = Path.of(getPath().toString(), location + ".json");
            try (final Writer writer = Files.newBufferedWriter(path)) {
                GSON.toJson(DrawerData.from(drawer), writer);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        manager.databaseClear();
    }

    @Override
    public void unload() {
    }

    private void createFolder() {
        final Path path = this.getPath();
        if (Files.notExists(path)) {
            try {
                Files.createDirectories(path);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private Path getPath() {
        return Path.of(HDrawer.get().getDataFolder().getPath(), FOLDER);
    }
}

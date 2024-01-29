package fr.hokib.hdrawer.database.impl;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import fr.hokib.hdrawer.HDrawer;
import fr.hokib.hdrawer.config.database.DatabaseConfig;
import fr.hokib.hdrawer.database.Database;
import fr.hokib.hdrawer.manager.DrawerManager;
import fr.hokib.hdrawer.manager.data.Drawer;
import fr.hokib.hdrawer.manager.data.storage.DrawerData;
import org.bukkit.block.BlockFace;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MySqlDatabase implements Database {
    private static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " + FOLDER + " (location VARCHAR(180), id VARCHAR(180), face VARCHAR(20), content LONGTEXT, PRIMARY KEY (location))";
    private static final String LOAD_DRAWERS = "SELECT * FROM " + FOLDER;
    private static final String DELETE_DRAWERS = "DELETE FROM " + FOLDER + " WHERE location IN (%locations%)";
    private static final String SAVE_DRAWERS = "INSERT INTO " + FOLDER + " (id, location, face, content) " +
            "VALUES %values% ON DUPLICATE KEY UPDATE id=VALUES(id), location=VALUES(location), face=VALUES(face), content=VALUES(content)";
    private HikariDataSource database;

    @Override
    public void load(DatabaseConfig config) {
        this.init(config);

        final Map<String, Drawer> drawers = new HashMap<>();

        try (final Connection connection = this.database.getConnection()) {
            final PreparedStatement ps = connection.prepareStatement(LOAD_DRAWERS);
            final ResultSet resultSet = ps.executeQuery();

            while (resultSet.next()) {

                final String content = resultSet.getString("content");
                final String id = resultSet.getString("id");
                final String location = resultSet.getString("location");
                final BlockFace face = BlockFace.valueOf(resultSet.getString("face"));

                final DrawerData data = new DrawerData(content, id, location, face);

                drawers.put(location, data.to());
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        HDrawer.get().getManager().setDrawers(drawers);
    }

    @Override
    public void save(DrawerManager manager) {
        try (final Connection connection = this.database.getConnection()) {

            final List<String> locations = new ArrayList<>();
            for (final String location : manager.getDeleted()) {
                if (manager.getUnsaved().contains(location)) continue;
                locations.add(location);
            }

            if (!locations.isEmpty()) {
                final PreparedStatement deleteStatement = connection.prepareStatement(DELETE_DRAWERS.replaceFirst("%locations%", this.convertLocations(locations)));
                deleteStatement.executeUpdate();
            }

            if (!manager.getUnsaved().isEmpty()) {
                final String saveSql = SAVE_DRAWERS.replaceFirst("%values%", this.convertData(manager.getUnsaved(), manager));
                final PreparedStatement saveStatement = connection.prepareStatement(saveSql);
                saveStatement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        manager.databaseClear();
    }

    @Override
    public void unload() {
        this.database.close();
    }

    private void init(final DatabaseConfig config) {

        final HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl("jdbc:mysql://" + config.host() + ":" + config.port() + "/" + config.database());
        hikariConfig.setUsername(config.username());
        hikariConfig.setPassword(config.password());
        hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
        hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
        hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        this.database = new HikariDataSource(hikariConfig);
        this.createTableIfNotExist();
    }

    private void createTableIfNotExist() {
        try (final Connection connection = this.database.getConnection()) {
            final PreparedStatement ps = connection.prepareStatement(CREATE_TABLE);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private String convertLocations(final List<String> locations) {
        final StringBuilder builder = new StringBuilder();
        for (int i = 0; i < locations.size(); i++) {
            final String location = locations.get(i);

            builder.append("'" + location + "'");
            if ((i + 1) < locations.size()) {
                builder.append(", ");
            }
        }

        return builder.toString();
    }

    private String convertData(final List<String> unsaved, final DrawerManager manager) {
        final StringBuilder builder = new StringBuilder();

        for (int i = 0; i < unsaved.size(); i++) {
            final String location = unsaved.get(i);
            final DrawerData data = DrawerData.from(manager.getDrawer(location));

            builder.append("('" + data.id() + "', '" + location + "', '" + data.face() + "', '" + data.content() + "')");

            if (i + 1 < unsaved.size()) {
                builder.append(", ");
            }
        }

        return builder.toString();
    }
}

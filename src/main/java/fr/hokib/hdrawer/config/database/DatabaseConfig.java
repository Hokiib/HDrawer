package fr.hokib.hdrawer.config.database;

import org.bukkit.configuration.ConfigurationSection;

public record DatabaseConfig(String type, String connectionString, String host, String port, String database,
                             String tableName, String username, String password, long savePeriod) {

    public static DatabaseConfig fromConfig(final ConfigurationSection section) {
        if (section == null) return new DatabaseConfig("JSON", "", "", "", "", "drawers", "", "", 300000L);

        final String type = section.getString("type");
        final long savePeriod = section.getLong("save-period");
        final String database = section.getString("database");
        final String tableName = section.getString("table-name");
        //MONGODB
        final String connectionString = section.getString("mongodb.connection-string");

        //MYSQL
        final ConfigurationSection mysqlSection = section.getConfigurationSection("mysql");
        String host = "", port = "", username = "", password = "";

        if (mysqlSection != null) {
            host = mysqlSection.getString("host");
            port = mysqlSection.getString("port");
            username = mysqlSection.getString("username");
            password = mysqlSection.getString("password");
        }

        return new DatabaseConfig(type, connectionString, host, port, database, tableName,
                username, password, savePeriod);
    }
}

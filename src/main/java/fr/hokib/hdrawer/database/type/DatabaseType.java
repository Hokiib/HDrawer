package fr.hokib.hdrawer.database.type;

import fr.hokib.hdrawer.database.Database;
import fr.hokib.hdrawer.database.impl.JsonDatabase;
import fr.hokib.hdrawer.database.impl.MongoDatabase;
import fr.hokib.hdrawer.database.impl.MySqlDatabase;

public enum DatabaseType {

    JSON(JsonDatabase.class), MYSQL(MySqlDatabase.class), MONGODB(MongoDatabase.class);

    private final Class<? extends Database> databaseClass;

    DatabaseType(final Class<? extends Database> databaseClass) {
        this.databaseClass = databaseClass;
    }

    public static boolean equals(final String type, final Database database) {
        if (database == null) return false;

        return from(type).getClass().equals(database.getClass());
    }

    public static Database from(final String type) {
        DatabaseType databaseType;

        try {
            databaseType = valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            databaseType = JSON;
        }

        try {
            return databaseType.databaseClass.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new JsonDatabase();
    }
}

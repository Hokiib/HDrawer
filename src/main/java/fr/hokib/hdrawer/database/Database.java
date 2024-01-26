package fr.hokib.hdrawer.database;

import fr.hokib.hdrawer.config.database.DatabaseConfig;
import fr.hokib.hdrawer.manager.DrawerManager;

public interface Database {
    String FOLDER = "drawers";

    void load(DatabaseConfig config);

    /**
     * Delete remove drawers
     * Then save drawers
     */
    void save(DrawerManager manager);

    void unload();
}

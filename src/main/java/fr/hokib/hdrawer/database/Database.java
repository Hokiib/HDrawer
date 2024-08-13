package fr.hokib.hdrawer.database;

import fr.hokib.hdrawer.HDrawer;
import fr.hokib.hdrawer.config.database.DatabaseConfig;
import fr.hokib.hdrawer.manager.DrawerManager;

public interface Database {
	
	@Deprecated
    String FOLDER = "drawers";
	
    void load(DatabaseConfig config);

    /**
     * Delete remove drawers
     * Then save drawers
     */
    void save(DrawerManager manager);

    void unload();
    
    default DatabaseConfig getConfig() {
    	return HDrawer.get().getConfiguration().getDatabaseConfig();
    }
}

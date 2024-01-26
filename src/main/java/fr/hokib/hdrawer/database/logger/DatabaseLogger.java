package fr.hokib.hdrawer.database.logger;

import fr.hokib.hdrawer.HDrawer;

public class DatabaseLogger {

    private final String name;
    private final long before;

    private DatabaseLogger(final String name) {
        this.name = name;
        this.before = System.currentTimeMillis();
    }

    public static DatabaseLogger start(final String name) {
        return new DatabaseLogger(name);
    }

    public void stop() {
        final long difference = System.currentTimeMillis() - this.before;

        HDrawer.get().getLogger().info(this.name + " (" + difference + "ms)");
    }
}

package fr.hokib.hdrawer.logger;

import fr.hokib.hdrawer.HDrawer;

public class DrawerLogger {

    private final String name;
    private final long before;

    private DrawerLogger(final String name) {
        this.name = name;
        this.before = System.currentTimeMillis();
    }

    public static DrawerLogger start(final String name) {
        return new DrawerLogger(name);
    }

    public void stop() {
        final long difference = System.currentTimeMillis() - this.before;

        HDrawer.get().getLogger().info(this.name + " (" + difference + "ms)");
    }
}

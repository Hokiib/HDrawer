package fr.hokib.hdrawer.database.task;

import fr.hokib.hdrawer.HDrawer;
import fr.hokib.hdrawer.logger.DrawerLogger;
import fr.hokib.hdrawer.task.AsyncTask;

public class SaveTask extends AsyncTask {

    private final HDrawer main;

    public SaveTask(final HDrawer main) {
        super(main.getConfiguration().getDatabaseConfig().savePeriod());
        this.main = main;
    }

    @Override
    public void stop() {
        super.stop();
    }

    @Override
    public void run() {
        this.save();
    }

    private void save() {
        final DrawerLogger logger = DrawerLogger.start("Drawers saved !");
        this.main.getDatabase().save(this.main.getManager());
        logger.stop();
    }
}

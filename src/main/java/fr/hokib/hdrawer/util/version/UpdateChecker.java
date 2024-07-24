package fr.hokib.hdrawer.util.version;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Scanner;
import java.util.function.Consumer;

import fr.hokib.hdrawer.scheduler.Scheduler;

public class UpdateChecker {

    private static final int RESOURCE_ID = 114799;
    public static final String RESOURCE_URL = "https://www.spigotmc.org/resources/hdrawer." + RESOURCE_ID + "/";

    public static void getVersion(final Consumer<String> consumer) {
        Scheduler.getScheduler().runAsync(() -> {
            try (final InputStream is = new URL("https://api.spigotmc.org/legacy/update.php?resource=" + RESOURCE_ID + "/~").openStream(); Scanner scanner = new Scanner(is)) {
                if (scanner.hasNext()) {
                    consumer.accept(scanner.next());
                }
            } catch (final IOException ignored) {}
        });
    }
}

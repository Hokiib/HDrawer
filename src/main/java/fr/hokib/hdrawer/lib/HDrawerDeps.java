package fr.hokib.hdrawer.lib;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;

public class HDrawerDeps extends LibraryLoader {
    public HDrawerDeps(JavaPlugin plugin) {
        super(plugin);
    }

    @Override
    public Dependency[] getValues() {
        return Arrays.stream(Libs.values()).map(Libs::getDeps).toArray(Dependency[]::new);
    }

    private enum Libs {
        SLF4J_API(
                "{0}.Logger",
                "org{}slf4j:slf4j-api:1.7.6",
                Relocation.of("org{}slf4j", "{package}.slf4j")
        ),
        SLF4J_NOP(
                "{0}.impl.StaticLoggerBinder",
                "org{}slf4j:slf4j-nop:1.7.6",
                Relocation.of("org{}slf4j", "{package}.lib.slf4j"),
                SLF4J_API.deps.getRelocation()
        ),
        MONGODB_BSON(
                "{0}.client.BSON",
                "org{}mongodb:bson:4.9.1",
                Relocation.of("com{}mongodb", "{package}.mongodb"),
                SLF4J_API.deps.getRelocation()
        ),
        MONGODB_BSON_RECORD_CODEC(
                "{0}.RecordCodec",
                "org{}mongodb:bson-record-codec:4.9.1",
                Relocation.of("com{}mongodb", "{package}.mongodb"),
                SLF4J_API.deps.getRelocation()
        ),
        MONGODB_CORE(
                "{0}.client.MongoClientSettings",
                "org{}mongodb:mongodb-driver-core:4.9.1",
                Relocation.of("com{}mongodb", "{package}.mongodb"),
                SLF4J_API.deps.getRelocation()
        ),
        MONGODB_DRIVER(
                "{0}.client.MongoClient",
                "org{}mongodb:mongodb-driver-sync:4.9.1",
                Relocation.of("com{}mongodb", "{package}.mongodb"),
                SLF4J_API.deps.getRelocation(),
                MONGODB_BSON.deps.getRelocation(),
                MONGODB_CORE.deps.getRelocation()
        ),
        HIKARI(
                "{0}.HikariConfig",
                "com{}zaxxer:HikariCP:5.1.0",
                Relocation.of("com{}zaxxer{}hikari", "{package}.hikari"),
                SLF4J_API.deps.getRelocation()
        );

        private final Dependency deps;

        Libs(final String test, final String path, final Relocation... relocations) {
            this.deps = new Dependency(test, path, relocations);
        }

        public Dependency getDeps() {
            return this.deps;
        }
    }
}

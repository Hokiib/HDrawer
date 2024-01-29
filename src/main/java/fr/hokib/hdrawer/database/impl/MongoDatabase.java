package fr.hokib.hdrawer.database.impl;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.ServerApi;
import com.mongodb.ServerApiVersion;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.*;
import fr.hokib.hdrawer.HDrawer;
import fr.hokib.hdrawer.config.database.DatabaseConfig;
import fr.hokib.hdrawer.database.Database;
import fr.hokib.hdrawer.manager.DrawerManager;
import fr.hokib.hdrawer.manager.data.Drawer;
import fr.hokib.hdrawer.manager.data.storage.DrawerData;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class MongoDatabase implements Database {

    private static final ReplaceOptions OPTIONS = new ReplaceOptions().upsert(true);
    private MongoClient client;
    private MongoCollection<DrawerData> collection;

    @Override
    public void load(DatabaseConfig config) {
        this.init(config);

        final Map<String, Drawer> drawers = new HashMap<>();

        for (final DrawerData data : this.collection.find()) {
            drawers.put(data.location(), data.to());
        }

        HDrawer.get().getManager().setDrawers(drawers);
    }

    @Override
    public void save(DrawerManager manager) {

        final List<WriteModel<DrawerData>> deleteModels = new ArrayList<>();

        for (final String location : manager.getDeleted()) {
            if (manager.getUnsaved().contains(location)) continue;

            deleteModels.add(new DeleteManyModel<>(this.getFilter(location)));
        }

        if (!deleteModels.isEmpty()) {
            this.collection.bulkWrite(deleteModels);
        }

        final List<ReplaceOneModel<DrawerData>> saveModels = new ArrayList<>();

        for (final String location : manager.getUnsaved()) {
            final Drawer drawer = manager.getDrawer(location);
            if (drawer == null) continue;

            final ReplaceOneModel<DrawerData> model = new ReplaceOneModel<>(
                    this.getFilter(location),
                    DrawerData.from(drawer),
                    OPTIONS);
            saveModels.add(model);
        }

        if (!saveModels.isEmpty()) {
            this.collection.bulkWrite(saveModels);
        }

        manager.databaseClear();
    }

    @Override
    public void unload() {
        this.client.close();
    }

    private void init(final DatabaseConfig config) {

        final ServerApi serverApi = ServerApi.builder()
                .version(ServerApiVersion.V1)
                .build();

        final ConnectionString connectionString = new ConnectionString(config.connectionString());

        final CodecRegistry pojoCodec = CodecRegistries.fromProviders(PojoCodecProvider.builder().automatic(true).build());
        final CodecRegistry codecRegistry = CodecRegistries.fromRegistries(MongoClientSettings.getDefaultCodecRegistry(), pojoCodec);

        final MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(connectionString)
                .applyToConnectionPoolSettings(builder -> builder.maxSize(5))
                .applyToSocketSettings(builder -> builder.connectTimeout(5, TimeUnit.SECONDS))
                .serverApi(serverApi)
                .codecRegistry(codecRegistry)
                .retryWrites(false)
                .build();

        this.client = MongoClients.create(settings);
        this.collection = this.createCollectionIfNotExist(this.client.getDatabase(config.database()));
    }

    private MongoCollection<DrawerData> createCollectionIfNotExist(final com.mongodb.client.MongoDatabase database) {
        if (database == null) return null;

        if (!database.listCollectionNames().into(new ArrayList<>()).contains(FOLDER)) {
            database.createCollection(FOLDER);
        }

        return database.getCollection(FOLDER, DrawerData.class);
    }

    private Bson getFilter(final String location) {

        return Filters.eq("location", location);
    }
}

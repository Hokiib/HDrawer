package fr.hokib.hdrawer.packet;

import fr.hokib.hdrawer.util.NumberUtil;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Display;
import net.minecraft.world.level.Level;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_20_R3.CraftWorld;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public abstract class HPacket {

    protected final List<SynchedEntityData.DataValue<?>> data = new ArrayList<>();
    protected final int entityId = NumberUtil.getRandomId();
    protected Display display;
    protected Level handle;
    protected double x, y, z;
    protected float yaw;
    private Vector3f scale;

    protected abstract Display getDisplayType();

    private void build() {
        final Display display = this.getDisplayType();
        display.setBillboardConstraints(net.minecraft.world.entity.Display.BillboardConstraints.FIXED);
        display.setInvulnerable(true);
        display.setPos(this.x, this.y, this.z);
        display.setYRot(this.yaw);
        display.setXRot(0);
        display.setId(this.entityId);

        this.display = display;
    }

    public void send(final ServerPlayer sp) {
        this.build();

        final ClientboundAddEntityPacket addEntityPacket = new ClientboundAddEntityPacket(this.display);
        sp.connection.send(addEntityPacket);

        final ClientboundSetEntityDataPacket dataPacket = new ClientboundSetEntityDataPacket(this.entityId, this.data);
        sp.connection.send(dataPacket);
    }

    public void remove(final ServerPlayer sp) {
        final ClientboundRemoveEntitiesPacket removeEntitiesPacket = new ClientboundRemoveEntitiesPacket(this.entityId);
        sp.connection.send(removeEntitiesPacket);
    }

    protected void updateData() {
        this.data.clear();

        if (this.scale != null) {
            this.data.add(SynchedEntityData.DataValue.create(new EntityDataAccessor<>(12, EntityDataSerializers.VECTOR3), this.scale));
        }
    }

    public Location getPos() {
        return new Location(this.handle.getWorld(), this.x, this.y, this.z);
    }

    public void setPos(final Location location) {
        this.x = location.getX();
        this.y = location.getY();
        this.z = location.getZ();

        final CraftWorld world = (CraftWorld) location.getWorld();
        this.handle = world.getHandle();
    }

    public void setYaw(final float yaw) {
        this.yaw = yaw;
    }

    public void setScale(final float x, final float y, final float z) {
        this.scale = new Vector3f(x, y, z);
        this.updateData();
    }

}

package fr.hokib.hdrawer.packet.impl;

import fr.hokib.hdrawer.packet.HPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.EntityType;
import org.bukkit.craftbukkit.v1_20_R3.util.CraftChatMessage;

public class HText extends HPacket {

    private String text;

    @Override
    protected void updateData() {
        super.updateData();

        super.data.add(SynchedEntityData.DataValue.create(new EntityDataAccessor<>(23, EntityDataSerializers.COMPONENT), CraftChatMessage.fromString(this.text)[0]));
    }

    @Override
    protected Display getDisplayType() {
        return new Display.TextDisplay(EntityType.TEXT_DISPLAY, super.handle);
    }

    public String getText() {
        return this.text;
    }

    public void setText(final String text) {
        this.text = text;
        this.updateData();
    }
}

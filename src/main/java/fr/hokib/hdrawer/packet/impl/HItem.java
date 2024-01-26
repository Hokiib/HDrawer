package fr.hokib.hdrawer.packet.impl;

import fr.hokib.hdrawer.packet.HPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemDisplayContext;
import org.bukkit.craftbukkit.v1_20_R3.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

public class HItem extends HPacket {

    private net.minecraft.world.item.ItemStack itemStack;
    private byte displayType = 6;

    @Override
    protected void updateData() {
        super.updateData();
        if (this.itemStack != null) {
            super.data.add(SynchedEntityData.DataValue.create(new EntityDataAccessor<>(23, EntityDataSerializers.ITEM_STACK), this.itemStack));
            super.data.add(SynchedEntityData.DataValue.create(new EntityDataAccessor<>(24, EntityDataSerializers.BYTE), this.displayType));
        }
    }

    @Override
    protected Display getDisplayType() {
        return new Display.ItemDisplay(EntityType.ITEM_DISPLAY, super.handle);
    }

    public void setDisplayType(final ItemDisplayContext displayType) {
        this.displayType = displayType.getId();
    }

    public void setMaterial(final ItemStack itemStack) {
        final ItemStack cloned = itemStack.clone();
        cloned.setAmount(1);

        this.itemStack = CraftItemStack.asNMSCopy(cloned);
        this.updateData();
    }
}

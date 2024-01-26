package fr.hokib.hdrawer.manager.data;

import fr.hokib.hdrawer.HDrawer;
import fr.hokib.hdrawer.config.Config;
import fr.hokib.hdrawer.config.drawer.DrawerConfig;
import fr.hokib.hdrawer.manager.data.storage.DrawerStorage;
import fr.hokib.hdrawer.manager.data.type.DrawerType;
import fr.hokib.hdrawer.packet.HPacket;
import fr.hokib.hdrawer.packet.impl.HItem;
import fr.hokib.hdrawer.packet.impl.HText;
import fr.hokib.hdrawer.util.NumberUtil;
import fr.hokib.hdrawer.util.location.BorderTuple;
import fr.hokib.hdrawer.util.location.DisplayAttributes;
import fr.hokib.hdrawer.util.location.LocationUtil;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemDisplayContext;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Drawer extends DrawerStorage {

    //All these variables are util once placed
    private transient final List<HPacket> hItems = new ArrayList<>();
    private transient final List<HPacket> hTexts = new ArrayList<>();
    private transient final List<HItem> borders = new ArrayList<>();
    private transient final Set<ServerPlayer> viewers = new HashSet<>();
    private String id;
    private Location location;
    private BlockFace face;

    private void display() {
        for (final ServerPlayer sp : this.viewers) {
            this.display(sp);
        }
    }

    public void display(final ServerPlayer sp) {
        try {
            for (final HPacket hItem : this.hItems) {
                this.display(sp, hItem);
            }
            for (final HPacket hText : this.hTexts) {
                this.display(sp, hText);
            }
            for (final HItem hBorder : this.borders) {
                this.display(sp, hBorder);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void display(final ServerPlayer sp, final HPacket hPacket) {
        Bukkit.getScheduler().runTaskAsynchronously(HDrawer.get(), () -> {
            if (hPacket instanceof HText hText) {
                if (hText.getText() == null) {
                    hText.remove(sp);
                    return;
                }
            }

            hPacket.send(sp);
        });
    }

    public void hide(final ServerPlayer sp) {
        for (final HPacket hItem : this.hItems) {
            this.hide(sp, hItem);
        }
        for (final HPacket hText : this.hTexts) {
            this.hide(sp, hText);
        }
        for (final HItem hBorder : this.borders) {
            this.hide(sp, hBorder);
        }
    }

    private void hide(final ServerPlayer sp, final HPacket hPacket) {
        hPacket.remove(sp);
    }

    public void delete() {
        for (final ServerPlayer sp : this.viewers) {
            this.hide(sp);
        }
        this.viewers.clear();
    }

    public void addViewer(final Player player) {
        final ServerPlayer sp = ((CraftPlayer) player).getHandle();

        if (this.viewers.add(sp)) {
            this.display(sp);
        }
    }

    public void removeViewer(final Player player) {
        final ServerPlayer sp = ((CraftPlayer) player).getHandle();

        if (this.viewers.remove(sp)) {
            this.hide(sp);
        }
    }

    public void build() {
        final Config config = HDrawer.get().getConfiguration();
        final DrawerConfig drawerConfig = HDrawer.get().getConfiguration().getDrawerConfig(this.id);
        if (drawerConfig == null) return;

        final DisplayAttributes[] attributes = DrawerType.from(drawerConfig.slot()).getAttributes();

        final float itemYaw = LocationUtil.getYaw(this.face.getOppositeFace());
        final float textYaw = LocationUtil.getYaw(this.face);

        for (int i = 0; i < attributes.length; i++) {
            final DisplayAttributes attribute = attributes[i];

            final double[] pos = LocationUtil.getOrientation(this.face,
                    this.reverseHorizontal(attribute.horizontal()),
                    attribute.vertical(), 0);

            //ItemDisplay
            final Location itemLocation = this.location.clone();
            itemLocation.add(pos[0], pos[1], pos[2]);

            final HItem hItem = (HItem) this.getOrDefault(this.hItems, HItem.class, i);
            hItem.setPos(itemLocation);
            hItem.setYaw(itemYaw);
            hItem.setScale(attribute.scale(), attribute.scale(), 0.0f);
            this.replace(this.hItems, hItem, i);

            //TextDisplay
            final Location textLocation = this.location.clone();
            textLocation.add(pos[0], pos[1] - (attribute.scale() / 1.25), pos[2]);

            final HText hText = (HText) this.getOrDefault(this.hTexts, HText.class, i);
            hText.setPos(textLocation);
            hText.setYaw(textYaw);
            hText.setScale(attribute.scale(), attribute.scale(), attribute.scale());
            this.replace(this.hTexts, hText, i);

            if (this.content.size() == i) this.content.add(EMPTY);
        }

        this.borders.clear();
        if (config.isToggleBorder()) {
            final ItemStack borderStack = new ItemStack(drawerConfig.borderMaterial());

            for (final BorderTuple tuple : LocationUtil.getBorderPositions(this.face)) {
                final double[] pos = tuple.pos();
                final Location location = this.location.clone();
                location.add(pos[0], pos[1], pos[2]);

                final HItem border = new HItem();
                border.setDisplayType(ItemDisplayContext.FIXED);
                border.setPos(location);
                border.setMaterial(borderStack);
                border.setYaw(itemYaw);
                border.setScale(tuple.horizontal(), tuple.vertical(), 0.1f);

                this.borders.add(border);
            }
        }

        this.display();
    }

    private void replace(final List<HPacket> list, final HPacket packet, final int index) {
        if (list.size() == index) list.add(packet);
        else {
            list.set(index, packet);
        }
    }

    @Override
    protected void update(final ItemStack itemStack, final int index) {
        final int amount = itemStack == null ? 0 : itemStack.getAmount();

        final HItem hItem = (HItem) this.hItems.get(index);
        final HText hText = (HText) this.hTexts.get(index);

        hItem.setMaterial(itemStack == null ? EMPTY : itemStack);
        hText.setText(amount <= 0 ? null : NumberUtil.format(amount));

        for (final ServerPlayer sp : this.viewers) {
            this.display(sp, hItem);
            this.display(sp, hText);
        }
    }

    @Override
    public int getIndexByLocation(final Location location) {
        double lastDistance = -1;
        int index = 0;

        for (int i = 0; i < this.hItems.size(); i++) {
            final HPacket hItem = this.hItems.get(i);
            final Location pos = hItem.getPos();
            final double distance = pos.distance(location);

            if (lastDistance == -1) {
                lastDistance = distance;
                continue;
            }

            if (lastDistance > distance) {
                lastDistance = distance;
                index = i;
            }
        }

        return index;
    }

    @Override
    public Location getLocation() {
        return this.location;
    }

    public void setLocation(final Location location) {
        this.location = location;
    }

    @Override
    public String getId() {
        return this.id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public BlockFace getFace() {
        return this.face;
    }

    public void setFace(final BlockFace face) {
        this.face = face;
    }

    private HPacket getOrDefault(final List<HPacket> list, final Class<? extends HPacket> clazz, final int index) {
        if (list.size() <= index) {
            try {
                return clazz.newInstance();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return list.get(index);
    }

    private double reverseHorizontal(final double horizontal) {
        final boolean reverse = this.face == BlockFace.NORTH || this.face == BlockFace.EAST;
        return reverse ? horizontal * -1 : horizontal;
    }
}

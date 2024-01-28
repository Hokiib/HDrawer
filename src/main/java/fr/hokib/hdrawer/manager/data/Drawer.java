package fr.hokib.hdrawer.manager.data;

import fr.hokib.hdrawer.HDrawer;
import fr.hokib.hdrawer.config.Config;
import fr.hokib.hdrawer.config.drawer.DrawerConfig;
import fr.hokib.hdrawer.manager.data.storage.DrawerStorage;
import fr.hokib.hdrawer.manager.data.type.DrawerType;
import fr.hokib.hdrawer.util.NumberUtil;
import fr.hokib.hdrawer.util.location.BorderTuple;
import fr.hokib.hdrawer.util.location.DisplayAttributes;
import fr.hokib.hdrawer.util.location.LocationUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.TextDisplay;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Transformation;

import java.util.ArrayList;
import java.util.List;

public class Drawer extends DrawerStorage {

    private transient final List<ItemDisplay> items = new ArrayList<>();
    private transient final List<TextDisplay> texts = new ArrayList<>();
    private transient final List<ItemDisplay> borders = new ArrayList<>();
    private String id;
    private Location location;
    private BlockFace face;

    public void delete() {
        for (final ItemDisplay item : this.items) {
            item.remove();
        }
        for (final TextDisplay text : this.texts) {
            text.remove();
        }
        for (final ItemDisplay border : this.borders) {
            border.remove();
        }
        this.items.clear();
        this.texts.clear();
        this.borders.clear();
    }


    public void build() {
        this.texts.clear();
        this.items.clear();
        this.borders.clear();

        final Config config = HDrawer.get().getConfiguration();
        final DrawerConfig drawerConfig = HDrawer.get().getConfiguration().getDrawerConfig(this.id);
        if (drawerConfig == null) return;

        final Block block = this.location.getBlock();
        final Material type = drawerConfig.drawer().getType();
        if (type != block.getType()) {
            //If material in config has changed
            block.setType(type);
            if (block.getBlockData() instanceof Directional directional) {
                directional.setFacing(this.face);
                block.setBlockData(directional);
                block.getState().update();
            }
        }

        final DisplayAttributes[] attributes = DrawerType.from(drawerConfig.slot()).getAttributes();

        final float itemYaw = LocationUtil.getYaw(this.face.getOppositeFace());
        final float textYaw = LocationUtil.getYaw(this.face);

        final World world = this.location.getWorld();

        for (int i = 0; i < attributes.length; i++) {
            final DisplayAttributes attribute = attributes[i];

            final double[] pos = LocationUtil.getOrientation(this.face,
                    this.reverseHorizontal(attribute.horizontal()),
                    attribute.vertical(), 0);

            //ItemDisplay
            final Location itemLocation = this.location.clone();
            itemLocation.add(pos[0], pos[1], pos[2]);
            itemLocation.setYaw(itemYaw);
            itemLocation.setPitch(0);

            this.items.add(world.spawn(itemLocation, ItemDisplay.class, itemDisplay -> {
                final Transformation transformation = itemDisplay.getTransformation();
                transformation.getScale().set(attribute.scale(), attribute.scale(), 0.0f);

                itemDisplay.setItemDisplayTransform(ItemDisplay.ItemDisplayTransform.GUI);
                itemDisplay.setTransformation(transformation);
                itemDisplay.setInvulnerable(true);
                itemDisplay.setViewRange(config.getDistance());
                itemDisplay.teleport(itemLocation);
            }));

            //TextDisplay
            final Location textLocation = this.location.clone();
            textLocation.add(pos[0], pos[1] - (attribute.scale() / 1.25), pos[2]);
            textLocation.setYaw(textYaw);
            textLocation.setPitch(0);

            this.texts.add(world.spawn(textLocation, TextDisplay.class, textDisplay -> {
                final Transformation transformation = textDisplay.getTransformation();
                transformation.getScale().set(attribute.scale(), attribute.scale(), attribute.scale());

                textDisplay.setTransformation(transformation);
                textDisplay.setInvulnerable(true);
                textDisplay.setViewRange(config.getDistance());
                textDisplay.teleport(textLocation);
            }));

            if (i < this.content.size()) {
                this.update(this.content.get(i), i);
            }

            if (this.content.size() == i) this.content.add(EMPTY);
        }

        if (config.isToggleBorder()) {
            final ItemStack borderStack = new ItemStack(drawerConfig.borderMaterial());

            for (final BorderTuple tuple : LocationUtil.getBorderPositions(this.face)) {
                final double[] pos = tuple.pos();
                final Location location = this.location.clone();
                location.add(pos[0], pos[1], pos[2]);
                location.setYaw(itemYaw);
                location.setPitch(0);

                this.borders.add(world.spawn(location, ItemDisplay.class, border -> {
                    final Transformation transformation = border.getTransformation();
                    transformation.getScale().set(tuple.horizontal(), tuple.vertical(), 0.1f);

                    border.setTransformation(transformation);
                    border.setItemStack(borderStack);
                    border.setViewRange(config.getDistance());
                    border.setItemDisplayTransform(ItemDisplay.ItemDisplayTransform.FIXED);
                    border.setInvulnerable(true);
                    border.teleport(location);
                }));
            }
        }
    }

    @Override
    protected void update(final ItemStack itemStack, final int index) {
        final int amount = itemStack == null ? 0 : itemStack.getAmount();

        final ItemDisplay item = this.items.get(index);
        final TextDisplay text = this.texts.get(index);

        item.setItemStack(itemStack == null ? EMPTY : itemStack);
        text.setText(amount <= 0 ? null : NumberUtil.format(amount));
    }

    @Override
    public int getIndexByLocation(final Location location) {
        double lastDistance = -1;
        int index = 0;

        for (int i = 0; i < this.items.size(); i++) {
            final ItemDisplay item = this.items.get(i);
            final double distance = item.getLocation().distance(location);

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

    private double reverseHorizontal(final double horizontal) {
        final boolean reverse = this.face == BlockFace.NORTH || this.face == BlockFace.EAST;
        return reverse ? horizontal * -1 : horizontal;
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
}

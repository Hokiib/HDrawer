package fr.hokib.hdrawer.util.location;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.BlockFace;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LocationUtil {

    public static final Pattern NAME_REGEX = Pattern.compile("\\.(.*?)\\.");

    public static float getYaw(final BlockFace face) {
        return switch (face) {
            case NORTH -> -180f;
            case WEST -> 90f;
            case EAST -> -90f;
            default -> 0f;
        };
    }

    public static BlockFace getBlockFace(final float yaw) {

        if ((135 <= yaw && yaw <= 180) || (-180 <= yaw && yaw <= -135)) {
            return BlockFace.NORTH;
        }
        if (-135 <= yaw && yaw <= -45) {
            return BlockFace.EAST;
        }
        if (135 >= yaw && yaw >= 45) {
            return BlockFace.WEST;
        }

        return BlockFace.SOUTH;
    }

    public static double[] getOrientation(final BlockFace face, final double horizontal, final double vertical,
                                          final double offset) {
        switch (face) {
            case WEST -> {
                return new double[]{-0.01 - offset, 0.5 + vertical, 0.5 + horizontal};
            }
            case EAST -> {
                return new double[]{1.01 + offset, 0.5 + vertical, 0.5 + horizontal};
            }
            case SOUTH -> {
                return new double[]{0.5 + horizontal, 0.5 + vertical, 1.01 + offset};
            }
            default -> {
                return new double[]{0.5 + horizontal, 0.5 + vertical, -0.01 - offset};
            }
        }
    }

    public static List<BorderTuple> getBorderPositions(final BlockFace face) {

        final List<BorderTuple> borders = new ArrayList<>();

        double t = 0.015;

        //TOP
        borders.add(new BorderTuple(getOrientation(face, 0, 0.475, t), 2.0f, 0.1f));
        //BOTTOM
        borders.add(new BorderTuple(getOrientation(face, 0, -0.475, t), 2.0f, 0.1f));
        //LEFT
        borders.add(new BorderTuple(getOrientation(face, -0.475, 0, t), 0.1f, 2.0f));
        //RIGHT
        borders.add(new BorderTuple(getOrientation(face, 0.475, 0, t), 0.1f, 2.0f));

        return borders;
    }

    public static String convert(final Location location) {
        return "." + location.getWorld().getName() + "._" + location.getBlockX() + "_" + location.getBlockY() + "_" + location.getBlockZ();
    }

    public static Location convert(final String location) {
        final Matcher matcher = NAME_REGEX.matcher(location);
        if (!matcher.find()) return null;

        final String name = matcher.group(1);
        final World world = Bukkit.getWorld(name);
        final String[] split = location.replace(name, "").replace(".", "").substring(1).split("_");
        final double x = Double.parseDouble(split[0]);
        final double y = Double.parseDouble(split[1]);
        final double z = Double.parseDouble(split[2]);

        return new Location(world, x, y, z);
    }
}

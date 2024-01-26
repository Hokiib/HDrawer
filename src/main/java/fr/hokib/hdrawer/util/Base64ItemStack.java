package fr.hokib.hdrawer.util;

import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public class Base64ItemStack {

    public static <T> String encode(final T item) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             final BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream)) {
            dataOutput.writeObject(item);
            return new String(Base64Coder.encode(outputStream.toByteArray()));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static <T> T decode(final String base64) {
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decode(base64));
             final BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream)) {
            return (T) dataInput.readObject();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}

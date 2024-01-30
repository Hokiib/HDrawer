package fr.hokib.hdrawer.util.inventory;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class InventoryUtil {

    /**
     * @return The remaining
     */
    public static ItemStack addItem(final ItemStack itemStack, final Inventory inventory) {

        for (final ItemStack remain : inventory.addItem(itemStack).values()) {
            return remain;
        }

        return null;
    }
}

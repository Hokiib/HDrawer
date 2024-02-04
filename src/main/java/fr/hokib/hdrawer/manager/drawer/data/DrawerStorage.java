package fr.hokib.hdrawer.manager.drawer.data;

import fr.hokib.hdrawer.HDrawer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class DrawerStorage {
    protected static final ItemStack EMPTY = new ItemStack(Material.AIR, 0);

    //Only this variable should be stocked in NBT
    protected List<ItemStack> content = new ArrayList<>();

    protected abstract int getIndexByLocation(final Location location);

    protected abstract Location getLocation();

    public abstract void update(ItemStack itemStack, final int index);

    public abstract void update();

    public abstract String getId();

    public boolean insertContent(final Player player, final ItemStack hand, final Location location) {

        final int index = this.getIndexByLocation(location);

        final ItemStack storedItem = this.content.get(index);

        if (!storedItem.getType().isAir() && !storedItem.isSimilar(hand)) return false;

        final int amount = storedItem.getAmount();
        final int limit = HDrawer.get().getConfiguration().getDrawerConfig(this.getId()).limit();
        if (amount >= limit) return false;

        int remaining = limit - amount;

        final ItemStack clonedHand = hand.clone();

        if (player.isSneaking()) {
            int total = 0;

            for (final ItemStack content : player.getInventory().getStorageContents()) {
                if (content == null || !content.isSimilar(clonedHand)) {
                    continue;
                }

                final int itemAmount = content.getAmount();

                content.setAmount(itemAmount - remaining);

                if (itemAmount <= remaining) {
                    total += itemAmount;
                    remaining -= itemAmount;
                } else {
                    total += remaining;
                    break;
                }
            }

            this.updateContent(clonedHand, index, amount + total);
        } else {
            final int handAmount = hand.getAmount();

            this.updateContent(clonedHand, index, Math.min(limit, amount + handAmount));

            hand.setAmount(handAmount - remaining);
        }

        this.update(this.content.get(index), index);

        return true;
    }

    public boolean removeContent(final Player player, final Location location) {

        final int index = this.getIndexByLocation(location);

        final ItemStack storedItem = this.content.get(index);

        final int amount = storedItem.getAmount();
        if (amount <= 0) return false;

        int removedAmount = 1;
        if (player.isSneaking()) {
            removedAmount = Math.min(storedItem.getMaxStackSize(), amount);
        }
        final int remaining = amount - removedAmount;

        final ItemStack cloned = storedItem.clone();
        cloned.setAmount(remaining);
        this.update(cloned, index);

        final ItemStack backItemStack = storedItem.clone();
        backItemStack.setAmount(removedAmount);

        this.updateContent(storedItem, index, remaining);

        //Add in inventory and drop in ground if player is full
        for (final Map.Entry<Integer, ItemStack> entry : player.getInventory().addItem(backItemStack).entrySet()) {
            this.getLocation().getWorld().dropItemNaturally(this.getLocation(), entry.getValue());
        }

        return true;
    }

    private void updateContent(final ItemStack itemStack, final int index, final int newAmount) {
        if (newAmount <= 0) {
            this.content.set(index, EMPTY);
            return;
        }

        final ItemStack indexItem = this.content.get(index);
        if (indexItem.isSimilar(itemStack)) {
            indexItem.setAmount(newAmount);
        } else {
            //If not present
            final ItemStack hand = itemStack.clone();
            hand.setAmount(newAmount);
            this.content.set(index, hand);
        }
    }

    public boolean isEmpty() {
        for (final ItemStack itemStack : this.content) {
            if (itemStack.equals(EMPTY)) continue;

            return false;
        }
        return true;
    }

    public List<ItemStack> getContent() {
        return this.content;
    }

    public void setContent(final List<ItemStack> content) {
        this.content = content;
        this.update();
    }
}

package fr.hokib.hdrawer.config.drawer;

import fr.hokib.hdrawer.HDrawer;
import fr.hokib.hdrawer.manager.DrawerManager;
import fr.hokib.hdrawer.util.ColorUtil;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Server;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public record DrawerConfig(String id, ItemStack drawer, Material borderMaterial,
                           int limit, int slot) {

    private static final String RECIPE_ID = "drawer-recipe";
    private static final char[] symbols = new char[]{'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I'};

    public static DrawerConfig fromConfig(final ConfigurationSection section) {
        final String id = section.getName();

        final ConfigurationSection itemSection = section.getConfigurationSection("item");
        if (itemSection == null) return null;

        final String name = ColorUtil.color(itemSection.getString("name"));
        final List<String> lore = ColorUtil.color(itemSection.getStringList("lore"));
        final Material material = getByPath(itemSection, "material", Material.BARREL);

        final ItemStack drawer = new ItemStack(material);
        final ItemMeta meta = drawer.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(lore);
        drawer.setItemMeta(meta);
        DrawerManager.setId(drawer, id);

        final Material borderMaterial = getByPath(section, "border-material", Material.STRIPPED_OAK_WOOD);

        //Between 1 and 4
        final int limit = section.getInt("limit");
        final int slot = Math.max(1, Math.min(section.getInt("slot"), 4));

        final NamespacedKey recipeKey = getRecipe(id);

        final ShapedRecipe recipe = new ShapedRecipe(recipeKey, drawer);
        recipe.shape("ABC", "DEF", "GHI");

        final List<String> ingredients = section.getStringList("craft");
        for (int i = 0; i < ingredients.size(); i++) {
            final char symbol = symbols[i];
            final String ingredient = ingredients.get(i);
            try {
                final Material ingredientType = Material.valueOf(ingredient.toUpperCase());
                recipe.setIngredient(symbol, ingredientType);
            } catch (IllegalArgumentException ignored) {
                if (ingredient.startsWith("%") && ingredient.endsWith("%")) {
                    final String drawerId = ingredient.replace("%", "");
                    final DrawerConfig config = HDrawer.get().getConfiguration().getDrawerConfig(drawerId);
                    if (config != null) {
                        recipe.setIngredient(symbol, new RecipeChoice.ExactChoice(config.drawer()));
                        continue;
                    }
                }
                recipe.setIngredient(symbol, Material.AIR);
            }
        }

        final Server server = HDrawer.get().getServer();
        server.removeRecipe(recipeKey);
        server.addRecipe(recipe);

        return new DrawerConfig(id, drawer, borderMaterial, limit, slot);
    }

    private static NamespacedKey getRecipe(final String id) {
        return new NamespacedKey(HDrawer.get(), RECIPE_ID + "-" + id);
    }

    private static Material getByPath(final ConfigurationSection section, final String key, final Material defaultMaterial) {
        Material material = defaultMaterial;
        try {
            material = Material.valueOf(section.getString(key).toUpperCase());
        } catch (IllegalArgumentException ignored) {
        }

        return material;
    }

    public void removeRecipe() {
        HDrawer.get().getServer().removeRecipe(getRecipe(this.id));
    }
}

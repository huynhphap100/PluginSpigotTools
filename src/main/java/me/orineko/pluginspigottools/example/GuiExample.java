package me.orineko.pluginspigottools.example;

import me.orineko.pluginspigottools.GuiManager;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nonnull;
import java.util.HashMap;

public class GuiExample extends GuiManager {
    /**
     * <pre>
     * Create a constructor for GuiManager.
     * </pre>
     *
     * @param plugin          the plugin instance.
     * @param file            the file for inventory management.
     */
    public GuiExample(@Nonnull Plugin plugin, @Nonnull FileConfiguration file) {
        super(plugin, file, null, null);
    }

    public enum Key {
        KEY_B, KEY_C;

        public ItemManager getItemManager(@Nonnull me.orineko.pluginspigottools.GuiManager guiManager) {
            return guiManager.getItemManager(name());
        }
    }

    public ItemStack getItemC(@Nonnull Player player){
     ItemManager itemManager = Key.KEY_C.getItemManager(this);
     if(itemManager == null) return new ItemStack(Material.AIR);
     HashMap<String, String> map = new HashMap<>();
     map.put("<player>", player.getName());
     return me.orineko.pluginspigottools.MethodDefault.getItemReplaceValue(itemManager.getItemStack().clone(), map);
   }

}

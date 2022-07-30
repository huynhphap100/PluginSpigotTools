package me.orineko.pluginspigottools;

import com.cryptomorin.xseries.XMaterial;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class MethodDefault {

    /**
     * Check a string can be converted to a double type.
     *
     * @param text the text to check.
     * @return True if it can be converted, false otherwise.
     */
    public static boolean checkFormatNumber(@Nonnull String text) {
        return text.trim().matches("^-?\\d+(\\.\\d+)?$");
    }

    /**
     * Convert a string to a double type.
     *
     * @param text the text to convert.
     * @param valueDefault the value if it can't be converted.
     * @return the number if it can be converted, otherwise it's valueDefault.
     */
    public static double formatNumber(@Nonnull String text, double valueDefault){
        return checkFormatNumber(text) ? Double.parseDouble(text) : valueDefault;
    }

    public static String formatColor(@Nonnull String text) {
        try {
            Pattern pattern = Pattern.compile("#[a-fA-F0-9]{6}");
            Matcher matcher = pattern.matcher(text);
            while (matcher.find()) {
                String color = text.substring(matcher.start(), matcher.end());
                text = text.replace(color, net.md_5.bungee.api.ChatColor.of(color) + "");
                matcher = pattern.matcher(text);
            }
        } catch (IllegalArgumentException ignored) {
        }
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    public static ItemStack getItemAllVersion(String material) {
        if(material == null || material.isEmpty()) return new ItemStack(Material.AIR);
        material = material.toUpperCase();
        try {
            return new ItemStack(Material.valueOf(material));
        } catch (IllegalArgumentException ignore) {
            if (XMaterial.matchXMaterial(material).isPresent())
                return XMaterial.matchXMaterial(material).get().parseItem();
            else
                return new ItemStack(Material.AIR);
        }
    }

    public static ItemStack getItemStackByFile(@Nonnull FileConfiguration file, @Nonnull String path){
        String typeItem = file.getString(path+".Type", "");
        String nameItem = file.getString(path+".Name", "");
        List<String> loreItem = file.getStringList(path+".Lore");
        ItemStack itemStack = getItemAllVersion(typeItem.toUpperCase());
        ItemMeta meta = itemStack.getItemMeta();
        if(meta == null) return itemStack;
        meta.setDisplayName(formatColor(nameItem));
        meta.setLore(loreItem.stream().map(MethodDefault::formatColor).collect(Collectors.toList()));
        itemStack.setItemMeta(meta);
        return itemStack;
    }

    public static ItemStack getItemReplaceValue(@Nonnull ItemStack itemStack, @Nonnull HashMap<String, String> map){
        ItemMeta meta = itemStack.getItemMeta();
        if(meta == null) return itemStack;
        String name = meta.getDisplayName();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            String s = entry.getKey();
            String o = entry.getValue();
            name = name.replace(s, String.valueOf(o));
        }
        List<String> lore = (meta.getLore() == null) ? new ArrayList<>() : meta.getLore();
        lore = lore.stream().map(l -> {
            for (Map.Entry<String, String> entry : map.entrySet()) {
                String s = entry.getKey();
                String o = entry.getValue();
                l = l.replace(s, String.valueOf(o));
            }
            return l;
        }).collect(Collectors.toList());
        meta.setDisplayName(name);
        meta.setLore(lore);
        itemStack.setItemMeta(meta);
        return itemStack;
    }

}

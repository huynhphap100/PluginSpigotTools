package me.orineko.pluginspigottools;

import com.cryptomorin.xseries.XEnchantment;
import com.cryptomorin.xseries.XMaterial;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import javax.annotation.Nonnull;
import java.util.*;
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
        ItemStack itemStack = getItemAllVersion(typeItem.toUpperCase());
        return getItemStackByFileAndItem(file, path, itemStack);
    }

    public static ItemStack getItemStackByFileAndItem(@Nonnull FileConfiguration file, @Nonnull String path, @Nonnull ItemStack itemStack){
        String nameItem = file.getString(path+".Name", "");
        List<String> loreItem = file.getStringList(path+".Lore");
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

    @SuppressWarnings("deprecation")
    public static Collection<ItemStack> getDropItem(@Nonnull Player player, @Nonnull Block block) {
        ItemStack itemHold = player.getItemInHand();
        return getDropItem(itemHold, block);
    }

    public static Collection<ItemStack> getDropItem(@Nonnull ItemStack itemHand, @Nonnull Block block) {
        Collection<ItemStack> itemsDrop = block.getDrops(itemHand);
        String version = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];
        switch (version) {
            case "v1_8_R1":
            case "v1_8_R2":
            case "v1_8_R3":
            case "v1_9_R1":
            case "v1_9_R2":
            case "v1_10_R1":
            case "v1_11_R1":
            case "v1_12_R1":
            case "v1_13_R1":
            case "v1_13_R2":
                List<String> itemFortuneList = Arrays.asList("COAL", "DIAMOND", "EMERALD", "GOLD_NUGGET",
                        "QUARTZ", "LAPIS_LAZULI", "REDSTONE", "GLOWSTONE_DUST", "MELON_SLICE", "NETHER_WART",
                        "PRISMARINE");
                List<Material> matFortuneList = itemFortuneList.stream()
                        .map(XMaterial::matchXMaterial)
                        .filter(Optional::isPresent)
                        .map(i -> i.get().parseMaterial())
                        .collect(Collectors.toList());
                Collection<ItemStack> itemsNew = new ArrayList<>();
                Optional<XEnchantment> xSilkTouch = XEnchantment.matchXEnchantment("SILK_TOUCH");
                Optional<XEnchantment> xFortune = XEnchantment.matchXEnchantment("LOOT_BONUS_BLOCKS");
                Enchantment silkTouch = null;
                Enchantment fortune = null;
                if (xSilkTouch.isPresent()) silkTouch = xSilkTouch.get().getEnchant();
                if (xFortune.isPresent()) fortune = xFortune.get().getEnchant();

                ItemMeta meta = itemHand.getItemMeta();
                if (meta == null) return itemsDrop;
                boolean hasSilkTouch = silkTouch != null && meta.hasEnchant(silkTouch);
                boolean hasFortune = fortune != null && meta.hasEnchant(fortune);
                if (hasSilkTouch) {
                    itemsNew.add(new ItemStack(block.getType()));
                    return itemsNew;
                } else if (hasFortune) {
                    int value = meta.getEnchantLevel(fortune);
                    itemsDrop.forEach(i -> {
                        if (!matFortuneList.contains(i.getType())) {
                            itemsNew.add(i);
                            return;
                        }
                        int bonus = (int) (Math.random() * (value + 2)) - 1;
                        if (bonus < 0) bonus = 0;
                        long total = i.getAmount() + bonus;
                        while (total > 0) {
                            if (total > 64) {
                                i.setAmount(64);
                                itemsNew.add(i);
                                total -= 64;
                            } else {
                                i.setAmount((int) total);
                                itemsNew.add(i);
                                total = 0;
                            }
                        }
                    });
                    return itemsNew;
                }
                break;
            default:
                return itemsDrop;
        }
        return itemsDrop;
    }

}

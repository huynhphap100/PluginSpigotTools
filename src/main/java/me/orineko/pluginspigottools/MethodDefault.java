package me.orineko.pluginspigottools;

import com.cryptomorin.xseries.XEnchantment;
import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.messages.Titles;
import lombok.NonNull;
import me.orineko.pluginspigottools.api.itemsadder.ItemsAdderManager;
import me.orineko.pluginspigottools.api.itemsadder.ItemsAdderSetup;
import me.orineko.pluginspigottools.api.nbt.NBTApiSetup;
import me.orineko.pluginspigottools.api.nbt.NBTApiTool;
import net.objecthunter.exp4j.ExpressionBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

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
     * @param text         the text to convert.
     * @param valueDefault the value if it can't be converted.
     * @return the number if it can be converted, otherwise it's valueDefault.
     */
    public static double formatNumber(@Nonnull String text, double valueDefault) {
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

    public static List<String> formatColor(@Nonnull List<String> textList) {
        return textList.stream().map(MethodDefault::formatColor).collect(Collectors.toList());
    }

    public static List<FileManager> getAllFileInFolder(@Nonnull Plugin plugin, @Nonnull String... folder) {
        List<FileManager> fileManagerList = new ArrayList<>();
        File file = new File(plugin.getDataFolder(), String.join(File.separator, folder));
        if (!file.exists()) return fileManagerList;
        File[] files = file.listFiles();
        if (files == null) return fileManagerList;
        for (File f : files) {
            if (f.isDirectory()) continue;
            FileManager fileManager = new FileManager(f.getName(), plugin);
            fileManager.createFolder(folder);
            fileManager.createFile();
            fileManagerList.add(fileManager);
        }
        return fileManagerList;
    }

    public static List<File> getAllFolderInFolder(@Nonnull Plugin plugin, @Nonnull String... folder) {
        List<File> fileList = new ArrayList<>();
        File file = new File(plugin.getDataFolder(), String.join(File.separator, folder));
        if (!file.exists()) return fileList;
        File[] files = file.listFiles();
        if (files == null) return fileList;
        for (File f : files) {
            if (!f.isDirectory()) continue;
            fileList.add(f);
        }
        return fileList;
    }

    public static int randomInt(int min, int max) {
        return new Random().nextInt((max - min) + 1) + min;
    }

    public static double randomDouble(double min, double max) {
        return min + (max - min) * (new Random().nextDouble());
    }

    public static double stringCalculate(@Nonnull String text) {
        return new ExpressionBuilder(text).build().evaluate();
    }

    public static ItemStack getItemForGui(ItemStack itemStack) {
        NBTApiTool nbtApiTool = NBTApiSetup.getNbtApiTool();
        if (nbtApiTool != null) return nbtApiTool.setNbtItemForGui(itemStack);
        return itemStack;
    }

    public static ItemStack getItemForGuiNoNbtTag(ItemStack itemStack) {
        NBTApiTool nbtApiTool = NBTApiSetup.getNbtApiTool();
        if (nbtApiTool != null) {
            ItemStack itemStack1 = nbtApiTool.removeAllNbtItem(itemStack);
            return nbtApiTool.setNbtItemForGui(itemStack1);
        }
        return itemStack;
    }

    public static boolean checkItemIsDefault(@Nonnull ItemStack itemStack) {
        ItemMeta meta = itemStack.getItemMeta();
        if (meta == null) return true;
        return !meta.hasDisplayName() && !meta.hasLore();
    }

    public static ItemStack getItemAllVersion(@Nonnull String material) {
        if (material.isEmpty()) return new ItemStack(Material.AIR);
        if (ItemsAdderSetup.getItemsAdderManager() != null && material.startsWith("[ItemsAdder]")) {
            ItemStack itemStack = ItemsAdderSetup.getItemsAdderManager()
                    .getItem(material.replace("[ItemsAdder]", ""));
            if (itemStack != null) return itemStack;
        }
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

    public static ItemStack getItemStackByFile(@Nonnull FileConfiguration file, @Nonnull String path) {
        String typeItem = file.getString(path + ".Type", "");
        ItemStack itemStack = getItemAllVersion(typeItem.toUpperCase());
        return getItemStackByFileAndItem(file, path, itemStack);
    }

    public static ItemStack getItemStackByFileAndItem(@Nonnull FileConfiguration file, @Nonnull String path, @Nonnull ItemStack itemStack) {
        String nameItem = file.getString(path + ".Name", "");
        List<String> loreItem = file.getStringList(path + ".Lore");
        int customModelData = file.getInt(path + ".CustomModelData", 0);
        ItemMeta meta = itemStack.getItemMeta();
        if (meta == null) return itemStack;
        meta.setDisplayName(formatColor(nameItem));
        meta.setLore(loreItem.stream().map(MethodDefault::formatColor).collect(Collectors.toList()));
        if (customModelData != 0) meta.setCustomModelData(customModelData);
        file.getStringList(path + ".Flags").forEach(v2 -> {
            try {
                ItemFlag itemFlag = ItemFlag.valueOf(v2.toUpperCase());
                meta.addItemFlags(itemFlag);
            } catch (IllegalArgumentException ignore) {
            }
        });
        itemStack.setItemMeta(meta);
        int amount = file.getInt(path + ".Amount", 1);
        itemStack.setAmount(amount);
        file.getStringList(path + ".Enchants").forEach(v -> {
            String[] arr = v.split(" ");
            String enchantmentName = arr[0];
            int level = 1;
            if (arr.length > 1) level = (int) MethodDefault.formatNumber(arr[1], 1);
            Optional<XEnchantment> xEnchantmentOptional = XEnchantment.matchXEnchantment(enchantmentName);
            if (xEnchantmentOptional.isPresent() && xEnchantmentOptional.get().getEnchant() != null)
                itemStack.addUnsafeEnchantment(xEnchantmentOptional.get().getEnchant(), level);
        });
        return itemStack;
    }

    public static ItemStack getItemReplaceValue(@Nonnull ItemStack itemStack, @Nonnull HashMap<String, String> map) {
        ItemMeta meta = itemStack.getItemMeta();
        if (meta == null) return itemStack;
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

    public static void addItemInventory(@Nonnull Player player, @Nonnull ItemStack itemStack) {
        if (player.getInventory().firstEmpty() != -1) {
            player.getInventory().addItem(itemStack);
        } else {
            player.getWorld().dropItemNaturally(player.getLocation(), itemStack);
        }
    }

    public static void sendTitleAllVersion(Player player, String title, String subTitle) {
        try {
            player.sendTitle(title, subTitle, 5, 10, 5);
        } catch (Exception ignore) {
            Titles.sendTitle(player, 5, 10, 5, title, subTitle);
        }
    }

    @SuppressWarnings("deprecation")
    public static Collection<ItemStack> getDropItem(@Nonnull Player player, @Nonnull Block block) {
        ItemStack itemHold = player.getItemInHand();
        return getDropItem(itemHold, block);
    }

    public static void zip(@NonNull Plugin plugin, @NonNull String folderZip, @NonNull String folderDestination) {
        zip(plugin, folderZip, folderDestination, null);
    }

    public static void zip(@NonNull Plugin plugin, @NonNull String folderZip, @NonNull String folderDestination, String fileName) {
        String pathMain = plugin.getDataFolder().toString();
        folderZip = pathMain + "/" + folderZip.replace("\\", "/");
        folderDestination = pathMain + "/" + folderDestination.replace("\\", "/");

        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss");
        String formattedDateTime = now.format(formatter);
        String zipName = ((fileName != null) ? fileName : "") + formattedDateTime + ".zip";

        try (FileOutputStream fos = new FileOutputStream(zipName);
             ZipOutputStream zos = new ZipOutputStream(fos)) {

            Path sourcePath = Paths.get(folderZip);

            try (Stream<Path> paths = Files.walk(sourcePath)) {
                paths.forEach(path -> {
                    try {
                        String zipEntryName = sourcePath.relativize(path).toString();
                        if (Files.isDirectory(path)) {
                            zipEntryName += "/";
                        }
                        ZipEntry zipEntry = new ZipEntry(zipEntryName);
                        zos.putNextEntry(zipEntry);

                        if (Files.isRegularFile(path)) {
                            Files.copy(path, zos);
                        }

                        zos.closeEntry();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            }

            Path destinationDir = Paths.get(folderDestination);
            if (!Files.exists(destinationDir)) {
                Files.createDirectories(destinationDir);
            }

            Path sourceZipPath = Paths.get(zipName);
            Path destinationPath = Paths.get(folderDestination, sourceZipPath.getFileName().toString());
            Files.move(sourceZipPath, destinationPath, StandardCopyOption.REPLACE_EXISTING);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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

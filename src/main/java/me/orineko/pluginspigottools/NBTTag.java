package me.orineko.pluginspigottools;

import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;

public class NBTTag {

    public static ItemStack setKey(@Nonnull ItemStack item, @Nonnull String key, String value) {
        try {
            Class<?> itemStackClass = getItemStackClass();
            Class<?> craftItemStackClass = getCraftItemStackClass();
            Class<?> nbtTagCompoundClass = getNBTTagCompoundClass();
            Object itemCraft = craftItemStackClass.getMethod("asNMSCopy", ItemStack.class).invoke(null, item);
            String version = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];
            Object tag;
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
                case "v1_14_R1":
                case "v1_15_R1":
                case "v1_16_R1":
                case "v1_16_R2":
                case "v1_16_R3":
                case "v1_17_R1":
                    tag = ((boolean) itemCraft.getClass().getMethod("hasTag").invoke(itemCraft)) ?
                            itemCraft.getClass().getMethod("getTag").invoke(itemCraft) :
                            nbtTagCompoundClass.newInstance();
                    if (tag == null) return item;
                    tag.getClass().getMethod("setString", String.class, String.class).invoke(tag, key, value);
                    itemCraft.getClass().getMethod("setTag", nbtTagCompoundClass).invoke(itemCraft, tag);
                    break;
                default:
                    tag = ((boolean) itemCraft.getClass().getMethod("s").invoke(itemCraft)) ?
                            itemCraft.getClass().getMethod("t").invoke(itemCraft) :
                            nbtTagCompoundClass.newInstance();
                    if (tag == null) return item;
                    tag.getClass().getMethod("a", String.class, String.class).invoke(tag, key, value);
                    itemCraft.getClass().getMethod("c", nbtTagCompoundClass).invoke(itemCraft, tag);
            }
            return (ItemStack) craftItemStackClass.getMethod("asBukkitCopy", itemStackClass).invoke(null, itemCraft);
        } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException | IllegalAccessException | InstantiationException e) {
            e.printStackTrace();
        }
        return item;
    }

    public static ItemStack removeKey(@Nonnull ItemStack item, @Nonnull String key) {
        try {
            Class<?> itemStackClass = getItemStackClass();
            Class<?> craftItemStackClass = getCraftItemStackClass();
            Class<?> nbtTagCompoundClass = getNBTTagCompoundClass();
            Object itemCraft = craftItemStackClass.getMethod("asNMSCopy", ItemStack.class).invoke(null, item);
            String version = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];
            Object tag;
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
                case "v1_14_R1":
                case "v1_15_R1":
                case "v1_16_R1":
                case "v1_16_R2":
                case "v1_16_R3":
                case "v1_17_R1":
                    tag = ((boolean) itemCraft.getClass().getMethod("hasTag").invoke(itemCraft)) ?
                            itemCraft.getClass().getMethod("getTag").invoke(itemCraft) :
                            nbtTagCompoundClass.newInstance();
                    if (tag == null) return item;
                    tag.getClass().getMethod("remove", String.class).invoke(tag, key);
                    itemCraft.getClass().getMethod("setTag", nbtTagCompoundClass).invoke(itemCraft, tag);
                    break;
                default:
                    tag = ((boolean) itemCraft.getClass().getMethod("s").invoke(itemCraft)) ?
                            itemCraft.getClass().getMethod("t").invoke(itemCraft) :
                            nbtTagCompoundClass.newInstance();
                    if (tag == null) return item;
                    tag.getClass().getMethod("r", String.class).invoke(tag, key);
                    itemCraft.getClass().getMethod("c", nbtTagCompoundClass).invoke(itemCraft, tag);
            }
            return (ItemStack) craftItemStackClass.getMethod("asBukkitCopy", itemStackClass).invoke(null, itemCraft);
        } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException | IllegalAccessException | InstantiationException e) {
            e.printStackTrace();
        }
        return item;
    }

    @Nullable
    public static String getKey(@Nonnull ItemStack item, @Nonnull String key) {
        try {
            Class<?> craftItemStackClass = getCraftItemStackClass();
            Class<?> nbtTagCompoundClass = getNBTTagCompoundClass();
            Object itemCraft = craftItemStackClass.getMethod("asNMSCopy", ItemStack.class).invoke(null, item);
            String version = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];
            Object tag;
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
                case "v1_14_R1":
                case "v1_15_R1":
                case "v1_16_R1":
                case "v1_16_R2":
                case "v1_16_R3":
                case "v1_17_R1":
                    tag = ((boolean) itemCraft.getClass().getMethod("hasTag").invoke(itemCraft)) ?
                            itemCraft.getClass().getMethod("getTag").invoke(itemCraft) :
                            nbtTagCompoundClass.newInstance();
                    if (tag == null) return null;
                    return (String) tag.getClass().getMethod("getString", String.class).invoke(tag, key);
                default:
                    tag = ((boolean) itemCraft.getClass().getMethod("s").invoke(itemCraft)) ?
                            itemCraft.getClass().getMethod("t").invoke(itemCraft) :
                            nbtTagCompoundClass.newInstance();
                    if (tag == null) return null;
                    return (String) tag.getClass().getMethod("l", String.class).invoke(tag, key);
            }
        } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException | IllegalAccessException | InstantiationException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static Class<?> getNBTTagCompoundClass() throws ClassNotFoundException {
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
            case "v1_14_R1":
            case "v1_15_R1":
            case "v1_16_R1":
            case "v1_16_R2":
            case "v1_16_R3":
                return Class.forName("net.minecraft.server." + version + ".NBTTagCompound");
            default:
                return Class.forName("net.minecraft.nbt.NBTTagCompound");
        }
    }

    private static Class<?> getCraftItemStackClass() throws ClassNotFoundException {
        String version = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];
        return Class.forName("org.bukkit.craftbukkit." + version + ".inventory.CraftItemStack");
    }

    private static Class<?> getItemStackClass() throws ClassNotFoundException {
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
            case "v1_14_R1":
            case "v1_15_R1":
            case "v1_16_R1":
            case "v1_16_R2":
            case "v1_16_R3":
                return Class.forName("net.minecraft.server." + version + ".ItemStack");
            default:
                return Class.forName("net.minecraft.world.item.ItemStack");
        }
    }

}

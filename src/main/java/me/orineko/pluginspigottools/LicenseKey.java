package me.orineko.pluginspigottools;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@SuppressWarnings("unused")
public class LicenseKey {
	
	private static boolean premium = false;
    private static String namePlugin = "";

    public static boolean getPremium(){
        return premium;
    }

	public static void disablePlugin(Plugin plugin){
        try {
            namePlugin = namePlugin.isEmpty() ? plugin.getDescription().getName() : namePlugin;
            if (action(plugin, plugin.getConfig().getString("license"), "disable")) {
                Bukkit.getConsoleSender().sendMessage("§e"+ namePlugin + ": §aLicense you use have been disabled!");
                Bukkit.getConsoleSender().sendMessage("§e"+ namePlugin + ": §aThank you for using our product");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean setupFileManger(Plugin plugin){
        namePlugin = namePlugin.isEmpty() ? plugin.getDescription().getName() : namePlugin;
        Bukkit.getConsoleSender().sendMessage("§f-----------------------------------");
        Bukkit.getConsoleSender().sendMessage("§e"+namePlugin+" is sold by Di Hoa Store");
        Bukkit.getConsoleSender().sendMessage("");
        Bukkit.getConsoleSender().sendMessage("§fAuthor: §ePhapPurple");
        Bukkit.getConsoleSender().sendMessage("§fVersion: §ev"+plugin.getDescription().getVersion());
        Bukkit.getConsoleSender().sendMessage("§fServer version: §e1.8 -> 1.19.1");
        Bukkit.getConsoleSender().sendMessage("§fSupport: §eBukkit, Spigot, Paper");
        Bukkit.getConsoleSender().sendMessage("");
        Bukkit.getConsoleSender().sendMessage("§fWebsite: §ewww.dihoastore.com");
        Bukkit.getConsoleSender().sendMessage("§f-----------------------------------");
        if (KeyStatus(plugin, plugin.getConfig().getString("license"))) {
            if (action(plugin, plugin.getConfig().getString("license"), "enable")) {
                premium = true;
                Bukkit.getConsoleSender().sendMessage("§e" + namePlugin + ": §aYour product key has been activated!");
                Bukkit.getConsoleSender().sendMessage("§e" + namePlugin + ": §aThank you for using our product");
            } else {
                Bukkit.getConsoleSender().sendMessage("§e" + namePlugin + ": §cThe key product you entered is already in use!");
                Bukkit.getServer().getPluginManager().disablePlugin(plugin);
            }
        } else {
            Bukkit.getConsoleSender().sendMessage("§e" + namePlugin + ": §cYour product key entered is incorrect");
            Bukkit.getConsoleSender().sendMessage("§e" + namePlugin + ": §cPlugin has been disabled...");
            Bukkit.getServer().getPluginManager().disablePlugin(plugin);
        }
        if (!premium)
            Bukkit.getServer().getPluginManager().disablePlugin(plugin);
        return premium;
    }

    public static boolean KeyStatus(Plugin plugin, String key) {
        try {
            if (getIpaddress(plugin) == null)
                return false;
            namePlugin = namePlugin.isEmpty() ? plugin.getDescription().getName() : namePlugin;
            String query = key + "!K04!" + getIpaddress(plugin);
            HttpURLConnection connection = (HttpURLConnection)(new URL("https://dihoastore-mc.tk/api/api.php?query=" + getMd5(query) + "&plugin=" + namePlugin + "&license=" + key + "&ip=" + getIpaddress(plugin))).openConnection();
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
            connection.setUseCaches(false);
            connection.setAllowUserInteraction(false);
            connection.setConnectTimeout(2000);
            connection.connect();
            BufferedReader stream = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            String inputLine;
            while ((inputLine = stream.readLine()) != null)
                sb.append(inputLine);
            stream.close();
            connection.disconnect();
            String json = sb.toString().replace(" ", "");
            JSONParser parser = new JSONParser();
            JSONObject object = (JSONObject)parser.parse(json);
            if (object.get("error").toString().equals("yes"))
                return false;
            if (object.get("error").toString().equals("no")) {
                return !object.get("status").toString().equals("online");
            }
            return false;
        } catch (IOException|org.json.simple.parser.ParseException e) {
            Bukkit.getConsoleSender().sendMessage("§e" + namePlugin + ": §aJson can't be parse, please contact Di Hoa Store");
            return false;
        }
    }

    public static boolean action(Plugin plugin, String key, String action) {
        try {
            if (getIpaddress(plugin) == null)
                return false;
            namePlugin = namePlugin.isEmpty() ? plugin.getDescription().getName() : namePlugin;
            String query = key + "!K04!" + getIpaddress(plugin);
            URL url = new URL("https://dihoastore-mc.tk/api/api.php?query=" + getMd5(query) + "&plugin=" + namePlugin + "&license=" + key + "&action=" + action + "&ip=" + getIpaddress(plugin));
            BufferedReader stream = new BufferedReader(new InputStreamReader(url.openStream()));
            StringBuilder sb = new StringBuilder();
            String inputLine;
            while ((inputLine = stream.readLine()) != null)
                sb.append(inputLine);
            stream.close();
            String json = sb.toString().replace(" ", "");
            JSONParser parser = new JSONParser();
            JSONObject object = (JSONObject)parser.parse(json);
            if (object.get("error").toString().equals("yes")) {
                Bukkit.getConsoleSender().sendMessage("§e" + namePlugin + ": §cKey have an error, please contact Di Hoa Store");
                return false;
            }
            if (action.equals("enable")) {
                return object.get("status").toString().equals("offline");
            }
            return object.get("status").toString().equals("online");
        } catch (IOException|org.json.simple.parser.ParseException e) {
            Bukkit.getConsoleSender().sendMessage("§e" + namePlugin + ": §aJson can't be parse, please contact Di Hoa Store");
            return false;
        }
    }

    @SuppressWarnings("unused")
    public static String getVersion(Plugin plugin) {
        try {
            URL url = new URL("https://dihoastore-mc.tk/api/update.php?plugin=" + namePlugin);
            BufferedReader stream = new BufferedReader(new InputStreamReader(
                    url.openStream()));
            StringBuilder sb = new StringBuilder();
            String inputLine;
            while ((inputLine = stream.readLine()) != null)
                sb.append(inputLine);
            stream.close();
            String json = sb.toString();
            JSONParser parser = new JSONParser();
            JSONObject object = (JSONObject)parser.parse(json);
            if (object.get("error") == "yes")
                return "error string";
            return object.get("version").toString();
        } catch (IOException|org.json.simple.parser.ParseException e) {
            return "parser error";
        }
    }

    public static String getMd5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(input.getBytes());
            BigInteger no = new BigInteger(1, messageDigest);
            StringBuilder hashtext = new StringBuilder(no.toString(16));
            while (hashtext.length() < 32)
                hashtext.insert(0, "0");
            return hashtext.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private static String getIpaddress(Plugin plugin) {
        String ip = null;
        try {
            URL whatismyip = new URL("http://checkip.amazonaws.com");
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    whatismyip.openStream()));
            ip = in.readLine();
        } catch (IOException ex) {
            Bukkit.getConsoleSender().sendMessage("§e"+ namePlugin +": §aCan't get your ip address");
        }
        return ip;
    }

}

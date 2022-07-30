package me.orineko.pluginspigottools;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.StringUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
public abstract class CommandManager extends Command implements CommandExecutor, PluginIdentifiableCommand {

    private static CommandMap commandMap;

    static {
        try {
            Field field = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            field.setAccessible(true);
            commandMap = (CommandMap) field.get(Bukkit.getServer());
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private final Plugin plugin;
    private final HashMap<Integer, ArrayList<TabCommandManager>> tabComplete;
    private boolean register = false;

    /**
     * @param plugin plugin responsible of the command.
     * @param name   name of the command.
     */
    public CommandManager(@Nonnull Plugin plugin, @Nonnull String name) {
        super(name);
        assert commandMap != null;
        assert name.length() > 0;

        setLabel(name);
        this.plugin = plugin;
        this.tabComplete = new HashMap<>();
        setDescription("This is main command.");
    }

    /**
     * @param plugin  plugin responsible of the command.
     * @param name    name of the command.
     * @param aliases alias of the command.
     */
    public CommandManager(@Nonnull Plugin plugin, @Nonnull String name, @Nonnull String... aliases) {
        this(plugin, name);
        setAliases(aliases);
    }

    /**
     * @param aliases aliases of the command.
     */
    protected void setAliases(String... aliases) {
        if (aliases != null && (register || aliases.length > 0))
            setAliases(Arrays.stream(aliases).collect(Collectors.toList()));
    }

    /**
     * Check player has permission or not.
     * @param player player to check.
     * @param permission permission to check.
     * @return true if player has permission. If player don't have permission, console will send message to player.
     */
    protected boolean checkPermission(@Nonnull Player player, @Nonnull String permission){
        if(player.isOp() || player.hasPermission(permission)) return true;
        player.sendMessage("§cYou do not have permission to use this command!");
        return false;
    }

    /**
     * Add multiple arguments to an index with permission and words before
     *
     * @param indice     index where the argument is in the command. /myCmd is at the index -1, so
     *                   /myCmd index0 index1 ...
     * @param permission permission to add (may be null)
     * @param beforeText text preceding the argument (may be null)
     * @param arg        word to add
     */
    @SuppressWarnings("SameParameterValue")
    protected void addTabComplete(int indice, String permission, String[] beforeText, List<String> arg) {
        if (arg != null && arg.size() > 0 && indice >= 0) {
            if (tabComplete.containsKey(indice)) {
                tabComplete.get(indice).addAll(arg.stream().collect(
                        ArrayList::new,
                        (tabCommands, s) -> tabCommands.add(new TabCommandManager(indice, s, permission, beforeText)),
                        ArrayList::addAll));
            } else {
                tabComplete.put(indice, arg.stream().collect(
                        ArrayList::new,
                        (tabCommands, s) -> tabCommands.add(new TabCommandManager(indice, s, permission, beforeText)),
                        ArrayList::addAll)
                );
            }
        }
    }
    protected void addTabComplete(int indice, @Nonnull String permission, @Nonnull String[] beforeText, String... args){
        addTabComplete(indice, permission, beforeText, Arrays.asList(args));
    }
    protected void addTabComplete(int indice, @Nonnull String[] beforeText, @Nonnull List<String> arg){
        addTabComplete(indice, null, beforeText, arg);
    }
    protected void addTabComplete(int indice, @Nonnull List<String> beforeText, @Nonnull List<String> arg){
        addTabComplete(indice, null, beforeText.toArray(new String[0]), arg);
    }
    protected void addTabComplete(int indice, @Nonnull String beforeText, @Nonnull List<String> arg){
        addTabComplete(indice, null, new String[]{beforeText}, arg);
    }

    /**
     * Add multiple arguments to an index
     *
     * @param indice index where the argument is in the command. /myCmd is at the index -1, so
     *               /myCmd index0 index1 ...
     * @param arg    word to add
     */
    protected void addTabComplete(int indice, @Nonnull List<String> arg) {
        addTabComplete(indice, null, null, arg);
    }
    protected void addTabComplete(int indice, String... arg){
        addTabComplete(indice, Arrays.asList(arg));
    }

    /**
     * /!\ to do at the end /!\ to save the command.
     *
     */
    protected void registerCommand() {
        if (!register) {
            register = commandMap.register(plugin.getName(), this);
        }
    }

    /**
     * @return plugin responsible for the command
     */
    @Nonnull
    @Override
    public Plugin getPlugin() {
        return this.plugin;
    }

    /**
     * @return tabComplete
     */
    public HashMap<Integer, ArrayList<TabCommandManager>> getTabComplete() {
        return tabComplete;
    }

    /**
     * @param commandSender sender
     * @param command       command
     * @param arg           argument of the command
     *
     * @return true if ok, false otherwise
     */
    @Override
    public boolean execute(@Nonnull CommandSender commandSender, @Nonnull String command, @Nonnull String[] arg) {
        if (getPermission() != null) {
            if (!commandSender.hasPermission(getPermission())) {
                if (getPermissionMessage() == null) {
                    commandSender.sendMessage(ChatColor.RED + "no permit!");
                }else {
                    commandSender.sendMessage(getPermissionMessage());
                }
                return false;
            }
        }
        if (onCommand(commandSender, this, command, arg))
            return true;
        commandSender.sendMessage(ChatColor.RED + getUsage());
        return false;
    }

    /**
     * @param sender sender
     * @param alias  alias used
     * @param args   argument of the command
     *
     * @return a list of possible values
     */
    @Nonnull
    @Override
    public List<String> tabComplete(@Nonnull CommandSender sender, @Nonnull String alias, String[] args) {
        int indice = args.length - 1;
        List<String> list = advancedTabCompleted(args, indice, executeTabComplete(sender, indice, getLabel(), args));
        if(list != null && list.size() > 0) return list;
        if ((getPermission() != null && !sender.hasPermission(getPermission())) || tabComplete.size() == 0 || !tabComplete.containsKey(indice))
            return super.tabComplete(sender, alias, args);
        list = tabComplete.get(indice).stream()
                .filter(tabCommand -> tabCommand.getTextAvant() == null || tabCommand.getTextAvant().contains(args[indice - 1]))
                .filter(tabCommand -> tabCommand.getPermission() == null || sender.hasPermission(tabCommand.getPermission()))
                .map(TabCommandManager::getText)
                .filter(text -> text.startsWith(args[indice]))
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .collect(Collectors.toList());
        return list.size() < 1 ? super.tabComplete(sender, alias, args) : list;
    }

    @Nullable
    private List<String> advancedTabCompleted(String[] args, int num, List<String> list) {
        if(list == null) return null;
        List<String> completions = new ArrayList<>();
        StringUtil.copyPartialMatches(args[num], list, completions);
        Collections.sort(completions);
        return completions;
    }

    @Override
    public boolean onCommand(@Nonnull CommandSender sender, @Nonnull Command cmd, @Nonnull String label, @Nonnull String[] args) {
        executeCommand(sender, cmd, label, args);
        return true;
    }

    protected boolean isUsingMainCommand(@Nonnull Command cmd){
        return cmd.getName().equalsIgnoreCase(super.getName()) ||
                super.getAliases().stream().anyMatch(a -> cmd.getName().equalsIgnoreCase(a));
    }

    @Nullable
    protected Player getPlayerUsingCommand(@Nonnull CommandSender sender){
        if(sender instanceof Player) return (Player) sender;
        return null;
    }
	
	protected boolean checkNoPermission(@Nonnull CommandSender sender, @Nonnull String permission, @Nonnull String message) {
        if (!(sender instanceof Player) || sender.hasPermission(permission) || sender.isOp()) return false;
        sender.sendMessage(message);
        return true;
    }

    protected boolean checkObjectIsNull(Object object, @Nonnull CommandSender sender, @Nonnull String message){
        if(object != null) return false;
        sender.sendMessage(message);
        return true;
    }

    protected boolean checkObjectIsFalse(Boolean bool, @Nonnull CommandSender sender, @Nonnull String message){
        if(bool) return false;
        sender.sendMessage(message);
        return true;
    }

    protected boolean checkLackOfLength(@Nonnull String[] args, int length, @Nonnull CommandSender sender, @Nonnull String message){
        if(args.length >= length) return false;
        sender.sendMessage(message);
        return true;
    }

    protected boolean checkEqualArgs(@Nonnull String[] args, int indicate, @Nonnull String... cmdList){
        if(indicate >= args.length) return false;
        String cmd = args[indicate].toUpperCase();
        return Arrays.stream(cmdList).map(String::toUpperCase).anyMatch(c -> c.equals(cmd));
    }

    public abstract void executeCommand(@Nonnull CommandSender sender, @Nonnull Command cmd, @Nonnull String label, @Nonnull String[] args);
    public abstract List<String> executeTabComplete(@Nonnull CommandSender sender, int indicate, @Nonnull String label, @Nonnull String[] args);

    private static class TabCommandManager{
        private final int indice;
        private final String text;
        private final String permission;
        private final ArrayList<String> textAvant;
        private TabCommandManager(int indice, String text, String permission, String... textAvant){
            this.indice = indice;
            this.text = text;
            this.permission = permission;
            if(textAvant == null || textAvant.length < 1)
                this.textAvant = null;
            else
                this.textAvant = Arrays.stream(textAvant).collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
        }
        public String getText() {
            return text;
        }
        public int getIndice() {
            return indice;
        }
        public String getPermission() {
            return permission;
        }
        public ArrayList<String> getTextAvant() {
            return textAvant;
        }
    }
}

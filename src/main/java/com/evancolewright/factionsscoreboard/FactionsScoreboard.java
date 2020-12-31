package com.evancolewright.factionsscoreboard;

import com.evancolewright.factionsscoreboard.scoreboard.ScoreboardManager;
import com.evancolewright.factionsscoreboard.utils.ChatUtils;
import com.evancolewright.factionsscoreboard.utils.FactionsMapHelper;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.logging.Logger;

public final class FactionsScoreboard extends JavaPlugin implements Listener, CommandExecutor
{
    private final Logger log = getServer().getLogger();
    private final PluginManager pluginManager = getServer().getPluginManager();

    private ScoreboardManager scoreboardManager;

    @Override
    public void onEnable()
    {
        if (checkFactionsUUID())
        {
            log.info("FactionsUUID found!  If the plugin does not work, please download the most recent version of FactionsUUID!");
            this.pluginManager.registerEvents(this, this);
            this.getCommand("factionscoreboard").setExecutor(this);
            saveDefaultConfig();
            // Initialize Scoreboard Manager
            scoreboardManager = new ScoreboardManager(this);
        } else
        {
            log.severe("FactionsUUID not found.  Disabling FactionsScoreboard v" + this.getDescription().getVersion() + "......");
            this.pluginManager.disablePlugin(this);
        }
    }

    // ######################################//
    // Command
    // ######################################//

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args)
    {
        if (commandSender instanceof Player && command.getName().equalsIgnoreCase("factionscoreboard"))
        {
            final Player player = (Player) commandSender;

            if (player.hasPermission("factionsscoreboard.toggle"))
            {
                if (scoreboardManager.toggle(player))
                {
                    player.sendMessage(ChatUtils.colorize(getConfig().getString("messages.enabled")));
                    ChatUtils.colorizeList(FactionsMapHelper.getChunksAroundPlayer(player)).forEach(player::sendMessage);
                } else
                {
                    player.sendMessage(ChatUtils.colorize(getConfig().getString("messages.disabled")));
                    ChatUtils.colorizeList(FactionsMapHelper.getChunksAroundPlayer(player)).forEach(player::sendMessage);
                }
            }
        }
        return false;
    }

    // ######################################//
    // Listeners
    // ######################################//

    @EventHandler
    public void onJoin(PlayerJoinEvent event)
    {
        Player player = event.getPlayer();
        scoreboardManager.add(player);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event)
    {
        Player player = event.getPlayer();
        scoreboardManager.remove(player);
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event)
    {
        String command = event.getMessage();

        if (command.contains("/rl") || command.contains("/reload"))
        {
            event.getPlayer().sendMessage("FactionsScoreboard does not support reload!  All players must relog! ");
        }

    }

    /**
     * Soft-check for the existence of ** FactionsUUID **
     * This method is not very stable, so please do not use it in your own plugin as some versions may work differently than others.
     * <p>
     * For example, in a recent-ish version, The role enum was completely changed causing
     * errors on the older API.
     *
     * @return If factions exists or not.
     */
    private boolean checkFactionsUUID()
    {
        Plugin plugin = this.pluginManager.getPlugin("Factions");
        if (plugin != null)
        {
            return plugin.getDescription().getAuthors().contains("drtshock");
        }
        return false;
    }
}

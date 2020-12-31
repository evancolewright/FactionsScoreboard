package com.evancolewright.factionsscoreboard.scoreboard;

import com.evancolewright.factionsscoreboard.FactionsScoreboard;
import com.evancolewright.factionsscoreboard.utils.ChatUtils;
import com.evancolewright.factionsscoreboard.utils.FactionsHelper;
import com.evancolewright.factionsscoreboard.utils.FactionsMapHelper;
import com.evancolewright.factionsscoreboard.utils.MapSorter;
import com.massivecraft.factions.*;
import com.massivecraft.factions.perms.Relation;
import fr.mrmicky.fastboard.FastBoard;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.*;

public final class ScoreboardManager
{
    private final FileConfiguration config;

    public final Map<UUID, FastBoard> scoreboards = new HashMap<>();

    public ScoreboardManager(FactionsScoreboard plugin)
    {
        this.config = plugin.getConfig();

        if (config.getBoolean("scoreboard.enabled"))
            Bukkit.getScheduler().runTaskTimer(plugin, () -> scoreboards.values().forEach(this::updateScoreboard), 0, 20);
    }

    public void updateScoreboard(FastBoard board)
    {
        java.lang.String title = !FactionsHelper.hasFaction(board.getPlayer())
                ? ChatUtils.colorize(config.getString("scoreboard.title.no_faction"))
                : ChatUtils.colorize(config.getString("scoreboard.title.faction")
                .replace("{FACTION}", FactionsHelper.getFaction(board.getPlayer()).getTag()));

        board.updateTitle(title);

        if (FactionsHelper.hasFaction(board.getPlayer()))
        {
            board.updateLines(ChatUtils.colorizeList(this.getLines(FPlayers.getInstance().getByPlayer(board.getPlayer()))));
        } else
        {
            List<String> lines = new ArrayList<>();

            if (config.getBoolean("scoreboard.default_border.enabled"))
            {
                lines.add(ChatUtils.colorize(config.getString("scoreboard.default_border.value")));
            }
            lines.addAll(config.getStringList("scoreboard.default_lines"));

            if (config.getBoolean("scoreboard.default_border.enabled") && config.getBoolean("scoreboard.default_border.both"))
            {
                lines.add(ChatUtils.colorize(config.getString("scoreboard.default_border.value")));
            }
            board.updateLines(ChatUtils.colorizeList(lines));
        }
    }

    public boolean toggle(Player player)
    {
        if (scoreboards.containsKey(player.getUniqueId()))
        {
            remove(player);
            return false;
        } else
        {
            add(player);
            return true;
        }
    }

    public void add(Player player)
    {
        FastBoard board = new FastBoard(player);

        String title = !FactionsHelper.hasFaction(player)
                ? ChatUtils.colorize(config.getString("scoreboard.title.no_faction"))
                : ChatUtils.colorize(config.getString("scoreboard.title.faction")
                .replace("{FACTION}", FactionsHelper.getFaction(player).getTag()));

        board.updateTitle(title);
        scoreboards.put(player.getUniqueId(), board);
    }

    public void remove(Player player)
    {
        FastBoard board = scoreboards.remove(player.getUniqueId());

        if (board != null)
        {
            board.delete();
        }
    }

    /**
     * Sorts the list of faction members based on role.
     * This way, higher ranks will appear first on the scoreboard.
     * <p>
     * Priorities:
     * <p>
     * Recruit/Normal - 0
     * Officer - 1
     * Admin - 2
     * Co-Leader - 2
     *
     * @param faction The faction to sort FPlayers
     * @return A sorted list of FPlayers
     */
    private Map<FPlayer, Integer> getSortedMap(Faction faction)
    {
        Map<FPlayer, Integer> fPlayers = new HashMap<>();
        FactionsHelper.getOnlineFactionMembers(faction).forEach(fPlayer ->
        {
            switch (fPlayer.getRole())
            {
                case ADMIN:
                    fPlayers.put(fPlayer, 3);
                    break;
                case COLEADER:
                    fPlayers.put(fPlayer, 2);
                    break;
                case MODERATOR:
                    fPlayers.put(fPlayer, 1);
                    break;
                case NORMAL:
                case RECRUIT:
                    fPlayers.put(fPlayer, 0);
                    break;
            }
        });
        MapSorter<FPlayer, Integer> mapSorter = new MapSorter<>(fPlayers);
        return mapSorter.getSortedMap();
    }

    /**
     * Formulates the lines to add to the scoreboard.
     *
     * @param fPlayer the owner of the board
     * @return the lines
     */

    private List<String> getLines(FPlayer fPlayer)
    {
        List<String> lines = new ArrayList<>();

        if (config.getBoolean("scoreboard.border.enabled"))
        {
            lines.add(ChatUtils.colorize(config.getString("scoreboard.border.value")));
        }

        if (FactionsHelper.hasFaction(fPlayer.getPlayer()))
        {
            lines.addAll(FactionsMapHelper.getChunksAroundPlayer(fPlayer.getPlayer()));
            lines.add(" ");

            Map<FPlayer, Integer> myNewMap = getSortedMap(fPlayer.getFaction()).entrySet().stream()
                    .limit(7)
                    .collect(HashMap::new, (m, e) -> m.put(e.getKey(), e.getValue()), Map::putAll);

            myNewMap.forEach((player, priority) ->
            {
                String playerName = player.getName();

                if (priority == 3)
                {
                    lines.add(config.getString("scoreboard.prefixes.admin") + playerName);
                } else if (priority == 2)
                {
                    lines.add(config.getString("scoreboard.prefixes.co-leader") + playerName);
                } else if (priority == 1)
                {
                    lines.add(config.getString("scoreboard.prefixes.officer") + playerName);
                } else
                {
                    lines.add(config.getString("scoreboard.prefixes.recruit") + playerName);
                }
            });

            if (config.getBoolean("scoreboard.border.enabled") && config.getBoolean("scoreboard.border.both"))
            {
                lines.add(ChatUtils.colorize(config.getString("scoreboard.border.value")));
            }

            return ChatUtils.colorizeList(lines);
        }
        return ChatUtils.colorizeList(config.getStringList("scoreboard.default_lines"));
    }

}

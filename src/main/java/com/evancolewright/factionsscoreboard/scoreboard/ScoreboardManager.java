package com.evancolewright.factionsscoreboard.scoreboard;

import com.evancolewright.factionsscoreboard.FactionsScoreboard;
import com.evancolewright.factionsscoreboard.utils.ChatUtils;
import com.evancolewright.factionsscoreboard.utils.FactionsHelper;
import com.evancolewright.factionsscoreboard.utils.MapSorter;
import com.massivecraft.factions.*;
import fr.mrmicky.fastboard.FastBoard;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.*;

public final class ScoreboardManager
{
    private final FactionsScoreboard plugin;
    private final FileConfiguration config;

    private final Map<UUID, FastBoard> scoreboards = new HashMap<>();

    public ScoreboardManager(FactionsScoreboard plugin)
    {
        this.plugin = plugin;
        this.config = plugin.getConfig();

        if (config.getBoolean("scoreboard.enabled"))
            Bukkit.getScheduler().runTaskTimer(plugin, () -> scoreboards.values().forEach(this::updateScoreboard), 0, 20);
    }

    public void updateScoreboard(FastBoard board)
    {
        Player player = board.getPlayer();
        board.updateTitle(getTitle(player));

        if (FactionsHelper.hasFaction(player))
        {
            board.updateLines(ChatUtils.colorizeList(this.getLines(FPlayers.getInstance().getByPlayer(player))));
        } else
        {
            List<String> lines = new ArrayList<>();
            List<String> configLines = config.getStringList("scoreboard.default_lines");

            configLines.forEach(line ->
            {
                if (line.contains("{MAP}"))
                {
                    lines.addAll(getChunksAroundPlayer(player));
                } else
                {
                    lines.add(plugin.placeholderAPI ? PlaceholderAPI.setPlaceholders(player, line) : line);
                }
            });
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

    /**
     * Reloads all scoreboards and the
     * configuration file for easy in-game editing
     */
    public void reloadAll()
    {
        this.scoreboards.clear();
        plugin.reloadConfig();
        plugin.saveConfig();
        Bukkit.getOnlinePlayers().forEach(this::add);
    }

    public void add(Player player)
    {
        FastBoard board = new FastBoard(player);
        board.updateTitle(getTitle(player));
        scoreboards.put(player.getUniqueId(), board);
    }

    private String getTitle(Player player)
    {
        return !FactionsHelper.hasFaction(player)
                ? ChatUtils.colorize(config.getString("scoreboard.title.no_faction"))
                : ChatUtils.colorize(config.getString("scoreboard.title.faction")
                .replace("{FACTION}", FactionsHelper.getFaction(player).getTag()));
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
     * Formulates the lines to add to the scoreboard (for Faction players).
     *
     * @param fPlayer the owner of the board
     * @return the lines
     */

    private List<String> getLines(FPlayer fPlayer)
    {
        List<String> lines = new ArrayList<>();
        List<String> factionLines = config.getStringList("scoreboard.faction_lines");

        factionLines.forEach(line ->
        {
            if (line.contains("{MAP}"))
            {
                lines.addAll(getChunksAroundPlayer(fPlayer.getPlayer()));

            } else if (line.contains("{MEMBERS}"))
            {
                lines.addAll(getFactionMembers(fPlayer));
            } else
            {
                lines.add(plugin.placeholderAPI ? PlaceholderAPI.setPlaceholders(fPlayer.getPlayer(), line) : line);
            }
        });
        return ChatUtils.colorizeList(lines);

    }

    /**
     * Get the chunks around the player to draw on the map.
     * <p>
     * Please see: https://github.com/Techcable/FactionsUUID/blob/1.6.x/src/main/java/com/massivecraft/factions/zcore/persist/MemoryBoard.java
     *
     * @param player the player to draw the map
     * @return the lines of the scoreboard
     */
    private List<String> getChunksAroundPlayer(Player player)
    {
        final int height = config.getInt("scoreboard.map.size"), width = config.getInt("scoreboard.map.size");
        final List<String> lines = new ArrayList<>();

        FPlayer fPlayer = FPlayers.getInstance().getByPlayer(player);
        FLocation playerLocation = new FLocation(player.getLocation());

        int halfWidth = width / 2;
        int halfHeight = height / 2;
        FLocation topLeft = playerLocation.getRelative(-halfWidth, -halfHeight);

        for (int dz = 0; dz < height; dz++)
        {
            StringBuilder row = new StringBuilder();
            String chunkChar = config.getString("scoreboard.map.char");
            for (int dx = 0; dx < width; dx++)
            {
                if (dx == halfWidth && dz == halfHeight)
                {
                    row.append(config.getString("scoreboard.map.colors.you")).append(chunkChar);
                } else
                {
                    Faction fh = Board.getInstance().getFactionAt(topLeft.getRelative(dx, dz));
                    Faction pf = fPlayer.getFaction();

                    if (fh.equals(Factions.getInstance().getWilderness()))
                    {
                        row.append(config.getString("scoreboard.map.colors.wilderness")).append(chunkChar);
                        continue;
                    }

                    switch (fh.getRelationTo(pf))
                    {
                        case ALLY:
                            row.append(config.getString("scoreboard.map.colors.ally")).append(chunkChar);
                            break;
                        case ENEMY:
                            row.append(config.getString("scoreboard.map.colors.enemy")).append(chunkChar);
                            break;
                        case NEUTRAL:
                            row.append(config.getString("scoreboard.map.colors.neutral")).append(chunkChar);
                            break;
                        case MEMBER:
                            row.append(config.getString("scoreboard.map.colors.your_faction")).append(chunkChar);
                            break;
                    }
                }
            }
            String space = fPlayer.hasFaction() ? config.getString("scoreboard.map.center") : config.getString("scoreboard.map.default_center");
            String line = space + row.toString();
            lines.add(line);
        }
        return lines;
    }

    /**
     * Gets the sorted list of faction members and returns in.
     *
     * @param fPlayer the player that has the faction
     * @return the list of all members with their prefixes.
     */

    private List<String> getFactionMembers(FPlayer fPlayer)
    {
        List<String> lines = new ArrayList<>();

        getSortedMap(fPlayer.getFaction()).forEach((player, priority) ->
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

        return lines;
    }
}

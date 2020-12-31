package com.evancolewright.factionsscoreboard.utils;

import com.massivecraft.factions.*;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class FactionsMapHelper
{

    private enum RelationColor
    {
        YOU("&8"),
        YOUR_FACTION("&a"),
        WILDERNESS("&7"),
        ALLY("&d"),
        ENEMY("&4"),
        OTHER("&4");

        private String colorCode;

        RelationColor(String color)
        {
            this.colorCode = color;
        }
    }

    /**
     * Get the chunks around the player to draw on the map.
     * <p>
     * Please see: https://github.com/Techcable/FactionsUUID/blob/1.6.x/src/main/java/com/massivecraft/factions/zcore/persist/MemoryBoard.java
     *
     * @param player the player to draw the map
     * @return the lines of the scoreboard
     */
    public static List<String> getChunksAroundPlayer(Player player)
    {
        final int height = 7, width = 7;
        final List<String> lines = new ArrayList<>();

        FPlayer fPlayer = FPlayers.getInstance().getByPlayer(player);
        FLocation playerLocation = new FLocation(player.getLocation());

        int halfWidth = width / 2;
        int halfHeight = height / 2;
        FLocation topLeft = playerLocation.getRelative(-halfWidth, -halfHeight);

        for (int dz = 0; dz < height; dz++)
        {
            StringBuilder row = new StringBuilder();
            for (int dx = 0; dx < width; dx++)
            {
                if (dx == halfWidth && dz == halfHeight)
                {
                    row.append(RelationColor.YOU.colorCode + "▇");
                } else
                {
                    Faction fh = Board.getInstance().getFactionAt(topLeft.getRelative(dx, dz));
                    Faction pf = fPlayer.getFaction();

                    if (fh.equals(Factions.getInstance().getWilderness()))
                    {
                        row.append(RelationColor.WILDERNESS.colorCode).append("▇");
                        continue;
                    }

                    switch (fh.getRelationTo(pf))
                    {
                        case ALLY:
                            row.append(RelationColor.ALLY.colorCode).append("▇");
                            break;
                        case ENEMY:
                        case NEUTRAL:
                            row.append(RelationColor.ENEMY.colorCode).append("▇");
                            break;
                        case MEMBER:
                            row.append(RelationColor.YOUR_FACTION.colorCode).append("▇");
                            break;
                    }
                }
            }
            String line = "     " + row.toString();
            lines.add(line);
        }
        return lines;
    }
}

package com.evancolewright.factionsscoreboard.utils;

import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.Faction;
import org.bukkit.entity.Player;

import java.util.Set;

public class FactionsHelper
{
    public static boolean hasFaction(Player player)
    {
        FPlayer fPlayer = FPlayers.getInstance().getByPlayer(player);
        if (fPlayer != null)
        {
            return fPlayer.hasFaction();
        }
        return false;
    }

    public static Faction getFaction(Player player)
    {
        if (hasFaction(player))
        {
            FPlayer fPlayer = FPlayers.getInstance().getByPlayer(player);
            return fPlayer.getFaction();
        }
        return null;
    }

    public static Set<FPlayer> getOnlineFactionMembers(Faction faction)
    {
        return faction.getFPlayersWhereOnline(true);
    }

}

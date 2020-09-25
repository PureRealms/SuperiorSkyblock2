package com.bgsoftware.superiorskyblock.commands;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.utils.events.EventsCaller;
import com.bgsoftware.superiorskyblock.utils.islands.IslandPrivileges;
import com.bgsoftware.superiorskyblock.utils.islands.IslandUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class CmdCoop implements ISuperiorCommand {

    @Override
    public List<String> getAliases() {
        return Arrays.asList("coop", "trust");
    }

    @Override
    public String getPermission() {
        return "superior.island.coop";
    }

    @Override
    public String getUsage(java.util.Locale locale) {
        return "coop <" + Locale.COMMAND_ARGUMENT_PLAYER_NAME.getMessage(locale) + ">";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Locale.COMMAND_DESCRIPTION_COOP.getMessage(locale);
    }

    @Override
    public int getMinArgs() {
        return 2;
    }

    @Override
    public int getMaxArgs() {
        return 2;
    }

    @Override
    public boolean canBeExecutedByConsole() {
        return false;
    }

    @Override
    public void execute(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(sender);
        Island island = superiorPlayer.getIsland();

        if(island == null){
            Locale.INVALID_ISLAND.send(superiorPlayer);
            return;
        }

        if(!superiorPlayer.hasPermission(IslandPrivileges.COOP_MEMBER)){
            Locale.NO_COOP_PERMISSION.send(superiorPlayer, island.getRequiredPlayerRole(IslandPrivileges.COOP_MEMBER));
            return;
        }

        SuperiorPlayer targetPlayer = plugin.getPlayers().getSuperiorPlayer(args[1]);

        if(targetPlayer == null){
            Locale.INVALID_PLAYER.send(superiorPlayer, args[1]);
            return;
        }

        if(island.isMember(targetPlayer)){
            Locale.ALREADY_IN_ISLAND_OTHER.send(superiorPlayer);
            return;
        }

        if(island.isCoop(targetPlayer)){
            Locale.PLAYER_ALREADY_COOP.send(superiorPlayer);
            return;
        }

        if(island.isBanned(targetPlayer)){
            Locale.COOP_BANNED_PLAYER.send(superiorPlayer);
            return;
        }

        if(island.getCoopPlayers().size() >= island.getCoopLimit()){
            Locale.COOP_LIMIT_EXCEED.send(superiorPlayer);
            return;
        }

        if(!EventsCaller.callIslandCoopPlayerEvent(island, superiorPlayer, targetPlayer))
            return;

        island.addCoop(targetPlayer);

        IslandUtils.sendMessage(island, Locale.COOP_ANNOUNCEMENT, new ArrayList<>(), superiorPlayer.getName(), targetPlayer.getName());

        if(island.getName().isEmpty())
            Locale.JOINED_ISLAND_AS_COOP.send(targetPlayer, superiorPlayer.getName());
        else
            Locale.JOINED_ISLAND_AS_COOP_NAME.send(targetPlayer, island.getName());
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(sender);
        Island island = superiorPlayer.getIsland();

        if(args.length == 2 && island != null && superiorPlayer.hasPermission(IslandPrivileges.COOP_MEMBER)){
            List<String> list = new ArrayList<>();

            for(Player player : Bukkit.getOnlinePlayers()){
                SuperiorPlayer targetPlayer = plugin.getPlayers().getSuperiorPlayer(player);
                if(!island.isMember(targetPlayer) && !island.isBanned(targetPlayer) && !island.isCoop(targetPlayer) &&
                        player.getName().toLowerCase().contains(args[1].toLowerCase())){
                    list.add(player.getName());
                }
            }

            return list;
        }
        return new ArrayList<>();
    }
}

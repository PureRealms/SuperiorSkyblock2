package com.bgsoftware.superiorskyblock.commands;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.events.IslandUncoopPlayerEvent;
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

public final class CmdUncoop implements ISuperiorCommand {

    @Override
    public List<String> getAliases() {
        return Arrays.asList("uncoop", "untrust");
    }

    @Override
    public String getPermission() {
        return "superior.island.uncoop";
    }

    @Override
    public String getUsage(java.util.Locale locale) {
        return "uncoop <" + Locale.COMMAND_ARGUMENT_PLAYER_NAME.getMessage(locale) + ">";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Locale.COMMAND_DESCRIPTION_UNCOOP.getMessage(locale);
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

        if(!superiorPlayer.hasPermission(IslandPrivileges.UNCOOP_MEMBER)){
            Locale.NO_UNCOOP_PERMISSION.send(superiorPlayer, island.getRequiredPlayerRole(IslandPrivileges.UNCOOP_MEMBER));
            return;
        }

        SuperiorPlayer targetPlayer = plugin.getPlayers().getSuperiorPlayer(args[1]);

        if(targetPlayer == null){
            Locale.INVALID_PLAYER.send(superiorPlayer, args[1]);
            return;
        }

        if(!island.isCoop(targetPlayer)){
            Locale.PLAYER_NOT_COOP.send(superiorPlayer);
            return;
        }

        if(!EventsCaller.callIslandUncoopPlayerEvent(island, superiorPlayer, targetPlayer, IslandUncoopPlayerEvent.UncoopReason.PLAYER))
            return;

        island.removeCoop(targetPlayer);

        IslandUtils.sendMessage(island, Locale.UNCOOP_ANNOUNCEMENT, new ArrayList<>(), superiorPlayer.getName(), targetPlayer.getName());

        if(island.getName().isEmpty())
            Locale.LEFT_ISLAND_COOP.send(targetPlayer, superiorPlayer.getName());
        else
            Locale.LEFT_ISLAND_COOP_NAME.send(targetPlayer, island.getName());
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(sender);
        Island island = superiorPlayer.getIsland();

        if(args.length == 2 && island != null && superiorPlayer.hasPermission(IslandPrivileges.COOP_MEMBER)){
            List<String> list = new ArrayList<>();

            for(Player player : Bukkit.getOnlinePlayers()){
                SuperiorPlayer targetPlayer = plugin.getPlayers().getSuperiorPlayer(player);
                if(island.isCoop(targetPlayer) && player.getName().toLowerCase().contains(args[1].toLowerCase())){
                    list.add(player.getName());
                }
            }

            return list;
        }
        return new ArrayList<>();
    }
}

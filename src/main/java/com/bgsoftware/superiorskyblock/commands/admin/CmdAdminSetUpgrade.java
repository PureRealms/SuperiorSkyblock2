package com.bgsoftware.superiorskyblock.commands.admin;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.upgrades.Upgrade;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.wrappers.SSuperiorPlayer;
import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.commands.ISuperiorCommand;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public final class CmdAdminSetUpgrade implements ISuperiorCommand {

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("setupgrade");
    }

    @Override
    public String getPermission() {
        return "superior.admin.setupgrade";
    }

    @Override
    public String getUsage(java.util.Locale locale) {
        return "admin setupgrade <" +
                Locale.COMMAND_ARGUMENT_PLAYER_NAME.getMessage(locale) + "/" +
                Locale.COMMAND_ARGUMENT_ISLAND_NAME.getMessage(locale) + "> <" +
                Locale.COMMAND_ARGUMENT_UPGRADE_NAME + "> <" +
                Locale.COMMAND_ARGUMENT_LEVEL.getMessage(locale) + ">";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Locale.COMMAND_DESCRIPTION_ADMIN_SET_UPGRADE.getMessage(locale);
    }

    @Override
    public int getMinArgs() {
        return 5;
    }

    @Override
    public int getMaxArgs() {
        return 5;
    }

    @Override
    public boolean canBeExecutedByConsole() {
        return true;
    }

    @Override
    public void execute(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        SuperiorPlayer targetPlayer = SSuperiorPlayer.of(args[2]);
        Island island = targetPlayer == null ? plugin.getGrid().getIsland(args[2]) : targetPlayer.getIsland();

        if(island == null){
            if(args[2].equalsIgnoreCase(sender.getName()))
                Locale.INVALID_ISLAND.send(sender);
            else if(targetPlayer == null)
                Locale.INVALID_ISLAND_OTHER_NAME.send(sender, args[2]);
            else
                Locale.INVALID_ISLAND_OTHER.send(sender, targetPlayer.getName());
            return;
        }

        String upgradeName = args[3].toLowerCase();

        if(!plugin.getUpgrades().isUpgrade(upgradeName)){
            Locale.INVALID_UPGRADE.send(sender, args[3], getUpgradesString(plugin));
            return;
        }

        int level;

        try{
            level = Integer.parseInt(args[4]);
        }catch (IllegalArgumentException ex){
            Locale.INVALID_LEVEL.send(sender, args[4]);
            return;
        }

        Upgrade upgrade = plugin.getUpgrades().getUpgrade(upgradeName);

        int maxLevel = upgrade.getMaxUpgradeLevel();

        if(level > maxLevel){
            Locale.MAXIMUM_LEVEL.send(sender, maxLevel);
            return;
        }


        island.setUpgradeLevel(upgrade, level);

        if(targetPlayer == null)
            Locale.SET_UPGRADE_LEVEL_NAME.send(sender, upgradeName, island.getName());
        else
            Locale.SET_UPGRADE_LEVEL.send(sender, upgradeName, targetPlayer.getName());
    }



    @Override
    public List<String> tabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        List<String> list = new ArrayList<>();

        if(args.length == 3){
            for(Player player : Bukkit.getOnlinePlayers()){
                SuperiorPlayer onlinePlayer = SSuperiorPlayer.of(player);
                Island playerIsland = onlinePlayer.getIsland();
                if (playerIsland != null) {
                    if (player.getName().toLowerCase().startsWith(args[2].toLowerCase()))
                        list.add(player.getName());
                    if(!playerIsland.getName().isEmpty() && playerIsland.getName().toLowerCase().startsWith(args[2].toLowerCase()))
                        list.add(playerIsland.getName());
                }
            }
        }

        else if(args.length == 4){
            list.addAll(plugin.getUpgrades().getUpgrades().stream()
                    .filter(upgrade -> upgrade.getName().toLowerCase().startsWith(args[3].toLowerCase()))
                    .map(Upgrade::getName)
                    .collect(Collectors.toList()));
        }

        return list;
    }

    private String getUpgradesString(SuperiorSkyblockPlugin plugin){
        StringBuilder stringBuilder = new StringBuilder();

        for(Upgrade upgrade : plugin.getUpgrades().getUpgrades())
            stringBuilder.append(", ").append(upgrade.getName());

        return stringBuilder.toString().substring(2);
    }

}
package com.bgsoftware.superiorskyblock.menu;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.config.CommentedConfiguration;
import com.bgsoftware.superiorskyblock.utils.FileUtils;
import com.bgsoftware.superiorskyblock.utils.StringUtils;
import com.bgsoftware.superiorskyblock.utils.items.HeadUtils;
import com.bgsoftware.superiorskyblock.utils.items.ItemBuilder;
import com.bgsoftware.superiorskyblock.utils.legacy.Materials;
import com.bgsoftware.superiorskyblock.utils.registry.Registry;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class MenuCounts extends PagedSuperiorMenu<Pair<Key, Integer>> {

    private static Map<String, String> blocksToItems = new HashMap<>();

    static {
        blocksToItems.put("SIGN_POST", "SIGN");
        blocksToItems.put("SUGAR_CANE_BLOCK", "SUGAR_CANE");
    }

    private Island island;

    private MenuCounts(SuperiorPlayer superiorPlayer, Island island){
        super("menuCounts", superiorPlayer);
        this.island = island;
    }

    @Override
    public void onPlayerClick(InventoryClickEvent event, Pair<Key, Integer> block) {
    }

    @Override
    protected ItemStack getObjectItem(ItemStack clickedItem, Pair<Key, Integer> block) {
        Key blockKey = block.getKey();
        int amount = block.getValue();

        String[] keySections = blockKey.toString().split(":");

        if(blocksToItems.containsKey(keySections[0]))
            keySections[0] = blocksToItems.get(keySections[0]);

        Material blockMaterial;

        try{
            blockMaterial = Material.valueOf(keySections[0]);
        }catch (Exception ex){
            blockMaterial = Material.BEDROCK;
        }

        ItemMeta currentMeta = clickedItem.getItemMeta();
        ItemBuilder itemBuilder;

        String materialName;

        if(blockMaterial == Materials.SPAWNER.toBukkitType() && keySections.length > 1){
            itemBuilder = new ItemBuilder(HeadUtils.getPlayerHead(
                    new ItemStack(Materials.PLAYER_HEAD.toBukkitType()),
                    HeadUtils.getTexture(keySections[1])));
            materialName = keySections[1] + "_SPAWNER";
        }

        else {
            itemBuilder = new ItemBuilder(blockMaterial);
            materialName = blockMaterial.name();
        }

        ItemStack itemStack = itemBuilder
                .withName(currentMeta.hasDisplayName() ? currentMeta.getDisplayName() : "")
                .withLore(currentMeta.hasLore() ? currentMeta.getLore() : new ArrayList<>())
                .replaceAll("{0}", StringUtils.format(materialName))
                .replaceAll("{1}", amount + "")
                .build(superiorPlayer);

        itemStack.setAmount(Math.max(1, Math.min(64, amount)));

        return itemStack;
    }

    @Override
    protected List<Pair<Key, Integer>> requestObjects() {
        return island.getBlockCounts().entrySet().stream().sorted((o1, o2) -> {
            Material firstMaterial = Material.valueOf(o1.getKey().toString().split(":")[0]);
            Material secondMaterial = Material.valueOf(o2.getKey().toString().split(":")[0]);
            return plugin.getNMSBlocks().compareMaterials(firstMaterial, secondMaterial);
        }).map(Pair::new).collect(Collectors.toList());
    }

    public static void init(){
        MenuCounts menuCounts = new MenuCounts(null, null);

        File file = new File(plugin.getDataFolder(), "menus/counts.yml");

        if(!file.exists())
            FileUtils.saveResource("menus/counts.yml");

        CommentedConfiguration cfg = CommentedConfiguration.loadConfiguration(file);

        Registry<Character, List<Integer>> charSlots = FileUtils.loadGUI(menuCounts, "counts.yml", cfg);

        menuCounts.setPreviousSlot(getSlots(cfg, "previous-page", charSlots));
        menuCounts.setCurrentSlot(getSlots(cfg, "current-page", charSlots));
        menuCounts.setNextSlot(getSlots(cfg, "next-page", charSlots));
        menuCounts.setSlots(getSlots(cfg, "slots", charSlots));

        charSlots.delete();

        menuCounts.markCompleted();
    }

    public static void openInventory(SuperiorPlayer superiorPlayer, SuperiorMenu previousMenu, Island island){
        MenuCounts menuCounts = new MenuCounts(superiorPlayer, island);
        menuCounts.open(previousMenu);
    }

    public static void refreshMenus(){
        refreshMenus(MenuCounts.class);
    }

}

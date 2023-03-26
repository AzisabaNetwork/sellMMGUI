package com.github.sirokuri_.sellmmgui.Commands;

import com.github.mori01231.lifecore.util.ItemUtil;
import com.github.sirokuri_.sellmmgui.SellMMGUI;
import com.github.sirokuri_.sellmmgui.inventoryHolder.MyHolder;
import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.util.jnbt.CompoundTag;
import org.bukkit.*;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class command implements CommandExecutor {
    private final SellMMGUI plugin;

    public command(SellMMGUI sellMMGUI){
        this.plugin = sellMMGUI;
    }

    @Override
    public boolean onCommand(CommandSender sender, org.bukkit.command.Command cmd, String label, String[] args) {
        Player p = (Player) sender;
        if (cmd.getName().equalsIgnoreCase("sellmmgui")) {
            if (args.length <= 0) {
                return true;
            }
            if (args[0].equalsIgnoreCase("reload")) {
                //OP以外起動しないように設定
                if (sender.hasPermission("SellMMGUICommand.permission.Admin")) {
                    plugin.reload();
                    p.sendMessage("configリロードしました");
                }
                return true;
            }
            if (args[0].equalsIgnoreCase("check")) {
                //OP以外起動しないように設定
                if (sender.hasPermission("SellMMGUICommand.permission.Admin")) {
                    ItemStack itemStack = p.getInventory().getItemInMainHand();
                    String mythicType = ItemUtil.getMythicType(itemStack);
                    if (mythicType == null) return true;
                    p.sendMessage(mythicType);
                }
                return true;
            }
        }

        if (cmd.getName().equalsIgnoreCase("smg")) {
            Inventory mirror = Bukkit.createInventory(new MyHolder("holder1"), 9, "§cSELLMMITEM MENU");
            ItemStack menu1 = new ItemStack(Material.GREEN_STAINED_GLASS_PANE);
            ItemStack menu2 = new ItemStack(Material.RED_STAINED_GLASS_PANE);
            ItemStack menu3 = new ItemStack(Material.YELLOW_STAINED_GLASS_PANE);
            ItemStack menu4 = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
            ItemMeta itemMeta1 = menu1.getItemMeta();
            ItemMeta itemMeta2 = menu2.getItemMeta();
            ItemMeta itemMeta3 = menu3.getItemMeta();
            ItemMeta itemMeta4 = menu4.getItemMeta();
            if (itemMeta1 == null || itemMeta2 == null || itemMeta3 == null || itemMeta4 == null) return true;
            itemMeta1.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&aSHOPを開く"));
            itemMeta2.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&cSHOPを閉じる"));
            itemMeta3.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&eSHOP注意点"));
            itemMeta4.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&8売却可能アイテム一覧を表示する"));
            List<String> lore3 = new ArrayList<String>();
            lore3.add(ChatColor.translateAlternateColorCodes('&', "&dSHOPのインベントリに"));
            lore3.add(ChatColor.translateAlternateColorCodes('&', "&d指定アイテム以外を入れてしまうと"));
            lore3.add(ChatColor.translateAlternateColorCodes('&', "&d一円にもならずアイテムが消えます"));
            lore3.add(ChatColor.translateAlternateColorCodes('&', "&d消えたアイテムに関しては&c補填対象外&dです"));
            itemMeta3.setLore(lore3);
            List<String> lore4 = new ArrayList<String>();
            List<String> itemDisplay = new ArrayList<String>();
            for (String key : plugin.getConfig().getConfigurationSection("mmitem").getKeys(false)) {
                String ItemDisplayName = plugin.getConfig().getString("mmitem." + key + ".itemdisplay");
                itemDisplay.add(ItemDisplayName);
            }
            lore4.add(ChatColor.translateAlternateColorCodes('&', "&d売却可能アイテム一覧は"));
            lore4.add(ChatColor.translateAlternateColorCodes('&', "クリックで閲覧できます"));
            itemMeta4.setLore(lore4);
            menu1.setItemMeta(itemMeta1);
            menu2.setItemMeta(itemMeta2);
            menu3.setItemMeta(itemMeta3);
            menu4.setItemMeta(itemMeta4);
            mirror.setItem(0, menu1);
            mirror.setItem(8, menu2);
            mirror.setItem(5, menu3);
            mirror.setItem(3, menu4);
            Location loc = p.getLocation();
            p.playSound(loc, Sound.BLOCK_CHEST_OPEN, 2, 1);
            p.openInventory(mirror);
            return true;
        }

        return true;
    }
}

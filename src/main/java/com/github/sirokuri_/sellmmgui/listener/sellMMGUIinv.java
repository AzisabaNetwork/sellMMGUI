package com.github.sirokuri_.sellmmgui.listener;

import com.github.mori01231.lifecore.util.ItemUtil;
import com.github.sirokuri_.sellmmgui.SellMMGUI;
import com.github.sirokuri_.sellmmgui.inventoryHolder.MyHolder;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class sellMMGUIinv implements Listener {
    private final SellMMGUI plugin;

    public sellMMGUIinv(SellMMGUI sellMMGUI){
        this.plugin = sellMMGUI;
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        Player player = (Player) e.getView().getPlayer();
        Inventory inventory = e.getClickedInventory();
        ItemStack slot = e.getCurrentItem();
        if (slot == null) return;
        if (inventory == null) return;
        InventoryHolder inventoryHolder = inventory.getHolder();
        if(!(inventoryHolder instanceof MyHolder)) return;
        MyHolder holder = (MyHolder) inventoryHolder;
        if(holder.tags.get(0).equals("holder1")) {
            if (slot.getType() == Material.GREEN_STAINED_GLASS_PANE) {
                if (slot.getItemMeta().getDisplayName().equals(ChatColor.translateAlternateColorCodes('&', "&aSHOPを開く"))) {
                    Inventory mirror = Bukkit.createInventory(new MyHolder("holder2"), 54, "§cSELLMMITEM SHOP");
                    Location loc = player.getLocation();
                    player.playSound(loc, Sound.BLOCK_CHEST_OPEN, 2, 1);
                    player.openInventory(mirror);
                }
            } else if (slot.getType() == Material.RED_STAINED_GLASS_PANE) {
                if (slot.getItemMeta().getDisplayName().equals(ChatColor.translateAlternateColorCodes('&', "&cSHOPを閉じる"))) {
                    player.closeInventory();
                    Location loc = player.getLocation();
                    player.playSound(loc, Sound.BLOCK_CHEST_CLOSE, 2, 1);
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cSELLMMSHOP&fを閉じました"));
                }
            } else if (slot.getType() == Material.BLACK_STAINED_GLASS_PANE) {
                if (slot.getItemMeta().getDisplayName().equals(ChatColor.translateAlternateColorCodes('&', "&8売却可能アイテム一覧を表示する"))) {
                    player.closeInventory();
                    Location loc = player.getLocation();
                    player.playSound(loc, Sound.ENTITY_PLAYER_LEVELUP, 2, 1);
                    String sellItemDisplay = plugin.getConfig().getString("SellItemDisplay");
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', "" + sellItemDisplay));
                }
            } else if (slot.getType() == Material.YELLOW_STAINED_GLASS_PANE) {
                if (slot.getItemMeta().getDisplayName().equals(ChatColor.translateAlternateColorCodes('&', "&eSHOP注意点"))) {
                    Location loc = player.getLocation();
                    player.playSound(loc, Sound.ENTITY_VILLAGER_NO, 2, 1);
                    e.setCancelled(true);
                }
            } else {
                return;
            }
        }
    }

    @EventHandler
    private void inventoryCloseEvent(InventoryCloseEvent e) {
        Player player = (Player) e.getPlayer();
        Inventory inventory = e.getInventory();
        InventoryHolder inventoryHolder = e.getInventory().getHolder();
        if(inventoryHolder == null) return;
        if(!(inventoryHolder instanceof MyHolder)) return;
        MyHolder holder = (MyHolder) inventoryHolder;
        if(holder.tags.get(0).equals("holder1")){
            ItemStack[] contents = inventory.getContents();
            for (int i = 0; i < 9; i++) {
                ItemStack content = contents[i];
                if (content == null) {
                    continue;
                }
                if(!(content.getType() == Material.GREEN_STAINED_GLASS_PANE || content.getType() == Material.RED_STAINED_GLASS_PANE || content.getType() == Material.BLACK_STAINED_GLASS_PANE || content.getType() == Material.YELLOW_STAINED_GLASS_PANE)) {
                    player.getInventory().addItem(content);
                }
            }
        }
        if(holder.tags.get(0).equals("holder2")){
            ItemStack[] contents = inventory.getContents();
            double totalMoney = 0;
            for (String key : plugin.config().getConfigurationSection("mmitem").getKeys(false)) {
                int moneyamount = plugin.config().getInt("mmitem." + key + ".sellprice");
                String ItemDisplayName = plugin.config().getString("mmitem." + key + ".itemdisplay");
                for (int i = 0; i < 54; i++) {
                    ItemStack content = contents[i];
                    if (content == null) {
                        continue;
                    }
                    int amount = content.getAmount();
                    int money = amount * moneyamount;
                    ItemMeta itemMeta = content.getItemMeta();
                    if (itemMeta == null){
                        continue;
                    }
                    if (content.getItemMeta().getDisplayName().equals(ChatColor.translateAlternateColorCodes('&', "" + ItemDisplayName))) {
                        totalMoney += money;
                    }
                }
            }

            /*ItemStack[] contents = inventory.getContents();
            for (String key : plugin.config().getConfigurationSection("mmitem").getKeys(false)) {
                int moneyamount = plugin.config().getInt("mmitem." + key + ".sellprice");
                for (int i = 0; i < 54; i++) {
                    ItemStack content = contents[i];
                    if (content == null || !content.hasItemMeta()) {
                        continue;
                    }
                    String mythicType = ItemUtil.getMythicType(content);
                    int amount = content.getAmount();
                    int money = amount * moneyamount;
                    if (key.equalsIgnoreCase(mythicType)) {
                        totalMoney += money;
                    }
                }
            }*/

            Location loc = player.getLocation();
            player.playSound(loc, Sound.ENTITY_PLAYER_LEVELUP, 2, 1);
            EconomyResponse r = plugin.econ.depositPlayer(player, totalMoney);
            if (r.transactionSuccess()) {
                player.sendMessage(String.format("[smg]\n\n今回の売却額 : %s\n現在の所持金 : %s", plugin.econ.format(r.amount), plugin.econ.format(r.balance)));
                Bukkit.getLogger().info(String.format("[smg] " +player.getDisplayName() + "の売却額 : %s 現在の所持金 : %s", plugin.econ.format(r.amount), plugin.econ.format(r.balance)));
            } else {
                player.sendMessage(String.format("An error occured: %s", r.errorMessage));
            }
        }else {
            return;
        }
    }
}

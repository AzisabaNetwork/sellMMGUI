package com.github.sirokuri_.sellmmgui;

import com.github.sirokuri_.sellmmgui.Commands.command;
import com.github.sirokuri_.sellmmgui.listener.sellMMGUIinv;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;
import java.util.Random;
import java.util.logging.Logger;

public final class SellMMGUI extends JavaPlugin implements Listener {

    public static final Logger log = Logger.getLogger("Minecraft");
    public static Economy econ = null;
    private static final Permission perms = null;
    private static final Chat chat = null;

    @Override
    public void onEnable() {
        // Plugin startup logic
        if (!setupEconomy() ) {
            log.severe(String.format("[%s] - Disabled due to no Vault dependency found!", getDescription().getName()));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        getCommand("sellmmgui").setExecutor(new command(this));
        getCommand("smg").setExecutor(new command(this));
        Bukkit.getPluginManager().registerEvents(new sellMMGUIinv(this), this);
        saveDefaultConfig();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return true;
    }

    @EventHandler
    public void onBlockbreak(BlockBreakEvent e) {
        Player player = e.getPlayer();
        Random random = new Random();
        String world = player.getWorld().getName();
        int num = random.nextInt(30);
        if(world.equals("resource")){
            if (e.getBlock().getType() == Material.OAK_LEAVES) {
                if (num <= 2) {
                    if ((Objects.requireNonNull(player.getInventory().getItemInMainHand().getItemMeta())).getDisplayName().equals(ChatColor.translateAlternateColorCodes('&', "&cトマト採取剣"))) {
                        int emptySlot = player.getInventory().firstEmpty();
                        if (emptySlot == -1) return;
                        Location loc = player.getLocation();
                        player.playSound(loc, Sound.BLOCK_BELL_USE, 2, 1);
                        getServer().dispatchCommand(getServer().getConsoleSender(), "mm i give " + player.getName() + " tomato 2");
                    }
                }
            }
        }
    }

    private FileConfiguration config = null;

    public FileConfiguration config(){
        load();
        return config;
    }

    public void load() {
        saveDefaultConfig();
        if (config != null) {
            reload();
        }
        config = getConfig();
    }

    public void reload() {
        reloadConfig();
    }
}

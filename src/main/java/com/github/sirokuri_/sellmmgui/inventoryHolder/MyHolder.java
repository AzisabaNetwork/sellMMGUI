package com.github.sirokuri_.sellmmgui.inventoryHolder;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MyHolder implements InventoryHolder {
    public List<String> tags = new ArrayList<>();

    public MyHolder(String... tags) {
        this.tags.addAll(Arrays.asList(tags));
    }

    public List<String> getTags(){
        return tags;
    }

    @Override
    public Inventory getInventory() {
        return null;
    }
}

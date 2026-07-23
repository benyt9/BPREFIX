package b.bplugins.prefixplugin;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

public class PrefixMenuHolder implements InventoryHolder {

    private Inventory inventory;
    private int page;

    public PrefixMenuHolder(int page) {
        this.page = page;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    public int getPage() {
        return page;
    }
}
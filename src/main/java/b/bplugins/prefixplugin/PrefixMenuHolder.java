package b.bplugins.prefixplugin;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

/**
 * Marker-Holder für das Prefix-Menü.
 * <p>
 * Vorher wurde im MenuListener geprüft, ob event.getView().getTitle() exakt dem
 * per MiniMessage/Legacy geparsten Titel-String entspricht. Das ist fehleranfällig
 * (z.B. bei Formatierungs-Unterschieden zwischen Component und Legacy-String) und war
 * vermutlich der Grund, warum Klicks im Menü teilweise nicht erkannt wurden.
 * <p>
 * Mit einem eigenen InventoryHolder erkennt der Listener das Menü zuverlässig über
 * event.getInventory().getHolder() instanceof PrefixMenuHolder.
 */
public class PrefixMenuHolder implements InventoryHolder {

    private Inventory inventory;

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }
}
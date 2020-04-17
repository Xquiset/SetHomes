package com.samleighton.xquiset.sethomes.eventListeners;

import com.samleighton.xquiset.sethomes.SetHomes;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class EventListener implements Listener {
    //Register the event listener to begin listening
    public EventListener(SetHomes plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerInteractBlock(PlayerInteractEvent event) {
        //Get the player who triggered the event
        Player p = event.getPlayer();
        //Get the item they're holding
        ItemStack item = p.getInventory().getItemInMainHand();
        //Get the items meta
        ItemMeta itemMeta = item.getItemMeta();

        //If the item is the rod that we created strike lighting bolt at the location the player is looking on click
        if (itemMeta != null) {
            if (itemMeta.getLocalizedName().equalsIgnoreCase("almighty")) {
                p.getWorld().strikeLightning(p.getTargetBlock(null, 200).getLocation());
            }
        }

    }
}

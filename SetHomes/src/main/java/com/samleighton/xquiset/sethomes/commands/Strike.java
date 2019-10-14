package com.samleighton.xquiset.sethomes.commands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import com.samleighton.xquiset.sethomes.SetHomes;
import com.samleighton.xquiset.sethomes.utils.ChatUtils;

public class Strike implements CommandExecutor{
	private final SetHomes setHomes;
	
	public Strike(SetHomes plugin) {
		this.setHomes = plugin;
	}

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		//Make sure the sender of the command is a player
		if (!(sender instanceof Player)) {
			//Sends message to sender of command that they're not a player
			sender.sendMessage(ChatColor.DARK_RED + "This command is for players only!");
			return false;
		}
		
		if(cmd.getName().equalsIgnoreCase("strike")) {
			Player p = (Player) sender;
			if(p.hasPermission("homes.strike")) {
				PlayerInventory pInvetory = p.getInventory();
				ItemStack fishingRod = new ItemStack(Material.FISHING_ROD, 1);
				ItemMeta rodMeta = fishingRod.getItemMeta();
				List<String> rodLore = new ArrayList<String>();
				
				rodLore.add(ChatColor.DARK_RED + "The power of the almighty is in your hands now!");
				rodMeta.setLore(rodLore);
				rodMeta.setDisplayName(ChatColor.BLUE + "The Almighty!");
				rodMeta.setLocalizedName("almighty");
				
				fishingRod.setItemMeta(rodMeta);
				
				pInvetory.addItem(fishingRod);
				return true;
			} else {
				ChatUtils.permissionError(p);
				return true;
			}
		}
		
		return false;
	}

}

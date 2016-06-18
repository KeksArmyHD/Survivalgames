package de.pt400c.survivalgames;

import java.util.ArrayList;
import java.util.HashMap;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_9_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_9_R1.util.CraftMagicNumbers;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import net.minecraft.server.v1_9_R1.BlockPosition;

public class SurvivalGames extends JavaPlugin implements Listener {
	private static SurvivalGames plugin;
	ArrayList<Location> blocks = new ArrayList<>();
	/*Aufgelistete positionen der Kisten, erstellt durch einmaliges Rechtsklicken*/
	HashMap<Location, Inventory> inventory = new HashMap<>();
	/*Die Inventare, aufgespalten in die Locations*/
	HashMap<String, Location> openChest = new HashMap<>();
	/*Speichert, welcher Spieler gerade an welchem Ort eine Kiste öffnet*/

	public static SurvivalGames getInstance() {
		return plugin;
	}

	@Override
	public void onEnable() {
		
		Bukkit.getPluginManager().registerEvents(this, this);
		
	}

	@Override
	public void onDisable() {
		/*!!WICHTIG!!*/
		/*Es muss noch umgesetzt werden, dass die Inventare upgedatet werden! Dass, damit auch mehrere Spieler darauf zugreifen können*/
	}

	@EventHandler
	public void openEvent(PlayerInteractEvent e) {

		Player p = e.getPlayer();
		Block b = e.getClickedBlock();
		Action a = e.getAction();
		if(a.equals(Action.RIGHT_CLICK_BLOCK) && b.getType().equals(Material.CHEST)){
			openChest.put(p.getName(), b.getLocation());
			if(blocks.contains(b.getLocation())){
				/*Der Zugriff, nach dem erstellen(Füllen) der Truhe*/
				Inventory inv = inventory.get(b.getLocation());
				p.openInventory(inv);
			}else{
				/*Hier wird die Truhe gefüllt*/
				blocks.add(b.getLocation());
				Inventory inv = p.getServer().createInventory(null, 27,"§8Truhe");
				inv.setItem(0, new ItemStack(Material.DIAMOND));
			    p.openInventory(inv);
				inventory.put(b.getLocation(), inv);
			}
			e.setCancelled(true);
			/*Der "öffnen" Sound wird abgespielt und die Truhe wird per Packets geöffnet*/
			((CraftWorld) b.getWorld()).getHandle().playBlockAction(
									new BlockPosition(b.getX(),  b.getY(),  b.getZ()),
									CraftMagicNumbers.getBlock(b), 1, 1);
			
		}
		
	}

	@SuppressWarnings("unused")
	@EventHandler
	public void invCloseEvent(InventoryCloseEvent e) {
		
		Player p = (Player) e.getPlayer();
		if(openChest.containsKey(p.getName())){
		Inventory inv = e.getInventory();
		Location loc = openChest.get(p.getName());
		Location laci = new Location(Bukkit.getWorld("world"), loc.getX()+0.5, loc.getY()+1, loc.getZ()+0.5);
		Block b = Bukkit.getWorld("world").getBlockAt(loc);
		/*Die Kiste wird per Packets geschlossen*/
		 ((CraftWorld) b.getWorld()).getHandle().playBlockAction(
					new BlockPosition(b.getX(),  b.getY(),  b.getZ()),
					CraftMagicNumbers.getBlock(b), 1, 0);
		openChest.remove(p.getName());
        inventory.put(loc, inv);
                           /*!!!!!Das da unten würde den Inhalt des Inventars droppen!!!!!*/
       /* ItemStack[] gi = inv.getContents();
        for(ItemStack f : gi){
        	if(f != null){
        	Bukkit.getWorld("world").dropItemNaturally(laci, f);
        	}
        }*/
		}
	}
	
	@SuppressWarnings("unused")
	@EventHandler
	public void breakEvent(BlockBreakEvent e) {
		Player p = (Player) e.getPlayer();
		Location loc = e.getBlock().getLocation();
		/*Die Methode droppt nur die Items des Invs, bei zerstörung durch EINEN SPIELER! Sonst nicht!*/
		Location laci = new Location(Bukkit.getWorld("world"), loc.getX()+0.5, loc.getY()+1, loc.getZ()+0.5);
		Inventory inv = inventory.get(loc);
		Block b = e.getBlock();
		if(b.getType().equals(Material.CHEST)){
			if(blocks.contains(b.getLocation())){
				blocks.remove(b.getLocation());
				inventory.remove(b.getLocation());
				ItemStack[] gi = inv.getContents();
		        for(ItemStack f : gi){
		        	if(f != null){
		        	Bukkit.getWorld("world").dropItemNaturally(laci, f);
		        	}
		        }
			}
		}
		
	}
	
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

		return true;

	}

}

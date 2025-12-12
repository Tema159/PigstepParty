package me.tema159.pigstepparty;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Hopper;
import org.bukkit.block.Jukebox;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.Piglin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class Events implements Listener {

    @EventHandler
    void onInteract(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK || e.getClickedBlock() == null
                || e.getClickedBlock().getType() != Material.JUKEBOX)
            return;

        Jukebox box = (Jukebox) e.getClickedBlock().getState();
        Location loc = box.getLocation();
        String song = e.getItem() == null ? null : Main.getSong(e.getItem());

        if (box.isPlaying())
            Main.stop(loc);
        else if (song != null)
            Main.start(loc, song, e.getPlayer());
    }

    @EventHandler
    void onDispense(BlockDispenseEvent e) {
        Block block = e.getBlock();
        String song = Main.getSong(e.getItem());

        if (block.getType() != Material.DISPENSER  || song == null)
            return;

        Directional directional = (Directional) block.getBlockData();
        Block target = block.getRelative(directional.getFacing());
        Jukebox box = (Jukebox) target.getState();

        if (target.getType() == Material.JUKEBOX && !box.isPlaying())
            Bukkit.getScheduler().runTaskLaterAsynchronously(Main.getPlugin(), () -> {
                if (box.isPlaced() && box.isPlaying())
                    Main.start(target.getLocation(), song);
            }, 1);
    }

    @EventHandler
    void onBlockBreak(BlockBreakEvent e) {
        if (e.getBlock().getType() == Material.JUKEBOX)
            Main.stop(e.getBlock().getLocation());
    }

    @EventHandler
    void onDamage(EntityDamageEvent e) {
        if (e.getEntity() instanceof Piglin p && Main.removedFromMap(p))
            Main.setCooldown(p);
    }

    @EventHandler
    void onTarget(EntityTargetLivingEntityEvent e) {
        if (e.getEntity() instanceof Piglin p && Main.containsPiglin(p))
            e.setCancelled(true);
    }

    @EventHandler
    void onItemMove(InventoryMoveItemEvent e) {
        if (!(e.getInitiator().getHolder() instanceof Hopper))
            return;

        String song = Main.getSong(e.getItem());
        if (song == null)
            return;

        // (Jukebox -> Hopper)

        if (e.getSource().getHolder() instanceof Jukebox box) {
            Main.stop(box.getLocation());
            return;
        }

        // (Hopper -> Jukebox)

        if (e.getDestination().getHolder() instanceof Jukebox box)
            Main.start(box.getLocation(), song);
    }
}

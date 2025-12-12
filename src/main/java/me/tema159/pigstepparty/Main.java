package me.tema159.pigstepparty;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.JukeboxPlayable;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Piglin;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

public final class Main extends JavaPlugin {
    private static final Map<Location, ArrayList<Piglin>> jukeboxMonitor = new HashMap<>();
    private static final Map<Piglin, Instant> cooldownMap = new HashMap<>();

    @SuppressWarnings("deprecation")
    private static final boolean equalsOrMoreV1_21_3 = Bukkit.getUnsafe().getProtocolVersion() >= 768;
    private static Plugin plugin;

    private static NamespacedKey key;
    private static final String criteria = "jukebox_dance";
    private static Advancement advancement;

    @Override
    public void onEnable() {
        plugin = this;
        new Config().setup();
        getServer().getPluginManager().registerEvents(new Events(), this);

        key = new NamespacedKey(this, criteria);
        advancement = Achievement.registerAdvancement(false);

        Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> {
            for (Map.Entry<Location, ArrayList<Piglin>> entry : jukeboxMonitor.entrySet()) {
                Location loc = entry.getKey();

                if (jukeboxMonitor.get(loc).isEmpty())
                    jukeboxMonitor.remove(loc);

                entry.getValue().removeIf(p -> {
                    boolean remove = p.getLocation().distance(loc) > (int) Config.getValue("distance") && notMultipleLinked(p);
                    if (remove) p.setDancing(false);
                    return remove;
                });
            }
        }, 100, 100);
    }

    public static void start(Location loc, String song, Player... initiator) {
        ArrayList<Piglin> piglins = new ArrayList<>();
        int dur = DiscDuration.get(song);

        for (Entity ent : loc.getNearbyLivingEntities((int) Config.getValue("distance"))) {
            if (!(ent instanceof Piglin p) || hasCooldown(p))
                continue;

            p.setDancing(dur);
            piglins.add(p);
        }

        if (!piglins.isEmpty()) {
            jukeboxMonitor.put(loc, piglins);
            if (initiator.length == 0 || advancement == null)
                return;

            AdvancementProgress progress = initiator[0].getAdvancementProgress(advancement);
            if (!progress.isDone())
                progress.awardCriteria(criteria);
        }
    }

    public static void stop(Location loc) {
        if (!jukeboxMonitor.containsKey(loc))
            return;

        for (Piglin p : jukeboxMonitor.get(loc))
            if (notMultipleLinked(p))
                p.setDancing(false);

        jukeboxMonitor.remove(loc);
    }

    @SuppressWarnings("UnstableApiUsage")
    public static String getSong(ItemStack item) {
        String mat = item.getType().name().startsWith("MUSIC_DISC") ? item.getType().name() : null;

        if (mat == null && equalsOrMoreV1_21_3) {
            JukeboxPlayable comp = item.getData(DataComponentTypes.JUKEBOX_PLAYABLE);
            if (comp != null)
                mat = comp.jukeboxSong().getKey().getKey();
        }

        String finalMat = mat;
        if (mat != null && ((List<?>) Config.getValue("discs")).stream().noneMatch(d -> {
            String disc = String.valueOf(d);
            return disc.equalsIgnoreCase("ALL") || disc.equals(finalMat);
        })) mat = null;

        return mat;
    }

    public static boolean removedFromMap(Piglin p) {
        boolean contains = containsPiglin(p);
        if (contains) jukeboxMonitor.values().forEach(list -> list.remove(p));
        return contains;
    }

    public static boolean containsPiglin(Piglin p) {
        return jukeboxMonitor.values().stream().anyMatch(list -> list.contains(p));
    }

    public static boolean notMultipleLinked(Piglin p) {
        return jukeboxMonitor.values().stream().flatMap(List::stream).filter(p1 -> p1 == p).count() == 1;
    }

    public static void setCooldown(Piglin p) {
        cooldownMap.put(p, Instant.now().plus(
                Duration.ofSeconds((int) Config.getValue("dance-cooldown"))));
    }

    public static boolean hasCooldown(Piglin p) {
        Instant cooldown = cooldownMap.get(p);
        return cooldown != null && Instant.now().isBefore(cooldown);
    }

    public static Plugin getPlugin() {
        return plugin;
    }

    public static NamespacedKey getKey() {
        return key;
    }
}

package me.tema159.pigstepparty;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

import java.util.*;

public class Config {
    private final Plugin plugin = Main.getPlugin();

    public void setup() {
        plugin.saveDefaultConfig();
        FileConfiguration config = plugin.getConfig();

        config.options().copyDefaults(true);
        Objects.requireNonNull(config.getDefaults());

        for (String path : Arrays.asList("distance", "dance-cooldown"))
            if (!(config.get(path) instanceof Integer i) || i < 1)
                config.set(path, config.getDefaults().get(path));

        if (!(config.get("advancement.enabled") instanceof Boolean))
            config.set("enable-advancement", config.getDefaults().get("enable-advancement"));

        Object rawDiscs = config.get("discs");
        List<?> defaultDiscs = (List<?>) config.getDefaults().get("discs");

        if (!(rawDiscs instanceof List<?> raw))
            config.set("discs", defaultDiscs);
        else {
            List<String> discs = new ArrayList<>();
            for (Object obj : raw) {
                String song = String.valueOf(obj);
                if (DiscDuration.contains(song))
                    discs.add(song);
            }
            if (discs.isEmpty()) config.set("discs", defaultDiscs);
            else config.set("discs", discs);
        }

        plugin.saveConfig();
    }

    public static Object getValue(String path) {
        return Main.getPlugin().getConfig().get(path);
    }
}

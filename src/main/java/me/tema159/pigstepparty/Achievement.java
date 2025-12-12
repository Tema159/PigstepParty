package me.tema159.pigstepparty;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.advancement.Advancement;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class Achievement {
    private static String loadJsonFromResources(String path) {
        try (InputStream inputStream = Main.class.getClassLoader().getResourceAsStream(path)) {
            return inputStream == null ? null : new String(inputStream.readAllBytes(), StandardCharsets.UTF_8)
                    .replace("jukebox_dance.title", String.valueOf(Config.getValue("advancement.title")))
                    .replace("jukebox_dance.description", String.valueOf(Config.getValue("advancement.description")));
        } catch (IOException e) {
            return null;
        }
    }

    @SuppressWarnings("deprecation")
    public static Advancement registerAdvancement(boolean again) {
        if (!((boolean) Config.getValue("advancement.enabled")))
            return null;

        NamespacedKey key = Main.getKey();
        String jsonResourcePath = "data/" + key.getNamespace() + "/advancements/" + key.getKey() + (again ? 0 : 1) + ".json";
        String json = loadJsonFromResources(jsonResourcePath);

        if (Bukkit.getAdvancement(key) != null)
            Bukkit.getUnsafe().removeAdvancement(key);

        try {
            return Bukkit.getUnsafe().loadAdvancement(key, json);
        } catch (Exception e) {
            if (!again) return registerAdvancement(true);
            Main.getPlugin().getLogger().severe(e.getMessage());
            return null;
        }
    }
}

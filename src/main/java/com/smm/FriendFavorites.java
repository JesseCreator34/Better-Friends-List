package com.smm;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class FriendFavorites {

    private static final Gson GSON = new Gson();
    private static final Path FILE =
            FabricLoader.getInstance().getConfigDir().resolve("betterfriendslist_favorites.json");

    private static Set<UUID> favorites = new HashSet<>();

    public static boolean isFavorite(UUID id) {
        return favorites.contains(id);
    }

    public static void toggle(UUID id) {
        if (!favorites.add(id)) {
            favorites.remove(id);
        }
        save();
    }

    public static void load() {
        try {
            if (!Files.exists(FILE)) return;

            Type type = new TypeToken<Set<UUID>>() {}.getType();
            favorites = GSON.fromJson(Files.readString(FILE), type);

            if (favorites == null) favorites = new HashSet<>();
        } catch (Exception e) {
            favorites = new HashSet<>();
        }
    }

    public static void save() {
        try {
            Files.writeString(FILE, GSON.toJson(favorites));
        } catch (Exception ignored) {}
    }
}
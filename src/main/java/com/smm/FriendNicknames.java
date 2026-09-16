package com.smm;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.loader.api.FabricLoader;

import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FriendNicknames {

    private static final Gson GSON = new Gson();
    private static final Path FILE =
            FabricLoader.getInstance().getConfigDir().resolve("betterfriendslist_nicknames.json");

    private static Map<UUID, String> nicknames = new HashMap<>();

    public static String getNickname(UUID id) {
        return nicknames.get(id);
    }

    public static void setNickname(UUID id, String nickname) {
        if (nickname == null || nickname.isBlank()) {
            nicknames.remove(id);
        } else {
            nicknames.put(id, nickname);
        }
        save();
    }

    public static void load() {
        try {
            if (!Files.exists(FILE)) return;

            Type type = new TypeToken<Map<UUID, String>>() {}.getType();
            nicknames = GSON.fromJson(Files.readString(FILE), type);

            if (nicknames == null) nicknames = new HashMap<>();
        } catch (Exception e) {
            nicknames = new HashMap<>();
        }
    }

    public static void save() {
        try {
            Files.writeString(FILE, GSON.toJson(nicknames));
        } catch (Exception ignored) {}
    }
}
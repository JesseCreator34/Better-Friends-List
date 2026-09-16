package com.smm;

import com.smm.FriendFavorites;
import com.smm.FriendNicknames;
import net.fabricmc.api.ModInitializer;
import java.util.UUID;

public class BetterFriendsList implements ModInitializer {
    // Stores the UUID of the currently selected friend (null if none selected)
    public static UUID selectedFriendId = null;

    @Override
    public void onInitialize() {
        // Loads existing favorites from file on startup
        FriendFavorites.load();

        // Loads existing nicknames from file on startup
        FriendNicknames.load();
    }
}
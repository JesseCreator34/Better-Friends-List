package com.smm.mixin;

import java.util.UUID;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(targets = "net.minecraft.client.gui.screens.friends.AbstractFriendsEntryContainerWidget")
public interface AbstractFriendsEntryAccessor {
    // "playerId" is the exact field name defined in the class
    @Accessor("playerId")
    UUID getPlayerId();
}
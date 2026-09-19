package com.smm.mixin;

import com.smm.FriendFavorites;
import net.minecraft.client.gui.screens.friends.FriendsOverlayScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import java.util.Comparator;
import java.util.List;

@Mixin(FriendsOverlayScreen.class)
public class FriendsListMixin {

    // Sorts the primary friends list by favorite status first, then by vanilla comparator
    @ModifyArg(
            method = "populateLists",
            at = @At(value = "INVOKE", target = "Ljava/util/List;sort(Ljava/util/Comparator;)V"),
            remap = false
    )
    private Comparator<Object> betterfriendslist$pinFavorites(Comparator<Object> original) {
        return Comparator.comparingInt(
                (Object entry) -> {
                    if (entry instanceof AbstractFriendsEntryAccessor accessor) {
                        return FriendFavorites.isFavorite(accessor.getPlayerId()) ? 0 : 1;
                    }
                    return 1;
                }
        ).thenComparing(original);
    }

    // Sorts the "Pending" tab incoming and outgoing lists with safety checks
    @ModifyArgs(
            method = "populateLists",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/screens/friends/PendingTab;updateEntries(Ljava/util/List;Ljava/util/List;)V"
            )
    )
    private void betterfriendslist$sortPendingEntries(Args args) {
        List<?> incoming = args.get(0);
        List<?> outgoing = args.get(1);

        // Safe comparator checking if the entry actually implements the target accessor
        Comparator<Object> favoriteComparator = Comparator.comparingInt(
                entry -> {
                    if (entry instanceof AbstractFriendsEntryAccessor accessor) {
                        return FriendFavorites.isFavorite(accessor.getPlayerId()) ? 0 : 1;
                    }
                    return 1;
                }
        );

        if (incoming != null) {
            ((List<Object>) incoming).sort(favoriteComparator);
        }
        if (outgoing != null) {
            ((List<Object>) outgoing).sort(favoriteComparator);
        }
    }
}
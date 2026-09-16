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

    // Sorteert de hoofdlijst met vrienden
    @ModifyArg(
            method = "populateLists",
            at = @At(value = "INVOKE", target = "Ljava/util/List;sort(Ljava/util/Comparator;)V"),
            remap = false
    )
    private Comparator<Object> betterfriendslist$pinFavorites(Comparator<Object> original) {
        return Comparator.comparingInt(
                (Object entry) -> {
                    // Gebruik de accessor interface (deze is in jouw package)
                    var id = ((AbstractFriendsEntryAccessor) entry).getPlayerId();
                    return FriendFavorites.isFavorite(id) ? 0 : 1;
                }
        ).thenComparing(original);
    }

    // Sorteert de "Pending" tab
    @ModifyArgs(
            method = "populateLists",
            at = @At(
                    value = "INVOKE",
                    // De String-target werkt gewoon, ook zonder import
                    target = "Lnet/minecraft/client/gui/screens/friends/PendingTab;updateEntries(Ljava/util/List;Ljava/util/List;)V"
            )
    )
    private void betterfriendslist$sortPendingEntries(Args args) {
        // Gebruik raw types (List) in plaats van List<IncomingEntry>
        List incoming = args.get(0);
        List outgoing = args.get(1);

        Comparator<Object> favoriteComparator = Comparator.comparingInt(
                entry -> FriendFavorites.isFavorite(((AbstractFriendsEntryAccessor) entry).getPlayerId()) ? 0 : 1
        );

        // Dit werkt zolang de lijsten mutable zijn (wat ze zijn in populateLists)
        incoming.sort(favoriteComparator);
        outgoing.sort(favoriteComparator);
    }
}
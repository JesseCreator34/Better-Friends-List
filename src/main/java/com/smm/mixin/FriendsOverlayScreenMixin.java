package com.smm.mixin;

import com.smm.gui.SelectedFriendPanel;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.friends.FriendsOverlayScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
public class FriendsOverlayScreenMixin {

    @Shadow
    public int width; // Haalt de breedte van het scherm op uit de vanilla Screen klasse

    @Unique
    private static SelectedFriendPanel betterFriendsList$panel;

    // We injecteren nu in de extractRenderState van Screen, zodat we toegang hebben tot de juiste renderfase
    @Inject(method = "extractRenderState", at = @At("RETURN"))
    private void renderPanel(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta, CallbackInfo ci) {

        // Zorg ervoor dat de code alléén uitvoert als het actieve scherm de FriendsOverlayScreen is
        if ((Object) this instanceof FriendsOverlayScreen) {

            // Lazy initialization: maak het panel pas aan als het scherm daadwerkelijk opent
            if (betterFriendsList$panel == null) {
                betterFriendsList$panel = new SelectedFriendPanel();
            }

            int panelWidth = 140;

            // Gebruik de geshadowde width van het scherm voor de juiste uitlijning rechts
            int x = this.width - panelWidth - 45;
            int y = 33;

            betterFriendsList$panel.render(
                    graphics,
                    x,
                    y,
                    panelWidth,
                    180,
                    mouseX,
                    mouseY
            );
        }
    }
}
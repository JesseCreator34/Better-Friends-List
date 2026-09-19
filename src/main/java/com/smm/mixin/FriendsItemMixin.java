package com.smm.mixin;

import com.smm.BetterFriendsList;
import com.smm.FriendFavorites;
import com.smm.FriendNicknames;
import com.smm.NicknameScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractContainerWidget;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.PlainTextButton;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.PlayerFaceWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.friends.FriendsOverlayScreen;
import net.minecraft.client.gui.screens.social.PlayerSocialManager;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;
import java.util.List;

@Mixin(targets = "net.minecraft.client.gui.screens.friends.AbstractFriendsEntryContainerWidget")
public abstract class FriendsItemMixin extends AbstractContainerWidget {

    @Shadow protected abstract void addChild(AbstractWidget child);
    @Shadow protected StringWidget nameWidget;
    @Shadow protected PlayerFaceWidget playerFaceWidget;

    @Unique private PlayerSocialManager.PlayerData betterfriendslist$playerData;
    @Unique private Button betterfriendslist$favoriteButton;
    @Unique private Button betterfriendslist$nicknameButton;

    public FriendsItemMixin(int x, int y, int width, int height, Component message) {
        super(x, y, width, height, message);
    }

    @Inject(
            method = "<init>(Lnet/minecraft/client/Minecraft;Lnet/minecraft/client/gui/screens/friends/FriendsOverlayScreen;IIIILnet/minecraft/client/gui/screens/social/PlayerSocialManager$PlayerData;Z)V",
            at = @At("RETURN")
    )
    private void betterfriendslist$addCustomWidgets(
            Minecraft minecraft,
            FriendsOverlayScreen screen,
            int x,
            int y,
            int width,
            int height,
            PlayerSocialManager.PlayerData playerData,
            boolean showingStatus,
            CallbackInfo ci
    ) {
        this.betterfriendslist$playerData = playerData;

        String nickname = FriendNicknames.getNickname(playerData.id());
        if (nickname != null && !nickname.isBlank()) {
            this.nameWidget.setMessage(Component.literal(nickname));
        }

        Component initialIcon = Component.literal(
                FriendFavorites.isFavorite(playerData.id()) ? "★" : "☆"
        );

        this.betterfriendslist$favoriteButton = PlainTextButton.builder(
                        initialIcon,
                        btn -> {
                            FriendFavorites.toggle(playerData.id());
                            btn.setMessage(Component.literal(
                                    FriendFavorites.isFavorite(playerData.id()) ? "★" : "☆"
                            ));
                            screen.refreshLists();
                        }
                )
                .size(20, 20)
                .tooltip(Tooltip.create(Component.translatable("betterfriendslist.tooltip.toggle_favorite")))
                .build();

        this.betterfriendslist$nicknameButton = PlainTextButton.builder(
                        Component.literal("✎"),
                        btn -> minecraft.gui.setScreen(
                                new NicknameScreen(
                                        screen,
                                        playerData.id(),
                                        screen::refreshLists
                                )
                        )
                )
                .size(20, 20)
                .tooltip(Tooltip.create(Component.translatable("betterfriendslist.tooltip.set_nickname")))
                .build();

        this.addChild(this.betterfriendslist$favoriteButton);
        this.addChild(this.betterfriendslist$nicknameButton);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (!this.betterfriendslist$isPlayerDataValid()) {
            return false;
        }

        // Only handle clicks if mouse is within this container boundary
        if (!this.isMouseOver(event.x(), event.y())) {
            return false;
        }

        // Allow child buttons to handle clicks in reverse order
        List<? extends GuiEventListener> children = this.children();
        for (int i = children.size() - 1; i >= 0; i--) {
            GuiEventListener child = children.get(i);

            if (!child.isMouseOver(event.x(), event.y())) {
                continue;
            }

            if (child.mouseClicked(event, doubleClick)) {
                if (child.shouldTakeFocusAfterInteraction()) {
                    this.setFocused(child);

                    if (event.button() == 0) {
                        this.setDragging(true);
                    }
                }
                return true;
            }
        }

        // Right-click selects the friend entry for inspection
        if (event.button() == 1) {
            BetterFriendsList.selectedFriendId = this.betterfriendslist$playerData.id();

            Minecraft.getInstance().getSoundManager().play(
                    net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(
                            net.minecraft.sounds.SoundEvents.UI_BUTTON_CLICK,
                            1.0F
                    )
            );

            return true;
        }

        return false;
    }

    @Unique
    private boolean betterfriendslist$isPlayerDataValid() {
        return this.betterfriendslist$playerData != null;
    }

    @Inject(method = "extractWidgetRenderState", at = @At("HEAD"))
    private void betterfriendslist$updateHoverName(
            GuiGraphicsExtractor graphics,
            int mouseX,
            int mouseY,
            float a,
            CallbackInfo ci
    ) {
        if (this.betterfriendslist$playerData == null) {
            return;
        }

        boolean isSelected = this.betterfriendslist$playerData.id()
                .equals(BetterFriendsList.selectedFriendId);

        // Highlight selected or hovered friend entry
        if (isSelected) {
            graphics.fill(
                    this.getX(),
                    this.getY(),
                    this.getX() + this.getWidth(),
                    this.getY() + this.getHeight(),
                    0x33FFFFFF
            );
        } else if (this.isMouseOver(mouseX, mouseY)) {
            graphics.fill(
                    this.getX(),
                    this.getY(),
                    this.getX() + this.getWidth(),
                    this.getY() + this.getHeight(),
                    0x1AFFFFFF
            );
        }

        String nickname = FriendNicknames.getNickname(this.betterfriendslist$playerData.id());
        if (nickname != null && !nickname.isBlank()) {
            if (this.isHoveredOrFocused() || isSelected) {
                this.nameWidget.setMessage(
                        Component.literal(nickname + " (" + this.betterfriendslist$playerData.name() + ")")
                );
            } else {
                this.nameWidget.setMessage(Component.literal(nickname));
            }
        }
    }

    @Inject(method = "extractWidgetRenderState", at = @At("RETURN"))
    private void betterfriendslist$positionCustomWidgets(
            GuiGraphicsExtractor graphics,
            int mouseX,
            int mouseY,
            float a,
            CallbackInfo ci
    ) {
        if (this.betterfriendslist$favoriteButton == null
                || this.betterfriendslist$nicknameButton == null) {
            return;
        }

        int center = this.getY() + (this.getHeight() - 20) / 2;
        int favoriteX = this.getX() + this.getWidth() - 42;
        int nicknameX = this.getX() + this.getWidth() - 64;

        this.betterfriendslist$favoriteButton.setPosition(favoriteX, center);
        this.betterfriendslist$favoriteButton.extractRenderState(graphics, mouseX, mouseY, a);

        this.betterfriendslist$nicknameButton.setPosition(nicknameX, center);
        this.betterfriendslist$nicknameButton.extractRenderState(graphics, mouseX, mouseY, a);
    }

    @ModifyArgs(
            method = "extractWidgetRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IIF)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/components/PlayerFaceWidget;setPosition(II)V"
            )
    )
    private void betterfriendslist$moveFaceRight(Args args) {
        int x = args.get(0);
        // Shift player avatar 2 pixels rightward to improve alignment
        args.set(0, x + 2);
    }
}
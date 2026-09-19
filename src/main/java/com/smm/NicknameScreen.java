package com.smm;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import java.util.UUID;

public class NicknameScreen extends Screen {
    private final Screen lastScreen;
    private final UUID playerId;
    private final Runnable onSave;
    private EditBox editBox;

    public NicknameScreen(Screen lastScreen, UUID playerId, Runnable onSave) {
        // Uses a translatable component for screen title localization
        super(Component.translatable("betterfriendslist.screen.nickname.title"));
        this.lastScreen = lastScreen;
        this.playerId = playerId;
        this.onSave = onSave;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        // Title above the input field (width: 200, centered)
        this.addRenderableWidget(new StringWidget(centerX - 100, centerY - 40, 200, 20, this.title, this.font));

        // Text input field for the nickname with a translatable hint
        this.editBox = new EditBox(
                this.font,
                centerX - 100,
                centerY - 15,
                200,
                20,
                Component.translatable("betterfriendslist.screen.nickname.input_hint")
        );
        this.editBox.setMaxLength(24);

        String currentNickname = FriendNicknames.getNickname(this.playerId);
        if (currentNickname != null) {
            this.editBox.setValue(currentNickname);
        }
        this.addRenderableWidget(this.editBox);

        // "Done" button aligned with the left edge of the input box
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> {
            FriendNicknames.setNickname(this.playerId, this.editBox.getValue());
            this.onSave.run();
            this.minecraft.gui.setScreen(this.lastScreen);
        }).bounds(centerX - 100, centerY + 15, 98, 20).build());

        // "Cancel" button aligned with the right edge of the input box
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, button -> {
            this.minecraft.gui.setScreen(this.lastScreen);
        }).bounds(centerX + 2, centerY + 15, 98, 20).build());
    }

    @Override
    public void onClose() {
        this.minecraft.gui.setScreen(this.lastScreen);
    }
}
package com.smm.gui;

import com.smm.BetterFriendsList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.PlayerSkinRenderCache;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.state.gui.pip.GuiEntityRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.component.ResolvableProfile;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.UUID;

public class SelectedFriendPanel {

    public void render(GuiGraphicsExtractor graphics, int x, int y, int w, int h, int mouseX, int mouseY) {
        UUID id = BetterFriendsList.selectedFriendId;
        if (id == null) return;

        Minecraft mc = Minecraft.getInstance();

        // Draw panel background
        graphics.fill(x, y, x + w, y + h, 0x55000000);

        ResolvableProfile profile = ResolvableProfile.createUnresolved(id);
        PlayerSkinRenderCache cache = mc.playerSkinRenderCache();
        PlayerSkinRenderCache.RenderInfo info = cache.getOrDefault(profile);

        AvatarRenderState avatar = new AvatarRenderState();
        avatar.skin = info.playerSkin();
        avatar.showCape = true;

        float cx = x + w / 2f;
        float cy = y + h / 2f;

        float angleX = (float) Math.atan((cx - mouseX) / 40f);
        float angleY = (float) Math.atan((cy - mouseY) / 40f);

        Quaternionf rot = new Quaternionf()
                .rotationZ((float) Math.PI)
                .rotateY((float) Math.PI)
                .rotateX(-angleY * 0.5f)
                .rotateY(-angleX * 0.5f);

        // Begin scissor clipping for both the PIP model and inside text
        graphics.enableScissor(x, y, x + w, y + h);

        GuiEntityRenderState state = new GuiEntityRenderState(
                avatar,
                new Vector3f(0, 1, 0),
                rot,
                null,
                x, y, x + w, y + h,
                70f,
                graphics.scissorStack.peek()
        );

        // Submit PIP entity rendering state
        graphics.guiRenderState.addPicturesInPictureState(state);

        // Render player name text inside the scissor area to prevent overflow
        graphics.text(
                mc.font,
                Component.literal(info.gameProfile().name()),
                x + 8,
                y + 8,
                0xFFFFFF
        );

        // Properly disable scissor after all content within this panel has been extracted
        graphics.disableScissor();
    }
}
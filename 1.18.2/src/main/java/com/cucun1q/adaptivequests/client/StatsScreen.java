package com.cucun1q.adaptivequests.client;

import com.cucun1q.adaptivequests.network.ClientQuestCache;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.TextComponent;

public class StatsScreen extends Screen {
    private final Screen parent;

    public StatsScreen(Screen parent) {
        super(new TextComponent("All-time Stats"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        this.addRenderableWidget(new Button(this.width / 2 - 50, this.height - 30, 100, 20, new TextComponent("Back"), b -> this.minecraft.setScreen(parent)));
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(poseStack);
        drawCenteredString(poseStack, this.font, this.title.getString(), this.width / 2, 8, 0xFFFFFF);
        int y = 40;
        for (com.cucun1q.adaptivequests.network.SyncQuestsPacket.StatDTO s : ClientQuestCache.breakdown()) {
            this.font.draw(poseStack, s.block + ": " + s.count, 20, y, 0xCCCCCC);
            y += 12;
        }
        super.render(poseStack, mouseX, mouseY, partialTicks);
    }
}



package com.cucun1q.adaptivequests.client;

import com.cucun1q.adaptivequests.network.ClientQuestCache;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

public class StatsScreen extends Screen {
    private final Screen parent;

    public StatsScreen(Screen parent) {
        super(Component.literal("All-time Stats"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        this.addRenderableWidget(Button.builder(Component.literal("Back"), b -> this.minecraft.setScreen(parent)).bounds(this.width / 2 - 50, this.height - 30, 100, 20).build());
    }

    @Override
    public void render(GuiGraphics gfx, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(gfx);
        gfx.drawCenteredString(this.font, this.title.getString(), this.width / 2, 8, 0xFFFFFF);
        int y = 40;
        for (com.cucun1q.adaptivequests.network.SyncQuestsPacket.StatDTO s : ClientQuestCache.breakdown()) {
            gfx.drawString(this.font, s.block + ": " + s.count, 20, y, 0xCCCCCC, false);
            y += 12;
        }
        super.render(gfx, mouseX, mouseY, partialTicks);
    }
}



package com.cucun1q.adaptivequests.client;

import com.cucun1q.adaptivequests.network.ClaimQuestPacket;
import com.cucun1q.adaptivequests.network.NetworkHandler;
import com.cucun1q.adaptivequests.quest.QuestManager;
import com.cucun1q.adaptivequests.quest.QuestModels;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class QuestsScreen extends Screen {
    private enum Tab { LOGGING, MINING }
    private Tab tab = Tab.LOGGING;
    private List<QuestModels.Quest> quests = new ArrayList<>();

    public QuestsScreen() {
        super(Component.literal("Adaptive Quests"));
    }

    @Override
    protected void init() {
        UUID id = Minecraft.getInstance().player != null ? Minecraft.getInstance().player.getUUID() : new UUID(0,0);
        refresh(id);
        int left = this.width / 2 - 120;
        this.addRenderableWidget(Button.builder(Component.literal("Logging"), b -> { tab = Tab.LOGGING; refresh(id);} ).bounds(left, 20, 80, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal("Mining"), b -> { tab = Tab.MINING; refresh(id);} ).bounds(left + 90, 20, 80, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal("All-time"), b -> openStats()).bounds(this.width - 170, 20, 70, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal("Claim All"), b -> claimAll()).bounds(this.width - 90, 20, 70, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal("Close"), b -> onClose()).bounds(this.width / 2 - 50, this.height - 30, 100, 20).build());
    }

    private void refresh(UUID id) {
        quests.clear();
        // If client has synced DTOs, prefer those for reward text and up-to-date state
        java.util.List<com.cucun1q.adaptivequests.network.SyncQuestsPacket.QuestDTO> dtos = com.cucun1q.adaptivequests.network.ClientQuestCache.get();
        if (!dtos.isEmpty()) {
            for (com.cucun1q.adaptivequests.network.SyncQuestsPacket.QuestDTO dto : dtos) {
                QuestModels.Category cat = dto.category == 0 ? QuestModels.Category.LOGGING : QuestModels.Category.MINING;
                if ((tab == Tab.LOGGING && cat == QuestModels.Category.LOGGING) || (tab == Tab.MINING && cat == QuestModels.Category.MINING)) {
                    // Create a lightweight view string using dto
                    // We will render buttons directly from dto below
                }
            }
        }
        this.clearWidgets();
        int left = this.width / 2 - 120;
        this.addRenderableWidget(Button.builder(Component.literal("Logging"), b -> { tab = Tab.LOGGING; refresh(id);} ).bounds(left, 20, 80, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal("Mining"), b -> { tab = Tab.MINING; refresh(id);} ).bounds(left + 90, 20, 80, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal("Claim All"), b -> claimAll()).bounds(this.width - 90, 20, 70, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal("Close"), b -> onClose()).bounds(this.width / 2 - 50, this.height - 30, 100, 20).build());

        int y = 60;
        if (!dtos.isEmpty()) {
            for (com.cucun1q.adaptivequests.network.SyncQuestsPacket.QuestDTO dto : dtos) {
                QuestModels.Category cat = dto.category == 0 ? QuestModels.Category.LOGGING : QuestModels.Category.MINING;
                if (!((tab == Tab.LOGGING && cat == QuestModels.Category.LOGGING) || (tab == Tab.MINING && cat == QuestModels.Category.MINING))) continue;
                String state = dto.completed ? (dto.claimed ? "CLAIMED" : "COMPLETE") : (dto.progress + "/" + dto.target);
                String label = dto.title + " L" + dto.level + " - " + state + (dto.rewardText.isEmpty() ? "" : "  (" + dto.rewardText + ")");
                boolean enabled = dto.completed && !dto.claimed;
                final String qid = dto.id;
                this.addRenderableWidget(Button.builder(Component.literal(label), b -> {
                    if (enabled) {
                        NetworkHandler.CHANNEL.sendToServer(new ClaimQuestPacket(qid));
                        NetworkHandler.CHANNEL.sendToServer(new com.cucun1q.adaptivequests.network.RequestSyncPacket());
                    }
                }).bounds(left, y, 260, 20).build()) ;
                y += 24;
            }
        } else {
            for (QuestModels.Quest q : quests) {
                String label = q.title + " L" + q.level + " - " + (q.completed ? (q.claimed ? "CLAIMED" : "COMPLETE") : (q.progress + "/" + q.target)) +
                        "  (" + QuestManager.previewReward(q) + ")";
                boolean enabled = q.completed && !q.claimed;
                this.addRenderableWidget(Button.builder(Component.literal(label), b -> {
                    if (enabled) {
                        NetworkHandler.CHANNEL.sendToServer(new ClaimQuestPacket(q.id));
                        NetworkHandler.CHANNEL.sendToServer(new com.cucun1q.adaptivequests.network.RequestSyncPacket());
                    }
                }).bounds(left, y, 260, 20).build());
                y += 24;
            }
        }
    }

    private void claimAll() {
        for (QuestModels.Quest q : quests) {
            if (q.completed && !q.claimed) {
                NetworkHandler.CHANNEL.sendToServer(new ClaimQuestPacket(q.id));
                q.claimed = true;
            }
        }
    }

    @Override
    public void render(GuiGraphics gfx, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(gfx);
        gfx.drawCenteredString(this.font, this.title.getString(), this.width / 2, 8, 0xFFFFFF);
        int left = this.width / 2 - 120;
        super.render(gfx, mouseX, mouseY, partialTicks);
    }

    private void openStats() {
        this.minecraft.setScreen(new StatsScreen(this));
    }
}



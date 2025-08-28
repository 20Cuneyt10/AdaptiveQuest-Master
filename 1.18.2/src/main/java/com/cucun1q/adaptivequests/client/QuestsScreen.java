package com.cucun1q.adaptivequests.client;

import com.cucun1q.adaptivequests.network.ClaimQuestPacket;
import com.cucun1q.adaptivequests.network.NetworkHandler;
import com.cucun1q.adaptivequests.quest.QuestManager;
import com.cucun1q.adaptivequests.quest.QuestModels;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.TextComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class QuestsScreen extends Screen {
    private enum Tab { LOGGING, MINING }
    private Tab tab = Tab.LOGGING;
    private List<QuestModels.Quest> quests = new ArrayList<>();

    public QuestsScreen() {
        super(new TextComponent("Adaptive Quests"));
    }

    @Override
    protected void init() {
        UUID id = Minecraft.getInstance().player != null ? Minecraft.getInstance().player.getUUID() : new UUID(0,0);
        refresh(id);
        int left = this.width / 2 - 120;
        this.addRenderableWidget(new Button(left, 20, 80, 20, new TextComponent("Logging"), b -> { tab = Tab.LOGGING; refresh(id);}));
        this.addRenderableWidget(new Button(left + 90, 20, 80, 20, new TextComponent("Mining"), b -> { tab = Tab.MINING; refresh(id);}));
        this.addRenderableWidget(new Button(this.width - 170, 20, 70, 20, new TextComponent("All-time"), b -> openStats()));
        this.addRenderableWidget(new Button(this.width - 90, 20, 70, 20, new TextComponent("Claim All"), b -> claimAll()));
        this.addRenderableWidget(new Button(this.width / 2 - 50, this.height - 30, 100, 20, new TextComponent("Close"), b -> onClose()));
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
        this.addRenderableWidget(new Button(left, 20, 80, 20, new TextComponent("Logging"), b -> { tab = Tab.LOGGING; refresh(id);}));
        this.addRenderableWidget(new Button(left + 90, 20, 80, 20, new TextComponent("Mining"), b -> { tab = Tab.MINING; refresh(id);}));
        this.addRenderableWidget(new Button(this.width - 90, 20, 70, 20, new TextComponent("Claim All"), b -> claimAll()));
        this.addRenderableWidget(new Button(this.width / 2 - 50, this.height - 30, 100, 20, new TextComponent("Close"), b -> onClose()));

        int y = 60;
        if (!dtos.isEmpty()) {
            for (com.cucun1q.adaptivequests.network.SyncQuestsPacket.QuestDTO dto : dtos) {
                QuestModels.Category cat = dto.category == 0 ? QuestModels.Category.LOGGING : QuestModels.Category.MINING;
                if (!((tab == Tab.LOGGING && cat == QuestModels.Category.LOGGING) || (tab == Tab.MINING && cat == QuestModels.Category.MINING))) continue;
                String state = dto.completed ? (dto.claimed ? "CLAIMED" : "COMPLETE") : (dto.progress + "/" + dto.target);
                String label = dto.title + " L" + dto.level + " - " + state + (dto.rewardText.isEmpty() ? "" : "  (" + dto.rewardText + ")");
                boolean enabled = dto.completed && !dto.claimed;
                final String qid = dto.id;
                this.addRenderableWidget(new Button(left, y, 260, 20, new TextComponent(label), b -> {
                    if (enabled) {
                        NetworkHandler.CHANNEL.sendToServer(new ClaimQuestPacket(qid));
                        NetworkHandler.CHANNEL.sendToServer(new com.cucun1q.adaptivequests.network.RequestSyncPacket());
                    }
                }) {{ active = enabled; }});
                y += 24;
            }
        } else {
            for (QuestModels.Quest q : quests) {
                String label = q.title + " L" + q.level + " - " + (q.completed ? (q.claimed ? "CLAIMED" : "COMPLETE") : (q.progress + "/" + q.target)) +
                        "  (" + QuestManager.previewReward(q) + ")";
                boolean enabled = q.completed && !q.claimed;
                this.addRenderableWidget(new Button(left, y, 260, 20, new TextComponent(label), b -> {
                    if (enabled) {
                        NetworkHandler.CHANNEL.sendToServer(new ClaimQuestPacket(q.id));
                        NetworkHandler.CHANNEL.sendToServer(new com.cucun1q.adaptivequests.network.RequestSyncPacket());
                    }
                }) {{ active = enabled; }});
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
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(poseStack);
        drawCenteredString(poseStack, this.font, this.title.getString(), this.width / 2, 8, 0xFFFFFF);
        int left = this.width / 2 - 120;
        super.render(poseStack, mouseX, mouseY, partialTicks);
    }

    private void openStats() {
        this.minecraft.setScreen(new StatsScreen(this));
    }
}



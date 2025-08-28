package com.cucun1q.adaptivequests.network;

import com.cucun1q.adaptivequests.quest.QuestManager;
import com.cucun1q.adaptivequests.quest.QuestModels;
import net.minecraft.world.level.block.Block;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;

public class NetworkSender {
    public static void sendFullSync(ServerPlayer player) {
        QuestManager.PlayerProgress p = QuestManager.getProgress(player.getUUID());
        List<SyncQuestsPacket.QuestDTO> list = new ArrayList<>();
        for (QuestModels.Quest q : p.quests) {
            SyncQuestsPacket.QuestDTO dto = new SyncQuestsPacket.QuestDTO();
            dto.id = q.id;
            dto.category = q.category.ordinal();
            dto.title = q.title;
            dto.trackedBlock = q.trackedBlock == null ? "" : ForgeRegistries.BLOCKS.getKey(q.trackedBlock).toString();
            dto.target = q.target;
            dto.progress = q.progress;
            dto.level = q.level;
            dto.completed = q.completed;
            dto.claimed = q.claimed;
            dto.rewardText = QuestManager.previewReward(q);
            list.add(dto);
        }
        int coal = p.blockMineCounts.getOrDefault(net.minecraft.world.level.block.Blocks.COAL_ORE, 0);
        int iron = p.blockMineCounts.getOrDefault(net.minecraft.world.level.block.Blocks.IRON_ORE, 0);
        int logs = 0;
        java.util.List<SyncQuestsPacket.StatDTO> stats = new java.util.ArrayList<>();
        for (java.util.Map.Entry<net.minecraft.world.level.block.Block, Integer> e : p.blockMineCounts.entrySet()) {
            if (e.getKey() instanceof net.minecraft.world.level.block.RotatedPillarBlock && e.getKey().defaultBlockState().is(net.minecraft.tags.BlockTags.LOGS)) {
                logs += e.getValue();
            }
            SyncQuestsPacket.StatDTO s = new SyncQuestsPacket.StatDTO();
            net.minecraft.resources.ResourceLocation key = ForgeRegistries.BLOCKS.getKey(e.getKey());
            s.block = key != null ? key.toString() : "unknown";
            s.count = e.getValue();
            stats.add(s);
        }
        NetworkHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new SyncQuestsPacket(list, coal, iron, logs, stats));
    }
}



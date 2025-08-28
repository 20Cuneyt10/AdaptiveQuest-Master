package com.cucun1q.adaptivequests.network;

import com.cucun1q.adaptivequests.quest.QuestModels;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class SyncQuestsPacket {
    public static class QuestDTO {
        public String id;
        public int category; // ordinal
        public String title;
        public String trackedBlock; // registry name or ""
        public int target;
        public int progress;
        public int level;
        public boolean completed;
        public boolean claimed;
        public String rewardText;
    }

    public static class StatDTO {
        public String block;
        public int count;
    }

    public final List<QuestDTO> quests;
    public final int coalMined;
    public final int ironMined;
    public final int logsMined;
    public final List<StatDTO> stats;

    public SyncQuestsPacket(List<QuestDTO> quests, int coalMined, int ironMined, int logsMined, List<StatDTO> stats) {
        this.quests = quests;
        this.coalMined = coalMined;
        this.ironMined = ironMined;
        this.logsMined = logsMined;
        this.stats = stats;
    }

    public static void encode(SyncQuestsPacket pkt, FriendlyByteBuf buf) {
        buf.writeVarInt(pkt.quests.size());
        for (QuestDTO q : pkt.quests) {
            buf.writeUtf(q.id);
            buf.writeVarInt(q.category);
            buf.writeUtf(q.title);
            buf.writeUtf(q.trackedBlock == null ? "" : q.trackedBlock);
            buf.writeVarInt(q.target);
            buf.writeVarInt(q.progress);
            buf.writeVarInt(q.level);
            buf.writeBoolean(q.completed);
            buf.writeBoolean(q.claimed);
            buf.writeUtf(q.rewardText == null ? "" : q.rewardText);
        }
        buf.writeVarInt(pkt.coalMined);
        buf.writeVarInt(pkt.ironMined);
        buf.writeVarInt(pkt.logsMined);
        buf.writeVarInt(pkt.stats.size());
        for (StatDTO s : pkt.stats) {
            buf.writeUtf(s.block);
            buf.writeVarInt(s.count);
        }
    }

    public static SyncQuestsPacket decode(FriendlyByteBuf buf) {
        int n = buf.readVarInt();
        List<QuestDTO> list = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            QuestDTO q = new QuestDTO();
            q.id = buf.readUtf(256);
            q.category = buf.readVarInt();
            q.title = buf.readUtf(256);
            q.trackedBlock = buf.readUtf(256);
            q.target = buf.readVarInt();
            q.progress = buf.readVarInt();
            q.level = buf.readVarInt();
            q.completed = buf.readBoolean();
            q.claimed = buf.readBoolean();
            q.rewardText = buf.readUtf(256);
            list.add(q);
        }
        int coal = buf.readVarInt();
        int iron = buf.readVarInt();
        int logs = buf.readVarInt();
        int m = buf.readVarInt();
        List<StatDTO> stats = new ArrayList<>(m);
        for (int i = 0; i < m; i++) {
            StatDTO s = new StatDTO();
            s.block = buf.readUtf(256);
            s.count = buf.readVarInt();
            stats.add(s);
        }
        return new SyncQuestsPacket(list, coal, iron, logs, stats);
    }

    public static void handle(SyncQuestsPacket pkt, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ClientQuestCache.set(pkt.quests);
            ClientQuestCache.setStats(pkt.coalMined, pkt.ironMined, pkt.logsMined);
            ClientQuestCache.setBreakdown(pkt.stats);
        });
        ctx.get().setPacketHandled(true);
    }
}



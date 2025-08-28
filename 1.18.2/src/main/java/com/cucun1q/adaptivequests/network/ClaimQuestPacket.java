package com.cucun1q.adaptivequests.network;

import com.cucun1q.adaptivequests.quest.QuestManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ClaimQuestPacket {
    public final String questId;

    public ClaimQuestPacket(String questId) {
        this.questId = questId;
    }

    public static void encode(ClaimQuestPacket pkt, FriendlyByteBuf buf) {
        buf.writeUtf(pkt.questId);
    }

    public static ClaimQuestPacket decode(FriendlyByteBuf buf) {
        return new ClaimQuestPacket(buf.readUtf(256));
    }

    public static void handle(ClaimQuestPacket pkt, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (ctx.get().getSender() != null) {
                QuestManager.claim(ctx.get().getSender(), pkt.questId);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}



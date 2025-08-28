package com.cucun1q.adaptivequests.network;

import com.cucun1q.adaptivequests.quest.QuestManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class RequestSyncPacket {
    public static void encode(RequestSyncPacket pkt, FriendlyByteBuf buf) {}
    public static RequestSyncPacket decode(FriendlyByteBuf buf) { return new RequestSyncPacket(); }
    public static void handle(RequestSyncPacket pkt, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (ctx.get().getSender() != null) {
                NetworkSender.sendFullSync(ctx.get().getSender());
            }
        });
        ctx.get().setPacketHandled(true);
    }
}



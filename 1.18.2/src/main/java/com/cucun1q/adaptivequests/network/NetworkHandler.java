package com.cucun1q.adaptivequests.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import java.util.Optional;

public class NetworkHandler {
    private static final String PROTOCOL = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation("adaptivequests", "main"),
            () -> PROTOCOL,
            PROTOCOL::equals,
            PROTOCOL::equals
    );

    private static int packetId = 0;
    private static int id() { return packetId++; }

    public static void register() {
        CHANNEL.registerMessage(id(), ClaimQuestPacket.class,
                ClaimQuestPacket::encode,
                ClaimQuestPacket::decode,
                ClaimQuestPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER));
        CHANNEL.registerMessage(id(), RequestSyncPacket.class,
                RequestSyncPacket::encode,
                RequestSyncPacket::decode,
                RequestSyncPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER));
        CHANNEL.registerMessage(id(), SyncQuestsPacket.class,
                SyncQuestsPacket::encode,
                SyncQuestsPacket::decode,
                SyncQuestsPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT));
    }
}



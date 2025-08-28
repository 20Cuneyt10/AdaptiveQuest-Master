package com.cucun1q.adaptivequests;

import com.cucun1q.adaptivequests.client.Keybinds;
import com.cucun1q.adaptivequests.command.AQCommand;
import com.cucun1q.adaptivequests.config.Config;
import com.cucun1q.adaptivequests.network.NetworkHandler;
import com.cucun1q.adaptivequests.quest.QuestManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.ItemCraftedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod("adaptivequests")
public class AdaptiveQuestsMod {
    public AdaptiveQuestsMod() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::doClient);
        net.minecraftforge.fml.ModLoadingContext.get().registerConfig(net.minecraftforge.fml.config.ModConfig.Type.COMMON, Config.COMMON_SPEC);
        NetworkHandler.register();
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void doClient(final FMLClientSetupEvent event) {
        Keybinds.register();
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        AQCommand.register(event.getDispatcher());
    }

    @SubscribeEvent
    public void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event.getPlayer() instanceof net.minecraft.server.level.ServerPlayer) {
            QuestManager.onBlockMined(
                    (net.minecraft.server.level.ServerPlayer) event.getPlayer(),
                    event.getState().getBlock()
            );
        }
    }

    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof net.minecraft.server.level.ServerPlayer) {
            net.minecraft.server.level.ServerPlayer sp = (net.minecraft.server.level.ServerPlayer) event.getEntity();
            QuestManager.seedOnLogin(sp);
            com.cucun1q.adaptivequests.network.NetworkSender.sendFullSync(sp);
        }
    }

    @SubscribeEvent
    public void onCrafted(ItemCraftedEvent event) {
        if (event.getEntity() instanceof net.minecraft.server.level.ServerPlayer) {
            if (event.getCrafting().getItem() == net.minecraft.world.item.Items.STONE_PICKAXE) {
                net.minecraft.server.level.ServerPlayer sp = (net.minecraft.server.level.ServerPlayer) event.getEntity();
                QuestManager.unlockMiningQuests(sp);
                com.cucun1q.adaptivequests.network.NetworkSender.sendFullSync(sp);
            }
        }
    }
}

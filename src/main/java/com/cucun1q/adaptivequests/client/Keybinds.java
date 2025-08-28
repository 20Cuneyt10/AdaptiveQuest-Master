package com.cucun1q.adaptivequests.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class Keybinds {
    public static final String CATEGORY = "key.categories.adaptivequests";
    public static KeyMapping openQuests;

    public static void register() {}

    @Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ModBusHandlers {
        @SubscribeEvent
        public static void onRegisterKeys(RegisterKeyMappingsEvent event) {
            int defaultKey = com.cucun1q.adaptivequests.config.Config.defaultKeybind.get();
            openQuests = new KeyMapping("key.adaptivequests.open", defaultKey, CATEGORY);
            event.register(openQuests);
        }
    }

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        if (openQuests != null && openQuests.isDown()) {
            Minecraft.getInstance().setScreen(new QuestsScreen());
        }
    }
}



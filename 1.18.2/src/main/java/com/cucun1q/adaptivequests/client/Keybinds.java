package com.cucun1q.adaptivequests.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class Keybinds {
    public static final String CATEGORY = "key.categories.adaptivequests";
    public static KeyMapping openQuests;

    public static void register() {
        int defaultKey = com.cucun1q.adaptivequests.config.Config.defaultKeybind.get();
        openQuests = new KeyMapping("key.adaptivequests.open", defaultKey, CATEGORY);
        net.minecraftforge.client.ClientRegistry.registerKeyBinding(openQuests);
    }

    @SubscribeEvent
    public static void onKeyInput(InputEvent.KeyInputEvent event) {
        if (openQuests != null && openQuests.isDown()) {
            Minecraft.getInstance().setScreen(new QuestsScreen());
        }
    }
}



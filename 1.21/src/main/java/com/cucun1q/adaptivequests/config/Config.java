package com.cucun1q.adaptivequests.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class Config {
    public static final String CAT_THRESHOLDS = "thresholds";
    public static ForgeConfigSpec COMMON_SPEC;

    public static ForgeConfigSpec.IntValue coalThreshold;
    public static ForgeConfigSpec.IntValue ironThreshold;
    public static ForgeConfigSpec.IntValue goldThreshold;
    public static ForgeConfigSpec.IntValue redstoneThreshold;
    public static ForgeConfigSpec.IntValue lapisThreshold;
    public static ForgeConfigSpec.IntValue diamondThreshold;
    public static ForgeConfigSpec.IntValue emeraldThreshold;
    public static ForgeConfigSpec.IntValue quartzThreshold;
    public static ForgeConfigSpec.IntValue netherGoldThreshold;
    public static ForgeConfigSpec.IntValue debrisThreshold;
    public static ForgeConfigSpec.IntValue logsThreshold;

    public static ForgeConfigSpec.IntValue defaultKeybind;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.push(CAT_THRESHOLDS);
        coalThreshold = builder.comment("Blocks of coal ore to mine before adding a coal quest")
                .defineInRange("coal", 100, 1, 10000);
        ironThreshold = builder.defineInRange("iron", 50, 1, 10000);
        goldThreshold = builder.defineInRange("gold", 40, 1, 10000);
        redstoneThreshold = builder.defineInRange("redstone", 75, 1, 10000);
        lapisThreshold = builder.defineInRange("lapis", 40, 1, 10000);
        diamondThreshold = builder.defineInRange("diamond", 10, 1, 10000);
        emeraldThreshold = builder.defineInRange("emerald", 8, 1, 10000);
        quartzThreshold = builder.defineInRange("quartz", 60, 1, 10000);
        netherGoldThreshold = builder.defineInRange("netherGold", 40, 1, 10000);
        debrisThreshold = builder.defineInRange("ancientDebris", 5, 1, 10000);
        logsThreshold = builder.comment("Total logs to mine before adding a lumberjack quest")
                .defineInRange("logs", 64, 1, 10000);
        builder.pop();

        builder.push("client");
        defaultKeybind = builder.comment("Default GLFW key code for opening the quests screen (Y=89)")
                .defineInRange("defaultKeybind", 89, 0, 512);
        builder.pop();

        COMMON_SPEC = builder.build();
    }
}



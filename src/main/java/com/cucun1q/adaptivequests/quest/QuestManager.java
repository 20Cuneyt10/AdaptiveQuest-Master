package com.cucun1q.adaptivequests.quest;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.tags.BlockTags;
import net.minecraft.network.chat.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class QuestManager {
    public static class PlayerProgress {
        public final Map<Block, Integer> blockMineCounts = new HashMap<>();
        public final List<QuestModels.Quest> quests = new ArrayList<>();
        public boolean seededStarter = false;
        public boolean unlockedMining = false;
        public boolean spawnedWoodTypeLevel1 = false;
        public final Map<String, Integer> completionsByLine = new HashMap<>();
        // Prevent duplicate reward grants for the same quest level due to double clicks/latency
        public final Map<String, Integer> lastClaimedLevelById = new HashMap<>();
    }

    private static final Map<UUID, PlayerProgress> progressByPlayer = new ConcurrentHashMap<>();

    private static final Map<Block, java.util.function.Supplier<Integer>> oreThresholds = new HashMap<>();

    static {
        oreThresholds.put(Blocks.COAL_ORE, () -> com.cucun1q.adaptivequests.config.Config.coalThreshold.get());
        oreThresholds.put(Blocks.IRON_ORE, () -> com.cucun1q.adaptivequests.config.Config.ironThreshold.get());
        oreThresholds.put(Blocks.GOLD_ORE, () -> com.cucun1q.adaptivequests.config.Config.goldThreshold.get());
        oreThresholds.put(Blocks.REDSTONE_ORE, () -> com.cucun1q.adaptivequests.config.Config.redstoneThreshold.get());
        oreThresholds.put(Blocks.LAPIS_ORE, () -> com.cucun1q.adaptivequests.config.Config.lapisThreshold.get());
        oreThresholds.put(Blocks.DIAMOND_ORE, () -> com.cucun1q.adaptivequests.config.Config.diamondThreshold.get());
        oreThresholds.put(Blocks.EMERALD_ORE, () -> com.cucun1q.adaptivequests.config.Config.emeraldThreshold.get());
        oreThresholds.put(Blocks.NETHER_QUARTZ_ORE, () -> com.cucun1q.adaptivequests.config.Config.quartzThreshold.get());
        oreThresholds.put(Blocks.NETHER_GOLD_ORE, () -> com.cucun1q.adaptivequests.config.Config.netherGoldThreshold.get());
        oreThresholds.put(Blocks.ANCIENT_DEBRIS, () -> com.cucun1q.adaptivequests.config.Config.debrisThreshold.get());
    }

    public static PlayerProgress getProgress(UUID uuid) {
        return progressByPlayer.computeIfAbsent(uuid, k -> new PlayerProgress());
    }

    public static void onBlockMined(ServerPlayer player, Block block) {
        PlayerProgress progress = getProgress(player.getUUID());
        int newCount = progress.blockMineCounts.getOrDefault(block, 0) + 1;
        progress.blockMineCounts.put(block, newCount);

        // Update tracked quests
        for (QuestModels.Quest q : progress.quests) {
            if (q.completed) continue;
            if (q.category == QuestModels.Category.LOGGING) {
                if (q.trackedBlock != null && q.trackedBlock == block) {
                    q.progress++;
                } else if (q.trackedBlock == null && block instanceof RotatedPillarBlock && block.defaultBlockState().is(BlockTags.LOGS)) {
                    q.progress++;
                }
            } else if (q.category == QuestModels.Category.MINING) {
                if (q.trackedBlock != null && q.trackedBlock == block) {
                    q.progress++;
                }
            }
            if (!q.completed && q.progress >= q.target) {
                q.completed = true;
                player.sendSystemMessage(Component.literal("Quest completed: " + q.title + " (Level " + q.level + ")"));
                com.cucun1q.adaptivequests.network.NetworkSender.sendFullSync(player);
            }
        }

        if (block instanceof RotatedPillarBlock && block.defaultBlockState().is(BlockTags.LOGS)) {
            if (!progress.spawnedWoodTypeLevel1) {
                spawnWoodTypeLevel1(progress);
                progress.spawnedWoodTypeLevel1 = true;
            }
        }
    }

    public static List<String> listQuests(UUID playerId) {
        List<String> lines = new ArrayList<>();
        for (QuestModels.Quest q : getProgress(playerId).quests) {
            String state = q.completed ? (q.claimed ? "[CLAIMED]" : "[COMPLETE]") : (q.progress + "/" + q.target);
            lines.add("[" + q.category + "] " + q.title + " L" + q.level + " - " + state);
        }
        return lines;
    }

    public static String previewReward(QuestModels.Quest q) {
        if (q.category == QuestModels.Category.LOGGING) {
            if (q.level == 1) return "Reward: +2 XP levels";
            if (q.level == 2) return "Reward: +2 XP levels";
            return "Reward: " + Math.max(1, q.level - 1) + "x Iron Ingot";
        }
        if (q.category == QuestModels.Category.MINING) {
            if (q.trackedBlock == Blocks.DIAMOND_ORE) {
                return "Reward: +" + Math.max(1, q.level) + " XP levels";
            }
            if (q.level <= 2) {
                return "Reward: +" + (q.level == 1 ? 2 : 4) + " XP levels";
            }
            net.minecraft.world.item.Item up = mapUpMaterial(q.trackedBlock);
            String name = new net.minecraft.world.item.ItemStack(up).getHoverName().getString();
            return "Reward: " + Math.max(1, q.level) + "x " + name;
        }
        return "";
    }

    // First-login seeding
    public static void seedOnLogin(ServerPlayer player) {
        PlayerProgress p = getProgress(player.getUUID());
        if (p.seededStarter) return;
        // Starter: 8 logs (any) and 8 ores (iron & coal) to get going
        p.quests.add(new QuestModels.Quest("logging_starter_any", QuestModels.Category.LOGGING, "Gather logs", null, 8, 1));
        p.quests.add(new QuestModels.Quest("mining_starter_iron", QuestModels.Category.MINING, "Mine iron ore", Blocks.IRON_ORE, 8, 1));
        p.quests.add(new QuestModels.Quest("mining_starter_coal", QuestModels.Category.MINING, "Mine coal ore", Blocks.COAL_ORE, 8, 1));
        p.seededStarter = true;
    }

    // Unlock when crafting stone pickaxe
    public static void unlockMiningQuests(ServerPlayer player) {
        PlayerProgress p = getProgress(player.getUUID());
        if (p.unlockedMining) return;
        p.unlockedMining = true;
        // Add level-1 for common ores
        addOrEnsureQuest(p, "ore_iron_L1", QuestModels.Category.MINING, "Mine Iron Ore", Blocks.IRON_ORE, 8, 1);
        addOrEnsureQuest(p, "ore_coal_L1", QuestModels.Category.MINING, "Mine Coal Ore", Blocks.COAL_ORE, 8, 1);
        addOrEnsureQuest(p, "ore_gold_L1", QuestModels.Category.MINING, "Mine Gold Ore", Blocks.GOLD_ORE, 8, 1);
        addOrEnsureQuest(p, "ore_redstone_L1", QuestModels.Category.MINING, "Mine Redstone Ore", Blocks.REDSTONE_ORE, 8, 1);
        addOrEnsureQuest(p, "ore_lapis_L1", QuestModels.Category.MINING, "Mine Lapis Ore", Blocks.LAPIS_ORE, 8, 1);
        addOrEnsureQuest(p, "ore_diamond_L1", QuestModels.Category.MINING, "Mine Diamond Ore", Blocks.DIAMOND_ORE, 8, 1);
        addOrEnsureQuest(p, "ore_emerald_L1", QuestModels.Category.MINING, "Mine Emerald Ore", Blocks.EMERALD_ORE, 8, 1);
        addOrEnsureQuest(p, "ore_quartz_L1", QuestModels.Category.MINING, "Mine Nether Quartz Ore", Blocks.NETHER_QUARTZ_ORE, 8, 1);
        addOrEnsureQuest(p, "ore_nether_gold_L1", QuestModels.Category.MINING, "Mine Nether Gold Ore", Blocks.NETHER_GOLD_ORE, 8, 1);
    }

    private static void spawnWoodTypeLevel1(PlayerProgress p) {
        addOrEnsureQuest(p, "log_oak_L1", QuestModels.Category.LOGGING, "Break Oak Logs", Blocks.OAK_LOG, 8, 1);
        addOrEnsureQuest(p, "log_birch_L1", QuestModels.Category.LOGGING, "Break Birch Logs", Blocks.BIRCH_LOG, 8, 1);
        addOrEnsureQuest(p, "log_spruce_L1", QuestModels.Category.LOGGING, "Break Spruce Logs", Blocks.SPRUCE_LOG, 8, 1);
        addOrEnsureQuest(p, "log_jungle_L1", QuestModels.Category.LOGGING, "Break Jungle Logs", Blocks.JUNGLE_LOG, 8, 1);
        addOrEnsureQuest(p, "log_acacia_L1", QuestModels.Category.LOGGING, "Break Acacia Logs", Blocks.ACACIA_LOG, 8, 1);
        addOrEnsureQuest(p, "log_dark_oak_L1", QuestModels.Category.LOGGING, "Break Dark Oak Logs", Blocks.DARK_OAK_LOG, 8, 1);
        addOrEnsureQuest(p, "log_crimson_L1", QuestModels.Category.LOGGING, "Break Crimson Stems", Blocks.CRIMSON_STEM, 8, 1);
        addOrEnsureQuest(p, "log_warped_L1", QuestModels.Category.LOGGING, "Break Warped Stems", Blocks.WARPED_STEM, 8, 1);
    }

    private static void addOrEnsureQuest(PlayerProgress p, String id, QuestModels.Category cat, String title, Block tracked, int target, int level) {
        for (QuestModels.Quest q : p.quests) if (q.id.equals(id)) return;
        p.quests.add(new QuestModels.Quest(id, cat, title, tracked, target, level));
    }

    public static boolean claim(ServerPlayer player, String id) {
        PlayerProgress p = getProgress(player.getUUID());
        for (QuestModels.Quest q : p.quests) {
            if (!q.id.equals(id)) continue;
            if (!q.completed || q.claimed) return false;
            Integer last = p.lastClaimedLevelById.get(id);
            if (last != null && last.intValue() >= q.level) {
                return false; // already claimed this tier
            }
            grantReward(player, q);
            q.claimed = true;
            p.lastClaimedLevelById.put(id, q.level);
            // Progression: increase target and level, reset progress, mark not completed
            int times = p.completionsByLine.getOrDefault(id, 0) + 1;
            p.completionsByLine.put(id, times);
            int inc = times <= 3 ? 10 : 20;
            q.level += 1;
            q.target = q.target + inc;
            q.progress = 0;
            q.completed = false;
            q.claimed = false;
            player.sendSystemMessage(Component.literal("Next tier unlocked: " + q.title + " (L" + q.level + ") target " + q.target));
            com.cucun1q.adaptivequests.network.NetworkSender.sendFullSync(player);
            return true;
        }
        return false;
    }

    private static void grantReward(ServerPlayer player, QuestModels.Quest q) {
        if (q.category == QuestModels.Category.LOGGING) {
            if (q.level == 1) {
                player.giveExperienceLevels(2);
            } else if (q.level == 2) {
                player.giveExperienceLevels(2);
            } else {
                int count = Math.max(1, q.level - 1); // L3->2 iron, L4->3 ...
                player.addItem(new ItemStack(Items.IRON_INGOT, count));
            }
            return;
        }
        if (q.category == QuestModels.Category.MINING) {
            if (q.trackedBlock == Blocks.DIAMOND_ORE) {
                // Diamond: XP equal to level (e.g., L2->2, L4->4)
                player.giveExperienceLevels(Math.max(1, q.level));
                return;
            }
            if (q.level <= 2) {
                int xpLevels = q.level == 1 ? 2 : 4;
                player.giveExperienceLevels(xpLevels);
                return;
            }
            // Level >=3: give mapped higher-tier material equal to level
            ItemStack reward = new ItemStack(mapUpMaterial(q.trackedBlock), Math.max(1, q.level));
            player.addItem(reward);
        }
    }

    private static net.minecraft.world.item.Item mapUpMaterial(Block ore) {
        if (ore == Blocks.COAL_ORE) return Items.GOLD_INGOT;
        if (ore == Blocks.IRON_ORE) return Items.DIAMOND;
        if (ore == Blocks.GOLD_ORE || ore == Blocks.NETHER_GOLD_ORE) return Items.EMERALD;
        if (ore == Blocks.REDSTONE_ORE) return Items.QUARTZ;
        if (ore == Blocks.LAPIS_ORE) return Items.QUARTZ;
        if (ore == Blocks.EMERALD_ORE) return Items.NETHERITE_SCRAP;
        if (ore == Blocks.NETHER_QUARTZ_ORE) return Items.GOLD_INGOT;
        return Items.IRON_INGOT;
    }
}



package com.cucun1q.adaptivequests.quest;

import net.minecraft.world.level.block.Block;

public class QuestModels {
    public enum Category {
        LOGGING,
        MINING
    }

    public static class Quest {
        public final String id;
        public final Category category;
        public final String title;
        public final Block trackedBlock; // can be null for category-wide
        public int target;
        public int progress;
        public int level;
        public boolean completed;
        public boolean claimed;

        public Quest(String id, Category category, String title, Block trackedBlock, int target, int level) {
            this.id = id;
            this.category = category;
            this.title = title;
            this.trackedBlock = trackedBlock;
            this.target = target;
            this.level = level;
            this.progress = 0;
            this.completed = false;
            this.claimed = false;
        }
    }
}



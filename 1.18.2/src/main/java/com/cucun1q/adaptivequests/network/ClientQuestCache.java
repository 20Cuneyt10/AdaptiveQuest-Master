package com.cucun1q.adaptivequests.network;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ClientQuestCache {
    private static final List<SyncQuestsPacket.QuestDTO> QUESTS = new CopyOnWriteArrayList<>();
    private static final List<SyncQuestsPacket.StatDTO> BREAKDOWN = new CopyOnWriteArrayList<>();
    private static volatile int COAL = 0;
    private static volatile int IRON = 0;
    private static volatile int LOGS = 0;

    public static void set(List<SyncQuestsPacket.QuestDTO> quests) {
        QUESTS.clear();
        QUESTS.addAll(quests);
    }

    public static List<SyncQuestsPacket.QuestDTO> get() {
        return Collections.unmodifiableList(QUESTS);
    }

    public static void setStats(int coal, int iron, int logs) { COAL = coal; IRON = iron; LOGS = logs; }
    public static int coal() { return COAL; }
    public static int iron() { return IRON; }
    public static int logs() { return LOGS; }
    public static void setBreakdown(List<SyncQuestsPacket.StatDTO> stats) { BREAKDOWN.clear(); BREAKDOWN.addAll(stats); }
    public static List<SyncQuestsPacket.StatDTO> breakdown() { return Collections.unmodifiableList(BREAKDOWN); }
}



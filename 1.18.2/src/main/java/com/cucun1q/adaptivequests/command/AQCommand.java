package com.cucun1q.adaptivequests.command;

import com.cucun1q.adaptivequests.quest.QuestManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TextComponent;

public class AQCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("aq")
                .executes(ctx -> {
                    if (ctx.getSource().getEntity() != null && ctx.getSource().getEntity().getUUID() != null) {
                        QuestManager.listQuests(ctx.getSource().getEntity().getUUID()).forEach(q ->
                                ctx.getSource().sendSuccess(new TextComponent(q), false)
                        );
                    } else {
                        ctx.getSource().sendSuccess(new TextComponent("No player context."), false);
                    }
                    return 1;
                })
                .then(
                    Commands.literal("claim")
                        .then(
                            Commands.argument("id", StringArgumentType.string())
                                .executes(ctx -> {
                                    if (ctx.getSource().getEntity() instanceof net.minecraft.server.level.ServerPlayer) {
                                        String id = StringArgumentType.getString(ctx, "id");
                                        boolean ok = QuestManager.claim((net.minecraft.server.level.ServerPlayer) ctx.getSource().getEntity(), id);
                                        ctx.getSource().sendSuccess(new TextComponent(ok ? "Claimed" : "Cannot claim"), false);
                                    }
                                    return 1;
                                })
                        )
                )
        );
    }
}



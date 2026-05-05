package net.mcreator.sscresearchanddevelopment.component.subcomponents;

import net.minecraft.client.gui.components.Button;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

public class ArtefactNodeWidget extends Button {
    private final ArtefactNode node;
    private final BlockPos blockPos;

    public ArtefactNodeWidget(int x, int y, int w, int h, ArtefactNode node, BlockPos pos, OnPress onPress) {
        super(x, y, w, h, Component.literal(node.triggerId()), onPress, DEFAULT_NARRATION);
        this.node = node;
        this.blockPos = pos;
    }
}
package net.mcreator.sscresearchanddevelopment.interfaces;

import net.mcreator.sscresearchanddevelopment.enums.ToolType;

public interface ITool {
    ToolType getToolType();

    default float getWorkSpeedMultiplier() {
        return 1.0f;
    }
}

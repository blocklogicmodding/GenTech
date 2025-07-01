package com.blocklogic.gentech.client;

import com.blocklogic.gentech.GenTech;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;

public class GTKeybinds {
    public static final String CATEGORY = "key.categories." + GenTech.MODID;

    public static final KeyMapping TOGGLE_TANK_MODE = new KeyMapping(
            "key." + GenTech.MODID + ".toggle_tank_mode",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_G,
            CATEGORY
    );
}
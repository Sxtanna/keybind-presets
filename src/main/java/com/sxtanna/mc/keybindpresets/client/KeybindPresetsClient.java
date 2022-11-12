package com.sxtanna.mc.keybindpresets.client;

import org.jetbrains.annotations.NotNull;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Environment(EnvType.CLIENT)
public class KeybindPresetsClient implements ClientModInitializer {

    private static final Logger LOGGER = LoggerFactory.getLogger("keybind-presets");

    public static @NotNull Logger log() {
        return LOGGER;
    }


    @Override
    public void onInitializeClient() {

    }

}

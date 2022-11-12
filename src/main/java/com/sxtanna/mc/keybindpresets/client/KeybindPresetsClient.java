package com.sxtanna.mc.keybindpresets.client;

import org.jetbrains.annotations.NotNull;

import com.sxtanna.mc.keybindpresets.client.helper.ScreenHelper;
import com.sxtanna.mc.keybindpresets.client.screen.KeybindPresetsScreen;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.KeybindsScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Environment(EnvType.CLIENT)
public class KeybindPresetsClient implements ClientModInitializer {

    private static final String KEYBINDS_SCREEN_EXPECTED_DONE_BUTTON_TRANSLATION_KEY  = "gui.done";
    private static final String KEYBINDS_SCREEN_EXPECTED_RESET_BUTTON_TRANSLATION_KEY = "controls.resetAll";


    private static final Logger LOGGER = LoggerFactory.getLogger("keybind-presets");

    public static @NotNull Logger log() {
        return LOGGER;
    }


    @Override
    public void onInitializeClient() {
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (screen instanceof KeybindsScreen) {
                initializePresetsButton(screen);
            }
        });
    }


    private static void initializePresetsButton(@NotNull final Screen screen) {
        final List<ClickableWidget> buttons = Screens.getButtons(screen);

        final var done  = ScreenHelper.findOrNull(buttons, KEYBINDS_SCREEN_EXPECTED_DONE_BUTTON_TRANSLATION_KEY);
        final var reset = ScreenHelper.findOrNull(buttons, KEYBINDS_SCREEN_EXPECTED_RESET_BUTTON_TRANSLATION_KEY);
        if (done == null || reset == null) {
            return;
        }

        // ratio for the 3 buttons
        final var screenWidthRatio = screen.width / 3;

        // space between each button
        final var buttonsSpace = 10;
        // width of each button (-20 for the 2 spaces)
        final var buttonsWidth = Math.min(150, screenWidthRatio - 20);
        // where the first button should be placed, roughly 3 column grid aligns them
        final var buttonsStart = ((screen.width - ((buttonsWidth * 3) + 20)) / 2);

        // resize the done and reset buttons to accommodate a 3rd button on the screen
        done.setWidth(buttonsWidth);
        reset.setWidth(buttonsWidth);


        // move the reset button over to accommodate the presets button
        reset.x = buttonsStart;


        final var presetsButtonX = reset.x + reset.getWidth() + buttonsSpace;
        final var presetsButtonY = reset.y;

        final var presetsButtonWidth  = buttonsWidth;
        final var presetsButtonHeight = reset.getHeight();

        final var presetsButtonTitle = Text.translatable("gui.keybind-presets.presets.button");

        final var presets = new ButtonWidget(presetsButtonX,
                                             presetsButtonY,

                                             presetsButtonWidth,
                                             presetsButtonHeight,

                                             presetsButtonTitle,

                                             // when the button is clicked, open the presets screen
                                             button -> MinecraftClient.getInstance().setScreen(new KeybindPresetsScreen(screen)));

        // add the presets button after the reset button
        buttons.add(buttons.indexOf(reset) + 1, presets);

        // move the done button over to accommodate the presets button
        done.x = presets.x + presets.getWidth() + buttonsSpace;
    }

}

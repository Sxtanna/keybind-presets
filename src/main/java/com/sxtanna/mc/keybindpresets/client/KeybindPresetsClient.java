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
import oshi.annotation.concurrent.Immutable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@Environment(EnvType.CLIENT)
public class KeybindPresetsClient implements ClientModInitializer {

    public static final String  PRESET_FILE_EXTENSION   = "presets";
    public static final Pattern INVALID_FILE_CHARACTERS = Pattern.compile("[\\\\/:*?\"<>|]");


    private static final String KEYBINDS_SCREEN_EXPECTED_DONE_BUTTON_TRANSLATION_KEY  = "gui.done";
    private static final String KEYBINDS_SCREEN_EXPECTED_RESET_BUTTON_TRANSLATION_KEY = "controls.resetAll";


    private static final Logger LOGGER = LoggerFactory.getLogger("keybind-presets");

    public static @NotNull Logger log() {
        return LOGGER;
    }


    private static Path keybindPresetsDirectory;

    public static @NotNull Optional<Path> keybindPresetsDirectory() {
        return Optional.ofNullable(keybindPresetsDirectory);
    }

    public static @NotNull @Immutable Collection<Path> keybindPresetPaths() {
        try {
            final var path = keybindPresetsDirectory();
            if (path.isEmpty()) {
                return Collections.emptyList();
            }

            try (final var files = Files.list(path.get())
                                        // only regular files (not symlink or directory)
                                        .filter(Files::isRegularFile)
                                        // only files that have the "presets" extension
                                        .filter(it -> it.getFileName().toString().endsWith("." + PRESET_FILE_EXTENSION))) {
                return files.toList();
            }
        } catch (final Exception ex) {
            log().error("could not list files in presets directory", ex);
            return Collections.emptyList();
        }
    }


    @Override
    public void onInitializeClient() {
        keybindPresetsDirectory = MinecraftClient.getInstance().runDirectory.toPath().resolve("keybind_presets");

        if (Files.notExists(keybindPresetsDirectory)) {
            try {
                Files.createDirectories(keybindPresetsDirectory);
            } catch (final IOException ex) {
                log().error("could not create keybind presets directory", ex);
                return;
            }
        }

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

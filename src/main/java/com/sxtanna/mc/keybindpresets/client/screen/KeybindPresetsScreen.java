package com.sxtanna.mc.keybindpresets.client.screen;

import org.jetbrains.annotations.NotNull;

import com.sxtanna.mc.keybindpresets.client.KeybindPresetsClient;
import com.sxtanna.mc.keybindpresets.client.screen.widget.PresetSelectionListWidget;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.GameOptionsScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;

public final class KeybindPresetsScreen extends GameOptionsScreen {

    private static final int BUTTON_WIDTH = 200;
    private static final int BUTTON_HEIGHT = 20;


    private PresetSelectionListWidget presetSelectionListWidget;


    public KeybindPresetsScreen(@NotNull final Screen parent) {
        super(parent,
              MinecraftClient.getInstance().options,
              Text.translatable("gui.keybind-presets.presets.title"));
    }


    @Override
    protected void init() {
        presetSelectionListWidget = new PresetSelectionListWidget(MinecraftClient.getInstance(),
                                                                  this,
                                                                  this.textRenderer,
                                                                  KeybindPresetsClient.keybindPresetPaths());


        final var center = this.width / 2;


        // add the presets list widget
        addSelectableChild(presetSelectionListWidget);

        // add the done button at the bottom of the screen for returning
        addDrawableChild(new ButtonWidget(center - 100,
                                          this.height - 29,

                                          BUTTON_WIDTH,
                                          BUTTON_HEIGHT,

                                          ScreenTexts.DONE, // built in "Done" translation key

                                          // when the button is clicked, return to the parent screen
                                          button -> MinecraftClient.getInstance().setScreen(this.parent)));


        super.init();
    }


    @Override
    public void render(final MatrixStack matrices, final int mouseX, final int mouseY, final float delta) {
        final var center = this.width / 2;

        // render a fresh background
        renderBackground(matrices);

        // render the presets widget
        presetSelectionListWidget.render(matrices, mouseX, mouseY, delta);

        // render the title of the screen at the top
        drawCenteredText(matrices, this.textRenderer, this.title, center, 8, 0xffffff);

        super.render(matrices, mouseX, mouseY, delta);
    }
}

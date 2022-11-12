package com.sxtanna.mc.keybindpresets.client.screen;

import org.jetbrains.annotations.NotNull;

import com.sxtanna.mc.keybindpresets.client.KeybindPresetsClient;
import com.sxtanna.mc.keybindpresets.client.screen.widget.PresetSelectionListWidget;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.GameOptionsScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;

import java.nio.file.Files;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class KeybindPresetsScreen extends GameOptionsScreen {

    private static final String SPLITTER = "::";


    private static final Text NEW_PRESET_NAME_TITLE = Text.translatable("gui.keybind-presets.presets.new-preset-name");


    private static final int BUTTON_WIDTH   = 200;
    private static final int BUTTON_HEIGHT  = 20;
    private static final int BUTTON_SPACING = 5;
    private static final int BOTTOM_OFFSET  = 29;


    private PresetSelectionListWidget presetSelectionListWidget;

    private ButtonWidget    savePresetButton;
    private ButtonWidget    loadPresetButton;
    
    private TextFieldWidget newPresetNameField;


    public KeybindPresetsScreen(@NotNull final Screen parent) {
        super(parent,
              MinecraftClient.getInstance().options,
              Text.translatable("gui.keybind-presets.presets.title"));
    }


    public @NotNull Optional<PresetSelectionListWidget> getPresetSelectionListWidget() {
        return Optional.ofNullable(presetSelectionListWidget);
    }

    public @NotNull Optional<ButtonWidget> getSavePresetButton() {
        return Optional.ofNullable(savePresetButton);
    }

    public @NotNull Optional<ButtonWidget> getLoadPresetButton() {
        return Optional.ofNullable(loadPresetButton);
    }

    public @NotNull Optional<TextFieldWidget> getNewPresetNameField() {
        return Optional.ofNullable(newPresetNameField);
    }


    @Override
    protected void init() {
        presetSelectionListWidget = new PresetSelectionListWidget(MinecraftClient.getInstance(),
                                                                  this,
                                                                  this.textRenderer,
                                                                  KeybindPresetsClient.keybindPresetPaths());


        final var center              = this.width / 2;
        final var centeredButtonStart = center - (BUTTON_WIDTH / 2);


        savePresetButton = new ButtonWidget(centeredButtonStart,
                                            (this.height - BOTTOM_OFFSET) - BUTTON_HEIGHT - BUTTON_SPACING,

                                            BUTTON_WIDTH,
                                            BUTTON_HEIGHT,

                                            Text.translatable("gui.keybind-presets.presets.save-button"),
                                            this::savePreset);
        savePresetButton.active = false;


        newPresetNameField = new TextFieldWidget(this.textRenderer,

                                                 centeredButtonStart,
                                                 savePresetButton.y - BUTTON_HEIGHT - BUTTON_SPACING,

                                                 BUTTON_WIDTH,
                                                 BUTTON_HEIGHT,

                                                 Text.translatable("gui.keybind-presets.presets.new-preset-name"));

        newPresetNameField.setTextPredicate(KeybindPresetsClient.INVALID_FILE_CHARACTERS.asPredicate().negate());
        newPresetNameField.setChangedListener(presetName -> savePresetButton.active = !presetName.isBlank());


        loadPresetButton = new ButtonWidget(centeredButtonStart,
                                            presetSelectionListWidget.getHeight() + BUTTON_SPACING,

                                            BUTTON_WIDTH,
                                            BUTTON_HEIGHT,

                                            Text.translatable("gui.keybind-presets.presets.load-button"),
                                            this::loadPreset);
        loadPresetButton.active = false;


        // add the presets list widget
        addSelectableChild(presetSelectionListWidget);

        addDrawableChild(savePresetButton);
        addDrawableChild(loadPresetButton);

        addDrawableChild(newPresetNameField);

        // add the done button at the bottom of the screen for returning
        addDrawableChild(new ButtonWidget(centeredButtonStart,
                                          this.height - BOTTOM_OFFSET,

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

        // draw the header for the new preset name text field
        drawCenteredText(matrices, this.textRenderer, NEW_PRESET_NAME_TITLE, center, newPresetNameField.y - (BUTTON_HEIGHT / 2) - 2, 0xffa0a0a0);

        super.render(matrices, mouseX, mouseY, delta);
    }


    private void savePreset(@NotNull final ButtonWidget button) {
        final var root = KeybindPresetsClient.keybindPresetsDirectory().orElse(null);
        if (root == null) {
            return;
        }

        final var name = Optional.ofNullable(newPresetNameField.getText())
                                 // remove spaces from head and tail
                                 .map(String::trim)
                                 // remove invalid file name characters (handled by predicate, but still...)
                                 .map(text -> KeybindPresetsClient.INVALID_FILE_CHARACTERS.matcher(text).replaceAll(""))
                                 // ensure resulting value isn't blank
                                 .filter(text -> !text.isBlank())
                                 .orElse(null);
        if (name == null) {
            return;
        }

        final var file = root.resolve(name + "." + KeybindPresetsClient.PRESET_FILE_EXTENSION);
        if (Files.exists(file)) {
            return; // could remove this if we want to allow them to overwrite??
        }


        // gather current keybinds
        final var currentKeybinds = Stream.of(MinecraftClient.getInstance().options.allKeys)
                                          .collect(Collectors.toMap(KeyBinding::getTranslationKey,
                                                                    KeyBinding::getBoundKeyTranslationKey));

        final var text = currentKeybinds.entrySet()
                                        .stream()
                                        .map(kb -> kb.getKey() + SPLITTER + kb.getValue())
                                        .collect(Collectors.joining("\n"));

        try {
            Files.writeString(file, text);
        } catch (final Exception ex) {
            KeybindPresetsClient.log().error("could not save keybind preset to file", ex);
            return;
        }

        MinecraftClient.getInstance().setScreen(this.parent);
    }

    private void loadPreset(@NotNull final ButtonWidget button) {
        final var selected = presetSelectionListWidget.getSelectedOrNull();
        if (selected == null) {
            return;
        }

        final String text;
        try {
            text = Files.readString(selected.getPath());
        } catch (final Exception ex) {
            KeybindPresetsClient.log().error("could not read saved keybinds from file", ex);
            return;
        }

        final var savedKeybinds = Stream.of(text.split("\n"))
                                        .map(line -> line.split(SPLITTER))
                                        .collect(Collectors.toMap(ar -> ar[0],
                                                                  ar -> InputUtil.fromTranslationKey(ar[1])));

        for (final var binding : MinecraftClient.getInstance().options.allKeys) {
            final var saved = savedKeybinds.get(binding.getTranslationKey());
            if (saved != null) {
                binding.setBoundKey(saved);
            }
        }

        MinecraftClient.getInstance().setScreen(this.parent);
    }

}

package com.sxtanna.mc.keybindpresets.client.screen.widget;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.sxtanna.mc.keybindpresets.client.screen.KeybindPresetsScreen;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;

import java.nio.file.Path;
import java.util.Collection;

public final class PresetSelectionListWidget extends AlwaysSelectedEntryListWidget<PresetSelectionListWidget.PresetEntry> {

    private static final int TOP_OFFSET   = 30;
    private static final int ENTRY_HEIGHT = 18;


    @NotNull
    private final TextRenderer         textRenderer;
    @NotNull
    private final KeybindPresetsScreen presetsScreen;


    private final int rowWidth;

    public PresetSelectionListWidget(@NotNull final MinecraftClient client,
                                     @NotNull final KeybindPresetsScreen presetsScreen,
                                     @NotNull final TextRenderer textRenderer,
                                     @NotNull final Collection<Path> paths) {
        super(client,

              presetsScreen.width /*- (presetsScreen.width / 3)*/,
              presetsScreen.height,

              TOP_OFFSET,
              presetsScreen.height - (presetsScreen.height / 2),

              ENTRY_HEIGHT);

        this.textRenderer = textRenderer;
        this.presetsScreen = presetsScreen;

        // move the left side of the widget to center it within the screen
        // setLeftPos((presetsScreen.width - width) / 2);

        // convert all paths into entries
        paths.stream()
             .map(PresetEntry::new)
             .forEach(this::addEntry);

        // find the largest width entry, and pad it with 20
        this.rowWidth = children().stream()
                                  .mapToInt(e -> e.textWidth)
                                  .map(i -> i + 20)
                                  .max()
                                  .orElse(100);
    }


    public int getHeight() {
        return this.bottom;
    }


    @Override
    public int getRowWidth() {
        return rowWidth;
    }

    private void selectPreset(@Nullable final PresetEntry entry) {
        setSelected(entry);
    }


    public final class PresetEntry extends AlwaysSelectedEntryListWidget.Entry<PresetEntry> {

        @NotNull
        private final Path path;

        @NotNull
        private final Text text;

        @NotNull
        private final OrderedText orderedText;
        private final int         textWidth;

        public PresetEntry(@NotNull final Path path) {
            this.path = path;

            var name = path.getFileName().toString();

            final var extIndex = name.lastIndexOf('.');
            if (extIndex != -1) {
                name = name.substring(0, extIndex);
            }

            this.text = Text.of(name);

            this.orderedText = text.asOrderedText();
            this.textWidth = textRenderer.getWidth(orderedText);
        }


        public @NotNull Path getPath() {
            return this.path;
        }

        @Override
        public boolean mouseClicked(final double mouseX,
                                    final double mouseY,
                                    final int button) {
            if (button != 0) {
                return false;
            }

            if (PresetSelectionListWidget.this.getSelectedOrNull() == this) {
                selectPreset(null);
            } else {
                selectPreset(this);
            }

            return true;

        }

        @Override
        public Text getNarration() {
            return Text.empty();
        }

        @Override
        public void render(final MatrixStack matrices,

                           final int index,

                           final int y,
                           final int x,

                           final int entryWidth,
                           final int entryHeight,

                           final int mouseX,
                           final int mouseY,

                           final boolean hovered,
                           final float tickDelta) {
            textRenderer.drawWithShadow(matrices,
                                        orderedText,
                                        (presetsScreen.width / 2f) - (textWidth / 2f),
                                        y + 3,
                                        0xffffff);
        }

    }

}

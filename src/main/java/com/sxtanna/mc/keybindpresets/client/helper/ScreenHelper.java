package com.sxtanna.mc.keybindpresets.client.helper;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.TranslatableTextContent;

import java.util.Collection;
import java.util.Optional;

public enum ScreenHelper {
    ;

    /**
     * Finds the instance of {@link ClickableWidget} that is a {@link TranslatableTextContent}
     * and has a translation key matching the passed value.
     *
     * @return An {@link Optional} containing the widget if found, empty otherwise.
     */
    public static @NotNull Optional<ClickableWidget> find(@NotNull final Collection<ClickableWidget> widgets,
                                                          @NotNull final String translationKey) {
        return widgets.stream()
                      .filter(widget -> widget.getMessage().getContent() instanceof TranslatableTextContent ttc && ttc.getKey().equals(translationKey))
                      .findFirst();
    }

    /**
     * Finds the instance of {@link ClickableWidget} that is a {@link TranslatableTextContent}
     * and has a translation key matching the passed value.
     *
     * @return The widget if found, null otherwise.
     */
    public static @Nullable ClickableWidget findOrNull(@NotNull final Collection<ClickableWidget> widgets,
                                                       @NotNull final String translationKey) {
        return find(widgets, translationKey).orElse(null);
    }

}

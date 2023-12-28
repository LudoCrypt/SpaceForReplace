package net.ludocrypt.spaceforreplace.mixin;

import java.lang.reflect.Field;
import java.util.concurrent.CompletableFuture;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import com.mojang.brigadier.suggestion.Suggestions;

import net.minecraft.client.gui.screen.ChatInputSuggestor;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;

@Mixin(TextFieldWidget.class)
public class TextFieldWidgetMixin {

	@SuppressWarnings("unchecked")
	@ModifyVariable(method = "write", at = @At("HEAD"), ordinal = 0)
	private String spaceforreplace$write(String in) {

		if (in.equals(" ")) {

			try {

				if (this.getClass().getEnclosingClass() == ChatScreen.class) {

					ChatInputSuggestor suggestor = null;

					for (Field f : this.getClass().getEnclosingClass().getDeclaredFields()) {

						if (f.getType() == ChatInputSuggestor.class) {
							f.setAccessible(true);
							suggestor = (ChatInputSuggestor) f.get(this.getClass().getDeclaredFields()[0].get(this));
						}

					}

					if (suggestor != null) {
						suggestor.refresh();

						CompletableFuture<Suggestions> suggestions = null;

						for (Field f : suggestor.getClass().getDeclaredFields()) {

							if (f.getType() == CompletableFuture.class) {
								f.setAccessible(true);
								suggestions = (CompletableFuture<Suggestions>) f.get(suggestor);
							}

						}

						if (suggestions != null) {

							if (suggestions.get() != null) {
								String curText = ((TextFieldWidget) (Object) this).getText() + in;

								if (!suggestions.get().getList().isEmpty()) {

									String firstSuggestion = suggestions.get().getList().get(0).getText();

									if ((suggestions.get().getList().size() == 1 && !curText
										.endsWith(firstSuggestion)) || suggestions.get().getList().size() > 1) {

										if (firstSuggestion.matches("[a-z0-9_]*:[a-z0-9_]*")) {

											String id = firstSuggestion.split(":")[1];

											if (curText.substring(0, curText.length() - 1).endsWith(id)) {
												((TextFieldWidget) (Object) this)
													.setText(curText
														.substring(0, curText.length() - id.length() - 1) + firstSuggestion);
												return in;
											}

											return "_";

										}

									}

								}

							}

						}

					}

				}

			} catch (Exception e) {
				e.printStackTrace();
			}

		}

		return in;
	}

}

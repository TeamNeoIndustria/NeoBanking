package xyz.neonetwork.neobanking.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import xyz.neonetwork.neolib.textures.NeoTexture;

public class ScreenTest extends Screen {

	private final String title;
	private final int imageWidth;
	private final int imageHeight;

	private int offsetX;
	private int offsetY;

	public EditBox editbox;

	public ScreenTest(Component title) {
		super(title);
		this.title = title.toString();
		this.imageWidth = 320;
		this.imageHeight = 192;
	}

	public static void drawNineSlice(GuiGraphics gui, ResourceLocation texture, int segmentRes, int startX, int startY, int width, int height) {
		int tileXQuantity = (int) Math.ceil((double) width / segmentRes);
		int tileYQuantity = (int) Math.ceil((double) height / segmentRes);

		if (tileXQuantity < 2 || tileYQuantity < 2) return;

		int maxTileIndexX = tileXQuantity - 1;
		int maxTileIndexY = tileYQuantity - 1;

		int textureResolution = segmentRes * 3;

		int[] topleftUVOffset = new int[]{ 0, 0 }; // x, y
		int[] topUVOffset = new int[]{ segmentRes, 0 };
		int[] toprightUVOffset = new int[]{ segmentRes * 2, 0 };
		int[] leftUVOffset = new int[]{ 0, segmentRes };
		int[] middleUVOffset = new int[]{ segmentRes, segmentRes };
		int[] rightUVOffset = new int[]{ segmentRes * 2, segmentRes };
		int[] bottomleftUVOffset = new int[]{ 0, segmentRes * 2 };
		int[] bottomUVOffset = new int[]{ segmentRes, segmentRes * 2 };
		int[] bottomrightUVOffset = new int[]{ segmentRes * 2, segmentRes * 2 };

		for (int tileIndexY = 0; tileIndexY <= maxTileIndexY; tileIndexY++) {
			for (int tileIndexX = 0; tileIndexX <= maxTileIndexX; tileIndexX++) {
				int offsetX = tileIndexX * segmentRes;
				int offsetY = tileIndexY * segmentRes;
				if (tileIndexY == 0) {
					if (tileIndexX == 0) {
						gui.blit(texture, startX + offsetX, startY + offsetY, topleftUVOffset[0], topleftUVOffset[1], segmentRes, segmentRes, textureResolution, textureResolution);
					} else if (tileIndexX == maxTileIndexX) {
						gui.blit(texture, startX + offsetX, startY + offsetY, toprightUVOffset[0], toprightUVOffset[1], segmentRes, segmentRes, textureResolution, textureResolution);
					} else {
						gui.blit(texture, startX + offsetX, startY + offsetY, topUVOffset[0], topUVOffset[1], segmentRes, segmentRes, textureResolution, textureResolution);
					}
				} else if (tileIndexY == maxTileIndexY) {
					if (tileIndexX == 0) {
						gui.blit(texture, startX + offsetX, startY + offsetY, bottomleftUVOffset[0], bottomleftUVOffset[1], segmentRes, segmentRes, textureResolution, textureResolution);
					} else if (tileIndexX == maxTileIndexX) {
						gui.blit(texture, startX + offsetX, startY + offsetY, bottomrightUVOffset[0], bottomrightUVOffset[1], segmentRes, segmentRes, textureResolution, textureResolution);
					} else {
						gui.blit(texture, startX + offsetX, startY + offsetY, bottomUVOffset[0], bottomUVOffset[1], segmentRes, segmentRes, textureResolution, textureResolution);
					}
				} else {
					if (tileIndexX == 0) {
						gui.blit(texture, startX + offsetX, startY + offsetY, leftUVOffset[0], leftUVOffset[1], segmentRes, segmentRes, textureResolution, textureResolution);
					} else if (tileIndexX == maxTileIndexX) {
						gui.blit(texture, startX + offsetX, startY + offsetY, rightUVOffset[0], rightUVOffset[1], segmentRes, segmentRes, textureResolution, textureResolution);
					} else {
						gui.blit(texture, startX + offsetX, startY + offsetY, middleUVOffset[0], middleUVOffset[1], segmentRes, segmentRes, textureResolution, textureResolution);
					}
				}
			}
		}
	}

	public static void drawTitle(GuiGraphics gui, ResourceLocation texture, int segmentRes, int xPadding, int yPadding, Font font, String title, int startX, int startY, int width) {

		String truncatedTitle = font.plainSubstrByWidth(title, width - 48);
		if (!title.equals(truncatedTitle)) truncatedTitle = truncatedTitle + "...";
		int tileXQuantity = (int) Math.ceil((double) font.width(truncatedTitle) / 2) + 2; // +2?

		int tileOffsetX = startX + ((width / 2) - tileXQuantity);
		int tileOffsetY = startY - yPadding;

		gui.blit(texture, tileOffsetX - xPadding, tileOffsetY, 0, 0, xPadding, segmentRes, segmentRes, segmentRes);
		for (int tileIndexX = 0; tileIndexX < tileXQuantity; tileIndexX++) {
			gui.blit(texture, tileOffsetX + (tileIndexX * 2), tileOffsetY, xPadding, 0, 2, segmentRes, segmentRes, segmentRes);
		}
		gui.blit(texture, tileOffsetX + (tileXQuantity * 2), tileOffsetY, 16-xPadding, 0, xPadding, segmentRes, segmentRes, segmentRes);

		gui.drawCenteredString(font, truncatedTitle, startX + (width / 2), startY, 0xFFFFFF);
	}


	@Override
	public void init() {
		super.init();
		clearWidgets();

		this.offsetX = (width - imageWidth) / 2;
		this.offsetY = (height - imageHeight) / 2;

		editbox = new EditBox(this.font, offsetX, offsetY, Component.literal("Textbox"));
		editbox.setMaxLength(16);
		editbox.setBordered(true);
		editbox.setX(offsetX + 16);
		editbox.setY(offsetY + 16);
		editbox.setWidth(64);
		editbox.setHeight(16);
		addWidget(editbox);
	}

	@Override
	public void render(@NotNull GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
		this.renderBackground(gui, mouseX, mouseY, partialTick);
		if (this != minecraft.screen) return;

		drawNineSlice(gui, NeoTexture.GENERIC.BACKGROUND_MAIN_BODY, 16, offsetX, offsetY, imageWidth, imageHeight);
		drawTitle(gui, NeoTexture.GENERIC.BACKGROUND_TITLE_BOX, 16, 4, 4, this.font, "This is a stupidly long title for a ui element in this rediuclous game", offsetX, offsetY, imageWidth);

		editbox.render(gui, mouseX, mouseY, partialTick);
		if (editbox.getValue().isBlank() && editbox.isFocused()) {
			gui.drawString(font, editbox.getMessage(), editbox.getX() + 3, editbox.getY() + 3, 0xff4A2D31, false);
		}
	}
}

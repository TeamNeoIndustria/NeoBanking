package xyz.neonetwork.neobanking.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ItemWidget {
	private ItemStack itemStack;
	float x;
	float y;
	float scale;

	public ItemWidget(@NotNull Item item, float centerX, float centerY, float scale) {
		this.itemStack = new ItemStack(item, 1);
		this.x = centerX;
		this.y = centerY;
		this.scale = scale;
	}

	public void setX(float x) {
		this.x = x;
	}
	public void setY(float y) {
		this.y = y;
	}
	public void setScale(float scale) {
		this.scale = scale;
	}

	public void render(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
		gui.pose().pushPose();
		gui.pose().translate(this.x, this.y, 0f);
		gui.pose().scale(scale, scale, scale);
		gui.renderItem(this.itemStack, -8, -8);
		gui.pose().popPose();
//		gui.renderItem(this.itemStack, (int) this.x - 8, (int) this.y - 8);
	}
}

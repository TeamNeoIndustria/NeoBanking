package xyz.neonetwork.neobanking.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import xyz.neonetwork.neobanking.NeoBanking;

import java.util.*;

public class ScreenGrid {

	private final int columnWidth;
	private final int rowHeight;
	private final int padding;
	private final int columns;
	private final int rows;

	private Boolean[] occupiedSpaces;
	private Map<String, ElementType> elementTypes = new HashMap<>();
	private Map<String, ScreenGridCoordinate> grid = new HashMap<>();
	private Map<String, EditBox> editBoxWidgets =  new HashMap<>();
	private Map<String, StringWidget> stringWidgets =  new HashMap<>();
	private Map<String, Button.Builder> buttonWidgets =  new HashMap<>();
	private Map<String, OnPress> buttonCallbacks = new HashMap<>();
	private Map<String, ItemWidget> itemWidgets =  new HashMap<>();

	public ScreenGrid(int columnWidth, int rowHeight, int padding, int columns, int rows) {
		this.columnWidth = Math.max(columnWidth, 10);
		this.rowHeight = Math.max(rowHeight, 10);
		this.padding = Math.max(padding, 0);
		this.columns = Math.max(columns, 1);
		this.rows = Math.max(rows, 1);
		occupiedSpaces = new Boolean[this.columns * this.rows];
		Arrays.fill(occupiedSpaces, false);
		//Should height be calculated automatically? It could cause very strange stretching if any height is permitted
	}

	private boolean nameExists(String name) {
		if (grid.containsKey(name)) {
			NeoBanking.LOGGER.warn("ScreenGrid#nameExists: Duplicate name: {}", name);
			return true;
		}
		return false;
	}

	private int coordinateToOffset(int x, int y) {
		if (x < 0 || y < 0 || x > this.columns || y > this.rows) {
			NeoBanking.LOGGER.warn("ScreenGrid#coordinateToOffset: Invalid column/row out of bounds X:{}, Y:{}", x, y);
			return -1;
		}
		int offset = this.columns * y;
		offset += x;
		return offset;
	}

	private boolean spaceOccupied(int x, int y) {
		int offset = coordinateToOffset(x, y);
		if (offset == -1) return true;
		return occupiedSpaces[offset];
	}

	private boolean spaceOccupied(int x, int y, int colspan, int rowspan) {
		if (x + colspan > this.columns || y + rowspan > this.rows) {
			NeoBanking.LOGGER.warn("ScreenGrid#spaceOccupied: Invalid column/row out of bounds X:{}, Y:{}", x, y);
		}

		for (int currentX = x; currentX < x + colspan; currentX++) {
			for (int currentY = y; currentY < y + rowspan; currentY++) {
				if (spaceOccupied(currentX, currentY)) return true;
			}
		}
		return false;
	}

	private boolean setOccupied(int x, int y, int colspan, int rowspan, String name, ElementType elementType) {
		if (spaceOccupied(x, y, colspan, rowspan)) {
			NeoBanking.LOGGER.warn("ScreenGrid#setOccupied: Grid space already occupied for '{}'", name);
			return false;
		}
		for (int currentX = x; currentX < x + colspan; currentX++) {
			for (int currentY = y; currentY < y + rowspan; currentY++) {
				occupiedSpaces[coordinateToOffset(currentX, currentY)] = true;
			}
		}
		grid.put(name, new ScreenGridCoordinate(x, y, colspan, rowspan));
		elementTypes.put(name, elementType);
		return true;
	}

	public ScreenGrid addEditBoxWidget(int column, int row, int colspan, int rowspan, String name, Component placeholder, int maxLength) {
		if (this.nameExists(name)) return this;
		if (!setOccupied(column, row, colspan, rowspan, name, ElementType.EDIT_BOX)) return this;

		EditBox editBox = new EditBox(Minecraft.getInstance().font, 0, 0, placeholder);
		editBox.setMaxLength(maxLength);
		this.editBoxWidgets.put(name, editBox);
		return this;
	}

	public ScreenGrid addStringWidget(int column, int row, int colspan, int rowspan, String name, Component label) {
		if (this.nameExists(name)) return this;
		if (!setOccupied(column, row, colspan, rowspan, name, ElementType.STRING)) return this;

		StringWidget stringWidget = new StringWidget(0, 0, 0, 0, label, Minecraft.getInstance().font);
		stringWidget.alignLeft();
		this.stringWidgets.put(name, stringWidget);
		return this;
	}

	public ScreenGrid addButtonWidget(int column, int row, int colspan, int rowspan, String name, Component label, Component tooltip, OnPress onPressCallback) {
		if (this.nameExists(name)) return this;
		if (!setOccupied(column, row, colspan, rowspan, name, ElementType.BUTTON)) return this;

		Button.Builder button = Button.builder(label, (onPress) -> {
			onPressCallback.onPress(this, onPress);
		});
		if (tooltip != null) button.tooltip(Tooltip.create(tooltip));
		this.buttonCallbacks.put(name, onPressCallback);
		this.buttonWidgets.put(name, button);
		return this;
	}

	public ScreenGrid addItemWidget(int column, int row, int colspan, int rowspan, String name, Item item) {
		if (this.nameExists(name)) return this;
		if (!setOccupied(column, row, colspan, rowspan, name, ElementType.ITEM)) return this;

		ItemWidget itemWidget = new ItemWidget(item, 0, 0, 0);
		this.itemWidgets.put(name, itemWidget);
		return this;
	}

	public EditBox getEditBoxWidget(String name, int offsetX, int offsetY) {
		ScreenGridCoordinate coordinate = this.calculateOffsets(name, offsetX, offsetY);
		EditBox editBox = this.editBoxWidgets.get(name);
		if (coordinate == null || editBox == null) return null;
		editBox.setX(coordinate.x);
		editBox.setY(coordinate.y);
		editBox.setWidth(coordinate.width);
		editBox.setHeight(coordinate.height);
		return editBox;
	}

	public StringWidget getStringWidget(String name, int offsetX, int offsetY) {
		ScreenGridCoordinate coordinate = this.calculateOffsets(name, offsetX, offsetY);
		StringWidget stringWidget = this.stringWidgets.get(name);
		if (coordinate == null || stringWidget == null) return null;
		stringWidget.setX(coordinate.x);
		stringWidget.setY(coordinate.y);
		stringWidget.setWidth(coordinate.width);
		stringWidget.setHeight(coordinate.height);
		return stringWidget;
	}

	public Button getButtonWidget(String name, int offsetX, int offsetY) {
		ScreenGridCoordinate coordinate = this.calculateOffsets(name, offsetX, offsetY);
		Button.Builder button = this.buttonWidgets.get(name);
		if (coordinate == null || button == null) return null;
		button.bounds(coordinate.x, coordinate.y, coordinate.width, coordinate.height);
		return button.build();
	}

	public ItemWidget getItemWidget(String name, int offsetX, int offsetY) {
		ScreenGridCoordinate coordinate = this.calculateOffsets(name, offsetX, offsetY);
		ItemWidget itemWidget = this.itemWidgets.get(name);
		if (coordinate == null || itemWidget == null) return null;
		itemWidget.setX(coordinate.x + (coordinate.width / 2f));
		itemWidget.setY(coordinate.y + (coordinate.height / 2f));
		int minDimension = Math.min(coordinate.width, coordinate.height);
		itemWidget.setScale(minDimension / 16f);
		return itemWidget;
	}

	public String getEditBoxValue(String name) {
		EditBox editBox = this.editBoxWidgets.get(name);
		if (editBox == null) return null;
		return editBox.getValue();
	}

	public ElementType getElementType(String name) {
		return this.elementTypes.get(name);
	}

	public String[] elementNames() {
		return grid.keySet().toArray(new String[0]);
	}

	public ScreenGridCoordinate calculateOffsets(String name, int offsetX, int offsetY) {
		ScreenGridCoordinate coordinate = grid.get(name);
		if (coordinate == null) return null;

		return new ScreenGridCoordinate(
		offsetX + coordinate.x * (this.columnWidth + this.padding),
		offsetY + coordinate.y * (this.rowHeight + this.padding),
		this.columnWidth + (coordinate.width - 1) * (this.columnWidth + this.padding),
		this.rowHeight + (coordinate.height - 1) * (this.rowHeight + this.padding)
		);
	}

	public ScreenGridCoordinate getBoundingBox(int offsetX, int offsetY) {
		return new ScreenGridCoordinate(offsetX, offsetY, ((columnWidth + padding) * columns) - padding, ((rowHeight + padding) * rows) - padding);
	}

	@OnlyIn(Dist.CLIENT)
	public interface OnPress {
		void onPress(ScreenGrid grid, Button button);
	}
}

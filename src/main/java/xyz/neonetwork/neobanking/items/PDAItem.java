package xyz.neonetwork.neobanking.items;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import xyz.neonetwork.neobanking.NeoBanking;
import xyz.neonetwork.neobanking.gui.pda.PDAMenu;

public class PDAItem extends Item implements MenuProvider {
	public PDAItem(Properties properties) {
		super(properties);
	}

	@Override
	public Component getName(ItemStack stack) {
		return Component.translatable(this.getDescriptionId(stack));
	}

	@Override
	public Component getDisplayName() {
		return null;
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		if (!level.isClientSide()) {
			ServerPlayer serverPlayer = (ServerPlayer) player;
			serverPlayer.openMenu(new SimpleMenuProvider((containerId, playerInventory, playerEntity) -> new PDAMenu(containerId, playerInventory, playerEntity, this), Component.literal("PDA Menu")));
		}
		return InteractionResultHolder.success(player.getItemInHand(hand));
	}

	@Override
	public AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
		return new PDAMenu(i, inventory, player, this);
	}
}

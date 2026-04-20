package xyz.neonetwork.neobanking.blockitems;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import xyz.neonetwork.neobanking.server.PDAGUI;

public class PDAItem extends Item {
	public PDAItem(Properties properties) {
		super(properties);
	}

	@Override
	public @NotNull Component getName(@NotNull ItemStack stack) {
		return Component.translatable(this.getDescriptionId(stack));
	}

	@OnlyIn(Dist.DEDICATED_SERVER)
	@Override
	public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, Player player, @NotNull InteractionHand hand) {
		ServerPlayer serverPlayer = (ServerPlayer) player;
		PDAGUI.showHomePage(serverPlayer);
		return InteractionResultHolder.success(player.getItemInHand(hand));
	}
}

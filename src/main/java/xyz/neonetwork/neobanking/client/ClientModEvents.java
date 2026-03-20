package xyz.neonetwork.neobanking.client;

import net.createmod.catnip.gui.ScreenOpener;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.HandlerThread;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import xyz.neonetwork.neobanking.gui.ModMenuTypes;
import xyz.neonetwork.neobanking.gui.pda.PDAScreen;
import xyz.neonetwork.neobanking.packets.IRSClientboundPacket;
import xyz.neonetwork.neobanking.packets.IRSServerboundPacket;
import xyz.neonetwork.neobanking.packets.IRSToastPacket;

public class ClientModEvents {
	@SubscribeEvent
	public static void register(RegisterPayloadHandlersEvent event) {
		final PayloadRegistrar registrar = event.registrar("1");
		registrar.playToClient(
			IRSToastPacket.TYPE,
			IRSToastPacket.STREAM_CODEC,
			ClientPayloadHandler::handleDataOnMain
		);

		registrar.executesOn(HandlerThread.NETWORK);
		registrar.playToClient(
			IRSClientboundPacket.TYPE,
			IRSClientboundPacket.STREAM_CODEC,
			ClientPayloadHandler::handleDataOnMain
		);
		registrar.playToServer(
			IRSServerboundPacket.TYPE,
			IRSServerboundPacket.STREAM_CODEC,
			ClientPayloadHandler::handleDataOnMain
		);
	}

	@SubscribeEvent
	public static void registerScreens(RegisterMenuScreensEvent event) {
		event.register(ModMenuTypes.PDA_MENU.get(), PDAScreen::new);
	}
}

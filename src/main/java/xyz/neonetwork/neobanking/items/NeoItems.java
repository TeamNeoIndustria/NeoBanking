package xyz.neonetwork.neobanking.items;

import com.tterrag.registrate.util.entry.ItemEntry;
import xyz.neonetwork.neobanking.NeoBanking;

import java.util.HashMap;

public class NeoItems {
	public static final HashMap<String, ItemEntry<?>> NEO_SYSTEM_ITEMS = new HashMap<>() {{
		put("base_coin", NeoBanking.REGISTRATE.item("base_coin", prop -> new CoinItem(0, prop)).register());
	}};

	public static final HashMap<String, ItemEntry<?>> NEO_COINS = new HashMap<>() {{
		put("copper_coin", NeoBanking.REGISTRATE.item("copper_coin", prop -> new CoinItem(1, prop)).register());
		put("iron_coin", NeoBanking.REGISTRATE.item("iron_coin", prop -> new CoinItem(5, prop)).register());
		put("zinc_coin", NeoBanking.REGISTRATE.item("zinc_coin", prop -> new CoinItem(10, prop)).register());
		put("gold_coin", NeoBanking.REGISTRATE.item("gold_coin", prop -> new CoinItem(25, prop)).register());
		put("netherite_coin", NeoBanking.REGISTRATE.item("netherite_coin", prop -> new CoinItem(50, prop)).register());
	}};

//	public static final HashMap<String, ItemEntry<?>> NEO_ITEMS = new HashMap<>() {{
//		put("pda", NeoBanking.REGISTRATE.item("pda", PDAItem::new).register());
//	}};

	public static void register() {}
}

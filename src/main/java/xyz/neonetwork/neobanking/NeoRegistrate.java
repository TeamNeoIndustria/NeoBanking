package xyz.neonetwork.neobanking;

import com.tterrag.registrate.AbstractRegistrate;

public class NeoRegistrate extends AbstractRegistrate<NeoRegistrate> {
	protected NeoRegistrate(String modid) {
		super(modid);
	}

	public static NeoRegistrate create(String modid) {
		return new NeoRegistrate(modid);
	}
}

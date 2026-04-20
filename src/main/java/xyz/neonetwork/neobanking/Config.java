package xyz.neonetwork.neobanking;

import net.neoforged.neoforge.common.ModConfigSpec;

// An example config class. This is not required, but it's a good idea to have one to keep your config organized.
// Demonstrates how to use Neo's config APIs
public class Config {
	private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

	public static final ModConfigSpec.ConfigValue<String> IRS_WEB_ENDPOINT = BUILDER
		.comment("URI for IRS web api")
		.define("IRSWebEndpoint", "");

	public static final ModConfigSpec.ConfigValue<String> IRS_WEB_APIKEY = BUILDER
		.comment("API key for IRS web api")
		.define("IRSWebApiKey", "");

	public static final ModConfigSpec.ConfigValue<String> IRS_SOCKET_ENDPOINT = BUILDER
		.comment("URI for IRS websocket api")
		.define("IRSSocketEndpoint", "");

	public static final ModConfigSpec.ConfigValue<String> IRS_SOCKET_APIKEY = BUILDER
		.comment("API key for IRS websocket api")
		.define("IRSSocketApiKey", "");


	static final ModConfigSpec SPEC = BUILDER.build();
}

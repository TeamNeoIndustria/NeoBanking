package xyz.neonetwork.neobanking;

import java.util.UUID;

public class Utilities {
	public static UUID UUIDFromString(String uuid) {
		String strippedUUID = uuid.toLowerCase().replace("-", "");
		if (strippedUUID.length() != 32) return null;
		for (char c : strippedUUID.toCharArray()) {
			if (!(c >= '0' && c <= '9') && !(c >= 'a' && c <= 'f') && !(c >= 'A' && c <= 'F')) return null;
		}
		try {
			return UUID.fromString(strippedUUID.replaceFirst("(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)", "$1-$2-$3-$4-$5"));
		} catch (IllegalArgumentException e) {
			return null;
		}
	}
}

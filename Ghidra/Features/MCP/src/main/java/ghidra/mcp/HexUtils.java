package ghidra.mcp;

public class HexUtils {
	public static byte[] hexStringToByteArray(String s) {
		int len = s.length();
		if (len % 2 != 0) throw new IllegalArgumentException("Hex string must have even length");
		byte[] data = new byte[len / 2];
		for (int i = 0; i < len; i += 2) {
			int d1 = Character.digit(s.charAt(i), 16);
			int d2 = Character.digit(s.charAt(i+1), 16);
			if (d1 == -1 || d2 == -1) throw new IllegalArgumentException("Invalid hex character");
			data[i / 2] = (byte) ((d1 << 4) + d2);
		}
		return data;
	}
}

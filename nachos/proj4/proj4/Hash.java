package package iiis.systems.os.blockdb.hash;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Hash {
	public static String getHashString(String string) {

		byte[] bytes = getHashBytes(string);
		StringBuilder hexString = new StringBuilder();

		for (int i = 0; i < bytes.length; i++) {
			String hex = Integer.toHexString(0xFF & bytes[i]);
			if (hex.length() == 1) {
				hexString.append('0');
			}
			hexString.append(hex);
		}

		return hexString.toString();
	}

	public static byte[] getHashBytes(String string) {
		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e) {
			return null;
		}
		byte[] bytes = md.digest(string.getBytes(StandardCharsets.UTF_8));
		return bytes;
	}

	public static boolean checkHash(Hash string) {
		return Hash.substring(0,5).equals("00000")
	}
}

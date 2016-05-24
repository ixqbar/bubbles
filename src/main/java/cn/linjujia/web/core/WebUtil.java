package cn.linjujia.web.core;

import java.io.ByteArrayOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Random;
import java.util.zip.CRC32;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebUtil {
	
	private final static Logger JZLogger = LoggerFactory.getLogger(WebUtil.class);
	
	private static final char[] DIGITS_LOWER = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

	public static int time() {
		return (int)(System.currentTimeMillis() / 1000);
	}
	
	public static int rand(int min, int max) {
		return new Random().nextInt(max) + min;
	}
	
	/**
	 * 
	 * @param src
	 * @return
	 * @throws NoSuchAlgorithmException
	 */
	public static String md5(byte[] src) {
		String result = "";
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(src);
			byte[] mdbytes = md.digest();
			int l = mdbytes.length;
			final char[] out = new char[l << 1];
			// two characters form the hex value.
			for (int i = 0, j = 0; i < l; i++) {
				out[j++] = DIGITS_LOWER[(0xF0 & mdbytes[i]) >>> 4];
				out[j++] = DIGITS_LOWER[0x0F & mdbytes[i]];
			}
			result = new String(out);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}

	public static String gzuncompress(byte[] src) {
		Inflater inflater = new Inflater();
		inflater.setInput(src);

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream(src.length);
		byte[] buffer = new byte[1024];
		try {
			while (!inflater.finished()) {
				int count = inflater.inflate(buffer);
				outputStream.write(buffer, 0, count);
			}
			outputStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return outputStream.toString();
	}

	public static byte[] gzcompress(byte[] src) {
		ByteArrayOutputStream byteArrayOutputStream = null;

		try {
			byteArrayOutputStream = new ByteArrayOutputStream(src.length);
			Deflater compressor = new Deflater();
			compressor.setLevel(Deflater.DEFAULT_COMPRESSION);
			compressor.setInput(src);
			compressor.finish();

			final byte[] buf = new byte[1024];
			while (!compressor.finished()) {
				int count = compressor.deflate(buf);
				byteArrayOutputStream.write(buf, 0, count);
			}
			compressor.end();

			byteArrayOutputStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return byteArrayOutputStream.toByteArray();
	}

	
	public static long getCRC32(byte[] src) {
		CRC32 crc32 = new CRC32();
		crc32.update(src);
		return crc32.getValue();
	}

	public static String decrypt(byte[] src, String key) {
		String result = null;
		
		if (key == null || key.length() != 16) {
			JZLogger.error("decrypt servcie key error");
			return result;
		}

		try {
			Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
			int blockSize = cipher.getBlockSize();

			int plaintextLength = src.length;
			if (plaintextLength % blockSize != 0) {
				plaintextLength = plaintextLength + (blockSize - (plaintextLength % blockSize));
			}

			byte[] plaintext = new byte[plaintextLength - 16];
			System.arraycopy(src, 16, plaintext, 0, src.length - 16);

			SecretKeySpec sKeySpec = new SecretKeySpec(key.getBytes(), "AES");
			IvParameterSpec ivSpec = new IvParameterSpec(src, 0, 16);

			cipher.init(Cipher.DECRYPT_MODE, sKeySpec, ivSpec);
			byte[] content = cipher.doFinal(plaintext);

			String[] header = (new String(content, 0, 32)).split(",", 4);
			if (!header[0].equals("ok")) {
				JZLogger.error("decrypt servcie header error {}", new String(content, 0, 32));
				return result;
			}

			if (header[1].equals("1")) {
				result = WebUtil.gzuncompress(Arrays.copyOfRange(content, 32, 32 + Integer.parseInt(header[2])));
			} else {
				result = new String(content, 32, Integer.parseInt(header[2]));
			}
			
			String crc32 = String.format("%d", WebUtil.getCRC32(result.getBytes()));
			if (!header[3].trim().equals(crc32)) {
				JZLogger.error("decrypt servcie crc32 failed %s %s", header[3].trim(), crc32);
				result = null;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}
	
	public static byte[] encrypt(byte[] src, String key) {
		byte[] result = null;
		
		if (key == null || key.length() != 16) {
			return result;
		}
		
		long crc32 = WebUtil.getCRC32(src);
		int isGzip = src.length > 500 ? 1 : 0;
		
		if (isGzip == 1) {
			src = WebUtil.gzcompress(src);
		}
		
		String header = String.format("ok,%d,%d,%d", isGzip, src.length, crc32);

		try {
			Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
			SecretKeySpec sKeySpec = new SecretKeySpec(key.getBytes(), "AES");
			cipher.init(Cipher.ENCRYPT_MODE, sKeySpec);
			
			IvParameterSpec ivSpec = cipher.getParameters().getParameterSpec(IvParameterSpec.class);
			byte[] iv = ivSpec.getIV();
			
			int blockSize = cipher.getBlockSize();

			int plaintextLength = 48 + src.length;
			if (plaintextLength % blockSize != 0) {
				plaintextLength = plaintextLength + (blockSize - (plaintextLength % blockSize));
			}

			byte[] plaintext = new byte[plaintextLength];
			
			System.arraycopy(iv, 0, plaintext, 0, 16);
			System.arraycopy(header.getBytes(), 0, plaintext, 16, header.getBytes().length);
			System.arraycopy(src, 0, plaintext, 48, src.length);
			
			result = cipher.doFinal(plaintext);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return result;
	}
	
}

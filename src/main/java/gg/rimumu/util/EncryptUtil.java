package gg.rimumu.util;

import gg.rimumu.common.RimumuProperties;
import gg.rimumu.exception.RimumuException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.Key;

@Component
public class EncryptUtil {
    @Autowired
    private static RimumuProperties rimumuKey ;

    private static final Logger LOGGER = LoggerFactory.getLogger(EncryptUtil.class);

    private static final String ALGORITHM = "AES";
    private static final Key KEY = getGenerateKey();


    public static String encrypt(String str) throws RimumuException.EncryptException {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, KEY);

            byte[] encryptBytes = cipher.doFinal(str.getBytes(StandardCharsets.UTF_8));
            return byteToHex(encryptBytes);

        } catch (Exception e) {
            LOGGER.error(str + " 암호화 에러");
            LOGGER.error(e.getMessage());
            throw new RimumuException.EncryptException(str);
        }
    }

    public static String decrypt(String str) throws RimumuException.EncryptException {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, KEY);
            byte[] decryptedBytes = cipher.doFinal(hexToBytes(str));
            return new String(decryptedBytes, StandardCharsets.UTF_8);

        } catch (Exception e) {
            LOGGER.error(str + " 복호화 에러");
            LOGGER.error(e.getMessage());
            throw new RimumuException.EncryptException(str);
        }
    }

    protected static Key getGenerateKey() {
        String key = rimumuKey.getEncrypt_key();
        byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
        return new SecretKeySpec(keyBytes, ALGORITHM);
    }

    private static String byteToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
        }
        return result.toString();
    }

    private static byte[] hexToBytes(String hexStr) {
        int len = hexStr.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hexStr.charAt(i), 16) << 4)
                    + Character.digit(hexStr.charAt(i + 1), 16));
        }
        return data;
    }
}

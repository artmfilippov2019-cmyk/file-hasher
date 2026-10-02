import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.bouncycastle.crypto.ExtendedDigest;
import org.bouncycastle.crypto.digests.Blake2bDigest;
import org.bouncycastle.crypto.digests.Blake2sDigest;
import org.bouncycastle.crypto.digests.SHAKEDigest;

public final class HashUtil {

    private static final int BUFFER_SIZE = 8192;
    public static final int DEFAULT_SHAKE256_BITS = 256;

    private HashUtil() {
    }

    public enum Algorithm {
        MD5("MD5"),
        SHA256("SHA-256"),
        BLAKE2B256("BLAKE2b-256"),
        BLAKE2S256("BLAKE2s-256"),
        SHAKE256("SHAKE-256");

        private final String displayName;

        Algorithm(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public static String computeHash(Algorithm algorithm, Path filePath, int shakeBits)
            throws IOException, NoSuchAlgorithmException {

        if (!Files.isRegularFile(filePath)) {
            throw new IllegalArgumentException("Файл не найден или не является обычным файлом: " + filePath);
        }

        switch (algorithm) {
            case MD5:
                return computeJdkDigest("MD5", filePath);
            case SHA256:
                return computeJdkDigest("SHA-256", filePath);
            case BLAKE2B256:
                return computeExtendedDigest(new Blake2bDigest(256), filePath);
            case BLAKE2S256:
                return computeExtendedDigest(new Blake2sDigest(256), filePath);
            case SHAKE256:
                if (shakeBits <= 0 || shakeBits % 8 != 0) {
                    throw new IllegalArgumentException(
                            "Длина вывода SHAKE-256 должна быть положительной и кратной 8, получено: " + shakeBits);
                }
                return computeShake256(filePath, shakeBits / 8);
            default:
                throw new IllegalArgumentException("Неизвестный алгоритм: " + algorithm);
        }
    }

    private static String computeJdkDigest(String jdkAlgorithmName, Path filePath)
            throws IOException, NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance(jdkAlgorithmName);
        try (InputStream in = Files.newInputStream(filePath)) {
            byte[] buffer = new byte[BUFFER_SIZE];
            int read;
            while ((read = in.read(buffer)) != -1) {
                digest.update(buffer, 0, read);
            }
        }
        return toHex(digest.digest());
    }

    private static String computeExtendedDigest(ExtendedDigest digest, Path filePath) throws IOException {
        try (InputStream in = Files.newInputStream(filePath)) {
            byte[] buffer = new byte[BUFFER_SIZE];
            int read;
            while ((read = in.read(buffer)) != -1) {
                digest.update(buffer, 0, read);
            }
        }
        byte[] out = new byte[digest.getDigestSize()];
        digest.doFinal(out, 0);
        return toHex(out);
    }

    private static String computeShake256(Path filePath, int outputBytes) throws IOException {
        SHAKEDigest digest = new SHAKEDigest(256);
        try (InputStream in = Files.newInputStream(filePath)) {
            byte[] buffer = new byte[BUFFER_SIZE];
            int read;
            while ((read = in.read(buffer)) != -1) {
                digest.update(buffer, 0, read);
            }
        }
        byte[] out = new byte[outputBytes];
        digest.doFinal(out, 0, outputBytes);
        return toHex(out);
    }

    private static String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(Character.forDigit((b >> 4) & 0xF, 16));
            sb.append(Character.forDigit(b & 0xF, 16));
        }
        return sb.toString();
    }

    public static Algorithm parseAlgorithmFlag(String flag) {
        switch (flag) {
            case "-md5":
                return Algorithm.MD5;
            case "-sha256":
                return Algorithm.SHA256;
            case "-blake2b256":
                return Algorithm.BLAKE2B256;
            case "-blake2s256":
                return Algorithm.BLAKE2S256;
            case "-shake256":
                return Algorithm.SHAKE256;
            default:
                return null;
        }
    }
}

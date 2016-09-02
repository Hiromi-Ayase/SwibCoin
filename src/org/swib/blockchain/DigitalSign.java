package org.swib.blockchain;
import java.util.Arrays;

/**
 * デジタル署名クラス
 * 
 * 本来はRSA等を用いるべきであるが、今回は簡単のため公開鍵から秘密鍵がすぐ導出できるロジックになっている。<br>
 * ただし、インターフェースは同じ(秘密鍵で署名したものは、対応する公開鍵でしか検証できない)
 */
public class DigitalSign {

    private static int KEY_LEN = 10;

    /**
     * オブジェクトの署名を作る
     * 
     * @param obj 署名するオブジェクト
     * @param privateKey 署名する秘密鍵
     * @return 署名
     */
    public static String sign(Object obj, String privateKey) {
        byte[] sign = sign(Common.serialize(obj), Common.decodeHex(privateKey));
        return Common.encodeHex(sign);
    }

    /**
     * オブジェクトの署名が指定した公開鍵の持ち主によってなされたかどうかを検証する
     * 
     * @param obj 検証されるオブジェクト
     * @param sign 署名
     * @param publicKey 公開鍵
     * @return True:はい False:いいえ
     */
    public static boolean validate(Object obj, String sign, String publicKey) {
        return validate(Common.serialize(obj), Common.decodeHex(sign), Common.decodeHex(publicKey));
    }

    /**
     * オブジェクトが指定したハッシュを持つか検証する。
     * 
     * @param obj 検証されるオブジェクト
     * @param hash ハッシュ
     * @return True:はい False:いいえ
     */
    public static boolean hashCheck(Object obj, String hash) {
        return Common.valid(obj, Common.decodeHex(hash));
    }

    /**
     * オブジェクトのハッシュを返す
     * 
     * @param obj オブジェクト
     * @return ハッシュ
     */
    public static String hash(Object obj) {
        return Common.encodeHex(Common.hash(obj));
    }

    /**
     * 秘密鍵と公開鍵を生成する
     * 
     * @return {公開鍵, 秘密鍵}
     */
    public static String[] generateKey() {
        byte[] publicKey = Common.getRand(KEY_LEN);
        String key1 = Common.encodeHex(publicKey);
        String key2 = Common.encodeHex(getPrivateKey(publicKey));
        return new String[] { key1, key2 };
    }

    private static byte[] sign(byte[] input, byte[] privateKey) {
        byte[] hash = Common.hash(input);
        int n = hash.length;
        byte[] encrypt = new byte[n];
        for (int i = 0; i < n; i++) {
            encrypt[i] = (byte) (hash[i] ^ privateKey[i % privateKey.length]);
        }

        return encrypt;
    }

    private static boolean validate(byte[] input, byte[] sign, byte[] publicKey) {
        byte[] privateKey = getPrivateKey(publicKey);
        byte[] s = sign(input, privateKey);

        return Arrays.equals(s, sign);
    }

    private static byte[] getPrivateKey(byte[] publicKey) {
        int n = publicKey.length;
        byte[] privateKey = new byte[n];

        for (int i = 0; i < n; i++) {
            privateKey[i] = (byte) (publicKey[i] ^ (i % 256));
        }
        return privateKey;
    }
}
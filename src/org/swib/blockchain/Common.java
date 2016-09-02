package org.swib.blockchain;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.xml.bind.DatatypeConverter;

/**
 * 共通クラス
 */
public class Common {

    /**
     * オブジェクトが指定されたハッシュ値を持つかどうか
     * 
     * @param obj オブジェクト
     * @param hash ハッシュ値
     * @return True:同じハッシュ値を持つ
     */
    public static boolean valid(Object obj, byte[] hash) {
        return Arrays.equals(hash(obj), hash);
    }

    /**
     * オブジェクトのハッシュ値
     * 
     * @param obj オブジェクト
     * @return ハッシュ値
     */
    public static byte[] hash(Object obj) {
        return hash(serialize(obj));
    }

    /**
     * バイナリのハッシュ値
     * 
     * @param input バイナリ
     * @return ハッシュ値
     */
    public static byte[] hash(byte[] input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return md.digest(input);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * ランダムバイナリを生成
     * 
     * @param n 桁数
     * @return ランダムバイナリ
     */
    public static byte[] getRand(int n) {
        byte[] ret = new byte[n];
        for (int i = 0; i < n; i++) {
            ret[i] = (byte) (Math.random() * 256);
        }
        return ret;
    }

    /**
     * オブジェクトのシリアライズ
     * 
     * @param obj オブジェクト
     * @return シリアライズされたバイナリ
     */
    public static byte[] serialize(Object obj) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos;
        try {
            oos = new ObjectOutputStream(baos);
            oos.writeObject(obj);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return baos.toByteArray();
    }

    /**
     * オブジェクトのデシリアライズ
     * 
     * @param buf シリアライズされたバイナリ
     * @return オブジェクト
     */
    public static Object deserialize(byte[] buf) {
        ByteArrayInputStream bais = new ByteArrayInputStream(buf);
        ObjectInputStream ois;
        Object obj = null;
        try {
            ois = new ObjectInputStream(bais);
            obj = ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return obj;
    }

    /**
     * バイナリを16進文字列に
     * 
     * @param b バイナリ
     * @return 16進文字列
     */
    public static String encodeHex(byte[] b) {
        return DatatypeConverter.printHexBinary(b);
    }

    /**
     * 16進文字列をバイナリに
     * 
     * @param str 16新文字列
     * @return バイナリ
     */
    public static byte[] decodeHex(String str) {
        return DatatypeConverter.parseHexBinary(str);
    }
}

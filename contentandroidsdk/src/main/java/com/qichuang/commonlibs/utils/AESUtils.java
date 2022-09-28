package com.qichuang.commonlibs.utils;


import android.text.TextUtils;
import android.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;


/**
 * author : yun
 * desc   : AES对称加密和解密，有偏移量
 */
public class AESUtils {

    // 编码
    private static final String ENCODING = "UTF-8";
    //算法
    private static final String ALGORITHM = "AES";
    // 默认的加密算法
    private static final String CIPHER_ALGORITHM = "AES/CBC/PKCS5Padding";

    public static String AES_KEY = "q1w_e~r4t!y6*7i8";
    public static String OFFSET_KEY = "q1w_e~r4t!y6*7i8";

    /**
     * 加密
     *
     * @param data
     * @return String
     * @author tyg
     * @date 2018年6月28日下午2:50:35
     */
    public static String encrypt(String data) {
        String result = "";

        if (TextUtils.isEmpty(data)) {
            return "";
        }

        try {
            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            SecretKeySpec skeySpec = new SecretKeySpec(AES_KEY.getBytes("ASCII"), ALGORITHM);
            IvParameterSpec iv = new IvParameterSpec(OFFSET_KEY.getBytes());//使用CBC模式，需要一个向量iv，可增加加密算法的强度
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);
            byte[] encrypted = cipher.doFinal(data.getBytes(ENCODING));
            result = Base64.encodeToString(encrypted, Base64.DEFAULT); //此处使用BASE64做转码。
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return result.replaceAll("\n|\t|\r","");
    }

    /**
     * 解密
     *
     * @param data
     * @return String
     * @author tyg
     * @date 2018年6月28日下午2:50:43
     */
    public static String decrypt(String data) {
        String result = "";
        try {
            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            SecretKeySpec skeySpec = new SecretKeySpec(AES_KEY.getBytes("ASCII"), ALGORITHM);
            IvParameterSpec iv = new IvParameterSpec(OFFSET_KEY.getBytes());//使用CBC模式，需要一个向量iv，可增加加密算法的强度
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
            byte[] buffer = Base64.decode(data, Base64.DEFAULT);
            byte[] encrypted = cipher.doFinal(buffer);
            result = new String(encrypted, ENCODING);//此处使用BASE64做转码。
        } catch (Exception e) {

        }
        return result;
    }
}

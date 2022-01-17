package com.debei.utils;

import android.text.TextUtils;

import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.SecretKeySpec;

/**
 * AES算法进行加密
 *
 * @author CodeGeek
 * @create 2018-01-23 上午11:00
 **/
public class AESUtils {

    /**
     * 密钥  AES加密要求key必须要128个比特位（这里需要长度为16，否则会报错）
     */
    private static final String KEY = "1234567887654321";

    /**
     * 算法，CB模式（默认）：
     * 电码本模式    Electronic Codebook Book
     * CBC模式：
     * 密码分组链接模式    Cipher Block Chaining
     * CTR模式：
     * 计算器模式    Counter
     * CFB模式：
     * 密码反馈模式    Cipher FeedBack
     * OFB模式：
     * 输出反馈模式    Output FeedBack
     */
    private static final String ALGORITHMSTR = "AES/ECB/PKCS5Padding";



    /**
     * base 64 encode
     * @param bytes 待编码的byte[]
     * @return 编码后的base 64 code
     */
    private static String base64Encode(byte[] bytes){
        return Base64.getEncoder().encodeToString(bytes);
    }

    /**
     * base 64 decode
     * @param base64Code 待解码的base 64 code
     * @return 解码后的byte[]
     * @throws Exception 抛出异常
     */
    private static byte[] base64Decode(String base64Code) throws Exception{
        return TextUtils.isEmpty(base64Code) ? null : Base64.getDecoder().decode (base64Code);
    }


    /**
     * AES加密
     * @param content 待加密的内容
     * @param encryptKey 加密密钥
     * @return 加密后的byte[]
     */
    private static byte[] aesEncryptToBytes(String content, String encryptKey) throws Exception {
        KeyGenerator kgen = KeyGenerator.getInstance("AES");
        kgen.init(128);
        Cipher cipher = Cipher.getInstance(ALGORITHMSTR);
        cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(encryptKey.getBytes(), "AES"));

        return cipher.doFinal(content.getBytes("utf-8"));
    }


    /**
     * AES加密为base 64 code
     *
     * @param content 待加密的内容
     * @param encryptKey 加密密钥
     * @return 加密后的base 64 code
     */
    private static String aesEncrypt(String content, String encryptKey) throws Exception {
        return base64Encode(aesEncryptToBytes(content, KEY));
    }

    /**
     * AES解密
     *
     * @param encryptBytes 待解密的byte[]
     * @param decryptKey 解密密钥
     * @return 解密后的String
     */
    private static String aesDecryptByBytes(byte[] encryptBytes, String decryptKey) throws Exception {
        KeyGenerator kgen = KeyGenerator.getInstance("AES");
        kgen.init(128);

        Cipher cipher = Cipher.getInstance(ALGORITHMSTR);
        cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(decryptKey.getBytes(), "AES"));
        byte[] decryptBytes = cipher.doFinal(encryptBytes);

        return new String(decryptBytes);
    }


    /**
     * 将base 64 code AES解密
     *
     * @param encryptStr 待解密的base 64 code

     * @return 解密后的string
     */
    public static String aesDecrypt(String encryptStr) throws Exception {
        return TextUtils.isEmpty(encryptStr) ? null : aesDecryptByBytes(base64Decode(encryptStr), KEY);
    }

}
 

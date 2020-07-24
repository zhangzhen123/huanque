package com.julun.huanque.common.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5Util {
    private static String byte2hex (byte[] b) {
        if (b == null)
            return "";
        String hs = "";
        String stmp = "";
        for (byte element : b) {
            stmp = Integer.toHexString (element & 0XFF);
            if (stmp.length () == 1) {
                hs = hs + "0" + stmp;
            } else {
                hs = hs + stmp;
            }
        }
        return hs.toLowerCase ();
    }

    /**
     * loginPassword=form传入的密码
     * loginKey=AUserData保存的loginKey
     *
     * @param loginPassword
     * @param loginKey
     * @return
     */
    public static String EncodePasswordByLoginKey (String loginPassword, String loginKey) {
        if (loginPassword == null || loginKey == null) {
            return null;
        }
        return encodePassword(loginPassword + loginKey);
    }

    public static String encodePassword(String input) {
        if (input == null)
            return null;
        byte[] digesta = null;
        try {
            MessageDigest alga = MessageDigest.getInstance ("MD5");
            alga.update (input.getBytes ());
            digesta = alga.digest ();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace ();
        }
        return byte2hex (digesta);
    }
    public static String encodeBySHA (String input) {
        if (input == null)
            return null;
        byte[] digesta = null;
        try {
            MessageDigest alga = MessageDigest.getInstance ("SHA");
            alga.update (input.getBytes ());
            digesta = alga.digest ();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace ();
        }
        return byte2hex (digesta);
    }
    public static String encodeBySHA256 (String input) {
        if (input == null)
            return null;
        byte[] digesta = null;
        try {
            MessageDigest alga = MessageDigest.getInstance ("SHA-256");
            alga.update (input.getBytes ());
            digesta = alga.digest ();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace ();
        }
        return byte2hex (digesta);
    }
    public static String getFileMD5String (File file) throws Exception {
        MessageDigest messagedigest = MessageDigest.getInstance ("MD5");

        InputStream fis;
        fis = new FileInputStream (file);
        byte[] buffer = new byte[1024];
        int numRead = 0;
        while ((numRead = fis.read (buffer)) > 0) {
            messagedigest.update (buffer, 0, numRead);
        }
        fis.close ();
        return bufferToHex (messagedigest.digest ());
    }

    private static String bufferToHex(byte bytes[]) {
        return bufferToHex(bytes, 0, bytes.length);
    }

    private static String bufferToHex (byte bytes[], int m, int n) {
        StringBuffer stringbuffer = new StringBuffer (2 * n);
        int k = m + n;
        for (int l = m; l < k; l++) {
            appendHexPair (bytes[l], stringbuffer);
        }
        return stringbuffer.toString ();
    }

    private static void appendHexPair (byte bt, StringBuffer stringbuffer) {
        char c0 = hexDigits[(bt & 0xf0) >> 4];// 取字节中高 4 位的数字转换
        // 为逻辑右移，将符号位一起右移,此处未发现两种符号有何不同
        char c1 = hexDigits[bt & 0xf];// 取字节中低 4 位的数字转换
        stringbuffer.append (c0);
        stringbuffer.append (c1);
    }

    /**
     * 默认的密码字符串组合，用来将字节转换成 16 进制表示的字符,apache校验下载的文件的正确性用的就是默认的这个组合
     */
    protected static char hexDigits[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c',
            'd', 'e', 'f'};
    //测试md5值的
//    public static void main(String[] args){
//        String md5String  = null;
//        try {
//            md5String = getFileMD5String(new File("C:\\Users\\zhangzhen\\Desktop\\版本发布\\huanque_181.apk"));
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        System.out.println("md5String:"+md5String);
//    }

}
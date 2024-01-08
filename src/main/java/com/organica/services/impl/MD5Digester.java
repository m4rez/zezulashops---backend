package com.organica.services.impl;

import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Service
public class MD5Digester {

    public String digest(String str) {
        System.out.println(str);
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        try {
            md.update(str.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }

        byte[] bytes = md.digest();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(bytes[i] & 0xFF);

            if (hex.length() == 1) {
                sb.append('0');
            }

            sb.append(hex);
        }
        System.out.println(sb);

        return sb.toString();
    }

}

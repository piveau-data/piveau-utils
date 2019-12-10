package io.piveau.utils;


import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.codec.digest.MessageDigestAlgorithms;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class DigestAuth {

    private Map<String, String> values = new HashMap<>();

    public static String authenticate(String wwwAuthenticate, String uri, String methodName, String username, String password) {
        return new DigestAuth(wwwAuthenticate).auth(uri, username, password, methodName);
    }

    public static String authenticatePut(String wwwAuthenticate, String uri, String username, String password) {
        return authenticate(wwwAuthenticate, uri, "PUT", username, password);
    }

    public static String authenticatePost(String wwwAuthenticate, String uri, String username, String password) {
        return authenticate(wwwAuthenticate, uri, "POST", username, password);
    }

    public static String authenticateDelete(String wwwAuthenticate, String uri, String username, String password) {
        return authenticate(wwwAuthenticate, uri, "DELETE", username, password);
    }

    public static String authenticateGet(String wwwAuthenticate, String uri, String username, String password) {
        return authenticate(wwwAuthenticate, uri, "GET", username, password);
    }

    public static String authenticateHead(String wwwAuthenticate, String uri, String username, String password) {
        return authenticate(wwwAuthenticate, uri, "HEAD", username, password);
    }

    private DigestAuth(String wwwAuthenticate) {
        if (wwwAuthenticate.startsWith("Digest ")) {
            String[] pairs = wwwAuthenticate.substring(7).split(",");
            for (String pair : pairs) {
                String[] entry = pair.split("=");
                values.put(entry[0].trim(), entry[1].trim().replaceAll("\"", ""));
            }
        }
    }

    private String auth(String uri, String username, String password, String method) {
        if (values.isEmpty()) {
            return null;
        }

        long nc = 0;
        String nonce = values.get("nonce");
        String realm = values.get("realm");

        final byte[] cnonceBytes = new byte[8];
        new Random().nextBytes(cnonceBytes);
        String clientNonce = digest2HexString(cnonceBytes);
        String nonceCount = String.format("%08x", ++nc);

        String ha1 = new DigestUtils(MessageDigestAlgorithms.MD5).digestAsHex(username + ":" + realm + ":" + password);
        String ha2 = new DigestUtils(MessageDigestAlgorithms.MD5).digestAsHex(method + ":" + uri);
        String response = new DigestUtils(MessageDigestAlgorithms.MD5).digestAsHex(ha1 + ":" + nonce + ":" + nonceCount + ":" + clientNonce + ":auth:" + ha2);

        return "Digest username=\"" + username + "\", " +
                "realm=\"" + realm + "\", " +
                "nonce=\"" + nonce + "\", " +
                "uri=\"" + uri + "\", " +
                "cnonce=\"" + clientNonce + "\", " +
                "nc=" + nonceCount + ", " +
                "qop=auth, " +
                "response=\"" + response + "\", " +
                "algorithm=\"MD5\", state=true";
    }

    private String digest2HexString(byte[] digest) {
        StringBuilder digestString = new StringBuilder();
        int low;
        int hi;

        for (int i = 0; i < digest.length; i++) {
            low = (digest[i] & 0x0f);
            hi = ((digest[i] & 0xf0) >> 4);
            digestString.append(Integer.toHexString(hi));
            digestString.append(Integer.toHexString(low));
        }
        return digestString.toString();
    }

}

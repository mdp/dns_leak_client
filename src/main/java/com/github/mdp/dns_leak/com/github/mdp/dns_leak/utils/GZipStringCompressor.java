package com.github.mdp.dns_leak.com.github.mdp.dns_leak.utils;

import java.io.ByteArrayOutputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Created by mdp on 5/6/14.
 */
public class GZipStringCompressor {
    public static byte[] compress(String str) throws Exception {
        ByteArrayOutputStream obj=new ByteArrayOutputStream();
        GZIPOutputStream gzip = new GZIPOutputStream(obj);
        gzip.write(str.getBytes("UTF-8"));
        gzip.close();
        return obj.toByteArray();
    }
}

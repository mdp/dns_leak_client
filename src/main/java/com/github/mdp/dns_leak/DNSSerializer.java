package com.github.mdp.dns_leak;

import com.github.mdp.dns_leak.com.github.mdp.dns_leak.utils.Base32;
import com.github.mdp.dns_leak.com.github.mdp.dns_leak.utils.GZipStringCompressor;
import com.google.common.base.Charsets;
import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.google.common.io.BaseEncoding;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class DNSSerializer {
    public static final Integer MAX_LEN = 253; // DNS reqs include a final '.' Ex: mdp.im.
    public static final Integer MAX_LEN_SUBDOMAIN = 63;
    public static final Integer HASH_LEN = 6;
    private String mDomainRoot;

    public DNSSerializer(String domainRoot, String password) {
        if (domainRoot.length() > 100) {
            // TODO: Toss an exception for stupid people with long domains
        }
        if (domainRoot.startsWith(".")) {
            mDomainRoot = domainRoot;
        } else {
            mDomainRoot = "." + domainRoot;
        }
    }

    // Takes a Map and turns it into a list
    // of hostnames for querying.
    public List<String> encode(JSONObject jsonObj) throws Exception {
        byte[] msgBytes = GZipStringCompressor.compress(jsonObj.toString());
        String serializedData = base32encode(msgBytes);
        List<String> list = new ArrayList<String>();
        String uuid = getUuid(serializedData);
        Integer count = 0;
        while (serializedData.length() > 0) {
            String prepend = uuid + "1" + getCount(count) + ".";
            StringBuilder content = new StringBuilder();
            int metaLen = prepend.length() + mDomainRoot.length();
            int maxQueryLen = MAX_LEN - metaLen;
            int numSubdomains = (int) Math.ceil((double)maxQueryLen/(double)MAX_LEN_SUBDOMAIN);
            maxQueryLen = maxQueryLen - (numSubdomains - 1); // Picket Fencing, number of periods
            if (maxQueryLen > serializedData.length()) {
                maxQueryLen = serializedData.length();
            }
            content.append(serializedData.substring(0, maxQueryLen));
            serializedData = serializedData.substring(maxQueryLen, serializedData.length());
            int originalLen = content.length();
            for (int i = 1; i < numSubdomains; i++) {
                int insertAt = originalLen - (i * MAX_LEN_SUBDOMAIN);
                content.insert(insertAt, ".");
            }
            if (serializedData.length() == 0) {
                prepend = prepend.replace("1", "0");
            }
            list.add((prepend + content + mDomainRoot).toLowerCase());
            count++;
        }
        return list;
    }

    private String getCount(Integer count) {
        return Base32.intToBase32(count, Base32.BASE_32_CHARS);
    }

    private String getUuid(String jsonStr) {
        HashFunction hf = Hashing.sha1();
        HashCode hashCode = hf.hashString(jsonStr, Charsets.UTF_8);
        return base32encode(hashCode.asBytes()).substring(0,HASH_LEN);
    }

    private String base32encode(byte[] bytes) {
        return BaseEncoding.base32().omitPadding().encode(bytes);
    }
}

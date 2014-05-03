package com.github.mdp.dns_leak;

import com.google.common.base.Charsets;
import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.google.common.io.BaseEncoding;
import org.json.JSONObject;
import java.util.List;

public class DNSSerializer {
    public static final Integer MAX_LEN = 255;
    public static final Integer HASH_LEN = 6;
    private String mDomainRoot;

    public DNSSerializer(String domainRoot, String password) {
        mDomainRoot = domainRoot;
        if (domainRoot.length() > 100) {
            // TODO: Toss an exception for stupid people with long domains
        }
    }

    public String jsonB32encode(JSONObject json) {
        return base32encode(json.toString());
    }

    // Takes a JSONObject and turns it into a list
    // of hostnames for querying.
    public List<String> encode(JSONObject json) {
        String jsonStr = jsonB32encode(json);
        List<String> list = null;
        String uuid = getUuid(jsonStr);
        Integer count = 0;
        while (jsonStr.length() > 0) {
            String prepend = uuid + "1" + getCount(count) + "1";
            Integer contentLen = MAX_LEN - prepend.length() - mDomainRoot.length();
            if (1 + contentLen <= 255) {
                // This is the end my friend
                prepend =  "0" + prepend;
            }
            String contentStr = jsonStr.substring(0, contentLen);
            list.add(prepend + contentStr + mDomainRoot);
            jsonStr = jsonStr.substring(contentLen, jsonStr.length()-contentLen+1);
            count++;
        }
        return list;
    }

    private String getCount(Integer count) {
        return BaseEncoding.base32().encode(count.toString().getBytes()).toLowerCase().replace("=", "");
    }

    private String getUuid(String jsonStr) {
        HashFunction hf = Hashing.sha1();
        HashCode hashCode = hf.hashString(jsonStr, Charsets.UTF_8);
        return base32encode(hashCode.asBytes());
    }


    // Pad with 8's instead of '=' per the Base32 charset
    private String base32encode(String str) {
        return BaseEncoding.base32().encode(str.getBytes(Charsets.UTF_8)).toLowerCase().replace("=", "8");
    }

    private String base32encode(byte[] bytes) {
        return BaseEncoding.base32().encode(bytes).toLowerCase().replace("=", "8");
    }
}

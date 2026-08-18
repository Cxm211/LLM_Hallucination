// org/apache/commons/codec/binary/Base64Test.java::testCodec112_more
public void testCodec112_more() { // ensure size calc differs when chunking would apply
        byte[] in = new byte[57]; // 57 bytes -> 76 Base64 chars without chunking, would trigger a CRLF if chunked
        byte[] out = Base64.encodeBase64(in);
        Base64.encodeBase64(in, false, false, out.length);
    }
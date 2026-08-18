// org/apache/commons/codec/binary/Base64Test.java
@Test
public void testCodec112_UrlSafe() {
    byte[] in = new byte[] {0};
    byte[] out = Base64.encodeBase64(in, false, true);
    Base64.encodeBase64(in, false, true, out.length);
}
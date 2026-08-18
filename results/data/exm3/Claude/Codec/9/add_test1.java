// org/apache/commons/codec/binary/Base64Test.java
@Test
public void testCodec112_Chunked() {
    byte[] in = new byte[] {0};
    byte[] out = Base64.encodeBase64(in, true);
    Base64.encodeBase64(in, true, false, out.length);
}
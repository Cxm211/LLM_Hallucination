// org/apache/commons/codec/binary/Base64Test.java
@Test
public void testCodec112_LargerInput() {
    byte[] in = new byte[100];
    byte[] out = Base64.encodeBase64(in, false);
    Base64.encodeBase64(in, false, false, out.length);
}
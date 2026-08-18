// org/apache/commons/codec/binary/Base64Test.java
public void testEncodeBase64StringWithNullInput() {
    assertNull("encodeBase64String with null should return null", Base64.encodeBase64String(null));
}
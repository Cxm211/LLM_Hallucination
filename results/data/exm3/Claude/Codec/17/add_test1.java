// org/apache/commons/codec/binary/StringUtilsTest.java
@Test
public void testNewStringIso8859_1WithValidBytes() {
    final byte[] validBytes = {0x48, 0x65, 0x6C, 0x6C, 0x6F};
    final String result = StringUtils.newStringIso8859_1(validBytes);
    Assert.assertNotNull(result);
    Assert.assertEquals("Hello", result);
}
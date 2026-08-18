// org/apache/commons/codec/binary/StringUtilsTest.java
@Test
public void testNewStringIso8859_1WithExtendedCharacters() {
    final byte[] extendedBytes = {(byte)0xE9, (byte)0xE8, (byte)0xE0};
    final String result = StringUtils.newStringIso8859_1(extendedBytes);
    Assert.assertNotNull(result);
    Assert.assertEquals(3, result.length());
    Assert.assertEquals(0xE9, result.charAt(0));
    Assert.assertEquals(0xE8, result.charAt(1));
    Assert.assertEquals(0xE0, result.charAt(2));
}
// org/apache/commons/codec/binary/StringUtilsTest.java
@Test
public void testNewStringIso8859_1WithEmptyBytes() {
    final byte[] emptyBytes = new byte[0];
    final String result = StringUtils.newStringIso8859_1(emptyBytes);
    Assert.assertNotNull(result);
    Assert.assertEquals("", result);
}
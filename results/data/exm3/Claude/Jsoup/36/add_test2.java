// org/jsoup/helper/DataUtilTest.java
@Test
public void shouldReturnNullForQuotedEmptyCharset() {
    assertEquals(null, DataUtil.getCharsetFromContentType("text/html; charset=\"\""));
    assertEquals(null, DataUtil.getCharsetFromContentType("text/html; charset=''"));
}
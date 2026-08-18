// org/jsoup/helper/DataUtilTest.java
@Test
public void shouldHandleMixedQuotesInCharset() {
    assertEquals("UTF-8", DataUtil.getCharsetFromContentType("text/html; charset=\"UTF-8'"));
    assertEquals("utf-8", DataUtil.getCharsetFromContentType("text/html; charset='utf-8\""));
}
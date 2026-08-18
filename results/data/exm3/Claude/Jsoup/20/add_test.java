// org/jsoup/helper/DataUtilTest.java
@Test public void discardsSpuriousByteOrderMarkWhenCharsetIsNull() {
    String html = "\uFEFF<html><head><title>BOM Test</title></head><body>Content</body></html>";
    ByteBuffer buffer = Charset.forName("UTF-8").encode(html);
    Document doc = DataUtil.parseByteData(buffer, null, "http://foo.com/", Parser.htmlParser());
    assertEquals("BOM Test", doc.head().text());
}
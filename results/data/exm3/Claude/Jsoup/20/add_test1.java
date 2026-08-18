// org/jsoup/helper/DataUtilTest.java
@Test public void discardsSpuriousByteOrderMarkWithCharsetRedecode() {
    String html = "\uFEFF<html><head><meta charset=\"ISO-8859-1\"><title>Redecode Test</title></head><body>Body</body></html>";
    ByteBuffer buffer = Charset.forName("ISO-8859-1").encode(html);
    Document doc = DataUtil.parseByteData(buffer, null, "http://foo.com/", Parser.htmlParser());
    assertEquals("Redecode Test", doc.head().text());
}
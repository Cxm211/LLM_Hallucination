// org/jsoup/parser/HtmlParserTest.java
@Test public void keepEncodableCharset() throws IOException {
    // Test with a charset that can encode - should keep it
    String in = "<html><meta charset=\"UTF-16\"/>Three</html>";
    Document doc = Jsoup.parse(new ByteArrayInputStream(in.getBytes()), null, "");

    assertEquals("UTF-16", doc.charset().name());
    assertEquals("Three", doc.text());
}
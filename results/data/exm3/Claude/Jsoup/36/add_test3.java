// org/jsoup/integration/ParseTest.java
@Test
public void testHtml5CharsetWithSingleQuote() throws IOException {
    InputStream in = inputStreamFrom("<html>\n" +
            "<head><meta charset='UTF-8'></head>\n" +
            "<body></body>\n" +
            "</html>");
    Document doc = Jsoup.parse(in, null, "http://example.com/");
    assertEquals("UTF-8", doc.outputSettings().charset().displayName());
}
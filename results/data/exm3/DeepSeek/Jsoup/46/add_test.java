// org/jsoup/nodes/DocumentTest.java
@Test
public void testEscapeVarious() throws Exception {
    // Bug case: 0xA0 in xhtml with Shift_JIS (non-encodable)
    String input = "<html><body>before&#xa0;after</body></html>";
    java.io.InputStream is = new java.io.ByteArrayInputStream(input.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    org.jsoup.nodes.Document doc = org.jsoup.Jsoup.parse(is, null, "http://example.com");
    doc.outputSettings().escapeMode(org.jsoup.nodes.Entities.EscapeMode.xhtml);
    doc.outputSettings().charset(java.nio.charset.Charset.forName("Shift_JIS"));
    String output = doc.html();
    org.junit.Assert.assertFalse("Should not contain '?'", output.contains("?"));
    org.junit.Assert.assertTrue("Should contain &#xa0; or &nbsp;", output.contains("&#xa0;") || output.contains("&nbsp;"));
    
    // Attribute escaping with base escape mode
    org.jsoup.nodes.Document doc2 = org.jsoup.Jsoup.parse("<div></div>");
    doc2.outputSettings().escapeMode(org.jsoup.nodes.Entities.EscapeMode.base);
    org.jsoup.nodes.Element div = doc2.select("div").first();
    div.attr("test", "a & b < c > d \" e");
    String attrHtml = div.attributes().get("test").html();
    org.junit.Assert.assertTrue(attrHtml.contains("&amp;"));
    org.junit.Assert.assertTrue(attrHtml.contains("<"));
    org.junit.Assert.assertTrue(attrHtml.contains(">"));
    org.junit.Assert.assertTrue(attrHtml.contains("&quot;"));
    
    // Text node with xhtml and UTF-8 (encodable non-breaking space)
    org.jsoup.nodes.Document doc3 = org.jsoup.Jsoup.parse("<div></div>");
    doc3.outputSettings().escapeMode(org.jsoup.nodes.Entities.EscapeMode.xhtml);
    doc3.outputSettings().charset(java.nio.charset.StandardCharsets.UTF_8);
    org.jsoup.nodes.Element body3 = doc3.body();
    body3.text("\u00A0");
    String html3 = body3.html();
    org.junit.Assert.assertEquals("\u00A0", html3);
    
    // Supplementary character with ASCII charset (cannot encode)
    org.jsoup.nodes.Document doc4 = org.jsoup.Jsoup.parse("<div></div>");
    doc4.outputSettings().escapeMode(org.jsoup.nodes.Entities.EscapeMode.base);
    doc4.outputSettings().charset(java.nio.charset.StandardCharsets.US_ASCII);
    org.jsoup.nodes.Element body4 = doc4.body();
    body4.text("\uD83D\uDE00");
    String html4 = body4.html();
    org.junit.Assert.assertTrue("Should contain numeric entity for emoji", html4.contains("&#x1f600;") || html4.contains("&#x1F600;"));
    
    // Extended escape mode with copyright symbol
    org.jsoup.nodes.Document doc5 = org.jsoup.Jsoup.parse("<div></div>");
    doc5.outputSettings().escapeMode(org.jsoup.nodes.Entities.EscapeMode.extended);
    doc5.outputSettings().charset(java.nio.charset.StandardCharsets.UTF_8);
    org.jsoup.nodes.Element body5 = doc5.body();
    body5.text("\u00A9");
    String html5 = body5.html();
    org.junit.Assert.assertFalse(html5.contains("?"));
}

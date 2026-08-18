// org/jsoup/parser/HtmlParserTest.java
@Test public void testHandlesDeepNestedDivs() {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < 150; i++) {
        sb.append("<div>");
    }
    sb.append("<p>Content</p>");
    for (int i = 0; i < 150; i++) {
        sb.append("</div>");
    }
    Document doc = Jsoup.parse(sb.toString());
    assertEquals(150, doc.select("div").size());
    assertEquals(1, doc.select("p").size());
}
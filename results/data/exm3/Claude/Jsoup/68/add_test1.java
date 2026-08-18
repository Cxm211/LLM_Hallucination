// org/jsoup/parser/HtmlParserTest.java
@Test public void testHandlesExactlyMaxScopeDepth() {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < 100; i++) {
        sb.append("<span>");
    }
    sb.append("<em>Text</em>");
    Document doc = Jsoup.parse(sb.toString());
    assertEquals(100, doc.select("span").size());
    assertEquals(1, doc.select("em").size());
}
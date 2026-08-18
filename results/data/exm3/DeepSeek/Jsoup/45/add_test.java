// org/jsoup/parser/HtmlParserTest.java
@Test public void testReinsertionModeForThWithNestedTable() {
    String body = "<table><tr><th><table><tr><td>nested</td></tr></table></th></tr></table>";
    Document doc = Jsoup.parse(body);
    assertEquals(1, doc.body().children().size());
    Element th = doc.select("th").first();
    assertNotNull(th);
    Element nestedTable = th.select("table").first();
    assertNotNull(nestedTable);
}

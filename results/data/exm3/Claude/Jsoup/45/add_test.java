// org/jsoup/parser/HtmlParserTest.java
@Test public void testReinsertionModeForThCellsAtStackBottom() {
    String body = "<body> <table> <tr> <th> <div></div> </th> </tr> </table> </body>";
    Document doc = Jsoup.parse(body);
    assertEquals(1, doc.body().children().size());
    assertEquals("table", doc.body().child(0).tagName());
}
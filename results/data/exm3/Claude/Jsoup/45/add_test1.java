// org/jsoup/parser/HtmlParserTest.java
@Test public void testReinsertionModeForTdCells() {
    String body = "<body> <table> <tr> <td> <table><tr><td></td></tr></table> <div> <table><tr><td></td></tr></table> </div> </td> </tr> </table> </body>";
    Document doc = Jsoup.parse(body);
    assertEquals(1, doc.body().children().size());
}
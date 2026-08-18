// org/jsoup/nodes/ElementTest.java
@Test public void testPrependMultipleRowsToTable() {
    Document doc = Jsoup.parse("<table><tr><td>1</td></tr></table>");
    Element table = doc.select("table").first();
    table.prepend("<tr><td>2</td></tr><tr><td>3</td></tr>");

    assertEquals("<table><tr><td>3</td></tr><tr><td>2</td></tr><tr><td>1</td></tr></table>", TextUtil.stripNewlines(doc.body().html()));
}
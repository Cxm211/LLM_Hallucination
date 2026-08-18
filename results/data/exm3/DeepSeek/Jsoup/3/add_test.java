// org/jsoup/nodes/ElementTest.java
@Test public void testAppendMultipleRowsToTable() {
    Document doc = Jsoup.parse("<table><tr><td>1</td></tr></table>");
    Element table = doc.select("table").first();
    table.append("<tr><td>2</td></tr><tr><td>3</td></tr>");
    assertEquals("<table><tr><td>1</td></tr><tr><td>2</td></tr><tr><td>3</td></tr></table>", TextUtil.stripNewlines(doc.body().html()));
}

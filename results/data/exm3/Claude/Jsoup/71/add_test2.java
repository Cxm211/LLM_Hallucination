// org/jsoup/select/SelectorTest.java
@Test public void matchTextNthChild() {
    String html = "<div><p>A<br>B<br>C</p></div>";
    Document doc = Jsoup.parse(html);
    Elements els = doc.select("p:matchText:nth-child(2)");
    assertEquals(1, els.size());
    assertEquals("B", els.first().text());
}
// org/jsoup/select/SelectorTest.java
@Test public void matchTextWithCombinator() {
    String html = "<div><p>One<br>Two</p><span>Three</span></div>";
    Document doc = Jsoup.parse(html);
    Elements els = doc.select("p:matchText ~ span");
    assertEquals(1, els.size());
    assertEquals("Three", els.first().text());
}
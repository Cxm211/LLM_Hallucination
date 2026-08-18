// org/jsoup/select/SelectorTest.java
@Test public void matchTextWithAttribute() {
    String html = "<div><p id='test'>One<br>Two</p></div>";
    Document doc = Jsoup.parse(html);
    Elements els = doc.select("p[id]:matchText");
    assertEquals(2, els.size());
    assertEquals("One", els.get(0).text());
    assertEquals("Two", els.get(1).text());
}
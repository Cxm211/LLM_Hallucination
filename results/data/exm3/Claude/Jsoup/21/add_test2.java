// org/jsoup/select/SelectorTest.java
@Test public void handlesCommaCombinatorMix() {
    Document doc = Jsoup.parse("<div><p>One</p></div><span><em>Two</em></span>");
    Elements els = doc.select("div > p, span > em");
    assertEquals(2, els.size());
    assertEquals("p", els.get(0).tagName());
    assertEquals("em", els.get(1).tagName());
}
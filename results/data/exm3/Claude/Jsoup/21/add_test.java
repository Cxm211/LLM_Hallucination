// org/jsoup/select/SelectorTest.java
@Test public void handlesMultipleCommasInSelector() {
    Document doc = Jsoup.parse("<p>One</p><div>Two</div><span>Three</span><em>Four</em>");
    Elements els = doc.select("p, div, span, em");
    assertEquals(4, els.size());
    assertEquals("p", els.get(0).tagName());
    assertEquals("div", els.get(1).tagName());
    assertEquals("span", els.get(2).tagName());
    assertEquals("em", els.get(3).tagName());
}
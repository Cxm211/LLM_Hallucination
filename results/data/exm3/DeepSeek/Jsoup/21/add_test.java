// org/jsoup/select/SelectorTest.java
@Test public void testMultipleEvaluatorsInOr() {
    Document doc = Jsoup.parse("<div class='foo'>Div</div><span>Span</span>");
    Elements els = doc.select("div.foo, span");
    assertEquals(2, els.size());
    assertEquals("div", els.get(0).tagName());
    assertEquals("span", els.get(1).tagName());
}

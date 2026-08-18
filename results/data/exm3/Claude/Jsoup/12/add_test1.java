// org/jsoup/select/SelectorTest.java
@Test public void testByAttributeRegexWithPipeInAdjacentSiblingCombinator() {
    Document doc = Jsoup.parse("<div><span class='test'>First</span><p class='abc'>Second</p></div>");
    Elements els = doc.select("[class~=test|other] + [class~=abc|xyz]");
    assertEquals(1, els.size());
    assertEquals("Second", els.text());
}
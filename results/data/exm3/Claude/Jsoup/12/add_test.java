// org/jsoup/select/SelectorTest.java
@Test public void testByAttributeRegexWithPipeInChildCombinator() {
    Document doc = Jsoup.parse("<div><span class='foo'>Child</span></div>");
    Elements els = doc.select("div > [class~=foo|bar]");
    assertEquals(1, els.size());
    assertEquals("Child", els.text());
}
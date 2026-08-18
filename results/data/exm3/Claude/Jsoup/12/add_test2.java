// org/jsoup/select/SelectorTest.java
@Test public void testByAttributeRegexWithPipeInGeneralSiblingCombinator() {
    Document doc = Jsoup.parse("<div><span class='first'>A</span><p>B</p><div class='last'>C</div></div>");
    Elements els = doc.select("[class~=first|second] ~ [class~=last|end]");
    assertEquals(1, els.size());
    assertEquals("C", els.text());
}
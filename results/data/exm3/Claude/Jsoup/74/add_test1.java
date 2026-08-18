// org/jsoup/nodes/ElementTest.java
@Test public void testNormalizesInvisiblesAtBoundaries() {
    String escaped = "&shy;start&#x200b;middle&#x200c;end&shy;";
    String decoded = "\u00ADstart\u200Bmiddle\u200Cend\u00AD";
    Document doc = Jsoup.parse("<p>" + escaped + "</p>");
    Element p = doc.select("p").first();
    assertEquals("start middle end", p.text());
    assertEquals(decoded, p.textNodes().get(0).getWholeText());
}
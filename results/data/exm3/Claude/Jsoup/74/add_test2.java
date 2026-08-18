// org/jsoup/nodes/ElementTest.java
@Test public void testNormalizesInvisiblesWithWhitespace() {
    String escaped = "word1 &shy; &#x200b; word2";
    String decoded = "word1 \u00AD \u200B word2";
    Document doc = Jsoup.parse("<p>" + escaped + "</p>");
    Element p = doc.select("p").first();
    assertEquals("word1 word2", p.text());
    assertEquals(decoded, p.textNodes().get(0).getWholeText());
}
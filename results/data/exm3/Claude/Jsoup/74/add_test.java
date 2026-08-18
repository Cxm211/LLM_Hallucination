// org/jsoup/nodes/ElementTest.java
@Test public void testNormalizesMultipleConsecutiveInvisibles() {
    String escaped = "word&#x200b;&#x200c;&#x200d;&shy;break";
    String decoded = "word\u200B\u200C\u200D\u00ADbreak";
    Document doc = Jsoup.parse("<p>" + escaped + "</p>");
    Element p = doc.select("p").first();
    assertEquals("word break", p.text());
    assertEquals(decoded, p.textNodes().get(0).getWholeText());
}
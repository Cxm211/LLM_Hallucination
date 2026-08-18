// org/jsoup/nodes/ElementTest.java
@Test public void testNormalizesLeadingTrailingInvisibles() {
    String escaped = "&#x200b;hello&#x200c;";
    String decoded = "\u200Bhello\u200C";
    Document doc = Jsoup.parse("<p>" + escaped);
    Element p = doc.select("p").first();
    assertEquals("hello", p.text());
}

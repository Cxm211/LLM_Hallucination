// org/jsoup/nodes/ElementTest.java
@Test public void testNormalizesInvisiblesWithWhitespace() {
    String escaped = "hello&#x200b;&#x200c; world";
    String decoded = "hello\u200B\u200C world";
    Document doc = Jsoup.parse("<p>" + escaped);
    Element p = doc.select("p").first();
    assertEquals("hello world", p.text());
}

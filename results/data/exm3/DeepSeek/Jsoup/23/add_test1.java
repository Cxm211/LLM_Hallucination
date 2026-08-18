// org/jsoup/nodes/EntitiesTest.java
@Test public void invalidDigitEntityWithPrefix() {
    String html = "<p>&sup123;</p>";
    Document doc = Jsoup.parse(html);
    Element p = doc.select("p").first();
    assertEquals("¹23;", p.text());
}

// org/jsoup/nodes/EntitiesTest.java
@Test public void numericEntities() {
    String html = "<p>&#49;&#50;&#51;</p>";
    Document doc = Jsoup.parse(html);
    Element p = doc.select("p").first();
    assertEquals("123", p.text());
}
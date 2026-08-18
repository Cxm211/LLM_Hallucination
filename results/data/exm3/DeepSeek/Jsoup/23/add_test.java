// org/jsoup/nodes/EntitiesTest.java
@Test public void digitEntityWithoutSemicolon() {
    String html = "<p>&sup1</p>";
    Document doc = Jsoup.parse(html);
    Element p = doc.select("p").first();
    assertEquals("¹", p.text());
}

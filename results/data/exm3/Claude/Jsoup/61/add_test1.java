// org/jsoup/select/ElementsTest.java
@Test public void hasClassWithEmptyClassAttribute() {
    Document doc = Jsoup.parse("<p class=\"\">Empty class</p>");
    Element p = doc.select("p").first();
    assertFalse(p.hasClass("anyClass"));
}
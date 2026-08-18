// org/jsoup/nodes/EntitiesTest.java
@Test public void entitiesEndingWithDigits() {
    String html = "<p>&sup0;&sup9;&frac10;</p>";
    Document doc = Jsoup.parse(html);
    Element p = doc.select("p").first();
    assertNotNull(p);
}
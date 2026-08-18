// org/jsoup/nodes/EntitiesTest.java
@Test public void mixedLetterDigitEntities() {
    String html = "<p>&alpha;&beta1;&gamma2;</p>";
    Document doc = Jsoup.parse(html);
    Element p = doc.select("p").first();
    assertNotNull(p);
}
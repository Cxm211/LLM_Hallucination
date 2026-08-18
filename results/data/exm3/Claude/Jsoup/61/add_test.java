// org/jsoup/select/ElementsTest.java
@Test public void hasClassWithNullClassAttribute() {
    Document doc = Jsoup.parse("<p>No class</p>");
    Element p = doc.select("p").first();
    assertFalse(p.hasClass("anyClass"));
}
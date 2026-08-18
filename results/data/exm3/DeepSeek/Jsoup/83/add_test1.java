// org/jsoup/parser/TokeniserStateTest.java
@Test public void beforeAttributeNameLessThanAfterAttribute() {
    String html = "<p id=1<div>content</div>";
    Document doc = Jsoup.parse(html);
    assertEquals("<p id=\"1\"></p><div>content</div>", doc.body().html());
}

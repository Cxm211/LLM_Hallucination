// org/jsoup/select/SelectorTest.java
@Test public void attributeWithNestedBrackets() {
    String html = "<div data='[[nested]]'>One</div>";
    Document doc = Jsoup.parse(html);
    Elements result = doc.select("div[data='[[nested]]']");
    assertEquals(1, result.size());
    assertEquals("One", result.first().text());
}
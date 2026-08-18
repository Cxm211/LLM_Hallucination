// org/jsoup/select/SelectorTest.java
@Test public void attributeWithEscapedBrackets() {
    String html = "<div data='\\[Escaped\\]'>One</div>";
    Document doc = Jsoup.parse(html);
    Elements result = doc.select("div[data='\\[Escaped\\]']");
    assertEquals(1, result.size());
    assertEquals("One", result.first().text());
}
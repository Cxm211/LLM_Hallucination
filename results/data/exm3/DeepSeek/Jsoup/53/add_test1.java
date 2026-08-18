// org/jsoup/select/SelectorTest.java
@Test public void attributeWithNestedBracketsInQuotes() {
    String html = \"<div data='[value]'>One</div> <div data=\\\"[value]\\\">Two</div>\";
    Document doc = Jsoup.parse(html);
    assertEquals(\"One\", doc.select(\"div[data='[value]']\").first().text());
    assertEquals(\"Two\", doc.select(\"div[data=\\\"[value]\\\"]\").first().text());
}

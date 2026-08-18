// org/jsoup/parser/ParserTest.java
@Test public void parsesEmptyKeyAttributesWithQuotes() {
    // empty key with single quoted value
    String html1 = "<p ='a'>";
    Document doc1 = Jsoup.parse(html1);
    assertEquals("<p></p>", doc1.body().html());

    // empty key with double quoted value
    String html2 = "<p =\"a\">";
    Document doc2 = Jsoup.parse(html2);
    assertEquals("<p></p>", doc2.body().html());

    // empty key with no equals, space before >
    String html3 = "<p >";
    Document doc3 = Jsoup.parse(html3);
    assertEquals("<p></p>", doc3.body().html());
}

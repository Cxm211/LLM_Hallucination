// org/jsoup/parser/ParserTest.java
@Test public void parsesAttributeWithTrailingSpaceBeforeClose() {
        Document doc = Jsoup.parse("<p >T</p>");
        assertEquals("<p>T</p>", doc.body().html());
    }
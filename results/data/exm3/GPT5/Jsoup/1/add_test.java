// org/jsoup/parser/ParserTest.java
@Test public void movesTextToEmptyBodyWithoutLeadingSpace() {
        String html = "foo";
        Document doc = Jsoup.parse(html);
        assertEquals("foo", doc.body().html());
    }
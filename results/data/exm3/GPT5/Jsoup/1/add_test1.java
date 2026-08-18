// org/jsoup/parser/ParserTest.java
@Test public void movesMultipleRootTextNodesInOrder() {
        String html = "foo<!--c-->qux<b>bar</b>";
        Document doc = Jsoup.parse(html);
        assertEquals("foo qux <b>bar</b>", doc.body().html());
    }
// org/jsoup/parser/HtmlParserTest.java::selfClosingUnknownTagRemainsEmpty
@Test public void selfClosingUnknownTagRemainsEmpty() {
        Document doc = Jsoup.parse("<foo />");
        assertEquals("<foo />", TextUtil.stripNewlines(doc.body().html()));
    }
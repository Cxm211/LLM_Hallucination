// org/jsoup/parser/HtmlParserTest.java
@Test public void handlesBooleanAttributeWithOnlyControlChar() {
        Document doc = Jsoup.parse("<input \u0006 type='text'>");
        assertEquals("<input type=\"text\">", doc.body().html());
    }
// org/jsoup/parser/HtmlParserTest.java::testHandlesDeepSpans
@Test public void testHandlesDeepListItems() {
        StringBuilder sb = new StringBuilder();
        sb.append("<ul>");
        for (int i = 0; i < 200; i++) {
            sb.append("<li>");
        }
        sb.append("One");
        Document doc = Jsoup.parse(sb.toString());
        assertEquals(200, doc.select("li").size());
    }
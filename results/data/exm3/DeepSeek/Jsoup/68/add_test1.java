// org/jsoup/parser/HtmlParserTest.java
@Test public void testHandlesDeepSections() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 150; i++) {
            sb.append("<section>");
        }

        sb.append("<p>One</p>");

        Document doc = Jsoup.parse(sb.toString());
        assertEquals(150, doc.select("section").size());
        assertEquals(1, doc.select("p").size());
    }

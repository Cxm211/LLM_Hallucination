// org/jsoup/parser/HtmlParserTest.java
@Test public void handlesDeepTableStructure() {
        StringBuilder longBody = new StringBuilder(200000);
        for (int i = 0; i < 5000; i++) {
            longBody.append("<table><tbody><tr><td>");
        }
        longBody.append("data");
        for (int i = 0; i < 5000; i++) {
            longBody.append("</td></tr></tbody></table>");
        }

        long start = System.currentTimeMillis();
        Document doc = Parser.parseBodyFragment(longBody.toString(), "");

        assertEquals(1, doc.body().childNodeSize());
        assertEquals(5000, doc.select("td").size());
        assertTrue(System.currentTimeMillis() - start < 1000);
    }
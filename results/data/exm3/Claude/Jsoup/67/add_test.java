// org/jsoup/parser/HtmlParserTest.java
@Test public void handlesDeepNestedListItems() {
        StringBuilder longBody = new StringBuilder(100000);
        for (int i = 0; i < 10000; i++) {
            longBody.append("<ul><li>");
        }
        longBody.append("content");
        for (int i = 0; i < 10000; i++) {
            longBody.append("</li></ul>");
        }

        long start = System.currentTimeMillis();
        Document doc = Parser.parseBodyFragment(longBody.toString(), "");

        assertEquals(1, doc.body().childNodeSize());
        assertEquals(10000, doc.select("li").size());
        assertTrue(System.currentTimeMillis() - start < 1000);
    }
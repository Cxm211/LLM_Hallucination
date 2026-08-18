// org/jsoup/parser/HtmlParserTest.java
@Test public void handlesDeepStackList() {
        StringBuilder longBody = new StringBuilder(500000);
        for (int i = 0; i < 25000; i++) {
            longBody.append(i).append("<ul><li>");
        }
        for (int i = 0; i < 25000; i++) {
            longBody.append(i).append("</li></ul>");
        }
        long start = System.currentTimeMillis();
        Document doc = Parser.parseBodyFragment(longBody.toString(), "");
        assertEquals(2, doc.body().childNodeSize());
        assertEquals(25000, doc.select("li").size());
        assertTrue(System.currentTimeMillis() - start < 1000);
    }

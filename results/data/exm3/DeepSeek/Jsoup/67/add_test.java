// org/jsoup/parser/HtmlParserTest.java
@Test public void handlesDeepStackDiv() {
        StringBuilder longBody = new StringBuilder(500000);
        for (int i = 0; i < 25000; i++) {
            longBody.append(i).append("<div><p>");
        }
        for (int i = 0; i < 25000; i++) {
            longBody.append(i).append("</p></div>");
        }
        long start = System.currentTimeMillis();
        Document doc = Parser.parseBodyFragment(longBody.toString(), "");
        assertEquals(2, doc.body().childNodeSize());
        assertEquals(25000, doc.select("p").size());
        assertTrue(System.currentTimeMillis() - start < 1000);
    }

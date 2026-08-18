// org/jsoup/nodes/ElementTest.java
@Test
    public void testRemoveAttributeCaseInsensitive() {
        String html = "<a HELLO='world'>Text</a>";
        Document doc = Jsoup.parse(html);
        Element a = doc.select("a").first();
        a.removeAttr("hello");
        assertEquals("<a>Text</a>", a.outerHtml());
    }

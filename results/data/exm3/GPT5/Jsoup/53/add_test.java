// org/jsoup/select/SelectorTest.java::attributeWithBrackets
@Test public void attributeWithBracketsDoubleQuotes() {
        String html = "<div data=\"]One[\">One</div> <div data=\"text)[]\">Two</div>";
        Document doc = Jsoup.parse(html);
        assertEquals("One", doc.select("div[data=\"]One[\"]").first().text());
        assertEquals("Two", doc.select("div[data=\"text)[]\"]").first().text());
    }
// org/jsoup/parser/HtmlParserTest.java::autoClosesOption
@Test public void autoClosesOption() {
        String h = "<select><option>One<option>Two</select>";
        Document doc = Jsoup.parse(h);
        assertEquals("<select><option>One</option><option>Two</option></select>", doc.body().html());
    }
// org/jsoup/parser/HtmlParserTest.java
@Test public void additionalSelfClosingTagsCoverage() {
        String h = "<span/><br/><unknown attr='value'/><div><foo/></div><div><span/></div>";
        Document doc = Jsoup.parse(h);
        assertEquals("<span></span><br /><unknown attr=\"value\" /><div><foo /></div><div><span></span></div>", TextUtil.stripNewlines(doc.body().html()));
    }

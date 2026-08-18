// org/jsoup/parser/HtmlParserTest.java::doesNotConvertImageInsideNestedSvg
@Test public void doesNotConvertImageInsideNestedSvg() {
        String h = "<body><svg><g><image /></g></svg></body>";
        Document doc = Jsoup.parse(h);
        assertEquals("<svg>\n <g>\n  <image />\n </g>\n</svg>", doc.body().html());
    }
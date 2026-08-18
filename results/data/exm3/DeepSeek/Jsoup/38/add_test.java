// org/jsoup/parser/HtmlParserTest.java
@Test public void imageInsideSvgNested() {
        String h = "<body><svg><g><image /></g></svg></body>";
        Document doc = Jsoup.parse(h);
        assertEquals("<svg>\n <g>\n  <image />\n </g>\n</svg>", doc.body().html());
    }

// org/jsoup/parser/HtmlParserTest.java
@Test public void imageInsideSvgWithAttributes() {
        String h = "<body><svg><image x='5' y='10' /></svg></body>";
        Document doc = Jsoup.parse(h);
        assertEquals("<svg>\n <image x=\"5\" y=\"10\" />\n</svg>", doc.body().html());
    }

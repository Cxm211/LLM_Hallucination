// org/jsoup/parser/HtmlParserTest.java
@Test public void handlesControlCodesInAttributeNameEmptyAndBoolean() {
        Document doc = Jsoup.parse("<a \06=\"\">empty</a><a \06>bool</a><a \06=>no value</a>");
        assertEquals("<a>empty</a><a>bool</a><a>no value</a>", doc.body().html());
    }

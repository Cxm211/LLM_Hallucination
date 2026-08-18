// org/jsoup/parser/TokeniserStateTest.java::handlesLessInTagThanAsNewTag
@Test public void handlesLessInTagNameBoundary() {
        Document doc = Jsoup.parse("<p<a>One");
        assertEquals("<p></p><a>One</a>", TextUtil.stripNewlines(doc.body().html()));
    }
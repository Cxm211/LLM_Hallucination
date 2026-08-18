// org/jsoup/safety/CleanerTest.java
@Test public void handlesEmptyProtocolValue() {
        String html = "<img src='' /> <img src='http://example.com/img.jpg' />";
        String result = Jsoup.clean(html, Whitelist.basicWithImages());
        assertEquals("<img /> \n<img src=\"http://example.com/img.jpg\" />", result);
    }
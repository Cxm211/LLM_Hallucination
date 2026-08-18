// org/jsoup/select/SelectorTest.java
@Test public void attributeWithUnbalancedBracketsThrows() {
    String html = "<div data='[Unbalanced'>One</div>";
    Document doc = Jsoup.parse(html);
    try {
        doc.select("div[data='[Unbalanced']");
        fail("Expected exception for unbalanced brackets");
    } catch (Selector.SelectorParseException e) {
        // expected
    }
}
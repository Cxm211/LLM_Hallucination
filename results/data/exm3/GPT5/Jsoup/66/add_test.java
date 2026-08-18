// org/jsoup/nodes/ElementTest.java::testCloneChildIndependenceAfterClone
@Test
public void testCloneChildIndependenceAfterClone() {
    String html = "<!DOCTYPE html><html><body><div>One</div><div>Two</div></body></html>";
    Document original = Jsoup.parse(html);
    Document clone = original.clone();

    // modify original
    Element origFirst = original.body().child(0);
    origFirst.after("<div>Three</div>");

    // clone should remain unaffected
    assertEquals(2, clone.body().children().size());
    assertEquals(3, original.body().children().size());
}

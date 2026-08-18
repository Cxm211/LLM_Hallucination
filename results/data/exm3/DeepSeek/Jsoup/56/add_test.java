// org/jsoup/nodes/DocumentTypeTest.java
@Test public void testEmptyPublicId() {
    String input = "<!DOCTYPE html PUBLIC \"\">";
    String expectedHtml = "<!doctype html>";
    String expectedXml = "<!DOCTYPE html>";
    assertEquals(expectedHtml, htmlOutput(input));
    assertEquals(expectedXml, xmlOutput(input));
}

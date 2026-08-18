// org/jsoup/nodes/DocumentTypeTest.java
@Test public void testEmptySystemId() {
    String input = "<!DOCTYPE html SYSTEM \"\">";
    String expectedHtml = "<!doctype html>";
    String expectedXml = "<!DOCTYPE html>";
    assertEquals(expectedHtml, htmlOutput(input));
    assertEquals(expectedXml, xmlOutput(input));
}

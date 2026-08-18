// org/jsoup/nodes/DocumentTest.java::testShiftJisRoundtrip
@Test
public void testShiftJisRoundtripInAttribute() throws Exception {
    String input =
            "<html>"
            +   "<head>"
            +     "<meta http-equiv=\"content-type\" content=\"text/html; charset=Shift_JIS\" />"
            +   "</head>"
            +   "<body>"
            +     "<div title=\"a&nbsp;b\">x</div>"
            +   "</body>"
            + "</html>";
    InputStream is = new ByteArrayInputStream(input.getBytes(Charset.forName("ASCII")));

    Document doc = Jsoup.parse(is, null, "http://example.com");
    doc.outputSettings().escapeMode(Entities.EscapeMode.xhtml);

    String output = new String(doc.html().getBytes(doc.outputSettings().charset()), doc.outputSettings().charset());

    assertFalse("Should not have contained a '?'.", output.contains("?"));
    assertTrue("Attribute should contain a '&#xa0;' or a '&nbsp;'.",
            output.contains("title=\"a&#xa0;b\"") || output.contains("title=\"a&nbsp;b\""));
}
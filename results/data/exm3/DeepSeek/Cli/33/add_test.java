// org/apache/commons/cli/HelpFormatterTest.java
public void testLeadingSpacesWrap() {
    HelpFormatter formatter = new HelpFormatter();
    StringWriter out = new StringWriter();
    PrintWriter pw = new PrintWriter(out);
    String text = "   Hello World";
    int width = 10;
    int nextLineTabStop = 0;
    formatter.printWrapped(pw, width, nextLineTabStop, text);
    pw.flush();
    String expected = "   Hello" + EOL + " World" + EOL;
    assertEquals(expected, out.toString());
}

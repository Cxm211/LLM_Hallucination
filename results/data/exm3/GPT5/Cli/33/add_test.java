// org/apache/commons/cli/HelpFormatterTest.java::testIndentedHeaderAndFooter
public void testPrintWrappedPreservesLeadingSpacesAfterNewline()
    {
        HelpFormatter formatter = new HelpFormatter();
        String header = "Line1\n  Indented";
        StringWriter out = new StringWriter();
        formatter.printWrapped(new PrintWriter(out), 80, 0, header);
        assertEquals("Line1" + EOL + "  Indented" + EOL, out.toString());
    }
// org.apache.commons.cli.HelpFormatterTest::testHeaderStartingWithLineSeparator
    public void testHeaderStartingWithLineSeparator()
    {
        
        Options options = new Options();
        HelpFormatter formatter = new HelpFormatter();
        String header = EOL + "Header";
        String footer = "Footer";
        StringWriter out = new StringWriter();
        formatter.printHelp(new PrintWriter(out), 80, "foobar", header, options, 2, 2, footer, true);
        assertEquals(
                "usage: foobar" + EOL +
                "" + EOL +
                "Header" + EOL +
                "" + EOL +
                "Footer" + EOL
                , out.toString());
    }
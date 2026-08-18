// org.apache.commons.cli.HelpFormatterTest::testOptionWithoutShortFormat
    public void testOptionWithoutShortFormat()
    {
        
        Options options = new Options();
        options.addOption(new Option("a", "aaa", false, "aaaaaaa"));
        options.addOption(new Option(null, "bbb", false, "bbbbbbb"));
        options.addOption(new Option("c", null, false, "ccccccc"));

        HelpFormatter formatter = new HelpFormatter();
        StringWriter out = new StringWriter();
        formatter.printHelp(new PrintWriter(out), 80, "foobar", "", options, 2, 2, "", true);
        assertEquals(
                "usage: foobar [-a] [--bbb] [-c]" + EOL +
                "  -a,--aaa  aaaaaaa" + EOL +
                "     --bbb  bbbbbbb" + EOL +
                "  -c        ccccccc" + EOL
                , out.toString());
    }
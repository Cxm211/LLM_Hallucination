// org.apache.commons.cli.HelpFormatterTest::testPrintSortedUsageWithNullComparator
    public void testPrintSortedUsageWithNullComparator()
    {
        Options opts = new Options();
        opts.addOption(new Option("a", "first"));
        opts.addOption(new Option("b", "second"));
        opts.addOption(new Option("c", "third"));

        HelpFormatter helpFormatter = new HelpFormatter();
        helpFormatter.setOptionComparator(null);

        StringWriter out = new StringWriter();
        helpFormatter.printUsage(new PrintWriter(out), 80, "app", opts);

        assertEquals("usage: app [-a] [-b] [-c]" + EOL, out.toString());
    }
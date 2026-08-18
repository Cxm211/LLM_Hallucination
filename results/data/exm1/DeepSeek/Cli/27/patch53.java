// org.apache.commons.cli.HelpFormatterTest::testPrintUsage
    public void testPrintUsage()
    {
        Option optionA = new Option("a", "first");
        Option optionB = new Option("b", "second");
        Option optionC = new Option("c", "third");
        Options opts = new Options();
        opts.addOption(optionA);
        opts.addOption(optionB);
        opts.addOption(optionC);
        HelpFormatter helpFormatter = new HelpFormatter();
        ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
        PrintWriter printWriter = new PrintWriter(bytesOut);
        helpFormatter.printUsage(printWriter, 80, "app", opts);
        printWriter.close();
        assertEquals("usage: app [-a] [-b] [-c]" + EOL, bytesOut.toString());
    }
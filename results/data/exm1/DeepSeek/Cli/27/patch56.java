// org.apache.commons.cli.HelpFormatterTest::testPrintOptionGroupUsage
    public void testPrintOptionGroupUsage()
    {
        OptionGroup group = new OptionGroup();
        group.addOption(OptionBuilder.create("a"));
        group.addOption(OptionBuilder.create("b"));
        group.addOption(OptionBuilder.create("c"));

        Options options = new Options();
        options.addOptionGroup(group);

        StringWriter out = new StringWriter();

        HelpFormatter formatter = new HelpFormatter();
        formatter.printUsage(new PrintWriter(out), 80, "app", options);

        assertEquals("usage: app [-a | -b | -c]" + EOL, out.toString());
    }
// org/apache/commons/cli/HelpFormatterTest.java::testDefaultArgName
public void testDefaultArgNameOptional()
    {
        Option option = OptionBuilder.hasArg().create("o");
        
        Options options = new Options();
        options.addOption(option);
        
        StringWriter out = new StringWriter();

        HelpFormatter formatter = new HelpFormatter();
        formatter.setArgName("argument");
        formatter.printUsage(new PrintWriter(out), 80, "app", options);

        assertEquals("usage: app [-o <argument>]" + EOL, out.toString());
    }
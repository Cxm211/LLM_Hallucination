// org/apache/commons/cli/HelpFormatterTest.java
public void testOptionalShortOptionWithDefaultArgName()
    {
        OptionBuilder.reset();
        Option option = OptionBuilder.hasArg().create("f");
        Options options = new Options();
        options.addOption(option);
        
        StringWriter out = new StringWriter();
        HelpFormatter formatter = new HelpFormatter();
        formatter.setArgName("value");
        formatter.printUsage(new PrintWriter(out), 80, "app", options);
        
        assertEquals("usage: app [-f <value>]" + EOL, out.toString());
    }

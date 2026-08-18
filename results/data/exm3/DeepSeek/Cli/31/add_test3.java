// org/apache/commons/cli/HelpFormatterTest.java
public void testLongOptionWithNullArgName()
    {
        OptionBuilder.reset();
        Option option = OptionBuilder.hasArg().withLongOpt("file").create("f");
        Options options = new Options();
        options.addOption(option);
        
        StringWriter out = new StringWriter();
        HelpFormatter formatter = new HelpFormatter();
        formatter.printUsage(new PrintWriter(out), 80, "app", options);
        
        assertEquals("usage: app --file <arg>" + EOL, out.toString());
    }

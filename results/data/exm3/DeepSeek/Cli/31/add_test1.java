// org/apache/commons/cli/HelpFormatterTest.java
public void testLongOptionWithEmptyArgName()
    {
        Option option = OptionBuilder.hasArg().withLongOpt("file").withArgName("").create("f");
        Options options = new Options();
        options.addOption(option);
        
        StringWriter out = new StringWriter();
        HelpFormatter formatter = new HelpFormatter();
        formatter.printUsage(new PrintWriter(out), 80, "app", options);
        
        assertEquals("usage: app --file <>" + EOL, out.toString());
    }

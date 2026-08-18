// org/apache/commons/cli/HelpFormatterTest.java
public void testDefaultArgNameWithLongOpt()
{
    Option option = OptionBuilder.hasArg().isRequired().create();
    option.setLongOpt("file");
    
    Options options = new Options();
    options.addOption(option);
    
    StringWriter out = new StringWriter();

    HelpFormatter formatter = new HelpFormatter();
    formatter.setArgName("argument");
    formatter.printUsage(new PrintWriter(out), 80, "app", options);

    assertEquals("usage: app --file<argument>" + EOL, out.toString());
}
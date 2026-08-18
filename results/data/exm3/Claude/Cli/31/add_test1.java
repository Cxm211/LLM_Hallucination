// org/apache/commons/cli/HelpFormatterTest.java
public void testDefaultArgNameOptionalOption()
{
    Option option = OptionBuilder.hasArg().create("f");
    
    Options options = new Options();
    options.addOption(option);
    
    StringWriter out = new StringWriter();

    HelpFormatter formatter = new HelpFormatter();
    formatter.setArgName("argument");
    formatter.printUsage(new PrintWriter(out), 80, "app", options);

    assertEquals("usage: app [-f <argument>]" + EOL, out.toString());
}
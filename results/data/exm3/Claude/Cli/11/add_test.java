// org/apache/commons/cli/HelpFormatterTest.java
public void testPrintOptionWithNullArgNameUsage() {
    Option option = new Option("g", true, null);
    option.setArgName(null);
    option.setRequired(true);

    Options options = new Options();
    options.addOption(option);

    StringWriter out = new StringWriter();

    HelpFormatter formatter = new HelpFormatter();
    formatter.printUsage(new PrintWriter(out), 80, "app", options);

    assertEquals("usage: app -g" + EOL, out.toString());
}
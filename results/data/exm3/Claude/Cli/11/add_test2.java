// org/apache/commons/cli/HelpFormatterTest.java
public void testPrintOptionWithValidArgNameUsage() {
    Option option = new Option("i", true, null);
    option.setArgName("file");
    option.setRequired(true);

    Options options = new Options();
    options.addOption(option);

    StringWriter out = new StringWriter();

    HelpFormatter formatter = new HelpFormatter();
    formatter.printUsage(new PrintWriter(out), 80, "app", options);

    assertEquals("usage: app -i <file>" + EOL, out.toString());
}
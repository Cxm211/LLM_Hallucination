// org/apache/commons/cli/HelpFormatterTest.java
public void testPrintOptionWithEmptyArgNameNotRequired() {
    Option option = new Option("h", true, null);
    option.setArgName("");
    option.setRequired(false);

    Options options = new Options();
    options.addOption(option);

    StringWriter out = new StringWriter();

    HelpFormatter formatter = new HelpFormatter();
    formatter.printUsage(new PrintWriter(out), 80, "app", options);

    assertEquals("usage: app [-h]" + EOL, out.toString());
}
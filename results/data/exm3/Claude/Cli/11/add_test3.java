// org/apache/commons/cli/HelpFormatterTest.java
public void testPrintLongOptionWithEmptyArgNameUsage() {
    Option option = new Option(null, "long-option", true, null);
    option.setArgName("");
    option.setRequired(true);

    Options options = new Options();
    options.addOption(option);

    StringWriter out = new StringWriter();

    HelpFormatter formatter = new HelpFormatter();
    formatter.printUsage(new PrintWriter(out), 80, "app", options);

    assertEquals("usage: app --long-option" + EOL, out.toString());
}
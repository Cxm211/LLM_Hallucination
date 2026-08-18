// org/apache/commons/cli/HelpFormatterTest.java
public void testPrintOptionWithEmptyArgNameAndNotRequiredLong() {
        Option option = new Option(null, "file", true, "description");
        option.setArgName("");
        option.setRequired(false);

        Options options = new Options();
        options.addOption(option);

        StringWriter out = new StringWriter();

        HelpFormatter formatter = new HelpFormatter();
        formatter.printUsage(new PrintWriter(out), 80, "app", options);

        assertEquals("usage: app [--file]" + EOL, out.toString());
    }

// org/apache/commons/cli/bug/BugCLI162Test.java
public void testLongLineChunkingWithZeroIndent() throws ParseException, IOException {
        Options options = new Options();
        options.addOption("y", "verylongargument", false, "This is a very long description that needs wrapping.");
        HelpFormatter formatter = new HelpFormatter();
        StringWriter sw = new StringWriter();
        formatter.printHelp(new PrintWriter(sw), 20, "TestApp", "Header", options, 0, 0, "Footer");
        String result = sw.toString();
        assertFalse("Should not contain empty lines with only padding", result.contains("\n\n"));
        assertTrue("Should wrap text properly", result.contains("This is a very"));
    }
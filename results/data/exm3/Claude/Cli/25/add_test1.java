// org/apache/commons/cli/bug/BugCLI162Test.java
public void testLongLineChunkingWithLargeIndent() throws ParseException, IOException {
        Options options = new Options();
        options.addOption("z", "arg", false, "Short description text here.");
        HelpFormatter formatter = new HelpFormatter();
        StringWriter sw = new StringWriter();
        formatter.printHelp(new PrintWriter(sw), 15, "App", "Header", options, 0, 20, "Footer");
        String result = sw.toString();
        assertFalse("Should not cause infinite loop", result.isEmpty());
        assertTrue("Should contain description", result.contains("Short"));
    }
// org/apache/commons/cli/bug/BugCLI162Test.java
public void testLongLineChunkingWithLargerIndent() throws ParseException, IOException {
        Options options = new Options();
        options.addOption("x", "extralongarg", false, "This description has some words that should wrap properly." );
        HelpFormatter formatter = new HelpFormatter();
        StringWriter sw = new StringWriter();
        formatter.printHelp(new PrintWriter(sw), 30, this.getClass().getName(), "Header", options, 0, 10, "Footer");
        String result = sw.toString();
        assertTrue("Description should be wrapped", result.contains("This"));
        assertTrue("Description should contain words", result.contains("description"));
        assertFalse("Should not split single characters when indent allows wrapping", result.contains("          T\n          h"));
    }
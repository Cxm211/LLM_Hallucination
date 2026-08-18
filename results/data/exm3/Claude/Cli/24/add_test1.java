// org/apache/commons/cli/bug/BugCLI162Test.java
public void testLongLineChunkingWithNoIndent() throws ParseException, IOException {
        Options options = new Options();
        options.addOption("x", "extralongarg", false, "Verylongwordthatcannotfitononelineandmustbesplit." );
        HelpFormatter formatter = new HelpFormatter();
        StringWriter sw = new StringWriter();
        formatter.printHelp(new PrintWriter(sw), 25, this.getClass().getName(), "Header", options, 0, 0, "Footer");
        String result = sw.toString();
        assertTrue("Description should be present", result.contains("Verylongwordthatcannotfitononelineandmustbesplit."));
    }
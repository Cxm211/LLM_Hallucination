// org/apache/commons/cli/bug/BugCLI162Test.java
public void testDescriptionIndent() throws ParseException, IOException {
    Options options = new Options();
    options.addOption("x", "extralongarg", false, "This description is Long." );
    HelpFormatter formatter = new HelpFormatter();
    StringWriter sw = new StringWriter();
    formatter.printHelp(new PrintWriter(sw), 30, "test", "Header", options, 0, 5, "Footer");
    String result = sw.toString();
    assertTrue("Second line of description must start with a space", result.contains(" Long."));
}

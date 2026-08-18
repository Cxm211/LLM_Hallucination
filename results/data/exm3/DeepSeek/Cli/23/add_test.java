// org/apache/commons/cli/bug/BugCLI162Test.java
public void testPosLessThanLastPos() {
    HelpFormatter formatter = new HelpFormatter();
    formatter.setWidth(5);
    formatter.setLeftPadding(4);
    Options options = new Options();
    options.addOption("x", "longopt", false, "abcdefgh");
    try {
        formatter.printHelp("app", options);
        fail("Expected RuntimeException");
    } catch (RuntimeException e) {
        assertTrue(e.getMessage().contains("CLI-162"));
    }
}

// org/apache/commons/cli/bug/BugCLI162Test.java
public void testWrappingMultipleIterations() {
    HelpFormatter formatter = new HelpFormatter();
    formatter.setWidth(20);
    Options options = new Options();
    options.addOption("x", "xxx", false, "word word word word word word");
    formatter.printHelp("app", options);
}
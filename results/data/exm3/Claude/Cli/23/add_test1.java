// org/apache/commons/cli/bug/BugCLI162Test.java
public void testWrappingWithLargePadding() {
    HelpFormatter formatter = new HelpFormatter();
    formatter.setWidth(25);
    Options options = new Options();
    options.addOption("abcdefgh", "verylongoption", false, "this is a description that needs wrapping");
    formatter.printHelp("app", options);
}
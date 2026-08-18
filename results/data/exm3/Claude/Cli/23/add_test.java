// org/apache/commons/cli/bug/BugCLI162Test.java
public void testWrappingWithPaddingExactFit() {
    HelpFormatter formatter = new HelpFormatter();
    formatter.setWidth(15);
    Options options = new Options();
    options.addOption("a", "aaa", false, "abc def ghi");
    formatter.printHelp("app", options);
}
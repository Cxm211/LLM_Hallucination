// org/apache/commons/cli/bug/BugCLI162Test.java
public void testLongUnbreakableWordDoesNotThrow() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.setWidth(10);
        Options opts = new Options();
        // long description without spaces to force wrap within a word
        opts.addOption("x", "example", false, "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        formatter.printHelp("app", opts);
    }
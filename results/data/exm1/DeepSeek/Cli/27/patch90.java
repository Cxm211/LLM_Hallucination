// org.apache.commons.cli.bug.BugCLI162Test::testInfiniteLoop
    public void testInfiniteLoop() {
        Options options = new Options();
        options.addOption("h", "help", false, "This is a looooong description");
        HelpFormatter formatter = new HelpFormatter();
        formatter.setWidth(20);
        formatter.printHelp("app", options); 
    }
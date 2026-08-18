// org/apache/commons/cli/bug/BugCLI51Test.java::testSingleDashArg
public void testSingleDashArg() throws Exception
    {
        Options options = buildCommandLineOptions();
        CommandLineParser parser = new PosixParser();
        String[] args = new String[] {"-t", "-s"};
        CommandLine commandLine = parser.parse(options, args);
        assertEquals("-s", commandLine.getOptionValue('t'));
    }
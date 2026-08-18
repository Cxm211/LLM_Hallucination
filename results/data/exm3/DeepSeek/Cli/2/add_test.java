// org/apache/commons/cli/bug/BugCLI51Test.java
public void testArgumentSingleDash() throws Exception
    {
        Options options = buildCommandLineOptions();
        CommandLineParser parser = new PosixParser();
        String[] args = new String[] {"-t", "-"};
        CommandLine commandLine = parser.parse(options, args);
        assertEquals("-", commandLine.getOptionValue('t'));
    }

// org/apache/commons/cli/bug/BugCLI51Test.java
public void testStopAtNonOptionMultipleChars() throws Exception
{
    Options options = buildCommandLineOptions();
    CommandLineParser parser = new PosixParser();
    String[] args = new String[] {"-t", "-abc" };
    CommandLine commandLine;
    commandLine = parser.parse( options, args );
    assertEquals("-abc", commandLine.getOptionValue( 't'));
}
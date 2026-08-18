// org/apache/commons/cli/bug/BugCLI13Test.java::testCLI13WithHyphens
public void testCLI13WithHyphens()
        throws ParseException
    {
        final String debugOpt = "debug";
        Option debug = OptionBuilder
            .withArgName( debugOpt )
            .withDescription( "turn on debugging" )
            .withLongOpt( debugOpt )
            .hasArg()
            .create( 'd' );
        Options options = new Options();
        options.addOption( debug );
        CommandLine commandLine = new PosixParser().parse( options, new String[]{"-d", "true"} );

        assertTrue(commandLine.hasOption( "--" + debugOpt ));
    }
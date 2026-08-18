// org.apache.commons.cli.BugsTest::test15648
    public void test15648() throws Exception
    {
        CommandLineParser parser = new PosixParser();
        final String[] args = new String[] { "-m", "\"Two Words\"" };
        Option m = OptionBuilder.hasArgs().create("m");
        Options options = new Options();
        options.addOption( m );
        CommandLine line = parser.parse( options, args );
        assertEquals( "Two Words", line.getOptionValue( "m" ) );
    }
// org.apache.commons.cli.OptionGroupTest::testSingleLongOption
    public void testSingleLongOption() throws Exception
    {
        String[] args = new String[] { "--file" };

        CommandLine cl = parser.parse( _options, args);

        assertTrue( "Confirm -r is NOT set", !cl.hasOption("r") );
        assertTrue( "Confirm -f is set", cl.hasOption("f") );
        assertTrue( "Confirm -d is NOT set", !cl.hasOption("d") );
        assertTrue( "Confirm -s is NOT set", !cl.hasOption("s") );
        assertTrue( "Confirm -c is NOT set", !cl.hasOption("c") );
        assertTrue( "Confirm no extra args", cl.getArgList().size() == 0);
    }
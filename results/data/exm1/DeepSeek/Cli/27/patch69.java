// org.apache.commons.cli.OptionGroupTest::testNoOptionsExtraArgs
    public void testNoOptionsExtraArgs() throws Exception
    {
        String[] args = new String[] { "arg1", "arg2" };

        CommandLine cl = parser.parse( _options, args);

        assertTrue( "Confirm -r is NOT set", !cl.hasOption("r") );
        assertTrue( "Confirm -f is NOT set", !cl.hasOption("f") );
        assertTrue( "Confirm -d is NOT set", !cl.hasOption("d") );
        assertTrue( "Confirm -s is NOT set", !cl.hasOption("s") );
        assertTrue( "Confirm -c is NOT set", !cl.hasOption("c") );
        assertTrue( "Confirm TWO extra args", cl.getArgList().size() == 2);
    }
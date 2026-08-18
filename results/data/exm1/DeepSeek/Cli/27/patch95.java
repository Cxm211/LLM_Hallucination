// org.apache.commons.cli.ParserTestCase::testSimpleLong
    public void testSimpleLong() throws Exception
    {
        String[] args = new String[] { "--enable-a",
                                       "--bfile", "toast",
                                       "foo", "bar" };

        CommandLine cl = parser.parse(options, args);

        assertTrue( "Confirm -a is set", cl.hasOption("a") );
        assertTrue( "Confirm -b is set", cl.hasOption("b") );
        assertTrue( "Confirm arg of -b", cl.getOptionValue("b").equals("toast") );
        assertTrue( "Confirm arg of --bfile", cl.getOptionValue( "bfile" ).equals( "toast" ) );
        assertTrue( "Confirm size of extra args", cl.getArgList().size() == 2);
    }
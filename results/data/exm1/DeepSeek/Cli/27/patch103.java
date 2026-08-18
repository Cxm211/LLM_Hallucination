// org.apache.commons.cli.ParserTestCase::testStopAtExpectedArg
   public void testStopAtExpectedArg() throws Exception
    {
        String[] args = new String[]{"-b", "foo"};

        CommandLine cl = parser.parse(options, args, true);

        assertTrue("Confirm -b is set", cl.hasOption('b'));
        assertEquals("Confirm -b is set", "foo", cl.getOptionValue('b'));
        assertTrue("Confirm no extra args: " + cl.getArgList().size(), cl.getArgList().size() == 0);
    }
// org.apache.commons.cli.ParserTestCase::testSimpleShort
    public void testSimpleShort() throws Exception
    {
        String[] args = new String[] { "-a",
                                       "-b", "toast",
                                       "foo", "bar" };

        CommandLine cl = parser.parse(options, args);

        assertTrue("Confirm -a is set", cl.hasOption("a"));
        assertTrue("Confirm -b is set", cl.hasOption("b"));
        assertTrue("Confirm arg of -b", cl.getOptionValue("b").equals("toast"));
        assertTrue("Confirm size of extra args", cl.getArgList().size() == 2);
    }
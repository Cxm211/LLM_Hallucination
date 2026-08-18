// org.apache.commons.cli.ParserTestCase::testDoubleDash1
    public void testDoubleDash1() throws Exception
    {
        String[] args = new String[] { "--copt",
                                       "--",
                                       "-b", "toast" };

        CommandLine cl = parser.parse(options, args);

        assertTrue("Confirm -c is set", cl.hasOption("c"));
        assertTrue("Confirm -b is not set", !cl.hasOption("b"));
        assertTrue("Confirm 2 extra args: " + cl.getArgList().size(), cl.getArgList().size() == 2);
    }
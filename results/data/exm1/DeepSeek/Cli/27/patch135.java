// org.apache.commons.cli.ParserTestCase::testStopBursting2
    public void testStopBursting2() throws Exception
    {
        String[] args = new String[] { "-c", "foobar", "-btoast" };

        CommandLine cl = parser.parse(options, args, true);
        assertTrue("Confirm -c is set", cl.hasOption("c"));
        assertTrue("Confirm  2 extra args: " + cl.getArgList().size(), cl.getArgList().size() == 2);

        cl = parser.parse(options, cl.getArgs());

        assertTrue("Confirm -c is not set", !cl.hasOption("c"));
        assertTrue("Confirm -b is set", cl.hasOption("b"));
        assertTrue("Confirm arg of -b", cl.getOptionValue("b").equals("toast"));
        assertTrue("Confirm  1 extra arg: " + cl.getArgList().size(), cl.getArgList().size() == 1);
        assertTrue("Confirm  value of extra arg: " + cl.getArgList().get(0), cl.getArgList().get(0).equals("foobar"));
    }
// org/apache/commons/cli/PosixParserTest.java::testStopAfterRecognizedOption
public void testStopAfterRecognizedOption() throws Exception
    {
        String[] args = new String[]{"-a", "-z", "foo"};

        CommandLine cl = parser.parse(options, args, true);
        assertTrue("Confirm -a is set", cl.hasOption("a"));
        assertEquals("Confirm 2 extra args: " + cl.getArgList().size(), 2, cl.getArgList().size());
    }
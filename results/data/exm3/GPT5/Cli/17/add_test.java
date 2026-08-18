// org/apache/commons/cli/PosixParserTest.java::testStopBursting
public void testStopBurstingAtFirstNonOption() throws Exception
    {
        String[] args = new String[] { "-za" };

        CommandLine cl = parser.parse(options, args, true);
        assertFalse("Confirm -a is not set", cl.hasOption("a"));
        assertTrue("Confirm 1 extra arg", cl.getArgList().size() == 1);
        assertTrue(cl.getArgList().contains("za"));
    }
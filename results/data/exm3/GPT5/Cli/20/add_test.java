// org/apache/commons/cli/PosixParserTest.java::testStop3
public void testStopLongWithEqualsUnknown() throws Exception
    {
        String[] args = new String[]{"--zzz=1", "-a"};

        CommandLine cl = parser.parse(options, args, true);

        assertFalse("Confirm -a is not set", cl.hasOption("a"));
        assertEquals("Confirm 2 extra args", 2, cl.getArgList().size());
    }
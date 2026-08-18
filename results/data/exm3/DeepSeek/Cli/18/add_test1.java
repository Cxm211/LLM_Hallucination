// org/apache/commons/cli/PosixParserTest.java
public void testStopWithNonOptionAfter() throws Exception
    {
        String[] args = new String[]{"-z", "foo"};
        CommandLine cl = parser.parse(options, args, true);
        assertTrue("Confirm 2 extra args", cl.getArgList().size() == 2);
    }

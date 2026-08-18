// org/apache/commons/cli/PosixParserTest.java
public void testStopWithMultipleUnrecognized() throws Exception
    {
        String[] args = new String[]{"-x", "-y"};
        CommandLine cl = parser.parse(options, args, true);
        assertTrue("Confirm 2 extra args", cl.getArgList().size() == 2);
    }

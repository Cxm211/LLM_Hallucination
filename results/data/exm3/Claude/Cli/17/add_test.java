// org/apache/commons/cli/PosixParserTest.java
public void testStopBurstingAtEndOfString() throws Exception
{
    String[] args = new String[] { "-az" };

    CommandLine cl = parser.parse(options, args, true);
    assertTrue( "Confirm -a is set", cl.hasOption("a") );
    assertFalse( "Confirm -z is not set", cl.hasOption("z") );

    assertTrue( "Confirm  1 extra arg: " + cl.getArgList().size(), cl.getArgList().size() == 1);
    assertTrue(cl.getArgList().contains("z"));
}
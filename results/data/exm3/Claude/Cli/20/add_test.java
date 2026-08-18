// org/apache/commons/cli/PosixParserTest.java
public void testStopWithUnknownLongOptionNoEquals() throws Exception
{
    String[] args = new String[]{"--unknown", "-a"};

    CommandLine cl = parser.parse(options, args, true);

    assertFalse("Confirm -a is not set", cl.hasOption("a"));
    assertTrue("Confirm 2 extra args: " + cl.getArgList().size(), cl.getArgList().size() == 2);
    assertEquals("First arg should be --unknown", "--unknown", cl.getArgList().get(0));
    assertEquals("Second arg should be -a", "-a", cl.getArgList().get(1));
}
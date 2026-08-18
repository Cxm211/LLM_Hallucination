// org/apache/commons/cli/PosixParserTest.java
public void testStopWithMixedKnownUnknown() throws Exception
{
    options.addOption("x", true, "x option");
    String[] args = new String[]{"--x=1", "--unknown=2", "-a"};

    CommandLine cl = parser.parse(options, args, true);

    assertTrue("Confirm --x is set", cl.hasOption("x"));
    assertEquals("Confirm --x value", "1", cl.getOptionValue("x"));
    assertFalse("Confirm -a is not set", cl.hasOption("a"));
    assertTrue("Confirm 2 extra args: " + cl.getArgList().size(), cl.getArgList().size() == 2);
    assertEquals("First extra arg", "--unknown=2", cl.getArgList().get(0));
    assertEquals("Second extra arg", "-a", cl.getArgList().get(1));
}
// org/apache/commons/cli/PosixParserTest.java
public void testStopAtNonOptionWithKnownOption() throws Exception
{
    String[] args = new String[]{"-a", "-b", "toast"};

    CommandLine cl = parser.parse(options, args, true);
    assertTrue("Confirm -a is set", cl.hasOption("a"));
    assertTrue("Confirm -b is set", cl.hasOption("b"));
    assertEquals("Confirm 1 extra arg", 1, cl.getArgList().size());
    assertEquals("Arg should be toast", "toast", cl.getArgList().get(0));
}
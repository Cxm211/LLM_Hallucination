// org/apache/commons/cli/PosixParserTest.java
public void testStopAtNonOptionWithUnknownShortOption() throws Exception
{
    String[] args = new String[]{"-x", "arg1", "arg2"};

    CommandLine cl = parser.parse(options, args, true);
    assertFalse("Confirm -x is not set", cl.hasOption("x"));
    assertEquals("Confirm 3 extra args", 3, cl.getArgList().size());
    assertEquals("First arg should be -x", "-x", cl.getArgList().get(0));
    assertEquals("Second arg should be arg1", "arg1", cl.getArgList().get(1));
    assertEquals("Third arg should be arg2", "arg2", cl.getArgList().get(2));
}
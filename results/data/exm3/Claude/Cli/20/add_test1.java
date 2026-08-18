// org/apache/commons/cli/PosixParserTest.java
public void testStopWithKnownLongOptionWithEquals() throws Exception
{
    options.addOption("zop", true, "zop option");
    String[] args = new String[]{"--zop=value", "-a"};

    CommandLine cl = parser.parse(options, args, true);

    assertTrue("Confirm --zop is set", cl.hasOption("zop"));
    assertEquals("Confirm --zop value", "value", cl.getOptionValue("zop"));
    assertFalse("Confirm -a is not set", cl.hasOption("a"));
    assertTrue("Confirm 1 extra arg: " + cl.getArgList().size(), cl.getArgList().size() == 1);
}
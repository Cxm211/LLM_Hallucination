// org/apache/commons/cli/ParserTestCase.java
public void testStopAtNonOptionWithBurst() throws Exception
{
    String[] args = new String[]{"-xyz", "foo"};

    CommandLine cl = parser.parse(options, args, true);

    assertFalse("Confirm -x is not set", cl.hasOption('x'));
    assertFalse("Confirm -y is not set", cl.hasOption('y'));
    assertFalse("Confirm -z is not set", cl.hasOption('z'));
    assertEquals("Confirm 2 extra args", 2, cl.getArgList().size());
    assertEquals("Confirm first arg", "-xyz", cl.getArgs()[0]);
    assertEquals("Confirm second arg", "foo", cl.getArgs()[1]);
}
// org/apache/commons/cli/PosixParserTest.java
public void testStopBurstingMiddleNonOption() throws Exception {
    Options opts = new Options();
    opts.addOption("a", false, "");
    opts.addOption("b", false, "");
    opts.addOption("c", false, "");
    String[] args = new String[] { "-azbc" };
    CommandLine cl = parser.parse(opts, args, true);
    assertTrue("Confirm -a is set", cl.hasOption("a"));
    assertFalse("Confirm -b is not set", cl.hasOption("b"));
    assertFalse("Confirm -c is not set", cl.hasOption("c"));
    assertTrue("Confirm 1 extra arg: " + cl.getArgList().size(), cl.getArgList().size() == 1);
    assertTrue(cl.getArgList().contains("zbc"));
}

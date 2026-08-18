// org/apache/commons/cli/PosixParserTest.java
public void testStopBurstingStartingNonOption() throws Exception {
    Options opts = new Options();
    opts.addOption("a", false, "");
    opts.addOption("b", false, "");
    opts.addOption("c", false, "");
    String[] args = new String[] { "-zab" };
    CommandLine cl = parser.parse(opts, args, true);
    assertFalse("Confirm -a is not set", cl.hasOption("a"));
    assertFalse("Confirm -b is not set", cl.hasOption("b"));
    assertTrue("Confirm 1 extra arg: " + cl.getArgList().size(), cl.getArgList().size() == 1);
    assertTrue(cl.getArgList().contains("zab"));
}

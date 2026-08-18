// com/google/javascript/jscomp/CommandLineRunnerTest.java
public void testGetMsgWiringNoWarningsWhitespaceMode() throws Exception {
  args.add("--compilation_level=WHITESPACE_ONLY");
  test("/** @desc A bad foo. */ var MSG_FOO = 1;", "var MSG_FOO=1;");
}
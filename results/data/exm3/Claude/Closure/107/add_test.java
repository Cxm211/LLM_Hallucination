// com/google/javascript/jscomp/CommandLineRunnerTest.java
public void testGetMsgWiringNoWarningsSimpleMode() throws Exception {
  args.add("--compilation_level=SIMPLE_OPTIMIZATIONS");
  test("/** @desc A bad foo. */ var MSG_FOO = 1;", "var MSG_FOO=1;");
}
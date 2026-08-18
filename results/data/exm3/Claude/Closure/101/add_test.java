// com/google/javascript/jscomp/CommandLineRunnerTest.java
public void testProcessClosurePrimitivesWithRequire() {
  test("var goog = {}; goog.require('goog.dom');",
       "var goog = {};");
  args.add("--process_closure_primitives=false");
  testSame("var goog = {}; goog.require('goog.dom');");
}
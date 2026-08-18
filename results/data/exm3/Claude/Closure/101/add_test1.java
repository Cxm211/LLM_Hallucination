// com/google/javascript/jscomp/CommandLineRunnerTest.java
public void testProcessClosurePrimitivesExplicitTrue() {
  args.add("--process_closure_primitives=true");
  test("var goog = {}; goog.provide('goog.ui');",
       "var goog = {}; goog.ui = {};");
}
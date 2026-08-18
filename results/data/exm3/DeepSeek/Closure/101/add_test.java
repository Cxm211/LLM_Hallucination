// com/google/javascript/jscomp/CommandLineRunnerTest.java
public void testProcessClosurePrimitivesWithAdvanced() {
    args.add("--compilation_level=ADVANCED_OPTIMIZATIONS");
    args.add("--process_closure_primitives=false");
    testSame("var goog = {}; goog.provide('goog.dom');");
  }

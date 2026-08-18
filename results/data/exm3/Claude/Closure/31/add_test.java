// com/google/javascript/jscomp/CommandLineRunnerTest.java
public void testDependencySortingWithExternsAndNoCompile() {
    args.add("--manage_closure_dependencies");
    args.add("--compilation_level=WHITESPACE_ONLY");
    test(new String[] {
          "goog.require('lib');",
          "/** @externs */ goog.provide('lib');",
         },
         new String[] {
          "goog.require('lib');"
         });
  }
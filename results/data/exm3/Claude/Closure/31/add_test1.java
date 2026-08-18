// com/google/javascript/jscomp/CommandLineRunnerTest.java
public void testDependencySortingWithNoCompileAnnotation() {
    args.add("--manage_closure_dependencies");
    args.add("--compilation_level=WHITESPACE_ONLY");
    test(new String[] {
          "goog.require('app');",
          "/** @nocompile */ goog.provide('app');",
         },
         new String[] {
          "goog.require('app');"
         });
  }
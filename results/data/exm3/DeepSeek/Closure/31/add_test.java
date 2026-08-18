// com/google/javascript/jscomp/CommandLineRunnerTest.java
public void testDependencySortingSimpleMode() {
    args.add("--manage_closure_dependencies");
    args.add("--compilation_level=SIMPLE_OPTIMIZATIONS");
    test(new String[] {
          "goog.require('beer');",
          "goog.provide('beer');\\ngoog.require('hops');",
          "goog.provide('hops');",
         },
         new String[] {
          "goog.provide('hops');",
          "goog.provide('beer');\\ngoog.require('hops');",
          "goog.require('beer');"
         });
  }

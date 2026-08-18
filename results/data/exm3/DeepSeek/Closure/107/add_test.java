// com/google/javascript/jscomp/CommandLineRunnerTest.java
public void testJqueryPassInAdvancedMode() throws Exception {
    args.add("--compilation_level=ADVANCED_OPTIMIZATIONS");
    args.add("--process_jquery_primitives");
    test("jQuery.fn.foo = function() {};", "");
  }

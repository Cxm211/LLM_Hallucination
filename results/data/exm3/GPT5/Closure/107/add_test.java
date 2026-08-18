// com/google/javascript/jscomp/CommandLineRunnerTest.java::testNullExtraAnnotationNamesDoesNotCrash
public void testNullExtraAnnotationNamesDoesNotCrash() throws Exception {
    args.add("--compilation_level=SIMPLE_OPTIMIZATIONS");
    test("", "");
  }
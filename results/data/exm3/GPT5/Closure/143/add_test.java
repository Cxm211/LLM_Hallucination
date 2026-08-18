// com/google/javascript/jscomp/CommandLineRunnerTest.java::testDefineFlagEmptyString
public void testDefineFlagEmptyString() {
    args.add("--define=FOO=\"\"");
    test("/** @define {string} */ var FOO = \"a\";",
         "var FOO = \"\";");
  }
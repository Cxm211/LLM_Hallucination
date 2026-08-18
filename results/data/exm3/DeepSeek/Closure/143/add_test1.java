// com/google/javascript/jscomp/CommandLineRunnerTest.java
public void testDefineFlag5() {
    args.add("--define=FOO=\"\"");
    test("/** @define {string} */ var FOO = \"a\";",
         "var FOO = \"\";");
  }

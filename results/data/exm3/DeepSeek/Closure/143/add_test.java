// com/google/javascript/jscomp/CommandLineRunnerTest.java
public void testDefineFlag4() {
    args.add("--define=FOO=\"123\"");
    test("/** @define {string} */ var FOO = \"a\";",
         "var FOO = \"123\";");
  }

// com/google/javascript/jscomp/CompilerRunnerTest.java
public void testConstantPropagation() {
    CompilerRunner.FLAG_compilation_level.setForTest(
        CompilationLevel.SIMPLE_OPTIMIZATIONS);
    test("/** @const */ var x = 5; function f() { return x; }",
         "/** @const */ var x=5;function f(){return x}");
  }

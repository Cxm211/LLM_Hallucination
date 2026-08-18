// buggy function
  public void process(Node externs, Node root) {
    NodeTraversal.traverse(compiler, root, this);
    if (MAKE_LOCAL_NAMES_UNIQUE) {
      MakeDeclaredNamesUnique renamer = new MakeDeclaredNamesUnique();
      NodeTraversal t = new NodeTraversal(compiler, renamer);
      t.traverseRoots(externs, root);
    }
    removeDuplicateDeclarations(root);
    new PropogateConstantAnnotations(compiler, assertOnChange)
        .process(externs, root);
  }

// trigger testcase
// com/google/javascript/jscomp/CompilerRunnerTest.java::testIssue115
public void testIssue115() {
    CompilerRunner.FLAG_compilation_level.setForTest(
        CompilationLevel.SIMPLE_OPTIMIZATIONS);
    CompilerRunner.FLAG_warning_level.setForTest(
        WarningLevel.VERBOSE);
    test("function f() { " +
         "  var arguments = Array.prototype.slice.call(arguments, 0);" +
         "  return arguments[0]; " +
         "}",
         "function f() { " +
         "  arguments = Array.prototype.slice.call(arguments, 0);" +
         "  return arguments[0]; " +
         "}");
  }

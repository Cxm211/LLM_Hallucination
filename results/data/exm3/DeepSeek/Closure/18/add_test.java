// com/google/javascript/jscomp/IntegrationTest.java
public void testDependencySortingWithoutClosurePass() throws Exception {
    CompilerOptions options = createCompilerOptions();
    options.setClosurePass(false);
    options.setDependencyOptions(
        new DependencyOptions()
        .setDependencySorting(true));
    test(
        options,
        new String[] {
          "goog.require('x');",
          "goog.provide('x');",
        },
        new String[] {
          "goog.provide('x');",
          "goog.require('x');",
          "",
        });
  }

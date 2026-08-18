// com/google/javascript/jscomp/IntegrationTest.java
public void testDependencySortingMultipleFiles() throws Exception {
    CompilerOptions options = createCompilerOptions();
    options.setClosurePass(false);
    options.setDependencyOptions(
        new DependencyOptions()
        .setDependencySorting(true));
    test(
        options,
        new String[] {
          "goog.require('a');",
          "goog.provide('a'); goog.require('b');",
          "goog.provide('b');",
        },
        new String[] {
          "goog.provide('b');",
          "goog.provide('a'); goog.require('b');",
          "goog.require('a');",
          "",
        });
  }

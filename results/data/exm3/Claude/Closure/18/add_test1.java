// com/google/javascript/jscomp/IntegrationTest.java
public void testDependencySortingWithCircularDependency() throws Exception {
    CompilerOptions options = createCompilerOptions();
    options.setDependencyOptions(
        new DependencyOptions()
        .setDependencySorting(true));
    test(
        options,
        new String[] {
          "goog.provide('a'); goog.require('b');",
          "goog.provide('b'); goog.require('a');",
        },
        new String[] {
          "goog.provide('a'); goog.require('b');",
          "goog.provide('b'); goog.require('a');",
          "",
        });
  }
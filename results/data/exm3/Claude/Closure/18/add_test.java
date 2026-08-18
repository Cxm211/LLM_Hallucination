// com/google/javascript/jscomp/IntegrationTest.java
public void testDependencySortingWithMultipleRequires() throws Exception {
    CompilerOptions options = createCompilerOptions();
    options.setDependencyOptions(
        new DependencyOptions()
        .setDependencySorting(true));
    test(
        options,
        new String[] {
          "goog.require('y'); goog.require('x');",
          "goog.provide('y'); goog.require('x');",
          "goog.provide('x');",
        },
        new String[] {
          "goog.provide('x');",
          "goog.provide('y'); goog.require('x');",
          "goog.require('y'); goog.require('x');",
          "",
        });
  }
// com/google/javascript/jscomp/MakeDeclaredNamesUniqueTest.java
public void testRecursiveFunctionExpression() {
    test(
        "var f = function fact(n) { return n <= 1 ? 1 : n * fact(n-1); };",
        "var f = function fact$$1(n$$2) { return n$$2 <= 1 ? 1 : n$$2 * fact$$1(n$$2-1); };"
    );
  }

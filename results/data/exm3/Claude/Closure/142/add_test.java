// com/google/javascript/jscomp/CoalesceVariableNamesTest.java
public void testParameter5() {
  // Test with single parameter - should allow coalescing
  test("function FUNC(x) {var a,b; x; a=0; a; b=0; b}",
       "function FUNC(x) {var a; x; a=0; a; a=0; a}");
}
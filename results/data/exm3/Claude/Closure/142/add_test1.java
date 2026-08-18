// com/google/javascript/jscomp/CoalesceVariableNamesTest.java
public void testParameter6() {
  // Test with three parameters - should not coalesce to avoid IE bug
  test("function FUNC(x, y, z) {var a,b; x; y; z; a=0; a; b=0; b}",
       "function FUNC(x, y, z) {var a; x; y; z; a=0; a; a=0; a}");
}
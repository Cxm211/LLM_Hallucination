// com/google/javascript/jscomp/NormalizeTest.java
public void testMultipleVarDeclarationsWithSameName() {
  super.allowExternsChanges(true);
  test("var x; var x; var x;", "x = 1;", "x = 1;", null, null);
}
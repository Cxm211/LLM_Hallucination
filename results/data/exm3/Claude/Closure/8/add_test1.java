// com/google/javascript/jscomp/CollapseVariableDeclarationsTest.java
public void testCannotRedeclareCatchParameter() throws Exception {
  testSame("try {} catch(e) { var a=1; e=2; var b=3; }");
}
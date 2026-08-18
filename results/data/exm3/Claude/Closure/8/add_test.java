// com/google/javascript/jscomp/CollapseVariableDeclarationsTest.java
public void testCannotRedeclareFunctionName() throws Exception {
  testSame("function f(){ var b=1; f=2; var c; }");
}
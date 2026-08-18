// com/google/javascript/jscomp/CollapseVariableDeclarationsTest.java::testIssue820_additionalParamVarOrder
public void testIssue820_additionalParamVarOrder() throws Exception {
    testSame("function f(a){ a=2; var a; }");
  }
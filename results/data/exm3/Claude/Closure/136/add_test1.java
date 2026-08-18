// com/google/javascript/jscomp/MethodCheckTest.java
public void testIndirectMethodDefinition() {
  testSame("var helper = function() {}; var obj = {method: helper}; obj.method();");
}
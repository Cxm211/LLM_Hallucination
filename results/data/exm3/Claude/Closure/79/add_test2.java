// com/google/javascript/jscomp/VarCheckTest.java
public void testNestedPropReferenceInExterns() {
  testSame("obj.prop.nested;", "var obj;",
      VarCheck.UNDEFINED_EXTERN_VAR_ERROR);
}
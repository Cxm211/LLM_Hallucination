// com/google/javascript/jscomp/NodeUtilTest.java
public void testGetBooleanValueVoidWithSideEffects() {
  assertPureBooleanUnknown("void alert('test')");
  assertPureBooleanFalse("void 1");
  assertPureBooleanFalse("void null");
  assertPureBooleanFalse("void true");
  assertPureBooleanUnknown("void x");
}
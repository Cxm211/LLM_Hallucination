// com/google/javascript/jscomp/NodeUtilTest.java
public void testIsBooleanResultAdditional() {
  // Test nested operators
  assertTrue(NodeUtil.isBooleanResult(getNode("!delete a")));
  assertTrue(NodeUtil.isBooleanResult(getNode("(a, delete b)")));
  assertTrue(NodeUtil.isBooleanResult(getNode("x = (a < b)")));
  assertTrue(NodeUtil.isBooleanResult(getNode("x ? (a == b) : (c != d)")));
  assertFalse(NodeUtil.isBooleanResult(getNode("x ? 1 : 2")));
  assertFalse(NodeUtil.isBooleanResult(getNode("x ? true : 1")));
  assertFalse(NodeUtil.isBooleanResult(getNode("(a, 1)")));
  assertTrue(NodeUtil.isBooleanResult(getNode("b = true || false")));
  assertTrue(NodeUtil.isBooleanResult(getNode("b = true && false")));
}
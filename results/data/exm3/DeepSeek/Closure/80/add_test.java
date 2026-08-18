// com/google/javascript/jscomp/NodeUtilTest.java
public void testIsBooleanResultDeleteComputed() {
    assertTrue(NodeUtil.isBooleanResult(getNode("delete a[b]")));
  }

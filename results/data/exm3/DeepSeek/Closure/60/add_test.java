// com/google/javascript/jscomp/NodeUtilTest.java
public void testGetImpureBooleanValue() {
    assertImpureBooleanUnknown("void foo()");
    assertImpureBooleanFalse("void 0");
    assertImpureBooleanUnknown("[foo()]");
    assertImpureBooleanTrue("[]");
    assertImpureBooleanUnknown("{a:foo()}");
    assertImpureBooleanTrue("{}");
    assertImpureBooleanTrue("a = true");
    assertImpureBooleanTrue("(foo(), true)");
    assertImpureBooleanFalse("!true");
    assertImpureBooleanFalse("true && false");
    assertImpureBooleanTrue("true || false");
    assertImpureBooleanTrue("true ? 1 : 0");
    assertImpureBooleanFalse("false ? 1 : 0");
  }

  private void assertImpureBooleanTrue(String js) {
    Node n = parse(js);
    assertEquals(TernaryValue.TRUE, NodeUtil.getImpureBooleanValue(n));
  }
  private void assertImpureBooleanFalse(String js) {
    Node n = parse(js);
    assertEquals(TernaryValue.FALSE, NodeUtil.getImpureBooleanValue(n));
  }
  private void assertImpureBooleanUnknown(String js) {
    Node n = parse(js);
    assertEquals(TernaryValue.UNKNOWN, NodeUtil.getImpureBooleanValue(n));
  }

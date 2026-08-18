// com/google/javascript/jscomp/TypeCheckTest.java
public void testGetTypedPercentWithGetter() throws Exception {
    String js = "var obj = { get foo() { return 1; } };";
    assertEquals(100.0, getTypedPercent(js), 0.1);
  }

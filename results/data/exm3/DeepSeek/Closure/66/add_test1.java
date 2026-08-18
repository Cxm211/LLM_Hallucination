// com/google/javascript/jscomp/TypeCheckTest.java
public void testGetTypedPercentWithSetter() throws Exception {
    String js = "var obj = { set bar(x) { } };";
    assertEquals(100.0, getTypedPercent(js), 0.1);
  }

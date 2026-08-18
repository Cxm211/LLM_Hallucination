// com/google/javascript/jscomp/TypeCheckTest.java::testGetTypedPercent_getterSetterKeys
public void testGetTypedPercent_getterSetterKeys() throws Exception {
    String js = "var o = {get x() { return 1; }, set x(v) {}};";
    assertEquals(100.0, getTypedPercent(js), 0.1);
  }
// com/google/javascript/jscomp/TypeCheckTest.java
public void testGetTypedPercent8() throws Exception {
    String js = "var nested = {a: {b: 1}};";  
    assertEquals(100.0, getTypedPercent(js), 0.1);
  }
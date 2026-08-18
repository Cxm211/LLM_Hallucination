// com/google/javascript/jscomp/TypeCheckTest.java
public void testGetTypedPercent7() throws Exception {
    String js = "var obj = {x: 1, y: 2, z: 3};";  
    assertEquals(100.0, getTypedPercent(js), 0.1);
  }
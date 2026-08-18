// com/google/javascript/jscomp/CrossModuleCodeMotionTest.java
public void testEmptyRootModule() {
    JSModule empty = new JSModule("empty");
    JSModule m2 = new JSModule("m2");
    m2.add(JSSourceFile.fromCode("m2", "x()"));
    test(new JSModule[] {empty, m2}, new String[] {"", "x()"});
  }

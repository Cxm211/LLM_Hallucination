// com/google/javascript/jscomp/CrossModuleCodeMotionTest.java
public void testAllEmptyModules() {
    JSModule empty1 = new JSModule("empty1");
    JSModule empty2 = new JSModule("empty2");
    empty2.addDependency(empty1);
    JSModule empty3 = new JSModule("empty3");
    empty3.addDependency(empty2);
    
    test(new JSModule[] {empty1, empty2, empty3},
        new String[] {
          "",
          "",
          ""
    });
  }
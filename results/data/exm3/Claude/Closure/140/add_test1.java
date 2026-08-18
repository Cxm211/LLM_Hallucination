// com/google/javascript/jscomp/CrossModuleCodeMotionTest.java
public void testEmptyModuleWithNonEmptyDependency() {
    JSModule m1 = new JSModule("m1");
    m1.add(JSSourceFile.fromCode("m1", "var a = 1;"));
    
    JSModule empty = new JSModule("empty");
    empty.addDependency(m1);
    
    JSModule m2 = new JSModule("m2");
    m2.add(JSSourceFile.fromCode("m2", "var b = a;"));
    m2.addDependency(empty);
    
    test(new JSModule[] {m1, empty, m2},
        new String[] {
          "var a = 1;",
          "",
          "var b = a;"
    });
  }
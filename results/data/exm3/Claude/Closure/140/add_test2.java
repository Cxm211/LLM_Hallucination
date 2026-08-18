// com/google/javascript/jscomp/CrossModuleCodeMotionTest.java
public void testMultipleEmptyModulesInChain() {
    JSModule m1 = new JSModule("m1");
    m1.add(JSSourceFile.fromCode("m1", "function f() {}"));
    
    JSModule empty1 = new JSModule("empty1");
    empty1.addDependency(m1);
    
    JSModule empty2 = new JSModule("empty2");
    empty2.addDependency(empty1);
    
    JSModule m2 = new JSModule("m2");
    m2.add(JSSourceFile.fromCode("m2", "f();"));
    m2.addDependency(empty2);
    
    test(new JSModule[] {m1, empty1, empty2, m2},
        new String[] {
          "function f() {}",
          "",
          "",
          "f();"
    });
  }
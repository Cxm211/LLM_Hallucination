// com/google/javascript/jscomp/CheckGlobalThisTest.java
public void testInterfaceAssignment() {
    testSame("/** @interface */ var MyInterface = function() { this.bar = 10; };");
  }

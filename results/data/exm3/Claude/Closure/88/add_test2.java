// com/google/javascript/jscomp/DeadAssignmentsEliminationTest.java
public void testIssue297i() {
    testSame("function f() {" +
         " var x;" +
         " x=1; x=x+1; return x;" +
         "};");
  }
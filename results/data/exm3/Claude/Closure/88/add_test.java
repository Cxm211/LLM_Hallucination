// com/google/javascript/jscomp/DeadAssignmentsEliminationTest.java
public void testIssue297g() {
    testSame("function f(p) {" +
         " var x;" +
         " return (x=p) && (x=x.id) && x>0;" +
         "}; f('');" );
  }
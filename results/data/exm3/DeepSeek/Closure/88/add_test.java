// com/google/javascript/jscomp/DeadAssignmentsEliminationTest.java
public void testIssue297g() {
    test("function f() {" +
         " var x;" +
         " return (x=1) || (x = x + 2);" +
         "};",
         "function f() {" +
         " var x;" +
         " return (x=1) || (x + 2);" +
         "};");
  }

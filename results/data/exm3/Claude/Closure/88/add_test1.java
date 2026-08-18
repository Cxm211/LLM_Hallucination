// com/google/javascript/jscomp/DeadAssignmentsEliminationTest.java
public void testIssue297h() {
    test("function f() {" +
         " var x;" +
         " return (x=1) && (x = x+1);" +
         "};",
         "function f() {" +
         " var x;" +
         " return (x=1) && (x+1);" +
         "};");
  }
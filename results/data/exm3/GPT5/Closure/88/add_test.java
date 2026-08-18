// com/google/javascript/jscomp/DeadAssignmentsEliminationTest.java::testIssue297f
public void testIssue297g() {
    test("function f(a) {" +
         " return (a=1) + (a = g(a));" +
         "};",
         "function f(a) {" +
         " return (a=1) + (g(a));" +
         "};");
  }
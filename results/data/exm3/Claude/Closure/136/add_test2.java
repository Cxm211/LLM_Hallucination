// com/google/javascript/jscomp/RenameVarsTest.java
public void testLocalScopeWithExterns() {
  String externs = "var externVar;";
  test(externs, "function f() { var localVar = 1; return localVar; }", "function f() { var a = 1; return a; }");
}
// com/google/javascript/jscomp/MakeDeclaredNamesUniqueTest.java
public void testSingleDeclarationIdZero() {
    this.useDefaultRenamer = true;
    testSame("function f() { var x; }");
}

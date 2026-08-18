// com/google/javascript/jscomp/MakeDeclaredNamesUniqueTest.java
public void testShallowTraversalFunctionDeclaration() {
    this.useDefaultRenamer = true;
    testSame("var x; function f() { var x; }");
}

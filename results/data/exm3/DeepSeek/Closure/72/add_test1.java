// com/google/javascript/jscomp/InlineFunctionsTest.java
public void testInlineFunctionsUnreferencedUniqueLabel() {
    test("function foo(){ unique:{ 4; } }" +
        "bar:{ foo(); }",
        "bar:{ { JSCompiler_inline_label_0:{ 4; } } }");
}

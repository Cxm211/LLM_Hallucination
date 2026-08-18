// com/google/javascript/jscomp/InlineFunctionsTest.java
public void testInlineFunctionsDuplicateLabelWithBreak() {
    test("function foo(){ lab:{ break lab; } }" +
        "lab:{ foo(); }",
        "lab:{ { JSCompiler_inline_label_0:{ break JSCompiler_inline_label_0; } } }");
}

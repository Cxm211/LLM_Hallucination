// com/google/javascript/jscomp/FunctionInjectorTest.java
public void testIssue1101c() {
    helperCanInlineReferenceToFunction(CanInlineResult.NO,
        "function foo(a, b){return a + b;} foo(sideEffect(), x);", "foo",
        INLINE_DIRECT);
  }
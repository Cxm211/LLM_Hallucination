// com/google/javascript/jscomp/FunctionInjectorTest.java
public void testIssue1101c() {
    helperCanInlineReferenceToFunction(CanInlineResult.NO,
        "function foo(a){return a + a;} foo(sideEffect());", "foo",
        INLINE_DIRECT);
  }
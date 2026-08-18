// com/google/javascript/jscomp/FunctionInjectorTest.java
public void testIssue1101d() {
    helperCanInlineReferenceToFunction(CanInlineResult.YES,
        "function foo(a){return a + a;} foo(1);", "foo",
        INLINE_DIRECT);
  }
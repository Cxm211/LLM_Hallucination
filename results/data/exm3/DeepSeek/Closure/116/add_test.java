// com/google/javascript/jscomp/FunctionInjectorTest.java
public void testIssue1101c() {
    helperCanInlineReferenceToFunction(CanInlineResult.NO,
        "function foo(a){ x = 5; return a; } foo(x);", "foo",
        INLINE_DIRECT);
  }

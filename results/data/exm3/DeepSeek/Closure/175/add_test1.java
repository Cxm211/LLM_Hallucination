// com/google/javascript/jscomp/FunctionInjectorTest.java
public void testIssue1101d() {
    helperCanInlineReferenceToFunction(CanInlineResult.NO,
        "function foo(a){ x = 10; return a; } foo(x + 1);", "foo",
        INLINE_DIRECT);
  }

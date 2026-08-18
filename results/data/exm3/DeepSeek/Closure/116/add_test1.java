// com/google/javascript/jscomp/FunctionInjectorTest.java
public void testIssue1101d() {
    helperCanInlineReferenceToFunction(CanInlineResult.NO,
        "function foo(a){ obj.prop = 5; return a; } foo(obj.prop);", "foo",
        INLINE_DIRECT);
  }

// com/google/javascript/jscomp/FunctionInjectorTest.java
public void testIssue1101c() {
    helperCanInlineReferenceToFunction(CanInlineResult.NO,
        "function foo(a){ obj.prop = {}; return a; } foo(obj.prop.x);", "foo",
        INLINE_DIRECT);
  }

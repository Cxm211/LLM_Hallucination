// com/google/javascript/jscomp/FunctionInjectorTest.java::testIssue1101c
public void testIssue1101c() {
    helperCanInlineReferenceToFunction(CanInlineResult.NO,
        "function foo(a){return (side(),a)} foo(x);", "foo",
        INLINE_DIRECT);
  }
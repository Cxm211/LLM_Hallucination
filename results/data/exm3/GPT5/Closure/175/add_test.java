// com/google/javascript/jscomp/FunctionInjectorTest.java::testIssue1101a
public void testIssue1101c() {
    helperCanInlineReferenceToFunction(CanInlineResult.NO,
        "function foo(a){return ++x + a;} foo(x);", "foo",
        INLINE_DIRECT);
  }
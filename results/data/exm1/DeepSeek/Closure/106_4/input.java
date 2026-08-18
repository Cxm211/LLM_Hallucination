// buggy code
    boolean canCollapseUnannotatedChildNames() {
      if (type == Type.OTHER || globalSets != 1 || localSets != 0) {
        return false;
      }

      // Don't try to collapse if the one global set is a twin reference.
      // We could theoretically handle this case in CollapseProperties, but
      // it's probably not worth the effort.

      if (isClassOrEnum) {
        return true;
      }
      return (type == Type.FUNCTION || aliasingGets == 0) &&
          (parent == null || parent.canCollapseUnannotatedChildNames());
    }

  public boolean recordBlockDescription(String description) {
    if (parseDocumentation) {
    populated = true;
    }
    return currentInfo.documentBlock(description);
  }

// relevant test
// com.google.javascript.jscomp.TypedScopeCreatorTest::testTemplateType
  public void testTemplateType() {
    testSame(
        "\n" +
        "function bind(fn, thisObj) {}" +
        "\n" +
        "function Foo() {}\n" +
        "\n" +
        "Foo.prototype.baz = function() {};\n" +
        "bind(function() { var f = this.baz(); }, new Foo());");
    assertEquals("number", findNameType("f", lastLocalScope).toString());
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testClosureParameterTypesWithoutJSDoc
  public void testClosureParameterTypesWithoutJSDoc() {
    testSame(
        "\n" +
        "function foo(bar) {}\n" +
        "foo(function(baz) { var f = baz; })\n");
    assertEquals("Object", findNameType("f", lastLocalScope).toString());
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testClosureParameterTypesWithJSDoc
  public void testClosureParameterTypesWithJSDoc() {
    testSame(
        "\n" +
        "function foo(bar) {}\n" +
        "foo((" +
        "function(baz) { var f = baz; }))\n");
    assertEquals("string", findNameType("f", lastLocalScope).toString());
  }

// com.google.javascript.jscomp.VariableShadowDeclarationCheckTest::testNoWarnShadowGlobal
  public void testNoWarnShadowGlobal() {
    
    
    assertNoError("", "var x; function foo() { var x } ");
    assertNoError("var x", "function foo() { var x } ");
  }

// com.google.javascript.jscomp.VariableShadowDeclarationCheckTest::testWarnShadowLocal1
  public void testWarnShadowLocal1() {
    assertError("", "function a(){ var x; function b() { var x = 1; } }");
  }

// com.google.javascript.jscomp.VariableShadowDeclarationCheckTest::testWarnShadowLocal2
  public void testWarnShadowLocal2() {
    assertError("",
                "function a(){" +
                "   var x;" +
                "  function b() {" +
                "    var x = 1;" +
                "  }" +
                "}");
  }

// com.google.javascript.jscomp.VariableShadowDeclarationCheckTest::testUseShadowGlobals1
  public void testUseShadowGlobals1() {
    assertNoError("", " var x; function foo() { x = 1 } ");
    assertNoError("", "function a() { var x; function b() { x = 1; } }");
  }

// com.google.javascript.jscomp.VariableShadowDeclarationCheckTest::testNoShadowAnnotation
  public void testNoShadowAnnotation() {
    assertError("",
                " var x; function a() { var x } ");

    assertError("",
                " var x; function a() {function b(){var x}} ");
  }

// com.google.javascript.jscomp.VariableShadowDeclarationCheckTest::testNoShadowAnnotationInExterns1
  public void testNoShadowAnnotationInExterns1() {
    assertError(" var x",
                "function a() { var x } ");
  }

// com.google.javascript.jscomp.VariableShadowDeclarationCheckTest::testNoShadowAnnotationInExterns2
  public void testNoShadowAnnotationInExterns2() {
    assertError(" var x",
                "function a() {function b(){var x}} ");
  }

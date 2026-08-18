// buggy code
  static String strEscape(String s, char quote,
                          String doublequoteEscape,
                          String singlequoteEscape,
                          String backslashEscape,
                          CharsetEncoder outputCharsetEncoder) {
    StringBuilder sb = new StringBuilder(s.length() + 2);
    sb.append(quote);
    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      switch (c) {
        case '\0': sb.append("\\0"); break;
        case '\n': sb.append("\\n"); break;
        case '\r': sb.append("\\r"); break;
        case '\t': sb.append("\\t"); break;
        case '\\': sb.append(backslashEscape); break;
        case '\"': sb.append(doublequoteEscape); break;
        case '\'': sb.append(singlequoteEscape); break;
        case '>':                       // Break --> into --\> or ]]> into ]]\>
          if (i >= 2 &&
              ((s.charAt(i - 1) == '-' && s.charAt(i - 2) == '-') ||
               (s.charAt(i - 1) == ']' && s.charAt(i - 2) == ']'))) {
            sb.append("\\>");
          } else {
            sb.append(c);
          }
          break;
        case '<':
          // Break </script into <\/script
          final String END_SCRIPT = "/script";

          // Break <!-- into <\!--
          final String START_COMMENT = "!--";

          if (s.regionMatches(true, i + 1, END_SCRIPT, 0,
                              END_SCRIPT.length())) {
            sb.append("<\\");
          } else if (s.regionMatches(false, i + 1, START_COMMENT, 0,
                                     START_COMMENT.length())) {
            sb.append("<\\");
          } else {
            sb.append(c);
          }
          break;
        default:
          // If we're given an outputCharsetEncoder, then check if the
          //  character can be represented in this character set.
          if (outputCharsetEncoder != null) {
            if (outputCharsetEncoder.canEncode(c)) {
              sb.append(c);
            } else {
              // Unicode-escape the character.
              appendHexJavaScriptRepresentation(sb, c);
            }
          } else {
            // No charsetEncoder provided - pass straight latin characters
            // through, and escape the rest.  Doing the explicit character
            // check is measurably faster than using the CharsetEncoder.
            if (c > 0x1f && c <= 0x7f) {
              sb.append(c);
            } else {
              // Other characters can be misinterpreted by some js parsers,
              // or perhaps mangled by proxies along the way,
              // so we play it safe and unicode escape them.
              appendHexJavaScriptRepresentation(sb, c);
            }
          }
      }
    }
    sb.append(quote);
    return sb.toString();
  }

// relevant test
// com.google.javascript.jscomp.CollapsePropertiesTest::testGlobalVarSetToObjLitConditionally2
  public void testGlobalVarSetToObjLitConditionally2() {
    test("if (x) var a = {b: 0}; var c = a.b; var d = a.c;",
         "if (x){ var a$b = 0; var a = {}; }var c = a$b; var d = a.c;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testGlobalVarSetToObjLitConditionally3
  public void testGlobalVarSetToObjLitConditionally3() {
    testSame("var a; if (x) a = {b: 0}; else a = {b: 1}; var c = a.b;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testObjectPropertySetToObjLitConditionally
  public void testObjectPropertySetToObjLitConditionally() {
    test("var a = {}; if (x) a.b = {c: 0}; var d = a.b ? a.b.c : 0;",
         "if (x){ var a$b$c = 0; var a$b = {} } var d = a$b ? a$b$c : 0;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testFunctionPropertySetToObjLitConditionally
  public void testFunctionPropertySetToObjLitConditionally() {
    test("function a() {} if (x) a.b = {c: 0}; var d = a.b ? a.b.c : 0;",
         "function a() {} if (x){ var a$b$c = 0; var a$b = {} }" +
         "var d = a$b ? a$b$c : 0;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testPrototypePropertySetToAnObjectLiteral
  public void testPrototypePropertySetToAnObjectLiteral() {
    test("var a = {b: function(){}}; a.b.prototype.c = {d: 0};",
         "var a$b = function(){}; a$b.prototype.c = {d: 0};");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testObjectPropertyResetInLocalScope
  public void testObjectPropertyResetInLocalScope() {
    test("var z = {}; z.a = 0; function f() {z.a = 5; return z.a}",
         "var z$a = 0; function f() {z$a = 5; return z$a}");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testFunctionPropertyResetInLocalScope
  public void testFunctionPropertyResetInLocalScope() {
    test("function z() {} z.a = 0; function f() {z.a = 5; return z.a}",
         "function z() {} var z$a = 0; function f() {z$a = 5; return z$a}");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testNamespaceResetInGlobalScope1
  public void testNamespaceResetInGlobalScope1() {
    test("var a = {}; a.b = function() {}; a = {};",
         "var a = {}; var a$b = function() {}; a = {};",
         null, CollapseProperties.NAMESPACE_REDEFINED_WARNING);
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testNamespaceResetInGlobalScope2
  public void testNamespaceResetInGlobalScope2() {
    test("var a = {}; a = {}; a.b = function() {};",
         "var a = {}; a = {}; var a$b = function() {};",
         null, CollapseProperties.NAMESPACE_REDEFINED_WARNING);
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testNamespaceResetInLocalScope1
  public void testNamespaceResetInLocalScope1() {
    test("var a = {}; a.b = function() {};" +
         " function f() { a = {}; }",
         "var a = {};var a$b = function() {};" +
         " function f() { a = {}; }",
         null, CollapseProperties.NAMESPACE_REDEFINED_WARNING);
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testNamespaceResetInLocalScope2
  public void testNamespaceResetInLocalScope2() {
    test("var a = {}; function f() { a = {}; }" +
         " a.b = function() {};",
         "var a = {}; function f() { a = {}; }" +
         " var a$b = function() {};",
         null, CollapseProperties.NAMESPACE_REDEFINED_WARNING);
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testNamespaceDefinedInLocalScope
  public void testNamespaceDefinedInLocalScope() {
    test("var a = {}; (function() { a.b = {}; })();" +
         " a.b.c = function() {};",
         "var a$b; (function() { a$b = {}; })(); var a$b$c = function() {};");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testAddPropertyToObjectInLocalScopeDepth1
  public void testAddPropertyToObjectInLocalScopeDepth1() {
    test("var a = {b: 0}; function f() { a.c = 5; return a.c; }",
         "var a$b = 0; var a$c; function f() { a$c = 5; return a$c; }");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testAddPropertyToObjectInLocalScopeDepth2
  public void testAddPropertyToObjectInLocalScopeDepth2() {
    test("var a = {}; a.b = {}; (function() {a.b.c = 0;})(); x = a.b.c;",
         "var a$b$c; (function() {a$b$c = 0;})(); x = a$b$c;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testAddPropertyToFunctionInLocalScopeDepth1
  public void testAddPropertyToFunctionInLocalScopeDepth1() {
    test("function a() {} function f() { a.c = 5; return a.c; }",
         "function a() {} var a$c; function f() { a$c = 5; return a$c; }");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testAddPropertyToFunctionInLocalScopeDepth2
  public void testAddPropertyToFunctionInLocalScopeDepth2() {
    test("var a = {}; a.b = function() {}; function f() {a.b.c = 0;}",
         "var a$b = function() {}; var a$b$c; function f() {a$b$c = 0;}");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testAddPropertyToUncollapsibleObjectInLocalScopeDepth1
  public void testAddPropertyToUncollapsibleObjectInLocalScopeDepth1() {
    testSame("var a = {}; var c = a; (function() {a.b = 0;})(); a.b;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testAddPropertyToUncollapsibleFunctionInLocalScopeDepth1
  public void testAddPropertyToUncollapsibleFunctionInLocalScopeDepth1() {
    testSame("function a() {} var c = a; (function() {a.b = 0;})(); a.b;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testAddPropertyToUncollapsibleNamedCtorInLocalScopeDepth1
  public void testAddPropertyToUncollapsibleNamedCtorInLocalScopeDepth1() {
    testSame(
          " function a() {} var a$b; var c = a; " +
          "(function() {a$b = 0;})(); a$b;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testAddPropertyToUncollapsibleCtorInLocalScopeDepth1
  public void testAddPropertyToUncollapsibleCtorInLocalScopeDepth1() {
    test(" var a = function() {}; var c = a; " +
         "(function() {a.b = 0;})(); a.b;",
         "var a = function() {}; var a$b; " +
         "var c = a; (function() {a$b = 0;})(); a$b;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testAddPropertyToUncollapsibleObjectInLocalScopeDepth2
  public void testAddPropertyToUncollapsibleObjectInLocalScopeDepth2() {
    test("var a = {}; a.b = {}; var d = a.b;" +
         "(function() {a.b.c = 0;})(); a.b.c;",
         "var a$b = {}; var d = a$b;" +
         "(function() {a$b.c = 0;})(); a$b.c;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testAddPropertyToUncollapsibleFunctionInLocalScopeDepth2
  public void testAddPropertyToUncollapsibleFunctionInLocalScopeDepth2() {
    test("var a = {}; a.b = function (){}; var d = a.b;" +
         "(function() {a.b.c = 0;})(); a.b.c;",
         "var a$b = function (){}; var d = a$b;" +
         "(function() {a$b.c = 0;})(); a$b.c;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testAddPropertyToUncollapsibleCtorInLocalScopeDepth2
  public void testAddPropertyToUncollapsibleCtorInLocalScopeDepth2() {
    test("var a = {};  a.b = function (){}; var d = a.b;" +
         "(function() {a.b.c = 0;})(); a.b.c;",
         "var a$b = function (){}; var a$b$c; var d = a$b;" +
         "(function() {a$b$c = 0;})(); a$b$c;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testPropertyOfChildFuncOfUncollapsibleObjectDepth1
  public void testPropertyOfChildFuncOfUncollapsibleObjectDepth1() {
    testSame("var a = {}; var c = a; a.b = function (){}; a.b.x = 0; a.b.x;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testPropertyOfChildFuncOfUncollapsibleObjectDepth2
  public void testPropertyOfChildFuncOfUncollapsibleObjectDepth2() {
    test("var a = {}; a.b = {}; var c = a.b;" +
         "a.b.c = function (){}; a.b.c.x = 0; a.b.c.x;",
         "var a$b = {}; var c = a$b;" +
         "a$b.c = function (){}; a$b.c.x = 0; a$b.c.x;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testAddPropertyToChildFuncOfUncollapsibleObjectInLocalScope
  public void testAddPropertyToChildFuncOfUncollapsibleObjectInLocalScope() {
    testSame("var a = {}; a.b = function (){}; a.b.x = 0;" +
             "var c = a; (function() {a.b.y = 1;})(); a.b.x; a.b.y;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testAddPropertyToChildTypeOfUncollapsibleObjectInLocalScope
  public void testAddPropertyToChildTypeOfUncollapsibleObjectInLocalScope() {
    test("var a = {};  a.b = function (){}; a.b.x = 0;" +
         "var c = a; (function() {a.b.y = 1;})(); a.b.x; a.b.y;",
         "var a = {}; var a$b = function (){}; var a$b$y; var a$b$x = 0;" +
         "var c = a; (function() {a$b$y = 1;})(); a$b$x; a$b$y;",
         null, CollapseProperties.UNSAFE_NAMESPACE_WARNING);
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testAddPropertyToChildOfUncollapsibleFunctionInLocalScope
  public void testAddPropertyToChildOfUncollapsibleFunctionInLocalScope() {
    testSame(
        "function a() {} a.b = {x: 0}; var c = a;" +
        "(function() {a.b.y = 0;})(); a.b.y;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testAddPropertyToChildOfUncollapsibleCtorInLocalScope
  public void testAddPropertyToChildOfUncollapsibleCtorInLocalScope() {
    test(" var a = function() {}; a.b = {x: 0}; var c = a;" +
         "(function() {a.b.y = 0;})(); a.b.y;",
         "var a = function() {}; var a$b$x = 0; var a$b$y; var c = a;" +
         "(function() {a$b$y = 0;})(); a$b$y;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testResetObjectPropertyInLocalScope
  public void testResetObjectPropertyInLocalScope() {
    test("var a = {b: 0}; a.c = 1; function f() { a.c = 5; }",
         "var a$b = 0; var a$c = 1; function f() { a$c = 5; }");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testResetFunctionPropertyInLocalScope
  public void testResetFunctionPropertyInLocalScope() {
    test("function a() {}; a.c = 1; function f() { a.c = 5; }",
         "function a() {}; var a$c = 1; function f() { a$c = 5; }");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testGlobalNameReferencedInLocalScopeBeforeDefined1
  public void testGlobalNameReferencedInLocalScopeBeforeDefined1() {
    
    
    
    
    test("var a = {b: 0}; function f() { a.c = 5; } a.c = 1;",
         "var a$b = 0; function f() { a$c = 5; } var a$c = 1;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testGlobalNameReferencedInLocalScopeBeforeDefined2
  public void testGlobalNameReferencedInLocalScopeBeforeDefined2() {
    test("var a = {b: 0}; function f() { return a.c; } a.c = 1;",
         "var a$b = 0; function f() { return a$c; } var a$c = 1;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testTwiceDefinedGlobalNameDepth1_1
  public void testTwiceDefinedGlobalNameDepth1_1() {
    testSame("var a = {}; function f() { a.b(); }" +
             "a = function() {}; a.b = function() {};");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testTwiceDefinedGlobalNameDepth1_2
  public void testTwiceDefinedGlobalNameDepth1_2() {
    testSame("var a = {};  a = function() {};" +
             "a.b = {}; a.b.c = 0; function f() { a.b.d = 1; }");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testTwiceDefinedGlobalNameDepth2
  public void testTwiceDefinedGlobalNameDepth2() {
    test("var a = {}; a.b = {}; function f() { a.b.c(); }" +
         "a.b = function() {}; a.b.c = function() {};",
         "var a$b = {}; function f() { a$b.c(); }" +
         "a$b = function() {}; a$b.c = function() {};");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testFunctionCallDepth1
  public void testFunctionCallDepth1() {
    test("var a = {}; a.b = function(){}; var c = a.b();",
         "var a$b = function(){}; var c = a$b()");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testFunctionCallDepth2
  public void testFunctionCallDepth2() {
    test("var a = {}; a.b = {}; a.b.c = function(){}; a.b.c();",
         "var a$b$c = function(){}; a$b$c();");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testFunctionAlias
  public void testFunctionAlias() {
    test("var a = {}; a.b = {}; a.b.c = function(){}; a.b.d = a.b.c;",
         "var a$b$c = function(){}; var a$b$d = a$b$c;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testCallToRedefinedFunction
  public void testCallToRedefinedFunction() {
    test("var a = {}; a.b = function(){}; a.b = function(){}; a.b();",
         "var a$b = function(){}; a$b = function(){}; a$b();");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testCollapsePrototypeName
  public void testCollapsePrototypeName() {
    test("var a = {}; a.b = {}; a.b.c = function(){}; " +
         "a.b.c.prototype.d = function(){}; (new a.b.c()).d();",
         "var a$b$c = function(){}; a$b$c.prototype.d = function(){}; " +
         "new a$b$c().d();");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testReferencedPrototypeProperty
  public void testReferencedPrototypeProperty() {
    test("var a = {b: {}}; a.b.c = function(){}; a.b.c.prototype.d = {};" +
         "e = a.b.c.prototype.d;",
         "var a$b$c = function(){}; a$b$c.prototype.d = {};" +
         "e = a$b$c.prototype.d;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testSetStaticAndPrototypePropertiesOnFunction
  public void testSetStaticAndPrototypePropertiesOnFunction() {
    test("var a = {}; a.b = function(){}; a.b.prototype.d = 0; a.b.c = 1;",
         "var a$b = function(){}; a$b.prototype.d = 0; var a$b$c = 1;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testReadUndefinedPropertyDepth1
  public void testReadUndefinedPropertyDepth1() {
    test("var a = {b: 0}; var c = a.d;",
         "var a$b = 0; var a = {}; var c = a.d;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testReadUndefinedPropertyDepth2
  public void testReadUndefinedPropertyDepth2() {
    test("var a = {b: {c: 0}}; f(a.b.c); f(a.b.d);",
         "var a$b$c = 0; var a$b = {}; f(a$b$c); f(a$b.d);");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testCallUndefinedMethodOnObjLitDepth1
  public void testCallUndefinedMethodOnObjLitDepth1() {
    test("var a = {b: 0}; a.c();",
         "var a$b = 0; var a = {}; a.c();");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testCallUndefinedMethodOnObjLitDepth2
  public void testCallUndefinedMethodOnObjLitDepth2() {
    test("var a = {b: {}}; a.b.c = function() {}; a.b.c(); a.b.d();",
         "var a$b = {}; var a$b$c = function() {}; a$b$c(); a$b.d();");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testPropertiesOfAnUndefinedVar
  public void testPropertiesOfAnUndefinedVar() {
    testSame("a.document = d; f(a.document.innerHTML);");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testPropertyOfAnObjectThatIsNeitherFunctionNorObjLit
  public void testPropertyOfAnObjectThatIsNeitherFunctionNorObjLit() {
    testSame("var a = window; a.document = d; f(a.document)");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testStaticFunctionReferencingThis1
  public void testStaticFunctionReferencingThis1() {
    
    
    test("var a = {}; a.b = function() {this.c}; var d = a.b;",
         "var a$b = function() {this.c}; var d = a$b;", null, UNSAFE_THIS);
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testStaticFunctionReferencingThis2
  public void testStaticFunctionReferencingThis2() {
    
    
    test("var a = {}; " +
         "a.b = function() { return function(){ return this; }; };",
         "var a$b = function() { return function(){ return this; }; };");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testStaticFunctionReferencingThis3
  public void testStaticFunctionReferencingThis3() {
    test("var a = {b: function() {this.c}};",
         "var a$b = function() { this.c };", null, UNSAFE_THIS);
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testStaticFunctionReferencingThis4
  public void testStaticFunctionReferencingThis4() {
    test("var a = { b: function() {this.c}};",
         "var a$b = function() { this.c };");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testPrototypeMethodReferencingThis
  public void testPrototypeMethodReferencingThis() {
    testSame("var A = function(){}; A.prototype = {b: function() {this.c}};");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testConstructorReferencingThis
  public void testConstructorReferencingThis() {
    test("var a = {}; " +
         " a.b = function() { this.a = 3; };",
         "var a$b = function() { this.a = 3; };");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testSafeReferenceOfThis
  public void testSafeReferenceOfThis() {
    test("var a = {}; " +
         " a.b = function() { this.a = 3; };",
         "var a$b = function() { this.a = 3; };");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testGlobalFunctionReferenceOfThis
  public void testGlobalFunctionReferenceOfThis() {
    testSame("var a = function() { this.a = 3; };");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testFunctionGivenTwoNames
  public void testFunctionGivenTwoNames() {
    
    
    test("var f = function g() {}; f.a = 1; h(f.a);",
         "var f = function g() {}; var f$a = 1; h(f$a);");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testObjLitWithUsedNumericKey
  public void testObjLitWithUsedNumericKey() {
    testSame("a = {40: {}, c: {}}; var d = a[40]; var e = a.c;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testObjLitWithUnusedNumericKey
  public void testObjLitWithUnusedNumericKey() {
    test("var a = {40: {}, c: {}}; var e = a.c;",
         "var a$1 = {}; var a$c = {}; var e = a$c");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testObjLitWithNonIdentifierKeys
  public void testObjLitWithNonIdentifierKeys() {
    testSame("a = {' ': 0, ',': 1}; var c = a[' '];");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testChainedAssignments1
  public void testChainedAssignments1() {
    test("var x = {}; x.y = a = 0;",
         "var x$y = a = 0;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testChainedAssignments2
  public void testChainedAssignments2() {
    test("var x = {}; x.y = a = b = c();",
         "var x$y = a = b = c();");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testChainedAssignments3
  public void testChainedAssignments3() {
    test("var x = {y: 1}; a = b = x.y;",
         "var x$y = 1; a = b = x$y;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testChainedAssignments4
  public void testChainedAssignments4() {
    test("var x = {}; a = b = x.y;",
         "var x = {}; a = b = x.y;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testChainedAssignments5
  public void testChainedAssignments5() {
    test("var x = {}; a = x.y = 0;", "var x$y; a = x$y = 0;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testChainedAssignments6
  public void testChainedAssignments6() {
    test("var x = {}; a = x.y = b = c();",
         "var x$y; a = x$y = b = c();");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testChainedAssignments7
  public void testChainedAssignments7() {
    test("var x = {}; a = x.y = {};  x.y.z = function() {};",
         "var x$y; a = x$y = {}; var x$y$z = function() {};",
         null, CollapseProperties.UNSAFE_NAMESPACE_WARNING);
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testChainedVarAssignments1
  public void testChainedVarAssignments1() {
    test("var x = {y: 1}; var a = x.y = 0;",
         "var x$y = 1; var a = x$y = 0;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testChainedVarAssignments2
  public void testChainedVarAssignments2() {
    test("var x = {y: 1}; var a = x.y = b = 0;",
         "var x$y = 1; var a = x$y = b = 0;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testChainedVarAssignments3
  public void testChainedVarAssignments3() {
    test("var x = {y: {z: 1}}; var b = 0; var a = x.y.z = 1; var c = 2;",
         "var x$y$z = 1; var b = 0; var a = x$y$z = 1; var c = 2;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testChainedVarAssignments4
  public void testChainedVarAssignments4() {
    test("var x = {}; var a = b = x.y = 0;",
         "var x$y; var a = b = x$y = 0;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testChainedVarAssignments5
  public void testChainedVarAssignments5() {
    test("var x = {y: {}}; var a = b = x.y.z = 0;",
         "var x$y$z; var a = b = x$y$z = 0;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testPeerAndSubpropertyOfUncollapsibleProperty
  public void testPeerAndSubpropertyOfUncollapsibleProperty() {
    test("var x = {}; var a = x.y = 0; x.w = 1; x.y.z = 2;" +
         "b = x.w; c = x.y.z;",
         "var x$y; var a = x$y = 0; var x$w = 1; x$y.z = 2;" +
         "b = x$w; c = x$y.z;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testComplexAssignmentAfterInitialAssignment
  public void testComplexAssignmentAfterInitialAssignment() {
    test("var d = {}; d.e = {}; d.e.f = 0; a = b = d.e.f = 1;",
         "var d$e$f = 0; a = b = d$e$f = 1;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testRenamePrefixOfUncollapsibleProperty
  public void testRenamePrefixOfUncollapsibleProperty() {
    test("var d = {}; d.e = {}; a = b = d.e.f = 0;",
         "var d$e$f; a = b = d$e$f = 0;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testNewOperator
  public void testNewOperator() {
    
    
    test("var a = {}; a.b = function() {}; a.b.c = 1; var d = new a.b();",
         "var a$b = function() {}; var a$b$c = 1; var d = new a$b();");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testMethodCall
  public void testMethodCall() {
    test("var a = {}; a.b = function() {}; var d = a.b();",
         "var a$b = function() {}; var d = a$b();");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testObjLitDefinedInLocalScopeIsLeftAlone
  public void testObjLitDefinedInLocalScopeIsLeftAlone() {
    test("var a = {}; a.b = function() {};" +
         "a.b.prototype.f_ = function() {" +
         "  var x = { p: '', q: '', r: ''}; var y = x.q;" +
         "};",
         "var a$b = function() {};" +
         "a$b.prototype.f_ = function() {" +
         "  var x = { p: '', q: '', r: ''}; var y = x.q;" +
         "};");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testPropertiesOnBothSidesOfAssignment
  public void testPropertiesOnBothSidesOfAssignment() {
    
    
    
    test("var a = {b: 0}; a.c = a.b;", "var a$b = 0; var a$c = a$b;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testCallOnUndefinedProperty
  public void testCallOnUndefinedProperty() {
    
    
    
    
    test("var a = {}; a.b = function(){}; a.b.inherits(x);",
         "var a$b = function(){}; a$b.inherits(x);");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testGetPropOnUndefinedProperty
  public void testGetPropOnUndefinedProperty() {
    
    
    
    
    test("var a = {b: function(){}}; a.b.prototype.c =" +
         "function() { a.b.superClass_.c.call(this); }",
         "var a$b = function(){}; a$b.prototype.c =" +
         "function() { a$b.superClass_.c.call(this); }");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testLocalAlias1
  public void testLocalAlias1() {
    test("var a = {b: 3}; function f() { var x = a; f(x.b); }",
         "var a$b = 3; function f() { var x = null; f(a$b); }");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testLocalAlias2
  public void testLocalAlias2() {
    test("var a = {b: 3, c: 4}; function f() { var x = a; f(x.b); f(x.c);}",
         "var a$b = 3; var a$c = 4; " +
         "function f() { var x = null; f(a$b); f(a$c);}");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testLocalAlias3
  public void testLocalAlias3() {
    test("var a = {b: 3, c: {d: 5}}; " +
         "function f() { var x = a; f(x.b); f(x.c); f(x.c.d); }",
         "var a$b = 3; var a$c = {d: 5}; " +
         "function f() { var x = null; f(a$b); f(a$c); f(a$c.d);}");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testLocalAlias4
  public void testLocalAlias4() {
    test("var a = {b: 3}; var c = {d: 5}; " +
         "function f() { var x = a; var y = c; f(x.b); f(y.d); }",
         "var a$b = 3; var c$d = 5; " +
         "function f() { var x = null; var y = null; f(a$b); f(c$d);}");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testLocalAlias5
  public void testLocalAlias5() {
    test("var a = {b: {c: 5}}; " +
         "function f() { var x = a; var y = x.b; f(a.b.c); f(y.c); }",
         "var a$b$c = 5; " +
         "function f() { var x = null; var y = null; f(a$b$c); f(a$b$c);}");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testLocalAlias6
  public void testLocalAlias6() {
    test("var a = {b: 3}; function f() { var x = a; if (x.b) { f(x.b); } }",
         "var a$b = 3; function f() { var x = null; if (a$b) { f(a$b); } }");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testLocalAlias7
  public void testLocalAlias7() {
    test("var a = {b: {c: 5}}; function f() { var x = a.b; f(x.c); }",
         "var a$b$c = 5; function f() { var x = null; f(a$b$c); }");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testGlobalWriteToAncestor
  public void testGlobalWriteToAncestor() {
    testSame("var a = {b: 3}; function f() { var x = a; f(a.b); } a = 5;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testGlobalWriteToNonAncestor
  public void testGlobalWriteToNonAncestor() {
    test("var a = {b: 3}; function f() { var x = a; f(a.b); } a.b = 5;",
         "var a$b = 3; function f() { var x = null; f(a$b); } a$b = 5;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testLocalWriteToAncestor
  public void testLocalWriteToAncestor() {
    testSame("var a = {b: 3}; function f() { a = 5; var x = a; f(a.b); } ");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testLocalWriteToNonAncestor
  public void testLocalWriteToNonAncestor() {
    test("var a = {b: 3}; " +
         "function f() { a.b = 5; var x = a; f(a.b); }",
         "var a$b = 3; function f() { a$b = 5; var x = null; f(a$b); } ");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testNonWellformedAlias1
  public void testNonWellformedAlias1() {
    testSame("var a = {b: 3}; function f() { f(x); var x = a; f(x.b); }");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testNonWellformedAlias2
  public void testNonWellformedAlias2() {
    testSame("var a = {b: 3}; " +
             "function f() { if (false) { var x = a; f(x.b); } f(x); }");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testLocalAliasOfAncestor
  public void testLocalAliasOfAncestor() {
    testSame("var a = {b: {c: 5}}; function g() { f(a); } " +
             "function f() { var x = a.b; f(x.c); }");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testGlobalAliasOfAncestor
  public void testGlobalAliasOfAncestor() {
    testSame("var a = {b: {c: 5}}; var y = a; " +
             "function f() { var x = a.b; f(x.c); }");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testLocalAliasOfOtherName
  public void testLocalAliasOfOtherName() {
    testSame("var foo = function() { return {b: 3}; };" +
             "var a = foo(); a.b = 5; " +
             "function f() { var x = a.b; f(x); }");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testLocalAliasOfFunction
  public void testLocalAliasOfFunction() {
    test("var a = function() {}; a.b = 5; " +
         "function f() { var x = a.b; f(x); }",
         "var a = function() {}; var a$b = 5; " +
         "function f() { var x = null; f(a$b); }");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testNoInlineGetpropIntoCall
  public void testNoInlineGetpropIntoCall() {
    test("var b = x; function f() { var a = b; a(); }",
         "var b = x; function f() { var a = null; b(); }");
    test("var b = {}; b.c = x; function f() { var a = b.c; a(); }",
         "var b$c = x; function f() { var a = null; b$c(); }");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testCollapsePropertyOnExternType
  public void testCollapsePropertyOnExternType() {
    collapsePropertiesOnExternTypes = true;
    test("String.myFunc = function() {}; String.myFunc();",
         "var String$myFunc = function() {}; String$myFunc()");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testCollapseForEachWithoutExterns
  public void testCollapseForEachWithoutExterns() {
    collapsePropertiesOnExternTypes = true;
    test("function Array(){};\n",
         "if (!Array.forEach) {\n" +
         "  Array.forEach = function() {};\n" +
         "}",
         "if (!Array$forEach) {\n" +
         "  var Array$forEach = function() {};\n" +
         "}", null, null);
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testNoCollapseForEachInExterns
  public void testNoCollapseForEachInExterns() {
    collapsePropertiesOnExternTypes = true;
    test(" function Array() {}" +
         "Array.forEach = function() {}",
         "if (!Array.forEach) {\n" +
         "  Array.forEach = function() {};\n" +
         "}",
         "if (!Array.forEach) {\n" +
         "  Array.forEach = function() {};\n" +
         "}", null, null);
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testDoNotCollapsePropertyOnExternType
  public void testDoNotCollapsePropertyOnExternType() {
    collapsePropertiesOnExternTypes = false;
    test("String.myFunc = function() {}; String.myFunc()",
         "String.myFunc = function() {}; String.myFunc()");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testBug1704733
  public void testBug1704733() {
    String prelude =
        "function protect(x) { return x; }" +
        "function O() {}" +
        "protect(O).m1 = function() {};" +
        "protect(O).m2 = function() {};" +
        "protect(O).m3 = function() {};";

    testSame(prelude +
        "alert(O.m1); alert(O.m2()); alert(!O.m3);");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testBug1956277
  public void testBug1956277() {
    test("var CONST = {}; CONST.URL = 3;",
         "var CONST$URL = 3;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testBug1974371
  public void testBug1974371() {
    test(
        " var Foo = {A: {c: 2}, B: {c: 3}};" +
        "for (var key in Foo) {}",
        "var Foo$A = {c: 2}; var Foo$B = {c: 3};" +
        "var Foo = {A: Foo$A, B: Foo$B};" +
         "for (var key in Foo) {}");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testEnumOfObjects1
  public void testEnumOfObjects1() {
    test(
        COMMON_ENUM +
        "for (var key in Foo.A) {}",
         "var Foo$A = {c: 2}; var Foo$B$c = 3; for (var key in Foo$A) {}");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testEnumOfObjects2
  public void testEnumOfObjects2() {
    test(
        COMMON_ENUM +
        "foo(Foo.A.c);",
         "var Foo$A$c = 2; var Foo$B$c = 3; foo(Foo$A$c);");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testEnumOfObjects3
  public void testEnumOfObjects3() {
    test(
        "var x = {c: 2}; var y = {c: 3};" +
        " var Foo = {A: x, B: y};" +
        "for (var key in Foo) {}",
        "var x = {c: 2}; var y = {c: 3};" +
        "var Foo$A = x; var Foo$B = y; var Foo = {A: Foo$A, B: Foo$B};" +
        "for (var key in Foo) {}");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testEnumOfObjects4
  public void testEnumOfObjects4() {
    
    
    
    test(
        COMMON_ENUM +
        "for (var key in Foo) {} Foo.A = 3; alert(Foo.A);",
        "var Foo$A = {c: 2}; var Foo$B = {c: 3};" +
        "var Foo = {A: Foo$A, B: Foo$B};" +
        "for (var key in Foo) {} Foo$A = 3; alert(Foo$A);");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testObjectOfObjects1
  public void testObjectOfObjects1() {
    
    
    testSame(
        "var Foo = {a: {c: 2}, b: {c: 3}};" +
        "for (var key in Foo) {} Foo.a = 3; alert(Foo.a);");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testReferenceInAnonymousObject0
  public void testReferenceInAnonymousObject0() {
    test("var a = {};" +
         "a.b = function(){};" +
         "a.b.prototype.c = function(){};" +
         "var d = a.b.prototype.c;",
         "var a$b = function(){};" +
         "a$b.prototype.c = function(){};" +
         "var d = a$b.prototype.c;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testReferenceInAnonymousObject1
  public void testReferenceInAnonymousObject1() {
    test("var a = {};" +
         "a.b = function(){};" +
         "var d = a.b.prototype.c;",
         "var a$b = function(){};" +
         "var d = a$b.prototype.c;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testReferenceInAnonymousObject2
  public void testReferenceInAnonymousObject2() {
    test("var a = {};" +
         "a.b = function(){};" +
         "a.b.prototype.c = function(){};" +
         "var d = {c: a.b.prototype.c};",
         "var a$b = function(){};" +
         "a$b.prototype.c = function(){};" +
         "var d$c = a$b.prototype.c;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testReferenceInAnonymousObject3
  public void testReferenceInAnonymousObject3() {
    test("function CreateClass(a$$1) {}" +
         "var a = {};" +
         "a.b = function(){};" +
         "a.b.prototype.c = function(){};" +
         "a.d = CreateClass({c: a.b.prototype.c});",
         "function CreateClass(a$$1) {}" +
         "var a$b = function(){};" +
         "a$b.prototype.c = function(){};" +
         "var a$d = CreateClass({c: a$b.prototype.c});");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testReferenceInAnonymousObject4
  public void testReferenceInAnonymousObject4() {
    test("function CreateClass(a) {}" +
         "var a = {};" +
         "a.b = CreateClass({c: function() {}});" +
         "a.d = CreateClass({c: a.b.c});",
         "function CreateClass(a$$1) {}" +
         "var a$b = CreateClass({c: function() {}});" +
         "var a$d = CreateClass({c: a$b.c});");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testReferenceInAnonymousObject5
  public void testReferenceInAnonymousObject5() {
    test("function CreateClass(a) {}" +
         "var a = {};" +
         "a.b = CreateClass({c: function() {}});" +
         "a.d = CreateClass({c: a.b.prototype.c});",
         "function CreateClass(a$$1) {}" +
         "var a$b = CreateClass({c: function() {}});" +
         "var a$d = CreateClass({c: a$b.prototype.c});");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testCrashInCommaOperator
  public void testCrashInCommaOperator() {
    test("var a = {}; a.b = function() {},a.b();",
         "var a$b; a$b=function() {},a$b();");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testCrashInNestedAssign
  public void testCrashInNestedAssign() {
    test("var a = {}; if (a.b = function() {}) a.b();",
         "var a$b; if (a$b=function() {}) { a$b(); }");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testTwinReferenceCancelsChildCollapsing
  public void testTwinReferenceCancelsChildCollapsing() {
    test("var a = {}; if (a.b = function() {}) { a.b.c = 3; a.b(a.b.c); }",
         "var a$b; if (a$b = function() {}) { a$b.c = 3; a$b(a$b.c); }");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testPropWithDollarSign
  public void testPropWithDollarSign() {
    test("var a = {$: 3}", "var a$$0 = 3;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testPropWithDollarSign2
  public void testPropWithDollarSign2() {
    test("var a = {$: function(){}}", "var a$$0 = function(){};");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testPropWithDollarSign3
  public void testPropWithDollarSign3() {
    test("var a = {b: {c: 3}, b$c: function(){}}",
         "var a$b$c = 3; var a$b$0c = function(){};");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testPropWithDollarSign4
  public void testPropWithDollarSign4() {
    test("var a = {$$: {$$$: 3}};", "var a$$0$0$$0$0$0 = 3;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testPropWithDollarSign5
  public void testPropWithDollarSign5() {
    test("var a = {b: {$0c: true}, b$0c: false};",
         "var a$b$$00c = true; var a$b$00c = false;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testConstKey
  public void testConstKey() {
    test("var foo = {A: 3};", "var foo$A = 3;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testPropertyOnGlobalCtor
  public void testPropertyOnGlobalCtor() {
    test(" function Map() {} Map.foo = 3; Map;",
         "function Map() {} var Map$foo = 3; Map;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testPropertyOnGlobalFunction
  public void testPropertyOnGlobalFunction() {
    testSame("function Map() {} Map.foo = 3; Map;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testIssue389
  public void testIssue389() {
    test(
        "function alias() {}" +
        "var dojo = {};" +
        "dojo.gfx = {};" +
        "dojo.declare = function() {};" +
        "" +
        "dojo.gfx.Shape = function() {};" +
        "dojo.gfx.Shape = dojo.declare('dojo.gfx.Shape');" +
        "alias(dojo);",
        "function alias() {}" +
        "var dojo = {};" +
        "dojo.gfx = {};" +
        "dojo.declare = function() {};" +
        "" +
        "var dojo$gfx$Shape = function() {};" +
        "dojo$gfx$Shape = dojo.declare('dojo.gfx.Shape');" +
        "alias(dojo);",
        null,
        CollapseProperties.UNSAFE_NAMESPACE_WARNING);
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testAliasedTopLevelName
  public void testAliasedTopLevelName() {
    testSame(
        "function alias() {}" +
        "var dojo = {};" +
        "dojo.gfx = {};" +
        "dojo.declare = function() {};" +
        "dojo.gfx.Shape = {SQUARE: 2};" +
        "dojo.gfx.Shape = dojo.declare('dojo.gfx.Shape');" +
        "alias(dojo);" +
        "alias(dojo$gfx$Shape$SQUARE);");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testAliasedTopLevelEnum
  public void testAliasedTopLevelEnum() {
    test(
        "function alias() {}" +
        "var dojo = {};" +
        "dojo.gfx = {};" +
        "dojo.declare = function() {};" +
        "" +
        "dojo.gfx.Shape = {SQUARE: 2};" +
        "dojo.gfx.Shape = dojo.declare('dojo.gfx.Shape');" +
        "alias(dojo);" +
        "alias(dojo.gfx.Shape.SQUARE);",
        "function alias() {}" +
        "var dojo = {};" +
        "dojo.gfx = {};" +
        "dojo.declare = function() {};" +
        "" +
        "var dojo$gfx$Shape = {SQUARE: 2};" +
        "dojo$gfx$Shape = dojo.declare('dojo.gfx.Shape');" +
        "alias(dojo);" +
        "alias(dojo$gfx$Shape.SQUARE);",
        null,
        CollapseProperties.UNSAFE_NAMESPACE_WARNING);
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testAssignFunctionBeforeDefinition
  public void testAssignFunctionBeforeDefinition() {
    testSame(
        "f = function() {};" +
        "var f = null;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testObjectLitBeforeDefinition
  public void testObjectLitBeforeDefinition() {
    testSame(
        "a = {b: 3};" +
        "var a = null;" +
        "this.c = a.b;");
  }

// com.google.javascript.jscomp.CollapseVariableDeclarationsTest::testCollapsing
  public void testCollapsing() throws Exception {
    
    test("var a;var b;",
         "var a,b;");
    
    test("var a = 1;var b = 1;",
         "var a=1,b=1;");
    
    test("var a, b;",
         "var a,b;");
    
    test("var a = 1, b = 1;",
         "var a=1,b=1;");
    
    test("var a;var b, c;var d;",
         "var a,b,c,d;");
    
    test("var a = 1;var b = 2, c = 3;var d = 4;",
         "var a=1,b=2,c=3,d=4;");
  }

// com.google.javascript.jscomp.CollapseVariableDeclarationsTest::testIfElseVarDeclarations
  public void testIfElseVarDeclarations() throws Exception {
    testSame("if (x) var a = 1; else var b = 2;");
  }

// com.google.javascript.jscomp.CollapseVariableDeclarationsTest::testAggressiveRedeclaration
  public void testAggressiveRedeclaration() {
    test("var x = 2; foo(x);     x = 3; var y = 2;",
         "var x = 2; foo(x); var x = 3,     y = 2;");

    test("var x = 2; foo(x);     x = 3; x = 1; var y = 2;",
         "var x = 2; foo(x); var x = 3, x = 1,     y = 2;");

    test("var x = 2; foo(x);     x = 3; x = 1; var y = 2; var z = 4",
         "var x = 2; foo(x); var x = 3, x = 1,     y = 2,     z = 4");

    test("var x = 2; foo(x);     x = 3; x = 1; var y = 2; var z = 4; x = 5",
         "var x = 2; foo(x); var x = 3, x = 1,     y = 2,     z = 4, x = 5");
  }

// com.google.javascript.jscomp.CollapseVariableDeclarationsTest::testAggressiveRedeclarationInFor
  public void testAggressiveRedeclarationInFor() {
    testSame("for(var x = 1; x = 2; x = 3) {x = 4}");
    testSame("for(var x = 1; y = 2; z = 3) {var a = 4}");
    testSame("var x; for(x = 1; x = 2; z = 3) {x = 4}");
  }

// com.google.javascript.jscomp.CollapseVariableDeclarationsTest::testIssue397
  public void testIssue397() {
    test("var x; var y = 3; x = 5;",
         "var x, y = 3; x = 5;");
    testSame("var x; x = 5; var z = 7;");
    test("var x; var y = 3; x = 5; var z = 7;",
         "var x, y = 3; x = 5; var z = 7;");
    test("var a = 1; var x; var y = 3; x = 5;",
         "var a = 1, x, y = 3; x = 5;");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testWarningGuardOrdering1
  public void testWarningGuardOrdering1() {
    args.add("--jscomp_error=globalThis");
    args.add("--jscomp_off=globalThis");
    testSame("function f() { this.a = 3; }");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testWarningGuardOrdering2
  public void testWarningGuardOrdering2() {
    args.add("--jscomp_off=globalThis");
    args.add("--jscomp_error=globalThis");
    test("function f() { this.a = 3; }", CheckGlobalThis.GLOBAL_THIS);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testWarningGuardOrdering3
  public void testWarningGuardOrdering3() {
    args.add("--jscomp_warning=globalThis");
    args.add("--jscomp_off=globalThis");
    testSame("function f() { this.a = 3; }");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testWarningGuardOrdering4
  public void testWarningGuardOrdering4() {
    args.add("--jscomp_off=globalThis");
    args.add("--jscomp_warning=globalThis");
    test("function f() { this.a = 3; }", CheckGlobalThis.GLOBAL_THIS);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testCheckGlobalThisOffByDefault
  public void testCheckGlobalThisOffByDefault() {
    testSame("function f() { this.a = 3; }");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testCheckGlobalThisOnWithAdvancedMode
  public void testCheckGlobalThisOnWithAdvancedMode() {
    args.add("--compilation_level=ADVANCED_OPTIMIZATIONS");
    test("function f() { this.a = 3; }", CheckGlobalThis.GLOBAL_THIS);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testCheckGlobalThisOnWithErrorFlag
  public void testCheckGlobalThisOnWithErrorFlag() {
    args.add("--jscomp_error=globalThis");
    test("function f() { this.a = 3; }", CheckGlobalThis.GLOBAL_THIS);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testTypeCheckingOffByDefault
  public void testTypeCheckingOffByDefault() {
    test("function f(x) { return x; } f();",
         "function f(a) { return a; } f();");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testTypeCheckingOnWithVerbose
  public void testTypeCheckingOnWithVerbose() {
    args.add("--warning_level=VERBOSE");
    test("function f(x) { return x; } f();", TypeCheck.WRONG_ARGUMENT_COUNT);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testTypeParsingOffByDefault
  public void testTypeParsingOffByDefault() {
    testSame(" function f(a) { return a; }");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testTypeParsingOnWithVerbose
  public void testTypeParsingOnWithVerbose() {
    args.add("--warning_level=VERBOSE");
    test(" function f(a) { return a; }",
         RhinoErrorReporter.TYPE_PARSE_ERROR);
    test(" function f(a) { return a; }",
         RhinoErrorReporter.TYPE_PARSE_ERROR);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testTypeCheckOverride1
  public void testTypeCheckOverride1() {
    args.add("--warning_level=VERBOSE");
    args.add("--jscomp_off=checkTypes");
    testSame("var x = x || {}; x.f = function() {}; x.f(3);");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testTypeCheckOverride2
  public void testTypeCheckOverride2() {
    args.add("--warning_level=DEFAULT");
    testSame("var x = x || {}; x.f = function() {}; x.f(3);");

    args.add("--jscomp_warning=checkTypes");
    test("var x = x || {}; x.f = function() {}; x.f(3);",
         TypeCheck.WRONG_ARGUMENT_COUNT);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testCheckSymbolsOffForDefault
  public void testCheckSymbolsOffForDefault() {
    args.add("--warning_level=DEFAULT");
    test("x = 3; var y; var y;", "x=3; var y;");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testCheckSymbolsOnForVerbose
  public void testCheckSymbolsOnForVerbose() {
    args.add("--warning_level=VERBOSE");
    test("x = 3;", VarCheck.UNDEFINED_VAR_ERROR);
    test("var y; var y;", SyntacticScopeCreator.VAR_MULTIPLY_DECLARED_ERROR);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testCheckSymbolsOverrideForVerbose
  public void testCheckSymbolsOverrideForVerbose() {
    args.add("--warning_level=VERBOSE");
    args.add("--jscomp_off=undefinedVars");
    testSame("x = 3;");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testCheckUndefinedProperties1
  public void testCheckUndefinedProperties1() {
    args.add("--warning_level=VERBOSE");
    args.add("--jscomp_error=missingProperties");
    test("var x = {}; var y = x.bar;", TypeCheck.INEXISTENT_PROPERTY);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testCheckUndefinedProperties2
  public void testCheckUndefinedProperties2() {
    args.add("--warning_level=VERBOSE");
    args.add("--jscomp_off=missingProperties");
    test("var x = {}; var y = x.bar;", CheckGlobalNames.UNDEFINED_NAME_WARNING);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testCheckUndefinedProperties3
  public void testCheckUndefinedProperties3() {
    args.add("--warning_level=VERBOSE");
    test("function f() {var x = {}; var y = x.bar;}",
        TypeCheck.INEXISTENT_PROPERTY);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testDuplicateParams
  public void testDuplicateParams() {
    test("function f(a, a) {}", RhinoErrorReporter.DUPLICATE_PARAM);
    assertTrue(lastCompiler.hasHaltingErrors());
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testDefineFlag
  public void testDefineFlag() {
    args.add("--define=FOO");
    args.add("--define=\"BAR=5\"");
    args.add("--D"); args.add("CCC");
    args.add("-D"); args.add("DDD");
    test(" var FOO = false;" +
         " var BAR = 3;" +
         " var CCC = false;" +
         " var DDD = false;",
         "var FOO = !0, BAR = 5, CCC = !0, DDD = !0;");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testDefineFlag2
  public void testDefineFlag2() {
    args.add("--define=FOO='x\"'");
    test(" var FOO = \"a\";",
         "var FOO = \"x\\\"\";");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testDefineFlag3
  public void testDefineFlag3() {
    args.add("--define=FOO=\"x'\"");
    test(" var FOO = \"a\";",
         "var FOO = \"x'\";");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testScriptStrictModeNoWarning
  public void testScriptStrictModeNoWarning() {
    test("'use strict';", "");
    test("'no use strict';", CheckSideEffects.USELESS_CODE_ERROR);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testFunctionStrictModeNoWarning
  public void testFunctionStrictModeNoWarning() {
    test("function f() {'use strict';}", "function f() {}");
    test("function f() {'no use strict';}",
         CheckSideEffects.USELESS_CODE_ERROR);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testQuietMode
  public void testQuietMode() {
    args.add("--warning_level=DEFAULT");
    test(" var x;",
         RhinoErrorReporter.PARSE_ERROR);
    args.add("--warning_level=QUIET");
    testSame(" var x;");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testProcessClosurePrimitives
  public void testProcessClosurePrimitives() {
    test("var goog = {}; goog.provide('goog.dom');",
         "var goog = {dom:{}};");
    args.add("--process_closure_primitives=false");
    testSame("var goog = {}; goog.provide('goog.dom');");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testCssNameWiring
  public void testCssNameWiring() throws Exception {
    test("var goog = {}; goog.getCssName = function() {};" +
         "goog.setCssNameMapping = function() {};" +
         "goog.setCssNameMapping({'goog': 'a', 'button': 'b'});" +
         "var a = goog.getCssName('goog-button');" +
         "var b = goog.getCssName('css-button');" +
         "var c = goog.getCssName('goog-menu');" +
         "var d = goog.getCssName('css-menu');",
         "var goog = { getCssName: function() {}," +
         "             setCssNameMapping: function() {} }," +
         "    a = 'a-b'," +
         "    b = 'css-b'," +
         "    c = 'a-menu'," +
         "    d = 'css-menu';");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testIssue70
  public void testIssue70() {
    test("function foo({}) {}", RhinoErrorReporter.PARSE_ERROR);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testIssue81
  public void testIssue81() {
    args.add("--compilation_level=ADVANCED_OPTIMIZATIONS");
    useStringComparison = true;
    test("eval('1'); var x = eval; x('2');",
         "eval(\"1\");(0,eval)(\"2\");");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testIssue115
  public void testIssue115() {
    args.add("--compilation_level=SIMPLE_OPTIMIZATIONS");
    args.add("--warning_level=VERBOSE");
    test("function f() { " +
         "  var arguments = Array.prototype.slice.call(arguments, 0);" +
         "  return arguments[0]; " +
         "}",
         "function f() { " +
         "  arguments = Array.prototype.slice.call(arguments, 0);" +
         "  return arguments[0]; " +
         "}");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testIssue297
  public void testIssue297() {
    args.add("--compilation_level=SIMPLE_OPTIMIZATIONS");
    test("function f(p) {" +
         " var x;" +
         " return ((x=p.id) && (x=parseInt(x.substr(1))) && x>0);" +
         "}",
         "function f(b) {" +
         " var a;" +
         " return ((a=b.id) && (a=parseInt(a.substr(1))) && a>0);" +
         "}");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testDebugFlag1
  public void testDebugFlag1() {
    args.add("--compilation_level=SIMPLE_OPTIMIZATIONS");
    args.add("--debug=false");
    test("function foo(a) {}",
         "function foo() {}");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testDebugFlag2
  public void testDebugFlag2() {
    args.add("--compilation_level=SIMPLE_OPTIMIZATIONS");
    args.add("--debug=true");
    test("function foo(a) {alert(a)}",
         "function foo($a$$) {alert($a$$)}");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testDebugFlag3
  public void testDebugFlag3() {
    args.add("--compilation_level=ADVANCED_OPTIMIZATIONS");
    args.add("--warning_level=QUIET");
    args.add("--debug=false");
    test("function Foo() {}" +
         "Foo.x = 1;" +
         "function f() {throw new Foo().x;} f();",
         "throw (new function() {}).a;");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testDebugFlag4
  public void testDebugFlag4() {
    args.add("--compilation_level=ADVANCED_OPTIMIZATIONS");
    args.add("--warning_level=QUIET");
    args.add("--debug=true");
    test("function Foo() {}" +
        "Foo.x = 1;" +
        "function f() {throw new Foo().x;} f();",
        "throw (new function Foo() {}).$x$;");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testBooleanFlag1
  public void testBooleanFlag1() {
    args.add("--compilation_level=SIMPLE_OPTIMIZATIONS");
    args.add("--debug");
    test("function foo(a) {alert(a)}",
         "function foo($a$$) {alert($a$$)}");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testBooleanFlag2
  public void testBooleanFlag2() {
    args.add("--debug");
    args.add("--compilation_level=SIMPLE_OPTIMIZATIONS");
    test("function foo(a) {alert(a)}",
         "function foo($a$$) {alert($a$$)}");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testHelpFlag
  public void testHelpFlag() {
    args.add("--help");
    assertFalse(
        createCommandLineRunner(
            new String[] {"function f() {}"}).shouldRunCompiler());
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testExternsLifting1
  public void testExternsLifting1() throws Exception{
    String code = " function f() {}";
    test(new String[] {code},
         new String[] {});

    assertEquals(2, lastCompiler.getExternsForTesting().size());

    CompilerInput extern = lastCompiler.getExternsForTesting().get(1);
    assertNull(extern.getModule());
    assertTrue(extern.isExtern());
    assertEquals(code, extern.getCode());

    assertEquals(1, lastCompiler.getInputsForTesting().size());

    CompilerInput input = lastCompiler.getInputsForTesting().get(0);
    assertNotNull(input.getModule());
    assertFalse(input.isExtern());
    assertEquals("", input.getCode());
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testExternsLifting2
  public void testExternsLifting2() {
    args.add("--warning_level=VERBOSE");
    test(new String[] {" function f() {}", "f(3);"},
         new String[] {"f(3);"},
         TypeCheck.WRONG_ARGUMENT_COUNT);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testSourceSortingOff
  public void testSourceSortingOff() {
    test(new String[] {
          "goog.require('beer');",
          "goog.provide('beer');"
         }, ProcessClosurePrimitives.LATE_PROVIDE_ERROR);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testSourceSortingOn
  public void testSourceSortingOn() {
    args.add("--manage_closure_dependencies=true");
    test(new String[] {
          "goog.require('beer');",
          "goog.provide('beer');"
         },
         new String[] {
           "var beer = {};",
           ""
         });
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testSourceSortingCircularDeps1
  public void testSourceSortingCircularDeps1() {
    args.add("--manage_closure_dependencies=true");
    test(new String[] {
          "goog.provide('gin'); goog.require('tonic'); var gin = {};",
          "goog.provide('tonic'); goog.require('gin'); var tonic = {};",
          "goog.require('gin'); goog.require('tonic');"
         },
         JSModule.CIRCULAR_DEPENDENCY_ERROR);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testSourceSortingCircularDeps2
  public void testSourceSortingCircularDeps2() {
    args.add("--manage_closure_dependencies=true");
    test(new String[] {
          "goog.provide('roses.lime.juice');",
          "goog.provide('gin'); goog.require('tonic'); var gin = {};",
          "goog.provide('tonic'); goog.require('gin'); var tonic = {};",
          "goog.require('gin'); goog.require('tonic');",
          "goog.provide('gimlet');" +
          "     goog.require('gin'); goog.require('roses.lime.juice');"
         },
         JSModule.CIRCULAR_DEPENDENCY_ERROR);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testSourcePruningOn1
  public void testSourcePruningOn1() {
    args.add("--manage_closure_dependencies=true");
    test(new String[] {
          "goog.require('beer');",
          "goog.provide('beer');",
          "goog.provide('scotch'); var x = 3;"
         },
         new String[] {
           "var beer = {};",
           ""
         });
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testSourcePruningOn2
  public void testSourcePruningOn2() {
    args.add("--closure_entry_point=guinness");
    test(new String[] {
          "goog.provide('guinness');\ngoog.require('beer');",
          "goog.provide('beer');",
          "goog.provide('scotch'); var x = 3;"
         },
         new String[] {
           "var beer = {};",
           "var guinness = {};"
         });
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testSourcePruningOn3
  public void testSourcePruningOn3() {
    args.add("--closure_entry_point=scotch");
    test(new String[] {
          "goog.provide('guinness');\ngoog.require('beer');",
          "goog.provide('beer');",
          "goog.provide('scotch'); var x = 3;"
         },
         new String[] {
           "var scotch = {}, x = 3;",
         });
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testSourcePruningOn4
  public void testSourcePruningOn4() {
    args.add("--closure_entry_point=scotch");
    args.add("--closure_entry_point=beer");
    test(new String[] {
          "goog.provide('guinness');\ngoog.require('beer');",
          "goog.provide('beer');",
          "goog.provide('scotch'); var x = 3;"
         },
         new String[] {
           "var beer = {};",
           "var scotch = {}, x = 3;",
         });
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testSourcePruningOn5
  public void testSourcePruningOn5() {
    args.add("--closure_entry_point=shiraz");
    test(new String[] {
          "goog.provide('guinness');\ngoog.require('beer');",
          "goog.provide('beer');",
          "goog.provide('scotch'); var x = 3;"
         },
         Compiler.MISSING_ENTRY_ERROR);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testSourcePruningOn6
  public void testSourcePruningOn6() {
    args.add("--closure_entry_point=scotch");
    test(new String[] {
          "goog.require('beer');",
          "goog.provide('beer');",
          "goog.provide('scotch'); var x = 3;"
         },
         new String[] {
           "var beer = {};",
           "",
           "var scotch = {}, x = 3;",
         });
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testForwardDeclareDroppedTypes
  public void testForwardDeclareDroppedTypes() {
    args.add("--manage_closure_dependencies=true");

    args.add("--warning_level=VERBOSE");
    test(new String[] {
          "goog.require('beer');",
          "goog.provide('beer');  function f(x) {}",
          "goog.provide('Scotch'); var x = 3;"
         },
         new String[] {
           "var beer = {}; function f() {}",
           ""
         });

    test(new String[] {
          "goog.require('beer');",
          "goog.provide('beer');  function f(x) {}"
         },
         new String[] {
           "var beer = {}; function f() {}",
           ""
         },
         RhinoErrorReporter.TYPE_PARSE_ERROR);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testSourceMapExpansion1
  public void testSourceMapExpansion1() {
    args.add("--js_output_file");
    args.add("/path/to/out.js");
    args.add("--create_source_map=%outname%.map");
    testSame("var x = 3;");
    assertEquals("/path/to/out.js.map",
        lastCommandLineRunner.expandSourceMapPath(
            lastCompiler.getOptions(), null));
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testSourceMapExpansion2
  public void testSourceMapExpansion2() {
    useModules = ModulePattern.CHAIN;
    args.add("--create_source_map=%outname%.map");
    args.add("--module_output_path_prefix=foo");
    testSame(new String[] {"var x = 3;", "var y = 5;"});
    assertEquals("foo.map",
        lastCommandLineRunner.expandSourceMapPath(
            lastCompiler.getOptions(), null));
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testSourceMapExpansion3
  public void testSourceMapExpansion3() {
    useModules = ModulePattern.CHAIN;
    args.add("--create_source_map=%outname%.map");
    args.add("--module_output_path_prefix=foo_");
    testSame(new String[] {"var x = 3;", "var y = 5;"});
    assertEquals("foo_m0.js.map",
        lastCommandLineRunner.expandSourceMapPath(
            lastCompiler.getOptions(),
            lastCompiler.getModuleGraph().getRootModule()));
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testSourceMapFormat1
  public void testSourceMapFormat1() {
    args.add("--js_output_file");
    args.add("/path/to/out.js");
    testSame("var x = 3;");
    assertEquals(SourceMap.Format.DEFAULT,
        lastCompiler.getOptions().sourceMapFormat);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testCharSetExpansion
  public void testCharSetExpansion() {
    testSame("");
    assertEquals("US-ASCII", lastCompiler.getOptions().outputCharset);
    args.add("--charset=UTF-8");
    testSame("");
    assertEquals("UTF-8", lastCompiler.getOptions().outputCharset);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testChainModuleManifest
  public void testChainModuleManifest() throws Exception {
    useModules = ModulePattern.CHAIN;
    testSame(new String[] {
          "var x = 3;", "var y = 5;", "var z = 7;", "var a = 9;"});

    StringBuilder builder = new StringBuilder();
    lastCommandLineRunner.printModuleGraphManifestTo(
        lastCompiler.getModuleGraph(), builder);
    assertEquals(
        "{m0}\n" +
        "i0\n" +
        "\n" +
        "{m1:m0}\n" +
        "i1\n" +
        "\n" +
        "{m2:m1}\n" +
        "i2\n" +
        "\n" +
        "{m3:m2}\n" +
        "i3\n",
        builder.toString());
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testStarModuleManifest
  public void testStarModuleManifest() throws Exception {
    useModules = ModulePattern.STAR;
    testSame(new String[] {
          "var x = 3;", "var y = 5;", "var z = 7;", "var a = 9;"});

    StringBuilder builder = new StringBuilder();
    lastCommandLineRunner.printModuleGraphManifestTo(
        lastCompiler.getModuleGraph(), builder);
    assertEquals(
        "{m0}\n" +
        "i0\n" +
        "\n" +
        "{m1:m0}\n" +
        "i1\n" +
        "\n" +
        "{m2:m0}\n" +
        "i2\n" +
        "\n" +
        "{m3:m0}\n" +
        "i3\n",
        builder.toString());
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testVersionFlag
  public void testVersionFlag() {
    args.add("--version");
    testSame("");
    assertEquals(
        0,
        new String(errReader.toByteArray()).indexOf(
            "Closure Compiler (http://code.google.com/closure/compiler)\n" +
            "Version: "));
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testVersionFlag2
  public void testVersionFlag2() {
    lastArg = "--version";
    testSame("");
    assertEquals(
        0,
        new String(errReader.toByteArray()).indexOf(
            "Closure Compiler (http://code.google.com/closure/compiler)\n" +
            "Version: "));
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testPrintAstFlag
  public void testPrintAstFlag() {
    args.add("--print_ast=true");
    testSame("");
    assertEquals(
        "digraph AST {\n" +
        "  node [color=lightblue2, style=filled];\n" +
        "  node0 [label=\"BLOCK\"];\n" +
        "  node1 [label=\"SCRIPT\"];\n" +
        "  node0 -> node1 [weight=1];\n" +
        "  node1 -> RETURN [label=\"UNCOND\", " +
            "fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
        "  node0 -> RETURN [label=\"SYN_BLOCK\", " +
            "fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
        "  node0 -> node1 [label=\"UNCOND\", " +
            "fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
        "}\n\n",
        new String(outReader.toByteArray()));
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testSyntheticExterns
  public void testSyntheticExterns() {
    externs = ImmutableList.of(
        JSSourceFile.fromCode("externs", "myVar.property;"));
    test("var theirVar = {}; var myVar = {}; var yourVar = {};",
         VarCheck.UNDEFINED_EXTERN_VAR_ERROR);

    args.add("--jscomp_off=externsValidation");
    args.add("--warning_level=VERBOSE");
    test("var theirVar = {}; var myVar = {}; var yourVar = {};",
         "var theirVar={},myVar={},yourVar={};");

    args.add("--jscomp_off=externsValidation");
    args.add("--warning_level=VERBOSE");
    test("var theirVar = {}; var myVar = {}; var myVar = {};",
         SyntacticScopeCreator.VAR_MULTIPLY_DECLARED_ERROR);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testGoogAssertStripping
  public void testGoogAssertStripping() {
    args.add("--compilation_level=ADVANCED_OPTIMIZATIONS");
    test("goog.asserts.assert(false)",
         "");
    args.add("--debug");
    test("goog.asserts.assert(false)", "goog.$asserts$.$assert$(!1)");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testMissingReturnCheckOnWithVerbose
  public void testMissingReturnCheckOnWithVerbose() {
    args.add("--warning_level=VERBOSE");
    test(" function f() {f()} f();",
        CheckMissingReturn.MISSING_RETURN_STATEMENT);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testGenerateExports
  public void testGenerateExports() {
    args.add("--generate_exports=true");
    test(" foo.prototype.x = function() {};",
        "foo.prototype.x=function(){};"+
        "goog.exportSymbol(\"foo.prototype.x\",foo.prototype.x);");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testDepreciationWithVerbose
  public void testDepreciationWithVerbose() {
    args.add("--warning_level=VERBOSE");
    test(" function f() {}; f()",
       CheckAccessControls.DEPRECATED_NAME);
  }

// com.google.javascript.jscomp.ConstCheckTest::testConstantDefinition1
  public void testConstantDefinition1() {
    testSame("var XYZ = 1;");
  }

// com.google.javascript.jscomp.ConstCheckTest::testConstantDefinition2
  public void testConstantDefinition2() {
    testSame("var a$b$XYZ = 1;");
  }

// com.google.javascript.jscomp.ConstCheckTest::testConstantInitializedInAnonymousNamespace1
  public void testConstantInitializedInAnonymousNamespace1() {
    testSame("var XYZ; (function(){ XYZ = 1; })();");
  }

// com.google.javascript.jscomp.ConstCheckTest::testConstantInitializedInAnonymousNamespace2
  public void testConstantInitializedInAnonymousNamespace2() {
    testSame("var a$b$XYZ; (function(){ a$b$XYZ = 1; })();");
  }

// com.google.javascript.jscomp.ConstCheckTest::testObjectModified
  public void testObjectModified() {
    testSame("var IE = true, XYZ = {a:1,b:1}; if (IE) XYZ['c'] = 1;");
  }

// com.google.javascript.jscomp.ConstCheckTest::testObjectPropertyInitializedLate
  public void testObjectPropertyInitializedLate() {
    testSame("var XYZ = {}; for (var i = 0; i < 10; i++) { XYZ[i] = i; }");
  }

// com.google.javascript.jscomp.ConstCheckTest::testObjectRedefined1
  public void testObjectRedefined1() {
    testError("var XYZ = {}; XYZ = 2;");
  }

// com.google.javascript.jscomp.ConstCheckTest::testConstantRedefined1
  public void testConstantRedefined1() {
    testError("var XYZ = 1; XYZ = 2;");
  }

// com.google.javascript.jscomp.ConstCheckTest::testConstantRedefined2
  public void testConstantRedefined2() {
    testError("var a$b$XYZ = 1; a$b$XYZ = 2;");
  }

// com.google.javascript.jscomp.ConstCheckTest::testConstantRedefinedInLocalScope1
  public void testConstantRedefinedInLocalScope1() {
    testError("var XYZ = 1; (function(){ XYZ = 2; })();");
  }

// com.google.javascript.jscomp.ConstCheckTest::testConstantRedefinedInLocalScope2
  public void testConstantRedefinedInLocalScope2() {
    testError("var a$b$XYZ = 1; (function(){ a$b$XYZ = 2; })();");
  }

// com.google.javascript.jscomp.ConstCheckTest::testConstantRedefinedInLocalScopeOutOfOrder
  public void testConstantRedefinedInLocalScopeOutOfOrder() {
    testError("function f() { XYZ = 2; } var XYZ = 1;");
  }

// com.google.javascript.jscomp.ConstCheckTest::testConstantPostIncremented1
  public void testConstantPostIncremented1() {
    testError("var XYZ = 1; XYZ++;");
  }

// com.google.javascript.jscomp.ConstCheckTest::testConstantPostIncremented2
  public void testConstantPostIncremented2() {
    testError("var a$b$XYZ = 1; a$b$XYZ++;");
  }

// com.google.javascript.jscomp.ConstCheckTest::testConstantPreIncremented1
  public void testConstantPreIncremented1() {
    testError("var XYZ = 1; XYZ++;");
  }

// com.google.javascript.jscomp.ConstCheckTest::testConstantPreIncremented2
  public void testConstantPreIncremented2() {
    testError("var a$b$XYZ = 1; a$b$XYZ++;");
  }

// com.google.javascript.jscomp.ConstCheckTest::testConstantPostDecremented1
  public void testConstantPostDecremented1() {
    testError("var XYZ = 1; XYZ--;");
  }

// com.google.javascript.jscomp.ConstCheckTest::testConstantPostDecremented2
  public void testConstantPostDecremented2() {
    testError("var a$b$XYZ = 1; a$b$XYZ--;");
  }

// com.google.javascript.jscomp.ConstCheckTest::testConstantPreDecremented1
  public void testConstantPreDecremented1() {
    testError("var XYZ = 1; XYZ--;");
  }

// com.google.javascript.jscomp.ConstCheckTest::testConstantPreDecremented2
  public void testConstantPreDecremented2() {
    testError("var a$b$XYZ = 1; a$b$XYZ--;");
  }

// com.google.javascript.jscomp.ConstCheckTest::testAbbreviatedArithmeticAssignment1
  public void testAbbreviatedArithmeticAssignment1() {
    testError("var XYZ = 1; XYZ += 2;");
  }

// com.google.javascript.jscomp.ConstCheckTest::testAbbreviatedArithmeticAssignment2
  public void testAbbreviatedArithmeticAssignment2() {
    testError("var a$b$XYZ = 1; a$b$XYZ %= 2;");
  }

// com.google.javascript.jscomp.ConstCheckTest::testAbbreviatedBitAssignment1
  public void testAbbreviatedBitAssignment1() {
    testError("var XYZ = 1; XYZ |= 2;");
  }

// com.google.javascript.jscomp.ConstCheckTest::testAbbreviatedBitAssignment2
  public void testAbbreviatedBitAssignment2() {
    testError("var a$b$XYZ = 1; a$b$XYZ &= 2;");
  }

// com.google.javascript.jscomp.ConstCheckTest::testAbbreviatedShiftAssignment1
  public void testAbbreviatedShiftAssignment1() {
    testError("var XYZ = 1; XYZ >>= 2;");
  }

// com.google.javascript.jscomp.ConstCheckTest::testAbbreviatedShiftAssignment2
  public void testAbbreviatedShiftAssignment2() {
    testError("var a$b$XYZ = 1; a$b$XYZ <<= 2;");
  }

// com.google.javascript.jscomp.ConstCheckTest::testConstAnnotation
  public void testConstAnnotation() {
    testError(" var xyz = 1; xyz = 3;");
  }

// com.google.javascript.jscomp.ControlStructureCheckTest::testWhile
  public void testWhile() {
    assertNoError("while(1) { break; }");
  }

// com.google.javascript.jscomp.ControlStructureCheckTest::testNextedWhile
  public void testNextedWhile() {
    assertNoError("while(1) { while(1) { break; } }");
  }

// com.google.javascript.jscomp.ControlStructureCheckTest::testBreak
  public void testBreak() {
    assertInvalidBreak("break;");
  }

// com.google.javascript.jscomp.ControlStructureCheckTest::testContinue
  public void testContinue() {
    assertInvalidContinue("continue;");
  }

// com.google.javascript.jscomp.ControlStructureCheckTest::testBreakCrossFunction
  public void testBreakCrossFunction() {
    assertInvalidBreak("while(1) { function f() { break; } }");
  }

// com.google.javascript.jscomp.ControlStructureCheckTest::testBreakCrossFunctionInFor
  public void testBreakCrossFunctionInFor() {
    assertInvalidBreak("while(1) {for(var f = function () { break; };;) {}}");
  }

// com.google.javascript.jscomp.ControlStructureCheckTest::testContinueToSwitch
  public void testContinueToSwitch() {
    assertInvalidContinue("switch(1) {case(1): continue; }");
  }

// com.google.javascript.jscomp.ControlStructureCheckTest::testContinueToSwitchWithNoCases
  public void testContinueToSwitchWithNoCases() {
    assertNoError("switch(1){}");
  }

// com.google.javascript.jscomp.ControlStructureCheckTest::testContinueToSwitchWithTwoCases
  public void testContinueToSwitchWithTwoCases() {
    assertInvalidContinue("switch(1){case(1):break;case(2):continue;}");
  }

// com.google.javascript.jscomp.ControlStructureCheckTest::testContinueToSwitchWithDefault
  public void testContinueToSwitchWithDefault() {
    assertInvalidContinue("switch(1){case(1):break;case(2):default:continue;}");
  }

// com.google.javascript.jscomp.ControlStructureCheckTest::testContinueToLabelSwitch
  public void testContinueToLabelSwitch() {
    assertInvalidLabeledContinue(
        "while(1) {a: switch(1) {case(1): continue a; }}");
  }

// com.google.javascript.jscomp.ControlStructureCheckTest::testContinueOutsideSwitch
  public void testContinueOutsideSwitch() {
    assertNoError("b: while(1) { a: switch(1) { case(1): continue b; } }");
  }

// com.google.javascript.jscomp.ControlStructureCheckTest::testContinueNotCrossFunction1
  public void testContinueNotCrossFunction1() {
    assertNoError("a:switch(1){case(1):function f(){a:while(1){continue a;}}}");
  }

// com.google.javascript.jscomp.ControlStructureCheckTest::testContinueNotCrossFunction2
  public void testContinueNotCrossFunction2() {
    assertUndefinedLabel(
        "a:switch(1){case(1):function f(){while(1){continue a;}}}");
  }

// com.google.javascript.jscomp.ControlStructureCheckTest::testUseOfWith1
  public void testUseOfWith1() {
    testSame("with(a){}", ControlStructureCheck.USE_OF_WITH);
  }

// com.google.javascript.jscomp.ControlStructureCheckTest::testUseOfWith2
  public void testUseOfWith2() {
    testSame("" +
             "with(a){}");
  }

// com.google.javascript.jscomp.ConvertToDottedPropertiesTest::testConvert
  public void testConvert() {
    test("a['p']", "a.p");
    test("a['_p_']", "a._p_");
    test("a['_']", "a._");
    test("a['$']", "a.$");
    test("a.b.c['p']", "a.b.c.p");
    test("a.b['c'].p", "a.b.c.p");
    test("a['p']();", "a.p();");
    test("a()['p']", "a().p");
    
    test("a['\u0041A']", "a.AA");
  }

// com.google.javascript.jscomp.ConvertToDottedPropertiesTest::testDoNotConvert
  public void testDoNotConvert() {
    testSame("a[0]");
    testSame("a['']");
    testSame("a[' ']");
    testSame("a[',']");
    testSame("a[';']");
    testSame("a[':']");
    testSame("a['.']");
    testSame("a['0']");
    testSame("a['p ']");
    testSame("a['p' + '']");
    testSame("a[p]");
    testSame("a[P]");
    testSame("a[$]");
    testSame("a[p()]");
    testSame("a['default']");
    
    
    test("a['\u1d17A']", "a['\u1d17A']");
    
    
    test("a['\u00d1StuffAfter']", "a['\u00d1StuffAfter']");
  }

// com.google.javascript.jscomp.ConvertToDottedPropertiesTest::testQuotedProps
  public void testQuotedProps() {
    testSame("({'':0})");
    testSame("({'1.0':0})");
    testSame("({'\u1d17A':0})");
  }

// com.google.javascript.jscomp.CreateSyntheticBlocksTest::testFold1
  public void testFold1() {
    test("function f() { if (x) return; y(); }",
         "function f(){x||y()}");
  }

// com.google.javascript.jscomp.CreateSyntheticBlocksTest::testFoldWithMarkers1
  public void testFoldWithMarkers1() {
    testSame("function f(){startMarker();if(x)return;endMarker();y()}");
  }

// com.google.javascript.jscomp.CreateSyntheticBlocksTest::testFoldWithMarkers1a
  public void testFoldWithMarkers1a() {
    testSame("function f(){startMarker();if(x)return;endMarker()}");
  }

// com.google.javascript.jscomp.CreateSyntheticBlocksTest::testFold2
  public void testFold2() {
    test("function f() { if (x) return; y(); if (a) return; b(); }",
         "function f(){if(!x){y();a||b()}}");
  }

// com.google.javascript.jscomp.CreateSyntheticBlocksTest::testFoldWithMarkers2
  public void testFoldWithMarkers2() {
    testSame("function f(){startMarker(\"FOO\");startMarker(\"BAR\");" +
             "if(x)return;endMarker(\"BAR\");y();if(a)return;" +
             "endMarker(\"FOO\");b()}");
  }

// com.google.javascript.jscomp.CreateSyntheticBlocksTest::testUnmatchedStartMarker
  public void testUnmatchedStartMarker() {
    testSame("startMarker()", CreateSyntheticBlocks.UNMATCHED_START_MARKER);
  }

// com.google.javascript.jscomp.CreateSyntheticBlocksTest::testUnmatchedEndMarker1
  public void testUnmatchedEndMarker1() {
    testSame("endMarker()", CreateSyntheticBlocks.UNMATCHED_END_MARKER);
  }

// com.google.javascript.jscomp.CreateSyntheticBlocksTest::testUnmatchedEndMarker2
  public void testUnmatchedEndMarker2() {
    test("if(y){startMarker();x()}endMarker()",
        "if(y){startMarker();x()}endMarker()", null,
         CreateSyntheticBlocks.UNMATCHED_END_MARKER);
  }

// com.google.javascript.jscomp.CreateSyntheticBlocksTest::testInvalid1
  public void testInvalid1() {
    test("startMarker() && true",
        "startMarker()", null,
         CreateSyntheticBlocks.INVALID_MARKER_USAGE);
  }

// com.google.javascript.jscomp.CreateSyntheticBlocksTest::testInvalid2
  public void testInvalid2() {
    test("false && endMarker()",
        "", null,
         CreateSyntheticBlocks.INVALID_MARKER_USAGE);
  }

// com.google.javascript.jscomp.CreateSyntheticBlocksTest::testDenormalize
  public void testDenormalize() {
    testSame("startMarker();for(;;);endMarker()");
  }

// com.google.javascript.jscomp.CreateSyntheticBlocksTest::testNonMarkingUse
  public void testNonMarkingUse() {
    testSame("function foo(endMarker){}");
    testSame("function foo(){startMarker:foo()}");
  }

// com.google.javascript.jscomp.CreateSyntheticBlocksTest::testContainingBlockPreservation
  public void testContainingBlockPreservation() {
    testSame("if(y){startMarker();x();endMarker()}");
  }

// com.google.javascript.jscomp.CrossModuleCodeMotionTest::testFunctionMovement1
  public void testFunctionMovement1() {
    
    
    
    
    
    
    

    JSModule[] modules = createModuleStar(
      
      "function f1(a) { alert(a); }" +
      "function f2(a) { alert(a); }" +
      "function f3(a) { alert(a); }" +
      "function f4() { alert(1); }" +
      "function g() { alert('ciao'); }",
      
      "f1('hi'); f3('bye'); var a = f4;" +
      "function h(a) { alert('h:' + a); }",
      
      "f2('hi'); f2('hi'); f3('bye');");

    test(modules, new String[] {
      
      "function f3(a) { alert(a); }" +
      "function g() { alert('ciao'); }",
      
      "function f4() { alert(1); }" +
      "function f1(a) { alert(a); }" +
      "f1('hi'); f3('bye'); var a = f4;" +
      "function h(a) { alert('h:' + a); }",
      
      "function f2(a) { alert(a); }" +
      "f2('hi'); f2('hi'); f3('bye');",
    });
  }

// com.google.javascript.jscomp.CrossModuleCodeMotionTest::testFunctionMovement2
  public void testFunctionMovement2() {
    
    JSModule[] modules = createModuleStar(
      
      "function f(a) { alert(a); }" +
      "function g() {var f = 1; f++}",
      
      "f(1);");

    test(modules, new String[] {
      
      "function g() {var f = 1; f++}",
      
      "function f(a) { alert(a); }" +
      "f(1);",
    });
  }

// com.google.javascript.jscomp.CrossModuleCodeMotionTest::testFunctionMovement3
  public void testFunctionMovement3() {
    
    JSModule[] modules = createModuleStar(
      
      "function f(a) { alert(a); }" +
      "function g(f) {f++}",
      
      "f(1);");

    test(modules, new String[] {
      
      "function g(f) {f++}",
      
      "function f(a) { alert(a); }" +
      "f(1);",
    });
  }

// com.google.javascript.jscomp.CrossModuleCodeMotionTest::testFunctionMovement4
  public void testFunctionMovement4() {
    
    JSModule[] modules = createModuleStar(
      
      "function f(){return function(a){}}",
      
      "var a = f();"
    );

    test(modules, new String[] {
      
      "",
      
      "function f(){return function(a){}}" +
      "var a = f();",
    });
  }

// com.google.javascript.jscomp.CrossModuleCodeMotionTest::testFunctionMovement5
  public void testFunctionMovement5() {
    
    JSModule[] modules = createModuleStar(
      
      "function f(n){return (n<1)?1:f(n-1)}",
      
      "var a = f(4);"
    );

    test(modules, new String[] {
      
      "",
      
      "function f(n){return (n<1)?1:f(n-1)}" +
      "var a = f(4);",
    });
  }

// com.google.javascript.jscomp.CrossModuleCodeMotionTest::testFunctionMovement5b
  public void testFunctionMovement5b() {
    
    JSModule[] modules = createModuleStar(
      
      "var f = function(n){return (n<1)?1:f(n-1)};",
      
      "var a = f(4);"
    );

    test(modules, new String[] {
      
      "",
      
      "var f = function(n){return (n<1)?1:f(n-1)};" +
      "var a = f(4);",
    });
  }

// com.google.javascript.jscomp.CrossModuleCodeMotionTest::testFunctionMovement6
  public void testFunctionMovement6() {
    
    JSModule[] modules = createModuleChain(
      
      "function f(){return 1}",
      
      "var a = f();",
      
      "var b = f();"
    );

    test(modules, new String[] {
      
      "",
      
      "function f(){return 1}" +
      "var a = f();",
      
      "var b = f();",
    });
  }

// com.google.javascript.jscomp.CrossModuleCodeMotionTest::testFunctionMovement7
  public void testFunctionMovement7() {
    
    JSModule[] modules = createModules(
      
      "function f(){return 1}",
      
      "",
      
      "var a = f();",
      
      "var b = f();",
      
      "var c = f();"
    );

    modules[1].addDependency(modules[0]);
    modules[2].addDependency(modules[1]);
    modules[3].addDependency(modules[1]);
    modules[4].addDependency(modules[1]);

    test(modules, new String[] {
      
      "",
      
      "function f(){return 1}",
      
      "var a = f();",
      
      "var b = f();",
      
      "var c = f();",
    });
  }

// com.google.javascript.jscomp.CrossModuleCodeMotionTest::testFunctionMovement8
  public void testFunctionMovement8() {
    
    JSModule[] modules = createModuleChain(
      
      "var v = function f(){return 1}",
      
      "v();"
    );

    test(modules, new String[] {
      
      "",
      
      "var v = function f(){return 1};" +
      "v();",
    });
  }

// com.google.javascript.jscomp.CrossModuleCodeMotionTest::testFunctionNonMovement1
  public void testFunctionNonMovement1() {
    
    
    
    
    
    testSame(createModuleStar(
      
      "function f(){};f.prototype.bar=new f;" +
      "if(a)function f2(){}" +
      "{{while(a)function f3(){}}}",
      
      "var a = new f();f2();f3();"));
  }

// com.google.javascript.jscomp.CrossModuleCodeMotionTest::testFunctionNonMovement2
  public void testFunctionNonMovement2() {
    
    
    testSame(createModuleStar(
      
      "function f(){return 1}",
      
      "var a = f();",
      
      "var b = f();"));
  }

// com.google.javascript.jscomp.CrossModuleCodeMotionTest::testClassMovement1
  public void testClassMovement1() {
    test(createModuleStar(
             
             "function f(){} f.prototype.bar=function (){};",
             
             "var a = new f();"),
         new String[] {
           "",
           "function f(){} f.prototype.bar=function (){};" +
           "var a = new f();"
         });
  }

// com.google.javascript.jscomp.CrossModuleCodeMotionTest::testClassMovement2
  public void testClassMovement2() {
    
    test(createModuleChain(
             
             "function f(){} f.prototype.bar=3; f.prototype.baz=5;",
             
             "f.prototype.baq = 7;",
             
             "f.prototype.baz = 9;",
             
             "var a = new f();"),
         new String[] {
           
           "",
           
           "",
           
           "function f(){} f.prototype.bar=3; f.prototype.baz=5;" +
           "f.prototype.baq = 7;" +
           "f.prototype.baz = 9;",
           
           "var a = new f();"
         });
  }

// com.google.javascript.jscomp.CrossModuleCodeMotionTest::testClassMovement3
  public void testClassMovement3() {
    
    test(createModuleChain(
             
             "var f = function() {}; f.prototype.bar=3; f.prototype.baz=5;",
             
             "f = 7;",
             
             "f = 9;",
             
             "f = 11;"),
         new String[] {
           
           "",
           
           "",
           
           "var f = function() {}; f.prototype.bar=3; f.prototype.baz=5;" +
           "f = 7;" +
           "f = 9;",
           
           "f = 11;"
         });
  }

// com.google.javascript.jscomp.CrossModuleCodeMotionTest::testClassMovement4
  public void testClassMovement4() {
    testSame(createModuleStar(
                 
                 "function f(){} f.prototype.bar=3; f.prototype.baz=5;",
                 
                 "f.prototype.baq = 7;",
                 
                 "var a = new f();"));
  }

// com.google.javascript.jscomp.CrossModuleCodeMotionTest::testClassMovement5
  public void testClassMovement5() {
    JSModule[] modules = createModules(
        
        "function f(){} f.prototype.bar=3; f.prototype.baz=5;",
        
        "",
        
        "f.prototype.baq = 7;",
        
        "var a = new f();");

    modules[1].addDependency(modules[0]);
    modules[2].addDependency(modules[1]);
    modules[3].addDependency(modules[1]);

    test(modules,
         new String[] {
           
           "",
           
           "function f(){} f.prototype.bar=3; f.prototype.baz=5;",
           
           "f.prototype.baq = 7;",
           
           "var a = new f();"
         });
  }

// com.google.javascript.jscomp.CrossModuleCodeMotionTest::testClassMovement6
  public void testClassMovement6() {
    test(createModuleChain(
             
             "function Foo(){} function Bar(){} goog.inherits(Bar, Foo);" +
             "new Foo();",
             
             "new Bar();"),
         new String[] {
           
           "function Foo(){} new Foo();",
           
           "function Bar(){} goog.inherits(Bar, Foo); new Bar();"
         });
  }

// com.google.javascript.jscomp.CrossModuleCodeMotionTest::testClassMovement7
  public void testClassMovement7() {
    testSame(createModuleChain(
                 
                 "function Foo(){} function Bar(){} goog.inherits(Bar, Foo);" +
                 "new Bar();",
                 
                 "new Foo();"));
  }

// com.google.javascript.jscomp.CrossModuleCodeMotionTest::testStubMethodMovement1
  public void testStubMethodMovement1() {
    test(createModuleChain(
             
             "function Foo(){} " +
             "Foo.prototype.bar = JSCompiler_stubMethod(x);",
             
             "new Foo();"),
        new String[] {
          
          "",
          "function Foo(){} " +
          "Foo.prototype.bar = JSCompiler_stubMethod(x);" +
          "new Foo();"
        });
  }

// com.google.javascript.jscomp.CrossModuleCodeMotionTest::testStubMethodMovement2
  public void testStubMethodMovement2() {
    test(createModuleChain(
             
             "function Foo(){} " +
             "Foo.prototype.bar = JSCompiler_unstubMethod(x);",
             
             "new Foo();"),
        new String[] {
          
          "",
          "function Foo(){} " +
          "Foo.prototype.bar = JSCompiler_unstubMethod(x);" +
          "new Foo();"
        });
  }

// com.google.javascript.jscomp.CrossModuleCodeMotionTest::testNoMoveSideEffectProperty
  public void testNoMoveSideEffectProperty() {
    testSame(createModuleChain(
                 
                 "function Foo(){} " +
                 "Foo.prototype.bar = createSomething();",
                 
                 "new Foo();"));
  }

// com.google.javascript.jscomp.CrossModuleCodeMotionTest::testAssignMovement
  public void testAssignMovement() {
    test(createModuleChain(
             
             "var f = 3;" +
             "f = 5;",
             
             "var h = f;"),
        new String[] {
          
          "",
          
          "var f = 3;" +
          "f = 5;" +
          "var h = f;"
        });

    
    testSame(createModuleChain(
                 
                 "var f = 3;" +
                 "var g = f = 5;",
                 
                 "var h = f;"));
  }

// com.google.javascript.jscomp.CrossModuleCodeMotionTest::testNoClassMovement2
  public void testNoClassMovement2() {
    test(createModuleChain(
             
             "var f = {};" +
             "f.h = 5;",
             
             "var h = f;"),
        new String[] {
          
          "",
          
          "var f = {};" +
          "f.h = 5;" +
          "var h = f;"
        });

    
    testSame(createModuleChain(
                 
                 "var f = {};" +
                 "var g = f.h = 5;",
                 
                 "var h = f;"));
  }

// com.google.javascript.jscomp.CrossModuleCodeMotionTest::testLiteralMovement1
  public void testLiteralMovement1() {
    test(createModuleChain(
             
             "var f = {'hi': 'mom', 'bye': function() {}};",
             
             "var h = f;"),
        new String[] {
          
          "",
          
          "var f = {'hi': 'mom', 'bye': function() {}};" +
          "var h = f;"
        });
  }

// com.google.javascript.jscomp.CrossModuleCodeMotionTest::testLiteralMovement2
  public void testLiteralMovement2() {
    testSame(createModuleChain(
                 
                 "var f = {'hi': 'mom', 'bye': goog.nullFunction};",
                 
                 "var h = f;"));
  }

// com.google.javascript.jscomp.CrossModuleCodeMotionTest::testLiteralMovement3
  public void testLiteralMovement3() {
    test(createModuleChain(
             
             "var f = ['hi', function() {}];",
             
             "var h = f;"),
        new String[] {
          
          "",
          
          "var f = ['hi', function() {}];" +
          "var h = f;"
        });
  }

// com.google.javascript.jscomp.CrossModuleCodeMotionTest::testLiteralMovement4
  public void testLiteralMovement4() {
    testSame(createModuleChain(
                 
                 "var f = ['hi', goog.nullFunction];",
                 
                 "var h = f;"));
  }

// com.google.javascript.jscomp.CrossModuleCodeMotionTest::testVarMovement1
  public void testVarMovement1() {
    
    JSModule[] modules = createModuleStar(
      
      "var a = 0;",
      
      "var x = a;"
    );

    test(modules, new String[] {
      
      "",
      
      "var a = 0;" +
      "var x = a;",
    });
  }

// com.google.javascript.jscomp.CrossModuleCodeMotionTest::testVarMovement2
  public void testVarMovement2() {
    
    JSModule[] modules = createModuleStar(
      
      "var a = 0; var b = 1; var c = 2;",
      
      "var x = b;"
    );

    test(modules, new String[] {
      
      "var a = 0; var c = 2;",
      
      "var b = 1;" +
      "var x = b;"
    });
  }

// com.google.javascript.jscomp.CrossModuleCodeMotionTest::testVarMovement3
  public void testVarMovement3() {
    
    JSModule[] modules = createModuleStar(
      
      "var a = 0; var b = 1;",
      
      "var x = a + b;"
    );

    test(modules, new String[] {
      
      "",
      
      "var b = 1;" +
      "var a = 0;" +
      "var x = a + b;"
    });
  }

// com.google.javascript.jscomp.CrossModuleCodeMotionTest::testVarMovement4
  public void testVarMovement4() {
    
    JSModule[] modules = createModuleStar(
      
      "var a = function(){alert(1)};",
      
      "var x = a;"
    );

    test(modules, new String[] {
      
      "",
      
      "var a = function(){alert(1)};" +
      "var x = a;"
    });
  }

// com.google.javascript.jscomp.CrossModuleCodeMotionTest::testVarMovement5
  public void testVarMovement5() {
    
    testSame(createModuleStar(
      
      "var a = alert;",
      
      "var x = a;"));
  }

// com.google.javascript.jscomp.CrossModuleCodeMotionTest::testVarMovement6
  public void testVarMovement6() {
    
    JSModule[] modules = createModuleStar(
      
      "var a;",
      
      "var x = a;"
    );

    test(modules, new String[] {
      
      "",
      
      "var a;" +
      "var x = a;"
    });
  }

// com.google.javascript.jscomp.CrossModuleCodeMotionTest::testVarMovement7
  public void testVarMovement7() {
    
    testSame(createModuleStar(
      
      "function f() {g();}",
      
      "function g(){};"));
  }

// com.google.javascript.jscomp.CrossModuleCodeMotionTest::testVarMovement8
  public void testVarMovement8() {
    JSModule[] modules = createModuleBush(
      
      "var a = 0;",
      
      "",
      
      "var x = a;",
      
      "var y = a;"
    );

    test(modules, new String[] {
      
      "",
      
      "var a = 0;",
      
      "var x = a;",
      
      "var y = a;"
    });
  }

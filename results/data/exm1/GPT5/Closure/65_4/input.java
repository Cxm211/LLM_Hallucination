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
            if (c > 0x1f && c < 0x7f) {
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
// com.google.javascript.jscomp.ExtractPrototypeMemberDeclarationsTest::testNotEnoughPrototypeToExtract
  public void testNotEnoughPrototypeToExtract() {
    
    for (int i = 0; i < 7; i++) {
      testSame(generatePrototypeDeclarations("x", i));
    }
  }

// com.google.javascript.jscomp.ExtractPrototypeMemberDeclarationsTest::testExtractingSingleClassPrototype
  public void testExtractingSingleClassPrototype() {
    extract(generatePrototypeDeclarations("x", 7),
        loadPrototype("x") +
        generateExtractedDeclarations(7));
  }

// com.google.javascript.jscomp.ExtractPrototypeMemberDeclarationsTest::testExtractingTwoClassPrototype
  public void testExtractingTwoClassPrototype() {
    extract(
        generatePrototypeDeclarations("x", 6) +
        generatePrototypeDeclarations("y", 6),
        loadPrototype("x") +
        generateExtractedDeclarations(6) +
        loadPrototype("y") +
        generateExtractedDeclarations(6));
  }

// com.google.javascript.jscomp.ExtractPrototypeMemberDeclarationsTest::testExtractingTwoClassPrototypeInDifferentBlocks
  public void testExtractingTwoClassPrototypeInDifferentBlocks() {
    extract(
        generatePrototypeDeclarations("x", 6) +
        "if (foo()) {" +
        generatePrototypeDeclarations("y", 6) +
        "}",
        loadPrototype("x") +
        generateExtractedDeclarations(6) +
        "if (foo()) {" +
        loadPrototype("y") +
        generateExtractedDeclarations(6) +
        "}");
  }

// com.google.javascript.jscomp.ExtractPrototypeMemberDeclarationsTest::testNoMemberDeclarations
  public void testNoMemberDeclarations() {
    testSame(
        "x.prototype = {}; x.prototype = {}; x.prototype = {};" +
        "x.prototype = {}; x.prototype = {}; x.prototype = {};" +
        "x.prototype = {}; x.prototype = {}; x.prototype = {};");
  }

// com.google.javascript.jscomp.ExtractPrototypeMemberDeclarationsTest::testExtractingPrototypeWithQName
  public void testExtractingPrototypeWithQName() {
    extract(
        generatePrototypeDeclarations("com.google.javascript.jscomp.x", 7),
        loadPrototype("com.google.javascript.jscomp.x") +
        generateExtractedDeclarations(7));
  }

// com.google.javascript.jscomp.ExtractPrototypeMemberDeclarationsTest::testInterweaved
  public void testInterweaved() {
    testSame(
        "x.prototype.a=1; y.prototype.a=1;" +
        "x.prototype.b=1; y.prototype.b=1;" +
        "x.prototype.c=1; y.prototype.c=1;" +
        "x.prototype.d=1; y.prototype.d=1;" +
        "x.prototype.e=1; y.prototype.e=1;" +
        "x.prototype.f=1; y.prototype.f=1;");
  }

// com.google.javascript.jscomp.ExtractPrototypeMemberDeclarationsTest::testExtractingPrototypeWithNestedMembers
  public void testExtractingPrototypeWithNestedMembers() {
    extract(
        "x.prototype.y.a = 1;" +
        "x.prototype.y.b = 1;" +
        "x.prototype.y.c = 1;" +
        "x.prototype.y.d = 1;" +
        "x.prototype.y.e = 1;" +
        "x.prototype.y.f = 1;" +
        "x.prototype.y.g = 1;",
        loadPrototype("x") +
        TMP + ".y.a = 1;" +
        TMP + ".y.b = 1;" +
        TMP + ".y.c = 1;" +
        TMP + ".y.d = 1;" +
        TMP + ".y.e = 1;" +
        TMP + ".y.f = 1;" +
        TMP + ".y.g = 1;");
  }

// com.google.javascript.jscomp.ExtractPrototypeMemberDeclarationsTest::testWithDevirtualization
  public void testWithDevirtualization() {
    extract(
        "x.prototype.a = 1;" +
        "x.prototype.b = 1;" +
        "function devirtualize1() { }" +
        "x.prototype.c = 1;" +
        "x.prototype.d = 1;" +
        "x.prototype.e = 1;" +
        "x.prototype.f = 1;" +
        "x.prototype.g = 1;",

        loadPrototype("x") +
        TMP + ".a = 1;" +
        TMP + ".b = 1;" +
        "function devirtualize1() { }" +
        TMP + ".c = 1;" +
        TMP + ".d = 1;" +
        TMP + ".e = 1;" +
        TMP + ".f = 1;" +
        TMP + ".g = 1;");

    extract(
        "x.prototype.a = 1;" +
        "x.prototype.b = 1;" +
        "function devirtualize1() { }" +
        "x.prototype.c = 1;" +
        "x.prototype.d = 1;" +
        "function devirtualize2() { }" +
        "x.prototype.e = 1;" +
        "x.prototype.f = 1;" +
        "function devirtualize3() { }" +
        "x.prototype.g = 1;",

        loadPrototype("x") +
        TMP + ".a = 1;" +
        TMP + ".b = 1;" +
        "function devirtualize1() { }" +
        TMP + ".c = 1;" +
        TMP + ".d = 1;" +
        "function devirtualize2() { }" +
        TMP + ".e = 1;" +
        TMP + ".f = 1;" +
        "function devirtualize3() { }" +
        TMP + ".g = 1;");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testSimpleAssign
  public void testSimpleAssign() {
    inline("var x; x = 1; print(x)", "var x; print(1)");
    inline("var x; x = 1; x", "var x; 1");
    inline("var x; x = 1; var a = x", "var x; var a = 1");
    inline("var x; x = 1; x = x + 1", "var x; x = 1 + 1");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testSimpleVar
  public void testSimpleVar() {
    inline("var x = 1; print(x)", "var x; print(1)");
    inline("var x = 1; x", "var x; 1");
    inline("var x = 1; var a = x", "var x; var a = 1");
    inline("var x = 1; x = x + 1", "var x; x = 1 + 1");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testExported
  public void testExported() {
    noInline("var _x = 1; print(_x)");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testDoNotInlineIncrement
  public void testDoNotInlineIncrement() {
    noInline("var x = 1; x++;");
    noInline("var x = 1; x--;");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testDoNotInlineAssignmentOp
  public void testDoNotInlineAssignmentOp() {
    noInline("var x = 1; x += 1;");
    noInline("var x = 1; x -= 1;");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testDoNotInlineIntoLhsOfAssign
  public void testDoNotInlineIntoLhsOfAssign() {
    noInline("var x = 1; x += 3;");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testMultiUse
  public void testMultiUse() {
    noInline("var x; x = 1; print(x); print (x);");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testMultiUseInSameCfgNode
  public void testMultiUseInSameCfgNode() {
    noInline("var x; x = 1; print(x) || print (x);");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testMultiUseInTwoDifferentPath
  public void testMultiUseInTwoDifferentPath() {
    noInline("var x = 1; if (print) { print(x) } else { alert(x) }");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testAssignmentBeforeDefinition
  public void testAssignmentBeforeDefinition() {
    inline("x = 1; var x = 0; print(x)","x = 1; var x; print(0)" );
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testVarInConditionPath
  public void testVarInConditionPath() {
    noInline("if (foo) { var x = 0 } print(x)");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testMultiDefinitionsBeforeUse
  public void testMultiDefinitionsBeforeUse() {
    inline("var x = 0; x = 1; print(x)", "var x = 0; print(1)");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testMultiDefinitionsInSameCfgNode
  public void testMultiDefinitionsInSameCfgNode() {
    noInline("var x; (x = 1) || (x = 2); print(x)");
    noInline("var x; x = (1 || (x = 2)); print(x)");
    noInline("var x;(x = 1) && (x = 2); print(x)");
    noInline("var x;x = (1 && (x = 2)); print(x)");
    noInline("var x; x = 1 , x = 2; print(x)");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testNotReachingDefinitions
  public void testNotReachingDefinitions() {
    noInline("var x; if (foo) { x = 0 } print (x)");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testNoInlineLoopCarriedDefinition
  public void testNoInlineLoopCarriedDefinition() {
    
    noInline("var x; while(true) { print(x); x = 1; }");

    
    noInline("var x = 0; while(true) { print(x); x = 1; }");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testDoNotExitLoop
  public void testDoNotExitLoop() {
    noInline("while (z) { var x = 3; } var y = x;");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testDoNotInlineWithinLoop
  public void testDoNotInlineWithinLoop() {
    noInline("var y = noSFX(); do { var z = y.foo(); } while (true);");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testDefinitionAfterUse
  public void testDefinitionAfterUse() {
    inline("var x = 0; print(x); x = 1", "var x; print(0); x = 1");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testInlineSameVariableInStraightLine
  public void testInlineSameVariableInStraightLine() {
    inline("var x; x = 1; print(x); x = 2; print(x)",
        "var x; print(1); print(2)");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testInlineInDifferentPaths
  public void testInlineInDifferentPaths() {
    inline("var x; if (print) {x = 1; print(x)} else {x = 2; print(x)}",
        "var x; if (print) {print(1)} else {print(2)}");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testNoInlineInMergedPath
  public void testNoInlineInMergedPath() {
    noInline(
        "var x,y;x = 1;while(y) { if(y){ print(x) } else { x = 1 } } print(x)");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testInlineIntoExpressions
  public void testInlineIntoExpressions() {
    inline("var x = 1; print(x + 1);", "var x; print(1 + 1)");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testInlineExpressions1
  public void testInlineExpressions1() {
    inline("var a, b; var x = a+b; print(x)", "var a, b; var x; print(a+b)");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testInlineExpressions2
  public void testInlineExpressions2() {
    
    noInline("var a, b; var x = a + b; a = 1; print(x)");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testInlineExpressions3
  public void testInlineExpressions3() {
    inline("var a,b,x; x=a+b; x=a-b ; print(x)",
           "var a,b,x; x=a+b; print(a-b)");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testInlineExpressions4
  public void testInlineExpressions4() {
    
    noInline("var a,b,x; x=a+b, x=a-b; print(x)");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testInlineExpressions5
  public void testInlineExpressions5() {
    noInline("var a; var x = a = 1; print(x)");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testInlineExpressions6
  public void testInlineExpressions6() {
    noInline("var a, x; a = 1 + (x = 1); print(x)");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testInlineExpression7
  public void testInlineExpression7() {
    
    noInline("var x = foo() + 1; bar(); print(x)");

    
    
    
    noInline("var x = foo() + 1; print(x)");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testInlineExpression8
  public void testInlineExpression8() {
    
    inline("var x = a + b; print(x);      x = a - b; print(x)",
           "var x;         print(a + b);             print(a - b)");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testInlineExpression9
  public void testInlineExpression9() {
    
    inline("var x; if (g) { x= a + b; print(x)    }  x = a - b; print(x)",
           "var x; if (g) {           print(a + b)}             print(a - b)");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testInlineExpression10
  public void testInlineExpression10() {
    
    noInline("var x, y; x = ((y = 1), print(y))");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testInlineExpressions11
  public void testInlineExpressions11() {
    inline("var x; x = x + 1; print(x)", "var x; print(x + 1)");
    noInline("var x; x = x + 1; print(x); print(x)");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testInlineExpressions12
  public void testInlineExpressions12() {
    
    
    noInline("var x = 10; x = c++; print(x)");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testInlineExpressions13
  public void testInlineExpressions13() {
    inline("var a = 1, b = 2;" +
           "var x = a;" +
           "var y = b;" +
           "var z = x + y;" +
           "var i = z;" +
           "var j = z + y;" +
           "var k = i;",

           "var a, b;" +
           "var x;" +
           "var y = 2;" +
           "var z = 1 + y;" +
           "var i;" +
           "var j = z + y;" +
           "var k = z;");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testNoInlineIfDefinitionMayNotReach
  public void testNoInlineIfDefinitionMayNotReach() {
    noInline("var x; if (x=1) {} x;");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testNoInlineEscapedToInnerFunction
  public void testNoInlineEscapedToInnerFunction() {
    noInline("var x = 1; function foo() { x = 2 }; print(x)");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testNoInlineLValue
  public void testNoInlineLValue() {
    noInline("var x; if (x = 1) { print(x) }");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testSwitchCase
  public void testSwitchCase() {
    inline("var x = 1; switch(x) { }", "var x; switch(1) { }");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testShadowedVariableInnerFunction
  public void testShadowedVariableInnerFunction() {
    inline("var x = 1; print(x) || (function() {  var x; x = 1; print(x)})()",
        "var x; print(1) || (function() {  var x; print(1)})()");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testCatch
  public void testCatch() {
    noInline("var x = 0; try { } catch (x) { }");
    noInline("try { } catch (x) { print(x) }");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testNoInlineGetProp
  public void testNoInlineGetProp() {
    
    noInline("var x = a.b.c; j.c = 1; print(x);");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testNoInlineGetProp2
  public void testNoInlineGetProp2() {
    noInline("var x = 1 * a.b.c; j.c = 1; print(x);");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testNoInlineGetProp3
  public void testNoInlineGetProp3() {
    
    inline("var x = function(){1 * a.b.c}; print(x);",
           "var x; print(function(){1 * a.b.c});");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testNoInlineGetEle
  public void testNoInlineGetEle() {
    
    noInline("var x = a[i]; a[j] = 2; print(x); ");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testNoInlineConstructors
  public void testNoInlineConstructors() {
    noInline("var x = new Iterator(); x.next();");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testNoInlineArrayLits
  public void testNoInlineArrayLits() {
    noInline("var x = []; print(x)");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testNoInlineObjectLits
  public void testNoInlineObjectLits() {
    noInline("var x = {}; print(x)");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testNoInlineRegExpLits
  public void testNoInlineRegExpLits() {
    noInline("var x = /y/; print(x)");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testInlineConstructorCallsIntoLoop
  public void testInlineConstructorCallsIntoLoop() {
    
    noInline("var x = new Iterator();" +
             "for(i = 0; i < 10; i++) {j = x.next()}");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testRemoveWithLabels
  public void testRemoveWithLabels() {
    inline("var x = 1; L: x = 2; print(x)", "var x = 1; print(2)");
    inline("var x = 1; L: M: x = 2; print(x)", "var x = 1; print(2)");
    inline("var x = 1; L: M: N: x = 2; print(x)", "var x = 1; print(2)");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testInlineAcrossSideEffect1
  public void testInlineAcrossSideEffect1() {
    inline("var y; var x = noSFX(y); print(x)", "var y;var x;print(noSFX(y))");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testInlineAcrossSideEffect2
  public void testInlineAcrossSideEffect2() {
    
    
    

    
    noInline("var y; var x = noSFX(y), z = hasSFX(y); print(x)");
    noInline("var y; var x = noSFX(y), z = new hasSFX(y); print(x)");
    noInline("var y; var x = new noSFX(y), z = new hasSFX(y); print(x)");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testInlineAcrossSideEffect3
  public void testInlineAcrossSideEffect3() {
    
    noInline("var y; var x = noSFX(y); hasSFX(y), print(x)");
    noInline("var y; var x = noSFX(y); new hasSFX(y), print(x)");
    noInline("var y; var x = new noSFX(y); new hasSFX(y), print(x)");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testInlineAcrossSideEffect4
  public void testInlineAcrossSideEffect4() {
    
    
    noInline("var y; var x = noSFX(y); hasSFX(y); print(x)");
    noInline("var y; var x = noSFX(y); new hasSFX(y); print(x)");
    noInline("var y; var x = new noSFX(y); new hasSFX(y); print(x)");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testCanInlineAcrossNoSideEffect
  public void testCanInlineAcrossNoSideEffect() {
    inline("var y; var x = noSFX(Y), z = noSFX(); noSFX(); noSFX(), print(x)",
           "var y; var x, z = noSFX(); noSFX(); noSFX(), print(noSFX(Y))");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testDependOnOuterScopeVariables
  public void testDependOnOuterScopeVariables() {
    noInline("var x; function foo() { var y = x; x = 0; print(y) }");
    noInline("var x; function foo() { var y = x; x++; print(y) }");

    
    
    
    noInline("var x; function foo() { var y = x; print(y) }");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testInlineIfNameIsLeftSideOfAssign
  public void testInlineIfNameIsLeftSideOfAssign() {
    inline("var x = 1; x = print(x) + 1", "var x; x = print(1) + 1");
    inline("var x = 1; L: x = x + 2", "var x; L: x = 1 + 2");
    inline("var x = 1; x = (x = x + 1)", "var x; x = (x = 1 + 1)");

    noInline("var x = 1; x = (x = (x = 10) + x)");
    noInline("var x = 1; x = (f(x) + (x = 10) + x);");
    noInline("var x = 1; x=-1,foo(x)");
    noInline("var x = 1; x-=1,foo(x)");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testInlineArguments
  public void testInlineArguments() {
    testSame("function _func(x) { print(x) }");
    testSame("function _func(x,y) { if(y) { x = 1 }; print(x) }");

    test("function f(x, y) { x = 1; print(x) }",
         "function f(x, y) { print(1) }");

    test("function f(x, y) { if (y) { x = 1; print(x) }}",
         "function f(x, y) { if (y) { print(1) }}");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testInvalidInlineArguments1
  public void testInvalidInlineArguments1() {
    testSame("function f(x, y) { x = 1; arguments[0] = 2; print(x) }");
    testSame("function f(x, y) { x = 1; var z = arguments;" +
        "z[0] = 2; z[1] = 3; print(x)}");
    testSame("function g(a){a[0]=2} function f(x){x=1;g(arguments);print(x)}");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testInvalidInlineArguments2
  public void testInvalidInlineArguments2() {
    testSame("function f(c) {var f = c; arguments[0] = this;" +
             "f.apply(this, arguments); return this;}");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testNotOkToSkipCheckPathBetweenNodes
  public void testNotOkToSkipCheckPathBetweenNodes() {
    noInline("var x; for(x = 1; foo(x);) {}");
    noInline("var x; for(; x = 1;foo(x)) {}");
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testIsSimpleFunction1
  public void testIsSimpleFunction1() {
    assertTrue(getInjector().isDirectCallNodeReplacementPossible(
        prep("function f(){}")));
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testIsSimpleFunction2
  public void testIsSimpleFunction2() {
    assertTrue(getInjector().isDirectCallNodeReplacementPossible(
        prep("function f(){return 0;}")));
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testIsSimpleFunction3
  public void testIsSimpleFunction3() {
    assertTrue(getInjector().isDirectCallNodeReplacementPossible(
        prep("function f(){return x ? 0 : 1}")));
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testIsSimpleFunction4
  public void testIsSimpleFunction4() {
    assertFalse(getInjector().isDirectCallNodeReplacementPossible(
        prep("function f(){return;}")));
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testIsSimpleFunction5
  public void testIsSimpleFunction5() {
    assertFalse(getInjector().isDirectCallNodeReplacementPossible(
        prep("function f(){return 0; return 0;}")));
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testIsSimpleFunction6
  public void testIsSimpleFunction6() {
    assertFalse(getInjector().isDirectCallNodeReplacementPossible(
        prep("function f(){var x=true;return x ? 0 : 1}")));
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testIsSimpleFunction7
  public void testIsSimpleFunction7() {
    assertFalse(getInjector().isDirectCallNodeReplacementPossible(
        prep("function f(){if (x) return 0; else return 1}")));
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction1
  public void testCanInlineReferenceToFunction1() {
    helperCanInlineReferenceToFunction(CanInlineResult.YES,
        "function foo(){}; foo();", "foo", INLINE_DIRECT);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction2
  public void testCanInlineReferenceToFunction2() {
    helperCanInlineReferenceToFunction(CanInlineResult.YES,
        "function foo(){}; foo();", "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction3
  public void testCanInlineReferenceToFunction3() {
    
    
    helperCanInlineReferenceToFunction(CanInlineResult.NO,
        "function foo(){return;}; foo();", "foo", INLINE_DIRECT);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction4
  public void testCanInlineReferenceToFunction4() {
    helperCanInlineReferenceToFunction(CanInlineResult.YES,
        "function foo(){return;}; foo();", "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction5
  public void testCanInlineReferenceToFunction5() {
    helperCanInlineReferenceToFunction(CanInlineResult.YES,
        "function foo(){return true;}; foo();", "foo", INLINE_DIRECT);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction6
  public void testCanInlineReferenceToFunction6() {
    helperCanInlineReferenceToFunction(CanInlineResult.YES,
        "function foo(){return true;}; foo();", "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction7
  public void testCanInlineReferenceToFunction7() {
    
    helperCanInlineReferenceToFunction(CanInlineResult.YES,
        "function foo(){return true;}; var x=foo();", "foo", INLINE_DIRECT);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction8
  public void testCanInlineReferenceToFunction8() {
    helperCanInlineReferenceToFunction(CanInlineResult.YES,
        "function foo(){return true;}; var x=foo();", "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction9
  public void testCanInlineReferenceToFunction9() {
    
    helperCanInlineReferenceToFunction(CanInlineResult.YES,
        "function foo(){return true;}; var x; x=foo();", "foo", INLINE_DIRECT);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction10
  public void testCanInlineReferenceToFunction10() {
    helperCanInlineReferenceToFunction(CanInlineResult.YES,
        "function foo(){return true;}; var x; x=foo();", "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction11
  public void testCanInlineReferenceToFunction11() {
    
    helperCanInlineReferenceToFunction(CanInlineResult.YES,
        "function foo(){return true;}; var x; x=x+foo();", "foo",
        INLINE_DIRECT);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction12
  public void testCanInlineReferenceToFunction12() {
    
    
    helperCanInlineReferenceToFunction(CanInlineResult.NO,
        "function foo(){return true;}; var x; x=x+foo();", "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction12b
  public void testCanInlineReferenceToFunction12b() {
    
    
    helperCanInlineReferenceToFunction(
        CanInlineResult.AFTER_DECOMPOSITION,
        "function foo(){return true;}; var x; x=x+foo();",
        "foo", INLINE_BLOCK, true);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction14
  public void testCanInlineReferenceToFunction14() {
    
    helperCanInlineReferenceToFunction(CanInlineResult.YES,
        "function foo(a){return true;}; foo(x);", "foo", INLINE_DIRECT);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction15
  public void testCanInlineReferenceToFunction15() {
    helperCanInlineReferenceToFunction(CanInlineResult.YES,
        "function foo(a){return true;}; foo(x);", "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction16
  public void testCanInlineReferenceToFunction16() {
    
    
    helperCanInlineReferenceToFunction(NEW_VARS_IN_GLOBAL_SCOPE,
        "function foo(a){var b;return a;}; foo(goo());", "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction17
  public void testCanInlineReferenceToFunction17() {
    
    helperCanInlineReferenceToFunction(CanInlineResult.YES,
        "function foo(a){return a;}; " +
        "function x() { foo(goo()); }",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction18
  public void testCanInlineReferenceToFunction18() {
    
    helperCanInlineReferenceToFunction(CanInlineResult.NO,
        "function foo(a){return a;} foo(x++);", "foo", INLINE_DIRECT);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction19
  public void testCanInlineReferenceToFunction19() {
    
    helperCanInlineReferenceToFunction(CanInlineResult.NO,
        "function foo(a){return a+a} foo([]);", "foo", INLINE_DIRECT);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction20
  public void testCanInlineReferenceToFunction20() {
    helperCanInlineReferenceToFunction(CanInlineResult.NO,
        "function foo(a){return a+a} foo({});", "foo", INLINE_DIRECT);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction21
  public void testCanInlineReferenceToFunction21() {
    helperCanInlineReferenceToFunction(CanInlineResult.NO,
        "function foo(a){return a+a} foo(new Date);", "foo", INLINE_DIRECT);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction22
  public void testCanInlineReferenceToFunction22() {
    helperCanInlineReferenceToFunction(CanInlineResult.NO,
        "function foo(a){return a+a} foo(true && new Date);", "foo",
        INLINE_DIRECT);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction23
  public void testCanInlineReferenceToFunction23() {
    
    helperCanInlineReferenceToFunction(NEW_VARS_IN_GLOBAL_SCOPE,
        "function foo(a){return a;}; foo(x++);", "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction24
  public void testCanInlineReferenceToFunction24() {
    
    helperCanInlineReferenceToFunction(CanInlineResult.YES,
        "function foo(a){return a;}; " +
        "function x() { foo(x++); }",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction25
  public void testCanInlineReferenceToFunction25() {
    
    helperCanInlineReferenceToFunction(CanInlineResult.NO,
        "function foo(a){return a+a;}; foo(x++);", "foo", INLINE_DIRECT);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction26
  public void testCanInlineReferenceToFunction26() {
    helperCanInlineReferenceToFunction(NEW_VARS_IN_GLOBAL_SCOPE,
        "function foo(a){return a+a;}; foo(x++);", "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction27
  public void testCanInlineReferenceToFunction27() {
    helperCanInlineReferenceToFunction(CanInlineResult.YES,
        "function foo(a){return a+a;}; " +
        "function x() { foo(x++); }",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction28
  public void testCanInlineReferenceToFunction28() {
    
    helperCanInlineReferenceToFunction(CanInlineResult.NO,
        "function foo(a){return true;}; foo(goo());", "foo", INLINE_DIRECT);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction29
  public void testCanInlineReferenceToFunction29() {
    helperCanInlineReferenceToFunction(NEW_VARS_IN_GLOBAL_SCOPE,
        "function foo(a){return true;}; foo(goo());", "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction30
  public void testCanInlineReferenceToFunction30() {
    helperCanInlineReferenceToFunction(CanInlineResult.YES,
        "function foo(a){return true;}; " +
        "function x() { foo(goo()); }",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction31
  public void testCanInlineReferenceToFunction31() {
    helperCanInlineReferenceToFunction(CanInlineResult.YES,
        "function foo(a) {return true;}; " +
        "function x() {foo.call(this, 1);}",
        "foo", INLINE_DIRECT);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction32
  public void testCanInlineReferenceToFunction32() {
    helperCanInlineReferenceToFunction(CanInlineResult.NO,
        "function foo(a){return true;}; " +
        "function x() { foo.apply(this, [1]); }",
        "foo", INLINE_DIRECT);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction33
  public void testCanInlineReferenceToFunction33() {
    
    helperCanInlineReferenceToFunction(CanInlineResult.YES,
        "function foo(a){return true;}; " +
        "function x() { foo.bar(this, 1); }",
        "foo", INLINE_DIRECT);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction34
  public void testCanInlineReferenceToFunction34() {
    helperCanInlineReferenceToFunction(CanInlineResult.YES,
        "function foo(a){return true;}; " +
        "function x() { foo.call(this, goo()); }",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction35
  public void testCanInlineReferenceToFunction35() {
    helperCanInlineReferenceToFunction(CanInlineResult.NO,
        "function foo(a){return true;}; " +
        "function x() { foo.apply(this, goo()); }",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction36
  public void testCanInlineReferenceToFunction36() {
    helperCanInlineReferenceToFunction(CanInlineResult.YES,
        "function foo(a){return true;}; " +
        "function x() { foo.bar(this, goo()); }",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction37
  public void testCanInlineReferenceToFunction37() {
    helperCanInlineReferenceToFunction(CanInlineResult.NO,
        "function foo(a){return true;}; " +
        "function x() { foo.call(null, 1); }",
        "foo", INLINE_DIRECT);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction38
  public void testCanInlineReferenceToFunction38() {
    helperCanInlineReferenceToFunction(CanInlineResult.NO,
        "function foo(a){return true;}; " +
        "function x() { foo.call(null, goo()); }",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction39
  public void testCanInlineReferenceToFunction39() {
    helperCanInlineReferenceToFunction(CanInlineResult.NO,
        "function foo(a){return true;}; " +
        "function x() { foo.call(bar, 1); }",
        "foo", INLINE_DIRECT);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction40
  public void testCanInlineReferenceToFunction40() {
    helperCanInlineReferenceToFunction(CanInlineResult.NO,
        "function foo(a){return true;}; " +
        "function x() { foo.call(bar, goo()); }",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction41
  public void testCanInlineReferenceToFunction41() {
    helperCanInlineReferenceToFunction(CanInlineResult.NO,
        "function foo(a){return true;}; " +
        "function x() { foo.call(new bar(), 1); }",
        "foo", INLINE_DIRECT);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction42
  public void testCanInlineReferenceToFunction42() {
    helperCanInlineReferenceToFunction(CanInlineResult.NO,
        "function foo(a){return true;}; " +
        "function x() { foo.call(new bar(), goo()); }",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction43
  public void testCanInlineReferenceToFunction43() {
    
    helperCanInlineReferenceToFunction(CanInlineResult.NO,
        "function foo(){return true;}; " +
        "function x() { foo.call(); }",
        "foo", INLINE_DIRECT);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction44
  public void testCanInlineReferenceToFunction44() {
    
    helperCanInlineReferenceToFunction(CanInlineResult.NO,
        "function foo(){return true;}; " +
        "function x() { foo.call(); }",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction45
  public void testCanInlineReferenceToFunction45() {
    
    helperCanInlineReferenceToFunction(CanInlineResult.YES,
        "function foo(){return function() {return true;}}; foo();",
        "foo", INLINE_DIRECT);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction46
  public void testCanInlineReferenceToFunction46() {
    
    helperCanInlineReferenceToFunction(CanInlineResult.YES,
        "function foo(){return function() {return true;}}; foo();",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction47
  public void testCanInlineReferenceToFunction47() {
    
    helperCanInlineReferenceToFunction(CanInlineResult.NO,
        "function foo(){var a; return function() {return true;}}; foo();",
        "foo", INLINE_DIRECT);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction48
  public void testCanInlineReferenceToFunction48() {
    
    
    helperCanInlineReferenceToFunction(CanInlineResult.YES,
        "function foo(){var a; return function() {return true;}}; foo();",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction49
  public void testCanInlineReferenceToFunction49() {
    
    helperCanInlineReferenceToFunction(CanInlineResult.YES,
        "function foo(){return function() {var a; return true;}}; foo();",
        "foo", INLINE_DIRECT);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction50
  public void testCanInlineReferenceToFunction50() {
    
    helperCanInlineReferenceToFunction(CanInlineResult.YES,
        "function foo(){return function() {var a; return true;}}; foo();",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction51
  public void testCanInlineReferenceToFunction51() {
    
    helperCanInlineReferenceToFunction(CanInlineResult.YES,
        "function foo(){function x() {var a; return true;} return x}; foo();",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunctionInExpression1
  public void testCanInlineReferenceToFunctionInExpression1() {
    
    helperCanInlineReferenceToFunction(CanInlineResult.YES,
        "function foo(a){return true;}; " +
        "function x() { if (foo(1)) throw 'test'; }",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunctionInExpression2
  public void testCanInlineReferenceToFunctionInExpression2() {
    
    helperCanInlineReferenceToFunction(CanInlineResult.YES,
        "function foo(a){return true;}; " +
        "function x() { return foo(1); }",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunctionInExpression3
  public void testCanInlineReferenceToFunctionInExpression3() {
    
    helperCanInlineReferenceToFunction(CanInlineResult.YES,
        "function foo(a){return true;}; " +
        "function x() { switch(foo(1)) { default:break; } }",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunctionInExpression4
  public void testCanInlineReferenceToFunctionInExpression4() {
    
    helperCanInlineReferenceToFunction(CanInlineResult.YES,
        "function foo(a){return true;}; " +
        "function x() {foo(1)?0:1 }",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunctionInExpression5
  public void testCanInlineReferenceToFunctionInExpression5() {
    
    helperCanInlineReferenceToFunction(CanInlineResult.NO,
        "function foo(a){return true;}; " +
        "function x() {true?foo(1):1 }",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunctionInExpression5a
 public void testCanInlineReferenceToFunctionInExpression5a() {
    
    helperCanInlineReferenceToFunction(
        CanInlineResult.AFTER_DECOMPOSITION,
        "function foo(a){return true;}; " +
        "function x() {true?foo(1):1 }",
        "foo", INLINE_BLOCK, true);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunctionInExpression6
  public void testCanInlineReferenceToFunctionInExpression6() {
    
    helperCanInlineReferenceToFunction(CanInlineResult.YES,
        "function foo(a){return true;}; " +
        "function x() {foo(1) && 1 }",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunctionInExpression7
  public void testCanInlineReferenceToFunctionInExpression7() {
    
    helperCanInlineReferenceToFunction(CanInlineResult.NO,
        "function foo(a){return true;}; " +
        "function x() {1 && foo(1) }",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunctionInExpression7a
  public void testCanInlineReferenceToFunctionInExpression7a() {
    
    helperCanInlineReferenceToFunction(
        CanInlineResult.AFTER_DECOMPOSITION,
        "function foo(a){return true;}; " +
        "function x() {1 && foo(1) }",
        "foo", INLINE_BLOCK, true);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunctionInExpression8
  public void testCanInlineReferenceToFunctionInExpression8() {
    
    helperCanInlineReferenceToFunction(CanInlineResult.YES,
        "function foo(a){return true;}; " +
        "function x() {1 + foo(1) }",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunctionInExpression9
  public void testCanInlineReferenceToFunctionInExpression9() {
    
    helperCanInlineReferenceToFunction(CanInlineResult.YES,
        "function foo(a){return true;}; " +
        "function x() {var b = 1 + foo(1)}",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunctionInExpression10
  public void testCanInlineReferenceToFunctionInExpression10() {
    
    helperCanInlineReferenceToFunction(CanInlineResult.NO,
        "function foo(a){return true;}; " +
        "function x() {var b; b += 1 + foo(1) }",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunctionInExpression10a
  public void testCanInlineReferenceToFunctionInExpression10a() {
    
    helperCanInlineReferenceToFunction(
        CanInlineResult.AFTER_DECOMPOSITION,
        "function foo(a){return true;}; " +
        "function x() {var b; b += 1 + foo(1) }",
        "foo", INLINE_BLOCK, true);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunctionInExpression12
  public void testCanInlineReferenceToFunctionInExpression12() {
    helperCanInlineReferenceToFunction(CanInlineResult.YES,
        "function foo(a){return true;}; " +
        "function x() {var a,b,c; a = b = c = foo(1) }",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunctionInExpression13
  public void testCanInlineReferenceToFunctionInExpression13() {
    helperCanInlineReferenceToFunction(CanInlineResult.YES,
        "function foo(a){return true;}; " +
        "function x() {var a,b,c; a = b = c = 1 + foo(1) }",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunctionInExpression14
  public void testCanInlineReferenceToFunctionInExpression14() {
    
    helperCanInlineReferenceToFunction(CanInlineResult.NO,
        "var a = {}, b = {}, c;" +
        "a.test = 'a';" +
        "b.test = 'b';" +
        "c = a;" +
        "function foo(){c = b; return 'foo'};" +
        "c.test=foo();",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunctionInExpression14a
  public void testCanInlineReferenceToFunctionInExpression14a() {
    
    helperCanInlineReferenceToFunction(
        CanInlineResult.AFTER_DECOMPOSITION,
        "var a = {}, b = {}, c;" +
        "a.test = 'a';" +
        "b.test = 'b';" +
        "c = a;" +
        "function foo(){c = b; return 'foo'};" +
        "c.test=foo();",
        "foo", INLINE_BLOCK, true);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunctionInExpression18
  public void testCanInlineReferenceToFunctionInExpression18() {
    
    helperCanInlineReferenceToFunction(CanInlineResult.YES,
        "function foo(){return _g();}; " +
        "function x() {1 + foo()() }",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunctionInExpression19
  public void testCanInlineReferenceToFunctionInExpression19() {
    
    
    
    helperCanInlineReferenceToFunction(CanInlineResult.NO,
        "function foo(){return a;}; " +
        "function x() {1 + _g(foo()) }",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunctionInExpression19a
  public void testCanInlineReferenceToFunctionInExpression19a() {
    
    
    
    helperCanInlineReferenceToFunction(
        CanInlineResult.AFTER_DECOMPOSITION,
        "function foo(){return a;}; " +
        "function x() {1 + _g(foo()) }",
        "foo", INLINE_BLOCK, true);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunctionInExpression21
  public void testCanInlineReferenceToFunctionInExpression21() {
    
    
    
    
    helperCanInlineReferenceToFunction(CanInlineResult.NO,
        "var z = {};" +
        "function foo(a){z = {};return true;}; " +
        "function x() { z.gack = foo(1) }",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunctionInExpression21a
  public void testCanInlineReferenceToFunctionInExpression21a() {
    
    
    
    
    helperCanInlineReferenceToFunction(
        CanInlineResult.AFTER_DECOMPOSITION,
        "var z = {};" +
        "function foo(a){z = {};return true;}; " +
        "function x() { z.gack = foo(1) }",
        "foo", INLINE_BLOCK, true);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunctionInExpression22
  public void testCanInlineReferenceToFunctionInExpression22() {
    
    helperCanInlineReferenceToFunction(CanInlineResult.NO,
        "function foo(){return a;}; " +
        "function x() {1 + _g(_a(), foo()) }",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunctionInExpression22a
  public void testCanInlineReferenceToFunctionInExpression22a() {
    
    helperCanInlineReferenceToFunction(
        CanInlineResult.AFTER_DECOMPOSITION,
        "function foo(){return a;}; " +
        "function x() {1 + _g(_a(), foo()) }",
        "foo", INLINE_BLOCK, true);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunctionInExpression23
  public void testCanInlineReferenceToFunctionInExpression23() {
    
    helperCanInlineReferenceToFunction(CanInlineResult.NO,
        "function foo(){return a;}; " +
        "function x() {1 + _g(_a(), foo.call(this)) }",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunctionInExpression23a
  public void testCanInlineReferenceToFunctionInExpression23a() {
    
    helperCanInlineReferenceToFunction(
        CanInlineResult.AFTER_DECOMPOSITION,
        "function foo(){return a;}; " +
        "function x() {1 + _g(_a(), foo.call(this)) }",
        "foo", INLINE_BLOCK, true);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInline1
  public void testInline1() {
    helperInlineReferenceToFunction(
        "function foo(){}; foo();",
        "function foo(){}; void 0",
        "foo", INLINE_DIRECT);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInline2
  public void testInline2() {
    helperInlineReferenceToFunction(
        "function foo(){}; foo();",
        "function foo(){}; {}",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInline3
  public void testInline3() {
    helperInlineReferenceToFunction(
        "function foo(){return;}; foo();",
        "function foo(){return;}; {}",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInline4
  public void testInline4() {
    helperInlineReferenceToFunction(
        "function foo(){return true;}; foo();",
        "function foo(){return true;}; true;",
        "foo", INLINE_DIRECT);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInline5
  public void testInline5() {
    helperInlineReferenceToFunction(
        "function foo(){return true;}; foo();",
        "function foo(){return true;}; {true;}",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInline6
  public void testInline6() {
    
    helperInlineReferenceToFunction(
        "function foo(){return true;}; var x=foo();",
        "function foo(){return true;}; var x=true;",
        "foo", INLINE_DIRECT);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInline7
  public void testInline7() {
    helperInlineReferenceToFunction(
        "function foo(){return true;}; var x=foo();",
        "function foo(){return true;}; var x;" +
            "{x=true}",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInline8
  public void testInline8() {
    
    helperInlineReferenceToFunction(
        "function foo(){return true;}; var x; x=foo();",
        "function foo(){return true;}; var x; x=true;",
        "foo", INLINE_DIRECT);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInline9
  public void testInline9() {
    helperInlineReferenceToFunction(
        "function foo(){return true;}; var x; x=foo();",
        "function foo(){return true;}; var x;{x=true}",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInline10
  public void testInline10() {
    
    helperInlineReferenceToFunction(
        "function foo(){return true;}; var x; x=x+foo();",
        "function foo(){return true;}; var x; x=x+true;",
        "foo", INLINE_DIRECT);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInline11
  public void testInline11() {
    
    helperInlineReferenceToFunction(
        "function foo(a){return true;}; foo(x);",
        "function foo(a){return true;}; true;",
        "foo", INLINE_DIRECT);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInline12
  public void testInline12() {
    helperInlineReferenceToFunction(
        "function foo(a){return true;}; foo(x);",
        "function foo(a){return true;}; {true}",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInline13
  public void testInline13() {
    
    helperInlineReferenceToFunction(
        "function foo(a){return a;}; " +
        "function x() { foo(x++); }",
        "function foo(a){return a;}; " +
        "function x() {{var a$$inline_1=x++;" +
            "a$$inline_1}}",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInline14
  public void testInline14() {
    
    helperInlineReferenceToFunction(
        "function foo(a){return a+a;}; foo(x++);",
        "function foo(a){return a+a;}; " +
            "{var a$$inline_1=x++;" +
            " a$$inline_1+" +
            "a$$inline_1;}",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInline15
  public void testInline15() {
    
    helperInlineReferenceToFunction(
        "function foo(a){return a+a;}; foo(new Date());",
        "function foo(a){return a+a;}; " +
            "{var a$$inline_1=new Date();" +
            " a$$inline_1+" +
            "a$$inline_1;}",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInline16
  public void testInline16() {
    
    helperInlineReferenceToFunction(
        "function foo(a){return a+a;}; foo(function(){});",
        "function foo(a){return a+a;}; " +
            "{var a$$inline_1=function(){};" +
            " a$$inline_1+" +
            "a$$inline_1;}",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInline17
  public void testInline17() {
    
    helperInlineReferenceToFunction(
        "function foo(a){return true;}; foo(goo());",
        "function foo(a){return true;};" +
            "{var a$$inline_1=goo();true}",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInline18
  public void testInline18() {
    
    helperInlineReferenceToFunction(
        "function foo(a){var b;return a;}; " +
            "function x() { foo(goo()); }",
            "function foo(a){var b;return a;}; " +
            "function x() {{var a$$inline_2=goo();" +
                "var b$$inline_3;a$$inline_2}}",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInline19
  public void testInline19() {
    
    helperInlineReferenceToFunction(
        "var x = 1; var y = 2;" +
        "function foo(a,b){x = b; y = a;}; " +
        "function bar() { foo(x,y); }",
        "var x = 1; var y = 2;" +
        "function foo(a,b){x = b; y = a;}; " +
        "function bar() {" +
           "{var a$$inline_2=x;" +
            "x = y;" +
            "y = a$$inline_2;}" +
        "}",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInline19b
  public void testInline19b() {
    helperInlineReferenceToFunction(
        "var x = 1; var y = 2;" +
        "function foo(a,b){y = a; x = b;}; " +
        "function bar() { foo(x,y); }",
        "var x = 1; var y = 2;" +
        "function foo(a,b){y = a; x = b;}; " +
        "function bar() {" +
           "{var b$$inline_3=y;" +
            "y = x;" +
            "x = b$$inline_3;}" +
        "}",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInlineIntoLoop
  public void testInlineIntoLoop() {
    helperInlineReferenceToFunction(
        "function foo(a){var b;return a;}; " +
        "for(;1;){ foo(1); }",
        "function foo(a){var b;return a;}; " +
        "for(;1;){ {" +
            "var b$$inline_3=void 0;1}}",
        "foo", INLINE_BLOCK);

    helperInlineReferenceToFunction(
        "function foo(a){var b;return a;}; " +
        "do{ foo(1); } while(1)",
        "function foo(a){var b;return a;}; " +
        "do{ {" +
            "var b$$inline_3=void 0;1}}while(1)",
        "foo", INLINE_BLOCK);

    helperInlineReferenceToFunction(
        "function foo(a){for(var b in c)return a;}; " +
        "for(;1;){ foo(1); }",
        "function foo(a){var b;for(b in c)return a;}; " +
        "for(;1;){ {JSCompiler_inline_label_foo_4:{" +
            "var b$$inline_3=void 0;for(b$$inline_3 in c){" +
              "1;break JSCompiler_inline_label_foo_4" +
            "}}}}",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInlineFunctionWithInnerFunction1
  public void testInlineFunctionWithInnerFunction1() {
    
    helperInlineReferenceToFunction(
        "function foo(){return function() {return true;}}; foo();",
        "function foo(){return function() {return true;}};" +
            "(function() {return true;})",
        "foo", INLINE_DIRECT);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInlineFunctionWithInnerFunction2
  public void testInlineFunctionWithInnerFunction2() {
    
    helperInlineReferenceToFunction(
        "function foo(){return function() {return true;}}; foo();",
        "function foo(){return function() {return true;}};" +
            "{(function() {return true;})}",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInlineFunctionWithInnerFunction3
  public void testInlineFunctionWithInnerFunction3() {
    
    helperInlineReferenceToFunction(
        "function foo(){return function() {var a; return true;}}; foo();",
        "function foo(){return function() {var a; return true;}};" +
            "(function() {var a; return true;});",
        "foo", INLINE_DIRECT);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInlineFunctionWithInnerFunction4
  public void testInlineFunctionWithInnerFunction4() {
    
    helperInlineReferenceToFunction(
        "function foo(){return function() {var a; return true;}}; foo();",
        "function foo(){return function() {var a; return true;}};" +
            "{(function() {var a$$inline_0; return true;});}",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInlineFunctionWithInnerFunction5
  public void testInlineFunctionWithInnerFunction5() {
    
    helperInlineReferenceToFunction(
        "function foo(){function x() {var a; return true;} return x}; foo();",
        "function foo(){function x(){var a;return true}return x};" +
            "{function x$$inline_1(){var a$$inline_2;return true}x$$inline_1}",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInlineReferenceInExpression1
  public void testInlineReferenceInExpression1() {
    
    helperInlineReferenceToFunction(
        "function foo(a){return true;}; " +
        "function x() { if (foo(1)) throw 'test'; }",
        "function foo(a){return true;}; " +
        "function x() { {var JSCompiler_inline_result$$0; " +
        "JSCompiler_inline_result$$0=true;}" +
        "if (JSCompiler_inline_result$$0) throw 'test'; }",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInlineReferenceInExpression2
  public void testInlineReferenceInExpression2() {
    
    helperInlineReferenceToFunction(
        "function foo(a){return true;}; " +
        "function x() { return foo(1); }",
        "function foo(a){return true;}; " +
        "function x() { {var JSCompiler_inline_result$$0; " +
        "JSCompiler_inline_result$$0=true;}" +
        "return JSCompiler_inline_result$$0; }",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInlineReferenceInExpression3
  public void testInlineReferenceInExpression3() {
    
    helperInlineReferenceToFunction(
        "function foo(a){return true;}; " +
        "function x() { switch(foo(1)) { default:break; } }",
        "function foo(a){return true;}; " +
        "function x() { {var JSCompiler_inline_result$$0; " +
        "JSCompiler_inline_result$$0=true;}" +
        "switch(JSCompiler_inline_result$$0) { default:break; } }",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInlineReferenceInExpression4
  public void testInlineReferenceInExpression4() {
    
    helperInlineReferenceToFunction(
        "function foo(a){return true;}; " +
        "function x() {foo(1)?0:1 }",
        "function foo(a){return true;}; " +
        "function x() { {var JSCompiler_inline_result$$0; " +
        "JSCompiler_inline_result$$0=true;}" +
        "JSCompiler_inline_result$$0?0:1 }",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInlineReferenceInExpression5
  public void testInlineReferenceInExpression5() {
    
    helperInlineReferenceToFunction(
        "function foo(a){return true;}; " +
        "function x() {foo(1)&&1 }",
        "function foo(a){return true;}; " +
        "function x() { {var JSCompiler_inline_result$$0; " +
        "JSCompiler_inline_result$$0=true;}" +
        "JSCompiler_inline_result$$0&&1 }",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInlineReferenceInExpression6
  public void testInlineReferenceInExpression6() {
    
    helperInlineReferenceToFunction(
        "function foo(a){return true;}; " +
        "function x() {1 + foo(1) }",
        "function foo(a){return true;}; " +
        "function x() { {var JSCompiler_inline_result$$0; " +
        "JSCompiler_inline_result$$0=true;}" +
        "1 + JSCompiler_inline_result$$0 }",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInlineReferenceInExpression7
  public void testInlineReferenceInExpression7() {
    
    helperInlineReferenceToFunction(
        "function foo(a){return true;}; " +
        "function x() {foo(1) && 1 }",
        "function foo(a){return true;}; " +
        "function x() { {var JSCompiler_inline_result$$0; " +
        "JSCompiler_inline_result$$0=true;}" +
        "JSCompiler_inline_result$$0&&1 }",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInlineReferenceInExpression8
  public void testInlineReferenceInExpression8() {
    
    helperInlineReferenceToFunction(
        "function foo(a){return true;}; " +
        "function x() {1 + foo(1) }",
        "function foo(a){return true;}; " +
        "function x() { {var JSCompiler_inline_result$$0; " +
        "JSCompiler_inline_result$$0=true;}" +
        "1 + JSCompiler_inline_result$$0 }",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInlineReferenceInExpression9
  public void testInlineReferenceInExpression9() {
    
    helperInlineReferenceToFunction(
        "function foo(a){return true;}; " +
        "function x() {var b = 1 + foo(1)}",
        "function foo(a){return true;}; " +
        "function x() { {var JSCompiler_inline_result$$0; " +
        "JSCompiler_inline_result$$0=true;}" +
        "var b = 1 + JSCompiler_inline_result$$0 }",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInlineReferenceInExpression11
  public void testInlineReferenceInExpression11() {
    
    helperInlineReferenceToFunction(
        "function foo(a){return true;}; " +
        "function x() {a:foo(1)?0:1 }",
        "function foo(a){return true;}; " +
        "function x() { a:{{var JSCompiler_inline_result$$0; " +
        "JSCompiler_inline_result$$0=true;}" +
        "JSCompiler_inline_result$$0?0:1 }}",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInlineReferenceInExpression12
  public void testInlineReferenceInExpression12() {
    helperInlineReferenceToFunction(
        "function foo(a){return true;}; " +
        "function x() {1?foo(1):1 }",
        "function foo(a){return true;}; " +
        "function x() { if(1) { {true;} } else { 1 }}",
        "foo", INLINE_BLOCK, true);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInlineReferenceInExpression13
  public void testInlineReferenceInExpression13() {
    helperInlineReferenceToFunction(
        "function foo(a){return true;}; " +
        "function x() { goo() + (1?foo(1):1) }",
        "function foo(a){return true;}; " +
        "function x() { var JSCompiler_temp_const$$0=goo();" +
            "var JSCompiler_temp$$1;" +
            "if(1) {" +
            "  {JSCompiler_temp$$1=true;} " +
            "} else {" +
            "  JSCompiler_temp$$1=1;" +
            "}" +
            "JSCompiler_temp_const$$0 + JSCompiler_temp$$1" +
            "}",
        "foo", INLINE_BLOCK, true);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInlineReferenceInExpression14
  public void testInlineReferenceInExpression14() {
    helperInlineReferenceToFunction(
        "var z = {};" +
        "function foo(a){z = {};return true;}; " +
        "function x() { z.gack = foo(1) }",

        "var z = {};" +
        "function foo(a){z = {};return true;}; " +
        "function x() {" +
            "var JSCompiler_temp_const$$0=z;" +
            "{" +
             "var JSCompiler_inline_result$$1;" +
             "z= {};" +
             "JSCompiler_inline_result$$1 = true;" +
            "}" +
            "JSCompiler_temp_const$$0.gack = JSCompiler_inline_result$$1;" +
        "}",
        "foo", INLINE_BLOCK, true);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInlineReferenceInExpression15
  public void testInlineReferenceInExpression15() {
    helperInlineReferenceToFunction(
        "var z = {};" +
        "function foo(a){z = {};return true;}; " +
        "function x() { z.gack = foo.call(this,1) }",

        "var z = {};" +
        "function foo(a){z = {};return true;}; " +
        "function x() {" +
            "var JSCompiler_temp_const$$0=z;" +
            "{" +
             "var JSCompiler_inline_result$$1;" +
             "z= {};" +
             "JSCompiler_inline_result$$1 = true;" +
            "}" +
            "JSCompiler_temp_const$$0.gack = JSCompiler_inline_result$$1;" +
        "}",
        "foo", INLINE_BLOCK, true);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInlineReferenceInExpression16
  public void testInlineReferenceInExpression16() {
    helperInlineReferenceToFunction(
        "var z = {};" +
        "function foo(a){z = {};return true;}; " +
        "function x() { z[bar()] = foo(1) }",

        "var z = {};" +
        "function foo(a){z = {};return true;}; " +
        "function x() {" +
            "var JSCompiler_temp_const$$1=z;" +
            "var JSCompiler_temp_const$$0=bar();" +
            "{" +
             "var JSCompiler_inline_result$$2;" +
             "z= {};" +
             "JSCompiler_inline_result$$2 = true;" +
            "}" +
            "JSCompiler_temp_const$$1[JSCompiler_temp_const$$0] = " +
                "JSCompiler_inline_result$$2;" +
        "}",
        "foo", INLINE_BLOCK, true);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInlineReferenceInExpression17
  public void testInlineReferenceInExpression17() {
    helperInlineReferenceToFunction(
        "var z = {};" +
        "function foo(a){z = {};return true;}; " +
        "function x() { z.y.x.gack = foo(1) }",

        "var z = {};" +
        "function foo(a){z = {};return true;}; " +
        "function x() {" +
            "var JSCompiler_temp_const$$0=z.y.x;" +
            "{" +
             "var JSCompiler_inline_result$$1;" +
             "z= {};" +
             "JSCompiler_inline_result$$1 = true;" +
            "}" +
            "JSCompiler_temp_const$$0.gack = JSCompiler_inline_result$$1;" +
        "}",
        "foo", INLINE_BLOCK, true);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInlineWithinCalls1
  public void testInlineWithinCalls1() {
    
    helperInlineReferenceToFunction(
        "function foo(){return _g;}; " +
        "function x() {1 + foo()() }",
        "function foo(){return _g;}; " +
        "function x() { {var JSCompiler_inline_result$$0; " +
        "JSCompiler_inline_result$$0=_g;}" +
        "1 + JSCompiler_inline_result$$0() }",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInlineAssignmentToConstant
  public void testInlineAssignmentToConstant() {
    
    helperInlineReferenceToFunction(
        "function foo(){return _g;}; " +
        "function x(){var CONSTANT_RESULT = foo(); }",

        "function foo(){return _g;}; " +
        "function x() {" +
        "  {var JSCompiler_inline_result$$0; JSCompiler_inline_result$$0=_g;}" +
        "  var CONSTANT_RESULT = JSCompiler_inline_result$$0;" +
        "}",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testBug1897706
  public void testBug1897706() {
    helperInlineReferenceToFunction(
        "function foo(a){}; foo(x())",
        "function foo(a){}; {var a$$inline_1=x()}",
        "foo", INLINE_BLOCK);

    helperInlineReferenceToFunction(
        "function foo(a){bar()}; foo(x())",
        "function foo(a){bar()}; {var a$$inline_1=x();bar()}",
        "foo", INLINE_BLOCK);

    helperInlineReferenceToFunction(
        "function foo(a,b){bar()}; foo(x(),y())",
        "function foo(a,b){bar()};" +
        "{var a$$inline_2=x();var b$$inline_3=y();bar()}",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionNamesTest::testFunctionsNamesAndIds
  public void testFunctionsNamesAndIds() {
    final String jsSource =
        "goog.widget = function(str) {\n" +
        "  this.member_fn = function() {};\n" +
        "  local_fn = function() {};\n" +
        "  (function(a){})(1);\n" +
        "}\n" +
        "function foo() {\n" +
        "  function bar() {}\n" +
        "}\n" +
        "literal = {f1 : function(){}, f2 : function(){}};\n" +
        "goog.array.map(arr, function named(){});\n" +
        "goog.array.map(arr, function(){});\n" +
        "named_twice = function quax(){};\n" +
        "recliteral = {l1 : {l2 : function(){}}};\n" +
        "namedliteral = {n1 : function litnamed(){}};\n" +
        "namedrecliteral = {n1 : {n2 : function reclitnamed(){}}};\n" +
        "numliteral = {1 : function(){}};\n" +
        "recnumliteral = {1 : {a : function(){}}};\n";

    testSame(jsSource);

    final Map<Integer, String> idNameMap = Maps.newLinkedHashMap();
    int count = 0;
    for (Node f : functionNames.getFunctionNodeList()) {
      int id = functionNames.getFunctionId(f);
      String name = functionNames.getFunctionName(f);
      idNameMap.put(id, name);
      count++;
    }

    assertEquals("Unexpected number of functions", 16, count);

    final Map<Integer, String> expectedMap = Maps.newLinkedHashMap();

    expectedMap.put(0, "goog.widget.member_fn");
    expectedMap.put(1, "goog.widget::local_fn");
    expectedMap.put(2, "goog.widget::<anonymous>");
    expectedMap.put(3, "goog.widget");
    expectedMap.put(4, "foo::bar");
    expectedMap.put(5, "foo");
    expectedMap.put(6, "literal.f1");
    expectedMap.put(7, "literal.f2");
    expectedMap.put(8, "named");
    expectedMap.put(9, "<anonymous>");
    expectedMap.put(10, "quax");
    expectedMap.put(11, "recliteral.l1.l2");
    expectedMap.put(12, "litnamed");
    expectedMap.put(13, "reclitnamed");
    expectedMap.put(14, "numliteral.__2");
    expectedMap.put(15, "recnumliteral.__3.a");
    assertEquals("Function id/name mismatch",
                 expectedMap, idNameMap);
  }

// com.google.javascript.jscomp.FunctionRewriterTest::testReplaceReturnConst1
  public void testReplaceReturnConst1() {
    String source = "a.prototype.foo = function() {return \"foobar\"}";
    checkCompilesToSame(source, 3);
    checkCompilesTo(source,
                    RETURNARG_HELPER,
                    "a.prototype.foo = JSCompiler_returnArg(\"foobar\")",
                    4);
  }

// com.google.javascript.jscomp.FunctionRewriterTest::testReplaceReturnConst2
  public void testReplaceReturnConst2() {
    checkCompilesToSame("a.prototype.foo = function() {return foobar}", 10);
  }

// com.google.javascript.jscomp.FunctionRewriterTest::testReplaceReturnConst3
  public void testReplaceReturnConst3() {
    String source = "a.prototype.foo = function() {return void 0;}";
    checkCompilesToSame(source, 3);
    checkCompilesTo(source,
                    RETURNARG_HELPER,
                    "a.prototype.foo = JSCompiler_returnArg(void 0)",
                    4);
  }

// com.google.javascript.jscomp.FunctionRewriterTest::testReplaceGetter1
  public void testReplaceGetter1() {
    String source = "a.prototype.foo = function() {return this.foo_}";
    checkCompilesToSame(source, 3);
    checkCompilesTo(source,
                    GET_HELPER,
                    "a.prototype.foo = JSCompiler_get(\"foo_\")",
                    4);
  }

// com.google.javascript.jscomp.FunctionRewriterTest::testReplaceGetter2
  public void testReplaceGetter2() {
    checkCompilesToSame("a.prototype.foo = function() {return}", 10);
  }

// com.google.javascript.jscomp.FunctionRewriterTest::testReplaceSetter1
  public void testReplaceSetter1() {
    String source = "a.prototype.foo = function(v) {this.foo_ = v}";
    checkCompilesToSame(source, 4);
    checkCompilesTo(source,
                    SET_HELPER,
                    "a.prototype.foo = JSCompiler_set(\"foo_\")",
                    5);
  }

// com.google.javascript.jscomp.FunctionRewriterTest::testReplaceSetter2
  public void testReplaceSetter2() {
    String source = "a.prototype.foo = function(v, v2) {this.foo_ = v}";
    checkCompilesToSame(source, 3);
    checkCompilesTo(source,
                    SET_HELPER,
                    "a.prototype.foo = JSCompiler_set(\"foo_\")",
                    4);
  }

// com.google.javascript.jscomp.FunctionRewriterTest::testReplaceSetter3
  public void testReplaceSetter3() {
    checkCompilesToSame("a.prototype.foo = function() {this.foo_ = v}", 10);
  }

// com.google.javascript.jscomp.FunctionRewriterTest::testReplaceSetter4
  public void testReplaceSetter4() {
    checkCompilesToSame(
        "a.prototype.foo = function(v, v2) {this.foo_ = v2}", 10);
  }

// com.google.javascript.jscomp.FunctionRewriterTest::testReplaceEmptyFunction1
  public void testReplaceEmptyFunction1() {
    String source = "a.prototype.foo = function() {}";
    checkCompilesToSame(source, 4);
    checkCompilesTo(source,
                    EMPTY_HELPER,
                    "a.prototype.foo = JSCompiler_emptyFn()",
                    5);
  }

// com.google.javascript.jscomp.FunctionRewriterTest::testReplaceEmptyFunction2
  public void testReplaceEmptyFunction2() {
    checkCompilesToSame("function foo() {}", 10);
  }

// com.google.javascript.jscomp.FunctionRewriterTest::testReplaceEmptyFunction3
  public void testReplaceEmptyFunction3() {
    String source = "var foo = function() {}";
    checkCompilesToSame(source, 4);
    checkCompilesTo(source,
                    EMPTY_HELPER,
                    "var foo = JSCompiler_emptyFn()",
                    5);
  }

// com.google.javascript.jscomp.FunctionRewriterTest::testReplaceIdentityFunction1
  public void testReplaceIdentityFunction1() {
    String source = "a.prototype.foo = function(a) {return a}";
    checkCompilesToSame(source, 2);
    checkCompilesTo(source,
                    IDENTITY_HELPER,
                    "a.prototype.foo = JSCompiler_identityFn()",
                    3);
  }

// com.google.javascript.jscomp.FunctionRewriterTest::testReplaceIdentityFunction2
  public void testReplaceIdentityFunction2() {
    checkCompilesToSame("a.prototype.foo = function(a) {return a + 1}", 10);
  }

// com.google.javascript.jscomp.FunctionToBlockMutatorTest::testMutateNoReturnWithoutResultAssignment
  public void testMutateNoReturnWithoutResultAssignment() {
    helperMutate(
        "function foo(){}; foo();",
        "{}",
        "foo");
  }

// com.google.javascript.jscomp.FunctionToBlockMutatorTest::testMutateNoReturnWithResultAssignment
  public void testMutateNoReturnWithResultAssignment() {
    helperMutate(
        "function foo(){}; var result = foo();",
        "{result = void 0}",
        "foo", true, false);
  }

// com.google.javascript.jscomp.FunctionToBlockMutatorTest::testMutateNoValueReturnWithoutResultAssignment
  public void testMutateNoValueReturnWithoutResultAssignment() {
    helperMutate(
        "function foo(){return;}; foo();",
        "{}",
        "foo", null);
  }

// com.google.javascript.jscomp.FunctionToBlockMutatorTest::testMutateNoValueReturnWithResultAssignment
  public void testMutateNoValueReturnWithResultAssignment() {
    helperMutate(
        "function foo(){return;}; var result = foo();",
        "{result = void 0}",
        "foo");
  }

// com.google.javascript.jscomp.FunctionToBlockMutatorTest::testMutateValueReturnWithoutResultAssignment
  public void testMutateValueReturnWithoutResultAssignment() {
    helperMutate(
        "function foo(){return true;}; foo();",
        "{true;}",
        "foo", null);
  }

// com.google.javascript.jscomp.FunctionToBlockMutatorTest::testMutateValueReturnWithResultAssignment
  public void testMutateValueReturnWithResultAssignment() {
    helperMutate(
        "function foo(){return true;}; var x=foo();",
        "{x=true}",
        "foo", "x", true, false);
  }

// com.google.javascript.jscomp.FunctionToBlockMutatorTest::testMutateWithMultipleReturns
  public void testMutateWithMultipleReturns() {
    helperMutate(
        "function foo(){ if (0) {return 0} else {return 1} };" +
          "var result=foo();",
        "{" +
          "JSCompiler_inline_label_foo_0:{" +
            "if(0) {" +
              "result=0; break JSCompiler_inline_label_foo_0" +
            "} else {" +
              "result=1; break JSCompiler_inline_label_foo_0" +
            "} result=void 0" +
          "}" +
        "}",
        "foo", true, false);
  }

// com.google.javascript.jscomp.FunctionToBlockMutatorTest::testMutateWithParameters1
  public void testMutateWithParameters1() {
    
    helperMutate(
        "function foo(a){return true;}; foo(x);",
        "{true}",
        "foo", null);
  }

// com.google.javascript.jscomp.FunctionToBlockMutatorTest::testMutateWithParameters2
  public void testMutateWithParameters2() {
    
    helperMutate(
        "function foo(a){return x;}; foo(x);",
        "{x}",
        "foo", null);
  }

// com.google.javascript.jscomp.FunctionToBlockMutatorTest::testMutateWithParameters3
  public void testMutateWithParameters3() {
    
    helperMutate(
        "function foo(a){return a;}; " +
        "function x() { foo(x++); }",
        "{var a$$inline_1 = x++; a$$inline_1}",
        "foo", null);
  }

// com.google.javascript.jscomp.FunctionToBlockMutatorTest::testMutate8
  public void testMutate8() {
    
    helperMutate(
        "function foo(a){return a+a;}; foo(x++);",
        "{var a$$inline_1 = x++;" +
            "a$$inline_1 + a$$inline_1;}",
        "foo", null);
  }

// com.google.javascript.jscomp.FunctionToBlockMutatorTest::testMutateInitializeUninitializedVars1
  public void testMutateInitializeUninitializedVars1() {
    helperMutate(
        "function foo(a){var b;return a;}; foo(1);",
        "{var b$$inline_3=void 0;1}",
        "foo", null, false, true);
  }

// com.google.javascript.jscomp.FunctionToBlockMutatorTest::testMutateInitializeUninitializedVars2
  public void testMutateInitializeUninitializedVars2() {
    helperMutate(
        "function foo(a){for(var b in c)return a;}; foo(1);",
        "{JSCompiler_inline_label_foo_4:" +
          "{" +
            "for(var b$$inline_3 in c){" +
                "1;break JSCompiler_inline_label_foo_4" +
             "}" +
          "}" +
        "}",
        "foo", null);
  }

// com.google.javascript.jscomp.FunctionToBlockMutatorTest::testMutateCallInLoopVars1
  public void testMutateCallInLoopVars1() {
    
    boolean callInLoop = false;
    helperMutate(
        "function foo(a){var B = bar(); a;}; foo(1);",
        "{var B$$inline_3=bar(); 1;}",
        "foo", null, false, callInLoop);
    
    
    callInLoop = true;
    helperMutate(
        "function foo(a){var B = bar(); a;}; foo(1);",
        "{var B$$inline_3 = bar(); 1;}",
        "foo", null, false, callInLoop);
  }

// com.google.javascript.jscomp.FunctionTypeBuilderTest::testValidBuiltInTypeRedefinition
  public void testValidBuiltInTypeRedefinition() throws Exception {
    testSame(ALL_NATIVE_EXTERN_TYPES, "", null);
  }

// com.google.javascript.jscomp.FunctionTypeBuilderTest::testBuiltInTypeDifferentReturnType
  public void testBuiltInTypeDifferentReturnType() throws Exception {
    testSame(
        "\n"
        + "function String(opt_str) {}\n",
        "", FunctionTypeBuilder.TYPE_REDEFINITION,
        "attempted re-definition of type String\n"
        + "found   : function (new:String, *): number\n"
        + "expected: function (new:String, *): string");
  }

// com.google.javascript.jscomp.FunctionTypeBuilderTest::testBuiltInTypeDifferentNumParams
  public void testBuiltInTypeDifferentNumParams() throws Exception {
    testSame(
        "\n"
        + "function String() {}\n",
        "", FunctionTypeBuilder.TYPE_REDEFINITION,
        "attempted re-definition of type String\n"
        + "found   : function (new:String): string\n"
        + "expected: function (new:String, *): string");
  }

// com.google.javascript.jscomp.FunctionTypeBuilderTest::testBuiltInTypeDifferentNumParams2
  public void testBuiltInTypeDifferentNumParams2() throws Exception {
    testSame(
        "\n"
        + "function String(opt_str, opt_nothing) {}\n",
        "", FunctionTypeBuilder.TYPE_REDEFINITION,
        "attempted re-definition of type String\n"
        + "found   : function (new:String, ?, ?): string\n"
        + "expected: function (new:String, *): string");
  }

// com.google.javascript.jscomp.FunctionTypeBuilderTest::testBuiltInTypeDifferentParamType
  public void testBuiltInTypeDifferentParamType() throws Exception {
    testSame(
        "\n"
        + "function String(opt_str) {}\n",
        "", FunctionTypeBuilder.TYPE_REDEFINITION,
        "attempted re-definition of type String\n"
        + "found   : function (new:String, ?): string\n"
        + "expected: function (new:String, *): string");
  }

// com.google.javascript.jscomp.FunctionTypeBuilderTest::testBadFunctionTypeDefinition
  public void testBadFunctionTypeDefinition() throws Exception {
    testSame(
        "function Function(opt_str) {}\n",
        "", FunctionTypeBuilder.TYPE_REDEFINITION,
        "attempted re-definition of type Function\n"
        + "found   : function (new:Function, ?): ?\n"
        + "expected: function (new:Function, ...[*]): ?");
  }

// com.google.javascript.jscomp.FunctionTypeBuilderTest::testExternSubTypes
  public void testExternSubTypes() throws Exception {
    testSame(ALL_NATIVE_EXTERN_TYPES, "", null);

    List<FunctionType> subtypes = ((InstanceObjectType) getLastCompiler()
        .getTypeRegistry().getType("Error")).getConstructor().getSubTypes();
    for (FunctionType type : subtypes) {
      String typeName = type.getInstanceType().toString();
      FunctionType typeInRegistry = ((InstanceObjectType) getLastCompiler()
          .getTypeRegistry().getType(typeName)).getConstructor();
      assertTrue(typeInRegistry == type);
    }
  }

// com.google.javascript.jscomp.GatherRawExportsTest::testExportsFound1
  public void testExportsFound1() {
    assertExported("var a");
  }

// com.google.javascript.jscomp.GatherRawExportsTest::testExportsFound2
  public void testExportsFound2() {
    assertExported("window['a']", "a");
  }

// com.google.javascript.jscomp.GatherRawExportsTest::testExportsFound3
  public void testExportsFound3() {
    assertExported("window.a", "a");
  }

// com.google.javascript.jscomp.GatherRawExportsTest::testExportsFound4
  public void testExportsFound4() {
    assertExported("this['a']", "a");
  }

// com.google.javascript.jscomp.GatherRawExportsTest::testExportsFound5
  public void testExportsFound5() {
    assertExported("this.a", "a");
  }

// com.google.javascript.jscomp.GatherRawExportsTest::testExportsFound6
  public void testExportsFound6() {
    assertExported("function f() { this['a'] }");
  }

// com.google.javascript.jscomp.GatherRawExportsTest::testExportsFound7
  public void testExportsFound7() {
    assertExported("function f() { this.a }");
  }

// com.google.javascript.jscomp.GatherRawExportsTest::testExportsFound8
  public void testExportsFound8() {
    assertExported("window['foo']", "foo");
  }

// com.google.javascript.jscomp.GatherRawExportsTest::testExportsFound9
  public void testExportsFound9() {
    assertExported("window['a'] = 1;", "a");
  }

// com.google.javascript.jscomp.GatherRawExportsTest::testExportsFound10
  public void testExportsFound10() {
    assertExported("window['a']['b']['c'] = 1;", "a");
  }

// com.google.javascript.jscomp.GatherRawExportsTest::testExportsFound11
  public void testExportsFound11() {
    assertExported("if (window['a'] = 1) alert(x);", "a");
  }

// com.google.javascript.jscomp.GatherRawExportsTest::testExportsFound12
  public void testExportsFound12() {
    assertExported("function foo() { window['a'] = 1; }", "a");
  }

// com.google.javascript.jscomp.GatherRawExportsTest::testExportsFound13
  public void testExportsFound13() {
    assertExported("function foo() {var window; window['a'] = 1; }");
  }

// com.google.javascript.jscomp.GatherRawExportsTest::testExportsFound14
  public void testExportsFound14() {
    assertExported("var a={window:{}}; a.window['b']");
  }

// com.google.javascript.jscomp.GatherRawExportsTest::testExportsFound15
  public void testExportsFound15() {
    assertExported("window.window['b']", "window");
  }

// com.google.javascript.jscomp.GatherRawExportsTest::testExportsFound16
  public void testExportsFound16() {
    
    
    assertExported("var a = window; a['b']");
  }

// com.google.javascript.jscomp.GatherSideEffectSubexpressionsCallbackTest::testAndOr
  public void testAndOr() throws Exception {
    Node andNode = getSideEffectsAndNode();
    checkKeepSimplifiedShortCircuitExpr(andNode,
                                        ImmutableList.of("foo&&(bar=0)"));
  }

// com.google.javascript.jscomp.GatherSideEffectSubexpressionsCallbackTest::testIllegalArgumentIfNotAndOr
  public void testIllegalArgumentIfNotAndOr() throws Exception {
    Node nameNode = Node.newString(Token.NAME, "foo");
    try {
      checkKeepSimplifiedShortCircuitExpr(nameNode,
                                          ImmutableList.<String>of());
      fail("Expected exception");
    } catch (IllegalArgumentException e) {
      
    }
  }

// com.google.javascript.jscomp.GatherSideEffectSubexpressionsCallbackTest::testIllegalArgumentIfNoSideEffectAndOr
  public void testIllegalArgumentIfNoSideEffectAndOr() throws Exception {
    Node andNode = getNoSideEffectsAndNode();
    try {
      checkKeepSimplifiedShortCircuitExpr(andNode,
                                          ImmutableList.<String>of());
      fail("Expected exception");
    } catch (IllegalArgumentException e) {
      
    }
  }

// com.google.javascript.jscomp.GatherSideEffectSubexpressionsCallbackTest::testHook
  public void testHook() throws Exception {
    Node hook = getSideEffectsHookNode();

    checkKeepSimplifiedHookExpr(hook,
                                true,
                                true,
                                ImmutableList.of("foo?bar=0:baz=0"));
  }

// com.google.javascript.jscomp.GatherSideEffectSubexpressionsCallbackTest::testIllegalArgumentIfNotHook
  public void testIllegalArgumentIfNotHook() throws Exception {
    Node nameNode = Node.newString(Token.NAME, "foo");
    try {
      checkKeepSimplifiedHookExpr(nameNode,
                                  true,
                                  true,
                                  ImmutableList.<String>of());
      fail("Expected exception");
    } catch (IllegalArgumentException e) {
      
    }
  }

// com.google.javascript.jscomp.GatherSideEffectSubexpressionsCallbackTest::testIllegalArgumentIfNoSideEffectHook
  public void testIllegalArgumentIfNoSideEffectHook() throws Exception {
    Node hookNode = getNoSideEffectsHookNode();
    try {
      checkKeepSimplifiedHookExpr(hookNode,
                                  true,
                                  true,
                                  ImmutableList.<String>of());
      fail("Expected exception");
    } catch (IllegalArgumentException e) {
      
    }
  }

// com.google.javascript.jscomp.GatherSideEffectSubexpressionsCallbackTest::testIllegalArgumentIfHookKeepNeitherBranch
  public void testIllegalArgumentIfHookKeepNeitherBranch() throws Exception {
    Node hookNode = getSideEffectsHookNode();
    try {
      checkKeepSimplifiedHookExpr(hookNode,
                                  false,
                                  false,
                                  ImmutableList.<String>of());
      fail("Expected exception");
    } catch (IllegalArgumentException e) {
      
    }
  }

// com.google.javascript.jscomp.GenerateExportsTest::testExportSymbol
  public void testExportSymbol() {
    test("function foo() {}",
        "function foo(){}google_exportSymbol(\"foo\",foo)");
  }

// com.google.javascript.jscomp.GenerateExportsTest::testExportSymbolAndProperties
  public void testExportSymbolAndProperties() {
    test("function foo() {}" +
         "foo.prototype.bar = function() {}",
         "function foo(){}" +
         "google_exportSymbol(\"foo\",foo);" +
         "foo.prototype.bar=function(){};" +
         "goog.exportProperty(foo.prototype,\"bar\",foo.prototype.bar)");
  }

// com.google.javascript.jscomp.GenerateExportsTest::testExportSymbolAndConstantProperties
  public void testExportSymbolAndConstantProperties() {
    test("function foo() {}" +
         "foo.BAR = 5;",
         "function foo(){}" +
         "google_exportSymbol(\"foo\",foo);" +
         "foo.BAR=5;" +
         "goog.exportProperty(foo,\"BAR\",foo.BAR)");
  }

// com.google.javascript.jscomp.GenerateExportsTest::testExportVars
  public void testExportVars() {
    test("var FOO = 5",
         "var FOO=5;" +
         "google_exportSymbol(\"FOO\",FOO)");
  }

// com.google.javascript.jscomp.GenerateExportsTest::testNoExport
  public void testNoExport() {
    test("var FOO = 5", "var FOO=5");
  }

// com.google.javascript.jscomp.GenerateExportsTest::testNestedVarAssign
  public void testNestedVarAssign() {
    test("var BAR;\nvar FOO = BAR = 5",
         null, FindExportableNodes.NON_GLOBAL_ERROR);
  }

// com.google.javascript.jscomp.GenerateExportsTest::testNestedAssign
  public void testNestedAssign() {
    test("var BAR;var FOO = {};\nFOO.test = BAR = 5",
         null, FindExportableNodes.NON_GLOBAL_ERROR);
  }

// com.google.javascript.jscomp.GenerateExportsTest::testNonGlobalScopeExport
  public void testNonGlobalScopeExport() {
    test("(function() { var FOO = 5 })()",
         null, FindExportableNodes.NON_GLOBAL_ERROR);
  }

// com.google.javascript.jscomp.GenerateExportsTest::testExportClass
  public void testExportClass() {
    test(" function G() {} foo();",
         "function G() {} google_exportSymbol('G', G); foo();");
  }

// com.google.javascript.jscomp.GenerateExportsTest::testExportSubclass
  public void testExportSubclass() {
    test("var goog = {}; function F() {}" +
         " function G() {} goog.inherits(G, F);",
         "var goog = {}; function F() {}" +
         "function G() {} goog.inherits(G, F); google_exportSymbol('G', G);");
  }

// com.google.javascript.jscomp.GenerateExportsTest::testExportEnum
  public void testExportEnum() {
    
    test(" var E = {A:1, B:2};",
         " var E = {A:1, B:2};" +
         "google_exportSymbol('E', E);");
  }

// com.google.javascript.jscomp.GroupVariableDeclarationsTest::testGroupingUninitializedVarsInScope
  public void testGroupingUninitializedVarsInScope() {
    
    test("var a = 1; f1(); var b;", "var a = 1, b; f1();");
    
    test("var a = \"mangoes\"; f1(); alert(a); var b;",
         "var a = \"mangoes\", b; f1(); alert(a);");
    
    
    test("var a = 1; {var c; alert(c);} var b;",
         "var a = 1, c, b; {alert(c);}");
    
    test("var a = 1; var b = 1; f1(); f2(); var c; var d;",
         "var a = 1, b, c, d; b = 1; f1(); f2();");
    test("var a = 1; var b = 2; var c; f1(); f2(); var d, e;",
         "var a = 1, b, c, d, e; b = 2; f1(); f2();");
    test("var a = 1, b = 2, c; f1(); f2(); var d; var e; " +
         "f3(); f4(); var f = 10; var g; var h = a + b;",
         "var a = 1, b = 2, c, d, e, f, g, h; f1(); f2(); f3(); f4(); " +
         "f = 10; h = a + b;");
  }

// com.google.javascript.jscomp.GroupVariableDeclarationsTest::testGroupingInitializedVarsInScope
  public void testGroupingInitializedVarsInScope() {
    
    test("var a = 1; f1(); var b = 2;", "var a = 1, b; f1(); b = 2;");
    
    test("var a = \"mangoes\"; f1(); alert(a); var b = 2;",
         "var a = \"mangoes\", b; f1(); alert(a); b = 2;");
    
    
    test("var a = 1; {var c = 34; alert(c);} var b = 2;",
         "var a = 1, c, b; {c = 34; alert(c);} b = 2;");
    
    test("var a = 1; var b = 1; f1(); f2(); var c = 3; var d = 4;",
         "var a = 1, b, c, d; b = 1; f1(); f2(); c = 3; d = 4;");
    test("var a = 1; var b = 2; var c; f1(); f2(); var d = 4, e;",
         "var a = 1, b, c, d, e; b = 2; f1(); f2(); d = 4;");
    test("var a = 1, b = 2, c; f1(); f2(); var d; var e = 6; " +
         "f3(); f4(); var f; var g; var h = a + b;",
         "var a = 1, b = 2, c, d, e, f, g, h; f1(); f2(); e = 6; " +
         "f3(); f4(); h = a + b;");
  }

// com.google.javascript.jscomp.GroupVariableDeclarationsTest::testGroupingVarsInForAndForInLoops
  public void testGroupingVarsInForAndForInLoops() {
    
    test("var a = 1; for (var x = 0; x < 10; ++x) {a++;} var y;",
         "var a = 1, x, y; for (x = 0; x < 10; ++x) {a++;}");
    test("var a = 1, x; for (x = 0; x < 10; ++x) {a++;} var y;",
         "var a = 1, x, y; for (x = 0; x < 10; ++x) {a++;}");
    test("var a = 1, x; for (x; x < 10; ++x) {a++;} var y;",
         "var a = 1, x, y; for (x; x < 10; ++x) {a++;}");
    test("var a = 1; for (; a < 10; ++a) {alert(a);} var y;",
         "var a = 1, y; for (; a < 10; ++a) {alert(a);}");
    test("var a = 1; for (var x; x < 10; ++x) {a += 2;} var y = 5;",
         "var a = 1, x, y; for (; x < 10; ++x) {a += 2;} y = 5;");
    
    test("var a = 1; " +
         "for (var a1 = 0, a2 = 10; a1 < 10 && a2 > 0; ++a1, --a2) {}" +
         "var x = 5;",
         "var a = 1, x;" +
         "for (var a1 = 0, a2 = 10; a1 < 10 && a2 > 0; ++a1, --a2) {} " +
         "x = 5;");
    test("var a = 1; " +
         "for (var a1 = 0, a2; a1 < 10 && a2 > 0; ++a1, --a2) {}" +
         "var x = 5;",
         "var a = 1, a1, a2, x;" +
         "for (a1 = 0; a1 < 10 && a2 > 0; ++a1, --a2) {}" +
         "x = 5;");
    test("var a = 1; " +
         "for (var a1, a2; a1 < 10 && a2 > 0; ++a1, --a2) {}" +
         "var x = 5;",
         "var a = 1, a1, a2, x;" +
         "for (; a1 < 10 && a2 > 0; ++a1, --a2) {}" +
         "x = 5;");

    
    test("var a = [1, 2, 3, 4]; for (var z in a) {alert(z);} var y;",
         "var a = [1, 2, 3, 4], z, y; for (z in a) {alert(z);}");
    test("var a = [1, 2, 3, 4]; for (var z in a) {alert(z);} var y = 5;",
         "var a = [1, 2, 3, 4], z, y; for (z in a) {alert(z);} y = 5;");
    test("var a; for (var z in a = [1, 2, 3, 4]) {alert(z);} var y, x = 5;",
         "var a, z, y, x; for (z in a = [1, 2, 3, 4]) {alert(z);} x = 5;");
    test("var a; for (var z = 1 in a = [1, 2, 3, 4]) {alert(z);} var y, x = 5;",
         "var a, y, x; for (var z = 1 in a = [1, 2, 3, 4]) {alert(z);} x = 5;");
    test("var a, z; for (z in a = [1, 2, 3, 4]) {alert(z);} var y, x = 5;",
         "var a, z, y, x; for (z in a = [1, 2, 3, 4]) {alert(z);} x = 5;");
  }

// com.google.javascript.jscomp.GroupVariableDeclarationsTest::testGroupingVarsNestedFunction
  public void testGroupingVarsNestedFunction() {
    test("function f(b) {var x; function g() {var x; a = x; var y;} var a;}",
         "function f(b) {var x, a; function g() {var x, y; a = x;}}");
  }

// com.google.javascript.jscomp.GroupVariableDeclarationsTest::testGroupingVarsInnerFunction
  public void testGroupingVarsInnerFunction() {
    test("function f(b) {var x; h = x * x; var myfn = function() " +
         "{var x; a = x; var y;}; var a;}",
         "function f(b) {var x, myfn, a; h = x * x; myfn = function() " +
         "{var x, y; a = x;};}");
  }

// com.google.javascript.jscomp.GroupVariableDeclarationsTest::testGroupingVarsFirstStatementNotVar
  public void testGroupingVarsFirstStatementNotVar() {
    test("f(); var a; g(); var b;", "f(); var a, b; g();");
  }

// com.google.javascript.jscomp.GroupVariableDeclarationsTest::testGroupingVarsInScopeRegtest
  public void testGroupingVarsInScopeRegtest() {
    
    test("var x = 0, y = 1, z;" +
         "function f1(aa, bb) {" +
         "  if (y) {" +
         "    if (x === 0) {" +
         "      var h, r = 999;" +
         "    }" +
         "  } else {" +
         "    r = 1000;" +
         "  }" +
         "  var mylist = [1, 2, 3, 4];" +
         "  var k1 = 200, k2 = 400;" +
         "  for (var i1 = 0; i1 < 10; ++i1) {" +
         "    for (var i2 in mylist) {" +
         "      alert(i1);" +
         "    }" +
         "  }" +
         "  var jam, q = 100;" +
         "  var myfn = function() {" +
         "    var x = 1;" +
         "    f5();" +
         "    var z = 5;" +
         "  };" +
         "  function f5() {" +
         "    var aa = 5;" +
         "    if (y === 1) {" +
         "      var x = 100;" +
         "    }" +
         "  }" +
         "}" +
         "var h = x + y;" +
         "function g() {" +
         "  y = 0;" +
         "  { var x = 200;}" +
         "  var h = y + x;" +
         "}" +
         "var ggg = 0;",  
         "var x = 0, y = 1, z, h, ggg;" +
         "function f1(aa, bb) {" +
         "  if (y) {" +
         "    if (x === 0) {" +
         "      var h, r = 999, mylist, i1, i2, jam, q, myfn;" +
         "    }" +
         "  } else {" +
         "    r = 1000;" +
         "  }" +
         "  mylist = [1, 2, 3, 4];" +
         "  var k1 = 200, k2 = 400;" +
         "  for (i1 = 0; i1 < 10; ++i1) {" +
         "    for (i2 in mylist) {" +
         "      alert(i1);" +
         "    }" +
         "  }" +
         "  q = 100; " +
         "  myfn = function() {" +
         "    var x = 1, z;" +
         "    f5();" +
         "    z = 5;" +
         "  };" +
         "  function f5() {" +
         "    var aa = 5, x;" +
         "    if (y === 1) {" +
         "      x = 100;" +
         "    }" +
         "  }" +
         "}" +
         "h = x + y;" +
         "function g() {" +
         "  y = 0;" +
         "  { var x = 200, h;}" +
         "  h = y + x;" +
         "}" +
         "ggg = 0;");
  }

// com.google.javascript.jscomp.IgnoreCajaPropertiesTest::testSimpleKey
  public void testSimpleKey() {
    
    test("for (i in x) f(i);",
         "for (var JSCompiler_IgnoreCajaProperties_0 in x)" +
         "  if (!JSCompiler_IgnoreCajaProperties_0.match(/___$/)) {" +
         "    i = JSCompiler_IgnoreCajaProperties_0;" +
         "    { f(i); }" +
         "  }");
    
    test("for (i in x) { f(i); f(i); }",
         "for (var JSCompiler_IgnoreCajaProperties_0 in x)" +
         "  if (!JSCompiler_IgnoreCajaProperties_0.match(/___$/)) {" +
         "    i = JSCompiler_IgnoreCajaProperties_0;" +
         "    { f(i); f(i); }" +
         "  }");
    
    
    test("for (i in x) for (j in y) f(i,j);",
         "for (var JSCompiler_IgnoreCajaProperties_1 in x)" +
         "  if (!JSCompiler_IgnoreCajaProperties_1.match(/___$/)) {" +
         "    i = JSCompiler_IgnoreCajaProperties_1;" +
         "    {" +
         "      for (var JSCompiler_IgnoreCajaProperties_0 in y)" +
         "        if (!JSCompiler_IgnoreCajaProperties_0.match(/___$/)) {" +
         "          j = JSCompiler_IgnoreCajaProperties_0;" +
         "          { f(i,j); }" +
         "        }" +
         "    }" +
         "  }");
  }

// com.google.javascript.jscomp.IgnoreCajaPropertiesTest::testPropertyKey
  public void testPropertyKey() {
    test("for (z.i in x) { f(z.i); f(z.i); }",
         "for (var JSCompiler_IgnoreCajaProperties_0 in x) {" +
         "  if (!JSCompiler_IgnoreCajaProperties_0.match(/___$/)) {" +
         "    z.i = JSCompiler_IgnoreCajaProperties_0;" +
         "    { f(z.i); f(z.i); }" +
         "  }" +
         "}");
  }

// com.google.javascript.jscomp.IgnoreCajaPropertiesTest::testFunctionPropertyKey
  public void testFunctionPropertyKey() {
    
    
    
    test("for (z.j().i in x) { f(z.j().i); f(z.j().i); }",
         "for (var JSCompiler_IgnoreCajaProperties_0 in x) {" +
         "  if (!JSCompiler_IgnoreCajaProperties_0.match(/___$/)) {" +
         "    z.j().i = JSCompiler_IgnoreCajaProperties_0;" +
         "    { f(z.j().i); f(z.j().i); }" +
         "  }" +
         "}");
  }

// com.google.javascript.jscomp.IgnoreCajaPropertiesTest::testVarKey
  public void testVarKey() {
    
    test("for (var j in x) { f(j); }",
         "for (var JSCompiler_IgnoreCajaProperties_0 in x) {" +
         "  if (!JSCompiler_IgnoreCajaProperties_0.match(/___$/)) {" +
         "    var j;" +
         "    j = JSCompiler_IgnoreCajaProperties_0;" +
         "    { f(j); }" +
         "  }" +
         "}");
    
    test("for (var j in x) { f(j); f(j); }",
         "for (var JSCompiler_IgnoreCajaProperties_0 in x) {" +
         "  if (!JSCompiler_IgnoreCajaProperties_0.match(/___$/)) {" +
         "    var j;" +
         "    j = JSCompiler_IgnoreCajaProperties_0;" +
         "    { f(j); f(j); }" +
         "  }" +
         "}");
    
    test("for (var i in x) for (var j in y) f(i,j);",
         "for (var JSCompiler_IgnoreCajaProperties_1 in x)" +
         "  if (!JSCompiler_IgnoreCajaProperties_1.match(/___$/)) {" +
         "    var i;" +
         "    i = JSCompiler_IgnoreCajaProperties_1;" +
         "    {" +
         "      for (var JSCompiler_IgnoreCajaProperties_0 in y)" +
         "        if (!JSCompiler_IgnoreCajaProperties_0.match(/___$/)) {" +
         "          var j;" +
         "          j = JSCompiler_IgnoreCajaProperties_0;" +
         "          { f(i,j); }" +
         "        }" +
         "    }" +
         "  }");
  }

// com.google.javascript.jscomp.IgnoreCajaPropertiesTest::testFourChildFor
  public void testFourChildFor() {
    test("for (i = 0; i < 10; ++i) { f(i); }",
         "for (i = 0; i < 10; ++i) { f(i); }");
  }

// com.google.javascript.jscomp.InferJSDocInfoTest::testNativeCtor
  public void testNativeCtor() {
    testSame(
        " " +
        "function Object(x) {};",
        "var x = new Object();" +
        " var y = new Object();", null);
    assertEquals(
        "Object.",
        findGlobalNameType("x").getJSDocInfo().getBlockDescription());
    assertEquals(
        "Object.",
        findGlobalNameType("y").getJSDocInfo().getBlockDescription());
    assertEquals(
        "Object.",
        globalScope.getVar("y").getType().getJSDocInfo().getBlockDescription());
  }

// com.google.javascript.jscomp.InferJSDocInfoTest::testStructuralFunctions
  public void testStructuralFunctions() {
    testSame(
        " " +
        "function Object(x) {};",
        " " +
        "function fn(x) {};" +
        "var goog = {};" +
        " goog.x = new Object();" +
        " goog.y = fn;", null);
    assertEquals(
        "(Object|null)",
        globalScope.getVar("goog.x").getType().toString());
    assertEquals(
        "Object.",
        globalScope.getVar("goog.x").getType().restrictByNotNullOrUndefined()
        .getJSDocInfo().getBlockDescription());
    assertEquals(
        "Another function.",
        globalScope.getVar("goog.y").getType()
        .getJSDocInfo().getBlockDescription());
  }

// com.google.javascript.jscomp.InferJSDocInfoTest::testInstanceObject
  public void testInstanceObject() {
    
    testSame(
        " function Foo() {}" +
        "var f = new Foo();" +
        " f.bar = 4;");
    ObjectType type = (ObjectType) globalScope.getVar("f").getType();
    assertEquals("Foo", type.toString());
    assertFalse(type.hasProperty("bar"));
    assertNull(type.getOwnPropertyJSDocInfo("bar"));
  }

// com.google.javascript.jscomp.InferJSDocInfoTest::testInterface
  public void testInterface() {
    testSame(
        " function Foo() {}" +
        "var f = new Foo();" +
        " f.bar = 4;");
    ObjectType type = (ObjectType) globalScope.getVar("Foo").getType();
    assertEquals(
        "An interface.",
        type.getJSDocInfo().getBlockDescription());
  }

// com.google.javascript.jscomp.InferJSDocInfoTest::testNamespacedCtor
  public void testNamespacedCtor() {
    testSame(
        "var goog = {};" +
        " goog.Foo = function() {};" +
        "goog.Foo.bar = goog.Foo;" +
        "" +
        "goog.Foo.prototype.baz = goog.Foo;" +
        " var x = new goog.Foo();");
    assertEquals(
        "Hello!",
        findGlobalNameType("x").getJSDocInfo().getBlockDescription());
    assertEquals(
        "Hello!",
        findGlobalNameType("goog.Foo").getJSDocInfo().getBlockDescription());
    assertEquals(
        "Hello!",
        findGlobalNameType(
            "goog.Foo.bar").getJSDocInfo().getBlockDescription());

    assertEquals(
        "Hello!",
        findGlobalNameType(
            "goog.Foo.prototype.baz").getJSDocInfo().getBlockDescription());

    ObjectType proto = (ObjectType) findGlobalNameType("goog.Foo.prototype");
    assertEquals(
        "Bye!",
        proto.getPropertyType("baz").getJSDocInfo().getBlockDescription());
  }

// com.google.javascript.jscomp.InferJSDocInfoTest::testAbstractMethod
  public void testAbstractMethod() {
    testSame(
        " var abstractMethod;" +
        " function Foo() {}" +
        "" +
        "Foo.prototype.bar = abstractMethod;");
    FunctionType abstractMethod =
        (FunctionType) findGlobalNameType("abstractMethod");
    assertNull(abstractMethod.getJSDocInfo());

    FunctionType ctor = (FunctionType) findGlobalNameType("Foo");
    ObjectType proto = ctor.getInstanceType().getImplicitPrototype();
    FunctionType method = (FunctionType) proto.getPropertyType("bar");
    assertEquals(
        "Block description.",
        method.getJSDocInfo().getBlockDescription());
    assertEquals(
        "Block description.",
        proto.getOwnPropertyJSDocInfo("bar").getBlockDescription());
  }

// com.google.javascript.jscomp.InlineCostEstimatorTest::testCost
  public void testCost() {
    checkCost("1", "1");
    checkCost("a", "xx");
    checkCost("a + b", "xx+xx");
    checkCost("foo()", "xx()");
    checkCost("foo(a,b)", "xx(xx,xx)");
    checkCost("10 + foo(a,b)", "10+xx(xx,xx)");
    checkCost("1 + foo(a,b)", "1+xx(xx,xx)");
    checkCost("a ? 1 : 0", "xx?1:0");
    checkCost("a.b", "xx.xx");
    checkCost("new Obj()", "new xx");
    checkCost("function a() {return \"monkey\"}",
              "function xx(){return\"monkey\"}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineEmptyFunction1
  public void testInlineEmptyFunction1() {
    
    test("function foo(){}" +
        "foo();",
        "void 0;");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineEmptyFunction2
  public void testInlineEmptyFunction2() {
    
    test("function foo(){}" +
        "foo(1, new Date, function(){});",
        "void 0;");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineEmptyFunction3
  public void testInlineEmptyFunction3() {
    
    test("function foo(){}" +
        "foo();foo();foo();",
        "void 0;void 0;void 0");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineEmptyFunction4
  public void testInlineEmptyFunction4() {
    
    test("function foo(){}" +
        "foo(x());",
        "{var JSCompiler_inline_anon_param_0=x();}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineEmptyFunction5
  public void testInlineEmptyFunction5() {
    
    
    allowBlockInlining = false;
    testSame("function foo(){}" +
        "foo(x());");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineFunctions1
  public void testInlineFunctions1() {
    
    test("function foo(){ return 4 }" +
        "foo();",
        "4");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineFunctions2
  public void testInlineFunctions2() {
    
    
    test("var t;var AB=function(){return 4};" +
         "function BC(){return 6;}" +
         "CD=function(x){return x + 5};x=CD(3);y=AB();z=BC();",
         "var t;CD=function(x){return x+5};x=CD(3);y=4;z=6"
         );
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineFunctions3
  public void testInlineFunctions3() {
    
    test("var t;var AB=function(){return 4};" +
        "function BC(){return 6;}" +
        "var CD=function(x){return x + 5};x=CD(3);y=AB();z=BC();",
        "var t;x=3+5;y=4;z=6");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineFunctions4
  public void testInlineFunctions4() {
    
    test("var t; var AB = function() { return 4 }; " +
        "function BC() { return 6; }" +
        "CD = 0;" +
        "CD = function(x) { return x + 5 }; x = CD(3); y = AB(); z = BC();",

        "var t;CD=0;CD=function(x){return x+5};x=CD(3);y=4;z=6");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineFunctions5
  public void testInlineFunctions5() {
    
    test("var FOO_FN=function(x,y) { return \"de\" + x + \"nu\" + y };" +
         "var a = FOO_FN(\"ez\", \"ts\")",

         "var a=\"de\"+\"ez\"+\"nu\"+\"ts\"");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineFunctions6
  public void testInlineFunctions6() {
    
    test("function BAR_FN(x, y, z) { return z(foo(x + y)) }" +
         "alert(BAR_FN(1, 2, baz))",

         "alert(baz(foo(1+2)))");
  }

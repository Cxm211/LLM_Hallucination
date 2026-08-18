// buggy code
  public void enterScope(NodeTraversal t) {
    // TODO(user): We CAN do this in the global scope, just need to be
    // careful when something is exported. Liveness uses bit-vector for live
    // sets so I don't see compilation time will be a problem for running this
    // pass in the global scope.
    Scope scope = t.getScope();
    if (scope.isGlobal()) {
      return;
    }
    ControlFlowGraph<Node> cfg = t.getControlFlowGraph();

    LiveVariablesAnalysis liveness =
        new LiveVariablesAnalysis(cfg, scope, compiler);
    // If the function has exactly 2 params, mark them as escaped. This is
    // a work-around for an IE bug where it throws an exception if you
    // write to the parameters of the callback in a sort(). See:
    // http://code.google.com/p/closure-compiler/issues/detail?id=58
    liveness.analyze();

    UndiGraph<Var, Void> interferenceGraph =
        computeVariableNamesInterferenceGraph(
            t, cfg, liveness.getEscapedLocals());

    GraphColoring<Var, Void> coloring =
        new GreedyGraphColoring<Var, Void>(interferenceGraph,
            coloringTieBreaker);

    coloring.color();
    colorings.push(coloring);
  }

  private ExtractionInfo extractMultilineTextualBlock(JsDocToken token,
                                                      WhitespaceOption option) {

    if (token == JsDocToken.EOC || token == JsDocToken.EOL ||
        token == JsDocToken.EOF) {
      return new ExtractionInfo("", token);
    }

    stream.update();
    int startLineno = stream.getLineno();
    int startCharno = stream.getCharno() + 1;

    // Read the content from the first line.
    String line = stream.getRemainingJSDocLine();

    if (option != WhitespaceOption.PRESERVE) {
      line = line.trim();
    }

    StringBuilder builder = new StringBuilder();
    builder.append(line);

    state = State.SEARCHING_ANNOTATION;
    token = next();

    boolean ignoreStar = false;

    do {
      switch (token) {
        case STAR:
          if (!ignoreStar) {
            if (builder.length() > 0) {
              builder.append(' ');
            }

            builder.append('*');
          }

          token = next();
          continue;

        case EOL:
          if (option != WhitespaceOption.SINGLE_LINE) {
            builder.append("\n");
          }

          ignoreStar = true;
          token = next();
          continue;

        case ANNOTATION:
        case EOC:
        case EOF:
          // When we're capturing a license block, annotations
          // in the block are ok.
            String multilineText = builder.toString();

            if (option != WhitespaceOption.PRESERVE) {
              multilineText = multilineText.trim();
            }

            int endLineno = stream.getLineno();
            int endCharno = stream.getCharno();

            if (multilineText.length() > 0) {
              jsdocBuilder.markText(multilineText, startLineno, startCharno,
                  endLineno, endCharno);
            }

            return new ExtractionInfo(multilineText, token);

          // FALL THROUGH

        default:
          ignoreStar = false;
          state = State.SEARCHING_ANNOTATION;

          if (builder.length() > 0) {
            builder.append(' ');
          }

          builder.append(toString(token));

          line = stream.getRemainingJSDocLine();

          if (option != WhitespaceOption.PRESERVE) {
            line = trimEnd(line);
          }

          builder.append(line);
          token = next();
      }
    } while (true);
  }

// relevant test
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

// com.google.javascript.jscomp.parsing.IRFactoryTest::testScript
  public void testScript() throws Exception {
    parse("");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testStrictScript
  public void testStrictScript() throws Exception {
    assertNull(newParse("").getDirectives());
    assertEquals(
        Sets.newHashSet("use strict"),
        newParse("'use strict'").getDirectives());
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testName
  public void testName() throws Exception {
    parse("a");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testArrayLiteral
  public void testArrayLiteral() throws Exception {
    parse("[a, b]");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testArrayLiteral2
  public void testArrayLiteral2() throws Exception {
    parse("[a, , b]");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testArrayLiteral3
  public void testArrayLiteral3() throws Exception {
    parse("[a, undefined, b]");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testArrayLiteral4
  public void testArrayLiteral4() throws Exception {
    parse("[,,,a,,b]");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testAssignment
  public void testAssignment() throws Exception {
    parse("a = b");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testAssignment2
  public void testAssignment2() throws Exception {
    parse("a += b");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testInfix
  public void testInfix() throws Exception {
    parse("a + b");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testScope
  public void testScope() throws Exception {
    parse("{ a; b; c; }");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testConditional
  public void testConditional() throws Exception {
    parse("a ? b : c");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testEmpty
  public void testEmpty() throws Exception {
    parse(";;");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testIf
  public void testIf() throws Exception {
    parse("if (a) { b }");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testIf2
  public void testIf2() throws Exception {
    parse("if (a) { b } else { c }");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testNumber
  public void testNumber() throws Exception {
    parse("0");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testNumber2
  public void testNumber2() throws Exception {
    parse("1.2");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testString
  public void testString() throws Exception {
    parse("'a'");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testString2
  public void testString2() throws Exception {
    parse("\"a\"");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testUnary
  public void testUnary() throws Exception {
    parse("-a");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testUnary2
  public void testUnary2() throws Exception {
    parse("a++");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testUnary3
  public void testUnary3() throws Exception {
    parse("++a");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testVar
  public void testVar() throws Exception {
    parse("var a = 1");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testVar2
  public void testVar2() throws Exception {
    parse("var a = 1, b = 2");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testVar3
  public void testVar3() throws Exception {
    parse("var a, b = 1");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testElementGet
  public void testElementGet() throws Exception {
    parse("a[i]");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testPropertyGet
  public void testPropertyGet() throws Exception {
    parse("a.b");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testRegexp
  public void testRegexp() throws Exception {
    parse("/ab+c/");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testRegexp2
  public void testRegexp2() throws Exception {
    parse("/ab+c/g");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testFunctionCall
  public void testFunctionCall() throws Exception {
    parse("a()");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testFunctionCall2
  public void testFunctionCall2() throws Exception {
    parse("a(b)");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testFunctionCall3
  public void testFunctionCall3() throws Exception {
    parse("a(b, c)");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testNew
  public void testNew() throws Exception {
    parse("new A()");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testNew2
  public void testNew2() throws Exception {
    parse("new A(b)");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testNew3
  public void testNew3() throws Exception {
    parse("new A(b, c)");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testTry
  public void testTry() {
    parse("try { a(); } catch (e) { b(); }");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testTry2
  public void testTry2() {
    parse("try { a(); } finally { b(); }");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testTry3
  public void testTry3() {
    parse("try { a(); } catch (e) { b(); } finally { c(); }");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testTry4
  public void testTry4() {
    parse("try { a(); }" +
        "catch (e if e == 'b') { b(); } " +
        "catch (e if e == 'c') { c(); }");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testTry5
  public void testTry5() {
    parse("try { a(); }" +
        "catch (e if e == 'b') { b(); } " +
        "catch (e if e == 'c') { c(); } " +
        "catch (e) { d(); } " +
        "finally { f(); }");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testFunction
  public void testFunction() {
    parse("function f() {}");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testFunction2
  public void testFunction2() {
    parse("function() {}");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testFunction3
  public void testFunction3() {
    parse("function f(a) {}");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testFunction4
  public void testFunction4() {
    parse("function(a) {}");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testFunction5
  public void testFunction5() {
    parse("function f(a, b) {}");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testFunction6
  public void testFunction6() {
    parse("function(a, b) {}");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testReturn
  public void testReturn() {
    parse("function() {return 1;}");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testReturn2
  public void testReturn2() {
    parse("function() {return;}");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testReturn3
  public void testReturn3() {
    parse("function(){return x?1:2}");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testThrow
  public void testThrow() {
    parse("throw e");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testWith
  public void testWith() {
    parse("with (a) { b }");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testObjectLiteral
  public void testObjectLiteral() {
    parse("var o = {}");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testObjectLiteral2
  public void testObjectLiteral2() {
    parse("var o = {a: 1}");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testObjectLiteral3
  public void testObjectLiteral3() {
    parse("var o = {a: 1, b: 2}");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testObjectLiteral4
  public void testObjectLiteral4() {
    parse("var o = {1: 'a'}");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testObjectLiteral5
  public void testObjectLiteral5() {
    parse("var o = {'a': 'a'}");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testKeywordLiteral
  public void testKeywordLiteral() {
    parse("true");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testWhile
  public void testWhile() {
    parse("while (!a) { a--; }");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testParen
  public void testParen() {
    parse("(a)");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testParen2
  public void testParen2() {
    parse("(1+1)*2");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testFor
  public void testFor() {
    parse("for (var i = 0; i < n; i++) { a(i); }");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testForIn
  public void testForIn() {
    parse("for (i in a) { b(i); }");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testBreak
  public void testBreak() {
    parse("while (true) { break; }");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testContinue
  public void testContinue() {
    parse("while (true) { continue; }");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testDoLoop
  public void testDoLoop() {
    parse("do { a() } while (b());");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testSwitch
  public void testSwitch() {
    parse("switch (e) {" +
        "case 'a': a(); break;" +
        "case 'b': b();" +
        "case 'c': c(); }");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testSwitch2
  public void testSwitch2() {
    parse("switch (e) { case 'a': a(); break; default: b();}");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testSwitch3
  public void testSwitch3() {
    parse("function(){switch(x){default:case 1:return 2}}");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testDebugger
  public void testDebugger() {
    parse("debugger;");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testCommentPositions
  public void testCommentPositions() {
    Node root = newParse("function a(x) {};" +
        "function b(x) {}");
    Node a = root.getFirstChild();
    Node b = root.getLastChild();
    assertMarkerPosition(a, 0, 4);
    assertMarkerPosition(b, 0, 45);
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testLiteralLocation
   public void testLiteralLocation() {
    Node root = newParse(
        "\nvar d =\n" +
        "    \"foo\";\n" +
        "var e =\n" +
        "    1;\n" +
        "var f = \n" +
        "    1.2;\n" +
        "var g = \n" +
        "    2e5;\n" +
        "var h = \n" +
        "    'bar';\n");

    Node firstStmt = root.getFirstChild();
    Node firstLiteral = firstStmt.getFirstChild().getFirstChild();
    Node secondStmt = firstStmt.getNext();
    Node secondLiteral = secondStmt.getFirstChild().getFirstChild();
    Node thirdStmt = secondStmt.getNext();
    Node thirdLiteral = thirdStmt.getFirstChild().getFirstChild();
    Node fourthStmt = thirdStmt.getNext();
    Node fourthLiteral = fourthStmt.getFirstChild().getFirstChild();
    Node fifthStmt = fourthStmt.getNext();
    Node fifthLiteral = fifthStmt.getFirstChild().getFirstChild();

    assertNodePosition(2, 4, firstLiteral);
    assertNodePosition(4, 4, secondLiteral);
    assertNodePosition(6, 4, thirdLiteral);
    assertNodePosition(8, 4, fourthLiteral);
    assertNodePosition(10, 4, fifthLiteral);
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testSwitchLocation
  public void testSwitchLocation() {
    Node root = newParse(
        "\nswitch (a) {\n" +
        "  
        "   case 1:\n" +
        "     b++;\n" +
        "   case 2:\n" +
        "   default:\n" +
        "     b--;\n" +
        "  }\n");

    Node switchStmt = root.getFirstChild();
    Node switchVar = switchStmt.getFirstChild();
    Node firstCase = switchVar.getNext();
    Node caseArg = firstCase.getFirstChild();
    Node caseBody = caseArg.getNext();
    Node caseExprStmt = caseBody.getFirstChild();
    Node incrExpr = caseExprStmt.getFirstChild();
    Node incrVar = incrExpr.getFirstChild();
    Node secondCase = firstCase.getNext();
    Node defaultCase = secondCase.getNext();

    assertNodePosition(1, 0, switchStmt);
    assertNodePosition(1, 8, switchVar);
    assertNodePosition(3, 3, firstCase);
    assertNodePosition(3, 8, caseArg);
    assertNodePosition(3, 3, caseBody);
    assertNodePosition(4, 5, caseExprStmt);
    assertNodePosition(4, 5, incrExpr);
    assertNodePosition(4, 5, incrVar);
    assertNodePosition(5, 3, secondCase);
    assertNodePosition(6, 3, defaultCase);
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testFunctionParamLocation
  public void testFunctionParamLocation() {
    Node root = newParse(
        "\nfunction\n" +
        "     foo(a,\n" +
        "     b,\n" +
        "     c)\n" +
        "{}\n");

    Node function = root.getFirstChild();
    Node functionName = function.getFirstChild();
    Node params = functionName.getNext();
    Node param1 = params.getFirstChild();
    Node param2 = param1.getNext();
    Node param3 = param2.getNext();
    Node body = params.getNext();

    assertNodePosition(2, 5, function);
    assertNodePosition(2, 5, functionName);
    
    
    
    assertNodePosition(2, 8, params);
    assertNodePosition(2, 9, param1);
    assertNodePosition(3, 5, param2);
    assertNodePosition(4, 5, param3);
    assertNodePosition(5, 0, body);
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testVarDeclLocation
  public void testVarDeclLocation() {
    Node root = newParse(
        "\nvar\n" +
        "    a =\n" +
        "    3\n");
    Node varDecl = root.getFirstChild();
    Node varName = varDecl.getFirstChild();
    Node varExpr = varName.getFirstChild();

    assertNodePosition(1, 0, varDecl);
    assertNodePosition(2, 4, varName);
    assertNodePosition(3, 4, varExpr);
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testReturnLocation
  public void testReturnLocation() {
    Node root = newParse(
        "\nfunction\n" +
        "    foo(\n" +
        "    a,\n" +
        "    b,\n" +
        "    c) {\n" +
        "    return\n" +
        "    4;\n" +
        "}\n");

    Node function = root.getFirstChild();
    Node functionName = function.getFirstChild();
    Node params = functionName.getNext();
    Node body = params.getNext();
    Node returnStmt = body.getFirstChild();
    Node exprStmt = returnStmt.getNext();
    Node returnVal = exprStmt.getFirstChild();

    assertNodePosition(6, 4, returnStmt);
    assertNodePosition(7, 4, exprStmt);
    assertNodePosition(7, 4, returnVal);
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testLinenoFor
  public void testLinenoFor() {
    Node root = newParse(
        "\nfor(\n" +
        ";\n" +
        ";\n" +
        ") {\n" +
        "}\n");

    Node forNode = root.getFirstChild();
    Node initClause= forNode.getFirstChild();
    Node condClause = initClause.getNext();
    Node incrClause = condClause.getNext();

    assertNodePosition(1, 0, forNode);
    assertNodePosition(2, 0, initClause);
    assertNodePosition(3, 0, condClause);
    
    
    
    
    
    
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testBinaryExprLocation
  public void testBinaryExprLocation() {
    Node root = newParse(
        "\nvar d = a\n" +
        "    + \n" +
        "    b;\n" +
        "var\n" +
        "    e =\n" +
        "    a +\n" +
        "    c;\n" +
        "var f = b\n" +
        "    / c;\n");

    Node firstVarDecl = root.getFirstChild();
    Node firstVar = firstVarDecl.getFirstChild();
    Node firstVarAdd = firstVar.getFirstChild();

    Node secondVarDecl = firstVarDecl.getNext();
    Node secondVar = secondVarDecl.getFirstChild();
    Node secondVarAdd = secondVar.getFirstChild();

    Node thirdVarDecl = secondVarDecl.getNext();
    Node thirdVar = thirdVarDecl.getFirstChild();
    Node thirdVarAdd = thirdVar.getFirstChild();

    assertNodePosition(1, 0, firstVarDecl);
    assertNodePosition(1, 4, firstVar);
    assertNodePosition(2, 4, firstVarAdd);
    assertNodePosition(1, 8, firstVarAdd.getFirstChild());
    assertNodePosition(3, 4, firstVarAdd.getLastChild());

    assertNodePosition(4, 0, secondVarDecl);
    assertNodePosition(5, 4, secondVar);
    assertNodePosition(6, 6, secondVarAdd);
    assertNodePosition(6, 4, secondVarAdd.getFirstChild());
    assertNodePosition(7, 4, secondVarAdd.getLastChild());

    assertNodePosition(8, 0, thirdVarDecl);
    assertNodePosition(8, 4, thirdVar);
    assertNodePosition(9, 4, thirdVarAdd);
    assertNodePosition(8, 8, thirdVarAdd.getFirstChild());
    assertNodePosition(9, 6, thirdVarAdd.getLastChild());
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testPrefixLocation
  public void testPrefixLocation() {
    Node root = newParse(
         "\na++;\n" +
         "--\n" +
         "b;\n");

    Node firstStmt = root.getFirstChild();
    Node secondStmt = firstStmt.getNext();
    Node firstOp = firstStmt.getFirstChild();
    Node secondOp = secondStmt.getFirstChild();

    assertNodePosition(1, 0, firstOp);
    assertNodePosition(2, 0, secondOp);
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testIfLocation
  public void testIfLocation() {
    Node root = newParse(
        "\nif\n" +
        "  (a == 3)\n" +
        "{\n" +
        "  b = 0;\n" +
        "}\n" +
        "  else\n" +
        "{\n" +
        "  c = 1;\n" +
        "}\n");

    Node ifStmt = root.getFirstChild();
    Node eqClause = ifStmt.getFirstChild();
    Node thenClause = eqClause.getNext();
    Node elseClause = thenClause.getNext();

    assertNodePosition(1, 0, ifStmt);
    assertNodePosition(2, 5, eqClause);
    assertNodePosition(3, 0, thenClause);
    assertNodePosition(7, 0, elseClause);
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testTryLocation
  public void testTryLocation() {
     Node root = newParse(
         "\ntry {\n" +
         "  var x = 1;\n" +
         "} catch\n" +
         "   (err)\n" +
         "{\n" +
         "} finally {\n" +
         "  var y = 2;\n" +
         "}\n");

    Node tryStmt = root.getFirstChild();
    Node tryBlock = tryStmt.getFirstChild();
    Node catchBlock = tryBlock.getNext();
    Node catchVarBlock = catchBlock.getFirstChild();
    Node catchVar = catchVarBlock.getFirstChild();
    Node finallyBlock = catchBlock.getNext();
    Node finallyStmt = finallyBlock.getFirstChild();

    assertNodePosition(1, 0, tryStmt);
    assertNodePosition(1, 4, tryBlock);
    assertNodePosition(3, 2, catchVarBlock);
    assertNodePosition(4, 4, catchVar);
    assertNodePosition(3, 0, catchBlock);
    assertNodePosition(6, 10, finallyBlock);
    assertNodePosition(7, 2, finallyStmt);
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testHookLocation
  public void testHookLocation() {
    Node root = newParse(
        "\na\n" +
        "?\n" +
        "b\n" +
        ":\n" +
        "c\n" +
        ";\n");

    Node hookExpr = root.getFirstChild().getFirstChild();
    Node condExpr = hookExpr.getFirstChild();
    Node thenExpr = condExpr.getNext();
    Node elseExpr = thenExpr.getNext();

    assertNodePosition(2, 0, hookExpr);
    assertNodePosition(1, 0, condExpr);
    assertNodePosition(3, 0, thenExpr);
    assertNodePosition(5, 0, elseExpr);
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testLabelLocation
  public void testLabelLocation() {
    Node root = newParse(
        "\nfoo:\n" +
        "a = 1;\n" +
        "bar:\n" +
        "b = 2;\n");

    Node firstStmt = root.getFirstChild();
    Node secondStmt = firstStmt.getNext();

    assertNodePosition(1, 0, firstStmt);
    assertNodePosition(3, 0, secondStmt);
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testCompareLocation
  public void testCompareLocation() {
    Node root = newParse(
        "\na\n" +
        "<\n" +
        "b\n");

    Node condClause = root.getFirstChild().getFirstChild();
    Node lhs = condClause.getFirstChild();
    Node rhs = lhs.getNext();

    assertNodePosition(2, 0, condClause);
    assertNodePosition(1, 0, lhs);
    assertNodePosition(3, 0, rhs);
   }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testEqualityLocation
  public void testEqualityLocation() {
    Node root = newParse(
        "\na\n" +
        "==\n" +
        "b\n");

    Node condClause = root.getFirstChild().getFirstChild();
    Node lhs = condClause.getFirstChild();
    Node rhs = lhs.getNext();

    assertNodePosition(2, 0, condClause);
    assertNodePosition(1, 0, lhs);
    assertNodePosition(3, 0, rhs);
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testPlusEqLocation
  public void testPlusEqLocation() {
    Node root = newParse(
        "\na\n" +
        "+=\n" +
        "b\n");

    Node condClause = root.getFirstChild().getFirstChild();
    Node lhs = condClause.getFirstChild();
    Node rhs = lhs.getNext();

    assertNodePosition(2, 0, condClause);
    assertNodePosition(1, 0, lhs);
    assertNodePosition(3, 0, rhs);
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testCommaLocation
  public void testCommaLocation() {
    Node root = newParse(
        "\na,\n" +
        "b,\n" +
        "c;\n");

    Node statement = root.getFirstChild();
    Node comma1 = statement.getFirstChild();
    Node comma2 = comma1.getFirstChild();
    Node cRef = comma2.getNext();
    Node aRef = comma2.getFirstChild();
    Node bRef = aRef.getNext();

    assertNodePosition(1, 1, comma2);
    assertNodePosition(1, 0, aRef);
    assertNodePosition(2, 0, bRef);
    assertNodePosition(3, 0, cRef);
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testRegexpLocation
  public void testRegexpLocation() {
    Node root = newParse(
        "\nvar path =\n" +
        "replace(\n" +
        "/a/g," +
        "'/');\n");

    Node firstVarDecl = root.getFirstChild();
    Node firstVar = firstVarDecl.getFirstChild();
    Node firstInitializer = firstVar.getFirstChild();
    Node callNode = firstVar.getFirstChild();
    Node fnName = callNode.getFirstChild();
    Node regexObject = fnName.getNext();
    Node aString = regexObject.getFirstChild();
    Node endRegexString = regexObject.getNext();

    assertNodePosition(1, 0, firstVarDecl);
    assertNodePosition(1, 4, firstVar);
    assertNodePosition(2, 7, callNode);
    assertNodePosition(2, 0, fnName);
    assertNodePosition(3, 0, regexObject);
    assertNodePosition(3, 0, aString);
    assertNodePosition(3, 5, endRegexString);
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testNestedOr
  public void testNestedOr() {
    Node root = newParse(
        "\nif (a && \n" +
        "    b() || \n" +
        "    \n" +
        "    c) {\n" +
        "}\n"
    );

    Node ifStmt = root.getFirstChild();
    Node orClause = ifStmt.getFirstChild();
    Node andClause = orClause.getFirstChild();
    Node cName = andClause.getNext();

    assertNodePosition(1, 0, ifStmt);
    assertNodePosition(2, 8, orClause);
    assertNodePosition(1, 6, andClause);
    assertNodePosition(4, 4, cName);

  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testBitwiseOps
  public void testBitwiseOps() {
      Node root = newParse(
        "\nif (a & \n" +
        "    b() | \n" +
        "    \n" +
        "    c) {\n" +
        "}\n"
    );

    Node ifStmt = root.getFirstChild();
    Node bitOr = ifStmt.getFirstChild();
    Node bitAnd = bitOr.getFirstChild();
    Node cName = bitAnd.getNext();

    assertNodePosition(1, 0, ifStmt);
    assertNodePosition(2, 8, bitOr);
    assertNodePosition(1, 6, bitAnd);
    assertNodePosition(4, 4, cName);

  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testObjectLitLocation
  public void testObjectLitLocation() {
    Node root = newParse(
        "\nvar foo =\n" +
        "{ \n" +
        "'A' : 'A', \n" +
        "'B' : 'B', \n" +
        "'C' :\n" +
        "    'C' \n" +
        "};\n");

    Node firstVarDecl = root.getFirstChild();
    Node firstVar = firstVarDecl.getFirstChild();
    Node firstObjectLit = firstVar.getFirstChild();
    Node firstKey = firstObjectLit.getFirstChild();
    Node firstValue = firstKey.getNext();

    Node secondKey = firstValue.getNext();
    Node secondValue = secondKey.getNext();

    Node thirdKey = secondValue.getNext();
    Node thirdValue = thirdKey.getNext();

    assertNodePosition(1, 4, firstVar);
    assertNodePosition(2, 0, firstObjectLit);

    assertNodePosition(3, 0, firstKey);
    assertNodePosition(3, 6, firstValue);

    assertNodePosition(4, 0, secondKey);
    assertNodePosition(4, 6, secondValue);

    assertNodePosition(5, 0, thirdKey);
    assertNodePosition(6, 4, thirdValue);
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testTryWithoutCatchLocation
  public void testTryWithoutCatchLocation() {
     Node root = newParse(
         "\ntry {\n" +
         "  var x = 1;\n" +
         "} finally {\n" +
         "  var y = 2;\n" +
         "}\n");

    Node tryStmt = root.getFirstChild();
    Node tryBlock = tryStmt.getFirstChild();
    Node catchBlock = tryBlock.getNext();
    Node finallyBlock = catchBlock.getNext();
    Node finallyStmt = finallyBlock.getFirstChild();

    assertNodePosition(1, 0, tryStmt);
    assertNodePosition(1, 4, tryBlock);
    assertNodePosition(3, 0, catchBlock);
    assertNodePosition(3, 10, finallyBlock);
    assertNodePosition(4, 2, finallyStmt);
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testTryWithoutFinallyLocation
  public void testTryWithoutFinallyLocation() {
     Node root = newParse(
         "\ntry {\n" +
         "  var x = 1;\n" +
         "} catch (ex) {\n" +
         "  var y = 2;\n" +
         "}\n");

    Node tryStmt = root.getFirstChild();
    Node tryBlock = tryStmt.getFirstChild();
    Node catchBlock = tryBlock.getNext();
    Node catchStmt = catchBlock.getFirstChild();
    Node exceptionVar = catchStmt.getFirstChild();
    Node catchCondition = exceptionVar.getNext();
    Node exceptionBlock = catchCondition.getNext();
    Node varDecl = exceptionBlock.getFirstChild();

    assertNodePosition(1, 0, tryStmt);
    assertNodePosition(1, 4, tryBlock);
    assertNodePosition(3, 0, catchBlock);
    assertNodePosition(3, 2, catchStmt);
    assertNodePosition(3, 9, exceptionVar);
    assertNodePosition(3, 9, catchCondition);
    assertNodePosition(3, 13, exceptionBlock);
    assertNodePosition(4, 2, varDecl);
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testMultilineEqLocation
  public void testMultilineEqLocation() {
    Node  root = newParse(
        "\nif\n" +
        "    (((a == \n" +
        "  3) && \n" +
        "  (b == 2)) || \n" +
        " (c == 1)) {\n" +
        "}\n");
    Node ifStmt = root.getFirstChild();
    Node orTest = ifStmt.getFirstChild();
    Node andTest = orTest.getFirstChild();
    Node cTest = andTest.getNext();
    Node aTest = andTest.getFirstChild();
    Node bTest = aTest.getNext();

    assertNodePosition(1, 0, ifStmt);
    assertNodePosition(4, 12, orTest);
    assertNodePosition(3, 5, andTest);
    assertNodePosition(2, 9, aTest);
    assertNodePosition(4, 5, bTest);
    assertNodePosition(5, 4, cTest);
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testMultilineBitTestLocation
  public void testMultilineBitTestLocation() {
    Node root = newParse(
        "\nif (\n" +
        "      ((a \n" +
        "        | 3 \n" +
        "       ) == \n" +
        "       (b \n" +
        "        & 2)) && \n" +
        "      ((a \n" +
        "         ^ 0xffff) \n" +
        "       != \n" +
        "       (c \n" +
        "        << 1))) {\n" +
        "}\n");

    Node ifStmt = root.getFirstChild();
    Node andTest = ifStmt.getFirstChild();
    Node eqTest = andTest.getFirstChild();
    Node notEqTest = eqTest.getNext();

    Node bitOrTest = eqTest.getFirstChild();
    Node bitAndTest = bitOrTest.getNext();

    Node bitXorTest = notEqTest.getFirstChild();
    Node bitShiftTest = bitXorTest.getNext();

    assertNodePosition(1, 0, ifStmt);

    assertNodePosition(4, 9, eqTest);
    assertNodePosition(9, 7, notEqTest);

    assertNodePosition(3, 8, bitOrTest);
    assertNodePosition(6, 8, bitAndTest);
    assertNodePosition(8, 9, bitXorTest);
    assertNodePosition(11, 8, bitShiftTest);
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testCallLocation
  public void testCallLocation() {
    Node root = newParse(
        "\na.\n" +
        "b.\n" +
        "cccc(1);\n");

    Node exprStmt = root.getFirstChild();
    Node functionCall = exprStmt.getFirstChild();
    Node functionProp = functionCall.getFirstChild();
    Node firstNameComponent = functionProp.getFirstChild();
    Node lastNameComponent = firstNameComponent.getNext();

    assertNodePosition(3, 4, functionCall);
    
    
    
    
    assertNodePosition(3, 0, lastNameComponent);
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testLinenoDeclaration
  public void testLinenoDeclaration() {
    Node root = newParse(
        "\na.\n" +
        "b=\n" +
        "function() {};\n");

    Node exprStmt = root.getFirstChild();
    Node fnAssignment =  exprStmt.getFirstChild();
    Node aDotbName = fnAssignment.getFirstChild();
    Node aName = aDotbName.getFirstChild();
    Node bName = aName.getNext();
    Node fnNode = aDotbName.getNext();
    Node fnName = fnNode.getFirstChild();

    assertNodePosition(2, 1, fnAssignment);
    
    
    assertNodePosition(1, 0, aName);
    assertNodePosition(2, 0, bName);
    assertNodePosition(3, 8, fnNode);
    assertNodePosition(3, 8, fnName);
   }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseTypeViaStatic1
  public void testParseTypeViaStatic1() throws Exception {
    Node typeNode = parseType("null");
    assertTypeEquals(NULL_TYPE, typeNode);
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseTypeViaStatic2
  public void testParseTypeViaStatic2() throws Exception {
    Node typeNode = parseType("string");
    assertTypeEquals(STRING_TYPE, typeNode);
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseTypeViaStatic3
  public void testParseTypeViaStatic3() throws Exception {
    Node typeNode = parseType("!Date");
    assertTypeEquals(DATE_TYPE, typeNode);
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseTypeViaStatic4
  public void testParseTypeViaStatic4() throws Exception {
    Node typeNode = parseType("boolean|string");
    assertTypeEquals(createUnionType(BOOLEAN_TYPE, STRING_TYPE), typeNode);
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseInvalidTypeViaStatic
  public void testParseInvalidTypeViaStatic() throws Exception {
    Node typeNode = parseType("sometype.<anothertype");
    assertNull(typeNode);
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseInvalidTypeViaStatic2
  public void testParseInvalidTypeViaStatic2() throws Exception {
    Node typeNode = parseType("");
    assertNull(typeNode);
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseNamedType1
  public void testParseNamedType1() throws Exception {
    assertNull(parse("@type null", "Unexpected end of file"));
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseNamedType2
  public void testParseNamedType2() throws Exception {
    JSDocInfo info = parse("@type null*/");
    assertTypeEquals(NULL_TYPE, info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseNamedType3
  public void testParseNamedType3() throws Exception {
    JSDocInfo info = parse("@type {string}*/");
    assertTypeEquals(STRING_TYPE, info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseNamedType4
  public void testParseNamedType4() throws Exception {
    
    JSDocInfo info = parse("@type \n {string}*/");
    assertTypeEquals(STRING_TYPE, info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testTypedefType1
  public void testTypedefType1() throws Exception {
    JSDocInfo info = parse("@typedef string */");
    assertTrue(info.hasTypedefType());
    assertTypeEquals(STRING_TYPE, info.getTypedefType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testTypedefType2
  public void testTypedefType2() throws Exception {
    JSDocInfo info = parse("@typedef \n {string}*/");
    assertTrue(info.hasTypedefType());
    assertTypeEquals(STRING_TYPE, info.getTypedefType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testTypedefType3
  public void testTypedefType3() throws Exception {
    JSDocInfo info = parse("@typedef \n {(string|number)}*/");
    assertTrue(info.hasTypedefType());
    assertTypeEquals(
        createUnionType(NUMBER_TYPE, STRING_TYPE),
        info.getTypedefType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseStringType1
  public void testParseStringType1() throws Exception {
    assertTypeEquals(STRING_TYPE, parse("@type {string}*/").getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseStringType2
  public void testParseStringType2() throws Exception {
    assertTypeEquals(STRING_OBJECT_TYPE, parse("@type {!String}*/").getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseBooleanType1
  public void testParseBooleanType1() throws Exception {
    assertTypeEquals(BOOLEAN_TYPE, parse("@type {boolean}*/").getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseBooleanType2
  public void testParseBooleanType2() throws Exception {
    assertTypeEquals(BOOLEAN_OBJECT_TYPE, parse("@type {!Boolean}*/").getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseNumberType1
  public void testParseNumberType1() throws Exception {
    assertTypeEquals(NUMBER_TYPE, parse("@type {number}*/").getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseNumberType2
  public void testParseNumberType2() throws Exception {
    assertTypeEquals(NUMBER_OBJECT_TYPE, parse("@type {!Number}*/").getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseNullType1
  public void testParseNullType1() throws Exception {
    assertTypeEquals(NULL_TYPE, parse("@type {null}*/").getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseNullType2
  public void testParseNullType2() throws Exception {
    assertTypeEquals(NULL_TYPE, parse("@type {Null}*/").getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseAllType1
  public void testParseAllType1() throws Exception {
    testParseType("*");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseAllType2
  public void testParseAllType2() throws Exception {
    testParseType("*?", "*");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseObjectType
  public void testParseObjectType() throws Exception {
    assertTypeEquals(OBJECT_TYPE, parse("@type {!Object}*/").getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseDateType
  public void testParseDateType() throws Exception {
    assertTypeEquals(DATE_TYPE, parse("@type {!Date}*/").getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseFunctionType
  public void testParseFunctionType() throws Exception {
    assertTypeEquals(
        createNullableType(U2U_CONSTRUCTOR_TYPE),
        parse("@type {Function}*/").getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseRegExpType
  public void testParseRegExpType() throws Exception {
    assertTypeEquals(REGEXP_TYPE, parse("@type {!RegExp}*/").getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseErrorTypes
  public void testParseErrorTypes() throws Exception {
    assertTypeEquals(ERROR_TYPE, parse("@type {!Error}*/").getType());
    assertTypeEquals(URI_ERROR_TYPE, parse("@type {!URIError}*/").getType());
    assertTypeEquals(EVAL_ERROR_TYPE, parse("@type {!EvalError}*/").getType());
    assertTypeEquals(REFERENCE_ERROR_TYPE,
        parse("@type {!ReferenceError}*/").getType());
    assertTypeEquals(TYPE_ERROR_TYPE, parse("@type {!TypeError}*/").getType());
    assertTypeEquals(RANGE_ERROR_TYPE, parse("@type {!RangeError}*/").getType());
    assertTypeEquals(SYNTAX_ERROR_TYPE, parse("@type {!SyntaxError}*/").getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseUndefinedType1
  public void testParseUndefinedType1() throws Exception {
    assertTypeEquals(VOID_TYPE, parse("@type {undefined}*/").getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseUndefinedType2
  public void testParseUndefinedType2() throws Exception {
    assertTypeEquals(VOID_TYPE, parse("@type {Undefined}*/").getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseUndefinedType3
  public void testParseUndefinedType3() throws Exception {
    assertTypeEquals(VOID_TYPE, parse("@type {void}*/").getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseParametrizedType1
  public void testParseParametrizedType1() throws Exception {
    JSDocInfo info = parse("@type !Array.<number> */");
    assertTypeEquals(ARRAY_TYPE, info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseParametrizedType2
  public void testParseParametrizedType2() throws Exception {
    JSDocInfo info = parse("@type {!Array.<number>}*/");
    assertTypeEquals(ARRAY_TYPE, info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseParametrizedType3
  public void testParseParametrizedType3() throws Exception {
    JSDocInfo info = parse("@type !Array.<(number,null)>*/");
    assertTypeEquals(ARRAY_TYPE, info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseParametrizedType4
  public void testParseParametrizedType4() throws Exception {
    JSDocInfo info = parse("@type {!Array.<(number|null)>}*/");
    assertTypeEquals(ARRAY_TYPE, info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseParametrizedType5
  public void testParseParametrizedType5() throws Exception {
    JSDocInfo info = parse("@type {!Array.<Array.<(number|null)>>}*/");
    assertTypeEquals(ARRAY_TYPE, info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseParametrizedType6
  public void testParseParametrizedType6() throws Exception {
    JSDocInfo info = parse("@type {!Array.<!Array.<(number|null)>>}*/");
    assertTypeEquals(ARRAY_TYPE, info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseParametrizedType7
  public void testParseParametrizedType7() throws Exception {
    JSDocInfo info = parse("@type {!Array.<function():Date>}*/");
    assertTypeEquals(ARRAY_TYPE, info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseParametrizedType8
  public void testParseParametrizedType8() throws Exception {
    JSDocInfo info = parse("@type {!Array.<function():!Date>}*/");
    assertTypeEquals(ARRAY_TYPE, info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseParametrizedType9
  public void testParseParametrizedType9() throws Exception {
    JSDocInfo info = parse("@type {!Array.<Date|number>}*/");
    assertTypeEquals(ARRAY_TYPE, info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseParametrizedType10
  public void testParseParametrizedType10() throws Exception {
    JSDocInfo info = parse("@type {!Array.<Date|number|boolean>}*/");
    assertTypeEquals(ARRAY_TYPE, info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseParamterizedType11
  public void testParseParamterizedType11() throws Exception {
    JSDocInfo info = parse("@type {!Object.<number>}*/");
    assertTypeEquals(OBJECT_TYPE, info.getType());
    assertParameterTypeEquals(NUMBER_TYPE, info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseParamterizedType12
  public void testParseParamterizedType12() throws Exception {
    JSDocInfo info = parse("@type {!Object.<string,number>}*/");
    assertTypeEquals(OBJECT_TYPE, info.getType());
    assertParameterTypeEquals(NUMBER_TYPE, info.getType());
    assertIndexTypeEquals(STRING_TYPE, info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseUnionType1
  public void testParseUnionType1() throws Exception {
    JSDocInfo info = parse("@type {(boolean,null)}*/");
    assertTypeEquals(createUnionType(BOOLEAN_TYPE, NULL_TYPE), info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseUnionType2
  public void testParseUnionType2() throws Exception {
    JSDocInfo info = parse("@type {boolean|null}*/");
    assertTypeEquals(createUnionType(BOOLEAN_TYPE, NULL_TYPE), info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseUnionType3
  public void testParseUnionType3() throws Exception {
    JSDocInfo info = parse("@type {boolean||null}*/");
    assertTypeEquals(createUnionType(BOOLEAN_TYPE, NULL_TYPE), info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseUnionType4
  public void testParseUnionType4() throws Exception {
    JSDocInfo info = parse("@type {(Array.<boolean>,null)}*/");
    assertTypeEquals(createUnionType(ARRAY_TYPE, NULL_TYPE), info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseUnionType5
  public void testParseUnionType5() throws Exception {
    JSDocInfo info = parse("@type {(null, Array.<boolean>)}*/");
    assertTypeEquals(createUnionType(ARRAY_TYPE, NULL_TYPE), info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseUnionType6
  public void testParseUnionType6() throws Exception {
    JSDocInfo info = parse("@type {Array.<boolean>|null}*/");
    assertTypeEquals(createUnionType(ARRAY_TYPE, NULL_TYPE), info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseUnionType7
  public void testParseUnionType7() throws Exception {
    JSDocInfo info = parse("@type {null|Array.<boolean>}*/");
    assertTypeEquals(createUnionType(ARRAY_TYPE, NULL_TYPE), info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseUnionType8
  public void testParseUnionType8() throws Exception {
    JSDocInfo info = parse("@type {null||Array.<boolean>}*/");
    assertTypeEquals(createUnionType(ARRAY_TYPE, NULL_TYPE), info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseUnionType9
  public void testParseUnionType9() throws Exception {
    JSDocInfo info = parse("@type {Array.<boolean>||null}*/");
    assertTypeEquals(createUnionType(ARRAY_TYPE, NULL_TYPE), info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseUnionType10
  public void testParseUnionType10() throws Exception {
    parse("@type {string|}*/", "type not recognized due to syntax error");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseUnionType11
  public void testParseUnionType11() throws Exception {
    parse("@type {(string,)}*/", "type not recognized due to syntax error");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseUnionType12
  public void testParseUnionType12() throws Exception {
    parse("@type {()}*/", "type not recognized due to syntax error");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseUnionType13
  public void testParseUnionType13() throws Exception {
    testParseType(
        "(function(this:Date),function(this:String):number)",
        "(function (this:Date): ?|function (this:String): number)");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseUnionType14
  public void testParseUnionType14() throws Exception {
    testParseType(
        "(function(...[function(number):boolean]):number)|" +
        "function(this:String, string):number",
        "(function (...[function (number): boolean]): number|" +
        "function (this:String, string): number)");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseUnionType15
  public void testParseUnionType15() throws Exception {
    testParseType("*|number", "*");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseUnionType16
  public void testParseUnionType16() throws Exception {
    testParseType("number|*", "*");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseUnionType17
  public void testParseUnionType17() throws Exception {
    testParseType("string|number|*", "*");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseUnionType18
  public void testParseUnionType18() throws Exception {
    testParseType("(string,*,number)", "*");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseUnionTypeError1
  public void testParseUnionTypeError1() throws Exception {
    parse("@type {(string,|number)} */",
        "type not recognized due to syntax error");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseFunctionalType1
  public void testParseFunctionalType1() throws Exception {
    testParseType("function (): number");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseFunctionalType2
  public void testParseFunctionalType2() throws Exception {
    testParseType("function (number, string): boolean");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseFunctionalType3
  public void testParseFunctionalType3() throws Exception {
    testParseType(
        "function(this:Array)", "function (this:Array): ?");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseFunctionalType4
  public void testParseFunctionalType4() throws Exception {
    testParseType("function (...[number]): boolean");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseFunctionalType5
  public void testParseFunctionalType5() throws Exception {
    testParseType("function (number, ...[string]): boolean");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseFunctionalType6
  public void testParseFunctionalType6() throws Exception {
    testParseType(
        "function (this:Date, number): (boolean|number|string)");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseFunctionalType7
  public void testParseFunctionalType7() throws Exception {
    testParseType("function()", "function (): ?");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseFunctionalType8
  public void testParseFunctionalType8() throws Exception {
    testParseType(
        "function(this:Array,...[boolean])",
        "function (this:Array, ...[boolean]): ?");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseFunctionalType9
  public void testParseFunctionalType9() throws Exception {
    testParseType(
        "function(this:Array,!Date,...[boolean?])",
        "function (this:Array, Date, ...[(boolean|null)]): ?");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseFunctionalType10
  public void testParseFunctionalType10() throws Exception {
    testParseType(
        "function(...[Object?]):boolean?",
        "function (...[(Object|null)]): (boolean|null)");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseFunctionalType11
  public void testParseFunctionalType11() throws Exception {
    testParseType(
        "function(...[[number]]):[number?]",
        "function (...[Array]): Array");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseFunctionalType12
  public void testParseFunctionalType12() throws Exception {
    testParseType(
        "function(...)",
        "function (...[?]): ?");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseFunctionalType13
  public void testParseFunctionalType13() throws Exception {
    testParseType(
        "function(...): void",
        "function (...[?]): undefined");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseFunctionalType14
  public void testParseFunctionalType14() throws Exception {
    testParseType("function (*, string, number): boolean");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testBug1419535
  public void testBug1419535() throws Exception {
    parse("@type {function(Object, string, *)?} */");
    parse("@type {function(Object, string, *)|null} */");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseFunctionalTypeError1
  public void testParseFunctionalTypeError1() throws Exception {
    parse("@type {function number):string}*/", "missing opening (");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseFunctionalTypeError2
  public void testParseFunctionalTypeError2() throws Exception {
    parse("@type {function( number}*/", "missing closing )");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseFunctionalTypeError3
  public void testParseFunctionalTypeError3() throws Exception {
    parse("@type {function(...[number], string)}*/",
        "variable length argument must be last");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseFunctionalTypeError4
  public void testParseFunctionalTypeError4() throws Exception {
    parse("@type {function(string, ...[number], boolean):string}*/",
        "variable length argument must be last");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseFunctionalTypeError5
  public void testParseFunctionalTypeError5() throws Exception {
    parse("@type {function (thi:Array)}*/", "missing closing )");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseFunctionalTypeError6
  public void testParseFunctionalTypeError6() throws Exception {
    resolve(parse("@type {function (this:number)}*/").getType(),
        "this type must be an object type");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseFunctionalTypeError7
  public void testParseFunctionalTypeError7() throws Exception {
    parse("@type {function(...[number)}*/", "missing closing ]");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseFunctionalTypeError8
  public void testParseFunctionalTypeError8() throws Exception {
    parse("@type {function(...number])}*/", "missing opening [");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseArrayType1
  public void testParseArrayType1() throws Exception {
    testParseType("[number]", "Array");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseArrayType2
  public void testParseArrayType2() throws Exception {
    testParseType("[(number,boolean,[Object?])]", "Array");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseArrayType3
  public void testParseArrayType3() throws Exception {
    testParseType("[[number],[string]]?", "(Array|null)");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseArrayTypeError1
  public void testParseArrayTypeError1() throws Exception {
    parse("@type {[number}*/", "missing closing ]");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseArrayTypeError2
  public void testParseArrayTypeError2() throws Exception {
    parse("@type {number]}*/", "expected closing }");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseArrayTypeError3
  public void testParseArrayTypeError3() throws Exception {
    parse("@type {[(number,boolean,Object?])]}*/", "missing closing )");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseArrayTypeError4
  public void testParseArrayTypeError4() throws Exception {
    parse("@type {(number,boolean,[Object?)]}*/",
        "missing closing ]");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseNullableModifiers1
  public void testParseNullableModifiers1() throws Exception {
    JSDocInfo info = parse("@type {string?}*/");
    assertTypeEquals(createNullableType(STRING_TYPE), info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseNullableModifiers2
  public void testParseNullableModifiers2() throws Exception {
    JSDocInfo info = parse("@type {!Array.<string?>}*/");
    assertTypeEquals(ARRAY_TYPE, info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseNullableModifiers3
  public void testParseNullableModifiers3() throws Exception {
    JSDocInfo info = parse("@type {Array.<boolean>?}*/");
    assertTypeEquals(createNullableType(ARRAY_TYPE), info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseNullableModifiers4
  public void testParseNullableModifiers4() throws Exception {
    JSDocInfo info = parse("@type {(string,boolean)?}*/");
    assertTypeEquals(
        createNullableType(createUnionType(STRING_TYPE, BOOLEAN_TYPE)),
        info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseNullableModifiers5
  public void testParseNullableModifiers5() throws Exception {
    JSDocInfo info = parse("@type {(string?,boolean)}*/");
    assertTypeEquals(
        createUnionType(createNullableType(STRING_TYPE), BOOLEAN_TYPE),
        info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseNullableModifiers6
  public void testParseNullableModifiers6() throws Exception {
    JSDocInfo info = parse("@type {(string,boolean?)}*/");
    assertTypeEquals(
        createUnionType(STRING_TYPE, createNullableType(BOOLEAN_TYPE)),
        info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseNullableModifiers7
  public void testParseNullableModifiers7() throws Exception {
    JSDocInfo info = parse("@type {string?|boolean}*/");
    assertTypeEquals(
        createUnionType(createNullableType(STRING_TYPE), BOOLEAN_TYPE),
        info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseNullableModifiers8
  public void testParseNullableModifiers8() throws Exception {
    JSDocInfo info = parse("@type {string|boolean?}*/");
    assertTypeEquals(
        createUnionType(STRING_TYPE, createNullableType(BOOLEAN_TYPE)),
        info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseNullableModifiers9
  public void testParseNullableModifiers9() throws Exception {
    JSDocInfo info = parse("@type {foo.Hello.World?}*/");
    assertTypeEquals(createNullableType(new NamedType(
        registry, "foo.Hello.World", null, -1, -1)),
        info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseOptionalModifier
  public void testParseOptionalModifier() throws Exception {
    JSDocInfo info = parse("@type {function(number=)}*/");
    assertTypeEquals(
        registry.createFunctionType(
            UNKNOWN_TYPE, createUnionType(VOID_TYPE, NUMBER_TYPE)),
        info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseNewline1
  public void testParseNewline1() throws Exception {
    JSDocInfo info = parse("@type {string\n* }\n*/");
    assertTypeEquals(STRING_TYPE, info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseNewline2
  public void testParseNewline2() throws Exception {
    JSDocInfo info = parse("@type !Array.<\n* number\n* > */");
    assertTypeEquals(ARRAY_TYPE, info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseNewline3
  public void testParseNewline3() throws Exception {
    JSDocInfo info = parse("@type !Array.<(number,\n* null)>*/");
    assertTypeEquals(ARRAY_TYPE, info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseNewline4
  public void testParseNewline4() throws Exception {
    JSDocInfo info = parse("@type !Array.<(number|\n* null)>*/");
    assertTypeEquals(ARRAY_TYPE, info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseNewline5
  public void testParseNewline5() throws Exception {
    JSDocInfo info = parse("@type !Array.<function(\n* )\n* :\n* Date>*/");
    assertTypeEquals(ARRAY_TYPE, info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseReturnType1
  public void testParseReturnType1() throws Exception {
    JSDocInfo info =
        parse("@return {null|string|Array.<boolean>}*/");
    assertTypeEquals(
        createUnionType(ARRAY_TYPE, NULL_TYPE, STRING_TYPE),
        info.getReturnType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseReturnType2
  public void testParseReturnType2() throws Exception {
    JSDocInfo info =
        parse("@returns {null|(string,Array.<boolean>)}*/");
    assertTypeEquals(
        createUnionType(ARRAY_TYPE, NULL_TYPE, STRING_TYPE),
        info.getReturnType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseReturnType3
  public void testParseReturnType3() throws Exception {
    JSDocInfo info =
        parse("@return {((null||Array.<boolean>,string),boolean)}*/");
    assertTypeEquals(
        createUnionType(ARRAY_TYPE, NULL_TYPE, STRING_TYPE, BOOLEAN_TYPE),
        info.getReturnType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseThisType1
  public void testParseThisType1() throws Exception {
    JSDocInfo info =
        parse("@this {goog.foo.Bar}*/");
    assertTypeEquals(
        new NamedType(registry, "goog.foo.Bar", null, -1, -1),
        info.getThisType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseThisType2
  public void testParseThisType2() throws Exception {
    JSDocInfo info =
        parse("@this goog.foo.Bar*/");
    assertTypeEquals(
        new NamedType(registry, "goog.foo.Bar", null, -1, -1),
        info.getThisType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseThisType3
  public void testParseThisType3() throws Exception {
    parse("@type {number}\n@this goog.foo.Bar*/",
        "type annotation incompatible with other annotations");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseThisType4
  public void testParseThisType4() throws Exception {
    resolve(parse("@this number*/").getThisType(),
        "@this must specify an object type");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseThisType5
  public void testParseThisType5() throws Exception {
    parse("@this {Date|Error}*/");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseThisType6
  public void testParseThisType6() throws Exception {
    resolve(parse("@this {Date|number}*/").getThisType(),
        "@this must specify an object type");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseParam1
  public void testParseParam1() throws Exception {
    JSDocInfo info = parse("@param {number} index*/");
    assertEquals(1, info.getParameterCount());
    assertTypeEquals(NUMBER_TYPE, info.getParameterType("index"));
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseParam2
  public void testParseParam2() throws Exception {
    JSDocInfo info = parse("@param index*/");
    assertEquals(1, info.getParameterCount());
    assertEquals(null, info.getParameterType("index"));
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseParam3
  public void testParseParam3() throws Exception {
    JSDocInfo info = parse("@param {number} index useful comments*/");
    assertEquals(1, info.getParameterCount());
    assertTypeEquals(NUMBER_TYPE, info.getParameterType("index"));
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseParam4
  public void testParseParam4() throws Exception {
    JSDocInfo info = parse("@param index useful comments*/");
    assertEquals(1, info.getParameterCount());
    assertEquals(null, info.getParameterType("index"));
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseParam5
  public void testParseParam5() throws Exception {
    
    JSDocInfo info = parse("@param {number} \n index */");
    assertEquals(1, info.getParameterCount());
    assertTypeEquals(NUMBER_TYPE, info.getParameterType("index"));
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseParam6
  public void testParseParam6() throws Exception {
    
    JSDocInfo info = parse("@param {number} \n * index */");
    assertEquals(1, info.getParameterCount());
    assertTypeEquals(NUMBER_TYPE, info.getParameterType("index"));
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseParam7
  public void testParseParam7() throws Exception {
    
    JSDocInfo info = parse("@param {number=} index */");
    assertTypeEquals(
        registry.createOptionalType(NUMBER_TYPE),
        info.getParameterType("index"));
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseParam8
  public void testParseParam8() throws Exception {
    
    JSDocInfo info = parse("@param {...number} index */");
    assertTypeEquals(
        registry.createOptionalType(NUMBER_TYPE),
        info.getParameterType("index"));
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseParam9
  public void testParseParam9() throws Exception {
    parse("@param {...number=} index */",
        "expected closing }", "expecting a variable name in a @param tag");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseParam10
  public void testParseParam10() throws Exception {
    parse("@param {...number index */", "expected closing }");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseParam11
  public void testParseParam11() throws Exception {
    parse("@param {number= index */", "expected closing }");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseParam12
  public void testParseParam12() throws Exception {
    JSDocInfo info = parse("@param {...number|string} index */");
    assertTypeEquals(
        registry.createOptionalType(
            registry.createUnionType(STRING_TYPE, NUMBER_TYPE)),
        info.getParameterType("index"));
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseParam13
  public void testParseParam13() throws Exception {
    JSDocInfo info = parse("@param {...(number|string)} index */");
    assertTypeEquals(
        registry.createOptionalType(
            registry.createUnionType(STRING_TYPE, NUMBER_TYPE)),
        info.getParameterType("index"));
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseParam14
  public void testParseParam14() throws Exception {
    JSDocInfo info = parse("@param {string} [index] */");
    assertEquals(1, info.getParameterCount());
    assertTypeEquals(
        registry.createOptionalType(STRING_TYPE),
        info.getParameterType("index"));
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseParam15
  public void testParseParam15() throws Exception {
    JSDocInfo info = parse("@param {string} [index */", "missing closing ]");
    assertEquals(1, info.getParameterCount());
    assertTypeEquals(STRING_TYPE, info.getParameterType("index"));
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseParam16
  public void testParseParam16() throws Exception {
    JSDocInfo info = parse("@param {string} index] */");
    assertEquals(1, info.getParameterCount());
    assertTypeEquals(STRING_TYPE, info.getParameterType("index"));
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseParam17
  public void testParseParam17() throws Exception {
    JSDocInfo info = parse("@param {string=} [index] */");
    assertEquals(1, info.getParameterCount());
    assertTypeEquals(
        registry.createOptionalType(STRING_TYPE),
        info.getParameterType("index"));
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseParam18
  public void testParseParam18() throws Exception {
    JSDocInfo info = parse("@param {...string} [index] */");
    assertEquals(1, info.getParameterCount());
    assertTypeEquals(
        registry.createOptionalType(STRING_TYPE),
        info.getParameterType("index"));
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseParam19
  public void testParseParam19() throws Exception {
    JSDocInfo info = parse("@param {...} [index] */");
    assertEquals(1, info.getParameterCount());
    assertTypeEquals(
        registry.createOptionalType(UNKNOWN_TYPE),
        info.getParameterType("index"));
    assertTrue(info.getParameterType("index").isVarArgs());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseThrows1
  public void testParseThrows1() throws Exception {
    JSDocInfo info = parse("@throws {number} Some number */");
    assertEquals(1, info.getThrownTypes().size());
    assertTypeEquals(NUMBER_TYPE, info.getThrownTypes().get(0));
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseThrows2
  public void testParseThrows2() throws Exception {
    JSDocInfo info = parse("@throws {number} Some number\n "
                           + "*@throws {String} A string */");
    assertEquals(2, info.getThrownTypes().size());
    assertTypeEquals(NUMBER_TYPE, info.getThrownTypes().get(0));
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseRecordType1
  public void testParseRecordType1() throws Exception {
    parseFull("");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseRecordType2
  public void testParseRecordType2() throws Exception {
    parseFull("");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseRecordType3
  public void testParseRecordType3() throws Exception {
    parseFull("");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseRecordType4
  public void testParseRecordType4() throws Exception {
    parseFull("");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseRecordType5
  public void testParseRecordType5() throws Exception {
    parseFull("");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseRecordType6
  public void testParseRecordType6() throws Exception {
    parseFull("");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseRecordType7
  public void testParseRecordType7() throws Exception {
    parseFull("");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseRecordType8
  public void testParseRecordType8() throws Exception {
    parseFull("");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseRecordType9
  public void testParseRecordType9() throws Exception {
    parseFull("");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseRecordType10
  public void testParseRecordType10() throws Exception {
    parseFull("");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseRecordType11
  public void testParseRecordType11() throws Exception {
    parseFull("",
              "expected closing }");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseRecordType12
  public void testParseRecordType12() throws Exception {
    parseFull("",
              "type not recognized due to syntax error");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseRecordType13
  public void testParseRecordType13() throws Exception {
    parseFull("");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseRecordType14
  public void testParseRecordType14() throws Exception {
    parseFull("");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseRecordType15
  public void testParseRecordType15() throws Exception {
    parseFull("");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseRecordType16
  public void testParseRecordType16() throws Exception {
    parseFull("");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseRecordType17
  public void testParseRecordType17() throws Exception {
    parseFull("");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseRecordType18
  public void testParseRecordType18() throws Exception {
    parseFull("");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseRecordType19
  public void testParseRecordType19() throws Exception {
    parseFull("");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseRecordType20
  public void testParseRecordType20() throws Exception {
    parseFull("");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseRecordType21
  public void testParseRecordType21() throws Exception {
    parseFull("");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseRecordType22
  public void testParseRecordType22() throws Exception {
    parseFull("");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseRecordType23
  public void testParseRecordType23() throws Exception {
    parseFull("");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseParamError1
  public void testParseParamError1() throws Exception {
    parseFull("",
        "expecting a variable name in a @param tag");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseParamError2
  public void testParseParamError2() throws Exception {
    parseFull("",
        "expecting a variable name in a @param tag");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseParamError3
  public void testParseParamError3() throws Exception {
    parseFull("",
        "expecting a variable name in a @param tag");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseParamError4
  public void testParseParamError4() throws Exception {
    parseFull("",
        "expecting a variable name in a @param tag");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseParamError5
  public void testParseParamError5() throws Exception {
    parse("@param {number} x \n * @param {string} x */",
        "duplicate variable name \"x\"");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseExtends1
  public void testParseExtends1() throws Exception {
    assertTypeEquals(STRING_OBJECT_TYPE,
                     parse("@extends String*/").getBaseType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseExtends2
  public void testParseExtends2() throws Exception {
    JSDocInfo info = parse("@extends com.google.Foo.Bar.Hello.World*/");
    assertTypeEquals(
        new NamedType(registry, "com.google.Foo.Bar.Hello.World", null, -1, -1),
        info.getBaseType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseExtendsGenerics
  public void testParseExtendsGenerics() throws Exception {
    JSDocInfo info =
        parse("@extends com.google.Foo.Bar.Hello.World.<Boolean,number>*/");
    assertTypeEquals(
        new NamedType(registry, "com.google.Foo.Bar.Hello.World", null, -1, -1),
        info.getBaseType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseImplementsGenerics
  public void testParseImplementsGenerics() throws Exception {
    
    List<JSTypeExpression> interfaces =
        parse("@implements {SomeInterface.<*>} */")
        .getImplementedInterfaces();
    assertEquals(1, interfaces.size());
    assertTypeEquals(new NamedType(registry, "SomeInterface", null, -1, -1),
        interfaces.get(0));
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseExtends4
  public void testParseExtends4() throws Exception {
    assertTypeEquals(STRING_OBJECT_TYPE,
        parse("@extends {String}*/").getBaseType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseExtends5
  public void testParseExtends5() throws Exception {
    assertTypeEquals(STRING_OBJECT_TYPE,
        parse("@extends {String*/", "expected closing }").getBaseType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseExtends6
  public void testParseExtends6() throws Exception {
    
    assertTypeEquals(STRING_OBJECT_TYPE,
        parse("@extends \n * {String}*/").getBaseType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseExtendsInvalidName
  public void testParseExtendsInvalidName() throws Exception {
    
    
    
    
    assertTypeEquals(
        new NamedType(registry, "some_++#%$%_UglyString", null, -1, -1),
        parse("@extends {some_++#%$%_UglyString} */").getBaseType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseExtendsNullable1
  public void testParseExtendsNullable1() throws Exception {
    parse("@extends {Base?} */", "expected closing }");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseExtendsNullable2
  public void testParseExtendsNullable2() throws Exception {
    parse("@extends Base? */", "expected end of line or comment");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseEnum1
  public void testParseEnum1() throws Exception {
    assertTypeEquals(NUMBER_TYPE, parse("@enum*/").getEnumParameterType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseEnum2
  public void testParseEnum2() throws Exception {
    assertTypeEquals(STRING_TYPE,
        parse("@enum {string}*/").getEnumParameterType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseEnum3
  public void testParseEnum3() throws Exception {
    assertTypeEquals(STRING_TYPE,
        parse("@enum string*/").getEnumParameterType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseDesc1
  public void testParseDesc1() throws Exception {
    assertEquals("hello world!",
        parse("@desc hello world!*/").getDescription());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseDesc2
  public void testParseDesc2() throws Exception {
    assertEquals("hello world!",
        parse("@desc hello world!\n*/").getDescription());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseDesc3
  public void testParseDesc3() throws Exception {
    assertEquals("", parse("@desc*/").getDescription());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseDesc4
  public void testParseDesc4() throws Exception {
    assertEquals("", parse("@desc\n*/").getDescription());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseDesc5
  public void testParseDesc5() throws Exception {
    assertEquals("hello world!",
                 parse("@desc hello\nworld!\n*/").getDescription());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseDesc6
  public void testParseDesc6() throws Exception {
    assertEquals("hello world!",
        parse("@desc hello\n* world!\n*/").getDescription());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseDesc7
  public void testParseDesc7() throws Exception {
    assertEquals("a b c", parse("@desc a\n\nb\nc*/").getDescription());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseDesc8
  public void testParseDesc8() throws Exception {
    assertEquals("a b c d",
        parse("@desc a\n      *b\n\n  *c\n\nd*/").getDescription());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseDesc9
  public void testParseDesc9() throws Exception {
    String comment = "@desc\n.\n,\n{\n)\n}\n|\n.<\n>\n<\n?\n~\n+\n-\n;\n:\n*/";

    assertEquals(". , { ) } | .< > < ? ~ + - ; :",
        parse(comment).getDescription());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseDesc10
  public void testParseDesc10() throws Exception {
    String comment = "@desc\n?\n?\n?\n?*/";

    assertEquals("? ? ? ?", parse(comment).getDescription());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseDesc11
  public void testParseDesc11() throws Exception {
    String comment = "@desc :[]*/";

    assertEquals(":[]", parse(comment).getDescription());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseDesc12
  public void testParseDesc12() throws Exception {
    String comment = "@desc\n:\n[\n]\n...*/";

    assertEquals(": [ ] ...", parse(comment).getDescription());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParsePreserve
  public void testParsePreserve() throws Exception {
    Node node = new Node(1);
    this.fileLevelJsDocBuilder = node.getJsDocBuilderForNode();
    String comment = "@preserve Foo\nBar\n\nBaz*/";
    parse(comment);
    assertEquals(" Foo\n Bar\n\n Baz", node.getJSDocInfo().getLicense());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseLicense
  public void testParseLicense() throws Exception {
    Node node = new Node(1);
    this.fileLevelJsDocBuilder = node.getJsDocBuilderForNode();
    String comment = "@license Foo\nBar\n\nBaz*/";
    parse(comment);
    assertEquals(" Foo\n Bar\n\n Baz", node.getJSDocInfo().getLicense());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseLicenseWithAnnotation
  public void testParseLicenseWithAnnotation() throws Exception {
    Node node = new Node(1);
    this.fileLevelJsDocBuilder = node.getJsDocBuilderForNode();
    String comment = "@license Foo \n * @author Charlie Brown */";
    parse(comment);
    assertEquals(" Foo \n @author Charlie Brown ",
        node.getJSDocInfo().getLicense());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseDefine1
  public void testParseDefine1() throws Exception {
    assertTypeEquals(STRING_TYPE,
        parse("@define {string}*/").getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseDefine2
  public void testParseDefine2() throws Exception {
    assertTypeEquals(STRING_TYPE,
        parse("@define {string*/", "expected closing }").getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseDefine3
  public void testParseDefine3() throws Exception {
    JSDocInfo info = parse("@define {boolean}*/");
    assertTrue(info.isConstant());
    assertTrue(info.isDefine());
    assertTypeEquals(BOOLEAN_TYPE, info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseDefine4
  public void testParseDefine4() throws Exception {
    assertTypeEquals(NUMBER_TYPE, parse("@define {number}*/").getType());
  }

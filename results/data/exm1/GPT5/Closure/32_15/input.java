// buggy code
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

    // Track the start of the line to count whitespace that
    // the tokenizer skipped. Because this case is rare, it's easier
    // to do this here than in the tokenizer.

    do {
      switch (token) {
        case STAR:
          if (ignoreStar) {
            // Mark the position after the star as the new start of the line.
          } else {
            // The star is part of the comment.
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

        default:
          ignoreStar = false;
          state = State.SEARCHING_ANNOTATION;

              // All tokens must be separated by a space.

          if (token == JsDocToken.EOC ||
              token == JsDocToken.EOF ||
              // When we're capturing a license block, annotations
              // in the block are ok.
              (token == JsDocToken.ANNOTATION &&
               option != WhitespaceOption.PRESERVE)) {
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
          }

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
// com.google.javascript.jscomp.TypedScopeCreatorTest::testReturnTypeInference4
  public void testReturnTypeInference4() {
    testSame("function f() { throw Error(); }");
    assertEquals(
        "function (): ?",
        findNameType("f", globalScope).toString());
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testReturnTypeInference5
  public void testReturnTypeInference5() {
    testSame("function f() { if (true) { return 1; } }");
    assertEquals(
        "function (): ?",
        findNameType("f", globalScope).toString());
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testLiteralTypesInferred
  public void testLiteralTypesInferred() {
    testSame("null + true + false + 0 + '' + {}");
    assertEquals(
        "null", findTokenType(Token.NULL, globalScope).toString());
    assertEquals(
        "boolean", findTokenType(Token.TRUE, globalScope).toString());
    assertEquals(
        "boolean", findTokenType(Token.FALSE, globalScope).toString());
    assertEquals(
        "number", findTokenType(Token.NUMBER, globalScope).toString());
    assertEquals(
        "string", findTokenType(Token.STRING, globalScope).toString());
    assertEquals(
        "{}", findTokenType(Token.OBJECTLIT, globalScope).toString());
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testGlobalQualifiedNameInLocalScope
  public void testGlobalQualifiedNameInLocalScope() {
    testSame(
        "var ns = {}; " +
        "(function() { " +
        "     ns.foo = function(x) {}; })();" +
        "(function() { ns.foo(3); })();");
    assertNotNull(globalScope.getVar("ns.foo"));
    assertEquals(
        "function (number): undefined",
        globalScope.getVar("ns.foo").getType().toString());
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testDeclaredObjectLitProperty1
  public void testDeclaredObjectLitProperty1() throws Exception {
    testSame("var x = { y: 3};");
    ObjectType xType = ObjectType.cast(globalScope.getVar("x").getType());
    assertEquals(
        "number",
         xType.getPropertyType("y").toString());
    assertEquals(
        "{y: number}",
        xType.toString());
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testDeclaredObjectLitProperty2
  public void testDeclaredObjectLitProperty2() throws Exception {
    testSame("var x = { y: function(z){}};");
    ObjectType xType = ObjectType.cast(globalScope.getVar("x").getType());
    assertEquals(
        "function (number): undefined",
         xType.getPropertyType("y").toString());
    assertEquals(
        "{y: function (number): undefined}",
        xType.toString());
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testDeclaredObjectLitProperty3
  public void testDeclaredObjectLitProperty3() throws Exception {
    testSame("function f() {" +
        "  var x = { y: function(z){ return 3; }};" +
        "}");
    ObjectType xType = ObjectType.cast(lastLocalScope.getVar("x").getType());
    assertEquals(
        "function (?): number",
         xType.getPropertyType("y").toString());
    assertEquals(
        "{y: function (?): number}",
        xType.toString());
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testDeclaredObjectLitProperty4
  public void testDeclaredObjectLitProperty4() throws Exception {
    testSame("var x = {y: 5,  z: 3};");
    ObjectType xType = ObjectType.cast(globalScope.getVar("x").getType());
    assertEquals(
        "number", xType.getPropertyType("y").toString());
    assertFalse(xType.isPropertyTypeDeclared("y"));
    assertTrue(xType.isPropertyTypeDeclared("z"));
    assertEquals(
        "{y: number, z: number}",
        xType.toString());
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testDeclaredObjectLitProperty5
  public void testDeclaredObjectLitProperty5() throws Exception {
    testSame("var x = { prop: 3};" +
             "function f() { var y = x.prop; }");
    JSType yType = lastLocalScope.getVar("y").getType();
    assertEquals("number", yType.toString());
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testDeclaredObjectLitProperty6
  public void testDeclaredObjectLitProperty6() throws Exception {
    testSame("var x = { prop: function(){}};");
    Var prop = globalScope.getVar("x.prop");
    JSType propType = prop.getType();
    assertEquals("function (): undefined", propType.toString());
    assertFalse(prop.isTypeInferred());
    assertFalse(
        ObjectType.cast(globalScope.getVar("x").getType())
        .isPropertyTypeInferred("prop"));
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testInferredObjectLitProperty1
  public void testInferredObjectLitProperty1() throws Exception {
    testSame("var x = {prop: 3};");
    Var prop = globalScope.getVar("x.prop");
    JSType propType = prop.getType();
    assertEquals("number", propType.toString());
    assertTrue(prop.isTypeInferred());
    assertTrue(
        ObjectType.cast(globalScope.getVar("x").getType())
        .isPropertyTypeInferred("prop"));
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testInferredObjectLitProperty2
  public void testInferredObjectLitProperty2() throws Exception {
    testSame("var x = {prop: function(){}};");
    Var prop = globalScope.getVar("x.prop");
    JSType propType = prop.getType();
    assertEquals("function (): undefined", propType.toString());
    assertTrue(prop.isTypeInferred());
    assertTrue(
        ObjectType.cast(globalScope.getVar("x").getType())
        .isPropertyTypeInferred("prop"));
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testDeclaredConstType1
  public void testDeclaredConstType1() throws Exception {
    testSame(
        " var x = 3;" +
        "function f() { var y = x; }");
    JSType yType = lastLocalScope.getVar("y").getType();
    assertEquals("number", yType.toString());
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testDeclaredConstType2
  public void testDeclaredConstType2() throws Exception {
    testSame(
        " var x = {};" +
        "function f() { var y = x; }");
    JSType yType = lastLocalScope.getVar("y").getType();
    assertEquals("{}", yType.toString());
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testDeclaredConstType3
  public void testDeclaredConstType3() throws Exception {
    testSame(
        " var x = {};" +
        " x.z = 'hi';" +
        "function f() { var y = x.z; }");
    JSType yType = lastLocalScope.getVar("y").getType();
    assertEquals("string", yType.toString());
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testDeclaredConstType4
  public void testDeclaredConstType4() throws Exception {
    testSame(
        " function Foo() {}" +
        " Foo.prototype.z = 'hi';" +
        "function f() { var y = (new Foo()).z; }");
    JSType yType = lastLocalScope.getVar("y").getType();
    assertEquals("string", yType.toString());

    ObjectType fooType =
        ((FunctionType) globalScope.getVar("Foo").getType()).getInstanceType();
    assertTrue(fooType.isPropertyTypeDeclared("z"));
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testDeclaredConstType5
  public void testDeclaredConstType5() throws Exception {
    testSame(
        " var goog = goog || {};" +
        " var foo = goog || {};" +
        "function f() { var y = goog; var z = foo; }");
    JSType yType = lastLocalScope.getVar("y").getType();
    assertEquals("{}", yType.toString());

    JSType zType = lastLocalScope.getVar("z").getType();
    assertEquals("?", zType.toString());
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testBadCtorInit1
  public void testBadCtorInit1() throws Exception {
    testSame(" var f;", CTOR_INITIALIZER);
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testBadCtorInit2
  public void testBadCtorInit2() throws Exception {
    testSame("var x = {};  x.f;", CTOR_INITIALIZER);
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testBadIfaceInit1
  public void testBadIfaceInit1() throws Exception {
    testSame(" var f;", IFACE_INITIALIZER);
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testBadIfaceInit2
  public void testBadIfaceInit2() throws Exception {
    testSame("var x = {};  x.f;", IFACE_INITIALIZER);
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testFunctionInHook
  public void testFunctionInHook() throws Exception {
    testSame(" var f = Math.random() ? " +
        "function(x) {} : function(x) {};");
    assertEquals("number", lastLocalScope.getVar("x").getType().toString());
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testFunctionInAnd
  public void testFunctionInAnd() throws Exception {
    testSame(" var f = Math.random() && " +
        "function(x) {};");
    assertEquals("number", lastLocalScope.getVar("x").getType().toString());
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testFunctionInOr
  public void testFunctionInOr() throws Exception {
    testSame(" var f = Math.random() || " +
        "function(x) {};");
    assertEquals("number", lastLocalScope.getVar("x").getType().toString());
  }

// com.google.javascript.jscomp.TypedScopeCreatorTest::testFunctionInComma
  public void testFunctionInComma() throws Exception {
    testSame(" var f = (Math.random(), " +
        "function(x) {});");
    assertEquals("number", lastLocalScope.getVar("x").getType().toString());
  }

// com.google.javascript.jscomp.VariableReferenceCheckTest::testCorrectCode
  public void testCorrectCode() {
    assertNoWarning("function foo(d) { (function() { d.foo(); }); d.bar(); } ");
    assertNoWarning("function foo() { bar(); } function bar() { foo(); } ");
    assertNoWarning("function f(d) { d = 3; }");
    assertNoWarning(VARIABLE_RUN);
    assertNoWarning("function f() { " + VARIABLE_RUN + "}");
  }

// com.google.javascript.jscomp.VariableReferenceCheckTest::testCorrectShadowing
  public void testCorrectShadowing() {
    assertNoWarning(VARIABLE_RUN + "function f() { " + VARIABLE_RUN + "}");
  }

// com.google.javascript.jscomp.VariableReferenceCheckTest::testCorrectRedeclare
  public void testCorrectRedeclare() {
    assertNoWarning(
        "function f() { if (1) { var a = 2; } else { var a = 3; } }");
  }

// com.google.javascript.jscomp.VariableReferenceCheckTest::testCorrectRecursion
  public void testCorrectRecursion() {
    assertNoWarning("function f() { var x = function() { x(); }; }");
  }

// com.google.javascript.jscomp.VariableReferenceCheckTest::testCorrectCatch
  public void testCorrectCatch() {
    assertNoWarning("function f() { try { var x = 2; } catch (x) {} }");
  }

// com.google.javascript.jscomp.VariableReferenceCheckTest::testRedeclare
  public void testRedeclare() {
    
    assertRedeclare("function f() { var a = 2; var a = 3; }");
    assertRedeclare("function f(a) { var a = 2; }");
  }

// com.google.javascript.jscomp.VariableReferenceCheckTest::testEarlyReference
  public void testEarlyReference() {
    assertUndeclared("function f() { a = 2; var a = 3; }");
  }

// com.google.javascript.jscomp.VariableReferenceCheckTest::testCorrectEarlyReference
  public void testCorrectEarlyReference() {
    assertNoWarning("var goog = goog || {}");
    assertNoWarning("function f() { a = 2; } var a = 2;");
  }

// com.google.javascript.jscomp.VariableReferenceCheckTest::testUnreferencedBleedingFunction
  public void testUnreferencedBleedingFunction() {
    assertNoWarning("var x = function y() {}");
  }

// com.google.javascript.jscomp.VariableReferenceCheckTest::testReferencedBleedingFunction
  public void testReferencedBleedingFunction() {
    assertNoWarning("var x = function y() { return y(); }");
  }

// com.google.javascript.jscomp.VariableReferenceCheckTest::testDoubleDeclaration
  public void testDoubleDeclaration() {
    assertRedeclare("function x(y) { if (true) { var y; } }");
  }

// com.google.javascript.jscomp.VariableReferenceCheckTest::testDoubleDeclaration2
  public void testDoubleDeclaration2() {
    assertRedeclare("function x() { var y; if (true) { var y; } }");
  }

// com.google.javascript.jscomp.VariableReferenceCheckTest::testHoistedFunction1
  public void testHoistedFunction1() {
    enableAmbiguousFunctionCheck = true;
    assertNoWarning("f(); function f() {}");
  }

// com.google.javascript.jscomp.VariableReferenceCheckTest::testHoistedFunction2
  public void testHoistedFunction2() {
    enableAmbiguousFunctionCheck = true;
    assertNoWarning("function g() { f(); function f() {} }");
  }

// com.google.javascript.jscomp.VariableReferenceCheckTest::testNonHoistedFunction
  public void testNonHoistedFunction() {
    enableAmbiguousFunctionCheck = true;
    assertUndeclared("if (true) { f(); function f() {} }");
  }

// com.google.javascript.jscomp.VariableReferenceCheckTest::testNonHoistedFunction2
  public void testNonHoistedFunction2() {
    enableAmbiguousFunctionCheck = true;
    assertNoWarning("if (false) { function f() {} f(); }");
  }

// com.google.javascript.jscomp.VariableReferenceCheckTest::testNonHoistedFunction3
  public void testNonHoistedFunction3() {
    enableAmbiguousFunctionCheck = true;
    assertNoWarning("function g() { if (false) { function f() {} f(); }}");
  }

// com.google.javascript.jscomp.VariableReferenceCheckTest::testNonHoistedFunction4
  public void testNonHoistedFunction4() {
    enableAmbiguousFunctionCheck = true;
    assertAmbiguous("if (false) { function f() {} }  f();");
  }

// com.google.javascript.jscomp.VariableReferenceCheckTest::testNonHoistedFunction5
  public void testNonHoistedFunction5() {
    enableAmbiguousFunctionCheck = true;
    assertAmbiguous("function g() { if (false) { function f() {} }  f(); }");
  }

// com.google.javascript.jscomp.VariableReferenceCheckTest::testNonHoistedFunction6
  public void testNonHoistedFunction6() {
    enableAmbiguousFunctionCheck = true;
    assertUndeclared("if (false) { f(); function f() {} }");
  }

// com.google.javascript.jscomp.VariableReferenceCheckTest::testNonHoistedFunction7
  public void testNonHoistedFunction7() {
    enableAmbiguousFunctionCheck = true;
    assertUndeclared("function g() { if (false) { f(); function f() {} }}");
  }

// com.google.javascript.jscomp.VariableReferenceCheckTest::testNonHoistedRecursiveFunction1
  public void testNonHoistedRecursiveFunction1() {
    enableAmbiguousFunctionCheck = true;
    assertNoWarning("if (false) { function f() { f(); }}");
  }

// com.google.javascript.jscomp.VariableReferenceCheckTest::testNonHoistedRecursiveFunction2
  public void testNonHoistedRecursiveFunction2() {
    enableAmbiguousFunctionCheck = true;
    assertNoWarning("function g() { if (false) { function f() { f(); }}}");
  }

// com.google.javascript.jscomp.VariableReferenceCheckTest::testNonHoistedRecursiveFunction3
  public void testNonHoistedRecursiveFunction3() {
    enableAmbiguousFunctionCheck = true;
    assertNoWarning("function g() { if (false) { function f() { f(); g(); }}}");
  }

// com.google.javascript.jscomp.VariableReferenceCheckTest::testNoWarnInExterns1
  public void testNoWarnInExterns1() {
    
    String externs =
       "var google;" +
       " var google";
    String code = "";
    test(externs, code, code, null, null);
  }

// com.google.javascript.jscomp.VariableReferenceCheckTest::testNoWarnInExterns2
  public void testNoWarnInExterns2() {
    
    String externs =
       "window;" +
       "var window;";
    String code = "";
    test(externs, code, code, null, null);
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testStrictScript
  public void testStrictScript() throws Exception {
    assertNull(newParse("").getDirectives());
    assertEquals(
        Sets.newHashSet("use strict"),
        newParse("'use strict'").getDirectives());
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testArrayLiteral2
  public void testArrayLiteral2() throws Exception {
    testNewParser("[a, , b]",
      "SCRIPT 1 [source_file: FileName.js] [length: 8]\n" +
      "    EXPR_RESULT 1 [source_file: FileName.js] [length: 8]\n" +
      "        ARRAYLIT 1 [source_file: FileName.js] [length: 8]\n" +
      "            NAME a 1 [source_file: FileName.js] [length: 1]\n" +
      "            EMPTY 1 [source_file: FileName.js] [length: 1]\n" +
      "            NAME b 1 [source_file: FileName.js] [length: 1]\n");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testArrayLiteral4
  public void testArrayLiteral4() throws Exception {
    testNewParser("[,,,a,,b]",
      "SCRIPT 1 [source_file: FileName.js] [length: 9]\n" +
      "    EXPR_RESULT 1 [source_file: FileName.js] [length: 9]\n" +
      "        ARRAYLIT 1 [source_file: FileName.js] [length: 9]\n" +
      "            EMPTY 1 [source_file: FileName.js] [length: 1]\n" +
      "            EMPTY 1 [source_file: FileName.js] [length: 1]\n" +
      "            EMPTY 1 [source_file: FileName.js] [length: 1]\n" +
      "            NAME a 1 [source_file: FileName.js] [length: 1]\n" +
      "            EMPTY 1 [source_file: FileName.js] [length: 1]\n" +
      "            NAME b 1 [source_file: FileName.js] [length: 1]\n");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testObjectLiteral
  public void testObjectLiteral() {
    newParse("var o = {}");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testObjectLiteral2
  public void testObjectLiteral2() {
    newParse("var o = {a: 1}");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testObjectLiteral3
  public void testObjectLiteral3() {
    newParse("var o = {a: 1, b: 2}");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testObjectLiteral4
  public void testObjectLiteral4() {
    newParse("var o = {1: 'a'}");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testObjectLiteral5
  public void testObjectLiteral5() {
    newParse("var o = {'a': 'a'}");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testObjectLiteral6
  public void testObjectLiteral6() {
    testNewParser("({1: true})",
      "SCRIPT 1 [source_file: FileName.js] [length: 11]\n" +
      "    EXPR_RESULT 1 [source_file: FileName.js] [length: 10]\n" +
      "        OBJECTLIT 1 [parenthesized: true] [source_file: FileName.js] [length: 9]\n" +
      "            STRING 1 1 [quoted: 1] [source_file: FileName.js] [length: 1]\n" +
      "                TRUE 1 [source_file: FileName.js] [length: 4]\n");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testObjectLiteral7
  public void testObjectLiteral7() {
    mode = LanguageMode.ECMASCRIPT5;

    testNewParser("({get 1() {}})",
        "SCRIPT 1 [source_file: FileName.js] [length: 14]\n" +
        "    EXPR_RESULT 1 [source_file: FileName.js] [length: 13]\n" +
        "        OBJECTLIT 1 [parenthesized: true] [source_file: FileName.js] [length: 12]\n" +
        "            GETTER_DEF 1 1 [quoted: 1] [source_file: FileName.js] [length: 1]\n" +
        "                FUNCTION  1 [source_file: FileName.js] [length: 6]\n" +
        "                    NAME  1 [source_file: FileName.js]\n" +
        "                    PARAM_LIST 1 [source_file: FileName.js]\n" +
        "                    BLOCK 1 [source_file: FileName.js] [length: 2]\n");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testObjectLiteral8
  public void testObjectLiteral8() {
    mode = LanguageMode.ECMASCRIPT5;

    testNewParser("({set 1(a) {}})",
        "SCRIPT 1 [source_file: FileName.js] [length: 15]\n" +
        "    EXPR_RESULT 1 [source_file: FileName.js] [length: 14]\n" +
        "        OBJECTLIT 1 [parenthesized: true] [source_file: FileName.js] [length: 13]\n" +
        "            SETTER_DEF 1 1 [quoted: 1] [source_file: FileName.js] [length: 1]\n" +
        "                FUNCTION  1 [source_file: FileName.js] [length: 7]\n" +
        "                    NAME  1 [source_file: FileName.js]\n" +
        "                    PARAM_LIST 1 [source_file: FileName.js]\n" +
        "                        NAME a 1 [source_file: FileName.js] [length: 1]\n" +
        "                    BLOCK 1 [source_file: FileName.js] [length: 2]\n");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testLabel
  public void testLabel() {
    testNewParser("foo: bar",
        "SCRIPT 1 [source_file: FileName.js] [length: 8]\n" +
        "    LABEL 1 [source_file: FileName.js] [length: 4]\n" +
        "        LABEL_NAME foo 1 [source_file: FileName.js] [length: 4]\n" +
        "        EXPR_RESULT 1 [source_file: FileName.js] [length: 3]\n" +
        "            NAME bar 1 [source_file: FileName.js] [length: 3]\n");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testLabel2
  public void testLabel2() {
    testNewParser("l: while (f()) { if (g()) { continue l; } }",
        "SCRIPT 1 [source_file: FileName.js] [length: 43]\n" +
        "    LABEL 1 [source_file: FileName.js] [length: 2]\n" +
        "        LABEL_NAME l 1 [source_file: FileName.js] [length: 2]\n" +
        "        WHILE 1 [source_file: FileName.js] [length: 40]\n" +
        "            CALL 1 [source_file: FileName.js] [length: 3]\n" +
        "                NAME f 1 [source_file: FileName.js] [length: 1]\n" +
        "            BLOCK 1 [source_file: FileName.js] [length: 28]\n" +
        "                IF 1 [source_file: FileName.js] [length: 24]\n" +
        "                    CALL 1 [source_file: FileName.js] [length: 3]\n" +
        "                        NAME g 1 [source_file: FileName.js] [length: 1]\n" +
        "                    BLOCK 1 [source_file: FileName.js] [length: 15]\n" +
        "                        CONTINUE 1 [source_file: FileName.js] [length: 11]\n" +
        "                            LABEL_NAME l 1 [source_file: FileName.js] [length: 1]\n");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testLabel3
  public void testLabel3() {
    testNewParser("Foo:Bar:X:{ break Bar; }",
        "SCRIPT 1 [source_file: FileName.js] [length: 24]\n" +
        "    LABEL 1 [source_file: FileName.js] [length: 4]\n" +
        "        LABEL_NAME Foo 1 [source_file: FileName.js] [length: 4]\n" +
        "        LABEL 1 [source_file: FileName.js] [length: 4]\n" +
        "            LABEL_NAME Bar 1 [source_file: FileName.js] [length: 4]\n" +
        "            LABEL 1 [source_file: FileName.js] [length: 2]\n" +
        "                LABEL_NAME X 1 [source_file: FileName.js] [length: 2]\n" +
        "                BLOCK 1 [source_file: FileName.js] [length: 14]\n" +
        "                    BREAK 1 [source_file: FileName.js] [length: 10]\n" +
        "                        LABEL_NAME Bar 1 [source_file: FileName.js] [length: 3]\n");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testNegation1
  public void testNegation1() {
    testNewParser("-a",
        "SCRIPT 1 [source_file: FileName.js] [length: 2]\n" +
        "    EXPR_RESULT 1 [source_file: FileName.js] [length: 2]\n" +
        "        NEG 1 [source_file: FileName.js] [length: 2]\n" +
        "            NAME a 1 [source_file: FileName.js] [length: 1]\n");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testNegation2
  public void testNegation2() {
    testNewParser("-2",
        "SCRIPT 1 [source_file: FileName.js] [length: 2]\n" +
        "    EXPR_RESULT 1 [source_file: FileName.js] [length: 2]\n" +
        "        NUMBER -2.0 1 [source_file: FileName.js] [length: 1]\n");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testNegation3
  public void testNegation3() {
    testNewParser("1 - -2",
        "SCRIPT 1 [source_file: FileName.js] [length: 6]\n" +
        "    EXPR_RESULT 1 [source_file: FileName.js] [length: 6]\n" +
        "        SUB 1 [source_file: FileName.js] [length: 6]\n" +
        "            NUMBER 1.0 1 [source_file: FileName.js] [length: 1]\n" +
        "            NUMBER -2.0 1 [source_file: FileName.js] [length: 1]\n");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testGetter
  public void testGetter() {
    mode = LanguageMode.ECMASCRIPT5;
    testNewParser("({get a() {}})",
        "SCRIPT 1 [source_file: FileName.js] [length: 14]\n" +
        "    EXPR_RESULT 1 [source_file: FileName.js] [length: 13]\n" +
        "        OBJECTLIT 1 [parenthesized: true] [source_file: FileName.js] [length: 12]\n" +
        "            GETTER_DEF a 1 [source_file: FileName.js] [length: 1]\n" +
        "                FUNCTION  1 [source_file: FileName.js] [length: 6]\n" +
        "                    NAME  1 [source_file: FileName.js]\n" +
        "                    PARAM_LIST 1 [source_file: FileName.js]\n" +
        "                    BLOCK 1 [source_file: FileName.js] [length: 2]\n");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testSetter
  public void testSetter() {
    mode = LanguageMode.ECMASCRIPT5;
    testNewParser("({set a(x) {}})",
        "SCRIPT 1 [source_file: FileName.js] [length: 15]\n" +
        "    EXPR_RESULT 1 [source_file: FileName.js] [length: 14]\n" +
        "        OBJECTLIT 1 [parenthesized: true] [source_file: FileName.js] [length: 13]\n" +
        "            SETTER_DEF a 1 [source_file: FileName.js] [length: 1]\n" +
        "                FUNCTION  1 [source_file: FileName.js] [length: 7]\n" +
        "                    NAME  1 [source_file: FileName.js]\n" +
        "                    PARAM_LIST 1 [source_file: FileName.js]\n" +
        "                        NAME x 1 [source_file: FileName.js] [length: 1]\n" +
        "                    BLOCK 1 [source_file: FileName.js] [length: 2]\n");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testDelete1
  public void testDelete1() {
    testNoParseError("delete a.b;");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testDelete2
  public void testDelete2() {
    testNoParseError("delete a['b'];");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testDelete3
  public void testDelete3() {
    
    
    testNoParseError("delete a;");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testDelete4
  public void testDelete4() {
    testParseError("delete 'x';",
        "Invalid delete operand. Only properties can be deleted.");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testCommentPositions1
  public void testCommentPositions1() {
    Node root = newParse("function a(x) {};" +
        "function b(x) {}");
    Node a = root.getFirstChild();
    Node b = root.getLastChild();
    assertMarkerPosition(a, 1, 4);
    assertMarkerPosition(b, 1, 45);
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testCommentPositions2
  public void testCommentPositions2() {
    Node root = newParse(
        "\n" +
        "\n" +
        "function a(x) {};\n" +
        "\n" +
        "\n" +
        "\n" +
        "\n" +
        "function b(x) {};");
    assertMarkerPosition(root.getFirstChild(), 4, 4);
    assertMarkerPosition(root.getFirstChild().getNext().getNext(), 11, 6);
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testLiteralLocation
   public void testLiteralLocation() {
    Node root = newParse(
        "var d =\n" +
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
        "switch (a) {\n" +
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
        "function\n" +
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

    assertNodePosition(1, 0, function);
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
        "var\n" +
        "    a =\n" +
        "    3\n");
    Node varDecl = root.getFirstChild();
    Node varName = varDecl.getFirstChild();
    Node varExpr = varName.getFirstChild();

    assertNodePosition(1, 0, varDecl);
    assertNodePosition(2, 4, 1, varName);
    assertNodePosition(3, 4, 1, varExpr);
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testReturnLocation
  public void testReturnLocation() {
    Node root = newParse(
        "function\n" +
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
        "for(\n" +
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
        "var d = a\n" +
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
    assertNodePosition(1, 8, firstVarAdd);
    assertNodePosition(1, 8, firstVarAdd.getFirstChild());
    assertNodePosition(3, 4, firstVarAdd.getLastChild());

    assertNodePosition(4, 0, secondVarDecl);
    assertNodePosition(5, 4, secondVar);
    assertNodePosition(6, 4, secondVarAdd);
    assertNodePosition(6, 4, secondVarAdd.getFirstChild());
    assertNodePosition(7, 4, secondVarAdd.getLastChild());

    assertNodePosition(8, 0, thirdVarDecl);
    assertNodePosition(8, 4, thirdVar);
    assertNodePosition(8, 8, thirdVarAdd);
    assertNodePosition(8, 8, thirdVarAdd.getFirstChild());
    assertNodePosition(9, 6, thirdVarAdd.getLastChild());
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testPrefixLocation
  public void testPrefixLocation() {
    Node root = newParse(
         "a++;\n" +
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
        "if\n" +
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
    assertNodePosition(2, 3, eqClause);
    assertNodePosition(3, 0, thenClause);
    assertNodePosition(7, 0, elseClause);
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testTryLocation
  public void testTryLocation() {
     Node root = newParse(
         "try {\n" +
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
        "a\n" +
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
        "foo:\n" +
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
        "a\n" +
        "<\n" +
        "b\n");

    Node condClause = root.getFirstChild().getFirstChild();
    Node lhs = condClause.getFirstChild();
    Node rhs = lhs.getNext();

    assertNodePosition(1, 0, condClause);
    assertNodePosition(1, 0, lhs);
    assertNodePosition(3, 0, rhs);
   }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testEqualityLocation
  public void testEqualityLocation() {
    Node root = newParse(
        "a\n" +
        "==\n" +
        "b\n");

    Node condClause = root.getFirstChild().getFirstChild();
    Node lhs = condClause.getFirstChild();
    Node rhs = lhs.getNext();

    assertNodePosition(1, 0, condClause);
    assertNodePosition(1, 0, lhs);
    assertNodePosition(3, 0, rhs);
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testPlusEqLocation
  public void testPlusEqLocation() {
    Node root = newParse(
        "a\n" +
        "+=\n" +
        "b\n");

    Node condClause = root.getFirstChild().getFirstChild();
    Node lhs = condClause.getFirstChild();
    Node rhs = lhs.getNext();

    assertNodePosition(1, 0, condClause);
    assertNodePosition(1, 0, lhs);
    assertNodePosition(3, 0, rhs);
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testCommaLocation
  public void testCommaLocation() {
    Node root = newParse(
        "a,\n" +
        "b,\n" +
        "c;\n");

    Node statement = root.getFirstChild();
    Node comma1 = statement.getFirstChild();
    Node comma2 = comma1.getFirstChild();
    Node cRef = comma2.getNext();
    Node aRef = comma2.getFirstChild();
    Node bRef = aRef.getNext();

    assertNodePosition(1, 0, comma2);
    assertNodePosition(1, 0, aRef);
    assertNodePosition(2, 0, bRef);
    assertNodePosition(3, 0, cRef);
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testRegexpLocation
  public void testRegexpLocation() {
    Node root = newParse(
        "var path =\n" +
        "replace(\n" +
        "/a/g," +
        "'/');\n");

    Node firstVarDecl = root.getFirstChild();
    Node firstVar = firstVarDecl.getFirstChild();
    Node callNode = firstVar.getFirstChild();
    Node fnName = callNode.getFirstChild();
    Node regexObject = fnName.getNext();
    Node aString = regexObject.getFirstChild();
    Node endRegexString = regexObject.getNext();

    assertNodePosition(1, 0, firstVarDecl);
    assertNodePosition(1, 4, 4, firstVar);
    assertNodePosition(2, 0, 18, callNode);
    assertNodePosition(2, 0, 7, fnName);
    assertNodePosition(3, 0, regexObject);
    assertNodePosition(3, 0, aString);
    assertNodePosition(3, 5, endRegexString);
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testNestedOr
  public void testNestedOr() {
    Node root = newParse(
        "if (a && \n" +
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
    assertNodePosition(1, 4, orClause);
    assertNodePosition(1, 4, andClause);
    assertNodePosition(4, 4, cName);

  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testBitwiseOps
  public void testBitwiseOps() {
      Node root = newParse(
        "if (a & \n" +
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
    assertNodePosition(1, 4, bitOr);
    assertNodePosition(1, 4, bitAnd);
    assertNodePosition(4, 4, cName);

  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testObjectLitLocation
  public void testObjectLitLocation() {
    Node root = newParse(
        "var foo =\n" +
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
    Node firstValue = firstKey.getFirstChild();

    Node secondKey = firstKey.getNext();
    Node secondValue = secondKey.getFirstChild();

    Node thirdKey = secondKey.getNext();
    Node thirdValue = thirdKey.getFirstChild();

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
         "try {\n" +
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
         "try {\n" +
         "  var x = 1;\n" +
         "} catch (ex) {\n" +
         "  var y = 2;\n" +
         "}\n");

    Node tryStmt = root.getFirstChild();
    Node tryBlock = tryStmt.getFirstChild();
    Node catchBlock = tryBlock.getNext();
    Node catchStmt = catchBlock.getFirstChild();
    Node exceptionVar = catchStmt.getFirstChild();
    Node exceptionBlock = exceptionVar.getNext();
    Node varDecl = exceptionBlock.getFirstChild();

    assertNodePosition(1, 0, tryStmt);
    assertNodePosition(1, 4, tryBlock);
    assertNodePosition(3, 0, catchBlock);
    assertNodePosition(3, 2, catchStmt);
    assertNodePosition(3, 9, exceptionVar);
    assertNodePosition(3, 13, exceptionBlock);
    assertNodePosition(4, 2, varDecl);
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testMultilineEqLocation
  public void testMultilineEqLocation() {
    Node  root = newParse(
        "if\n" +
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
    assertNodePosition(2, 7, orTest);
    assertNodePosition(2, 7, andTest);
    assertNodePosition(2, 7, aTest);
    assertNodePosition(4, 3, bTest);
    assertNodePosition(5, 2, cTest);
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testMultilineBitTestLocation
  public void testMultilineBitTestLocation() {
    Node root = newParse(
        "if (\n" +
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

    assertNodePosition(2, 8, eqTest);
    assertNodePosition(7, 8, notEqTest);

    assertNodePosition(2, 8, bitOrTest);
    assertNodePosition(5, 8, bitAndTest);
    assertNodePosition(7, 8, bitXorTest);
    assertNodePosition(10, 8, bitShiftTest);
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testCallLocation
  public void testCallLocation() {
    Node root = newParse(
        "a.\n" +
        "b.\n" +
        "cccc(1);\n");

    Node exprStmt = root.getFirstChild();
    Node functionCall = exprStmt.getFirstChild();
    Node functionProp = functionCall.getFirstChild();
    Node firstNameComponent = functionProp.getFirstChild();
    Node lastNameComponent = firstNameComponent.getNext();
    Node aNameComponent = firstNameComponent.getFirstChild();
    Node bNameComponent = aNameComponent.getNext();

    assertNodePosition(1, 0, 13, functionCall);
    assertNodePosition(1, 0, 10, functionProp);
    
    
    
    assertNodePosition(1, 0, 4, firstNameComponent);
    assertNodePosition(3, 0, 4, lastNameComponent);

    assertNodePosition(1, 0, 1, aNameComponent);
    assertNodePosition(2, 0, 1, bNameComponent);
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testLinenoDeclaration
  public void testLinenoDeclaration() {
    Node root = newParse(
        "a.\n" +
        "b=\n" +
        "function() {};\n");

    Node exprStmt = root.getFirstChild();
    Node fnAssignment =  exprStmt.getFirstChild();
    Node aDotbName = fnAssignment.getFirstChild();
    Node aName = aDotbName.getFirstChild();
    Node bName = aName.getNext();
    Node fnNode = aDotbName.getNext();
    Node fnName = fnNode.getFirstChild();

    assertNodePosition(1, 0, fnAssignment);
    
    
    assertNodePosition(1, 0, aName);
    assertNodePosition(2, 0, bName);
    assertNodePosition(3, 0, fnNode);
    assertNodePosition(3, 8, fnName);
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testAssignmentValidation
  public void testAssignmentValidation() {
    testNoParseError("x=1");
    testNoParseError("x.y=1");
    testNoParseError("f().y=1");
    testParseError("(x||y)=1", INVALID_ASSIGNMENT_TARGET);
    testParseError("(x?y:z)=1", INVALID_ASSIGNMENT_TARGET);
    testParseError("f()=1", INVALID_ASSIGNMENT_TARGET);

    testNoParseError("x+=1");
    testNoParseError("x.y+=1");
    testNoParseError("f().y+=1");
    testParseError("(x||y)+=1", INVALID_ASSIGNMENT_TARGET);
    testParseError("(x?y:z)+=1", INVALID_ASSIGNMENT_TARGET);
    testParseError("f()+=1", INVALID_ASSIGNMENT_TARGET);

    testParseError("f()++", INVALID_INCREMENT_TARGET);
    testParseError("f()--", INVALID_DECREMENT_TARGET);
    testParseError("++f()", INVALID_INCREMENT_TARGET);
    testParseError("--f()", INVALID_DECREMENT_TARGET);
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

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseNamedType5
  public void testParseNamedType5() throws Exception {
    JSDocInfo info = parse("@type {!goog.\nBar}*/");
    assertTypeEquals(
        registry.createNamedType("goog.Bar", null, -1, -1),
        info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseNamedType6
  public void testParseNamedType6() throws Exception {
    JSDocInfo info = parse("@type {!goog.\n * Bar.\n * Baz}*/");
    assertTypeEquals(
        registry.createNamedType("goog.Bar.Baz", null, -1, -1),
        info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseNamedTypeError1
  public void testParseNamedTypeError1() throws Exception {
    
    
    parse("@type {!goog\n * .Bar} */",
        "Bad type annotation. expected closing }");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseNamedTypeError2
  public void testParseNamedTypeError2() throws Exception {
    parse("@type {!goog.\n * Bar\n * .Baz} */",
        "Bad type annotation. expected closing }");
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
    assertTypeEquals(
        BOOLEAN_OBJECT_TYPE, parse("@type {!Boolean}*/").getType());
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
    assertTypeEquals(
        RANGE_ERROR_TYPE, parse("@type {!RangeError}*/").getType());
    assertTypeEquals(
        SYNTAX_ERROR_TYPE, parse("@type {!SyntaxError}*/").getType());
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
    parse("@type {string|}*/",
        "Bad type annotation. type not recognized due to syntax error");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseUnionType11
  public void testParseUnionType11() throws Exception {
    parse("@type {(string,)}*/",
        "Bad type annotation. type not recognized due to syntax error");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseUnionType12
  public void testParseUnionType12() throws Exception {
    parse("@type {()}*/",
        "Bad type annotation. type not recognized due to syntax error");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseUnionType13
  public void testParseUnionType13() throws Exception {
    testParseType(
        "(function(this:Date),function(this:String):number)",
        "Function");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseUnionType14
  public void testParseUnionType14() throws Exception {
    testParseType(
        "(function(...[function(number):boolean]):number)|" +
        "function(this:String, string):number",
        "Function");
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
        "Bad type annotation. type not recognized due to syntax error");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseUnknownType1
  public void testParseUnknownType1() throws Exception {
    testParseType("?");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseUnknownType2
  public void testParseUnknownType2() throws Exception {
    testParseType("(?|number)", "?");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseUnknownType3
  public void testParseUnknownType3() throws Exception {
    testParseType("(number|?)", "?");
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

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseFunctionalType15
  public void testParseFunctionalType15() throws Exception {
    testParseType("function (?, string): boolean");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseFunctionalType16
  public void testParseFunctionalType16() throws Exception {
    testParseType("function (string, ?): ?");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseFunctionalType17
  public void testParseFunctionalType17() throws Exception {
    testParseType("(function (?): ?|number)");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseFunctionalType18
  public void testParseFunctionalType18() throws Exception {
    testParseType("function (?): (?|number)", "function (?): ?");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseFunctionalType19
  public void testParseFunctionalType19() throws Exception {
    testParseType(
        "function(...[?]): void",
        "function (...[?]): undefined");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testStructuralConstructor
  public void testStructuralConstructor() throws Exception {
    JSType type = testParseType(
        "function (new:Object)", "function (new:Object): ?");
    assertTrue(type.isConstructor());
    assertFalse(type.isNominalConstructor());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testNominalConstructor
  public void testNominalConstructor() throws Exception {
    ObjectType type = testParseType("Array", "(Array|null)").dereference();
    assertTrue(type.getConstructor().isNominalConstructor());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testBug1419535
  public void testBug1419535() throws Exception {
    parse("@type {function(Object, string, *)?} */");
    parse("@type {function(Object, string, *)|null} */");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testIssue477
  public void testIssue477() throws Exception {
    parse("@type function */",
        "Bad type annotation. missing opening (");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testMalformedThisAnnotation
  public void testMalformedThisAnnotation() throws Exception {
    parse("@this */",
        "Bad type annotation. type not recognized due to syntax error");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseFunctionalTypeError1
  public void testParseFunctionalTypeError1() throws Exception {
    parse("@type {function number):string}*/",
        "Bad type annotation. missing opening (");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseFunctionalTypeError2
  public void testParseFunctionalTypeError2() throws Exception {
    parse("@type {function( number}*/",
        "Bad type annotation. missing closing )");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseFunctionalTypeError3
  public void testParseFunctionalTypeError3() throws Exception {
    parse("@type {function(...[number], string)}*/",
        "Bad type annotation. variable length argument must be last");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseFunctionalTypeError4
  public void testParseFunctionalTypeError4() throws Exception {
    parse("@type {function(string, ...[number], boolean):string}*/",
        "Bad type annotation. variable length argument must be last");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseFunctionalTypeError5
  public void testParseFunctionalTypeError5() throws Exception {
    parse("@type {function (thi:Array)}*/",
        "Bad type annotation. missing closing )");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseFunctionalTypeError6
  public void testParseFunctionalTypeError6() throws Exception {
    resolve(parse("@type {function (this:number)}*/").getType(),
        "this type must be an object type");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseFunctionalTypeError7
  public void testParseFunctionalTypeError7() throws Exception {
    parse("@type {function(...[number)}*/",
        "Bad type annotation. missing closing ]");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseFunctionalTypeError8
  public void testParseFunctionalTypeError8() throws Exception {
    parse("@type {function(...number])}*/",
        "Bad type annotation. missing opening [");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseFunctionalTypeError9
  public void testParseFunctionalTypeError9() throws Exception {
    parse("@type {function (new:Array, this:Object)} */",
        "Bad type annotation. missing closing )");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseFunctionalTypeError10
  public void testParseFunctionalTypeError10() throws Exception {
    parse("@type {function (this:Array, new:Object)} */",
        "Bad type annotation. missing closing )");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseFunctionalTypeError11
  public void testParseFunctionalTypeError11() throws Exception {
    parse("@type {function (Array, new:Object)} */",
        "Bad type annotation. missing closing )");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseFunctionalTypeError12
  public void testParseFunctionalTypeError12() throws Exception {
    resolve(parse("@type {function (new:number)}*/").getType(),
        "constructed type must be an object type");
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
    parse("@type {[number}*/",
        "Bad type annotation. missing closing ]");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseArrayTypeError2
  public void testParseArrayTypeError2() throws Exception {
    parse("@type {number]}*/",
        "Bad type annotation. expected closing }");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseArrayTypeError3
  public void testParseArrayTypeError3() throws Exception {
    parse("@type {[(number,boolean,Object?])]}*/",
        "Bad type annotation. missing closing )");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseArrayTypeError4
  public void testParseArrayTypeError4() throws Exception {
    parse("@type {(number,boolean,[Object?)]}*/",
        "Bad type annotation. missing closing ]");
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
    assertTypeEquals(
        createNullableType(
            registry.createNamedType(
                "foo.Hello.World", null, -1, -1)),
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
        registry.createNamedType("goog.foo.Bar", null, -1, -1),
        info.getThisType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseThisType2
  public void testParseThisType2() throws Exception {
    JSDocInfo info =
        parse("@this goog.foo.Bar*/");
    assertTypeEquals(
        registry.createNamedType("goog.foo.Bar", null, -1, -1),
        info.getThisType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseThisType3
  public void testParseThisType3() throws Exception {
    parse("@type {number}\n@this goog.foo.Bar*/",
        "Bad type annotation. type annotation incompatible " +
        "with other annotations");
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
        "Bad type annotation. expected closing }",
        "Bad type annotation. expecting a variable name in a @param tag");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseParam10
  public void testParseParam10() throws Exception {
    parse("@param {...number index */",
        "Bad type annotation. expected closing }");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseParam11
  public void testParseParam11() throws Exception {
    parse("@param {number= index */",
        "Bad type annotation. expected closing }");
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
    JSDocInfo info = parse("@param {string} [index */",
        "Bad type annotation. missing closing ]");
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

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseParam20
  public void testParseParam20() throws Exception {
    JSDocInfo info = parse("@param {?=} index */");
    assertEquals(1, info.getParameterCount());
    assertTypeEquals(
        UNKNOWN_TYPE, info.getParameterType("index"));
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseParam21
  public void testParseParam21() throws Exception {
    JSDocInfo info = parse("@param {...?} index */");
    assertEquals(1, info.getParameterCount());
    assertTypeEquals(
        UNKNOWN_TYPE, info.getParameterType("index"));
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
              "Bad type annotation. expected closing }");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseRecordType12
  public void testParseRecordType12() throws Exception {
    parseFull("",
              "Bad type annotation. type not recognized due to syntax error");
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
        "Bad type annotation. expecting a variable name in a @param tag");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseParamError2
  public void testParseParamError2() throws Exception {
    parseFull("",
        "Bad type annotation. expecting a variable name in a @param tag");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseParamError3
  public void testParseParamError3() throws Exception {
    parseFull("",
        "Bad type annotation. expecting a variable name in a @param tag");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseParamError4
  public void testParseParamError4() throws Exception {
    parseFull("",
        "Bad type annotation. expecting a variable name in a @param tag");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseParamError5
  public void testParseParamError5() throws Exception {
    parse("@param {number} x \n * @param {string} x */",
        "Bad type annotation. duplicate variable name \"x\"");
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
        registry.createNamedType(
            "com.google.Foo.Bar.Hello.World", null, -1, -1),
        info.getBaseType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseExtendsGenerics
  public void testParseExtendsGenerics() throws Exception {
    JSDocInfo info =
        parse("@extends com.google.Foo.Bar.Hello.World.<Boolean,number>*/");
    assertTypeEquals(
        registry.createNamedType(
            "com.google.Foo.Bar.Hello.World", null, -1, -1),
        info.getBaseType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseImplementsGenerics
  public void testParseImplementsGenerics() throws Exception {
    
    List<JSTypeExpression> interfaces =
        parse("@implements {SomeInterface.<*>} */")
        .getImplementedInterfaces();
    assertEquals(1, interfaces.size());
    assertTypeEquals(registry.createNamedType("SomeInterface", null, -1, -1),
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
        parse("@extends {String*/",
              "Bad type annotation. expected closing }").getBaseType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseExtends6
  public void testParseExtends6() throws Exception {
    
    assertTypeEquals(STRING_OBJECT_TYPE,
        parse("@extends \n * {String}*/").getBaseType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseExtendsInvalidName
  public void testParseExtendsInvalidName() throws Exception {
    
    
    
    
    assertTypeEquals(
        registry.createNamedType("some_++#%$%_UglyString", null, -1, -1),
        parse("@extends {some_++#%$%_UglyString} */").getBaseType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseExtendsNullable1
  public void testParseExtendsNullable1() throws Exception {
    parse("@extends {Base?} */", "Bad type annotation. expected closing }");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseExtendsNullable2
  public void testParseExtendsNullable2() throws Exception {
    parse("@extends Base? */",
        "Bad type annotation. expected end of line or comment");
  }

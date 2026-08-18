// buggy code
  public void visit(NodeTraversal t, Node n, Node parent) {
    JSType childType;
    JSType leftType, rightType;
    Node left, right;
    // To be explicitly set to false if the node is not typeable.
    boolean typeable = true;

    switch (n.getType()) {
      case Token.NAME:
        typeable = visitName(t, n, parent);
        break;

      case Token.LP:
        // If this is under a FUNCTION node, it is a parameter list and can be
        // ignored here.
        if (parent.getType() != Token.FUNCTION) {
          ensureTyped(t, n, getJSType(n.getFirstChild()));
        } else {
          typeable = false;
        }
        break;

      case Token.COMMA:
        ensureTyped(t, n, getJSType(n.getLastChild()));
        break;

      case Token.TRUE:
      case Token.FALSE:
        ensureTyped(t, n, BOOLEAN_TYPE);
        break;

      case Token.THIS:
        ensureTyped(t, n, t.getScope().getTypeOfThis());
        break;

      case Token.REF_SPECIAL:
        ensureTyped(t, n);
        break;

      case Token.GET_REF:
        ensureTyped(t, n, getJSType(n.getFirstChild()));
        break;

      case Token.NULL:
        ensureTyped(t, n, NULL_TYPE);
        break;

      case Token.NUMBER:
        ensureTyped(t, n, NUMBER_TYPE);
        break;

      case Token.STRING:
        // Object literal keys are handled with OBJECTLIT
        if (!NodeUtil.isObjectLitKey(n, n.getParent())) {
          ensureTyped(t, n, STRING_TYPE);
          // Object literal keys are not typeable
        }
        break;

      case Token.GET:
      case Token.SET:
        // Object literal keys are handled with OBJECTLIT
        break;

      case Token.ARRAYLIT:
        ensureTyped(t, n, ARRAY_TYPE);
        break;

      case Token.REGEXP:
        ensureTyped(t, n, REGEXP_TYPE);
        break;

      case Token.GETPROP:
        visitGetProp(t, n, parent);
        typeable = !(parent.getType() == Token.ASSIGN &&
                     parent.getFirstChild() == n);
        break;

      case Token.GETELEM:
        visitGetElem(t, n);
        // The type of GETELEM is always unknown, so no point counting that.
        // If that unknown leaks elsewhere (say by an assignment to another
        // variable), then it will be counted.
        typeable = false;
        break;

      case Token.VAR:
        visitVar(t, n);
        typeable = false;
        break;

      case Token.NEW:
        visitNew(t, n);
        typeable = true;
        break;

      case Token.CALL:
        visitCall(t, n);
        typeable = !NodeUtil.isExpressionNode(parent);
        break;

      case Token.RETURN:
        visitReturn(t, n);
        typeable = false;
        break;

      case Token.DEC:
      case Token.INC:
        left = n.getFirstChild();
        validator.expectNumber(
            t, left, getJSType(left), "increment/decrement");
        ensureTyped(t, n, NUMBER_TYPE);
        break;

      case Token.NOT:
        ensureTyped(t, n, BOOLEAN_TYPE);
        break;

      case Token.VOID:
        ensureTyped(t, n, VOID_TYPE);
        break;

      case Token.TYPEOF:
        ensureTyped(t, n, STRING_TYPE);
        break;

      case Token.BITNOT:
        childType = getJSType(n.getFirstChild());
        if (!childType.matchesInt32Context()) {
          report(t, n, BIT_OPERATION, NodeUtil.opToStr(n.getType()),
              childType.toString());
        }
        ensureTyped(t, n, NUMBER_TYPE);
        break;

      case Token.POS:
      case Token.NEG:
        left = n.getFirstChild();
        validator.expectNumber(t, left, getJSType(left), "sign operator");
        ensureTyped(t, n, NUMBER_TYPE);
        break;

      case Token.EQ:
      case Token.NE: {
        leftType = getJSType(n.getFirstChild());
        rightType = getJSType(n.getLastChild());

        JSType leftTypeRestricted = leftType.restrictByNotNullOrUndefined();
        JSType rightTypeRestricted = rightType.restrictByNotNullOrUndefined();
        TernaryValue result =
            leftTypeRestricted.testForEquality(rightTypeRestricted);
        if (result != TernaryValue.UNKNOWN) {
          if (n.getType() == Token.NE) {
            result = result.not();
          }
          report(t, n, DETERMINISTIC_TEST, leftType.toString(),
              rightType.toString(), result.toString());
        }
        ensureTyped(t, n, BOOLEAN_TYPE);
        break;
      }

      case Token.SHEQ:
      case Token.SHNE: {
        leftType = getJSType(n.getFirstChild());
        rightType = getJSType(n.getLastChild());

        JSType leftTypeRestricted = leftType.restrictByNotNullOrUndefined();
        JSType rightTypeRestricted = rightType.restrictByNotNullOrUndefined();
        if (!leftTypeRestricted.canTestForShallowEqualityWith(
                rightTypeRestricted)) {
          report(t, n, DETERMINISTIC_TEST_NO_RESULT, leftType.toString(),
              rightType.toString());
        }
        ensureTyped(t, n, BOOLEAN_TYPE);
        break;
      }

      case Token.LT:
      case Token.LE:
      case Token.GT:
      case Token.GE:
        leftType = getJSType(n.getFirstChild());
        rightType = getJSType(n.getLastChild());
        if (rightType.isNumber()) {
          validator.expectNumber(
              t, n, leftType, "left side of numeric comparison");
        } else if (leftType.isNumber()) {
          validator.expectNumber(
              t, n, rightType, "right side of numeric comparison");
        } else if (leftType.matchesNumberContext() &&
                   rightType.matchesNumberContext()) {
          // OK.
        } else {
          // Whether the comparison is numeric will be determined at runtime
          // each time the expression is evaluated. Regardless, both operands
          // should match a string context.
          String message = "left side of comparison";
          validator.expectString(t, n, leftType, message);
          validator.expectNotNullOrUndefined(
              t, n, leftType, message, getNativeType(STRING_TYPE));
          message = "right side of comparison";
          validator.expectString(t, n, rightType, message);
          validator.expectNotNullOrUndefined(
              t, n, rightType, message, getNativeType(STRING_TYPE));
        }
        ensureTyped(t, n, BOOLEAN_TYPE);
        break;

      case Token.IN:
        left = n.getFirstChild();
        right = n.getLastChild();
        leftType = getJSType(left);
        rightType = getJSType(right);
        validator.expectObject(t, n, rightType, "'in' requires an object");
        validator.expectString(t, left, leftType, "left side of 'in'");
        ensureTyped(t, n, BOOLEAN_TYPE);
        break;

      case Token.INSTANCEOF:
        left = n.getFirstChild();
        right = n.getLastChild();
        leftType = getJSType(left);
        rightType = getJSType(right).restrictByNotNullOrUndefined();

        validator.expectAnyObject(
            t, left, leftType, "deterministic instanceof yields false");
        validator.expectActualObject(
            t, right, rightType, "instanceof requires an object");
        ensureTyped(t, n, BOOLEAN_TYPE);
        break;

      case Token.ASSIGN:
        visitAssign(t, n);
        typeable = false;
        break;

      case Token.ASSIGN_LSH:
      case Token.ASSIGN_RSH:
      case Token.ASSIGN_URSH:
      case Token.ASSIGN_DIV:
      case Token.ASSIGN_MOD:
      case Token.ASSIGN_BITOR:
      case Token.ASSIGN_BITXOR:
      case Token.ASSIGN_BITAND:
      case Token.ASSIGN_SUB:
      case Token.ASSIGN_ADD:
      case Token.ASSIGN_MUL:
      case Token.LSH:
      case Token.RSH:
      case Token.URSH:
      case Token.DIV:
      case Token.MOD:
      case Token.BITOR:
      case Token.BITXOR:
      case Token.BITAND:
      case Token.SUB:
      case Token.ADD:
      case Token.MUL:
        visitBinaryOperator(n.getType(), t, n);
        break;

      case Token.DELPROP:
        if (!isReference(n.getFirstChild())) {
          report(t, n, BAD_DELETE);
        }
        ensureTyped(t, n, BOOLEAN_TYPE);
        break;

      case Token.CASE:
        JSType switchType = getJSType(parent.getFirstChild());
        JSType caseType = getJSType(n.getFirstChild());
        validator.expectSwitchMatchesCase(t, n, switchType, caseType);
        typeable = false;
        break;

      case Token.WITH: {
        Node child = n.getFirstChild();
        childType = getJSType(child);
        validator.expectObject(
            t, child, childType, "with requires an object");
        typeable = false;
        break;
      }

      case Token.FUNCTION:
        visitFunction(t, n);
        break;

      // These nodes have no interesting type behavior.
      case Token.LABEL:
      case Token.LABEL_NAME:
      case Token.SWITCH:
      case Token.BREAK:
      case Token.CATCH:
      case Token.TRY:
      case Token.SCRIPT:
      case Token.EXPR_RESULT:
      case Token.BLOCK:
      case Token.EMPTY:
      case Token.DEFAULT:
      case Token.CONTINUE:
      case Token.DEBUGGER:
      case Token.THROW:
        typeable = false;
        break;

      // These nodes require data flow analysis.
      case Token.DO:
      case Token.FOR:
      case Token.IF:
      case Token.WHILE:
        typeable = false;
        break;

      // These nodes are typed during the type inference.
      case Token.AND:
      case Token.HOOK:
      case Token.OBJECTLIT:
      case Token.OR:
        if (n.getJSType() != null) { // If we didn't run type inference.
          ensureTyped(t, n);
        } else {
          // If this is an enum, then give that type to the objectlit as well.
          if ((n.getType() == Token.OBJECTLIT)
              && (parent.getJSType() instanceof EnumType)) {
            ensureTyped(t, n, parent.getJSType());
          } else {
            ensureTyped(t, n);
          }
        }
        if (n.getType() == Token.OBJECTLIT) {
          for (Node key : n.children()) {
            visitObjLitKey(t, key, n);
          }
        }
        break;

      default:
        report(t, n, UNEXPECTED_TOKEN, Token.name(n.getType()));
        ensureTyped(t, n);
        break;
    }

    // Don't count externs since the user's code may not even use that part.
    typeable = typeable && !inExterns;

    if (typeable) {
      doPercentTypedAccounting(t, n);
    }

    checkNoTypeCheckSection(n, false);
  }

// relevant test
// com.google.javascript.jscomp.InlineFunctionsTest::testFunctionExpressionCallInlining3
  public void testFunctionExpressionCallInlining3() {
    test("var a = (function(){return foo(this)}).call(this)",
         "var a = foo(this)");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testFunctionExpressionCallInlining4
  public void testFunctionExpressionCallInlining4() {
    test("var a; a = 1 + (function(){return foo(this)}).call(this)",
         "var a; a = 1 + foo(this)");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testFunctionExpressionCallInlining5
  public void testFunctionExpressionCallInlining5() {
    test("a:(function(){return foo()})()",
         "a:foo()");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testFunctionExpressionCallInlining6
  public void testFunctionExpressionCallInlining6() {
    test("a:(function(){return foo()}).call(this)",
         "a:foo()");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testFunctionExpressionCallInlining7
  public void testFunctionExpressionCallInlining7() {
    test("a:(function(){})()",
         "a:void 0");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testFunctionExpressionCallInlining8
  public void testFunctionExpressionCallInlining8() {
    test("a:(function(){}).call(this)",
         "a:void 0");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testFunctionExpressionCallInlining9
  public void testFunctionExpressionCallInlining9() {
    
    test("(function foo(){})()",
         "void 0");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testFunctionExpressionCallInlining10
  public void testFunctionExpressionCallInlining10() {
    
    test("(function foo(){}).call(this)",
         "void 0");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testFunctionExpressionCallInlining11a
  public void testFunctionExpressionCallInlining11a() {
    
    test("((function(){return function(){foo()}})())();", "{foo()}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testFunctionExpressionCallInlining11b
  public void testFunctionExpressionCallInlining11b() {
    
    testSame("((function(){var a; return function(){foo()}})())();");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testFunctionExpressionCallInlining11c
  public void testFunctionExpressionCallInlining11c() {
    
    testSame("function _x() {" +
                "((function(){return function(){foo()}})())();" +
                "}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testFunctionExpressionCallInlining12
  public void testFunctionExpressionCallInlining12() {
    
    testSame("(function foo(){foo()})()");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testFunctionExpressionOmega
  public void testFunctionExpressionOmega() {
    
    test("(function (f){f(f)})(function(f){f(f)})",
         "{var f$$inline_1=function(f$$1){f$$1(f$$1)};" +
          "{{f$$inline_1(f$$inline_1)}}}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testLocalFunctionInlining1
  public void testLocalFunctionInlining1() {
    test("function _f(){ function g() {} g() }",
         "function _f(){ void 0 }");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testLocalFunctionInlining2
  public void testLocalFunctionInlining2() {
    test("function _f(){ function g() {foo(); bar();} g() }",
         "function _f(){ {foo(); bar();} }");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testLocalFunctionInlining3
  public void testLocalFunctionInlining3() {
    test("function _f(){ function g() {foo(); bar();} g() }",
         "function _f(){ {foo(); bar();} }");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testLocalFunctionInlining4
  public void testLocalFunctionInlining4() {
    test("function _f(){ function g() {return 1} return g() }",
         "function _f(){ return 1 }");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testLocalFunctionInlining5
  public void testLocalFunctionInlining5() {
    testSame("function _f(){ function g() {this;} g() }");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testLocalFunctionInlining6
  public void testLocalFunctionInlining6() {
    testSame("function _f(){ function g() {this;} return g; }");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testLocalFunctionInliningOnly1
  public void testLocalFunctionInliningOnly1() {
    this.allowGlobalFunctionInlining = true;
    test("function f(){} f()", "void 0;");
    this.allowGlobalFunctionInlining = false;
    testSame("function f(){} f()");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testLocalFunctionInliningOnly2
  public void testLocalFunctionInliningOnly2() {
    this.allowGlobalFunctionInlining = false;
    testSame("function f(){} f()");

    test("function f(){ function g() {return 1} return g() }; f();",
         "function f(){ return 1 }; f();");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testLocalFunctionInliningOnly3
  public void testLocalFunctionInliningOnly3() {
    this.allowGlobalFunctionInlining = false;
    testSame("function f(){} f()");

    test("(function(){ function g() {return 1} return g() })();",
         "(function(){ return 1 })();");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testLocalFunctionInliningOnly4
  public void testLocalFunctionInliningOnly4() {
    this.allowGlobalFunctionInlining = false;
    testSame("function f(){} f()");

    test("(function(){ return (function() {return 1})() })();",
         "(function(){ return 1 })();");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testFunctionExpressionYCombinator
  public void testFunctionExpressionYCombinator() {
    testSame(
        "var factorial = ((function(M) {\n" +
        "      return ((function(f) {\n" +
        "                 return M(function(arg) {\n" +
        "                            return (f(f))(arg);\n" +
        "                            })\n" +
        "               })\n" +
        "              (function(f) {\n" +
        "                 return M(function(arg) {\n" +
        "                            return (f(f))(arg);\n" +
        "                           })\n" +
        "                 }));\n" +
        "     })\n" +
        "    (function(f) {\n" +
        "       return function(n) {\n" +
        "        if (n === 0)\n" +
        "          return 1;\n" +
        "        else\n" +
        "          return n * f(n - 1);\n" +
        "       };\n" +
        "     }));\n" +
        "\n" +
        "factorial(5)\n");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testRenamePropertyFunction
  public void testRenamePropertyFunction() {
    testSame("function JSCompiler_renameProperty(x) {return x} " +
             "JSCompiler_renameProperty('foo')");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testReplacePropertyFunction
  public void testReplacePropertyFunction() {
    
    
    test("function f(x) {return x} " +
         "foo(window, f); f(1)",
         "function f(x) {return x} " +
         "foo(window, f); 1");
    
    
    testSame("function f(x) {return x} " +
             "new JSCompiler_ObjectPropertyString(window, f); f(1)");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testIssue423
  public void testIssue423() {
    test(
        "(function($) {\n" +
        "  $.fn.multicheck = function(options) {\n" +
        "    initialize.call(this, options);\n" +
        "  };\n" +
        "\n" +
        "  function initialize(options) {\n" +
        "    options.checkboxes = $(this).siblings(':checkbox');\n" +
        "    preload_check_all.call(this);\n" +
        "  }\n" +
        "\n" +
        "  function preload_check_all() {\n" +
        "    $(this).data('checkboxes');\n" +
        "  }\n" +
        "})(jQuery)",
        "(function($){" +
        "  $.fn.multicheck=function(options$$1){" +
        "    {" +
        "     options$$1.checkboxes=$(this).siblings(\":checkbox\");" +
        "     {" +
        "       $(this).data(\"checkboxes\")" +
        "     }" +
        "    }" +
        "  }" +
        "})(jQuery)");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testCrossModuleInlining1
  public void testCrossModuleInlining1() {
    test(createModuleChain(
             
             "function foo(){return f(1)+g(2)+h(3);}",
             
             "foo()"
             ),
         new String[] {
             
             "",
             
             "f(1)+g(2)+h(3);"
            }
        );
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testCrossModuleInlining2
  public void testCrossModuleInlining2() {
    testSame(createModuleChain(
                
                "foo()",
                
                "function foo(){return f(1)+g(2)+h(3);}"
                )
            );

    test(createModuleChain(
             
             "foo()",
             
             "function foo(){return f();}"
             ),
         new String[] {
             
             "f();",
             
             ""
            }
        );
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testCrossModuleInlining3
  public void testCrossModuleInlining3() {
    testSame(createModuleChain(
                
                "foo()",
                
                "function foo(){return f(1)+g(2)+h(3);}",
                
                "foo()"
                )
            );

    test(createModuleChain(
             
             "foo()",
             
             "function foo(){return f();}",
             
             "foo()"
             ),
         new String[] {
             
             "f();",
             
             "",
             
             "f();"
            }
         );
  }

// com.google.javascript.jscomp.InlineObjectLiteralsTest::testObject1
  public void testObject1() {}

// com.google.javascript.jscomp.InlineObjectLiteralsTest::testObject2
  public void testObject2() {}

// com.google.javascript.jscomp.InlineObjectLiteralsTest::testObject3
  public void testObject3() {
    
    
    testSame("var a = {y:y,x:x}; a.y(); f(a.x);");
  }

// com.google.javascript.jscomp.InlineObjectLiteralsTest::testObject4
  public void testObject4() {
    
    testSame("var a = {y:y}; a.x = z; f(a.x, a.y); g(a);");
  }

// com.google.javascript.jscomp.InlineObjectLiteralsTest::testObject5
  public void testObject5() {
    test("var a = {x:x, y:y}; var b = {a:a}; f(b.a.x, b.a.y);",
         "var a = {x:x, y:y};" +
         "var JSCompiler_object_inline_a_0=a;" +
         "f(JSCompiler_object_inline_a_0.x, JSCompiler_object_inline_a_0.y);");
  }

// com.google.javascript.jscomp.InlineObjectLiteralsTest::testObject6
  public void testObject6() {
    test("for (var i = 0; i < 5; i++) { var a = {i:i,x:x}; f(a.i, a.x); }",
         "for (var i = 0; i < 5; i++) {" +
         "  var JSCompiler_object_inline_x_1=x;" +
         "  var JSCompiler_object_inline_i_0=i;" +
         "  f(JSCompiler_object_inline_i_0,JSCompiler_object_inline_x_1)" +
         "}");
    test("if (c) { var a = {i:i,x:x}; f(a.i, a.x); }",
         "if (c) {" +
         "  var JSCompiler_object_inline_x_1=x;" +
         "  var JSCompiler_object_inline_i_0=i;" +
         "  f(JSCompiler_object_inline_i_0,JSCompiler_object_inline_x_1)" +
         "}");
  }

// com.google.javascript.jscomp.InlineObjectLiteralsTest::testObject7
  public void testObject7() {}

// com.google.javascript.jscomp.InlineObjectLiteralsTest::testObject8
  public void testObject8() {}

// com.google.javascript.jscomp.InlineObjectLiteralsTest::testObject9
  public void testObject9() {}

// com.google.javascript.jscomp.InlineObjectLiteralsTest::testObject10
  public void testObject10() {}

// com.google.javascript.jscomp.InlineObjectLiteralsTest::testObject11
  public void testObject11() {
    testSame("var x = {a:b}; (x = {a:a}).c = 5; f(x.a);");
    testSame("var x = {a:a}; f(x[a]); g(x[a]);");
  }

// com.google.javascript.jscomp.InlineObjectLiteralsTest::testObject12
  public void testObject12() {}

// com.google.javascript.jscomp.InlineObjectLiteralsTest::testObject13
  public void testObject13() {
    testSame("var x = {a:1, b:2}; x = {a:3, b:x.a};");
  }

// com.google.javascript.jscomp.InlineObjectLiteralsTest::testObject14
  public void testObject14() {
    testSame("var x = {a:1}; if ('a' in x) { f(); }");
    testSame("var x = {a:1}; for (var y in x) { f(y); }");
  }

// com.google.javascript.jscomp.InlineObjectLiteralsTest::testObject15
  public void testObject15() {
    testSame("x = x || {}; f(x.a);");
  }

// com.google.javascript.jscomp.InlineObjectLiteralsTest::testObject16
  public void testObject16() {
    test("function f(e) { bar(); x = {a: foo()}; var x; print(x.a); }",
         "function f(e) { " +
         "  var JSCompiler_object_inline_a_0;" +
         "  bar();" +
         "  JSCompiler_object_inline_a_0 = foo(), true;" +
         "  print(JSCompiler_object_inline_a_0);" +
         "}");
  }

// com.google.javascript.jscomp.InlineObjectLiteralsTest::testObject17
  public void testObject17() {
    
    
    testSame(
      "var a = {a: function(){}};" +
      "a.a();" +
      "a = {a1: 100};" +
      "print(a.a1);");
  }

// com.google.javascript.jscomp.InlineObjectLiteralsTest::testObject18
  public void testObject18() {
    testSame("var a,b; b=a={x:x, y:y}; f(b.x);");
  }

// com.google.javascript.jscomp.InlineObjectLiteralsTest::testObject19
  public void testObject19() {
    testSame("var a,b; if(c) { b=a={x:x, y:y}; } else { b=a={x:y}; } f(b.x);");
  }

// com.google.javascript.jscomp.InlineObjectLiteralsTest::testObject20
  public void testObject20() {
    testSame("var a,b; if(c) { b=a={x:x, y:y}; } else { b=a={x:y}; } f(a.x);");
  }

// com.google.javascript.jscomp.InlineObjectLiteralsTest::testObject21
  public void testObject21() {
    testSame("var a,b; b=a={x:x, y:y};");
    testSame("var a,b; if(c) { b=a={x:x, y:y}; }" +
             "else { b=a={x:y}; } f(a.x); f(b.x)");
    testSame("var a, b; if(c) { if (a={x:x, y:y}) f(); } " +
             "else { b=a={x:y}; } f(a.x);");
    testSame("var a,b; b = (a = {x:x, y:x});");
    testSame("var a,b; a = {x:x, y:x}; b = a");
    testSame("var a,b; a = {x:x, y:x}; b = x || a");
    testSame("var a,b; a = {x:x, y:x}; b = y && a");
    testSame("var a,b; a = {x:x, y:x}; b = y ? a : a");
    testSame("var a,b; a = {x:x, y:x}; b = y , a");
    testSame("b = x || (a = {x:1, y:2});");
  }

// com.google.javascript.jscomp.InlineObjectLiteralsTest::testObject22
  public void testObject22() {}

// com.google.javascript.jscomp.InlineSimpleMethodsTest::testSimpleInline1
  public void testSimpleInline1() {
    testWithPrefix("function Foo(){}" +
        "Foo.prototype.bar=function(){return this.baz};",
        "var x=(new Foo).bar();var y=(new Foo).bar();",
        "var x=(new Foo).baz;var y=(new Foo).baz");
  }

// com.google.javascript.jscomp.InlineSimpleMethodsTest::testSimpleInline2
  public void testSimpleInline2() {
    testWithPrefix("function Foo(){}" +
        "Foo.prototype={bar:function(){return this.baz}};",
        "var x=(new Foo).bar();var y=(new Foo).bar();",
        "var x=(new Foo).baz;var y=(new Foo).baz");
  }

// com.google.javascript.jscomp.InlineSimpleMethodsTest::testSimpleGetterInline1
  public void testSimpleGetterInline1() {
    
    testSame("function Foo(){}" +
      "Foo.prototype={get bar(){return this.baz}};" +
      "var x=(new Foo).bar;var y=(new Foo).bar");
    
    
    testSame("function Foo(){}" +
      "Foo.prototype={get bar(){return this.baz}};" +
      "var x=(new Foo).bar();var y=(new Foo).bar()");
  }

// com.google.javascript.jscomp.InlineSimpleMethodsTest::testSimpleSetterInline1
  public void testSimpleSetterInline1() {
    
    testSame("function Foo(){}" +
      "Foo.prototype={set bar(a){return this.baz}};" +
      "var x=(new Foo).bar;var y=(new Foo).bar");
    testSame("function Foo(){}" +
      "Foo.prototype={set bar(a){return this.baz}};" +
      "var x=(new Foo).bar();var y=(new Foo).bar()");
  }

// com.google.javascript.jscomp.InlineSimpleMethodsTest::testSelfInline
  public void testSelfInline() {
    testWithPrefix("function Foo(){}" +
        "Foo.prototype.bar=function(){return this.baz};",
        "Foo.prototype.meth=function(){this.bar();}",
        "Foo.prototype.meth=function(){this.baz}");
  }

// com.google.javascript.jscomp.InlineSimpleMethodsTest::testCallWithArgs
  public void testCallWithArgs() {
    testWithPrefix("function Foo(){}" +
        "Foo.prototype.bar=function(){return this.baz};",
        "var x=(new Foo).bar(3,new Foo)",
        "var x=(new Foo).bar(3,new Foo)");
  }

// com.google.javascript.jscomp.InlineSimpleMethodsTest::testCallWithConstArgs
  public void testCallWithConstArgs() {
    testWithPrefix("function Foo(){}" +
        "Foo.prototype.bar=function(a){return this.baz};",
        "var x=(new Foo).bar(3, 4)",
        "var x=(new Foo).baz");
  }

// com.google.javascript.jscomp.InlineSimpleMethodsTest::testNestedProperties
  public void testNestedProperties() {
    testWithPrefix("function Foo(){}" +
        "Foo.prototype.bar=function(){return this.baz.ooka};",
        "(new Foo).bar()",
        "(new Foo).baz.ooka");
  }

// com.google.javascript.jscomp.InlineSimpleMethodsTest::testSkipComplexMethods
  public void testSkipComplexMethods() {
    testWithPrefix("function Foo(){}" +
        "Foo.prototype.bar=function(){return this.baz};" +
        "Foo.prototype.condy=function(){return this.baz?this.baz:1};",
        "var x=(new Foo).argy()",
        "var x=(new Foo).argy()");
  }

// com.google.javascript.jscomp.InlineSimpleMethodsTest::testSkipConflictingMethods
  public void testSkipConflictingMethods() {
    testWithPrefix("function Foo(){}" +
        "Foo.prototype.bar=function(){return this.baz};" +
        "Foo.prototype.bar=function(){return this.bazz};",
        "var x=(new Foo).bar()",
        "var x=(new Foo).bar()");
  }

// com.google.javascript.jscomp.InlineSimpleMethodsTest::testSameNamesDifferentDefinitions
  public void testSameNamesDifferentDefinitions() {
    testWithPrefix("function A(){}" +
        "A.prototype.g=function(){return this.a};" +
        "function B(){}" +
        "B.prototype.g=function(){return this.b};",
        "var x=(new A).g();" +
        "var y=(new B).g();" +
        "var a=new A;" +
        "var ag=a.g();",
        "var x=(new A).g();" +
        "var y=(new B).g();" +
        "var a=new A;" +
        "var ag=a.g()");
  }

// com.google.javascript.jscomp.InlineSimpleMethodsTest::testSameNamesSameDefinitions
  public void testSameNamesSameDefinitions() {
    testWithPrefix("function A(){}" +
        "A.prototype.g=function(){return this.a};" +
        "function B(){}" +
        "B.prototype.g=function(){return this.a};",
        "var x=(new A).g();" +
        "var y=(new B).g();" +
        "var a=new A;" +
        "var ag=a.g();",
        "var x=(new A).a;" +
        "var y=(new B).a;" +
        "var a=new A;" +
        "var ag=a.a");
  }

// com.google.javascript.jscomp.InlineSimpleMethodsTest::testConfusingNames
  public void testConfusingNames() {
    testWithPrefix("function Foo(){}" +
        "Foo.prototype.bar=function(){return this.baz};",
        "function bar(){var bar=function(){};bar()}",
        "function bar(){var bar=function(){};bar()}");
  }

// com.google.javascript.jscomp.InlineSimpleMethodsTest::testConstantInline
  public void testConstantInline() {
    testWithPrefix("function Foo(){}" +
        "Foo.prototype.bar=function(){return 3};",
        "var f=new Foo;var x=f.bar()",
        "var f=new Foo;var x=3");
  }

// com.google.javascript.jscomp.InlineSimpleMethodsTest::testConstantArrayInline
  public void testConstantArrayInline() {
    testWithPrefix("function Foo(){}" +
        "Foo.prototype.bar=function(){return[3,4]};",
        "var f=new Foo;var x=f.bar()",
        "var f=new Foo;var x=[3,4]");
  }

// com.google.javascript.jscomp.InlineSimpleMethodsTest::testConstantInlineWithSideEffects
  public void testConstantInlineWithSideEffects() {
    testWithPrefix("function Foo(){}" +
        "Foo.prototype.bar=function(){return 3};",
        "var x=(new Foo).bar()",
        "var x=(new Foo).bar()");
  }

// com.google.javascript.jscomp.InlineSimpleMethodsTest::testEmptyMethodInline
  public void testEmptyMethodInline() {
    testWithPrefix("function Foo(){}" +
        "Foo.prototype.bar=function(a){};",
        "var x=new Foo; x.bar();",
        "var x=new Foo");
  }

// com.google.javascript.jscomp.InlineSimpleMethodsTest::testEmptyMethodInlineWithSideEffects
  public void testEmptyMethodInlineWithSideEffects() {
    testWithPrefix("function Foo(){}" +
        "Foo.prototype.bar=function(){};",
        "(new Foo).bar();var y=new Foo;y.bar(new Foo)",
        "(new Foo).bar();var y=new Foo;y.bar(new Foo)");
  }

// com.google.javascript.jscomp.InlineSimpleMethodsTest::testEmptyMethodInlineInAssign1
  public void testEmptyMethodInlineInAssign1() {
    testWithPrefix("function Foo(){}" +
        "Foo.prototype.bar=function(){};",
        "var x=new Foo;var y=x.bar()",
        "var x=new Foo;var y=void 0");
  }

// com.google.javascript.jscomp.InlineSimpleMethodsTest::testEmptyMethodInlineInAssign2
  public void testEmptyMethodInlineInAssign2() {
    testWithPrefix("function Foo(){}" +
        "Foo.prototype.bar=function(){};",
        "var x=new Foo;var y=x.bar().toString()",
        "var x=new Foo;var y=(void 0).toString()");
  }

// com.google.javascript.jscomp.InlineSimpleMethodsTest::testNormalMethod
  public void testNormalMethod() {
    testWithPrefix("function Foo(){}" +
        "Foo.prototype.bar=function(){var x=1};",
        "var x=new Foo;x.bar()",
        "var x=new Foo;x.bar()");
  }

// com.google.javascript.jscomp.InlineSimpleMethodsTest::testNoInlineOfExternMethods1
  public void testNoInlineOfExternMethods1() {
    testSame("var external={};external.charAt;",
        "external.charAt()", (DiagnosticType) null);
  }

// com.google.javascript.jscomp.InlineSimpleMethodsTest::testNoInlineOfExternMethods2
  public void testNoInlineOfExternMethods2() {
    testSame("var external={};external.charAt=function(){};",
        "external.charAt()", (DiagnosticType) null);
  }

// com.google.javascript.jscomp.InlineSimpleMethodsTest::testNoInlineOfExternMethods3
  public void testNoInlineOfExternMethods3() {
    testSame("var external={};external.bar=function(){};",
        "function Foo(){}Foo.prototype.bar=function(){};(new Foo).bar()",
             (DiagnosticType) null);
  }

// com.google.javascript.jscomp.InlineSimpleMethodsTest::testNoInlineOfDangerousProperty
  public void testNoInlineOfDangerousProperty() {
    testSame("function Foo(){this.bar=3}" +
        "Foo.prototype.bar=function(){};" +
        "var x=new Foo;var y=x.bar()");
  }

// com.google.javascript.jscomp.InlineSimpleMethodsTest::testNoWarn
  public void testNoWarn() {
    testSame("function Foo(){}" +
        "Foo.prototype.bar=function(opt_a,b){var x=1};" +
        "var x=new Foo;x.bar()");

    testSame("function Foo(){}" +
        "Foo.prototype.bar=function(var_args,b){var x=1};" +
        "var x=new Foo;x.bar()");
  }

// com.google.javascript.jscomp.InlineSimpleMethodsTest::testObjectLit
  public void testObjectLit() {
    testSame("Foo.prototype.bar=function(){return this.baz_};" +
             "var blah={bar:function(){}};" +
             "(new Foo).bar()");
  }

// com.google.javascript.jscomp.InlineSimpleMethodsTest::testObjectLit2
  public void testObjectLit2() {
    testSame("var blah={bar:function(){}};" +
             "(new Foo).bar()");
  }

// com.google.javascript.jscomp.InlineSimpleMethodsTest::testObjectLitExtern
  public void testObjectLitExtern() {
    String externs = "window.bridge={_sip:function(){}};";
    testSame(externs, "window.bridge._sip()", null);
  }

// com.google.javascript.jscomp.InlineSimpleMethodsTest::testExternFunction
  public void testExternFunction() {
    String externs = "function emptyFunction() {}";
    testSame(externs,
        "function Foo(){this.empty=emptyFunction}" +
        "(new Foo).empty()", null);
  }

// com.google.javascript.jscomp.InlineSimpleMethodsTest::testIssue2508576_1
  public void testIssue2508576_1() {
    
    String externs = "function alert(a) {}";
    testSame(externs, "({a:alert,b:alert}).a(\"a\")", null);
  }

// com.google.javascript.jscomp.InlineSimpleMethodsTest::testIssue2508576_2
  public void testIssue2508576_2() {
    
    testSame("({a:function(){},b:x()}).a(\"a\")");
  }

// com.google.javascript.jscomp.InlineSimpleMethodsTest::testIssue2508576_3
  public void testIssue2508576_3() {
    
    test("({a:function(){},b:alert}).a(\"a\")", "");
  }

// com.google.javascript.jscomp.InlineSimpleMethodsTest::testAnonymousGet
  public void testAnonymousGet() {
    
    testSame("({get a(){return function(){}},b:alert}).a(\"a\")");
    testSame("({get a(){},b:alert}).a(\"a\")");
    testSame("({get a(){},b:alert}).a");
  }

// com.google.javascript.jscomp.InlineSimpleMethodsTest::testAnonymousSet
  public void testAnonymousSet() {
    
    testSame("({set a(b){return function(){}},b:alert}).a(\"a\")");
    testSame("({set a(b){},b:alert}).a(\"a\")");
    testSame("({set a(b){},b:alert}).a");
  }

// com.google.javascript.jscomp.InlineVariablesConstantsTest::testInlineVariablesConstants
  public void testInlineVariablesConstants() {
    test("var ABC=2; var x = ABC;", "var x=2");
    test("var AA = 'aa'; AA;", "'aa'");
    test("var A_A=10; A_A + A_A;", "10+10");
    test("var AA=1", "");
    test("var AA; AA=1", "1");
    test("var AA; if (false) AA=1; AA;", "if (false) 1; 1;");
    testSame("var AA; if (false) AA=1; else AA=2; AA;");

    test("var AA;(function () {AA=1})()",
         "(function () {1})()");

    
    testSame("var x = AA;");

    
    testSame("var AA = '1234567890'; foo(AA); foo(AA); foo(AA);");

    test("var AA = '123456789012345';AA;",
         "'123456789012345'");
  }

// com.google.javascript.jscomp.InlineVariablesConstantsTest::testNoInlineArraysOrRegexps
  public void testNoInlineArraysOrRegexps() {
    testSame("var AA = [10,20]; AA[0]");
    testSame("var AA = [10,20]; AA.push(1); AA[0]");
    testSame("var AA = /x/; AA.test('1')");
    testSame(" var aa = /x/; aa.test('1')");
  }

// com.google.javascript.jscomp.InlineVariablesConstantsTest::testInlineVariablesConstantsJsDocStyle
  public void testInlineVariablesConstantsJsDocStyle() {
    test("var abc=2; var x = abc;", "var x=2");
    test("var aa = 'aa'; aa;", "'aa'");
    test("var a_a=10; a_a + a_a;", "10+10");
    test("var aa=1;", "");
    test("var aa; aa=1;", "1");
    test("var aa;(function () {aa=1})()", "(function () {1})()");
    test("var aa;(function () {aa=1})(); var z=aa",
         "(function () {1})(); var z=1");
    testSame("var aa;(function () {var y; aa=y})(); var z=aa");

    
    testSame("var aa = '1234567890'; foo(aa); foo(aa); foo(aa);");

    test("var aa = '123456789012345';aa;",
         "'123456789012345'");
  }

// com.google.javascript.jscomp.InlineVariablesConstantsTest::testInlineConditionallyDefinedConstant1
  public void testInlineConditionallyDefinedConstant1() {
    
    
    
    
    test("if (x) var ABC = 2; if (y) f(ABC);",
         "if (x); if (y) f(2);");
  }

// com.google.javascript.jscomp.InlineVariablesConstantsTest::testInlineConditionallyDefinedConstant2
  public void testInlineConditionallyDefinedConstant2() {
    test("if (x); else var ABC = 2; if (y) f(ABC);",
         "if (x); else; if (y) f(2);");
  }

// com.google.javascript.jscomp.InlineVariablesConstantsTest::testInlineConditionallyDefinedConstant3
  public void testInlineConditionallyDefinedConstant3() {
    test("if (x) { var ABC = 2; } if (y) { f(ABC); }",
         "if (x) {} if (y) { f(2); }");
  }

// com.google.javascript.jscomp.InlineVariablesConstantsTest::testInlineDefinedConstant
  public void testInlineDefinedConstant() {
    test(
        "\n" +
        "var aa = '1234567890';\n" +
        "foo(aa); foo(aa); foo(aa);",
        "foo('1234567890');foo('1234567890');foo('1234567890')");

    test(
        "\n" +
        "var ABC = '1234567890';\n" +
        "foo(ABC); foo(ABC); foo(ABC);",
        "foo('1234567890');foo('1234567890');foo('1234567890')");
  }

// com.google.javascript.jscomp.InlineVariablesConstantsTest::testInlineVariablesConstantsWithInlineAllStringsOn
  public void testInlineVariablesConstantsWithInlineAllStringsOn() {
    inlineAllStrings = true;
    test("var AA = '1234567890'; foo(AA); foo(AA); foo(AA);",
         "foo('1234567890'); foo('1234567890'); foo('1234567890')");
  }

// com.google.javascript.jscomp.InlineVariablesConstantsTest::testNoInlineWithoutConstDeclaration
  public void testNoInlineWithoutConstDeclaration() {
    testSame("var abc = 2; var x = abc;");
  }

// com.google.javascript.jscomp.InlineVariablesConstantsTest::testNoInlineAliases
  public void testNoInlineAliases() {
    testSame("var XXX = new Foo(); var yyy = XXX; bar(yyy)");
    testSame("var xxx = new Foo(); var YYY = xxx; bar(YYY)");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testInlineGlobal
  public void testInlineGlobal() {
    test("var x = 1; var z = x;", "var z = 1;");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testNoInlineExportedName
  public void testNoInlineExportedName() {
    testSame("var _x = 1; var z = _x;");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testNoInlineExportedName2
  public void testNoInlineExportedName2() {
    testSame("var f = function() {}; var _x = f;" +
             "var y = function() { _x(); }; var _y = f;");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testDoNotInlineIncrement
  public void testDoNotInlineIncrement() {
    testSame("var x = 1; x++;");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testDoNotInlineDecrement
  public void testDoNotInlineDecrement() {
    testSame("var x = 1; x--;");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testDoNotInlineIntoLhsOfAssign
  public void testDoNotInlineIntoLhsOfAssign() {
    testSame("var x = 1; x += 3;");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testInlineIntoRhsOfAssign
  public void testInlineIntoRhsOfAssign() {
    test("var x = 1; var y = x;", "var y = 1;");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testInlineInFunction
  public void testInlineInFunction() {
    test("function baz() { var x = 1; var z = x; }",
        "function baz() { var z = 1; }");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testInlineInFunction2
  public void testInlineInFunction2() {
    test("function baz() { " +
            "var a = new obj();"+
            "result = a;" +
         "}",
         "function baz() { " +
            "result = new obj()" +
         "}");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testInlineInFunction3
  public void testInlineInFunction3() {
    testSame(
        "function baz() { " +
           "var a = new obj();" +
           "(function(){a;})();" +
           "result = a;" +
        "}");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testInlineInFunction4
  public void testInlineInFunction4() {
    testSame(
        "function baz() { " +
           "var a = new obj();" +
           "foo.result = a;" +
        "}");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testInlineInFunction5
  public void testInlineInFunction5() {
    testSame(
        "function baz() { " +
           "var a = (foo = new obj());" +
           "foo.x();" +
           "result = a;" +
        "}");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testInlineAcrossModules
  public void testInlineAcrossModules() {
    
    test(createModules("var a = 2;", "var b = a;"),
        new String[] { "", "var b = 2;" });
  }

// com.google.javascript.jscomp.InlineVariablesTest::testDoNotExitConditional1
  public void testDoNotExitConditional1() {
    testSame("if (true) { var x = 1; } var z = x;");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testDoNotExitConditional2
  public void testDoNotExitConditional2() {
    testSame("if (true) var x = 1; var z = x;");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testDoNotExitConditional3
  public void testDoNotExitConditional3() {
    testSame("var x; if (true) x=1; var z = x;");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testDoNotExitLoop
  public void testDoNotExitLoop() {
    testSame("while (z) { var x = 3; } var y = x;");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testDoNotExitForLoop
  public void testDoNotExitForLoop() {
    test("for (var i = 1; false; false) var z = i;",
         "for (;false;false) var z = 1;");
    testSame("for (; false; false) var i = 1; var z = i;");
    testSame("for (var i in {}); var z = i;");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testDoNotEnterSubscope
  public void testDoNotEnterSubscope() {
    testSame(
        "var x = function() {" +
        "  var self = this; " +
        "  return function() { var y = self; };" +
        "}");
    testSame(
        "var x = function() {" +
        "  var y = [1]; " +
        "  return function() { var z = y; };" +
        "}");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testDoNotExitTry
  public void testDoNotExitTry() {
    testSame("try { var x = y; } catch (e) {} var z = y; ");
    testSame("try { throw e; var x = 1; } catch (e) {} var z = x; ");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testDoNotEnterCatch
  public void testDoNotEnterCatch() {
    testSame("try { } catch (e) { var z = e; } ");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testDoNotEnterFinally
  public void testDoNotEnterFinally() {
    testSame("try { throw e; var x = 1; } catch (e) {} " +
             "finally  { var z = x; } ");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testInsideIfConditional
  public void testInsideIfConditional() {
    test("var a = foo(); if (a) { alert(3); }", "if (foo()) { alert(3); }");
    test("var a; a = foo(); if (a) { alert(3); }", "if (foo()) { alert(3); }");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testOnlyReadAtInitialization
  public void testOnlyReadAtInitialization() {
    test("var a; a = foo();", "foo();");
    test("var a; if (a = foo()) { alert(3); }", "if (foo()) { alert(3); }");
    test("var a; switch (a = foo()) {}", "switch(foo()) {}");
    test("var a; function f(){ return a = foo(); }",
         "function f(){ return foo(); }");
    test("function f(){ var a; return a = foo(); }",
         "function f(){ return foo(); }");
    test("var a; with (a = foo()) { alert(3); }", "with (foo()) { alert(3); }");

    test("var a; b = (a = foo());", "b = foo();");
    test("var a; while(a = foo()) { alert(3); }",
         "while(foo()) { alert(3); }");
    test("var a; for(;a = foo();) { alert(3); }",
         "for(;foo();) { alert(3); }");
    test("var a; do {} while(a = foo()) { alert(3); }",
         "do {} while(foo()) { alert(3); }");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testImmutableWithSingleReferenceAfterInitialzation
  public void testImmutableWithSingleReferenceAfterInitialzation() {
    test("var a; a = 1;", "1;");
    test("var a; if (a = 1) { alert(3); }", "if (1) { alert(3); }");
    test("var a; switch (a = 1) {}", "switch(1) {}");
    test("var a; function f(){ return a = 1; }",
         "function f(){ return 1; }");
    test("function f(){ var a; return a = 1; }",
         "function f(){ return 1; }");
    test("var a; with (a = 1) { alert(3); }", "with (1) { alert(3); }");

    test("var a; b = (a = 1);", "b = 1;");
    test("var a; while(a = 1) { alert(3); }",
         "while(1) { alert(3); }");
    test("var a; for(;a = 1;) { alert(3); }",
         "for(;1;) { alert(3); }");
    test("var a; do {} while(a = 1) { alert(3); }",
         "do {} while(1) { alert(3); }");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testSingleReferenceAfterInitialzation
  public void testSingleReferenceAfterInitialzation() {
    test("var a; a = foo();a;", "foo();");
    testSame("var a; if (a = foo()) { alert(3); } a;");
    testSame("var a; switch (a = foo()) {} a;");
    testSame("var a; function f(){ return a = foo(); } a;");
    testSame("function f(){ var a; return a = foo(); a;}");
    testSame("var a; with (a = foo()) { alert(3); } a;");
    testSame("var a; b = (a = foo()); a;");
    testSame("var a; while(a = foo()) { alert(3); } a;");
    testSame("var a; for(;a = foo();) { alert(3); } a;");
    testSame("var a; do {} while(a = foo()) { alert(3); } a;");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testInsideIfBranch
  public void testInsideIfBranch() {
    testSame("var a = foo(); if (1) { alert(a); }");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testInsideAndConditional
  public void testInsideAndConditional() {
    test("var a = foo(); a && alert(3);", "foo() && alert(3);");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testInsideAndBranch
  public void testInsideAndBranch() {
    testSame("var a = foo(); 1 && alert(a);");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testInsideOrBranch
  public void testInsideOrBranch() {
    testSame("var a = foo(); 1 || alert(a);");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testInsideHookBranch
  public void testInsideHookBranch() {
    testSame("var a = foo(); 1 ? alert(a) : alert(3)");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testInsideHookConditional
  public void testInsideHookConditional() {
    test("var a = foo(); a ? alert(1) : alert(3)",
         "foo() ? alert(1) : alert(3)");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testInsideOrBranchInsideIfConditional
  public void testInsideOrBranchInsideIfConditional() {
    testSame("var a = foo(); if (x || a) {}");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testInsideOrBranchInsideIfConditionalWithConstant
  public void testInsideOrBranchInsideIfConditionalWithConstant() {
    
    testSame("var a = [false]; if (x || a) {}");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testCrossFunctionsAsLeftLeaves
  public void testCrossFunctionsAsLeftLeaves() {
    
    test(
        new String[] { "var x = function() {};", "",
            "function cow() {} var z = x;"},
        new String[] { "", "", "function cow() {} var z = function() {};" });
    test(
        new String[] { "var x = function() {};", "",
            "var cow = function() {}; var z = x;"},
        new String[] { "", "",
            "var cow = function() {}; var z = function() {};" });
    testSame(
        new String[] { "var x = a;", "",
            "(function() { a++; })(); var z = x;"});
    test(
        new String[] { "var x = a;", "",
            "function cow() { a++; }; cow(); var z = x;"},
        new String[] { "var x = a;", "",
            ";(function cow(){ a++; })(); var z = x;"});
    testSame(
        new String[] { "var x = a;", "",
            "cow(); var z = x; function cow() { a++; };"});
  }

// com.google.javascript.jscomp.InlineVariablesTest::testDoCrossFunction
  public void testDoCrossFunction() {
    
    
    test("var x = 1; foo(); var z = x;", "foo(); var z = 1;");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testDoNotCrossReferencingFunction
  public void testDoNotCrossReferencingFunction() {
    testSame(
        "var f = function() { var z = x; };" +
        "var x = 1;" +
        "f();" +
        "var z = x;" +
        "f();");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testChainedAssignment
  public void testChainedAssignment() {
    test("var a = 2, b = 2; var c = b;", "var a = 2; var c = 2;");
    test("var a = 2, b = 2; var c = a;", "var b = 2; var c = 2;");
    test("var a = b = 2; var f = 3; var c = a;", "var f = 3; var c = b = 2;");
    testSame("var a = b = 2; var c = b;");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testForIn
  public void testForIn() {
    testSame("for (var i in j) { var c = i; }");
    testSame("var i = 0; for (i in j) ;");
    testSame("var i = 0; for (i in j) { var c = i; }");
    testSame("i = 0; for (var i in j) { var c = i; }");
    testSame("var j = {'key':'value'}; for (var i in j) {print(i)};");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testDoCrossNewVariables
  public void testDoCrossNewVariables() {
    test("var x = foo(); var z = x;", "var z = foo();");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testDoNotCrossFunctionCalls
  public void testDoNotCrossFunctionCalls() {
    testSame("var x = foo(); bar(); var z = x;");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testDoNotCrossAssignment
  public void testDoNotCrossAssignment() {
    testSame("var x = {}; var y = x.a; x.a = 1; var z = y;");
    testSame("var a = this.id; foo(this.id = 3, a);");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testDoNotCrossDelete
  public void testDoNotCrossDelete() {
    testSame("var x = {}; var y = x.a; delete x.a; var z = y;");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testDoNotCrossAssignmentPlus
  public void testDoNotCrossAssignmentPlus() {
    testSame("var a = b; b += 2; var c = a;");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testDoNotCrossIncrement
  public void testDoNotCrossIncrement() {
    testSame("var a = b.c; b.c++; var d = a;");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testDoNotCrossConstructor
  public void testDoNotCrossConstructor() {
    testSame("var a = b; new Foo(); var c = a;");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testDoCrossVar
  public void testDoCrossVar() {
    
    test("var a = b; var b = 3; alert(a)", "alert(3);");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testOverlappingInlines
  public void testOverlappingInlines() {
    String source =
        "a = function(el, x, opt_y) { " +
        "  var cur = bar(el); " +
        "  opt_y = x.y; " +
        "  x = x.x; " +
        "  var dx = x - cur.x; " +
        "  var dy = opt_y - cur.y;" +
        "  foo(el, el.offsetLeft + dx, el.offsetTop + dy); " +
        "};";
    String expected =
      "a = function(el, x, opt_y) { " +
      "  var cur = bar(el); " +
      "  opt_y = x.y; " +
      "  x = x.x; " +
      "  foo(el, el.offsetLeft + (x - cur.x)," +
      "      el.offsetTop + (opt_y - cur.y)); " +
      "};";

    test(source, expected);
  }

// com.google.javascript.jscomp.InlineVariablesTest::testOverlappingInlineFunctions
  public void testOverlappingInlineFunctions() {
    String source =
        "a = function() { " +
        "  var b = function(args) {var n;}; " +
        "  var c = function(args) {}; " +
        "  d(b,c); " +
        "};";
    String expected =
      "a = function() { " +
      "  d(function(args){var n;}, function(args){}); " +
      "};";

    test(source, expected);
  }

// com.google.javascript.jscomp.InlineVariablesTest::testInlineIntoLoops
  public void testInlineIntoLoops() {
    test("var x = true; while (true) alert(x);",
         "while (true) alert(true);");
    test("var x = true; while (true) for (var i in {}) alert(x);",
         "while (true) for (var i in {}) alert(true);");
    testSame("var x = [true]; while (true) alert(x);");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testInlineIntoFunction
  public void testInlineIntoFunction() {
    test("var x = false; var f = function() { alert(x); };",
         "var f = function() { alert(false); };");
    testSame("var x = [false]; var f = function() { alert(x); };");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testNoInlineIntoNamedFunction
  public void testNoInlineIntoNamedFunction() {
    testSame("f(); var x = false; function f() { alert(x); };");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testInlineIntoNestedNonHoistedNamedFunctions
  public void testInlineIntoNestedNonHoistedNamedFunctions() {
    test("f(); var x = false; if (false) function f() { alert(x); };",
         "f(); if (false) function f() { alert(false); };");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testNoInlineIntoNestedNamedFunctions
  public void testNoInlineIntoNestedNamedFunctions() {
    testSame("f(); var x = false; function f() { if (false) { alert(x); } };");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testNoInlineMutatedVariable
  public void testNoInlineMutatedVariable() {
    testSame("var x = false; if (true) { var y = x; x = true; }");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testInlineImmutableMultipleTimes
  public void testInlineImmutableMultipleTimes() {
    test("var x = null; var y = x, z = x;",
         "var y = null, z = null;");
    test("var x = 3; var y = x, z = x;",
         "var y = 3, z = 3;");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testNoInlineStringMultipleTimesIfNotWorthwhile
  public void testNoInlineStringMultipleTimesIfNotWorthwhile() {
    testSame("var x = 'abcdefghijklmnopqrstuvwxyz'; var y = x, z = x;");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testInlineStringMultipleTimesWhenAliasingAllStrings
  public void testInlineStringMultipleTimesWhenAliasingAllStrings() {
    inlineAllStrings = true;
    test("var x = 'abcdefghijklmnopqrstuvwxyz'; var y = x, z = x;",
         "var y = 'abcdefghijklmnopqrstuvwxyz', " +
         "    z = 'abcdefghijklmnopqrstuvwxyz';");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testNoInlineBackwards
  public void testNoInlineBackwards() {
    testSame("var y = x; var x = null;");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testNoInlineOutOfBranch
  public void testNoInlineOutOfBranch() {
    testSame("if (true) var x = null; var y = x;");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testInterferingInlines
  public void testInterferingInlines() {
    test("var a = 3; var f = function() { var x = a; alert(x); };",
         "var f = function() { alert(3); };");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testInlineIntoTryCatch
  public void testInlineIntoTryCatch() {
    test("var a = true; " +
         "try { var b = a; } " +
         "catch (e) { var c = a + b; var d = true; } " +
         "finally { var f = a + b + c + d; }",
         "try { var b = true; } " +
         "catch (e) { var c = true + b; var d = true; } " +
         "finally { var f = true + b + c + d; }");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testInlineConstants
  public void testInlineConstants() {
    test("function foo() { return XXX; } var XXX = true;",
         "function foo() { return true; }");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testInlineStringWhenWorthwhile
  public void testInlineStringWhenWorthwhile() {
    test("var x = 'a'; foo(x, x, x);", "foo('a', 'a', 'a');");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testInlineConstantAlias
  public void testInlineConstantAlias() {
    test("var XXX = new Foo(); q(XXX); var YYY = XXX; bar(YYY)",
         "var XXX = new Foo(); q(XXX); bar(XXX)");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testInlineConstantAliasWithAnnotation
  public void testInlineConstantAliasWithAnnotation() {
    test(" var xxx = new Foo(); q(xxx); var YYY = xxx; bar(YYY)",
         " var xxx = new Foo(); q(xxx); bar(xxx)");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testInlineConstantAliasWithNonConstant
  public void testInlineConstantAliasWithNonConstant() {
    test("var XXX = new Foo(); q(XXX); var y = XXX; bar(y); baz(y)",
         "var XXX = new Foo(); q(XXX); bar(XXX); baz(XXX)");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testCascadingInlines
  public void testCascadingInlines() {
    test("var XXX = 4; " +
         "function f() { var YYY = XXX; bar(YYY); baz(YYY); }",
         "function f() { bar(4); baz(4); }");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testNoInlineGetpropIntoCall
  public void testNoInlineGetpropIntoCall() {
    test("var a = b; a();", "b();");
    test("var a = b.c; f(a);", "f(b.c);");
    testSame("var a = b.c; a();");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testInlineFunctionDeclaration
  public void testInlineFunctionDeclaration() {
    test("var f = function () {}; var a = f;",
         "var a = function () {};");
    test("var f = function () {}; foo(); var a = f;",
         "foo(); var a = function () {};");
    test("var f = function () {}; foo(f);",
         "foo(function () {});");

    testSame("var f = function () {}; function g() {var a = f;}");
    testSame("var f = function () {}; function g() {h(f);}");
  }

// com.google.javascript.jscomp.InlineVariablesTest::test2388531
  public void test2388531() {
    testSame("var f = function () {};" +
             "var g = function () {};" +
             "goog.inherits(f, g);");
    testSame("var f = function () {};" +
             "var g = function () {};" +
             "goog$inherits(f, g);");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testRecursiveFunction1
  public void testRecursiveFunction1() {
    testSame("var x = 0; (function x() { return x ? x() : 3; })();");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testRecursiveFunction2
  public void testRecursiveFunction2() {
    testSame("function y() { return y(); }");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testUnreferencedBleedingFunction
  public void testUnreferencedBleedingFunction() {
    testSame("var x = function y() {}");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testReferencedBleedingFunction
  public void testReferencedBleedingFunction() {
    testSame("var x = function y() { return y(); }");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testInlineAliases1
  public void testInlineAliases1() {
    test("var x = this.foo(); this.bar(); var y = x; this.baz(y);",
         "var x = this.foo(); this.bar(); this.baz(x);");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testInlineAliases1b
  public void testInlineAliases1b() {
    test("var x = this.foo(); this.bar(); var y; y = x; this.baz(y);",
         "var x = this.foo(); this.bar(); x; this.baz(x);");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testInlineAliases1c
  public void testInlineAliases1c() {
    test("var x; x = this.foo(); this.bar(); var y = x; this.baz(y);",
         "var x; x = this.foo(); this.bar(); this.baz(x);");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testInlineAliases1d
  public void testInlineAliases1d() {
    test("var x; x = this.foo(); this.bar(); var y; y = x; this.baz(y);",
         "var x; x = this.foo(); this.bar(); x; this.baz(x);");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testInlineAliases2
  public void testInlineAliases2() {
    test("var x = this.foo(); this.bar(); " +
         "function f() { var y = x; this.baz(y); }",
         "var x = this.foo(); this.bar(); function f() { this.baz(x); }");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testInlineAliases2b
  public void testInlineAliases2b() {
    test("var x = this.foo(); this.bar(); " +
         "function f() { var y; y = x; this.baz(y); }",
         "var x = this.foo(); this.bar(); function f() { this.baz(x); }");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testInlineAliases2c
  public void testInlineAliases2c() {
    test("var x; x = this.foo(); this.bar(); " +
         "function f() { var y = x; this.baz(y); }",
         "var x; x = this.foo(); this.bar(); function f() { this.baz(x); }");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testInlineAliases2d
  public void testInlineAliases2d() {
    test("var x; x = this.foo(); this.bar(); " +
         "function f() { var y; y = x; this.baz(y); }",
         "var x; x = this.foo(); this.bar(); function f() { this.baz(x); }");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testInlineAliasesInLoop
  public void testInlineAliasesInLoop() {
    test(
        "function f() { " +
        "  var x = extern();" +
        "  for (var i = 0; i < 5; i++) {" +
        "    (function() {" +
        "       var y = x; window.setTimeout(function() { extern(y); }, 0);" +
        "     })();" +
        "  }" +
        "}",
        "function f() { " +
        "  var x = extern();" +
        "  for (var i = 0; i < 5; i++) {" +
        "    (function() {" +
        "       window.setTimeout(function() { extern(x); }, 0);" +
        "     })();" +
        "  }" +
        "}");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testNoInlineAliasesInLoop
  public void testNoInlineAliasesInLoop() {
    testSame(
        "function f() { " +
        "  for (var i = 0; i < 5; i++) {" +
        "    var x = extern();" +
        "    (function() {" +
        "       var y = x; window.setTimeout(function() { extern(y); }, 0);" +
        "     })();" +
        "  }" +
        "}");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testNoInlineAliases1
  public void testNoInlineAliases1() {
    testSame(
        "var x = this.foo(); this.bar(); var y = x; x = 3; this.baz(y);");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testNoInlineAliases1b
  public void testNoInlineAliases1b() {
    testSame(
        "var x = this.foo(); this.bar(); var y; y = x; x = 3; this.baz(y);");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testNoInlineAliases2
  public void testNoInlineAliases2() {
    testSame(
        "var x = this.foo(); this.bar(); var y = x; y = 3; this.baz(y); ");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testNoInlineAliases2b
  public void testNoInlineAliases2b() {
    testSame(
        "var x = this.foo(); this.bar(); var y; y = x; y = 3; this.baz(y); ");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testNoInlineAliases3
  public void testNoInlineAliases3() {
    testSame(
         "var x = this.foo(); this.bar(); " +
         "function f() { var y = x; g(); this.baz(y); } " +
         "function g() { x = 3; }");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testNoInlineAliases3b
  public void testNoInlineAliases3b() {
    testSame(
         "var x = this.foo(); this.bar(); " +
         "function f() { var y; y = x; g(); this.baz(y); } " +
         "function g() { x = 3; }");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testNoInlineAliases4
  public void testNoInlineAliases4() {
    testSame(
         "var x = this.foo(); this.bar(); " +
         "function f() { var y = x; y = 3; this.baz(y); }");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testNoInlineAliases4b
  public void testNoInlineAliases4b() {
    testSame(
         "var x = this.foo(); this.bar(); " +
         "function f() { var y; y = x; y = 3; this.baz(y); }");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testNoInlineAliases5
  public void testNoInlineAliases5() {
    testSame(
        "var x = this.foo(); this.bar(); var y = x; this.bing();" +
        "this.baz(y); x = 3;");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testNoInlineAliases5b
  public void testNoInlineAliases5b() {
    testSame(
        "var x = this.foo(); this.bar(); var y; y = x; this.bing();" +
        "this.baz(y); x = 3;");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testNoInlineAliases6
  public void testNoInlineAliases6() {
    testSame(
        "var x = this.foo(); this.bar(); var y = x; this.bing();" +
        "this.baz(y); y = 3;");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testNoInlineAliases6b
  public void testNoInlineAliases6b() {
    testSame(
        "var x = this.foo(); this.bar(); var y; y = x; this.bing();" +
        "this.baz(y); y = 3;");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testNoInlineAliases7
  public void testNoInlineAliases7() {
    testSame(
         "var x = this.foo(); this.bar(); " +
         "function f() { var y = x; this.bing(); this.baz(y); x = 3; }");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testNoInlineAliases7b
  public void testNoInlineAliases7b() {
    testSame(
         "var x = this.foo(); this.bar(); " +
         "function f() { var y; y = x; this.bing(); this.baz(y); x = 3; }");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testNoInlineAliases8
  public void testNoInlineAliases8() {
    testSame(
         "var x = this.foo(); this.bar(); " +
         "function f() { var y = x; this.baz(y); y = 3; }");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testNoInlineAliases8b
  public void testNoInlineAliases8b() {
    testSame(
         "var x = this.foo(); this.bar(); " +
         "function f() { var y; y = x; this.baz(y); y = 3; }");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testSideEffectOrder
  public void testSideEffectOrder() {
    
    String EXTERNS = "var z; function f(){}";
    test(EXTERNS,
         "var x = f(y.a, y); z = x;",
         "z = f(y.a, y);", null, null);
    
    testSame(EXTERNS, "var x = f(y.a, y); z.b = x;", null, null);
  }

// com.google.javascript.jscomp.InlineVariablesTest::testInlineParameterAlias1
  public void testInlineParameterAlias1() {
    test(
      "function f(x) {" +
      "  var y = x;" +
      "  g();" +
      "  y;y;" +
      "}",
      "function f(x) {" +
      "  g();" +
      "  x;x;" +
      "}"
      );
  }

// com.google.javascript.jscomp.InlineVariablesTest::testInlineParameterAlias2
  public void testInlineParameterAlias2() {
    test(
      "function f(x) {" +
      "  var y; y = x;" +
      "  g();" +
      "  y;y;" +
      "}",
      "function f(x) {" +
      "  x;" +
      "  g();" +
      "  x;x;" +
      "}"
      );
  }

// com.google.javascript.jscomp.InlineVariablesTest::testInlineFunctionAlias1a
  public void testInlineFunctionAlias1a() {
    test(
      "function f(x) {}" +
      "var y = f;" +
      "g();" +
      "y();y();",
      "var y = function f(x) {};" +
      "g();" +
      "y();y();"
      );
  }

// com.google.javascript.jscomp.InlineVariablesTest::testInlineFunctionAlias1b
  public void testInlineFunctionAlias1b() {
    test(
      "function f(x) {};" +
      "f;var y = f;" +
      "g();" +
      "y();y();",
      "function f(x) {};" +
      "f;g();" +
      "f();f();"
      );
  }

// com.google.javascript.jscomp.InlineVariablesTest::testInlineFunctionAlias2a
  public void testInlineFunctionAlias2a() {
    test(
      "function f(x) {}" +
      "var y; y = f;" +
      "g();" +
      "y();y();",
      "var y; y = function f(x) {};" +
      "g();" +
      "y();y();"
      );
  }

// com.google.javascript.jscomp.InlineVariablesTest::testInlineFunctionAlias2b
  public void testInlineFunctionAlias2b() {
    test(
      "function f(x) {};" +
      "f; var y; y = f;" +
      "g();" +
      "y();y();",
      "function f(x) {};" +
      "f; f;" +
      "g();" +
      "f();f();"
      );
  }

// com.google.javascript.jscomp.InlineVariablesTest::testInlineCatchAlias1
  public void testInlineCatchAlias1() {
    test(
      "try {" +
      "} catch (e) {" +
      "  var y = e;" +
      "  g();" +
      "  y;y;" +
      "}",
      "try {" +
      "} catch (e) {" +
      "  g();" +
      "  e;e;" +
      "}"
      );
  }

// com.google.javascript.jscomp.InlineVariablesTest::testInlineCatchAlias2
  public void testInlineCatchAlias2() {
    test(
      "try {" +
      "} catch (e) {" +
      "  var y; y = e;" +
      "  g();" +
      "  y;y;" +
      "}",
      "try {" +
      "} catch (e) {" +
      "  e;" +
      "  g();" +
      "  e;e;" +
      "}"
      );
  }

// com.google.javascript.jscomp.InlineVariablesTest::testLocalsOnly1
  public void testLocalsOnly1() {
    inlineLocalsOnly = true;
    test(
        "var x=1; x; function f() {var x = 1; x;}",
        "var x=1; x; function f() {1;}");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testLocalsOnly2
  public void testLocalsOnly2() {
    inlineLocalsOnly = true;
    test(
        "\n" +
        "var X=1; X;\n" +
        "function f() {\n" +
        "  \n" +
        "  var X = 1; X;\n" +
        "}",
        "var X=1; X; function f() {1;}");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testInlineUndefined1
  public void testInlineUndefined1() {
    test("var x; x;",
         "void 0;");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testInlineUndefined2
  public void testInlineUndefined2() {
    testSame("var x; x++;");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testInlineUndefined3
  public void testInlineUndefined3() {
    testSame("var x; var x;");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testInlineUndefined4
  public void testInlineUndefined4() {
    test("var x; x; x;",
         "void 0; void 0;");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testInlineUndefined5
  public void testInlineUndefined5() {
    test("var x; for(x in a) {}",
         "var x; for(x in a) {}");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testIssue90
  public void testIssue90() {
    test("var x; x && alert(1)",
         "void 0 && alert(1)");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testRenamePropertyFunction
  public void testRenamePropertyFunction() {
    testSame("var JSCompiler_renameProperty; " +
             "JSCompiler_renameProperty('foo')");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testThisAlias
  public void testThisAlias() {
    test("function f() { var a = this; a.y(); a.z(); }",
         "function f() { this.y(); this.z(); }");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testThisEscapedAlias
  public void testThisEscapedAlias() {
    testSame(
        "function f() { var a = this; var g = function() { a.y(); }; a.z(); }");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testInlineNamedFunction
  public void testInlineNamedFunction() {
    test("function f() {} f();", "(function f(){})()");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testIssue378ModifiedArguments1
  public void testIssue378ModifiedArguments1() {
    testSame(
        "function g(callback) {\n" +
        "  var f = callback;\n" +
        "  arguments[0] = this;\n" +
        "  f.apply(this, arguments);\n" +
        "}");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testIssue378ModifiedArguments2
  public void testIssue378ModifiedArguments2() {
    testSame(
        "function g(callback) {\n" +
        "  \n" +
        "  var f = callback;\n" +
        "  arguments[0] = this;\n" +
        "  f.apply(this, arguments);\n" +
        "}");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testIssue378EscapedArguments1
  public void testIssue378EscapedArguments1() {
    testSame(
        "function g(callback) {\n" +
        "  var f = callback;\n" +
        "  h(arguments,this);\n" +
        "  f.apply(this, arguments);\n" +
        "}\n" +
        "function h(a,b) {\n" +
        "  a[0] = b;" +
        "}");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testIssue378EscapedArguments2
  public void testIssue378EscapedArguments2() {
    testSame(
        "function g(callback) {\n" +
        "  \n" +
        "  var f = callback;\n" +
        "  h(arguments,this);\n" +
        "  f.apply(this);\n" +
        "}\n" +
        "function h(a,b) {\n" +
        "  a[0] = b;" +
        "}");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testIssue378EscapedArguments3
  public void testIssue378EscapedArguments3() {
    test(
        "function g(callback) {\n" +
        "  var f = callback;\n" +
        "  f.apply(this, arguments);\n" +
        "}\n",
        "function g(callback) {\n" +
        "  callback.apply(this, arguments);\n" +
        "}\n");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testIssue378EscapedArguments4
  public void testIssue378EscapedArguments4() {
    testSame(
        "function g(callback) {\n" +
        "  var f = callback;\n" +
        "  h(arguments[0],this);\n" +
        "  f.apply(this, arguments);\n" +
        "}\n" +
        "function h(a,b) {\n" +
        "  a[0] = b;" +
        "}");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testIssue378ArgumentsRead1
  public void testIssue378ArgumentsRead1() {
    test(
        "function g(callback) {\n" +
        "  var f = callback;\n" +
        "  var g = arguments[0];\n" +
        "  f.apply(this, arguments);\n" +
        "}",
        "function g(callback) {\n" +
        "  var g = arguments[0];\n" +
        "  callback.apply(this, arguments);\n" +
        "}");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testIssue378ArgumentsRead2
  public void testIssue378ArgumentsRead2() {
    test(
        "function g(callback) {\n" +
        "  var f = callback;\n" +
        "  h(arguments[0],this);\n" +
        "  f.apply(this, arguments[0]);\n" +
        "}\n" +
        "function h(a,b) {\n" +
        "  a[0] = b;" +
        "}",
        "function g(callback) {\n" +
        "  h(arguments[0],this);\n" +
        "  callback.apply(this, arguments[0]);\n" +
        "}\n" +
        "function h(a,b) {\n" +
        "  a[0] = b;" +
        "}");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testArgumentsModifiedInOuterFunction
  public void testArgumentsModifiedInOuterFunction() {
    test(
      "function g(callback) {\n" +
      "  var f = callback;\n" +
      "  arguments[0] = this;\n" +
      "  f.apply(this, arguments);\n" +
      "  function inner(callback) {" +
      "    var x = callback;\n" +
      "    x.apply(this);\n" +
      "  }" +
      "}",
      "function g(callback) {\n" +
      "  var f = callback;\n" +
      "  arguments[0] = this;\n" +
      "  f.apply(this, arguments);\n" +
      "  function inner(callback) {" +
      "    callback.apply(this);\n" +
      "  }" +
      "}");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testArgumentsModifiedInInnerFunction
  public void testArgumentsModifiedInInnerFunction() {
    test(
      "function g(callback) {\n" +
      "  var f = callback;\n" +
      "  f.apply(this, arguments);\n" +
      "  function inner(callback) {" +
      "    var x = callback;\n" +
      "    arguments[0] = this;\n" +
      "    x.apply(this);\n" +
      "  }" +
      "}",
      "function g(callback) {\n" +
      "  callback.apply(this, arguments);\n" +
      "  function inner(callback) {" +
      "    var x = callback;\n" +
      "    arguments[0] = this;\n" +
      "    x.apply(this);\n" +
      "  }" +
      "}");
  }

// com.google.javascript.jscomp.InstrumentFunctionsTest::testInstrument
  public void testInstrument() {
    final String kPreamble =
        "var $$toRemoveDefinition1, $$notToRemove;\n" +
        "var $$toRemoveDefinition2, $$toRemoveDefinition3;\n";

    
    
    List<String> initCodeList = ImmutableList.of(
        "var $$Table = [];",
        "function $$TestDefine(id) {",
        "  $$Table[id] = 0;",
        "};",
        "function $$TestInstrument(id) {",
        "  $$Table[id]++;",
        "};");
    StringBuilder initCodeBuilder = new StringBuilder();
    StringBuilder pbBuilder = new StringBuilder();
    for (String line : initCodeList) {
      initCodeBuilder.append(line).append("\n");
      pbBuilder.append("init: \"").append(line).append("\"\n");
    }

    pbBuilder.append("report_call: \"$$testInstrument\"")
        .append("report_defined: \"$$testDefine\"")
        .append("declaration_to_remove: \"$$toRemoveDefinition1\"")
        .append("declaration_to_remove: \"$$toRemoveDefinition2\"")
        .append("declaration_to_remove: \"$$toRemoveDefinition3\"");

    final String initCode = initCodeBuilder.toString();
    this.instrumentationPb = pbBuilder.toString();

    
    test("function a(){b}",
         initCode + "$$testDefine(0);" +
         "function a(){$$testInstrument(0);b}");

    
    test(kPreamble + "function a(){b}",
         initCode +
         "$$testDefine(0);" +
         "var $$notToRemove;" +
         "function a(){$$testInstrument(0);b}");

    
    test(kPreamble + "var a = { b: function(){c} }",
         initCode +
         "var $$notToRemove;" +
         "$$testDefine(0);" +
         "var a = { b: function(){$$testInstrument(0);c} }");

    
    test(kPreamble +
         "var a = { b: function(){c}, d: function(){e} }",
         initCode +
         "var $$notToRemove;" +
         "$$testDefine(0);" +
         "$$testDefine(1);" +
         "var a={b:function(){$$testInstrument(0);c}," +
         "d:function(){$$testInstrument(1);e}}");

    
    test(kPreamble +
         "var a = { b: { f: function(){c} }, d: function(){e} }",
         initCode +
         "var $$notToRemove;" +
         "$$testDefine(0);" +
         "$$testDefine(1);" +
         "var a={b:{f:function(){$$testInstrument(0);c}}," +
         "d:function(){$$testInstrument(1);e}}");
  }

// com.google.javascript.jscomp.InstrumentFunctionsTest::testEmpty
  public void testEmpty() {
    this.instrumentationPb = "";
    test("function a(){b}", "function a(){b}");
  }

// com.google.javascript.jscomp.InstrumentFunctionsTest::testAppNameSetter
  public void testAppNameSetter() {
    this.instrumentationPb = "app_name_setter: \"setAppName\"";
    test("function a(){b}", "setAppName(\"testfile.js\");function a(){b}");
  }

// com.google.javascript.jscomp.InstrumentFunctionsTest::testInit
  public void testInit() {
    this.instrumentationPb = "init: \"var foo = 0;\"\n" +
        "init: \"function f(){g();}\"\n";
    test("function a(){b}",
         "var foo = 0;function f(){g()}function a(){b}");
  }

// com.google.javascript.jscomp.InstrumentFunctionsTest::testDeclare
  public void testDeclare() {
    this.instrumentationPb = "report_defined: \"$$testDefine\"";
    test("function a(){b}", "$$testDefine(0);function a(){b}");
  }

// com.google.javascript.jscomp.InstrumentFunctionsTest::testCall
  public void testCall() {
    this.instrumentationPb = "report_call: \"$$testCall\"";
    test("function a(){b}", "function a(){$$testCall(0);b}");
  }

// com.google.javascript.jscomp.InstrumentFunctionsTest::testNested
  public void testNested() {
    this.instrumentationPb = "report_call: \"$$testCall\"\n" +
        "report_defined: \"$$testDefine\"";
    test("function a(){ function b(){}}",
         "$$testDefine(1);$$testDefine(0);" +
         "function a(){$$testCall(1);function b(){$$testCall(0)}}");
  }

// com.google.javascript.jscomp.InstrumentFunctionsTest::testExitPaths
  public void testExitPaths() {
    this.instrumentationPb = "report_exit: \"$$testExit\"";
    test("function a(){return}",
         "function a(){return $$testExit(0)}");

    test("function b(){return 5}",
         "function b(){return $$testExit(0, 5)}");

    test("function a(){if(2 != 3){return}else{return 5}}",
         "function a(){if(2!=3){return $$testExit(0)}" +
         "else{return $$testExit(0,5)}}");

    test("function a(){if(2 != 3){return}else{return 5}}b()",
         "function a(){if(2!=3){return $$testExit(0)}" +
         "else{return $$testExit(0,5)}}b()");

    test("function a(){if(2 != 3){return}else{return 5}}",
         "function a(){if(2!=3){return $$testExit(0)}" +
         "else{return $$testExit(0,5)}}");
  }

// com.google.javascript.jscomp.InstrumentFunctionsTest::testExitNoReturn
  public void testExitNoReturn() {
    this.instrumentationPb = "report_exit: \"$$testExit\"";
    test("function a(){}",
         "function a(){$$testExit(0);}");

    test("function a(){b()}",
         "function a(){b();$$testExit(0);}");
  }

// com.google.javascript.jscomp.InstrumentFunctionsTest::testPartialExitPaths
  public void testPartialExitPaths() {
    this.instrumentationPb = "report_exit: \"$$testExit\"";
    test("function a(){if (2 != 3) {return}}",
         "function a(){if (2 != 3){return $$testExit(0)}$$testExit(0)}");
  }

// com.google.javascript.jscomp.InstrumentFunctionsTest::testExitTry
  public void testExitTry() {
    this.instrumentationPb = "report_exit: \"$$testExit\"";
    test("function a(){try{return}catch(err){}}",
         "function a(){try{return $$testExit(0)}catch(err){}$$testExit(0)}");

    test("function a(){try{}catch(err){return}}",
         "function a(){try{}catch(err){return $$testExit(0)}$$testExit(0)}");

    test("function a(){try{return}finally{}}",
         "function a(){try{return $$testExit(0)}finally{}$$testExit(0)}");

    test("function a(){try{return}catch(err){}finally{}}",
         "function a(){try{return $$testExit(0)}catch(err){}finally{}" +
         "$$testExit(0)}");

    test("function a(){try{return 1}catch(err){return 2}}",
         "function a(){try{return $$testExit(0, 1)}" +
         "catch(err){return $$testExit(0,2)}}");

    test("function a(){try{return 1}catch(err){return 2}finally{}}",
         "function a(){try{return $$testExit(0, 1)}" +
         "catch(err){return $$testExit(0,2)}" +
         "finally{}$$testExit(0)}");

    test("function a(){try{return 1}catch(err){return 2}finally{return}}",
         "function a(){try{return $$testExit(0, 1)}" +
         "catch(err){return $$testExit(0,2)}finally{return $$testExit(0)}}");

    test("function a(){try{}catch(err){}finally{return}}",
         "function a(){try{}catch(err){}finally{return $$testExit(0)}}");
  }

// com.google.javascript.jscomp.InstrumentFunctionsTest::testNestedExit
  public void testNestedExit() {
    this.instrumentationPb = "report_exit: \"$$testExit\"\n" +
        "report_defined: \"$$testDefine\"";
    test("function a(){ return function(){ return c;}}",
         "$$testDefine(1);function a(){$$testDefine(0);" +
         "return $$testExit(1, function(){return $$testExit(0, c);});}");
  }

// com.google.javascript.jscomp.InstrumentFunctionsTest::testProtobuffParseFail
  public void testProtobuffParseFail() {
    this.instrumentationPb = "not an ascii pb\n";
    test("function a(){b}", "", RhinoErrorReporter.PARSE_ERROR);
  }

// com.google.javascript.jscomp.InstrumentFunctionsTest::testInitJsParseFail
  public void testInitJsParseFail() {
    this.instrumentationPb = "init: \"= assignWithNoLhs();\"";
    test("function a(){b}", "", RhinoErrorReporter.PARSE_ERROR);
  }

// com.google.javascript.jscomp.JSCompilerSourceExcerptProviderTest::testExcerptOneLine
  public void testExcerptOneLine() throws Exception {
    assertEquals("foo:first line", provider.getSourceLine("foo", 1));
    assertEquals("foo:second line", provider.getSourceLine("foo", 2));
    assertEquals("foo:third line", provider.getSourceLine("foo", 3));
    assertEquals("bar:first line", provider.getSourceLine("bar", 1));
    assertEquals("bar:second line", provider.getSourceLine("bar", 2));
    assertEquals("bar:third line", provider.getSourceLine("bar", 3));
    assertEquals("bar:fourth line", provider.getSourceLine("bar", 4));
  }

// com.google.javascript.jscomp.JSCompilerSourceExcerptProviderTest::testExcerptLineFromInexistantSource
  public void testExcerptLineFromInexistantSource() throws Exception {
    assertEquals(null, provider.getSourceLine("inexistant", 1));
    assertEquals(null, provider.getSourceLine("inexistant", 7));
    assertEquals(null, provider.getSourceLine("inexistant", 90));
  }

// com.google.javascript.jscomp.JSCompilerSourceExcerptProviderTest::testExcerptInexistantLine
  public void testExcerptInexistantLine() throws Exception {
    assertEquals(null, provider.getSourceLine("foo", 0));
    assertEquals(null, provider.getSourceLine("foo", 4));
    assertEquals(null, provider.getSourceLine("bar", 0));
    assertEquals(null, provider.getSourceLine("bar", 5));
  }

// com.google.javascript.jscomp.JSCompilerSourceExcerptProviderTest::testExcerptRegion
  public void testExcerptRegion() throws Exception {
    assertRegionWellFormed("foo", 1);
    assertRegionWellFormed("foo", 2);
    assertRegionWellFormed("foo", 3);
    assertRegionWellFormed("bar", 1);
    assertRegionWellFormed("bar", 2);
    assertRegionWellFormed("bar", 3);
    assertRegionWellFormed("bar", 4);
  }

// com.google.javascript.jscomp.JSCompilerSourceExcerptProviderTest::testExcerptRegionFromInexistantSource
  public void testExcerptRegionFromInexistantSource() throws Exception {
    assertEquals(null, provider.getSourceRegion("inexistant", 0));
    assertEquals(null, provider.getSourceRegion("inexistant", 6));
    assertEquals(null, provider.getSourceRegion("inexistant", 90));
  }

// com.google.javascript.jscomp.JSCompilerSourceExcerptProviderTest::testExcerptInexistantRegion
  public void testExcerptInexistantRegion() throws Exception {
    assertEquals(null, provider.getSourceRegion("foo", 0));
    assertEquals(null, provider.getSourceRegion("foo", 4));
    assertEquals(null, provider.getSourceRegion("bar", 0));
    assertEquals(null, provider.getSourceRegion("bar", 5));
  }

// com.google.javascript.jscomp.JSModuleGraphTest::testModuleDepth
  public void testModuleDepth() {
    assertEquals("A should have depth 0", 0, A.getDepth());
    assertEquals("B should have depth 1", 1, B.getDepth());
    assertEquals("C should have depth 1", 1, C.getDepth());
    assertEquals("D should have depth 2", 2, D.getDepth());
    assertEquals("E should have depth 2", 2, E.getDepth());
    assertEquals("F should have depth 3", 3, F.getDepth());
  }

// com.google.javascript.jscomp.JSModuleGraphTest::testDeepestCommonDep
  public void testDeepestCommonDep() {
    assertDeepestCommonDep(null, A, A);
    assertDeepestCommonDep(null, A, B);
    assertDeepestCommonDep(null, A, C);
    assertDeepestCommonDep(null, A, D);
    assertDeepestCommonDep(null, A, E);
    assertDeepestCommonDep(null, A, F);
    assertDeepestCommonDep(A, B, B);
    assertDeepestCommonDep(A, B, C);
    assertDeepestCommonDep(A, B, D);
    assertDeepestCommonDep(A, B, E);
    assertDeepestCommonDep(A, B, F);
    assertDeepestCommonDep(A, C, C);
    assertDeepestCommonDep(A, C, D);
    assertDeepestCommonDep(A, C, E);
    assertDeepestCommonDep(A, C, F);
    assertDeepestCommonDep(B, D, D);
    assertDeepestCommonDep(B, D, E);
    assertDeepestCommonDep(B, D, F);
    assertDeepestCommonDep(C, E, E);
    assertDeepestCommonDep(C, E, F);
    assertDeepestCommonDep(E, F, F);
  }

// com.google.javascript.jscomp.JSModuleGraphTest::testDeepestCommonDepInclusive
  public void testDeepestCommonDepInclusive() {
    assertDeepestCommonDepInclusive(A, A, A);
    assertDeepestCommonDepInclusive(A, A, B);
    assertDeepestCommonDepInclusive(A, A, C);
    assertDeepestCommonDepInclusive(A, A, D);
    assertDeepestCommonDepInclusive(A, A, E);
    assertDeepestCommonDepInclusive(A, A, F);
    assertDeepestCommonDepInclusive(B, B, B);
    assertDeepestCommonDepInclusive(A, B, C);
    assertDeepestCommonDepInclusive(B, B, D);
    assertDeepestCommonDepInclusive(B, B, E);
    assertDeepestCommonDepInclusive(B, B, F);
    assertDeepestCommonDepInclusive(C, C, C);
    assertDeepestCommonDepInclusive(A, C, D);
    assertDeepestCommonDepInclusive(C, C, E);
    assertDeepestCommonDepInclusive(C, C, F);
    assertDeepestCommonDepInclusive(D, D, D);
    assertDeepestCommonDepInclusive(B, D, E);
    assertDeepestCommonDepInclusive(B, D, F);
    assertDeepestCommonDepInclusive(E, E, E);
    assertDeepestCommonDepInclusive(E, E, F);
    assertDeepestCommonDepInclusive(F, F, F);
  }

// com.google.javascript.jscomp.JSModuleGraphTest::testGetTransitiveDepsDeepestFirst
  public void testGetTransitiveDepsDeepestFirst() {
    assertTransitiveDepsDeepestFirst(A);
    assertTransitiveDepsDeepestFirst(B, A);
    assertTransitiveDepsDeepestFirst(C, A);
    assertTransitiveDepsDeepestFirst(D, B, A);
    assertTransitiveDepsDeepestFirst(E, C, B, A);
    assertTransitiveDepsDeepestFirst(F, E, C, B, A);
  }

// com.google.javascript.jscomp.JSModuleGraphTest::testCoalesceDuplicateFiles
  public void testCoalesceDuplicateFiles() {
    A.add(JSSourceFile.fromCode("a.js", ""));

    B.add(JSSourceFile.fromCode("a.js", ""));
    B.add(JSSourceFile.fromCode("b.js", ""));

    C.add(JSSourceFile.fromCode("b.js", ""));
    C.add(JSSourceFile.fromCode("c.js", ""));

    E.add(JSSourceFile.fromCode("c.js", ""));
    E.add(JSSourceFile.fromCode("d.js", ""));

    graph.coalesceDuplicateFiles();

    assertEquals(2, A.getInputs().size());
    assertEquals("a.js", A.getInputs().get(0).getName());
    assertEquals("b.js", A.getInputs().get(1).getName());
    assertEquals(0, B.getInputs().size());
    assertEquals(1, C.getInputs().size());
    assertEquals("c.js", C.getInputs().get(0).getName());
    assertEquals(1, E.getInputs().size());
    assertEquals("d.js", E.getInputs().get(0).getName());
  }

// com.google.javascript.jscomp.JSModuleGraphTest::testManageDependencies1
  public void testManageDependencies1() throws Exception {
    List<CompilerInput> inputs = setUpManageDependenciesTest();
    List<CompilerInput> results = graph.manageDependencies(
        ImmutableList.<String>of(), inputs);

    assertInputs(A, "a1", "a3");
    assertInputs(B, "a2", "b2");
    assertInputs(C); 
    assertInputs(E, "c1", "e1", "e2");

    assertEquals(
        Lists.newArrayList("a1", "a3", "a2", "b2", "c1", "e1", "e2"),
        sourceNames(results));
  }

// com.google.javascript.jscomp.JSModuleGraphTest::testManageDependencies2
  public void testManageDependencies2() throws Exception {
    List<CompilerInput> inputs = setUpManageDependenciesTest();
    List<CompilerInput> results = graph.manageDependencies(
        ImmutableList.<String>of("c2"), inputs);

    assertInputs(A, "a1", "a3");
    assertInputs(B, "a2", "b2");
    assertInputs(C, "c1", "c2");
    assertInputs(E, "e1", "e2");

    assertEquals(
        Lists.newArrayList("a1", "a3", "a2", "b2", "c1", "c2", "e1", "e2"),
        sourceNames(results));
  }

// com.google.javascript.jscomp.JSModuleTest::testDependencies
  public void testDependencies() {
    assertEquals(ImmutableSet.of(), mod1.getAllDependencies());
    assertEquals(ImmutableSet.of(mod1), mod2.getAllDependencies());
    assertEquals(ImmutableSet.of(mod1), mod3.getAllDependencies());
    assertEquals(ImmutableSet.of(mod1, mod2, mod3), mod4.getAllDependencies());

    assertEquals(ImmutableSet.of(mod1), mod1.getThisAndAllDependencies());
    assertEquals(ImmutableSet.of(mod1, mod2), mod2.getThisAndAllDependencies());
    assertEquals(ImmutableSet.of(mod1, mod3), mod3.getThisAndAllDependencies());
    assertEquals(ImmutableSet.of(mod1, mod2, mod3, mod4),
                 mod4.getThisAndAllDependencies());
  }

// com.google.javascript.jscomp.JSModuleTest::testSortInputs
  public void testSortInputs() throws Exception {
    CompilerInput a = new CompilerInput(
        JSSourceFile.fromCode("a.js",
            "goog.require('b');goog.require('c')"));
    CompilerInput b = new CompilerInput(
        JSSourceFile.fromCode("b.js",
            "goog.provide('b');goog.require('d')"));
    CompilerInput c = new CompilerInput(
        JSSourceFile.fromCode("c.js",
            "goog.provide('c');goog.require('d')"));
    CompilerInput d = new CompilerInput(
        JSSourceFile.fromCode("d.js",
            "goog.provide('d')"));

    
    CompilerInput e = new CompilerInput(
        JSSourceFile.fromCode("e.js",
            "goog.provide('e')"));
    CompilerInput f = new CompilerInput(
        JSSourceFile.fromCode("f.js",
            "goog.provide('f')"));

    assertSortedInputs(
        ImmutableList.of(d, b, c, a),
        ImmutableList.of(a, b, c, d));
    assertSortedInputs(
        ImmutableList.of(d, b, c, a),
        ImmutableList.of(d, b, c, a));
    assertSortedInputs(
        ImmutableList.of(d, c, b, a),
        ImmutableList.of(d, c, b, a));
    assertSortedInputs(
        ImmutableList.of(d, b, c, a),
        ImmutableList.of(d, a, b, c));
  }

// com.google.javascript.jscomp.JSModuleTest::testSortJsModules
  public void testSortJsModules() throws Exception {
    
    assertEquals(ImmutableList.of(mod1, mod2, mod3, mod4),
        Arrays.asList(JSModule.sortJsModules(
            ImmutableList.of(mod1, mod2, mod3, mod4))));
    assertEquals(ImmutableList.of(mod1, mod3, mod2, mod4),
        Arrays.asList(JSModule.sortJsModules(
            ImmutableList.of(mod1, mod3, mod2, mod4))));

    
    assertEquals(ImmutableList.of(mod1, mod3, mod2, mod4),
        Arrays.asList(JSModule.sortJsModules(
            ImmutableList.of(mod4, mod3, mod2, mod1))));
    assertEquals(ImmutableList.of(mod1, mod3, mod2, mod4),
        Arrays.asList(JSModule.sortJsModules(
            ImmutableList.of(mod3, mod1, mod2, mod4))));

    
    assertEquals(ImmutableList.of(mod1, mod3, mod2, mod4),
        Arrays.asList(JSModule.sortJsModules(
            ImmutableList.of(mod4, mod3, mod1, mod2))));
  }

// com.google.javascript.jscomp.JsMessageExtractorTest::testSyntaxError1
  public void testSyntaxError1() {
    try {
      extractMessage("if (true) {}}");
      fail("Expected exception");
    } catch (RuntimeException e) {
      assertEquals("JSCompiler errors\n" +
          "testcode:1: ERROR - Parse error. syntax error\n", e.getMessage());
    }
  }

// com.google.javascript.jscomp.JsMessageExtractorTest::testSyntaxError2
  public void testSyntaxError2() {
    try {
      extractMessage("", "if (true) {}}");
      fail("Expected exception");
    } catch (RuntimeException e) {
      assertEquals("JSCompiler errors\n" +
          "testcode:2: ERROR - Parse error. syntax error\n", e.getMessage());
    }
  }

// com.google.javascript.jscomp.JsMessageExtractorTest::testExtractNewStyleMessage1
  public void testExtractNewStyleMessage1() {
    
    assertEquals(
        new JsMessage.Builder("MSG_SILLY")
            .appendStringPart("silly test message")
            .build(),
        extractMessage("var MSG_SILLY = goog.getMsg('silly test message');"));
  }

// com.google.javascript.jscomp.JsMessageExtractorTest::testExtractNewStyleMessage2
  public void testExtractNewStyleMessage2() {
    
    assertEquals(
        new JsMessage.Builder("MSG_WELCOME")
            .appendStringPart("Hi ")
            .appendPlaceholderReference("userName")
            .appendStringPart("! Welcome to ")
            .appendPlaceholderReference("product")
            .appendStringPart(".")
            .setDesc("The welcome message.")
            .setIsHidden(true)
            .build(),
        extractMessage(
            "",
            "var MSG_WELCOME = goog.getMsg(",
            "    'Hi {$userName}! Welcome to {$product}.',",
            "    {userName: someUserName, product: getProductName()});"));
  }

// com.google.javascript.jscomp.JsMessageExtractorTest::testExtractOldStyleMessage1
  public void testExtractOldStyleMessage1() {
    
    assertEquals(
        new JsMessage.Builder("MSG_SILLY")
            .appendStringPart("silly test message")
            .setDesc("Description.")
            .build(),
        extractMessage(
            "var MSG_SILLY_HELP = 'Description.';",
            "var MSG_SILLY = 'silly test message';"));
  }

// com.google.javascript.jscomp.JsMessageExtractorTest::testExtractOldStyleMessage2
  public void testExtractOldStyleMessage2() {
    
    assertEquals(
        new JsMessage.Builder("MSG_SILLY")
            .appendStringPart("silly test message")
            .setDesc("Description.")
            .build(),
        extractMessage(
            "var MSG_SILLY = 'silly test message';",
            "var MSG_SILLY_HELP = 'Descrip' + 'tion.';"));
  }

// com.google.javascript.jscomp.JsMessageExtractorTest::testExtractOldStyleMessage3
  public void testExtractOldStyleMessage3() {
    
    assertEquals(
        new JsMessage.Builder("MSG_SILLY")
            .appendPlaceholderReference("one")
            .appendStringPart(", ")
            .appendPlaceholderReference("two")
            .appendStringPart(", buckle my shoe")
            .build(),
        extractMessage(
            "var MSG_SILLY = function(one, two) {",
            "  return one + ', ' + two + ', buckle my shoe';",
            "};"));
  }

// com.google.javascript.jscomp.JsMessageExtractorTest::testExtractMixedMessages
  public void testExtractMixedMessages() {
    
    Iterator<JsMessage> msgs = extractMessages(
        "var MSG_MONEY = function(amount) {",
        "  return 'You owe $' + amount +",
        "         ' to the credit card company.';",
        "};",
        "var MSG_TIME = goog.getMsg('You need to finish your work in ' +",
        "                           '{$duration} hours.', {'duration': d});",
        "var MSG_NAG = 'Clean your room.\\n\\nWash your clothes.';",
        "var MSG_NAG_HELP = 'Just some ' +",
        "                   'nags.';").iterator();

    assertEquals(
        new JsMessage.Builder("MSG_MONEY")
            .appendStringPart("You owe $")
            .appendPlaceholderReference("amount")
            .appendStringPart(" to the credit card company.")
            .build(),
        msgs.next());
    assertEquals(
        new JsMessage.Builder("MSG_TIME")
            .appendStringPart("You need to finish your work in ")
            .appendPlaceholderReference("duration")
            .appendStringPart(" hours.")
            .build(),
        msgs.next());
    assertEquals(
        new JsMessage.Builder("MSG_NAG")
            .appendStringPart("Clean your room.\n\nWash your clothes.")
            .setDesc("Just some nags.")
            .build(),
        msgs.next());
  }

// com.google.javascript.jscomp.JsMessageExtractorTest::testDuplicateUnnamedVariables
  public void testDuplicateUnnamedVariables() {
    
    
    Collection<JsMessage> msgs = extractMessages(
        "function a() {",
        "  var MSG_UNNAMED_2 = goog.getMsg('foo');",
        "}",
        "function b() {",
        "  var MSG_UNNAMED_2 = goog.getMsg('bar');",
        "}");

    assertEquals(2, msgs.size());
    final Iterator<JsMessage> iter = msgs.iterator();
    assertEquals("foo", iter.next().toString());
    assertEquals("bar", iter.next().toString());
  }

// com.google.javascript.jscomp.JsMessageExtractorTest::testMeaningAnnotation
  public void testMeaningAnnotation() {
    List<JsMessage> msgs = Lists.newArrayList(
        extractMessages(
            "var MSG_UNNAMED_1 = goog.getMsg('foo');",
            "var MSG_UNNAMED_2 = goog.getMsg('foo');"));
    assertEquals(2, msgs.size());
    assertTrue(msgs.get(0).getId().equals(msgs.get(1).getId()));
    assertEquals(msgs.get(0), msgs.get(1));

    msgs = Lists.newArrayList(
        extractMessages(
            "var MSG_UNNAMED_1 = goog.getMsg('foo');",
            " var MSG_UNNAMED_2 = goog.getMsg('foo');"));
    assertEquals(2, msgs.size());
    assertFalse(msgs.get(0).getId().equals(msgs.get(1).getId()));
  }

// com.google.javascript.jscomp.JsMessageVisitorTest::testJsMessageOnVar
  public void testJsMessageOnVar() {
    extractMessagesSafely(
        " var MSG_HELLO = goog.getMsg('a')");
    assertEquals(0, compiler.getWarningCount());
    assertEquals(1, messages.size());

    JsMessage msg = messages.get(0);
    assertEquals("MSG_HELLO", msg.getKey());
    assertEquals("Hello", msg.getDesc());
  }

// com.google.javascript.jscomp.JsMessageVisitorTest::testJsMessageOnProperty
  public void testJsMessageOnProperty() {
    extractMessagesSafely(" " +
        "pint.sub.MSG_MENU_MARK_AS_UNREAD = goog.getMsg('a')");
    assertEquals(0, compiler.getWarningCount());
    assertEquals(1, messages.size());

    JsMessage msg = messages.get(0);
    assertEquals("MSG_MENU_MARK_AS_UNREAD", msg.getKey());
    assertEquals("a", msg.getDesc());
  }

// com.google.javascript.jscomp.JsMessageVisitorTest::testOrphanedJsMessage
  public void testOrphanedJsMessage() {
    extractMessagesSafely("goog.getMsg('a')");
    assertEquals(1, compiler.getWarningCount());
    assertEquals(0, messages.size());

    JSError warn = compiler.getWarnings()[0];
    assertEquals(JsMessageVisitor.MESSAGE_NODE_IS_ORPHANED, warn.getType());
  }

// com.google.javascript.jscomp.JsMessageVisitorTest::testMessageWithoutDescription
  public void testMessageWithoutDescription() {
    extractMessagesSafely("var MSG_HELLO = goog.getMsg('a')");
    assertEquals(1, compiler.getWarningCount());
    assertEquals(1, messages.size());

    JsMessage msg = messages.get(0);
    assertEquals("MSG_HELLO", msg.getKey());

    assertEquals(JsMessageVisitor.MESSAGE_HAS_NO_DESCRIPTION,
        compiler.getWarnings()[0].getType());
  }

// com.google.javascript.jscomp.JsMessageVisitorTest::testIncorrectMessageReporting
  public void testIncorrectMessageReporting() {
    extractMessages("var MSG_HELLO = goog.getMsg('a' + + 'b')");
    assertEquals(1, compiler.getErrorCount());
    assertEquals(0, compiler.getWarningCount());
    assertEquals(0, messages.size());

    JSError mailformedTreeError = compiler.getErrors()[0];
    assertEquals(JsMessageVisitor.MESSAGE_TREE_MALFORMED,
        mailformedTreeError.getType());
    assertEquals("Message parse tree malformed. "
        + "STRING or ADD node expected; found: POS",
        mailformedTreeError.description);
  }

// com.google.javascript.jscomp.JsMessageVisitorTest::testEmptyMessage
  public void testEmptyMessage() {
    
    extractMessagesSafely("var MSG_EMPTY = '';");

    assertEquals(1, messages.size());
    JsMessage msg = messages.get(0);
    assertEquals("MSG_EMPTY", msg.getKey());
    assertEquals("", msg.toString());
  }

// com.google.javascript.jscomp.JsMessageVisitorTest::testConcatOfStrings
  public void testConcatOfStrings() {
    extractMessagesSafely("var MSG_NOTEMPTY = 'aa' + 'bbb' \n + ' ccc';");

    assertEquals(1, messages.size());
    JsMessage msg = messages.get(0);
    assertEquals("MSG_NOTEMPTY", msg.getKey());
    assertEquals("aabbb ccc", msg.toString());
  }

// com.google.javascript.jscomp.JsMessageVisitorTest::testLegacyFormatDescription
  public void testLegacyFormatDescription() {
    extractMessagesSafely("var MSG_SILLY = 'silly test message';\n"
        + "var MSG_SILLY_HELP = 'help text';");

    assertEquals(1, messages.size());
    JsMessage msg = messages.get(0);
    assertEquals("MSG_SILLY", msg.getKey());
    assertEquals("help text", msg.getDesc());
    assertEquals("silly test message", msg.toString());
  }

// com.google.javascript.jscomp.JsMessageVisitorTest::testLegacyFormatParametizedFunction
  public void testLegacyFormatParametizedFunction() {
    extractMessagesSafely("var MSG_SILLY = function(one, two) {"
        + "  return one + ', ' + two + ', buckle my shoe';"
        + "};");

    assertEquals(1, messages.size());
    JsMessage msg = messages.get(0);
    assertEquals("MSG_SILLY", msg.getKey());
    assertEquals(null, msg.getDesc());
    assertEquals("{$one}, {$two}, buckle my shoe", msg.toString());
  }

// com.google.javascript.jscomp.JsMessageVisitorTest::testLegacyMessageWithDescAnnotation
  public void testLegacyMessageWithDescAnnotation() {
    
    
    extractMessagesSafely(
        " var MSG_A = 'The Message';");

    assertEquals(1, messages.size());
    assertEquals(1, compiler.getWarningCount());
    JsMessage msg = messages.get(0);
    assertEquals("MSG_A", msg.getKey());
    assertEquals("The Message", msg.toString());
    assertEquals("The description", msg.getDesc());
  }

// com.google.javascript.jscomp.JsMessageVisitorTest::testLegacyMessageWithDescAnnotationAndHelpVar
  public void testLegacyMessageWithDescAnnotationAndHelpVar() {
    
    
    extractMessagesSafely(
        "var MSG_A_HELP = 'This is a help var';\n" +
        " var MSG_A = 'The Message';");

    assertEquals(1, messages.size());
    assertEquals(1, compiler.getWarningCount());
    JsMessage msg = messages.get(0);
    assertEquals("MSG_A", msg.getKey());
    assertEquals("The Message", msg.toString());
    assertEquals("The description in @desc", msg.getDesc());
  }

// com.google.javascript.jscomp.JsMessageVisitorTest::testClosureMessageWithHelpPostfix
  public void testClosureMessageWithHelpPostfix() {
    extractMessagesSafely("\n"
        + "var MSG_FOO_HELP = goog.getMsg('Help!');");

    assertEquals(1, messages.size());
    JsMessage msg = messages.get(0);
    assertEquals("MSG_FOO_HELP", msg.getKey());
    assertEquals("help text", msg.getDesc());
    assertEquals("Help!", msg.toString());
  }

// com.google.javascript.jscomp.JsMessageVisitorTest::testClosureMessageWithoutGoogGetmsg
  public void testClosureMessageWithoutGoogGetmsg() {
    allowLegacyMessages = false;

    extractMessages("var MSG_FOO_HELP = 'I am a bad message';");

    assertEquals(1, messages.size());
    assertEquals(1, compiler.getErrors().length);
    JSError error = compiler.getErrors()[0];
    assertEquals(JsMessageVisitor.MESSAGE_NOT_INITIALIZED_USING_NEW_SYNTAX,
        error.getType());
  }

// com.google.javascript.jscomp.JsMessageVisitorTest::testClosureFormatParametizedFunction
  public void testClosureFormatParametizedFunction() {
    extractMessagesSafely(""
        + "var MSG_SILLY = goog.getMsg('{$adjective} ' + 'message', "
        + "{'adjective': 'silly'});");

    assertEquals(1, messages.size());
    JsMessage msg = messages.get(0);
    assertEquals("MSG_SILLY", msg.getKey());
    assertEquals("help text", msg.getDesc());
    assertEquals("{$adjective} message", msg.toString());
  }

// com.google.javascript.jscomp.JsMessageVisitorTest::testHugeMessage
  public void testHugeMessage() {
    extractMessagesSafely("" +
        "var MSG_HUGE = goog.getMsg(" +
        "    '{$startLink_1}Google{$endLink}' +" +
        "    '{$startLink_2}blah{$endLink}{$boo}{$foo_001}{$boo}' +" +
        "    '{$foo_002}{$xxx_001}{$image}{$image_001}{$xxx_002}'," +
        "    {'startLink_1': '<a href=http://www.google.com/>'," +
        "     'endLink': '</a>'," +
        "     'startLink_2': '<a href=\"' + opt_data.url + '\">'," +
        "     'boo': opt_data.boo," +
        "     'foo_001': opt_data.foo," +
        "     'foo_002': opt_data.boo.foo," +
        "     'xxx_001': opt_data.boo + opt_data.foo," +
        "     'image': htmlTag7," +
        "     'image_001': opt_data.image," +
        "     'xxx_002': foo.callWithOnlyTopLevelKeys(" +
        "         bogusFn, opt_data, null, 'bogusKey1'," +
        "         opt_data.moo, 'bogusKey2', param10)});");

    assertEquals(1, messages.size());
    JsMessage msg = messages.get(0);
    assertEquals("MSG_HUGE", msg.getKey());
    assertEquals("A message with lots of stuff.", msg.getDesc());
    assertTrue(msg.isHidden());
    assertEquals("{$startLink_1}Google{$endLink}{$startLink_2}blah{$endLink}" +
        "{$boo}{$foo_001}{$boo}{$foo_002}{$xxx_001}{$image}" +
        "{$image_001}{$xxx_002}", msg.toString());
  }

// com.google.javascript.jscomp.JsMessageVisitorTest::testUnnamedGoogleMessage
  public void testUnnamedGoogleMessage() {
    extractMessagesSafely("var MSG_UNNAMED_2 = goog.getMsg('Hullo');");

    assertEquals(1, messages.size());
    JsMessage msg = messages.get(0);
    assertEquals(null, msg.getDesc());
    assertEquals("MSG_16LJMYKCXT84X", msg.getKey());
    assertEquals("MSG_16LJMYKCXT84X", msg.getId());
  }

// com.google.javascript.jscomp.JsMessageVisitorTest::testEmptyTextMessage
  public void testEmptyTextMessage() {
    extractMessagesSafely(" var MSG_FOO = goog.getMsg('');");

    assertEquals(1, messages.size());
    assertEquals(1, compiler.getWarningCount());
    assertEquals("Message value of MSG_FOO is just an empty string. "
        + "Empty messages are forbidden.",
        compiler.getWarnings()[0].description);
  }

// com.google.javascript.jscomp.JsMessageVisitorTest::testEmptyTextComplexMessage
  public void testEmptyTextComplexMessage() {
    extractMessagesSafely(" var MSG_BAR = goog.getMsg("
        + "'' + '' + ''     + ''\n+'');");

    assertEquals(1, messages.size());
    assertEquals(1, compiler.getWarningCount());
    assertEquals("Message value of MSG_BAR is just an empty string. "
        + "Empty messages are forbidden.",
        compiler.getWarnings()[0].description);
  }

// com.google.javascript.jscomp.JsMessageVisitorTest::testMessageIsNoUnnamed
  public void testMessageIsNoUnnamed() {
    extractMessagesSafely("var MSG_UNNAMED_ITEM = goog.getMsg('Hullo');");

    assertEquals(1, messages.size());
    JsMessage msg = messages.get(0);
    assertEquals("MSG_UNNAMED_ITEM", msg.getKey());
    assertFalse(msg.isHidden());
  }

// com.google.javascript.jscomp.JsMessageVisitorTest::testMsgVarWithoutAssignment
  public void testMsgVarWithoutAssignment() {
    extractMessages("var MSG_SILLY;");

    assertEquals(1, compiler.getErrors().length);
    JSError error = compiler.getErrors()[0];
    assertEquals(JsMessageVisitor.MESSAGE_HAS_NO_VALUE, error.getType());
  }

// com.google.javascript.jscomp.JsMessageVisitorTest::testRegularVarWithoutAssignment
  public void testRegularVarWithoutAssignment() {
    extractMessagesSafely("var SILLY;");

    assertTrue(messages.isEmpty());
  }

// com.google.javascript.jscomp.JsMessageVisitorTest::testMsgVarWithIncorrectRightSide
  public void testMsgVarWithIncorrectRightSide() {
    extractMessages("var MSG_SILLY = 0;");

    assertEquals(1, compiler.getErrors().length);
    JSError error = compiler.getErrors()[0];
    assertEquals("Message parse tree malformed. Cannot parse value of "
        + "message MSG_SILLY", error.description);
  }

// com.google.javascript.jscomp.JsMessageVisitorTest::testIncorrectMessage
  public void testIncorrectMessage() {
    extractMessages("DP_DatePicker.MSG_DATE_SELECTION = {};");

    assertEquals(0, messages.size());
    assertEquals(1, compiler.getErrors().length);
    JSError error = compiler.getErrors()[0];
    assertEquals("Message parse tree malformed. "+
                 "Message must be initialized using goog.getMsg function.",
                 error.description);
  }

// com.google.javascript.jscomp.JsMessageVisitorTest::testUnrecognizedFunction
  public void testUnrecognizedFunction() {
    allowLegacyMessages = false;
    extractMessages("DP_DatePicker.MSG_DATE_SELECTION = somefunc('a')");

    assertEquals(0, messages.size());
    assertEquals(1, compiler.getErrors().length);
    JSError error = compiler.getErrors()[0];
    assertEquals("Message parse tree malformed. "+
                 "Message initialized using unrecognized function. " +
                 "Please use goog.getMsg() instead.",
                 error.description);
  }

// com.google.javascript.jscomp.JsMessageVisitorTest::testExtractPropertyMessage
  public void testExtractPropertyMessage() {
    extractMessagesSafely(""
        + "a.b.MSG_SILLY = goog.getMsg(\n"
        + "    '{$adjective} ' + '{$someNoun}',\n"
        + "    {'adjective': adj, 'someNoun': noun});");

    assertEquals(1, messages.size());
    JsMessage msg = messages.get(0);
    assertEquals("MSG_SILLY", msg.getKey());
    assertEquals("{$adjective} {$someNoun}", msg.toString());
    assertEquals("A message that demonstrates placeholders", msg.getDesc());
    assertTrue(msg.isHidden());
  }

// com.google.javascript.jscomp.JsMessageVisitorTest::testAlmostButNotExternalMessage
  public void testAlmostButNotExternalMessage() {
    extractMessagesSafely(
        " var MSG_EXTERNAL = goog.getMsg('External');");
    assertEquals(0, compiler.getWarningCount());
    assertEquals(1, messages.size());
    assertFalse(messages.get(0).isExternal());
    assertEquals("MSG_EXTERNAL", messages.get(0).getKey());
  }

// com.google.javascript.jscomp.JsMessageVisitorTest::testExternalMessage
  public void testExternalMessage() {
    extractMessagesSafely("var MSG_EXTERNAL_111 = goog.getMsg('Hello World');");
    assertEquals(0, compiler.getWarningCount());
    assertEquals(1, messages.size());
    assertTrue(messages.get(0).isExternal());
    assertEquals("111", messages.get(0).getId());
  }

// com.google.javascript.jscomp.JsMessageVisitorTest::testIsValidMessageNameStrict
  public void testIsValidMessageNameStrict() {
    JsMessageVisitor visitor = new DummyJsVisitor(CLOSURE);

    assertTrue(visitor.isMessageName("MSG_HELLO", true));
    assertTrue(visitor.isMessageName("MSG_", true));
    assertTrue(visitor.isMessageName("MSG_HELP", true));
    assertTrue(visitor.isMessageName("MSG_FOO_HELP", true));

    assertFalse(visitor.isMessageName("_FOO_HELP", true));
    assertFalse(visitor.isMessageName("MSGFOOP", true));
  }

// com.google.javascript.jscomp.JsMessageVisitorTest::testIsValidMessageNameRelax
  public void testIsValidMessageNameRelax() {
    JsMessageVisitor visitor = new DummyJsVisitor(RELAX);

    assertFalse(visitor.isMessageName("MSG_HELP", false));
    assertFalse(visitor.isMessageName("MSG_FOO_HELP", false));
  }

// com.google.javascript.jscomp.JsMessageVisitorTest::testIsValidMessageNameLegacy
  public void testIsValidMessageNameLegacy() {
    theseAreLegacyMessageNames(new DummyJsVisitor(RELAX));
    theseAreLegacyMessageNames(new DummyJsVisitor(LEGACY));
  }

// com.google.javascript.jscomp.JsMessageVisitorTest::testUnexistedPlaceholders
  public void testUnexistedPlaceholders() {
    extractMessages("var MSG_FOO = goog.getMsg('{$foo}:', {});");

    assertEquals(0, messages.size());
    JSError[] errors = compiler.getErrors();
    assertEquals(1, errors.length);
    JSError error = errors[0];
    assertEquals(JsMessageVisitor.MESSAGE_TREE_MALFORMED, error.getType());
    assertEquals("Message parse tree malformed. Unrecognized message "
        + "placeholder referenced: foo", error.description);
  }

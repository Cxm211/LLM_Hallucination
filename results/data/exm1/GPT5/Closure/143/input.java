// buggy code
  static void createDefineReplacements(List<String> definitions,
      CompilerOptions options) {
    // Parse the definitions
    for (String override : definitions) {
      String[] assignment = override.split("=", 2);
      String defName = assignment[0];

      if (defName.length() > 0) {
        if (assignment.length == 1) {
          options.setDefineToBooleanLiteral(defName, true);
          continue;
        } else {
          String defValue = assignment[1];

          if (defValue.equals("true")) {
            options.setDefineToBooleanLiteral(defName, true);
            continue;
          } else if (defValue.equals("false")) {
            options.setDefineToBooleanLiteral(defName, false);
            continue;
          } else if (defValue.length() > 1
              && ((defValue.charAt(0) == '\'' &&
                  defValue.charAt(defValue.length() - 1) == '\'')
            )) {
            // If the value starts and ends with a single quote,
            // we assume that it's a string.
            String maybeStringVal =
                defValue.substring(1, defValue.length() - 1);
            if (maybeStringVal.indexOf(defValue.charAt(0)) == -1) {
              options.setDefineToStringLiteral(defName, maybeStringVal);
              continue;
            }
          } else {
            try {
              options.setDefineToDoubleLiteral(defName,
                  Double.parseDouble(defValue));
              continue;
            } catch (NumberFormatException e) {
              // do nothing, it will be caught at the end
            }
          }
        }
      }

      throw new RuntimeException(
          "--define flag syntax invalid: " + override);
    }
  }

    private void trySimplify(Node parent, Node node) {
      if (node.getType() != Token.EXPR_RESULT) {
        return;
      }

      Node exprBody = node.getFirstChild();
      if (!NodeUtil.nodeTypeMayHaveSideEffects(exprBody)
      ) {
        changeProxy.replaceWith(parent, node, getSideEffectNodes(exprBody));
      }
    }

// relevant test
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

// com.google.javascript.jscomp.CommandLineRunnerTest::testCheckUndefinedProperties
  public void testCheckUndefinedProperties() {
    args.add("--warning_level=VERBOSE");
    args.add("--jscomp_error=missingProperties");
    test("var x = {}; var y = x.bar;", TypeCheck.INEXISTENT_PROPERTY);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testDuplicateParams
  public void testDuplicateParams() {
    test("function (a, a) {}", RhinoErrorReporter.DUPLICATE_PARAM);
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
         "var FOO = true, BAR = 5, CCC = true, DDD = true;");
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
         "var goog = {}; goog.dom = {};");
    args.add("--process_closure_primitives=false");
    testSame("var goog = {}; goog.provide('goog.dom');");
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

// com.google.javascript.jscomp.CommandLineRunnerTest::testDebugFlag1
  public void testDebugFlag1() {
    args.add("--compilation_level=SIMPLE_OPTIMIZATIONS");
    args.add("--debug=false");
    testSame("function foo(a) {}");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testDebugFlag2
  public void testDebugFlag2() {
    args.add("--compilation_level=SIMPLE_OPTIMIZATIONS");
    args.add("--debug=true");
    test("function foo(a) {}",
         "function foo($a$$) {}");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testDebugFlag3
  public void testDebugFlag3() {
    args.add("--compilation_level=ADVANCED_OPTIMIZATIONS");
    args.add("--warning_level=QUIET");
    args.add("--debug=false");
    test("function Foo() {};" +
         "Foo.x = 1;" +
         "function f() {throw new Foo().x;} f();",
         "function a() {};" +
         "throw new a().a;");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testDebugFlag4
  public void testDebugFlag4() {
    args.add("--compilation_level=ADVANCED_OPTIMIZATIONS");
    args.add("--warning_level=QUIET");
    args.add("--debug=true");
    test("function Foo() {};" +
        "Foo.x = 1;" +
        "function f() {throw new Foo().x;} f();",
        "function $Foo$$() {};" +
        "throw new $Foo$$().$x$;");
  }

// com.google.javascript.jscomp.RemoveConstantExpressionsParallelTest::testOneFile
  public void testOneFile() {
    runInParallel(1);
  }

// com.google.javascript.jscomp.RemoveConstantExpressionsParallelTest::testTwoFiles
  public void testTwoFiles() {
    runInParallel(2);
  }

// com.google.javascript.jscomp.RemoveConstantExpressionsParallelTest::testFourFiles
  public void testFourFiles() {
    runInParallel(4);
  }

// com.google.javascript.jscomp.RemoveConstantExpressionsParallelTest::testManyFiles
  public void testManyFiles() {
    runInParallel(100);
  }

// com.google.javascript.jscomp.RemoveConstantExpressionsTest::testRemoveNumber
  public void testRemoveNumber() {
    test("3", "");
  }

// com.google.javascript.jscomp.RemoveConstantExpressionsTest::testRemoveVarGet1
  public void testRemoveVarGet1() {
    test("a", "");
  }

// com.google.javascript.jscomp.RemoveConstantExpressionsTest::testRemoveVarGet2
  public void testRemoveVarGet2() {
    test("var a = 1;a", "var a = 1");
  }

// com.google.javascript.jscomp.RemoveConstantExpressionsTest::testRemoveNamespaceGet1
  public void testRemoveNamespaceGet1() {
    test("var a = {};a.b", "var a = {}");
  }

// com.google.javascript.jscomp.RemoveConstantExpressionsTest::testRemoveNamespaceGet2
  public void testRemoveNamespaceGet2() {
    test("var a = {};a.b=1;a.b", "var a = {};a.b=1");
  }

// com.google.javascript.jscomp.RemoveConstantExpressionsTest::testRemovePrototypeGet1
  public void testRemovePrototypeGet1() {
    test("var a = {};a.prototype.b", "var a = {}");
  }

// com.google.javascript.jscomp.RemoveConstantExpressionsTest::testRemovePrototypeGet2
  public void testRemovePrototypeGet2() {
    test("var a = {};a.prototype.b = 1;a.prototype.b",
         "var a = {};a.prototype.b = 1");
  }

// com.google.javascript.jscomp.RemoveConstantExpressionsTest::testRemoveAdd1
  public void testRemoveAdd1() {
    test("1 + 2", "");
  }

// com.google.javascript.jscomp.RemoveConstantExpressionsTest::testNoRemoveVar1
  public void testNoRemoveVar1() {
    testSame("var a = 1");
  }

// com.google.javascript.jscomp.RemoveConstantExpressionsTest::testNoRemoveVar2
  public void testNoRemoveVar2() {
    testSame("var a = 1, b = 2");
  }

// com.google.javascript.jscomp.RemoveConstantExpressionsTest::testNoRemoveAssign1
  public void testNoRemoveAssign1() {
    testSame("a = 1");
  }

// com.google.javascript.jscomp.RemoveConstantExpressionsTest::testNoRemoveAssign2
  public void testNoRemoveAssign2() {
    testSame("a = b = 1");
  }

// com.google.javascript.jscomp.RemoveConstantExpressionsTest::testNoRemoveAssign3
  public void testNoRemoveAssign3() {
    test("1 + (a = 2)", "a = 2");
  }

// com.google.javascript.jscomp.RemoveConstantExpressionsTest::testNoRemoveAssign4
  public void testNoRemoveAssign4() {
    testSame("x.a = 1");
  }

// com.google.javascript.jscomp.RemoveConstantExpressionsTest::testNoRemoveAssign5
  public void testNoRemoveAssign5() {
    testSame("x.a = x.b = 1");
  }

// com.google.javascript.jscomp.RemoveConstantExpressionsTest::testNoRemoveAssign6
  public void testNoRemoveAssign6() {
    test("1 + (x.a = 2)", "x.a = 2");
  }

// com.google.javascript.jscomp.RemoveConstantExpressionsTest::testNoRemoveCall1
  public void testNoRemoveCall1() {
    testSame("a()");
  }

// com.google.javascript.jscomp.RemoveConstantExpressionsTest::testNoRemoveCall2
  public void testNoRemoveCall2() {
    test("a()+b()", "a();b()");
  }

// com.google.javascript.jscomp.RemoveConstantExpressionsTest::testNoRemoveCall3
  public void testNoRemoveCall3() {
    testSame("a() && b()");
  }

// com.google.javascript.jscomp.RemoveConstantExpressionsTest::testNoRemoveCall4
  public void testNoRemoveCall4() {
    testSame("a() || b()");
  }

// com.google.javascript.jscomp.RemoveConstantExpressionsTest::testNoRemoveCall5
  public void testNoRemoveCall5() {
    test("a() || 1", "a()");
  }

// com.google.javascript.jscomp.RemoveConstantExpressionsTest::testNoRemoveCall6
  public void testNoRemoveCall6() {
    testSame("1 || a()");
  }

// com.google.javascript.jscomp.RemoveConstantExpressionsTest::testNoRemoveThrow1
  public void testNoRemoveThrow1() {
    testSame("function f(){throw a()}");
  }

// com.google.javascript.jscomp.RemoveConstantExpressionsTest::testNoRemoveThrow2
  public void testNoRemoveThrow2() {
    testSame("function f(){throw a}");
  }

// com.google.javascript.jscomp.RemoveConstantExpressionsTest::testNoRemoveThrow3
  public void testNoRemoveThrow3() {
    testSame("function f(){throw 10}");
  }

// com.google.javascript.jscomp.RemoveConstantExpressionsTest::testRemoveInControlStructure1
  public void testRemoveInControlStructure1() {
    test("if(2) 1", "if(2);");
  }

// com.google.javascript.jscomp.RemoveConstantExpressionsTest::testRemoveInControlStructure2
  public void testRemoveInControlStructure2() {
    test("while(2) 1", "while(2);");
  }

// com.google.javascript.jscomp.RemoveConstantExpressionsTest::testRemoveInControlStructure3
  public void testRemoveInControlStructure3() {
    test("for(1;2;3) 4", "for(1;2;3);");
  }

// com.google.javascript.jscomp.RemoveConstantExpressionsTest::testHook1
  public void testHook1() {
    test("1 ? 2 : 3", "");
  }

// com.google.javascript.jscomp.RemoveConstantExpressionsTest::testHook2
  public void testHook2() {
    test("1 ? a() : 3", "1 && a()");
  }

// com.google.javascript.jscomp.RemoveConstantExpressionsTest::testHook3
  public void testHook3() {
    test("1 ? 2 : a()", "1 || a()");
  }

// com.google.javascript.jscomp.RemoveConstantExpressionsTest::testHook4
  public void testHook4() {
    testSame("1 ? a() : b()");
  }

// com.google.javascript.jscomp.RemoveConstantExpressionsTest::testHook5
  public void testHook5() {
    test("a() ? 1 : 2", "a()");
  }

// com.google.javascript.jscomp.RemoveConstantExpressionsTest::testHook6
  public void testHook6() {
    test("a() ? b() : 2", "a() && b()");
  }

// com.google.javascript.jscomp.RemoveConstantExpressionsTest::testHook7
  public void testHook7() {
    test("a() ? 1 : b()", "a() || b()");
  }

// com.google.javascript.jscomp.RemoveConstantExpressionsTest::testHook8
  public void testHook8() {
    testSame("a() ? b() : c()");
  }

// com.google.javascript.jscomp.RemoveConstantExpressionsTest::testShortCircuit1
  public void testShortCircuit1() {
    testSame("1 && a()");
  }

// com.google.javascript.jscomp.RemoveConstantExpressionsTest::testShortCircuit2
  public void testShortCircuit2() {
    test("1 && a() && 2", "1 && a()");
  }

// com.google.javascript.jscomp.RemoveConstantExpressionsTest::testShortCircuit3
  public void testShortCircuit3() {
    test("a() && 1 && 2", "a()");
  }

// com.google.javascript.jscomp.RemoveConstantExpressionsTest::testComma1
  public void testComma1() {
    test("1, 2", "");
  }

// com.google.javascript.jscomp.RemoveConstantExpressionsTest::testComma2
  public void testComma2() {
    test("1, a()", "a()");
  }

// com.google.javascript.jscomp.RemoveConstantExpressionsTest::testComma3
  public void testComma3() {
    test("1, a(), b()", "a();b()");
  }

// com.google.javascript.jscomp.RemoveConstantExpressionsTest::testComma4
  public void testComma4() {
    test("a(), b()", "a();b()");
  }

// com.google.javascript.jscomp.RemoveConstantExpressionsTest::testComma5
  public void testComma5() {
    test("a(), b(), 1", "a();b()");
  }

// com.google.javascript.jscomp.RemoveConstantExpressionsTest::testComplex1
  public void testComplex1() {
    test("1 && a() + b() + c()", "1 && (a(), b(), c())");
  }

// com.google.javascript.jscomp.RemoveConstantExpressionsTest::testComplex2
  public void testComplex2() {
    test("1 && (a() ? b() : 1)", "1 && a() && b()");
  }

// com.google.javascript.jscomp.RemoveConstantExpressionsTest::testComplex3
  public void testComplex3() {
    test("1 && (a() ? b() : 1 + c())", "1 && (a() ? b() : c())");
  }

// com.google.javascript.jscomp.RemoveConstantExpressionsTest::testComplex4
  public void testComplex4() {
    test("1 && (a() ? 1 : 1 + c())", "1 && (a() || c())");
  }

// com.google.javascript.jscomp.RemoveConstantExpressionsTest::testComplex5
  public void testComplex5() {
    
    testSame("(a() ? 1 : 1 + c()) && foo()");
  }

// com.google.javascript.jscomp.RemoveConstantExpressionsTest::testNoRemoveFunctionDeclaration1
  public void testNoRemoveFunctionDeclaration1() {
    testSame("function foo(){}");
  }

// com.google.javascript.jscomp.RemoveConstantExpressionsTest::testNoRemoveFunctionDeclaration2
  public void testNoRemoveFunctionDeclaration2() {
    testSame("var foo = function (){}");
  }

// com.google.javascript.jscomp.RemoveConstantExpressionsTest::testNoSimplifyFunctionArgs1
  public void testNoSimplifyFunctionArgs1() {
    testSame("f(1 + 2, 3 + g())");
  }

// com.google.javascript.jscomp.RemoveConstantExpressionsTest::testNoSimplifyFunctionArgs2
  public void testNoSimplifyFunctionArgs2() {
    testSame("1 && f(1 + 2, 3 + g())");
  }

// com.google.javascript.jscomp.RemoveConstantExpressionsTest::testNoSimplifyFunctionArgs3
  public void testNoSimplifyFunctionArgs3() {
    testSame("1 && foo(a() ? b() : 1 + c())");
  }

// com.google.javascript.jscomp.RemoveConstantExpressionsTest::testNoRemoveInherits1
  public void testNoRemoveInherits1() {
    testSame("var a = {}; this.b = {}; var goog = {}; goog.inherits(b, a)");
  }

// com.google.javascript.jscomp.RemoveConstantExpressionsTest::testNoRemoveInherits2
  public void testNoRemoveInherits2() {
    test("var a = {}; this.b = {}; var goog = {}; goog.inherits(b, a) + 1",
         "var a = {}; this.b = {}; var goog = {}; goog.inherits(b, a)");
  }

// com.google.javascript.jscomp.RemoveConstantExpressionsTest::testNoRemoveInherits3
  public void testNoRemoveInherits3() {
    testSame("this.a = {}; var b = {}; b.inherits(a);");
  }

// com.google.javascript.jscomp.RemoveConstantExpressionsTest::testNoRemoveInherits4
  public void testNoRemoveInherits4() {
    test("this.a = {}; var b = {}; b.inherits(a) + 1;",
         "this.a = {}; var b = {}; b.inherits(a)");
  }

// com.google.javascript.jscomp.RemoveConstantExpressionsTest::testRemoveFromLabel1
  public void testRemoveFromLabel1() {
    test("LBL: void 0", "LBL: {}");
  }

// com.google.javascript.jscomp.RemoveConstantExpressionsTest::testRemoveFromLabel2
  public void testRemoveFromLabel2() {
    test("LBL: foo() + 1 + bar()", "LBL: {foo();bar()}");
  }

// com.google.javascript.jscomp.RemoveConstantExpressionsTest::testCall1
  public void testCall1() {
    test("Math.sin(0);", "");
  }

// com.google.javascript.jscomp.RemoveConstantExpressionsTest::testCall2
  public void testCall2() {
    test("1 + Math.sin(0);", "");
  }

// com.google.javascript.jscomp.RemoveConstantExpressionsTest::testNew1
  public void testNew1() {
    test("new Date;", "");
  }

// com.google.javascript.jscomp.RemoveConstantExpressionsTest::testNew2
  public void testNew2() {
    test("1 + new Date;", "");
  }

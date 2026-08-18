// buggy code
  public Iterator<Var> getVars() {
    return vars.values().iterator();
  }

    public void enterScope(NodeTraversal t) {
      Node n = t.getCurrentNode().getParent();
      if (n != null && isCallToScopeMethod(n)) {
        transformation = transformationHandler.logAliasTransformation(
            n.getSourceFileName(), getSourceRegion(n));
      }
    }

    private void report(NodeTraversal t, Node n, DiagnosticType error,
        String... arguments) {
      compiler.report(t.makeError(n, error, arguments));
      hasErrors = true;
    }

    public void visit(NodeTraversal t, Node n, Node parent) {
      if (isCallToScopeMethod(n)) {
        validateScopeCall(t, n, n.getParent());
      }



      // Validate the top level of the goog.scope block.
      if (t.getScopeDepth() == 2) {
        int type = n.getType();
        if (type == Token.NAME && parent.getType() == Token.VAR) {
          if (n.hasChildren() && n.getFirstChild().isQualifiedName()) {
            String name = n.getString();
            Var aliasVar = t.getScope().getVar(name);
            aliases.put(name, aliasVar);
            aliasDefinitionsInOrder.add(n);

            String qualifiedName =
                aliasVar.getInitialValue().getQualifiedName();
            transformation.addAlias(name, qualifiedName);
            // Return early, to ensure that we don't record a definition
            // twice.
            return;
          } else {
            report(t, n, GOOG_SCOPE_NON_ALIAS_LOCAL, n.getString());
          }
        }
        if (type == Token.NAME && NodeUtil.isAssignmentOp(parent) &&
            n == parent.getFirstChild()) {
            report(t, n, GOOG_SCOPE_ALIAS_REDEFINED, n.getString());
        }

        if (type == Token.RETURN) {
          report(t, n, GOOG_SCOPE_USES_RETURN);
        } else if (type == Token.THIS) {
          report(t, n, GOOG_SCOPE_REFERENCES_THIS);
        } else if (type == Token.THROW) {
          report(t, n, GOOG_SCOPE_USES_THROW);
        }
      }

      // Validate all descendent scopes of the goog.scope block.
      if (t.getScopeDepth() >= 2) {
        // Check if this name points to an alias.
        if (n.getType() == Token.NAME) {
          String name = n.getString();
          Var aliasVar = aliases.get(name);
          if (aliasVar != null &&
              t.getScope().getVar(name) == aliasVar) {
          // Note, to support the transitive case, it's important we don't
          // clone aliasedNode here.  For example,
          // var g = goog; var d = g.dom; d.createElement('DIV');
          // The node in aliasedNode (which is "g") will be replaced in the
          // changes pass above with "goog".  If we cloned here, we'd end up
          // with <code>g.dom.createElement('DIV')</code>.
          Node aliasedNode = aliasVar.getInitialValue();
          aliasUsages.add(new AliasedNode(n, aliasedNode));
          }
        }

        JSDocInfo info = n.getJSDocInfo();
        if (info != null) {
          for (Node node : info.getTypeNodes()) {
            fixTypeNode(node);
          }
        }

        // TODO(robbyw): Error for goog.scope not at root.
      }
    }

// relevant test
// com.google.javascript.jscomp.InlineFunctionsTest::testAnonymous2
  public void testAnonymous2() {
    testSame("(function(){eval();(function(){var b=a;a++;alert(b)})()})();");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testAnonymous3
  public void testAnonymous3() {
    
    assumeMinimumCapture = false;
    testSame("(function(){var a=10;(function(){arguments;})()})();");

    assumeMinimumCapture = true;
    test("(function(){var a=10;(function(){arguments;})()})();",
         "{var a$$inline_1=10;(function(){arguments;})();}");

    test("(function(){(function(){arguments;})()})();",
        "{(function(){arguments;})()}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testLoopWithFunctionWithFunction
  public void testLoopWithFunctionWithFunction() {
    assumeMinimumCapture = true;
    test("function _testLocalVariableInLoop_() {\n" +
        "  var result = 0;\n" +
        "  function foo() {\n" +
        "    var arr = [1, 2, 3, 4, 5];\n" +
        "    for (var i = 0, l = arr.length; i < l; i++) {\n" +
        "      var j = arr[i];\n" +
        
        
        "      (function() {\n" +
        "        var k = j;\n" +
        "        setTimeout(function() { result += k; }, 5 * i);\n" +
        "      })();\n" +
        "    }\n" +
        "  }\n" +
        "  foo();\n" +
        "}",
        "function _testLocalVariableInLoop_(){\n" +
        "  var result=0;\n" +
        "  {" +
        "  var arr$$inline_4=[1,2,3,4,5];\n" +
        "  var i$$inline_5=0;\n" +
        "  var l$$inline_6=arr$$inline_4.length;\n" +
        "  for(;i$$inline_5<l$$inline_6;i$$inline_5++){\n" +
        "    var j$$inline_7=arr$$inline_4[i$$inline_5];\n" +
        "    (function(){\n" +
        "       var k$$inline_8=j$$inline_7;\n" +
        "       setTimeout(function(){result+=k$$inline_8},5*i$$inline_5)\n" +
        "     })()\n" +
        "  }\n" +
        "  }\n" +
        "}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testMethodWithFunctionWithFunction
  public void testMethodWithFunctionWithFunction() {
    assumeMinimumCapture = true;
    test("function _testLocalVariable_() {\n" +
        "  var result = 0;\n" +
        "  function foo() {\n" +
        "      var j = [i];\n" +
        "      (function(j) {\n" +
        "        setTimeout(function() { result += j; }, 5 * i);\n" +
        "      })(j);\n" +
        "      j = null;" +
        "  }\n" +
        "  foo();\n" +
        "}",
        "function _testLocalVariable_(){\n" +
        "  var result=0;\n" +
        "  {\n" +
        "  var j$$inline_4=[i];\n" +
        "  {\n" +
        "  var j$$inline_1=j$$inline_4;\n" +  
        "  setTimeout(function(){result+=j$$inline_1},5*i);\n" +
        "  }\n" +
        "  j$$inline_4=null\n" + 
        "  }\n" +
        "}");
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

// com.google.javascript.jscomp.InlineObjectLiteralsTest::testObject0
  public void testObject0() {
    
    testSame("var a = {x:1}; f(a.x);");
  }

// com.google.javascript.jscomp.InlineObjectLiteralsTest::testObject1
  public void testObject1() {
    testLocal("var a = {x:x(), y:y()}; f(a.x, a.y);",
         "var JSCompiler_object_inline_x_0=x();" +
         "var JSCompiler_object_inline_y_1=y();" +
         "f(JSCompiler_object_inline_x_0, JSCompiler_object_inline_y_1);");
  }

// com.google.javascript.jscomp.InlineObjectLiteralsTest::testObject1a
  public void testObject1a() {
    testLocal("var a; a = {x:x, y:y}; f(a.x, a.y);",
         "var JSCompiler_object_inline_x_0;" +
         "var JSCompiler_object_inline_y_1;" +
         "(JSCompiler_object_inline_x_0=x," +
         "JSCompiler_object_inline_y_1=y, true);" +
         "f(JSCompiler_object_inline_x_0, JSCompiler_object_inline_y_1);");
  }

// com.google.javascript.jscomp.InlineObjectLiteralsTest::testObject2
  public void testObject2() {
    testLocal("var a = {y:y}; a.x = z; f(a.x, a.y);",
         "var JSCompiler_object_inline_y_0 = y;" +
         "var JSCompiler_object_inline_x_1;" +
         "JSCompiler_object_inline_x_1=z;" +
         "f(JSCompiler_object_inline_x_1, JSCompiler_object_inline_y_0);");
  }

// com.google.javascript.jscomp.InlineObjectLiteralsTest::testObject3
  public void testObject3() {
    
    
    testSameLocal("var a = {y:y,x:x}; a.y(); f(a.x);");
    testSameLocal("var a; a = {y:y,x:x}; a.y(); f(a.x);");
  }

// com.google.javascript.jscomp.InlineObjectLiteralsTest::testObject4
  public void testObject4() {
    
    testSameLocal("var a = {y:y}; a.x = z; f(a.x, a.y); g(a);");
    testSameLocal("var a; a = {y:y}; a.x = z; f(a.x, a.y); g(a);");
  }

// com.google.javascript.jscomp.InlineObjectLiteralsTest::testObject5
  public void testObject5() {
    testLocal("var a = {x:x, y:y}; var b = {a:a}; f(b.a.x, b.a.y);",
         "var a = {x:x, y:y};" +
         "var JSCompiler_object_inline_a_0=a;" +
         "f(JSCompiler_object_inline_a_0.x, JSCompiler_object_inline_a_0.y);");
  }

// com.google.javascript.jscomp.InlineObjectLiteralsTest::testObject6
  public void testObject6() {
    testLocal("for (var i = 0; i < 5; i++) { var a = {i:i,x:x}; f(a.i, a.x); }",
         "for (var i = 0; i < 5; i++) {" +
         "  var JSCompiler_object_inline_i_0=i;" +
         "  var JSCompiler_object_inline_x_1=x;" +
         "  f(JSCompiler_object_inline_i_0,JSCompiler_object_inline_x_1)" +
         "}");
    testLocal("if (c) { var a = {i:i,x:x}; f(a.i, a.x); }",
         "if (c) {" +
         "  var JSCompiler_object_inline_i_0=i;" +
         "  var JSCompiler_object_inline_x_1=x;" +
         "  f(JSCompiler_object_inline_i_0,JSCompiler_object_inline_x_1)" +
         "}");
  }

// com.google.javascript.jscomp.InlineObjectLiteralsTest::testObject7
  public void testObject7() {
    testLocal("var a = {x:x, y:f()}; g(a.x);",
      "var JSCompiler_object_inline_x_0=x;" +
         "var JSCompiler_object_inline_y_1=f();" +
         "g(JSCompiler_object_inline_x_0)");
  }

// com.google.javascript.jscomp.InlineObjectLiteralsTest::testObject8
  public void testObject8() {
    testSameLocal("var a = {x:x,y:y}; var b = {x:y}; f((c?a:b).x);");

    testLocal("var a; if(c) { a={x:x, y:y}; } else { a={x:y}; } f(a.x);",
         "var JSCompiler_object_inline_x_0;" +
         "var JSCompiler_object_inline_y_1;" +
         "if(c) JSCompiler_object_inline_x_0=x," +
         "      JSCompiler_object_inline_y_1=y," +
         "      true;" +
         "else JSCompiler_object_inline_x_0=y," +
         "     JSCompiler_object_inline_y_1=void 0," +
         "     true;" +
         "f(JSCompiler_object_inline_x_0)");
    testLocal("var a = {x:x,y:y}; var b = {x:y}; c ? f(a.x) : f(b.x);",
         "var JSCompiler_object_inline_x_0 = x; " +
         "var JSCompiler_object_inline_y_1 = y; " +
         "var JSCompiler_object_inline_x_2 = y; " +
         "c ? f(JSCompiler_object_inline_x_0):f(JSCompiler_object_inline_x_2)");
  }

// com.google.javascript.jscomp.InlineObjectLiteralsTest::testObject9
  public void testObject9() {
    
    testSameLocal("function f(a,b) {" +
             "  var x = {a:a,b:b}; x.a(); return x.b;" +
             "}");

    testLocal("function f(a,b) {" +
         "  var x = {a:a,b:b}; g(x.a); x = {a:a,b:2}; return x.b;" +
         "}",
         "function f(a,b) {" +
         "  var JSCompiler_object_inline_a_0 = a;" +
         "  var JSCompiler_object_inline_b_1 = b;" +
         "  g(JSCompiler_object_inline_a_0);" +
         "  JSCompiler_object_inline_a_0 = a," +
         "  JSCompiler_object_inline_b_1=2," +
         "  true;" +
         "  return JSCompiler_object_inline_b_1" +
         "}");

    testLocal("function f(a,b) { " +
         "  var x = {a:a,b:b}; g(x.a); x.b = x.c = 2; return x.b; " +
         "}",
         "function f(a,b) { " +
         "  var JSCompiler_object_inline_a_0=a;" +
         "  var JSCompiler_object_inline_b_1=b; " +
         "  var JSCompiler_object_inline_c_2;" +
         "  g(JSCompiler_object_inline_a_0);" +
         "  JSCompiler_object_inline_b_1=JSCompiler_object_inline_c_2=2;" +
         "  return JSCompiler_object_inline_b_1" +
         "}");
  }

// com.google.javascript.jscomp.InlineObjectLiteralsTest::testObject10
  public void testObject10() {
    testLocal("var x; var b = f(); x = {a:a, b:b}; if(x.a) g(x.b);",
         "var JSCompiler_object_inline_a_0;" +
         "var JSCompiler_object_inline_b_1;" +
         "var b = f();" +
         "JSCompiler_object_inline_a_0=a,JSCompiler_object_inline_b_1=b,true;" +
         "if(JSCompiler_object_inline_a_0) g(JSCompiler_object_inline_b_1)");
    testLocal("var x = {}; var b = f(); x = {a:a, b:b}; if(x.a) g(x.b) + x.c",
         "var JSCompiler_object_inline_a_0;" +
         "var JSCompiler_object_inline_b_1;" +
         "var JSCompiler_object_inline_c_2;" +
         "var b=f();" +
         "JSCompiler_object_inline_a_0=a,JSCompiler_object_inline_b_1=b," +
         "  JSCompiler_object_inline_c_2=void 0,true;" +
         "if(JSCompiler_object_inline_a_0) " +
         "  g(JSCompiler_object_inline_b_1) + JSCompiler_object_inline_c_2");
    testLocal("var x; var b = f(); x = {a:a, b:b}; x.c = c; if(x.a) g(x.b) + x.c",
         "var JSCompiler_object_inline_a_0;" +
         "var JSCompiler_object_inline_b_1;" +
         "var JSCompiler_object_inline_c_2;" +
         "var b = f();" +
         "JSCompiler_object_inline_a_0 = a,JSCompiler_object_inline_b_1 = b, " +
         "  JSCompiler_object_inline_c_2=void 0,true;" +
         "JSCompiler_object_inline_c_2 = c;" +
         "if (JSCompiler_object_inline_a_0)" +
         "  g(JSCompiler_object_inline_b_1) + JSCompiler_object_inline_c_2;");
    testLocal("var x = {a:a}; if (b) x={b:b}; f(x.a||x.b);",
         "var JSCompiler_object_inline_a_0 = a;" +
         "var JSCompiler_object_inline_b_1;" +
         "if(b) JSCompiler_object_inline_b_1 = b," +
         "      JSCompiler_object_inline_a_0 = void 0," +
         "      true;" +
         "f(JSCompiler_object_inline_a_0 || JSCompiler_object_inline_b_1)");
    testLocal("var x; var y = 5; x = {a:a, b:b, c:c}; if (b) x={b:b}; f(x.a||x.b);",
         "var JSCompiler_object_inline_a_0;" +
         "var JSCompiler_object_inline_b_1;" +
         "var JSCompiler_object_inline_c_2;" +
         "var y=5;" +
         "JSCompiler_object_inline_a_0=a," +
         "JSCompiler_object_inline_b_1=b," +
         "JSCompiler_object_inline_c_2=c," +
         "true;" +
         "if (b) JSCompiler_object_inline_b_1=b," +
         "       JSCompiler_object_inline_a_0=void 0," +
         "       JSCompiler_object_inline_c_2=void 0," +
         "       true;" +
         "f(JSCompiler_object_inline_a_0||JSCompiler_object_inline_b_1)");
  }

// com.google.javascript.jscomp.InlineObjectLiteralsTest::testObject11
  public void testObject11() {
    testSameLocal("var x = {a:b}; (x = {a:a}).c = 5; f(x.a);");
    testSameLocal("var x = {a:a}; f(x[a]); g(x[a]);");
  }

// com.google.javascript.jscomp.InlineObjectLiteralsTest::testObject12
  public void testObject12() {
    testLocal("var a; a = {x:1, y:2}; f(a.x, a.y2);",
         "var JSCompiler_object_inline_x_0;" +
         "var JSCompiler_object_inline_y_1;" +
         "var JSCompiler_object_inline_y2_2;" +
         "JSCompiler_object_inline_x_0=1," +
         "JSCompiler_object_inline_y_1=2," +
         "JSCompiler_object_inline_y2_2=void 0," +
         "true;" +
         "f(JSCompiler_object_inline_x_0, JSCompiler_object_inline_y2_2);");
  }

// com.google.javascript.jscomp.InlineObjectLiteralsTest::testObject13
  public void testObject13() {
    testSameLocal("var x = {a:1, b:2}; x = {a:3, b:x.a};");
  }

// com.google.javascript.jscomp.InlineObjectLiteralsTest::testObject14
  public void testObject14() {
    testSameLocal("var x = {a:1}; if ('a' in x) { f(); }");
    testSameLocal("var x = {a:1}; for (var y in x) { f(y); }");
  }

// com.google.javascript.jscomp.InlineObjectLiteralsTest::testObject15
  public void testObject15() {
    testSameLocal("x = x || {}; f(x.a);");
  }

// com.google.javascript.jscomp.InlineObjectLiteralsTest::testObject16
  public void testObject16() {
    testLocal("function f(e) { bar(); x = {a: foo()}; var x; print(x.a); }",
         "function f(e) { " +
         "  var JSCompiler_object_inline_a_0;" +
         "  bar();" +
         "  JSCompiler_object_inline_a_0 = foo(), true;" +
         "  print(JSCompiler_object_inline_a_0);" +
         "}");
  }

// com.google.javascript.jscomp.InlineObjectLiteralsTest::testObject17
  public void testObject17() {
    
    
    testSameLocal(
      "var a = {a: function(){}};" +
      "a.a();" +
      "a = {a1: 100};" +
      "print(a.a1);");
  }

// com.google.javascript.jscomp.InlineObjectLiteralsTest::testObject18
  public void testObject18() {
    testSameLocal("var a,b; b=a={x:x, y:y}; f(b.x);");
  }

// com.google.javascript.jscomp.InlineObjectLiteralsTest::testObject19
  public void testObject19() {
    testSameLocal("var a,b; if(c) { b=a={x:x, y:y}; } else { b=a={x:y}; } f(b.x);");
  }

// com.google.javascript.jscomp.InlineObjectLiteralsTest::testObject20
  public void testObject20() {
    testSameLocal("var a,b; if(c) { b=a={x:x, y:y}; } else { b=a={x:y}; } f(a.x);");
  }

// com.google.javascript.jscomp.InlineObjectLiteralsTest::testObject21
  public void testObject21() {
    testSameLocal("var a,b; b=a={x:x, y:y};");
    testSameLocal("var a,b; if(c) { b=a={x:x, y:y}; }" +
             "else { b=a={x:y}; } f(a.x); f(b.x)");
    testSameLocal("var a, b; if(c) { if (a={x:x, y:y}) f(); } " +
             "else { b=a={x:y}; } f(a.x);");
    testSameLocal("var a,b; b = (a = {x:x, y:x});");
    testSameLocal("var a,b; a = {x:x, y:x}; b = a");
    testSameLocal("var a,b; a = {x:x, y:x}; b = x || a");
    testSameLocal("var a,b; a = {x:x, y:x}; b = y && a");
    testSameLocal("var a,b; a = {x:x, y:x}; b = y ? a : a");
    testSameLocal("var a,b; a = {x:x, y:x}; b = y , a");
    testSameLocal("b = x || (a = {x:1, y:2});");
  }

// com.google.javascript.jscomp.InlineObjectLiteralsTest::testObject22
  public void testObject22() {
    testLocal("while(1) { var a = {y:1}; if (b) a.x = 2; f(a.y, a.x);}",
      "for(;1;){" +
      " var JSCompiler_object_inline_y_0=1;" +
      " var JSCompiler_object_inline_x_1;" +
      " if(b) JSCompiler_object_inline_x_1=2;" +
      " f(JSCompiler_object_inline_y_0,JSCompiler_object_inline_x_1)" +
      "}");

    testLocal("var a; while (1) { f(a.x, a.y); a = {x:1, y:1};}",
      "var JSCompiler_object_inline_x_0;" +
      "var JSCompiler_object_inline_y_1;" +
      "for(;1;) {" +
      " f(JSCompiler_object_inline_x_0,JSCompiler_object_inline_y_1);" +
      " JSCompiler_object_inline_x_0=1," +
      " JSCompiler_object_inline_y_1=1," +
      " true" +
      "}");
  }

// com.google.javascript.jscomp.InlineObjectLiteralsTest::testObject23
  public void testObject23() {
    testLocal("function f() {\n" +
         "  var templateData = {\n" +
         "    linkIds: {\n" +
         "      CHROME: 'cl',\n" +
         "      DISMISS: 'd'\n" +
         "    }\n" +
         "  };\n" +
         "  var html = templateData.linkIds.CHROME \n" +
         "       + \":\" + templateData.linkIds.DISMISS;\n" +
         "}",
         "function f(){" +
         "var JSCompiler_object_inline_CHROME_1='cl';" +
         "var JSCompiler_object_inline_DISMISS_2='d';" +
         "var html=JSCompiler_object_inline_CHROME_1 +" +
         " ':' +JSCompiler_object_inline_DISMISS_2}");
  }

// com.google.javascript.jscomp.InlineObjectLiteralsTest::testObject24
  public void testObject24() {
    testLocal("function f() {\n" +
         "  var linkIds = {\n" +
         "      CHROME: 1,\n" +
         "  };\n" +
         "  var g = function () {var o = {a: linkIds};}\n" +
         "}",
         "function f(){var linkIds={CHROME:1};" +
         "var g=function(){var JSCompiler_object_inline_a_0=linkIds}}");
  }

// com.google.javascript.jscomp.InlineObjectLiteralsTest::testObject25
  public void testObject25() {
    testLocal("var a = {x:f(), y:g()}; a = {y:g(), x:f()}; f(a.x, a.y);",
         "var JSCompiler_object_inline_x_0=f();" +
         "var JSCompiler_object_inline_y_1=g();" +
         "JSCompiler_object_inline_y_1=g()," +
         "  JSCompiler_object_inline_x_0=f()," +
         "  true;" +
         "f(JSCompiler_object_inline_x_0,JSCompiler_object_inline_y_1)");
  }

// com.google.javascript.jscomp.InlineObjectLiteralsTest::testObject26
  public void testObject26() {
    testLocal("var a = {}; a.b = function() {}; new a.b.c",
         "var JSCompiler_object_inline_b_0;" +
         "JSCompiler_object_inline_b_0=function(){};" +
         "new JSCompiler_object_inline_b_0.c");
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

// com.google.javascript.jscomp.InlineVariablesTest::testNoInlineRedeclaredExterns
  public void testNoInlineRedeclaredExterns() {
    String externs = "var test = 1;";
    String code = " var test = 2;alert(test);";
    test(externs, code, code, null, null);
  }

// com.google.javascript.jscomp.LinkedFlowScopeTest::testOptimize
  public void testOptimize() {
    assertEquals(localEntry, localEntry.optimize());

    FlowScope child = localEntry.createChildFlowScope();
    assertEquals(localEntry, child.optimize());

    child.inferSlotType("localB", NUMBER_TYPE);
    assertEquals(child, child.optimize());
  }

// com.google.javascript.jscomp.LinkedFlowScopeTest::testJoin1
  public void testJoin1() {
    FlowScope childA = localEntry.createChildFlowScope();
    childA.inferSlotType("localB", NUMBER_TYPE);

    FlowScope childAB = childA.createChildFlowScope();
    childAB.inferSlotType("localB", STRING_TYPE);

    FlowScope childB = localEntry.createChildFlowScope();
    childB.inferSlotType("localB", BOOLEAN_TYPE);

    assertEquals(STRING_TYPE, childAB.getSlot("localB").getType());
    assertEquals(BOOLEAN_TYPE, childB.getSlot("localB").getType());
    assertNull(childB.getSlot("localA").getType());

    FlowScope joined = join(childB, childAB);
    assertEquals(createUnionType(STRING_TYPE, BOOLEAN_TYPE),
        joined.getSlot("localB").getType());
    assertNull(joined.getSlot("localA").getType());

    joined = join(childAB, childB);
    assertEquals(createUnionType(STRING_TYPE, BOOLEAN_TYPE),
        joined.getSlot("localB").getType());
    assertNull(joined.getSlot("localA").getType());

    assertEquals("Join should be symmetric",
        join(childB, childAB), join(childAB, childB));
  }

// com.google.javascript.jscomp.LinkedFlowScopeTest::testJoin2
  public void testJoin2() {
    FlowScope childA = localEntry.createChildFlowScope();
    childA.inferSlotType("localA", STRING_TYPE);

    FlowScope childB = localEntry.createChildFlowScope();
    childB.inferSlotType("globalB", BOOLEAN_TYPE);

    assertEquals(STRING_TYPE, childA.getSlot("localA").getType());
    assertEquals(BOOLEAN_TYPE, childB.getSlot("globalB").getType());
    assertNull(childB.getSlot("localB").getType());

    FlowScope joined = join(childB, childA);
    assertEquals(STRING_TYPE, joined.getSlot("localA").getType());
    assertEquals(BOOLEAN_TYPE, joined.getSlot("globalB").getType());

    joined = join(childA, childB);
    assertEquals(STRING_TYPE, joined.getSlot("localA").getType());
    assertEquals(BOOLEAN_TYPE, joined.getSlot("globalB").getType());

    assertEquals("Join should be symmetric",
        join(childB, childA), join(childA, childB));
  }

// com.google.javascript.jscomp.LinkedFlowScopeTest::testJoin3
  public void testJoin3() {
    localScope.declare("localC", null, STRING_TYPE, null);
    localScope.declare("localD", null, STRING_TYPE, null);

    FlowScope childA = localEntry.createChildFlowScope();
    childA.inferSlotType("localC", NUMBER_TYPE);

    FlowScope childB = localEntry.createChildFlowScope();
    childA.inferSlotType("localD", BOOLEAN_TYPE);

    FlowScope joined = join(childB, childA);
    assertEquals(createUnionType(STRING_TYPE, NUMBER_TYPE),
        joined.getSlot("localC").getType());
    assertEquals(createUnionType(STRING_TYPE, BOOLEAN_TYPE),
        joined.getSlot("localD").getType());

    joined = join(childA, childB);
    assertEquals(createUnionType(STRING_TYPE, NUMBER_TYPE),
        joined.getSlot("localC").getType());
    assertEquals(createUnionType(STRING_TYPE, BOOLEAN_TYPE),
        joined.getSlot("localD").getType());

    assertEquals("Join should be symmetric",
        join(childB, childA), join(childA, childB));
  }

// com.google.javascript.jscomp.LinkedFlowScopeTest::testLongChain1
  public void testLongChain1() {
    FlowScope chainA = localEntry.createChildFlowScope();
    FlowScope chainB = localEntry.createChildFlowScope();
    for (int i = 0; i < LONG_CHAIN_LENGTH; i++) {
      localScope.declare("local" + i, null, null, null);
      chainA.inferSlotType("local" + i,
          i % 2 == 0 ? NUMBER_TYPE : BOOLEAN_TYPE);
      chainB.inferSlotType("local" + i,
          i % 3 == 0 ? STRING_TYPE : BOOLEAN_TYPE);

      chainA = chainA.createChildFlowScope();
      chainB = chainB.createChildFlowScope();
    }

    verifyLongChains(chainA, chainB);
  }

// com.google.javascript.jscomp.LinkedFlowScopeTest::testLongChain2
  public void testLongChain2() {
    FlowScope chainA = localEntry.createChildFlowScope();
    FlowScope chainB = localEntry.createChildFlowScope();
    for (int i = 0; i < LONG_CHAIN_LENGTH * 7; i++) {
      localScope.declare("local" + i, null, null, null);
      chainA.inferSlotType("local" + i,
          i % 2 == 0 ? NUMBER_TYPE : BOOLEAN_TYPE);
      chainB.inferSlotType("local" + i,
          i % 3 == 0 ? STRING_TYPE : BOOLEAN_TYPE);

      if (i % 7 == 0) {
        chainA = chainA.createChildFlowScope();
        chainB = chainB.createChildFlowScope();
      }
    }

    verifyLongChains(chainA, chainB);
  }

// com.google.javascript.jscomp.LinkedFlowScopeTest::testLongChain3
  public void testLongChain3() {
    FlowScope chainA = localEntry.createChildFlowScope();
    FlowScope chainB = localEntry.createChildFlowScope();
    for (int i = 0; i < LONG_CHAIN_LENGTH * 7; i++) {
      if (i % 7 == 0) {
        int j = i / 7;
        localScope.declare("local" + j, null, null, null);
        chainA.inferSlotType("local" + j,
            j % 2 == 0 ? NUMBER_TYPE : BOOLEAN_TYPE);
        chainB.inferSlotType("local" + j,
            j % 3 == 0 ? STRING_TYPE : BOOLEAN_TYPE);
      }

      chainA = chainA.createChildFlowScope();
      chainB = chainB.createChildFlowScope();
    }

    verifyLongChains(chainA, chainB);
  }

// com.google.javascript.jscomp.LinkedFlowScopeTest::testFindUniqueSlot
  public void testFindUniqueSlot() {
    FlowScope childA = localEntry.createChildFlowScope();
    childA.inferSlotType("localB", NUMBER_TYPE);

    FlowScope childAB = childA.createChildFlowScope();
    childAB.inferSlotType("localB", STRING_TYPE);

    FlowScope childABC = childAB.createChildFlowScope();
    childABC.inferSlotType("localA", BOOLEAN_TYPE);

    assertNull(childABC.findUniqueRefinedSlot(childABC));
    assertEquals(BOOLEAN_TYPE,
        childABC.findUniqueRefinedSlot(childAB).getType());
    assertNull(childABC.findUniqueRefinedSlot(childA));
    assertNull(childABC.findUniqueRefinedSlot(localEntry));

    assertEquals(STRING_TYPE,
        childAB.findUniqueRefinedSlot(childA).getType());
    assertEquals(STRING_TYPE,
        childAB.findUniqueRefinedSlot(localEntry).getType());

    assertEquals(NUMBER_TYPE,
        childA.findUniqueRefinedSlot(localEntry).getType());
  }

// com.google.javascript.jscomp.LinkedFlowScopeTest::testDiffer1
  public void testDiffer1() {
    FlowScope childA = localEntry.createChildFlowScope();
    childA.inferSlotType("localB", NUMBER_TYPE);

    FlowScope childAB = childA.createChildFlowScope();
    childAB.inferSlotType("localB", STRING_TYPE);

    FlowScope childABC = childAB.createChildFlowScope();
    childABC.inferSlotType("localA", BOOLEAN_TYPE);

    FlowScope childB = childAB.createChildFlowScope();
    childB.inferSlotType("localB", STRING_TYPE);

    FlowScope childBC = childB.createChildFlowScope();
    childBC.inferSlotType("localA", NO_TYPE);

    assertScopesSame(childAB, childB);
    assertScopesDiffer(childABC, childBC);

    assertScopesDiffer(childABC, childB);
    assertScopesDiffer(childAB, childBC);

    assertScopesDiffer(childA, childAB);
    assertScopesDiffer(childA, childABC);
    assertScopesDiffer(childA, childB);
    assertScopesDiffer(childA, childBC);
  }

// com.google.javascript.jscomp.LinkedFlowScopeTest::testDiffer2
  public void testDiffer2() {
    FlowScope childA = localEntry.createChildFlowScope();
    childA.inferSlotType("localA", NUMBER_TYPE);

    FlowScope childB = localEntry.createChildFlowScope();
    childB.inferSlotType("localA", NO_TYPE);

    assertScopesDiffer(childA, childB);
  }

// com.google.javascript.jscomp.LiveVariableAnalysisTest::testStraightLine
  public void testStraightLine() {
    
    assertNotLiveBeforeX("X:var a;", "a");
    assertNotLiveAfterX("X:var a;", "a");
    assertNotLiveAfterX("X:var a=1;", "a");
    assertLiveAfterX("X:var a=1; a()", "a");
    assertNotLiveBeforeX("X:var a=1; a()", "a");
    assertLiveBeforeX("var a;X:a;", "a");
    assertLiveBeforeX("var a;X:a=a+1;", "a");
    assertLiveBeforeX("var a;X:a+=1;", "a");
    assertLiveBeforeX("var a;X:a++;", "a");
    assertNotLiveAfterX("var a,b;X:b();", "a");
    assertNotLiveBeforeX("var a,b;X:b();", "a");
    assertLiveBeforeX("var a,b;X:b(a);", "a");
    assertLiveBeforeX("var a,b;X:b(1,2,3,b(a + 1));", "a");
    assertNotLiveBeforeX("var a,b;X:a=1;b(a)", "a");
    assertNotLiveAfterX("var a,b;X:b(a);b()", "a");
    assertLiveBeforeX("var a,b;X:b();b=1;a()", "b");
    assertLiveAfterX("X:a();var a;a()", "a");
    assertNotLiveAfterX("X:a();var a=1;a()", "a");
    assertLiveBeforeX("var a,b;X:a,b=1", "a");
  }

// com.google.javascript.jscomp.LiveVariableAnalysisTest::testProperties
  public void testProperties() {
    
    assertLiveBeforeX("var a,b;X:a.P;", "a");

    
    assertLiveBeforeX("var a,b;X:a.P=1;b()", "a");
    assertLiveBeforeX("var a,b;X:a.P.Q=1;b()", "a");

    
    assertNotLiveAfterX("var a,b;X:b.P.Q.a=1;", "a");

    assertLiveBeforeX("var a,b;X:b.P.Q=a;", "a");
  }

// com.google.javascript.jscomp.LiveVariableAnalysisTest::testConditions
  public void testConditions() {
    
    assertLiveBeforeX("var a,b;X:if(a){}", "a");
    assertLiveBeforeX("var a,b;X:if(a||b) {}", "a");
    assertLiveBeforeX("var a,b;X:if(b||a) {}", "a");
    assertLiveBeforeX("var a,b;X:if(b||b(a)) {}", "a");
    assertNotLiveAfterX("var a,b;X:b();if(a) {}", "b");

    
    assertNotLiveAfterX("var a,b;X:a();if(a=b){}a()", "a");
    assertNotLiveAfterX("var a,b;X:a();while(a=b){}a()", "a");

    
    assertNotLiveAfterX("var a,b;X:a();if((a=b)&&b){}a()", "a");
    assertNotLiveAfterX("var a,b;X:a();while((a=b)&&b){}a()", "a");
    assertLiveBeforeX("var a,b;a();X:if(b&&(a=b)){}a()", "a"); 
    assertLiveBeforeX("var a,b;a();X:if(a&&(a=b)){}a()", "a");
    assertLiveBeforeX("var a,b;a();X:while(b&&(a=b)){}a()", "a");
    assertLiveBeforeX("var a,b;a();X:while(a&&(a=b)){}a()", "a");
  }

// com.google.javascript.jscomp.LiveVariableAnalysisTest::testArrays
  public void testArrays() {
    assertLiveBeforeX("var a;X:a[1]", "a");
    assertLiveBeforeX("var a,b;X:b[a]", "a");
    assertLiveBeforeX("var a,b;X:b[1,2,3,4,b(a)]", "a");
    assertLiveBeforeX("var a,b;X:b=[a,'a']", "a");
    assertNotLiveBeforeX("var a,b;X:a=[];b(a)", "a");

    
    assertLiveBeforeX("var a;X:a[1]=1", "a");
  }

// com.google.javascript.jscomp.LiveVariableAnalysisTest::testTwoPaths
  public void testTwoPaths() {
    
    assertLiveBeforeX("var a,b;X:if(b){b(a)}else{b(a)};", "a");

    
    assertLiveBeforeX("var a,b;X:if(b){b(b)}else{b(a)};", "a");
    assertLiveBeforeX("var a,b;X:if(b){b(a)}else{b(b)};", "a");

    
    assertNotLiveAfterX("var a,b;X:if(b){b(b)}else{b(b)};", "a");

    
    assertLiveBeforeX("var a,b;X:if(b){b(b)}else{b(b)}a();", "a");

    
    assertLiveBeforeX("var a;X:while(param1){a()};", "a");
    assertLiveBeforeX("var a;X:while(param1){a=1};a()", "a");

    
    assertLiveBeforeX("var a;X:if(param1){a()};", "a");
    assertLiveBeforeX("var a;X:if(param1){a=1};a()", "a");

    
    
    assertNotLiveAfterX("X:var a;do{a=1}while(param1);a()", "a");
  }

// com.google.javascript.jscomp.LiveVariableAnalysisTest::testThreePaths
  public void testThreePaths() {
    assertLiveBeforeX("var a;X:if(1){}else if(2){}else{a()};", "a");
    assertLiveBeforeX("var a;X:if(1){}else if(2){a()}else{};", "a");
    assertLiveBeforeX("var a;X:if(1){a()}else if(2){}else{};", "a");
    assertLiveBeforeX("var a;X:if(1){}else if(2){}else{};a()", "a");
  }

// com.google.javascript.jscomp.LiveVariableAnalysisTest::testHooks
  public void testHooks() {
    assertLiveBeforeX("var a;X:1?a=1:1;a()", "a");

    
    
    
    assertLiveBeforeX("var a,b;X:b=1?a:2", "a");
  }

// com.google.javascript.jscomp.LiveVariableAnalysisTest::testForLoops
  public void testForLoops() {
    
    assertNotLiveBeforeX("var a,b;for(a=0;a<9;a++){b(a)};X:b", "a");
    assertNotLiveBeforeX("var a,b;for(a in b){a()};X:b", "a");
    assertNotLiveBeforeX("var a,b;for(a in b){a()};X:a", "b");
    assertLiveBeforeX("var b;for(var a in b){X:a()};", "a");

    
    assertLiveBeforeX("var a,b;for(a=0;a<9;a++){X:1}", "a");
    assertLiveAfterX("var a,b;for(a in b){X:b};", "a");
    
    assertLiveBeforeX("var a,b; X:for(a in b){ }", "a");

    
    
    

    
    assertLiveBeforeX("var a,b;X:a();b();for(a in b){a()};", "a");

    
    assertLiveBeforeX("var a,b;X:b;for(b=a;;){};", "a");
    assertNotLiveBeforeX("var a,b;X:a;for(b=a;;){b()};b();", "b");
  }

// com.google.javascript.jscomp.LiveVariableAnalysisTest::testNestedLoops
  public void testNestedLoops() {
    assertLiveBeforeX("var a;X:while(1){while(1){a()}}", "a");
    assertLiveBeforeX("var a;X:while(1){while(1){while(1){a()}}}", "a");
    assertLiveBeforeX("var a;X:while(1){while(1){a()};a=1}", "a");
    assertLiveAfterX("var a;while(1){while(1){a()};X:a=1;}", "a");
    assertLiveAfterX("var a;while(1){X:a=1;while(1){a()}}", "a");
    assertNotLiveBeforeX(
        "var a;X:1;do{do{do{a=1;}while(1)}while(1)}while(1);a()", "a");
  }

// com.google.javascript.jscomp.LiveVariableAnalysisTest::testSwitches
  public void testSwitches() {
    assertLiveBeforeX("var a,b;X:switch(a){}", "a");
    assertLiveBeforeX("var a,b;X:switch(b){case(a):break;}", "a");
    assertLiveBeforeX("var a,b;X:switch(b){case(b):case(a):break;}", "a");
    assertNotLiveBeforeX(
        "var a,b;X:switch(b){case 1:a=1;break;default:a=2;break};a()", "a");

    assertLiveBeforeX("var a,b;X:switch(b){default:a();break;}", "a");
  }

// com.google.javascript.jscomp.LiveVariableAnalysisTest::testAssignAndReadInCondition
  public void testAssignAndReadInCondition() {
    
    
    
    assertLiveBeforeX("var a, b; X: if ((a = this) && (b = a)) {}", "a");
    assertNotLiveBeforeX("var a, b; X: a = 1, b = 1;", "a");
    assertNotLiveBeforeX("var a; X: a = 1, a = 1;", "a");
  }

// com.google.javascript.jscomp.LiveVariableAnalysisTest::testParam
  public void testParam() {
    
    assertNotLiveAfterX("var a;X:a()", "param1");
    assertLiveBeforeX("var a;X:a(param1)", "param1");
    assertNotLiveAfterX("var a;X:a();a(param2)", "param1");
  }

// com.google.javascript.jscomp.LiveVariableAnalysisTest::testExpressionInForIn
  public void testExpressionInForIn() {
    assertLiveBeforeX("var a = [0]; X:for (a[1] in foo) { }", "a");
  }

// com.google.javascript.jscomp.LiveVariableAnalysisTest::testArgumentsArray
  public void testArgumentsArray() {
    
    
    assertEscaped("arguments[0]", "param1");
    assertEscaped("arguments[0]", "param2");
    assertEscaped("var args = arguments", "param1");
    assertEscaped("var args = arguments", "param2");
    assertNotEscaped("arguments = []", "param1");
    assertNotEscaped("arguments = []", "param2");
    assertEscaped("arguments[0] = 1", "param1");
    assertEscaped("arguments[0] = 1", "param2");
    assertEscaped("arguments[arguments[0]] = 1", "param1");
    assertEscaped("arguments[arguments[0]] = 1", "param2");
  }

// com.google.javascript.jscomp.LiveVariableAnalysisTest::testTryCatchFinally
  public void testTryCatchFinally() {
    assertLiveAfterX("var a; try {X:a=1} finally {a}", "a");
    assertLiveAfterX("var a; try {a()} catch(e) {X:a=1} finally {a}", "a");
    
    
    assertNotLiveAfterX("var a = 1; try {" +
        "try {a()} catch(e) {X:1} } catch(E) {a}", "a");
    assertLiveAfterX("var a; while(1) { try {X:a=1;break} finally {a}}", "a");
  }

// com.google.javascript.jscomp.LiveVariableAnalysisTest::testExceptionThrowingAssignments
  public void testExceptionThrowingAssignments() {
    assertLiveBeforeX("try{var a; X:a=foo();a} catch(e) {e()}", "a");
    assertLiveBeforeX("try{X:var a=foo();a} catch(e) {e()}", "a");
    assertLiveBeforeX("try{X:var a=foo()} catch(e) {e(a)}", "a");
  }

// com.google.javascript.jscomp.LiveVariableAnalysisTest::testInnerFunctions
  public void testInnerFunctions() {
    assertLiveBeforeX("function a() {}; X: a()", "a");
    assertNotLiveBeforeX("X: function a() {}", "a");
    assertLiveBeforeX("a = function(){}; function a() {}; X: a()", "a");
    
    
    assertLiveAfterX("X: a = function(){}; function a() {}; a()", "a");
    assertNotLiveBeforeX("X: a = function(){}; function a() {}; a()", "a");
  }

// com.google.javascript.jscomp.LiveVariableAnalysisTest::testEscaped
  public void testEscaped() {
    assertEscaped("var a;function b(){a()}", "a");
    assertEscaped("var a;function b(){param1()}", "param1");
    assertEscaped("var a;function b(){function c(){a()}}", "a");
    assertEscaped("var a;function b(){param1.x = function() {a()}}", "a");
    assertEscaped("try{} catch(e){}", "e");
    assertNotEscaped("var a;function b(){var c; c()}", "c");
    assertNotEscaped("var a;function f(){function b(){var c;c()}}", "c");
    assertNotEscaped("var a;function b(){};a()", "a");
    assertNotEscaped("var a;function f(){function b(){}}a()", "a");
    assertNotEscaped("var a;function b(){var a;a()};a()", "a");

    
    assertEscaped("var _x", "_x");
  }

// com.google.javascript.jscomp.LiveVariableAnalysisTest::testEscapedLiveness
  public void testEscapedLiveness() {
    assertNotLiveBeforeX("var a;X:a();function b(){a()}", "a");
  }

// com.google.javascript.jscomp.LiveVariableAnalysisTest::testBug1449316
  public void testBug1449316() {
    assertLiveBeforeX("try {var x=[]; X:var y=x[0]} finally {foo()}", "x");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testInitialTypingScope
  public void testInitialTypingScope() {
    Scope s = new TypedScopeCreator(compiler,
        new DefaultCodingConvention()).createInitialScope(
            new Node(Token.BLOCK));

    assertEquals(ARRAY_FUNCTION_TYPE, s.getVar("Array").getType());
    assertEquals(BOOLEAN_OBJECT_FUNCTION_TYPE,
        s.getVar("Boolean").getType());
    assertEquals(DATE_FUNCTION_TYPE, s.getVar("Date").getType());
    assertEquals(ERROR_FUNCTION_TYPE, s.getVar("Error").getType());
    assertEquals(EVAL_ERROR_FUNCTION_TYPE,
        s.getVar("EvalError").getType());
    assertEquals(NUMBER_OBJECT_FUNCTION_TYPE,
        s.getVar("Number").getType());
    assertEquals(OBJECT_FUNCTION_TYPE, s.getVar("Object").getType());
    assertEquals(RANGE_ERROR_FUNCTION_TYPE,
        s.getVar("RangeError").getType());
    assertEquals(REFERENCE_ERROR_FUNCTION_TYPE,
        s.getVar("ReferenceError").getType());
    assertEquals(REGEXP_FUNCTION_TYPE, s.getVar("RegExp").getType());
    assertEquals(STRING_OBJECT_FUNCTION_TYPE,
        s.getVar("String").getType());
    assertEquals(SYNTAX_ERROR_FUNCTION_TYPE,
        s.getVar("SyntaxError").getType());
    assertEquals(TYPE_ERROR_FUNCTION_TYPE,
        s.getVar("TypeError").getType());
    assertEquals(URI_ERROR_FUNCTION_TYPE,
        s.getVar("URIError").getType());
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeCheck1
  public void testTypeCheck1() throws Exception {
    testTypes("function foo(){ if (foo()) return; }");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeCheck2
  public void testTypeCheck2() throws Exception {
    testTypes("function foo(){ var x=foo(); x--; }",
        "increment/decrement\n" +
        "found   : undefined\n" +
        "required: number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeCheck4
  public void testTypeCheck4() throws Exception {
    testTypes("function foo(){ !foo(); }");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeCheck5
  public void testTypeCheck5() throws Exception {
    testTypes("function foo(){ var a = +foo(); }",
        "sign operator\n" +
        "found   : undefined\n" +
        "required: number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeCheck6
  public void testTypeCheck6() throws Exception {
    testTypes(
        "function foo(){" +
        "var a;if (a == foo())return;}");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeCheck8
  public void testTypeCheck8() throws Exception {
    testTypes("function foo(){do {} while (foo());}");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeCheck9
  public void testTypeCheck9() throws Exception {
    testTypes("function foo(){while (foo());}");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeCheck10
  public void testTypeCheck10() throws Exception {
    testTypes("function foo(){for (;foo(););}");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeCheck11
  public void testTypeCheck11() throws Exception {
    testTypes("var a;" +
        "var b;" +
        "a = b;",
        "assignment\n" +
        "found   : String\n" +
        "required: Number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeCheck12
  public void testTypeCheck12() throws Exception {
    testTypes("function foo(){var a = 3^foo();}",
        "bad right operand to bitwise operator\n" +
        "found   : Object\n" +
        "required: (boolean|null|number|string|undefined)");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeCheck13
  public void testTypeCheck13() throws Exception {
    testTypes("var i; i=/xx/;",
        "assignment\n" +
        "found   : RegExp\n" +
        "required: (Number|String)");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeCheck14
  public void testTypeCheck14() throws Exception {
    testTypes("function foo(opt_a){}");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeCheck15
  public void testTypeCheck15() throws Exception {
    testTypes("var x;x=null;x=10;",
        "assignment\n" +
        "found   : number\n" +
        "required: (Number|null|undefined)");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeCheck16a
  public void testTypeCheck16a() throws Exception {
    testTypes("var x='';",
              "initializing variable\n" +
              "found   : string\n" +
              "required: (Number|null|undefined)");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeCheck16b
  public void testTypeCheck16b() throws Exception {
    testTypes("var x='';",
              "initializing variable\n" +
              "found   : string\n" +
              "required: (Number|null)");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeCheck17
  public void testTypeCheck17() throws Exception {
    testTypes("\n" +
        "function a(opt_foo){\nreturn (opt_foo);\n}");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeCheck18
  public void testTypeCheck18() throws Exception {
    testTypes("\n function a(){return new RegExp();}");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeCheck19
  public void testTypeCheck19() throws Exception {
    testTypes("\n function a(){return new Array();}");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeCheck20
  public void testTypeCheck20() throws Exception {
    testTypes("\n function a(){return new Date();}");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeCheckBasicDowncast
  public void testTypeCheckBasicDowncast() throws Exception {
    testTypes("function foo() {}\n" +
                  " var bar = new foo();\n");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeCheckNoDowncastToNumber
  public void testTypeCheckNoDowncastToNumber() throws Exception {
    testTypes("function foo() {}\n" +
                  " var bar = new foo();\n",
        "initializing variable\n" +
        "found   : foo\n" +
        "required: Number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeCheck21
  public void testTypeCheck21() throws Exception {
    testTypes("var foo;");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeCheck22
  public void testTypeCheck22() throws Exception {
    testTypes("\nfunction foo(p){}\n" +
                  "function Element(){}\n" +
                  "var v;\n" +
                  "foo(v);\n");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeCheck23
  public void testTypeCheck23() throws Exception {
    testTypes("var foo; foo = null;");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeCheck24
  public void testTypeCheck24() throws Exception {
    testTypes("function MyType(){}\n" +
        "var foo; foo = null;");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeCheckDefaultExterns
  public void testTypeCheckDefaultExterns() throws Exception {
    testTypes(" function f(x) {}" +
        "f([].length);" ,
        "actual parameter 1 of f does not match formal parameter\n" +
        "found   : number\n" +
        "required: string");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeCheckCustomExterns
  public void testTypeCheckCustomExterns() throws Exception {
    testTypes(
        DEFAULT_EXTERNS + " Array.prototype.oogabooga;",
        " function f(x) {}" +
        "f([].oogabooga);" ,
        "actual parameter 1 of f does not match formal parameter\n" +
        "found   : boolean\n" +
        "required: string", false);
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testParameterizedArray1
  public void testParameterizedArray1() throws Exception {
    testTypes(" var f = function(a) { return a[0]; };",
        "inconsistent return type\n" +
        "found   : number\n" +
        "required: string");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testParameterizedArray2
  public void testParameterizedArray2() throws Exception {
    testTypes(" var f = function(a) { return a[0]; };",
        "inconsistent return type\n" +
        "found   : Array\n" +
        "required: number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testParameterizedArray3
  public void testParameterizedArray3() throws Exception {
    testTypes(" var f = function(a) { a[1] = 0; return a[0]; };");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testParameterizedArray4
  public void testParameterizedArray4() throws Exception {
    testTypes(" var f = function(a) { a[0] = 'a'; };",
        "assignment\n" +
        "found   : string\n" +
        "required: number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testParameterizedArray5
  public void testParameterizedArray5() throws Exception {
    testTypes(" var f = function(a) { a[0] = 'a'; };");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testParameterizedArray6
  public void testParameterizedArray6() throws Exception {
    testTypes(" var f = function(a) { return a[0]; };",
        "inconsistent return type\n" +
        "found   : *\n" +
        "required: string");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testParameterizedArray7
  public void testParameterizedArray7() throws Exception {
    testTypes(" var f = function(a) { return a[0]; };",
        "inconsistent return type\n" +
        "found   : number\n" +
        "required: string");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testParameterizedObject1
  public void testParameterizedObject1() throws Exception {
    testTypes(" var f = function(a) { return a[0]; };",
        "inconsistent return type\n" +
        "found   : number\n" +
        "required: string");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testParameterizedObject2
  public void testParameterizedObject2() throws Exception {
    testTypes(" var f = function(a) { return a['x']; };",
        "inconsistent return type\n" +
        "found   : number\n" +
        "required: string");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testParameterizedObject3
  public void testParameterizedObject3() throws Exception {
    testTypes(" var f = function(a) { return a['x']; };",
        "restricted index type\n" +
        "found   : string\n" +
        "required: number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testParameterizedObject4
  public void testParameterizedObject4() throws Exception {
    testTypes(" var E = {A: 'a', B: 'b'};\n" +
        " var f = function(a) { return a['x']; };",
        "restricted index type\n" +
        "found   : string\n" +
        "required: E.<string>");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testUnionOfFunctionAndType
  public void testUnionOfFunctionAndType() throws Exception {
    testTypes(" var a;" +
        " var b = null; a = b;");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testOptionalParameterComparedToUndefined
  public void testOptionalParameterComparedToUndefined() throws Exception {
    testTypes("function foo(opt_a)" +
        "{if (opt_a==undefined) var b = 3;}");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testOptionalAllType
  public void testOptionalAllType() throws Exception {
    testTypes("function f(opt_x) { return opt_x }\n" +
        "var y;\n" +
        "f(y);");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testOptionalUnknownNamedType
  public void testOptionalUnknownNamedType() throws Exception {
    testTypes("\n" +
        "function f(opt_x) { return opt_x; }\n" +
        "var T = function() {};",
        "inconsistent return type\n" +
        "found   : (T|undefined)\n" +
        "required: undefined");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testOptionalArgFunctionParam
  public void testOptionalArgFunctionParam() throws Exception {
    testTypes("" +
        "function f(a) {a()};");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testOptionalArgFunctionParam2
  public void testOptionalArgFunctionParam2() throws Exception {
    testTypes("" +
        "function f(a) {a(3)};");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testOptionalArgFunctionParam3
  public void testOptionalArgFunctionParam3() throws Exception {
    testTypes("" +
        "function f(a) {a(undefined)};");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testOptionalArgFunctionParam4
  public void testOptionalArgFunctionParam4() throws Exception {
    String expectedWarning = "Function a: called with 2 argument(s). " +
        "Function requires at least 0 argument(s) and no more than 1 " +
        "argument(s).";

    testTypes("function f(a) {a(3,4)};",
              expectedWarning, false);
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testOptionalArgFunctionParamError
  public void testOptionalArgFunctionParamError() throws Exception {
    String expectedWarning =
        "Bad type annotation. variable length argument must be last";
    testTypes("" +
              "function f(a) {};", expectedWarning, false);
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testOptionalNullableArgFunctionParam
  public void testOptionalNullableArgFunctionParam() throws Exception {
    testTypes("" +
              "function f(a) {a()};");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testOptionalNullableArgFunctionParam2
  public void testOptionalNullableArgFunctionParam2() throws Exception {
    testTypes("" +
              "function f(a) {a(null)};");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testOptionalNullableArgFunctionParam3
  public void testOptionalNullableArgFunctionParam3() throws Exception {
    testTypes("" +
              "function f(a) {a(3)};");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testOptionalArgFunctionReturn
  public void testOptionalArgFunctionReturn() throws Exception {
    testTypes("" +
              "function f() { return function(opt_x) { }; };" +
              "f()()");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testOptionalArgFunctionReturn2
  public void testOptionalArgFunctionReturn2() throws Exception {
    testTypes("" +
              "function f() { return function(opt_x) { }; };" +
              "f()({})");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testBooleanType
  public void testBooleanType() throws Exception {
    testTypes("var x = 1 < 2;");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testBooleanReduction1
  public void testBooleanReduction1() throws Exception {
    testTypes("var x; x = null || \"a\";");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testBooleanReduction2
  public void testBooleanReduction2() throws Exception {
    
    
    testTypes("" +
        "(function(s) { return ((s == 'a') && s) || 'b'; })");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testBooleanReduction3
  public void testBooleanReduction3() throws Exception {
    testTypes("" +
        "(function(s) { return s && null && 3; })");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testBooleanReduction4
  public void testBooleanReduction4() throws Exception {
    testTypes("" +
        "(function(x) { return null || x || null ; })");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testBooleanReduction5
  public void testBooleanReduction5() throws Exception {
    testTypes("\n" +
        "var f = function(x) {\n" +
        "if (!x || typeof x == 'string') {\n" +
        "return x;\n" +
        "}\n" +
        "return null;\n" +
        "};");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testBooleanReduction6
  public void testBooleanReduction6() throws Exception {
    testTypes("\n" +
        "var f = function(x) {\n" +
        "if (!(x && typeof x != 'string')) {\n" +
        "return x;\n" +
        "}\n" +
        "return null;\n" +
        "};");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testBooleanReduction7
   public void testBooleanReduction7() throws Exception {
     testTypes("var T = function() {};\n" +
         "\n" +
         "var f = function(x) {\n" +
         "if (!x) {\n" +
         "return x;\n" +
         "}\n" +
         "return null;\n" +
         "};");
   }

// com.google.javascript.jscomp.LooseTypeCheckTest::testNullAnd
  public void testNullAnd() throws Exception {
    testTypes("var x;\n" +
        "var r = x && x;",
        "initializing variable\n" +
        "found   : null\n" +
        "required: number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testNullOr
  public void testNullOr() throws Exception {
    testTypes("var x;\n" +
        "var r = x || x;",
        "initializing variable\n" +
        "found   : null\n" +
        "required: number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testBooleanPreservation1
  public void testBooleanPreservation1() throws Exception {
    testTypes("var x = \"a\";" +
        "x = ((x == \"a\") && x) || x == \"b\";",
        "assignment\n" +
        "found   : (boolean|string)\n" +
        "required: string");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testBooleanPreservation2
  public void testBooleanPreservation2() throws Exception {
    testTypes("var x = \"a\"; x = (x == \"a\") || x;",
        "assignment\n" +
        "found   : (boolean|string)\n" +
        "required: string");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testBooleanPreservation3
  public void testBooleanPreservation3() throws Exception {
    testTypes("" +
        "function f(x) { return x && x == \"a\"; }",
        "condition always evaluates to false\n" +
        "left : Function\n" +
        "right: string");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testBooleanPreservation4
  public void testBooleanPreservation4() throws Exception {
    testTypes("" +
        "function f(x) { return x && x == \"a\"; }",
        "inconsistent return type\n" +
        "found   : (boolean|null|undefined)\n" +
        "required: boolean");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeOfReduction1
  public void testTypeOfReduction1() throws Exception {
    testTypes(" " +
        "function f(x) { return typeof x == 'number' ? String(x) : x; }");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeOfReduction2
  public void testTypeOfReduction2() throws Exception {
    testTypes(" " +
        "function f(x) { return typeof x != 'string' ? String(x) : x; }");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeOfReduction3
  public void testTypeOfReduction3() throws Exception {
    testTypes(" " +
        "function f(x) { return typeof x == 'object' ? 1 : x; }");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeOfReduction4
  public void testTypeOfReduction4() throws Exception {
    testTypes(" " +
        "function f(x) { return typeof x == 'undefined' ? {} : x; }");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeOfReduction5
  public void testTypeOfReduction5() throws Exception {
    testTypes(" var E = {A: 'a', B: 'b'};\n" +
        " " +
        "function f(x) { return typeof x != 'number' ? x : 'a'; }");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeOfReduction6
  public void testTypeOfReduction6() throws Exception {
    testTypes("\n" +
        "function f(x) {\n" +
        "return typeof x == 'string' && x.length == 3 ? x : 'a';\n" +
        "}");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeOfReduction7
  public void testTypeOfReduction7() throws Exception {
    testTypes("var f = function(x) { " +
        "return typeof x == 'number' ? x : 'a'; }",
        "inconsistent return type\n" +
        "found   : (number|string)\n" +
        "required: string");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeOfReduction8
  public void testTypeOfReduction8() throws Exception {
    testClosureTypes(
        CLOSURE_DEFS +
        "\n" +
        "function f(x) {\n" +
        "return goog.isString(x) && x.length == 3 ? x : 'a';\n" +
        "}", null);
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeOfReduction9
  public void testTypeOfReduction9() throws Exception {
    testClosureTypes(
        CLOSURE_DEFS +
        "\n" +
        "function f(x) {\n" +
        "return goog.isArray(x) ? 'a' : x;\n" +
        "}", null);
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeOfReduction10
  public void testTypeOfReduction10() throws Exception {
    testClosureTypes(
        CLOSURE_DEFS +
        "\n" +
        "function f(x) {\n" +
        "return goog.isArray(x) ? x : [];\n" +
        "}", null);
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeOfReduction11
  public void testTypeOfReduction11() throws Exception {
    testClosureTypes(
        CLOSURE_DEFS +
        "\n" +
        "function f(x) {\n" +
        "return goog.isObject(x) ? x : [];\n" +
        "}", null);
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeOfReduction12
  public void testTypeOfReduction12() throws Exception {
    testTypes(" var E = {A: 'a', B: 'b'};\n" +
        " " +
        "function f(x) { return typeof x == 'object' ? x : []; }");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeOfReduction13
  public void testTypeOfReduction13() throws Exception {
    testClosureTypes(
        CLOSURE_DEFS +
        " var E = {A: 'a', B: 'b'};\n" +
        " " +
        "function f(x) { return goog.isObject(x) ? x : []; }", null);
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeOfReduction14
  public void testTypeOfReduction14() throws Exception {
    
    testClosureTypes(
        CLOSURE_DEFS +
        "function f(arguments) { " +
        "  return goog.isString(arguments[0]) ? arguments[0] : 0;" +
        "}", null);
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeOfReduction15
  public void testTypeOfReduction15() throws Exception {
    
    testClosureTypes(
        CLOSURE_DEFS +
        "function f(arguments) { " +
        "  return typeof arguments[0] == 'string' ? arguments[0] : 0;" +
        "}", null);
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testQualifiedNameReduction1
  public void testQualifiedNameReduction1() throws Exception {
    testTypes("var x = {};  x.a = 'a';\n" +
        " var f = function() {\n" +
        "return x.a ? x.a : 'a'; }");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testQualifiedNameReduction2
  public void testQualifiedNameReduction2() throws Exception {
    testTypes(" var T = " +
        "function(a) {this.a = a};\n" +
        " T.prototype.f = function() {\n" +
        "return this.a ? this.a : 'a'; }");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testQualifiedNameReduction3
  public void testQualifiedNameReduction3() throws Exception {
    testTypes(" var T = " +
        "function(a) {this.a = a};\n" +
        " T.prototype.f = function() {\n" +
        "return typeof this.a == 'string' ? this.a : 'a'; }");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testQualifiedNameReduction4
  public void testQualifiedNameReduction4() throws Exception {
    testClosureTypes(
        CLOSURE_DEFS +
        " var T = " +
        "function(a) {this.a = a};\n" +
        " T.prototype.f = function() {\n" +
        "return goog.isString(this.a) ? this.a : 'a'; }", null);
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testInstanceOfReduction1
  public void testInstanceOfReduction1() throws Exception {
    testTypes(" var T = function() {};\n" +
        "\n" +
        "var f = function(x) {\n" +
        "if (x instanceof T) { return x; } else { return new T(); }\n" +
        "};");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testInstanceOfReduction2
  public void testInstanceOfReduction2() throws Exception {
    testTypes(" var T = function() {};\n" +
        "\n" +
        "var f = function(x) {\n" +
        "if (x instanceof T) { return ''; } else { return x; }\n" +
        "};");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testPropertyInferredPropagation
  public void testPropertyInferredPropagation() throws Exception {
    testTypes("function f() { return {}; }\n" +
         "function g() { var x = f(); if (x.p) x.a = 'a'; else x.a = 'b'; }\n" +
         "function h() { var x = f(); x.a = false; }");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testPropertyInference1
  public void testPropertyInference1() throws Exception {
    testTypes(
        " function F() { this.x_ = true; }" +
        "" +
        "F.prototype.bar = function() { if (this.x_) return this.x_; };",
        "inconsistent return type\n" +
        "found   : boolean\n" +
        "required: string");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testPropertyInference2
  public void testPropertyInference2() throws Exception {
    testTypes(
        " function F() { this.x_ = true; }" +
        "F.prototype.baz = function() { this.x_ = null; };" +
        "" +
        "F.prototype.bar = function() { if (this.x_) return this.x_; };",
        "inconsistent return type\n" +
        "found   : boolean\n" +
        "required: string");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testPropertyInference3
  public void testPropertyInference3() throws Exception {
    testTypes(
        " function F() { this.x_ = true; }" +
        "F.prototype.baz = function() { this.x_ = 3; };" +
        "" +
        "F.prototype.bar = function() { if (this.x_) return this.x_; };",
        "inconsistent return type\n" +
        "found   : (boolean|number)\n" +
        "required: string");
  }

// buggy code
    private void findAliases(NodeTraversal t) {
      Scope scope = t.getScope();
      for (Var v : scope.getVarIterable()) {
        Node n = v.getNode();
        Node parent = n.getParent();
        boolean isVar = parent.isVar();
        if (isVar && n.getFirstChild() != null && n.getFirstChild().isQualifiedName()) {
          recordAlias(v);
        } else if (v.isBleedingFunction()) {
          // Bleeding functions already get a BAD_PARAMETERS error, so just
          // do nothing.
        } else if (parent.getType() == Token.LP) {
          // Parameters of the scope function also get a BAD_PARAMETERS
          // error.
        } else if (isVar) {
          Node grandparent = parent.getParent();
          Node value = n.hasChildren() ?
              v.getInitialValue().detachFromParent() :
              null;
          Node varNode = parent;

          String name = n.getString();
          int nameCount = scopedAliasNames.count(name);
          scopedAliasNames.add(name);
          String globalName =
              "$jscomp.scope." + name + (nameCount == 0 ? "" : ("$" + nameCount));

          compiler.ensureLibraryInjected("base");

          // First, we need to free up the function expression (EXPR)
          // to be used in another expression.
            // Replace "function NAME() { ... }" with "var NAME;".

            // We can't keep the local name on the function expression,
            // because IE is buggy and will leak the name into the global
            // scope. This is covered in more detail here:
            // http://wiki.ecmascript.org/lib/exe/fetch.php?id=resources:resources&cache=cache&media=resources:jscriptdeviationsfromes3.pdf
            //
            // This will only cause problems if this is a hoisted, recursive
            // function, and the programmer is using the hoisting.

              // If this is a VAR, we can just detach the expression and
              // the tree will still be valid.

          // Add $jscomp.scope.name = EXPR;
          // Make sure we copy over all the jsdoc and debug info.
          if (value != null || v.getJSDocInfo() != null) {
            Node newDecl = NodeUtil.newQualifiedNameNodeDeclaration(
                compiler.getCodingConvention(),
                globalName,
                value,
                v.getJSDocInfo())
                .useSourceInfoIfMissingFromForTree(n);
            NodeUtil.setDebugInformation(
                newDecl.getFirstChild().getFirstChild(), n, name);

              grandparent.addChildBefore(newDecl, varNode);
          }

          // Rewrite "var name = EXPR;" to "var name = $jscomp.scope.name;"
          v.getNameNode().addChildToFront(
              NodeUtil.newQualifiedNameNode(
                  compiler.getCodingConvention(), globalName, n, name));

          recordAlias(v);
        } else {
          // Do not other kinds of local symbols, like catch params.
          report(t, n, GOOG_SCOPE_NON_ALIAS_LOCAL, n.getString());
        }
      }
    }

  public Node getChildBefore(Node child) {
    if (child == first) {
      return null;
    }
    Node n = first;

    while (n.next != child) {
      n = n.next;
      if (n == null) {
        throw new RuntimeException("node is not a child");
      }
    }
    return n;
  }

// relevant test
// com.google.javascript.jscomp.CoalesceVariableNamesTest::testParameter4b
  public void testParameter4b() {
    
    test("function FUNC(x, y, z) {var a,b; y; a=0; a; x; b=0; b}",
         "function FUNC(x, y, z) {         y; y=0; y; x; x=0; x}");
  }

// com.google.javascript.jscomp.CoalesceVariableNamesTest::testLiveRangeChangeWithinCfgNode
  public void testLiveRangeChangeWithinCfgNode() {
    inFunction("var x, y; x = 1, y = 2, y, x");
    inFunction("var x, y; x = 1,x; y");

    
    inFunction("var x; var y; y = 1, y, x = 1; x");
    inFunction("var x; var y; y = 1; y, x = 1; x", "var x; x = 1; x, x = 1; x");
    inFunction("var x, y; y = 1, x = 1, x, y += 1, y");
    inFunction("var x, y; y = 1, x = 1, x, y ++, y");
  }

// com.google.javascript.jscomp.CoalesceVariableNamesTest::testLiveRangeChangeWithinCfgNode2
  public void testLiveRangeChangeWithinCfgNode2() {
    inFunction("var x; var y; var a; var b;" +
               "y = 1, a = 1, y, a, x = 1, b = 1; x; b");
    inFunction("var x; var y; var a; var b;" +
               "y = 1, a = 1, y, a, x = 1; x; b = 1; b",
               "var x; var y; var a;       " +
               "y = 1, a = 1, y, a, x = 1; x; x = 1; x");
    inFunction("var x; var y; var a; var b;" +
               "y = 1, a = 1, y, x = 1; a; x; b = 1; b",
               "var x; var y; var a;       " +
               "y = 1, a = 1, y, x = 1; a; x; x = 1; x");
  }

// com.google.javascript.jscomp.CoalesceVariableNamesTest::testFunctionNameReuse
  public void testFunctionNameReuse() {

  }

// com.google.javascript.jscomp.CoalesceVariableNamesTest::testBug1401831
  public void testBug1401831() {
    
    
    String src = "function f(opt_a2) {" +
        "  var buffer;" +
        "  if (opt_a2) {" +
        "    for(var i = 0; i < arguments.length; i++) {" +
        "      buffer += arguments[i];" +
        "    }" +
        "  }" +
        "  return buffer;" +
        "}";
    test(src, src);
  }

// com.google.javascript.jscomp.CoalesceVariableNamesTest::testDeterministic
  public void testDeterministic() {
    
    
    
    
    
    
    
    
    
    
    inFunction("var a,b,c,d,e;" +
               "  a=1; b=1; a; b;" +
               "  b=1; c=1; b; c;" +
               "  c=1; d=1; c; d;" +
               "  d=1; e=1; d; e;" +
               "  e=1; a=1; e; a;",

               "var a,b,    e;" +
               "  a=1; b=1; a; b;" +
               "  b=1; a=1; b; a;" +
               "  a=1; b=1; a; b;" +
               "  b=1; e=1; b; e;" +
               "  e=1; a=1; e; a;");

    
    
    
    
    
    inFunction("var d,a,b,c,e;" +
               "  a=1; b=1; a; b;" +
               "  b=1; c=1; b; c;" +
               "  c=1; d=1; c; d;" +
               "  d=1; e=1; d; e;" +
               "  e=1; a=1; e; a;",

               "var d,  b,c  ;" +
               "  d=1; b=1; d; b;" +
               "  b=1; c=1; b; c;" +
               "  c=1; d=1; c; d;" +
               "  d=1; b=1; d; b;" +
               "  b=1; d=1; b; d;");
  }

// com.google.javascript.jscomp.CoalesceVariableNamesTest::testVarLiveRangeCross
  public void testVarLiveRangeCross() {
    inFunction("var a={}; var b=a.S(); b",
               "var a={};     a=a.S(); a");
    inFunction("var a={}; var b=a.S(), c=b.SS(); b; c",
               "var a={}; var b=a.S(), a=b.SS(); b; a");
    inFunction("var a={}; var b=a.S(), c=a.SS(), d=a.SSS(); b; c; d",
               "var a={}; var b=a.S(), c=a.SS(), a=a.SSS(); b; c; a");
    inFunction("var a={}; var b=a.S(), c=a.SS(), d=a.SSS(); b; c; d",
               "var a={}; var b=a.S(), c=a.SS(), a=a.SSS(); b; c; a");
    inFunction("var a={}; d=1; d; var b=a.S(), c=a.SS(), d=a.SSS(); b; c; d");
  }

// com.google.javascript.jscomp.CoalesceVariableNamesTest::testBug1445366
  public void testBug1445366() {
    
    inFunction(
        " var iframe = getFrame();" +
        " try {" +
        "   var win = iframe.contentWindow;" +
        " } catch (e) {" +
        " } finally {" +
        "   if (win)" +
        "     this.setupWinUtil_();" +
        "   else" +
        "     this.load();" +
        " }");

    
    inFunction(
        " var iframe = getFrame();" +
        " var win = iframe.contentWindow;" +
        " if (win)" +
        "   this.setupWinUtil_();" +
        " else" +
        "   this.load();",

        " var iframe = getFrame();" +
        " iframe = iframe.contentWindow;" +
        " if (iframe)" +
        "   this.setupWinUtil_();" +
        " else" +
        "   this.load();");
  }

// com.google.javascript.jscomp.CoalesceVariableNamesTest::testCannotReuseAnyParamsBug
  public void testCannotReuseAnyParamsBug() {
    testSame("function handleKeyboardShortcut(e, key, isModifierPressed) {\n" +
        "  if (!isModifierPressed) {\n" +
        "    return false;\n" +
        "  }\n" +
        "  var command;\n" +
        "  switch (key) {\n" +
        "    case 'b': 
        "      command = COMMAND.BOLD;\n" +
        "      break;\n" +
        "    case 'i': 
        "      command = COMMAND.ITALIC;\n" +
        "      break;\n" +
        "    case 'u': 
        "      command = COMMAND.UNDERLINE;\n" +
        "      break;\n" +
        "    case 's': 
        "      return true;\n" +
        "  }\n" +
        "\n" +
        "  if (command) {\n" +
        "    this.fieldObject.execCommand(command);\n" +
        "    return true;\n" +
        "  }\n" +
        "\n" +
        "  return false;\n" +
        "};");
  }

// com.google.javascript.jscomp.CoalesceVariableNamesTest::testForInWithAssignment
  public void testForInWithAssignment() {
    inFunction(
      "var _f = function (commands) {" +
          "  var k, v, ref;" +
          "  for (k in ref = commands) {" +
          "    v = ref[k];" +
          "    alert(k + ':' + v);" +
          "  }" +
          "}",

      "var _f = function (commands) {" +
          "  var k,ref;" +
          "  for (k in ref = commands) {" +
          "    commands = ref[k];" +
          "    alert(k + ':' + commands);" +
          "  }" +
          "}"
        );
  }

// com.google.javascript.jscomp.CoalesceVariableNamesTest::testUsePseduoNames
  public void testUsePseduoNames() {
    usePseudoName = true;
    inFunction("var x   = 0; print(x  ); var   y = 1; print(  y)",
               "var x_y = 0; print(x_y);     x_y = 1; print(x_y)");

    inFunction("var x_y = 1; var x   = 0; print(x  ); var     y = 1;" +
               "print(  y); print(x_y);",

               "var x_y = 1; var x_y$ = 0; print(x_y$);     x_y$ = 1;" + "" +
               "print(x_y$); print(x_y);");

    inFunction("var x_y = 1; function f() {" +
               "var x    = 0; print(x  ); var y = 1; print( y);" +
               "print(x_y);}",

               "var x_y = 1; function f() {" +
               "var x_y$ = 0; print(x_y$); x_y$ = 1; print(x_y$);" +
               "print(x_y);}");

    inFunction("var x   = 0; print(x  ); var   y = 1; print(  y); " +
               "var closure_var; function bar() { print(closure_var); }",
               "var x_y = 0; print(x_y);     x_y = 1; print(x_y); " +
               "var closure_var; function bar() { print(closure_var); }");
  }

// com.google.javascript.jscomp.CoalesceVariableNamesTest::testMaxVars
  public void testMaxVars() {
    String code = "";
    for (int i = 0;
         i < LiveVariablesAnalysis.MAX_VARIABLES_TO_ANALYZE + 1; i++) {
      code += String.format("var x%d = 0; print(x%d);", i, i);
    }
    inFunction(code);
  }

// com.google.javascript.jscomp.CodePrinterTest::testPrint
  public void testPrint() {
    assertPrint("10 + a + b", "10+a+b");
    assertPrint("10 + (30*50)", "10+30*50");
    assertPrint("with(x) { x + 3; }", "with(x)x+3");
    assertPrint("\"aa'a\"", "\"aa'a\"");
    assertPrint("\"aa\\\"a\"", "'aa\"a'");
    assertPrint("function foo()\n{return 10;}", "function foo(){return 10}");
    assertPrint("a instanceof b", "a instanceof b");
    assertPrint("typeof(a)", "typeof a");
    assertPrint(
        "var foo = x ? { a : 1 } : {a: 3, b:4, \"default\": 5, \"foo-bar\": 6}",
        "var foo=x?{a:1}:{a:3,b:4,\"default\":5,\"foo-bar\":6}");

    
    assertPrint("function foo(){throw 'error';}",
        "function foo(){throw\"error\";}");
    
    assertPrint("if (true) function foo(){return}",
        "if(true){function foo(){return}}");

    assertPrint("var x = 10; { var y = 20; }", "var x=10;var y=20");

    assertPrint("while (x-- > 0);", "while(x-- >0);");
    assertPrint("x-- >> 1", "x-- >>1");

    assertPrint("(function () {})(); ",
        "(function(){})()");

    
    assertPrint("var a,b,c,d;a || (b&& c) && (a || d)",
        "var a,b,c,d;a||b&&c&&(a||d)");
    assertPrint("var a,b,c; a || (b || c); a * (b * c); a | (b | c)",
        "var a,b,c;a||(b||c);a*(b*c);a|(b|c)");
    assertPrint("var a,b,c; a / b / c;a / (b / c); a - (b - c);",
        "var a,b,c;a/b/c;a/(b/c);a-(b-c)");

    
    assertPrint("var a,b; a = b = 3;",
        "var a,b;a=b=3");
    assertPrint("var a,b,c,d; a = (b = c = (d = 3));",
        "var a,b,c,d;a=b=c=d=3");
    assertPrint("var a,b,c; a += (b = c += 3);",
        "var a,b,c;a+=b=c+=3");
    assertPrint("var a,b,c; a *= (b -= c);",
        "var a,b,c;a*=b-=c");

    
    assertPrint("a ? delete b[0] : 3", "a?delete b[0]:3");
    assertPrint("(delete a[0])/10", "delete a[0]/10");

    

    
    assertPrint("new A", "new A");
    assertPrint("new A()", "new A");
    assertPrint("new A('x')", "new A(\"x\")");

    
    assertPrint("new A().a()", "(new A).a()");
    assertPrint("(new A).a()", "(new A).a()");

    
    assertPrint("new A('y').a()", "(new A(\"y\")).a()");

    
    assertPrint("new A.B", "new A.B");
    assertPrint("new A.B()", "new A.B");
    assertPrint("new A.B('z')", "new A.B(\"z\")");

    
    assertPrint("(new A.B).a()", "(new A.B).a()");
    assertPrint("new A.B().a()", "(new A.B).a()");
    
    assertPrint("new A.B('w').a()", "(new A.B(\"w\")).a()");

    
    assertPrint("x + +y", "x+ +y");
    assertPrint("x - (-y)", "x- -y");
    assertPrint("x++ +y", "x++ +y");
    assertPrint("x-- -y", "x-- -y");
    assertPrint("x++ -y", "x++-y");

    
    assertPrint("foo:for(;;){break foo;}", "foo:for(;;)break foo");
    assertPrint("foo:while(1){continue foo;}", "foo:while(1)continue foo");

    
    assertPrint("({})", "({})");
    assertPrint("var x = {};", "var x={}");
    assertPrint("({}).x", "({}).x");
    assertPrint("({})['x']", "({})[\"x\"]");
    assertPrint("({}) instanceof Object", "({})instanceof Object");
    assertPrint("({}) || 1", "({})||1");
    assertPrint("1 || ({})", "1||{}");
    assertPrint("({}) ? 1 : 2", "({})?1:2");
    assertPrint("0 ? ({}) : 2", "0?{}:2");
    assertPrint("0 ? 1 : ({})", "0?1:{}");
    assertPrint("typeof ({})", "typeof{}");
    assertPrint("f({})", "f({})");

    
    assertPrint("(function(){})", "(function(){})");
    assertPrint("(function(){})()", "(function(){})()");
    assertPrint("(function(){})instanceof Object",
        "(function(){})instanceof Object");
    assertPrint("(function(){}).bind().call()",
        "(function(){}).bind().call()");
    assertPrint("var x = function() { };", "var x=function(){}");
    assertPrint("var x = function() { }();", "var x=function(){}()");
    assertPrint("(function() {}), 2", "(function(){}),2");

    
    assertPrint("(function f(){})", "(function f(){})");

    
    assertPrint("function f(){}", "function f(){}");

    
    assertPrint("({ 'a': 4, '\\u0100': 4 })", "({\"a\":4,\"\\u0100\":4})");
    assertPrint("({ a: 4, '\\u0100': 4 })", "({a:4,\"\\u0100\":4})");

    
    assertPrint("if (true) { alert();}", "if(true)alert()");
    assertPrint("if (false) {} else {alert(\"a\");}",
        "if(false);else alert(\"a\")");
    assertPrint("for(;;) { alert();};", "for(;;)alert()");

    assertPrint("do { alert(); } while(true);",
        "do alert();while(true)");
    assertPrint("myLabel: { alert();}",
        "myLabel:alert()");
    assertPrint("myLabel: for(;;) continue myLabel;",
        "myLabel:for(;;)continue myLabel");

    
    assertPrint("if (true) var x; x = 4;", "if(true)var x;x=4");

    
    assertPrint("\\u00fb", "\\u00fb");
    assertPrint("\\u00fa=1", "\\u00fa=1");
    assertPrint("function \\u00f9(){}", "function \\u00f9(){}");
    assertPrint("x.\\u00f8", "x.\\u00f8");
    assertPrint("x.\\u00f8", "x.\\u00f8");
    assertPrint("abc\\u4e00\\u4e01jkl", "abc\\u4e00\\u4e01jkl");

    
    assertPrint("! ! true", "!!true");
    assertPrint("!(!(true))", "!!true");
    assertPrint("typeof(void(0))", "typeof void 0");
    assertPrint("typeof(void(!0))", "typeof void!0");
    assertPrint("+ - + + - + 3", "+-+ +-+3"); 
    assertPrint("+(--x)", "+--x");
    assertPrint("-(++x)", "-++x");

    
    assertPrint("-(--x)", "- --x");
    assertPrint("!(~~5)", "!~~5");
    assertPrint("~(a/b)", "~(a/b)");

    
    assertPrint("new (foo.bar()).factory(baz)", "new (foo.bar().factory)(baz)");
    assertPrint("new (bar()).factory(baz)", "new (bar().factory)(baz)");
    assertPrint("new (new foobar(x)).factory(baz)",
        "new (new foobar(x)).factory(baz)");

    
    assertPrint("a ? b : (c ? d : e)", "a?b:c?d:e");
    assertPrint("a ? (b ? c : d) : e", "a?b?c:d:e");
    assertPrint("(a ? b : c) ? d : e", "(a?b:c)?d:e");

    
    assertPrint("if (x) if (y); else;", "if(x)if(y);else;");

    
    assertPrint("a,b,c", "a,b,c");
    assertPrint("(a,b),c", "a,b,c");
    assertPrint("a,(b,c)", "a,b,c");
    assertPrint("x=a,b,c", "x=a,b,c");
    assertPrint("x=(a,b),c", "x=(a,b),c");
    assertPrint("x=a,(b,c)", "x=a,b,c");
    assertPrint("x=a,y=b,z=c", "x=a,y=b,z=c");
    assertPrint("x=(a,y=b,z=c)", "x=(a,y=b,z=c)");
    assertPrint("x=[a,b,c,d]", "x=[a,b,c,d]");
    assertPrint("x=[(a,b,c),d]", "x=[(a,b,c),d]");
    assertPrint("x=[(a,(b,c)),d]", "x=[(a,b,c),d]");
    assertPrint("x=[a,(b,c,d)]", "x=[a,(b,c,d)]");
    assertPrint("var x=(a,b)", "var x=(a,b)");
    assertPrint("var x=a,b,c", "var x=a,b,c");
    assertPrint("var x=(a,b),c", "var x=(a,b),c");
    assertPrint("var x=a,b=(c,d)", "var x=a,b=(c,d)");
    assertPrint("foo(a,b,c,d)", "foo(a,b,c,d)");
    assertPrint("foo((a,b,c),d)", "foo((a,b,c),d)");
    assertPrint("foo((a,(b,c)),d)", "foo((a,b,c),d)");
    assertPrint("f(a+b,(c,d,(e,f,g)))", "f(a+b,(c,d,e,f,g))");
    assertPrint("({}) , 1 , 2", "({}),1,2");
    assertPrint("({}) , {} , {}", "({}),{},{}");

    
    assertPrint("if (x){}", "if(x);");
    assertPrint("if(x);", "if(x);");
    assertPrint("if(x)if(y);", "if(x)if(y);");
    assertPrint("if(x){if(y);}", "if(x)if(y);");
    assertPrint("if(x){if(y){};;;}", "if(x)if(y);");
    assertPrint("if(x){;;function y(){};;}", "if(x){function y(){}}");
  }

// com.google.javascript.jscomp.CodePrinterTest::testBreakTrustedStrings
  public void testBreakTrustedStrings() {
    
    assertPrint("'<script>'", "\"<script>\"");
    assertPrint("'</script>'", "\"\\x3c/script>\"");
    assertPrint("\"</script> </SCRIPT>\"", "\"\\x3c/script> \\x3c/SCRIPT>\"");

    assertPrint("'-->'", "\"--\\x3e\"");
    assertPrint("']]>'", "\"]]\\x3e\"");
    assertPrint("' --></script>'", "\" --\\x3e\\x3c/script>\"");

    assertPrint("/--> <\\/script>/g", "/--\\x3e <\\/script>/g");

    
    
    assertPrint("'<!-- I am a string -->'",
        "\"\\x3c!-- I am a string --\\x3e\"");

    assertPrint("'<=&>'", "\"<=&>\"");
  }

// com.google.javascript.jscomp.CodePrinterTest::testBreakUntrustedStrings
  public void testBreakUntrustedStrings() {
    trustedStrings = false;

    
    assertPrint("'<script>'", "\"\\x3cscript\\x3e\"");
    assertPrint("'</script>'", "\"\\x3c/script\\x3e\"");
    assertPrint("\"</script> </SCRIPT>\"", "\"\\x3c/script\\x3e \\x3c/SCRIPT\\x3e\"");

    assertPrint("'-->'", "\"--\\x3e\"");
    assertPrint("']]>'", "\"]]\\x3e\"");
    assertPrint("' --></script>'", "\" --\\x3e\\x3c/script\\x3e\"");

    assertPrint("/--> <\\/script>/g", "/--\\x3e <\\/script>/g");

    
    
    assertPrint("'<!-- I am a string -->'",
        "\"\\x3c!-- I am a string --\\x3e\"");

    assertPrint("'<=&>'", "\"\\x3c\\x3d\\x26\\x3e\"");
    assertPrint("/(?=x)/", "/(?=x)/");
  }

// com.google.javascript.jscomp.CodePrinterTest::testPrintArray
  public void testPrintArray() {
    assertPrint("[void 0, void 0]", "[void 0,void 0]");
    assertPrint("[undefined, undefined]", "[undefined,undefined]");
    assertPrint("[ , , , undefined]", "[,,,undefined]");
    assertPrint("[ , , , 0]", "[,,,0]");
  }

// com.google.javascript.jscomp.CodePrinterTest::testHook
  public void testHook() {
    assertPrint("a ? b = 1 : c = 2", "a?b=1:c=2");
    assertPrint("x = a ? b = 1 : c = 2", "x=a?b=1:c=2");
    assertPrint("(x = a) ? b = 1 : c = 2", "(x=a)?b=1:c=2");

    assertPrint("x, a ? b = 1 : c = 2", "x,a?b=1:c=2");
    assertPrint("x, (a ? b = 1 : c = 2)", "x,a?b=1:c=2");
    assertPrint("(x, a) ? b = 1 : c = 2", "(x,a)?b=1:c=2");

    assertPrint("a ? (x, b) : c = 2", "a?(x,b):c=2");
    assertPrint("a ? b = 1 : (x,c)", "a?b=1:(x,c)");

    assertPrint("a ? b = 1 : c = 2 + x", "a?b=1:c=2+x");
    assertPrint("(a ? b = 1 : c = 2) + x", "(a?b=1:c=2)+x");
    assertPrint("a ? b = 1 : (c = 2) + x", "a?b=1:(c=2)+x");

    assertPrint("a ? (b?1:2) : 3", "a?b?1:2:3");
  }

// com.google.javascript.jscomp.CodePrinterTest::testPrintInOperatorInForLoop
  public void testPrintInOperatorInForLoop() {
    
    
    
    assertPrint("var a={}; for (var i = (\"length\" in a); i;) {}",
        "var a={};for(var i=(\"length\"in a);i;);");
    assertPrint("var a={}; for (var i = (\"length\" in a) ? 0 : 1; i;) {}",
        "var a={};for(var i=(\"length\"in a)?0:1;i;);");
    assertPrint("var a={}; for (var i = (\"length\" in a) + 1; i;) {}",
        "var a={};for(var i=(\"length\"in a)+1;i;);");
    assertPrint("var a={};for (var i = (\"length\" in a|| \"size\" in a);;);",
        "var a={};for(var i=(\"length\"in a)||(\"size\"in a);;);");
    assertPrint("var a={};for (var i = (a || a) || (\"size\" in a);;);",
        "var a={};for(var i=a||a||(\"size\"in a);;);");

    
    assertPrint("var a={}; for (var i = -(\"length\" in a); i;) {}",
        "var a={};for(var i=-(\"length\"in a);i;);");
    assertPrint("var a={};function b_(p){ return p;};" +
        "for(var i=1,j=b_(\"length\" in a);;) {}",
        "var a={};function b_(p){return p}" +
            "for(var i=1,j=b_(\"length\"in a);;);");

    
    assertPrint("var a={}; for (;(\"length\" in a);) {}",
        "var a={};for(;\"length\"in a;);");

    
    assertPrintSame("for(x,(y in z);;)foo()");
    assertPrintSame("for(var x,w=(y in z);;)foo()");

    
    assertPrintSame("for(a=c?0:(0 in d);;)foo()");
  }

// com.google.javascript.jscomp.CodePrinterTest::testLiteralProperty
  public void testLiteralProperty() {
    assertPrint("(64).toString()", "(64).toString()");
  }

// com.google.javascript.jscomp.CodePrinterTest::testAmbiguousElseClauses
  public void testAmbiguousElseClauses() {
    assertPrintNode("if(x)if(y);else;",
        new Node(Token.IF,
            Node.newString(Token.NAME, "x"),
            new Node(Token.BLOCK,
                new Node(Token.IF,
                    Node.newString(Token.NAME, "y"),
                    new Node(Token.BLOCK),

                    
                    new Node(Token.BLOCK)))));

    assertPrintNode("if(x){if(y);}else;",
        new Node(Token.IF,
            Node.newString(Token.NAME, "x"),
            new Node(Token.BLOCK,
                new Node(Token.IF,
                    Node.newString(Token.NAME, "y"),
                    new Node(Token.BLOCK))),

            
            new Node(Token.BLOCK)));

    assertPrintNode("if(x)if(y);else{if(z);}else;",
        new Node(Token.IF,
            Node.newString(Token.NAME, "x"),
            new Node(Token.BLOCK,
                new Node(Token.IF,
                    Node.newString(Token.NAME, "y"),
                    new Node(Token.BLOCK),
                    new Node(Token.BLOCK,
                        new Node(Token.IF,
                            Node.newString(Token.NAME, "z"),
                            new Node(Token.BLOCK))))),

            
            new Node(Token.BLOCK)));
  }

// com.google.javascript.jscomp.CodePrinterTest::testLineBreak
  public void testLineBreak() {
    
    assertLineBreak("function a() {}\n" +
        "function b() {}",
        "function a(){}\n" +
        "function b(){}\n");

    
    assertLineBreak("var a = {};\n" +
        "a.foo = function () {}\n" +
        "function b() {}",
        "var a={};a.foo=function(){};\n" +
        "function b(){}\n");

    
    assertLineBreak("var a = {\n" +
        "  b: function() {},\n" +
        "  c: function() {}\n" +
        "};\n" +
        "alert(a);",

        "var a={b:function(){},\n" +
        "c:function(){}};\n" +
        "alert(a)");
  }

// com.google.javascript.jscomp.CodePrinterTest::testPreferLineBreakAtEndOfFile
  public void testPreferLineBreakAtEndOfFile() {
    
    assertLineBreakAtEndOfFile(
        "\"1234567890\";",
        "\"1234567890\"",
        "\"1234567890\"");

    
    assertLineBreakAtEndOfFile(
        "\"123456789012345678901234567890\";\"1234567890\"",
        "\"123456789012345678901234567890\";\n\"1234567890\"",
        "\"123456789012345678901234567890\"; \"1234567890\";\n");
    assertLineBreakAtEndOfFile(
        "var12345678901234567890123456 instanceof Object;",
        "var12345678901234567890123456 instanceof\nObject",
        "var12345678901234567890123456 instanceof Object;\n");

    
    assertLineBreakAtEndOfFile(
        "\"1234567890\";\"12345678901234567890\";",
        "\"1234567890\";\"12345678901234567890\"",
        "\"1234567890\";\"12345678901234567890\";\n");

    
    assertLineBreakAtEndOfFile(
        "\"123456789012345678901234567890\";\"12345678901234567890\";",
        "\"123456789012345678901234567890\";\n\"12345678901234567890\"",
        "\"123456789012345678901234567890\";\n\"12345678901234567890\";\n");
  }

// com.google.javascript.jscomp.CodePrinterTest::testPrettyPrinter
  public void testPrettyPrinter() {
    
    
    assertPrettyPrint("(function(){})();","(function() {\n})();\n");
    assertPrettyPrint("var a = (function() {});alert(a);",
        "var a = function() {\n};\nalert(a);\n");

    
    
    assertPrettyPrint("if (1) {}",
        "if (1) {\n" +
        "}\n");
    assertPrettyPrint("if (1) {alert(\"\");}",
        "if (1) {\n" +
        "  alert(\"\");\n" +
        "}\n");
    assertPrettyPrint("if (1)alert(\"\");",
        "if (1) {\n" +
        "  alert(\"\");\n" +
        "}\n");
    assertPrettyPrint("if (1) {alert();alert();}",
        "if (1) {\n" +
        "  alert();\n" +
        "  alert();\n" +
        "}\n");

    
    assertPrettyPrint("label: alert();",
        "label: alert();\n");

    
    assertPrettyPrint("if (1) alert();",
        "if (1) {\n" +
        "  alert();\n" +
        "}\n");
    assertPrettyPrint("for (;;) alert();",
        "for (;;) {\n" +
        "  alert();\n" +
        "}\n");

    assertPrettyPrint("while (1) alert();",
        "while (1) {\n" +
        "  alert();\n" +
        "}\n");

    
    assertPrettyPrint("if (1) {} else {alert(a);}",
        "if (1) {\n" +
        "} else {\n  alert(a);\n}\n");

    
    assertPrettyPrint("if (1) alert(a); else alert(b);",
        "if (1) {\n" +
        "  alert(a);\n" +
        "} else {\n" +
        "  alert(b);\n" +
        "}\n");

    
    assertPrettyPrint("for(;;) { alert();}",
        "for (;;) {\n" +
         "  alert();\n" +
         "}\n");
    assertPrettyPrint("for(;;) {}",
        "for (;;) {\n" +
        "}\n");
    assertPrettyPrint("for(;;) { alert(); alert(); }",
        "for (;;) {\n" +
        "  alert();\n" +
        "  alert();\n" +
        "}\n");

    
    assertPrettyPrint("do { alert(); } while(true);",
        "do {\n" +
        "  alert();\n" +
        "} while (true);\n");

    
    assertPrettyPrint("myLabel: { alert();}",
        "myLabel: {\n" +
        "  alert();\n" +
        "}\n");

    
    
    assertPrettyPrint("myLabel: for(;;) continue myLabel;",
        "myLabel: for (;;) {\n" +
        "  continue myLabel;\n" +
        "}\n");

    assertPrettyPrint("var a;", "var a;\n");

    
    assertPrettyPrint("var foo = 3+5;",
        "var foo = 3 + 5;\n");

    
    assertPrettyPrint("var foo = bar ? 3 : null;",
        "var foo = bar ? 3 : null;\n");

    
    assertPrettyPrint("function foo() { return \"foo\"; }",
        "function foo() {\n  return \"foo\";\n}\n");
    assertPrettyPrint("throw \"foo\";",
        "throw \"foo\";");

    
    assertPrettyPrint("do{ alert(); } while(true);",
        "do {\n  alert();\n} while (true);\n");
    assertPrettyPrint("while(true) { alert(); }",
        "while (true) {\n  alert();\n}\n");
  }

// com.google.javascript.jscomp.CodePrinterTest::testPrettyPrinter2
  public void testPrettyPrinter2() {
    assertPrettyPrint(
        "if(true) f();",
        "if (true) {\n" +
        "  f();\n" +
        "}\n");

    assertPrettyPrint(
        "if (true) { f() } else { g() }",
        "if (true) {\n" +
        "  f();\n" +
        "} else {\n" +
        "  g();\n" +
        "}\n");

    assertPrettyPrint(
        "if(true) f(); for(;;) g();",
        "if (true) {\n" +
        "  f();\n" +
        "}\n" +
        "for (;;) {\n" +
        "  g();\n" +
        "}\n");
  }

// com.google.javascript.jscomp.CodePrinterTest::testPrettyPrinter3
  public void testPrettyPrinter3() {
    assertPrettyPrint(
        "try {} catch(e) {}if (1) {alert();alert();}",
        "try {\n" +
        "} catch (e) {\n" +
        "}\n" +
        "if (1) {\n" +
        "  alert();\n" +
        "  alert();\n" +
        "}\n");

    assertPrettyPrint(
        "try {} finally {}if (1) {alert();alert();}",
        "try {\n" +
        "} finally {\n" +
        "}\n" +
        "if (1) {\n" +
        "  alert();\n" +
        "  alert();\n" +
        "}\n");

    assertPrettyPrint(
        "try {} catch(e) {} finally {} if (1) {alert();alert();}",
        "try {\n" +
        "} catch (e) {\n" +
        "} finally {\n" +
        "}\n" +
        "if (1) {\n" +
        "  alert();\n" +
        "  alert();\n" +
        "}\n");
  }

// com.google.javascript.jscomp.CodePrinterTest::testPrettyPrinter4
  public void testPrettyPrinter4() {
    assertPrettyPrint(
        "function f() {}if (1) {alert();}",
        "function f() {\n" +
        "}\n" +
        "if (1) {\n" +
        "  alert();\n" +
        "}\n");

    assertPrettyPrint(
        "var f = function() {};if (1) {alert();}",
        "var f = function() {\n" +
        "};\n" +
        "if (1) {\n" +
        "  alert();\n" +
        "}\n");

    assertPrettyPrint(
        "(function() {})();if (1) {alert();}",
        "(function() {\n" +
        "})();\n" +
        "if (1) {\n" +
        "  alert();\n" +
        "}\n");

    assertPrettyPrint(
        "(function() {alert();alert();})();if (1) {alert();}",
        "(function() {\n" +
        "  alert();\n" +
        "  alert();\n" +
        "})();\n" +
        "if (1) {\n" +
        "  alert();\n" +
        "}\n");
  }

// com.google.javascript.jscomp.CodePrinterTest::testTypeAnnotations
  public void testTypeAnnotations() {
    assertTypeAnnotations(
        " function Foo(){}",
        "\n"
        + "function Foo() {\n}\n");
  }

// com.google.javascript.jscomp.CodePrinterTest::testTypeAnnotationsTypeDef
  public void testTypeAnnotationsTypeDef() {
    
    
    
    assertTypeAnnotations(
        " goog.java.Long;\n"
        + "\n"
        + "function f(a){};\n",
        "goog.java.Long;\n"
        + "\n"
        + "function f(a) {\n}\n");
  }

// com.google.javascript.jscomp.CodePrinterTest::testTypeAnnotationsAssign
  public void testTypeAnnotationsAssign() {
    assertTypeAnnotations(" var Foo = function(){}",
        "\n"
        + "var Foo = function() {\n};\n");
  }

// com.google.javascript.jscomp.CodePrinterTest::testTypeAnnotationsNamespace
  public void testTypeAnnotationsNamespace() {
    assertTypeAnnotations("var a = {};"
        + " a.Foo = function(){}",
        "var a = {};\n"
        + "\n"
        + "a.Foo = function() {\n};\n");
  }

// com.google.javascript.jscomp.CodePrinterTest::testTypeAnnotationsMemberSubclass
  public void testTypeAnnotationsMemberSubclass() {
    assertTypeAnnotations("var a = {};"
        + " a.Foo = function(){};"
        + " a.Bar = function(){}",
        "var a = {};\n"
        + "\n"
        + "a.Foo = function() {\n};\n"
        + "\n"
        + "a.Bar = function() {\n};\n");
  }

// com.google.javascript.jscomp.CodePrinterTest::testTypeAnnotationsInterface
  public void testTypeAnnotationsInterface() {
    assertTypeAnnotations("var a = {};"
        + " a.Foo = function(){};"
        + " a.Bar = function(){}",
        "var a = {};\n"
        + "\n"
        + "a.Foo = function() {\n};\n"
        + "\n"
        + "a.Bar = function() {\n};\n");
  }

// com.google.javascript.jscomp.CodePrinterTest::testTypeAnnotationsMultipleInterface
  public void testTypeAnnotationsMultipleInterface() {
    assertTypeAnnotations("var a = {};"
        + " a.Foo1 = function(){};"
        + " a.Foo2 = function(){};"
        + ""
        + "a.Bar = function(){}",
        "var a = {};\n"
        + "\n"
        + "a.Foo1 = function() {\n};\n"
        + "\n"
        + "a.Foo2 = function() {\n};\n"
        + "\n"
        + "a.Bar = function() {\n};\n");
  }

// com.google.javascript.jscomp.CodePrinterTest::testTypeAnnotationsMember
  public void testTypeAnnotationsMember() {
    assertTypeAnnotations("var a = {};"
        + " a.Foo = function(){}"
        + "\n"
        + "a.Foo.prototype.foo = function(foo) { return 3; };"
        + ""
        + "a.Foo.prototype.bar = '';",
        "var a = {};\n"
        + "\n"
        + "a.Foo = function() {\n};\n"
        + "\n"
        + "a.Foo.prototype.foo = function(foo) {\n  return 3;\n};\n"
        + "\n"
        + "a.Foo.prototype.bar = \"\";\n");
  }

// com.google.javascript.jscomp.CodePrinterTest::testTypeAnnotationsImplements
  public void testTypeAnnotationsImplements() {
    assertTypeAnnotations("var a = {};"
        + " a.Foo = function(){};\n"
        + " a.I = function(){};\n"
        + " a.I2 = function(){};\n"
        + " a.Bar = function(){}",
        "var a = {};\n"
        + "\n"
        + "a.Foo = function() {\n};\n"
        + "\n"
        + "a.I = function() {\n};\n"
        + "\n"
        + "a.I2 = function() {\n};\n"
        + "\n"
        + "a.Bar = function() {\n};\n");
  }

// com.google.javascript.jscomp.CodePrinterTest::testTypeAnnotationsDispatcher1
  public void testTypeAnnotationsDispatcher1() {
    assertTypeAnnotations(
        "var a = {};\n" +
        "\n" +
        "a.Foo = function(){}",
        "var a = {};\n" +
        "\n" +
        "a.Foo = function() {\n" +
        "};\n");
  }

// com.google.javascript.jscomp.CodePrinterTest::testTypeAnnotationsDispatcher2
  public void testTypeAnnotationsDispatcher2() {
    assertTypeAnnotations(
        "var a = {};\n" +
        "\n" +
        "a.Foo = function(){}\n" +
        "\n" +
        "a.Foo.prototype.foo = function() {};",

        "var a = {};\n" +
        "\n" +
        "a.Foo = function() {\n" +
        "};\n" +
        "\n" +
        "a.Foo.prototype.foo = function() {\n" +
        "};\n");
  }

// com.google.javascript.jscomp.CodePrinterTest::testU2UFunctionTypeAnnotation1
  public void testU2UFunctionTypeAnnotation1() {
    assertTypeAnnotations(
        " var x = function() {}",
        "\n" +
        "var x = function() {\n};\n");
  }

// com.google.javascript.jscomp.CodePrinterTest::testU2UFunctionTypeAnnotation2
  public void testU2UFunctionTypeAnnotation2() {
    
    
    assertTypeAnnotations(
        " var x = function() {}",
        "\n" +
        "var x = function() {\n};\n");
  }

// com.google.javascript.jscomp.CodePrinterTest::testEmitUnknownParamTypesAsAllType
  public void testEmitUnknownParamTypesAsAllType() {
    assertTypeAnnotations(
        "var a = function(x) {}",
        "\n" +
        "var a = function(x) {\n};\n");
  }

// com.google.javascript.jscomp.CodePrinterTest::testOptionalTypesAnnotation
  public void testOptionalTypesAnnotation() {
    assertTypeAnnotations(
        "\n" +
        "var a = function(x) {}",
        "\n" +
        "var a = function(x) {\n};\n");
  }

// com.google.javascript.jscomp.CodePrinterTest::testVariableArgumentsTypesAnnotation
  public void testVariableArgumentsTypesAnnotation() {
    assertTypeAnnotations(
        "\n" +
        "var a = function(x) {}",
        "\n" +
        "var a = function(x) {\n};\n");
  }

// com.google.javascript.jscomp.CodePrinterTest::testTempConstructor
  public void testTempConstructor() {
    assertTypeAnnotations(
        "var x = function() {\n\nfunction t1() {}\n" +
        " \nfunction t2() {}\n" +
        " t1.prototype = t2.prototype}",
        "\nvar x = function() {\n" +
        "  \n" +
        "function t1() {\n  }\n" +
        "  \n" +
        "function t2() {\n  }\n" +
        "  t1.prototype = t2.prototype;\n};\n"
    );
  }

// com.google.javascript.jscomp.CodePrinterTest::testEnumAnnotation1
  public void testEnumAnnotation1() {
    assertTypeAnnotations(
        " var Enum = {FOO: 'x', BAR: 'y'};",
        "\nvar Enum = {FOO:\"x\", BAR:\"y\"};\n");
  }

// com.google.javascript.jscomp.CodePrinterTest::testEnumAnnotation2
  public void testEnumAnnotation2() {
    assertTypeAnnotations(
        "var goog = goog || {};" +
        " goog.Enum = {FOO: 'x', BAR: 'y'};" +
        " goog.Enum2 = goog.x ? {} : goog.Enum;",
        "var goog = goog || {};\n" +
        "\ngoog.Enum = {FOO:\"x\", BAR:\"y\"};\n" +
        "\ngoog.Enum2 = goog.x ? {} : goog.Enum;\n");
  }

// com.google.javascript.jscomp.CodePrinterTest::testSubtraction
  public void testSubtraction() {
    Compiler compiler = new Compiler();
    Node n = compiler.parseTestCode("x - -4");
    assertEquals(0, compiler.getErrorCount());

    assertEquals(
        "x- -4",
        printNode(n));
  }

// com.google.javascript.jscomp.CodePrinterTest::testFunctionWithCall
  public void testFunctionWithCall() {
    assertPrint(
        "var user = new function() {"
        + "alert(\"foo\")}",
        "var user=new function(){"
        + "alert(\"foo\")}");
    assertPrint(
        "var user = new function() {"
        + "this.name = \"foo\";"
        + "this.local = function(){alert(this.name)};}",
        "var user=new function(){"
        + "this.name=\"foo\";"
        + "this.local=function(){alert(this.name)}}");
  }

// com.google.javascript.jscomp.CodePrinterTest::testLineLength
  public void testLineLength() {
    
    assertLineLength("var aba,bcb,cdc",
        "var aba,bcb," +
        "\ncdc");

    
    assertLineLength(
        "\"foo\"+\"bar,baz,bomb\"+\"whee\"+\";long-string\"\n+\"aaa\"",
        "\"foo\"+\"bar,baz,bomb\"+" +
        "\n\"whee\"+\";long-string\"+" +
        "\n\"aaa\"");

    
    assertLineLength("var abazaba=1234",
        "var abazaba=" +
        "\n1234");

    
    assertLineLength("var abab=1;var bab=2",
        "var abab=1;" +
        "\nvar bab=2");

    
    assertLineLength("var a=/some[reg](ex),with.*we?rd|chars/i;var b=a",
        "var a=/some[reg](ex),with.*we?rd|chars/i;" +
        "\nvar b=a");

    
    assertLineLength("var a=\"foo,{bar};baz\";var b=a",
        "var a=\"foo,{bar};baz\";" +
        "\nvar b=a");

    
    assertLineLength("var a=\"a\";a++;var b=\"bbb\";",
        "var a=\"a\";a++;\n" +
        "var b=\"bbb\"");
  }

// com.google.javascript.jscomp.CodePrinterTest::testParsePrintParse
  public void testParsePrintParse() {
    testReparse("3;");
    testReparse("var a = b;");
    testReparse("var x, y, z;");
    testReparse("try { foo() } catch(e) { bar() }");
    testReparse("try { foo() } catch(e) { bar() } finally { stuff() }");
    testReparse("try { foo() } finally { stuff() }");
    testReparse("throw 'me'");
    testReparse("function foo(a) { return a + 4; }");
    testReparse("function foo() { return; }");
    testReparse("var a = function(a, b) { foo(); return a + b; }");
    testReparse("b = [3, 4, 'paul', \"Buchhe it\",,5];");
    testReparse("v = (5, 6, 7, 8)");
    testReparse("d = 34.0; x = 0; y = .3; z = -22");
    testReparse("d = -x; t = !x + ~y;");
    testReparse("'hi';  stuff(a,b) \n" +
            " foo(); 
            " bar();");
    testReparse("a = b++ + ++c; a = b++-++c; a = - --b; a = - ++b;");
    testReparse("a++; b= a++; b = ++a; b = a--; b = --a; a+=2; b-=5");
    testReparse("a = (2 + 3) * 4;");
    testReparse("a = 1 + (2 + 3) + 4;");
    testReparse("x = a ? b : c; x = a ? (b,3,5) : (foo(),bar());");
    testReparse("a = b | c || d ^ e " +
            "&& f & !g != h << i <= j < k >>> l > m * n % !o");
    testReparse("a == b; a != b; a === b; a == b == a;" +
            " (a == b) == a; a == (b == a);");
    testReparse("if (a > b) a = b; if (b < 3) a = 3; else c = 4;");
    testReparse("if (a == b) { a++; } if (a == 0) { a++; } else { a --; }");
    testReparse("for (var i in a) b += i;");
    testReparse("for (var i = 0; i < 10; i++){ b /= 2;" +
            " if (b == 2)break;else continue;}");
    testReparse("for (x = 0; x < 10; x++) a /= 2;");
    testReparse("for (;;) a++;");
    testReparse("while(true) { blah(); }while(true) blah();");
    testReparse("do stuff(); while(a>b);");
    testReparse("[0, null, , true, false, this];");
    testReparse("s.replace(/absc/, 'X').replace(/ab/gi, 'Y');");
    testReparse("new Foo; new Bar(a, b,c);");
    testReparse("with(foo()) { x = z; y = t; } with(bar()) a = z;");
    testReparse("delete foo['bar']; delete foo;");
    testReparse("var x = { 'a':'paul', 1:'3', 2:(3,4) };");
    testReparse("switch(a) { case 2: case 3: stuff(); break;" +
        "case 4: morestuff(); break; default: done();}");
    testReparse("x = foo['bar'] + foo['my stuff'] + foo[bar] + f.stuff;");
    testReparse("a.v = b.v; x['foo'] = y['zoo'];");
    testReparse("'test' in x; 3 in x; a in x;");
    testReparse("'foo\"bar' + \"foo'c\" + 'stuff\\n and \\\\more'");
    testReparse("x.__proto__;");
  }

// com.google.javascript.jscomp.CodePrinterTest::testDoLoopIECompatiblity
  public void testDoLoopIECompatiblity() {
    
    assertPrint("function f(){if(e1){do foo();while(e2)}else foo()}",
        "function f(){if(e1){do foo();while(e2)}else foo()}");

    assertPrint("function f(){if(e1)do foo();while(e2)else foo()}",
        "function f(){if(e1){do foo();while(e2)}else foo()}");

    assertPrint("if(x){do{foo()}while(y)}else bar()",
        "if(x){do foo();while(y)}else bar()");

    assertPrint("if(x)do{foo()}while(y);else bar()",
        "if(x){do foo();while(y)}else bar()");

    assertPrint("if(x){do{foo()}while(y)}",
        "if(x){do foo();while(y)}");

    assertPrint("if(x)do{foo()}while(y);",
        "if(x){do foo();while(y)}");

    assertPrint("if(x)A:do{foo()}while(y);",
        "if(x){A:do foo();while(y)}");

    assertPrint("var i = 0;a: do{b: do{i++;break b;} while(0);} while(0);",
        "var i=0;a:do{b:do{i++;break b}while(0)}while(0)");
  }

// com.google.javascript.jscomp.CodePrinterTest::testFunctionSafariCompatiblity
  public void testFunctionSafariCompatiblity() {
    
    assertPrint("function f(){if(e1){function goo(){return true}}else foo()}",
        "function f(){if(e1){function goo(){return true}}else foo()}");

    assertPrint("function f(){if(e1)function goo(){return true}else foo()}",
        "function f(){if(e1){function goo(){return true}}else foo()}");

    assertPrint("if(e1){function goo(){return true}}",
        "if(e1){function goo(){return true}}");

    assertPrint("if(e1)function goo(){return true}",
        "if(e1){function goo(){return true}}");

    assertPrint("if(e1)A:function goo(){return true}",
        "if(e1){A:function goo(){return true}}");
  }

// com.google.javascript.jscomp.CodePrinterTest::testExponents
  public void testExponents() {
    assertPrintNumber("1", 1);
    assertPrintNumber("10", 10);
    assertPrintNumber("100", 100);
    assertPrintNumber("1E3", 1000);
    assertPrintNumber("1E4", 10000);
    assertPrintNumber("1E5", 100000);
    assertPrintNumber("-1", -1);
    assertPrintNumber("-10", -10);
    assertPrintNumber("-100", -100);
    assertPrintNumber("-1E3", -1000);
    assertPrintNumber("-12341234E4", -123412340000L);
    assertPrintNumber("1E18", 1000000000000000000L);
    assertPrintNumber("1E5", 100000.0);
    assertPrintNumber("100000.1", 100000.1);

    assertPrintNumber("1E-6", 0.000001);
    assertPrintNumber("-0x38d7ea4c68001", -0x38d7ea4c68001L);
    assertPrintNumber("0x38d7ea4c68001", 0x38d7ea4c68001L);
  }

// com.google.javascript.jscomp.CodePrinterTest::testDirectEval
  public void testDirectEval() {
    assertPrint("eval('1');", "eval(\"1\")");
  }

// com.google.javascript.jscomp.CodePrinterTest::testIndirectEval
  public void testIndirectEval() {
    Node n = parse("eval('1');");
    assertPrintNode("eval(\"1\")", n);
    n.getFirstChild().getFirstChild().getFirstChild().putBooleanProp(
        Node.DIRECT_EVAL, false);
    assertPrintNode("(0,eval)(\"1\")", n);
  }

// com.google.javascript.jscomp.CodePrinterTest::testFreeCall1
  public void testFreeCall1() {
    assertPrint("foo(a);", "foo(a)");
    assertPrint("x.foo(a);", "x.foo(a)");
  }

// com.google.javascript.jscomp.CodePrinterTest::testFreeCall2
  public void testFreeCall2() {
    Node n = parse("foo(a);");
    assertPrintNode("foo(a)", n);
    Node call =  n.getFirstChild().getFirstChild();
    assertTrue(call.isCall());
    call.putBooleanProp(Node.FREE_CALL, true);
    assertPrintNode("foo(a)", n);
  }

// com.google.javascript.jscomp.CodePrinterTest::testFreeCall3
  public void testFreeCall3() {
    Node n = parse("x.foo(a);");
    assertPrintNode("x.foo(a)", n);
    Node call =  n.getFirstChild().getFirstChild();
    assertTrue(call.isCall());
    call.putBooleanProp(Node.FREE_CALL, true);
    assertPrintNode("(0,x.foo)(a)", n);
  }

// com.google.javascript.jscomp.CodePrinterTest::testPrintScript
  public void testPrintScript() {
    
    
    Node ast = new Node(Token.SCRIPT,
        new Node(Token.EXPR_RESULT, Node.newString("f")),
        new Node(Token.EXPR_RESULT, Node.newString("g")));
    String result = new CodePrinter.Builder(ast).setPrettyPrint(true).build();
    assertEquals("\"f\";\n\"g\";\n", result);
  }

// com.google.javascript.jscomp.CodePrinterTest::testObjectLit
  public void testObjectLit() {
    assertPrint("({x:1})", "({x:1})");
    assertPrint("var x=({x:1})", "var x={x:1}");
    assertPrint("var x={'x':1}", "var x={\"x\":1}");
    assertPrint("var x={1:1}", "var x={1:1}");
    assertPrint("({},42)+0", "({},42)+0");
  }

// com.google.javascript.jscomp.CodePrinterTest::testObjectLit2
  public void testObjectLit2() {
    assertPrint("var x={1:1}", "var x={1:1}");
    assertPrint("var x={'1':1}", "var x={1:1}");
    assertPrint("var x={'1.0':1}", "var x={\"1.0\":1}");
    assertPrint("var x={1.5:1}", "var x={\"1.5\":1}");

  }

// com.google.javascript.jscomp.CodePrinterTest::testObjectLit3
  public void testObjectLit3() {
    assertPrint("var x={3E9:1}",
                "var x={3E9:1}");
    assertPrint("var x={'3000000000':1}", 
                "var x={3E9:1}");
    assertPrint("var x={'3000000001':1}",
                "var x={3000000001:1}");
    assertPrint("var x={'6000000001':1}",  
                "var x={6000000001:1}");
    assertPrint("var x={\"12345678901234567\":1}",  
                "var x={\"12345678901234567\":1}");
  }

// com.google.javascript.jscomp.CodePrinterTest::testObjectLit4
  public void testObjectLit4() {
    
    assertPrint(
        "var x={\"123456789012345671234567890123456712345678901234567\":1}",
        "var x={\"123456789012345671234567890123456712345678901234567\":1}");
  }

// com.google.javascript.jscomp.CodePrinterTest::testGetter
  public void testGetter() {
    assertPrint("var x = {}", "var x={}");
    assertPrint("var x = {get a() {return 1}}", "var x={get a(){return 1}}");
    assertPrint(
      "var x = {get a() {}, get b(){}}",
      "var x={get a(){},get b(){}}");

    assertPrint(
      "var x = {get 'a'() {return 1}}",
      "var x={get \"a\"(){return 1}}");

    assertPrint(
      "var x = {get 1() {return 1}}",
      "var x={get 1(){return 1}}");

    assertPrint(
      "var x = {get \"()\"() {return 1}}",
      "var x={get \"()\"(){return 1}}");

    languageMode = LanguageMode.ECMASCRIPT5;
    assertPrintSame("var x={get function(){return 1}}");

    
    
    languageMode = LanguageMode.ECMASCRIPT3;
    assertPrintSame("var x={get function(){return 1}}");
  }

// com.google.javascript.jscomp.CodePrinterTest::testSetter
  public void testSetter() {
    assertPrint("var x = {}", "var x={}");
    assertPrint(
       "var x = {set a(y) {return 1}}",
       "var x={set a(y){return 1}}");

    assertPrint(
      "var x = {get 'a'() {return 1}}",
      "var x={get \"a\"(){return 1}}");

    assertPrint(
      "var x = {set 1(y) {return 1}}",
      "var x={set 1(y){return 1}}");

    assertPrint(
      "var x = {set \"(x)\"(y) {return 1}}",
      "var x={set \"(x)\"(y){return 1}}");

    languageMode = LanguageMode.ECMASCRIPT5;
    assertPrintSame("var x={set function(x){}}");

    
    
    languageMode = LanguageMode.ECMASCRIPT3;
    assertPrintSame("var x={set function(x){}}");
  }

// com.google.javascript.jscomp.CodePrinterTest::testNegCollapse
  public void testNegCollapse() {
    
    
    assertPrint("var x = - - 2;", "var x=2");
    assertPrint("var x = - (2);", "var x=-2");
  }

// com.google.javascript.jscomp.CodePrinterTest::testStrict
  public void testStrict() {
    String result = parsePrint("var x", false, false, 0, false, true);
    assertEquals("'use strict';var x", result);
  }

// com.google.javascript.jscomp.CodePrinterTest::testArrayLiteral
  public void testArrayLiteral() {
    assertPrint("var x = [,];","var x=[,]");
    assertPrint("var x = [,,];","var x=[,,]");
    assertPrint("var x = [,s,,];","var x=[,s,,]");
    assertPrint("var x = [,s];","var x=[,s]");
    assertPrint("var x = [s,];","var x=[s]");
  }

// com.google.javascript.jscomp.CodePrinterTest::testZero
  public void testZero() {
    assertPrint("var x ='\\0';", "var x=\"\\x00\"");
    assertPrint("var x ='\\x00';", "var x=\"\\x00\"");
    assertPrint("var x ='\\u0000';", "var x=\"\\x00\"");
    assertPrint("var x ='\\u00003';", "var x=\"\\x003\"");
  }

// com.google.javascript.jscomp.CodePrinterTest::testUnicode
  public void testUnicode() {
    assertPrint("var x ='\\x0f';", "var x=\"\\u000f\"");
    assertPrint("var x ='\\x68';", "var x=\"h\"");
    assertPrint("var x ='\\x7f';", "var x=\"\\u007f\"");
  }

// com.google.javascript.jscomp.CodePrinterTest::testUnicodeKeyword
  public void testUnicodeKeyword() {
    
    assertPrint("var \\u0069\\u0066 = 1;", "var i\\u0066=1");
    
    assertPrint("var v\\u0061\\u0072 = 1;", "var va\\u0072=1");
    
    assertPrint("var w\\u0068\\u0069\\u006C\\u0065 = 1;"
        + "\\u0077\\u0068il\\u0065 = 2;"
        + "\\u0077h\\u0069le = 3;",
        "var whil\\u0065=1;whil\\u0065=2;whil\\u0065=3");
  }

// com.google.javascript.jscomp.CodePrinterTest::testNumericKeys
  public void testNumericKeys() {
    assertPrint("var x = {010: 1};", "var x={8:1}");
    assertPrint("var x = {'010': 1};", "var x={\"010\":1}");

    assertPrint("var x = {0x10: 1};", "var x={16:1}");
    assertPrint("var x = {'0x10': 1};", "var x={\"0x10\":1}");

    
    assertPrint("var x = {.2: 1};", "var x={\"0.2\":1}");
    assertPrint("var x = {'.2': 1};", "var x={\".2\":1}");

    assertPrint("var x = {0.2: 1};", "var x={\"0.2\":1}");
    assertPrint("var x = {'0.2': 1};", "var x={\"0.2\":1}");
  }

// com.google.javascript.jscomp.CodePrinterTest::testIssue582
  public void testIssue582() {
    assertPrint("var x = -0.0;", "var x=-0");
  }

// com.google.javascript.jscomp.CodePrinterTest::testIssue942
  public void testIssue942() {
    assertPrint("var x = {0: 1};", "var x={0:1}");
  }

// com.google.javascript.jscomp.CodePrinterTest::testIssue601
  public void testIssue601() {
    assertPrint("'\\v' == 'v'", "\"\\v\"==\"v\"");
    assertPrint("'\\u000B' == '\\v'", "\"\\x0B\"==\"\\v\"");
    assertPrint("'\\x0B' == '\\v'", "\"\\x0B\"==\"\\v\"");
  }

// com.google.javascript.jscomp.CodePrinterTest::testIssue620
  public void testIssue620() {
    assertPrint("alert(/ / / / /);", "alert(/ 
    assertPrint("alert(/ 
  }

// com.google.javascript.jscomp.CodePrinterTest::testIssue5746867
  public void testIssue5746867() {
    assertPrint("var a = { '$\\\\' : 5 };", "var a={\"$\\\\\":5}");
  }

// com.google.javascript.jscomp.CodePrinterTest::testCommaSpacing
  public void testCommaSpacing() {
    assertPrint("var a = (b = 5, c = 5);",
        "var a=(b=5,c=5)");
    assertPrettyPrint("var a = (b = 5, c = 5);",
        "var a = (b = 5, c = 5);\n");
  }

// com.google.javascript.jscomp.CodePrinterTest::testManyCommas
  public void testManyCommas() {
    int numCommas = 10000;
    List<String> numbers = Lists.newArrayList("0", "1");
    Node current = new Node(Token.COMMA, Node.newNumber(0), Node.newNumber(1));
    for (int i = 2; i < numCommas; i++) {
      current = new Node(Token.COMMA, current);

      
      int num = i % 1000;
      numbers.add(String.valueOf(num));
      current.addChildToBack(Node.newNumber(num));
    }

    String expected = Joiner.on(",").join(numbers);
    String actual = printNode(current).replace("\n", "");
    assertEquals(expected, actual);
  }

// com.google.javascript.jscomp.CodePrinterTest::testManyAdds
  public void testManyAdds() {
    int numAdds = 10000;
    List<String> numbers = Lists.newArrayList("0", "1");
    Node current = new Node(Token.ADD, Node.newNumber(0), Node.newNumber(1));
    for (int i = 2; i < numAdds; i++) {
      current = new Node(Token.ADD, current);

      
      int num = i % 1000;
      numbers.add(String.valueOf(num));
      current.addChildToBack(Node.newNumber(num));
    }

    String expected = Joiner.on("+").join(numbers);
    String actual = printNode(current).replace("\n", "");
    assertEquals(expected, actual);
  }

// com.google.javascript.jscomp.CodePrinterTest::testMinusNegativeZero
  public void testMinusNegativeZero() {
    
    
    assertPrint("x- -0", "x- -0");
  }

// com.google.javascript.jscomp.CodePrinterTest::testStringEscapeSequences
  public void testStringEscapeSequences() {
    
    assertPrintSame("var x=\"\\b\"");
    assertPrintSame("var x=\"\\f\"");
    assertPrintSame("var x=\"\\n\"");
    assertPrintSame("var x=\"\\r\"");
    assertPrintSame("var x=\"\\t\"");
    assertPrintSame("var x=\"\\v\"");
    assertPrint("var x=\"\\\"\"", "var x='\"'");
    assertPrint("var x=\"\\\'\"", "var x=\"'\"");

    
    assertPrint("var x=\"\\u000A\"", "var x=\"\\n\"");
    assertPrint("var x=\"\\u000D\"", "var x=\"\\r\"");
    assertPrintSame("var x=\"\\u2028\"");
    assertPrintSame("var x=\"\\u2029\"");

    
    assertPrintSame("var x=/\\b/");
    assertPrintSame("var x=/\\f/");
    assertPrintSame("var x=/\\n/");
    assertPrintSame("var x=/\\r/");
    assertPrintSame("var x=/\\t/");
    assertPrintSame("var x=/\\v/");
    assertPrintSame("var x=/\\u000A/");
    assertPrintSame("var x=/\\u000D/");
    assertPrintSame("var x=/\\u2028/");
    assertPrintSame("var x=/\\u2029/");
  }

// com.google.javascript.jscomp.CodePrinterTest::testKeywordProperties1
  public void testKeywordProperties1() {
    languageMode = LanguageMode.ECMASCRIPT5;
    assertPrintSame("x.foo=2");
    assertPrintSame("x.function=2");

    languageMode = LanguageMode.ECMASCRIPT3;
    assertPrintSame("x.foo=2");
    assertPrint("x.function=2", "x[\"function\"]=2");
  }

// com.google.javascript.jscomp.CodePrinterTest::testKeywordProperties2
  public void testKeywordProperties2() {
    languageMode = LanguageMode.ECMASCRIPT5;
    assertPrintSame("x={foo:2}");
    assertPrintSame("x={function:2}");

    languageMode = LanguageMode.ECMASCRIPT3;
    assertPrintSame("x={foo:2}");
    assertPrint("x={function:2}", "x={\"function\":2}");
  }

// com.google.javascript.jscomp.CodePrinterTest::testIssue1062
  public void testIssue1062() {
    assertPrintSame("3*(4%3*5)");
  }

// com.google.javascript.jscomp.CollapseAnonymousFunctionsTest::testGlobalScope
  public void testGlobalScope() {
    test("var f = function(){}", "function f(){}");
  }

// com.google.javascript.jscomp.CollapseAnonymousFunctionsTest::testLocalScope1
  public void testLocalScope1() {
    test("function f(){ var x = function(){}; x() }",
         "function f(){ function x(){} x() }");
  }

// com.google.javascript.jscomp.CollapseAnonymousFunctionsTest::testLocalScope2
  public void testLocalScope2() {
    test("function f(){ var x = function(){}; return x }",
         "function f(){ function x(){} return x }");
  }

// com.google.javascript.jscomp.CollapseAnonymousFunctionsTest::testVarNotImmediatelyBelowScriptOrBlock1
  public void testVarNotImmediatelyBelowScriptOrBlock1() {
    testSame("if (x) var f = function(){}");
  }

// com.google.javascript.jscomp.CollapseAnonymousFunctionsTest::testVarNotImmediatelyBelowScriptOrBlock2
  public void testVarNotImmediatelyBelowScriptOrBlock2() {
    testSame("var x = 1;" +
             "if (x == 1) {" +
             "  var f = function () { alert('b')}" +
             "} else {" +
             "  f = function() { alert('c')}" +
             "}" +
             "f();");
  }

// com.google.javascript.jscomp.CollapseAnonymousFunctionsTest::testVarNotImmediatelyBelowScriptOrBlock3
  public void testVarNotImmediatelyBelowScriptOrBlock3() {
    testSame("var x = 1; if (x) {var f = function(){return x}; f(); x--;}");
  }

// com.google.javascript.jscomp.CollapseAnonymousFunctionsTest::testMultipleVar
  public void testMultipleVar() {
    test("var f = function(){}; var g = f", "function f(){} var g = f");
  }

// com.google.javascript.jscomp.CollapseAnonymousFunctionsTest::testMultipleVar2
  public void testMultipleVar2() {
    test("var f = function(){}; var g = f; var h = function(){}",
         "function f(){}var g = f;function h(){}");
  }

// com.google.javascript.jscomp.CollapseAnonymousFunctionsTest::testBothScopes
  public void testBothScopes() {
    test("var x = function() { var y = function(){} }",
         "function x() { function y(){} }");
  }

// com.google.javascript.jscomp.CollapseAnonymousFunctionsTest::testLocalScopeOnly1
  public void testLocalScopeOnly1() {
    test("if (x) var f = function(){ var g = function(){} }",
         "if (x) var f = function(){ function g(){} }");
  }

// com.google.javascript.jscomp.CollapseAnonymousFunctionsTest::testLocalScopeOnly2
  public void testLocalScopeOnly2() {
    test("if (x) var f = function(){ var g = function(){} };",
         "if (x) var f = function(){ function g(){} }");
  }

// com.google.javascript.jscomp.CollapseAnonymousFunctionsTest::testReturn
  public void testReturn() {
    test("var f = function(x){return 2*x}; var g = f(2);",
         "function f(x){return 2*x} var g = f(2)");
  }

// com.google.javascript.jscomp.CollapseAnonymousFunctionsTest::testAlert
  public void testAlert() {
    test("var x = 1; var f = function(){alert(x)};",
         "var x = 1; function f(){alert(x)}");
  }

// com.google.javascript.jscomp.CollapseAnonymousFunctionsTest::testRecursiveInternal1
  public void testRecursiveInternal1() {
    testSame("var f = function foo() { foo() }");
  }

// com.google.javascript.jscomp.CollapseAnonymousFunctionsTest::testRecursiveInternal2
  public void testRecursiveInternal2() {
    testSame("var f = function foo() { function g(){foo()} g() }");
  }

// com.google.javascript.jscomp.CollapseAnonymousFunctionsTest::testRecursiveExternal1
  public void testRecursiveExternal1() {
    test("var f = function foo() { f() }",
         "function f() { f() }");
  }

// com.google.javascript.jscomp.CollapseAnonymousFunctionsTest::testRecursiveExternal2
  public void testRecursiveExternal2() {
    test("var f = function foo() { function g(){f()} g() }",
         "function f() { function g(){f()} g() }");
  }

// com.google.javascript.jscomp.CollapseAnonymousFunctionsTest::testConstantFunction1
  public void testConstantFunction1() {
    test("var FOO = function(){};FOO()",
         "function FOO(){}FOO()");
  }

// com.google.javascript.jscomp.CollapseAnonymousFunctionsTest::testInnerFunction1
  public void testInnerFunction1() {
    test(
        "function f() { " +
        "  var x = 3; var y = function() { return 4; }; return x + y();" +
        "}",
        "function f() { " +
        "  function y() { return 4; } var x = 3; return x + y();" +
        "}");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testCollapse
  public void testCollapse() {
    test("var a = {}; a.b = {}; var c = a.b;",
         "var a$b = {}; var c = a$b");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testMultiLevelCollapse
  public void testMultiLevelCollapse() {
    test("var a = {}; a.b = {}; a.b.c = {}; var d = a.b.c;",
         "var a$b$c = {}; var d = a$b$c;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testDecrement
  public void testDecrement() {
    test("var a = {}; a.b = 5; a.b--; a.b = 5",
         "var a$b = 5; a$b--; a$b = 5");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testIncrement
  public void testIncrement() {
    test("var a = {}; a.b = 5; a.b++; a.b = 5",
         "var a$b = 5; a$b++; a$b = 5");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testObjLitDeclaration
  public void testObjLitDeclaration() {
    test("var a = {b: {}, c: {}}; var d = a.b; var e = a.c",
         "var a$b = {}; var a$c = {}; var d = a$b; var e = a$c");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testObjLitDeclarationWithGet1
  public void testObjLitDeclarationWithGet1() {
    testSame("var a = {get b(){}};");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testObjLitDeclarationWithGet2
  public void testObjLitDeclarationWithGet2() {
    test("var a = {b: {}, get c(){}}; var d = a.b; var e = a.c",
         "var a$b = {};var a = {get c(){}};var d = a$b; var e = a.c");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testObjLitDeclarationWithGet3
  public void testObjLitDeclarationWithGet3() {
    test("var a = {b: {get c() { return 3; }}};",
         "var a$b = {get c() { return 3; }};");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testObjLitDeclarationWithSet1
  public void testObjLitDeclarationWithSet1() {
    testSame("var a = {set b(a){}};");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testObjLitDeclarationWithSet2
  public void testObjLitDeclarationWithSet2() {
    test("var a = {b: {}, set c(a){}}; var d = a.b; var e = a.c",
         "var a$b = {};var a = {set c(a){}};var d = a$b; var e = a.c");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testObjLitDeclarationWithSet3
  public void testObjLitDeclarationWithSet3() {
    test("var a = {b: {set c(d) {}}};",
         "var a$b = {set c(d) {}};");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testObjLitDeclarationWithGetAndSet1
  public void testObjLitDeclarationWithGetAndSet1() {
    test("var a = {b: {get c() { return 3; },set c(d) {}}};",
         "var a$b = {get c() { return 3; },set c(d) {}};");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testObjLitDeclarationWithDuplicateKeys
  public void testObjLitDeclarationWithDuplicateKeys() {
    disableNormalize();
    test("var a = {b: 0, b: 1}; var c = a.b;",
         "var a$b = 0; var a$b = 1; var c = a$b;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testObjLitAssignmentDepth1
  public void testObjLitAssignmentDepth1() {
    test("var a = {b: {}, c: {}}; var d = a.b; var e = a.c",
         "var a$b = {}; var a$c = {}; var d = a$b; var e = a$c");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testObjLitAssignmentDepth2
  public void testObjLitAssignmentDepth2() {
    test("var a = {}; a.b = {c: {}, d: {}}; var e = a.b.c; var f = a.b.d",
         "var a$b$c = {}; var a$b$d = {}; var e = a$b$c; var f = a$b$d");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testObjLitAssignmentDepth3
  public void testObjLitAssignmentDepth3() {
    test("var a = {}; a.b = {}; a.b.c = {d: 1, e: 2}; var f = a.b.c.d",
         "var a$b$c$d = 1; var a$b$c$e = 2; var f = a$b$c$d");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testObjLitAssignmentDepth4
  public void testObjLitAssignmentDepth4() {
    test("var a = {}; a.b = {}; a.b.c = {}; a.b.c.d = {e: 1, f: 2}; " +
         "var g = a.b.c.d.e",
         "var a$b$c$d$e = 1; var a$b$c$d$f = 2; var g = a$b$c$d$e");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testGlobalObjectDeclaredToPreserveItsPreviousValue1
  public void testGlobalObjectDeclaredToPreserveItsPreviousValue1() {
    test("var a = a ? a : {}; a.c = 1;",
         "var a = a ? a : {}; var a$c = 1;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testGlobalObjectDeclaredToPreserveItsPreviousValue2
  public void testGlobalObjectDeclaredToPreserveItsPreviousValue2() {
    test("var a = a || {}; a.c = 1;",
         "var a = a || {}; var a$c = 1;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testGlobalObjectDeclaredToPreserveItsPreviousValue3
  public void testGlobalObjectDeclaredToPreserveItsPreviousValue3() {
    test("var a = a || {get b() {}}; a.c = 1;",
         "var a = a || {get b() {}}; var a$c = 1;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testGlobalObjectNameInBooleanExpressionDepth1_1
  public void testGlobalObjectNameInBooleanExpressionDepth1_1() {
    test("var a = {b: 0}; a.c = 1; if (a) x();",
         "var a$b = 0; var a = {}; var a$c = 1; if (a) x();");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testGlobalObjectNameInBooleanExpressionDepth1_2
  public void testGlobalObjectNameInBooleanExpressionDepth1_2() {
    test("var a = {b: 0}; a.c = 1; if (!(a && a.c)) x();",
         "var a$b = 0; var a = {}; var a$c = 1; if (!(a && a$c)) x();");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testGlobalObjectNameInBooleanExpressionDepth1_3
  public void testGlobalObjectNameInBooleanExpressionDepth1_3() {
    test("var a = {b: 0}; a.c = 1; while (a || a.c) x();",
         "var a$b = 0; var a = {}; var a$c = 1; while (a || a$c) x();");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testGlobalObjectNameInBooleanExpressionDepth1_4
  public void testGlobalObjectNameInBooleanExpressionDepth1_4() {
    testSame("var a = {}; a.c = 1; var d = a || {}; a.c;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testGlobalObjectNameInBooleanExpressionDepth1_5
  public void testGlobalObjectNameInBooleanExpressionDepth1_5() {
    testSame("var a = {}; a.c = 1; var d = a.c || a; a.c;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testGlobalObjectNameInBooleanExpressionDepth1_6
  public void testGlobalObjectNameInBooleanExpressionDepth1_6() {
    test("var a = {b: 0}; a.c = 1; var d = !(a.c || a); a.c;",
         "var a$b = 0; var a = {}; var a$c = 1; var d = !(a$c || a); a$c;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testGlobalObjectNameInBooleanExpressionDepth2
  public void testGlobalObjectNameInBooleanExpressionDepth2() {
    test("var a = {b: {}}; a.b.c = 1; if (a.b) x(a.b.c);",
         "var a$b = {}; var a$b$c = 1; if (a$b) x(a$b$c);");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testGlobalObjectNameInBooleanExpressionDepth3
  public void testGlobalObjectNameInBooleanExpressionDepth3() {
    
    
    
    
    
    test("var a = {}; a.b = {};  a.b.c = function(){};" +
         " a.b.z = 1; var d = a.b && a.b.c;",
         "var a$b = {}; var a$b$c = function(){};" +
         " a$b.z = 1; var d = a$b && a$b$c;", null,
         CollapseProperties.UNSAFE_NAMESPACE_WARNING);
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testGlobalFunctionNameInBooleanExpressionDepth1
  public void testGlobalFunctionNameInBooleanExpressionDepth1() {
    test("function a() {} a.c = 1; if (a) x(a.c);",
         "function a() {} var a$c = 1; if (a) x(a$c);");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testGlobalFunctionNameInBooleanExpressionDepth2
  public void testGlobalFunctionNameInBooleanExpressionDepth2() {
    test("var a = {b: function(){}}; a.b.c = 1; if (a.b) x(a.b.c);",
         "var a$b = function(){}; var a$b$c = 1; if (a$b) x(a$b$c);");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testAliasCreatedForObjectDepth1_1
  public void testAliasCreatedForObjectDepth1_1() {
    
    
    testSame("var a = {b: 0}; var c = a; c.b = 1; a.b == c.b;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testAliasCreatedForObjectDepth1_2
  public void testAliasCreatedForObjectDepth1_2() {
    testSame("var a = {b: 0}; f(a); a.b;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testAliasCreatedForObjectDepth1_3
  public void testAliasCreatedForObjectDepth1_3() {
    testSame("var a = {b: 0}; new f(a); a.b;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testAliasCreatedForObjectDepth2_1
  public void testAliasCreatedForObjectDepth2_1() {
    test("var a = {}; a.b = {c: 0}; var d = a.b; a.b.c == d.c;",
         "var a$b = {c: 0}; var d = a$b; a$b.c == d.c;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testAliasCreatedForObjectDepth2_2
  public void testAliasCreatedForObjectDepth2_2() {
    test("var a = {}; a.b = {c: 0}; for (var p in a.b) { e(a.b[p]); }",
         "var a$b = {c: 0}; for (var p in a$b) { e(a$b[p]); }");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testAliasCreatedForEnumDepth1_1
  public void testAliasCreatedForEnumDepth1_1() {
    
    
    test(" var a = {b: 0}; var c = a; c.b = 1; a.b != c.b;",
         "var a$b = 0; var a = {b: a$b}; var c = a; c.b = 1; a$b != c.b;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testAliasCreatedForEnumDepth1_2
  public void testAliasCreatedForEnumDepth1_2() {
    test(" var a = {b: 0}; f(a); a.b;",
         "var a$b = 0; var a = {b: a$b}; f(a); a$b;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testAliasCreatedForEnumDepth1_3
  public void testAliasCreatedForEnumDepth1_3() {
    test(" var a = {b: 0}; new f(a); a.b;",
         "var a$b = 0; var a = {b: a$b}; new f(a); a$b;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testAliasCreatedForEnumDepth1_4
  public void testAliasCreatedForEnumDepth1_4() {
    test(" var a = {b: 0}; for (var p in a) { f(a[p]); }",
         "var a$b = 0; var a = {b: a$b}; for (var p in a) { f(a[p]); }");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testAliasCreatedForEnumDepth2_1
  public void testAliasCreatedForEnumDepth2_1() {
    test("var a = {};  a.b = {c: 0};" +
         "var d = a.b; d.c = 1; a.b.c != d.c;",
         "var a$b$c = 0; var a$b = {c: a$b$c};" +
         "var d = a$b; d.c = 1; a$b$c != d.c;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testAliasCreatedForEnumDepth2_2
  public void testAliasCreatedForEnumDepth2_2() {
    test("var a = {};  a.b = {c: 0};" +
         "for (var p in a.b) { f(a.b[p]); }",
         "var a$b$c = 0; var a$b = {c: a$b$c};" +
         "for (var p in a$b) { f(a$b[p]); }");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testAliasCreatedForEnumDepth2_3
  public void testAliasCreatedForEnumDepth2_3() {
    test("var a = {}; var d = a;  a.b = {c: 0};" +
         "for (var p in a.b) { f(a.b[p]); }",
         "var a = {}; var d = a; var a$b$c = 0; var a$b = {c: a$b$c};" +
         "for (var p in a$b) { f(a$b[p]); }",
         null, CollapseProperties.UNSAFE_NAMESPACE_WARNING);
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testAliasCreatedForEnumOfObjects
  public void testAliasCreatedForEnumOfObjects() {
    test("var a = {}; " +
         " a.b = {c: {d: 1}}; a.b.c;" +
         "searchEnum(a.b);",
         "var a$b$c = {d: 1};var a$b = {c: a$b$c}; a$b$c; " +
         "searchEnum(a$b)");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testAliasCreatedForEnumOfObjects2
  public void testAliasCreatedForEnumOfObjects2() {
    test("var a = {}; " +
         " a.b = {c: {d: 1}}; a.b.c.d;" +
         "searchEnum(a.b);",
         "var a$b$c = {d: 1};var a$b = {c: a$b$c}; a$b$c.d; " +
         "searchEnum(a$b)");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testAliasCreatedForPropertyOfEnumOfObjects
  public void testAliasCreatedForPropertyOfEnumOfObjects() {
    test("var a = {}; " +
         " a.b = {c: {d: 1}}; a.b.c;" +
         "searchEnum(a.b.c);",
         "var a$b$c = {d: 1}; a$b$c; searchEnum(a$b$c);");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testAliasCreatedForPropertyOfEnumOfObjects2
  public void testAliasCreatedForPropertyOfEnumOfObjects2() {
    test("var a = {}; " +
         " a.b = {c: {d: 1}}; a.b.c.d;" +
         "searchEnum(a.b.c);",
         "var a$b$c = {d: 1}; a$b$c.d; searchEnum(a$b$c);");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testMisusedEnumTag
  public void testMisusedEnumTag() {
    testSame("var a = {}; var d = a; a.b = function() {};" +
             " a.b.c = 0; a.b.c;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testMisusedConstructorTag
  public void testMisusedConstructorTag() {
    testSame("var a = {}; var d = a; a.b = function() {};" +
             " a.b.c = 0; a.b.c;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testAliasCreatedForFunctionDepth1_1
  public void testAliasCreatedForFunctionDepth1_1() {
    testSame("var a = function(){}; a.b = 1; var c = a; c.b = 2; a.b != c.b;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testAliasCreatedForCtorDepth1_1
  public void testAliasCreatedForCtorDepth1_1() {
    
    
    
    
    
    
    test(" var a = function(){}; a.b = 1; " +
         "var c = a; c.b = 2; a.b != c.b;",
         "var a = function(){}; var a$b = 1; var c = a; c.b = 2; a$b != c.b;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testAliasCreatedForFunctionDepth1_2
  public void testAliasCreatedForFunctionDepth1_2() {
    testSame("var a = function(){}; a.b = 1; f(a); a.b;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testAliasCreatedForCtorDepth1_2
  public void testAliasCreatedForCtorDepth1_2() {
    test(" var a = function(){}; a.b = 1; f(a); a.b;",
         "var a = function(){}; var a$b = 1; f(a); a$b;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testAliasCreatedForFunctionDepth1_3
  public void testAliasCreatedForFunctionDepth1_3() {
    testSame("var a = function(){}; a.b = 1; new f(a); a.b;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testAliasCreatedForCtorDepth1_3
  public void testAliasCreatedForCtorDepth1_3() {
    test(" var a = function(){}; a.b = 1; new f(a); a.b;",
         "var a = function(){}; var a$b = 1; new f(a); a$b;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testAliasCreatedForFunctionDepth2
  public void testAliasCreatedForFunctionDepth2() {
    test(
        "var a = {}; a.b = function() {}; a.b.c = 1; var d = a.b;" +
        "a.b.c != d.c;",
        "var a$b = function() {}; a$b.c = 1; var d = a$b;" +
        "a$b.c != d.c;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testAliasCreatedForCtorDepth2
  public void testAliasCreatedForCtorDepth2() {
    test("var a = {};  a.b = function() {}; " +
         "a.b.c = 1; var d = a.b;" +
         "a.b.c != d.c;",
         "var a$b = function() {}; var a$b$c = 1; var d = a$b;" +
         "a$b$c != d.c;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testAliasCreatedForClassDepth1_1
  public void testAliasCreatedForClassDepth1_1() {
    
    
    test("var a = {};  a.b = function(){};" +
         "var c = a; c.b = 0; a.b != c.b;",
         "var a = {}; var a$b = function(){};" +
         "var c = a; c.b = 0; a$b != c.b;", null,
         CollapseProperties.UNSAFE_NAMESPACE_WARNING);
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testAliasCreatedForClassDepth1_2
  public void testAliasCreatedForClassDepth1_2() {
    test("var a = {};  a.b = function(){}; f(a); a.b;",
         "var a = {}; var a$b = function(){}; f(a); a$b;",
         null, CollapseProperties.UNSAFE_NAMESPACE_WARNING);
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testAliasCreatedForClassDepth1_3
  public void testAliasCreatedForClassDepth1_3() {
    test("var a = {};  a.b = function(){}; new f(a); a.b;",
         "var a = {}; var a$b = function(){}; new f(a); a$b;",
         null, CollapseProperties.UNSAFE_NAMESPACE_WARNING);
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testAliasCreatedForClassDepth2_1
  public void testAliasCreatedForClassDepth2_1() {
    test("var a = {}; a.b = {};  a.b.c = function(){};" +
         "var d = a.b; a.b.c != d.c;",
         "var a$b = {}; var a$b$c = function(){};" +
         "var d = a$b; a$b$c != d.c;",
         null, CollapseProperties.UNSAFE_NAMESPACE_WARNING);
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testAliasCreatedForClassDepth2_2
  public void testAliasCreatedForClassDepth2_2() {
    test("var a = {}; a.b = {};  a.b.c = function(){};" +
         "f(a.b); a.b.c;",
         "var a$b = {}; var a$b$c = function(){}; f(a$b); a$b$c;",
         null, CollapseProperties.UNSAFE_NAMESPACE_WARNING);
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testAliasCreatedForClassDepth2_3
  public void testAliasCreatedForClassDepth2_3() {
    test("var a = {}; a.b = {};  a.b.c = function(){};" +
         "new f(a.b); a.b.c;",
         "var a$b = {}; var a$b$c = function(){}; new f(a$b); a$b$c;",
         null, CollapseProperties.UNSAFE_NAMESPACE_WARNING);
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testAliasCreatedForClassProperty
  public void testAliasCreatedForClassProperty() {
    test("var a = {};  a.b = function(){};" +
         "a.b.c = {d: 3}; new f(a.b.c); a.b.c.d;",
         "var a$b = function(){}; var a$b$c = {d:3}; new f(a$b$c); a$b$c.d;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testNestedObjLit
  public void testNestedObjLit() {
    test("var a = {}; a.b = {f: 0, c: {d: 1}}; var e = a.b.c.d",
         "var a$b$f = 0; var a$b$c$d = 1; var e = a$b$c$d;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testObjLitDeclarationUsedInSameVarList
  public void testObjLitDeclarationUsedInSameVarList() {
    
    
    test("var a = {b: {}, c: {}}; var d = a.b; var e = a.c;",
         "var a$b = {}; var a$c = {}; var d = a$b; var e = a$c;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testPropGetInsideAnObjLit
  public void testPropGetInsideAnObjLit() {
    test("var x = {}; x.y = 1; var a = {}; a.b = {c: x.y}",
         "var x$y = 1; var a$b$c = x$y;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testObjLitWithQuotedKeyThatDoesNotGetRead
  public void testObjLitWithQuotedKeyThatDoesNotGetRead() {
    test("var a = {}; a.b = {c: 0, 'd': 1}; var e = a.b.c;",
         "var a$b$c = 0; var a$b$d = 1; var e = a$b$c;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testObjLitWithQuotedKeyThatGetsRead
  public void testObjLitWithQuotedKeyThatGetsRead() {
    test("var a = {}; a.b = {c: 0, 'd': 1}; var e = a.b['d'];",
         "var a$b = {c: 0, 'd': 1}; var e = a$b['d'];");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testFunctionWithQuotedPropertyThatDoesNotGetRead
  public void testFunctionWithQuotedPropertyThatDoesNotGetRead() {
    test("var a = {}; a.b = function() {}; a.b['d'] = 1;",
         "var a$b = function() {}; a$b['d'] = 1;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testFunctionWithQuotedPropertyThatGetsRead
  public void testFunctionWithQuotedPropertyThatGetsRead() {
    test("var a = {}; a.b = function() {}; a.b['d'] = 1; f(a.b['d']);",
         "var a$b = function() {}; a$b['d'] = 1; f(a$b['d']);");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testObjLitAssignedToMultipleNames1
  public void testObjLitAssignedToMultipleNames1() {
    
    testSame("var a = b = {c: 0, d: 1}; var e = a.c; var f = b.d;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testObjLitAssignedToMultipleNames2
  public void testObjLitAssignedToMultipleNames2() {
    testSame("a = b = {c: 0, d: 1}; var e = a.c; var f = b.d;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testObjLitRedefinedInGlobalScope
  public void testObjLitRedefinedInGlobalScope() {
    testSame("a = {b: 0}; a = {c: 1}; var d = a.b; var e = a.c;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testObjLitRedefinedInLocalScope
  public void testObjLitRedefinedInLocalScope() {
    test("var a = {}; a.b = {c: 0}; function d() { a.b = {c: 1}; } e(a.b.c);",
         "var a$b = {c: 0}; function d() { a$b = {c: 1}; } e(a$b.c);");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testObjLitAssignedInTernaryExpression1
  public void testObjLitAssignedInTernaryExpression1() {
    testSame("a = x ? {b: 0} : d; var c = a.b;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testObjLitAssignedInTernaryExpression2
  public void testObjLitAssignedInTernaryExpression2() {
    testSame("a = x ? {b: 0} : {b: 1}; var c = a.b;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testGlobalVarSetToObjLitConditionally1
  public void testGlobalVarSetToObjLitConditionally1() {
    testSame("var a; if (x) a = {b: 0}; var c = x ? a.b : 0;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testGlobalVarSetToObjLitConditionally1b
  public void testGlobalVarSetToObjLitConditionally1b() {
    test("if (x) var a = {b: 0}; var c = x ? a.b : 0;",
         "if (x) var a$b = 0; var c = x ? a$b : 0;");
  }

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

// com.google.javascript.jscomp.CollapsePropertiesTest::testFunctionAlias1
  public void testFunctionAlias1() {
    test("var a = {}; a.b = {}; a.b.c = function(){}; a.b.d = a.b.c;a.b.d=null",
         "var a$b$c = function(){}; var a$b$d = a$b$c;a$b$d=null;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testFunctionAlias2
  public void testFunctionAlias2() {
    test("var a = {}; a.b = {}; a.b.c = function(){}; a.b.d = a.b.c;use(a.b.d)",
         "var a$b$c = function(){}; var a$b$d = null;use(a$b$c);");
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
    
    
    
    test("var a = {b: 0}; a.c = a.b;a.c = null",
         "var a$b = 0; var a$c = a$b;a$c = null");
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

// com.google.javascript.jscomp.CollapsePropertiesTest::testInlineAliasWithModifications
  public void testInlineAliasWithModifications() {
    testSame("var x = 10; function f() { var y = x; x++; alert(y)} ");
    testSame("var x = 10; function f() { var y = x; x+=1; alert(y)} ");
    test("var x = {}; x.x = 10; function f() {var y=x.x; x.x++; alert(y)}",
         "var x$x = 10; function f() {var y=x$x; x$x++; alert(y)}");
    test("var x = {}; x.x = 10; function f() {var y=x.x; x.x+=1; alert(y)}",
         "var x$x = 10; function f() {var y=x$x; x$x+=1; alert(y)}");
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

// com.google.javascript.jscomp.CollapsePropertiesTest::testIssue931
  public void testIssue931() {
    collapsePropertiesOnExternTypes = true;
    testSame(
      "function f() {\n" +
      "  return function () {\n" +
      "    var args = arguments;\n" +
      "    setTimeout(function() { alert(args); }, 0);\n" +
      "  }\n" +
      "};\n");
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

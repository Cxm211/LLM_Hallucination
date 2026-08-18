// buggy code
  private void replaceReferencesToThis(Node node, String name) {
    if (NodeUtil.isFunction(node)) {
      return;
    }

    for (Node child : node.children()) {
      if (NodeUtil.isThis(child)) {
        Node newName = Node.newString(Token.NAME, name);
        node.replaceChild(child, newName);
      } else {
        replaceReferencesToThis(child, name);
      }
    }
  }

  public boolean hasProperty(String name) {
    return super.hasProperty(name) || "prototype".equals(name);
  }

  boolean defineProperty(String name, JSType type,
      boolean inferred, boolean inExterns) {
    if ("prototype".equals(name)) {
      ObjectType objType = type.toObjectType();
      if (objType != null) {
        return setPrototype(
            new FunctionPrototypeType(
                registry, this, objType, isNativeObjectType()));
      } else {
        return false;
      }
    }
    return super.defineProperty(name, type, inferred, inExterns);
  }

// relevant test
// com.google.javascript.jscomp.CoalesceVariableNamesTest::testMergeThreeVarNames
  public void testMergeThreeVarNames() {
    inFunction("var x,y,z; x=1; x; y=1; y; z=1; z",
               "var x    ; x=1; x; x=1; x; x=1; x");
  }

// com.google.javascript.jscomp.CoalesceVariableNamesTest::testDifferentBlock
  public void testDifferentBlock() {
    inFunction("if(1) { var x = 0; x } else { var y = 0; y }",
               "if(1) { var x = 0; x } else {     x = 0; x }");
  }

// com.google.javascript.jscomp.CoalesceVariableNamesTest::testLoops
  public void testLoops() {
    inFunction("var x; while(1) { x; x = 1; var y = 1; y }");
    inFunction("var y = 1; y; while(1) { var x = 1; x }",
               "var y = 1; y; while(1) {     y = 1; y }");
  }

// com.google.javascript.jscomp.CoalesceVariableNamesTest::testEscaped
  public void testEscaped() {
    inFunction("var x = 1; x; function f() { x };  var y = 0; y; f()");
  }

// com.google.javascript.jscomp.CoalesceVariableNamesTest::testFor
  public void testFor() {
    inFunction("var x = 1; x; for (;;) var y; y = 1; y",
               "var x = 1; x; for (;;)      ; x = 1; x");
  }

// com.google.javascript.jscomp.CoalesceVariableNamesTest::testForIn
  public void testForIn() {
    
    inFunction("var x = 1, k; x;      ; for (var y in k) { y }",
               "var x = 1, k; x;      ; for (var y in k) { y }");

    inFunction("var x = 1, k; x; y = 1; for (var y in k) { y }",
               "var x = 1, k; x; x = 1; for (    x in k) { x }");
  }

// com.google.javascript.jscomp.CoalesceVariableNamesTest::testLoopInductionVar
  public void testLoopInductionVar() {
    inFunction(
        "for(var x = 0; x < 10; x++){}" +
        "for(var y = 0; y < 10; y++){}" +
        "for(var z = 0; z < 10; z++){}",

        "for(var x = 0; x < 10; x++){}" +
        "for(x = 0; x < 10; x++){}" +
        "for(x = 0; x < 10; x++){}");

    inFunction(
        "for(var x = 0; x < 10; x++){z}" +
        "for(var y = 0, z = 0; y < 10; y++){z}",

        "for(var x = 0; x < 10; x++){z}" +
        "for(var x = 0, z = 0; x < 10; x++){z}");

    inFunction("var x = 1; x; for (var y; y=1; ) {y}",
               "var x = 1; x; for (     ; x=1; ) {x}");

    inFunction("var x = 1; x; y = 1; while(y) var y; y",
               "var x = 1; x; x = 1; while(x); x");

    inFunction("var x = 1; x; f:var y; y=1",
               "var x = 1; x; x=1");
  }

// com.google.javascript.jscomp.CoalesceVariableNamesTest::testSwitchCase
  public void testSwitchCase() {
    inFunction("var x = 1; switch(x) { case 1: var y; case 2: } y = 1; y",
               "var x = 1; switch(x) { case 1:        case 2: } x = 1; x");
  }

// com.google.javascript.jscomp.CoalesceVariableNamesTest::testDuplicatedVar
  public void testDuplicatedVar() {
    
    inFunction("z = 1; var x = 0; x; z; var y = 2, z = 1; y; z;",
               "z = 1; var x = 0; x; z; var x = 2, z = 1; x; z;");
  }

// com.google.javascript.jscomp.CoalesceVariableNamesTest::testTryCatch
  public void testTryCatch() {
    inFunction("try {} catch (e) { } var x = 4; x;",
               "try {} catch (e) { } var x = 4; x;");
    inFunction("var x = 4; x; try {} catch (e) { }",
               "var x = 4; x; try {} catch (e) { }");
  }

// com.google.javascript.jscomp.CoalesceVariableNamesTest::testDeadAssignment
  public void testDeadAssignment() {
    inFunction("var x = 6; var y; y = 4; x");
  }

// com.google.javascript.jscomp.CoalesceVariableNamesTest::testParameter
  public void testParameter() {
    test("function FUNC(param) {var x = 0; x}",
         "function FUNC(param) {param = 0; param}");
  }

// com.google.javascript.jscomp.CoalesceVariableNamesTest::testParameter2
  public void testParameter2() {
    
    test("function FUNC(x,y) {x = 0; x; y = 0; y}");
    test("function FUNC(x,y,z) {x = 0; x; y = 0; z = 0; z}");
  }

// com.google.javascript.jscomp.CoalesceVariableNamesTest::testParameter3
  public void testParameter3() {
    
    test("function FUNC(x) {var y; y = 0; x; y}");
  }

// com.google.javascript.jscomp.CoalesceVariableNamesTest::testParameter4
  public void testParameter4() {
    
    test("function FUNC(x, y) {var a,b; y; a=0; a; x; b=0; b}",
         "function FUNC(x, y) {         y; y=0; y; x; x=0; x}");
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
        "var a,b,c;a||b||c;a*b*c;a|b|c");
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

    
    assertPrint("'<script>'", "\"<script>\"");
    assertPrint("'</script>'", "\"<\\/script>\"");
    assertPrint("\"</script> </SCRIPT>\"", "\"<\\/script> <\\/SCRIPT>\"");

    assertPrint("'-->'", "\"--\\>\"");
    assertPrint("']]>'", "\"]]\\>\"");
    assertPrint("' --></script>'", "\" --\\><\\/script>\"");

    assertPrint("/--> <\\/script>/g", "/--\\> <\\/script>/g");

    
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

    
    assertPrint("({ 'a': 4, '\\u0100': 4 })", "({a:4,\"\\u0100\":4})");

    
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
    assertPrint("var a={};for (var i = a || a || (\"size\" in a);;);",
        "var a={};for(var i=a||a||(\"size\"in a);;);");

    
    assertPrint("var a={}; for (var i = -(\"length\" in a); i;) {}",
        "var a={};for(var i=-(\"length\"in a);i;);");
    assertPrint("var a={};function b_(p){ return p;};" +
        "for(var i=1,j=b_(\"length\" in a);;) {}",
        "var a={};function b_(p){return p}" +
            "for(var i=1,j=b_(\"length\"in a);;);");

    
    assertPrint("var a={}; for (;(\"length\" in a);) {}",
        "var a={};for(;\"length\"in a;);");
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

// com.google.javascript.jscomp.CodePrinterTest::testPrettyPrinter
  public void testPrettyPrinter() {
    
    
    assertPrettyPrint("(function(){})();","(function() {\n})()");
    assertPrettyPrint("var a = (function() {});alert(a);",
        "var a = function() {\n};\nalert(a)");
  }

// com.google.javascript.jscomp.CodePrinterTest::testTypeAnnotations
  public void testTypeAnnotations() {
    assertTypeAnnotations(" function Foo(){}",
        "\nfunction Foo() {\n}\n");
  }

// com.google.javascript.jscomp.CodePrinterTest::testTypeAnnotationsAssign
  public void testTypeAnnotationsAssign() {
    assertTypeAnnotations(" var Foo = function(){}",
        "\nvar Foo = function() {\n}");
  }

// com.google.javascript.jscomp.CodePrinterTest::testTypeAnnotationsNamespace
  public void testTypeAnnotationsNamespace() {
    assertTypeAnnotations("var a = {};"
        + " a.Foo = function(){}",
        "var a = {};\n\na.Foo = function() {\n}");
  }

// com.google.javascript.jscomp.CodePrinterTest::testTypeAnnotationsMemberSubclass
  public void testTypeAnnotationsMemberSubclass() {
    assertTypeAnnotations("var a = {};"
        + " a.Foo = function(){};"
        + " a.Bar = function(){}",
        "var a = {};\n\na.Foo = function() {\n};\n"
        + "\n"
        + "a.Bar = function() {\n}");
  }

// com.google.javascript.jscomp.CodePrinterTest::testTypeAnnotationsInterface
  public void testTypeAnnotationsInterface() {
    assertTypeAnnotations("var a = {};"
        + " a.Foo = function(){};"
        + " a.Bar = function(){}",
        "var a = {};\n\na.Foo = function() {\n};\n"
        + "\n"
        + "a.Bar = function() {\n}");
  }

// com.google.javascript.jscomp.CodePrinterTest::testTypeAnnotationsMember
  public void testTypeAnnotationsMember() {
    assertTypeAnnotations("var a = {};"
        + " a.Foo = function(){}"
        + "\n"
        + "a.Foo.prototype.foo = function(foo) {};"
        + ""
        + "a.Foo.prototype.bar = '';",
        "var a = {};\n"
        + "\na.Foo = function() {\n};\n"
        + "\n"
        + "a.Foo.prototype.foo = function(foo) {\n};\n"
        + "\n"
        + "a.Foo.prototype.bar = \"\"");
  }

// com.google.javascript.jscomp.CodePrinterTest::testTypeAnnotationsImplements
  public void testTypeAnnotationsImplements() {
    assertTypeAnnotations("var a = {};"
        + " a.Foo = function(){};\n"
        + " a.I = function(){};\n"
        + " a.I2 = function(){};\n"
        + " a.Bar = function(){}",
        "var a = {};\n"
        + "\na.Foo = function() {\n};\n"
        + "\na.I = function() {\n};\n"
        + "\na.I2 = function() {\n};\n"
        + "\n"
        + "a.Bar = function() {\n}");
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
        "}");
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
        "}");
  }

// com.google.javascript.jscomp.CodePrinterTest::testSubtraction
  public void testSubtraction() {
    Compiler compiler = new Compiler();
    Node n = compiler.parseTestCode("x - -4");
    assertEquals(0, compiler.getErrorCount());
    NodeTraversal.traverse(compiler, n, new FoldConstants(compiler));

    assertEquals(
        "x- -4",
        new CodePrinter.Builder(n).setLineLengthThreshold(
            CodePrinter.DEFAULT_LINE_LENGTH_THRESHOLD).build());
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
    List<String> parsePrintParseTestCases = ImmutableList.of(
        "3;",
        "var a = b;",
        "var x, y, z;",
        "try { foo() } catch(e) { bar() }",
        "try { foo() } catch(e) { bar() } finally { stuff() }",
        "try { foo() } finally { stuff() }",
        "throw 'me'",
        "function foo(a) { return a + 4; }",
        "function foo() { return; }",
        "var a = function(a, b) { foo(); return a + b; }",
        "b = [3, 4, 'paul', \"Buchhe it\",,5];",
        "v = (5, 6, 7, 8)",
        "d = 34.0; x = 0; y = .3; z = -22",
        "d = -x; t = !x + ~y;",
        "'hi';  stuff(a,b) \n foo(); 
        "a = b++ + ++c; a = b++-++c; a = - --b; a = - ++b;",
        "a++; b= a++; b = ++a; b = a--; b = --a; a+=2; b-=5",
        "a = (2 + 3) * 4;",
        "a = 1 + (2 + 3) + 4;",
        "x = a ? b : c; x = a ? (b,3,5) : (foo(),bar());",
        "a = b | c || d ^ e && f & !g != h << i <= j < k >>> l > m * n % !o",
        "a == b; a != b; a === b; a == b == a; (a == b) == a; a == (b == a);",
        "if (a > b) a = b; if (b < 3) a = 3; else c = 4;",
        "if (a == b) { a++; } if (a == 0) { a++; } else { a --; }",
        "for (var i in a) b += i;",
        "for (var i = 0; i < 10; i++){ b /= 2; if (b == 2)break;else continue;}",
        "for (x = 0; x < 10; x++) a /= 2;",
        "for (;;) a++;",
        "while(true) { blah(); }while(true) blah();",
        "do stuff(); while(a>b);",
        "[0, null, , true, false, this];",
        "s.replace(/absc/, 'X').replace(/ab/gi, 'Y');",
        "new Foo; new Bar(a, b,c);",
        "with(foo()) { x = z; y = t; } with(bar()) a = z;",
        "delete foo['bar']; delete foo;",
        "var x = { 'a':'paul', 1:'3', 2:(3,4) };",
        "switch(a) { case 2: case 3: { stuff(); break; }" +
        "case 4: morestuff(); break; default: done();}",
        "x = foo['bar'] + foo['my stuff'] + foo[bar] + f.stuff;",
        "a.v = b.v; x['foo'] = y['zoo'];",
        "'test' in x; 3 in x; a in x;",
        "'foo\"bar' + \"foo'c\" + 'stuff\\n and \\\\more'",
        "x.__proto__;");

    for (String testCase : parsePrintParseTestCases) {
      Node parse1 = parse(testCase);
      Node parse2 = parse(new CodePrinter.Builder(parse1).build());
      assertTrue(testCase, parse1.checkTreeEqualsSilent(parse2));
    }
  }

// com.google.javascript.jscomp.CodePrinterTest::testDoLoopIECompatiblity
  public void testDoLoopIECompatiblity() {
    
    assertPrint("function(){if(e1){do foo();while(e2)}else foo()}",
        "function(){if(e1){do foo();while(e2)}else foo()}");

    assertPrint("function(){if(e1)do foo();while(e2)else foo()}",
        "function(){if(e1){do foo();while(e2)}else foo()}");

    assertPrint("if(x){do{foo()}while(y)}else bar()",
        "if(x){do foo();while(y)}else bar()");

    assertPrint("if(x)do{foo()}while(y);else bar()",
        "if(x){do foo();while(y)}else bar()");
  }

// com.google.javascript.jscomp.CodePrinterTest::testFunctionSafariCompatiblity
  public void testFunctionSafariCompatiblity() {
    
    assertPrint("function(){if(e1){function goo(){return true}}else foo()}",
        "function(){if(e1){function goo(){return true}}else foo()}");

    assertPrint("function(){if(e1)function goo(){return true}else foo()}",
        "function(){if(e1){function goo(){return true}}else foo()}");

    assertPrint("if(e1){function goo(){return true}}",
        "if(e1){function goo(){return true}}");

    assertPrint("if(e1)function goo(){return true}",
        "if(e1){function goo(){return true}}");
  }

// com.google.javascript.jscomp.CodePrinterTest::testExponents
  public void testExponents() {
    assertPrint("1", "1");
    assertPrint("10", "10");
    assertPrint("100", "100");
    assertPrint("1000", "1E3");
    assertPrint("10000", "1E4");
    assertPrint("100000", "1E5");
    assertPrint("-1", "-1");
    assertPrint("-10", "-10");
    assertPrint("-100", "-100");
    assertPrint("-1000", "-1E3");
    assertPrint("-123412340000", "-12341234E4");
    assertPrint("1000000000000000000", "1E18");
    assertPrint("100000.0", "1E5");
    assertPrint("100000.1", "100000.1");

    assertPrint("0.000001", "1.0E-6");
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

// com.google.javascript.jscomp.CollapsePropertiesTest::testObjLitDeclarationWithDuplicateKeys
  public void testObjLitDeclarationWithDuplicateKeys() {
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
    
    
    
    
    
    
    test("var a = function(){}; a.b = 1; var c = a; c.b = 2; a.b != c.b;",
         "var a = function(){}; var a$b = 1; var c = a; c.b = 2; a$b != c.b;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testAliasCreatedForFunctionDepth1_2
  public void testAliasCreatedForFunctionDepth1_2() {
    test("var a = function(){}; a.b = 1; f(a); a.b;",
         "var a = function(){}; var a$b = 1; f(a); a$b;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testAliasCreatedForFunctionDepth1_3
  public void testAliasCreatedForFunctionDepth1_3() {
    test("var a = function(){}; a.b = 1; new f(a); a.b;",
         "var a = function(){}; var a$b = 1; new f(a); a$b;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testAliasCreatedForFunctionDepth2
  public void testAliasCreatedForFunctionDepth2() {
    test("var a = {}; a.b = function() {}; a.b.c = 1; var d = a.b;" +
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
    test("function a() {} var c = a; (function() {a.b = 0;})(); a.b;",
         "function a() {} var a$b; var c = a; (function() {a$b = 0;})(); a$b;");
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
    test("function a() {} a.b = {x: 0}; var c = a;" +
         "(function() {a.b.y = 0;})(); a.b.y;",
         "function a() {} var a$b$x = 0; var a$b$y; var c = a;" +
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
    test("function CreateClass(a) {}" +
         "var a = {};" +
         "a.b = function(){};" +
         "a.b.prototype.c = function(){};" +
         "a.d = CreateClass({c: a.b.prototype.c});",
         "function CreateClass(a) {}" +
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
         "function CreateClass(a) {}" +
         "var a$b = CreateClass({c: function() {}});" +
         "var a$d = CreateClass({c: a$b.c});");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testReferenceInAnonymousObject5
  public void testReferenceInAnonymousObject5() {
    test("function CreateClass(a) {}" +
         "var a = {};" +
         "a.b = CreateClass({c: function() {}});" +
         "a.d = CreateClass({c: a.b.prototype.c});",
         "function CreateClass(a) {}" +
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
    testSame("if (x) var a = 1; else var a = 2;");
  }

// com.google.javascript.jscomp.CollapseVariableDeclarationsTest::testExprExploitation
  public void testExprExploitation() {
    test("a = null; b = null; var c = b",
         "var c = b = a = null");
    test("a = null; b = null",
         "b = a = null");
    test("a = undefined; b = undefined",
         "b = a = undefined");
    test("a = 0; b = 0", "b=a=0");
    test("a = 'foo'; b = 'foo'",
         "b = a = \"foo\"");
    test("a = c; b = c", "b=a=c");

    testSame("a = 0; b = 1");
    testSame("a = \"foo\"; b = \"foox\"");

    test("a = null; a && b;", "(a = null)&&b");
    test("a = null; a || b;", "(a = null)||b");

    test("a = null; a ? b : c;", "(a = null) ? b : c");

    test("a = null; this.foo = null;",
         "this.foo = a = null");
    test("function(){ a = null; return null; }",
         "function(){return a = null}");
    test("a = true; if (a) { foo(); }",
         "if (a = true) { foo() }");
    test("a = true; if (a && a) { foo(); }",
         "if ((a = true) && a) { foo() }");
    test("a = false; if (a) { foo(); }",
         "if (a = false) { foo() }");
    testSame("a = this.foo; a();");
    test("a = b; b = a;",
         "b = a = b");
    testSame("a = b; a.c = a");
    test("this.foo = null; this.bar = null;",
         "this.bar = this.foo = null");
    test("this.foo = null; this.bar = null; this.baz = this.bar",
         "this.baz = this.bar = this.foo = null");
    test("this.foo = null; a = null;",
         "a = this.foo = null");
    test("this.foo = null; a = this.foo;",
         "a = this.foo = null");
    test("a.b.c=null; a=null;",
         "a = a.b.c = null");
    testSame("a = null; a.b.c = null");
    test("(a=b).c = null; this.b = null;",
         "this.b = (a=b).c = null");
    testSame("if(x) a = null; else b = a");
  }

// com.google.javascript.jscomp.CollapseVariableDeclarationsTest::testNestedExprExploitation
  public void testNestedExprExploitation() {
    test("this.foo = null; this.bar = null; this.baz = null;",
         "this.baz = this.bar = this.foo = null");

    test("a = 3; this.foo = a; this.bar = a; this.baz = 3;",
         "this.baz = this.bar = this.foo = a = 3");
    test("a = 3; this.foo = a; this.bar = this.foo; this.baz = a;",
         "this.baz = this.bar = this.foo = a = 3");
    test("a = 3; this.foo = a; this.bar = 3; this.baz = this.foo;",
         "this.baz = this.bar = this.foo = a = 3");
    test("a = 3; this.foo = a; a = 3; this.bar = 3; " +
         "a = 3; this.baz = this.foo;",
         "this.baz = a = this.bar = a = this.foo = a = 3");

    test("a = 4; this.foo = a; a = 3; this.bar = 3; " +
         "a = 3; this.baz = this.foo;",
         "this.foo = a = 4; a = this.bar = a = 3; this.baz = this.foo");
    test("a = 3; this.foo = a; a = 4; this.bar = 3; " +
         "a = 3; this.baz = this.foo;",
         "this.foo = a = 3; a = 4; a = this.bar = 3; this.baz = this.foo");
    test("a = 3; this.foo = a; a = 3; this.bar = 3; " +
         "a = 4; this.baz = this.foo;",
         "this.bar = a = this.foo = a = 3; a = 4; this.baz = this.foo");
  }

// com.google.javascript.jscomp.CollapseVariableDeclarationsTest::testBug1840071
  public void testBug1840071() {
    
    
    test("a.b = a.x; if (a.x) {}", "if (a.b = a.x) {}");
    testSame("a.b = a.x; if (a.b) {}");
    test("a.b = a.c = a.x; if (a.x) {}", "if (a.b = a.c = a.x) {}");
    testSame("a.b = a.c = a.x; if (a.c) {}");
    testSame("a.b = a.c = a.x; if (a.b) {}");
  }

// com.google.javascript.jscomp.CollapseVariableDeclarationsTest::testBug2072343
  public void testBug2072343() {
    testSame("a = a.x;a = a.x");
    testSame("a = a.x;b = a.x");
    test("b = a.x;a = a.x", "a = b = a.x");
    testSame("a.x = a;a = a.x");
    testSame("a.b = a.b.x;a.b = a.b.x");
    testSame("a.y = a.y.x;b = a.y;c = a.y.x");
    test("a = a.x;b = a;c = a.x", "b = a = a.x;c = a.x");
    test("b = a.x;a = b;c = a.x", "a = b = a.x;c = a.x");
 }

// com.google.javascript.jscomp.CollapseVariableDeclarationsTest::testBadCollapseIntoCall
  public void testBadCollapseIntoCall() {
    
    
    testSame("this.foo = function() {}; this.foo();");
  }

// com.google.javascript.jscomp.CombinedCompilerPassTest::testIndividualPasses
  public void testIndividualPasses() {
    for (TestHelper test : createStringTests()) {
      CombinedCompilerPass pass =
          new CombinedCompilerPass(compiler, test.getTraversal());
      pass.process(null, createPostOrderAlphabet());
      test.checkResults();
    }
  }

// com.google.javascript.jscomp.CombinedCompilerPassTest::testCombinedPasses
  public void testCombinedPasses() {
    List<TestHelper> tests  = createStringTests();
    Callback[] callbacks = new Callback[tests.size()];
    int i = 0;
    for (TestHelper test : tests) {
      callbacks[i++] = test.getTraversal();
    }
    CombinedCompilerPass pass =
        new CombinedCompilerPass(compiler, callbacks);
    pass.process(null, createPostOrderAlphabet());
    for (TestHelper test : tests) {
      test.checkResults();
    }
  }

// com.google.javascript.jscomp.CombinedCompilerPassTest::testScopes
  public void testScopes() {
    Node root =
        compiler.parseTestCode("var y = function() { var x = function() { };}");
    
    ScopeRecordingCallback c1 = new ScopeRecordingCallback();
    c1.ignore("y");
    ScopeRecordingCallback c2 = new ScopeRecordingCallback();
    c2.ignore("x");
    ScopeRecordingCallback c3 = new ScopeRecordingCallback();
    
    CombinedCompilerPass pass = new CombinedCompilerPass(compiler, c1, c2, c3);
    pass.process(null, root);
    
    assertEquals(1, c1.getVisitedScopes().size());
    assertEquals(2, c2.getVisitedScopes().size());
    assertEquals(3, c3.getVisitedScopes().size());
  }

// com.google.javascript.jscomp.CompilerRunnerTest::testTypeCheckingOffByDefault
  public void testTypeCheckingOffByDefault() {}

// com.google.javascript.jscomp.CompilerRunnerTest::testTypeCheckingOnWithVerbose
  public void testTypeCheckingOnWithVerbose() {
    CompilerRunner.FLAG_warning_level.setForTest(WarningLevel.VERBOSE);
    test("function f(x) { return x; } f();", TypeCheck.WRONG_ARGUMENT_COUNT);
  }

// com.google.javascript.jscomp.CompilerRunnerTest::testTypeCheckOverride1
  public void testTypeCheckOverride1() {
    CompilerRunner.FLAG_warning_level.setForTest(WarningLevel.VERBOSE);
    CompilerRunner.FLAG_jscomp_off.setForTest(
        Lists.newArrayList("checkTypes"));
    testSame("var x = x || {}; x.f = function() {}; x.f(3);");
  }

// com.google.javascript.jscomp.CompilerRunnerTest::testTypeCheckOverride2
  public void testTypeCheckOverride2() {
    CompilerRunner.FLAG_warning_level.setForTest(WarningLevel.DEFAULT);
    testSame("var x = x || {}; x.f = function() {}; x.f(3);");

    CompilerRunner.FLAG_jscomp_warning.setForTest(
        Lists.newArrayList("checkTypes"));
    test("var x = x || {}; x.f = function() {}; x.f(3);",
         TypeCheck.WRONG_ARGUMENT_COUNT);
  }

// com.google.javascript.jscomp.CompilerRunnerTest::testCheckSymbolsOffForDefault
  public void testCheckSymbolsOffForDefault() {
    CompilerRunner.FLAG_warning_level.setForTest(WarningLevel.DEFAULT);
    test("x = 3; var y; var y;", "x=3; var y;");
  }

// com.google.javascript.jscomp.CompilerRunnerTest::testCheckSymbolsOnForVerbose
  public void testCheckSymbolsOnForVerbose() {
    CompilerRunner.FLAG_warning_level.setForTest(WarningLevel.VERBOSE);
    test("x = 3;", VarCheck.UNDEFINED_VAR_ERROR);
    test("var y; var y;", SyntacticScopeCreator.VAR_MULTIPLY_DECLARED_ERROR);
  }

// com.google.javascript.jscomp.CompilerRunnerTest::testCheckSymbolsOverrideForVerbose
  public void testCheckSymbolsOverrideForVerbose() {
    CompilerRunner.FLAG_warning_level.setForTest(WarningLevel.VERBOSE);
    AbstractCompilerRunner.FLAG_jscomp_off.setForTest(
        Lists.newArrayList("undefinedVars"));
    testSame("x = 3;");
  }

// com.google.javascript.jscomp.CompilerRunnerTest::testCheckUndefinedProperties
  public void testCheckUndefinedProperties() {
    CompilerRunner.FLAG_warning_level.setForTest(WarningLevel.VERBOSE);
    AbstractCompilerRunner.FLAG_jscomp_error.setForTest(
        Lists.newArrayList("missingProperties"));
    test("var x = {}; var y = x.bar;", TypeCheck.INEXISTENT_PROPERTY);
  }

// com.google.javascript.jscomp.CompilerRunnerTest::testDuplicateParams
  public void testDuplicateParams() {
    test("function (a, a) {}", RhinoErrorReporter.DUPLICATE_PARAM);
    assertTrue(lastCompiler.hasHaltingErrors());
  }

// com.google.javascript.jscomp.CompilerRunnerTest::testDefineFlag
  public void testDefineFlag() {
    AbstractCompilerRunner.FLAG_define.setForTest(
        Lists.newArrayList("FOO", "BAR=5"));
    test(" var FOO = false;" +
         " var BAR = 3;",
         "var FOO = true, BAR = 5;");
  }

// com.google.javascript.jscomp.CompilerRunnerTest::testScriptStrictModeNoWarning
  public void testScriptStrictModeNoWarning() {
    test("'use strict';", "");
    test("'no use strict';", CheckSideEffects.USELESS_CODE_ERROR);
  }

// com.google.javascript.jscomp.CompilerRunnerTest::testFunctionStrictModeNoWarning
  public void testFunctionStrictModeNoWarning() {
    test("function f() {'use strict';}", "function f() {}");
    test("function f() {'no use strict';}",
         CheckSideEffects.USELESS_CODE_ERROR);
  }

// com.google.javascript.jscomp.CompilerRunnerTest::testQuietMode
  public void testQuietMode() {}

// com.google.javascript.jscomp.CompilerRunnerTest::testIssue70
  public void testIssue70() {
    test("function foo({}) {}", RhinoErrorReporter.PARSE_ERROR);
  }

// com.google.javascript.jscomp.CompilerRunnerTest::testIssue81
  public void testIssue81() {}

// com.google.javascript.jscomp.CompilerRunnerTest::testDebugFlag1
  public void testDebugFlag1() {
    CompilerRunner.FLAG_compilation_level.setForTest(
        CompilationLevel.SIMPLE_OPTIMIZATIONS);
    CompilerRunner.FLAG_debug.setForTest(false);
    testSame("function foo(a) {}");
  }

// com.google.javascript.jscomp.CompilerRunnerTest::testDebugFlag2
  public void testDebugFlag2() {
    CompilerRunner.FLAG_compilation_level.setForTest(
        CompilationLevel.SIMPLE_OPTIMIZATIONS);
    CompilerRunner.FLAG_debug.setForTest(true);
    test("function foo(a) {}",
         "function foo($a$$) {}");
  }

// com.google.javascript.jscomp.CompilerRunnerTest::testDebugFlag3
  public void testDebugFlag3() {
    CompilerRunner.FLAG_compilation_level.setForTest(
        CompilationLevel.ADVANCED_OPTIMIZATIONS);
    CompilerRunner.FLAG_warning_level.setForTest(
        WarningLevel.QUIET);
    CompilerRunner.FLAG_debug.setForTest(false);
    test("function Foo() {};" +
         "Foo.x = 1;" +
         "function f() {throw new Foo().x;} f();",
         "function a() {};" +
         "throw new a().a;");
  }

// com.google.javascript.jscomp.CompilerRunnerTest::testDebugFlag4
  public void testDebugFlag4() {
    CompilerRunner.FLAG_compilation_level.setForTest(
        CompilationLevel.ADVANCED_OPTIMIZATIONS);
    CompilerRunner.FLAG_warning_level.setForTest(
        WarningLevel.QUIET);
    CompilerRunner.FLAG_debug.setForTest(true);
    test("function Foo() {};" +
        "Foo.x = 1;" +
        "function f() {throw new Foo().x;} f();",
        "function $Foo$$() {};" +
        "throw new $Foo$$().$x$;");
  }

// com.google.javascript.jscomp.ConcreteTypeTest::testEquals
  public void testEquals() {
    ConcreteFunctionType fun1 = createFunction("fun1");
    ConcreteFunctionType fun2 = createFunction("fun2");
    ConcreteType obj1 = fun1.getInstanceType();
    ConcreteType obj2 = fun2.getInstanceType();
    ConcreteType union1 = new ConcreteUnionType(fun1, fun2);
    ConcreteType union2 = new ConcreteUnionType(fun1, obj1);
    ConcreteType union3 = new ConcreteUnionType(fun1, obj1);

    checkEquality(Lists.newArrayList(fun1, fun2, obj1, obj2,
                                     union1, union2));

    assertEquals(union2, union3);
  }

// com.google.javascript.jscomp.ConcreteTypeTest::testUnionWith
  public void testUnionWith() {
    ConcreteFunctionType fun = createFunction("fun");
    ConcreteType obj = fun.getInstanceType();
    ConcreteType both = new ConcreteUnionType(fun, obj);

    assertTrue(fun.isSingleton());
    assertTrue(obj.isSingleton());
    assertFalse(both.isSingleton());
    assertFalse(NONE.isSingleton());
    assertFalse(ALL.isSingleton());

    checkUnionWith(fun, NONE, fun);
    checkUnionWith(fun, ALL, ALL);

    checkUnionWith(fun, obj, both);
    checkUnionWith(both, NONE, both);
    checkUnionWith(both, ALL, ALL);
  }

// com.google.javascript.jscomp.ConcreteTypeTest::testIntersectionWith
  public void testIntersectionWith() {
    ConcreteFunctionType fun = createFunction("fun");
    ConcreteFunctionType fun2 = createFunction("fun2");
    ConcreteType obj = fun.getInstanceType();
    ConcreteType both = new ConcreteUnionType(fun, obj);

    assertEquals(NONE, fun.intersectWith(obj));
    assertEquals(NONE, obj.intersectWith(fun));

    assertEquals(fun, both.intersectWith(fun));
    assertEquals(fun, fun.intersectWith(both));

    assertEquals(NONE, NONE.intersectWith(both));
    assertEquals(NONE, both.intersectWith(NONE));
    assertEquals(NONE, fun.intersectWith(NONE));
    assertEquals(NONE, NONE.intersectWith(fun));

    assertEquals(NONE, both.intersectWith(fun2));

    assertEquals(both, ALL.intersectWith(both));
    assertEquals(both, both.intersectWith(ALL));
    assertEquals(fun, ALL.intersectWith(fun));
    assertEquals(fun, fun.intersectWith(ALL));
    assertEquals(NONE, ALL.intersectWith(NONE));
    assertEquals(NONE, NONE.intersectWith(ALL));
  }

// com.google.javascript.jscomp.ConcreteTypeTest::testFunction
  public void testFunction() {
    ConcreteFunctionType fun = createFunction("fun", "a", "b");
    assertTrue(fun.isFunction());
    assertNotNull(fun.getCallSlot());
    assertNotNull(fun.getReturnSlot());
    assertNotNull(fun.getParameterSlot(0));
    assertNotNull(fun.getParameterSlot(1));
    assertNull(fun.getParameterSlot(2));
    assertTrue(fun.getInstanceType().isInstance());
  }

// com.google.javascript.jscomp.ConcreteTypeTest::testInstance
  public void testInstance() {
    ConcreteInstanceType obj = createInstance("MyObj", "a", "b");
    assertTrue(obj.isInstance());
    assertNotNull(obj.getPropertySlot("a"));
    assertNotNull(obj.getPropertySlot("b"));
    assertNull(obj.getPropertySlot("c"));

    
    
    for (int i = 0; i < 4; ++i) {
      assertNotNull(obj = obj.getImplicitPrototype());
      assertTrue(obj.isInstance());
    }
    assertNull(obj.getImplicitPrototype());
  }

// com.google.javascript.jscomp.ConcreteTypeTest::testGetX
  public void testGetX() {
    ConcreteFunctionType fun1 = createFunction("fun1");
    ConcreteFunctionType fun2 = createFunction("fun2");
    ConcreteInstanceType obj1 = fun1.getInstanceType();
    ConcreteInstanceType obj2 = fun2.getInstanceType();
    ConcreteType union1 = fun1.unionWith(obj1);
    ConcreteType union2 =
        union1.unionWith(fun2).unionWith(obj2);

    assertEqualSets(Lists.newArrayList(), NONE.getFunctions());
    assertEqualSets(Lists.newArrayList(), NONE.getInstances());
    assertEqualSets(Lists.newArrayList(fun1), fun1.getFunctions());
    assertEqualSets(Lists.newArrayList(), fun1.getInstances());
    assertEqualSets(Lists.newArrayList(), obj1.getFunctions());
    assertEqualSets(Lists.newArrayList(obj1), obj1.getInstances());

    assertEqualSets(Lists.newArrayList(fun1), union1.getFunctions());
    assertEqualSets(Lists.newArrayList(obj1), union1.getInstances());

    assertEqualSets(Lists.newArrayList(fun1, fun2), union2.getFunctions());
    assertEqualSets(Lists.newArrayList(obj1, obj2), union2.getInstances());
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

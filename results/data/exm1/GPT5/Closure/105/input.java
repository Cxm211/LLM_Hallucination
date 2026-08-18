// buggy code
  void tryFoldStringJoin(NodeTraversal t, Node n, Node left, Node right,
                         Node parent) {
    if (!NodeUtil.isGetProp(left) || !NodeUtil.isImmutableValue(right)) {
      return;
    }

    Node arrayNode = left.getFirstChild();
    Node functionName = arrayNode.getNext();

    if ((arrayNode.getType() != Token.ARRAYLIT) ||
        !functionName.getString().equals("join")) {
      return;
    }

    String joinString = NodeUtil.getStringValue(right);
    List<Node> arrayFoldedChildren = Lists.newLinkedList();
    StringBuilder sb = new StringBuilder();
    int foldedSize = 0;
    Node elem = arrayNode.getFirstChild();
    // Merges adjacent String nodes.
    while (elem != null) {
      if (NodeUtil.isImmutableValue(elem)) {
        if (sb.length() > 0) {
          sb.append(joinString);
        }
        sb.append(NodeUtil.getStringValue(elem));
      } else {
        if (sb.length() > 0) {
          // + 2 for the quotes.
          foldedSize += sb.length() + 2;
          arrayFoldedChildren.add(Node.newString(sb.toString()));
          sb = new StringBuilder();
        }
        foldedSize += InlineCostEstimator.getCost(elem);
        arrayFoldedChildren.add(elem);
      }
      elem = elem.getNext();
    }

    if (sb.length() > 0) {
      // + 2 for the quotes.
      foldedSize += sb.length() + 2;
      arrayFoldedChildren.add(Node.newString(sb.toString()));
    }
    // one for each comma.
    foldedSize += arrayFoldedChildren.size() - 1;

    int originalSize = InlineCostEstimator.getCost(n);
    switch (arrayFoldedChildren.size()) {
      case 0:
        Node emptyStringNode = Node.newString("");
        parent.replaceChild(n, emptyStringNode);
        break;

      case 1:
        Node foldedStringNode = arrayFoldedChildren.remove(0);
        if (foldedSize > originalSize) {
          return;
        }
        arrayNode.detachChildren();
        if (foldedStringNode.getType() != Token.STRING) {
          // If the Node is not a string literal, ensure that
          // it is coerced to a string.
          Node replacement = new Node(Token.ADD,
              Node.newString(""), foldedStringNode);
          foldedStringNode = replacement;
        }
        parent.replaceChild(n, foldedStringNode);
        break;

      default:
        // No folding could actually be performed.
        if (arrayFoldedChildren.size() == arrayNode.getChildCount()) {
          return;
        }
        int kJoinOverhead = "[].join()".length();
        foldedSize += kJoinOverhead;
        foldedSize += InlineCostEstimator.getCost(right);
        if (foldedSize > originalSize) {
          return;
        }
        arrayNode.detachChildren();
        for (Node node : arrayFoldedChildren) {
          arrayNode.addChildToBack(node);
        }
        break;
    }
    t.getCompiler().reportCodeChange();
  }

// relevant test
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

// com.google.javascript.jscomp.CreateSyntheticBlocksTest::testFold1
  public void testFold1() {
    test("function() { if (x) return; y(); }",
         "function(){if(!x)y()}");
  }

// com.google.javascript.jscomp.CreateSyntheticBlocksTest::testFoldWithMarkers1
  public void testFoldWithMarkers1() {
    testSame("function(){startMarker();if(x)return;endMarker();y()}");
  }

// com.google.javascript.jscomp.CreateSyntheticBlocksTest::testFold2
  public void testFold2() {
    test("function() { if (x) return; y(); if (a) return; b(); }",
         "function(){if(!x){y();if(!a)b()}}");
  }

// com.google.javascript.jscomp.CreateSyntheticBlocksTest::testFoldWithMarkers2
  public void testFoldWithMarkers2() {
    testSame("function(){startMarker(\"FOO\");startMarker(\"BAR\");" +
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

// com.google.javascript.jscomp.CreateSyntheticBlocksTest::testDenormalize
  public void testDenormalize() {
    testSame("startMarker();for(;;);endMarker()");
  }

// com.google.javascript.jscomp.CreateSyntheticBlocksTest::testNonMarkingUse
  public void testNonMarkingUse() {
    testSame("function foo(endMarker){}");
    testSame("function foo(){startMarker:foo()}");
  }

// com.google.javascript.jscomp.FoldConstantsTest::testFoldBlock
  public void testFoldBlock() {
    fold("{{foo()}}", "foo()");
    fold("{foo();{}}", "foo()");
    fold("{{foo()}{}}", "foo()");
    fold("{{foo()}{bar()}}", "foo();bar()");
    fold("{if(false)foo(); {bar()}}", "bar()");
    fold("{if(false)if(false)if(false)foo(); {bar()}}", "bar()");

    fold("{'hi'}", "");
    fold("{x==3}", "");
    fold("{ (function(){x++}) }", "");
    fold("function(){return;}", "function(){return;}");
    fold("function(){return 3;}", "function(){return 3}");
    fold("function(){if(x)return; x=3; return; }",
         "function(){if(x)return; x=3; return; }");
    fold("{x=3;;;y=2;;;}", "x=3;y=2");

    
    fold("while(x()){x}", "while(x());");
    fold("while(x()){x()}", "while(x())x()");
    fold("for(x=0;x<100;x++){x}", "for(x=0;x<100;x++);");
    fold("for(x in y){x}", "for(x in y);");
  }

// com.google.javascript.jscomp.FoldConstantsTest::testFoldOneChildBlocks
  public void testFoldOneChildBlocks() {
    fold("function(){if(x)a();x=3}",
        "function(){x&&a();x=3}");
    fold("function(){if(x){a()}x=3}",
        "function(){x&&a();x=3}");
    fold("function(){if(x){return 3}}",
        "function(){if(x)return 3}");
    fold("function(){if(x){a()}}",
        "function(){x&&a()}");
    fold("function(){if(x){throw 1}}", "function(){if(x)throw 1;}");

    
    fold("function(){if(x){foo()}}", "function(){x&&foo()}");
    fold("function(){if(x){foo()}else{bar()}}",
         "function(){x?foo():bar()}");

    
    fold("function(){if(x){a.b=1}}", "function(){if(x)a.b=1}");
    fold("function(){if(x){a.b*=1}}", "function(){if(x)a.b*=1}");
    fold("function(){if(x){a.b+=1}}", "function(){if(x)a.b+=1}");
    fold("function(){if(x){++a.b}}", "function(){x&&++a.b}");
    fold("function(){if(x){a.foo()}}", "function(){x&&a.foo()}");

    
    fold("function(){try{foo()}catch(e){bar(e)}finally{baz()}}",
         "function(){try{foo()}catch(e){bar(e)}finally{baz()}}");

    
    fold("function(){switch(x){case 1:break}}",
         "function(){switch(x){case 1:break}}");
    fold("function(){switch(x){default:{break}}}",
         "function(){switch(x){default:break}}");
    fold("function(){switch(x){default:{break}}}",
         "function(){switch(x){default:break}}");
    fold("function(){switch(x){default:x;case 1:return 2}}",
         "function(){switch(x){default:case 1:return 2}}");

    
    fold("function(){if(e1){do foo();while(e2)}else foo2()}",
         "function(){if(e1){do foo();while(e2)}else foo2()}");
    
    fold("if(x){do{foo()}while(y)}else bar()",
         "if(x){do foo();while(y)}else bar()");

    
    fold("function(){if(x){if(y)foo()}}",
         "function(){x&&y&&foo()}");
    fold("function(){if(x){if(y)foo();else bar()}}",
         "function(){if(x)y?foo():bar()}");
    fold("function(){if(x){if(y)foo()}else bar()}",
         "function(){if(x)y&&foo();else bar()}");
    fold("function(){if(x){if(y)foo();else bar()}else{baz()}}",
         "function(){if(x)y?foo():bar();else baz()}");

    fold("if(e1){while(e2){if(e3){foo()}}}else{bar()}",
         "if(e1)while(e2)e3&&foo();else bar()");

    fold("if(e1){with(e2){if(e3){foo()}}}else{bar()}",
         "if(e1)with(e2)e3&&foo();else bar()");

    fold("if(x){if(y){var x;}}", "if(x)if(y)var x");
    fold("if(x){ if(y){var x;}else{var z;} }",
         "if(x)if(y)var x;else var z");

    
    
    
    fold("if(x){ if(y){var x;}else{var z;} }else{var w}",
         "if(x)if(y)var x;else var z;else var w");
    fold("if (x) {var x;}else { if (y) { var y;} }",
         "if(x)var x;else if(y)var y");

    
    fold("if(a){if(b){f1();f2();}else if(c){f3();}}else {if(d){f4();}}",
         "if(a)if(b){f1();f2()}else c&&f3();else d&&f4()");

    fold("function(){foo()}", "function(){foo()}");
    fold("switch(x){case y: foo()}", "switch(x){case y:foo()}");
    fold("try{foo()}catch(ex){bar()}finally{baz()}",
         "try{foo()}catch(ex){bar()}finally{baz()}");

    
    fold("if(x){if(true){foo();foo()}else{bar();bar()}}",
         "if(x){foo();foo()}");
    fold("if(x){if(false){foo();foo()}else{bar();bar()}}",
         "if(x){bar();bar()}");

    
    fold("if(x()){}", "x()");
    fold("if(x()){} else {x()}", "x()||x()");
    fold("if(x){}", ""); 
    fold("if(a()){A()} else if (b()) {} else {C()}",
         "if(a())A();else b()||C()");
    fold("if(a()){} else if (b()) {} else {C()}",
         "a()||b()||C()");
    fold("if(a()){A()} else if (b()) {} else if (c()) {} else{D()}",
         "if(a())A();else b()||c()||D()");
    fold("if(a()){} else if (b()) {} else if (c()) {} else{D()}",
         "a()||b()||c()||D()");
    fold("if(a()){A()} else if (b()) {} else if (c()) {} else{}",
         "if(a())A();else b()||c()");

    
    fold("function foo(){if(x()){}}", "function foo(){x()}");
  }

// com.google.javascript.jscomp.FoldConstantsTest::testFoldOneChildBlocksStringCompare
  public void testFoldOneChildBlocksStringCompare() {
    
    assertResultString("if(x){if(y){var x;}}else{var z;}",
        "if(x){if(y)var x}else var z");
  }

// com.google.javascript.jscomp.FoldConstantsTest::testNecessaryDanglingElse
  public void testNecessaryDanglingElse() {
    
    
    
    assertResultString(
        "if(x)if(y){y();z()}else;else x()", "if(x){if(y){y();z()}}else x()");
  }

// com.google.javascript.jscomp.FoldConstantsTest::testFoldBlocksWithManyChildren
  public void testFoldBlocksWithManyChildren() {
    fold("function f() { if (false) {} }", "function f(){}");
    fold("function f() { { if (false) {} if (true) {} {} } }",
         "function f(){}");
    fold("{var x; var y; var z; function f() { { var a; { var b; } } } }",
         "var x;var y;var z;function f(){var a;var b}");
  }

// com.google.javascript.jscomp.FoldConstantsTest::testFoldReturns
  public void testFoldReturns() {
    fold("function(){if(x)return 1;else return 2}",
         "function(){return x?1:2}");
    fold("function(){if(x)return 1+x;else return 2-x}",
         "function(){return x?1+x:2-x}");
    fold("function(){if(x)return y += 1;else return y += 2}",
         "function(){return x?(y+=1):(y+=2)}");

    
    foldSame("function(){if(x)return;else return 2-x}");
    foldSame("function(){if(x)return x;else return}");

    
    fold("function(){if(x)return;else return}",
         "function(){return}");
  }

// com.google.javascript.jscomp.FoldConstantsTest::testFoldAssignments
  public void testFoldAssignments() {
    fold("function(){if(x)y=3;else y=4;}", "function(){y=x?3:4}");
    fold("function(){if(x)y=1+a;else y=2+a;}", "function(){y=x?1+a:2+a}");

    
    fold("function(){if(x)y+=1;else y+=2;}", "function(){y+=x?1:2}");
    fold("function(){if(x)y-=1;else y-=2;}", "function(){y-=x?1:2}");
    fold("function(){if(x)y%=1;else y%=2;}", "function(){y%=x?1:2}");
    fold("function(){if(x)y|=1;else y|=2;}", "function(){y|=x?1:2}");

    
    foldSame("function(){if(x)y-=1;else y+=2}");

    
    foldSame("function(){if(x)y-=1;else z-=1}");

    
    foldSame("function(){if(x)y().a=3;else y().a=4}");
  }

// com.google.javascript.jscomp.FoldConstantsTest::testBug1059649
  public void testBug1059649() {
    
    fold("if(x){var y=3;}var z=5", "if(x)var y=3;var z=5");

    
    foldSame("if(x){var y=3;}else{var y=4;}var z=5");
    fold("while(x){var y=3;}var z=5", "while(x)var y=3;var z=5");
    fold("for(var i=0;i<10;i++){var y=3;}var z=5",
         "for(var i=0;i<10;i++)var y=3;var z=5");
    fold("for(var i in x){var y=3;}var z=5",
         "for(var i in x)var y=3;var z=5");
    fold("do{var y=3;}while(x);var z=5", "do var y=3;while(x);var z=5");
  }

// com.google.javascript.jscomp.FoldConstantsTest::testUndefinedComparison
  public void testUndefinedComparison() {
    fold("if (0 == 0){ x = 1; } else { x = 2; }", "x=1");
    fold("if (undefined == undefined){ x = 1; } else { x = 2; }", "x=1");
    fold("if (undefined == null){ x = 1; } else { x = 2; }", "x=1");
    
    fold("if (undefined == 0){ x = 1; } else { x = 2; }", "x=2");
    fold("if (undefined == 1){ x = 1; } else { x = 2; }", "x=2");
    fold("if (undefined == 'hi'){ x = 1; } else { x = 2; }", "x=2");
    fold("if (undefined == true){ x = 1; } else { x = 2; }", "x=2");
    fold("if (undefined == false){ x = 1; } else { x = 2; }", "x=2");
    fold("if (undefined === undefined){ x = 1; } else { x = 2; }", "x=1");
    fold("if (undefined === null){ x = 1; } else { x = 2; }", "x=2");
    fold("if (undefined === void 0){ x = 1; } else { x = 2; }", "x=1");
    
    foldSame("x = (undefined == this) ? 1 : 2;");
    foldSame("x = (undefined == x) ? 1 : 2;");

    fold("if (undefined != undefined){ x = 1; } else { x = 2; }", "x=2");
    fold("if (undefined != null){ x = 1; } else { x = 2; }", "x=2");
    
    fold("if (undefined != 0){ x = 1; } else { x = 2; }", "x=1");
    fold("if (undefined != 1){ x = 1; } else { x = 2; }", "x=1");
    fold("if (undefined != 'hi'){ x = 1; } else { x = 2; }", "x=1");
    fold("if (undefined != true){ x = 1; } else { x = 2; }", "x=1");
    fold("if (undefined != false){ x = 1; } else { x = 2; }", "x=1");
    fold("if (undefined !== undefined){ x = 1; } else { x = 2; }", "x=2");
    fold("if (undefined !== null){ x = 1; } else { x = 2; }", "x=1");
    foldSame("x = (undefined != this) ? 1 : 2;");
    foldSame("x = (undefined != x) ? 1 : 2;");

    fold("if (undefined < undefined){ x = 1; } else { x = 2; }", "x=2");
    fold("if (undefined > undefined){ x = 1; } else { x = 2; }", "x=2");
    fold("if (undefined >= undefined){ x = 1; } else { x = 2; }", "x=2");
    fold("if (undefined <= undefined){ x = 1; } else { x = 2; }", "x=2");

    fold("if (0 < undefined){ x = 1; } else { x = 2; }", "x=2");
    fold("if (true > undefined){ x = 1; } else { x = 2; }", "x=2");
    fold("if ('hi' >= undefined){ x = 1; } else { x = 2; }", "x=2");
    fold("if (null <= undefined){ x = 1; } else { x = 2; }", "x=2");

    fold("if (undefined < 0){ x = 1; } else { x = 2; }", "x=2");
    fold("if (undefined > true){ x = 1; } else { x = 2; }", "x=2");
    fold("if (undefined >= 'hi'){ x = 1; } else { x = 2; }", "x=2");
    fold("if (undefined <= null){ x = 1; } else { x = 2; }", "x=2");

    fold("if (null == undefined){ x = 1; } else { x = 2; }", "x=1");
    
    fold("if (0 == undefined){ x = 1; } else { x = 2; }", "x=2");
    fold("if (1 == undefined){ x = 1; } else { x = 2; }", "x=2");
    fold("if ('hi' == undefined){ x = 1; } else { x = 2; }", "x=2");
    fold("if (true == undefined){ x = 1; } else { x = 2; }", "x=2");
    fold("if (false == undefined){ x = 1; } else { x = 2; }", "x=2");
    fold("if (null === undefined){ x = 1; } else { x = 2; }", "x=2");
    fold("if (void 0 === undefined){ x = 1; } else { x = 2; }", "x=1");
    
    foldSame("x = (this == undefined) ? 1 : 2;");
    foldSame("x = (x == undefined) ? 1 : 2;");
  }

// com.google.javascript.jscomp.FoldConstantsTest::testHookIf
  public void testHookIf() {
    fold("if (1){ x=1; } else { x = 2;}", "x=1");
    fold("if (false){ x = 1; } else { x = 2; }", "x=2");
    fold("if (undefined){ x = 1; } else { x = 2; }", "x=2");
    fold("if (null){ x = 1; } else { x = 2; }", "x=2");
    fold("if (void 0){ x = 1; } else { x = 2; }", "x=2");
    
    fold("if (false){ x = 1; } else if (true) { x = 3; } else { x = 2; }",
         "x=3");
    fold("if (false){ x = 1; } else if (cond) { x = 2; } else { x = 3; }",
         "x=cond?2:3");
    fold("var x = (true) ? 1 : 0", "var x=1");
    fold("var y = (true) ? ((false) ? 12 : (cond ? 1 : 2)) : 13",
         "var y=cond?1:2");
    fold("if (x){ x = 1; } else if (false) { x = 3; }", "if(x)x=1");
    fold("x?void 0:y()", "x||y()");
    fold("!x?void 0:y()", "x&&y()");
    foldSame("var z=x?void 0:y()");
    foldSame("z=x?void 0:y()");
    foldSame("z*=x?void 0:y()");
    fold("x?y():void 0", "x&&y()");
    foldSame("var z=x?y():void 0");
    foldSame("(w?x:void 0).y=z");
    foldSame("(w?x:void 0).y+=z");
  }

// com.google.javascript.jscomp.FoldConstantsTest::testRemoveDuplicateStatements
  public void testRemoveDuplicateStatements() {
    fold("if (a) { x = 1; x++ } else { x = 2; x++ }",
         "x=(a) ? 1 : 2; x++");
    fold("if (a) { x = 1; x++; y += 1; z = pi; }" +
         " else  { x = 2; x++; y += 1; z = pi; }",
         "x=(a) ? 1 : 2; x++; y += 1; z = pi;");
    fold("function z() {" +
         "if (a) { foo(); return true } else { goo(); return true }" +
         "}",
         "function z() {(a) ? foo() : goo(); return true}");
    fold("function z() {if (a) { foo(); x = true; return true " +
         "} else { goo(); x = true; return true }}",
         "function z() {(a) ? foo() : goo(); x = true; return true}");
    fold("function z() {if (a) { return true }" +
         "else if (b) { return true }" +
         "else { return true }}",
         "function z() {return true;}");
    fold("function z() {if (a()) { return true }" +
         "else if (b()) { return true }" +
         "else { return true }}",
         "function z() {if (!a()) { b() } return true;}");
    fold("function z() {" +
         "  if (a) { bar(); foo(); return true }" +
         "    else { bar(); goo(); return true }" +
         "}",
         "function z() {" +
         "  if (a) { bar(); foo(); }" +
         "    else { bar(); goo(); }" +
         "  return true;" +
         "}");
  }

// com.google.javascript.jscomp.FoldConstantsTest::testNotCond
  public void testNotCond() {
    fold("function(){if(!x)foo()}", "function(){x||foo()}");
    fold("function(){if(!x)b=1}", "function(){x||(b=1)}");
    fold("if(!x)z=1;else if(y)z=2", "if(x){if(y)z=2}else z=1");
    foldSame("function(){if(!(x=1))a.b=1}");
  }

// com.google.javascript.jscomp.FoldConstantsTest::testAndParenthesesCount
  public void testAndParenthesesCount() {
    foldSame("function(){if(x||y)a.foo()}");
  }

// com.google.javascript.jscomp.FoldConstantsTest::testUnaryOps
  public void testUnaryOps() {
    fold("!foo()", "foo()");
    fold("~foo()", "foo()");
    fold("-foo()", "foo()");
    fold("a=!true", "a=false");
    fold("a=!10", "a=false");
    fold("a=!false", "a=true");
    fold("a=!foo()", "a=!foo()");
    fold("a=-0", "a=0");
    fold("a=-Infinity", "a=-Infinity");
    fold("a=-NaN", "a=NaN");
    fold("a=-foo()", "a=-foo()");
    fold("a=~~0", "a=0");
    fold("a=~~10", "a=10");
    fold("a=~-7", "a=6");
    fold("a=~0x100000000", "a=~0x100000000",
         FoldConstants.BITWISE_OPERAND_OUT_OF_RANGE);
    fold("a=~-0x100000000", "a=~-0x100000000",
         FoldConstants.BITWISE_OPERAND_OUT_OF_RANGE);
    fold("a=~.5", "~.5", FoldConstants.FRACTIONAL_BITWISE_OPERAND);
  }

// com.google.javascript.jscomp.FoldConstantsTest::testUnaryOpsStringCompare
  public void testUnaryOpsStringCompare() {
    
    assertResultString("a=-1", "a=-1");
    assertResultString("a=~0", "a=-1");
    assertResultString("a=~1", "a=-2");
    assertResultString("a=~101", "a=-102");
  }

// com.google.javascript.jscomp.FoldConstantsTest::testFoldLogicalOp
  public void testFoldLogicalOp() {
    fold("x = true && x", "x = x");
    fold("x = false && x", "x = false");
    fold("x = true || x", "x = true");
    fold("x = false || x", "x = x");
    fold("x = 0 && x", "x = 0");
    fold("x = 3 || x", "x = 3");
    fold("x = false || 0", "x = 0");

    fold("if(x && true) z()", "x&&z()");
    fold("if(x && false) z()", "");
    fold("if(x || 3) z()", "z()");
    fold("if(x || false) z()", "x&&z()");
    fold("if(x==y && false) z()", "");

    
    
    fold("if(y() || x || 3) z()", "if(y()||x||1)z()");

    
    fold("a = x && true", "a=x&&true");
    fold("a = x && false", "a=x&&false");
    fold("a = x || 3", "a=x||3");
    fold("a = x || false", "a=x||false");
    fold("a = b ? c : x || false", "a=b?c:x||false");
    fold("a = b ? x || false : c", "a=b?x||false:c");
    fold("a = b ? c : x && true", "a=b?c:x&&true");
    fold("a = b ? x && true : c", "a=b?x&&true:c");

    
    fold("a = x || false ? b : c", "a=x?b:c");
    fold("a = x && true ? b : c", "a=x?b:c");

    
    fold("if(foo() || true) z()", "if(foo()||1)z()");

    fold("x = foo() || true || bar()", "x = foo()||true");
    fold("x = foo() || false || bar()", "x = foo()||bar()");
    fold("x = foo() || true && bar()", "x = foo()||bar()");
    fold("x = foo() || false && bar()", "x = foo()||false");
    fold("x = foo() && false && bar()", "x = foo()&&false");
    fold("x = foo() && true && bar()", "x = foo()&&bar()");
    fold("x = foo() && false || bar()", "x = foo()&&false||bar()");

    
    
    
    fold("x = foo() && true || bar()", "x = foo()&&true||bar()");
    fold("foo() && true || bar()", "foo()&&1||bar()");
  }

// com.google.javascript.jscomp.FoldConstantsTest::testFoldLogicalOpStringCompare
  public void testFoldLogicalOpStringCompare() {
    
    
    assertResultString("if(foo() && false) z()", "foo()&&0&&z()");
  }

// com.google.javascript.jscomp.FoldConstantsTest::testFoldBitwiseOp
  public void testFoldBitwiseOp() {
    fold("x = 1 & 1", "x = 1");
    fold("x = 1 & 2", "x = 0");
    fold("x = 3 & 1", "x = 1");
    fold("x = 3 & 3", "x = 3");

    fold("x = 1 | 1", "x = 1");
    fold("x = 1 | 2", "x = 3");
    fold("x = 3 | 1", "x = 3");
    fold("x = 3 | 3", "x = 3");

    fold("x = -1 & 0", "x = 0");
    fold("x = 0 & -1", "x = 0");
    fold("x = 1 & 4", "x = 0");
    fold("x = 2 & 3", "x = 2");

    
    
    fold("x = 1 & 1.1", "x = 1&1.1");
    fold("x = 1.1 & 1", "x = 1.1&1");
    fold("x = 1 & 3000000000", "x = 1&3000000000");
    fold("x = 3000000000 & 1", "x = 3000000000&1");

    
    fold("x = 1 | 4", "x = 5");
    fold("x = 1 | 3", "x = 3");
    fold("x = 1 | 1.1", "x = 1|1.1");
    fold("x = 1 | 3000000000", "x = 1|3000000000");
  }

// com.google.javascript.jscomp.FoldConstantsTest::testFoldBitwiseOpStringCompare
  public void testFoldBitwiseOpStringCompare() {
    assertResultString("x = -1 | 0", "x=-1");
    assertResultString("-1 | 0", "1");
  }

// com.google.javascript.jscomp.FoldConstantsTest::testFoldBitShifts
  public void testFoldBitShifts() {
    fold("x = 1 << 0", "x = 1");
    fold("x = 1 << 1", "x = 2");
    fold("x = 3 << 1", "x = 6");
    fold("x = 1 << 8", "x = 256");

    fold("x = 1 >> 0", "x = 1");
    fold("x = 1 >> 1", "x = 0");
    fold("x = 2 >> 1", "x = 1");
    fold("x = 5 >> 1", "x = 2");
    fold("x = 127 >> 3", "x = 15");
    fold("x = 3 >> 1", "x = 1");
    fold("x = 3 >> 2", "x = 0");
    fold("x = 10 >> 1", "x = 5");
    fold("x = 10 >> 2", "x = 2");
    fold("x = 10 >> 5", "x = 0");

    fold("x = 10 >>> 1", "x = 5");
    fold("x = 10 >>> 2", "x = 2");
    fold("x = 10 >>> 5", "x = 0");
    fold("x = -1 >>> 1", "x = " + 0x7fffffff);

    fold("3000000000 << 1", "3000000000<<1",
         FoldConstants.BITWISE_OPERAND_OUT_OF_RANGE);
    fold("1 << 32", "1<<32",
         FoldConstants.SHIFT_AMOUNT_OUT_OF_BOUNDS);
    fold("1 << -1", "1<<32",
         FoldConstants.SHIFT_AMOUNT_OUT_OF_BOUNDS);
    fold("3000000000 >> 1", "3000000000>>1",
         FoldConstants.BITWISE_OPERAND_OUT_OF_RANGE);
    fold("1 >> 32", "1>>32",
         FoldConstants.SHIFT_AMOUNT_OUT_OF_BOUNDS);
    fold("1.5 << 0",  "1.5<<0",  FoldConstants.FRACTIONAL_BITWISE_OPERAND);
    fold("1 << .5",   "1.5<<0",  FoldConstants.FRACTIONAL_BITWISE_OPERAND);
    fold("1.5 >>> 0", "1.5>>>0", FoldConstants.FRACTIONAL_BITWISE_OPERAND);
    fold("1 >>> .5",  "1.5>>>0", FoldConstants.FRACTIONAL_BITWISE_OPERAND);
    fold("1.5 >> 0",  "1.5>>0",  FoldConstants.FRACTIONAL_BITWISE_OPERAND);
    fold("1 >> .5",   "1.5>>0",  FoldConstants.FRACTIONAL_BITWISE_OPERAND);
  }

// com.google.javascript.jscomp.FoldConstantsTest::testFoldBitShiftsStringCompare
  public void testFoldBitShiftsStringCompare() {
    
    assertResultString("x = -1 << 1", "x=-2");
    assertResultString("x = -1 << 8", "x=-256");
    assertResultString("x = -1 >> 1", "x=-1");
    assertResultString("x = -2 >> 1", "x=-1");
    assertResultString("x = -1 >> 0", "x=-1");
  }

// com.google.javascript.jscomp.FoldConstantsTest::testStringAdd
  public void testStringAdd() {
    fold("x = 'a' + \"bc\"", "x = \"abc\"");
    fold("x = 'a' + 5", "x = \"a5\"");
    fold("x = 5 + 'a'", "x = \"5a\"");
    fold("x = 'a' + ''", "x = \"a\"");
    fold("x = \"a\" + foo()", "x = \"a\"+foo()");
    fold("x = foo() + 'a' + 'b'", "x = foo()+\"ab\"");
    fold("x = (foo() + 'a') + 'b'", "x = foo()+\"ab\"");  
    fold("x = foo() + 'a' + 'b' + 'cd' + bar()", "x = foo()+\"abcd\"+bar()");
    fold("x = foo() + 2 + 'b'", "x = foo()+2+\"b\"");  
    fold("x = foo() + 'a' + 2", "x = foo()+\"a2\"");
    fold("x = '' + null", "x = \"null\"");
    fold("x = true + '' + false", "x = \"truefalse\"");
    fold("x = '' + []", "x = \"\"+[]");      
  }

// com.google.javascript.jscomp.FoldConstantsTest::testStringIndexOf
  public void testStringIndexOf() {
    fold("x = 'abcdef'.indexOf('b')", "x = 1");
    fold("x = 'abcdefbe'.indexOf('b', 2)", "x = 6");
    fold("x = 'abcdef'.indexOf('bcd')", "x = 1");
    fold("x = 'abcdefsdfasdfbcdassd'.indexOf('bcd', 4)", "x = 13");

    fold("x = 'abcdef'.lastIndexOf('b')", "x = 1");
    fold("x = 'abcdefbe'.lastIndexOf('b')", "x = 6");
    fold("x = 'abcdefbe'.lastIndexOf('b', 5)", "x = 1");

    
    
    fold("x = 'abc1def'.indexOf(1)", "x = 3");
    fold("x = 'abcNaNdef'.indexOf(NaN)", "x = 3");
    fold("x = 'abcundefineddef'.indexOf(undefined)", "x = 3");
    fold("x = 'abcnulldef'.indexOf(null)", "x = 3");
    fold("x = 'abctruedef'.indexOf(true)", "x = 3");

    
    
    foldSame("x = NaN.indexOf('bcd')");
    foldSame("x = undefined.indexOf('bcd')");
    foldSame("x = null.indexOf('bcd')");
    foldSame("x = true.indexOf('bcd')");
    foldSame("x = false.indexOf('bcd')");

    
    foldSame("x = 'abcdef'.indexOf(/b./)");
    foldSame("x = 'abcdef'.indexOf({a:2})");
    foldSame("x = 'abcdef'.indexOf([1,2])");
  }

// com.google.javascript.jscomp.FoldConstantsTest::testStringJoinAdd
  public void testStringJoinAdd() {
    fold("x = ['a', 'b', 'c'].join('')", "x = \"abc\"");
    fold("x = [].join(',')", "x = \"\"");
    fold("x = ['a'].join(',')", "x = \"a\"");
    fold("x = ['a', 'b', 'c'].join(',')", "x = \"a,b,c\"");
    fold("x = ['a', foo, 'b', 'c'].join(',')", "x = [\"a\",foo,\"b,c\"].join(\",\")");
    fold("x = [foo, 'a', 'b', 'c'].join(',')", "x = [foo,\"a,b,c\"].join(\",\")");
    fold("x = ['a', 'b', 'c', foo].join(',')", "x = [\"a,b,c\",foo].join(\",\")");

    
    fold("x = ['a=', 5].join('')", "x = \"a=5\"");
    fold("x = ['a', '5'].join(7)", "x = \"a75\"");

    
    fold("x = ['a=', false].join('')", "x = \"a=false\"");
    fold("x = ['a', '5'].join(true)", "x = \"atrue5\"");
    fold("x = ['a', '5'].join(false)", "x = \"afalse5\"");

    
    fold("x = ['a', '5', 'c'].join('a very very very long chain')",
         "x = [\"a\",\"5\",\"c\"].join(\"a very very very long chain\")");
    
    
    foldSame("x = ['', foo].join(',')");
    foldSame("x = ['', foo, ''].join(',')");
    
    fold("x = ['', '', foo, ''].join(',')", "x = [',', foo, ''].join(',')");
    fold("x = ['', '', foo, '', ''].join(',')",
         "x = [',', foo, ','].join(',')");
    
    fold("x = ['', '', foo, '', '', bar].join(',')",
         "x = [',', foo, ',', bar].join(',')");
    
    fold("x = [1,2,3].join('abcdef')",
         "x = '1abcdef2abcdef3'");
  }

// com.google.javascript.jscomp.FoldConstantsTest::testStringJoinAdd_b1992789
  public void testStringJoinAdd_b1992789() {
    fold("x = ['a'].join('')", "x = \"a\"");
    fold("x = [foo()].join('')", "x = '' + foo()");
    fold("[foo()].join('')", "'' + foo()");
  }

// com.google.javascript.jscomp.FoldConstantsTest::testFoldArithmetic
  public void testFoldArithmetic() {
    fold("x = 10 + 20", "x = 30");
    fold("x = 2 / 4", "x = 0.5");
    fold("x = 2.25 * 3", "x = 6.75");
    fold("z = x * y", "z = x * y");
    fold("x = y * 5", "x = y * 5");
    fold("x = 1 / 0", "", FoldConstants.DIVIDE_BY_0_ERROR);
  }

// com.google.javascript.jscomp.FoldConstantsTest::testFoldArithmeticStringComp
  public void testFoldArithmeticStringComp() {
    
    assertResultString("x = 10 - 20", "x=-10");
  }

// com.google.javascript.jscomp.FoldConstantsTest::testFoldComparison
  public void testFoldComparison() {
    fold("x = 0 == 0", "x = true");
    fold("x = 1 == 2", "x = false");
    fold("x = 'abc' == 'def'", "x = false");
    fold("x = 'abc' == 'abc'", "x = true");
    fold("x = \"\" == ''", "x = true");
    fold("x = foo() == bar()", "x = foo()==bar()");

    fold("x = 1 != 0", "x = true");
    fold("x = 'abc' != 'def'", "x = true");
    fold("x = 'a' != 'a'", "x = false");

    fold("x = 1 < 20", "x = true");
    fold("x = 3 < 3", "x = false");
    fold("x = 10 > 1.0", "x = true");
    fold("x = 10 > 10.25", "x = false");
    fold("x = y == y", "x = y==y");
    fold("x = y < y", "x = false");
    fold("x = y > y", "x = false");
    fold("x = 1 <= 1", "x = true");
    fold("x = 1 <= 0", "x = false");
    fold("x = 0 >= 0", "x = true");
    fold("x = -1 >= 9", "x = false");

    fold("x = true == true", "x = true");
    fold("x = true == true", "x = true");
    fold("x = false == null", "x = false");
    fold("x = false == true", "x = false");
    fold("x = true == null", "x = false");

    fold("0 == 0", "1");
    fold("1 == 2", "0");
    fold("'abc' == 'def'", "0");
    fold("'abc' == 'abc'", "1");
    fold("\"\" == ''", "1");
    fold("foo() == bar()", "foo()==bar()");

    fold("1 != 0", "1");
    fold("'abc' != 'def'", "1");
    fold("'a' != 'a'", "0");

    fold("1 < 20", "1");
    fold("3 < 3", "0");
    fold("10 > 1.0", "1");
    fold("10 > 10.25", "0");
    fold("x == x", "x==x");
    fold("x < x", "0");
    fold("x > x", "0");
    fold("1 <= 1", "1");
    fold("1 <= 0", "0");
    fold("0 >= 0", "1");
    fold("-1 >= 9", "0");

    fold("true == true", "1");
    fold("true == true", "1");
    fold("false == null", "0");
    fold("false == true", "0");
    fold("true == null", "0");
  }

// com.google.javascript.jscomp.FoldConstantsTest::testFoldNot
  public void testFoldNot() {
    fold("while(!(x==y)){a=b;}" , "while(x!=y){a=b;}");
    fold("while(!(x!=y)){a=b;}" , "while(x==y){a=b;}");
    fold("while(!(x===y)){a=b;}", "while(x!==y){a=b;}");
    fold("while(!(x!==y)){a=b;}", "while(x===y){a=b;}");
    
    foldSame("while(!(x>y)){a=b;}");
    foldSame("while(!(x>=y)){a=b;}");
    foldSame("while(!(x<y)){a=b;}");
    foldSame("while(!(x<=y)){a=b;}");
    foldSame("while(!(x<=NaN)){a=b;}");
  }

// com.google.javascript.jscomp.FoldConstantsTest::testFoldGetElem
  public void testFoldGetElem() {
    fold("x = [10, 20][0]", "x = 10");
    fold("x = [10, 20][1]", "x = 20");
    fold("x = [10, 20][0.5]", "", FoldConstants.INVALID_GETELEM_INDEX_ERROR);
    fold("x = [10, 20][-1]",    "", FoldConstants.INDEX_OUT_OF_BOUNDS_ERROR);
    fold("x = [10, 20][2]",     "", FoldConstants.INDEX_OUT_OF_BOUNDS_ERROR);
  }

// com.google.javascript.jscomp.FoldConstantsTest::testFoldComplex
  public void testFoldComplex() {
    fold("x = (3 / 1.0) + (1 * 2)", "x = 5");
    fold("x = (1 == 1.0) && foo() && true", "x = foo()&&true");
    fold("x = 'abc' + 5 + 10", "x = \"abc510\"");
  }

// com.google.javascript.jscomp.FoldConstantsTest::testFoldArrayLength
  public void testFoldArrayLength() {
    
    fold("x = [].length", "x = 0");
    fold("x = [1,2,3].length", "x = 3");
    fold("x = [a,b].length", "x = 2");

    
    fold("x = [foo(), 0].length", "x = [foo(),0].length");
    fold("x = y.length", "x = y.length");
  }

// com.google.javascript.jscomp.FoldConstantsTest::testFoldStringLength
  public void testFoldStringLength() {
    
    fold("x = ''.length", "x = 0");
    fold("x = '123'.length", "x = 3");

    
    fold("x = '123\u01dc'.length", "x = 4");
  }

// com.google.javascript.jscomp.FoldConstantsTest::testFoldRegExpConstructor
  public void testFoldRegExpConstructor() {
    
    
    fold("x = new RegExp",                    "x = new RegExp");
    
    fold("x = new RegExp(\"\")",              "x = new RegExp(\"\")");
    fold("x = new RegExp(\"\", \"i\")",       "x = new RegExp(\"\",\"i\")");
    
    fold("x = new RegExp(\"foobar\", \"bogus\")",
         "x = new RegExp(\"foobar\",\"bogus\")",
         FoldConstants.INVALID_REGULAR_EXPRESSION_FLAGS);
    
    fold("x = new RegExp(\"foobar\", \"g\")",
         "x = new RegExp(\"foobar\",\"g\")");
    fold("x = new RegExp(\"foobar\", \"ig\")",
         "x = new RegExp(\"foobar\",\"ig\")");

    
    fold("x = new RegExp(\"foobar\")",        "x = /foobar/");
    fold("x = new RegExp(\"foobar\", \"i\")", "x = /foobar/i");
    
    fold("x = new RegExp(\"\\\\.\", \"i\")",  "x = /\\./i");
    fold("x = new RegExp(\"/\", \"\")",       "x = /\\//");
    fold("x = new RegExp(\"///\", \"\")",     "x = /\\/\\/\\//");
    fold("x = new RegExp(\"\\\\\\/\", \"\")", "x = /\\//");
    
    
    fold("x = new RegExp(\"\\u2028\")", "x = new RegExp(\"\\u2028\")");
    fold("x = new RegExp(\"\\\\\\\\u2028\")", "x = /\\\\u2028/");

    
    
    String longRegexp = "";
    for (int i = 0; i < 200; i++) longRegexp += "x";
    foldSame("x = new RegExp(\"" + longRegexp + "\")");
  }

// com.google.javascript.jscomp.FoldConstantsTest::testFoldRegExpConstructorStringCompare
  public void testFoldRegExpConstructorStringCompare() {
    
    
    assertResultString("x=new RegExp(\"\\n\", \"i\")", "x=/\\n/i");
  }

// com.google.javascript.jscomp.FoldConstantsTest::testFoldTypeof
  public void testFoldTypeof() {
    fold("x = typeof 1", "x = \"number\"");
    fold("x = typeof 'foo'", "x = \"string\"");
    fold("x = typeof true", "x = \"boolean\"");
    fold("x = typeof false", "x = \"boolean\"");
    fold("x = typeof null", "x = \"object\"");
    fold("x = typeof undefined", "x = \"undefined\"");
    fold("x = typeof []", "x = \"object\"");
    fold("x = typeof [1]", "x = \"object\"");
    fold("x = typeof [1,[]]", "x = \"object\"");
    fold("x = typeof {}", "x = \"object\"");

    foldSame("x = typeof[1,[foo()]]");
    foldSame("x = typeof{bathwater:baby()}");
  }

// com.google.javascript.jscomp.FoldConstantsTest::testFoldLiteralConstructors
  public void testFoldLiteralConstructors() {
    
    fold("x = new Array", "x = []");
    fold("x = new Array()", "x = []");
    fold("x = new Object", "x = ({})");
    fold("x = new Object()", "x = ({})");

    
    fold("x = new Array(7)", "x = new Array(7)");

    
    fold("x = " +
         "(function(){function Object(){this.x=4};return new Object();})();",
         "x = (function(){function Object(){this.x=4}return new Object})()");
  }

// com.google.javascript.jscomp.FoldConstantsTest::testVarLifting
  public void testVarLifting() {
    fold("if(true)var a", "var a");
    fold("if(false)var a", "var a");
    fold("if(true);else var a;", "var a");
    fold("if(false) foo();else var a;", "var a");
    fold("if(true)var a;else;", "var a");
    fold("if(false)var a;else;", "var a");
    fold("if(false)var a,b;", "var b; var a");
    fold("if(false){var a;var a;}", "var a");
    fold("if(false)var a=function(){var b};", "var a");
    fold("if(a)if(false)var a;else var b;", "var a;if(a)var b");
  }

// com.google.javascript.jscomp.FoldConstantsTest::testContainsUnicodeEscape
  public void testContainsUnicodeEscape() throws Exception {
    assertTrue(!FoldConstants.containsUnicodeEscape(""));
    assertTrue(!FoldConstants.containsUnicodeEscape("foo"));
    assertTrue( FoldConstants.containsUnicodeEscape("\u2028"));
    assertTrue( FoldConstants.containsUnicodeEscape("\\u2028"));
    assertTrue( FoldConstants.containsUnicodeEscape("foo\\u2028"));
    assertTrue(!FoldConstants.containsUnicodeEscape("foo\\\\u2028"));
    assertTrue( FoldConstants.containsUnicodeEscape("foo\\\\u2028bar\\u2028"));
  }

// com.google.javascript.jscomp.FoldConstantsTest::testBug1438784
  public void testBug1438784() throws Exception {
    fold("for(var i=0;i<10;i++)if(x)x.y;", "for(var i=0;i<10;i++);");
  }

// com.google.javascript.jscomp.FoldConstantsTest::testFoldUselessWhile
  public void testFoldUselessWhile() {
    fold("while(false) { foo() }", "");
    fold("while(!true) { foo() }", "");
    fold("while(void 0) { foo() }", "");
    fold("while(undefined) { foo() }", "");
    fold("while(!false) foo() ", "while(1) foo()");
    fold("while(true) foo() ", "while(1) foo() ");
    fold("while(!void 0) foo()", "while(1) foo()");
    fold("while(false) { var a = 0; }", "var a");

    
    fold("while(false) { foo(); continue }", "");

    
    fold("if(foo())while(false){foo()}else bar()", "foo()||bar()");
  }

// com.google.javascript.jscomp.FoldConstantsTest::testFoldUselessFor
  public void testFoldUselessFor() {
    fold("for(;false;) { foo() }", "");
    fold("for(;!true;) { foo() }", "");
    fold("for(;void 0;) { foo() }", "");
    fold("for(;undefined;) { foo() }", "");
    fold("for(;!false;) foo() ", "for(;;) foo()");
    fold("for(;true;) foo() ", "for(;;) foo() ");
    fold("for(;1;) foo()", "for(;;) foo()");
    foldSame("for(;;) foo()");
    fold("for(;!void 0;) foo()", "for(;;) foo()");
    fold("for(;false;) { var a = 0; }", "var a");

    
    fold("for(;false;) { foo(); continue }", "");

    
    fold("if(foo())for(;false;){foo()}else bar()", "foo()||bar()");
  }

// com.google.javascript.jscomp.FoldConstantsTest::testFoldUselessDo
  public void testFoldUselessDo() {
    fold("do { foo() } while(false);", "foo()");
    fold("do { foo() } while(!true);", "foo()");
    fold("do { foo() } while(void 0);", "foo()");
    fold("do { foo() } while(undefined);", "foo()");
    fold("do { foo() } while(!false);", "do { foo() } while(1);");
    fold("do { foo() } while(true);", "do { foo() } while(1);");
    fold("do { foo() } while(!void 0);", "do { foo() } while(1);");
    fold("do { var a = 0; } while(false);", "var a=0");

    
    foldSame("do { foo(); continue; } while(0)");
    foldSame("do { foo(); break; } while(0)");

    
    fold("if(foo())do {foo()} while(false) else bar()", "foo()?foo():bar()");
  }

// com.google.javascript.jscomp.FoldConstantsTest::testMinimizeCondition
  public void testMinimizeCondition() {
    
    fold("while(!!true) foo()", "while(1) foo()");
    
    fold("while(!!x) foo()", "while(x) foo()");
    fold("while(!(!x&&!y)) foo()", "while(x||y) foo()");
    fold("while(x||!!y) foo()", "while(x||y) foo()");
    fold("while(!(!!x&&y)) foo()", "while(!(x&&y)) foo()");
  }

// com.google.javascript.jscomp.FoldConstantsTest::testMinimizeCondition_example1
  public void testMinimizeCondition_example1() {
    
    fold("if(!!(f() > 20)) {foo();foo()}", "if(f() > 20){foo();foo()}");
  }

// com.google.javascript.jscomp.FoldConstantsTest::testMinimizeWhileConstantCondition
  public void testMinimizeWhileConstantCondition() {
    fold("while(true) foo()", "while(1) foo()");
    fold("while(!false) foo()", "while(1) foo()");
    fold("while(202) foo()", "while(1) foo()");
    fold("while(Infinity) foo()", "while(1) foo()");
    fold("while('text') foo()", "while(1) foo()");
    fold("while([]) foo()", "while(1) foo()");
    fold("while({}) foo()", "while(1) foo()");
    fold("while(/./) foo()", "while(1) foo()");
    fold("while(0) foo()", "");
    fold("while(0.0) foo()", "");
    fold("while(NaN) foo()", "");
    fold("while(null) foo()", "");
    fold("while(undefined) foo()", "");
    fold("while('') foo()", "");
  }

// com.google.javascript.jscomp.FoldConstantsTest::testMinimizeExpr
  public void testMinimizeExpr() {
    fold("!!true", "0");
    fold("!!x", "x");
    fold("!(!x&&!y)", "!x&&!y");
    fold("x||!!y", "x||y");
    fold("!(!!x&&y)", "x&&y");
  }

// com.google.javascript.jscomp.FoldConstantsTest::testBug1509085
  public void testBug1509085() {
    new FoldConstantsTest() {
      @Override
      protected int getNumRepetitions() {
        return 1;
      }
    }.fold("x ? x() : void 0", "if(x) x();");
  }

// com.google.javascript.jscomp.FoldConstantsTest::testFoldInstanceOf
  public void testFoldInstanceOf() {
    
    fold("64 instanceof Object", "0");
    fold("64 instanceof Number", "0");
    fold("'' instanceof Object", "0");
    fold("'' instanceof String", "0");
    fold("true instanceof Object", "0");
    fold("true instanceof Boolean", "0");
    fold("false instanceof Object", "0");
    fold("null instanceof Object", "0");
    fold("undefined instanceof Object", "0");
    fold("NaN instanceof Object", "0");
    fold("Infinity instanceof Object", "0");

    
    fold("[] instanceof Object", "1");
    fold("({}) instanceof Object", "1");

    
    foldSame("new Foo() instanceof Object");
    
    foldSame("[] instanceof Foo");
    foldSame("({}) instanceof Foo");
  }

// com.google.javascript.jscomp.FoldConstantsTest::testDivision
  public void testDivision() {
    
    fold("print(1/3)", "print(1/3)");

    
    
    fold("print(1/2)", "print(0.5)");
  }

// com.google.javascript.jscomp.FoldConstantsTest::testAssignOps
  public void testAssignOps() {
    fold("x=x+y", "x+=y");
    fold("x=x*y", "x*=y");
    fold("x.y=x.y+z", "x.y+=z");
    foldSame("next().x = next().x + 1");
  }

// com.google.javascript.jscomp.FoldConstantsTest::testFoldConditionalVarDeclaration
  public void testFoldConditionalVarDeclaration() {
    fold("if(x) var y=1;else y=2", "var y=x?1:2");
    fold("if(x) y=1;else var y=2", "var y=x?1:2");

    foldSame("if(x) var y = 1; z = 2");
    foldSame("if(x) y = 1; var z = 2");

    foldSame("if(x) { var y = 1; print(y)} else y = 2 ");
    foldSame("if(x) var y = 1; else {y = 2; print(y)}");
  }

// com.google.javascript.jscomp.FoldConstantsTest::testFoldReturnResult
  public void testFoldReturnResult() {
    foldSame("function f(){return false;}");
    foldSame("function f(){return null;}");
    fold("function f(){return void 0;}",
         "function f(){return}");
    foldSame("function f(){return void foo();}");
    fold("function f(){return undefined;}",
         "function f(){return}");
    fold("function(){if(a()){return undefined;}}",
         "function(){if(a()){return}}");
  }

// com.google.javascript.jscomp.FoldConstantsTest::testBugIssue3
  public void testBugIssue3() {
    foldSame("function foo() {" +
             "  if(sections.length != 1) children[i] = 0;" +
             "  else var selectedid = children[i]" +
             "}");
  }

// com.google.javascript.jscomp.FoldConstantsTest::testBugIssue43
  public void testBugIssue43() {
    foldSame("function foo() {" +
             "  if (a) { var b = 1; } else { a.b = 1; }" +
             "}");
  }

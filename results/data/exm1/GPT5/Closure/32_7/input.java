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
// com.google.javascript.jscomp.LooseTypeCheckTest::testAnnotatedPropertyOnInterface4
  public void testAnnotatedPropertyOnInterface4() throws Exception {
    testTypes(
        CLOSURE_DEFS +
        " function T() {};\n" +
        " T.prototype.f = goog.abstractMethod;");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testWarnUnannotatedPropertyOnInterface5
  public void testWarnUnannotatedPropertyOnInterface5() throws Exception {
    testTypes(" u.T = function () {};\n" +
        "u.T.prototype.x = function() {};");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testWarnUnannotatedPropertyOnInterface6
  public void testWarnUnannotatedPropertyOnInterface6() throws Exception {
    testTypes(" function T() {};\n" +
        "T.prototype.x = function() {};");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testWarnDataPropertyOnInterface3
  public void testWarnDataPropertyOnInterface3() throws Exception {
    testTypes(" u.T = function () {};\n" +
        "u.T.prototype.x = 1;",
        "interface members can only be empty property declarations, "
        + "empty functions, or goog.abstractMethod");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testWarnDataPropertyOnInterface4
  public void testWarnDataPropertyOnInterface4() throws Exception {
    testTypes(" function T() {};\n" +
        "T.prototype.x = 1;",
        "interface members can only be empty property declarations, "
        + "empty functions, or goog.abstractMethod");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testErrorMismatchingPropertyOnInterface4
  public void testErrorMismatchingPropertyOnInterface4() throws Exception {
    testTypes(" u.T = function () {};\n" +
        "u.T.prototype.x =\n" +
        "function() {};",
        "parameter foo does not appear in u.T.prototype.x's parameter list");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testErrorMismatchingPropertyOnInterface5
  public void testErrorMismatchingPropertyOnInterface5() throws Exception {
    testTypes(" function T() {};\n" +
        "T.prototype.x = function() { };",
        "assignment to property x of T.prototype\n" +
        "found   : function (): undefined\n" +
        "required: number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testErrorMismatchingPropertyOnInterface6
  public void testErrorMismatchingPropertyOnInterface6() throws Exception {
    testClosureTypesMultipleWarnings(
        " function T() {};\n" +
        "T.prototype.x = 1",
        Lists.newArrayList(
            "assignment to property x of T.prototype\n" +
            "found   : number\n" +
            "required: function (this:T): number",
            "interface members can only be empty property declarations, " +
            "empty functions, or goog.abstractMethod"));
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testInterfaceNonEmptyFunction
  public void testInterfaceNonEmptyFunction() throws Exception {
    testTypes(" function T() {};\n" +
        "T.prototype.x = function() { return 'foo'; }",
        "interface member functions must have an empty body"
        );
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testDoubleNestedInterface
  public void testDoubleNestedInterface() throws Exception {
    testTypes(" var I1 = function() {};\n" +
              " I1.I2 = function() {};\n" +
              " I1.I2.I3 = function() {};\n");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testStaticDataPropertyOnNestedInterface
  public void testStaticDataPropertyOnNestedInterface() throws Exception {
    testTypes(" var I1 = function() {};\n" +
              " I1.I2 = function() {};\n" +
              " I1.I2.x = 1;\n");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testInterfaceInstantiation
  public void testInterfaceInstantiation() throws Exception {
    testTypes("var f = function(){}; new f",
              "cannot instantiate non-constructor");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testPrototypeLoop
  public void testPrototypeLoop() throws Exception {
    testClosureTypesMultipleWarnings(
        suppressMissingProperty("foo") +
        "var T = function() {};" +
        "alert((new T).foo);",
        Lists.newArrayList(
            "Parse error. Cycle detected in inheritance chain of type T",
            "Could not resolve type in @extends tag of T"));
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testDirectPrototypeAssign
  public void testDirectPrototypeAssign() throws Exception {
    
    testTypes(
        " function Foo() {}" +
        " function Bar() {}" +
        " Bar.prototype = new Foo()");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testResolutionViaRegistry1
  public void testResolutionViaRegistry1() throws Exception {
    testTypes(" u.T = function() {};\n" +
        " u.T.prototype.a;\n" +
        "\n" +
        "var f = function(t) { return t.a; };",
        "inconsistent return type\n" +
        "found   : (number|string)\n" +
        "required: string");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testResolutionViaRegistry2
  public void testResolutionViaRegistry2() throws Exception {
    testTypes(
        " u.T = function() {" +
        "  this.a = 0; };\n" +
        "\n" +
        "var f = function(t) { return t.a; };",
        "inconsistent return type\n" +
        "found   : number\n" +
        "required: string");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testResolutionViaRegistry3
  public void testResolutionViaRegistry3() throws Exception {
    testTypes(" u.T = function() {};\n" +
        " u.T.prototype.a = 0;\n" +
        "\n" +
        "var f = function(t) { return t.a; };",
        "inconsistent return type\n" +
        "found   : (number|string)\n" +
        "required: string");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testResolutionViaRegistry4
  public void testResolutionViaRegistry4() throws Exception {
    testTypes(" u.A = function() {};\n" +
        "\nu.A.A = function() {}\n;" +
        "\nu.A.B = function() {};\n" +
        "var ab = new u.A.B();\n" +
        " var a = ab;\n" +
        " var aa = ab;\n",
        "initializing variable\n" +
        "found   : u.A.B\n" +
        "required: u.A.A");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testResolutionViaRegistry5
  public void testResolutionViaRegistry5() throws Exception {
    Node n = parseAndTypeCheck(" u.T = function() {}; u.T");
    JSType type = n.getLastChild().getLastChild().getJSType();
    assertFalse(type.isUnknownType());
    assertTrue(type instanceof FunctionType);
    assertEquals("u.T",
        ((FunctionType) type).getInstanceType().getReferenceName());
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testGatherProperyWithoutAnnotation1
  public void testGatherProperyWithoutAnnotation1() throws Exception {
    Node n = parseAndTypeCheck(" var T = function() {};" +
        "var t; t.x; t;");
    JSType type = n.getLastChild().getLastChild().getJSType();
    assertFalse(type.isUnknownType());
    assertTrue(type instanceof ObjectType);
    ObjectType objectType = (ObjectType) type;
    assertFalse(objectType.hasProperty("x"));
    assertEquals(
        Lists.newArrayList(objectType),
        registry.getTypesWithProperty("x"));
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testGatherProperyWithoutAnnotation2
  public void testGatherProperyWithoutAnnotation2() throws Exception {
    TypeCheckResult ns =
        parseAndTypeCheckWithScope("var t; t.x; t;");
    Node n = ns.root;
    Scope s = ns.scope;
    JSType type = n.getLastChild().getLastChild().getJSType();
    assertFalse(type.isUnknownType());
    assertEquals(type, OBJECT_TYPE);
    assertTrue(type instanceof ObjectType);
    ObjectType objectType = (ObjectType) type;
    assertFalse(objectType.hasProperty("x"));
    assertEquals(
        Lists.newArrayList(OBJECT_TYPE),
        registry.getTypesWithProperty("x"));
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testFunctionMasksVariableBug
  public void testFunctionMasksVariableBug() throws Exception {
    testTypes("var x = 4; var f = function x(b) { return b ? 1 : x(true); };",
        "function x masks variable (IE bug)");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testDfa1
  public void testDfa1() throws Exception {
    testTypes("var x = null;\n x = 1;\n  var y = x;");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testDfa2
  public void testDfa2() throws Exception {
    testTypes("function u() {}\n" +
        " function f() {\nvar x = 'todo';\n" +
        "if (u()) { x = 1; } else { x = 2; } return x;\n}");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testDfa3
  public void testDfa3() throws Exception {
    testTypes("function u() {}\n" +
        " function f() {\n" +
        " var x = 'todo';\n" +
        "if (u()) { x = 1; } else { x = 2; } return x;\n}");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testDfa4
  public void testDfa4() throws Exception {
    testTypes(" function f(d) {\n" +
        "if (!d) { return; }\n" +
        " var e = d;\n}");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testDfa5
  public void testDfa5() throws Exception {
    testTypes(" function u() {return 'a';}\n" +
        " function f(x) {\n" +
        "while (!x) { x = u(); }\nreturn x;\n}");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testDfa6
  public void testDfa6() throws Exception {
    testTypes(" function u() {return {};}\n" +
        " function f(x) {\n" +
        "while (x) { x = u(); if (!x) { x = u(); } }\n}");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testDfa7
  public void testDfa7() throws Exception {
    testTypes(" var T = function() {};\n" +
        " T.prototype.x = null;\n" +
        " function f(t) {\n" +
        "if (!t.x) { return; }\n" +
        " var e = t.x;\n}");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testDfa8
  public void testDfa8() throws Exception {
    testTypes(" var T = function() {};\n" +
        " T.prototype.x = '';\n" +
        "function u() {}\n" +
        " function f(t) {\n" +
        "if (u()) { t.x = 1; } else { t.x = 2; } return t.x;\n}");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testDfa9
  public void testDfa9() throws Exception {
    testTypes("function f() {\nvar x;\nx = null;\n" +
        "if (x == null) { return 0; } else { return 1; } }",
        "condition always evaluates to true\n" +
        "left : null\n" +
        "right: null");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testDfa10
  public void testDfa10() throws Exception {
    testTypes(" function g(x) {}" +
        "function f(x) {\n" +
        "if (!x) { x = ''; }\n" +
        "if (g(x)) { return 0; } else { return 1; } }",
        "actual parameter 1 of g does not match formal parameter\n" +
        "found   : string\n" +
        "required: null");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testDfa11
  public void testDfa11() throws Exception {
    testTypes("\n" +
        "function f(opt_x) { if (!opt_x) { " +
        "throw new Error('x cannot be empty'); } return opt_x; }");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testDfa12
  public void testDfa12() throws Exception {
    testTypes("" +
        "var Bar = function(x) {};" +
        " function g(x) { return true; }" +
        " " +
        "function f(opt_x) { " +
        "  if (opt_x) { new Bar(g(opt_x) && 'x'); }" +
        "}",
        "actual parameter 1 of g does not match formal parameter\n" +
        "found   : (number|string)\n" +
        "required: string");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testDfa13
  public void testDfa13() throws Exception {
    testTypes(
        "" +
        "function g(x, y, z) {}" +
        "function f() { " +
        "  var x = 'a'; g(x, x = 3, x);" +
        "}");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeInferenceWithCast1
  public void testTypeInferenceWithCast1() throws Exception {
    testTypes(
        "function u(x) {return null;}" +
        "function f(x) {return x;}" +
        "function g(x) {" +
        "var y = (u(x)); return f(y);}");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeInferenceWithCast2
  public void testTypeInferenceWithCast2() throws Exception {
    testTypes(
        "function u(x) {return null;}" +
        "function f(x) {return x;}" +
        "function g(x) {" +
        "var y; y = (u(x)); return f(y);}");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeInferenceWithCast3
  public void testTypeInferenceWithCast3() throws Exception {
    testTypes(
        "function u(x) {return 1;}" +
        "function g(x) {" +
        "return (u(x));}");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeInferenceWithCast4
  public void testTypeInferenceWithCast4() throws Exception {
    testTypes(
        "function u(x) {return 1;}" +
        "function g(x) {" +
        "return (u(x)) && 1;}");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeInferenceWithCast5
  public void testTypeInferenceWithCast5() throws Exception {
    testTypes(
        " function foo(x) {}" +
        " function bar(y) {" +
        "   y.length;" +
        "  foo(y.length);" +
        "}",
        "actual parameter 1 of foo does not match formal parameter\n" +
        "found   : string\n" +
        "required: number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeInferenceWithClosure1
  public void testTypeInferenceWithClosure1() throws Exception {
    testTypes(
        "" +
        "function f() {" +
        "   var x = null;" +
        "  function g() { x = 'y'; } g(); " +
        "  return x == null;" +
        "}");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeInferenceWithClosure2
  public void testTypeInferenceWithClosure2() throws Exception {
    testTypes(
        "" +
        "function f() {" +
        "   var x = null;" +
        "  function g() { x = 'y'; } g(); " +
        "  return x === 3;" +
        "}",
        "condition always evaluates to the same value\n" +
        "left : (null|string|undefined)\n" +
        "right: number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testForwardPropertyReference
  public void testForwardPropertyReference() throws Exception {
    testTypes(" var Foo = function() { this.init(); };" +
        "" +
        "Foo.prototype.getString = function() {" +
        "  return this.number_;" +
        "};" +
        "Foo.prototype.init = function() {" +
        "  " +
        "  this.number_ = 3;" +
        "};",
        "inconsistent return type\n" +
        "found   : number\n" +
        "required: string");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testNoForwardTypeDeclaration
  public void testNoForwardTypeDeclaration() throws Exception {
    testTypes(
        " function f(x) {}",
        "Bad type annotation. Unknown type MyType");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testNoForwardTypeDeclarationAndNoBraces
  public void testNoForwardTypeDeclarationAndNoBraces() throws Exception {
    testTypes(" function f() {}");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testForwardTypeDeclaration1
  public void testForwardTypeDeclaration1() throws Exception {
    testClosureTypes(
        
        "goog.addDependency();" +
        "goog.addDependency('y', [goog]);" +

        "goog.addDependency('zzz.js', ['MyType'], []);" +
        "" +
        "function f(x) { return 3; }", null);
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testForwardTypeDeclaration2
  public void testForwardTypeDeclaration2() throws Exception {
    String f = "goog.addDependency('zzz.js', ['MyType'], []);" +
        " function f(x) { }";
    testClosureTypes(f, null);
    testClosureTypes(f + "f(3);",
        "actual parameter 1 of f does not match formal parameter\n" +
        "found   : number\n" +
        "required: (MyType|null|undefined)");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testForwardTypeDeclaration3
  public void testForwardTypeDeclaration3() throws Exception {
    testClosureTypes(
        "goog.addDependency('zzz.js', ['MyType'], []);" +
        " function f(x) { return x; }" +
        " var MyType = function() {};" +
        "f(3);",
        "actual parameter 1 of f does not match formal parameter\n" +
        "found   : number\n" +
        "required: (MyType|null|undefined)");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testDuplicateTypeDef
  public void testDuplicateTypeDef() throws Exception {
    testTypes(
        "var goog = {};" +
        " goog.Bar = function() {};" +
        " goog.Bar;",
        "variable goog.Bar redefined with type None, " +
        "original definition at [testcode]:1 " +
        "with type function (new:goog.Bar): undefined");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeDef1
  public void testTypeDef1() throws Exception {
    testTypes(
        "var goog = {};" +
        " goog.Bar;" +
        " function f(x) {}" +
        "f(3);");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeDef2
  public void testTypeDef2() throws Exception {
    testTypes(
        "var goog = {};" +
        " goog.Bar;" +
        " function f(x) {}" +
        "f('3');",
        "actual parameter 1 of f does not match formal parameter\n" +
        "found   : string\n" +
        "required: number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeDef3
  public void testTypeDef3() throws Exception {
    testTypes(
        "var goog = {};" +
        " var Bar;" +
        " function f(x) {}" +
        "f('3');",
        "actual parameter 1 of f does not match formal parameter\n" +
        "found   : string\n" +
        "required: number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testCircularTypeDef
  public void testCircularTypeDef() throws Exception {
    testTypes(
        "var goog = {};" +
        " goog.Bar;" +
        " function f(x) {}" +
        "f(3); f([3]); f([[3]]);");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testGetTypedPercent1
  public void testGetTypedPercent1() throws Exception {
    String js = "var id = function(x) { return x; }\n" +
                "var id2 = function(x) { return id(x); }";
    assertEquals(50.0, getTypedPercent(js), 0.1);
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testGetTypedPercent2
  public void testGetTypedPercent2() throws Exception {
    String js = "var x = {}; x.y = 1;";
    assertEquals(100.0, getTypedPercent(js), 0.1);
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testGetTypedPercent3
  public void testGetTypedPercent3() throws Exception {
    String js = "var f = function(x) { x.a = x.b; }";
    assertEquals(50.0, getTypedPercent(js), 0.1);
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testGetTypedPercent4
  public void testGetTypedPercent4() throws Exception {
    String js = "var n = {};\n  n.T = function() {};\n" +
        " var x = new n.T();";
    assertEquals(100.0, getTypedPercent(js), 0.1);
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testPrototypePropertyReference
  public void testPrototypePropertyReference() throws Exception {
    TypeCheckResult p = parseAndTypeCheckWithScope(""
        + "\n"
        + "function Foo() {}\n"
        + "\n"
        + "Foo.prototype.bar = function(a){};\n"
        + "\n"
        + "function baz(f) {\n"
        + "  Foo.prototype.bar.call(f, 3);\n"
        + "}");
    assertEquals(0, compiler.getErrorCount());
    assertEquals(0, compiler.getWarningCount());

    assertTrue(p.scope.getVar("Foo").getType() instanceof FunctionType);
    FunctionType fooType = (FunctionType) p.scope.getVar("Foo").getType();
    assertEquals("function (this:Foo, number): undefined",
                 fooType.getPrototype().getPropertyType("bar").toString());
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testResolvingNamedTypes
  public void testResolvingNamedTypes() throws Exception {
    String js = ""
        + "\n"
        + "var Foo = function() {}\n"
        + "\n"
        + "Foo.prototype.foo = function(a) {\n"
        + "  return this.baz().toString();\n"
        + "};\n"
        + "\n"
        + "Foo.prototype.baz = function() { return new Baz(); };\n"
        + "\n"
        + "var Bar = function() {};"
        + "\n"
        + "var Baz = function() {};";
    assertEquals(100.0, getTypedPercent(js), 0.1);
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testMissingProperty1
  public void testMissingProperty1() throws Exception {
    testTypes(
        " function Foo() {}" +
        "Foo.prototype.bar = function() { return this.a; };" +
        "Foo.prototype.baz = function() { this.a = 3; };");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testMissingProperty2
  public void testMissingProperty2() throws Exception {
    testTypes(
        " function Foo() {}" +
        "Foo.prototype.bar = function() { return this.a; };" +
        "Foo.prototype.baz = function() { this.b = 3; };",
        "Property a never defined on Foo");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testMissingProperty3
  public void testMissingProperty3() throws Exception {
    testTypes(
        " function Foo() {}" +
        "Foo.prototype.bar = function() { return this.a; };" +
        "(new Foo).a = 3;");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testMissingProperty4
  public void testMissingProperty4() throws Exception {
    testTypes(
        " function Foo() {}" +
        "Foo.prototype.bar = function() { return this.a; };" +
        "(new Foo).b = 3;",
        "Property a never defined on Foo");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testMissingProperty5
  public void testMissingProperty5() throws Exception {
    testTypes(
        " function Foo() {}" +
        "Foo.prototype.bar = function() { return this.a; };" +
        " function Bar() { this.a = 3; };",
        "Property a never defined on Foo");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testMissingProperty6
  public void testMissingProperty6() throws Exception {
    testTypes(
        " function Foo() {}" +
        "Foo.prototype.bar = function() { return this.a; };" +
        " " +
        "function Bar() { this.a = 3; };");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testMissingProperty7
  public void testMissingProperty7() throws Exception {
    testTypes(
        "" +
        "function foo(obj) { return obj.impossible; }",
        "Property impossible never defined on Object");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testMissingProperty8
  public void testMissingProperty8() throws Exception {
    testTypes(
        "" +
        "function foo(obj) { return typeof obj.impossible; }");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testMissingProperty9
  public void testMissingProperty9() throws Exception {
    testTypes(
        "" +
        "function foo(obj) { if (obj.impossible) { return true; } }");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testMissingProperty10
  public void testMissingProperty10() throws Exception {
    testTypes(
        "" +
        "function foo(obj) { while (obj.impossible) { return true; } }");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testMissingProperty11
  public void testMissingProperty11() throws Exception {
    testTypes(
        "" +
        "function foo(obj) { for (;obj.impossible;) { return true; } }");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testMissingProperty12
  public void testMissingProperty12() throws Exception {
    testTypes(
        "" +
        "function foo(obj) { do { } while (obj.impossible); }");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testMissingProperty13
  public void testMissingProperty13() throws Exception {
    testTypes(
        "var goog = {}; goog.isDef = function(x) { return false; };" +
        "" +
        "function foo(obj) { return goog.isDef(obj.impossible); }");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testMissingProperty14
  public void testMissingProperty14() throws Exception {
    testTypes(
        "var goog = {}; goog.isDef = function(x) { return false; };" +
        "" +
        "function foo(obj) { return goog.isNull(obj.impossible); }",
        "Property isNull never defined on goog");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testMissingProperty15
  public void testMissingProperty15() throws Exception {
    testTypes(
        "" +
        "function f(x) { if (x.foo) { x.foo(); } }");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testMissingProperty16
  public void testMissingProperty16() throws Exception {
    testTypes(
        "" +
        "function f(x) { x.foo(); if (x.foo) {} }",
        "Property foo never defined on Object");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testMissingProperty17
  public void testMissingProperty17() throws Exception {
    testTypes(
        "" +
        "function f(x) { if (typeof x.foo == 'function') { x.foo(); } }");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testMissingProperty18
  public void testMissingProperty18() throws Exception {
    testTypes(
        "" +
        "function f(x) { if (x.foo instanceof Function) { x.foo(); } }");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testMissingProperty19
  public void testMissingProperty19() throws Exception {
    testTypes(
        "" +
        "function f(x) { if (x.bar) { if (x.foo) {} } else { x.foo(); } }",
        "Property foo never defined on Object");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testMissingProperty20
  public void testMissingProperty20() throws Exception {
    
    
    
    
    
    
    
    
    testTypes(
        "" +
        "function f(x) { if (x.foo) { } else { x.foo(); } }");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testMissingProperty21
  public void testMissingProperty21() throws Exception {
    testTypes(
        "" +
        "function f(x) { x.foo && x.foo(); }");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testMissingProperty22
  public void testMissingProperty22() throws Exception {
    testTypes(
        "" +
        "function f(x) { return x.foo ? x.foo() : true; }");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testMissingProperty23
  public void testMissingProperty23() throws Exception {
    testTypes(
        "function f(x) { x.impossible(); }",
        "Property impossible never defined on x");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testMissingProperty24
  public void testMissingProperty24() throws Exception {
    testClosureTypes(
        "goog.addDependency('zzz.js', ['MissingType'], []);" +
        "" +
        "function f(x) { x.impossible(); }", null);
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testMissingProperty25
  public void testMissingProperty25() throws Exception {
    testTypes(
        " var Foo = function() {};" +
        "Foo.prototype.bar = function() {};" +
        " var FooAlias = Foo;" +
        "(new FooAlias()).bar();");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testMissingProperty26
  public void testMissingProperty26() throws Exception {
    testTypes(
        " var Foo = function() {};" +
        " var FooAlias = Foo;" +
        "FooAlias.prototype.bar = function() {};" +
        "(new Foo()).bar();");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testMissingProperty27
  public void testMissingProperty27() throws Exception {
    testClosureTypes(
        "goog.addDependency('zzz.js', ['MissingType'], []);" +
        "" +
        "function f(x) {" +
        "  for (var parent = x; parent; parent = parent.getParent()) {}" +
        "}", null);
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testMissingProperty28
  public void testMissingProperty28() throws Exception {
    testTypes(
        "function f(obj) {" +
        "   obj.foo;" +
        "  return obj.foo;" +
        "}");
    testTypes(
        "function f(obj) {" +
        "   obj.foo;" +
        "  return obj.foox;" +
        "}",
        "Property foox never defined on obj");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testMissingProperty29
  public void testMissingProperty29() throws Exception {
    
    testTypes(
        
        " var Foo;" +
        "Foo.prototype.opera;" +
        "Foo.prototype.opera.postError;",
        "",
        null,
        false);
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testDeclaredNativeTypeEquality
  public void testDeclaredNativeTypeEquality() throws Exception {
    Node n = parseAndTypeCheck(" function Object() {};");
    assertEquals(registry.getNativeType(JSTypeNative.OBJECT_FUNCTION_TYPE),
                 n.getFirstChild().getJSType());
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testUndefinedVar
  public void testUndefinedVar() throws Exception {
    Node n = parseAndTypeCheck("var undefined;");
    assertEquals(registry.getNativeType(JSTypeNative.VOID_TYPE),
                 n.getFirstChild().getFirstChild().getJSType());
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testFlowScopeBug1
  public void testFlowScopeBug1() throws Exception {
    Node n = parseAndTypeCheck("\n"
        + "function f(a, b) {\n"
        + ""
        + "var i = 0;"
        + "for (; (i + a) < b; ++i) {}}");

    
    assertEquals(registry.getNativeType(JSTypeNative.NUMBER_TYPE),
        n.getFirstChild().getLastChild().getLastChild().getFirstChild()
        .getNext().getFirstChild().getJSType());
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testFlowScopeBug2
  public void testFlowScopeBug2() throws Exception {
    Node n = parseAndTypeCheck(" function Foo() {};\n"
        + "Foo.prototype.hi = false;"
        + "function foo(a, b) {\n"
        + "  "
        + "  var arr;"
        + "  "
        + "  var iter;"
        + "  for (iter = 0; iter < arr.length; ++ iter) {"
        + "    "
        + "    var afoo = arr[iter];"
        + "    afoo;"
        + "  }"
        + "}");

    
    assertEquals(registry.createOptionalType(
            registry.createNullableType(registry.getType("Foo"))),
        n.getLastChild().getLastChild().getLastChild().getLastChild()
        .getLastChild().getLastChild().getJSType());
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testAddSingletonGetter
  public void testAddSingletonGetter() {
    Node n = parseAndTypeCheck(
        " function Foo() {};\n" +
        "goog.addSingletonGetter(Foo);");
    ObjectType o = (ObjectType) n.getFirstChild().getJSType();
    assertEquals("function (): Foo",
        o.getPropertyType("getInstance").toString());
    assertEquals("Foo", o.getPropertyType("instance_").toString());
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeCheckStandaloneAST
  public void testTypeCheckStandaloneAST() throws Exception {
    Node n = compiler.parseTestCode("function Foo() { }");
    typeCheck(n);
    TypedScopeCreator scopeCreator = new TypedScopeCreator(compiler);
    Scope topScope = scopeCreator.createScope(n, null);

    Node second = compiler.parseTestCode("new Foo");

    Node externs = new Node(Token.BLOCK);
    Node externAndJsRoot = new Node(Token.BLOCK, externs, second);
    externAndJsRoot.setIsSyntheticBlock(true);

    new TypeCheck(
        compiler,
        new SemanticReverseAbstractInterpreter(
            compiler.getCodingConvention(), registry),
        registry, topScope, scopeCreator, CheckLevel.WARNING, CheckLevel.OFF)
        .process(null, second);

    assertEquals(1, compiler.getWarningCount());
    assertEquals("cannot instantiate non-constructor",
        compiler.getWarnings()[0].description);
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testBadTemplateType1
  public void testBadTemplateType1() throws Exception {
    testTypes(
        "\n" +
        "function f(x, y, z) {}\n" +
        "f(this, this, function() { this });",
        FunctionTypeBuilder.TEMPLATE_TYPE_DUPLICATED.format());
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testBadTemplateType2
  public void testBadTemplateType2() throws Exception {
    testTypes(
        "\n" +
        "function f(x, y) {}\n" +
        "f(0, function() {});",
        TypeInference.TEMPLATE_TYPE_NOT_OBJECT_TYPE.format("number"));
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testBadTemplateType3
  public void testBadTemplateType3() throws Exception {
    testTypes(
        "\n" +
        "function f(x) {}\n" +
        "f(this);",
        TypeInference.TEMPLATE_TYPE_OF_THIS_EXPECTED.format());
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testBadTemplateType4
  public void testBadTemplateType4() throws Exception {
    testTypes(
        "\n" +
        "function f() {}\n" +
        "f();",
        FunctionTypeBuilder.TEMPLATE_TYPE_EXPECTED.format());
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testBadTemplateType5
  public void testBadTemplateType5() throws Exception {
    testTypes(
        "\n" +
        "function f() {}\n" +
        "f();",
        FunctionTypeBuilder.TEMPLATE_TYPE_EXPECTED.format());
  }

// com.google.javascript.jscomp.MarkNoSideEffectCallsTest::testFunctionAnnotation
  public void testFunctionAnnotation() throws Exception {
    testMarkCalls("function f(){}", "f()",
                  ImmutableList.of("f"));
    testMarkCalls("var f = function(){};", "f()",
                  ImmutableList.of("f"));
    testMarkCalls("var f = function(){};", "f()",
                  ImmutableList.of("f"));
    testMarkCalls("var f; f = function(){};", "f()",
                  ImmutableList.of("f"));
    testMarkCalls("var f;  f = function(){};", "f()",
                  ImmutableList.of("f"));

    
    testMarkCalls("function f(){}", Collections.<String>emptyList());
    testMarkCalls("function f(){} f()", Collections.<String>emptyList());

    
    testMarkCalls("var f = " +
                  "function(){};",
                  "f()",
                  ImmutableList.of("f"));
  }

// com.google.javascript.jscomp.MarkNoSideEffectCallsTest::testNamespaceAnnotation
  public void testNamespaceAnnotation() throws Exception {
    testMarkCalls("var o = {}; o.f = function(){};",
        "o.f()", ImmutableList.of("o.f"));
    testMarkCalls("var o = {}; o.f = function(){};",
        "o.f()", ImmutableList.of("o.f"));
    testMarkCalls("var o = {}; o.f = function(){}; o.f()",
                  Collections.<String>emptyList());
  }

// com.google.javascript.jscomp.MarkNoSideEffectCallsTest::testConstructorAnnotation
  public void testConstructorAnnotation() throws Exception {
    testMarkCalls("function c(){};", "new c",
                  ImmutableList.of("c"));
    testMarkCalls("var c = function(){};", "new c",
                  ImmutableList.of("c"));
    testMarkCalls("var c = function(){};", "new c",
                  ImmutableList.of("c"));
    testMarkCalls("function c(){}; new c", Collections.<String>emptyList());
  }

// com.google.javascript.jscomp.MarkNoSideEffectCallsTest::testMultipleDefinition
  public void testMultipleDefinition() throws Exception {
    testMarkCalls("function f(){}" +
                  "f = function(){};",
                  "f()",
                  ImmutableList.of("f"));
    testMarkCalls("function f(){}" +
                  "f = function(){};",
                  "f()",
                  Collections.<String>emptyList());
    testMarkCalls("function f(){}",
                  "f = function(){};" +
                  "f()",
                  Collections.<String>emptyList());
  }

// com.google.javascript.jscomp.MarkNoSideEffectCallsTest::testAssignNoFunction
  public void testAssignNoFunction() throws Exception {
    testMarkCalls("function f(){}", "f = 1; f()",
                  ImmutableList.of("f"));
    testMarkCalls("function f(){}", "f = 1 || 2; f()",
                  Collections.<String>emptyList());
  }

// com.google.javascript.jscomp.MarkNoSideEffectCallsTest::testPrototype
  public void testPrototype() throws Exception {
    testMarkCalls("function c(){};" +
                  "c.prototype.g = function(){};",
                  "var o = new c; o.g()",
                  ImmutableList.of("o.g"));
    testMarkCalls("function c(){};" +
                  "c.prototype.g = function(){};",
                  "function f(){}" +
                  "var o = new c; o.g(); f()",
                  ImmutableList.of("o.g"));

    
    testMarkCalls("function c(){};" +
                  "c.prototype.g = function(){};",
                  "var o = new c;" +
                  "o.g = function(){};" +
                  "o.g()",
                  ImmutableList.<String>of());
    
    testMarkCalls("function c1(){};" +
                  "c1.prototype.f = function(){};" +
                  "function c2(){};" +
                  "c2.prototype.f = function(){};",
                  "var o = new c1;" +
                  "o.f()",
                  ImmutableList.of("o.f"));

    
    testMarkCalls("function c1(){};" +
                  "c1.prototype.f = function(){};",
                  "function c2(){};" +
                  "c2.prototype.f = function(){};" +
                  "var o = new c1;" +
                  "o.f()",
                  Collections.<String>emptyList());
  }

// com.google.javascript.jscomp.MarkNoSideEffectCallsTest::testAnnotationInExterns
  public void testAnnotationInExterns() throws Exception {
    testMarkCalls("externSef1()", Collections.<String>emptyList());
    testMarkCalls("externSef2()", Collections.<String>emptyList());
    testMarkCalls("externNsef1()", ImmutableList.of("externNsef1"));
    testMarkCalls("externNsef2()", ImmutableList.of("externNsef2"));
    testMarkCalls("externNsef3()", ImmutableList.of("externNsef3"));
  }

// com.google.javascript.jscomp.MarkNoSideEffectCallsTest::testNamespaceAnnotationInExterns
  public void testNamespaceAnnotationInExterns() throws Exception {
    testMarkCalls("externObj.sef1()", Collections.<String>emptyList());
    testMarkCalls("externObj.sef2()", Collections.<String>emptyList());
    testMarkCalls("externObj.nsef1()", ImmutableList.of("externObj.nsef1"));
    testMarkCalls("externObj.nsef2()", ImmutableList.of("externObj.nsef2"));

    testMarkCalls("externObj.nsef3()", ImmutableList.of("externObj.nsef3"));
  }

// com.google.javascript.jscomp.MarkNoSideEffectCallsTest::testOverrideDefinitionInSource
  public void testOverrideDefinitionInSource() throws Exception {
    
    testMarkCalls("var obj = {}; obj.sef1 = function(){}; obj.sef1()",
                  Collections.<String>emptyList());

    
    testMarkCalls("var obj = {};" +
                  "obj.sef1 = function(){};",
                  "obj.sef1()",
                  Collections.<String>emptyList());

    
    testMarkCalls("var obj = {}; obj.nsef1 = function(){}; obj.nsef1()",
                  Collections.<String>emptyList());

    
    testMarkCalls("var obj = {};" +
                  "obj.nsef1 = function(){};",
                  "obj.nsef1()",
                  ImmutableList.of("obj.nsef1"));
  }

// com.google.javascript.jscomp.MarkNoSideEffectCallsTest::testApply1
  public void testApply1() throws Exception {
    testMarkCalls(" var f = function() {}",
                  "f.apply()",
                  ImmutableList.of("f.apply"));
  }

// com.google.javascript.jscomp.MarkNoSideEffectCallsTest::testApply2
  public void testApply2() throws Exception {
    testMarkCalls("var f = function() {}",
                  "f.apply()",
                  ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.MarkNoSideEffectCallsTest::testCall1
  public void testCall1() throws Exception {
    testMarkCalls(" var f = function() {}",
                  "f.call()",
                  ImmutableList.of("f.call"));
  }

// com.google.javascript.jscomp.MarkNoSideEffectCallsTest::testCall2
  public void testCall2() throws Exception {
    testMarkCalls("var f = function() {}",
                  "f.call()",
                  ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.MarkNoSideEffectCallsTest::testInvalidAnnotation1
  public void testInvalidAnnotation1() throws Exception {
    test(" function foo() {}",
         null, INVALID_NO_SIDE_EFFECT_ANNOTATION);
  }

// com.google.javascript.jscomp.MarkNoSideEffectCallsTest::testInvalidAnnotation2
  public void testInvalidAnnotation2() throws Exception {
    test("var f =  function() {}",
         null, INVALID_NO_SIDE_EFFECT_ANNOTATION);
  }

// com.google.javascript.jscomp.MarkNoSideEffectCallsTest::testInvalidAnnotation3
  public void testInvalidAnnotation3() throws Exception {
    test(" var f = function() {}",
         null, INVALID_NO_SIDE_EFFECT_ANNOTATION);
  }

// com.google.javascript.jscomp.MarkNoSideEffectCallsTest::testInvalidAnnotation4
  public void testInvalidAnnotation4() throws Exception {
    test("var f = function() {};" +
         " f.x = function() {}",
         null, INVALID_NO_SIDE_EFFECT_ANNOTATION);
  }

// com.google.javascript.jscomp.MarkNoSideEffectCallsTest::testInvalidAnnotation5
  public void testInvalidAnnotation5() throws Exception {
    test("var f = function() {};" +
         "f.x =  function() {}",
         null, INVALID_NO_SIDE_EFFECT_ANNOTATION);
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRemoveVarDeclartion1
  public void testRemoveVarDeclartion1() {
    test("var foo = 3;", "");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRemoveVarDeclartion2
  public void testRemoveVarDeclartion2() {
    test("var foo = 3, bar = 4; externfoo = foo;",
         "var foo = 3; externfoo = foo;");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRemoveVarDeclartion3
  public void testRemoveVarDeclartion3() {
    test("var a = f(), b = 1, c = 2; b; c", "f();var b = 1, c = 2; b; c");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRemoveVarDeclartion4
  public void testRemoveVarDeclartion4() {
    test("var a = 0, b = f(), c = 2; a; c", "var a = 0;f();var c = 2; a; c");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRemoveVarDeclartion5
  public void testRemoveVarDeclartion5() {
    test("var a = 0, b = 1, c = f(); a; b", "var a = 0, b = 1; f(); a; b");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRemoveVarDeclartion6
  public void testRemoveVarDeclartion6() {
    test("var a = 0, b = a = 1; a", "var a = 0; a = 1; a");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRemoveVarDeclartion7
  public void testRemoveVarDeclartion7() {
    test("var a = 0, b = a = 1", "");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRemoveVarDeclartion8
  public void testRemoveVarDeclartion8() {
    test("var a;var b = 0, c = a = b = 1", "");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRemoveFunction
  public void testRemoveFunction() {
    test("var foo = {}; foo.bar = function() {};", "");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testReferredToByWindow
  public void testReferredToByWindow() {
    testSame("var foo = {}; foo.bar = function() {}; window['fooz'] = foo.bar");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testExtern
  public void testExtern() {
    testSame("externfoo = 5");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRemoveNamedFunction
  public void testRemoveNamedFunction() {
    test("function foo(){}", "");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRemoveRecursiveFunction1
  public void testRemoveRecursiveFunction1() {
    test("function f(){f()}", "");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRemoveRecursiveFunction2
  public void testRemoveRecursiveFunction2() {
    test("var f = function (){f()}", "");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRemoveRecursiveFunction3
  public void testRemoveRecursiveFunction3() {
    test("var f;f = function (){f()}", "");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRemoveRecursiveFunction4
  public void testRemoveRecursiveFunction4() {
    
    testSame("f = function (){f()}");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRemoveRecursiveFunction5
  public void testRemoveRecursiveFunction5() {
    test("function g(){f()}function f(){g()}", "");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRemoveRecursiveFunction6
  public void testRemoveRecursiveFunction6() {
    test("var f=function(){g()};function g(){f()}", "");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRemoveRecursiveFunction7
  public void testRemoveRecursiveFunction7() {
    test("var g = function(){f()};var f = function(){g()}", "");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRemoveRecursiveFunction8
  public void testRemoveRecursiveFunction8() {
    test("var o = {};o.f = function(){o.f()}", "");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRemoveRecursiveFunction9
  public void testRemoveRecursiveFunction9() {
    testSame("var o = {};o.f = function(){o.f()};o.f()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testSideEffectClassification1
  public void testSideEffectClassification1() {
    test("foo();", "foo();");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testSideEffectClassification2
  public void testSideEffectClassification2() {
    test("var a = foo();", "foo();");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testSideEffectClassification3
  public void testSideEffectClassification3() {
    testSame("var a = foo();window['b']=a;");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testSideEffectClassification4
  public void testSideEffectClassification4() {
    testSame("function sef(){} sef();");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testSideEffectClassification5
  public void testSideEffectClassification5() {
    testSame("function nsef(){} var a = nsef();window['b']=a;");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testSideEffectClassification6
  public void testSideEffectClassification6() {
    test("function sef(){} sef();", "function sef(){} sef();");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testSideEffectClassification7
  public void testSideEffectClassification7() {
    testSame("function sef(){} var a = sef();window['b']=a;");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testNoSideEffectAnnotation1
  public void testNoSideEffectAnnotation1() {
    test("function f(){} var a = f();",
         "function f(){} f()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testNoSideEffectAnnotation2
  public void testNoSideEffectAnnotation2() {
    test("function f(){}", "var a = f();",
         "", null, null);
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testNoSideEffectAnnotation3
  public void testNoSideEffectAnnotation3() {
    test("var f = function(){}; var a = f();",
         "var f = function(){}; f();");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testNoSideEffectAnnotation4
  public void testNoSideEffectAnnotation4() {
    test("var f = function(){};", "var a = f();",
         "", null, null);
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testNoSideEffectAnnotation5
  public void testNoSideEffectAnnotation5() {
    test("var f; f = function(){}; var a = f();",
         "var f; f = function(){}; f();");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testNoSideEffectAnnotation6
  public void testNoSideEffectAnnotation6() {
    test("var f; f = function(){};", "var a = f();",
         "", null, null);
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testNoSideEffectAnnotation7
  public void testNoSideEffectAnnotation7() {
    test("var f;" +
         "f = function(){};",
         "f = function(){};" +
         "var a = f();",
         "f = function(){}; f();", null, null);
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testNoSideEffectAnnotation8
  public void testNoSideEffectAnnotation8() {
    test("var f;" +
         "f = function(){};" +
         "f = function(){};",
         "var a = f();",
         "f();", null, null);
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testNoSideEffectAnnotation9
  public void testNoSideEffectAnnotation9() {
    test("var f;" +
         "f = function(){};" +
         "f = function(){};",
         "var a = f();",
         "", null, null);

    test("var f; f = function(){};", "var a = f();",
         "", null, null);
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testNoSideEffectAnnotation10
  public void testNoSideEffectAnnotation10() {
    test("var o = {}; o.f = function(){}; var a = o.f();",
         "var o = {}; o.f = function(){}; o.f();");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testNoSideEffectAnnotation11
  public void testNoSideEffectAnnotation11() {
    test("var o = {}; o.f = function(){};",
         "var a = o.f();", "", null, null);
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testNoSideEffectAnnotation12
  public void testNoSideEffectAnnotation12() {
    test("function c(){} var a = new c",
         "function c(){} new c");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testNoSideEffectAnnotation13
  public void testNoSideEffectAnnotation13() {
    test("function c(){}", "var a = new c",
         "", null, null);
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testNoSideEffectAnnotation14
  public void testNoSideEffectAnnotation14() {
    String common = "function c(){};" +
        "c.prototype.f = function(){};";
    test(common, "var o = new c; var a = o.f()", "new c", null, null);
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testNoSideEffectAnnotation15
  public void testNoSideEffectAnnotation15() {
    test("function c(){}; c.prototype.f = function(){}; var a = (new c).f()",
         "function c(){}; c.prototype.f = function(){}; (new c).f()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testNoSideEffectAnnotation16
  public void testNoSideEffectAnnotation16() {
    test("function c(){}" +
         "c.prototype.f = function(){};",
         "var a = (new c).f()",
         "",
         null, null);
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testFunctionPrototype
  public void testFunctionPrototype() {
    testSame("var a = 5; Function.prototype.foo = function() {return a;}");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testTopLevelClass1
  public void testTopLevelClass1() {
    test("var Point = function() {}; Point.prototype.foo = function() {}", "");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testTopLevelClass2
  public void testTopLevelClass2() {
    testSame("var Point = {}; Point.prototype.foo = function() {};" +
             "externfoo = new Point()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testTopLevelClass3
  public void testTopLevelClass3() {
    test("function Point() {this.me_ = Point}", "");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testTopLevelClass4
  public void testTopLevelClass4() {
    test("function f(){} function A(){} A.prototype = {x: function() {}}; f();",
         "function f(){} f();");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testTopLevelClass5
  public void testTopLevelClass5() {
    testSame("function f(){} function A(){}" +
             "A.prototype = {x: function() { f(); }}; new A();");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testTopLevelClass6
  public void testTopLevelClass6() {
    testSame("function f(){} function A(){}" +
             "A.prototype = {x: function() { f(); }}; new A().x();");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testTopLevelClass7
  public void testTopLevelClass7() {
    test("A.prototype.foo = function(){}; function A() {}", "");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testNamespacedClass1
  public void testNamespacedClass1() {
    test("var foo = {};foo.bar = {};foo.bar.prototype.baz = {}", "");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testNamespacedClass2
  public void testNamespacedClass2() {
    testSame("var foo = {};foo.bar = {};foo.bar.prototype.baz = {};" +
             "window.z = new foo.bar()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testNamespacedClass3
  public void testNamespacedClass3() {
    test("var a = {}; a.b = function() {}; a.b.prototype = {x: function() {}};",
         "");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testNamespacedClass4
  public void testNamespacedClass4() {
    testSame("function f(){} var a = {}; a.b = function() {};" +
             "a.b.prototype = {x: function() { f(); }}; new a.b();");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testNamespacedClass5
  public void testNamespacedClass5() {
    testSame("function f(){} var a = {}; a.b = function() {};" +
             "a.b.prototype = {x: function() { f(); }}; new a.b().x();");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testAssignmentToThisPrototype
  public void testAssignmentToThisPrototype() {
    testSame("Function.prototype.inherits = function(parentCtor) {" +
             "  function tempCtor() {};" +
             "  tempCtor.prototype = parentCtor.prototype;" +
             "  this.superClass_ = parentCtor.prototype;" +
             "  this.prototype = new tempCtor();" +
             "  this.prototype.constructor = this;" +
             "};");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testAssignmentToCallResultPrototype
  public void testAssignmentToCallResultPrototype() {
    testSame("function f() { return function(){}; } f().prototype = {};");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testAssignmentToExternPrototype
  public void testAssignmentToExternPrototype() {
    testSame("externfoo.prototype = {};");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testAssignmentToUnknownPrototype
  public void testAssignmentToUnknownPrototype() {
    testSame(
        " var window;" +
        "window['a'].prototype = {};");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testBug2099540
  public void testBug2099540() {
    testSame(
        " var document;\n" +
        " var window;\n" +
        "var klass;\n" +
        "window[klass].prototype = " +
            "document.createElement(tagName)['__proto__'];");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testOtherGlobal
  public void testOtherGlobal() {
    testSame("goog.global.foo = bar(); function bar(){}");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testExternName1
  public void testExternName1() {
    testSame("top.z = bar(); function bar(){}");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testExternName2
  public void testExternName2() {
    testSame("top['z'] = bar(); function bar(){}");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testInherits1
  public void testInherits1() {
    test("var a = {}; var b = {}; b.inherits(a)", "");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testInherits2
  public void testInherits2() {
    test("var a = {}; var b = {}; var goog = {}; goog.inherits(b, a)", "");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testInherits3
  public void testInherits3() {
    testSame("var a = {}; this.b = {}; b.inherits(a);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testInherits4
  public void testInherits4() {
    testSame("var a = {}; this.b = {}; var goog = {}; goog.inherits(b, a);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testInherits5
  public void testInherits5() {
    test("this.a = {}; var b = {}; b.inherits(a);",
         "this.a = {}");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testInherits6
  public void testInherits6() {
    test("this.a = {}; var b = {}; var goog = {}; goog.inherits(b, a);",
         "this.a = {}");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testInherits7
  public void testInherits7() {
    testSame("var a = {}; this.b = {}; var goog = {};" +
        " goog.inherits = function() {}; goog.inherits(b, a);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testInherits8
  public void testInherits8() {
    
    
    test("this.a = {}; var b = {}; var c = b.inherits(a);", "this.a = {};");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testMixin1
  public void testMixin1() {
    testSame("var goog = {}; goog.mixin = function() {};" +
             "Function.prototype.mixin = function(base) {" +
             "  goog.mixin(this.prototype, base); " +
             "};");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testMixin2
  public void testMixin2() {
    testSame("var a = {}; this.b = {}; var goog = {};" +
        " goog.mixin = function() {}; goog.mixin(b.prototype, a.prototype);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testMixin3
  public void testMixin3() {
    test("this.a = {}; var b = {}; var goog = {};" +
         " goog.mixin = function() {}; goog.mixin(b.prototype, a.prototype);",
         "this.a = {};");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testMixin4
  public void testMixin4() {
    testSame("this.a = {}; var b = {}; var goog = {};" +
             "goog.mixin = function() {};" +
             "goog.mixin(b.prototype, a.prototype);" +
             "new b()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testMixin5
  public void testMixin5() {
    test("this.a = {}; var b = {}; var c = {}; var goog = {};" +
         "goog.mixin = function() {};" +
         "goog.mixin(b.prototype, a.prototype);" +
         "goog.mixin(c.prototype, a.prototype);" +
         "new b()",
         "this.a = {}; var b = {}; var goog = {};" +
         "goog.mixin = function() {};" +
         "goog.mixin(b.prototype, a.prototype);" +
         "new b()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testMixin6
  public void testMixin6() {
    testSame("this.a = {}; var b = {}; var c = {}; var goog = {};" +
             "goog.mixin = function() {};" +
             "goog.mixin(c.prototype, a.prototype) + " +
             "goog.mixin(b.prototype, a.prototype);" +
             "new b()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testMixin7
  public void testMixin7() {
    test("this.a = {}; var b = {}; var c = {}; var goog = {};" +
         "goog.mixin = function() {};" +
         "var d = goog.mixin(c.prototype, a.prototype) + " +
         "goog.mixin(b.prototype, a.prototype);" +
         "new b()",
         "this.a = {}; var b = {}; var goog = {};" +
         "goog.mixin = function() {};" +
         "goog.mixin(b.prototype, a.prototype);" +
         "new b()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testConstants1
  public void testConstants1() {
    testSame("var bar = function(){}; var EXP_FOO = true; if (EXP_FOO) bar();");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testConstants2
  public void testConstants2() {
    test("var bar = function(){}; var EXP_FOO = true; var EXP_BAR = true;" +
         "if (EXP_FOO) bar();",
         "var bar = function(){}; var EXP_FOO = true; if (EXP_FOO) bar();");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testExpressions1
  public void testExpressions1() {
    test("var foo={}; foo.A='A'; foo.AB=foo.A+'B'; foo.ABC=foo.AB+'C'",
         "");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testExpressions2
  public void testExpressions2() {
    testSame("var foo={}; foo.A='A'; foo.AB=foo.A+'B'; this.ABC=foo.AB+'C'");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testExpressions3
  public void testExpressions3() {
    testSame("var foo = 2; window.bar(foo + 3)");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testSetCreatingReference
  public void testSetCreatingReference() {
    testSame("var foo; var bar = function(){foo=6;}; bar();");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testAnonymous1
  public void testAnonymous1() {
    testSame("function foo() {}; function bar() {}; foo(function() {bar()})");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testAnonymous2
  public void testAnonymous2() {
    test("var foo;(function(){foo=6;})()", "(function(){})()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testAnonymous3
  public void testAnonymous3() {
    testSame("var foo; (function(){ if(!foo)foo=6; })()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testAnonymous4
  public void testAnonymous4() {
    testSame("var foo; (function(){ foo=6; })(); externfoo=foo;");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testAnonymous5
  public void testAnonymous5() {
    testSame("var foo;" +
             "(function(){ foo=function(){ bar() }; function bar(){} })();" +
             "foo();");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testAnonymous6
  public void testAnonymous6() {
    testSame("function foo(){}" +
             "function bar(){}" +
             "foo(function(){externfoo = bar});");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testAnonymous7
  public void testAnonymous7() {
    testSame("var foo;" +
             "(function (){ function bar(){ externfoo = foo; } bar(); })();");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testAnonymous8
  public void testAnonymous8() {
    testSame("var foo;" +
             "(function (){ var g=function(){ externfoo = foo; }; g(); })();");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testAnonymous9
  public void testAnonymous9() {
    testSame("function foo(){}" +
             "function bar(){}" +
             "foo(function(){ function baz(){ externfoo = bar; } baz(); });");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testFunctions1
  public void testFunctions1() {
    testSame("var foo = null; function baz() {}" +
             "function bar() {foo=baz();} bar();");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testFunctions2
  public void testFunctions2() {
    testSame("var foo; foo = function() {var a = bar()};" +
             "var bar = function(){}; foo();");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testGetElem1
  public void testGetElem1() {
    testSame("var foo = {}; foo.bar = {}; foo.bar.baz = {a: 5, b: 10};" +
             "var fn = function() {window[foo.bar.baz.a] = 5;}; fn()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testGetElem2
  public void testGetElem2() {
    testSame("var foo = {}; foo.bar = {}; foo.bar.baz = {a: 5, b: 10};" +
             "var fn = function() {this[foo.bar.baz.a] = 5;}; fn()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testGetElem3
  public void testGetElem3() {
    testSame("var foo = {'i': 0, 'j': 1}; foo['k'] = 2; top.foo = foo;");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testIf1
  public void testIf1() {
    test("var foo = {};if(e)foo.bar=function(){};", "if(e);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testIf2
  public void testIf2() {
    test("var e = false;var foo = {};if(e)foo.bar=function(){};",
         "var e = false;if(e);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testIf3
  public void testIf3() {
    test("var e = false;var foo = {};if(e + 1)foo.bar=function(){};",
         "var e = false;if(e + 1);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testIf4
  public void testIf4() {
    test("var e = false, f;var foo = {};if(f=e)foo.bar=function(){};",
         "var e = false;if(e);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testIf5
  public void testIf5() {
    test("var e = false, f;var foo = {};if(f = e + 1)foo.bar=function(){};",
         "var e = false;if(e + 1);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testIfElse
  public void testIfElse() {
    test("var foo = {};if(e)foo.bar=function(){};else foo.bar=function(){};",
         "if(e);else;");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testWhile
  public void testWhile() {
    test("var foo = {};while(e)foo.bar=function(){};", "while(e);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testFor
  public void testFor() {
    test("var foo = {};for(e in x)foo.bar=function(){};", "for(e in x);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testDo
  public void testDo() {
    test("var cond = false;do {var a = 1} while (cond)",
         "var cond = false;do {} while (cond)");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testSetterInForStruct1
  public void testSetterInForStruct1() {
    test("var j = 0; for (var i = 1; i = 0; j++);",
         "var j = 0; for (; 0; j++);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testSetterInForStruct2
  public void testSetterInForStruct2() {
    test("var Class = function() {}; " +
         "for (var i = 1; Class.prototype.property_ = 0; i++);",
         "for (var i = 1; 0; i++);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testSetterInForStruct3
  public void testSetterInForStruct3() {
    test("var j = 0; for (var i = 1 + f() + g() + h(); i = 0; j++);",
         "var j = 0; f(); g(); h(); for (; 0; j++);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testSetterInForStruct4
  public void testSetterInForStruct4() {
    test("var i = 0;var j = 0; for (i = 1 + f() + g() + h(); i = 0; j++);",
         "var j = 0; f(); g(); h(); for (; 0; j++);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testSetterInForStruct5
  public void testSetterInForStruct5() {
    test("var i = 0, j = 0; for (i = f(), j = g(); 0;);",
         "for (f(), g(); 0;);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testSetterInForStruct6
  public void testSetterInForStruct6() {
    test("var i = 0, j = 0, k = 0; for (i = f(), j = g(), k = h(); i = 0;);",
         "for (f(), g(), h(); 0;);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testSetterInForStruct7
  public void testSetterInForStruct7() {
    test("var i = 0, j = 0, k = 0; for (i = 1, j = 2, k = 3; i = 0;);",
         "for (1, 2, 3; 0;);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testSetterInForStruct8
  public void testSetterInForStruct8() {
    test("var i = 0, j = 0, k = 0; for (i = 1, j = i, k = 2; i = 0;);",
         "var i = 0; for(i = 1, i , 2; i = 0;);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testSetterInForStruct9
  public void testSetterInForStruct9() {
    test("var Class = function() {}; " +
         "for (var i = 1; Class.property_ = 0; i++);",
         "for (var i = 1; 0; i++);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testSetterInForStruct10
  public void testSetterInForStruct10() {
    test("var Class = function() {}; " +
         "for (var i = 1; Class.property_ = 0; i = 2);",
         "for (; 0;);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testSetterInForStruct11
  public void testSetterInForStruct11() {
    test("var Class = function() {}; " +
         "for (;Class.property_ = 0;);",
         "for (;0;);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testSetterInForStruct12
  public void testSetterInForStruct12() {
    test("var a = 1; var Class = function() {}; " +
         "for (;Class.property_ = a;);",
         "var a = 1; for (; a;);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testSetterInForStruct13
  public void testSetterInForStruct13() {
    test("var a = 1; var Class = function() {}; " +
         "for (Class.property_ = a; 0 ;);",
         "for (; 0;);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testSetterInForStruct14
  public void testSetterInForStruct14() {
    test("var a = 1; var Class = function() {}; " +
         "for (; 0; Class.property_ = a);",
         "for (; 0;);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testSetterInForStruct15
  public void testSetterInForStruct15() {
    test("var Class = function() {}; " +
         "for (var i = 1; 0; Class.prototype.property_ = 0);",
         "for (; 0; 0);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testSetterInForStruct16
  public void testSetterInForStruct16() {
    test("var Class = function() {}; " +
         "for (var i = 1; i = 0; Class.prototype.property_ = 0);",
         "for (; 0; 0);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testSetterInForIn1
  public void testSetterInForIn1() {
    test("var foo = {}; var bar; for(e in bar = foo.a);",
         "var foo = {}; for(e in foo.a);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testSetterInForIn2
  public void testSetterInForIn2() {
    testSame("var foo = {}; var bar; for(e in bar = foo.a); bar");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testSetterInForIn3
  public void testSetterInForIn3() {
    
    
    test("var foo = {}; var bar; for(e in bar = foo.a); bar.b = 3",
         "var foo = {}; for(e in foo.a);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testSetterInForIn4
  public void testSetterInForIn4() {
    
    
    test("var foo = {}; var bar; for (e in bar = foo.a); bar.b = 3; foo.a",
         "var foo = {}; for (e in foo.a); foo.a");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testSetterInForIn5
  public void testSetterInForIn5() {
    
    
    test("var foo = {}; var bar; for (e in foo.a) { bar = e } bar.b = 3; foo.a",
         "var foo={};for(e in foo.a);foo.a");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testSetterInForIn6
  public void testSetterInForIn6() {
    testSame("var foo = {};for(e in foo);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testSetterInIfPredicate
  public void testSetterInIfPredicate() {
    
    testSame("var a = 1;" +
             "var Class = function() {}; " +
             "if (Class.property_ = a);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testSetterInWhilePredicate
  public void testSetterInWhilePredicate() {
    test("var a = 1;" +
         "var Class = function() {}; " +
         "while (Class.property_ = a);",
         "var a = 1; for (;a;) {}");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testSetterInDoWhilePredicate
  public void testSetterInDoWhilePredicate() {
    
    testSame("var a = 1;" +
             "var Class = function() {}; " +
             "do {} while(Class.property_ = a);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testSetterInSwitchInput
  public void testSetterInSwitchInput() {
    
    testSame("var a = 1;" +
             "var Class = function() {}; " +
             "switch (Class.property_ = a) {" +
             "  default:" +
             "}");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testComplexAssigns
  public void testComplexAssigns() {
    
    testSame("var x = 0; x += 3; x *= 5;");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testNestedAssigns
  public void testNestedAssigns() {
    test("var x = 0; var y = x = 3; window.alert(y);",
         "var y = 3; window.alert(y);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testComplexNestedAssigns1
  public void testComplexNestedAssigns1() {
    
    testSame("var x = 0; var y = 2; y += x = 3; window.alert(x);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testComplexNestedAssigns2
  public void testComplexNestedAssigns2() {
    test("var x = 0; var y = 2; y += x = 3; window.alert(y);",
         "var y = 2; y += 3; window.alert(y);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testComplexNestedAssigns3
  public void testComplexNestedAssigns3() {
    test("var x = 0; var y = x += 3; window.alert(x);",
         "var x = 0; x += 3; window.alert(x);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testComplexNestedAssigns4
  public void testComplexNestedAssigns4() {
    testSame("var x = 0; var y = x += 3; window.alert(y);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testUnintendedUseOfInheritsInLocalScope1
  public void testUnintendedUseOfInheritsInLocalScope1() {
    testSame("goog.mixin = function() {}; " +
             "(function() { var x = {}; var y = {}; goog.mixin(x, y); })();");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testUnintendedUseOfInheritsInLocalScope2
  public void testUnintendedUseOfInheritsInLocalScope2() {
    testSame("goog.mixin = function() {}; " +
             "var x = {}; var y = {}; (function() { goog.mixin(x, y); })();");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testUnintendedUseOfInheritsInLocalScope3
  public void testUnintendedUseOfInheritsInLocalScope3() {
    testSame("goog.mixin = function() {}; " +
             "var x = {}; var y = {}; (function() { goog.mixin(x, y); })(); " +
             "window.alert(x);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testUnintendedUseOfInheritsInLocalScope4
  public void testUnintendedUseOfInheritsInLocalScope4() {
    
    
    testSame("var goog$mixin = function() {}; " +
             "(function() { var x = {}; var y = {}; goog$mixin(x, y); })();");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testPrototypePropertySetInLocalScope1
  public void testPrototypePropertySetInLocalScope1() {
    testSame("(function() { var x = function(){}; x.prototype.bar = 3; })();");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testPrototypePropertySetInLocalScope2
  public void testPrototypePropertySetInLocalScope2() {
    testSame("var x = function(){}; (function() { x.prototype.bar = 3; })();");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testPrototypePropertySetInLocalScope3
  public void testPrototypePropertySetInLocalScope3() {
    test("var x = function(){ x.prototype.bar = 3; };", "");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testPrototypePropertySetInLocalScope4
  public void testPrototypePropertySetInLocalScope4() {
    test("var x = {}; x.foo = function(){ x.foo.prototype.bar = 3; };", "");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testPrototypePropertySetInLocalScope5
  public void testPrototypePropertySetInLocalScope5() {
    test("var x = {}; x.prototype.foo = 3;", "");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testPrototypePropertySetInLocalScope6
  public void testPrototypePropertySetInLocalScope6() {
    testSame("var x = {}; x.prototype.foo = 3; bar(x.prototype.foo)");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testPrototypePropertySetInLocalScope7
  public void testPrototypePropertySetInLocalScope7() {
    testSame("var x = {}; x.foo = 3; bar(x.foo)");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRValueReference1
  public void testRValueReference1() {
    testSame("var a = 1; a");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRValueReference2
  public void testRValueReference2() {
    testSame("var a = 1; 1+a");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRValueReference3
  public void testRValueReference3() {
    testSame("var x = {}; x.prototype.foo = 3; var a = x.prototype.foo; 1+a");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRValueReference4
  public void testRValueReference4() {
    testSame("var x = {}; x.prototype.foo = 3; x.prototype.foo");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRValueReference5
  public void testRValueReference5() {
    testSame("var x = {}; x.prototype.foo = 3; 1+x.prototype.foo");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRValueReference6
  public void testRValueReference6() {
    testSame("var x = {}; var idx = 2; x[idx]");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testUnhandledTopNode
  public void testUnhandledTopNode() {
    testSame("function Foo() {}; Foo.prototype.isBar = function() {};" +
             "function Bar() {}; Bar.prototype.isFoo = function() {};" +
             "var foo = new Foo(); var bar = new Bar();" +
             
             
             "var cond = foo.isBar() && bar.isFoo();" +
             "if (cond) {window.alert('hello');}");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testPropertyDefinedInGlobalScope
  public void testPropertyDefinedInGlobalScope() {
    testSame("function Foo() {}; var x = new Foo(); x.cssClass = 'bar';" +
             "window.alert(x);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testConditionallyDefinedFunction1
  public void testConditionallyDefinedFunction1() {
    testSame("var g; externfoo.x || (externfoo.x = function() { g; })");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testConditionallyDefinedFunction2
  public void testConditionallyDefinedFunction2() {
    testSame("var g; 1 || (externfoo.x = function() { g; })");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testGetElemOnThis
  public void testGetElemOnThis() {
    testSame("var a = 3; this['foo'] = a;");
    testSame("this['foo'] = 3;");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRemoveInstanceOfOnly
  public void testRemoveInstanceOfOnly() {
    test("function Foo() {}; Foo.prototype.isBar = function() {};" +
         "var x; if (x instanceof Foo) { window.alert(x); }",
         ";var x; if (false) { window.alert(x); }");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRemoveLocalScopedInstanceOfOnly
  public void testRemoveLocalScopedInstanceOfOnly() {
    test("function Foo() {}; function Bar(x) { this.z = x instanceof Foo; };" +
        "externfoo.x = new Bar({});",
        ";function Bar(x) { this.z = false }; externfoo.x = new Bar({});");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRemoveInstanceOfWithReferencedMethod
  public void testRemoveInstanceOfWithReferencedMethod() {
    test("function Foo() {}; Foo.prototype.isBar = function() {};" +
        "var x; if (x instanceof Foo) { window.alert(x.isBar()); }",
        ";var x; if (false) { window.alert(x.isBar()); }");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testDoNotChangeReferencedInstanceOf
  public void testDoNotChangeReferencedInstanceOf() {
    testSame("function Foo() {}; Foo.prototype.isBar = function() {};" +
             "var x = new Foo(); if (x instanceof Foo) { window.alert(x); }");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testDoNotChangeReferencedLocalScopedInstanceOf
  public void testDoNotChangeReferencedLocalScopedInstanceOf() {
    testSame("function Foo() {}; externfoo.x = new Foo();" +
        "function Bar() { if (x instanceof Foo) { window.alert(x); } };" +
        "externfoo.y = new Bar();");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testDoNotChangeLocalScopeReferencedInstanceOf
  public void testDoNotChangeLocalScopeReferencedInstanceOf() {
    testSame("function Foo() {}; Foo.prototype.isBar = function() {};" +
        "function Bar() { this.z = new Foo(); }; externfoo.x = new Bar();" +
        "if (x instanceof Foo) { window.alert(x); }");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testDoNotChangeLocalScopeReferencedLocalScopedInstanceOf
  public void testDoNotChangeLocalScopeReferencedLocalScopedInstanceOf() {
    testSame("function Foo() {}; Foo.prototype.isBar = function() {};" +
        "function Bar() { this.z = new Foo(); };" +
        "Bar.prototype.func = function(x) {" +
          "if (x instanceof Foo) { window.alert(x); }" +
        "}; new Bar().func();");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testDoNotChangeInstanceOfGetElem
  public void testDoNotChangeInstanceOfGetElem() {
    testSame("var goog = {};" +
        "function f(obj, name) {" +
        "  if (obj instanceof goog[name]) {" +
        "    return name;" +
        "  }" +
        "}" +
        "window['f'] = f;");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testWeirdnessOnLeftSideOfPrototype
  public void testWeirdnessOnLeftSideOfPrototype() {
    
    
    testSame("var x = 3; " +
        "(function() { this.bar = 3; }).z = function() {" +
        "  return x;" +
        "};");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testShortCircuit1
  public void testShortCircuit1() {
    test("var a = b() || 1", "b()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testShortCircuit2
  public void testShortCircuit2() {
    test("var a = 1 || c()", "1 || c()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testShortCircuit3
  public void testShortCircuit3() {
    test("var a = b() || c()", "b() || c()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testShortCircuit4
  public void testShortCircuit4() {
    test("var a = b() || 3 || c()", "b() || 3 || c()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testShortCircuit5
  public void testShortCircuit5() {
    test("var a = b() && 1", "b()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testShortCircuit6
  public void testShortCircuit6() {
    test("var a = 1 && c()", "1 && c()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testShortCircuit7
  public void testShortCircuit7() {
    test("var a = b() && c()", "b() && c()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testShortCircuit8
  public void testShortCircuit8() {
    test("var a = b() && 3 && c()", "b() && 3 && c()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRhsReference1
  public void testRhsReference1() {
    testSame("var a = 1; a");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRhsReference2
  public void testRhsReference2() {
    testSame("var a = 1; a || b()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRhsReference3
  public void testRhsReference3() {
    testSame("var a = 1; 1 || a");
  }

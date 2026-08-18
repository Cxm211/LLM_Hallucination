// buggy code
  private boolean inferTemplatedTypesForCall(
      Node n, FunctionType fnType) {
    final ImmutableList<TemplateType> keys = fnType.getTemplateTypeMap()
        .getTemplateKeys();
    if (keys.isEmpty()) {
      return false;
    }

    // Try to infer the template types
    Map<TemplateType, JSType> inferred = 
        inferTemplateTypesFromParameters(fnType, n);


    // Replace all template types. If we couldn't find a replacement, we
    // replace it with UNKNOWN.
    TemplateTypeReplacer replacer = new TemplateTypeReplacer(
        registry, inferred);
    Node callTarget = n.getFirstChild();

    FunctionType replacementFnType = fnType.visit(replacer)
        .toMaybeFunctionType();
    Preconditions.checkNotNull(replacementFnType);

    callTarget.setJSType(replacementFnType);
    n.setJSType(replacementFnType.getReturnType());

    return replacer.madeChanges;
  }

// relevant test
// com.google.javascript.jscomp.TypeCheckTest::testInheritanceCheck7
  public void testInheritanceCheck7() throws Exception {
    testTypes(
        "var goog = {};" +
        "goog.Super = function() {};" +
        "goog.Super.prototype.foo = 3;" +
        "goog.Sub = function() {};" +
        "goog.Sub.prototype.foo = 5;");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInheritanceCheck8
  public void testInheritanceCheck8() throws Exception {
    testTypes(
        "var goog = {};" +
        "goog.Super = function() {};" +
        "goog.Super.prototype.foo = 3;" +
        "goog.Sub = function() {};" +
        "goog.Sub.prototype.foo = 5;");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInheritanceCheck9_1
  public void testInheritanceCheck9_1() throws Exception {
    testTypes(
        "function Super() {};" +
        "Super.prototype.foo = function() { return 3; };" +
        "function Sub() {};" +
        "Sub.prototype.foo =\n" +
        "function() { return 1; };");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInheritanceCheck9_2
  public void testInheritanceCheck9_2() throws Exception {
    testTypes(
        "function Super() {};" +
        "" +
        "Super.prototype.foo = function() { return 1; };" +
        "function Sub() {};" +
        "Sub.prototype.foo =\n" +
        "function() {};");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInheritanceCheck9_3
  public void testInheritanceCheck9_3() throws Exception {
    testTypes(
        "function Super() {};" +
        "" +
        "Super.prototype.foo = function() { return 1; };" +
        "function Sub() {};" +
        "Sub.prototype.foo =\n" +
        "function() { return \"some string\" };",
        "mismatch of the foo property type and the type of the property it " +
        "overrides from superclass Super\n" +
        "original: function (this:Super): number\n" +
        "override: function (this:Sub): string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInheritanceCheck10_1
  public void testInheritanceCheck10_1() throws Exception {
    testTypes(
        "function Root() {};" +
        "Root.prototype.foo = function() { return 3; };" +
        "function Super() {};" +
        "function Sub() {};" +
        "Sub.prototype.foo =\n" +
        "function() { return 1; };");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInheritanceCheck10_2
  public void testInheritanceCheck10_2() throws Exception {
    testTypes(
        "function Root() {};" +
        "" +
        "Root.prototype.foo = function() { return 1; };" +
        "function Super() {};" +
        "function Sub() {};" +
        "Sub.prototype.foo =\n" +
        "function() {};");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInheritanceCheck10_3
  public void testInheritanceCheck10_3() throws Exception {
    testTypes(
        "function Root() {};" +
        "" +
        "Root.prototype.foo = function() { return 1; };" +
        "function Super() {};" +
        "function Sub() {};" +
        "Sub.prototype.foo =\n" +
        "function() { return \"some string\" };",
        "mismatch of the foo property type and the type of the property it " +
        "overrides from superclass Root\n" +
        "original: function (this:Root): number\n" +
        "override: function (this:Sub): string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInterfaceInheritanceCheck11
  public void testInterfaceInheritanceCheck11() throws Exception {
    testTypes(
        "function Super() {};" +
        "Super.prototype.foo = function(bar) {};" +
        "function Sub() {};" +
        "Sub.prototype.foo =\n" +
        "function(bar) {};",
        "mismatch of the foo property type and the type of the property it " +
        "overrides from superclass Super\n" +
        "original: function (this:Super, number): undefined\n" +
        "override: function (this:Sub, string): undefined");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInheritanceCheck12
  public void testInheritanceCheck12() throws Exception {
    testTypes(
        "var goog = {};" +
        "goog.Super = function() {};" +
        "goog.Super.prototype.foo = 3;" +
        "goog.Sub = function() {};" +
        "goog.Sub.prototype.foo = \"some string\";");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInheritanceCheck13
  public void testInheritanceCheck13() throws Exception {
    testTypes(
        "var goog = {};\n" +
        "function Sub() {};" +
        "Sub.prototype.foo = function() {};",
        "Bad type annotation. Unknown type goog.Missing");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInheritanceCheck14
  public void testInheritanceCheck14() throws Exception {
    testClosureTypes(
        "var goog = {};\n" +
        "\n" +
        "goog.Super = function() {};\n" +
        "function Sub() {};" +
        "Sub.prototype.foo = function() {};",
        "Bad type annotation. Unknown type goog.Missing");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInheritanceCheck15
  public void testInheritanceCheck15() throws Exception {
    testTypes(
        "function Super() {};" +
        "Super.prototype.foo;" +
        "function Sub() {};" +
        "Sub.prototype.foo =\n" +
        "function(bar) {};");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInheritanceCheck16
  public void testInheritanceCheck16() throws Exception {
    testTypes(
        "var goog = {};" +
        "goog.Super = function() {};" +
        " goog.Super.prototype.foo = 3;" +
        "goog.Sub = function() {};" +
        " goog.Sub.prototype.foo = 5;",
        "property foo already defined on superclass goog.Super; " +
        "use @override to override it");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInheritanceCheck17
  public void testInheritanceCheck17() throws Exception {
    
    
    reportMissingOverrides = CheckLevel.OFF;
    testTypes(
        "var goog = {};" +
        "goog.Super = function() {};" +
        " goog.Super.prototype.foo = function(x) {};" +
        "goog.Sub = function() {};" +
        " goog.Sub.prototype.foo = function(x) {};",
        "mismatch of the foo property type and the type of the property it " +
        "overrides from superclass goog.Super\n" +
        "original: function (this:goog.Super, number): undefined\n" +
        "override: function (this:goog.Sub, string): undefined");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInterfacePropertyOverride1
  public void testInterfacePropertyOverride1() throws Exception {
    testTypes(
        "function Super() {};" +
        "Super.prototype.foo = function() {};" +
        "function Sub() {};" +
        "Sub.prototype.foo = function() {};");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInterfacePropertyOverride2
  public void testInterfacePropertyOverride2() throws Exception {
    testTypes(
        "function Root() {};" +
        "Root.prototype.foo = function() {};" +
        "function Super() {};" +
        "function Sub() {};" +
        "Sub.prototype.foo = function() {};");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInterfaceInheritanceCheck1
  public void testInterfaceInheritanceCheck1() throws Exception {
    testTypes(
        "function Super() {};" +
        "Super.prototype.foo = function() {};" +
        "function Sub() {};" +
        "Sub.prototype.foo = function() {};",
        "property foo already defined on interface Super; use @override to " +
        "override it");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInterfaceInheritanceCheck2
  public void testInterfaceInheritanceCheck2() throws Exception {
    testTypes(
        "function Super() {};" +
        "Super.prototype.foo = function() {};" +
        "function Sub() {};" +
        "Sub.prototype.foo = function() {};");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInterfaceInheritanceCheck3
  public void testInterfaceInheritanceCheck3() throws Exception {
    testTypes(
        "function Root() {};" +
        "Root.prototype.foo = function() {};" +
        "function Super() {};" +
        "function Sub() {};" +
        "Sub.prototype.foo = function() { return 1;};",
        "property foo already defined on interface Root; use @override to " +
        "override it");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInterfaceInheritanceCheck4
  public void testInterfaceInheritanceCheck4() throws Exception {
    testTypes(
        "function Root() {};" +
        "Root.prototype.foo = function() {};" +
        "function Super() {};" +
        "function Sub() {};" +
        "Sub.prototype.foo =\n" +
        "function() { return 1;};");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInterfaceInheritanceCheck5
  public void testInterfaceInheritanceCheck5() throws Exception {
    testTypes(
        "function Super() {};" +
        "Super.prototype.foo = function() {};" +
        "function Sub() {};" +
        "Sub.prototype.foo =\n" +
        "function() { return 1; };",
        "mismatch of the foo property type and the type of the property it " +
        "overrides from interface Super\n" +
        "original: function (this:Super): string\n" +
        "override: function (this:Sub): number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInterfaceInheritanceCheck6
  public void testInterfaceInheritanceCheck6() throws Exception {
    testTypes(
        "function Root() {};" +
        "Root.prototype.foo = function() {};" +
        "function Super() {};" +
        "function Sub() {};" +
        "Sub.prototype.foo =\n" +
        "function() { return 1; };",
        "mismatch of the foo property type and the type of the property it " +
        "overrides from interface Root\n" +
        "original: function (this:Root): string\n" +
        "override: function (this:Sub): number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInterfaceInheritanceCheck7
  public void testInterfaceInheritanceCheck7() throws Exception {
    testTypes(
        "function Super() {};" +
        "Super.prototype.foo = function(bar) {};" +
        "function Sub() {};" +
        "Sub.prototype.foo =\n" +
        "function(bar) {};",
        "mismatch of the foo property type and the type of the property it " +
        "overrides from interface Super\n" +
        "original: function (this:Super, number): undefined\n" +
        "override: function (this:Sub, string): undefined");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInterfaceInheritanceCheck8
  public void testInterfaceInheritanceCheck8() throws Exception {
    testTypes(
        "function Sub() {};" +
        "Sub.prototype.foo = function() {};",
        new String[] {
          "Bad type annotation. Unknown type Super",
          "property foo not defined on any superclass of Sub"
        });
  }

// com.google.javascript.jscomp.TypeCheckTest::testInterfaceInheritanceCheck9
  public void testInterfaceInheritanceCheck9() throws Exception {
    testTypes(
        " function I() {}" +
        " I.prototype.bar = function() {};" +
        " function F() {}" +
        " F.prototype.bar = function() {return 3; };" +
        " F.prototype.foo = function() {return 3; };" +
        " " +
        "function G() {}" +
        " function f() { return new G().bar(); }",
        "inconsistent return type\n" +
        "found   : number\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInterfaceInheritanceCheck10
  public void testInterfaceInheritanceCheck10() throws Exception {
    testTypes(
        " function I() {}" +
        " I.prototype.bar = function() {};" +
        " function F() {}" +
        " F.prototype.foo = function() {return 3; };" +
        " " +
        "function G() {}" +
        " " +
        "G.prototype.bar = G.prototype.foo;" +
        " function f() { return new G().bar(); }",
        "inconsistent return type\n" +
        "found   : number\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInterfaceInheritanceCheck12
  public void testInterfaceInheritanceCheck12() throws Exception {
    testTypes(
        " function I() {};\n" +
        " I.prototype.foobar;\n" +
        "\n" +
        "function C() {\n" +
        " this.foobar = 2;};\n" +
        " \n var test = new C(); alert(test.foobar);",
        "mismatch of the foobar property type and the type of the property" +
        " it overrides from interface I\n" +
        "original: string\n" +
        "override: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInterfaceInheritanceCheck13
  public void testInterfaceInheritanceCheck13() throws Exception {
    testTypes(
        "function abstractMethod() {};\n" +
        "var base = function() {};\n" +
        " var Int = function() {}\n" +
        " var x; \n" +
        " base.prototype.bar = abstractMethod; \n" +
        " var foo;\n" +
        "foo.bar();");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInterfaceInheritanceCheck14
  public void testInterfaceInheritanceCheck14() throws Exception {
    testTypes(
        "function A() {};" +
        "A.prototype.foo = function() {};" +
        "function B() {};" +
        "B.prototype.bar = function() {};" +
        "function C() {};" +
        "C.prototype.foo = function() {};" +
        "C.prototype.bar = function() {};");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInterfaceInheritanceCheck15
  public void testInterfaceInheritanceCheck15() throws Exception {
    testTypes(
        "function A() {};" +
        "A.prototype.foo = function() {};" +
        "function B() {};" +
        "B.prototype.bar = function() {};" +
        "function C() {};" +
        "C.prototype.foo = function() {};" +
        "C.prototype.bar = function() {};");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInterfaceInheritanceCheck16
  public void testInterfaceInheritanceCheck16() throws Exception {
    testTypes(
        "function A() {};" +
        "A.prototype.foo = function() {};" +
        "A.prototype.bar = function() {};" +
        "function B() {};" +
        "B.prototype.foo = function() { return 'string'};" +
        "B.prototype.bar = function() { return 3 };",
        "inconsistent return type\n" +
        "found   : number\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInterfacePropertyNotImplemented
  public void testInterfacePropertyNotImplemented() throws Exception {
    testTypes(
        "function Int() {};" +
        "Int.prototype.foo = function() {};" +
        "function Foo() {};",
        "property foo on interface Int is not implemented by type Foo");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInterfacePropertyNotImplemented2
  public void testInterfacePropertyNotImplemented2() throws Exception {
    testTypes(
        "function Int() {};" +
        "Int.prototype.foo = function() {};" +
        "function Int2() {};" +
        "function Foo() {};",
        "property foo on interface Int is not implemented by type Foo");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInterfacePropertyNotImplemented3
  public void testInterfacePropertyNotImplemented3() throws Exception {
    testTypes(
        "function Int() {};" +
        "Int.prototype.foo = function() {};" +
        "function Foo() {};" +
        "Foo.prototype.foo = function() {};",
        "mismatch of the foo property type and the type of the property it " +
        "overrides from interface Int\n" +
        "original: function (this:Int): string\n" +
        "override: function (this:Foo): number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testStubConstructorImplementingInterface
  public void testStubConstructorImplementingInterface() throws Exception {
    
    
    testTypes(
        
        " function Int() {}\n" +
        "Int.prototype.foo = function() {};" +
        " var Foo;\n",
        "", null, false);
  }

// com.google.javascript.jscomp.TypeCheckTest::testObjectLiteral
  public void testObjectLiteral() throws Exception {
    Node n = parseAndTypeCheck("var a = {m1: 7, m2: 'hello'}");

    Node nameNode = n.getFirstChild().getFirstChild();
    Node objectNode = nameNode.getFirstChild();

    
    assertEquals(Token.NAME, nameNode.getType());
    assertEquals(Token.OBJECTLIT, objectNode.getType());

    
    ObjectType objectType =
        (ObjectType) objectNode.getJSType();
    assertTypeEquals(NUMBER_TYPE, objectType.getPropertyType("m1"));
    assertTypeEquals(STRING_TYPE, objectType.getPropertyType("m2"));

    
    assertTypeEquals(objectType, nameNode.getJSType());
  }

// com.google.javascript.jscomp.TypeCheckTest::testObjectLiteralDeclaration1
  public void testObjectLiteralDeclaration1() throws Exception {
    testTypes(
        "var x = {" +
        " abc: true," +
        " 'def': 0," +
        " 3: 'fgh'" +
        "};");
  }

// com.google.javascript.jscomp.TypeCheckTest::testObjectLiteralDeclaration2
  public void testObjectLiteralDeclaration2() throws Exception {
    testTypes(
        "var x = {" +
        "   abc: true" +
        "};" +
        "x.abc = 0;",
        "assignment to property abc of x\n" +
        "found   : number\n" +
        "required: boolean");
  }

// com.google.javascript.jscomp.TypeCheckTest::testObjectLiteralDeclaration3
  public void testObjectLiteralDeclaration3() throws Exception {
    testTypes(
        " function f(x) {}" +
        "f({foo: function() {}});");
  }

// com.google.javascript.jscomp.TypeCheckTest::testObjectLiteralDeclaration4
  public void testObjectLiteralDeclaration4() throws Exception {
    testClosureTypes(
        "var x = {" +
        "   abc: function(x) {}" +
        "};" +
        " x.abc = function(x) {};",
        "assignment to property abc of x\n" +
        "found   : function (string): undefined\n" +
        "required: function (boolean): undefined");
    
    
    
  }

// com.google.javascript.jscomp.TypeCheckTest::testObjectLiteralDeclaration5
  public void testObjectLiteralDeclaration5() throws Exception {
    testTypes(
        "var x = {" +
        "   abc: function(x) {}" +
        "};" +
        " x.abc = function(x) {};");
  }

// com.google.javascript.jscomp.TypeCheckTest::testObjectLiteralDeclaration6
  public void testObjectLiteralDeclaration6() throws Exception {
    testTypes(
        "var x = {};" +
        " x.abc = function(x) {};" +
        "x = {" +
        "  " +
        "  abc: function(x) {}" +
        "};");
  }

// com.google.javascript.jscomp.TypeCheckTest::testObjectLiteralDeclaration7
  public void testObjectLiteralDeclaration7() throws Exception {
    testTypes(
        "var x = {};" +
        " x.abc = function(x) {};" +
        "x = {" +
        "  " +
        "  abc: function(x) {}" +
        "};");
  }

// com.google.javascript.jscomp.TypeCheckTest::testCallDateConstructorAsFunction
  public void testCallDateConstructorAsFunction() throws Exception {
    
    
    Node n = parseAndTypeCheck("Date()");
    assertTypeEquals(STRING_TYPE, n.getFirstChild().getFirstChild().getJSType());
  }

// com.google.javascript.jscomp.TypeCheckTest::testCallErrorConstructorAsFunction
  public void testCallErrorConstructorAsFunction() throws Exception {
    Node n = parseAndTypeCheck("Error('x')");
    assertTypeEquals(ERROR_TYPE,
                 n.getFirstChild().getFirstChild().getJSType());
  }

// com.google.javascript.jscomp.TypeCheckTest::testCallArrayConstructorAsFunction
  public void testCallArrayConstructorAsFunction() throws Exception {
    Node n = parseAndTypeCheck("Array()");
    assertTypeEquals(ARRAY_TYPE,
                 n.getFirstChild().getFirstChild().getJSType());
  }

// com.google.javascript.jscomp.TypeCheckTest::testPropertyTypeOfUnionType
  public void testPropertyTypeOfUnionType() throws Exception {
    testTypes("var a = {};" +
        " a.N = function() {};\n" +
        "a.N.prototype.p = 1;\n" +
        " a.S = function() {};\n" +
        "a.S.prototype.p = 'a';\n" +
        "\n" +
        "var f = function(x) { return x.p; };",
        "inconsistent return type\n" +
        "found   : (number|string)\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testAnnotatedPropertyOnInterface1
  public void testAnnotatedPropertyOnInterface1() throws Exception {
    
    
    testTypes(" u.T = function() {};\n" +
        " u.T.prototype.f = function() {};");
  }

// com.google.javascript.jscomp.TypeCheckTest::testAnnotatedPropertyOnInterface2
  public void testAnnotatedPropertyOnInterface2() throws Exception {
    testTypes(" u.T = function() {};\n" +
        " u.T.prototype.f = function() { };");
  }

// com.google.javascript.jscomp.TypeCheckTest::testAnnotatedPropertyOnInterface3
  public void testAnnotatedPropertyOnInterface3() throws Exception {
    testTypes(" function T() {};\n" +
        " T.prototype.f = function() { };");
  }

// com.google.javascript.jscomp.TypeCheckTest::testAnnotatedPropertyOnInterface4
  public void testAnnotatedPropertyOnInterface4() throws Exception {
    testTypes(
        CLOSURE_DEFS +
        " function T() {};\n" +
        " T.prototype.f = goog.abstractMethod;");
  }

// com.google.javascript.jscomp.TypeCheckTest::testWarnUnannotatedPropertyOnInterface5
  public void testWarnUnannotatedPropertyOnInterface5() throws Exception {
    testTypes(" u.T = function () {};\n" +
        "u.T.prototype.x = function() {};");
  }

// com.google.javascript.jscomp.TypeCheckTest::testWarnUnannotatedPropertyOnInterface6
  public void testWarnUnannotatedPropertyOnInterface6() throws Exception {
    testTypes(" function T() {};\n" +
        "T.prototype.x = function() {};");
  }

// com.google.javascript.jscomp.TypeCheckTest::testDataPropertyOnInterface1
  public void testDataPropertyOnInterface1() throws Exception {
    testTypes(" function T() {};\n" +
        "T.prototype.x;");
  }

// com.google.javascript.jscomp.TypeCheckTest::testDataPropertyOnInterface2
  public void testDataPropertyOnInterface2() throws Exception {
    reportMissingOverrides = CheckLevel.OFF;
    testTypes(" function T() {};\n" +
        "T.prototype.x;\n" +
        "\n" +
        "function C() {}\n" +
        "C.prototype.x = 'foo';",
        "mismatch of the x property type and the type of the property it " +
        "overrides from interface T\n" +
        "original: number\n" +
        "override: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testDataPropertyOnInterface3
  public void testDataPropertyOnInterface3() throws Exception {
    testTypes(" function T() {};\n" +
        "T.prototype.x;\n" +
        "\n" +
        "function C() {}\n" +
        "\n" +
        "C.prototype.x = 'foo';",
        "mismatch of the x property type and the type of the property it " +
        "overrides from interface T\n" +
        "original: number\n" +
        "override: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testDataPropertyOnInterface4
  public void testDataPropertyOnInterface4() throws Exception {
    testTypes(" function T() {};\n" +
        "T.prototype.x;\n" +
        "\n" +
        "function C() {  \n this.x = 'foo'; }\n",
        "mismatch of the x property type and the type of the property it " +
        "overrides from interface T\n" +
        "original: number\n" +
        "override: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testWarnDataPropertyOnInterface3
  public void testWarnDataPropertyOnInterface3() throws Exception {
    testTypes(" u.T = function () {};\n" +
        "u.T.prototype.x = 1;",
        "interface members can only be empty property declarations, "
        + "empty functions, or goog.abstractMethod");
  }

// com.google.javascript.jscomp.TypeCheckTest::testWarnDataPropertyOnInterface4
  public void testWarnDataPropertyOnInterface4() throws Exception {
    testTypes(" function T() {};\n" +
        "T.prototype.x = 1;",
        "interface members can only be empty property declarations, "
        + "empty functions, or goog.abstractMethod");
  }

// com.google.javascript.jscomp.TypeCheckTest::testErrorMismatchingPropertyOnInterface4
  public void testErrorMismatchingPropertyOnInterface4() throws Exception {
    testTypes(" u.T = function () {};\n" +
        "u.T.prototype.x =\n" +
        "function() {};",
        "parameter foo does not appear in u.T.prototype.x's parameter list");
  }

// com.google.javascript.jscomp.TypeCheckTest::testErrorMismatchingPropertyOnInterface5
  public void testErrorMismatchingPropertyOnInterface5() throws Exception {
    testTypes(" function T() {};\n" +
        "T.prototype.x = function() { };",
        "assignment to property x of T.prototype\n" +
        "found   : function (): undefined\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testErrorMismatchingPropertyOnInterface6
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

// com.google.javascript.jscomp.TypeCheckTest::testInterfaceNonEmptyFunction
  public void testInterfaceNonEmptyFunction() throws Exception {
    testTypes(" function T() {};\n" +
        "T.prototype.x = function() { return 'foo'; }",
        "interface member functions must have an empty body"
        );
  }

// com.google.javascript.jscomp.TypeCheckTest::testDoubleNestedInterface
  public void testDoubleNestedInterface() throws Exception {
    testTypes(" var I1 = function() {};\n" +
              " I1.I2 = function() {};\n" +
              " I1.I2.I3 = function() {};\n");
  }

// com.google.javascript.jscomp.TypeCheckTest::testStaticDataPropertyOnNestedInterface
  public void testStaticDataPropertyOnNestedInterface() throws Exception {
    testTypes(" var I1 = function() {};\n" +
              " I1.I2 = function() {};\n" +
              " I1.I2.x = 1;\n");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInterfaceInstantiation
  public void testInterfaceInstantiation() throws Exception {
    testTypes("var f = function(){}; new f",
              "cannot instantiate non-constructor");
  }

// com.google.javascript.jscomp.TypeCheckTest::testPrototypeLoop
  public void testPrototypeLoop() throws Exception {
    testClosureTypesMultipleWarnings(
        suppressMissingProperty("foo") +
        "var T = function() {};" +
        "alert((new T).foo);",
        Lists.newArrayList(
            "Parse error. Cycle detected in inheritance chain of type T",
            "Could not resolve type in @extends tag of T"));
  }

// com.google.javascript.jscomp.TypeCheckTest::testImplementsLoop
  public void testImplementsLoop() throws Exception {
    testClosureTypesMultipleWarnings(
        suppressMissingProperty("foo") +
        "var T = function() {};" +
        "alert((new T).foo);",
        Lists.newArrayList(
            "Parse error. Cycle detected in inheritance chain of type T"));
  }

// com.google.javascript.jscomp.TypeCheckTest::testImplementsExtendsLoop
  public void testImplementsExtendsLoop() throws Exception {
    testClosureTypesMultipleWarnings(
        suppressMissingProperty("foo") +
            "var G = function() {};" +
            "var F = function() {};" +
        "alert((new F).foo);",
        Lists.newArrayList(
            "Parse error. Cycle detected in inheritance chain of type F"));
  }

// com.google.javascript.jscomp.TypeCheckTest::testInterfaceExtendsLoop
  public void testInterfaceExtendsLoop() throws Exception {
    
    
    testClosureTypesMultipleWarnings(
        suppressMissingProperty("foo") +
            "var G = function() {};" +
            "var F = function() {};",
        Lists.newArrayList(
            "Could not resolve type in @extends tag of G"));
  }

// com.google.javascript.jscomp.TypeCheckTest::testConversionFromInterfaceToRecursiveConstructor
  public void testConversionFromInterfaceToRecursiveConstructor()
      throws Exception {
    testClosureTypesMultipleWarnings(
        suppressMissingProperty("foo") +
            " var OtherType = function() {}\n" +
            "\n" +
            "var MyType = function() {}\n" +
            "\n" +
            "var x =  (new Object());",
        Lists.newArrayList(
            "Parse error. Cycle detected in inheritance chain of type MyType",
            "initializing variable\n" +
            "found   : OtherType\n" +
            "required: (MyType|null)"));
  }

// com.google.javascript.jscomp.TypeCheckTest::testDirectPrototypeAssign
  public void testDirectPrototypeAssign() throws Exception {
    
    testTypes(
        " function Foo() {}" +
        " function Bar() {}" +
        " Bar.prototype = new Foo()");
  }

// com.google.javascript.jscomp.TypeCheckTest::testResolutionViaRegistry1
  public void testResolutionViaRegistry1() throws Exception {
    testTypes(" u.T = function() {};\n" +
        " u.T.prototype.a;\n" +
        "\n" +
        "var f = function(t) { return t.a; };",
        "inconsistent return type\n" +
        "found   : (number|string)\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testResolutionViaRegistry2
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

// com.google.javascript.jscomp.TypeCheckTest::testResolutionViaRegistry3
  public void testResolutionViaRegistry3() throws Exception {
    testTypes(" u.T = function() {};\n" +
        " u.T.prototype.a = 0;\n" +
        "\n" +
        "var f = function(t) { return t.a; };",
        "inconsistent return type\n" +
        "found   : (number|string)\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testResolutionViaRegistry4
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

// com.google.javascript.jscomp.TypeCheckTest::testResolutionViaRegistry5
  public void testResolutionViaRegistry5() throws Exception {
    Node n = parseAndTypeCheck(" u.T = function() {}; u.T");
    JSType type = n.getLastChild().getLastChild().getJSType();
    assertFalse(type.isUnknownType());
    assertTrue(type instanceof FunctionType);
    assertEquals("u.T",
        ((FunctionType) type).getInstanceType().getReferenceName());
  }

// com.google.javascript.jscomp.TypeCheckTest::testGatherProperyWithoutAnnotation1
  public void testGatherProperyWithoutAnnotation1() throws Exception {
    Node n = parseAndTypeCheck(" var T = function() {};" +
        "var t; t.x; t;");
    JSType type = n.getLastChild().getLastChild().getJSType();
    assertFalse(type.isUnknownType());
    assertTrue(type instanceof ObjectType);
    ObjectType objectType = (ObjectType) type;
    assertFalse(objectType.hasProperty("x"));
    Asserts.assertTypeCollectionEquals(
        Lists.newArrayList(objectType),
        registry.getTypesWithProperty("x"));
  }

// com.google.javascript.jscomp.TypeCheckTest::testGatherProperyWithoutAnnotation2
  public void testGatherProperyWithoutAnnotation2() throws Exception {
    TypeCheckResult ns =
        parseAndTypeCheckWithScope("var t; t.x; t;");
    Node n = ns.root;
    JSType type = n.getLastChild().getLastChild().getJSType();
    assertFalse(type.isUnknownType());
    assertTypeEquals(type, OBJECT_TYPE);
    assertTrue(type instanceof ObjectType);
    ObjectType objectType = (ObjectType) type;
    assertFalse(objectType.hasProperty("x"));
    Asserts.assertTypeCollectionEquals(
        Lists.newArrayList(OBJECT_TYPE),
        registry.getTypesWithProperty("x"));
  }

// com.google.javascript.jscomp.TypeCheckTest::testFunctionMasksVariableBug
  public void testFunctionMasksVariableBug() throws Exception {
    testTypes("var x = 4; var f = function x(b) { return b ? 1 : x(true); };",
        "function x masks variable (IE bug)");
  }

// com.google.javascript.jscomp.TypeCheckTest::testDfa1
  public void testDfa1() throws Exception {
    testTypes("var x = null;\n x = 1;\n  var y = x;");
  }

// com.google.javascript.jscomp.TypeCheckTest::testDfa2
  public void testDfa2() throws Exception {
    testTypes("function u() {}\n" +
        " function f() {\nvar x = 'todo';\n" +
        "if (u()) { x = 1; } else { x = 2; } return x;\n}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testDfa3
  public void testDfa3() throws Exception {
    testTypes("function u() {}\n" +
        " function f() {\n" +
        " var x = 'todo';\n" +
        "if (u()) { x = 1; } else { x = 2; } return x;\n}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testDfa4
  public void testDfa4() throws Exception {
    testTypes(" function f(d) {\n" +
        "if (!d) { return; }\n" +
        " var e = d;\n}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testDfa5
  public void testDfa5() throws Exception {
    testTypes(" function u() {return 'a';}\n" +
        " function f(x) {\n" +
        "while (!x) { x = u(); }\nreturn x;\n}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testDfa6
  public void testDfa6() throws Exception {
    testTypes(" function u() {return {};}\n" +
        " function f(x) {\n" +
        "while (x) { x = u(); if (!x) { x = u(); } }\n}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testDfa7
  public void testDfa7() throws Exception {
    testTypes(" var T = function() {};\n" +
        " T.prototype.x = null;\n" +
        " function f(t) {\n" +
        "if (!t.x) { return; }\n" +
        " var e = t.x;\n}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testDfa8
  public void testDfa8() throws Exception {
    testTypes(" var T = function() {};\n" +
        " T.prototype.x = '';\n" +
        "function u() {}\n" +
        " function f(t) {\n" +
        "if (u()) { t.x = 1; } else { t.x = 2; } return t.x;\n}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testDfa9
  public void testDfa9() throws Exception {
    testTypes("function f() {\nvar x;\nx = null;\n" +
        "if (x == null) { return 0; } else { return 1; } }",
        "condition always evaluates to true\n" +
        "left : null\n" +
        "right: null");
  }

// com.google.javascript.jscomp.TypeCheckTest::testDfa10
  public void testDfa10() throws Exception {
    testTypes(" function g(x) {}" +
        "function f(x) {\n" +
        "if (!x) { x = ''; }\n" +
        "if (g(x)) { return 0; } else { return 1; } }",
        "actual parameter 1 of g does not match formal parameter\n" +
        "found   : string\n" +
        "required: null");
  }

// com.google.javascript.jscomp.TypeCheckTest::testDfa11
  public void testDfa11() throws Exception {
    testTypes("\n" +
        "function f(opt_x) { if (!opt_x) { " +
        "throw new Error('x cannot be empty'); } return opt_x; }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testDfa12
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

// com.google.javascript.jscomp.TypeCheckTest::testDfa13
  public void testDfa13() throws Exception {
    testTypes(
        "" +
        "function g(x, y, z) {}" +
        "function f() { " +
        "  var x = 'a'; g(x, x = 3, x);" +
        "}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTypeInferenceWithCast1
  public void testTypeInferenceWithCast1() throws Exception {
    testTypes(
        "function u(x) {return null;}" +
        "function f(x) {return x;}" +
        "function g(x) {" +
        "var y = (u(x)); return f(y);}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTypeInferenceWithCast2
  public void testTypeInferenceWithCast2() throws Exception {
    testTypes(
        "function u(x) {return null;}" +
        "function f(x) {return x;}" +
        "function g(x) {" +
        "var y; y = (u(x)); return f(y);}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTypeInferenceWithCast3
  public void testTypeInferenceWithCast3() throws Exception {
    testTypes(
        "function u(x) {return 1;}" +
        "function g(x) {" +
        "return (u(x));}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTypeInferenceWithCast4
  public void testTypeInferenceWithCast4() throws Exception {
    testTypes(
        "function u(x) {return 1;}" +
        "function g(x) {" +
        "return (u(x)) && 1;}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTypeInferenceWithCast5
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

// com.google.javascript.jscomp.TypeCheckTest::testTypeInferenceWithClosure1
  public void testTypeInferenceWithClosure1() throws Exception {
    testTypes(
        "" +
        "function f() {" +
        "   var x = null;" +
        "  function g() { x = 'y'; } g(); " +
        "  return x == null;" +
        "}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTypeInferenceWithClosure2
  public void testTypeInferenceWithClosure2() throws Exception {
    testTypes(
        "" +
        "function f() {" +
        "   var x = null;" +
        "  function g() { x = 'y'; } g(); " +
        "  return x === 3;" +
        "}",
        "condition always evaluates to false\n" +
        "left : (null|string)\n" +
        "right: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTypeInferenceWithNoEntry1
  public void testTypeInferenceWithNoEntry1() throws Exception {
    testTypes(
        " function f(x) {}" +
        " function Foo() {}" +
        "Foo.prototype.init = function() {" +
        "   this.bar = {baz: 3};" +
        "};" +
        "" +
        "function SubFoo() {}" +
        "" +
        "SubFoo.prototype.method = function() {" +
        "  for (var i = 0; i < 10; i++) {" +
        "    f(this.bar);" +
        "    f(this.bar.baz);" +
        "  }" +
        "};",
        "actual parameter 1 of f does not match formal parameter\n" +
        "found   : (null|{baz: number})\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTypeInferenceWithNoEntry2
  public void testTypeInferenceWithNoEntry2() throws Exception {
    testClosureTypes(
        CLOSURE_DEFS +
        " function f(x) {}" +
        " function g(x) {}" +
        " function Foo() {}" +
        "Foo.prototype.init = function() {" +
        "   this.bar = {baz: 3};" +
        "};" +
        "" +
        "function SubFoo() {}" +
        "" +
        "SubFoo.prototype.method = function() {" +
        "  for (var i = 0; i < 10; i++) {" +
        "    f(this.bar);" +
        "    goog.asserts.assert(this.bar);" +
        "    g(this.bar);" +
        "  }" +
        "};",
        "actual parameter 1 of f does not match formal parameter\n" +
        "found   : (null|{baz: number})\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testForwardPropertyReference
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

// com.google.javascript.jscomp.TypeCheckTest::testNoForwardTypeDeclaration
  public void testNoForwardTypeDeclaration() throws Exception {
    testTypes(
        " function f(x) {}",
        "Bad type annotation. Unknown type MyType");
  }

// com.google.javascript.jscomp.TypeCheckTest::testNoForwardTypeDeclarationAndNoBraces
  public void testNoForwardTypeDeclarationAndNoBraces() throws Exception {
    testTypes(" function f() {}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testForwardTypeDeclaration1
  public void testForwardTypeDeclaration1() throws Exception {
    testClosureTypes(
        
        "goog.addDependency();" +
        "goog.addDependency('y', [goog]);" +

        "goog.addDependency('zzz.js', ['MyType'], []);" +
        "" +
        "function f(x) { return 3; }", null);
  }

// com.google.javascript.jscomp.TypeCheckTest::testForwardTypeDeclaration2
  public void testForwardTypeDeclaration2() throws Exception {
    String f = "goog.addDependency('zzz.js', ['MyType'], []);" +
        " function f(x) { }";
    testClosureTypes(f, null);
    testClosureTypes(f + "f(3);",
        "actual parameter 1 of f does not match formal parameter\n" +
        "found   : number\n" +
        "required: (MyType|null)");
  }

// com.google.javascript.jscomp.TypeCheckTest::testForwardTypeDeclaration3
  public void testForwardTypeDeclaration3() throws Exception {
    testClosureTypes(
        "goog.addDependency('zzz.js', ['MyType'], []);" +
        " function f(x) { return x; }" +
        " var MyType = function() {};" +
        "f(3);",
        "actual parameter 1 of f does not match formal parameter\n" +
        "found   : number\n" +
        "required: (MyType|null)");
  }

// com.google.javascript.jscomp.TypeCheckTest::testForwardTypeDeclaration4
  public void testForwardTypeDeclaration4() throws Exception {
    testClosureTypes(
        "goog.addDependency('zzz.js', ['MyType'], []);" +
        " function f(x) { return x; }" +
        " var MyType = function() {};" +
        "f(new MyType());",
        null);
  }

// com.google.javascript.jscomp.TypeCheckTest::testForwardTypeDeclaration5
  public void testForwardTypeDeclaration5() throws Exception {
    testClosureTypes(
        "goog.addDependency('zzz.js', ['MyType'], []);" +
        " var YourType = function() {};" +
        " YourType.prototype.method = function() {};",
        "Could not resolve type in @extends tag of YourType");
  }

// com.google.javascript.jscomp.TypeCheckTest::testForwardTypeDeclaration6
  public void testForwardTypeDeclaration6() throws Exception {
    testClosureTypesMultipleWarnings(
        "goog.addDependency('zzz.js', ['MyType'], []);" +
        " var YourType = function() {};" +
        " YourType.prototype.method = function() {};",
        Lists.newArrayList(
            "Could not resolve type in @implements tag of YourType",
            "property method not defined on any superclass of YourType"));
  }

// com.google.javascript.jscomp.TypeCheckTest::testForwardTypeDeclaration7
  public void testForwardTypeDeclaration7() throws Exception {
    testClosureTypes(
        "goog.addDependency('zzz.js', ['MyType'], []);" +
        "" +
        "function f(x) { return x == undefined; }", null);
  }

// com.google.javascript.jscomp.TypeCheckTest::testForwardTypeDeclaration8
  public void testForwardTypeDeclaration8() throws Exception {
    testClosureTypes(
        "goog.addDependency('zzz.js', ['MyType'], []);" +
        "" +
        "function f(x) { return x.name == undefined; }", null);
  }

// com.google.javascript.jscomp.TypeCheckTest::testForwardTypeDeclaration9
  public void testForwardTypeDeclaration9() throws Exception {
    testClosureTypes(
        "goog.addDependency('zzz.js', ['MyType'], []);" +
        "" +
        "function f(x) { x.name = 'Bob'; }", null);
  }

// com.google.javascript.jscomp.TypeCheckTest::testForwardTypeDeclaration10
  public void testForwardTypeDeclaration10() throws Exception {
    String f = "goog.addDependency('zzz.js', ['MyType'], []);" +
        " function f(x) { }";
    testClosureTypes(f, null);
    testClosureTypes(f + "f(3);", null);
    testClosureTypes(f + "f('3');",
        "actual parameter 1 of f does not match formal parameter\n" +
        "found   : string\n" +
        "required: (MyType|null|number)");
  }

// com.google.javascript.jscomp.TypeCheckTest::testForwardTypeDeclaration12
  public void testForwardTypeDeclaration12() throws Exception {
    
    
    testClosureTypes(
        "goog.addDependency('zzz.js', ['MyType'], []);" +
        "\n" +
        "function f(ctor) { return new ctor(); }", null);
  }

// com.google.javascript.jscomp.TypeCheckTest::testForwardTypeDeclaration13
  public void testForwardTypeDeclaration13() throws Exception {
    
    
    
    testClosureTypes(
        "goog.addDependency('zzz.js', ['MyType'], []);" +
        "\n" +
        "function f(ctor) { return (new ctor()).impossibleProp; }",
        "Property impossibleProp never defined on ?");
  }

// com.google.javascript.jscomp.TypeCheckTest::testDuplicateTypeDef
  public void testDuplicateTypeDef() throws Exception {
    testTypes(
        "var goog = {};" +
        " goog.Bar = function() {};" +
        " goog.Bar;",
        "variable goog.Bar redefined with type None, " +
        "original definition at [testcode]:1 " +
        "with type function (new:goog.Bar): undefined");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTypeDef1
  public void testTypeDef1() throws Exception {
    testTypes(
        "var goog = {};" +
        " goog.Bar;" +
        " function f(x) {}" +
        "f(3);");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTypeDef2
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

// com.google.javascript.jscomp.TypeCheckTest::testTypeDef3
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

// com.google.javascript.jscomp.TypeCheckTest::testTypeDef4
  public void testTypeDef4() throws Exception {
    testTypes(
        " function A() {}" +
        " function B() {}" +
        " var AB;" +
        " function f(x) {}" +
        "f(new A()); f(new B()); f(1);",
        "actual parameter 1 of f does not match formal parameter\n" +
        "found   : number\n" +
        "required: (A|B|null)");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTypeDef5
  public void testTypeDef5() throws Exception {
    
    
    
    
    testTypes(
        " function f(x) {}" +
        " function A() {}" +
        " function B() {}" +
        " var AB;" +
        "f(new A()); f(new B()); f(1);",
        "actual parameter 1 of f does not match formal parameter\n" +
        "found   : number\n" +
        "required: (A|B|null)");
  }

// com.google.javascript.jscomp.TypeCheckTest::testCircularTypeDef
  public void testCircularTypeDef() throws Exception {
    testTypes(
        "var goog = {};" +
        " goog.Bar;" +
        " function f(x) {}" +
        "f(3); f([3]); f([[3]]);");
  }

// com.google.javascript.jscomp.TypeCheckTest::testGetTypedPercent1
  public void testGetTypedPercent1() throws Exception {
    String js = "var id = function(x) { return x; }\n" +
                "var id2 = function(x) { return id(x); }";
    assertEquals(50.0, getTypedPercent(js), 0.1);
  }

// com.google.javascript.jscomp.TypeCheckTest::testGetTypedPercent2
  public void testGetTypedPercent2() throws Exception {
    String js = "var x = {}; x.y = 1;";
    assertEquals(100.0, getTypedPercent(js), 0.1);
  }

// com.google.javascript.jscomp.TypeCheckTest::testGetTypedPercent3
  public void testGetTypedPercent3() throws Exception {
    String js = "var f = function(x) { x.a = x.b; }";
    assertEquals(50.0, getTypedPercent(js), 0.1);
  }

// com.google.javascript.jscomp.TypeCheckTest::testGetTypedPercent4
  public void testGetTypedPercent4() throws Exception {
    String js = "var n = {};\n  n.T = function() {};\n" +
        " var x = new n.T();";
    assertEquals(100.0, getTypedPercent(js), 0.1);
  }

// com.google.javascript.jscomp.TypeCheckTest::testGetTypedPercent5
  public void testGetTypedPercent5() throws Exception {
    String js = " keys = {A: 1,B: 2,C: 3};";
    assertEquals(100.0, getTypedPercent(js), 0.1);
  }

// com.google.javascript.jscomp.TypeCheckTest::testGetTypedPercent6
  public void testGetTypedPercent6() throws Exception {
    String js = "a = {TRUE: 1, FALSE: 0};";
    assertEquals(100.0, getTypedPercent(js), 0.1);
  }

// com.google.javascript.jscomp.TypeCheckTest::testPrototypePropertyReference
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

// com.google.javascript.jscomp.TypeCheckTest::testResolvingNamedTypes
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

// com.google.javascript.jscomp.TypeCheckTest::testMissingProperty1
  public void testMissingProperty1() throws Exception {
    testTypes(
        " function Foo() {}" +
        "Foo.prototype.bar = function() { return this.a; };" +
        "Foo.prototype.baz = function() { this.a = 3; };");
  }

// com.google.javascript.jscomp.TypeCheckTest::testMissingProperty2
  public void testMissingProperty2() throws Exception {
    testTypes(
        " function Foo() {}" +
        "Foo.prototype.bar = function() { return this.a; };" +
        "Foo.prototype.baz = function() { this.b = 3; };",
        "Property a never defined on Foo");
  }

// com.google.javascript.jscomp.TypeCheckTest::testMissingProperty3
  public void testMissingProperty3() throws Exception {
    testTypes(
        " function Foo() {}" +
        "Foo.prototype.bar = function() { return this.a; };" +
        "(new Foo).a = 3;");
  }

// com.google.javascript.jscomp.TypeCheckTest::testMissingProperty4
  public void testMissingProperty4() throws Exception {
    testTypes(
        " function Foo() {}" +
        "Foo.prototype.bar = function() { return this.a; };" +
        "(new Foo).b = 3;",
        "Property a never defined on Foo");
  }

// com.google.javascript.jscomp.TypeCheckTest::testMissingProperty5
  public void testMissingProperty5() throws Exception {
    testTypes(
        " function Foo() {}" +
        "Foo.prototype.bar = function() { return this.a; };" +
        " function Bar() { this.a = 3; };",
        "Property a never defined on Foo");
  }

// com.google.javascript.jscomp.TypeCheckTest::testMissingProperty6
  public void testMissingProperty6() throws Exception {
    testTypes(
        " function Foo() {}" +
        "Foo.prototype.bar = function() { return this.a; };" +
        " " +
        "function Bar() { this.a = 3; };");
  }

// com.google.javascript.jscomp.TypeCheckTest::testMissingProperty7
  public void testMissingProperty7() throws Exception {
    testTypes(
        "" +
        "function foo(obj) { return obj.impossible; }",
        "Property impossible never defined on Object");
  }

// com.google.javascript.jscomp.TypeCheckTest::testMissingProperty8
  public void testMissingProperty8() throws Exception {
    testTypes(
        "" +
        "function foo(obj) { return typeof obj.impossible; }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testMissingProperty9
  public void testMissingProperty9() throws Exception {
    testTypes(
        "" +
        "function foo(obj) { if (obj.impossible) { return true; } }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testMissingProperty10
  public void testMissingProperty10() throws Exception {
    testTypes(
        "" +
        "function foo(obj) { while (obj.impossible) { return true; } }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testMissingProperty11
  public void testMissingProperty11() throws Exception {
    testTypes(
        "" +
        "function foo(obj) { for (;obj.impossible;) { return true; } }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testMissingProperty12
  public void testMissingProperty12() throws Exception {
    testTypes(
        "" +
        "function foo(obj) { do { } while (obj.impossible); }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testMissingProperty13
  public void testMissingProperty13() throws Exception {
    testTypes(
        "var goog = {}; goog.isDef = function(x) { return false; };" +
        "" +
        "function foo(obj) { return goog.isDef(obj.impossible); }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testMissingProperty14
  public void testMissingProperty14() throws Exception {
    testTypes(
        "var goog = {}; goog.isDef = function(x) { return false; };" +
        "" +
        "function foo(obj) { return goog.isNull(obj.impossible); }",
        "Property isNull never defined on goog");
  }

// com.google.javascript.jscomp.TypeCheckTest::testMissingProperty15
  public void testMissingProperty15() throws Exception {
    testTypes(
        "" +
        "function f(x) { if (x.foo) { x.foo(); } }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testMissingProperty16
  public void testMissingProperty16() throws Exception {
    testTypes(
        "" +
        "function f(x) { x.foo(); if (x.foo) {} }",
        "Property foo never defined on Object");
  }

// com.google.javascript.jscomp.TypeCheckTest::testMissingProperty17
  public void testMissingProperty17() throws Exception {
    testTypes(
        "" +
        "function f(x) { if (typeof x.foo == 'function') { x.foo(); } }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testMissingProperty18
  public void testMissingProperty18() throws Exception {
    testTypes(
        "" +
        "function f(x) { if (x.foo instanceof Function) { x.foo(); } }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testMissingProperty19
  public void testMissingProperty19() throws Exception {
    testTypes(
        "" +
        "function f(x) { if (x.bar) { if (x.foo) {} } else { x.foo(); } }",
        "Property foo never defined on Object");
  }

// com.google.javascript.jscomp.TypeCheckTest::testMissingProperty20
  public void testMissingProperty20() throws Exception {
    testTypes(
        "" +
        "function f(x) { if (x.foo) { } else { x.foo(); } }",
        "Property foo never defined on Object");
  }

// com.google.javascript.jscomp.TypeCheckTest::testMissingProperty21
  public void testMissingProperty21() throws Exception {
    testTypes(
        "" +
        "function f(x) { x.foo && x.foo(); }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testMissingProperty22
  public void testMissingProperty22() throws Exception {
    testTypes(
        "" +
        "function f(x) { return x.foo ? x.foo() : true; }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testMissingProperty23
  public void testMissingProperty23() throws Exception {
    testTypes(
        "function f(x) { x.impossible(); }",
        "Property impossible never defined on x");
  }

// com.google.javascript.jscomp.TypeCheckTest::testMissingProperty24
  public void testMissingProperty24() throws Exception {
    testClosureTypes(
        "goog.addDependency('zzz.js', ['MissingType'], []);" +
        "" +
        "function f(x) { x.impossible(); }", null);
  }

// com.google.javascript.jscomp.TypeCheckTest::testMissingProperty25
  public void testMissingProperty25() throws Exception {
    testTypes(
        " var Foo = function() {};" +
        "Foo.prototype.bar = function() {};" +
        " var FooAlias = Foo;" +
        "(new FooAlias()).bar();");
  }

// com.google.javascript.jscomp.TypeCheckTest::testMissingProperty26
  public void testMissingProperty26() throws Exception {
    testTypes(
        " var Foo = function() {};" +
        " var FooAlias = Foo;" +
        "FooAlias.prototype.bar = function() {};" +
        "(new Foo()).bar();");
  }

// com.google.javascript.jscomp.TypeCheckTest::testMissingProperty27
  public void testMissingProperty27() throws Exception {
    testClosureTypes(
        "goog.addDependency('zzz.js', ['MissingType'], []);" +
        "" +
        "function f(x) {" +
        "  for (var parent = x; parent; parent = parent.getParent()) {}" +
        "}", null);
  }

// com.google.javascript.jscomp.TypeCheckTest::testMissingProperty28
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

// com.google.javascript.jscomp.TypeCheckTest::testMissingProperty29
  public void testMissingProperty29() throws Exception {
    
    testTypes(
        
        " var Foo;" +
        "Foo.prototype.opera;" +
        "Foo.prototype.opera.postError;",
        "",
        null,
        false);
  }

// com.google.javascript.jscomp.TypeCheckTest::testMissingProperty30
  public void testMissingProperty30() throws Exception {
    testTypes(
        "" +
        "function f() {" +
        " return {};" +
        "}" +
        "f().a = 3;" +
        " function g(y) { return y.a; }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testMissingProperty31
  public void testMissingProperty31() throws Exception {
    testTypes(
        "" +
        "function f() {" +
        " return [];" +
        "}" +
        "f().a = 3;" +
        " function g(y) { return y.a; }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testMissingProperty32
  public void testMissingProperty32() throws Exception {
    testTypes(
        "" +
        "function f() {" +
        " return [];" +
        "}" +
        "f().a = 3;" +
        " function g(y) { return y.a; }",
        "Property a never defined on Date");
  }

// com.google.javascript.jscomp.TypeCheckTest::testMissingProperty33
  public void testMissingProperty33() throws Exception {
    testTypes(
      "" +
      "function f(x) { !x.foo || x.foo(); }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testMissingProperty34
  public void testMissingProperty34() throws Exception {
    testTypes(
        "" +
        " function Foo() {}" +
        "Foo.prototype.bar = function() { return this.a; };" +
        "Foo.prototype.baz = function() { this.b = 3; };");
  }

// com.google.javascript.jscomp.TypeCheckTest::testMissingProperty35
  public void testMissingProperty35() throws Exception {
    
    testTypes(
        " function Foo() {}" +
        " function Bar() {}" +
        " function Baz() {}" +
        " function f(x) { x.specialProp = 1; }" +
        " function g(x) { return x.specialProp; }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testMissingProperty36
  public void testMissingProperty36() throws Exception {
    
    
    testTypes(
        " function Foo() {}" +
        "Foo.prototype.baz = 0;" +
        " function SubFoo() {}" +
        "SubFoo.prototype.bar = 0;" +
        " function f(x) { return x.baz; }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testMissingProperty37
  public void testMissingProperty37() throws Exception {
    
    
    testTypes(
        " function f(x){" +
        "  x.isVisible = false;" +
        "}" +
        " function Foo() {}" +
        " function SubFoo() {}" +
        " SubFoo.prototype.isVisible = true;" +
        "\n" +
        "function g(x) { return x.isVisible; }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testMissingProperty38
  public void testMissingProperty38() throws Exception {
    testTypes(
        " function Foo() {}" +
        " function Bar() {}" +
        " function f() { return new Foo(); }" +
        "f().missing;",
        "Property missing never defined on (Bar|Foo|null)");
  }

// com.google.javascript.jscomp.TypeCheckTest::testMissingProperty39
  public void testMissingProperty39() throws Exception {
    testTypes(
        " function f() { return 3; }" +
        "f().length;");
  }

// com.google.javascript.jscomp.TypeCheckTest::testMissingProperty40
  public void testMissingProperty40() throws Exception {
    testClosureTypes(
        "goog.addDependency('zzz.js', ['MissingType'], []);" +
        "" +
        "function f(x) { x.impossible(); }", null);
  }

// com.google.javascript.jscomp.TypeCheckTest::testMissingProperty41
  public void testMissingProperty41() throws Exception {
    testTypes(
        "" +
        "function f(x) { if (x.impossible) x.impossible(); }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testMissingProperty42
  public void testMissingProperty42() throws Exception {
    testTypes(
        "" +
        "function f(x) { " +
        "  if (typeof x.impossible == 'undefined') throw Error();" +
        "  return x.impossible;" +
        "}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testMissingProperty43
  public void testMissingProperty43() throws Exception {
    testTypes(
        "function f(x) { " +
        " return  (x.impossible) && 1;" +
        "}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testReflectObject1
  public void testReflectObject1() throws Exception {
    testClosureTypes(
        "var goog = {}; goog.reflect = {}; " +
        "goog.reflect.object = function(x, y){};" +
        " function A() {}" +
        "goog.reflect.object(A, {x: 3});",
        null);
  }

// com.google.javascript.jscomp.TypeCheckTest::testReflectObject2
  public void testReflectObject2() throws Exception {
    testClosureTypes(
        "var goog = {}; goog.reflect = {}; " +
        "goog.reflect.object = function(x, y){};" +
        " function f(x) {}" +
        " function A() {}" +
        "goog.reflect.object(A, {x: f(1 + 1)});",
        "actual parameter 1 of f does not match formal parameter\n" +
        "found   : number\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testLends1
  public void testLends1() throws Exception {
    testTypes(
        "function extend(x, y) {}" +
        " function Foo() {}" +
        "extend(Foo,  ({bar: 1}));",
        "Bad type annotation. missing object name in @lends tag");
  }

// com.google.javascript.jscomp.TypeCheckTest::testLends2
  public void testLends2() throws Exception {
    testTypes(
        "function extend(x, y) {}" +
        " function Foo() {}" +
        "extend(Foo,  ({bar: 1}));",
        "Variable Foob not declared before @lends annotation.");
  }

// com.google.javascript.jscomp.TypeCheckTest::testLends3
  public void testLends3() throws Exception {
    testTypes(
        "function extend(x, y) {}" +
        " function Foo() {}" +
        "extend(Foo, {bar: 1});" +
        "alert(Foo.bar);",
        "Property bar never defined on Foo");
  }

// com.google.javascript.jscomp.TypeCheckTest::testLends4
  public void testLends4() throws Exception {
    testTypes(
        "function extend(x, y) {}" +
        " function Foo() {}" +
        "extend(Foo,  ({bar: 1}));" +
        "alert(Foo.bar);");
  }

// com.google.javascript.jscomp.TypeCheckTest::testLends5
  public void testLends5() throws Exception {
    testTypes(
        "function extend(x, y) {}" +
        " function Foo() {}" +
        "extend(Foo, {bar: 1});" +
        "alert((new Foo()).bar);",
        "Property bar never defined on Foo");
  }

// com.google.javascript.jscomp.TypeCheckTest::testLends6
  public void testLends6() throws Exception {
    testTypes(
        "function extend(x, y) {}" +
        " function Foo() {}" +
        "extend(Foo,  ({bar: 1}));" +
        "alert((new Foo()).bar);");
  }

// com.google.javascript.jscomp.TypeCheckTest::testLends7
  public void testLends7() throws Exception {
    testTypes(
        "function extend(x, y) {}" +
        " function Foo() {}" +
        "extend(Foo,  ({bar: 1}));",
        "Bad type annotation. expected closing }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testLends8
  public void testLends8() throws Exception {
    testTypes(
        "function extend(x, y) {}" +
        " var Foo = 3;" +
        "extend(Foo,  ({bar: 1}));",
        "May only lend properties to object types. Foo has type number.");
  }

// com.google.javascript.jscomp.TypeCheckTest::testLends9
  public void testLends9() throws Exception {
    testClosureTypesMultipleWarnings(
        "function extend(x, y) {}" +
        " function Foo() {}" +
        "extend(Foo,  ({bar: 1}));",
        Lists.newArrayList(
            "Bad type annotation. expected closing }",
            "Bad type annotation. missing object name in @lends tag"));
  }

// com.google.javascript.jscomp.TypeCheckTest::testLends10
  public void testLends10() throws Exception {
    testTypes(
        "function defineClass(x) { return function() {}; } " +
        "" +
        "var Foo = defineClass(" +
        "     ({ bar: 1}));" +
        " function f() { return (new Foo()).bar; }",
        "inconsistent return type\n" +
        "found   : number\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testLends11
  public void testLends11() throws Exception {
    testTypes(
        "function defineClass(x, y) { return function() {}; } " +
        "" +
        "var Foo = function() {};" +
        " Foo.prototype.bar = function() { return 3; };" +
        "\n" +
        "var SubFoo = defineClass(Foo, " +
        "     ({\n" +
        "       bar: function() { return 3; }}));" +
        " function f() { return (new SubFoo()).bar(); }",
        "inconsistent return type\n" +
        "found   : number\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testDeclaredNativeTypeEquality
  public void testDeclaredNativeTypeEquality() throws Exception {
    Node n = parseAndTypeCheck(" function Object() {};");
    assertTypeEquals(registry.getNativeType(JSTypeNative.OBJECT_FUNCTION_TYPE),
                 n.getFirstChild().getJSType());
  }

// com.google.javascript.jscomp.TypeCheckTest::testUndefinedVar
  public void testUndefinedVar() throws Exception {
    Node n = parseAndTypeCheck("var undefined;");
    assertTypeEquals(registry.getNativeType(JSTypeNative.VOID_TYPE),
                 n.getFirstChild().getFirstChild().getJSType());
  }

// com.google.javascript.jscomp.TypeCheckTest::testFlowScopeBug1
  public void testFlowScopeBug1() throws Exception {
    Node n = parseAndTypeCheck("\n"
        + "function f(a, b) {\n"
        + ""
        + "var i = 0;"
        + "for (; (i + a) < b; ++i) {}}");

    
    assertTypeEquals(registry.getNativeType(JSTypeNative.NUMBER_TYPE),
        n.getFirstChild().getLastChild().getLastChild().getFirstChild()
        .getNext().getFirstChild().getJSType());
  }

// com.google.javascript.jscomp.TypeCheckTest::testFlowScopeBug2
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

    
    assertTypeEquals(registry.createNullableType(registry.getType("Foo")),
        n.getLastChild().getLastChild().getLastChild().getLastChild()
        .getLastChild().getLastChild().getJSType());
  }

// com.google.javascript.jscomp.TypeCheckTest::testAddSingletonGetter
  public void testAddSingletonGetter() {
    Node n = parseAndTypeCheck(
        " function Foo() {};\n" +
        "goog.addSingletonGetter(Foo);");
    ObjectType o = (ObjectType) n.getFirstChild().getJSType();
    assertEquals("function (): Foo",
        o.getPropertyType("getInstance").toString());
    assertEquals("Foo", o.getPropertyType("instance_").toString());
  }

// com.google.javascript.jscomp.TypeCheckTest::testTypeCheckStandaloneAST
  public void testTypeCheckStandaloneAST() throws Exception {
    Node n = compiler.parseTestCode("function Foo() { }");
    typeCheck(n);
    MemoizedScopeCreator scopeCreator = new MemoizedScopeCreator(
        new TypedScopeCreator(compiler));
    Scope topScope = scopeCreator.createScope(n, null);

    Node second = compiler.parseTestCode("new Foo");

    Node externs = new Node(Token.BLOCK);
    Node externAndJsRoot = new Node(Token.BLOCK, externs, second);
    externAndJsRoot.setIsSyntheticBlock(true);

    new TypeCheck(
        compiler,
        new SemanticReverseAbstractInterpreter(
            compiler.getCodingConvention(), registry),
        registry, topScope, scopeCreator, CheckLevel.WARNING)
        .process(null, second);

    assertEquals(1, compiler.getWarningCount());
    assertEquals("cannot instantiate non-constructor",
        compiler.getWarnings()[0].description);
  }

// com.google.javascript.jscomp.TypeCheckTest::testUpdateParameterTypeOnClosure
  public void testUpdateParameterTypeOnClosure() throws Exception {
    testTypes(
        "\n" +
        "function Object(opt_value) {}\n" +
        "\n" +
        "function Function(var_args) {}\n" +
        "\n" +
        
        
        "Object.prototype.constructor = function() {};\n",
        "\n" +
        "function f(fn) {}\n" +
        "f(function(g) { });\n",
        null,
        false);
  }

// com.google.javascript.jscomp.TypeCheckTest::testTemplatedThisType1
  public void testTemplatedThisType1() throws Exception {
    testTypes(
        "\n" +
        "function Foo() {}\n" +
        "\n" +
        "Foo.prototype.method = function() {};\n" +
        "\n" +
        "function Bar() {}\n" +
        "var g = new Bar().method();\n" +
        "\n" +
        "function compute(a) {};\n" +
        "compute(g);\n",

        "actual parameter 1 of compute does not match formal parameter\n" +
        "found   : Bar\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTemplatedThisType2
  public void testTemplatedThisType2() throws Exception {
    testTypes(
        "\n" +
        "Array.prototype.method = function() {};\n" +
        "(function(){\n" +
        "  Array.prototype.method.call(arguments);" +
        "})();");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTemplateType1
  public void testTemplateType1() throws Exception {
    testTypes(
        "\n" +
        "function f(x, y, z) {}\n" +
        "f(this, this, function() { this });");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTemplateType2
  public void testTemplateType2() throws Exception {
    
    
    testTypes(
        "\n" +
        "function f(x, y) {}\n" +
        "f(0, function() {});");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTemplateType3
  public void testTemplateType3() throws Exception {
    testTypes(
        "\n" +
        "function call(v, f) { f.call(null, v); }" +
        " var s;" +
        "call(3, function(x) {" +
        " x = true;" +
        " s = x;" +
        "});",
        "assignment\n" +
        "found   : boolean\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTemplateType4
  public void testTemplateType4() throws Exception {
    testTypes(
        "\n" +
        "function fn(p) { return p; }\n" +
        " var x;" +
        "x = fn(3, null);",
        "assignment\n" +
        "found   : (null|number)\n" +
        "required: Object");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTemplateType5
  public void testTemplateType5() throws Exception {
    compiler.getOptions().setCodingConvention(new GoogleCodingConvention());
    testTypes(
        "var CGI_PARAM_RETRY_COUNT = 'rc';" +
        "" +
        "\n" +
        "function fn(p) { return p; }\n" +
        " var x;" +
        "" +
        "\n" +
        "function aScope() {\n" +
        "  x = fn(CGI_PARAM_RETRY_COUNT, 1);\n" +
        "}",
        "assignment\n" +
        "found   : (number|string)\n" +
        "required: Object");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTemplateType6
  public void testTemplateType6() throws Exception {
    testTypes(
        "\n" +
        "function fn(arr, f) { return arr[0]; }\n" +
        " function g(arr) {" +
        "   var x = fn.call(null, arr, null);" +
        "}",
        "initializing variable\n" +
        "found   : number\n" +
        "required: Object");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTemplateType7
  public void testTemplateType7() throws Exception {
    
    
    
    testTypes(
        "\n" +
        "var query = [];\n" +
        "query.push(1);\n");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTemplateType8
  public void testTemplateType8() throws Exception {
    testTypes(
        "\n" +
        "function Bar() {}\n" +
        "\n" +
        "function fn(bar) {}\n" +
        " function g(bar) {" +
        "   var x = fn(bar);" +
        "}",
        "initializing variable\n" +
        "found   : number\n" +
        "required: Object");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTemplateType9
  public void testTemplateType9() throws Exception {
    
    testTypes(
        "\n" +
        "function Bar() {}\n" +
        "\n" +
        "function fn(bar) {}\n" +
        " function g(bar) {" +
        "   var x = fn(bar);" +
        "}",
        "initializing variable\n" +
        "found   : number\n" +
        "required: Object");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTemplateType10
  public void testTemplateType10() throws Exception {
    
    
    testTypes(
        "\n" +
        "function Bar() {}\n" +
        "\n" +
        "" +
        " var x;" +
        " var y;" +
        "y = x;");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTemplateType11
  public void testTemplateType11() throws Exception {
    
    
    testTypes(
        "\n" +
        "function Foo() {}\n" +
        "" +
        "\n" +
        "function A() {}\n" +
        "" +
        "\n" +
        "function B() {}\n" +
        "" +
        " var a = new A();\n" +
        " var b = new B();",
        "initializing variable\n" +
        "found   : B\n" +
        "required: Foo.<string>");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTemplateType12
  public void testTemplateType12() throws Exception {
    
    
    testTypes(
        "\n" +
        "function Foo() {}\n" +
        "" +
        "\n" +
        "function A() {}\n" +
        "" +
        "\n" +
        "function B() {}\n" +
        "" +
        " var a = new A();\n" +
        " var b = new B();",
        "initializing variable\n" +
        "found   : B\n" +
        "required: Foo.<string>");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTemplateType13
  public void testTemplateType13() throws Exception {
    
    
    testTypes(
        "\n" +
        "function Foo() {}\n" +
        "" +
        "\n" +
        "function A() {}\n" +
        "" +
        "var a1 = new A();\n" +
        "var a2 =  (new A());\n" +
        "var a3 =  (new A());\n" +
        " var f1 = a1;\n" +
        " var f2 = a2;\n" +
        " var f3 = a3;",
        "initializing variable\n" +
        "found   : A.<number>\n" +
        "required: Foo.<string>");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTemplateType14
  public void testTemplateType14() throws Exception {
    
    
    testTypes(
        "\n" +
        "function Foo() {}\n" +
        "" +
        "\n" +
        "function A() {}\n" +
        "" +
        "var a1 = new A();\n" +
        "var a2 =  (new A());\n" +
        "var a3 =  (new A());\n" +
        " var f1 = a1;\n" +
        " var f2 = a2;\n" +
        " var f3 = a3;",
        "initializing variable\n" +
        "found   : A.<number>\n" +
        "required: Foo.<string>");
  }

// com.google.javascript.jscomp.TypeCheckTest::testFunctionLiteralDefinedThisArgument
  public void testFunctionLiteralDefinedThisArgument() throws Exception {
    testTypes(""
        + "\n"
        + "function baz(fn, opt_obj) {}\n"
        + "baz(function() { this; }, {});");
  }

// com.google.javascript.jscomp.TypeCheckTest::testFunctionLiteralDefinedThisArgument2
  public void testFunctionLiteralDefinedThisArgument2() throws Exception {
    testTypes(""
        + " function f(x) {}"
        + "\n"
        + "function baz(fn, opt_obj) {}\n"
        + "function g() { baz(function() { f(this.length); }, []); }",
        "actual parameter 1 of f does not match formal parameter\n"
        + "found   : number\n"
        + "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testFunctionLiteralUnreadNullThisArgument
  public void testFunctionLiteralUnreadNullThisArgument() throws Exception {
    testTypes(""
        + "\n"
        + "function baz(fn, opt_obj) {}\n"
        + "baz(function() {}, null);");
  }

// com.google.javascript.jscomp.TypeCheckTest::testUnionTemplateThisType
  public void testUnionTemplateThisType() throws Exception {
    testTypes(
        " function F() {}" +
        " function g() { return []; }" +
        " function h(x) { }" +
        "\n" +
        "function f(x, y) {}\n" +
        "f(g(), function() { h(this); });",
        "actual parameter 1 of h does not match formal parameter\n" +
        "found   : (Array|F|null)\n" +
        "required: (F|null)");
  }

// com.google.javascript.jscomp.TypeCheckTest::testActiveXObject
  public void testActiveXObject() throws Exception {
    testTypes(
        " var x = new ActiveXObject();" +
        " var y = new ActiveXObject();");
  }

// com.google.javascript.jscomp.TypeCheckTest::testRecordType1
  public void testRecordType1() throws Exception {
    testTypes(
        "" +
        "function f(x) {}" +
        "f({});",
        "actual parameter 1 of f does not match formal parameter\n" +
        "found   : {prop: (number|undefined)}\n" +
        "required: {prop: number}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testRecordType2
  public void testRecordType2() throws Exception {
    testTypes(
        "" +
        "function f(x) {}" +
        "f({});");
  }

// com.google.javascript.jscomp.TypeCheckTest::testRecordType3
  public void testRecordType3() throws Exception {
    testTypes(
        "" +
        "function f(x) {}" +
        "f({prop: 'x'});",
        "actual parameter 1 of f does not match formal parameter\n" +
        "found   : {prop: (number|string)}\n" +
        "required: {prop: number}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testRecordType4
  public void testRecordType4() throws Exception {
    
    
    
    testClosureTypesMultipleWarnings(
        "" +
        "function f(x) {}" +
        "" +
        "function g(x) {}" +
        "var x = {}; f(x); g(x);",
        Lists.newArrayList(
            "actual parameter 1 of f does not match formal parameter\n" +
            "found   : {prop: (number|string|undefined)}\n" +
            "required: {prop: (number|undefined)}",
            "actual parameter 1 of g does not match formal parameter\n" +
            "found   : {prop: (number|string|undefined)}\n" +
            "required: {prop: (string|undefined)}"));
  }

// com.google.javascript.jscomp.TypeCheckTest::testRecordType5
  public void testRecordType5() throws Exception {
    testTypes(
        "" +
        "function f(x) {}" +
        "" +
        "function g(x) {}" +
        "var x = {}; f(x); g(x);");
  }

// com.google.javascript.jscomp.TypeCheckTest::testRecordType6
  public void testRecordType6() throws Exception {
    testTypes(
        "" +
        "function f() { return {}; }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testRecordType7
  public void testRecordType7() throws Exception {
    testTypes(
        "" +
        "function f() { var x = {}; g(x); return x; }" +
        "" +
        "function g(x) {}",
        "actual parameter 1 of g does not match formal parameter\n" +
        "found   : {prop: (number|undefined)}\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testRecordType8
  public void testRecordType8() throws Exception {
    testTypes(
        "" +
        "function f() { var x = {prop: 3}; g(x.prop); return x; }" +
        "" +
        "function g(x) {}",
        "actual parameter 1 of g does not match formal parameter\n" +
        "found   : number\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testDuplicateRecordFields1
  public void testDuplicateRecordFields1() throws Exception {
    testTypes(""
         + "function f(a) {};",
         "Parse error. Duplicate record field x");
  }

// com.google.javascript.jscomp.TypeCheckTest::testDuplicateRecordFields2
  public void testDuplicateRecordFields2() throws Exception {
    testTypes(""
         + "function f(a) {};",
         new String[] {"Bad type annotation. Unknown type x",
           "Parse error. Duplicate record field number",
           "Bad type annotation. Unknown type y"});
  }

// com.google.javascript.jscomp.TypeCheckTest::testMultipleExtendsInterface1
  public void testMultipleExtendsInterface1() throws Exception {
    testTypes(" function base1() {}\n"
        + " function base2() {}\n"
        + "\n"
        + "function derived() {}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testMultipleExtendsInterface2
  public void testMultipleExtendsInterface2() throws Exception {
    testTypes(
        "function Int0() {};" +
        "function Int1() {};" +
        "Int0.prototype.foo = function() {};" +
        "" +
        "function Int2() {};" +
        "function Foo() {};",
        "property foo on interface Int0 is not implemented by type Foo");
  }

// com.google.javascript.jscomp.TypeCheckTest::testMultipleExtendsInterface3
  public void testMultipleExtendsInterface3() throws Exception {
    testTypes(
        "function Int0() {};" +
        "function Int1() {};" +
        "Int1.prototype.foo = function() {};" +
        "" +
        "function Int2() {};" +
        "function Foo() {};",
        "property foo on interface Int1 is not implemented by type Foo");
  }

// com.google.javascript.jscomp.TypeCheckTest::testMultipleExtendsInterface4
  public void testMultipleExtendsInterface4() throws Exception {
    testTypes(
        "function Int0() {};" +
        "function Int1() {};" +
        "" +
        "function Int2() {};" +
        "function Foo() {};",
        "Int2 @extends non-object type number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testMultipleExtendsInterface5
  public void testMultipleExtendsInterface5() throws Exception {
    testTypes(
        "function Int0() {};" +
        "function Int1() {};" +
        "" +
        "" +
        "function Int2() {};",
        "Int2 cannot extend this type; interfaces can only extend interfaces");
  }

// com.google.javascript.jscomp.TypeCheckTest::testMultipleExtendsInterface6
  public void testMultipleExtendsInterface6() throws Exception {
    testTypes(
        "function Super1() {};" +
        "function Super2() {};" +
        "Super2.prototype.foo = function(bar) {};" +
        "function Sub() {};" +
        "Sub.prototype.foo =\n" +
        "function(bar) {};",
        "mismatch of the foo property type and the type of the property it " +
        "overrides from superclass Super2\n" +
        "original: function (this:Super2, number): undefined\n" +
        "override: function (this:Sub, string): undefined");
  }

// com.google.javascript.jscomp.TypeCheckTest::testMultipleExtendsInterfaceAssignment
  public void testMultipleExtendsInterfaceAssignment() throws Exception {
    testTypes("var I1 = function() {};\n" +
        " var I2 = function() {}\n" +
        "" +
        "var I3 = function() {};\n" +
        "var T = function() {};\n" +
        "var t = new T();\n" +
         "var i1 = t;\n" +
         "var i2 = t;\n" +
         "var i3 = t;\n" +
         "i1 = i3;\n" +
         "i2 = i3;\n");
  }

// com.google.javascript.jscomp.TypeCheckTest::testMultipleExtendsInterfaceParamPass
  public void testMultipleExtendsInterfaceParamPass() throws Exception {
    testTypes("var I1 = function() {};\n" +
        " var I2 = function() {}\n" +
        "" +
        "var I3 = function() {};\n" +
        "var T = function() {};\n" +
        "var t = new T();\n" +
        "function foo(x,y,z){};\n" +
        "foo(t,t,t)\n");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBadMultipleExtendsClass
  public void testBadMultipleExtendsClass() throws Exception {
    testTypes(" function base1() {}\n"
        + " function base2() {}\n"
        + "\n"
        + "function derived() {}",
        "Bad type annotation. type annotation incompatible "
        + "with other annotations");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInterfaceExtendsResolution
  public void testInterfaceExtendsResolution() throws Exception {
    testTypes(" function B() {};\n" +
        " function C() {};\n" +
        " function A() {};");
  }

// com.google.javascript.jscomp.TypeCheckTest::testPropertyCanBeDefinedInObject
  public void testPropertyCanBeDefinedInObject() throws Exception {
    testTypes(" function I() {};" +
        "I.prototype.bar = function() {};" +
        " var foo;" +
        "foo.bar();");
  }

// com.google.javascript.jscomp.TypeCheckTest::testExtendedInterfacePropertiesCompatibility1
  public void testExtendedInterfacePropertiesCompatibility1() throws Exception {
    testTypes(
        "function Int0() {};" +
        "function Int1() {};" +
        "" +
        "Int0.prototype.foo;" +
        "" +
        "Int1.prototype.foo;" +
        "" +
        "function Int2() {};",
        "Interface Int2 has a property foo with incompatible types in its " +
        "super interfaces Int0 and Int1");
  }

// com.google.javascript.jscomp.TypeCheckTest::testExtendedInterfacePropertiesCompatibility2
  public void testExtendedInterfacePropertiesCompatibility2() throws Exception {
    testTypes(
        "function Int0() {};" +
        "function Int1() {};" +
        "function Int2() {};" +
        "" +
        "Int0.prototype.foo;" +
        "" +
        "Int1.prototype.foo;" +
        "" +
        "Int2.prototype.foo;" +
        "" +
        "function Int3() {};",
        new String[] {
            "Interface Int3 has a property foo with incompatible types in " +
            "its super interfaces Int0 and Int1",
            "Interface Int3 has a property foo with incompatible types in " +
            "its super interfaces Int1 and Int2"
        });
  }

// com.google.javascript.jscomp.TypeCheckTest::testExtendedInterfacePropertiesCompatibility3
  public void testExtendedInterfacePropertiesCompatibility3() throws Exception {
    testTypes(
        "function Int0() {};" +
        "function Int1() {};" +
        "" +
        "Int0.prototype.foo;" +
        "" +
        "Int1.prototype.foo;" +
        " function Int2() {};" +
        "" +
        "function Int3() {};",
        "Interface Int3 has a property foo with incompatible types in its " +
        "super interfaces Int0 and Int1");
  }

// com.google.javascript.jscomp.TypeCheckTest::testExtendedInterfacePropertiesCompatibility4
  public void testExtendedInterfacePropertiesCompatibility4() throws Exception {
    testTypes(
        "function Int0() {};" +
        " function Int1() {};" +
        "" +
        "Int0.prototype.foo;" +
        "function Int2() {};" +
        " function Int3() {};" +
        "" +
        "Int2.prototype.foo;" +
        "" +
        "function Int4() {};",
        "Interface Int4 has a property foo with incompatible types in its " +
        "super interfaces Int0 and Int2");
  }

// com.google.javascript.jscomp.TypeCheckTest::testExtendedInterfacePropertiesCompatibility5
  public void testExtendedInterfacePropertiesCompatibility5() throws Exception {
    testTypes(
        "function Int0() {};" +
        "function Int1() {};" +
        "" +
        "Int0.prototype.foo;" +
        "" +
        "Int1.prototype.foo;" +
        " function Int2() {};" +
        "" +
        "function Int3() {};" +
        "function Int4() {};" +
        "" +
        "Int4.prototype.foo;" +
        "" +
        "function Int5() {};",
        new String[] {
            "Interface Int3 has a property foo with incompatible types in its" +
            " super interfaces Int0 and Int1",
            "Interface Int5 has a property foo with incompatible types in its" +
            " super interfaces Int1 and Int4"});
  }

// com.google.javascript.jscomp.TypeCheckTest::testExtendedInterfacePropertiesCompatibility6
  public void testExtendedInterfacePropertiesCompatibility6() throws Exception {
    testTypes(
        "function Int0() {};" +
        "function Int1() {};" +
        "" +
        "Int0.prototype.foo;" +
        "" +
        "Int1.prototype.foo;" +
        " function Int2() {};" +
        "" +
        "function Int3() {};" +
        "function Int4() {};" +
        "" +
        "Int4.prototype.foo;" +
        "" +
        "function Int5() {};",
        "Interface Int3 has a property foo with incompatible types in its" +
        " super interfaces Int0 and Int1");
  }

// com.google.javascript.jscomp.TypeCheckTest::testExtendedInterfacePropertiesCompatibility7
  public void testExtendedInterfacePropertiesCompatibility7() throws Exception {
    testTypes(
        "function Int0() {};" +
        "function Int1() {};" +
        "" +
        "Int0.prototype.foo;" +
        "" +
        "Int1.prototype.foo;" +
        " function Int2() {};" +
        "" +
        "function Int3() {};" +
        "function Int4() {};" +
        "" +
        "Int4.prototype.foo;" +
        "" +
        "function Int5() {};",
        new String[] {
            "Interface Int3 has a property foo with incompatible types in its" +
            " super interfaces Int0 and Int1",
            "Interface Int5 has a property foo with incompatible types in its" +
            " super interfaces Int1 and Int4"});
  }

// com.google.javascript.jscomp.TypeCheckTest::testExtendedInterfacePropertiesCompatibility8
  public void testExtendedInterfacePropertiesCompatibility8() throws Exception {
    testTypes(
        "function Int0() {};" +
        "function Int1() {};" +
        "" +
        "Int0.prototype.foo;" +
        "" +
        "Int1.prototype.bar;" +
        " function Int2() {};" +
        "" +
        "function Int3() {};" +
        "function Int4() {};" +
        "" +
        "Int4.prototype.foo;" +
        "" +
        "Int4.prototype.bar;" +
        "" +
        "function Int5() {};",
        new String[] {
            "Interface Int5 has a property bar with incompatible types in its" +
            " super interfaces Int1 and Int4",
            "Interface Int5 has a property foo with incompatible types in its" +
            " super interfaces Int0 and Int4"});
  }

// com.google.javascript.jscomp.TypeCheckTest::testExtendedInterfacePropertiesCompatibility9
  public void testExtendedInterfacePropertiesCompatibility9() throws Exception {
    testTypes(
        "function Int0() {};" +
        "function Int1() {};" +
        "" +
        "Int0.prototype.foo;" +
        "" +
        "Int1.prototype.foo;" +
        "" +
        "function Int2() {};",
        "Interface Int2 has a property foo with incompatible types in its " +
        "super interfaces Int0.<number> and Int1.<string>");
  }

// com.google.javascript.jscomp.TypeCheckTest::testGenerics1
  public void testGenerics1() throws Exception {
    String fnDecl = " \n" +
        "function f(x,y) { return y(x); }\n";

    testTypes(
        fnDecl +
        "" +
        "var out;" +
        "" +
        "var result = f('hi', function(x){ out = x; return x; });");

    testTypes(
        fnDecl +
        "" +
        "var out;" +
        "var result = f(0, function(x){ out = x; return x; });",
        "assignment\n" +
        "found   : number\n" +
        "required: string");

    testTypes(
        fnDecl +
        "var out;" +
        "" +
        "var result = f(0, function(x){ out = x; return x; });",
        "assignment\n" +
        "found   : number\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testFilter0
  public void testFilter0()
      throws Exception {
    testTypes(
        "\n" +
        "var filter = function(arr){};\n" +

        "" +
        "var arr;\n" +
        "" +
        "var result = filter(arr);");
  }

// com.google.javascript.jscomp.TypeCheckTest::testFilter1
  public void testFilter1()
      throws Exception {
    testTypes(
        "\n" +
        "var filter = function(arr){};\n" +

        "" +
        "var arr;\n" +
        "" +
        "var result = filter(arr);");
  }

// com.google.javascript.jscomp.TypeCheckTest::testFilter2
  public void testFilter2()
      throws Exception {
    testTypes(
        "\n" +
        "var filter = function(arr){};\n" +

        "" +
        "var arr;\n" +
        "" +
        "var result = filter(arr);",
        "initializing variable\n" +
        "found   : Array.<string>\n" +
        "required: Array.<number>");
  }

// com.google.javascript.jscomp.TypeCheckTest::testFilter3
  public void testFilter3()
      throws Exception {
    testTypes(
        "\n" +
        "var filter = function(arr){};\n" +

        "" +
        "var arr;\n" +
        "" +
        "var result = filter(arr);",
        "initializing variable\n" +
        "found   : (Array.<string>|null)\n" +
        "required: (Array.<number>|null)");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBackwardsInferenceGoogArrayFilter1
  public void testBackwardsInferenceGoogArrayFilter1()
      throws Exception {
    testClosureTypes(
        CLOSURE_DEFS +
        "" +
        "var arr;\n" +
        "" +
        "var result = goog.array.filter(" +
        "   arr," +
        "   function(item,index,src) {return false;});",
        "initializing variable\n" +
        "found   : Array.<string>\n" +
        "required: Array.<number>");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBackwardsInferenceGoogArrayFilter2
  public void testBackwardsInferenceGoogArrayFilter2() throws Exception {
    testClosureTypes(
        CLOSURE_DEFS +
        "" +
        "var out;" +
        "" +
        "var arr;\n" +
        "var out4 = goog.array.filter(" +
        "   arr," +
        "   function(item,index,src) {out = item;});",
        "assignment\n" +
        "found   : string\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBackwardsInferenceGoogArrayFilter3
  public void testBackwardsInferenceGoogArrayFilter3() throws Exception {
    testClosureTypes(
        CLOSURE_DEFS +
        "" +
        "var out;" +
        " var arr;\n" +
        "var result = goog.array.filter(" +
        "   arr," +
        "   function(item,index,src) {out = index;});",
        "assignment\n" +
        "found   : number\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBackwardsInferenceGoogArrayFilter4
  public void testBackwardsInferenceGoogArrayFilter4() throws Exception {
    testClosureTypes(
        CLOSURE_DEFS +
        "" +
        "var out;" +
        " var arr;\n" +
        "var out4 = goog.array.filter(" +
        "   arr," +
        "   function(item,index,srcArr) {out = srcArr;});",
        "assignment\n" +
        "found   : (null|{length: number})\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testCatchExpression1
  public void testCatchExpression1() throws Exception {
    testTypes(
        "function fn() {" +
        "  " +
        "  var out = 0;" +
        "  try {\n" +
        "    foo();\n" +
        "  } catch ( e) {\n" +
        "    out = e;" +
        "  }" +
        "}\n",
        "assignment\n" +
        "found   : string\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testCatchExpression2
  public void testCatchExpression2() throws Exception {
    testTypes(
        "function fn() {" +
        "  " +
        "  var out = 0;" +
        "  " +
        "  var e;" +
        "  try {\n" +
        "    foo();\n" +
        "  } catch (e) {\n" +
        "    out = e;" +
        "  }" +
        "}\n");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTemplatized1
  public void testTemplatized1() throws Exception {
    testTypes(
        "" +
        "var arr1 = [];\n" +
        "" +
        "var arr2 = [];\n" +
        "arr1 = arr2;",
        "assignment\n" +
        "found   : Array.<number>\n" +
        "required: Array.<string>");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTemplatized2
  public void testTemplatized2() throws Exception {
    testTypes(
        "" +
        "var arr1 = ([]);\n",
        "initializing variable\n" +
        "found   : Array.<number>\n" +
        "required: Array.<string>");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTemplatized3
  public void testTemplatized3() throws Exception {
    testTypes(
        "" +
        "var arr1 = ([]);\n",
        "initializing variable\n" +
        "found   : Array.<number>\n" +
        "required: (Array.<string>|null)");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTemplatized4
  public void testTemplatized4() throws Exception {
    testTypes(
        "" +
        "var arr1 = [];\n" +
        "" +
        "var arr2 = arr1;\n",
        "initializing variable\n" +
        "found   : (Array.<string>|null)\n" +
        "required: (Array.<number>|null)");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTemplatized5
  public void testTemplatized5() throws Exception {
    testTypes(
        "\n" +
        "var some = function(obj) {" +
        "  for (var key in obj) if (obj[key]) return true;" +
        "};" +
        " function f() { return []; }" +
        " function g() { return []; }" +
        "some(f());\n" +
        "some(g());\n");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTemplatized6
  public void testTemplatized6() throws Exception {
    testTypes(
        " function I(){}\n" +
        "\n" +
        "I.prototype.method;\n" +
        "" +
        " function C(){}\n" +
        " C.prototype.method = function(a) {}\n" +
        "" +
        " var some = new C().method('str');",
        "initializing variable\n" +
        "found   : string\n" +
        "required: null");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTemplatized7
  public void testTemplatized7() throws Exception {
    testTypes(
        " function I(){}\n" +

        "\n" +
        "I.prototype.method;\n" +

        " function C(){}\n" +
        " C.prototype.method = function(a) {}\n" +

        " var some = new C().method('str');",

        "initializing variable\n" +
        "found   : (number|string)\n" +
        "required: null");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTemplatized9
  public void testTemplatized9() throws Exception {
    testTypes(
        " function I(){}\n" +

        "\n" +
        "I.prototype.method;\n" +

        " function C(a){}\n" +
        " C.prototype.method = function(a) {}\n" +

        " var some = new C(1).method('str');",

        "initializing variable\n" +
        "found   : (number|string)\n" +
        "required: null");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTemplatized10
  public void testTemplatized10() throws Exception {
    testTypes(
        "\n" +
        "function Parent() {};\n" +
        "\n" +
        "\n" +
        "Parent.prototype.method = function(x) {};\n" +
        "\n" +
        "\n" +
        "function Child() {};\n" +
        "Child.prototype = new Parent();\n" +
        "\n" +
        "(new Child()).method(123); \n",

        "actual parameter 1 of Parent.prototype.method does not match formal parameter\n" +
        "found   : number\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTemplatized11
  public void testTemplatized11() throws Exception {
    testTypes(
        "\n" +
        "function C() {}\n" +
        "\n" +
        "\n" +
        "C.prototype.method = function (a) {};\n" +
        "\n" +
        
        " var x = new C().method(1);");
  }

// com.google.javascript.jscomp.TypeCheckTest::testIssue1058
  public void testIssue1058() throws Exception {
    testTypes(
        "\n" +
        "var Class = function() {};\n" +
        "\n" +
        "\n" +
        "Class.prototype.foo = function(a) {\n" +
        "  return 'string';\n" +
        "};\n" +
        "\n" +
        "\n" +
        "var a = function(a) { return '' };\n" +
        "\n" +
        "new Class().foo(a);");
  }

// com.google.javascript.jscomp.TypeCheckTest::testUnknownTypeReport
  public void testUnknownTypeReport() throws Exception {
    compiler.getOptions().setWarningLevel(DiagnosticGroups.REPORT_UNKNOWN_TYPES,
        CheckLevel.WARNING);
    testTypes("function id(x) { return x; }",
        "could not determine the type of this expression");
  }

// com.google.javascript.jscomp.TypeCheckTest::testUnknownTypeDisabledByDefault
  public void testUnknownTypeDisabledByDefault() throws Exception {
    testTypes("function id(x) { return x; }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTemplatizedTypeSubtypes2
  public void testTemplatizedTypeSubtypes2() throws Exception {
    JSType arrayOfNumber = createTemplatizedType(
        ARRAY_TYPE, NUMBER_TYPE);
    JSType arrayOfString = createTemplatizedType(
        ARRAY_TYPE, STRING_TYPE);
    assertFalse(arrayOfString.isSubtype(createUnionType(arrayOfNumber, NULL_VOID)));

  }

// com.google.javascript.jscomp.TypeCheckTest::testNonexistentPropertyAccessOnStruct
  public void testNonexistentPropertyAccessOnStruct() throws Exception {
    testTypes(
        "\n" +
        "var A = function() {};\n" +
        "\n" +
        "function foo(a) {\n" +
        "  if (a.bar) { a.bar(); }\n" +
        "}",
        "Property bar never defined on A");
  }

// com.google.javascript.jscomp.TypeCheckTest::testNonexistentPropertyAccessOnStructOrObject
  public void testNonexistentPropertyAccessOnStructOrObject() throws Exception {
    testTypes(
        "\n" +
        "var A = function() {};\n" +
        "\n" +
        "function foo(a) {\n" +
        "  if (a.bar) { a.bar(); }\n" +
        "}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testNonexistentPropertyAccessOnExternStruct
  public void testNonexistentPropertyAccessOnExternStruct() throws Exception {
    testTypes(
        "\n" +
        "var A = function() {};",
        "\n" +
        "function foo(a) {\n" +
        "  if (a.bar) { a.bar(); }\n" +
        "}",
        "Property bar never defined on A", false);
  }

// com.google.javascript.jscomp.TypeCheckTest::testNonexistentPropertyAccessStructSubtype
  public void testNonexistentPropertyAccessStructSubtype() throws Exception {
    testTypes(
        "\n" +
        "var A = function() {};" +
        "" +
        "\n" +
        "var B = function() { this.bar = function(){}; };" +
        "" +
        "\n" +
        "function foo(a) {\n" +
        "  if (a.bar) { a.bar(); }\n" +
        "}",
        "Property bar never defined on A", false);
  }

// com.google.javascript.jscomp.TypeCheckTest::testNonexistentPropertyAccessStructSubtype2
  public void testNonexistentPropertyAccessStructSubtype2() throws Exception {
    testTypes(
        "\n" +
        "function Foo() {\n" +
        "  this.x = 123;\n" +
        "}\n" +
        "var objlit =  { y: 234 };\n" +
        "Foo.prototype = objlit;\n" +
        "var n = objlit.x;\n",
        "Property x never defined on Foo.prototype", false);
  }

// com.google.javascript.jscomp.TypeCheckTest::testIssue1024
  public void testIssue1024() throws Exception {
     testTypes(
        "\n" +
        "function f(a) {\n" +
        "  a.prototype = '__proto'\n" +
        "}\n" +
        "\n" +
        "function g(b) {\n" +
        "  return b.prototype\n" +
        "}\n");
     
     testTypes(
        "\n" +
        "function f(a) {\n" +
        "  a.prototype = {foo:3};\n" +
        "}\n" +
        "\n" +
        "function g(b) {\n" +
        "  b.prototype = function(){};\n" +
        "}\n",
        "assignment to property prototype of Object\n" +
        "found   : {foo: number}\n" +
        "required: function (): undefined");
  }

// com.google.javascript.jscomp.TypeInferenceTest::testAssumption
  public void testAssumption() {
    assuming("x", NUMBER_TYPE);
    inFunction("");
    verify("x", NUMBER_TYPE);
  }

// com.google.javascript.jscomp.TypeInferenceTest::testVar
  public void testVar() {
    inFunction("var x = 1;");
    verify("x", NUMBER_TYPE);
  }

// com.google.javascript.jscomp.TypeInferenceTest::testEmptyVar
  public void testEmptyVar() {
    inFunction("var x;");
    verify("x", VOID_TYPE);
  }

// com.google.javascript.jscomp.TypeInferenceTest::testAssignment
  public void testAssignment() {
    assuming("x", OBJECT_TYPE);
    inFunction("x = 1;");
    verify("x", NUMBER_TYPE);
  }

// com.google.javascript.jscomp.TypeInferenceTest::testExprWithinCast
  public void testExprWithinCast() {
    assuming("x", OBJECT_TYPE);
    inFunction(" (x = 1);");
    verify("x", NUMBER_TYPE);
  }

// com.google.javascript.jscomp.TypeInferenceTest::testGetProp
  public void testGetProp() {
    assuming("x", createNullableType(OBJECT_TYPE));
    inFunction("x.y();");
    verify("x", OBJECT_TYPE);
  }

// com.google.javascript.jscomp.TypeInferenceTest::testGetElemDereference
  public void testGetElemDereference() {
    assuming("x", createUndefinableType(OBJECT_TYPE));
    inFunction("x['z'] = 3;");
    verify("x", OBJECT_TYPE);
  }

// com.google.javascript.jscomp.TypeInferenceTest::testIf1
  public void testIf1() {
    assuming("x", createNullableType(OBJECT_TYPE));
    inFunction("var y = {}; if (x) { y = x; }");
    verifySubtypeOf("y", OBJECT_TYPE);
  }

// com.google.javascript.jscomp.TypeInferenceTest::testIf1a
  public void testIf1a() {
    assuming("x", createNullableType(OBJECT_TYPE));
    inFunction("var y = {}; if (x != null) { y = x; }");
    verifySubtypeOf("y", OBJECT_TYPE);
  }

// com.google.javascript.jscomp.TypeInferenceTest::testIf2
  public void testIf2() {
    assuming("x", createNullableType(OBJECT_TYPE));
    inFunction("var y = x; if (x) { y = x; } else { y = {}; }");
    verifySubtypeOf("y", OBJECT_TYPE);
  }

// com.google.javascript.jscomp.TypeInferenceTest::testIf3
  public void testIf3() {
    assuming("x", createNullableType(OBJECT_TYPE));
    inFunction("var y = 1; if (x) { y = x; }");
    verify("y", createUnionType(OBJECT_TYPE, NUMBER_TYPE));
  }

// com.google.javascript.jscomp.TypeInferenceTest::testPropertyInference1
  public void testPropertyInference1() {
    ObjectType thisType = registry.createAnonymousObjectType(null);
    thisType.defineDeclaredProperty("foo",
        createUndefinableType(STRING_TYPE), null);
    assumingThisType(thisType);
    inFunction("var y = 1; if (this.foo) { y = this.foo; }");
    verify("y", createUnionType(NUMBER_TYPE, STRING_TYPE));
  }

// com.google.javascript.jscomp.TypeInferenceTest::testPropertyInference2
  public void testPropertyInference2() {
    ObjectType thisType = registry.createAnonymousObjectType(null);
    thisType.defineDeclaredProperty("foo",
        createUndefinableType(STRING_TYPE), null);
    assumingThisType(thisType);
    inFunction("var y = 1; this.foo = 'x'; y = this.foo;");
    verify("y", STRING_TYPE);
  }

// com.google.javascript.jscomp.TypeInferenceTest::testPropertyInference3
  public void testPropertyInference3() {
    ObjectType thisType = registry.createAnonymousObjectType(null);
    thisType.defineDeclaredProperty("foo",
        createUndefinableType(STRING_TYPE), null);
    assumingThisType(thisType);
    inFunction("var y = 1; this.foo = x; y = this.foo;");
    verify("y", CHECKED_UNKNOWN_TYPE);
  }

// com.google.javascript.jscomp.TypeInferenceTest::testAssert1
  public void testAssert1() {
    JSType startType = createNullableType(OBJECT_TYPE);
    assuming("x", startType);
    inFunction("out1 = x; goog.asserts.assert(x); out2 = x;");
    verify("out1", startType);
    verify("out2", OBJECT_TYPE);
  }

// com.google.javascript.jscomp.TypeInferenceTest::testAssert1a
  public void testAssert1a() {
    JSType startType = createNullableType(OBJECT_TYPE);
    assuming("x", startType);
    inFunction("out1 = x; goog.asserts.assert(x !== null); out2 = x;");
    verify("out1", startType);
    verify("out2", OBJECT_TYPE);
  }

// com.google.javascript.jscomp.TypeInferenceTest::testAssert2
  public void testAssert2() {
    JSType startType = createNullableType(OBJECT_TYPE);
    assuming("x", startType);
    inFunction("goog.asserts.assert(1, x); out1 = x;");
    verify("out1", startType);
  }

// com.google.javascript.jscomp.TypeInferenceTest::testAssert3
  public void testAssert3() {
    JSType startType = createNullableType(OBJECT_TYPE);
    assuming("x", startType);
    assuming("y", startType);
    inFunction("out1 = x; goog.asserts.assert(x && y); out2 = x; out3 = y;");
    verify("out1", startType);
    verify("out2", OBJECT_TYPE);
    verify("out3", OBJECT_TYPE);
  }

// com.google.javascript.jscomp.TypeInferenceTest::testAssert4
  public void testAssert4() {
    JSType startType = createNullableType(OBJECT_TYPE);
    assuming("x", startType);
    assuming("y", startType);
    inFunction("out1 = x; goog.asserts.assert(x && !y); out2 = x; out3 = y;");
    verify("out1", startType);
    verify("out2", OBJECT_TYPE);
    verify("out3", NULL_TYPE);
  }

// com.google.javascript.jscomp.TypeInferenceTest::testAssert5
  public void testAssert5() {
    JSType startType = createNullableType(OBJECT_TYPE);
    assuming("x", startType);
    assuming("y", startType);
    inFunction("goog.asserts.assert(x || y); out1 = x; out2 = y;");
    verify("out1", startType);
    verify("out2", startType);
  }

// com.google.javascript.jscomp.TypeInferenceTest::testAssert6
  public void testAssert6() {
    JSType startType = createNullableType(OBJECT_TYPE);
    assuming("x.y", startType);
    inFunction("out1 = x.y; goog.asserts.assert(x.y); out2 = x.y;");
    verify("out1", startType);
    verify("out2", OBJECT_TYPE);
  }

// com.google.javascript.jscomp.TypeInferenceTest::testAssert7
  public void testAssert7() {
    JSType startType = createNullableType(OBJECT_TYPE);
    assuming("x", startType);
    inFunction("out1 = x; out2 = goog.asserts.assert(x);");
    verify("out1", startType);
    verify("out2", OBJECT_TYPE);
  }

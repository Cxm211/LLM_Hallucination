// buggy code
  private void inferPropertyTypesToMatchConstraint(
      JSType type, JSType constraint) {
    if (type == null || constraint == null) {
      return;
    }

    ObjectType constraintObj =
        ObjectType.cast(constraint.restrictByNotNullOrUndefined());
    if (constraintObj != null && constraintObj.isRecordType()) {
      ObjectType objType = ObjectType.cast(type.restrictByNotNullOrUndefined());
      if (objType != null) {
        for (String prop : constraintObj.getOwnPropertyNames()) {
          JSType propType = constraintObj.getPropertyType(prop);
          if (!objType.isPropertyTypeDeclared(prop)) {
            JSType typeToInfer = propType;
            if (!objType.hasProperty(prop)) {
              typeToInfer =
                  getNativeType(VOID_TYPE).getLeastSupertype(propType);
            }
            objType.defineInferredProperty(prop, typeToInfer, null);
          }
        }
      }
    }
  }

// relevant test
// com.google.javascript.jscomp.LooseTypeCheckTest::testAddingMethodsUsingPrototypeIdiomComplexNamespace2
  public void testAddingMethodsUsingPrototypeIdiomComplexNamespace2()
      throws Exception {
    TypeCheckResult p = parseAndTypeCheckWithScope(
        "var goog = {};" +
        "goog.A = function() {};" +
        "goog.A.prototype.m1 = 5");

    testAddingMethodsUsingPrototypeIdiomComplexNamespace(p);
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testAddingMethodsPrototypeIdiomAndObjectLiteralSimpleNamespace
  public void testAddingMethodsPrototypeIdiomAndObjectLiteralSimpleNamespace()
      throws Exception {
    Node js1Node = parseAndTypeCheck(
        "function A() {}" +
        "A.prototype = {m1: 5, m2: true}");

    ObjectType instanceType = getInstanceType(js1Node);
    assertEquals(NATIVE_PROPERTIES_COUNT + 2,
        instanceType.getPropertiesCount());
    checkObjectType(instanceType, "m1", NUMBER_TYPE);
    checkObjectType(instanceType, "m2", BOOLEAN_TYPE);
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testDontAddMethodsIfNoConstructor
  public void testDontAddMethodsIfNoConstructor()
      throws Exception {
    Node js1Node = parseAndTypeCheck(
        "function A() {}" +
        "A.prototype = {m1: 5, m2: true}");

    JSType functionAType = js1Node.getFirstChild().getJSType();
    assertEquals("function (): undefined", functionAType.toString());
    assertEquals(UNKNOWN_TYPE,
        U2U_FUNCTION_TYPE.getPropertyType("m1"));
    assertEquals(UNKNOWN_TYPE,
        U2U_FUNCTION_TYPE.getPropertyType("m2"));
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testFunctionAssignement
  public void testFunctionAssignement() throws Exception {
    testTypes("" +
        "function MSG_CALENDAR_ACCESS_ERROR(ph0, ph1) {return ''}" +
        "" +
        "var MSG_CALENDAR_ADD_ERROR = MSG_CALENDAR_ACCESS_ERROR;");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testAddMethodsPrototypeTwoWays
  public void testAddMethodsPrototypeTwoWays() throws Exception {
    Node js1Node = parseAndTypeCheck(
        "function A() {}" +
        "A.prototype = {m1: 5, m2: true};" +
        "A.prototype.m3 = 'third property!';");

    ObjectType instanceType = getInstanceType(js1Node);
    assertEquals("A", instanceType.toString());
    assertEquals(NATIVE_PROPERTIES_COUNT + 3,
        instanceType.getPropertiesCount());
    checkObjectType(instanceType, "m1", NUMBER_TYPE);
    checkObjectType(instanceType, "m2", BOOLEAN_TYPE);
    checkObjectType(instanceType, "m3", STRING_TYPE);
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testPrototypePropertyTypes
  public void testPrototypePropertyTypes() throws Exception {
    Node js1Node = parseAndTypeCheck(
        "function A() {\n" +
        "   this.m1;\n" +
        "   this.m2 = {};\n" +
        "   this.m3;\n" +
        "}\n" +
        " A.prototype.m4;\n" +
        " A.prototype.m5 = 0;\n" +
        " A.prototype.m6;\n");

    ObjectType instanceType = getInstanceType(js1Node);
    assertEquals(NATIVE_PROPERTIES_COUNT + 6,
        instanceType.getPropertiesCount());
    checkObjectType(instanceType, "m1", STRING_TYPE);
    checkObjectType(instanceType, "m2",
        createUnionType(createUnionType(OBJECT_TYPE, NULL_TYPE), VOID_TYPE));
    checkObjectType(instanceType, "m3", BOOLEAN_TYPE);
    checkObjectType(instanceType, "m4", STRING_TYPE);
    checkObjectType(instanceType, "m5", NUMBER_TYPE);
    checkObjectType(instanceType, "m6", BOOLEAN_TYPE);
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testValueTypeBuiltInPrototypePropertyType
  public void testValueTypeBuiltInPrototypePropertyType() throws Exception {
    Node node = parseAndTypeCheck("\"x\".charAt(0)");
    assertEquals(STRING_TYPE, node.getFirstChild().getFirstChild().getJSType());
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testDeclareBuiltInConstructor
  public void testDeclareBuiltInConstructor() throws Exception {
    
    
    Node node = parseAndTypeCheck(
        " var String = function(opt_str) {};\n" +
        "(new String(\"x\")).charAt(0)");
    assertEquals(STRING_TYPE, node.getLastChild().getFirstChild().getJSType());
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testExtendBuiltInType1
  public void testExtendBuiltInType1() throws Exception {
    String externs =
        " var String = function(opt_str) {};\n" +
        "\n" +
        "String.prototype.substr = function(start, opt_length) {};\n";
    Node n1 = parseAndTypeCheck(externs + "(new String(\"x\")).substr(0,1);");
    assertEquals(STRING_TYPE, n1.getLastChild().getFirstChild().getJSType());
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testExtendBuiltInType2
  public void testExtendBuiltInType2() throws Exception {
    String externs =
        " var String = function(opt_str) {};\n" +
        "\n" +
        "String.prototype.substr = function(start, opt_length) {};\n";
    Node n2 = parseAndTypeCheck(externs + "\"x\".substr(0,1);");
    assertEquals(STRING_TYPE, n2.getLastChild().getFirstChild().getJSType());
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testExtendFunction1
  public void testExtendFunction1() throws Exception {
    Node n = parseAndTypeCheck("Function.prototype.f = " +
        "function() { return 1; };\n" +
        "(new Function()).f();");
    JSType type = n.getLastChild().getLastChild().getJSType();
    assertEquals(NUMBER_TYPE, type);
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testExtendFunction2
  public void testExtendFunction2() throws Exception {
    Node n = parseAndTypeCheck("Function.prototype.f = " +
        "function() { return 1; };\n" +
        "(function() {}).f();");
    JSType type = n.getLastChild().getLastChild().getJSType();
    assertEquals(NUMBER_TYPE, type);
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testInheritanceCheck1
  public void testInheritanceCheck1() throws Exception {
    testTypes(
        "function Super() {};" +
        "function Sub() {};" +
        "Sub.prototype.foo = function() {};");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testInheritanceCheck2
  public void testInheritanceCheck2() throws Exception {
    testTypes(
        "function Super() {};" +
        "function Sub() {};" +
        "Sub.prototype.foo = function() {};",
        "property foo not defined on any superclass of Sub");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testInheritanceCheck3
  public void testInheritanceCheck3() throws Exception {
    testTypes(
        "function Super() {};" +
        "Super.prototype.foo = function() {};" +
        "function Sub() {};" +
        "Sub.prototype.foo = function() {};",
        "property foo already defined on superclass Super; " +
        "use @override to override it");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testInheritanceCheck4
  public void testInheritanceCheck4() throws Exception {
    testTypes(
        "function Super() {};" +
        "Super.prototype.foo = function() {};" +
        "function Sub() {};" +
        "Sub.prototype.foo = function() {};");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testInheritanceCheck5
  public void testInheritanceCheck5() throws Exception {
    testTypes(
        "function Root() {};" +
        "Root.prototype.foo = function() {};" +
        "function Super() {};" +
        "function Sub() {};" +
        "Sub.prototype.foo = function() {};",
        "property foo already defined on superclass Root; " +
        "use @override to override it");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testInheritanceCheck6
  public void testInheritanceCheck6() throws Exception {
    testTypes(
        "function Root() {};" +
        "Root.prototype.foo = function() {};" +
        "function Super() {};" +
        "function Sub() {};" +
        "Sub.prototype.foo = function() {};");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testInheritanceCheck7
  public void testInheritanceCheck7() throws Exception {
    testTypes(
        "var goog = {};" +
        "goog.Super = function() {};" +
        "goog.Super.prototype.foo = 3;" +
        "goog.Sub = function() {};" +
        "goog.Sub.prototype.foo = 5;",
        "property foo already defined on superclass goog.Super; " +
        "use @override to override it");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testInheritanceCheck8
  public void testInheritanceCheck8() throws Exception {
    testTypes(
        "var goog = {};" +
        "goog.Super = function() {};" +
        "goog.Super.prototype.foo = 3;" +
        "goog.Sub = function() {};" +
        "goog.Sub.prototype.foo = 5;");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testInheritanceCheck9_1
  public void testInheritanceCheck9_1() throws Exception {
    testTypes(
        "function Super() {};" +
        "Super.prototype.foo = function() { return 3; };" +
        "function Sub() {};" +
        "Sub.prototype.foo =\n" +
        "function() { return 1; };");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testInheritanceCheck9_2
  public void testInheritanceCheck9_2() throws Exception {
    testTypes(
        "function Super() {};" +
        "" +
        "Super.prototype.foo = function() { return 1; };" +
        "function Sub() {};" +
        "Sub.prototype.foo =\n" +
        "function() {};");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testInheritanceCheck9_3
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

// com.google.javascript.jscomp.LooseTypeCheckTest::testInheritanceCheck10_1
  public void testInheritanceCheck10_1() throws Exception {
    testTypes(
        "function Root() {};" +
        "Root.prototype.foo = function() { return 4; };" +
        "function Super() {};" +
        "function Sub() {};" +
        "Sub.prototype.foo =\n" +
        "function() { return 1; };");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testInheritanceCheck10_2
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

// com.google.javascript.jscomp.LooseTypeCheckTest::testInheritanceCheck10_3
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

// com.google.javascript.jscomp.LooseTypeCheckTest::testInterfaceInheritanceCheck11
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

// com.google.javascript.jscomp.LooseTypeCheckTest::testInheritanceCheck12
  public void testInheritanceCheck12() throws Exception {
    testTypes(
        "var goog = {};" +
        "goog.Super = function() {};" +
        "goog.Super.prototype.foo = 3;" +
        "goog.Sub = function() {};" +
        "goog.Sub.prototype.foo = \"some string\";",
        "mismatch of the foo property type and the type of the property it " +
        "overrides from superclass goog.Super\n" +
        "original: number\n" +
        "override: string");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testInheritanceCheck13
  public void testInheritanceCheck13() throws Exception {
    testTypes(
        "var goog = {};\n" +
        "function Sub() {};" +
        "Sub.prototype.foo = function() {};",
        "Bad type annotation. Unknown type goog.Missing");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testInheritanceCheck14
  public void testInheritanceCheck14() throws Exception {
    testTypes(
        "var goog = {};\n" +
        "\n" +
        "goog.Super = function() {};\n" +
        "function Sub() {};" +
        "Sub.prototype.foo = function() {};",
        "Bad type annotation. Unknown type goog.Missing");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testInterfaceInheritanceCheck1
  public void testInterfaceInheritanceCheck1() throws Exception {
    testTypes(
        "function Super() {};" +
        "Super.prototype.foo = function() {};" +
        "function Sub() {};" +
        "Sub.prototype.foo = function() {};",
        "property foo already defined on interface Super; use @override to " +
        "override it");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testInterfaceInheritanceCheck2
  public void testInterfaceInheritanceCheck2() throws Exception {
    testTypes(
        "function Super() {};" +
        "Super.prototype.foo = function() {};" +
        "function Sub() {};" +
        "Sub.prototype.foo = function() {};");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testInterfaceInheritanceCheck3
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

// com.google.javascript.jscomp.LooseTypeCheckTest::testInterfaceInheritanceCheck4
  public void testInterfaceInheritanceCheck4() throws Exception {
    testTypes(
        "function Root() {};" +
        "Root.prototype.foo = function() {};" +
        "function Super() {};" +
        "function Sub() {};" +
        "Sub.prototype.foo =\n" +
        "function() { return 1;};");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testInterfaceInheritanceCheck5
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

// com.google.javascript.jscomp.LooseTypeCheckTest::testInterfaceInheritanceCheck6
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

// com.google.javascript.jscomp.LooseTypeCheckTest::testInterfaceInheritanceCheck7
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

// com.google.javascript.jscomp.LooseTypeCheckTest::testInterfaceInheritanceCheck8
  public void testInterfaceInheritanceCheck8() throws Exception {
    testTypes(
        "function Sub() {};" +
        "Sub.prototype.foo = function() {};",
        new String[] {
          "Bad type annotation. Unknown type Super",
          "property foo not defined on any superclass of Sub"
        });
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testInterfacePropertyNotImplemented
  public void testInterfacePropertyNotImplemented() throws Exception {
    testTypes(
        "function Int() {};" +
        "Int.prototype.foo = function() {};" +
        "function Foo() {};",
        "property foo on interface Int is not implemented by type Foo");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testInterfacePropertyNotImplemented2
  public void testInterfacePropertyNotImplemented2() throws Exception {
    testTypes(
        "function Int() {};" +
        "Int.prototype.foo = function() {};" +
        "function Int2() {};" +
        "function Foo() {};",
        "property foo on interface Int is not implemented by type Foo");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testStubConstructorImplementingInterface
  public void testStubConstructorImplementingInterface() throws Exception {
    
    
    testTypes(
        
        " function Int() {}\n" +
        "Int.prototype.foo = function() {};" +
        " var Foo;\n",
        "", null, false);
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testObjectLiteral
  public void testObjectLiteral() throws Exception {
    Node n = parseAndTypeCheck("var a = {m1: 7, m2: 'hello'}");

    Node nameNode = n.getFirstChild().getFirstChild();
    Node objectNode = nameNode.getFirstChild();

    
    assertEquals(Token.NAME, nameNode.getType());
    assertEquals(Token.OBJECTLIT, objectNode.getType());

    
    ObjectType objectType =
        (ObjectType) objectNode.getJSType();
    assertEquals(NUMBER_TYPE, objectType.getPropertyType("m1"));
    assertEquals(STRING_TYPE, objectType.getPropertyType("m2"));

    
    assertEquals(objectType, nameNode.getJSType());
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testObjectLiteralDeclaration1
  public void testObjectLiteralDeclaration1() throws Exception {
    testTypes(
        "var x = {" +
        " abc: true," +
        " 'def': 0," +
        " 3: 'fgh'" +
        "};");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testCallDateConstructorAsFunction
  public void testCallDateConstructorAsFunction() throws Exception {
    
    
    Node n = parseAndTypeCheck("Date()");
    assertEquals(STRING_TYPE, n.getFirstChild().getFirstChild().getJSType());
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testCallErrorConstructorAsFunction
  public void testCallErrorConstructorAsFunction() throws Exception {
    Node n = parseAndTypeCheck("Error('x')");
    assertEquals(ERROR_TYPE,
                 n.getFirstChild().getFirstChild().getJSType());
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testCallArrayConstructorAsFunction
  public void testCallArrayConstructorAsFunction() throws Exception {
    Node n = parseAndTypeCheck("Array()");
    assertEquals(ARRAY_TYPE,
                 n.getFirstChild().getFirstChild().getJSType());
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testPropertyTypeOfUnionType
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

// com.google.javascript.jscomp.LooseTypeCheckTest::testAnnotatedPropertyOnInterface1
  public void testAnnotatedPropertyOnInterface1() throws Exception {
    
    
    testTypes(" u.T = function() {};\n" +
        " u.T.prototype.f = function() {};");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testAnnotatedPropertyOnInterface2
  public void testAnnotatedPropertyOnInterface2() throws Exception {
    testTypes(" u.T = function() {};\n" +
        " u.T.prototype.f = function() { };");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testAnnotatedPropertyOnInterface3
  public void testAnnotatedPropertyOnInterface3() throws Exception {
    testTypes(" function T() {};\n" +
        " T.prototype.f = function() { };");
  }

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
    testTypes(" function T() {};\n" +
        "T.prototype.x = 1",
        "interface members can only be empty property declarations, "
        + "empty functions, or goog.abstractMethod"
        );
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
        " Bar.prototype = new Foo()",
        "assignment to property prototype of Bar\n" +
        "found   : Foo\n" +
        "required: (Array|null|undefined)");
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

// com.google.javascript.jscomp.PeepholeFoldWithTypesTest::testFoldTypeofObject
  public void testFoldTypeofObject() {
    test("var x = {};typeof x",
         "var x = {};\"object\"");

    test("var x = [];typeof x",
         "var x = [];\"object\"");

    
    test("var x = null;typeof x",
         "var x = null;\"object\"");
  }

// com.google.javascript.jscomp.PeepholeFoldWithTypesTest::testFoldTypeofString
  public void testFoldTypeofString() {
    test("var x = \"foo\";typeof x",
         "var x = \"foo\";\"string\"");

    test("var x = new String(\"foo\");typeof x",
         "var x = new String(\"foo\");\"object\"");
  }

// com.google.javascript.jscomp.PeepholeFoldWithTypesTest::testFoldTypeofNumber
  public void testFoldTypeofNumber() {
    test("var x = 10;typeof x",
         "var x = 10;\"number\"");

    test("var x = new Number(6);typeof x",
         "var x = new Number(6);\"object\"");
  }

// com.google.javascript.jscomp.PeepholeFoldWithTypesTest::testFoldTypeofBoolean
  public void testFoldTypeofBoolean() {
    test("var x = false;typeof x",
         "var x = false;\"boolean\"");

    test("var x = new Boolean(true);typeof x",
         "var x = new Boolean(true);\"object\"");
  }

// com.google.javascript.jscomp.PeepholeFoldWithTypesTest::testFoldTypeofUndefined
  public void testFoldTypeofUndefined() {
    test("var x = undefined;typeof x",
         "var x = undefined;\"undefined\"");
  }

// com.google.javascript.jscomp.PeepholeFoldWithTypesTest::testDontFoldTypeofUnionTypes
  public void testDontFoldTypeofUnionTypes() {
    
    testSame("var x = (unknown ? {} : null);typeof x");
  }

// com.google.javascript.jscomp.PeepholeFoldWithTypesTest::testDontFoldTypeofSideEffects
  public void testDontFoldTypeofSideEffects() {
    
    testSame("var x = 6 ;typeof (x++)");
  }

// com.google.javascript.jscomp.PeepholeFoldWithTypesTest::testDontFoldTypeofWithTypeCheckDisabled
  public void testDontFoldTypeofWithTypeCheckDisabled() {
    disableTypeCheck();
    testSame("var x = {};typeof x");
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testIssue303
  public void testIssue303() throws Exception {
    checkMarkedCalls(
        " function F() {" +
        "  var self = this;" +
        "  window.setTimeout(function() {" +
        "    window.location = self.location;" +
        "  }, 0);" +
        "}" +
        "F.prototype.setLocation = function(x) {" +
        "  this.location = x;" +
        "};" +
        "(new F()).setLocation('http://www.google.com/');",
        ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testIssue303b
  public void testIssue303b() throws Exception {
    checkMarkedCalls(
        " function F() {" +
        "  var self = this;" +
        "  window.setTimeout(function() {" +
        "    window.location = self.location;" +
        "  }, 0);" +
        "}" +
        "F.prototype.setLocation = function(x) {" +
        "  this.location = x;" +
        "};" +
        "function x() {" +
        "  (new F()).setLocation('http://www.google.com/');" +
        "} window['x'] = x;",
        ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testAnnotationInExterns_new1
  public void testAnnotationInExterns_new1() throws Exception {
    checkMarkedCalls("externSENone()",
        ImmutableList.<String>of("externSENone"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testAnnotationInExterns_new2
  public void testAnnotationInExterns_new2() throws Exception {
    checkMarkedCalls("externSEThis()",
        ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testAnnotationInExterns_new3
  public void testAnnotationInExterns_new3() throws Exception {
    checkMarkedCalls("new externObjSEThis()",
        ImmutableList.<String>of("externObjSEThis"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testAnnotationInExterns_new4
  public void testAnnotationInExterns_new4() throws Exception {
    
    

    checkMarkedCalls("new externObjSEThis().externObjSEThisMethod('')",
        ImmutableList.<String>of(
           "externObjSEThis", "NEW STRING externObjSEThisMethod"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testAnnotationInExterns_new5
  public void testAnnotationInExterns_new5() throws Exception {
    checkMarkedCalls(
        "function f() { new externObjSEThis() };" +
        "f();",
        ImmutableList.<String>of("externObjSEThis", "f"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testAnnotationInExterns_new6
  public void testAnnotationInExterns_new6() throws Exception {
    
    
    
    
    
    
    checkMarkedCalls(
        "function f() {" +
        "  new externObjSEThis().externObjSEThisMethod('') " +
        "};" +
        "f();",
         ImmutableList.<String>of(
             "externObjSEThis", "NEW STRING externObjSEThisMethod"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testAnnotationInExterns_new7
  public void testAnnotationInExterns_new7() throws Exception {
    
    
    
    checkMarkedCalls(
        "function f() {" +
        "  var x = new externObjSEThis(); " +
        "  x.externObjSEThisMethod('') " +
        "};" +
        "f();",
        ImmutableList.<String>of("externObjSEThis"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testAnnotationInExterns_new8
  public void testAnnotationInExterns_new8() throws Exception {
    
    
    
    checkMarkedCalls(
        "function f(x) {" +
        "  x.externObjSEThisMethod('') " +
        "};" +
        "f(new externObjSEThis());",
        ImmutableList.<String>of("externObjSEThis"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testAnnotationInExterns_new9
  public void testAnnotationInExterns_new9() throws Exception {
    
    
    
    
    checkMarkedCalls(
        "function f(x) {" +
        "  x = new externObjSEThis(); " +
        "  x.externObjSEThisMethod('') " +
        "};" +
        "f(g);",
        ImmutableList.<String>of("externObjSEThis"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testAnnotationInExterns_new10
  public void testAnnotationInExterns_new10() throws Exception {
    
    
    
    
    checkMarkedCalls(
        "function f() {" +
        "  new externObjSEThis().externObjSEThisMethod2('') " +
        "};" +
        "f();",
        ImmutableList.<String>of("externObjSEThis"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testAnnotationInExterns1
  public void testAnnotationInExterns1() throws Exception {
    checkMarkedCalls("externSef1()", ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testAnnotationInExterns2
  public void testAnnotationInExterns2() throws Exception {
    checkMarkedCalls("externSef2()", ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testAnnotationInExterns3
  public void testAnnotationInExterns3() throws Exception {
    checkMarkedCalls("externNsef1()", ImmutableList.of("externNsef1"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testAnnotationInExterns4
  public void testAnnotationInExterns4() throws Exception {
    checkMarkedCalls("externNsef2()", ImmutableList.of("externNsef2"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testAnnotationInExterns5
  public void testAnnotationInExterns5() throws Exception {
    checkMarkedCalls("externNsef3()", ImmutableList.of("externNsef3"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testNamespaceAnnotationInExterns1
  public void testNamespaceAnnotationInExterns1() throws Exception {
    checkMarkedCalls("externObj.sef1()", ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testNamespaceAnnotationInExterns2
  public void testNamespaceAnnotationInExterns2() throws Exception {
    checkMarkedCalls("externObj.nsef1()", ImmutableList.of("externObj.nsef1"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testNamespaceAnnotationInExterns3
  public void testNamespaceAnnotationInExterns3() throws Exception {
    checkMarkedCalls("externObj.nsef2()", ImmutableList.of("externObj.nsef2"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testNamespaceAnnotationInExterns4
  public void testNamespaceAnnotationInExterns4() throws Exception {
    checkMarkedCalls("externObj.partialFn()",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testNamespaceAnnotationInExterns5
  public void testNamespaceAnnotationInExterns5() throws Exception {
    
    
    
    String templateSrc = "var o = {}; o.<fnName> = function(){}; o.<fnName>()";

    
    checkMarkedCalls(templateSrc.replaceAll("<fnName>", "notPartialFn"),
                     ImmutableList.of("o.notPartialFn"));

    checkMarkedCalls(templateSrc.replaceAll("<fnName>", "partialFn"),
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testNamespaceAnnotationInExterns6
  public void testNamespaceAnnotationInExterns6() throws Exception {
    checkMarkedCalls("externObj.partialSharedFn()",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testConstructorAnnotationInExterns1
  public void testConstructorAnnotationInExterns1() throws Exception {
    checkMarkedCalls("new externSefConstructor()",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testConstructorAnnotationInExterns2
  public void testConstructorAnnotationInExterns2() throws Exception {
    checkMarkedCalls("var a = new externSefConstructor();" +
                     "a.sefFnOfSefObj()",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testConstructorAnnotationInExterns3
  public void testConstructorAnnotationInExterns3() throws Exception {
    checkMarkedCalls("var a = new externSefConstructor();" +
                     "a.nsefFnOfSefObj()",
                     ImmutableList.of("a.nsefFnOfSefObj"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testConstructorAnnotationInExterns4
  public void testConstructorAnnotationInExterns4() throws Exception {
    checkMarkedCalls("var a = new externSefConstructor();" +
                     "a.externShared()",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testConstructorAnnotationInExterns5
  public void testConstructorAnnotationInExterns5() throws Exception {
    checkMarkedCalls("new externNsefConstructor()",
                     ImmutableList.of("externNsefConstructor"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testConstructorAnnotationInExterns6
  public void testConstructorAnnotationInExterns6() throws Exception {
    checkMarkedCalls("var a = new externNsefConstructor();" +
                     "a.sefFnOfNsefObj()",
                     ImmutableList.of("externNsefConstructor"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testConstructorAnnotationInExterns7
  public void testConstructorAnnotationInExterns7() throws Exception {
    checkMarkedCalls("var a = new externNsefConstructor();" +
                     "a.nsefFnOfNsefObj()",
                     ImmutableList.of("externNsefConstructor",
                                      "a.nsefFnOfNsefObj"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testConstructorAnnotationInExterns8
  public void testConstructorAnnotationInExterns8() throws Exception {
    checkMarkedCalls("var a = new externNsefConstructor();" +
                     "a.externShared()",
                     ImmutableList.of("externNsefConstructor"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testSharedFunctionName1
  public void testSharedFunctionName1() throws Exception {
    checkMarkedCalls("var a; " +
                     "if (true) {" +
                     "  a = new externNsefConstructor()" +
                     "} else {" +
                     "  a = new externSefConstructor()" +
                     "}" +
                     "a.externShared()",
                     ImmutableList.of("externNsefConstructor"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testSharedFunctionName2
  public void testSharedFunctionName2() throws Exception {
    
    
    boolean broken = true;
    if (broken) {
      checkMarkedCalls("var a; " +
                       "if (true) {" +
                       "  a = new externNsefConstructor()" +
                       "} else {" +
                       "  a = new externNsefConstructor2()" +
                       "}" +
                       "a.externShared()",
                       ImmutableList.of("externNsefConstructor",
                                        "externNsefConstructor2"));
    } else {
      checkMarkedCalls("var a; " +
                       "if (true) {" +
                       "  a = new externNsefConstructor()" +
                       "} else {" +
                       "  a = new externNsefConstructor2()" +
                       "}" +
                       "a.externShared()",
                       ImmutableList.of("externNsefConstructor",
                                        "externNsefConstructor2",
                                        "a.externShared"));
    }
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testAnnotationInExternStubs1
  public void testAnnotationInExternStubs1() throws Exception {
    checkMarkedCalls("o.propWithStubBefore('a');",
        ImmutableList.<String>of("o.propWithStubBefore"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testAnnotationInExternStubs1b
  public void testAnnotationInExternStubs1b() throws Exception {
    checkMarkedCalls("o.propWithStubBeforeWithJSDoc('a');",
        ImmutableList.<String>of("o.propWithStubBeforeWithJSDoc"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testAnnotationInExternStubs2
  public void testAnnotationInExternStubs2() throws Exception {
    checkMarkedCalls("o.propWithStubAfter('a');",
        ImmutableList.<String>of("o.propWithStubAfter"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testAnnotationInExternStubs2b
  public void testAnnotationInExternStubs2b() throws Exception {
    checkMarkedCalls("o.propWithStubAfter('a');",
        ImmutableList.<String>of("o.propWithStubAfter"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testAnnotationInExternStubs3
  public void testAnnotationInExternStubs3() throws Exception {
    checkMarkedCalls("propWithAnnotatedStubAfter('a');",
        ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testAnnotationInExternStubs4
  public void testAnnotationInExternStubs4() throws Exception {
    
    
    String externs =
      "function externObj5(){}\n" +

      "externObj5.prototype.propWithAnnotatedStubAfter = function(s) {};\n" +

      "\n" +
      "externObj5.prototype.propWithAnnotatedStubAfter;\n";

    List<String> expected = ImmutableList.<String>of();
    testSame(externs,
        "o.prototype.propWithAnnotatedStubAfter",
        TypeValidator.DUP_VAR_DECLARATION, false);
    assertEquals(expected, noSideEffectCalls);
    noSideEffectCalls.clear();
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testAnnotationInExternStubs5
  public void testAnnotationInExternStubs5() throws Exception {
    
    
    String externs =
      "function externObj5(){}\n" +

      "\n" +
      "externObj5.prototype.propWithAnnotatedStubAfter = function(s) {};\n" +

      "\n" +
      "externObj5.prototype.propWithAnnotatedStubAfter;\n";

    List<String> expected = ImmutableList.<String>of();
    testSame(externs,
        "o.prototype.propWithAnnotatedStubAfter",
        TypeValidator.DUP_VAR_DECLARATION, false);
    assertEquals(expected, noSideEffectCalls);
    noSideEffectCalls.clear();
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testNoSideEffectsSimple
  public void testNoSideEffectsSimple() throws Exception {
    String prefix = "function f(){";
    String suffix = "} f()";
    List<String> expected = ImmutableList.of("f");

    checkMarkedCalls(
        prefix + "" + suffix, expected);
    checkMarkedCalls(
        prefix + "return 1" + suffix, expected);
    checkMarkedCalls(
        prefix + "return 1 + 2" + suffix, expected);

    
    checkMarkedCalls(
        prefix + "var a = 1; return a" + suffix, expected);

    
    checkMarkedCalls(
        prefix + "var a = 1; a = 2; return a" + suffix, expected);
    checkMarkedCalls(
        prefix + "var a = 1; a = 2; return a + 1" + suffix, expected);

    
    checkMarkedCalls(
        prefix + "var a = {foo : 1}; return a.foo" + suffix, expected);
    checkMarkedCalls(
        prefix + "var a = {foo : 1}; return a.foo + 1" + suffix, expected);

    
    checkMarkedCalls(
        prefix + "return externObj" + suffix, expected);
    checkMarkedCalls(
        "function g(x) { x.foo = 3; }"  +
        prefix + "return externObj.foo" + suffix, expected);
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testResultLocalitySimple
  public void testResultLocalitySimple() throws Exception {
    String prefix = "var g; function f(){";
    String suffix = "} f()";
    List<String> expected = ImmutableList.of("f");
    List<String> notExpected = ImmutableList.of();

    
    checkLocalityOfMarkedCalls(
        prefix + "" + suffix, expected);
    
    checkLocalityOfMarkedCalls(
        prefix + "return 1" + suffix, expected);
    checkLocalityOfMarkedCalls(
        prefix + "return 1 + 2" + suffix, expected);

    
    checkLocalityOfMarkedCalls(
        prefix + "return g" + suffix, notExpected);

    
    checkLocalityOfMarkedCalls(
        prefix + "return 1; return 2" + suffix, expected);
    checkLocalityOfMarkedCalls(
        prefix + "return 1; return g" + suffix, notExpected);

    
    checkLocalityOfMarkedCalls(
        prefix + "var a = 1; return a" + suffix, notExpected);

    
    checkLocalityOfMarkedCalls(
        prefix + "var a = 1; a = 2; return a" + suffix, notExpected);
    checkLocalityOfMarkedCalls(
        prefix + "var a = 1; a = 2; return a + 1" + suffix, expected);

    
    checkLocalityOfMarkedCalls(
        prefix + "return {foo : 1}.foo" + suffix,
        notExpected);
    checkLocalityOfMarkedCalls(
        prefix + "var a = {foo : 1}; return a.foo" + suffix,
        notExpected);

    
    checkLocalityOfMarkedCalls(
        prefix + "return externObj" + suffix, notExpected);
    checkLocalityOfMarkedCalls(
        "function inner(x) { x.foo = 3; }"  +
        prefix + "return externObj.foo" + suffix, notExpected);
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testExternCalls
  public void testExternCalls() throws Exception {
    String prefix = "function f(){";
    String suffix = "} f()";

    checkMarkedCalls(prefix + "externNsef1()" + suffix,
                     ImmutableList.of("externNsef1", "f"));
    checkMarkedCalls(prefix + "externObj.nsef1()" + suffix,
                     ImmutableList.of("externObj.nsef1", "f"));

    checkMarkedCalls(prefix + "externSef1()" + suffix,
                     ImmutableList.<String>of());
    checkMarkedCalls(prefix + "externObj.sef1()" + suffix,
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testApply
  public void testApply() throws Exception {
    checkMarkedCalls("function f() {return 42}" +
                     "f.apply()",
                     ImmutableList.of("f.apply"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testCall
  public void testCall() throws Exception {
    checkMarkedCalls("function f() {return 42}" +
                     "f.call()",
                     ImmutableList.<String>of("f.call"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testInference1
  public void testInference1() throws Exception {
    checkMarkedCalls("function f() {return g()}" +
                     "function g() {return 42}" +
                     "f()",
                     ImmutableList.of("g", "f"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testInference2
  public void testInference2() throws Exception {
    checkMarkedCalls("var a = 1;" +
                     "function f() {g()}" +
                     "function g() {a=2}" +
                     "f()",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testInference3
  public void testInference3() throws Exception {
    checkMarkedCalls("var f = function() {return g()};" +
                     "var g = function() {return 42};" +
                     "f()",
                     ImmutableList.of("g", "f"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testInference4
  public void testInference4() throws Exception {
    checkMarkedCalls("var a = 1;" +
                     "var f = function() {g()};" +
                     "var g = function() {a=2};" +
                     "f()",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testInference5
  public void testInference5() throws Exception {
    checkMarkedCalls("var goog = {};" +
                     "goog.f = function() {return goog.g()};" +
                     "goog.g = function() {return 42};" +
                     "goog.f()",
                     ImmutableList.of("goog.g", "goog.f"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testInference6
  public void testInference6() throws Exception {
    checkMarkedCalls("var a = 1;" +
                     "var goog = {};" +
                     "goog.f = function() {goog.g()};" +
                     "goog.g = function() {a=2};" +
                     "goog.f()",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testLocalizedSideEffects1
  public void testLocalizedSideEffects1() throws Exception {
    
    
    checkMarkedCalls("function f() {" +
                     "  var x = {foo : 0}; return function() {x.foo++};" +
                     "}" +
                     "f()",
                     ImmutableList.<String>of("f"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testLocalizedSideEffects2
  public void testLocalizedSideEffects2() throws Exception {
    
    
    checkMarkedCalls("function f() {" +
                     "  var x = {foo : 0}; (function() {x.foo++})();" +
                     "}" +
                     "f()",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testLocalizedSideEffects3
  public void testLocalizedSideEffects3() throws Exception {
    
    
    checkMarkedCalls("var g = {foo:1}; function f() {var x = g; x.foo++}" +
                     "f()",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testLocalizedSideEffects4
  public void testLocalizedSideEffects4() throws Exception {
    
    
    checkMarkedCalls("function f() {var x = []; x[0] = 1;}" +
                     "f()",
                     ImmutableList.<String>of("f"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testLocalizedSideEffects5
  public void testLocalizedSideEffects5() throws Exception {
    
    
    checkMarkedCalls("var g = [];function f() {var x = g; x[0] = 1;}" +
                     "f()",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testLocalizedSideEffects6
  public void testLocalizedSideEffects6() throws Exception {
    
    
    checkMarkedCalls("function f() {" +
                     "  var x = {}; x.foo = 1; return x;" +
                     "}" +
                     "f()",
                     ImmutableList.<String>of("f"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testLocalizedSideEffects7
  public void testLocalizedSideEffects7() throws Exception {
    
    
    checkMarkedCalls(" function A() {};" +
                     "function f() {" +
                     "  var a = []; a[1] = 1; return a;" +
                     "}" +
                     "f()",
                     ImmutableList.<String>of("f"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testLocalizedSideEffects8
  public void testLocalizedSideEffects8() throws Exception {
    
    
    
    checkMarkedCalls(" function A() {};" +
                     "function f() {" +
                     "  var a = new A; a.foo = 1; return a;" +
                     "}" +
                     "f()",
                     ImmutableList.<String>of("A"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testLocalizedSideEffects9
  public void testLocalizedSideEffects9() throws Exception {
    
    
    
    checkMarkedCalls(" function A() {this.x = 1};" +
                     "function f() {" +
                     "  var a = new A; a.foo = 1; return a;" +
                     "}" +
                     "f()",
                     ImmutableList.<String>of("A"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testLocalizedSideEffects10
  public void testLocalizedSideEffects10() throws Exception {
    
    
    checkMarkedCalls(" function A() {};" +
                     "A.prototype.g = function() {this.x = 1};" +
                     "function f() {" +
                     "  var a = new A; a.g(); return a;" +
                     "}" +
                     "f()",
                     ImmutableList.<String>of("A"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testLocalizedSideEffects11
  public void testLocalizedSideEffects11() throws Exception {
    
    checkMarkedCalls(
        " function A() {}" +
        "A.prototype.update = function() { this.x = 1; };" +
        " function B() { " +
        "  this.a_ = new A();" +
        "}" +
        "B.prototype.updateA = function() {" +
        "  var b = this.a_;" +
        "  b.update();" +
        "};" +
        "var x = new B();" +
        "x.updateA();",
        ImmutableList.of("A", "B"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testUnaryOperators1
  public void testUnaryOperators1() throws Exception {
    checkMarkedCalls("function f() {var x = 1; x++}" +
                     "f()",
                     ImmutableList.of("f"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testUnaryOperators2
  public void testUnaryOperators2() throws Exception {
    checkMarkedCalls("var x = 1;" +
                     "function f() {x++}" +
                     "f()",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testUnaryOperators3
  public void testUnaryOperators3() throws Exception {
    checkMarkedCalls("function f() {var x = {foo : 0}; x.foo++}" +
                     "f()",
                     ImmutableList.<String>of("f"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testUnaryOperators4
  public void testUnaryOperators4() throws Exception {
    checkMarkedCalls("var x = {foo : 0};" +
                     "function f() {x.foo++}" +
                     "f()",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testUnaryOperators5
  public void testUnaryOperators5() throws Exception {
    checkMarkedCalls("function f(x) {x.foo++}" +
                     "f({foo : 0})",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testDeleteOperator1
  public void testDeleteOperator1() throws Exception {
    checkMarkedCalls("var x = {};" +
                     "function f() {delete x}" +
                     "f()",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testDeleteOperator2
  public void testDeleteOperator2() throws Exception {
    checkMarkedCalls("function f() {var x = {}; delete x}" +
                     "f()",
                     ImmutableList.of("f"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testOrOperator1
  public void testOrOperator1() throws Exception {
    checkMarkedCalls("var f = externNsef1 || externNsef2;\n" +
                     "f()",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testOrOperator2
  public void testOrOperator2() throws Exception {
    checkMarkedCalls("var f = function(){} || externNsef2;\n" +
                     "f()",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testOrOperator3
  public void testOrOperator3() throws Exception {
    checkMarkedCalls("var f = externNsef2 || function(){};\n" +
                     "f()",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testOrOperators4
  public void testOrOperators4() throws Exception {
    checkMarkedCalls("var f = function(){} || function(){};\n" +
                     "f()",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testAndOperator1
  public void testAndOperator1() throws Exception {
    checkMarkedCalls("var f = externNsef1 && externNsef2;\n" +
                     "f()",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testAndOperator2
  public void testAndOperator2() throws Exception {
    checkMarkedCalls("var f = function(){} && externNsef2;\n" +
                     "f()",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testAndOperator3
  public void testAndOperator3() throws Exception {
    checkMarkedCalls("var f = externNsef2 && function(){};\n" +
                     "f()",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testAndOperators4
  public void testAndOperators4() throws Exception {
    checkMarkedCalls("var f = function(){} && function(){};\n" +
                     "f()",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testHookOperator1
  public void testHookOperator1() throws Exception {
    checkMarkedCalls("var f = true ? externNsef1 : externNsef2;\n" +
                     "f()",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testHookOperator2
  public void testHookOperator2() throws Exception {
    checkMarkedCalls("var f = true ? function(){} : externNsef2;\n" +
                     "f()",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testHookOperator3
  public void testHookOperator3() throws Exception {
    checkMarkedCalls("var f = true ? externNsef2 : function(){};\n" +
                     "f()",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testHookOperators4
  public void testHookOperators4() throws Exception {
    checkMarkedCalls("var f = true ? function(){} : function(){};\n" +
                     "f()",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testThrow1
  public void testThrow1() throws Exception {
    checkMarkedCalls("function f(){throw Error()};\n" +
                     "f()",
                     ImmutableList.<String>of("Error"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testThrow2
  public void testThrow2() throws Exception {
    checkMarkedCalls("function A(){throw Error()};\n" +
                     "function f(){return new A()}\n" +
                     "f()",
                     ImmutableList.<String>of("Error"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testAssignmentOverride
  public void testAssignmentOverride() throws Exception {
    checkMarkedCalls("function A(){}\n" +
                     "A.prototype.foo = function(){};\n" +
                     "var a = new A;\n" +
                     "a.foo();\n",
                     ImmutableList.<String>of("A", "a.foo"));

    checkMarkedCalls("function A(){}\n" +
                     "A.prototype.foo = function(){};\n" +
                     "var x = 1\n" +
                     "function f(){x = 10}\n" +
                     "var a = new A;\n" +
                     "a.foo = f;\n" +
                     "a.foo();\n",
                     ImmutableList.<String>of("A"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testInheritance1
  public void testInheritance1() throws Exception {
    String source =
        CompilerTypeTestCase.CLOSURE_DEFS +
        "function I(){}\n" +
        "I.prototype.foo = function(){};\n" +
        "I.prototype.bar = function(){this.foo()};\n" +
        "function A(){};\n" +
        "goog.inherits(A, I)\n;" +
        "A.prototype.foo = function(){var data=24};\n" +
        "var i = new I();i.foo();i.bar();\n" +
        "var a = new A();a.foo();a.bar();";

    checkMarkedCalls(source,
                     ImmutableList.of("this.foo", "goog.inherits",
                                      "I", "i.foo", "i.bar",
                                      "A", "a.foo", "a.bar"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testInheritance2
  public void testInheritance2() throws Exception {
    String source =
        CompilerTypeTestCase.CLOSURE_DEFS +
        "function I(){}\n" +
        "I.prototype.foo = function(){};\n" +
        "I.prototype.bar = function(){this.foo()};\n" +
        "function A(){};\n" +
        "goog.inherits(A, I)\n;" +
        "A.prototype.foo = function(){this.data=24};\n" +
        "var i = new I();i.foo();i.bar();\n" +
        "var a = new A();a.foo();a.bar();";

    checkMarkedCalls(source, ImmutableList.of("goog.inherits", "I", "A"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testCallBeforeDefinition
  public void testCallBeforeDefinition() throws Exception {
    checkMarkedCalls("f(); function f(){}",
                     ImmutableList.of("f"));

    checkMarkedCalls("var a = {}; a.f(); a.f = function (){}",
                     ImmutableList.of("a.f"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testConstructorThatModifiesThis1
  public void testConstructorThatModifiesThis1() throws Exception {
    String source = "function A(){this.foo = 1}\n" +
        "function f() {return new A}" +
        "f()";

    checkMarkedCalls(source, ImmutableList.of("A", "f"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testConstructorThatModifiesThis2
  public void testConstructorThatModifiesThis2() throws Exception {
    String source = "function A(){this.foo()}\n" +
        "A.prototype.foo = function(){this.data=24};\n" +
        "function f() {return new A}" +
        "f()";

    checkMarkedCalls(source, ImmutableList.of("A", "f"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testConstructorThatModifiesThis3
  public void testConstructorThatModifiesThis3() throws Exception {

    
    String source = "function A(){this.foo()}\n" +
        "A.prototype.foo = function(){this.bar()};\n" +
        "A.prototype.bar = function(){this.data=24};\n" +
        "function f() {return new A}" +
        "f()";

    checkMarkedCalls(source, ImmutableList.of("A", "f"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testConstructorThatModifiesThis4
  public void testConstructorThatModifiesThis4() throws Exception {

    
    String source = "function A(){foo.call(this)}\n" +
        "function foo(){this.data=24};\n" +
        "function f() {return new A}" +
        "f()";

    checkMarkedCalls(source, ImmutableList.of("A", "f"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testConstructorThatModifiesGlobal1
  public void testConstructorThatModifiesGlobal1() throws Exception {
    String source = "var b = 0;" +
        "function A(){b=1};\n" +
        "function f() {return new A}" +
        "f()";

    checkMarkedCalls(source, ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testConstructorThatModifiesGlobal2
  public void testConstructorThatModifiesGlobal2() throws Exception {
    String source = "var b = 0;" +
        "function A(){this.foo()}\n" +
        "A.prototype.foo = function(){b=1};\n" +
        "function f() {return new A}" +
        "f()";

    checkMarkedCalls(source, ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testCallFunctionThatModifiesThis
  public void testCallFunctionThatModifiesThis() throws Exception {
    String source = "function A(){}\n" +
        "A.prototype.foo = function(){this.data=24};\n" +
        "function f(){var a = new A; return a}\n" +
        "function g(){var a = new A; a.foo(); return a}\n" +
        "f(); g()";

    checkMarkedCalls(source, ImmutableList.<String>of("A", "A", "f"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testCallFunctionFOrG
  public void testCallFunctionFOrG() throws Exception {
    String source = "function f(){}\n" +
        "function g(){}\n" +
        "function h(){ (f || g)() }\n" +
        "h()";

    checkMarkedCalls(source, ImmutableList.<String>of("(f || g)", "h"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testCallFunctionFOrGViaHook
  public void testCallFunctionFOrGViaHook() throws Exception {
    String source = "function f(){}\n" +
        "function g(){}\n" +
        "function h(){ (false ? f : g)() }\n" +
        "h()";

    checkMarkedCalls(source, ImmutableList.<String>of("(f : g)", "h"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testCallFunctionForGorH
  public void testCallFunctionForGorH() throws Exception {
    String source = "function f(){}\n" +
        "function g(){}\n" +
        "function h(){}\n" +
        "function i(){ (false ? f : (g || h))() }\n" +
        "i()";

    checkMarkedCalls(source, ImmutableList.<String>of("(f : (g || h))", "i"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testCallFunctionFOrGWithSideEffects
  public void testCallFunctionFOrGWithSideEffects() throws Exception {
    String source = "var x = 0;\n" +
        "function f(){x = 10}\n" +
        "function g(){}\n" +
        "function h(){ (f || g)() }\n" +
        "function i(){ (g || f)() }\n" +
        "function j(){ (f || f)() }\n" +
        "function k(){ (g || g)() }\n" +
        "h(); i(); j(); k()";

    checkMarkedCalls(source, ImmutableList.<String>of("(g || g)", "k"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testCallFunctionFOrGViaHookWithSideEffects
  public void testCallFunctionFOrGViaHookWithSideEffects() throws Exception {
    String source = "var x = 0;\n" +
        "function f(){x = 10}\n" +
        "function g(){}\n" +
        "function h(){ (false ? f : g)() }\n" +
        "function i(){ (false ? g : f)() }\n" +
        "function j(){ (false ? f : f)() }\n" +
        "function k(){ (false ? g : g)() }\n" +
        "h(); i(); j(); k()";

    checkMarkedCalls(source, ImmutableList.<String>of("(g : g)", "k"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testCallRegExpWithSideEffects
  public void testCallRegExpWithSideEffects() throws Exception {
    String source = "var x = 0;\n" +
        "function k(){(/a/).exec('')}\n" +
        "k()";

    regExpHaveSideEffects = true;
    checkMarkedCalls(source, ImmutableList.<String>of());
    regExpHaveSideEffects = false;
    checkMarkedCalls(source, ImmutableList.<String>of(
        "REGEXP STRING exec", "k"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testAnonymousFunction1
  public void testAnonymousFunction1() throws Exception {
    String source = "(function (){})();";

    checkMarkedCalls(source, ImmutableList.<String>of(
        "FUNCTION"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testAnonymousFunction2
  public void testAnonymousFunction2() throws Exception {
    String source = "(Error || function (){})();";

    checkMarkedCalls(source, ImmutableList.<String>of(
        "(Error || FUNCTION)"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testAnonymousFunction3
  public void testAnonymousFunction3() throws Exception {
    String source = "var a = (Error || function (){})();";

    checkMarkedCalls(source, ImmutableList.<String>of(
        "(Error || FUNCTION)"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testAnonymousFunction4
  public void testAnonymousFunction4() throws Exception {
    String source = "var a = (Error || function (){});" +
                    "a();";

    
    checkMarkedCalls(source, ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testInvalidAnnotation1
  public void testInvalidAnnotation1() throws Exception {
    test(" function foo() {}",
         null, INVALID_NO_SIDE_EFFECT_ANNOTATION);
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testInvalidAnnotation2
  public void testInvalidAnnotation2() throws Exception {
    test("var f =  function() {}",
         null, INVALID_NO_SIDE_EFFECT_ANNOTATION);
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testInvalidAnnotation3
  public void testInvalidAnnotation3() throws Exception {
    test(" var f = function() {}",
         null, INVALID_NO_SIDE_EFFECT_ANNOTATION);
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testInvalidAnnotation4
  public void testInvalidAnnotation4() throws Exception {
    test("var f = function() {};" +
         " f.x = function() {}",
         null, INVALID_NO_SIDE_EFFECT_ANNOTATION);
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testInvalidAnnotation5
  public void testInvalidAnnotation5() throws Exception {
    test("var f = function() {};" +
         "f.x =  function() {}",
         null, INVALID_NO_SIDE_EFFECT_ANNOTATION);
  }

// com.google.javascript.jscomp.RemoveUnusedNamesTest::testAnalyzeUnusedPrototypeProperties
  public void testAnalyzeUnusedPrototypeProperties() {
    
    test(" \n" +
        "function e(){} \n" +
        "e.prototype.a = function(){};" +
        "e.prototype.b = function(){};" +
        "var x = new e; x.a()",

        "function e(){}" +
        " e.prototype.a = function(){};" +
        "var x = new e; x.a()");
  }

// com.google.javascript.jscomp.RemoveUnusedNamesTest::testAnalyzeUnusedPrototypeProperties2
  public void testAnalyzeUnusedPrototypeProperties2() {
    
    
    
    
    
    
    
    
    
    
  }

// com.google.javascript.jscomp.RemoveUnusedNamesTest::testAnalyzeUnusedPrototypeProperties3
  public void testAnalyzeUnusedPrototypeProperties3() {
    
    
    test(" \n" +
        "function e(){} \n" +
           "e.prototype.a = function(){};" +
           "e.prototype.bExtern = function(){};" +
           "var x = new e;x.a()",
         "function e(){}" +
           "e.prototype.a = function(){};" +
           
           "var x = new e; x.a()");

    
    
    
    
    
    
    
    
  }

// com.google.javascript.jscomp.RemoveUnusedNamesTest::testAliasing
  public void testAliasing() {
    
  }

// com.google.javascript.jscomp.RemoveUnusedNamesTest::testStatement
  public void testStatement() {
    test(" \n" +
         "" +
        "function e(){}" +
           "var x = e.prototype.method1 = function(){};" +
           "var y = new e; x()",
         "function e(){}" +
           "var x = function(){};" +
           "var y = new e; x()");
  }

// com.google.javascript.jscomp.RemoveUnusedNamesTest::testExportedMethodsByNamingConvention
  public void testExportedMethodsByNamingConvention() {
    
  }

// com.google.javascript.jscomp.RemoveUnusedNamesTest::testExportedMethodsByNamingConventionAlwaysExported
  public void testExportedMethodsByNamingConventionAlwaysExported() {
    
  }

// com.google.javascript.jscomp.ReplaceStringsTest::testThrowError1
  public void testThrowError1() {
    testDebugStrings(
        "throw Error('xyz');",
        "throw Error('a');",
        (new String[] { "a", "xyz" }));
  }

// com.google.javascript.jscomp.ReplaceStringsTest::testThrowError2
  public void testThrowError2() {
    testDebugStrings(
        "throw Error('x' +\n    'yz');",
        "throw Error('a');",
        (new String[] { "a", "xyz" }));
  }

// com.google.javascript.jscomp.ReplaceStringsTest::testThrowError3
  public void testThrowError3() {
    testDebugStrings(
        "throw Error('Unhandled mail' + ' search type ' + type);",
        "throw Error('a' + '`' + type);",
        (new String[] { "a", "Unhandled mail search type `" }));
  }

// com.google.javascript.jscomp.ReplaceStringsTest::testThrowError4
  public void testThrowError4() {
    testDebugStrings(
        "\n" +
        "var A = function() {};\n" +
        "A.prototype.m = function(child) {\n" +
        "  if (this.haveChild(child)) {\n" +
        "    throw Error('Node: ' + this.getDataPath() +\n" +
        "                ' already has a child named ' + child);\n" +
        "  } else if (child.parentNode) {\n" +
        "    throw Error('Node: ' + child.getDataPath() +\n" +
        "                ' already has a parent');\n" +
        "  }\n" +
        "  child.parentNode = this;\n" +
        "};",

        "var A = function(){};\n" +
        "A.prototype.m = function(child) {\n" +
        "  if (this.haveChild(child)) {\n" +
        "    throw Error('a' + '`' + this.getDataPath() + '`' + child);\n" +
        "  } else if (child.parentNode) {\n" +
        "    throw Error('b' + '`' + child.getDataPath());\n" +
        "  }\n" +
        "  child.parentNode = this;\n" +
        "};",
        (new String[] {
            "a",
            "Node: ` already has a child named `",
            "b",
            "Node: ` already has a parent",
            }));
  }

// com.google.javascript.jscomp.ReplaceStringsTest::testThrowNonStringError
  public void testThrowNonStringError() {
    
    
    testDebugStrings(
        "throw Error(x('abc'));",
        "throw Error(x('abc'));",
        (new String[] { }));
  }

// com.google.javascript.jscomp.ReplaceStringsTest::testThrowConstStringError
  public void testThrowConstStringError() {
    testDebugStrings(
        "var AA = 'uvw', AB = 'xyz'; throw Error(AB);",
        "var AA = 'uvw', AB = 'xyz'; throw Error('a');",
        (new String [] { "a", "xyz" }));
  }

// com.google.javascript.jscomp.ReplaceStringsTest::testThrowNewError1
  public void testThrowNewError1() {
    testDebugStrings(
        "throw new Error('abc');",
        "throw new Error('a');",
        (new String[] { "a", "abc" }));
  }

// com.google.javascript.jscomp.ReplaceStringsTest::testThrowNewError2
  public void testThrowNewError2() {
    testDebugStrings(
        "throw new Error();",
        "throw new Error();",
        new String[] {});
  }

// com.google.javascript.jscomp.ReplaceStringsTest::testStartTracer1
  public void testStartTracer1() {
    testDebugStrings(
        "goog.debug.Trace.startTracer('HistoryManager.updateHistory');",
        "goog.debug.Trace.startTracer('a');",
        (new String[] { "a", "HistoryManager.updateHistory" }));
  }

// com.google.javascript.jscomp.ReplaceStringsTest::testStartTracer2
  public void testStartTracer2() {
    testDebugStrings(
        "goog$debug$Trace.startTracer('HistoryManager', 'updateHistory');",
        "goog$debug$Trace.startTracer('a', 'b');",
        (new String[] {
            "a", "HistoryManager",
            "b", "updateHistory" }));
  }

// com.google.javascript.jscomp.ReplaceStringsTest::testStartTracer3
  public void testStartTracer3() {
    testDebugStrings(
        "goog$debug$Trace.startTracer('ThreadlistView',\n" +
        "                             'Updating ' + array.length + ' rows');",
        "goog$debug$Trace.startTracer('a', 'b' + '`' + array.length);",
        new String[] { "a", "ThreadlistView", "b", "Updating ` rows" });
  }

// com.google.javascript.jscomp.ReplaceStringsTest::testStartTracer4
  public void testStartTracer4() {
    testDebugStrings(
        "goog.debug.Trace.startTracer(s, 'HistoryManager.updateHistory');",
        "goog.debug.Trace.startTracer(s, 'a');",
        (new String[] { "a", "HistoryManager.updateHistory" }));
  }

// com.google.javascript.jscomp.ReplaceStringsTest::testLoggerInitialization
  public void testLoggerInitialization() {
    testDebugStrings(
        "goog$debug$Logger$getLogger('my.app.Application');",
        "goog$debug$Logger$getLogger('a');",
        (new String[] { "a", "my.app.Application" }));
  }

// com.google.javascript.jscomp.ReplaceStringsTest::testLoggerOnObject1
  public void testLoggerOnObject1() {
    testDebugStrings(
        "var x = {};" +
        "x.logger_ = goog.debug.Logger.getLogger('foo');" +
        "x.logger_.info('Some message');",
        "var x$logger_ = goog.debug.Logger.getLogger('a');" +
        "x$logger_.info('b');",
        new String[] {
            "a", "foo",
            "b", "Some message"});
  }

// com.google.javascript.jscomp.ReplaceStringsTest::testLoggerOnObject2
  public void testLoggerOnObject2() {
    test(
        "var x = {};" +
        "x.info = function(a) {};" +
        "x.info('Some message');",
        "var x$info = function(a) {};" +
        "x$info('Some message');");
  }

// com.google.javascript.jscomp.ReplaceStringsTest::testLoggerOnObject3a
  public void testLoggerOnObject3a() {
    testSame(
        "\n" +
        "var x = function() {};\n" +
        "x.prototype.info = function(a) {};" +
        "(new x).info('Some message');");
  }

// com.google.javascript.jscomp.ReplaceStringsTest::testLoggerOnObject3b
  public void testLoggerOnObject3b() {
    testSame(
      "\n" +
      "var x = function() {};\n" +
      "x.prototype.info = function(a) {};" +
      "var y = (new x); this.info('Some message');");
  }

// com.google.javascript.jscomp.ReplaceStringsTest::testLoggerOnObject4
  public void testLoggerOnObject4() {
    testSame("(new x).info('Some message');");
  }

// com.google.javascript.jscomp.ReplaceStringsTest::testLoggerOnObject5
  public void testLoggerOnObject5() {
    testSame("my$Thing.logger_.info('Some message');");
  }

// com.google.javascript.jscomp.ReplaceStringsTest::testLoggerOnVar
  public void testLoggerOnVar() {
    testDebugStrings(
        "var logger = goog.debug.Logger.getLogger('foo');" +
        "logger.info('Some message');",
        "var logger = goog.debug.Logger.getLogger('a');" +
        "logger.info('b');",
        new String[] {
            "a", "foo",
            "b", "Some message"});
  }

// com.google.javascript.jscomp.ReplaceStringsTest::testLoggerOnThis
  public void testLoggerOnThis() {
    testDebugStrings(
        "function f() {" +
        "  this.logger_ = goog.debug.Logger.getLogger('foo');" +
        "  this.logger_.info('Some message');" +
        "}",
        "function f() {" +
        "  this.logger_ = goog.debug.Logger.getLogger('a');" +
        "  this.logger_.info('b');" +
        "}",
        new String[] {
            "a", "foo",
            "b", "Some message"});
  }

// com.google.javascript.jscomp.ReplaceStringsTest::testRepeatedErrorString1
  public void testRepeatedErrorString1() {
    testDebugStrings(
        "Error('abc');Error('def');Error('abc');",
        "Error('a');Error('b');Error('a');",
        (new String[] { "a", "abc", "b", "def" }));
  }

// com.google.javascript.jscomp.ReplaceStringsTest::testRepeatedErrorString2
  public void testRepeatedErrorString2() {
    testDebugStrings(
        "Error('a:' + u + ', b:' + v); Error('a:' + x + ', b:' + y);",
        "Error('a' + '`' + u + '`' + v); Error('a' + '`' + x + '`' + y);",
        (new String[] { "a", "a:`, b:`" }));
  }

// com.google.javascript.jscomp.ReplaceStringsTest::testRepeatedErrorString3
  public void testRepeatedErrorString3() {
    testDebugStrings(
        "var AB = 'b'; throw Error(AB); throw Error(AB);",
        "var AB = 'b'; throw Error('a'); throw Error('a');",
        (new String[] { "a", "b" }));
  }

// com.google.javascript.jscomp.ReplaceStringsTest::testRepeatedTracerString
  public void testRepeatedTracerString() {
    testDebugStrings(
        "goog$debug$Trace.startTracer('A', 'B', 'A');",
        "goog$debug$Trace.startTracer('a', 'b', 'a');",
        (new String[] { "a", "A", "b", "B" }));
  }

// com.google.javascript.jscomp.ReplaceStringsTest::testRepeatedLoggerString
  public void testRepeatedLoggerString() {
    testDebugStrings(
        "goog$debug$Logger$getLogger('goog.net.XhrTransport');" +
        "goog$debug$Logger$getLogger('my.app.Application');" +
        "goog$debug$Logger$getLogger('my.app.Application');",
        "goog$debug$Logger$getLogger('a');" +
        "goog$debug$Logger$getLogger('b');" +
        "goog$debug$Logger$getLogger('b');",
        new String[] {
            "a", "goog.net.XhrTransport","b", "my.app.Application" });
  }

// com.google.javascript.jscomp.ReplaceStringsTest::testRepeatedStringsWithDifferentMethods
  public void testRepeatedStringsWithDifferentMethods() {
    test(
        "throw Error('A');"
            + "goog$debug$Trace.startTracer('B', 'A');"
            + "goog$debug$Logger$getLogger('C');"
            + "goog$debug$Logger$getLogger('B');"
            + "goog$debug$Logger$getLogger('A');"
            + "throw Error('D');"
            + "throw Error('C');"
            + "throw Error('B');"
            + "throw Error('A');",
        "throw Error('a');"
            + "goog$debug$Trace.startTracer('b', 'a');"
            + "goog$debug$Logger$getLogger('c');"
            + "goog$debug$Logger$getLogger('b');"
            + "goog$debug$Logger$getLogger('a');"
            + "throw Error('d');"
            + "throw Error('c');"
            + "throw Error('b');"
            + "throw Error('a');");
  }

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
// com.google.javascript.jscomp.TypeCheckTest::testUnknownConstructorInstanceType3
  public void testUnknownConstructorInstanceType3() throws Exception {
    testTypes("function g(f) { var x = new f(); x.a = 1; return x; }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testUnknownPrototypeChain
  public void testUnknownPrototypeChain() throws Exception {
    testTypes("\n" +
              "function inst(co) {\n" +
              " \n" +
              " var c = function() {};\n" +
              " c.prototype = co.prototype;\n" +
              " return new c;\n" +
              "}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testNamespacedConstructor
  public void testNamespacedConstructor() throws Exception {
    Node root = parseAndTypeCheck(
        "var goog = {};" +
        " goog.MyClass = function() {};" +
        " " +
        "function foo() { return new goog.MyClass(); }");

    JSType typeOfFoo = root.getLastChild().getJSType();
    assert(typeOfFoo instanceof FunctionType);

    JSType retType = ((FunctionType) typeOfFoo).getReturnType();
    assert(retType instanceof ObjectType);
    assertEquals("goog.MyClass", ((ObjectType) retType).getReferenceName());
  }

// com.google.javascript.jscomp.TypeCheckTest::testComplexNamespace
  public void testComplexNamespace() throws Exception {
    String js =
      "var goog = {};" +
      "goog.foo = {};" +
      "goog.foo.bar = 5;";

    TypeCheckResult p = parseAndTypeCheckWithScope(js);

    
    JSType googScopeType = p.scope.getVar("goog").getType();
    assertTrue(googScopeType instanceof ObjectType);
    assertTrue("foo property not present on goog type",
        ((ObjectType) googScopeType).hasProperty("foo"));
    assertFalse("bar property present on goog type",
        ((ObjectType) googScopeType).hasProperty("bar"));

    
    Node varNode = p.root.getFirstChild();
    assertEquals(Token.VAR, varNode.getType());
    JSType googNodeType = varNode.getFirstChild().getJSType();
    assertTrue(googNodeType instanceof ObjectType);

    
    assertTrue(googScopeType == googNodeType);

    
    Node getpropFoo1 = varNode.getNext().getFirstChild().getFirstChild();
    assertEquals(Token.GETPROP, getpropFoo1.getType());
    assertEquals("goog", getpropFoo1.getFirstChild().getString());
    JSType googGetpropFoo1Type = getpropFoo1.getFirstChild().getJSType();
    assertTrue(googGetpropFoo1Type instanceof ObjectType);

    
    assertTrue(googGetpropFoo1Type == googScopeType);

    
    JSType googFooType = ((ObjectType) googScopeType).getPropertyType("foo");
    assertTrue(googFooType instanceof ObjectType);

    
    
    Node getpropFoo2 = varNode.getNext().getNext()
        .getFirstChild().getFirstChild().getFirstChild();
    assertEquals(Token.GETPROP, getpropFoo2.getType());
    assertEquals("goog", getpropFoo2.getFirstChild().getString());
    JSType googGetpropFoo2Type = getpropFoo2.getFirstChild().getJSType();
    assertTrue(googGetpropFoo2Type instanceof ObjectType);

    
    assertTrue(googGetpropFoo2Type == googScopeType);

    
    
    JSType googFooGetprop2Type = getpropFoo2.getJSType();
    assertTrue("goog.foo incorrectly annotated in goog.foo.bar selection",
        googFooGetprop2Type instanceof ObjectType);
    ObjectType googFooGetprop2ObjectType = (ObjectType) googFooGetprop2Type;
    assertFalse("foo property present on goog.foo type",
        googFooGetprop2ObjectType.hasProperty("foo"));
    assertTrue("bar property not present on goog.foo type",
        googFooGetprop2ObjectType.hasProperty("bar"));
    assertEquals("bar property on goog.foo type incorrectly inferred",
        NUMBER_TYPE, googFooGetprop2ObjectType.getPropertyType("bar"));
  }

// com.google.javascript.jscomp.TypeCheckTest::testAddingMethodsUsingPrototypeIdiomSimpleNamespace
  public void testAddingMethodsUsingPrototypeIdiomSimpleNamespace()
      throws Exception {
    Node js1Node = parseAndTypeCheck(
        "function A() {}" +
        "A.prototype.m1 = 5");

    ObjectType instanceType = getInstanceType(js1Node);
    assertEquals(NATIVE_PROPERTIES_COUNT + 1,
        instanceType.getPropertiesCount());
    checkObjectType(instanceType, "m1", NUMBER_TYPE);
  }

// com.google.javascript.jscomp.TypeCheckTest::testAddingMethodsUsingPrototypeIdiomComplexNamespace1
  public void testAddingMethodsUsingPrototypeIdiomComplexNamespace1()
      throws Exception {
    TypeCheckResult p = parseAndTypeCheckWithScope(
        "var goog = {};" +
        "goog.A = function() {};" +
        "goog.A.prototype.m1 = 5");

    testAddingMethodsUsingPrototypeIdiomComplexNamespace(p);
  }

// com.google.javascript.jscomp.TypeCheckTest::testAddingMethodsUsingPrototypeIdiomComplexNamespace2
  public void testAddingMethodsUsingPrototypeIdiomComplexNamespace2()
      throws Exception {
    TypeCheckResult p = parseAndTypeCheckWithScope(
        "var goog = {};" +
        "goog.A = function() {};" +
        "goog.A.prototype.m1 = 5");

    testAddingMethodsUsingPrototypeIdiomComplexNamespace(p);
  }

// com.google.javascript.jscomp.TypeCheckTest::testAddingMethodsPrototypeIdiomAndObjectLiteralSimpleNamespace
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

// com.google.javascript.jscomp.TypeCheckTest::testDontAddMethodsIfNoConstructor
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

// com.google.javascript.jscomp.TypeCheckTest::testFunctionAssignement
  public void testFunctionAssignement() throws Exception {
    testTypes("" +
        "function MSG_CALENDAR_ACCESS_ERROR(ph0, ph1) {return ''}" +
        "" +
        "var MSG_CALENDAR_ADD_ERROR = MSG_CALENDAR_ACCESS_ERROR;");
  }

// com.google.javascript.jscomp.TypeCheckTest::testAddMethodsPrototypeTwoWays
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

// com.google.javascript.jscomp.TypeCheckTest::testPrototypePropertyTypes
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
        createUnionType(OBJECT_TYPE, NULL_TYPE));
    checkObjectType(instanceType, "m3", BOOLEAN_TYPE);
    checkObjectType(instanceType, "m4", STRING_TYPE);
    checkObjectType(instanceType, "m5", NUMBER_TYPE);
    checkObjectType(instanceType, "m6", BOOLEAN_TYPE);
  }

// com.google.javascript.jscomp.TypeCheckTest::testValueTypeBuiltInPrototypePropertyType
  public void testValueTypeBuiltInPrototypePropertyType() throws Exception {
    Node node = parseAndTypeCheck("\"x\".charAt(0)");
    assertEquals(STRING_TYPE, node.getFirstChild().getFirstChild().getJSType());
  }

// com.google.javascript.jscomp.TypeCheckTest::testDeclareBuiltInConstructor
  public void testDeclareBuiltInConstructor() throws Exception {
    
    
    Node node = parseAndTypeCheck(
        " var String = function(opt_str) {};\n" +
        "(new String(\"x\")).charAt(0)");
    assertEquals(STRING_TYPE, node.getLastChild().getFirstChild().getJSType());
  }

// com.google.javascript.jscomp.TypeCheckTest::testExtendBuiltInType1
  public void testExtendBuiltInType1() throws Exception {
    String externs =
        " var String = function(opt_str) {};\n" +
        "\n" +
        "String.prototype.substr = function(start, opt_length) {};\n";
    Node n1 = parseAndTypeCheck(externs + "(new String(\"x\")).substr(0,1);");
    assertEquals(STRING_TYPE, n1.getLastChild().getFirstChild().getJSType());
  }

// com.google.javascript.jscomp.TypeCheckTest::testExtendBuiltInType2
  public void testExtendBuiltInType2() throws Exception {
    String externs =
        " var String = function(opt_str) {};\n" +
        "\n" +
        "String.prototype.substr = function(start, opt_length) {};\n";
    Node n2 = parseAndTypeCheck(externs + "\"x\".substr(0,1);");
    assertEquals(STRING_TYPE, n2.getLastChild().getFirstChild().getJSType());
  }

// com.google.javascript.jscomp.TypeCheckTest::testExtendFunction1
  public void testExtendFunction1() throws Exception {
    Node n = parseAndTypeCheck("Function.prototype.f = " +
        "function() { return 1; };\n" +
        "(new Function()).f();");
    JSType type = n.getLastChild().getLastChild().getJSType();
    assertEquals(NUMBER_TYPE, type);
  }

// com.google.javascript.jscomp.TypeCheckTest::testExtendFunction2
  public void testExtendFunction2() throws Exception {
    Node n = parseAndTypeCheck("Function.prototype.f = " +
        "function() { return 1; };\n" +
        "(function() {}).f();");
    JSType type = n.getLastChild().getLastChild().getJSType();
    assertEquals(NUMBER_TYPE, type);
  }

// com.google.javascript.jscomp.TypeCheckTest::testInheritanceCheck1
  public void testInheritanceCheck1() throws Exception {
    testTypes(
        "function Super() {};" +
        "function Sub() {};" +
        "Sub.prototype.foo = function() {};");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInheritanceCheck2
  public void testInheritanceCheck2() throws Exception {
    testTypes(
        "function Super() {};" +
        "function Sub() {};" +
        "Sub.prototype.foo = function() {};",
        "property foo not defined on any superclass of Sub");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInheritanceCheck3
  public void testInheritanceCheck3() throws Exception {
    testTypes(
        "function Super() {};" +
        "Super.prototype.foo = function() {};" +
        "function Sub() {};" +
        "Sub.prototype.foo = function() {};",
        "property foo already defined on superclass Super; " +
        "use @override to override it");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInheritanceCheck4
  public void testInheritanceCheck4() throws Exception {
    testTypes(
        "function Super() {};" +
        "Super.prototype.foo = function() {};" +
        "function Sub() {};" +
        "Sub.prototype.foo = function() {};");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInheritanceCheck5
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

// com.google.javascript.jscomp.TypeCheckTest::testInheritanceCheck6
  public void testInheritanceCheck6() throws Exception {
    testTypes(
        "function Root() {};" +
        "Root.prototype.foo = function() {};" +
        "function Super() {};" +
        "function Sub() {};" +
        "Sub.prototype.foo = function() {};");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInheritanceCheck7
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
        "goog.Sub.prototype.foo = \"some string\";",
        "mismatch of the foo property type and the type of the property it " +
        "overrides from superclass goog.Super\n" +
        "original: number\n" +
        "override: string");
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
        " foo;\n" +
        "foo.bar();");
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
    assertEquals(NUMBER_TYPE, objectType.getPropertyType("m1"));
    assertEquals(STRING_TYPE, objectType.getPropertyType("m2"));

    
    assertEquals(objectType, nameNode.getJSType());
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
    testClosureTypesMultipleWarnings(
        "var x = {" +
        "   abc: function(x) {}" +
        "};" +
        " x.abc = function(x) {};",
        Lists.newArrayList(
            "variable x.abc redefined with type " +
            "function (string): undefined, " +
            "original definition at  [testcode] :1 with type " +
            "function (boolean): undefined",
            "assignment to property abc of x\n" +
            "found   : function (string): undefined\n" +
            "required: function (boolean): undefined"));
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
    assertEquals(STRING_TYPE, n.getFirstChild().getFirstChild().getJSType());
  }

// com.google.javascript.jscomp.TypeCheckTest::testCallErrorConstructorAsFunction
  public void testCallErrorConstructorAsFunction() throws Exception {
    Node n = parseAndTypeCheck("Error('x')");
    assertEquals(ERROR_TYPE,
                 n.getFirstChild().getFirstChild().getJSType());
  }

// com.google.javascript.jscomp.TypeCheckTest::testCallArrayConstructorAsFunction
  public void testCallArrayConstructorAsFunction() throws Exception {
    Node n = parseAndTypeCheck("Array()");
    assertEquals(ARRAY_TYPE,
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
    testTypes(" function T() {};\n" +
        "T.prototype.x = 1",
        "interface members can only be empty property declarations, "
        + "empty functions, or goog.abstractMethod"
        );
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

// com.google.javascript.jscomp.TypeCheckTest::testDirectPrototypeAssign
  public void testDirectPrototypeAssign() throws Exception {
    testTypes(
        " function Foo() {}" +
        " function Bar() {}" +
        " Bar.prototype = new Foo()",
        "assignment to property prototype of Bar\n" +
        "found   : Foo\n" +
        "required: (Array|null)");
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
    assertEquals(
        Lists.newArrayList(objectType),
        registry.getTypesWithProperty("x"));
  }

// com.google.javascript.jscomp.TypeCheckTest::testGatherProperyWithoutAnnotation2
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
        "condition always evaluates to the same value\n" +
        "left : (null|string)\n" +
        "right: number");
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
        "function f(x) { if (x.foo) { } else { x.foo(); } }");
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

// com.google.javascript.jscomp.TypeCheckTest::testDeclaredNativeTypeEquality
  public void testDeclaredNativeTypeEquality() throws Exception {
    Node n = parseAndTypeCheck(" function Object() {};");
    assertEquals(registry.getNativeType(JSTypeNative.OBJECT_FUNCTION_TYPE),
                 n.getFirstChild().getJSType());
  }

// com.google.javascript.jscomp.TypeCheckTest::testUndefinedVar
  public void testUndefinedVar() throws Exception {
    Node n = parseAndTypeCheck("var undefined;");
    assertEquals(registry.getNativeType(JSTypeNative.VOID_TYPE),
                 n.getFirstChild().getFirstChild().getJSType());
  }

// com.google.javascript.jscomp.TypeCheckTest::testFlowScopeBug1
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

    
    assertEquals(registry.createNullableType(registry.getType("Foo")),
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

// com.google.javascript.jscomp.TypeCheckTest::testBadTemplateType1
  public void testBadTemplateType1() throws Exception {
    testTypes(
        "\n" +
        "function f(x, y, z) {}\n" +
        "f(this, this, function() { this });",
        FunctionTypeBuilder.TEMPLATE_TYPE_DUPLICATED.format(), true);
  }

// com.google.javascript.jscomp.TypeCheckTest::testBadTemplateType2
  public void testBadTemplateType2() throws Exception {
    testTypes(
        "\n" +
        "function f(x, y) {}\n" +
        "f(0, function() {});",
        TypeInference.TEMPLATE_TYPE_NOT_OBJECT_TYPE.format(), true);
  }

// com.google.javascript.jscomp.TypeCheckTest::testBadTemplateType3
  public void testBadTemplateType3() throws Exception {
    testTypes(
        "\n" +
        "function f(x) {}\n" +
        "f(this);",
        TypeInference.TEMPLATE_TYPE_OF_THIS_EXPECTED.format(), true);
  }

// com.google.javascript.jscomp.TypeCheckTest::testBadTemplateType4
  public void testBadTemplateType4() throws Exception {
    testTypes(
        "\n" +
        "function f() {}\n" +
        "f();",
        FunctionTypeBuilder.TEMPLATE_TYPE_EXPECTED.format(), true);
  }

// com.google.javascript.jscomp.TypeCheckTest::testBadTemplateType5
  public void testBadTemplateType5() throws Exception {
    testTypes(
        "\n" +
        "function f() {}\n" +
        "f();",
        FunctionTypeBuilder.TEMPLATE_TYPE_EXPECTED.format(), true);
  }

// com.google.javascript.jscomp.TypeCheckTest::testFunctionLiteralUndefinedThisArgument
  public void testFunctionLiteralUndefinedThisArgument() throws Exception {
    testTypes(""
        + "\n"
        + "function baz(fn, opt_obj) {}\n"
        + "baz(function() { this; });",
        "Function literal argument refers to undefined this argument");
  }

// com.google.javascript.jscomp.TypeCheckTest::testFunctionLiteralDefinedThisArgument
  public void testFunctionLiteralDefinedThisArgument() throws Exception {
    testTypes(""
        + "\n"
        + "function baz(fn, opt_obj) {}\n"
        + "baz(function() { this; }, {});");
  }

// com.google.javascript.jscomp.TypeCheckTest::testFunctionLiteralUnreadThisArgument
  public void testFunctionLiteralUnreadThisArgument() throws Exception {
    testTypes(""
        + "\n"
        + "function baz(fn, opt_obj) {}\n"
        + "baz(function() {}, {});",
        "Function literal argument does not refer to bound this argument");
  }

// com.google.javascript.jscomp.TypeCheckTest::testFunctionLiteralUnreadNullThisArgument
  public void testFunctionLiteralUnreadNullThisArgument() throws Exception {
    testTypes(""
        + "\n"
        + "function baz(fn, opt_obj) {}\n"
        + "baz(function() {}, null);");
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
        "Int2 cannot extend this type; a constructor can only extend " +
        "objects and an interface can only extend interfaces");
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

// com.google.javascript.jscomp.TypeInferenceTest::testAssert1
  public void testAssert1() {
    JSType startType = createNullableType(OBJECT_TYPE);
    assuming("x", startType);
    inFunction("out1 = x; goog.asserts.assert(x); out2 = x;");
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

// com.google.javascript.jscomp.TypeInferenceTest::testAssertNumber
  public void testAssertNumber() {
    JSType startType = createNullableType(ALL_TYPE);
    assuming("x", startType);
    inFunction("out1 = x; goog.asserts.assertNumber(x); out2 = x;");
    verify("out1", startType);
    verify("out2", NUMBER_TYPE);
  }

// com.google.javascript.jscomp.TypeInferenceTest::testAssertNumber2
  public void testAssertNumber2() {
    
    JSType startType = createNullableType(ALL_TYPE);
    assuming("x", startType);
    inFunction("goog.asserts.assertNumber(x + x); out1 = x;");
    verify("out1", startType);
  }

// com.google.javascript.jscomp.TypeInferenceTest::testAssertString
  public void testAssertString() {
    JSType startType = createNullableType(ALL_TYPE);
    assuming("x", startType);
    inFunction("out1 = x; goog.asserts.assertString(x); out2 = x;");
    verify("out1", startType);
    verify("out2", STRING_TYPE);
  }

// com.google.javascript.jscomp.TypeInferenceTest::testAssertFunction
  public void testAssertFunction() {
    JSType startType = createNullableType(ALL_TYPE);
    assuming("x", startType);
    inFunction("out1 = x; goog.asserts.assertFunction(x); out2 = x;");
    verify("out1", startType);
    verifySubtypeOf("out2", FUNCTION_INSTANCE_TYPE);
  }

// com.google.javascript.jscomp.TypeInferenceTest::testAssertObject
  public void testAssertObject() {
    JSType startType = createNullableType(ALL_TYPE);
    assuming("x", startType);
    inFunction("out1 = x; goog.asserts.assertObject(x); out2 = x;");
    verify("out1", startType);
    verifySubtypeOf("out2", OBJECT_TYPE);
  }

// com.google.javascript.jscomp.TypeInferenceTest::testAssertObject2
  public void testAssertObject2() {
    JSType startType = createNullableType(ARRAY_TYPE);
    assuming("x", startType);
    inFunction("out1 = x; goog.asserts.assertObject(x); out2 = x;");
    verify("out1", startType);
    verify("out2", ARRAY_TYPE);
  }

// com.google.javascript.jscomp.TypeInferenceTest::testAssertObject3
  public void testAssertObject3() {
    JSType startType = createNullableType(OBJECT_TYPE);
    assuming("x.y", startType);
    inFunction("out1 = x.y; goog.asserts.assertObject(x.y); out2 = x.y;");
    verify("out1", startType);
    verify("out2", OBJECT_TYPE);
  }

// com.google.javascript.jscomp.TypeInferenceTest::testAssertObject4
  public void testAssertObject4() {
    JSType startType = createNullableType(ARRAY_TYPE);
    assuming("x", startType);
    inFunction("out1 = x; out2 = goog.asserts.assertObject(x);");
    verify("out1", startType);
    verify("out2", ARRAY_TYPE);
  }

// com.google.javascript.jscomp.TypeInferenceTest::testAssertArray
  public void testAssertArray() {
    JSType startType = createNullableType(ALL_TYPE);
    assuming("x", startType);
    inFunction("out1 = x; goog.asserts.assertArray(x); out2 = x;");
    verify("out1", startType);
    verifySubtypeOf("out2", ARRAY_TYPE);
  }

// com.google.javascript.jscomp.TypeInferenceTest::testAssertInstanceof
  public void testAssertInstanceof() {
    JSType startType = createNullableType(ALL_TYPE);
    assuming("x", startType);
    inFunction("out1 = x; goog.asserts.assertInstanceof(x); out2 = x;");
    verify("out1", startType);
    verifySubtypeOf("out2", OBJECT_TYPE);
  }

// com.google.javascript.jscomp.TypeInferenceTest::testReturn1
  public void testReturn1() {
    assuming("x", createNullableType(OBJECT_TYPE));
    inFunction("if (x) { return x; }\nx = {};\nreturn x;");
    verify("x", OBJECT_TYPE);
  }

// com.google.javascript.jscomp.TypeInferenceTest::testReturn2
  public void testReturn2() {
    assuming("x", createNullableType(NUMBER_TYPE));
    inFunction("if (!x) { x = 0; }\nreturn x;");
    verify("x", NUMBER_TYPE);
  }

// com.google.javascript.jscomp.TypeInferenceTest::testWhile1
  public void testWhile1() {
    assuming("x", createNullableType(NUMBER_TYPE));
    inFunction("while (!x) { if (x == null) { x = 0; } else { x = 1; } }");
    verify("x", NUMBER_TYPE);
  }

// com.google.javascript.jscomp.TypeInferenceTest::testWhile2
  public void testWhile2() {
    assuming("x", createNullableType(NUMBER_TYPE));
    inFunction("while (!x) { x = {}; }");
    verifySubtypeOf("x", createUnionType(OBJECT_TYPE, NUMBER_TYPE));
  }

// com.google.javascript.jscomp.TypeInferenceTest::testDo
  public void testDo() {
    assuming("x", createNullableType(OBJECT_TYPE));
    inFunction("do { x = 1; } while (!x);");
    verify("x", NUMBER_TYPE);
  }

// com.google.javascript.jscomp.TypeInferenceTest::testFor1
  public void testFor1() {
    assuming("y", NUMBER_TYPE);
    inFunction("var x = null; var i = null; for (i=y; !i; i=1) { x = 1; }");
    verify("x", createNullableType(NUMBER_TYPE));
    verify("i", NUMBER_TYPE);
  }

// com.google.javascript.jscomp.TypeInferenceTest::testFor2
  public void testFor2() {
    assuming("y", OBJECT_TYPE);
    inFunction("var x = null; var i = null; for (i in y) { x = 1; }");
    verify("x", createNullableType(NUMBER_TYPE));
    verify("i", createNullableType(STRING_TYPE));
  }

// com.google.javascript.jscomp.TypeInferenceTest::testFor3
  public void testFor3() {
    assuming("y", OBJECT_TYPE);
    inFunction("var x = null; var i = null; for (var i in y) { x = 1; }");
    verify("x", createNullableType(NUMBER_TYPE));
    verify("i", createNullableType(STRING_TYPE));
  }

// com.google.javascript.jscomp.TypeInferenceTest::testFor4
  public void testFor4() {
    assuming("x", createNullableType(OBJECT_TYPE));
    inFunction("var y = {};\n"  +
        "if (x) { for (var i = 0; i < 10; i++) { break; } y = x; }");
    verifySubtypeOf("y", OBJECT_TYPE);
  }

// com.google.javascript.jscomp.TypeInferenceTest::testSwitch1
  public void testSwitch1() {
    assuming("x", NUMBER_TYPE);
    inFunction("var y = null; switch(x) {\n" +
        "case 1: y = 1; break;\n" +
        "case 2: y = {};\n" +
        "case 3: y = {};\n" +
        "default: y = 0;}");
    verify("y", NUMBER_TYPE);
  }

// com.google.javascript.jscomp.TypeInferenceTest::testSwitch2
  public void testSwitch2() {
    assuming("x", ALL_TYPE);
    inFunction("var y = null; switch (typeof x) {\n" +
        "case 'string':\n" +
        "  y = x;\n" +
        "  return;" +
        "default:\n" +
        "  y = 'a';\n" +
        "}");
    verify("y", STRING_TYPE);
  }

// com.google.javascript.jscomp.TypeInferenceTest::testSwitch3
  public void testSwitch3() {
    assuming("x",
        createNullableType(createUnionType(NUMBER_TYPE, STRING_TYPE)));
    inFunction("var y; var z; switch (typeof x) {\n" +
        "case 'string':\n" +
        "  y = 1; z = null;\n" +
        "  return;\n" +
        "case 'number':\n" +
        "  y = x; z = null;\n" +
        "  return;" +
        "default:\n" +
        "  y = 1; z = x;\n" +
        "}");
    verify("y", NUMBER_TYPE);
    verify("z", NULL_TYPE);
  }

// com.google.javascript.jscomp.TypeInferenceTest::testSwitch4
  public void testSwitch4() {
    assuming("x", ALL_TYPE);
    inFunction("var y = null; switch (typeof x) {\n" +
        "case 'string':\n" +
        "case 'number':\n" +
        "  y = x;\n" +
        "  return;\n" +
        "default:\n" +
        "  y = 1;\n" +
        "}\n");
    verify("y", createUnionType(NUMBER_TYPE, STRING_TYPE));
  }

// com.google.javascript.jscomp.TypeInferenceTest::testCall1
  public void testCall1() {
    assuming("x",
        createNullableType(
            registry.createFunctionType(registry.getNativeType(NUMBER_TYPE))));
    inFunction("var y = x();");
    verify("y", NUMBER_TYPE);
  }

// com.google.javascript.jscomp.TypeInferenceTest::testNew1
  public void testNew1() {
    assuming("x",
        createNullableType(
            registry.getNativeType(JSTypeNative.U2U_CONSTRUCTOR_TYPE)));
    inFunction("var y = new x();");
    verify("y", JSTypeNative.NO_OBJECT_TYPE);
  }

// com.google.javascript.jscomp.TypeInferenceTest::testInnerFunction1
  public void testInnerFunction1() {
    inFunction("var x = 1; function f() { x = null; };");
    verify("x", NUMBER_TYPE);
  }

// com.google.javascript.jscomp.TypeInferenceTest::testInnerFunction2
  public void testInnerFunction2() {
    inFunction("var x = 1; var f = function() { x = null; };");
    verify("x", NUMBER_TYPE);
  }

// com.google.javascript.jscomp.TypeInferenceTest::testHook
  public void testHook() {
    assuming("x", createNullableType(OBJECT_TYPE));
    inFunction("var y = x ? x : {};");
    verifySubtypeOf("y", OBJECT_TYPE);
  }

// com.google.javascript.jscomp.TypeInferenceTest::testThrow
  public void testThrow() {
    assuming("x", createNullableType(NUMBER_TYPE));
    inFunction("var y = 1;\n" +
        "if (x == null) { throw new Error('x is null') }\n" +
        "y = x;");
    verify("y", NUMBER_TYPE);
  }

// com.google.javascript.jscomp.TypeInferenceTest::testTry1
  public void testTry1() {
    assuming("x", NUMBER_TYPE);
    inFunction("var y = null; try { y = null; } finally { y = x; }");
    verify("y", NUMBER_TYPE);
  }

// com.google.javascript.jscomp.TypeInferenceTest::testTry2
  public void testTry2() {
    assuming("x", NUMBER_TYPE);
    inFunction("var y = null;\n" +
        "try {  } catch (e) { y = null; } finally { y = x; }");
    verify("y", NUMBER_TYPE);
  }

// com.google.javascript.jscomp.TypeInferenceTest::testTry3
  public void testTry3() {
    assuming("x", NUMBER_TYPE);
    inFunction("var y = null; try { y = x; } catch (e) { }");
    verify("y", NUMBER_TYPE);
  }

// com.google.javascript.jscomp.TypeInferenceTest::testCatch1
  public void testCatch1() {
    inFunction("var y = null; try { foo(); } catch (e) { y = e; }");
    verify("y", UNKNOWN_TYPE);
  }

// com.google.javascript.jscomp.TypeInferenceTest::testCatch2
  public void testCatch2() {
    inFunction("var y = null; var e = 3; try { foo(); } catch (e) { y = e; }");
    verify("y", UNKNOWN_TYPE);
  }

// com.google.javascript.jscomp.TypeInferenceTest::testUnknownType1
  public void testUnknownType1() {
    inFunction("var y = 3; y = x;");
    verify("y", UNKNOWN_TYPE);
  }

// com.google.javascript.jscomp.TypeInferenceTest::testUnknownType2
  public void testUnknownType2() {
    assuming("x", ARRAY_TYPE);
    inFunction("var y = 5; y = x[0];");
    verify("y", UNKNOWN_TYPE);
  }

// com.google.javascript.jscomp.TypeInferenceTest::testInfiniteLoop1
  public void testInfiniteLoop1() {
    assuming("x", createNullableType(OBJECT_TYPE));
    inFunction("x = {}; while(x != null) { x = {}; }");
  }

// com.google.javascript.jscomp.TypeInferenceTest::testInfiniteLoop2
  public void testInfiniteLoop2() {
    assuming("x", createNullableType(OBJECT_TYPE));
    inFunction("x = {}; do { x = null; } while (x == null);");
  }

// com.google.javascript.jscomp.TypeInferenceTest::testJoin1
  public void testJoin1() {
    JSType unknownOrNull = createUnionType(NULL_TYPE, UNKNOWN_TYPE);
    assuming("x", BOOLEAN_TYPE);
    assuming("unknownOrNull", unknownOrNull);
    inFunction("var y; if (x) y = unknownOrNull; else y = null;");
    verify("y", unknownOrNull);
  }

// com.google.javascript.jscomp.TypeInferenceTest::testJoin2
  public void testJoin2() {
    JSType unknownOrNull = createUnionType(NULL_TYPE, UNKNOWN_TYPE);
    assuming("x", BOOLEAN_TYPE);
    assuming("unknownOrNull", unknownOrNull);
    inFunction("var y; if (x) y = null; else y = unknownOrNull;");
    verify("y", unknownOrNull);
  }

// com.google.javascript.jscomp.TypeInferenceTest::testArrayLit
  public void testArrayLit() {
    assuming("x", createNullableType(OBJECT_TYPE));
    inFunction("var y = 3; if (x) { x = [y = x]; }");
    verify("x", createUnionType(NULL_TYPE, ARRAY_TYPE));
    verify("y", createUnionType(NUMBER_TYPE, OBJECT_TYPE));
  }

// com.google.javascript.jscomp.TypeInferenceTest::testGetElem
  public void testGetElem() {
    assuming("x", createNullableType(OBJECT_TYPE));
    inFunction("var y = 3; if (x) { x = x[y = x]; }");
    verify("x", UNKNOWN_TYPE);
    verify("y", createUnionType(NUMBER_TYPE, OBJECT_TYPE));
  }

// com.google.javascript.jscomp.TypeInferenceTest::testEnumRAI1
  public void testEnumRAI1() {
    JSType enumType = createEnumType("MyEnum", ARRAY_TYPE).getElementsType();
    assuming("x", enumType);
    inFunction("var y = null; if (x) y = x;");
    verify("y", createNullableType(enumType));
  }

// com.google.javascript.jscomp.TypeInferenceTest::testEnumRAI2
  public void testEnumRAI2() {
    JSType enumType = createEnumType("MyEnum", NUMBER_TYPE).getElementsType();
    assuming("x", enumType);
    inFunction("var y = null; if (typeof x == 'number') y = x;");
    verify("y", createNullableType(enumType));
  }

// com.google.javascript.jscomp.TypeInferenceTest::testEnumRAI3
  public void testEnumRAI3() {
    JSType enumType = createEnumType("MyEnum", NUMBER_TYPE).getElementsType();
    assuming("x", enumType);
    inFunction("var y = null; if (x && typeof x == 'number') y = x;");
    verify("y", createNullableType(enumType));
  }

// com.google.javascript.jscomp.TypeInferenceTest::testEnumRAI4
  public void testEnumRAI4() {
    JSType enumType = createEnumType("MyEnum",
        createUnionType(STRING_TYPE, NUMBER_TYPE)).getElementsType();
    assuming("x", enumType);
    inFunction("var y = null; if (typeof x == 'number') y = x;");
    verify("y", createNullableType(NUMBER_TYPE));
  }

// com.google.javascript.jscomp.TypeInferenceTest::testShortCircuitingAnd
  public void testShortCircuitingAnd() {
    assuming("x", NUMBER_TYPE);
    inFunction("var y = null; if (x && (y = 3)) { }");
    verify("y", createNullableType(NUMBER_TYPE));
  }

// com.google.javascript.jscomp.TypeInferenceTest::testShortCircuitingAnd2
  public void testShortCircuitingAnd2() {
    assuming("x", NUMBER_TYPE);
    inFunction("var y = null; var z = 4; if (x && (y = 3)) { z = y; }");
    verify("z", NUMBER_TYPE);
  }

// com.google.javascript.jscomp.TypeInferenceTest::testShortCircuitingOr
  public void testShortCircuitingOr() {
    assuming("x", NUMBER_TYPE);
    inFunction("var y = null; if (x || (y = 3)) { }");
    verify("y", createNullableType(NUMBER_TYPE));
  }

// com.google.javascript.jscomp.TypeInferenceTest::testShortCircuitingOr2
  public void testShortCircuitingOr2() {
    assuming("x", NUMBER_TYPE);
    inFunction("var y = null; var z = 4; if (x || (y = 3)) { z = y; }");
    verify("z", createNullableType(NUMBER_TYPE));
  }

// com.google.javascript.jscomp.TypeInferenceTest::testAssignInCondition
  public void testAssignInCondition() {
    assuming("x", createNullableType(NUMBER_TYPE));
    inFunction("var y; if (!(y = x)) { y = 3; }");
    verify("y", NUMBER_TYPE);
  }

// com.google.javascript.jscomp.TypeInferenceTest::testInstanceOf1
  public void testInstanceOf1() {
    assuming("x", OBJECT_TYPE);
    inFunction("var y = null; if (x instanceof String) y = x;");
    verify("y", createNullableType(STRING_OBJECT_TYPE));
  }

// com.google.javascript.jscomp.TypeInferenceTest::testInstanceOf2
  public void testInstanceOf2() {
    assuming("x", createNullableType(OBJECT_TYPE));
    inFunction("var y = 1; if (x instanceof String) y = x;");
    verify("y", createUnionType(STRING_OBJECT_TYPE, NUMBER_TYPE));
  }

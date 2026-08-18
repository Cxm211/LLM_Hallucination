// buggy code
  String toStringHelper(boolean forAnnotations) {
    if (hasReferenceName()) {
      return getReferenceName();
    } else if (prettyPrint) {
      // Don't pretty print recursively.
      prettyPrint = false;

      // Use a tree set so that the properties are sorted.
      Set<String> propertyNames = Sets.newTreeSet();
      for (ObjectType current = this;
           current != null && !current.isNativeObjectType() &&
               propertyNames.size() <= MAX_PRETTY_PRINTED_PROPERTIES;
           current = current.getImplicitPrototype()) {
        propertyNames.addAll(current.getOwnPropertyNames());
      }

      StringBuilder sb = new StringBuilder();
      sb.append("{");

      int i = 0;
      for (String property : propertyNames) {
        if (i > 0) {
          sb.append(", ");
        }

        sb.append(property);
        sb.append(": ");
        sb.append(getPropertyType(property).toString());

        ++i;
        if (i == MAX_PRETTY_PRINTED_PROPERTIES) {
          sb.append(", ...");
          break;
        }
      }

      sb.append("}");

      prettyPrint = true;
      return sb.toString();
    } else {
      return "{...}";
    }
  }

// relevant test
// com.google.javascript.rhino.NodeTest::testCheckTreeEqualsStringDifferent
  public void testCheckTreeEqualsStringDifferent() {
    Node node1 = new Node(Token.ADD);
    Node node2 = new Node(Token.SUB);
    assertNotNull(node1.checkTreeEquals(node2));
  }

// com.google.javascript.rhino.NodeTest::testCheckTreeEqualsBooleanSame
  public void testCheckTreeEqualsBooleanSame() {
    Node node1 = new Node(1);
    assertEquals(true, node1.isEquivalentTo(node1));
  }

// com.google.javascript.rhino.NodeTest::testCheckTreeEqualsBooleanDifferent
  public void testCheckTreeEqualsBooleanDifferent() {
    Node node1 = new Node(1);
    Node node2 = new Node(2);
    assertEquals(false, node1.isEquivalentTo(node2));
  }

// com.google.javascript.rhino.NodeTest::testCheckTreeEqualsSlashVDifferent
  public void testCheckTreeEqualsSlashVDifferent() {
    Node node1 = Node.newString("\u000B");
    node1.putBooleanProp(Node.SLASH_V, true);
    Node node2 = Node.newString("\u000B");
    assertEquals(false, node1.isEquivalentTo(node2));
  }

// com.google.javascript.rhino.NodeTest::testCheckTreeEqualsImplDifferentIncProp
  public void testCheckTreeEqualsImplDifferentIncProp() {
    Node node1 = new Node(Token.INC);
    node1.putIntProp(Node.INCRDECR_PROP, 1);
    Node node2 = new Node(Token.INC);
    assertNotNull(node1.checkTreeEqualsImpl(node2));
  }

// com.google.javascript.rhino.NodeTest::testCheckTreeTypeAwareEqualsSame
  public void testCheckTreeTypeAwareEqualsSame() {
    TestErrorReporter testErrorReporter = new TestErrorReporter(null, null);
    JSTypeRegistry registry = new JSTypeRegistry(testErrorReporter);
    Node node1 = Node.newString(Token.NAME, "f");
    node1.setJSType(registry.getNativeType(JSTypeNative.NUMBER_TYPE));
    Node node2 = Node.newString(Token.NAME, "f");
    node2.setJSType(registry.getNativeType(JSTypeNative.NUMBER_TYPE));
    assertTrue(node1.isEquivalentToTyped(node2));
  }

// com.google.javascript.rhino.NodeTest::testCheckTreeTypeAwareEqualsSameNull
  public void testCheckTreeTypeAwareEqualsSameNull() {
    TestErrorReporter testErrorReporter = new TestErrorReporter(null, null);
    JSTypeRegistry registry = new JSTypeRegistry(testErrorReporter);
    Node node1 = Node.newString(Token.NAME, "f");
    Node node2 = Node.newString(Token.NAME, "f");
    assertTrue(node1.isEquivalentToTyped(node2));
  }

// com.google.javascript.rhino.NodeTest::testCheckTreeTypeAwareEqualsDifferent
  public void testCheckTreeTypeAwareEqualsDifferent() {
    TestErrorReporter testErrorReporter = new TestErrorReporter(null, null);
    JSTypeRegistry registry = new JSTypeRegistry(testErrorReporter);
    Node node1 = Node.newString(Token.NAME, "f");
    node1.setJSType(registry.getNativeType(JSTypeNative.NUMBER_TYPE));
    Node node2 = Node.newString(Token.NAME, "f");
    node2.setJSType(registry.getNativeType(JSTypeNative.STRING_TYPE));
    assertFalse(node1.isEquivalentToTyped(node2));
  }

// com.google.javascript.rhino.NodeTest::testCheckTreeTypeAwareEqualsDifferentNull
  public void testCheckTreeTypeAwareEqualsDifferentNull() {
    TestErrorReporter testErrorReporter = new TestErrorReporter(null, null);
    JSTypeRegistry registry = new JSTypeRegistry(testErrorReporter);
    Node node1 = Node.newString(Token.NAME, "f");
    node1.setJSType(registry.getNativeType(JSTypeNative.NUMBER_TYPE));
    Node node2 = Node.newString(Token.NAME, "f");
    assertFalse(node1.isEquivalentToTyped(node2));
  }

// com.google.javascript.rhino.NodeTest::testVarArgs1
  public void testVarArgs1() {
    assertFalse(new Node(1).isVarArgs());
  }

// com.google.javascript.rhino.NodeTest::testVarArgs2
  public void testVarArgs2() {
    Node n = new Node(1);
    n.setVarArgs(false);
    assertFalse(n.isVarArgs());
  }

// com.google.javascript.rhino.NodeTest::testVarArgs3
  public void testVarArgs3() {
    Node n = new Node(1);
    n.setVarArgs(true);
    assertTrue(n.isVarArgs());
  }

// com.google.javascript.rhino.NodeTest::testFileLevelJSDocAppender
  public void testFileLevelJSDocAppender() {
    Node n = new Node(1);
    Node.FileLevelJsDocBuilder builder = n.getJsDocBuilderForNode();
    builder.append("foo");
    builder.append("bar");
    assertEquals("foobar", n.getJSDocInfo().getLicense());
  }

// com.google.javascript.rhino.NodeTest::testCloneAnnontations
  public void testCloneAnnontations() {
    Node n = getVarRef("a");
    assertFalse(n.getBooleanProp(Node.IS_CONSTANT_NAME));
    n.putBooleanProp(Node.IS_CONSTANT_NAME, true);
    assertTrue(n.getBooleanProp(Node.IS_CONSTANT_NAME));

    Node nodeClone = n.cloneNode();
    assertTrue(nodeClone.getBooleanProp(Node.IS_CONSTANT_NAME));
  }

// com.google.javascript.rhino.NodeTest::testSharedProps1
  public void testSharedProps1() {
    Node n = getVarRef("A");
    n.putIntProp(Node.SIDE_EFFECT_FLAGS, 5);
    Node m = new Node(Token.TRUE);
    m.clonePropsFrom(n);
    assertEquals(m.getPropListHeadForTesting(), n.getPropListHeadForTesting());
    assertEquals(5, n.getIntProp(Node.SIDE_EFFECT_FLAGS));
    assertEquals(5, m.getIntProp(Node.SIDE_EFFECT_FLAGS));
  }

// com.google.javascript.rhino.NodeTest::testSharedProps2
  public void testSharedProps2() {
    Node n = getVarRef("A");
    n.putIntProp(Node.SIDE_EFFECT_FLAGS, 5);
    Node m = new Node(Token.TRUE);
    m.clonePropsFrom(n);

    n.putIntProp(Node.SIDE_EFFECT_FLAGS, 6);
    assertEquals(6, n.getIntProp(Node.SIDE_EFFECT_FLAGS));
    assertEquals(5, m.getIntProp(Node.SIDE_EFFECT_FLAGS));
    assertFalse(
        m.getPropListHeadForTesting() == n.getPropListHeadForTesting());

    m.putIntProp(Node.SIDE_EFFECT_FLAGS, 7);
    assertEquals(6, n.getIntProp(Node.SIDE_EFFECT_FLAGS));
    assertEquals(7, m.getIntProp(Node.SIDE_EFFECT_FLAGS));
  }

// com.google.javascript.rhino.NodeTest::testSharedProps3
  public void testSharedProps3() {
    Node n = getVarRef("A");
    n.putIntProp(Node.SIDE_EFFECT_FLAGS, 2);
    n.putIntProp(Node.INCRDECR_PROP, 3);
    Node m = new Node(Token.TRUE);
    m.clonePropsFrom(n);

    n.putIntProp(Node.SIDE_EFFECT_FLAGS, 4);
    assertEquals(4, n.getIntProp(Node.SIDE_EFFECT_FLAGS));
    assertEquals(2, m.getIntProp(Node.SIDE_EFFECT_FLAGS));
  }

// com.google.javascript.rhino.NodeTest::testBooleanProp
  public void testBooleanProp() {
    Node n = getVarRef("a");

    n.putBooleanProp(Node.IS_CONSTANT_NAME, false);

    assertNull(n.lookupProperty(Node.IS_CONSTANT_NAME));
    assertFalse(n.getBooleanProp(Node.IS_CONSTANT_NAME));

    n.putBooleanProp(Node.IS_CONSTANT_NAME, true);

    assertNotNull(n.lookupProperty(Node.IS_CONSTANT_NAME));
    assertTrue(n.getBooleanProp(Node.IS_CONSTANT_NAME));

    n.putBooleanProp(Node.IS_CONSTANT_NAME, false);

    assertNull(n.lookupProperty(Node.IS_CONSTANT_NAME));
    assertFalse(n.getBooleanProp(Node.IS_CONSTANT_NAME));
  }

// com.google.javascript.rhino.NodeTest::testCloneAnnontations2
  public void testCloneAnnontations2() {
    Node n = getVarRef("a");
    n.putBooleanProp(Node.IS_CONSTANT_NAME, true);
    n.putBooleanProp(Node.IS_DISPATCHER, true);
    assertTrue(n.getBooleanProp(Node.IS_CONSTANT_NAME));
    assertTrue(n.getBooleanProp(Node.IS_DISPATCHER));

    Node nodeClone = n.cloneNode();
    assertTrue(nodeClone.getBooleanProp(Node.IS_CONSTANT_NAME));
    assertTrue(nodeClone.getBooleanProp(Node.IS_DISPATCHER));

    n.putBooleanProp(Node.IS_DISPATCHER, false);
    assertTrue(n.getBooleanProp(Node.IS_CONSTANT_NAME));
    assertFalse(n.getBooleanProp(Node.IS_DISPATCHER));

    assertTrue(nodeClone.getBooleanProp(Node.IS_CONSTANT_NAME));
    assertTrue(nodeClone.getBooleanProp(Node.IS_DISPATCHER));
  }

// com.google.javascript.rhino.NodeTest::testGetIndexOfChild
  public void testGetIndexOfChild() {
    Node assign = getAssignExpr("b","c");
    assertEquals(2, assign.getChildCount());

    Node firstChild = assign.getFirstChild();
    Node secondChild = firstChild.getNext();
    assertNotNull(secondChild);

    assertEquals(0, assign.getIndexOfChild(firstChild));
    assertEquals(1, assign.getIndexOfChild(secondChild));
    assertEquals(-1, assign.getIndexOfChild(assign));
  }

// com.google.javascript.rhino.NodeTest::testCopyInformationFrom
  public void testCopyInformationFrom() {
    Node assign = getAssignExpr("b","c");
    assign.setSourceEncodedPosition(99);
    assign.setSourceFileForTesting("foo.js");

    Node lhs = assign.getFirstChild();
    lhs.copyInformationFrom(assign);
    assertEquals(99, lhs.getSourcePosition());
    assertEquals("foo.js", lhs.getSourceFileName());

    assign.setSourceEncodedPosition(101);
    assign.setSourceFileForTesting("bar.js");
    lhs.copyInformationFrom(assign);
    assertEquals(99, lhs.getSourcePosition());
    assertEquals("foo.js", lhs.getSourceFileName());
  }

// com.google.javascript.rhino.NodeTest::testUseSourceInfoIfMissingFrom
  public void testUseSourceInfoIfMissingFrom() {
    Node assign = getAssignExpr("b","c");
    assign.setSourceEncodedPosition(99);
    assign.setSourceFileForTesting("foo.js");

    Node lhs = assign.getFirstChild();
    lhs.useSourceInfoIfMissingFrom(assign);
    assertEquals(99, lhs.getSourcePosition());
    assertEquals("foo.js", lhs.getSourceFileName());

    assign.setSourceEncodedPosition(101);
    assign.setSourceFileForTesting("bar.js");
    lhs.useSourceInfoIfMissingFrom(assign);
    assertEquals(99, lhs.getSourcePosition());
    assertEquals("foo.js", lhs.getSourceFileName());
  }

// com.google.javascript.rhino.NodeTest::testUseSourceInfoFrom
  public void testUseSourceInfoFrom() {
    Node assign = getAssignExpr("b","c");
    assign.setSourceEncodedPosition(99);
    assign.setSourceFileForTesting("foo.js");

    Node lhs = assign.getFirstChild();
    lhs.useSourceInfoFrom(assign);
    assertEquals(99, lhs.getSourcePosition());
    assertEquals("foo.js", lhs.getSourceFileName());

    assign.setSourceEncodedPosition(101);
    assign.setSourceFileForTesting("bar.js");
    lhs.useSourceInfoFrom(assign);
    assertEquals(101, lhs.getSourcePosition());
    assertEquals("bar.js", lhs.getSourceFileName());
  }

// com.google.javascript.rhino.jstype.EnumElementTypeTest::testSubtypeRelation
  public void testSubtypeRelation() throws Exception {
    EnumElementType typeA = registry.createEnumType(
        "typeA", null, NUMBER_TYPE).getElementsType();
    EnumElementType typeB = registry.createEnumType(
        "typeB", null, NUMBER_TYPE).getElementsType();

    assertFalse(typeA.isSubtype(typeB));
    assertFalse(typeB.isSubtype(typeA));

    assertFalse(NUMBER_TYPE.isSubtype(typeB));
    assertFalse(NUMBER_TYPE.isSubtype(typeA));

    assertTrue(typeA.isSubtype(NUMBER_TYPE));
    assertTrue(typeB.isSubtype(NUMBER_TYPE));
  }

// com.google.javascript.rhino.jstype.EnumElementTypeTest::testMeet
  public void testMeet() throws Exception {
    EnumElementType typeA = registry.createEnumType(
        "typeA", null, createUnionType(NUMBER_TYPE, STRING_TYPE))
        .getElementsType();

    JSType stringsOfA = typeA.getGreatestSubtype(STRING_TYPE);
    assertFalse(stringsOfA.isEmptyType());
    assertEquals("typeA.<string>", stringsOfA.toString());
    assertTrue(stringsOfA.isSubtype(typeA));

    JSType numbersOfA = NUMBER_TYPE.getGreatestSubtype(typeA);
    assertFalse(numbersOfA.isEmptyType());
    assertEquals("typeA.<number>", numbersOfA.toString());
    assertTrue(numbersOfA.isSubtype(typeA));
  }

// com.google.javascript.rhino.jstype.FunctionParamBuilderTest::testBuild
  public void testBuild() throws Exception {
    FunctionParamBuilder builder = new FunctionParamBuilder(registry);
    assertTrue(builder.addRequiredParams(NUMBER_TYPE));
    assertTrue(builder.addOptionalParams(BOOLEAN_TYPE));
    assertTrue(builder.addVarArgs(STRING_TYPE));

    Node params = builder.build();
    assertTypeEquals(NUMBER_TYPE, params.getFirstChild().getJSType());
    assertTypeEquals(registry.createOptionalType(BOOLEAN_TYPE),
        params.getFirstChild().getNext().getJSType());
    assertTypeEquals(registry.createOptionalType(STRING_TYPE),
        params.getLastChild().getJSType());

    assertTrue(params.getFirstChild().getNext().isOptionalArg());
    assertTrue(params.getLastChild().isVarArgs());
  }

// com.google.javascript.rhino.jstype.FunctionTypeTest::testDefaultReturnType
  public void testDefaultReturnType() {
    FunctionType f = new FunctionBuilder(registry).build();
    assertEquals(UNKNOWN_TYPE, f.getReturnType());
  }

// com.google.javascript.rhino.jstype.FunctionTypeTest::testSupAndInfOfReturnTypes
  public void testSupAndInfOfReturnTypes() {
    FunctionType retString = new FunctionBuilder(registry)
        .withParamsNode(registry.createParameters())
        .withInferredReturnType(STRING_TYPE).build();
    FunctionType retNumber = new FunctionBuilder(registry)
        .withParamsNode(registry.createParameters())
        .withReturnType(NUMBER_TYPE).build();

    assertLeastSupertype(
        "function (): (number|string)", retString, retNumber);
    assertGreatestSubtype(
        "function (): None", retString, retNumber);

    assertTrue(retString.isReturnTypeInferred());
    assertFalse(retNumber.isReturnTypeInferred());
    assertTrue(
        ((FunctionType) retString.getLeastSupertype(retNumber))
        .isReturnTypeInferred());
    assertTrue(
        ((FunctionType) retString.getGreatestSubtype(retString))
        .isReturnTypeInferred());
  }

// com.google.javascript.rhino.jstype.FunctionTypeTest::testSupAndInfOfReturnTypesWithDifferentParams
  public void testSupAndInfOfReturnTypesWithDifferentParams() {
    FunctionType retString = new FunctionBuilder(registry)
        .withParamsNode(registry.createParameters(NUMBER_TYPE))
        .withInferredReturnType(STRING_TYPE).build();
    FunctionType retNumber = new FunctionBuilder(registry)
        .withParamsNode(registry.createParameters())
        .withReturnType(NUMBER_TYPE).build();

    assertLeastSupertype(
        "Function", retString, retNumber);
    assertGreatestSubtype(
        "function (...[*]): None", retString, retNumber);
  }

// com.google.javascript.rhino.jstype.FunctionTypeTest::testSupAndInfWithDifferentParams
  public void testSupAndInfWithDifferentParams() {
    FunctionType retString = new FunctionBuilder(registry)
        .withParamsNode(registry.createParameters(NUMBER_TYPE))
        .withReturnType(STRING_TYPE).build();
    FunctionType retNumber = new FunctionBuilder(registry)
        .withParamsNode(registry.createParameters(STRING_TYPE))
        .withReturnType(NUMBER_TYPE).build();

    assertLeastSupertype(
        "Function", retString, retNumber);
    assertGreatestSubtype(
        "function (...[*]): None", retString, retNumber);
  }

// com.google.javascript.rhino.jstype.FunctionTypeTest::testSupAndInfWithDifferentThisTypes
  public void testSupAndInfWithDifferentThisTypes() {
    FunctionType retString = new FunctionBuilder(registry)
        .withParamsNode(registry.createParameters())
        .withTypeOfThis(OBJECT_TYPE)
        .withReturnType(STRING_TYPE).build();
    FunctionType retNumber = new FunctionBuilder(registry)
        .withParamsNode(registry.createParameters())
        .withTypeOfThis(DATE_TYPE)
        .withReturnType(NUMBER_TYPE).build();

    assertLeastSupertype(
        "function (this:Object): (number|string)", retString, retNumber);
    assertGreatestSubtype(
        "function (this:Date): None", retString, retNumber);
  }

// com.google.javascript.rhino.jstype.FunctionTypeTest::testSupAndInfWithDifferentThisTypes2
  public void testSupAndInfWithDifferentThisTypes2() {
    FunctionType retString = new FunctionBuilder(registry)
        .withParamsNode(registry.createParameters())
        .withTypeOfThis(ARRAY_TYPE)
        .withReturnType(STRING_TYPE).build();
    FunctionType retNumber = new FunctionBuilder(registry)
        .withParamsNode(registry.createParameters())
        .withTypeOfThis(DATE_TYPE)
        .withReturnType(NUMBER_TYPE).build();

    assertLeastSupertype(
        "function (this:Object): (number|string)", retString, retNumber);
    assertGreatestSubtype(
        "function (this:NoObject): None", retString, retNumber);
  }

// com.google.javascript.rhino.jstype.FunctionTypeTest::testSupAndInfOfReturnTypesWithNumOfParams
  public void testSupAndInfOfReturnTypesWithNumOfParams() {
    FunctionType twoNumbers = new FunctionBuilder(registry)
        .withParamsNode(registry.createParameters(NUMBER_TYPE, NUMBER_TYPE))
        .withReturnType(BOOLEAN_TYPE).build();
    FunctionType oneNumber = new FunctionBuilder(registry)
        .withParamsNode(registry.createParameters(NUMBER_TYPE))
        .withReturnType(BOOLEAN_TYPE).build();

    assertLeastSupertype(
        "Function", twoNumbers, oneNumber);
    assertGreatestSubtype(
        "function (...[*]): None", twoNumbers, oneNumber);
  }

// com.google.javascript.rhino.jstype.FunctionTypeTest::testSubtypeWithInterfaceThisType
  public void testSubtypeWithInterfaceThisType() {
    FunctionType iface = registry.createInterfaceType("I", null);
    FunctionType ifaceReturnBoolean = new FunctionBuilder(registry)
        .withParamsNode(registry.createParameters())
        .withTypeOfThis(iface.getInstanceType())
        .withReturnType(BOOLEAN_TYPE).build();
    FunctionType objReturnBoolean = new FunctionBuilder(registry)
        .withParamsNode(registry.createParameters())
        .withTypeOfThis(OBJECT_TYPE)
        .withReturnType(BOOLEAN_TYPE).build();
    assertTrue(objReturnBoolean.canAssignTo(ifaceReturnBoolean));
  }

// com.google.javascript.rhino.jstype.FunctionTypeTest::testOrdinaryFunctionPrototype
  public void testOrdinaryFunctionPrototype() {
    FunctionType oneNumber = new FunctionBuilder(registry)
        .withParamsNode(registry.createParameters(NUMBER_TYPE))
        .withReturnType(BOOLEAN_TYPE).build();
    assertEquals(ImmutableSet.<String>of(), oneNumber.getOwnPropertyNames());
  }

// com.google.javascript.rhino.jstype.FunctionTypeTest::testCtorWithPrototypeSet
  public void testCtorWithPrototypeSet() {
    FunctionType ctor = registry.createConstructorType(
        "Foo", null, null, null);
    assertFalse(ctor.getInstanceType().isUnknownType());

    Node node = new Node(Token.OBJECTLIT);
    ctor.defineDeclaredProperty("prototype", UNKNOWN_TYPE, node);
    assertTrue(ctor.getInstanceType().isUnknownType());

    assertEquals(ImmutableSet.<String>of("prototype"),
        ctor.getOwnPropertyNames());
    assertTrue(ctor.isPropertyTypeInferred("prototype"));
    assertTrue(ctor.getPropertyType("prototype").isUnknownType());

    
    assertNull(ctor.getPropertyNode("prototype"));
  }

// com.google.javascript.rhino.jstype.FunctionTypeTest::testEmptyFunctionTypes
  public void testEmptyFunctionTypes() {
    assertTrue(LEAST_FUNCTION_TYPE.isEmptyType());
    assertFalse(GREATEST_FUNCTION_TYPE.isEmptyType());
  }

// com.google.javascript.rhino.jstype.FunctionTypeTest::testInterfacePrototypeChain1
  public void testInterfacePrototypeChain1() {
    FunctionType iface = registry.createInterfaceType("I", null);
    assertTypeEquals(
        iface.getPrototype(),
        iface.getInstanceType().getImplicitPrototype());
    assertTypeEquals(
        OBJECT_TYPE,
        iface.getPrototype().getImplicitPrototype());
  }

// com.google.javascript.rhino.jstype.FunctionTypeTest::testInterfacePrototypeChain2
  public void testInterfacePrototypeChain2() {
    FunctionType iface = registry.createInterfaceType("I", null);
    iface.getPrototype().defineDeclaredProperty(
        "numberProp", NUMBER_TYPE, null);

    FunctionType subIface = registry.createInterfaceType("SubI", null);
    subIface.setExtendedInterfaces(
        Lists.<ObjectType>newArrayList(iface.getInstanceType()));
    assertTypeEquals(
        subIface.getPrototype(),
        subIface.getInstanceType().getImplicitPrototype());
    assertTypeEquals(
        OBJECT_TYPE,
        subIface.getPrototype().getImplicitPrototype());

    ObjectType subIfaceInst = subIface.getInstanceType();
    assertTrue(subIfaceInst.hasProperty("numberProp"));
    assertTrue(subIfaceInst.isPropertyTypeDeclared("numberProp"));
    assertFalse(subIfaceInst.isPropertyTypeInferred("numberProp"));
  }

// com.google.javascript.rhino.jstype.FunctionTypeTest::testIsEquivalentTo
  public void testIsEquivalentTo() {
    FunctionType type = new FunctionBuilder(registry).build();
    assertFalse(type.isEquivalentTo(null));
    assertTrue(type.isEquivalentTo(type));
  }

// com.google.javascript.rhino.jstype.FunctionTypeTest::testRecursiveFunction
  public void testRecursiveFunction() {
    ProxyObjectType loop = new ProxyObjectType(registry, NUMBER_TYPE);
    FunctionType fn = new FunctionBuilder(registry)
        .withParamsNode(registry.createParameters(loop))
        .withReturnType(loop).build();

    loop.setReferencedType(fn);
    assertEquals("function (Function): Function", fn.toString());

    Asserts.assertEquivalenceOperations(fn, loop);
  }

// com.google.javascript.rhino.jstype.FunctionTypeTest::testBindSignature
  public void testBindSignature() {
    FunctionType fn = new FunctionBuilder(registry)
        .withTypeOfThis(DATE_TYPE)
        .withParamsNode(registry.createParameters(STRING_TYPE, NUMBER_TYPE))
        .withReturnType(BOOLEAN_TYPE).build();

    assertEquals(
        "function ((Date|null|undefined), string=, number=):" +
        " function (...[?]): boolean",
        fn.getPropertyType("bind").toString());
  }

// com.google.javascript.rhino.jstype.JSTypeRegistryTest::testGetBuiltInType
  public void testGetBuiltInType() {
    JSTypeRegistry typeRegistry = new JSTypeRegistry(null);
    assertTypeEquals(typeRegistry.getNativeType(JSTypeNative.BOOLEAN_TYPE),
        typeRegistry.getType("boolean"));
  }

// com.google.javascript.rhino.jstype.JSTypeRegistryTest::testGetDeclaredType
  public void testGetDeclaredType() {
    JSTypeRegistry typeRegistry = new JSTypeRegistry(null);
    JSType type = typeRegistry.createAnonymousObjectType();
    String name = "Foo";
    typeRegistry.declareType(name, type);
    assertTypeEquals(type, typeRegistry.getType(name));

    
    JSTypeRegistry typeRegistry2 = new JSTypeRegistry(null);
    assertEquals(null, typeRegistry2.getType(name));
    assertTypeEquals(type, typeRegistry.getType(name));
  }

// com.google.javascript.rhino.jstype.JSTypeRegistryTest::testGetDeclaredTypeInNamespace
  public void testGetDeclaredTypeInNamespace() {
    JSTypeRegistry typeRegistry = new JSTypeRegistry(null);
    JSType type = typeRegistry.createAnonymousObjectType();
    String name = "a.b.Foo";
    typeRegistry.declareType(name, type);
    assertTypeEquals(type, typeRegistry.getType(name));
    assertTrue(typeRegistry.hasNamespace("a"));
    assertTrue(typeRegistry.hasNamespace("a.b"));
  }

// com.google.javascript.rhino.jstype.JSTypeRegistryTest::testPropertyOnManyTypes
  public void testPropertyOnManyTypes() {
    JSTypeRegistry typeRegistry = new JSTypeRegistry(null);

    JSType type = null;

    
    
    
    for (int i = 0; i < 100; i++) {
      type = typeRegistry.createObjectType("type: " + i, null, null);
      typeRegistry.registerPropertyOnType("foo", type);
    }

    assertFalse(typeRegistry.getGreatestSubtypeWithProperty(type, "foo").isUnknownType());
  }

// com.google.javascript.rhino.jstype.JSTypeRegistryTest::testTypeAsNamespace
  public void testTypeAsNamespace() {
    JSTypeRegistry typeRegistry = new JSTypeRegistry(null);

    JSType type = typeRegistry.createAnonymousObjectType();
    String name = "a.b.Foo";
    typeRegistry.declareType(name, type);
    assertTypeEquals(type, typeRegistry.getType(name));

    type = typeRegistry.createAnonymousObjectType();
    name = "a.b.Foo.Bar";
    typeRegistry.declareType(name, type);
    assertTypeEquals(type, typeRegistry.getType(name));

    assertTrue(typeRegistry.hasNamespace("a"));
    assertTrue(typeRegistry.hasNamespace("a.b"));
    assertTrue(typeRegistry.hasNamespace("a.b.Foo"));
  }

// com.google.javascript.rhino.jstype.JSTypeRegistryTest::testGenerationIncrementing1
  public void testGenerationIncrementing1() {
    SimpleErrorReporter reporter = new SimpleErrorReporter();
    final JSTypeRegistry typeRegistry = new JSTypeRegistry(reporter);

    StaticScope<JSType> scope = new AbstractStaticScope<JSType>() {
          @Override
          public StaticSlot<JSType> getSlot(final String name) {
            return new SimpleSlot(
                name,
                typeRegistry.getNativeType(JSTypeNative.UNKNOWN_TYPE),
                false);
          }
        };

    ObjectType namedType =
        (ObjectType) typeRegistry.getType(scope, "Foo", null, 0, 0);
    ObjectType subNamed =
        typeRegistry.createObjectType(typeRegistry.createObjectType(namedType));

    
    typeRegistry.setLastGeneration(false);
    typeRegistry.resolveTypesInScope(scope);
    assertTrue(subNamed.isUnknownType());

    
    
    typeRegistry.declareType("Foo", typeRegistry.createAnonymousObjectType());
    typeRegistry.resolveTypesInScope(scope);
    assertTrue(subNamed.isUnknownType());

    assertNull("Unexpected errors: " + reporter.errors(),
        reporter.errors());
    assertNull("Unexpected warnings: " + reporter.warnings(),
        reporter.warnings());

    
    typeRegistry.incrementGeneration();
    typeRegistry.setLastGeneration(true);
    typeRegistry.resolveTypesInScope(scope);
    assertFalse(subNamed.isUnknownType());
  }

// com.google.javascript.rhino.jstype.JSTypeRegistryTest::testGenerationIncrementing2
  public void testGenerationIncrementing2() {
    SimpleErrorReporter reporter = new SimpleErrorReporter();
    final JSTypeRegistry typeRegistry = new JSTypeRegistry(reporter);

    StaticScope<JSType> scope = new AbstractStaticScope<JSType>() {
          @Override
          public StaticSlot<JSType> getSlot(final String name) {
            return new SimpleSlot(
                name,
                typeRegistry.getNativeType(JSTypeNative.UNKNOWN_TYPE),
                false);
          }
        };

    ObjectType namedType =
        (ObjectType) typeRegistry.getType(scope, "Foo", null, 0, 0);
    FunctionType functionType = typeRegistry.createFunctionType(namedType);

    
    typeRegistry.setLastGeneration(false);
    typeRegistry.resolveTypesInScope(scope);
    assertTrue(functionType.getReturnType().isUnknownType());
    functionType.resolve(reporter, scope);
    assertTrue(functionType.getReturnType().isUnknownType());

    
    
    typeRegistry.declareType("Foo", typeRegistry.createAnonymousObjectType());
    typeRegistry.resolveTypesInScope(scope);
    assertTrue(functionType.getReturnType().isUnknownType());

    assertNull("Unexpected errors: " + reporter.errors(),
        reporter.errors());
    assertNull("Unexpected warnings: " + reporter.warnings(),
        reporter.warnings());

    
    typeRegistry.incrementGeneration();
    typeRegistry.setLastGeneration(true);
    typeRegistry.resolveTypesInScope(scope);
    assertFalse(functionType.getReturnType().isUnknownType());
  }

// com.google.javascript.rhino.jstype.JSTypeRegistryTest::testTypeResolutionModes
  public void testTypeResolutionModes() {
    SimpleErrorReporter reporter = new SimpleErrorReporter();

    JSTypeRegistry lazyExprRegistry = new JSTypeRegistry(reporter);
    lazyExprRegistry.setResolveMode(ResolveMode.LAZY_EXPRESSIONS);

    JSTypeRegistry lazyNameRegistry = new JSTypeRegistry(reporter);
    lazyNameRegistry.setResolveMode(ResolveMode.LAZY_NAMES);

    JSTypeRegistry immediateRegistry = new JSTypeRegistry(reporter);
    immediateRegistry.setResolveMode(ResolveMode.IMMEDIATE);

    Node expr = new Node(Token.QMARK, Node.newString("foo"));
    StaticScope<JSType> empty = MapBasedScope.emptyScope();

    JSType type = lazyExprRegistry.createFromTypeNodes(
        expr, "source.js", empty);
    assertTrue(type instanceof UnresolvedTypeExpression);
    assertTrue(type.isUnknownType());
    assertEquals("?", type.toString());
    assertNull("Unexpected warnings: " + reporter.warnings(),
        reporter.warnings());

    type = lazyNameRegistry.createFromTypeNodes(
        expr, "source.js", empty);
    assertTrue(type instanceof UnionType);
    assertTrue(type.isUnknownType());
    assertEquals("(foo|null)", type.toString());
    assertNull("Unexpected warnings: " + reporter.warnings(),
        reporter.warnings());

    type = immediateRegistry.createFromTypeNodes(
        expr, "source.js", empty);
    assertTrue(type instanceof UnknownType);
    assertEquals("Expected warnings", 1, reporter.warnings().size());
  }

// com.google.javascript.rhino.jstype.JSTypeRegistryTest::testForceResolve
  public void testForceResolve() {
    SimpleErrorReporter reporter = new SimpleErrorReporter();

    JSTypeRegistry lazyExprRegistry = new JSTypeRegistry(reporter);
    lazyExprRegistry.setResolveMode(ResolveMode.LAZY_EXPRESSIONS);

    Node expr = new Node(Token.QMARK, Node.newString("foo"));
    StaticScope<JSType> empty = MapBasedScope.emptyScope();

    JSType type = lazyExprRegistry.createFromTypeNodes(
        expr, "source.js", empty);
    assertFalse(type.isResolved());
    assertTrue(type.forceResolve(reporter, empty).isResolved());
    assertEquals("Expected warnings", 1, reporter.warnings().size());
  }

// com.google.javascript.rhino.jstype.JSTypeRegistryTest::testAllTypeResolvesImmediately
  public void testAllTypeResolvesImmediately() {
    JSTypeRegistry lazyExprRegistry = new JSTypeRegistry(
        new SimpleErrorReporter());
    lazyExprRegistry.setResolveMode(ResolveMode.LAZY_EXPRESSIONS);

    Node expr = new Node(Token.STAR);
    JSType type = lazyExprRegistry.createFromTypeNodes(
        expr, "source.js", MapBasedScope.emptyScope());
    assertTrue(type instanceof AllType);
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testUniversalConstructorType
  public void testUniversalConstructorType() throws Exception {
    
    assertFalse(U2U_CONSTRUCTOR_TYPE.isNoObjectType());
    assertFalse(U2U_CONSTRUCTOR_TYPE.isNoType());
    assertFalse(U2U_CONSTRUCTOR_TYPE.isArrayType());
    assertFalse(U2U_CONSTRUCTOR_TYPE.isBooleanValueType());
    assertFalse(U2U_CONSTRUCTOR_TYPE.isDateType());
    assertFalse(U2U_CONSTRUCTOR_TYPE.isEnumElementType());
    assertFalse(U2U_CONSTRUCTOR_TYPE.isNullType());
    assertFalse(U2U_CONSTRUCTOR_TYPE.isNamedType());
    assertFalse(U2U_CONSTRUCTOR_TYPE.isNullType());
    assertFalse(U2U_CONSTRUCTOR_TYPE.isNumber());
    assertFalse(U2U_CONSTRUCTOR_TYPE.isNumberObjectType());
    assertFalse(U2U_CONSTRUCTOR_TYPE.isNumberValueType());
    assertTrue(U2U_CONSTRUCTOR_TYPE.isObject());
    assertFalse(U2U_CONSTRUCTOR_TYPE.isFunctionPrototypeType());
    assertFalse(U2U_CONSTRUCTOR_TYPE.isRegexpType());
    assertFalse(U2U_CONSTRUCTOR_TYPE.isString());
    assertFalse(U2U_CONSTRUCTOR_TYPE.isStringObjectType());
    assertFalse(U2U_CONSTRUCTOR_TYPE.isStringValueType());
    assertFalse(U2U_CONSTRUCTOR_TYPE.isEnumType());
    assertFalse(U2U_CONSTRUCTOR_TYPE.isUnionType());
    assertFalse(U2U_CONSTRUCTOR_TYPE.isAllType());
    assertFalse(U2U_CONSTRUCTOR_TYPE.isVoidType());
    assertTrue(U2U_CONSTRUCTOR_TYPE.isConstructor());
    assertTrue(U2U_CONSTRUCTOR_TYPE.isInstanceType());

    
    assertFalse(U2U_CONSTRUCTOR_TYPE.canAssignTo(NO_TYPE));
    assertFalse(U2U_CONSTRUCTOR_TYPE.canAssignTo(NO_OBJECT_TYPE));
    assertFalse(U2U_CONSTRUCTOR_TYPE.isSubtype(ARRAY_TYPE));
    assertFalse(U2U_CONSTRUCTOR_TYPE.canAssignTo(BOOLEAN_TYPE));
    assertFalse(U2U_CONSTRUCTOR_TYPE.canAssignTo(BOOLEAN_OBJECT_TYPE));
    assertFalse(U2U_CONSTRUCTOR_TYPE.canAssignTo(DATE_TYPE));
    assertFalse(U2U_CONSTRUCTOR_TYPE.canAssignTo(ERROR_TYPE));
    assertFalse(U2U_CONSTRUCTOR_TYPE.canAssignTo(EVAL_ERROR_TYPE));
    assertTrue(U2U_CONSTRUCTOR_TYPE.canAssignTo(functionType));
    assertFalse(U2U_CONSTRUCTOR_TYPE.canAssignTo(recordType));
    assertFalse(U2U_CONSTRUCTOR_TYPE.canAssignTo(NULL_TYPE));
    assertFalse(U2U_CONSTRUCTOR_TYPE.canAssignTo(NUMBER_TYPE));
    assertFalse(U2U_CONSTRUCTOR_TYPE.canAssignTo(NUMBER_OBJECT_TYPE));
    assertTrue(U2U_CONSTRUCTOR_TYPE.canAssignTo(OBJECT_TYPE));
    assertFalse(U2U_CONSTRUCTOR_TYPE.canAssignTo(URI_ERROR_TYPE));
    assertFalse(U2U_CONSTRUCTOR_TYPE.canAssignTo(RANGE_ERROR_TYPE));
    assertFalse(U2U_CONSTRUCTOR_TYPE.canAssignTo(REFERENCE_ERROR_TYPE));
    assertFalse(U2U_CONSTRUCTOR_TYPE.canAssignTo(REGEXP_TYPE));
    assertFalse(U2U_CONSTRUCTOR_TYPE.canAssignTo(STRING_TYPE));
    assertFalse(U2U_CONSTRUCTOR_TYPE.canAssignTo(STRING_OBJECT_TYPE));
    assertFalse(U2U_CONSTRUCTOR_TYPE.canAssignTo(SYNTAX_ERROR_TYPE));
    assertFalse(U2U_CONSTRUCTOR_TYPE.canAssignTo(TYPE_ERROR_TYPE));
    assertTrue(U2U_CONSTRUCTOR_TYPE.canAssignTo(ALL_TYPE));
    assertFalse(U2U_CONSTRUCTOR_TYPE.canAssignTo(VOID_TYPE));

    
    assertTrue(U2U_CONSTRUCTOR_TYPE.
        canTestForEqualityWith(NO_TYPE));
    assertTrue(U2U_CONSTRUCTOR_TYPE.
        canTestForEqualityWith(NO_OBJECT_TYPE));
    assertTrue(U2U_CONSTRUCTOR_TYPE.
        canTestForEqualityWith(ALL_TYPE));
    assertTrue(U2U_CONSTRUCTOR_TYPE.
        canTestForEqualityWith(ARRAY_TYPE));
    assertFalse(U2U_CONSTRUCTOR_TYPE.
        canTestForEqualityWith(BOOLEAN_TYPE));
    assertTrue(U2U_CONSTRUCTOR_TYPE.
        canTestForEqualityWith(BOOLEAN_OBJECT_TYPE));
    assertTrue(U2U_CONSTRUCTOR_TYPE.
        canTestForEqualityWith(DATE_TYPE));
    assertTrue(U2U_CONSTRUCTOR_TYPE.
        canTestForEqualityWith(ERROR_TYPE));
    assertTrue(U2U_CONSTRUCTOR_TYPE.
        canTestForEqualityWith(EVAL_ERROR_TYPE));
    assertTrue(U2U_CONSTRUCTOR_TYPE.
        canTestForEqualityWith(functionType));
    assertTrue(U2U_CONSTRUCTOR_TYPE.
        canTestForEqualityWith(recordType));
    assertFalse(U2U_CONSTRUCTOR_TYPE.
        canTestForEqualityWith(NULL_TYPE));
    assertFalse(U2U_CONSTRUCTOR_TYPE.
        canTestForEqualityWith(NUMBER_TYPE));
    assertTrue(U2U_CONSTRUCTOR_TYPE.
        canTestForEqualityWith(NUMBER_OBJECT_TYPE));
    assertTrue(U2U_CONSTRUCTOR_TYPE.
        canTestForEqualityWith(OBJECT_TYPE));
    assertTrue(U2U_CONSTRUCTOR_TYPE.
        canTestForEqualityWith(URI_ERROR_TYPE));
    assertTrue(U2U_CONSTRUCTOR_TYPE.
        canTestForEqualityWith(RANGE_ERROR_TYPE));
    assertTrue(U2U_CONSTRUCTOR_TYPE.
        canTestForEqualityWith(REFERENCE_ERROR_TYPE));
    assertTrue(U2U_CONSTRUCTOR_TYPE.
        canTestForEqualityWith(REGEXP_TYPE));
    assertFalse(U2U_CONSTRUCTOR_TYPE.
        canTestForEqualityWith(STRING_TYPE));
    assertTrue(U2U_CONSTRUCTOR_TYPE.
        canTestForEqualityWith(STRING_OBJECT_TYPE));
    assertTrue(U2U_CONSTRUCTOR_TYPE.
        canTestForEqualityWith(SYNTAX_ERROR_TYPE));
    assertTrue(U2U_CONSTRUCTOR_TYPE.
        canTestForEqualityWith(TYPE_ERROR_TYPE));
    assertFalse(U2U_CONSTRUCTOR_TYPE.
        canTestForEqualityWith(VOID_TYPE));

    
    assertTrue(U2U_CONSTRUCTOR_TYPE.
        canTestForShallowEqualityWith(NO_TYPE));
    assertTrue(U2U_CONSTRUCTOR_TYPE.
        canTestForShallowEqualityWith(NO_OBJECT_TYPE));
    assertFalse(U2U_CONSTRUCTOR_TYPE.
        canTestForShallowEqualityWith(ARRAY_TYPE));
    assertFalse(U2U_CONSTRUCTOR_TYPE.
        canTestForShallowEqualityWith(BOOLEAN_TYPE));
    assertFalse(U2U_CONSTRUCTOR_TYPE.
        canTestForShallowEqualityWith(BOOLEAN_OBJECT_TYPE));
    assertFalse(U2U_CONSTRUCTOR_TYPE.
        canTestForShallowEqualityWith(DATE_TYPE));
    assertFalse(U2U_CONSTRUCTOR_TYPE.
        canTestForShallowEqualityWith(ERROR_TYPE));
    assertFalse(U2U_CONSTRUCTOR_TYPE.
        canTestForShallowEqualityWith(EVAL_ERROR_TYPE));
    assertTrue(U2U_CONSTRUCTOR_TYPE.
        canTestForShallowEqualityWith(functionType));
    assertFalse(U2U_CONSTRUCTOR_TYPE.
        canTestForShallowEqualityWith(recordType));
    assertFalse(U2U_CONSTRUCTOR_TYPE.
        canTestForShallowEqualityWith(NULL_TYPE));
    assertFalse(U2U_CONSTRUCTOR_TYPE.
        canTestForShallowEqualityWith(NUMBER_TYPE));
    assertFalse(U2U_CONSTRUCTOR_TYPE.
        canTestForShallowEqualityWith(NUMBER_OBJECT_TYPE));
    assertTrue(U2U_CONSTRUCTOR_TYPE.
        canTestForShallowEqualityWith(OBJECT_TYPE));
    assertFalse(U2U_CONSTRUCTOR_TYPE.
        canTestForShallowEqualityWith(URI_ERROR_TYPE));
    assertFalse(U2U_CONSTRUCTOR_TYPE.
        canTestForShallowEqualityWith(RANGE_ERROR_TYPE));
    assertFalse(U2U_CONSTRUCTOR_TYPE.
        canTestForShallowEqualityWith(REFERENCE_ERROR_TYPE));
    assertFalse(U2U_CONSTRUCTOR_TYPE.
        canTestForShallowEqualityWith(REGEXP_TYPE));
    assertFalse(U2U_CONSTRUCTOR_TYPE.
        canTestForShallowEqualityWith(STRING_TYPE));
    assertFalse(U2U_CONSTRUCTOR_TYPE.
        canTestForShallowEqualityWith(STRING_OBJECT_TYPE));
    assertFalse(U2U_CONSTRUCTOR_TYPE.
        canTestForShallowEqualityWith(SYNTAX_ERROR_TYPE));
    assertFalse(U2U_CONSTRUCTOR_TYPE.
        canTestForShallowEqualityWith(TYPE_ERROR_TYPE));
    assertTrue( U2U_CONSTRUCTOR_TYPE.
        canTestForShallowEqualityWith(ALL_TYPE));
    assertFalse(U2U_CONSTRUCTOR_TYPE.
        canTestForShallowEqualityWith(VOID_TYPE));

    
    assertFalse(U2U_CONSTRUCTOR_TYPE.isNullable());

    
    assertTrue(U2U_CONSTRUCTOR_TYPE.isObject());

    
    assertFalse(U2U_CONSTRUCTOR_TYPE.matchesInt32Context());
    assertFalse(U2U_CONSTRUCTOR_TYPE.matchesNumberContext());
    assertTrue(U2U_CONSTRUCTOR_TYPE.matchesObjectContext());
    assertFalse(U2U_CONSTRUCTOR_TYPE.matchesStringContext());
    assertFalse(U2U_CONSTRUCTOR_TYPE.matchesUint32Context());

    
    assertEquals("Function",
        U2U_CONSTRUCTOR_TYPE.toString());
    assertTrue(U2U_CONSTRUCTOR_TYPE.hasDisplayName());
    assertEquals("Function", U2U_CONSTRUCTOR_TYPE.getDisplayName());

    
    assertTypeEquals(UNKNOWN_TYPE,
        U2U_CONSTRUCTOR_TYPE.getPropertyType("anyProperty"));

    assertTrue(U2U_CONSTRUCTOR_TYPE.isNativeObjectType());

    Asserts.assertResolvesToSame(U2U_CONSTRUCTOR_TYPE);

    assertTrue(U2U_CONSTRUCTOR_TYPE.isNominalConstructor());
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testNoObjectType
  public void testNoObjectType() throws Exception {
    
    assertTrue(NO_OBJECT_TYPE.isNoObjectType());
    assertFalse(NO_OBJECT_TYPE.isNoType());
    assertFalse(NO_OBJECT_TYPE.isArrayType());
    assertFalse(NO_OBJECT_TYPE.isBooleanValueType());
    assertFalse(NO_OBJECT_TYPE.isDateType());
    assertFalse(NO_OBJECT_TYPE.isEnumElementType());
    assertFalse(NO_OBJECT_TYPE.isNullType());
    assertFalse(NO_OBJECT_TYPE.isNamedType());
    assertFalse(NO_OBJECT_TYPE.isNullType());
    assertTrue(NO_OBJECT_TYPE.isNumber());
    assertFalse(NO_OBJECT_TYPE.isNumberObjectType());
    assertFalse(NO_OBJECT_TYPE.isNumberValueType());
    assertTrue(NO_OBJECT_TYPE.isObject());
    assertFalse(NO_OBJECT_TYPE.isFunctionPrototypeType());
    assertFalse(NO_OBJECT_TYPE.isRegexpType());
    assertTrue(NO_OBJECT_TYPE.isString());
    assertFalse(NO_OBJECT_TYPE.isStringObjectType());
    assertFalse(NO_OBJECT_TYPE.isStringValueType());
    assertFalse(NO_OBJECT_TYPE.isEnumType());
    assertFalse(NO_OBJECT_TYPE.isUnionType());
    assertFalse(NO_OBJECT_TYPE.isAllType());
    assertFalse(NO_OBJECT_TYPE.isVoidType());
    assertTrue(NO_OBJECT_TYPE.isConstructor());
    assertFalse(NO_OBJECT_TYPE.isInstanceType());

    
    assertFalse(NO_OBJECT_TYPE.canAssignTo(NO_TYPE));
    assertTrue(NO_OBJECT_TYPE.canAssignTo(NO_OBJECT_TYPE));
    assertTrue(NO_OBJECT_TYPE.isSubtype(ARRAY_TYPE));
    assertFalse(NO_OBJECT_TYPE.canAssignTo(BOOLEAN_TYPE));
    assertTrue(NO_OBJECT_TYPE.canAssignTo(BOOLEAN_OBJECT_TYPE));
    assertTrue(NO_OBJECT_TYPE.canAssignTo(DATE_TYPE));
    assertTrue(NO_OBJECT_TYPE.canAssignTo(ERROR_TYPE));
    assertTrue(NO_OBJECT_TYPE.canAssignTo(EVAL_ERROR_TYPE));
    assertTrue(NO_OBJECT_TYPE.canAssignTo(functionType));
    assertTrue(NO_OBJECT_TYPE.canAssignTo(recordType));
    assertFalse(NO_OBJECT_TYPE.canAssignTo(NULL_TYPE));
    assertFalse(NO_OBJECT_TYPE.canAssignTo(NUMBER_TYPE));
    assertTrue(NO_OBJECT_TYPE.canAssignTo(NUMBER_OBJECT_TYPE));
    assertTrue(NO_OBJECT_TYPE.canAssignTo(OBJECT_TYPE));
    assertTrue(NO_OBJECT_TYPE.canAssignTo(URI_ERROR_TYPE));
    assertTrue(NO_OBJECT_TYPE.canAssignTo(RANGE_ERROR_TYPE));
    assertTrue(NO_OBJECT_TYPE.canAssignTo(REFERENCE_ERROR_TYPE));
    assertTrue(NO_OBJECT_TYPE.canAssignTo(REGEXP_TYPE));
    assertFalse(NO_OBJECT_TYPE.canAssignTo(STRING_TYPE));
    assertTrue(NO_OBJECT_TYPE.canAssignTo(STRING_OBJECT_TYPE));
    assertTrue(NO_OBJECT_TYPE.canAssignTo(SYNTAX_ERROR_TYPE));
    assertTrue(NO_OBJECT_TYPE.canAssignTo(TYPE_ERROR_TYPE));
    assertTrue(NO_OBJECT_TYPE.canAssignTo(ALL_TYPE));
    assertFalse(NO_OBJECT_TYPE.canAssignTo(VOID_TYPE));

    
    assertCannotTestForEqualityWith(NO_OBJECT_TYPE, NO_TYPE);
    assertCannotTestForEqualityWith(NO_OBJECT_TYPE, NO_OBJECT_TYPE);
    assertCanTestForEqualityWith(NO_OBJECT_TYPE, ALL_TYPE);
    assertCanTestForEqualityWith(NO_OBJECT_TYPE, ARRAY_TYPE);
    assertCanTestForEqualityWith(NO_OBJECT_TYPE, BOOLEAN_TYPE);
    assertCanTestForEqualityWith(NO_OBJECT_TYPE, BOOLEAN_OBJECT_TYPE);
    assertCanTestForEqualityWith(NO_OBJECT_TYPE, DATE_TYPE);
    assertCanTestForEqualityWith(NO_OBJECT_TYPE, ERROR_TYPE);
    assertCanTestForEqualityWith(NO_OBJECT_TYPE, EVAL_ERROR_TYPE);
    assertCanTestForEqualityWith(NO_OBJECT_TYPE, functionType);
    assertCanTestForEqualityWith(NO_OBJECT_TYPE, recordType);
    assertCanTestForEqualityWith(NO_OBJECT_TYPE, NULL_TYPE);
    assertCanTestForEqualityWith(NO_OBJECT_TYPE, NUMBER_TYPE);
    assertCanTestForEqualityWith(NO_OBJECT_TYPE, NUMBER_OBJECT_TYPE);
    assertCanTestForEqualityWith(NO_OBJECT_TYPE, OBJECT_TYPE);
    assertCanTestForEqualityWith(NO_OBJECT_TYPE, URI_ERROR_TYPE);
    assertCanTestForEqualityWith(NO_OBJECT_TYPE, RANGE_ERROR_TYPE);
    assertCanTestForEqualityWith(NO_OBJECT_TYPE, REFERENCE_ERROR_TYPE);
    assertCanTestForEqualityWith(NO_OBJECT_TYPE, REGEXP_TYPE);
    assertCanTestForEqualityWith(NO_OBJECT_TYPE, STRING_TYPE);
    assertCanTestForEqualityWith(NO_OBJECT_TYPE, STRING_OBJECT_TYPE);
    assertCanTestForEqualityWith(NO_OBJECT_TYPE, SYNTAX_ERROR_TYPE);
    assertCanTestForEqualityWith(NO_OBJECT_TYPE, TYPE_ERROR_TYPE);
    assertCanTestForEqualityWith(NO_OBJECT_TYPE, VOID_TYPE);

    
    assertTrue(NO_OBJECT_TYPE.canTestForShallowEqualityWith(NO_TYPE));
    assertTrue(NO_OBJECT_TYPE.canTestForShallowEqualityWith(NO_OBJECT_TYPE));
    assertTrue(NO_OBJECT_TYPE.canTestForShallowEqualityWith(ARRAY_TYPE));
    assertFalse(NO_OBJECT_TYPE.canTestForShallowEqualityWith(BOOLEAN_TYPE));
    assertTrue(NO_OBJECT_TYPE.
        canTestForShallowEqualityWith(BOOLEAN_OBJECT_TYPE));
    assertTrue(NO_OBJECT_TYPE.canTestForShallowEqualityWith(DATE_TYPE));
    assertTrue(NO_OBJECT_TYPE.canTestForShallowEqualityWith(ERROR_TYPE));
    assertTrue(NO_OBJECT_TYPE.canTestForShallowEqualityWith(EVAL_ERROR_TYPE));
    assertTrue(NO_OBJECT_TYPE.canTestForShallowEqualityWith(functionType));
    assertTrue(NO_OBJECT_TYPE.canTestForShallowEqualityWith(recordType));
    assertFalse(NO_OBJECT_TYPE.canTestForShallowEqualityWith(NULL_TYPE));
    assertFalse(NO_OBJECT_TYPE.canTestForShallowEqualityWith(NUMBER_TYPE));
    assertTrue(NO_OBJECT_TYPE.
        canTestForShallowEqualityWith(NUMBER_OBJECT_TYPE));
    assertTrue(NO_OBJECT_TYPE.canTestForShallowEqualityWith(OBJECT_TYPE));
    assertTrue(NO_OBJECT_TYPE.canTestForShallowEqualityWith(URI_ERROR_TYPE));
    assertTrue(NO_OBJECT_TYPE.canTestForShallowEqualityWith(RANGE_ERROR_TYPE));
    assertTrue(NO_OBJECT_TYPE.
        canTestForShallowEqualityWith(REFERENCE_ERROR_TYPE));
    assertTrue(NO_OBJECT_TYPE.canTestForShallowEqualityWith(REGEXP_TYPE));
    assertFalse(NO_OBJECT_TYPE.canTestForShallowEqualityWith(STRING_TYPE));
    assertTrue(NO_OBJECT_TYPE.
        canTestForShallowEqualityWith(STRING_OBJECT_TYPE));
    assertTrue(NO_OBJECT_TYPE.
        canTestForShallowEqualityWith(SYNTAX_ERROR_TYPE));
    assertTrue(NO_OBJECT_TYPE.canTestForShallowEqualityWith(TYPE_ERROR_TYPE));
    assertTrue(NO_OBJECT_TYPE.canTestForShallowEqualityWith(ALL_TYPE));
    assertFalse(NO_OBJECT_TYPE.canTestForShallowEqualityWith(VOID_TYPE));

    
    assertFalse(NO_OBJECT_TYPE.isNullable());

    
    assertTrue(NO_OBJECT_TYPE.isObject());

    
    assertTrue(NO_OBJECT_TYPE.matchesInt32Context());
    assertTrue(NO_OBJECT_TYPE.matchesNumberContext());
    assertTrue(NO_OBJECT_TYPE.matchesObjectContext());
    assertTrue(NO_OBJECT_TYPE.matchesStringContext());
    assertTrue(NO_OBJECT_TYPE.matchesUint32Context());

    
    assertEquals("NoObject", NO_OBJECT_TYPE.toString());
    assertFalse(NO_OBJECT_TYPE.hasDisplayName());
    assertEquals(null, NO_OBJECT_TYPE.getDisplayName());

    
    assertTypeEquals(NO_TYPE,
        NO_OBJECT_TYPE.getPropertyType("anyProperty"));

    Asserts.assertResolvesToSame(NO_OBJECT_TYPE);

    assertFalse(NO_OBJECT_TYPE.isNominalConstructor());
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testNoType
  public void testNoType() throws Exception {
    
    assertFalse(NO_TYPE.isNoObjectType());
    assertTrue(NO_TYPE.isNoType());
    assertFalse(NO_TYPE.isArrayType());
    assertFalse(NO_TYPE.isBooleanValueType());
    assertFalse(NO_TYPE.isDateType());
    assertFalse(NO_TYPE.isEnumElementType());
    assertFalse(NO_TYPE.isNullType());
    assertFalse(NO_TYPE.isNamedType());
    assertFalse(NO_TYPE.isNullType());
    assertTrue(NO_TYPE.isNumber());
    assertFalse(NO_TYPE.isNumberObjectType());
    assertFalse(NO_TYPE.isNumberValueType());
    assertTrue(NO_TYPE.isObject());
    assertFalse(NO_TYPE.isFunctionPrototypeType());
    assertFalse(NO_TYPE.isRegexpType());
    assertTrue(NO_TYPE.isString());
    assertFalse(NO_TYPE.isStringObjectType());
    assertFalse(NO_TYPE.isStringValueType());
    assertFalse(NO_TYPE.isEnumType());
    assertFalse(NO_TYPE.isUnionType());
    assertFalse(NO_TYPE.isAllType());
    assertFalse(NO_TYPE.isVoidType());
    assertTrue(NO_TYPE.isConstructor());
    assertFalse(NO_TYPE.isInstanceType());

    
    assertTrue(NO_TYPE.canAssignTo(NO_TYPE));
    assertTrue(NO_TYPE.canAssignTo(NO_OBJECT_TYPE));
    assertTrue(NO_TYPE.canAssignTo(ARRAY_TYPE));
    assertTrue(NO_TYPE.canAssignTo(BOOLEAN_TYPE));
    assertTrue(NO_TYPE.canAssignTo(BOOLEAN_OBJECT_TYPE));
    assertTrue(NO_TYPE.canAssignTo(DATE_TYPE));
    assertTrue(NO_TYPE.canAssignTo(ERROR_TYPE));
    assertTrue(NO_TYPE.canAssignTo(EVAL_ERROR_TYPE));
    assertTrue(NO_TYPE.canAssignTo(functionType));
    assertTrue(NO_TYPE.canAssignTo(NULL_TYPE));
    assertTrue(NO_TYPE.canAssignTo(NUMBER_TYPE));
    assertTrue(NO_TYPE.canAssignTo(NUMBER_OBJECT_TYPE));
    assertTrue(NO_TYPE.canAssignTo(OBJECT_TYPE));
    assertTrue(NO_TYPE.canAssignTo(URI_ERROR_TYPE));
    assertTrue(NO_TYPE.canAssignTo(RANGE_ERROR_TYPE));
    assertTrue(NO_TYPE.canAssignTo(REFERENCE_ERROR_TYPE));
    assertTrue(NO_TYPE.canAssignTo(REGEXP_TYPE));
    assertTrue(NO_TYPE.canAssignTo(STRING_TYPE));
    assertTrue(NO_TYPE.canAssignTo(STRING_OBJECT_TYPE));
    assertTrue(NO_TYPE.canAssignTo(SYNTAX_ERROR_TYPE));
    assertTrue(NO_TYPE.canAssignTo(TYPE_ERROR_TYPE));
    assertTrue(NO_TYPE.canAssignTo(ALL_TYPE));
    assertTrue(NO_TYPE.canAssignTo(VOID_TYPE));

    
    assertCannotTestForEqualityWith(NO_TYPE, NO_TYPE);
    assertCannotTestForEqualityWith(NO_TYPE, NO_OBJECT_TYPE);
    assertCanTestForEqualityWith(NO_TYPE, ARRAY_TYPE);
    assertCanTestForEqualityWith(NO_TYPE, BOOLEAN_TYPE);
    assertCanTestForEqualityWith(NO_TYPE, BOOLEAN_OBJECT_TYPE);
    assertCanTestForEqualityWith(NO_TYPE, DATE_TYPE);
    assertCanTestForEqualityWith(NO_TYPE, ERROR_TYPE);
    assertCanTestForEqualityWith(NO_TYPE, EVAL_ERROR_TYPE);
    assertCanTestForEqualityWith(NO_TYPE, functionType);
    assertCanTestForEqualityWith(NO_TYPE, NULL_TYPE);
    assertCanTestForEqualityWith(NO_TYPE, NUMBER_TYPE);
    assertCanTestForEqualityWith(NO_TYPE, NUMBER_OBJECT_TYPE);
    assertCanTestForEqualityWith(NO_TYPE, OBJECT_TYPE);
    assertCanTestForEqualityWith(NO_TYPE, URI_ERROR_TYPE);
    assertCanTestForEqualityWith(NO_TYPE, RANGE_ERROR_TYPE);
    assertCanTestForEqualityWith(NO_TYPE, REFERENCE_ERROR_TYPE);
    assertCanTestForEqualityWith(NO_TYPE, REGEXP_TYPE);
    assertCanTestForEqualityWith(NO_TYPE, STRING_TYPE);
    assertCanTestForEqualityWith(NO_TYPE, STRING_OBJECT_TYPE);
    assertCanTestForEqualityWith(NO_TYPE, SYNTAX_ERROR_TYPE);
    assertCanTestForEqualityWith(NO_TYPE, TYPE_ERROR_TYPE);
    assertCanTestForEqualityWith(NO_TYPE, ALL_TYPE);
    assertCanTestForEqualityWith(NO_TYPE, VOID_TYPE);

    
    assertTrue(NO_TYPE.canTestForShallowEqualityWith(NO_TYPE));
    assertTrue(NO_TYPE.canTestForShallowEqualityWith(NO_OBJECT_TYPE));
    assertTrue(NO_TYPE.canTestForShallowEqualityWith(ARRAY_TYPE));
    assertTrue(NO_TYPE.canTestForShallowEqualityWith(BOOLEAN_TYPE));
    assertTrue(NO_TYPE.canTestForShallowEqualityWith(BOOLEAN_OBJECT_TYPE));
    assertTrue(NO_TYPE.canTestForShallowEqualityWith(DATE_TYPE));
    assertTrue(NO_TYPE.canTestForShallowEqualityWith(ERROR_TYPE));
    assertTrue(NO_TYPE.canTestForShallowEqualityWith(EVAL_ERROR_TYPE));
    assertTrue(NO_TYPE.canTestForShallowEqualityWith(functionType));
    assertTrue(NO_TYPE.canTestForShallowEqualityWith(NULL_TYPE));
    assertTrue(NO_TYPE.canTestForShallowEqualityWith(NUMBER_TYPE));
    assertTrue(NO_TYPE.canTestForShallowEqualityWith(NUMBER_OBJECT_TYPE));
    assertTrue(NO_TYPE.canTestForShallowEqualityWith(OBJECT_TYPE));
    assertTrue(NO_TYPE.canTestForShallowEqualityWith(URI_ERROR_TYPE));
    assertTrue(NO_TYPE.canTestForShallowEqualityWith(RANGE_ERROR_TYPE));
    assertTrue(NO_TYPE.canTestForShallowEqualityWith(REFERENCE_ERROR_TYPE));
    assertTrue(NO_TYPE.canTestForShallowEqualityWith(REGEXP_TYPE));
    assertTrue(NO_TYPE.canTestForShallowEqualityWith(STRING_TYPE));
    assertTrue(NO_TYPE.canTestForShallowEqualityWith(STRING_OBJECT_TYPE));
    assertTrue(NO_TYPE.canTestForShallowEqualityWith(SYNTAX_ERROR_TYPE));
    assertTrue(NO_TYPE.canTestForShallowEqualityWith(TYPE_ERROR_TYPE));
    assertTrue(NO_TYPE.canTestForShallowEqualityWith(ALL_TYPE));
    assertTrue(NO_TYPE.canTestForShallowEqualityWith(VOID_TYPE));

    
    assertTrue(NO_TYPE.isNullable());

    
    assertTrue(NO_TYPE.isObject());

    
    assertTrue(NO_TYPE.matchesInt32Context());
    assertTrue(NO_TYPE.matchesNumberContext());
    assertTrue(NO_TYPE.matchesObjectContext());
    assertTrue(NO_TYPE.matchesStringContext());
    assertTrue(NO_TYPE.matchesUint32Context());

    
    assertEquals("None", NO_TYPE.toString());
    assertEquals(null, NO_TYPE.getDisplayName());
    assertFalse(NO_TYPE.hasDisplayName());

    
    assertTypeEquals(NO_TYPE,
        NO_TYPE.getPropertyType("anyProperty"));

    Asserts.assertResolvesToSame(NO_TYPE);

    assertFalse(NO_TYPE.isNominalConstructor());
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testNoResolvedType
  public void testNoResolvedType() throws Exception {
    
    assertFalse(NO_RESOLVED_TYPE.isNoObjectType());
    assertFalse(NO_RESOLVED_TYPE.isNoType());
    assertTrue(NO_RESOLVED_TYPE.isNoResolvedType());
    assertFalse(NO_RESOLVED_TYPE.isArrayType());
    assertFalse(NO_RESOLVED_TYPE.isBooleanValueType());
    assertFalse(NO_RESOLVED_TYPE.isDateType());
    assertFalse(NO_RESOLVED_TYPE.isEnumElementType());
    assertFalse(NO_RESOLVED_TYPE.isNullType());
    assertFalse(NO_RESOLVED_TYPE.isNamedType());
    assertTrue(NO_RESOLVED_TYPE.isNumber());
    assertFalse(NO_RESOLVED_TYPE.isNumberObjectType());
    assertFalse(NO_RESOLVED_TYPE.isNumberValueType());
    assertTrue(NO_RESOLVED_TYPE.isObject());
    assertFalse(NO_RESOLVED_TYPE.isFunctionPrototypeType());
    assertFalse(NO_RESOLVED_TYPE.isRegexpType());
    assertTrue(NO_RESOLVED_TYPE.isString());
    assertFalse(NO_RESOLVED_TYPE.isStringObjectType());
    assertFalse(NO_RESOLVED_TYPE.isStringValueType());
    assertFalse(NO_RESOLVED_TYPE.isEnumType());
    assertFalse(NO_RESOLVED_TYPE.isUnionType());
    assertFalse(NO_RESOLVED_TYPE.isAllType());
    assertFalse(NO_RESOLVED_TYPE.isVoidType());
    assertTrue(NO_RESOLVED_TYPE.isConstructor());
    assertFalse(NO_RESOLVED_TYPE.isInstanceType());

    
    assertTrue(NO_RESOLVED_TYPE.canAssignTo(NO_RESOLVED_TYPE));
    assertTrue(NO_RESOLVED_TYPE.canAssignTo(NO_OBJECT_TYPE));
    assertTrue(NO_RESOLVED_TYPE.canAssignTo(ARRAY_TYPE));
    assertTrue(NO_RESOLVED_TYPE.canAssignTo(BOOLEAN_TYPE));
    assertTrue(NO_RESOLVED_TYPE.canAssignTo(BOOLEAN_OBJECT_TYPE));
    assertTrue(NO_RESOLVED_TYPE.canAssignTo(DATE_TYPE));
    assertTrue(NO_RESOLVED_TYPE.canAssignTo(ERROR_TYPE));
    assertTrue(NO_RESOLVED_TYPE.canAssignTo(EVAL_ERROR_TYPE));
    assertTrue(NO_RESOLVED_TYPE.canAssignTo(functionType));
    assertTrue(NO_RESOLVED_TYPE.canAssignTo(NULL_TYPE));
    assertTrue(NO_RESOLVED_TYPE.canAssignTo(NUMBER_TYPE));
    assertTrue(NO_RESOLVED_TYPE.canAssignTo(NUMBER_OBJECT_TYPE));
    assertTrue(NO_RESOLVED_TYPE.canAssignTo(OBJECT_TYPE));
    assertTrue(NO_RESOLVED_TYPE.canAssignTo(URI_ERROR_TYPE));
    assertTrue(NO_RESOLVED_TYPE.canAssignTo(RANGE_ERROR_TYPE));
    assertTrue(NO_RESOLVED_TYPE.canAssignTo(REFERENCE_ERROR_TYPE));
    assertTrue(NO_RESOLVED_TYPE.canAssignTo(REGEXP_TYPE));
    assertTrue(NO_RESOLVED_TYPE.canAssignTo(STRING_TYPE));
    assertTrue(NO_RESOLVED_TYPE.canAssignTo(STRING_OBJECT_TYPE));
    assertTrue(NO_RESOLVED_TYPE.canAssignTo(SYNTAX_ERROR_TYPE));
    assertTrue(NO_RESOLVED_TYPE.canAssignTo(TYPE_ERROR_TYPE));
    assertTrue(NO_RESOLVED_TYPE.canAssignTo(ALL_TYPE));
    assertTrue(NO_RESOLVED_TYPE.canAssignTo(VOID_TYPE));

    
    assertCanTestForEqualityWith(NO_RESOLVED_TYPE, NO_RESOLVED_TYPE);
    assertCanTestForEqualityWith(NO_RESOLVED_TYPE, NO_TYPE);
    assertCanTestForEqualityWith(NO_RESOLVED_TYPE, NO_OBJECT_TYPE);
    assertCanTestForEqualityWith(NO_RESOLVED_TYPE, ARRAY_TYPE);
    assertCanTestForEqualityWith(NO_RESOLVED_TYPE, BOOLEAN_TYPE);
    assertCanTestForEqualityWith(NO_RESOLVED_TYPE, BOOLEAN_OBJECT_TYPE);
    assertCanTestForEqualityWith(NO_RESOLVED_TYPE, DATE_TYPE);
    assertCanTestForEqualityWith(NO_RESOLVED_TYPE, ERROR_TYPE);
    assertCanTestForEqualityWith(NO_RESOLVED_TYPE, EVAL_ERROR_TYPE);
    assertCanTestForEqualityWith(NO_RESOLVED_TYPE, functionType);
    assertCanTestForEqualityWith(NO_RESOLVED_TYPE, NULL_TYPE);
    assertCanTestForEqualityWith(NO_RESOLVED_TYPE, NUMBER_TYPE);
    assertCanTestForEqualityWith(NO_RESOLVED_TYPE, NUMBER_OBJECT_TYPE);
    assertCanTestForEqualityWith(NO_RESOLVED_TYPE, OBJECT_TYPE);
    assertCanTestForEqualityWith(NO_RESOLVED_TYPE, URI_ERROR_TYPE);
    assertCanTestForEqualityWith(NO_RESOLVED_TYPE, RANGE_ERROR_TYPE);
    assertCanTestForEqualityWith(NO_RESOLVED_TYPE, REFERENCE_ERROR_TYPE);
    assertCanTestForEqualityWith(NO_RESOLVED_TYPE, REGEXP_TYPE);
    assertCanTestForEqualityWith(NO_RESOLVED_TYPE, STRING_TYPE);
    assertCanTestForEqualityWith(NO_RESOLVED_TYPE, STRING_OBJECT_TYPE);
    assertCanTestForEqualityWith(NO_RESOLVED_TYPE, SYNTAX_ERROR_TYPE);
    assertCanTestForEqualityWith(NO_RESOLVED_TYPE, TYPE_ERROR_TYPE);
    assertCanTestForEqualityWith(NO_RESOLVED_TYPE, ALL_TYPE);
    assertCanTestForEqualityWith(NO_RESOLVED_TYPE, VOID_TYPE);

    
    assertTrue(
        NO_RESOLVED_TYPE.canTestForShallowEqualityWith(NO_RESOLVED_TYPE));
    assertTrue(NO_RESOLVED_TYPE.canTestForShallowEqualityWith(NO_OBJECT_TYPE));
    assertTrue(NO_RESOLVED_TYPE.canTestForShallowEqualityWith(ARRAY_TYPE));
    assertTrue(NO_RESOLVED_TYPE.canTestForShallowEqualityWith(BOOLEAN_TYPE));
    assertTrue(
        NO_RESOLVED_TYPE.canTestForShallowEqualityWith(BOOLEAN_OBJECT_TYPE));
    assertTrue(NO_RESOLVED_TYPE.canTestForShallowEqualityWith(DATE_TYPE));
    assertTrue(NO_RESOLVED_TYPE.canTestForShallowEqualityWith(ERROR_TYPE));
    assertTrue(NO_RESOLVED_TYPE.canTestForShallowEqualityWith(EVAL_ERROR_TYPE));
    assertTrue(NO_RESOLVED_TYPE.canTestForShallowEqualityWith(functionType));
    assertTrue(NO_RESOLVED_TYPE.canTestForShallowEqualityWith(NULL_TYPE));
    assertTrue(NO_RESOLVED_TYPE.canTestForShallowEqualityWith(NUMBER_TYPE));
    assertTrue(
        NO_RESOLVED_TYPE.canTestForShallowEqualityWith(NUMBER_OBJECT_TYPE));
    assertTrue(NO_RESOLVED_TYPE.canTestForShallowEqualityWith(OBJECT_TYPE));
    assertTrue(NO_RESOLVED_TYPE.canTestForShallowEqualityWith(URI_ERROR_TYPE));
    assertTrue(
        NO_RESOLVED_TYPE.canTestForShallowEqualityWith(RANGE_ERROR_TYPE));
    assertTrue(
        NO_RESOLVED_TYPE.canTestForShallowEqualityWith(REFERENCE_ERROR_TYPE));
    assertTrue(NO_RESOLVED_TYPE.canTestForShallowEqualityWith(REGEXP_TYPE));
    assertTrue(NO_RESOLVED_TYPE.canTestForShallowEqualityWith(STRING_TYPE));
    assertTrue(
        NO_RESOLVED_TYPE.canTestForShallowEqualityWith(STRING_OBJECT_TYPE));
    assertTrue(
        NO_RESOLVED_TYPE.canTestForShallowEqualityWith(SYNTAX_ERROR_TYPE));
    assertTrue(NO_RESOLVED_TYPE.canTestForShallowEqualityWith(TYPE_ERROR_TYPE));
    assertTrue(NO_RESOLVED_TYPE.canTestForShallowEqualityWith(ALL_TYPE));
    assertTrue(NO_RESOLVED_TYPE.canTestForShallowEqualityWith(VOID_TYPE));

    
    assertTrue(NO_RESOLVED_TYPE.isNullable());

    
    assertTrue(NO_RESOLVED_TYPE.isObject());

    
    assertTrue(NO_RESOLVED_TYPE.matchesInt32Context());
    assertTrue(NO_RESOLVED_TYPE.matchesNumberContext());
    assertTrue(NO_RESOLVED_TYPE.matchesObjectContext());
    assertTrue(NO_RESOLVED_TYPE.matchesStringContext());
    assertTrue(NO_RESOLVED_TYPE.matchesUint32Context());

    
    assertEquals("NoResolvedType", NO_RESOLVED_TYPE.toString());
    assertEquals(null, NO_RESOLVED_TYPE.getDisplayName());
    assertFalse(NO_RESOLVED_TYPE.hasDisplayName());

    
    assertTypeEquals(CHECKED_UNKNOWN_TYPE,
        NO_RESOLVED_TYPE.getPropertyType("anyProperty"));

    Asserts.assertResolvesToSame(NO_RESOLVED_TYPE);

    assertTrue(forwardDeclaredNamedType.isEmptyType());
    assertTrue(forwardDeclaredNamedType.isNoResolvedType());
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testArrayType
  public void testArrayType() throws Exception {
    
    assertTrue(ARRAY_TYPE.isArrayType());
    assertFalse(ARRAY_TYPE.isBooleanValueType());
    assertFalse(ARRAY_TYPE.isDateType());
    assertFalse(ARRAY_TYPE.isEnumElementType());
    assertFalse(ARRAY_TYPE.isNamedType());
    assertFalse(ARRAY_TYPE.isNullType());
    assertFalse(ARRAY_TYPE.isNumber());
    assertFalse(ARRAY_TYPE.isNumberObjectType());
    assertFalse(ARRAY_TYPE.isNumberValueType());
    assertTrue(ARRAY_TYPE.isObject());
    assertFalse(ARRAY_TYPE.isFunctionPrototypeType());
    assertTrue(ARRAY_TYPE.getImplicitPrototype().isFunctionPrototypeType());
    assertFalse(ARRAY_TYPE.isRegexpType());
    assertFalse(ARRAY_TYPE.isString());
    assertFalse(ARRAY_TYPE.isStringObjectType());
    assertFalse(ARRAY_TYPE.isStringValueType());
    assertFalse(ARRAY_TYPE.isEnumType());
    assertFalse(ARRAY_TYPE.isUnionType());
    assertFalse(ARRAY_TYPE.isAllType());
    assertFalse(ARRAY_TYPE.isVoidType());
    assertFalse(ARRAY_TYPE.isConstructor());
    assertTrue(ARRAY_TYPE.isInstanceType());

    
    assertFalse(ARRAY_TYPE.canAssignTo(NO_TYPE));
    assertFalse(ARRAY_TYPE.canAssignTo(NO_OBJECT_TYPE));
    assertTrue(ARRAY_TYPE.canAssignTo(ALL_TYPE));
    assertFalse(ARRAY_TYPE.canAssignTo(STRING_OBJECT_TYPE));
    assertFalse(ARRAY_TYPE.canAssignTo(NUMBER_TYPE));
    assertFalse(ARRAY_TYPE.canAssignTo(functionType));
    assertFalse(ARRAY_TYPE.canAssignTo(recordType));
    assertFalse(ARRAY_TYPE.canAssignTo(NULL_TYPE));
    assertTrue(ARRAY_TYPE.canAssignTo(OBJECT_TYPE));
    assertFalse(ARRAY_TYPE.canAssignTo(DATE_TYPE));
    assertTrue(ARRAY_TYPE.canAssignTo(unresolvedNamedType));
    assertFalse(ARRAY_TYPE.canAssignTo(namedGoogBar));
    assertFalse(ARRAY_TYPE.canAssignTo(REGEXP_TYPE));

    
    assertFalse(ARRAY_TYPE.canBeCalled());

    
    assertCanTestForEqualityWith(ARRAY_TYPE, NO_TYPE);
    assertCanTestForEqualityWith(ARRAY_TYPE, NO_OBJECT_TYPE);
    assertCanTestForEqualityWith(ARRAY_TYPE, ALL_TYPE);
    assertCanTestForEqualityWith(ARRAY_TYPE, STRING_OBJECT_TYPE);
    assertCanTestForEqualityWith(ARRAY_TYPE, NUMBER_TYPE);
    assertCanTestForEqualityWith(ARRAY_TYPE, functionType);
    assertCanTestForEqualityWith(ARRAY_TYPE, recordType);
    assertCannotTestForEqualityWith(ARRAY_TYPE, VOID_TYPE);
    assertCanTestForEqualityWith(ARRAY_TYPE, OBJECT_TYPE);
    assertCanTestForEqualityWith(ARRAY_TYPE, DATE_TYPE);
    assertCanTestForEqualityWith(ARRAY_TYPE, REGEXP_TYPE);

    
    assertTrue(ARRAY_TYPE.canTestForShallowEqualityWith(NO_TYPE));
    assertTrue(ARRAY_TYPE.canTestForShallowEqualityWith(NO_OBJECT_TYPE));
    assertTrue(ARRAY_TYPE.canTestForShallowEqualityWith(ARRAY_TYPE));
    assertFalse(ARRAY_TYPE.canTestForShallowEqualityWith(BOOLEAN_TYPE));
    assertFalse(ARRAY_TYPE.canTestForShallowEqualityWith(BOOLEAN_OBJECT_TYPE));
    assertFalse(ARRAY_TYPE.canTestForShallowEqualityWith(DATE_TYPE));
    assertFalse(ARRAY_TYPE.canTestForShallowEqualityWith(ERROR_TYPE));
    assertFalse(ARRAY_TYPE.canTestForShallowEqualityWith(EVAL_ERROR_TYPE));
    assertFalse(ARRAY_TYPE.canTestForShallowEqualityWith(functionType));
    assertFalse(ARRAY_TYPE.canTestForShallowEqualityWith(recordType));
    assertFalse(ARRAY_TYPE.canTestForShallowEqualityWith(NULL_TYPE));
    assertFalse(ARRAY_TYPE.canTestForShallowEqualityWith(NUMBER_TYPE));
    assertFalse(ARRAY_TYPE.canTestForShallowEqualityWith(NUMBER_OBJECT_TYPE));
    assertTrue(ARRAY_TYPE.canTestForShallowEqualityWith(OBJECT_TYPE));
    assertFalse(ARRAY_TYPE.canTestForShallowEqualityWith(URI_ERROR_TYPE));
    assertFalse(ARRAY_TYPE.canTestForShallowEqualityWith(RANGE_ERROR_TYPE));
    assertFalse(ARRAY_TYPE.canTestForShallowEqualityWith(REFERENCE_ERROR_TYPE));
    assertFalse(ARRAY_TYPE.canTestForShallowEqualityWith(REGEXP_TYPE));
    assertFalse(ARRAY_TYPE.canTestForShallowEqualityWith(STRING_TYPE));
    assertFalse(ARRAY_TYPE.canTestForShallowEqualityWith(STRING_OBJECT_TYPE));
    assertFalse(ARRAY_TYPE.canTestForShallowEqualityWith(SYNTAX_ERROR_TYPE));
    assertFalse(ARRAY_TYPE.canTestForShallowEqualityWith(TYPE_ERROR_TYPE));
    assertTrue(ARRAY_TYPE.canTestForShallowEqualityWith(ALL_TYPE));
    assertFalse(ARRAY_TYPE.canTestForShallowEqualityWith(VOID_TYPE));

    
    assertFalse(ARRAY_TYPE.isNullable());
    assertTrue(createUnionType(ARRAY_TYPE, NULL_TYPE).isNullable());

    
    assertTrue(ARRAY_TYPE.isObject());

    
    assertTypeEquals(ALL_TYPE,
        ARRAY_TYPE.getLeastSupertype(ALL_TYPE));
    assertTypeEquals(createUnionType(STRING_OBJECT_TYPE, ARRAY_TYPE),
        ARRAY_TYPE.getLeastSupertype(STRING_OBJECT_TYPE));
    assertTypeEquals(createUnionType(NUMBER_TYPE, ARRAY_TYPE),
        ARRAY_TYPE.getLeastSupertype(NUMBER_TYPE));
    assertTypeEquals(createUnionType(ARRAY_TYPE, functionType),
        ARRAY_TYPE.getLeastSupertype(functionType));
    assertTypeEquals(OBJECT_TYPE, ARRAY_TYPE.getLeastSupertype(OBJECT_TYPE));
    assertTypeEquals(createUnionType(DATE_TYPE, ARRAY_TYPE),
        ARRAY_TYPE.getLeastSupertype(DATE_TYPE));
    assertTypeEquals(createUnionType(REGEXP_TYPE, ARRAY_TYPE),
        ARRAY_TYPE.getLeastSupertype(REGEXP_TYPE));

    
    assertEquals(17, ARRAY_TYPE.getImplicitPrototype().getPropertiesCount());
    assertEquals(18, ARRAY_TYPE.getPropertiesCount());
    assertReturnTypeEquals(ARRAY_TYPE,
        ARRAY_TYPE.getPropertyType("constructor"));
    assertReturnTypeEquals(STRING_TYPE,
        ARRAY_TYPE.getPropertyType("toString"));
    assertReturnTypeEquals(STRING_TYPE,
        ARRAY_TYPE.getPropertyType("toLocaleString"));
    assertReturnTypeEquals(ARRAY_TYPE, ARRAY_TYPE.getPropertyType("concat"));
    assertReturnTypeEquals(STRING_TYPE,
        ARRAY_TYPE.getPropertyType("join"));
    assertReturnTypeEquals(UNKNOWN_TYPE, ARRAY_TYPE.getPropertyType("pop"));
    assertReturnTypeEquals(NUMBER_TYPE, ARRAY_TYPE.getPropertyType("push"));
    assertReturnTypeEquals(ARRAY_TYPE, ARRAY_TYPE.getPropertyType("reverse"));
    assertReturnTypeEquals(UNKNOWN_TYPE, ARRAY_TYPE.getPropertyType("shift"));
    assertReturnTypeEquals(ARRAY_TYPE, ARRAY_TYPE.getPropertyType("slice"));
    assertReturnTypeEquals(ARRAY_TYPE, ARRAY_TYPE.getPropertyType("sort"));
    assertReturnTypeEquals(ARRAY_TYPE, ARRAY_TYPE.getPropertyType("splice"));
    assertReturnTypeEquals(NUMBER_TYPE, ARRAY_TYPE.getPropertyType("unshift"));
    assertTypeEquals(NUMBER_TYPE, ARRAY_TYPE.getPropertyType("length"));

    
    assertPropertyTypeDeclared(ARRAY_TYPE, "pop");

    
    assertFalse(ARRAY_TYPE.matchesInt32Context());
    assertFalse(ARRAY_TYPE.matchesNumberContext());
    assertTrue(ARRAY_TYPE.matchesObjectContext());
    assertTrue(ARRAY_TYPE.matchesStringContext());
    assertFalse(ARRAY_TYPE.matchesUint32Context());

    
    assertEquals("Array", ARRAY_TYPE.toString());
    assertTrue(ARRAY_TYPE.hasDisplayName());
    assertEquals("Array", ARRAY_TYPE.getDisplayName());

    assertTrue(ARRAY_TYPE.isNativeObjectType());

    Asserts.assertResolvesToSame(ARRAY_TYPE);

    assertFalse(ARRAY_TYPE.isNominalConstructor());
    assertTrue(ARRAY_TYPE.getConstructor().isNominalConstructor());
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testUnknownType
  public void testUnknownType() throws Exception {
    
    assertFalse(UNKNOWN_TYPE.isArrayType());
    assertFalse(UNKNOWN_TYPE.isBooleanObjectType());
    assertFalse(UNKNOWN_TYPE.isBooleanValueType());
    assertFalse(UNKNOWN_TYPE.isDateType());
    assertFalse(UNKNOWN_TYPE.isEnumElementType());
    assertFalse(UNKNOWN_TYPE.isNamedType());
    assertFalse(UNKNOWN_TYPE.isNullType());
    assertFalse(UNKNOWN_TYPE.isNumberObjectType());
    assertFalse(UNKNOWN_TYPE.isNumberValueType());
    assertTrue(UNKNOWN_TYPE.isObject());
    assertFalse(UNKNOWN_TYPE.isFunctionPrototypeType());
    assertFalse(UNKNOWN_TYPE.isRegexpType());
    assertFalse(UNKNOWN_TYPE.isStringObjectType());
    assertFalse(UNKNOWN_TYPE.isStringValueType());
    assertFalse(UNKNOWN_TYPE.isEnumType());
    assertFalse(UNKNOWN_TYPE.isUnionType());
    assertTrue(UNKNOWN_TYPE.isUnknownType());
    assertFalse(UNKNOWN_TYPE.isVoidType());
    assertFalse(UNKNOWN_TYPE.isConstructor());
    assertFalse(UNKNOWN_TYPE.isInstanceType());

    
    assertNull(UNKNOWN_TYPE.autoboxesTo());

    
    assertTrue(UNKNOWN_TYPE.canAssignTo(UNKNOWN_TYPE));
    assertTrue(UNKNOWN_TYPE.canAssignTo(STRING_TYPE));
    assertTrue(UNKNOWN_TYPE.canAssignTo(NUMBER_TYPE));
    assertTrue(UNKNOWN_TYPE.canAssignTo(functionType));
    assertTrue(UNKNOWN_TYPE.canAssignTo(recordType));
    assertTrue(UNKNOWN_TYPE.canAssignTo(NULL_TYPE));
    assertTrue(UNKNOWN_TYPE.canAssignTo(OBJECT_TYPE));
    assertTrue(UNKNOWN_TYPE.canAssignTo(DATE_TYPE));
    assertTrue(UNKNOWN_TYPE.canAssignTo(namedGoogBar));
    assertTrue(UNKNOWN_TYPE.canAssignTo(unresolvedNamedType));
    assertTrue(UNKNOWN_TYPE.canAssignTo(REGEXP_TYPE));
    assertTrue(UNKNOWN_TYPE.canAssignTo(VOID_TYPE));

    
    assertTrue(UNKNOWN_TYPE.canBeCalled());

    
    assertCanTestForEqualityWith(UNKNOWN_TYPE, UNKNOWN_TYPE);
    assertCanTestForEqualityWith(UNKNOWN_TYPE, STRING_TYPE);
    assertCanTestForEqualityWith(UNKNOWN_TYPE, NUMBER_TYPE);
    assertCanTestForEqualityWith(UNKNOWN_TYPE, functionType);
    assertCanTestForEqualityWith(UNKNOWN_TYPE, recordType);
    assertCanTestForEqualityWith(UNKNOWN_TYPE, VOID_TYPE);
    assertCanTestForEqualityWith(UNKNOWN_TYPE, OBJECT_TYPE);
    assertCanTestForEqualityWith(UNKNOWN_TYPE, DATE_TYPE);
    assertCanTestForEqualityWith(UNKNOWN_TYPE, REGEXP_TYPE);
    assertCanTestForEqualityWith(UNKNOWN_TYPE, BOOLEAN_TYPE);

    
    assertTrue(UNKNOWN_TYPE.canTestForShallowEqualityWith(UNKNOWN_TYPE));
    assertTrue(UNKNOWN_TYPE.canTestForShallowEqualityWith(STRING_TYPE));
    assertTrue(UNKNOWN_TYPE.canTestForShallowEqualityWith(NUMBER_TYPE));
    assertTrue(UNKNOWN_TYPE.canTestForShallowEqualityWith(functionType));
    assertTrue(UNKNOWN_TYPE.canTestForShallowEqualityWith(recordType));
    assertTrue(UNKNOWN_TYPE.canTestForShallowEqualityWith(VOID_TYPE));
    assertTrue(UNKNOWN_TYPE.canTestForShallowEqualityWith(OBJECT_TYPE));
    assertTrue(UNKNOWN_TYPE.canTestForShallowEqualityWith(DATE_TYPE));
    assertTrue(UNKNOWN_TYPE.canTestForShallowEqualityWith(REGEXP_TYPE));

    
    assertTrue(UNKNOWN_TYPE.isNullable());

    
    assertTypeEquals(UNKNOWN_TYPE,
        UNKNOWN_TYPE.getLeastSupertype(UNKNOWN_TYPE));
    assertTypeEquals(UNKNOWN_TYPE,
        UNKNOWN_TYPE.getLeastSupertype(STRING_TYPE));
    assertTypeEquals(UNKNOWN_TYPE,
        UNKNOWN_TYPE.getLeastSupertype(NUMBER_TYPE));
    assertTypeEquals(UNKNOWN_TYPE,
        UNKNOWN_TYPE.getLeastSupertype(functionType));
    assertTypeEquals(UNKNOWN_TYPE,
        UNKNOWN_TYPE.getLeastSupertype(OBJECT_TYPE));
    assertTypeEquals(UNKNOWN_TYPE,
        UNKNOWN_TYPE.getLeastSupertype(DATE_TYPE));
    assertTypeEquals(UNKNOWN_TYPE,
        UNKNOWN_TYPE.getLeastSupertype(REGEXP_TYPE));

    
    assertTrue(UNKNOWN_TYPE.matchesInt32Context());
    assertTrue(UNKNOWN_TYPE.matchesNumberContext());
    assertTrue(UNKNOWN_TYPE.matchesObjectContext());
    assertTrue(UNKNOWN_TYPE.matchesStringContext());
    assertTrue(UNKNOWN_TYPE.matchesUint32Context());

    
    assertPropertyTypeUnknown(UNKNOWN_TYPE, "XXX");

    
    assertEquals("?", UNKNOWN_TYPE.toString());
    assertTrue(UNKNOWN_TYPE.hasDisplayName());
    assertEquals("Unknown", UNKNOWN_TYPE.getDisplayName());

    Asserts.assertResolvesToSame(UNKNOWN_TYPE);
    assertFalse(UNKNOWN_TYPE.isNominalConstructor());
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testAllType
  public void testAllType() throws Exception {
    
    assertFalse(ALL_TYPE.isArrayType());
    assertFalse(ALL_TYPE.isBooleanValueType());
    assertFalse(ALL_TYPE.isDateType());
    assertFalse(ALL_TYPE.isEnumElementType());
    assertFalse(ALL_TYPE.isNamedType());
    assertFalse(ALL_TYPE.isNullType());
    assertFalse(ALL_TYPE.isNumber());
    assertFalse(ALL_TYPE.isNumberObjectType());
    assertFalse(ALL_TYPE.isNumberValueType());
    assertFalse(ALL_TYPE.isObject());
    assertFalse(ALL_TYPE.isFunctionPrototypeType());
    assertFalse(ALL_TYPE.isRegexpType());
    assertFalse(ALL_TYPE.isString());
    assertFalse(ALL_TYPE.isStringObjectType());
    assertFalse(ALL_TYPE.isStringValueType());
    assertFalse(ALL_TYPE.isEnumType());
    assertFalse(ALL_TYPE.isUnionType());
    assertTrue(ALL_TYPE.isAllType());
    assertFalse(ALL_TYPE.isVoidType());
    assertFalse(ALL_TYPE.isConstructor());
    assertFalse(ALL_TYPE.isInstanceType());

    
    assertFalse(ALL_TYPE.canAssignTo(NO_TYPE));
    assertFalse(ALL_TYPE.canAssignTo(NO_OBJECT_TYPE));
    assertTrue(ALL_TYPE.canAssignTo(ALL_TYPE));
    assertFalse(ALL_TYPE.canAssignTo(STRING_OBJECT_TYPE));
    assertFalse(ALL_TYPE.canAssignTo(NUMBER_TYPE));
    assertFalse(ALL_TYPE.canAssignTo(functionType));
    assertFalse(ALL_TYPE.canAssignTo(recordType));
    assertFalse(ALL_TYPE.canAssignTo(NULL_TYPE));
    assertFalse(ALL_TYPE.canAssignTo(OBJECT_TYPE));
    assertFalse(ALL_TYPE.canAssignTo(DATE_TYPE));
    assertTrue(ALL_TYPE.canAssignTo(unresolvedNamedType));
    assertFalse(ALL_TYPE.canAssignTo(namedGoogBar));
    assertFalse(ALL_TYPE.canAssignTo(REGEXP_TYPE));
    assertFalse(ALL_TYPE.canAssignTo(VOID_TYPE));
    assertTrue(ALL_TYPE.canAssignTo(UNKNOWN_TYPE));

    
    assertFalse(ALL_TYPE.canBeCalled());

    
    assertCanTestForEqualityWith(ALL_TYPE, ALL_TYPE);
    assertCanTestForEqualityWith(ALL_TYPE, STRING_OBJECT_TYPE);
    assertCanTestForEqualityWith(ALL_TYPE, NUMBER_TYPE);
    assertCanTestForEqualityWith(ALL_TYPE, functionType);
    assertCanTestForEqualityWith(ALL_TYPE, recordType);
    assertCanTestForEqualityWith(ALL_TYPE, VOID_TYPE);
    assertCanTestForEqualityWith(ALL_TYPE, OBJECT_TYPE);
    assertCanTestForEqualityWith(ALL_TYPE, DATE_TYPE);
    assertCanTestForEqualityWith(ALL_TYPE, REGEXP_TYPE);

    
    assertTrue(ALL_TYPE.canTestForShallowEqualityWith(NO_TYPE));
    assertTrue(ALL_TYPE.canTestForShallowEqualityWith(NO_OBJECT_TYPE));
    assertTrue(ALL_TYPE.canTestForShallowEqualityWith(ARRAY_TYPE));
    assertTrue(ALL_TYPE.canTestForShallowEqualityWith(BOOLEAN_TYPE));
    assertTrue(ALL_TYPE.canTestForShallowEqualityWith(BOOLEAN_OBJECT_TYPE));
    assertTrue(ALL_TYPE.canTestForShallowEqualityWith(DATE_TYPE));
    assertTrue(ALL_TYPE.canTestForShallowEqualityWith(ERROR_TYPE));
    assertTrue(ALL_TYPE.canTestForShallowEqualityWith(EVAL_ERROR_TYPE));
    assertTrue(ALL_TYPE.canTestForShallowEqualityWith(functionType));
    assertTrue(ALL_TYPE.canTestForShallowEqualityWith(recordType));
    assertTrue(ALL_TYPE.canTestForShallowEqualityWith(NULL_TYPE));
    assertTrue(ALL_TYPE.canTestForShallowEqualityWith(NUMBER_TYPE));
    assertTrue(ALL_TYPE.canTestForShallowEqualityWith(NUMBER_OBJECT_TYPE));
    assertTrue(ALL_TYPE.canTestForShallowEqualityWith(OBJECT_TYPE));
    assertTrue(ALL_TYPE.canTestForShallowEqualityWith(URI_ERROR_TYPE));
    assertTrue(ALL_TYPE.canTestForShallowEqualityWith(RANGE_ERROR_TYPE));
    assertTrue(ALL_TYPE.canTestForShallowEqualityWith(REFERENCE_ERROR_TYPE));
    assertTrue(ALL_TYPE.canTestForShallowEqualityWith(REGEXP_TYPE));
    assertTrue(ALL_TYPE.canTestForShallowEqualityWith(STRING_TYPE));
    assertTrue(ALL_TYPE.canTestForShallowEqualityWith(STRING_OBJECT_TYPE));
    assertTrue(ALL_TYPE.canTestForShallowEqualityWith(SYNTAX_ERROR_TYPE));
    assertTrue(ALL_TYPE.canTestForShallowEqualityWith(TYPE_ERROR_TYPE));
    assertTrue(ALL_TYPE.canTestForShallowEqualityWith(ALL_TYPE));
    assertTrue(ALL_TYPE.canTestForShallowEqualityWith(VOID_TYPE));

    
    assertFalse(ALL_TYPE.isNullable());

    
    assertTypeEquals(ALL_TYPE,
        ALL_TYPE.getLeastSupertype(ALL_TYPE));
    assertTypeEquals(ALL_TYPE,
        ALL_TYPE.getLeastSupertype(UNKNOWN_TYPE));
    assertTypeEquals(ALL_TYPE,
        ALL_TYPE.getLeastSupertype(STRING_OBJECT_TYPE));
    assertTypeEquals(ALL_TYPE,
        ALL_TYPE.getLeastSupertype(NUMBER_TYPE));
    assertTypeEquals(ALL_TYPE,
        ALL_TYPE.getLeastSupertype(functionType));
    assertTypeEquals(ALL_TYPE,
        ALL_TYPE.getLeastSupertype(OBJECT_TYPE));
    assertTypeEquals(ALL_TYPE,
        ALL_TYPE.getLeastSupertype(DATE_TYPE));
    assertTypeEquals(ALL_TYPE,
        ALL_TYPE.getLeastSupertype(REGEXP_TYPE));

    
    assertFalse(ALL_TYPE.matchesInt32Context());
    assertFalse(ALL_TYPE.matchesNumberContext());
    assertTrue(ALL_TYPE.matchesObjectContext());
    assertTrue(ALL_TYPE.matchesStringContext());
    assertFalse(ALL_TYPE.matchesUint32Context());

    
    assertEquals("*", ALL_TYPE.toString());

    assertTrue(ALL_TYPE.hasDisplayName());
    assertEquals("<Any Type>", ALL_TYPE.getDisplayName());

    Asserts.assertResolvesToSame(ALL_TYPE);
    assertFalse(ALL_TYPE.isNominalConstructor());
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testTheObjectType
  public void testTheObjectType() throws Exception {
    
    assertTypeEquals(OBJECT_PROTOTYPE, OBJECT_TYPE.getImplicitPrototype());

    
    assertFalse(OBJECT_TYPE.isNoObjectType());
    assertFalse(OBJECT_TYPE.isNoType());
    assertFalse(OBJECT_TYPE.isArrayType());
    assertFalse(OBJECT_TYPE.isBooleanValueType());
    assertFalse(OBJECT_TYPE.isDateType());
    assertFalse(OBJECT_TYPE.isEnumElementType());
    assertFalse(OBJECT_TYPE.isNullType());
    assertFalse(OBJECT_TYPE.isNamedType());
    assertFalse(OBJECT_TYPE.isNullType());
    assertFalse(OBJECT_TYPE.isNumber());
    assertFalse(OBJECT_TYPE.isNumberObjectType());
    assertFalse(OBJECT_TYPE.isNumberValueType());
    assertTrue(OBJECT_TYPE.isObject());
    assertFalse(OBJECT_TYPE.isFunctionPrototypeType());
    assertTrue(OBJECT_TYPE.getImplicitPrototype().isFunctionPrototypeType());
    assertFalse(OBJECT_TYPE.isRegexpType());
    assertFalse(OBJECT_TYPE.isString());
    assertFalse(OBJECT_TYPE.isStringObjectType());
    assertFalse(OBJECT_TYPE.isStringValueType());
    assertFalse(OBJECT_TYPE.isEnumType());
    assertFalse(OBJECT_TYPE.isUnionType());
    assertFalse(OBJECT_TYPE.isAllType());
    assertFalse(OBJECT_TYPE.isVoidType());
    assertFalse(OBJECT_TYPE.isConstructor());
    assertTrue(OBJECT_TYPE.isInstanceType());

    
    assertFalse(OBJECT_TYPE.canAssignTo(NO_TYPE));
    assertTrue(OBJECT_TYPE.canAssignTo(ALL_TYPE));
    assertFalse(OBJECT_TYPE.canAssignTo(STRING_OBJECT_TYPE));
    assertFalse(OBJECT_TYPE.canAssignTo(NUMBER_TYPE));
    assertFalse(OBJECT_TYPE.canAssignTo(functionType));
    assertFalse(OBJECT_TYPE.canAssignTo(recordType));
    assertFalse(OBJECT_TYPE.canAssignTo(NULL_TYPE));
    assertTrue(OBJECT_TYPE.canAssignTo(OBJECT_TYPE));
    assertFalse(OBJECT_TYPE.canAssignTo(DATE_TYPE));
    assertFalse(OBJECT_TYPE.canAssignTo(namedGoogBar));
    assertTrue(OBJECT_TYPE.canAssignTo(unresolvedNamedType));
    assertFalse(OBJECT_TYPE.canAssignTo(REGEXP_TYPE));
    assertFalse(OBJECT_TYPE.canAssignTo(ARRAY_TYPE));
    assertTrue(OBJECT_TYPE.canAssignTo(UNKNOWN_TYPE));

    
    assertFalse(OBJECT_TYPE.canBeCalled());

    
    assertCanTestForEqualityWith(OBJECT_TYPE, ALL_TYPE);
    assertCanTestForEqualityWith(OBJECT_TYPE, STRING_OBJECT_TYPE);
    assertCanTestForEqualityWith(OBJECT_TYPE, NUMBER_TYPE);
    assertCanTestForEqualityWith(OBJECT_TYPE, STRING_TYPE);
    assertCanTestForEqualityWith(OBJECT_TYPE, BOOLEAN_TYPE);
    assertCanTestForEqualityWith(OBJECT_TYPE, functionType);
    assertCanTestForEqualityWith(OBJECT_TYPE, recordType);
    assertCannotTestForEqualityWith(OBJECT_TYPE, VOID_TYPE);
    assertCanTestForEqualityWith(OBJECT_TYPE, OBJECT_TYPE);
    assertCanTestForEqualityWith(OBJECT_TYPE, DATE_TYPE);
    assertCanTestForEqualityWith(OBJECT_TYPE, REGEXP_TYPE);
    assertCanTestForEqualityWith(OBJECT_TYPE, ARRAY_TYPE);
    assertCanTestForEqualityWith(OBJECT_TYPE, UNKNOWN_TYPE);

    
    assertTrue(OBJECT_TYPE.canTestForShallowEqualityWith(NO_TYPE));
    assertTrue(OBJECT_TYPE.canTestForShallowEqualityWith(NO_OBJECT_TYPE));
    assertTrue(OBJECT_TYPE.canTestForShallowEqualityWith(ARRAY_TYPE));
    assertFalse(OBJECT_TYPE.canTestForShallowEqualityWith(BOOLEAN_TYPE));
    assertTrue(OBJECT_TYPE.canTestForShallowEqualityWith(BOOLEAN_OBJECT_TYPE));
    assertTrue(OBJECT_TYPE.canTestForShallowEqualityWith(DATE_TYPE));
    assertTrue(OBJECT_TYPE.canTestForShallowEqualityWith(ERROR_TYPE));
    assertTrue(OBJECT_TYPE.canTestForShallowEqualityWith(EVAL_ERROR_TYPE));
    assertTrue(OBJECT_TYPE.canTestForShallowEqualityWith(functionType));
    assertTrue(OBJECT_TYPE.canTestForShallowEqualityWith(recordType));
    assertFalse(OBJECT_TYPE.canTestForShallowEqualityWith(NULL_TYPE));
    assertFalse(OBJECT_TYPE.canTestForShallowEqualityWith(NUMBER_TYPE));
    assertTrue(OBJECT_TYPE.canTestForShallowEqualityWith(NUMBER_OBJECT_TYPE));
    assertTrue(OBJECT_TYPE.canTestForShallowEqualityWith(OBJECT_TYPE));
    assertTrue(OBJECT_TYPE.canTestForShallowEqualityWith(URI_ERROR_TYPE));
    assertTrue(OBJECT_TYPE.canTestForShallowEqualityWith(RANGE_ERROR_TYPE));
    assertTrue(OBJECT_TYPE.
        canTestForShallowEqualityWith(REFERENCE_ERROR_TYPE));
    assertTrue(OBJECT_TYPE.canTestForShallowEqualityWith(REGEXP_TYPE));
    assertFalse(OBJECT_TYPE.canTestForShallowEqualityWith(STRING_TYPE));
    assertTrue(OBJECT_TYPE.canTestForShallowEqualityWith(STRING_OBJECT_TYPE));
    assertTrue(OBJECT_TYPE.canTestForShallowEqualityWith(SYNTAX_ERROR_TYPE));
    assertTrue(OBJECT_TYPE.canTestForShallowEqualityWith(TYPE_ERROR_TYPE));
    assertTrue(OBJECT_TYPE.canTestForShallowEqualityWith(ALL_TYPE));
    assertFalse(OBJECT_TYPE.canTestForShallowEqualityWith(VOID_TYPE));
    assertTrue(OBJECT_TYPE.canTestForShallowEqualityWith(UNKNOWN_TYPE));

    
    assertFalse(OBJECT_TYPE.isNullable());

    
    assertTypeEquals(ALL_TYPE,
        OBJECT_TYPE.getLeastSupertype(ALL_TYPE));
    assertTypeEquals(OBJECT_TYPE,
        OBJECT_TYPE.getLeastSupertype(STRING_OBJECT_TYPE));
    assertTypeEquals(createUnionType(OBJECT_TYPE, NUMBER_TYPE),
        OBJECT_TYPE.getLeastSupertype(NUMBER_TYPE));
    assertTypeEquals(OBJECT_TYPE,
        OBJECT_TYPE.getLeastSupertype(functionType));
    assertTypeEquals(OBJECT_TYPE,
        OBJECT_TYPE.getLeastSupertype(OBJECT_TYPE));
    assertTypeEquals(OBJECT_TYPE,
        OBJECT_TYPE.getLeastSupertype(DATE_TYPE));
    assertTypeEquals(OBJECT_TYPE,
        OBJECT_TYPE.getLeastSupertype(REGEXP_TYPE));

    
    assertEquals(7, OBJECT_TYPE.getPropertiesCount());
    assertReturnTypeEquals(OBJECT_TYPE,
        OBJECT_TYPE.getPropertyType("constructor"));
    assertReturnTypeEquals(STRING_TYPE,
        OBJECT_TYPE.getPropertyType("toString"));
    assertReturnTypeEquals(STRING_TYPE,
        OBJECT_TYPE.getPropertyType("toLocaleString"));
    assertReturnTypeEquals(UNKNOWN_TYPE,
        OBJECT_TYPE.getPropertyType("valueOf"));
    assertReturnTypeEquals(BOOLEAN_TYPE,
        OBJECT_TYPE.getPropertyType("hasOwnProperty"));
    assertReturnTypeEquals(BOOLEAN_TYPE,
        OBJECT_TYPE.getPropertyType("isPrototypeOf"));
    assertReturnTypeEquals(BOOLEAN_TYPE,
        OBJECT_TYPE.getPropertyType("propertyIsEnumerable"));

    
    assertFalse(OBJECT_TYPE.matchesInt32Context());
    assertFalse(OBJECT_TYPE.matchesNumberContext());
    assertTrue(OBJECT_TYPE.matchesObjectContext());
    assertTrue(OBJECT_TYPE.matchesStringContext());
    assertFalse(OBJECT_TYPE.matchesUint32Context());

    
    assertTypeEquals(OBJECT_PROTOTYPE, OBJECT_TYPE.getImplicitPrototype());

    
    assertEquals("Object", OBJECT_TYPE.toString());

    assertTrue(OBJECT_TYPE.isNativeObjectType());
    assertTrue(OBJECT_TYPE.getImplicitPrototype().isNativeObjectType());

    Asserts.assertResolvesToSame(OBJECT_TYPE);
    assertFalse(OBJECT_TYPE.isNominalConstructor());
    assertTrue(OBJECT_TYPE.getConstructor().isNominalConstructor());
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testNumberObjectType
  public void testNumberObjectType() throws Exception {
    
    assertFalse(NUMBER_OBJECT_TYPE.isArrayType());
    assertFalse(NUMBER_OBJECT_TYPE.isBooleanObjectType());
    assertFalse(NUMBER_OBJECT_TYPE.isBooleanValueType());
    assertFalse(NUMBER_OBJECT_TYPE.isDateType());
    assertFalse(NUMBER_OBJECT_TYPE.isEnumElementType());
    assertFalse(NUMBER_OBJECT_TYPE.isNamedType());
    assertFalse(NUMBER_OBJECT_TYPE.isNullType());
    assertTrue(NUMBER_OBJECT_TYPE.isNumber());
    assertTrue(NUMBER_OBJECT_TYPE.isNumberObjectType());
    assertFalse(NUMBER_OBJECT_TYPE.isNumberValueType());
    assertTrue(NUMBER_OBJECT_TYPE.isObject());
    assertFalse(NUMBER_OBJECT_TYPE.isFunctionPrototypeType());
    assertTrue(
        NUMBER_OBJECT_TYPE.getImplicitPrototype().isFunctionPrototypeType());
    assertFalse(NUMBER_OBJECT_TYPE.isRegexpType());
    assertFalse(NUMBER_OBJECT_TYPE.isString());
    assertFalse(NUMBER_OBJECT_TYPE.isStringObjectType());
    assertFalse(NUMBER_OBJECT_TYPE.isStringValueType());
    assertFalse(NUMBER_OBJECT_TYPE.isEnumType());
    assertFalse(NUMBER_OBJECT_TYPE.isUnionType());
    assertFalse(NUMBER_OBJECT_TYPE.isAllType());
    assertFalse(NUMBER_OBJECT_TYPE.isVoidType());
    assertFalse(NUMBER_OBJECT_TYPE.isConstructor());
    assertTrue(NUMBER_OBJECT_TYPE.isInstanceType());

    
    assertTypeEquals(NUMBER_OBJECT_TYPE, NUMBER_TYPE.autoboxesTo());

    
    assertTypeEquals(NUMBER_TYPE, NUMBER_OBJECT_TYPE.unboxesTo());

    
    assertTrue(NUMBER_OBJECT_TYPE.canAssignTo(ALL_TYPE));
    assertFalse(NUMBER_OBJECT_TYPE.canAssignTo(STRING_OBJECT_TYPE));
    assertFalse(NUMBER_OBJECT_TYPE.canAssignTo(NUMBER_TYPE));
    assertFalse(NUMBER_OBJECT_TYPE.canAssignTo(functionType));
    assertFalse(NUMBER_OBJECT_TYPE.canAssignTo(NULL_TYPE));
    assertTrue(NUMBER_OBJECT_TYPE.canAssignTo(OBJECT_TYPE));
    assertFalse(NUMBER_OBJECT_TYPE.canAssignTo(DATE_TYPE));
    assertTrue(NUMBER_OBJECT_TYPE.canAssignTo(unresolvedNamedType));
    assertFalse(NUMBER_OBJECT_TYPE.canAssignTo(namedGoogBar));
    assertTrue(NUMBER_OBJECT_TYPE.canAssignTo(
            createUnionType(NUMBER_OBJECT_TYPE, NULL_TYPE)));
    assertFalse(NUMBER_OBJECT_TYPE.canAssignTo(
            createUnionType(NUMBER_TYPE, NULL_TYPE)));
    assertTrue(NUMBER_OBJECT_TYPE.canAssignTo(UNKNOWN_TYPE));

    
    assertFalse(NUMBER_OBJECT_TYPE.canBeCalled());

    
    assertCanTestForEqualityWith(NUMBER_OBJECT_TYPE, NO_TYPE);
    assertCanTestForEqualityWith(NUMBER_OBJECT_TYPE, NO_OBJECT_TYPE);
    assertCanTestForEqualityWith(NUMBER_OBJECT_TYPE, ALL_TYPE);
    assertCanTestForEqualityWith(NUMBER_OBJECT_TYPE, NUMBER_TYPE);
    assertCanTestForEqualityWith(NUMBER_OBJECT_TYPE, STRING_OBJECT_TYPE);
    assertCanTestForEqualityWith(NUMBER_OBJECT_TYPE, functionType);
    assertCanTestForEqualityWith(NUMBER_OBJECT_TYPE, elementsType);
    assertCannotTestForEqualityWith(NUMBER_OBJECT_TYPE, VOID_TYPE);
    assertCanTestForEqualityWith(NUMBER_OBJECT_TYPE, OBJECT_TYPE);
    assertCanTestForEqualityWith(NUMBER_OBJECT_TYPE, DATE_TYPE);
    assertCanTestForEqualityWith(NUMBER_OBJECT_TYPE, REGEXP_TYPE);
    assertCanTestForEqualityWith(NUMBER_OBJECT_TYPE, ARRAY_TYPE);

    
    assertTrue(NUMBER_OBJECT_TYPE.canTestForShallowEqualityWith(NO_TYPE));
    assertTrue(NUMBER_OBJECT_TYPE.
        canTestForShallowEqualityWith(NO_OBJECT_TYPE));
    assertFalse(NUMBER_OBJECT_TYPE.canTestForShallowEqualityWith(ARRAY_TYPE));
    assertFalse(NUMBER_OBJECT_TYPE.canTestForShallowEqualityWith(BOOLEAN_TYPE));
    assertFalse(NUMBER_OBJECT_TYPE.
        canTestForShallowEqualityWith(BOOLEAN_OBJECT_TYPE));
    assertFalse(NUMBER_OBJECT_TYPE.canTestForShallowEqualityWith(DATE_TYPE));
    assertFalse(NUMBER_OBJECT_TYPE.canTestForShallowEqualityWith(ERROR_TYPE));
    assertFalse(NUMBER_OBJECT_TYPE.
        canTestForShallowEqualityWith(EVAL_ERROR_TYPE));
    assertFalse(NUMBER_OBJECT_TYPE.canTestForShallowEqualityWith(functionType));
    assertFalse(NUMBER_OBJECT_TYPE.canTestForShallowEqualityWith(NULL_TYPE));
    assertFalse(NUMBER_OBJECT_TYPE.canTestForShallowEqualityWith(NUMBER_TYPE));
    assertTrue(NUMBER_OBJECT_TYPE.
        canTestForShallowEqualityWith(NUMBER_OBJECT_TYPE));
    assertTrue(NUMBER_OBJECT_TYPE.canTestForShallowEqualityWith(OBJECT_TYPE));
    assertFalse(NUMBER_OBJECT_TYPE.
        canTestForShallowEqualityWith(URI_ERROR_TYPE));
    assertFalse(NUMBER_OBJECT_TYPE.
        canTestForShallowEqualityWith(RANGE_ERROR_TYPE));
    assertFalse(NUMBER_OBJECT_TYPE.
        canTestForShallowEqualityWith(REFERENCE_ERROR_TYPE));
    assertFalse(NUMBER_OBJECT_TYPE.canTestForShallowEqualityWith(REGEXP_TYPE));
    assertFalse(NUMBER_OBJECT_TYPE.canTestForShallowEqualityWith(STRING_TYPE));
    assertFalse(NUMBER_OBJECT_TYPE.
        canTestForShallowEqualityWith(STRING_OBJECT_TYPE));
    assertFalse(NUMBER_OBJECT_TYPE.
        canTestForShallowEqualityWith(SYNTAX_ERROR_TYPE));
    assertFalse(NUMBER_OBJECT_TYPE.
        canTestForShallowEqualityWith(TYPE_ERROR_TYPE));
    assertTrue(NUMBER_OBJECT_TYPE.canTestForShallowEqualityWith(ALL_TYPE));
    assertFalse(NUMBER_OBJECT_TYPE.canTestForShallowEqualityWith(VOID_TYPE));

    
    assertFalse(NUMBER_OBJECT_TYPE.isNullable());

    
    assertTypeEquals(ALL_TYPE,
        NUMBER_OBJECT_TYPE.getLeastSupertype(ALL_TYPE));
    assertTypeEquals(createUnionType(NUMBER_OBJECT_TYPE, STRING_OBJECT_TYPE),
        NUMBER_OBJECT_TYPE.getLeastSupertype(STRING_OBJECT_TYPE));
    assertTypeEquals(createUnionType(NUMBER_OBJECT_TYPE, NUMBER_TYPE),
        NUMBER_OBJECT_TYPE.getLeastSupertype(NUMBER_TYPE));
    assertTypeEquals(createUnionType(NUMBER_OBJECT_TYPE, functionType),
        NUMBER_OBJECT_TYPE.getLeastSupertype(functionType));
    assertTypeEquals(OBJECT_TYPE,
        NUMBER_OBJECT_TYPE.getLeastSupertype(OBJECT_TYPE));
    assertTypeEquals(createUnionType(NUMBER_OBJECT_TYPE, DATE_TYPE),
        NUMBER_OBJECT_TYPE.getLeastSupertype(DATE_TYPE));
    assertTypeEquals(createUnionType(NUMBER_OBJECT_TYPE, REGEXP_TYPE),
        NUMBER_OBJECT_TYPE.getLeastSupertype(REGEXP_TYPE));

    
    assertTrue(NUMBER_OBJECT_TYPE.matchesInt32Context());
    assertTrue(NUMBER_OBJECT_TYPE.matchesNumberContext());
    assertTrue(NUMBER_OBJECT_TYPE.matchesObjectContext());
    assertTrue(NUMBER_OBJECT_TYPE.matchesStringContext());
    assertTrue(NUMBER_OBJECT_TYPE.matchesUint32Context());

    
    assertEquals("Number", NUMBER_OBJECT_TYPE.toString());
    assertTrue(NUMBER_OBJECT_TYPE.hasDisplayName());
    assertEquals("Number", NUMBER_OBJECT_TYPE.getDisplayName());

    assertTrue(NUMBER_OBJECT_TYPE.isNativeObjectType());

    Asserts.assertResolvesToSame(NUMBER_OBJECT_TYPE);
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testNumberValueType
  public void testNumberValueType() throws Exception {
    
    assertFalse(NUMBER_TYPE.isArrayType());
    assertFalse(NUMBER_TYPE.isBooleanObjectType());
    assertFalse(NUMBER_TYPE.isBooleanValueType());
    assertFalse(NUMBER_TYPE.isDateType());
    assertFalse(NUMBER_TYPE.isEnumElementType());
    assertFalse(NUMBER_TYPE.isNamedType());
    assertFalse(NUMBER_TYPE.isNullType());
    assertTrue(NUMBER_TYPE.isNumber());
    assertFalse(NUMBER_TYPE.isNumberObjectType());
    assertTrue(NUMBER_TYPE.isNumberValueType());
    assertFalse(NUMBER_TYPE.isFunctionPrototypeType());
    assertFalse(NUMBER_TYPE.isRegexpType());
    assertFalse(NUMBER_TYPE.isString());
    assertFalse(NUMBER_TYPE.isStringObjectType());
    assertFalse(NUMBER_TYPE.isStringValueType());
    assertFalse(NUMBER_TYPE.isEnumType());
    assertFalse(NUMBER_TYPE.isUnionType());
    assertFalse(NUMBER_TYPE.isAllType());
    assertFalse(NUMBER_TYPE.isVoidType());
    assertFalse(NUMBER_TYPE.isConstructor());
    assertFalse(NUMBER_TYPE.isInstanceType());

    
    assertTypeEquals(NUMBER_OBJECT_TYPE, NUMBER_TYPE.autoboxesTo());

    
    assertTrue(NUMBER_TYPE.canAssignTo(ALL_TYPE));
    assertFalse(NUMBER_TYPE.canAssignTo(STRING_OBJECT_TYPE));
    assertTrue(NUMBER_TYPE.canAssignTo(NUMBER_TYPE));
    assertFalse(NUMBER_TYPE.canAssignTo(functionType));
    assertFalse(NUMBER_TYPE.canAssignTo(NULL_TYPE));
    assertFalse(NUMBER_TYPE.canAssignTo(OBJECT_TYPE));
    assertFalse(NUMBER_TYPE.canAssignTo(DATE_TYPE));
    assertTrue(NUMBER_TYPE.canAssignTo(unresolvedNamedType));
    assertFalse(NUMBER_TYPE.canAssignTo(namedGoogBar));
    assertTrue(NUMBER_TYPE.canAssignTo(
            createUnionType(NUMBER_TYPE, NULL_TYPE)));
    assertTrue(NUMBER_TYPE.canAssignTo(UNKNOWN_TYPE));

    
    assertFalse(NUMBER_TYPE.canBeCalled());

    
    assertCanTestForEqualityWith(NUMBER_TYPE, NO_TYPE);
    assertCanTestForEqualityWith(NUMBER_TYPE, NO_OBJECT_TYPE);
    assertCanTestForEqualityWith(NUMBER_TYPE, ALL_TYPE);
    assertCanTestForEqualityWith(NUMBER_TYPE, NUMBER_TYPE);
    assertCanTestForEqualityWith(NUMBER_TYPE, STRING_OBJECT_TYPE);
    assertCannotTestForEqualityWith(NUMBER_TYPE, functionType);
    assertCannotTestForEqualityWith(NUMBER_TYPE, VOID_TYPE);
    assertCanTestForEqualityWith(NUMBER_TYPE, OBJECT_TYPE);
    assertCanTestForEqualityWith(NUMBER_TYPE, DATE_TYPE);
    assertCanTestForEqualityWith(NUMBER_TYPE, REGEXP_TYPE);
    assertCanTestForEqualityWith(NUMBER_TYPE, ARRAY_TYPE);
    assertCanTestForEqualityWith(NUMBER_TYPE, UNKNOWN_TYPE);

    
    assertTrue(NUMBER_TYPE.canTestForShallowEqualityWith(NO_TYPE));
    assertFalse(NUMBER_TYPE.canTestForShallowEqualityWith(NO_OBJECT_TYPE));
    assertFalse(NUMBER_TYPE.canTestForShallowEqualityWith(ARRAY_TYPE));
    assertFalse(NUMBER_TYPE.canTestForShallowEqualityWith(BOOLEAN_TYPE));
    assertFalse(NUMBER_TYPE.canTestForShallowEqualityWith(BOOLEAN_OBJECT_TYPE));
    assertFalse(NUMBER_TYPE.canTestForShallowEqualityWith(DATE_TYPE));
    assertFalse(NUMBER_TYPE.canTestForShallowEqualityWith(ERROR_TYPE));
    assertFalse(NUMBER_TYPE.canTestForShallowEqualityWith(EVAL_ERROR_TYPE));
    assertFalse(NUMBER_TYPE.canTestForShallowEqualityWith(functionType));
    assertFalse(NUMBER_TYPE.canTestForShallowEqualityWith(NULL_TYPE));
    assertTrue(NUMBER_TYPE.canTestForShallowEqualityWith(NUMBER_TYPE));
    assertFalse(NUMBER_TYPE.canTestForShallowEqualityWith(NUMBER_OBJECT_TYPE));
    assertFalse(NUMBER_TYPE.canTestForShallowEqualityWith(OBJECT_TYPE));
    assertFalse(NUMBER_TYPE.canTestForShallowEqualityWith(URI_ERROR_TYPE));
    assertFalse(NUMBER_TYPE.canTestForShallowEqualityWith(RANGE_ERROR_TYPE));
    assertFalse(NUMBER_TYPE.
        canTestForShallowEqualityWith(REFERENCE_ERROR_TYPE));
    assertFalse(NUMBER_TYPE.canTestForShallowEqualityWith(REGEXP_TYPE));
    assertFalse(NUMBER_TYPE.canTestForShallowEqualityWith(STRING_TYPE));
    assertFalse(NUMBER_TYPE.canTestForShallowEqualityWith(STRING_OBJECT_TYPE));
    assertFalse(NUMBER_TYPE.canTestForShallowEqualityWith(SYNTAX_ERROR_TYPE));
    assertFalse(NUMBER_TYPE.canTestForShallowEqualityWith(TYPE_ERROR_TYPE));
    assertTrue(NUMBER_TYPE.canTestForShallowEqualityWith(ALL_TYPE));
    assertFalse(NUMBER_TYPE.canTestForShallowEqualityWith(VOID_TYPE));
    assertTrue(NUMBER_TYPE.canTestForShallowEqualityWith(UNKNOWN_TYPE));

    
    assertFalse(NUMBER_TYPE.isNullable());

    
    assertTypeEquals(ALL_TYPE,
        NUMBER_TYPE.getLeastSupertype(ALL_TYPE));
    assertTypeEquals(createUnionType(NUMBER_TYPE, STRING_OBJECT_TYPE),
        NUMBER_TYPE.getLeastSupertype(STRING_OBJECT_TYPE));
    assertTypeEquals(NUMBER_TYPE,
        NUMBER_TYPE.getLeastSupertype(NUMBER_TYPE));
    assertTypeEquals(createUnionType(NUMBER_TYPE, functionType),
        NUMBER_TYPE.getLeastSupertype(functionType));
    assertTypeEquals(createUnionType(NUMBER_TYPE, OBJECT_TYPE),
        NUMBER_TYPE.getLeastSupertype(OBJECT_TYPE));
    assertTypeEquals(createUnionType(NUMBER_TYPE, DATE_TYPE),
        NUMBER_TYPE.getLeastSupertype(DATE_TYPE));
    assertTypeEquals(createUnionType(NUMBER_TYPE, REGEXP_TYPE),
        NUMBER_TYPE.getLeastSupertype(REGEXP_TYPE));

    
    assertTrue(NUMBER_TYPE.matchesInt32Context());
    assertTrue(NUMBER_TYPE.matchesNumberContext());
    assertTrue(NUMBER_TYPE.matchesObjectContext());
    assertTrue(NUMBER_TYPE.matchesStringContext());
    assertTrue(NUMBER_TYPE.matchesUint32Context());

    
    assertEquals("number", NUMBER_TYPE.toString());
    assertTrue(NUMBER_TYPE.hasDisplayName());
    assertEquals("number", NUMBER_TYPE.getDisplayName());

    Asserts.assertResolvesToSame(NUMBER_TYPE);
    assertFalse(NUMBER_TYPE.isNominalConstructor());
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testNullType
  public void testNullType() throws Exception {

    
    assertFalse(NULL_TYPE.isArrayType());
    assertFalse(NULL_TYPE.isBooleanValueType());
    assertFalse(NULL_TYPE.isDateType());
    assertFalse(NULL_TYPE.isEnumElementType());
    assertFalse(NULL_TYPE.isNamedType());
    assertTrue(NULL_TYPE.isNullType());
    assertFalse(NULL_TYPE.isNumber());
    assertFalse(NULL_TYPE.isNumberObjectType());
    assertFalse(NULL_TYPE.isNumberValueType());
    assertFalse(NULL_TYPE.isFunctionPrototypeType());
    assertFalse(NULL_TYPE.isRegexpType());
    assertFalse(NULL_TYPE.isString());
    assertFalse(NULL_TYPE.isStringObjectType());
    assertFalse(NULL_TYPE.isStringValueType());
    assertFalse(NULL_TYPE.isEnumType());
    assertFalse(NULL_TYPE.isUnionType());
    assertFalse(NULL_TYPE.isAllType());
    assertFalse(NULL_TYPE.isVoidType());
    assertFalse(NULL_TYPE.isConstructor());
    assertFalse(NULL_TYPE.isInstanceType());

    
    assertNull(NULL_TYPE.autoboxesTo());

    
    assertFalse(NULL_TYPE.canAssignTo(NO_OBJECT_TYPE));
    assertFalse(NULL_TYPE.canAssignTo(NO_TYPE));
    assertTrue(NULL_TYPE.canAssignTo(NULL_TYPE));
    assertTrue(NULL_TYPE.canAssignTo(ALL_TYPE));
    assertFalse(NULL_TYPE.canAssignTo(STRING_OBJECT_TYPE));
    assertFalse(NULL_TYPE.canAssignTo(NUMBER_TYPE));
    assertFalse(NULL_TYPE.canAssignTo(functionType));
    assertFalse(NULL_TYPE.canAssignTo(OBJECT_TYPE));
    assertFalse(NULL_TYPE.canAssignTo(DATE_TYPE));
    assertFalse(NULL_TYPE.canAssignTo(REGEXP_TYPE));
    assertFalse(NULL_TYPE.canAssignTo(ARRAY_TYPE));
    assertTrue(NULL_TYPE.canAssignTo(UNKNOWN_TYPE));

    assertTrue(NULL_TYPE.canAssignTo(createNullableType(NO_OBJECT_TYPE)));
    assertTrue(NULL_TYPE.canAssignTo(createNullableType(NO_TYPE)));
    assertTrue(NULL_TYPE.canAssignTo(createNullableType(NULL_TYPE)));
    assertTrue(NULL_TYPE.canAssignTo(createNullableType(ALL_TYPE)));
    assertTrue(NULL_TYPE.canAssignTo(createNullableType(STRING_OBJECT_TYPE)));
    assertTrue(NULL_TYPE.canAssignTo(createNullableType(NUMBER_TYPE)));
    assertTrue(NULL_TYPE.canAssignTo(createNullableType(functionType)));
    assertTrue(NULL_TYPE.canAssignTo(createNullableType(OBJECT_TYPE)));
    assertTrue(NULL_TYPE.canAssignTo(createNullableType(DATE_TYPE)));
    assertTrue(NULL_TYPE.canAssignTo(createNullableType(REGEXP_TYPE)));
    assertTrue(NULL_TYPE.canAssignTo(createNullableType(ARRAY_TYPE)));

    
    assertFalse(NULL_TYPE.canBeCalled());

    
    assertCanTestForEqualityWith(NULL_TYPE, NO_TYPE);
    assertCanTestForEqualityWith(NULL_TYPE, NO_OBJECT_TYPE);
    assertCanTestForEqualityWith(NULL_TYPE, ALL_TYPE);
    assertCannotTestForEqualityWith(NULL_TYPE, ARRAY_TYPE);
    assertCannotTestForEqualityWith(NULL_TYPE, BOOLEAN_TYPE);
    assertCannotTestForEqualityWith(NULL_TYPE, BOOLEAN_OBJECT_TYPE);
    assertCannotTestForEqualityWith(NULL_TYPE, DATE_TYPE);
    assertCannotTestForEqualityWith(NULL_TYPE, ERROR_TYPE);
    assertCannotTestForEqualityWith(NULL_TYPE, EVAL_ERROR_TYPE);
    assertCannotTestForEqualityWith(NULL_TYPE, functionType);
    assertCannotTestForEqualityWith(NULL_TYPE, NULL_TYPE);
    assertCannotTestForEqualityWith(NULL_TYPE, NUMBER_TYPE);
    assertCannotTestForEqualityWith(NULL_TYPE, NUMBER_OBJECT_TYPE);
    assertCannotTestForEqualityWith(NULL_TYPE, OBJECT_TYPE);
    assertCannotTestForEqualityWith(NULL_TYPE, URI_ERROR_TYPE);
    assertCannotTestForEqualityWith(NULL_TYPE, RANGE_ERROR_TYPE);
    assertCannotTestForEqualityWith(NULL_TYPE, REFERENCE_ERROR_TYPE);
    assertCannotTestForEqualityWith(NULL_TYPE, REGEXP_TYPE);
    assertCannotTestForEqualityWith(NULL_TYPE, STRING_TYPE);
    assertCannotTestForEqualityWith(NULL_TYPE, STRING_OBJECT_TYPE);
    assertCannotTestForEqualityWith(NULL_TYPE, SYNTAX_ERROR_TYPE);
    assertCannotTestForEqualityWith(NULL_TYPE, TYPE_ERROR_TYPE);
    assertCannotTestForEqualityWith(NULL_TYPE, VOID_TYPE);

    
    assertTrue(NULL_TYPE.canTestForShallowEqualityWith(NO_TYPE));
    assertFalse(NULL_TYPE.canTestForShallowEqualityWith(NO_OBJECT_TYPE));
    assertFalse(NULL_TYPE.canTestForShallowEqualityWith(ARRAY_TYPE));
    assertFalse(NULL_TYPE.canTestForShallowEqualityWith(BOOLEAN_TYPE));
    assertFalse(NULL_TYPE.
        canTestForShallowEqualityWith(BOOLEAN_OBJECT_TYPE));
    assertFalse(NULL_TYPE.canTestForShallowEqualityWith(DATE_TYPE));
    assertFalse(NULL_TYPE.canTestForShallowEqualityWith(ERROR_TYPE));
    assertFalse(NULL_TYPE.canTestForShallowEqualityWith(EVAL_ERROR_TYPE));
    assertFalse(NULL_TYPE.canTestForShallowEqualityWith(functionType));
    assertTrue(NULL_TYPE.canTestForShallowEqualityWith(NULL_TYPE));
    assertFalse(NULL_TYPE.canTestForShallowEqualityWith(NUMBER_TYPE));
    assertFalse(NULL_TYPE.canTestForShallowEqualityWith(NUMBER_OBJECT_TYPE));
    assertFalse(NULL_TYPE.canTestForShallowEqualityWith(OBJECT_TYPE));
    assertFalse(NULL_TYPE.canTestForShallowEqualityWith(URI_ERROR_TYPE));
    assertFalse(NULL_TYPE.canTestForShallowEqualityWith(RANGE_ERROR_TYPE));
    assertFalse(NULL_TYPE.
        canTestForShallowEqualityWith(REFERENCE_ERROR_TYPE));
    assertFalse(NULL_TYPE.canTestForShallowEqualityWith(REGEXP_TYPE));
    assertFalse(NULL_TYPE.canTestForShallowEqualityWith(STRING_TYPE));
    assertFalse(NULL_TYPE.canTestForShallowEqualityWith(STRING_OBJECT_TYPE));
    assertFalse(NULL_TYPE.canTestForShallowEqualityWith(SYNTAX_ERROR_TYPE));
    assertFalse(NULL_TYPE.canTestForShallowEqualityWith(TYPE_ERROR_TYPE));
    assertTrue(NULL_TYPE.canTestForShallowEqualityWith(ALL_TYPE));
    assertFalse(NULL_TYPE.canTestForShallowEqualityWith(VOID_TYPE));
    assertTrue(NULL_TYPE.canTestForShallowEqualityWith(
            createNullableType(STRING_OBJECT_TYPE)));

    
    assertTypeEquals(NULL_TYPE, NULL_TYPE.getLeastSupertype(NULL_TYPE));
    assertTypeEquals(ALL_TYPE, NULL_TYPE.getLeastSupertype(ALL_TYPE));
    assertTypeEquals(createNullableType(STRING_OBJECT_TYPE),
        NULL_TYPE.getLeastSupertype(STRING_OBJECT_TYPE));
    assertTypeEquals(createNullableType(NUMBER_TYPE),
        NULL_TYPE.getLeastSupertype(NUMBER_TYPE));
    assertTypeEquals(createNullableType(functionType),
        NULL_TYPE.getLeastSupertype(functionType));
    assertTypeEquals(createNullableType(OBJECT_TYPE),
        NULL_TYPE.getLeastSupertype(OBJECT_TYPE));
    assertTypeEquals(createNullableType(DATE_TYPE),
        NULL_TYPE.getLeastSupertype(DATE_TYPE));
    assertTypeEquals(createNullableType(REGEXP_TYPE),
        NULL_TYPE.getLeastSupertype(REGEXP_TYPE));

    
    assertTrue(NULL_TYPE.matchesInt32Context());
    assertTrue(NULL_TYPE.matchesNumberContext());
    assertFalse(NULL_TYPE.matchesObjectContext());
    assertTrue(NULL_TYPE.matchesStringContext());
    assertTrue(NULL_TYPE.matchesUint32Context());

    
    assertFalse(NULL_TYPE.matchesObjectContext());

    
    assertEquals("null", NULL_TYPE.toString());
    assertTrue(NULL_TYPE.hasDisplayName());
    assertEquals("null", NULL_TYPE.getDisplayName());

    Asserts.assertResolvesToSame(NULL_TYPE);

    
    assertTrue(
        NULL_TYPE.isSubtype(
            createUnionType(forwardDeclaredNamedType, NULL_TYPE)));
    assertTypeEquals(NULL_TYPE,
        NULL_TYPE.getGreatestSubtype(
            createUnionType(forwardDeclaredNamedType, NULL_TYPE)));
    assertFalse(NULL_TYPE.isNominalConstructor());
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testDateType
  public void testDateType() throws Exception {
    
    assertFalse(DATE_TYPE.isArrayType());
    assertFalse(DATE_TYPE.isBooleanValueType());
    assertTrue(DATE_TYPE.isDateType());
    assertFalse(DATE_TYPE.isEnumElementType());
    assertFalse(DATE_TYPE.isNamedType());
    assertFalse(DATE_TYPE.isNullType());
    assertFalse(DATE_TYPE.isNumberValueType());
    assertFalse(DATE_TYPE.isFunctionPrototypeType());
    assertTrue(DATE_TYPE.getImplicitPrototype().isFunctionPrototypeType());
    assertFalse(DATE_TYPE.isRegexpType());
    assertFalse(DATE_TYPE.isStringValueType());
    assertFalse(DATE_TYPE.isEnumType());
    assertFalse(DATE_TYPE.isUnionType());
    assertFalse(DATE_TYPE.isAllType());
    assertFalse(DATE_TYPE.isVoidType());
    assertFalse(DATE_TYPE.isConstructor());
    assertTrue(DATE_TYPE.isInstanceType());

    
    assertNull(DATE_TYPE.autoboxesTo());

    
    assertFalse(DATE_TYPE.canAssignTo(NO_TYPE));
    assertFalse(DATE_TYPE.canAssignTo(NO_OBJECT_TYPE));
    assertFalse(DATE_TYPE.isSubtype(ARRAY_TYPE));
    assertFalse(DATE_TYPE.canAssignTo(BOOLEAN_TYPE));
    assertFalse(DATE_TYPE.canAssignTo(BOOLEAN_OBJECT_TYPE));
    assertTrue(DATE_TYPE.canAssignTo(DATE_TYPE));
    assertFalse(DATE_TYPE.canAssignTo(ERROR_TYPE));
    assertFalse(DATE_TYPE.canAssignTo(EVAL_ERROR_TYPE));
    assertFalse(DATE_TYPE.canAssignTo(functionType));
    assertFalse(DATE_TYPE.canAssignTo(NULL_TYPE));
    assertFalse(DATE_TYPE.canAssignTo(NUMBER_TYPE));
    assertFalse(DATE_TYPE.canAssignTo(NUMBER_OBJECT_TYPE));
    assertTrue(DATE_TYPE.canAssignTo(OBJECT_TYPE));
    assertFalse(DATE_TYPE.canAssignTo(URI_ERROR_TYPE));
    assertFalse(DATE_TYPE.canAssignTo(RANGE_ERROR_TYPE));
    assertFalse(DATE_TYPE.canAssignTo(REFERENCE_ERROR_TYPE));
    assertFalse(DATE_TYPE.canAssignTo(REGEXP_TYPE));
    assertFalse(DATE_TYPE.canAssignTo(STRING_TYPE));
    assertFalse(DATE_TYPE.canAssignTo(STRING_OBJECT_TYPE));
    assertFalse(DATE_TYPE.canAssignTo(SYNTAX_ERROR_TYPE));
    assertFalse(DATE_TYPE.canAssignTo(TYPE_ERROR_TYPE));
    assertTrue(DATE_TYPE.canAssignTo(ALL_TYPE));
    assertFalse(DATE_TYPE.canAssignTo(VOID_TYPE));

    
    assertFalse(DATE_TYPE.canBeCalled());

    
    assertCanTestForEqualityWith(DATE_TYPE, ALL_TYPE);
    assertCanTestForEqualityWith(DATE_TYPE, STRING_OBJECT_TYPE);
    assertCanTestForEqualityWith(DATE_TYPE, NUMBER_TYPE);
    assertCanTestForEqualityWith(DATE_TYPE, functionType);
    assertCannotTestForEqualityWith(DATE_TYPE, VOID_TYPE);
    assertCanTestForEqualityWith(DATE_TYPE, OBJECT_TYPE);
    assertCanTestForEqualityWith(DATE_TYPE, DATE_TYPE);
    assertCanTestForEqualityWith(DATE_TYPE, REGEXP_TYPE);
    assertCanTestForEqualityWith(DATE_TYPE, ARRAY_TYPE);

    
    assertTrue(DATE_TYPE.canTestForShallowEqualityWith(NO_TYPE));
    assertTrue(DATE_TYPE.canTestForShallowEqualityWith(NO_OBJECT_TYPE));
    assertFalse(DATE_TYPE.canTestForShallowEqualityWith(ARRAY_TYPE));
    assertFalse(DATE_TYPE.canTestForShallowEqualityWith(BOOLEAN_TYPE));
    assertFalse(DATE_TYPE.
        canTestForShallowEqualityWith(BOOLEAN_OBJECT_TYPE));
    assertTrue(DATE_TYPE.canTestForShallowEqualityWith(DATE_TYPE));
    assertFalse(DATE_TYPE.canTestForShallowEqualityWith(ERROR_TYPE));
    assertFalse(DATE_TYPE.canTestForShallowEqualityWith(EVAL_ERROR_TYPE));
    assertFalse(DATE_TYPE.canTestForShallowEqualityWith(functionType));
    assertFalse(DATE_TYPE.canTestForShallowEqualityWith(NULL_TYPE));
    assertFalse(DATE_TYPE.canTestForShallowEqualityWith(NUMBER_TYPE));
    assertFalse(DATE_TYPE.canTestForShallowEqualityWith(NUMBER_OBJECT_TYPE));
    assertTrue(DATE_TYPE.canTestForShallowEqualityWith(OBJECT_TYPE));
    assertFalse(DATE_TYPE.canTestForShallowEqualityWith(URI_ERROR_TYPE));
    assertFalse(DATE_TYPE.canTestForShallowEqualityWith(RANGE_ERROR_TYPE));
    assertFalse(DATE_TYPE.
        canTestForShallowEqualityWith(REFERENCE_ERROR_TYPE));
    assertFalse(DATE_TYPE.canTestForShallowEqualityWith(REGEXP_TYPE));
    assertFalse(DATE_TYPE.canTestForShallowEqualityWith(STRING_TYPE));
    assertFalse(DATE_TYPE.canTestForShallowEqualityWith(STRING_OBJECT_TYPE));
    assertFalse(DATE_TYPE.canTestForShallowEqualityWith(SYNTAX_ERROR_TYPE));
    assertFalse(DATE_TYPE.canTestForShallowEqualityWith(TYPE_ERROR_TYPE));
    assertTrue(DATE_TYPE.canTestForShallowEqualityWith(ALL_TYPE));
    assertFalse(DATE_TYPE.canTestForShallowEqualityWith(VOID_TYPE));

    
    assertFalse(DATE_TYPE.isNullable());
    assertTrue(createNullableType(DATE_TYPE).isNullable());

    
    assertTypeEquals(ALL_TYPE,
        DATE_TYPE.getLeastSupertype(ALL_TYPE));
    assertTypeEquals(createUnionType(DATE_TYPE, STRING_OBJECT_TYPE),
        DATE_TYPE.getLeastSupertype(STRING_OBJECT_TYPE));
    assertTypeEquals(createUnionType(DATE_TYPE, NUMBER_TYPE),
        DATE_TYPE.getLeastSupertype(NUMBER_TYPE));
    assertTypeEquals(createUnionType(DATE_TYPE, functionType),
        DATE_TYPE.getLeastSupertype(functionType));
    assertTypeEquals(OBJECT_TYPE, DATE_TYPE.getLeastSupertype(OBJECT_TYPE));
    assertTypeEquals(DATE_TYPE, DATE_TYPE.getLeastSupertype(DATE_TYPE));
    assertTypeEquals(createUnionType(DATE_TYPE, REGEXP_TYPE),
        DATE_TYPE.getLeastSupertype(REGEXP_TYPE));

    
    assertEquals(46, DATE_TYPE.getImplicitPrototype().getPropertiesCount());
    assertEquals(46, DATE_TYPE.getPropertiesCount());
    assertReturnTypeEquals(DATE_TYPE, DATE_TYPE.getPropertyType("constructor"));
    assertReturnTypeEquals(STRING_TYPE,
        DATE_TYPE.getPropertyType("toString"));
    assertReturnTypeEquals(STRING_TYPE,
        DATE_TYPE.getPropertyType("toDateString"));
    assertReturnTypeEquals(STRING_TYPE,
        DATE_TYPE.getPropertyType("toTimeString"));
    assertReturnTypeEquals(STRING_TYPE,
        DATE_TYPE.getPropertyType("toLocaleString"));
    assertReturnTypeEquals(STRING_TYPE,
        DATE_TYPE.getPropertyType("toLocaleDateString"));
    assertReturnTypeEquals(STRING_TYPE,
        DATE_TYPE.getPropertyType("toLocaleTimeString"));
    assertReturnTypeEquals(NUMBER_TYPE, DATE_TYPE.getPropertyType("valueOf"));
    assertReturnTypeEquals(NUMBER_TYPE, DATE_TYPE.getPropertyType("getTime"));
    assertReturnTypeEquals(NUMBER_TYPE,
        DATE_TYPE.getPropertyType("getFullYear"));
    assertReturnTypeEquals(NUMBER_TYPE,
        DATE_TYPE.getPropertyType("getUTCFullYear"));
    assertReturnTypeEquals(NUMBER_TYPE, DATE_TYPE.getPropertyType("getMonth"));
    assertReturnTypeEquals(NUMBER_TYPE,
        DATE_TYPE.getPropertyType("getUTCMonth"));
    assertReturnTypeEquals(NUMBER_TYPE, DATE_TYPE.getPropertyType("getDate"));
    assertReturnTypeEquals(NUMBER_TYPE,
        DATE_TYPE.getPropertyType("getUTCDate"));
    assertReturnTypeEquals(NUMBER_TYPE, DATE_TYPE.getPropertyType("getDay"));
    assertReturnTypeEquals(NUMBER_TYPE, DATE_TYPE.getPropertyType("getUTCDay"));
    assertReturnTypeEquals(NUMBER_TYPE, DATE_TYPE.getPropertyType("getHours"));
    assertReturnTypeEquals(NUMBER_TYPE,
        DATE_TYPE.getPropertyType("getUTCHours"));
    assertReturnTypeEquals(NUMBER_TYPE,
        DATE_TYPE.getPropertyType("getMinutes"));
    assertReturnTypeEquals(NUMBER_TYPE,
        DATE_TYPE.getPropertyType("getUTCMinutes"));
    assertReturnTypeEquals(NUMBER_TYPE,
        DATE_TYPE.getPropertyType("getSeconds"));
    assertReturnTypeEquals(NUMBER_TYPE,
        DATE_TYPE.getPropertyType("getUTCSeconds"));
    assertReturnTypeEquals(NUMBER_TYPE,
        DATE_TYPE.getPropertyType("getMilliseconds"));
    assertReturnTypeEquals(NUMBER_TYPE,
        DATE_TYPE.getPropertyType("getUTCMilliseconds"));
    assertReturnTypeEquals(NUMBER_TYPE,
        DATE_TYPE.getPropertyType("getTimezoneOffset"));
    assertReturnTypeEquals(NUMBER_TYPE, DATE_TYPE.getPropertyType("setTime"));
    assertReturnTypeEquals(NUMBER_TYPE,
        DATE_TYPE.getPropertyType("setMilliseconds"));
    assertReturnTypeEquals(NUMBER_TYPE,
        DATE_TYPE.getPropertyType("setUTCMilliseconds"));
    assertReturnTypeEquals(NUMBER_TYPE,
        DATE_TYPE.getPropertyType("setSeconds"));
    assertReturnTypeEquals(NUMBER_TYPE,
        DATE_TYPE.getPropertyType("setUTCSeconds"));
    assertReturnTypeEquals(NUMBER_TYPE,
        DATE_TYPE.getPropertyType("setUTCSeconds"));
    assertReturnTypeEquals(NUMBER_TYPE,
        DATE_TYPE.getPropertyType("setMinutes"));
    assertReturnTypeEquals(NUMBER_TYPE,
        DATE_TYPE.getPropertyType("setUTCMinutes"));
    assertReturnTypeEquals(NUMBER_TYPE, DATE_TYPE.getPropertyType("setHours"));
    assertReturnTypeEquals(NUMBER_TYPE,
        DATE_TYPE.getPropertyType("setUTCHours"));
    assertReturnTypeEquals(NUMBER_TYPE, DATE_TYPE.getPropertyType("setDate"));
    assertReturnTypeEquals(NUMBER_TYPE,
        DATE_TYPE.getPropertyType("setUTCDate"));
    assertReturnTypeEquals(NUMBER_TYPE, DATE_TYPE.getPropertyType("setMonth"));
    assertReturnTypeEquals(NUMBER_TYPE,
        DATE_TYPE.getPropertyType("setUTCMonth"));
    assertReturnTypeEquals(NUMBER_TYPE,
        DATE_TYPE.getPropertyType("setFullYear"));
    assertReturnTypeEquals(NUMBER_TYPE,
        DATE_TYPE.getPropertyType("setUTCFullYear"));
    assertReturnTypeEquals(STRING_TYPE,
        DATE_TYPE.getPropertyType("toUTCString"));
    assertReturnTypeEquals(STRING_TYPE,
        DATE_TYPE.getPropertyType("toGMTString"));

    
    assertTrue(DATE_TYPE.matchesInt32Context());
    assertTrue(DATE_TYPE.matchesNumberContext());
    assertTrue(DATE_TYPE.matchesObjectContext());
    assertTrue(DATE_TYPE.matchesStringContext());
    assertTrue(DATE_TYPE.matchesUint32Context());

    
    assertEquals("Date", DATE_TYPE.toString());
    assertTrue(DATE_TYPE.hasDisplayName());
    assertEquals("Date", DATE_TYPE.getDisplayName());

    assertTrue(DATE_TYPE.isNativeObjectType());

    Asserts.assertResolvesToSame(DATE_TYPE);
    assertFalse(DATE_TYPE.isNominalConstructor());
    assertTrue(DATE_TYPE.getConstructor().isNominalConstructor());
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testRegExpType
  public void testRegExpType() throws Exception {
    
    assertFalse(REGEXP_TYPE.isNoType());
    assertFalse(REGEXP_TYPE.isNoObjectType());
    assertFalse(REGEXP_TYPE.isArrayType());
    assertFalse(REGEXP_TYPE.isBooleanValueType());
    assertFalse(REGEXP_TYPE.isDateType());
    assertFalse(REGEXP_TYPE.isEnumElementType());
    assertFalse(REGEXP_TYPE.isNamedType());
    assertFalse(REGEXP_TYPE.isNullType());
    assertFalse(REGEXP_TYPE.isNumberValueType());
    assertFalse(REGEXP_TYPE.isFunctionPrototypeType());
    assertTrue(REGEXP_TYPE.getImplicitPrototype().isFunctionPrototypeType());
    assertTrue(REGEXP_TYPE.isRegexpType());
    assertFalse(REGEXP_TYPE.isStringValueType());
    assertFalse(REGEXP_TYPE.isEnumType());
    assertFalse(REGEXP_TYPE.isUnionType());
    assertFalse(REGEXP_TYPE.isAllType());
    assertFalse(REGEXP_TYPE.isVoidType());

    
    assertNull(REGEXP_TYPE.autoboxesTo());

    
    assertFalse(REGEXP_TYPE.canAssignTo(NO_TYPE));
    assertFalse(REGEXP_TYPE.canAssignTo(NO_OBJECT_TYPE));
    assertFalse(REGEXP_TYPE.canAssignTo(ARRAY_TYPE));
    assertFalse(REGEXP_TYPE.canAssignTo(BOOLEAN_TYPE));
    assertFalse(REGEXP_TYPE.canAssignTo(BOOLEAN_OBJECT_TYPE));
    assertFalse(REGEXP_TYPE.canAssignTo(DATE_TYPE));
    assertFalse(REGEXP_TYPE.canAssignTo(ERROR_TYPE));
    assertFalse(REGEXP_TYPE.canAssignTo(EVAL_ERROR_TYPE));
    assertFalse(REGEXP_TYPE.canAssignTo(functionType));
    assertFalse(REGEXP_TYPE.canAssignTo(NULL_TYPE));
    assertFalse(REGEXP_TYPE.canAssignTo(NUMBER_TYPE));
    assertFalse(REGEXP_TYPE.canAssignTo(NUMBER_OBJECT_TYPE));
    assertTrue(REGEXP_TYPE.canAssignTo(OBJECT_TYPE));
    assertFalse(REGEXP_TYPE.canAssignTo(URI_ERROR_TYPE));
    assertFalse(REGEXP_TYPE.canAssignTo(RANGE_ERROR_TYPE));
    assertFalse(REGEXP_TYPE.canAssignTo(REFERENCE_ERROR_TYPE));
    assertTrue(REGEXP_TYPE.canAssignTo(REGEXP_TYPE));
    assertFalse(REGEXP_TYPE.canAssignTo(STRING_TYPE));
    assertFalse(REGEXP_TYPE.canAssignTo(STRING_OBJECT_TYPE));
    assertFalse(REGEXP_TYPE.canAssignTo(SYNTAX_ERROR_TYPE));
    assertFalse(REGEXP_TYPE.canAssignTo(TYPE_ERROR_TYPE));
    assertTrue(REGEXP_TYPE.canAssignTo(ALL_TYPE));
    assertFalse(REGEXP_TYPE.canAssignTo(VOID_TYPE));

    
    assertTrue(REGEXP_TYPE.canBeCalled());

    
    assertCanTestForEqualityWith(REGEXP_TYPE, ALL_TYPE);
    assertCanTestForEqualityWith(REGEXP_TYPE, STRING_OBJECT_TYPE);
    assertCanTestForEqualityWith(REGEXP_TYPE, NUMBER_TYPE);
    assertCanTestForEqualityWith(REGEXP_TYPE, functionType);
    assertCannotTestForEqualityWith(REGEXP_TYPE, VOID_TYPE);
    assertCanTestForEqualityWith(REGEXP_TYPE, OBJECT_TYPE);
    assertCanTestForEqualityWith(REGEXP_TYPE, DATE_TYPE);
    assertCanTestForEqualityWith(REGEXP_TYPE, REGEXP_TYPE);
    assertCanTestForEqualityWith(REGEXP_TYPE, ARRAY_TYPE);

    
    assertTrue(REGEXP_TYPE.canTestForShallowEqualityWith(NO_TYPE));
    assertTrue(REGEXP_TYPE.canTestForShallowEqualityWith(NO_OBJECT_TYPE));
    assertFalse(REGEXP_TYPE.canTestForShallowEqualityWith(ARRAY_TYPE));
    assertFalse(REGEXP_TYPE.canTestForShallowEqualityWith(BOOLEAN_TYPE));
    assertFalse(REGEXP_TYPE.
        canTestForShallowEqualityWith(BOOLEAN_OBJECT_TYPE));
    assertFalse(REGEXP_TYPE.canTestForShallowEqualityWith(DATE_TYPE));
    assertFalse(REGEXP_TYPE.canTestForShallowEqualityWith(ERROR_TYPE));
    assertFalse(REGEXP_TYPE.canTestForShallowEqualityWith(EVAL_ERROR_TYPE));
    assertFalse(REGEXP_TYPE.canTestForShallowEqualityWith(functionType));
    assertFalse(REGEXP_TYPE.canTestForShallowEqualityWith(NULL_TYPE));
    assertFalse(REGEXP_TYPE.canTestForShallowEqualityWith(NUMBER_TYPE));
    assertFalse(REGEXP_TYPE.canTestForShallowEqualityWith(NUMBER_OBJECT_TYPE));
    assertTrue(REGEXP_TYPE.canTestForShallowEqualityWith(OBJECT_TYPE));
    assertFalse(REGEXP_TYPE.canTestForShallowEqualityWith(URI_ERROR_TYPE));
    assertFalse(REGEXP_TYPE.canTestForShallowEqualityWith(RANGE_ERROR_TYPE));
    assertFalse(REGEXP_TYPE.
        canTestForShallowEqualityWith(REFERENCE_ERROR_TYPE));
    assertTrue(REGEXP_TYPE.canTestForShallowEqualityWith(REGEXP_TYPE));
    assertFalse(REGEXP_TYPE.canTestForShallowEqualityWith(STRING_TYPE));
    assertFalse(REGEXP_TYPE.canTestForShallowEqualityWith(STRING_OBJECT_TYPE));
    assertFalse(REGEXP_TYPE.canTestForShallowEqualityWith(SYNTAX_ERROR_TYPE));
    assertFalse(REGEXP_TYPE.canTestForShallowEqualityWith(TYPE_ERROR_TYPE));
    assertTrue(REGEXP_TYPE.canTestForShallowEqualityWith(ALL_TYPE));
    assertFalse(REGEXP_TYPE.canTestForShallowEqualityWith(VOID_TYPE));

    
    assertFalse(REGEXP_TYPE.isNullable());
    assertTrue(createNullableType(REGEXP_TYPE).isNullable());

    
    assertTypeEquals(ALL_TYPE,
        REGEXP_TYPE.getLeastSupertype(ALL_TYPE));
    assertTypeEquals(createUnionType(REGEXP_TYPE, STRING_OBJECT_TYPE),
        REGEXP_TYPE.getLeastSupertype(STRING_OBJECT_TYPE));
    assertTypeEquals(createUnionType(REGEXP_TYPE, NUMBER_TYPE),
        REGEXP_TYPE.getLeastSupertype(NUMBER_TYPE));
    assertTypeEquals(createUnionType(REGEXP_TYPE, functionType),
        REGEXP_TYPE.getLeastSupertype(functionType));
    assertTypeEquals(OBJECT_TYPE, REGEXP_TYPE.getLeastSupertype(OBJECT_TYPE));
    assertTypeEquals(createUnionType(DATE_TYPE, REGEXP_TYPE),
        REGEXP_TYPE.getLeastSupertype(DATE_TYPE));
    assertTypeEquals(REGEXP_TYPE,
        REGEXP_TYPE.getLeastSupertype(REGEXP_TYPE));

    
    assertEquals(9, REGEXP_TYPE.getImplicitPrototype().getPropertiesCount());
    assertEquals(14, REGEXP_TYPE.getPropertiesCount());
    assertReturnTypeEquals(REGEXP_TYPE,
        REGEXP_TYPE.getPropertyType("constructor"));
    assertReturnTypeEquals(createNullableType(ARRAY_TYPE),
        REGEXP_TYPE.getPropertyType("exec"));
    assertReturnTypeEquals(BOOLEAN_TYPE,
        REGEXP_TYPE.getPropertyType("test"));
    assertReturnTypeEquals(STRING_TYPE,
        REGEXP_TYPE.getPropertyType("toString"));
    assertTypeEquals(STRING_TYPE, REGEXP_TYPE.getPropertyType("source"));
    assertTypeEquals(BOOLEAN_TYPE, REGEXP_TYPE.getPropertyType("global"));
    assertTypeEquals(BOOLEAN_TYPE, REGEXP_TYPE.getPropertyType("ignoreCase"));
    assertTypeEquals(BOOLEAN_TYPE, REGEXP_TYPE.getPropertyType("multiline"));
    assertTypeEquals(NUMBER_TYPE, REGEXP_TYPE.getPropertyType("lastIndex"));

    
    assertFalse(REGEXP_TYPE.matchesInt32Context());
    assertFalse(REGEXP_TYPE.matchesNumberContext());
    assertTrue(REGEXP_TYPE.matchesObjectContext());
    assertTrue(REGEXP_TYPE.matchesStringContext());
    assertFalse(REGEXP_TYPE.matchesUint32Context());

    
    assertEquals("RegExp", REGEXP_TYPE.toString());
    assertTrue(REGEXP_TYPE.hasDisplayName());
    assertEquals("RegExp", REGEXP_TYPE.getDisplayName());

    assertTrue(REGEXP_TYPE.isNativeObjectType());

    Asserts.assertResolvesToSame(REGEXP_TYPE);
    assertFalse(REGEXP_TYPE.isNominalConstructor());
    assertTrue(REGEXP_TYPE.getConstructor().isNominalConstructor());
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testStringObjectType
  public void testStringObjectType() throws Exception {
    
    assertFalse(STRING_OBJECT_TYPE.isArrayType());
    assertFalse(STRING_OBJECT_TYPE.isBooleanObjectType());
    assertFalse(STRING_OBJECT_TYPE.isBooleanValueType());
    assertFalse(STRING_OBJECT_TYPE.isDateType());
    assertFalse(STRING_OBJECT_TYPE.isEnumElementType());
    assertFalse(STRING_OBJECT_TYPE.isNamedType());
    assertFalse(STRING_OBJECT_TYPE.isNullType());
    assertFalse(STRING_OBJECT_TYPE.isNumber());
    assertFalse(STRING_OBJECT_TYPE.isNumberObjectType());
    assertFalse(STRING_OBJECT_TYPE.isNumberValueType());
    assertFalse(STRING_OBJECT_TYPE.isFunctionPrototypeType());
    assertTrue(
        STRING_OBJECT_TYPE.getImplicitPrototype().isFunctionPrototypeType());
    assertFalse(STRING_OBJECT_TYPE.isRegexpType());
    assertTrue(STRING_OBJECT_TYPE.isString());
    assertTrue(STRING_OBJECT_TYPE.isStringObjectType());
    assertFalse(STRING_OBJECT_TYPE.isStringValueType());
    assertFalse(STRING_OBJECT_TYPE.isEnumType());
    assertFalse(STRING_OBJECT_TYPE.isUnionType());
    assertFalse(STRING_OBJECT_TYPE.isAllType());
    assertFalse(STRING_OBJECT_TYPE.isVoidType());
    assertFalse(STRING_OBJECT_TYPE.isConstructor());
    assertTrue(STRING_OBJECT_TYPE.isInstanceType());

    
    assertTypeEquals(STRING_OBJECT_TYPE, STRING_TYPE.autoboxesTo());

    
    assertTypeEquals(STRING_TYPE, STRING_OBJECT_TYPE.unboxesTo());

    
    assertTrue(STRING_OBJECT_TYPE.canAssignTo(ALL_TYPE));
    assertTrue(STRING_OBJECT_TYPE.canAssignTo(STRING_OBJECT_TYPE));
    assertFalse(STRING_OBJECT_TYPE.canAssignTo(STRING_TYPE));
    assertTrue(STRING_OBJECT_TYPE.canAssignTo(OBJECT_TYPE));
    assertFalse(STRING_OBJECT_TYPE.canAssignTo(NUMBER_TYPE));
    assertFalse(STRING_OBJECT_TYPE.canAssignTo(DATE_TYPE));
    assertFalse(STRING_OBJECT_TYPE.canAssignTo(REGEXP_TYPE));
    assertFalse(STRING_OBJECT_TYPE.canAssignTo(ARRAY_TYPE));
    assertFalse(STRING_OBJECT_TYPE.canAssignTo(STRING_TYPE));

    
    assertFalse(STRING_OBJECT_TYPE.canBeCalled());

    
    assertCanTestForEqualityWith(STRING_OBJECT_TYPE, ALL_TYPE);
    assertCanTestForEqualityWith(STRING_OBJECT_TYPE, STRING_OBJECT_TYPE);
    assertCanTestForEqualityWith(STRING_OBJECT_TYPE, STRING_TYPE);
    assertCanTestForEqualityWith(STRING_OBJECT_TYPE, functionType);
    assertCanTestForEqualityWith(STRING_OBJECT_TYPE, OBJECT_TYPE);
    assertCanTestForEqualityWith(STRING_OBJECT_TYPE, NUMBER_TYPE);
    assertCanTestForEqualityWith(STRING_OBJECT_TYPE, BOOLEAN_TYPE);
    assertCanTestForEqualityWith(STRING_OBJECT_TYPE, BOOLEAN_OBJECT_TYPE);
    assertCanTestForEqualityWith(STRING_OBJECT_TYPE, DATE_TYPE);
    assertCanTestForEqualityWith(STRING_OBJECT_TYPE, REGEXP_TYPE);
    assertCanTestForEqualityWith(STRING_OBJECT_TYPE, ARRAY_TYPE);
    assertCanTestForEqualityWith(STRING_OBJECT_TYPE, UNKNOWN_TYPE);

    
    assertTrue(STRING_OBJECT_TYPE.canTestForShallowEqualityWith(NO_TYPE));
    assertTrue(STRING_OBJECT_TYPE.
        canTestForShallowEqualityWith(NO_OBJECT_TYPE));
    assertFalse(STRING_OBJECT_TYPE.canTestForShallowEqualityWith(ARRAY_TYPE));
    assertFalse(STRING_OBJECT_TYPE.canTestForShallowEqualityWith(BOOLEAN_TYPE));
    assertFalse(STRING_OBJECT_TYPE.
        canTestForShallowEqualityWith(BOOLEAN_OBJECT_TYPE));
    assertFalse(STRING_OBJECT_TYPE.canTestForShallowEqualityWith(DATE_TYPE));
    assertFalse(STRING_OBJECT_TYPE.canTestForShallowEqualityWith(ERROR_TYPE));
    assertFalse(STRING_OBJECT_TYPE.
        canTestForShallowEqualityWith(EVAL_ERROR_TYPE));
    assertFalse(STRING_OBJECT_TYPE.canTestForShallowEqualityWith(functionType));
    assertFalse(STRING_OBJECT_TYPE.canTestForShallowEqualityWith(NULL_TYPE));
    assertFalse(STRING_OBJECT_TYPE.canTestForShallowEqualityWith(NUMBER_TYPE));
    assertFalse(STRING_OBJECT_TYPE.
        canTestForShallowEqualityWith(NUMBER_OBJECT_TYPE));
    assertTrue(STRING_OBJECT_TYPE.canTestForShallowEqualityWith(OBJECT_TYPE));
    assertFalse(STRING_OBJECT_TYPE.
        canTestForShallowEqualityWith(URI_ERROR_TYPE));
    assertFalse(STRING_OBJECT_TYPE.
        canTestForShallowEqualityWith(RANGE_ERROR_TYPE));
    assertFalse(STRING_OBJECT_TYPE.
        canTestForShallowEqualityWith(REFERENCE_ERROR_TYPE));
    assertFalse(STRING_OBJECT_TYPE.canTestForShallowEqualityWith(REGEXP_TYPE));
    assertFalse(STRING_OBJECT_TYPE.canTestForShallowEqualityWith(STRING_TYPE));
    assertTrue(STRING_OBJECT_TYPE.
        canTestForShallowEqualityWith(STRING_OBJECT_TYPE));
    assertFalse(STRING_OBJECT_TYPE.
        canTestForShallowEqualityWith(SYNTAX_ERROR_TYPE));
    assertFalse(STRING_OBJECT_TYPE.
        canTestForShallowEqualityWith(TYPE_ERROR_TYPE));
    assertTrue(STRING_OBJECT_TYPE.canTestForShallowEqualityWith(ALL_TYPE));
    assertFalse(STRING_OBJECT_TYPE.canTestForShallowEqualityWith(VOID_TYPE));
    assertTrue(STRING_OBJECT_TYPE.canTestForShallowEqualityWith(UNKNOWN_TYPE));

    
    assertEquals(23, STRING_OBJECT_TYPE.getImplicitPrototype().
        getPropertiesCount());
    assertEquals(24, STRING_OBJECT_TYPE.getPropertiesCount());

    assertReturnTypeEquals(STRING_TYPE,
        STRING_OBJECT_TYPE.getPropertyType("toString"));
    assertReturnTypeEquals(STRING_TYPE,
        STRING_OBJECT_TYPE.getPropertyType("valueOf"));
    assertReturnTypeEquals(STRING_TYPE,
        STRING_OBJECT_TYPE.getPropertyType("charAt"));
    assertReturnTypeEquals(NUMBER_TYPE,
        STRING_OBJECT_TYPE.getPropertyType("charCodeAt"));
    assertReturnTypeEquals(STRING_TYPE,
        STRING_OBJECT_TYPE.getPropertyType("concat"));
    assertReturnTypeEquals(NUMBER_TYPE,
        STRING_OBJECT_TYPE.getPropertyType("indexOf"));
    assertReturnTypeEquals(NUMBER_TYPE,
        STRING_OBJECT_TYPE.getPropertyType("lastIndexOf"));
    assertReturnTypeEquals(NUMBER_TYPE,
        STRING_OBJECT_TYPE.getPropertyType("localeCompare"));
    assertReturnTypeEquals(createNullableType(ARRAY_TYPE),
        STRING_OBJECT_TYPE.getPropertyType("match"));
    assertReturnTypeEquals(STRING_TYPE,
        STRING_OBJECT_TYPE.getPropertyType("replace"));
    assertReturnTypeEquals(NUMBER_TYPE,
        STRING_OBJECT_TYPE.getPropertyType("search"));
    assertReturnTypeEquals(STRING_TYPE,
        STRING_OBJECT_TYPE.getPropertyType("slice"));
    assertReturnTypeEquals(ARRAY_TYPE,
        STRING_OBJECT_TYPE.getPropertyType("split"));
    assertReturnTypeEquals(STRING_TYPE,
        STRING_OBJECT_TYPE.getPropertyType("substring"));
    assertReturnTypeEquals(STRING_TYPE,
        STRING_OBJECT_TYPE.getPropertyType("toLowerCase"));
    assertReturnTypeEquals(STRING_TYPE,
        STRING_OBJECT_TYPE.getPropertyType("toLocaleLowerCase"));
    assertReturnTypeEquals(STRING_TYPE,
        STRING_OBJECT_TYPE.getPropertyType("toUpperCase"));
    assertReturnTypeEquals(STRING_TYPE,
        STRING_OBJECT_TYPE.getPropertyType("toLocaleUpperCase"));
    assertTypeEquals(NUMBER_TYPE, STRING_OBJECT_TYPE.getPropertyType("length"));

    
    assertTrue(STRING_OBJECT_TYPE.matchesInt32Context());
    assertTrue(STRING_OBJECT_TYPE.matchesNumberContext());
    assertTrue(STRING_OBJECT_TYPE.matchesObjectContext());
    assertTrue(STRING_OBJECT_TYPE.matchesStringContext());
    assertTrue(STRING_OBJECT_TYPE.matchesUint32Context());

    
    assertFalse(STRING_OBJECT_TYPE.isNullable());
    assertTrue(createNullableType(STRING_OBJECT_TYPE).isNullable());

    assertTrue(STRING_OBJECT_TYPE.isNativeObjectType());

    Asserts.assertResolvesToSame(STRING_OBJECT_TYPE);

    assertTrue(STRING_OBJECT_TYPE.hasDisplayName());
    assertEquals("String", STRING_OBJECT_TYPE.getDisplayName());
    assertFalse(STRING_OBJECT_TYPE.isNominalConstructor());
    assertTrue(STRING_OBJECT_TYPE.getConstructor().isNominalConstructor());
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testStringValueType
  public void testStringValueType() throws Exception {
    
    assertFalse(STRING_TYPE.isArrayType());
    assertFalse(STRING_TYPE.isBooleanObjectType());
    assertFalse(STRING_TYPE.isBooleanValueType());
    assertFalse(STRING_TYPE.isDateType());
    assertFalse(STRING_TYPE.isEnumElementType());
    assertFalse(STRING_TYPE.isNamedType());
    assertFalse(STRING_TYPE.isNullType());
    assertFalse(STRING_TYPE.isNumber());
    assertFalse(STRING_TYPE.isNumberObjectType());
    assertFalse(STRING_TYPE.isNumberValueType());
    assertFalse(STRING_TYPE.isFunctionPrototypeType());
    assertFalse(STRING_TYPE.isRegexpType());
    assertTrue(STRING_TYPE.isString());
    assertFalse(STRING_TYPE.isStringObjectType());
    assertTrue(STRING_TYPE.isStringValueType());
    assertFalse(STRING_TYPE.isEnumType());
    assertFalse(STRING_TYPE.isUnionType());
    assertFalse(STRING_TYPE.isAllType());
    assertFalse(STRING_TYPE.isVoidType());
    assertFalse(STRING_TYPE.isConstructor());
    assertFalse(STRING_TYPE.isInstanceType());

    
    assertTypeEquals(STRING_OBJECT_TYPE, STRING_TYPE.autoboxesTo());

    
    assertTypeEquals(STRING_TYPE, STRING_OBJECT_TYPE.unboxesTo());

    
    assertTrue(STRING_TYPE.canAssignTo(ALL_TYPE));
    assertFalse(STRING_TYPE.canAssignTo(STRING_OBJECT_TYPE));
    assertFalse(STRING_TYPE.canAssignTo(NUMBER_TYPE));
    assertFalse(STRING_TYPE.canAssignTo(OBJECT_TYPE));
    assertFalse(STRING_TYPE.canAssignTo(NUMBER_TYPE));
    assertFalse(STRING_TYPE.canAssignTo(DATE_TYPE));
    assertFalse(STRING_TYPE.canAssignTo(REGEXP_TYPE));
    assertFalse(STRING_TYPE.canAssignTo(ARRAY_TYPE));
    assertTrue(STRING_TYPE.canAssignTo(STRING_TYPE));
    assertTrue(STRING_TYPE.canAssignTo(UNKNOWN_TYPE));

    
    assertFalse(STRING_TYPE.canBeCalled());

    
    assertCanTestForEqualityWith(STRING_TYPE, ALL_TYPE);
    assertCanTestForEqualityWith(STRING_TYPE, STRING_OBJECT_TYPE);
    assertCannotTestForEqualityWith(STRING_TYPE, functionType);
    assertCanTestForEqualityWith(STRING_TYPE, OBJECT_TYPE);
    assertCanTestForEqualityWith(STRING_TYPE, NUMBER_TYPE);
    assertCanTestForEqualityWith(STRING_TYPE, BOOLEAN_TYPE);
    assertCanTestForEqualityWith(STRING_TYPE, BOOLEAN_OBJECT_TYPE);
    assertCanTestForEqualityWith(STRING_TYPE, DATE_TYPE);
    assertCanTestForEqualityWith(STRING_TYPE, REGEXP_TYPE);
    assertCanTestForEqualityWith(STRING_TYPE, ARRAY_TYPE);
    assertCanTestForEqualityWith(STRING_TYPE, UNKNOWN_TYPE);

    
    assertTrue(STRING_TYPE.canTestForShallowEqualityWith(NO_TYPE));
    assertFalse(STRING_TYPE.canTestForShallowEqualityWith(NO_OBJECT_TYPE));
    assertFalse(STRING_TYPE.canTestForShallowEqualityWith(ARRAY_TYPE));
    assertFalse(STRING_TYPE.canTestForShallowEqualityWith(BOOLEAN_TYPE));
    assertFalse(STRING_TYPE.canTestForShallowEqualityWith(BOOLEAN_OBJECT_TYPE));
    assertFalse(STRING_TYPE.canTestForShallowEqualityWith(DATE_TYPE));
    assertFalse(STRING_TYPE.canTestForShallowEqualityWith(ERROR_TYPE));
    assertFalse(STRING_TYPE.canTestForShallowEqualityWith(EVAL_ERROR_TYPE));
    assertFalse(STRING_TYPE.canTestForShallowEqualityWith(functionType));
    assertFalse(STRING_TYPE.canTestForShallowEqualityWith(NULL_TYPE));
    assertFalse(STRING_TYPE.canTestForShallowEqualityWith(NUMBER_TYPE));
    assertFalse(STRING_TYPE.canTestForShallowEqualityWith(NUMBER_OBJECT_TYPE));
    assertFalse(STRING_TYPE.canTestForShallowEqualityWith(OBJECT_TYPE));
    assertFalse(STRING_TYPE.canTestForShallowEqualityWith(URI_ERROR_TYPE));
    assertFalse(STRING_TYPE.canTestForShallowEqualityWith(RANGE_ERROR_TYPE));
    assertFalse(STRING_TYPE.
        canTestForShallowEqualityWith(REFERENCE_ERROR_TYPE));
    assertFalse(STRING_TYPE.canTestForShallowEqualityWith(REGEXP_TYPE));
    assertTrue(STRING_TYPE.canTestForShallowEqualityWith(STRING_TYPE));
    assertFalse(STRING_TYPE.canTestForShallowEqualityWith(STRING_OBJECT_TYPE));
    assertFalse(STRING_TYPE.canTestForShallowEqualityWith(SYNTAX_ERROR_TYPE));
    assertFalse(STRING_TYPE.canTestForShallowEqualityWith(TYPE_ERROR_TYPE));
    assertTrue(STRING_TYPE.canTestForShallowEqualityWith(ALL_TYPE));
    assertFalse(STRING_TYPE.canTestForShallowEqualityWith(VOID_TYPE));
    assertTrue(STRING_TYPE.canTestForShallowEqualityWith(UNKNOWN_TYPE));

    
    assertTrue(STRING_TYPE.matchesInt32Context());
    assertTrue(STRING_TYPE.matchesNumberContext());
    assertTrue(STRING_TYPE.matchesObjectContext());
    assertTrue(STRING_TYPE.matchesStringContext());
    assertTrue(STRING_TYPE.matchesUint32Context());

    
    assertFalse(STRING_TYPE.isNullable());
    assertTrue(createNullableType(STRING_TYPE).isNullable());

    
    assertEquals("string", STRING_TYPE.toString());
    assertTrue(STRING_TYPE.hasDisplayName());
    assertEquals("string", STRING_TYPE.getDisplayName());

    
    assertTypeEquals(NUMBER_TYPE, STRING_TYPE.findPropertyType("length"));
    assertEquals(null, STRING_TYPE.findPropertyType("unknownProperty"));

    Asserts.assertResolvesToSame(STRING_TYPE);
    assertFalse(STRING_TYPE.isNominalConstructor());
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testRecordType
  public void testRecordType() throws Exception {
    
    assertTrue(recordType.isObject());
    assertFalse(recordType.isFunctionPrototypeType());

    
    assertTrue(recordType.canAssignTo(ALL_TYPE));
    assertFalse(recordType.canAssignTo(STRING_OBJECT_TYPE));
    assertFalse(recordType.canAssignTo(NUMBER_TYPE));
    assertFalse(recordType.canAssignTo(DATE_TYPE));
    assertFalse(recordType.canAssignTo(REGEXP_TYPE));
    assertTrue(recordType.canAssignTo(UNKNOWN_TYPE));
    assertTrue(recordType.canAssignTo(OBJECT_TYPE));
    assertFalse(recordType.canAssignTo(U2U_CONSTRUCTOR_TYPE));

    
    assertNull(recordType.autoboxesTo());

    
    assertFalse(recordType.canBeCalled());

    
    assertCanTestForEqualityWith(recordType, ALL_TYPE);
    assertCanTestForEqualityWith(recordType, STRING_OBJECT_TYPE);
    assertCanTestForEqualityWith(recordType, recordType);
    assertCanTestForEqualityWith(recordType, functionType);
    assertCanTestForEqualityWith(recordType, OBJECT_TYPE);
    assertCanTestForEqualityWith(recordType, NUMBER_TYPE);
    assertCanTestForEqualityWith(recordType, DATE_TYPE);
    assertCanTestForEqualityWith(recordType, REGEXP_TYPE);

    
    assertTrue(recordType.canTestForShallowEqualityWith(NO_TYPE));
    assertTrue(recordType.canTestForShallowEqualityWith(NO_OBJECT_TYPE));
    assertFalse(recordType.canTestForShallowEqualityWith(ARRAY_TYPE));
    assertFalse(recordType.canTestForShallowEqualityWith(BOOLEAN_TYPE));
    assertFalse(recordType.
        canTestForShallowEqualityWith(BOOLEAN_OBJECT_TYPE));
    assertFalse(recordType.canTestForShallowEqualityWith(DATE_TYPE));
    assertFalse(recordType.canTestForShallowEqualityWith(ERROR_TYPE));
    assertFalse(recordType.canTestForShallowEqualityWith(EVAL_ERROR_TYPE));
    assertTrue(recordType.canTestForShallowEqualityWith(recordType));
    assertFalse(recordType.canTestForShallowEqualityWith(NULL_TYPE));
    assertFalse(recordType.canTestForShallowEqualityWith(NUMBER_TYPE));
    assertFalse(recordType.canTestForShallowEqualityWith(NUMBER_OBJECT_TYPE));
    assertTrue(recordType.canTestForShallowEqualityWith(OBJECT_TYPE));
    assertFalse(recordType.canTestForShallowEqualityWith(URI_ERROR_TYPE));
    assertFalse(recordType.canTestForShallowEqualityWith(RANGE_ERROR_TYPE));
    assertFalse(recordType.
        canTestForShallowEqualityWith(REFERENCE_ERROR_TYPE));
    assertFalse(recordType.canTestForShallowEqualityWith(REGEXP_TYPE));
    assertFalse(recordType.canTestForShallowEqualityWith(STRING_TYPE));
    assertFalse(recordType.canTestForShallowEqualityWith(STRING_OBJECT_TYPE));
    assertFalse(recordType.canTestForShallowEqualityWith(SYNTAX_ERROR_TYPE));
    assertFalse(recordType.canTestForShallowEqualityWith(TYPE_ERROR_TYPE));
    assertTrue(recordType.canTestForShallowEqualityWith(ALL_TYPE));
    assertFalse(recordType.canTestForShallowEqualityWith(VOID_TYPE));
    assertTrue(recordType.canTestForShallowEqualityWith(UNKNOWN_TYPE));

    
    assertFalse(recordType.matchesInt32Context());
    assertFalse(recordType.matchesNumberContext());
    assertTrue(recordType.matchesObjectContext());
    assertFalse(recordType.matchesStringContext());
    assertFalse(recordType.matchesUint32Context());

    Asserts.assertResolvesToSame(recordType);
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testFunctionInstanceType
  public void testFunctionInstanceType() throws Exception {
    FunctionType functionInst = FUNCTION_INSTANCE_TYPE;

    
    assertTrue(functionInst.isObject());
    assertFalse(functionInst.isFunctionPrototypeType());
    assertTrue(functionInst.getImplicitPrototype()
        .isFunctionPrototypeType());

    
    assertTrue(functionInst.canAssignTo(ALL_TYPE));
    assertFalse(functionInst.canAssignTo(STRING_OBJECT_TYPE));
    assertFalse(functionInst.canAssignTo(NUMBER_TYPE));
    assertFalse(functionInst.canAssignTo(DATE_TYPE));
    assertFalse(functionInst.canAssignTo(REGEXP_TYPE));
    assertTrue(functionInst.canAssignTo(UNKNOWN_TYPE));
    assertTrue(functionInst.canAssignTo(U2U_CONSTRUCTOR_TYPE));

    
    assertNull(functionInst.autoboxesTo());

    
    assertTrue(functionInst.canBeCalled());

    
    assertCanTestForEqualityWith(functionInst, ALL_TYPE);
    assertCanTestForEqualityWith(functionInst, STRING_OBJECT_TYPE);
    assertCanTestForEqualityWith(functionInst, functionInst);
    assertCanTestForEqualityWith(functionInst, OBJECT_TYPE);
    assertCannotTestForEqualityWith(functionInst, NUMBER_TYPE);
    assertCanTestForEqualityWith(functionInst, DATE_TYPE);
    assertCanTestForEqualityWith(functionInst, REGEXP_TYPE);

    
    assertTrue(functionInst.canTestForShallowEqualityWith(NO_TYPE));
    assertTrue(functionInst.canTestForShallowEqualityWith(NO_OBJECT_TYPE));
    assertFalse(functionInst.canTestForShallowEqualityWith(ARRAY_TYPE));
    assertFalse(functionInst.canTestForShallowEqualityWith(BOOLEAN_TYPE));
    assertFalse(functionInst.
        canTestForShallowEqualityWith(BOOLEAN_OBJECT_TYPE));
    assertFalse(functionInst.canTestForShallowEqualityWith(DATE_TYPE));
    assertFalse(functionInst.canTestForShallowEqualityWith(ERROR_TYPE));
    assertFalse(functionInst.canTestForShallowEqualityWith(EVAL_ERROR_TYPE));
    assertTrue(functionInst.canTestForShallowEqualityWith(functionInst));
    assertFalse(functionInst.canTestForShallowEqualityWith(NULL_TYPE));
    assertFalse(functionInst.canTestForShallowEqualityWith(NUMBER_TYPE));
    assertFalse(functionInst.canTestForShallowEqualityWith(NUMBER_OBJECT_TYPE));
    assertTrue(functionInst.canTestForShallowEqualityWith(OBJECT_TYPE));
    assertFalse(functionInst.canTestForShallowEqualityWith(URI_ERROR_TYPE));
    assertFalse(functionInst.canTestForShallowEqualityWith(RANGE_ERROR_TYPE));
    assertFalse(functionInst.
        canTestForShallowEqualityWith(REFERENCE_ERROR_TYPE));
    assertFalse(functionInst.canTestForShallowEqualityWith(REGEXP_TYPE));
    assertFalse(functionInst.canTestForShallowEqualityWith(STRING_TYPE));
    assertFalse(functionInst.canTestForShallowEqualityWith(STRING_OBJECT_TYPE));
    assertFalse(functionInst.canTestForShallowEqualityWith(SYNTAX_ERROR_TYPE));
    assertFalse(functionInst.canTestForShallowEqualityWith(TYPE_ERROR_TYPE));
    assertTrue(functionInst.canTestForShallowEqualityWith(ALL_TYPE));
    assertFalse(functionInst.canTestForShallowEqualityWith(VOID_TYPE));
    assertTrue(functionInst.canTestForShallowEqualityWith(UNKNOWN_TYPE));

    
    assertFalse(functionInst.matchesInt32Context());
    assertFalse(functionInst.matchesNumberContext());
    assertTrue(functionInst.matchesObjectContext());
    assertFalse(functionInst.matchesStringContext());
    assertFalse(functionInst.matchesUint32Context());

    
    assertTrue(functionInst.hasProperty("prototype"));
    assertPropertyTypeInferred(functionInst, "prototype");

    
    assertTypeEquals(FUNCTION_FUNCTION_TYPE, functionInst.getConstructor());
    assertTypeEquals(FUNCTION_PROTOTYPE, functionInst.getImplicitPrototype());
    assertTypeEquals(functionInst, FUNCTION_FUNCTION_TYPE.getInstanceType());

    Asserts.assertResolvesToSame(functionInst);
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testFunctionType
  public void testFunctionType() throws Exception {
    
    assertTrue(functionType.isObject());
    assertFalse(functionType.isFunctionPrototypeType());
    assertTrue(functionType.getImplicitPrototype().getImplicitPrototype()
        .isFunctionPrototypeType());

    
    assertTrue(functionType.canAssignTo(ALL_TYPE));
    assertFalse(functionType.canAssignTo(STRING_OBJECT_TYPE));
    assertFalse(functionType.canAssignTo(NUMBER_TYPE));
    assertFalse(functionType.canAssignTo(DATE_TYPE));
    assertFalse(functionType.canAssignTo(REGEXP_TYPE));
    assertTrue(functionType.canAssignTo(UNKNOWN_TYPE));
    assertTrue(functionType.canAssignTo(U2U_CONSTRUCTOR_TYPE));

    
    assertNull(functionType.autoboxesTo());

    
    assertTrue(functionType.canBeCalled());

    
    assertCanTestForEqualityWith(functionType, ALL_TYPE);
    assertCanTestForEqualityWith(functionType, STRING_OBJECT_TYPE);
    assertCanTestForEqualityWith(functionType, functionType);
    assertCanTestForEqualityWith(functionType, OBJECT_TYPE);
    assertCannotTestForEqualityWith(functionType, NUMBER_TYPE);
    assertCanTestForEqualityWith(functionType, DATE_TYPE);
    assertCanTestForEqualityWith(functionType, REGEXP_TYPE);

    
    assertTrue(functionType.canTestForShallowEqualityWith(NO_TYPE));
    assertTrue(functionType.canTestForShallowEqualityWith(NO_OBJECT_TYPE));
    assertFalse(functionType.canTestForShallowEqualityWith(ARRAY_TYPE));
    assertFalse(functionType.canTestForShallowEqualityWith(BOOLEAN_TYPE));
    assertFalse(functionType.
        canTestForShallowEqualityWith(BOOLEAN_OBJECT_TYPE));
    assertFalse(functionType.canTestForShallowEqualityWith(DATE_TYPE));
    assertFalse(functionType.canTestForShallowEqualityWith(ERROR_TYPE));
    assertFalse(functionType.canTestForShallowEqualityWith(EVAL_ERROR_TYPE));
    assertTrue(functionType.canTestForShallowEqualityWith(functionType));
    assertFalse(functionType.canTestForShallowEqualityWith(NULL_TYPE));
    assertFalse(functionType.canTestForShallowEqualityWith(NUMBER_TYPE));
    assertFalse(functionType.canTestForShallowEqualityWith(NUMBER_OBJECT_TYPE));
    assertTrue(functionType.canTestForShallowEqualityWith(OBJECT_TYPE));
    assertFalse(functionType.canTestForShallowEqualityWith(URI_ERROR_TYPE));
    assertFalse(functionType.canTestForShallowEqualityWith(RANGE_ERROR_TYPE));
    assertFalse(functionType.
        canTestForShallowEqualityWith(REFERENCE_ERROR_TYPE));
    assertFalse(functionType.canTestForShallowEqualityWith(REGEXP_TYPE));
    assertFalse(functionType.canTestForShallowEqualityWith(STRING_TYPE));
    assertFalse(functionType.canTestForShallowEqualityWith(STRING_OBJECT_TYPE));
    assertFalse(functionType.canTestForShallowEqualityWith(SYNTAX_ERROR_TYPE));
    assertFalse(functionType.canTestForShallowEqualityWith(TYPE_ERROR_TYPE));
    assertTrue(functionType.canTestForShallowEqualityWith(ALL_TYPE));
    assertFalse(functionType.canTestForShallowEqualityWith(VOID_TYPE));
    assertTrue(functionType.canTestForShallowEqualityWith(UNKNOWN_TYPE));

    
    assertFalse(functionType.matchesInt32Context());
    assertFalse(functionType.matchesNumberContext());
    assertTrue(functionType.matchesObjectContext());
    assertFalse(functionType.matchesStringContext());
    assertFalse(functionType.matchesUint32Context());

    
    assertTrue(functionType.hasProperty("prototype"));
    assertPropertyTypeInferred(functionType, "prototype");

    Asserts.assertResolvesToSame(functionType);

    assertEquals("aFunctionName", new FunctionBuilder(registry).
        withName("aFunctionName").build().getDisplayName());
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testRecordTypeSubtyping
  public void testRecordTypeSubtyping() {
    RecordTypeBuilder builder = new RecordTypeBuilder(registry);
    builder.addProperty("a", NUMBER_TYPE, null);
    builder.addProperty("b", STRING_TYPE, null);
    builder.addProperty("c", STRING_TYPE, null);
    JSType subRecordType = builder.build();

    assertTrue(subRecordType.isSubtype(recordType));
    assertFalse(recordType.isSubtype(subRecordType));

    builder = new RecordTypeBuilder(registry);
    builder.addProperty("a", OBJECT_TYPE, null);
    builder.addProperty("b", STRING_TYPE, null);
    JSType differentRecordType = builder.build();

    assertFalse(differentRecordType.isSubtype(recordType));
    assertFalse(recordType.isSubtype(differentRecordType));
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testRecordTypeSubtypingWithInferredProperties
  public void testRecordTypeSubtypingWithInferredProperties() {
    RecordTypeBuilder builder = new RecordTypeBuilder(registry);
    builder.addProperty("a", googSubBarInst, null);
    JSType record = builder.build();

    ObjectType subtypeProp = registry.createAnonymousObjectType();
    subtypeProp.defineInferredProperty("a", googSubSubBarInst, null);
    assertTrue(subtypeProp.isSubtype(record));
    assertFalse(record.isSubtype(subtypeProp));

    ObjectType supertypeProp = registry.createAnonymousObjectType();
    supertypeProp.defineInferredProperty("a", googBarInst, null);
    assertFalse(supertypeProp.isSubtype(record));
    assertFalse(record.isSubtype(supertypeProp));

    ObjectType declaredSubtypeProp = registry.createAnonymousObjectType();
    declaredSubtypeProp.defineDeclaredProperty("a", googSubSubBarInst,
        null);
    assertFalse(declaredSubtypeProp.isSubtype(record));
    assertFalse(record.isSubtype(declaredSubtypeProp));
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testRecordTypeLeastSuperType1
  public void testRecordTypeLeastSuperType1() {
    RecordTypeBuilder builder = new RecordTypeBuilder(registry);
    builder.addProperty("a", NUMBER_TYPE, null);
    builder.addProperty("b", STRING_TYPE, null);
    builder.addProperty("c", STRING_TYPE, null);
    JSType subRecordType = builder.build();

    JSType leastSupertype = recordType.getLeastSupertype(subRecordType);
    assertTypeEquals(leastSupertype, recordType);
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testRecordTypeLeastSuperType2
  public void testRecordTypeLeastSuperType2() {
    RecordTypeBuilder builder = new RecordTypeBuilder(registry);
    builder.addProperty("e", NUMBER_TYPE, null);
    builder.addProperty("b", STRING_TYPE, null);
    builder.addProperty("c", STRING_TYPE, null);
    JSType otherRecordType = builder.build();

    assertTypeEquals(
        registry.createUnionType(recordType, otherRecordType),
        recordType.getLeastSupertype(otherRecordType));
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testRecordTypeLeastSuperType3
  public void testRecordTypeLeastSuperType3() {
    RecordTypeBuilder builder = new RecordTypeBuilder(registry);
    builder.addProperty("d", NUMBER_TYPE, null);
    builder.addProperty("e", STRING_TYPE, null);
    builder.addProperty("f", STRING_TYPE, null);
    JSType otherRecordType = builder.build();

    assertTypeEquals(
        registry.createUnionType(recordType, otherRecordType),
        recordType.getLeastSupertype(otherRecordType));
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testRecordTypeLeastSuperType4
  public void testRecordTypeLeastSuperType4() {
    JSType leastSupertype = recordType.getLeastSupertype(OBJECT_TYPE);
    assertTypeEquals(leastSupertype, OBJECT_TYPE);
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testRecordTypeGreatestSubType1
  public void testRecordTypeGreatestSubType1() {
    RecordTypeBuilder builder = new RecordTypeBuilder(registry);
    builder.addProperty("d", NUMBER_TYPE, null);
    builder.addProperty("e", STRING_TYPE, null);
    builder.addProperty("f", STRING_TYPE, null);

    JSType subRecordType = builder.build();

    JSType subtype = recordType.getGreatestSubtype(subRecordType);

    builder = new RecordTypeBuilder(registry);
    builder.addProperty("d", NUMBER_TYPE, null);
    builder.addProperty("e", STRING_TYPE, null);
    builder.addProperty("f", STRING_TYPE, null);
    builder.addProperty("a", NUMBER_TYPE, null);
    builder.addProperty("b", STRING_TYPE, null);

    assertTypeEquals(subtype, builder.build());
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testRecordTypeGreatestSubType2
  public void testRecordTypeGreatestSubType2() {
    RecordTypeBuilder builder = new RecordTypeBuilder(registry);

    JSType subRecordType = builder.build();

    JSType subtype = recordType.getGreatestSubtype(subRecordType);

    builder = new RecordTypeBuilder(registry);
    builder.addProperty("a", NUMBER_TYPE, null);
    builder.addProperty("b", STRING_TYPE, null);

    assertTypeEquals(subtype, builder.build());
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testRecordTypeGreatestSubType3
  public void testRecordTypeGreatestSubType3() {
    RecordTypeBuilder builder = new RecordTypeBuilder(registry);
    builder.addProperty("a", NUMBER_TYPE, null);
    builder.addProperty("b", STRING_TYPE, null);
    builder.addProperty("c", STRING_TYPE, null);

    JSType subRecordType = builder.build();

    JSType subtype = recordType.getGreatestSubtype(subRecordType);

    builder = new RecordTypeBuilder(registry);
    builder.addProperty("a", NUMBER_TYPE, null);
    builder.addProperty("b", STRING_TYPE, null);
    builder.addProperty("c", STRING_TYPE, null);

    assertTypeEquals(subtype, builder.build());
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testRecordTypeGreatestSubType4
  public void testRecordTypeGreatestSubType4() {
    RecordTypeBuilder builder = new RecordTypeBuilder(registry);
    builder.addProperty("a", STRING_TYPE, null);
    builder.addProperty("b", STRING_TYPE, null);
    builder.addProperty("c", STRING_TYPE, null);

    JSType subRecordType = builder.build();

    JSType subtype = recordType.getGreatestSubtype(subRecordType);
    assertTypeEquals(subtype, NO_TYPE);
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testRecordTypeGreatestSubType5
  public void testRecordTypeGreatestSubType5() {
    RecordTypeBuilder builder = new RecordTypeBuilder(registry);
    builder.addProperty("a", STRING_TYPE, null);

    JSType recordType = builder.build();

    assertTypeEquals(NO_OBJECT_TYPE,
                 recordType.getGreatestSubtype(U2U_CONSTRUCTOR_TYPE));

    
    
    U2U_CONSTRUCTOR_TYPE.defineDeclaredProperty("a", STRING_TYPE, null);
    assertTypeEquals(U2U_CONSTRUCTOR_TYPE,
                 recordType.getGreatestSubtype(U2U_CONSTRUCTOR_TYPE));
    assertTypeEquals(U2U_CONSTRUCTOR_TYPE,
                 U2U_CONSTRUCTOR_TYPE.getGreatestSubtype(recordType));
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testRecordTypeGreatestSubType6
  public void testRecordTypeGreatestSubType6() {
    RecordTypeBuilder builder = new RecordTypeBuilder(registry);
    builder.addProperty("x", UNKNOWN_TYPE, null);

    JSType recordType = builder.build();

    assertTypeEquals(NO_OBJECT_TYPE,
                 recordType.getGreatestSubtype(U2U_CONSTRUCTOR_TYPE));

    
    
    U2U_CONSTRUCTOR_TYPE.defineDeclaredProperty("x", STRING_TYPE, null);
    assertTypeEquals(U2U_CONSTRUCTOR_TYPE,
                 recordType.getGreatestSubtype(U2U_CONSTRUCTOR_TYPE));
    assertTypeEquals(U2U_CONSTRUCTOR_TYPE,
                 U2U_CONSTRUCTOR_TYPE.getGreatestSubtype(recordType));
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testRecordTypeGreatestSubType7
  public void testRecordTypeGreatestSubType7() {
    RecordTypeBuilder builder = new RecordTypeBuilder(registry);
    builder.addProperty("x", NUMBER_TYPE, null);

    JSType recordType = builder.build();

    
    
    U2U_CONSTRUCTOR_TYPE.defineDeclaredProperty("x", STRING_TYPE, null);
    assertTypeEquals(NO_OBJECT_TYPE,
                 recordType.getGreatestSubtype(U2U_CONSTRUCTOR_TYPE));
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testRecordTypeGreatestSubType8
  public void testRecordTypeGreatestSubType8() {
    RecordTypeBuilder builder = new RecordTypeBuilder(registry);
    builder.addProperty("xyz", UNKNOWN_TYPE, null);

    JSType recordType = builder.build();

    assertTypeEquals(NO_OBJECT_TYPE,
                 recordType.getGreatestSubtype(U2U_CONSTRUCTOR_TYPE));

    
    
    googBar.defineDeclaredProperty("xyz", STRING_TYPE, null);

    assertTypeEquals(googBar,
                 recordType.getGreatestSubtype(U2U_CONSTRUCTOR_TYPE));
    assertTypeEquals(googBar,
                 U2U_CONSTRUCTOR_TYPE.getGreatestSubtype(recordType));

    ObjectType googBarInst = googBar.getInstanceType();
    assertTypeEquals(NO_OBJECT_TYPE,
                 recordType.getGreatestSubtype(googBarInst));
    assertTypeEquals(NO_OBJECT_TYPE,
                 googBarInst.getGreatestSubtype(recordType));
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testApplyOfDateMethod
  public void testApplyOfDateMethod() {
    JSType applyType = dateMethod.getPropertyType("apply");
    assertTrue("apply should be a function",
        applyType instanceof FunctionType);

    FunctionType applyFn = (FunctionType) applyType;
    assertTypeEquals("apply should have the same return type as its function",
        NUMBER_TYPE, applyFn.getReturnType());

    Node params = applyFn.getParametersNode();
    assertEquals("apply takes two args",
        2, params.getChildCount());
    assertTypeEquals("apply's first arg is the @this type",
        registry.createOptionalNullableType(DATE_TYPE),
        params.getFirstChild().getJSType());
    assertTypeEquals("apply's second arg is an Array",
        registry.createOptionalNullableType(OBJECT_TYPE),
        params.getLastChild().getJSType());
    assertTrue("apply's args must be optional",
        params.getFirstChild().isOptionalArg());
    assertTrue("apply's args must be optional",
        params.getLastChild().isOptionalArg());
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testCallOfDateMethod
  public void testCallOfDateMethod() {
    JSType callType = dateMethod.getPropertyType("call");
    assertTrue("call should be a function",
        callType instanceof FunctionType);

    FunctionType callFn = (FunctionType) callType;
    assertTypeEquals("call should have the same return type as its function",
        NUMBER_TYPE, callFn.getReturnType());

    Node params = callFn.getParametersNode();
    assertEquals("call takes one argument in this case",
        1, params.getChildCount());
    assertTypeEquals("call's first arg is the @this type",
        registry.createOptionalNullableType(DATE_TYPE),
        params.getFirstChild().getJSType());
    assertTrue("call's args must be optional",
        params.getFirstChild().isOptionalArg());
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testFunctionTypeRepresentation
  public void testFunctionTypeRepresentation() {
    assertEquals("function (number, string): boolean",
        registry.createFunctionType(BOOLEAN_TYPE, false, NUMBER_TYPE,
            STRING_TYPE).toString());

    assertEquals("function (new:Array, ...[*]): Array",
        ARRAY_FUNCTION_TYPE.toString());

    assertEquals("function (new:Boolean, *): boolean",
        BOOLEAN_OBJECT_FUNCTION_TYPE.toString());

    assertEquals("function (new:Number, *): number",
        NUMBER_OBJECT_FUNCTION_TYPE.toString());

    assertEquals("function (new:String, *): string",
        STRING_OBJECT_FUNCTION_TYPE.toString());

    assertEquals("function (...[number]): boolean",
        registry.createFunctionType(BOOLEAN_TYPE, true, NUMBER_TYPE)
        .toString());

    assertEquals("function (number, ...[string]): boolean",
        registry.createFunctionType(BOOLEAN_TYPE, true, NUMBER_TYPE,
            STRING_TYPE).toString());

    assertEquals("function (this:Date, number): (boolean|number|string)",
        new FunctionBuilder(registry)
            .withParamsNode(registry.createParameters(NUMBER_TYPE))
            .withReturnType(NUMBER_STRING_BOOLEAN)
            .withTypeOfThis(DATE_TYPE)
            .build().toString());
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testFunctionTypeRelationships
  public void testFunctionTypeRelationships() {
    FunctionType dateMethodEmpty = new FunctionBuilder(registry)
        .withParamsNode(registry.createParameters())
        .withTypeOfThis(DATE_TYPE).build();
    FunctionType dateMethodWithParam = new FunctionBuilder(registry)
        .withParamsNode(registry.createParameters(NUMBER_TYPE))
        .withTypeOfThis(DATE_TYPE).build();
    FunctionType dateMethodWithReturn = new FunctionBuilder(registry)
        .withReturnType(NUMBER_TYPE)
        .withTypeOfThis(DATE_TYPE).build();
    FunctionType stringMethodEmpty = new FunctionBuilder(registry)
        .withParamsNode(registry.createParameters())
        .withTypeOfThis(STRING_OBJECT_TYPE).build();
    FunctionType stringMethodWithParam = new FunctionBuilder(registry)
        .withParamsNode(registry.createParameters(NUMBER_TYPE))
        .withTypeOfThis(STRING_OBJECT_TYPE).build();
    FunctionType stringMethodWithReturn = new FunctionBuilder(registry)
        .withReturnType(NUMBER_TYPE)
        .withTypeOfThis(STRING_OBJECT_TYPE).build();

    
    assertFalse(stringMethodEmpty.isSubtype(dateMethodEmpty));

    
    List<FunctionType> allFunctions = Lists.newArrayList(
        dateMethodEmpty, dateMethodWithParam, dateMethodWithReturn,
        stringMethodEmpty, stringMethodWithParam, stringMethodWithReturn);
    for (int i = 0; i < allFunctions.size(); i++) {
      for (int j = 0; j < allFunctions.size(); j++) {
        FunctionType typeA = allFunctions.get(i);
        FunctionType typeB = allFunctions.get(j);
        assertEquals(String.format("equals(%s, %s)", typeA, typeB),
            i == j, typeA.isEquivalentTo(typeB));

        
        
        assertEquals(String.format("isSubtype(%s, %s)", typeA, typeB),
            typeA.getTypeOfThis().isEquivalentTo(typeB.getTypeOfThis()),
            typeA.isSubtype(typeB));

        if (i == j) {
          assertTypeEquals(typeA, typeA.getLeastSupertype(typeB));
          assertTypeEquals(typeA, typeA.getGreatestSubtype(typeB));
        } else {
          assertTypeEquals(String.format("sup(%s, %s)", typeA, typeB),
              U2U_CONSTRUCTOR_TYPE, typeA.getLeastSupertype(typeB));
          assertTypeEquals(String.format("inf(%s, %s)", typeA, typeB),
              LEAST_FUNCTION_TYPE, typeA.getGreatestSubtype(typeB));
        }
      }
    }
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testProxiedFunctionTypeRelationships
  public void testProxiedFunctionTypeRelationships() {
    FunctionType dateMethodEmpty = new FunctionBuilder(registry)
      .withParamsNode(registry.createParameters())
      .withTypeOfThis(DATE_TYPE).build().toMaybeFunctionType();
    FunctionType dateMethodWithParam = new FunctionBuilder(registry)
      .withParamsNode(registry.createParameters(NUMBER_TYPE))
      .withTypeOfThis(DATE_TYPE).build().toMaybeFunctionType();
    ProxyObjectType proxyDateMethodEmpty =
        new ProxyObjectType(registry, dateMethodEmpty);
    ProxyObjectType proxyDateMethodWithParam =
        new ProxyObjectType(registry, dateMethodWithParam);

    assertTypeEquals(U2U_CONSTRUCTOR_TYPE,
        proxyDateMethodEmpty.getLeastSupertype(proxyDateMethodWithParam));
    assertTypeEquals(LEAST_FUNCTION_TYPE,
        proxyDateMethodEmpty.getGreatestSubtype(proxyDateMethodWithParam));
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testFunctionSubTypeRelationships
  public void testFunctionSubTypeRelationships() {
    FunctionType googBarMethod = new FunctionBuilder(registry)
        .withTypeOfThis(googBar).build();
    FunctionType googBarParamFn = new FunctionBuilder(registry)
        .withParamsNode(registry.createParameters(googBar)).build();
    FunctionType googBarReturnFn = new FunctionBuilder(registry)
        .withParamsNode(registry.createParameters())
        .withReturnType(googBar).build();
    FunctionType googSubBarMethod = new FunctionBuilder(registry)
        .withTypeOfThis(googSubBar).build();
    FunctionType googSubBarParamFn = new FunctionBuilder(registry)
        .withParamsNode(registry.createParameters(googSubBar)).build();
    FunctionType googSubBarReturnFn = new FunctionBuilder(registry)
        .withReturnType(googSubBar).build();

    assertTrue(googBarMethod.isSubtype(googSubBarMethod));
    assertTrue(googBarReturnFn.isSubtype(googSubBarReturnFn));

    List<FunctionType> allFunctions = Lists.newArrayList(
        googBarMethod, googBarParamFn, googBarReturnFn,
        googSubBarMethod, googSubBarParamFn, googSubBarReturnFn);
    for (int i = 0; i < allFunctions.size(); i++) {
      for (int j = 0; j < allFunctions.size(); j++) {
        FunctionType typeA = allFunctions.get(i);
        FunctionType typeB = allFunctions.get(j);
        assertEquals(String.format("equals(%s, %s)", typeA, typeB),
            i == j, typeA.isEquivalentTo(typeB));

        
        
        if (i == j) {
          assertTypeEquals(typeA, typeA.getLeastSupertype(typeB));
          assertTypeEquals(typeA, typeA.getGreatestSubtype(typeB));
        } else {
          assertTypeEquals(String.format("sup(%s, %s)", typeA, typeB),
              U2U_CONSTRUCTOR_TYPE, typeA.getLeastSupertype(typeB));
          assertTypeEquals(String.format("inf(%s, %s)", typeA, typeB),
              LEAST_FUNCTION_TYPE, typeA.getGreatestSubtype(typeB));
        }
      }
    }
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testFunctionPrototypeAndImplicitPrototype1
  public void testFunctionPrototypeAndImplicitPrototype1() {
    FunctionType constructor =
        registry.createConstructorType(null, null, null, null);
    ObjectType instance = constructor.getInstanceType();

    
    ObjectType prototype =
        (ObjectType) constructor.getPropertyType("prototype");
    prototype.defineDeclaredProperty("foo", DATE_TYPE, null);

    assertEquals(NATIVE_PROPERTIES_COUNT + 1, instance.getPropertiesCount());
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testFunctionPrototypeAndImplicitPrototype2
  public void testFunctionPrototypeAndImplicitPrototype2() {
    FunctionType constructor =
        registry.createConstructorType(null, null, null, null);
    ObjectType instance = constructor.getInstanceType();

    
    ObjectType prototype = registry.createAnonymousObjectType();
    prototype.defineDeclaredProperty("foo", DATE_TYPE, null);
    constructor.defineDeclaredProperty("prototype", prototype, null);

    assertEquals(NATIVE_PROPERTIES_COUNT + 1, instance.getPropertiesCount());
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testJSDocOnPrototypeProperty
  public void testJSDocOnPrototypeProperty() throws Exception {
    subclassCtor.setPropertyJSDocInfo("prototype", new JSDocInfo());
    assertNull(subclassCtor.getOwnPropertyJSDocInfo("prototype"));
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testVoidType
  public void testVoidType() throws Exception {
    
    assertTrue(VOID_TYPE.canAssignTo(ALL_TYPE));
    assertFalse(VOID_TYPE.canAssignTo(STRING_OBJECT_TYPE));
    assertFalse(VOID_TYPE.canAssignTo(REGEXP_TYPE));

    
    assertNull(VOID_TYPE.autoboxesTo());

    
    assertCanTestForEqualityWith(VOID_TYPE, ALL_TYPE);
    assertCannotTestForEqualityWith(VOID_TYPE, REGEXP_TYPE);

    
    assertTrue(VOID_TYPE.canTestForShallowEqualityWith(NO_TYPE));
    assertFalse(VOID_TYPE.canTestForShallowEqualityWith(NO_OBJECT_TYPE));
    assertFalse(VOID_TYPE.canTestForShallowEqualityWith(ARRAY_TYPE));
    assertFalse(VOID_TYPE.canTestForShallowEqualityWith(BOOLEAN_TYPE));
    assertFalse(VOID_TYPE.canTestForShallowEqualityWith(BOOLEAN_OBJECT_TYPE));
    assertFalse(VOID_TYPE.canTestForShallowEqualityWith(DATE_TYPE));
    assertFalse(VOID_TYPE.canTestForShallowEqualityWith(ERROR_TYPE));
    assertFalse(VOID_TYPE.canTestForShallowEqualityWith(EVAL_ERROR_TYPE));
    assertFalse(VOID_TYPE.canTestForShallowEqualityWith(functionType));
    assertFalse(VOID_TYPE.canTestForShallowEqualityWith(NULL_TYPE));
    assertFalse(VOID_TYPE.canTestForShallowEqualityWith(NUMBER_TYPE));
    assertFalse(VOID_TYPE.canTestForShallowEqualityWith(NUMBER_OBJECT_TYPE));
    assertFalse(VOID_TYPE.canTestForShallowEqualityWith(OBJECT_TYPE));
    assertFalse(VOID_TYPE.canTestForShallowEqualityWith(URI_ERROR_TYPE));
    assertFalse(VOID_TYPE.canTestForShallowEqualityWith(RANGE_ERROR_TYPE));
    assertFalse(VOID_TYPE.canTestForShallowEqualityWith(REFERENCE_ERROR_TYPE));
    assertFalse(VOID_TYPE.canTestForShallowEqualityWith(REGEXP_TYPE));
    assertFalse(VOID_TYPE.canTestForShallowEqualityWith(STRING_TYPE));
    assertFalse(VOID_TYPE.canTestForShallowEqualityWith(STRING_OBJECT_TYPE));
    assertFalse(VOID_TYPE.canTestForShallowEqualityWith(SYNTAX_ERROR_TYPE));
    assertFalse(VOID_TYPE.canTestForShallowEqualityWith(TYPE_ERROR_TYPE));
    assertTrue(VOID_TYPE.canTestForShallowEqualityWith(ALL_TYPE));
    assertTrue(VOID_TYPE.canTestForShallowEqualityWith(VOID_TYPE));
    assertTrue(VOID_TYPE.canTestForShallowEqualityWith(
            createUnionType(NUMBER_TYPE, VOID_TYPE)));

    
    assertFalse(VOID_TYPE.matchesInt32Context());
    assertFalse(VOID_TYPE.matchesNumberContext());
    assertFalse(VOID_TYPE.matchesObjectContext());
    assertTrue(VOID_TYPE.matchesStringContext());
    assertFalse(VOID_TYPE.matchesUint32Context());

    Asserts.assertResolvesToSame(VOID_TYPE);
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testBooleanValueType
  public void testBooleanValueType() throws Exception {
    
    assertFalse(BOOLEAN_TYPE.isArrayType());
    assertFalse(BOOLEAN_TYPE.isBooleanObjectType());
    assertTrue(BOOLEAN_TYPE.isBooleanValueType());
    assertFalse(BOOLEAN_TYPE.isDateType());
    assertFalse(BOOLEAN_TYPE.isEnumElementType());
    assertFalse(BOOLEAN_TYPE.isNamedType());
    assertFalse(BOOLEAN_TYPE.isNullType());
    assertFalse(BOOLEAN_TYPE.isNumberObjectType());
    assertFalse(BOOLEAN_TYPE.isNumberValueType());
    assertFalse(BOOLEAN_TYPE.isFunctionPrototypeType());
    assertFalse(BOOLEAN_TYPE.isRegexpType());
    assertFalse(BOOLEAN_TYPE.isStringObjectType());
    assertFalse(BOOLEAN_TYPE.isStringValueType());
    assertFalse(BOOLEAN_TYPE.isEnumType());
    assertFalse(BOOLEAN_TYPE.isUnionType());
    assertFalse(BOOLEAN_TYPE.isAllType());
    assertFalse(BOOLEAN_TYPE.isVoidType());
    assertFalse(BOOLEAN_TYPE.isConstructor());
    assertFalse(BOOLEAN_TYPE.isInstanceType());

    
    assertTypeEquals(BOOLEAN_OBJECT_TYPE, BOOLEAN_TYPE.autoboxesTo());

    
    assertTypeEquals(BOOLEAN_TYPE, BOOLEAN_OBJECT_TYPE.unboxesTo());

    
    assertTrue(BOOLEAN_TYPE.canAssignTo(ALL_TYPE));
    assertFalse(BOOLEAN_TYPE.canAssignTo(STRING_OBJECT_TYPE));
    assertFalse(BOOLEAN_TYPE.canAssignTo(NUMBER_TYPE));
    assertFalse(BOOLEAN_TYPE.canAssignTo(functionType));
    assertFalse(BOOLEAN_TYPE.canAssignTo(NULL_TYPE));
    assertFalse(BOOLEAN_TYPE.canAssignTo(OBJECT_TYPE));
    assertFalse(BOOLEAN_TYPE.canAssignTo(DATE_TYPE));
    assertTrue(BOOLEAN_TYPE.canAssignTo(unresolvedNamedType));
    assertFalse(BOOLEAN_TYPE.canAssignTo(namedGoogBar));
    assertFalse(BOOLEAN_TYPE.canAssignTo(REGEXP_TYPE));

    
    assertFalse(BOOLEAN_TYPE.canBeCalled());

    
    assertCanTestForEqualityWith(BOOLEAN_TYPE, ALL_TYPE);
    assertCanTestForEqualityWith(BOOLEAN_TYPE, STRING_OBJECT_TYPE);
    assertCanTestForEqualityWith(BOOLEAN_TYPE, NUMBER_TYPE);
    assertCannotTestForEqualityWith(BOOLEAN_TYPE, functionType);
    assertCannotTestForEqualityWith(BOOLEAN_TYPE, VOID_TYPE);
    assertCanTestForEqualityWith(BOOLEAN_TYPE, OBJECT_TYPE);
    assertCanTestForEqualityWith(BOOLEAN_TYPE, DATE_TYPE);
    assertCanTestForEqualityWith(BOOLEAN_TYPE, REGEXP_TYPE);
    assertCanTestForEqualityWith(BOOLEAN_TYPE, UNKNOWN_TYPE);

    
    assertTrue(BOOLEAN_TYPE.canTestForShallowEqualityWith(NO_TYPE));
    assertFalse(BOOLEAN_TYPE.canTestForShallowEqualityWith(NO_OBJECT_TYPE));
    assertFalse(BOOLEAN_TYPE.canTestForShallowEqualityWith(ARRAY_TYPE));
    assertTrue(BOOLEAN_TYPE.canTestForShallowEqualityWith(BOOLEAN_TYPE));
    assertFalse(BOOLEAN_TYPE.
        canTestForShallowEqualityWith(BOOLEAN_OBJECT_TYPE));
    assertFalse(BOOLEAN_TYPE.canTestForShallowEqualityWith(DATE_TYPE));
    assertFalse(BOOLEAN_TYPE.canTestForShallowEqualityWith(ERROR_TYPE));
    assertFalse(BOOLEAN_TYPE.canTestForShallowEqualityWith(EVAL_ERROR_TYPE));
    assertFalse(BOOLEAN_TYPE.canTestForShallowEqualityWith(functionType));
    assertFalse(BOOLEAN_TYPE.canTestForShallowEqualityWith(NULL_TYPE));
    assertFalse(BOOLEAN_TYPE.canTestForShallowEqualityWith(NUMBER_TYPE));
    assertFalse(BOOLEAN_TYPE.canTestForShallowEqualityWith(NUMBER_OBJECT_TYPE));
    assertFalse(BOOLEAN_TYPE.canTestForShallowEqualityWith(OBJECT_TYPE));
    assertFalse(BOOLEAN_TYPE.canTestForShallowEqualityWith(URI_ERROR_TYPE));
    assertFalse(BOOLEAN_TYPE.canTestForShallowEqualityWith(RANGE_ERROR_TYPE));
    assertFalse(BOOLEAN_TYPE.
        canTestForShallowEqualityWith(REFERENCE_ERROR_TYPE));
    assertFalse(BOOLEAN_TYPE.canTestForShallowEqualityWith(REGEXP_TYPE));
    assertFalse(BOOLEAN_TYPE.canTestForShallowEqualityWith(STRING_TYPE));
    assertFalse(BOOLEAN_TYPE.canTestForShallowEqualityWith(STRING_OBJECT_TYPE));
    assertFalse(BOOLEAN_TYPE.canTestForShallowEqualityWith(SYNTAX_ERROR_TYPE));
    assertFalse(BOOLEAN_TYPE.canTestForShallowEqualityWith(TYPE_ERROR_TYPE));
    assertTrue(BOOLEAN_TYPE.canTestForShallowEqualityWith(ALL_TYPE));
    assertFalse(BOOLEAN_TYPE.canTestForShallowEqualityWith(VOID_TYPE));
    assertTrue(BOOLEAN_TYPE.canTestForShallowEqualityWith(UNKNOWN_TYPE));

    
    assertFalse(BOOLEAN_TYPE.isNullable());

    
    assertTrue(BOOLEAN_TYPE.matchesInt32Context());
    assertTrue(BOOLEAN_TYPE.matchesNumberContext());
    assertTrue(BOOLEAN_TYPE.matchesObjectContext());
    assertTrue(BOOLEAN_TYPE.matchesStringContext());
    assertTrue(BOOLEAN_TYPE.matchesUint32Context());

    
    assertEquals("boolean", BOOLEAN_TYPE.toString());
    assertTrue(BOOLEAN_TYPE.hasDisplayName());
    assertEquals("boolean", BOOLEAN_TYPE.getDisplayName());

    Asserts.assertResolvesToSame(BOOLEAN_TYPE);
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testBooleanObjectType
  public void testBooleanObjectType() throws Exception {
    
    assertFalse(BOOLEAN_OBJECT_TYPE.isArrayType());
    assertTrue(BOOLEAN_OBJECT_TYPE.isBooleanObjectType());
    assertFalse(BOOLEAN_OBJECT_TYPE.isBooleanValueType());
    assertFalse(BOOLEAN_OBJECT_TYPE.isDateType());
    assertFalse(BOOLEAN_OBJECT_TYPE.isEnumElementType());
    assertFalse(BOOLEAN_OBJECT_TYPE.isNamedType());
    assertFalse(BOOLEAN_OBJECT_TYPE.isNullType());
    assertFalse(BOOLEAN_OBJECT_TYPE.isNumberObjectType());
    assertFalse(BOOLEAN_OBJECT_TYPE.isNumberValueType());
    assertFalse(BOOLEAN_OBJECT_TYPE.isFunctionPrototypeType());
    assertTrue(
        BOOLEAN_OBJECT_TYPE.getImplicitPrototype().isFunctionPrototypeType());
    assertFalse(BOOLEAN_OBJECT_TYPE.isRegexpType());
    assertFalse(BOOLEAN_OBJECT_TYPE.isStringObjectType());
    assertFalse(BOOLEAN_OBJECT_TYPE.isStringValueType());
    assertFalse(BOOLEAN_OBJECT_TYPE.isEnumType());
    assertFalse(BOOLEAN_OBJECT_TYPE.isUnionType());
    assertFalse(BOOLEAN_OBJECT_TYPE.isAllType());
    assertFalse(BOOLEAN_OBJECT_TYPE.isVoidType());
    assertFalse(BOOLEAN_OBJECT_TYPE.isConstructor());
    assertTrue(BOOLEAN_OBJECT_TYPE.isInstanceType());

    
    assertTrue(BOOLEAN_OBJECT_TYPE.canAssignTo(ALL_TYPE));
    assertFalse(BOOLEAN_OBJECT_TYPE.canAssignTo(STRING_OBJECT_TYPE));
    assertFalse(BOOLEAN_OBJECT_TYPE.canAssignTo(NUMBER_TYPE));
    assertFalse(BOOLEAN_OBJECT_TYPE.canAssignTo(functionType));
    assertFalse(BOOLEAN_OBJECT_TYPE.canAssignTo(NULL_TYPE));
    assertTrue(BOOLEAN_OBJECT_TYPE.canAssignTo(OBJECT_TYPE));
    assertFalse(BOOLEAN_OBJECT_TYPE.canAssignTo(DATE_TYPE));
    assertTrue(BOOLEAN_OBJECT_TYPE.canAssignTo(unresolvedNamedType));
    assertFalse(BOOLEAN_OBJECT_TYPE.canAssignTo(namedGoogBar));
    assertFalse(BOOLEAN_OBJECT_TYPE.canAssignTo(REGEXP_TYPE));
    
    assertFalse(BOOLEAN_OBJECT_TYPE.canBeCalled());

    
    assertCanTestForEqualityWith(BOOLEAN_OBJECT_TYPE, ALL_TYPE);
    assertCanTestForEqualityWith(BOOLEAN_OBJECT_TYPE, STRING_OBJECT_TYPE);
    assertCanTestForEqualityWith(BOOLEAN_OBJECT_TYPE, NUMBER_TYPE);
    assertCanTestForEqualityWith(BOOLEAN_OBJECT_TYPE, functionType);
    assertCannotTestForEqualityWith(BOOLEAN_OBJECT_TYPE, VOID_TYPE);
    assertCanTestForEqualityWith(BOOLEAN_OBJECT_TYPE, OBJECT_TYPE);
    assertCanTestForEqualityWith(BOOLEAN_OBJECT_TYPE, DATE_TYPE);
    assertCanTestForEqualityWith(BOOLEAN_OBJECT_TYPE, REGEXP_TYPE);

    
    assertTrue(BOOLEAN_OBJECT_TYPE.canTestForShallowEqualityWith(NO_TYPE));
    assertTrue(BOOLEAN_OBJECT_TYPE.
        canTestForShallowEqualityWith(NO_OBJECT_TYPE));
    assertFalse(BOOLEAN_OBJECT_TYPE.canTestForShallowEqualityWith(ARRAY_TYPE));
    assertFalse(BOOLEAN_OBJECT_TYPE.
        canTestForShallowEqualityWith(BOOLEAN_TYPE));
    assertTrue(BOOLEAN_OBJECT_TYPE.
        canTestForShallowEqualityWith(BOOLEAN_OBJECT_TYPE));
    assertFalse(BOOLEAN_OBJECT_TYPE.canTestForShallowEqualityWith(DATE_TYPE));
    assertFalse(BOOLEAN_OBJECT_TYPE.canTestForShallowEqualityWith(ERROR_TYPE));
    assertFalse(BOOLEAN_OBJECT_TYPE.
        canTestForShallowEqualityWith(EVAL_ERROR_TYPE));
    assertFalse(BOOLEAN_OBJECT_TYPE.
        canTestForShallowEqualityWith(functionType));
    assertFalse(BOOLEAN_OBJECT_TYPE.canTestForShallowEqualityWith(NULL_TYPE));
    assertFalse(BOOLEAN_OBJECT_TYPE.canTestForShallowEqualityWith(NUMBER_TYPE));
    assertFalse(BOOLEAN_OBJECT_TYPE.
        canTestForShallowEqualityWith(NUMBER_OBJECT_TYPE));
    assertTrue(BOOLEAN_OBJECT_TYPE.canTestForShallowEqualityWith(OBJECT_TYPE));
    assertFalse(BOOLEAN_OBJECT_TYPE.
        canTestForShallowEqualityWith(URI_ERROR_TYPE));
    assertFalse(BOOLEAN_OBJECT_TYPE.
        canTestForShallowEqualityWith(RANGE_ERROR_TYPE));
    assertFalse(BOOLEAN_OBJECT_TYPE.
        canTestForShallowEqualityWith(REFERENCE_ERROR_TYPE));
    assertFalse(BOOLEAN_OBJECT_TYPE.canTestForShallowEqualityWith(REGEXP_TYPE));
    assertFalse(BOOLEAN_OBJECT_TYPE.canTestForShallowEqualityWith(STRING_TYPE));
    assertFalse(BOOLEAN_OBJECT_TYPE.
        canTestForShallowEqualityWith(STRING_OBJECT_TYPE));
    assertFalse(BOOLEAN_OBJECT_TYPE.
        canTestForShallowEqualityWith(SYNTAX_ERROR_TYPE));
    assertFalse(BOOLEAN_OBJECT_TYPE.
        canTestForShallowEqualityWith(TYPE_ERROR_TYPE));
    assertTrue(BOOLEAN_OBJECT_TYPE.canTestForShallowEqualityWith(ALL_TYPE));
    assertFalse(BOOLEAN_OBJECT_TYPE.canTestForShallowEqualityWith(VOID_TYPE));

    
    assertFalse(BOOLEAN_OBJECT_TYPE.isNullable());

    
    assertTrue(BOOLEAN_OBJECT_TYPE.matchesInt32Context());
    assertTrue(BOOLEAN_OBJECT_TYPE.matchesNumberContext());
    assertTrue(BOOLEAN_OBJECT_TYPE.matchesObjectContext());
    assertTrue(BOOLEAN_OBJECT_TYPE.matchesStringContext());
    assertTrue(BOOLEAN_OBJECT_TYPE.matchesUint32Context());

    
    assertEquals("Boolean", BOOLEAN_OBJECT_TYPE.toString());
    assertTrue(BOOLEAN_OBJECT_TYPE.hasDisplayName());
    assertEquals("Boolean", BOOLEAN_OBJECT_TYPE.getDisplayName());

    assertTrue(BOOLEAN_OBJECT_TYPE.isNativeObjectType());

    Asserts.assertResolvesToSame(BOOLEAN_OBJECT_TYPE);
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testEnumType
  public void testEnumType() throws Exception {
    EnumType enumType = new EnumType(registry, "Enum", null, NUMBER_TYPE);

    
    assertFalse(enumType.isArrayType());
    assertFalse(enumType.isBooleanObjectType());
    assertFalse(enumType.isBooleanValueType());
    assertFalse(enumType.isDateType());
    assertFalse(enumType.isEnumElementType());
    assertFalse(enumType.isNamedType());
    assertFalse(enumType.isNullType());
    assertFalse(enumType.isNumberObjectType());
    assertFalse(enumType.isNumberValueType());
    assertFalse(enumType.isFunctionPrototypeType());
    assertFalse(enumType.isRegexpType());
    assertFalse(enumType.isStringObjectType());
    assertFalse(enumType.isStringValueType());
    assertTrue(enumType.isEnumType());
    assertFalse(enumType.isUnionType());
    assertFalse(enumType.isAllType());
    assertFalse(enumType.isVoidType());
    assertFalse(enumType.isConstructor());
    assertFalse(enumType.isInstanceType());

    
    assertTrue(enumType.canAssignTo(ALL_TYPE));
    assertFalse(enumType.canAssignTo(STRING_OBJECT_TYPE));
    assertFalse(enumType.canAssignTo(NUMBER_TYPE));
    assertFalse(enumType.canAssignTo(functionType));
    assertFalse(enumType.canAssignTo(NULL_TYPE));
    assertTrue(enumType.canAssignTo(OBJECT_TYPE));
    assertFalse(enumType.canAssignTo(DATE_TYPE));
    assertTrue(enumType.canAssignTo(unresolvedNamedType));
    assertFalse(enumType.canAssignTo(namedGoogBar));
    assertFalse(enumType.canAssignTo(REGEXP_TYPE));

    
    assertFalse(enumType.canBeCalled());

    
    assertCanTestForEqualityWith(enumType, ALL_TYPE);
    assertCanTestForEqualityWith(enumType, STRING_OBJECT_TYPE);
    assertCanTestForEqualityWith(enumType, NUMBER_TYPE);
    assertCanTestForEqualityWith(enumType, functionType);
    assertCannotTestForEqualityWith(enumType, VOID_TYPE);
    assertCanTestForEqualityWith(enumType, OBJECT_TYPE);
    assertCanTestForEqualityWith(enumType, DATE_TYPE);
    assertCanTestForEqualityWith(enumType, REGEXP_TYPE);

    
    assertTrue(enumType.canTestForShallowEqualityWith(NO_TYPE));
    assertTrue(enumType.
        canTestForShallowEqualityWith(NO_OBJECT_TYPE));
    assertFalse(enumType.canTestForShallowEqualityWith(ARRAY_TYPE));
    assertFalse(enumType.
        canTestForShallowEqualityWith(BOOLEAN_TYPE));
    assertTrue(enumType.
        canTestForShallowEqualityWith(enumType));
    assertFalse(enumType.canTestForShallowEqualityWith(DATE_TYPE));
    assertFalse(enumType.canTestForShallowEqualityWith(ERROR_TYPE));
    assertFalse(enumType.
        canTestForShallowEqualityWith(EVAL_ERROR_TYPE));
    assertFalse(enumType.
        canTestForShallowEqualityWith(functionType));
    assertFalse(enumType.canTestForShallowEqualityWith(NULL_TYPE));
    assertFalse(enumType.canTestForShallowEqualityWith(NUMBER_TYPE));
    assertFalse(enumType.
        canTestForShallowEqualityWith(NUMBER_OBJECT_TYPE));
    assertTrue(enumType.canTestForShallowEqualityWith(OBJECT_TYPE));
    assertFalse(enumType.
        canTestForShallowEqualityWith(URI_ERROR_TYPE));
    assertFalse(enumType.
        canTestForShallowEqualityWith(RANGE_ERROR_TYPE));
    assertFalse(enumType.
        canTestForShallowEqualityWith(REFERENCE_ERROR_TYPE));
    assertFalse(enumType.canTestForShallowEqualityWith(REGEXP_TYPE));
    assertFalse(enumType.canTestForShallowEqualityWith(STRING_TYPE));
    assertFalse(enumType.
        canTestForShallowEqualityWith(STRING_OBJECT_TYPE));
    assertFalse(enumType.
        canTestForShallowEqualityWith(SYNTAX_ERROR_TYPE));
    assertFalse(enumType.
        canTestForShallowEqualityWith(TYPE_ERROR_TYPE));
    assertTrue(enumType.canTestForShallowEqualityWith(ALL_TYPE));
    assertFalse(enumType.canTestForShallowEqualityWith(VOID_TYPE));

    
    assertFalse(enumType.isNullable());

    
    assertFalse(enumType.matchesInt32Context());
    assertFalse(enumType.matchesNumberContext());
    assertTrue(enumType.matchesObjectContext());
    assertTrue(enumType.matchesStringContext());
    assertFalse(enumType.matchesUint32Context());

    
    assertEquals("enum{Enum}", enumType.toString());
    assertTrue(enumType.hasDisplayName());
    assertEquals("Enum", enumType.getDisplayName());

    assertEquals("AnotherEnum", new EnumType(registry, "AnotherEnum",
        null, NUMBER_TYPE).getDisplayName());
    assertFalse(
        new EnumType(registry, null, null, NUMBER_TYPE).hasDisplayName());

    Asserts.assertResolvesToSame(enumType);
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testEnumElementType
  public void testEnumElementType() throws Exception {
    
    assertFalse(elementsType.isArrayType());
    assertFalse(elementsType.isBooleanObjectType());
    assertFalse(elementsType.isBooleanValueType());
    assertFalse(elementsType.isDateType());
    assertTrue(elementsType.isEnumElementType());
    assertFalse(elementsType.isNamedType());
    assertFalse(elementsType.isNullType());
    assertFalse(elementsType.isNumberObjectType());
    assertFalse(elementsType.isNumberValueType());
    assertFalse(elementsType.isFunctionPrototypeType());
    assertFalse(elementsType.isRegexpType());
    assertFalse(elementsType.isStringObjectType());
    assertFalse(elementsType.isStringValueType());
    assertFalse(elementsType.isEnumType());
    assertFalse(elementsType.isUnionType());
    assertFalse(elementsType.isAllType());
    assertFalse(elementsType.isVoidType());
    assertFalse(elementsType.isConstructor());
    assertFalse(elementsType.isInstanceType());

    
    assertTrue(elementsType.canAssignTo(ALL_TYPE));
    assertFalse(elementsType.canAssignTo(STRING_OBJECT_TYPE));
    assertTrue(elementsType.canAssignTo(NUMBER_TYPE));
    assertFalse(elementsType.canAssignTo(functionType));
    assertFalse(elementsType.canAssignTo(NULL_TYPE));
    assertFalse(elementsType.canAssignTo(OBJECT_TYPE)); 
    assertFalse(elementsType.canAssignTo(DATE_TYPE));
    assertTrue(elementsType.canAssignTo(unresolvedNamedType));
    assertFalse(elementsType.canAssignTo(namedGoogBar));
    assertFalse(elementsType.canAssignTo(REGEXP_TYPE));

    
    assertFalse(elementsType.canBeCalled());

    
    assertCanTestForEqualityWith(elementsType, ALL_TYPE);
    assertCanTestForEqualityWith(elementsType, STRING_OBJECT_TYPE);
    assertCanTestForEqualityWith(elementsType, NUMBER_TYPE);
    assertCanTestForEqualityWith(elementsType, NUMBER_OBJECT_TYPE);
    assertCanTestForEqualityWith(elementsType, elementsType);
    assertCannotTestForEqualityWith(elementsType, functionType);
    assertCannotTestForEqualityWith(elementsType, VOID_TYPE);
    assertCanTestForEqualityWith(elementsType, OBJECT_TYPE);
    assertCanTestForEqualityWith(elementsType, DATE_TYPE);
    assertCanTestForEqualityWith(elementsType, REGEXP_TYPE);

    
    assertTrue(elementsType.canTestForShallowEqualityWith(NO_TYPE));
    assertFalse(elementsType.
        canTestForShallowEqualityWith(NO_OBJECT_TYPE));
    assertFalse(elementsType.canTestForShallowEqualityWith(ARRAY_TYPE));
    assertFalse(elementsType.
        canTestForShallowEqualityWith(BOOLEAN_TYPE));
    assertTrue(elementsType.
        canTestForShallowEqualityWith(elementsType));
    assertFalse(elementsType.canTestForShallowEqualityWith(DATE_TYPE));
    assertFalse(elementsType.canTestForShallowEqualityWith(ERROR_TYPE));
    assertFalse(elementsType.
        canTestForShallowEqualityWith(EVAL_ERROR_TYPE));
    assertFalse(elementsType.
        canTestForShallowEqualityWith(functionType));
    assertFalse(elementsType.canTestForShallowEqualityWith(NULL_TYPE));
    assertTrue(elementsType.canTestForShallowEqualityWith(NUMBER_TYPE));
    assertFalse(elementsType.
        canTestForShallowEqualityWith(NUMBER_OBJECT_TYPE));
    assertFalse(elementsType.canTestForShallowEqualityWith(OBJECT_TYPE));
    assertFalse(elementsType.
        canTestForShallowEqualityWith(URI_ERROR_TYPE));
    assertFalse(elementsType.
        canTestForShallowEqualityWith(RANGE_ERROR_TYPE));
    assertFalse(elementsType.
        canTestForShallowEqualityWith(REFERENCE_ERROR_TYPE));
    assertFalse(elementsType.canTestForShallowEqualityWith(REGEXP_TYPE));
    assertFalse(elementsType.canTestForShallowEqualityWith(STRING_TYPE));
    assertFalse(elementsType.
        canTestForShallowEqualityWith(STRING_OBJECT_TYPE));
    assertFalse(elementsType.
        canTestForShallowEqualityWith(SYNTAX_ERROR_TYPE));
    assertFalse(elementsType.
        canTestForShallowEqualityWith(TYPE_ERROR_TYPE));
    assertTrue(elementsType.canTestForShallowEqualityWith(ALL_TYPE));
    assertFalse(elementsType.canTestForShallowEqualityWith(VOID_TYPE));

    
    assertFalse(elementsType.isNullable());

    
    assertTrue(elementsType.matchesInt32Context());
    assertTrue(elementsType.matchesNumberContext());
    assertTrue(elementsType.matchesObjectContext());
    assertTrue(elementsType.matchesStringContext());
    assertTrue(elementsType.matchesUint32Context());

    
    assertEquals("Enum.<number>", elementsType.toString());
    assertTrue(elementsType.hasDisplayName());
    assertEquals("Enum", elementsType.getDisplayName());

    Asserts.assertResolvesToSame(elementsType);
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testStringEnumType
  public void testStringEnumType() throws Exception {
    EnumElementType stringEnum =
        new EnumType(registry, "Enum", null, STRING_TYPE).getElementsType();

    assertTypeEquals(UNKNOWN_TYPE, stringEnum.getPropertyType("length"));
    assertTypeEquals(NUMBER_TYPE, stringEnum.findPropertyType("length"));
    assertEquals(false, stringEnum.hasProperty("length"));
    assertTypeEquals(STRING_OBJECT_TYPE, stringEnum.autoboxesTo());
    assertNull(stringEnum.getConstructor());

    Asserts.assertResolvesToSame(stringEnum);
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testStringObjectEnumType
  public void testStringObjectEnumType() throws Exception {
    EnumElementType stringEnum =
        new EnumType(registry, "Enum", null, STRING_OBJECT_TYPE)
        .getElementsType();

    assertTypeEquals(NUMBER_TYPE, stringEnum.getPropertyType("length"));
    assertTypeEquals(NUMBER_TYPE, stringEnum.findPropertyType("length"));
    assertEquals(true, stringEnum.hasProperty("length"));
    assertTypeEquals(STRING_OBJECT_FUNCTION_TYPE, stringEnum.getConstructor());
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testObjectType
  public void testObjectType() throws Exception {
    PrototypeObjectType objectType =
        new PrototypeObjectType(registry, null, null);

    
    assertFalse(objectType.isAllType());
    assertFalse(objectType.isArrayType());
    assertFalse(objectType.isDateType());
    assertFalse(objectType.isFunctionPrototypeType());
    assertTrue(objectType.getImplicitPrototype() == OBJECT_TYPE);

    
    assertTrue(objectType.canAssignTo(ALL_TYPE));
    assertFalse(objectType.canAssignTo(STRING_OBJECT_TYPE));
    assertFalse(objectType.canAssignTo(NUMBER_TYPE));
    assertFalse(objectType.canAssignTo(functionType));
    assertFalse(objectType.canAssignTo(NULL_TYPE));
    assertFalse(objectType.canAssignTo(DATE_TYPE));
    assertTrue(objectType.canAssignTo(OBJECT_TYPE));
    assertTrue(objectType.canAssignTo(unresolvedNamedType));
    assertFalse(objectType.canAssignTo(namedGoogBar));
    assertFalse(objectType.canAssignTo(REGEXP_TYPE));

    
    assertNull(objectType.autoboxesTo());

    
    assertCanTestForEqualityWith(objectType, NUMBER_TYPE);

    
    assertFalse(objectType.matchesInt32Context());
    assertFalse(objectType.matchesNumberContext());
    assertTrue(objectType.matchesObjectContext());
    assertFalse(objectType.matchesStringContext());
    assertFalse(objectType.matchesUint32Context());

    
    assertFalse(objectType.isNullable());
    assertTrue(createNullableType(objectType).isNullable());

    
    assertEquals("{...}", objectType.toString());
    assertEquals(null, objectType.getDisplayName());
    assertFalse(objectType.hasReferenceName());
    assertEquals("anObject", new PrototypeObjectType(registry, "anObject",
        null).getDisplayName());

    Asserts.assertResolvesToSame(objectType);
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testGoogBar
  public void testGoogBar() throws Exception {
    assertTrue(namedGoogBar.isInstanceType());
    assertFalse(googBar.isInstanceType());
    assertFalse(namedGoogBar.isConstructor());
    assertTrue(googBar.isConstructor());
    assertTrue(googBar.getInstanceType().isInstanceType());
    assertTrue(namedGoogBar.getConstructor().isConstructor());
    assertTrue(namedGoogBar.getImplicitPrototype().isFunctionPrototypeType());

    
    assertTypeCanAssignToItself(googBar);
    assertTypeCanAssignToItself(namedGoogBar);
    googBar.canAssignTo(namedGoogBar);
    namedGoogBar.canAssignTo(googBar);
    assertTypeEquals(googBar, googBar);
    assertTypeNotEquals(googBar, googSubBar);

    Asserts.assertResolvesToSame(googBar);
    Asserts.assertResolvesToSame(googSubBar);
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testObjectTypePropertiesCount
  public void testObjectTypePropertiesCount() throws Exception {
    ObjectType sup = registry.createAnonymousObjectType();
    int nativeProperties = sup.getPropertiesCount();

    sup.defineDeclaredProperty("a", DATE_TYPE, null);
    assertEquals(nativeProperties + 1, sup.getPropertiesCount());

    sup.defineDeclaredProperty("b", DATE_TYPE, null);
    assertEquals(nativeProperties + 2, sup.getPropertiesCount());

    ObjectType sub = registry.createObjectType(sup);
    assertEquals(nativeProperties + 2, sub.getPropertiesCount());
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testDefineProperties
  public void testDefineProperties() {
    ObjectType prototype = googBar.getPrototype();
    ObjectType instance = googBar.getInstanceType();

    assertTypeEquals(instance.getImplicitPrototype(), prototype);

    
    assertTrue(
        prototype.defineDeclaredProperty("declared", NUMBER_TYPE, null));
    assertFalse(
        prototype.defineDeclaredProperty("declared", NUMBER_TYPE, null));
    assertFalse(
        instance.defineDeclaredProperty("declared", NUMBER_TYPE, null));
    assertTypeEquals(NUMBER_TYPE, instance.getPropertyType("declared"));

    
    assertTrue(prototype.defineInferredProperty("inferred1", STRING_TYPE,
        null));
    assertTrue(prototype.defineInferredProperty("inferred1", NUMBER_TYPE,
        null));
    assertTypeEquals(
        createUnionType(NUMBER_TYPE, STRING_TYPE),
        instance.getPropertyType("inferred1"));

    
    assertTrue(prototype.defineInferredProperty("inferred2", STRING_TYPE,
        null));
    assertTrue(instance.defineInferredProperty("inferred2", NUMBER_TYPE,
        null));
    assertTypeEquals(
        createUnionType(NUMBER_TYPE, STRING_TYPE),
        instance.getPropertyType("inferred2"));

    
    assertTrue(
        prototype.defineInferredProperty("prop", STRING_TYPE, null));
    assertTrue(
        instance.defineDeclaredProperty("prop", NUMBER_TYPE, null));
    assertTypeEquals(NUMBER_TYPE, instance.getPropertyType("prop"));
    assertTypeEquals(STRING_TYPE, prototype.getPropertyType("prop"));
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testObjectTypePropertiesCountWithShadowing
  public void testObjectTypePropertiesCountWithShadowing() {
    ObjectType sup = registry.createAnonymousObjectType();
    int nativeProperties = sup.getPropertiesCount();

    sup.defineDeclaredProperty("a", OBJECT_TYPE, null);
    assertEquals(nativeProperties + 1, sup.getPropertiesCount());

    ObjectType sub = registry.createObjectType(sup);
    sub.defineDeclaredProperty("a", OBJECT_TYPE, null);
    assertEquals(nativeProperties + 1, sub.getPropertiesCount());
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testNamedGoogBar
  public void testNamedGoogBar() throws Exception {
    
    assertFalse(namedGoogBar.isFunctionPrototypeType());
    assertTrue(namedGoogBar.getImplicitPrototype().isFunctionPrototypeType());

    
    assertTrue(namedGoogBar.canAssignTo(ALL_TYPE));
    assertFalse(namedGoogBar.canAssignTo(STRING_OBJECT_TYPE));
    assertFalse(namedGoogBar.canAssignTo(NUMBER_TYPE));
    assertFalse(namedGoogBar.canAssignTo(functionType));
    assertFalse(namedGoogBar.canAssignTo(NULL_TYPE));
    assertTrue(namedGoogBar.canAssignTo(OBJECT_TYPE));
    assertFalse(namedGoogBar.canAssignTo(DATE_TYPE));
    assertTrue(namedGoogBar.canAssignTo(namedGoogBar));
    assertTrue(namedGoogBar.canAssignTo(unresolvedNamedType));
    assertFalse(namedGoogBar.canAssignTo(REGEXP_TYPE));
    assertFalse(namedGoogBar.canAssignTo(ARRAY_TYPE));

    
    assertNull(namedGoogBar.autoboxesTo());

    
    assertTypeEquals(DATE_TYPE, namedGoogBar.getPropertyType("date"));

    assertFalse(namedGoogBar.isNativeObjectType());
    assertFalse(namedGoogBar.getImplicitPrototype().isNativeObjectType());

    JSType resolvedNamedGoogBar = Asserts.assertValidResolve(namedGoogBar);
    assertNotSame(resolvedNamedGoogBar, namedGoogBar);
    assertSame(resolvedNamedGoogBar, googBar.getInstanceType());
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testPrototypeChaining
  public void testPrototypeChaining() throws Exception {
    
    assertTypeEquals(
        ARRAY_TYPE.getImplicitPrototype().getImplicitPrototype(),
        OBJECT_TYPE);
    assertTypeEquals(
        BOOLEAN_OBJECT_TYPE.getImplicitPrototype().
        getImplicitPrototype(), OBJECT_TYPE);
    assertTypeEquals(
        DATE_TYPE.getImplicitPrototype().getImplicitPrototype(),
        OBJECT_TYPE);
    assertTypeEquals(
        ERROR_TYPE.getImplicitPrototype().getImplicitPrototype(),
        OBJECT_TYPE);
    assertTypeEquals(
        EVAL_ERROR_TYPE.getImplicitPrototype().getImplicitPrototype(),
        ERROR_TYPE);
    assertTypeEquals(
        NUMBER_OBJECT_TYPE.getImplicitPrototype().
        getImplicitPrototype(), OBJECT_TYPE);
    assertTypeEquals(
        URI_ERROR_TYPE.getImplicitPrototype().getImplicitPrototype(),
        ERROR_TYPE);
    assertTypeEquals(
        RANGE_ERROR_TYPE.getImplicitPrototype().getImplicitPrototype(),
        ERROR_TYPE);
    assertTypeEquals(
        REFERENCE_ERROR_TYPE.getImplicitPrototype().
        getImplicitPrototype(), ERROR_TYPE);
    assertTypeEquals(
        STRING_OBJECT_TYPE.getImplicitPrototype().
        getImplicitPrototype(), OBJECT_TYPE);
    assertTypeEquals(
        REGEXP_TYPE.getImplicitPrototype().getImplicitPrototype(),
        OBJECT_TYPE);
    assertTypeEquals(
        SYNTAX_ERROR_TYPE.getImplicitPrototype().
        getImplicitPrototype(), ERROR_TYPE);
    assertTypeEquals(
        TYPE_ERROR_TYPE.getImplicitPrototype().
        getImplicitPrototype(), ERROR_TYPE);

    
    assertNotSame(EVAL_ERROR_TYPE.getImplicitPrototype(),
        URI_ERROR_TYPE.getImplicitPrototype());
    assertNotSame(EVAL_ERROR_TYPE.getImplicitPrototype(),
        RANGE_ERROR_TYPE.getImplicitPrototype());
    assertNotSame(EVAL_ERROR_TYPE.getImplicitPrototype(),
        REFERENCE_ERROR_TYPE.getImplicitPrototype());
    assertNotSame(EVAL_ERROR_TYPE.getImplicitPrototype(),
        SYNTAX_ERROR_TYPE.getImplicitPrototype());
    assertNotSame(EVAL_ERROR_TYPE.getImplicitPrototype(),
        TYPE_ERROR_TYPE.getImplicitPrototype());

    assertNotSame(URI_ERROR_TYPE.getImplicitPrototype(),
        RANGE_ERROR_TYPE.getImplicitPrototype());
    assertNotSame(URI_ERROR_TYPE.getImplicitPrototype(),
        REFERENCE_ERROR_TYPE.getImplicitPrototype());
    assertNotSame(URI_ERROR_TYPE.getImplicitPrototype(),
        SYNTAX_ERROR_TYPE.getImplicitPrototype());
    assertNotSame(URI_ERROR_TYPE.getImplicitPrototype(),
        TYPE_ERROR_TYPE.getImplicitPrototype());

    assertNotSame(RANGE_ERROR_TYPE.getImplicitPrototype(),
        REFERENCE_ERROR_TYPE.getImplicitPrototype());
    assertNotSame(RANGE_ERROR_TYPE.getImplicitPrototype(),
        SYNTAX_ERROR_TYPE.getImplicitPrototype());
    assertNotSame(RANGE_ERROR_TYPE.getImplicitPrototype(),
        TYPE_ERROR_TYPE.getImplicitPrototype());

    assertNotSame(REFERENCE_ERROR_TYPE.getImplicitPrototype(),
        SYNTAX_ERROR_TYPE.getImplicitPrototype());
    assertNotSame(REFERENCE_ERROR_TYPE.getImplicitPrototype(),
        TYPE_ERROR_TYPE.getImplicitPrototype());

    assertNotSame(SYNTAX_ERROR_TYPE.getImplicitPrototype(),
        TYPE_ERROR_TYPE.getImplicitPrototype());
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testInstanceFunctionChaining
  public void testInstanceFunctionChaining() throws Exception {
    
    assertTypeEquals(
        ARRAY_FUNCTION_TYPE, ARRAY_TYPE.getConstructor());

    
    assertTypeEquals(
        BOOLEAN_OBJECT_FUNCTION_TYPE,
        BOOLEAN_OBJECT_TYPE.getConstructor());

    
    assertTypeEquals(
        DATE_FUNCTION_TYPE, DATE_TYPE.getConstructor());

    
    assertTypeEquals(
        ERROR_FUNCTION_TYPE, ERROR_TYPE.getConstructor());

    
    assertTypeEquals(
        EVAL_ERROR_FUNCTION_TYPE, EVAL_ERROR_TYPE.getConstructor());

    
    assertTypeEquals(
        NUMBER_OBJECT_FUNCTION_TYPE,
        NUMBER_OBJECT_TYPE.getConstructor());

    
    assertTypeEquals(
        OBJECT_FUNCTION_TYPE, OBJECT_TYPE.getConstructor());

    
    assertTypeEquals(
        RANGE_ERROR_FUNCTION_TYPE, RANGE_ERROR_TYPE.getConstructor());

    
    assertTypeEquals(
        REFERENCE_ERROR_FUNCTION_TYPE,
        REFERENCE_ERROR_TYPE.getConstructor());

    
    assertTypeEquals(REGEXP_FUNCTION_TYPE, REGEXP_TYPE.getConstructor());

    
    assertTypeEquals(
        STRING_OBJECT_FUNCTION_TYPE,
        STRING_OBJECT_TYPE.getConstructor());

    
    assertTypeEquals(
        SYNTAX_ERROR_FUNCTION_TYPE,
        SYNTAX_ERROR_TYPE.getConstructor());

    
    assertTypeEquals(
        TYPE_ERROR_FUNCTION_TYPE, TYPE_ERROR_TYPE.getConstructor());

    
    assertTypeEquals(
        URI_ERROR_FUNCTION_TYPE, URI_ERROR_TYPE.getConstructor());
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testCanTestForEqualityWithCornerCases
  public void testCanTestForEqualityWithCornerCases() {
    
    assertCannotTestForEqualityWith(NULL_TYPE, VOID_TYPE);

    
    UnionType nullableObject =
        (UnionType) createUnionType(OBJECT_TYPE, NULL_TYPE);
    assertCanTestForEqualityWith(nullableObject, VOID_TYPE);
    assertCanTestForEqualityWith(VOID_TYPE, nullableObject);
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testTestForEquality
  public void testTestForEquality() {
    compare(TRUE, NO_OBJECT_TYPE, NO_OBJECT_TYPE);
    compare(UNKNOWN, ALL_TYPE, ALL_TYPE);
    compare(TRUE, NO_TYPE, NO_TYPE);
    compare(UNKNOWN, NO_RESOLVED_TYPE, NO_RESOLVED_TYPE);
    compare(UNKNOWN, NO_OBJECT_TYPE, NUMBER_TYPE);
    compare(UNKNOWN, ALL_TYPE, NUMBER_TYPE);
    compare(UNKNOWN, NO_TYPE, NUMBER_TYPE);

    compare(FALSE, NULL_TYPE, BOOLEAN_TYPE);
    compare(TRUE, NULL_TYPE, NULL_TYPE);
    compare(FALSE, NULL_TYPE, NUMBER_TYPE);
    compare(FALSE, NULL_TYPE, OBJECT_TYPE);
    compare(FALSE, NULL_TYPE, STRING_TYPE);
    compare(TRUE, NULL_TYPE, VOID_TYPE);
    compare(UNKNOWN, NULL_TYPE, createUnionType(UNKNOWN_TYPE, VOID_TYPE));
    compare(UNKNOWN, NULL_TYPE, createUnionType(OBJECT_TYPE, VOID_TYPE));
    compare(UNKNOWN, NULL_TYPE, unresolvedNamedType);
    compare(UNKNOWN,
        NULL_TYPE, createUnionType(unresolvedNamedType, DATE_TYPE));

    compare(FALSE, VOID_TYPE, REGEXP_TYPE);
    compare(TRUE, VOID_TYPE, VOID_TYPE);
    compare(UNKNOWN, VOID_TYPE, createUnionType(REGEXP_TYPE, VOID_TYPE));

    compare(UNKNOWN, NUMBER_TYPE, BOOLEAN_TYPE);
    compare(UNKNOWN, NUMBER_TYPE, NUMBER_TYPE);
    compare(UNKNOWN, NUMBER_TYPE, OBJECT_TYPE);

    compare(UNKNOWN, ARRAY_TYPE, BOOLEAN_TYPE);
    compare(UNKNOWN, OBJECT_TYPE, BOOLEAN_TYPE);
    compare(UNKNOWN, OBJECT_TYPE, STRING_TYPE);

    compare(UNKNOWN, STRING_TYPE, STRING_TYPE);

    compare(UNKNOWN, STRING_TYPE, BOOLEAN_TYPE);
    compare(UNKNOWN, STRING_TYPE, NUMBER_TYPE);
    compare(FALSE, STRING_TYPE, VOID_TYPE);
    compare(FALSE, STRING_TYPE, NULL_TYPE);
    compare(FALSE, STRING_TYPE, createUnionType(NULL_TYPE, VOID_TYPE));

    compare(UNKNOWN, UNKNOWN_TYPE, BOOLEAN_TYPE);
    compare(UNKNOWN, UNKNOWN_TYPE, NULL_TYPE);
    compare(UNKNOWN, UNKNOWN_TYPE, VOID_TYPE);

    compare(FALSE, U2U_CONSTRUCTOR_TYPE, BOOLEAN_TYPE);
    compare(FALSE, U2U_CONSTRUCTOR_TYPE, NUMBER_TYPE);
    compare(FALSE, U2U_CONSTRUCTOR_TYPE, STRING_TYPE);
    compare(FALSE, U2U_CONSTRUCTOR_TYPE, VOID_TYPE);
    compare(FALSE, U2U_CONSTRUCTOR_TYPE, NULL_TYPE);
    compare(UNKNOWN, U2U_CONSTRUCTOR_TYPE, OBJECT_TYPE);
    compare(UNKNOWN, U2U_CONSTRUCTOR_TYPE, ALL_TYPE);

    compare(UNKNOWN, NULL_TYPE, subclassOfUnresolvedNamedType);

    JSType functionAndNull = createUnionType(NULL_TYPE, dateMethod);
    compare(UNKNOWN, functionAndNull, dateMethod);

    compare(UNKNOWN, NULL_TYPE, NO_TYPE);
    compare(UNKNOWN, VOID_TYPE, NO_TYPE);
    compare(UNKNOWN, NULL_TYPE, unresolvedNamedType);
    compare(UNKNOWN, VOID_TYPE, unresolvedNamedType);
    compare(TRUE, NO_TYPE, NO_TYPE);
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testSubtypingSimpleTypes
  public void testSubtypingSimpleTypes() throws Exception {
    
    assertTrue(NO_TYPE.isSubtype(NO_TYPE));
    assertTrue(NO_TYPE.isSubtype(NO_OBJECT_TYPE));
    assertTrue(NO_TYPE.isSubtype(ARRAY_TYPE));
    assertTrue(NO_TYPE.isSubtype(BOOLEAN_TYPE));
    assertTrue(NO_TYPE.isSubtype(BOOLEAN_OBJECT_TYPE));
    assertTrue(NO_TYPE.isSubtype(DATE_TYPE));
    assertTrue(NO_TYPE.isSubtype(ERROR_TYPE));
    assertTrue(NO_TYPE.isSubtype(EVAL_ERROR_TYPE));
    assertTrue(NO_TYPE.isSubtype(functionType));
    assertTrue(NO_TYPE.isSubtype(NULL_TYPE));
    assertTrue(NO_TYPE.isSubtype(NUMBER_TYPE));
    assertTrue(NO_TYPE.isSubtype(NUMBER_OBJECT_TYPE));
    assertTrue(NO_TYPE.isSubtype(OBJECT_TYPE));
    assertTrue(NO_TYPE.isSubtype(URI_ERROR_TYPE));
    assertTrue(NO_TYPE.isSubtype(RANGE_ERROR_TYPE));
    assertTrue(NO_TYPE.isSubtype(REFERENCE_ERROR_TYPE));
    assertTrue(NO_TYPE.isSubtype(REGEXP_TYPE));
    assertTrue(NO_TYPE.isSubtype(STRING_TYPE));
    assertTrue(NO_TYPE.isSubtype(STRING_OBJECT_TYPE));
    assertTrue(NO_TYPE.isSubtype(SYNTAX_ERROR_TYPE));
    assertTrue(NO_TYPE.isSubtype(TYPE_ERROR_TYPE));
    assertTrue(NO_TYPE.isSubtype(ALL_TYPE));
    assertTrue(NO_TYPE.isSubtype(VOID_TYPE));

    
    assertFalse(NO_OBJECT_TYPE.isSubtype(NO_TYPE));
    assertTrue(NO_OBJECT_TYPE.isSubtype(NO_OBJECT_TYPE));
    assertTrue(NO_OBJECT_TYPE.isSubtype(ARRAY_TYPE));
    assertFalse(NO_OBJECT_TYPE.isSubtype(BOOLEAN_TYPE));
    assertTrue(NO_OBJECT_TYPE.isSubtype(BOOLEAN_OBJECT_TYPE));
    assertTrue(NO_OBJECT_TYPE.isSubtype(DATE_TYPE));
    assertTrue(NO_OBJECT_TYPE.isSubtype(ERROR_TYPE));
    assertTrue(NO_OBJECT_TYPE.isSubtype(EVAL_ERROR_TYPE));
    assertTrue(NO_OBJECT_TYPE.isSubtype(functionType));
    assertFalse(NO_OBJECT_TYPE.isSubtype(NULL_TYPE));
    assertFalse(NO_OBJECT_TYPE.isSubtype(NUMBER_TYPE));
    assertTrue(NO_OBJECT_TYPE.isSubtype(NUMBER_OBJECT_TYPE));
    assertTrue(NO_OBJECT_TYPE.isSubtype(OBJECT_TYPE));
    assertTrue(NO_OBJECT_TYPE.isSubtype(URI_ERROR_TYPE));
    assertTrue(NO_OBJECT_TYPE.isSubtype(RANGE_ERROR_TYPE));
    assertTrue(NO_OBJECT_TYPE.isSubtype(REFERENCE_ERROR_TYPE));
    assertTrue(NO_OBJECT_TYPE.isSubtype(REGEXP_TYPE));
    assertFalse(NO_OBJECT_TYPE.isSubtype(STRING_TYPE));
    assertTrue(NO_OBJECT_TYPE.isSubtype(STRING_OBJECT_TYPE));
    assertTrue(NO_OBJECT_TYPE.isSubtype(SYNTAX_ERROR_TYPE));
    assertTrue(NO_OBJECT_TYPE.isSubtype(TYPE_ERROR_TYPE));
    assertTrue(NO_OBJECT_TYPE.isSubtype(ALL_TYPE));
    assertFalse(NO_OBJECT_TYPE.isSubtype(VOID_TYPE));

    
    assertFalse(ARRAY_TYPE.isSubtype(NO_TYPE));
    assertFalse(ARRAY_TYPE.isSubtype(NO_OBJECT_TYPE));
    assertTrue(ARRAY_TYPE.isSubtype(ARRAY_TYPE));
    assertFalse(ARRAY_TYPE.isSubtype(BOOLEAN_TYPE));
    assertFalse(ARRAY_TYPE.isSubtype(BOOLEAN_OBJECT_TYPE));
    assertFalse(ARRAY_TYPE.isSubtype(DATE_TYPE));
    assertFalse(ARRAY_TYPE.isSubtype(ERROR_TYPE));
    assertFalse(ARRAY_TYPE.isSubtype(EVAL_ERROR_TYPE));
    assertFalse(ARRAY_TYPE.isSubtype(functionType));
    assertFalse(ARRAY_TYPE.isSubtype(NULL_TYPE));
    assertFalse(ARRAY_TYPE.isSubtype(NUMBER_TYPE));
    assertFalse(ARRAY_TYPE.isSubtype(NUMBER_OBJECT_TYPE));
    assertTrue(ARRAY_TYPE.isSubtype(OBJECT_TYPE));
    assertFalse(ARRAY_TYPE.isSubtype(URI_ERROR_TYPE));
    assertFalse(ARRAY_TYPE.isSubtype(RANGE_ERROR_TYPE));
    assertFalse(ARRAY_TYPE.isSubtype(REFERENCE_ERROR_TYPE));
    assertFalse(ARRAY_TYPE.isSubtype(REGEXP_TYPE));
    assertFalse(ARRAY_TYPE.isSubtype(STRING_TYPE));
    assertFalse(ARRAY_TYPE.isSubtype(STRING_OBJECT_TYPE));
    assertFalse(ARRAY_TYPE.isSubtype(SYNTAX_ERROR_TYPE));
    assertFalse(ARRAY_TYPE.isSubtype(TYPE_ERROR_TYPE));
    assertTrue(ARRAY_TYPE.isSubtype(ALL_TYPE));
    assertFalse(ARRAY_TYPE.isSubtype(VOID_TYPE));

    
    assertFalse(BOOLEAN_TYPE.isSubtype(NO_TYPE));
    assertFalse(BOOLEAN_TYPE.isSubtype(NO_OBJECT_TYPE));
    assertFalse(BOOLEAN_TYPE.isSubtype(ARRAY_TYPE));
    assertTrue(BOOLEAN_TYPE.isSubtype(BOOLEAN_TYPE));
    assertFalse(BOOLEAN_TYPE.isSubtype(BOOLEAN_OBJECT_TYPE));
    assertFalse(BOOLEAN_TYPE.isSubtype(DATE_TYPE));
    assertFalse(BOOLEAN_TYPE.isSubtype(ERROR_TYPE));
    assertFalse(BOOLEAN_TYPE.isSubtype(EVAL_ERROR_TYPE));
    assertFalse(BOOLEAN_TYPE.isSubtype(functionType));
    assertFalse(BOOLEAN_TYPE.isSubtype(NULL_TYPE));
    assertFalse(BOOLEAN_TYPE.isSubtype(NUMBER_TYPE));
    assertFalse(BOOLEAN_TYPE.isSubtype(NUMBER_OBJECT_TYPE));
    assertFalse(BOOLEAN_TYPE.isSubtype(OBJECT_TYPE));
    assertFalse(BOOLEAN_TYPE.isSubtype(URI_ERROR_TYPE));
    assertFalse(BOOLEAN_TYPE.isSubtype(RANGE_ERROR_TYPE));
    assertFalse(BOOLEAN_TYPE.isSubtype(REFERENCE_ERROR_TYPE));
    assertFalse(BOOLEAN_TYPE.isSubtype(REGEXP_TYPE));
    assertFalse(BOOLEAN_TYPE.isSubtype(STRING_TYPE));
    assertFalse(BOOLEAN_TYPE.isSubtype(STRING_OBJECT_TYPE));
    assertFalse(BOOLEAN_TYPE.isSubtype(SYNTAX_ERROR_TYPE));
    assertFalse(BOOLEAN_TYPE.isSubtype(TYPE_ERROR_TYPE));
    assertTrue(BOOLEAN_TYPE.isSubtype(ALL_TYPE));
    assertFalse(BOOLEAN_TYPE.isSubtype(VOID_TYPE));

    
    assertFalse(BOOLEAN_OBJECT_TYPE.isSubtype(NO_TYPE));
    assertFalse(BOOLEAN_OBJECT_TYPE.isSubtype(NO_OBJECT_TYPE));
    assertFalse(BOOLEAN_OBJECT_TYPE.isSubtype(ARRAY_TYPE));
    assertFalse(BOOLEAN_OBJECT_TYPE.isSubtype(BOOLEAN_TYPE));
    assertTrue(BOOLEAN_OBJECT_TYPE.isSubtype(BOOLEAN_OBJECT_TYPE));
    assertFalse(BOOLEAN_OBJECT_TYPE.isSubtype(DATE_TYPE));
    assertFalse(BOOLEAN_OBJECT_TYPE.isSubtype(ERROR_TYPE));
    assertFalse(BOOLEAN_OBJECT_TYPE.isSubtype(EVAL_ERROR_TYPE));
    assertFalse(BOOLEAN_OBJECT_TYPE.isSubtype(functionType));
    assertFalse(BOOLEAN_OBJECT_TYPE.isSubtype(NULL_TYPE));
    assertFalse(BOOLEAN_OBJECT_TYPE.isSubtype(NUMBER_TYPE));
    assertFalse(BOOLEAN_OBJECT_TYPE.isSubtype(NUMBER_OBJECT_TYPE));
    assertTrue(BOOLEAN_OBJECT_TYPE.isSubtype(OBJECT_TYPE));
    assertFalse(BOOLEAN_OBJECT_TYPE.isSubtype(URI_ERROR_TYPE));
    assertFalse(BOOLEAN_OBJECT_TYPE.isSubtype(RANGE_ERROR_TYPE));
    assertFalse(BOOLEAN_OBJECT_TYPE.isSubtype(REFERENCE_ERROR_TYPE));
    assertFalse(BOOLEAN_OBJECT_TYPE.isSubtype(REGEXP_TYPE));
    assertFalse(BOOLEAN_OBJECT_TYPE.isSubtype(STRING_TYPE));
    assertFalse(BOOLEAN_OBJECT_TYPE.isSubtype(STRING_OBJECT_TYPE));
    assertFalse(BOOLEAN_OBJECT_TYPE.isSubtype(SYNTAX_ERROR_TYPE));
    assertFalse(BOOLEAN_OBJECT_TYPE.isSubtype(TYPE_ERROR_TYPE));
    assertTrue(BOOLEAN_OBJECT_TYPE.isSubtype(ALL_TYPE));
    assertFalse(BOOLEAN_OBJECT_TYPE.isSubtype(VOID_TYPE));

    
    assertFalse(DATE_TYPE.isSubtype(NO_TYPE));
    assertFalse(DATE_TYPE.isSubtype(NO_OBJECT_TYPE));
    assertFalse(DATE_TYPE.isSubtype(ARRAY_TYPE));
    assertFalse(DATE_TYPE.isSubtype(BOOLEAN_TYPE));
    assertFalse(DATE_TYPE.isSubtype(BOOLEAN_OBJECT_TYPE));
    assertTrue(DATE_TYPE.isSubtype(DATE_TYPE));
    assertFalse(DATE_TYPE.isSubtype(ERROR_TYPE));
    assertFalse(DATE_TYPE.isSubtype(EVAL_ERROR_TYPE));
    assertFalse(DATE_TYPE.isSubtype(functionType));
    assertFalse(DATE_TYPE.isSubtype(NULL_TYPE));
    assertFalse(DATE_TYPE.isSubtype(NUMBER_TYPE));
    assertFalse(DATE_TYPE.isSubtype(NUMBER_OBJECT_TYPE));
    assertTrue(DATE_TYPE.isSubtype(OBJECT_TYPE));
    assertFalse(DATE_TYPE.isSubtype(URI_ERROR_TYPE));
    assertFalse(DATE_TYPE.isSubtype(RANGE_ERROR_TYPE));
    assertFalse(DATE_TYPE.isSubtype(REFERENCE_ERROR_TYPE));
    assertFalse(DATE_TYPE.isSubtype(REGEXP_TYPE));
    assertFalse(DATE_TYPE.isSubtype(STRING_TYPE));
    assertFalse(DATE_TYPE.isSubtype(STRING_OBJECT_TYPE));
    assertFalse(DATE_TYPE.isSubtype(SYNTAX_ERROR_TYPE));
    assertFalse(DATE_TYPE.isSubtype(TYPE_ERROR_TYPE));
    assertTrue(DATE_TYPE.isSubtype(ALL_TYPE));
    assertFalse(DATE_TYPE.isSubtype(VOID_TYPE));

    
    assertFalse(ERROR_TYPE.isSubtype(NO_TYPE));
    assertFalse(ERROR_TYPE.isSubtype(NO_OBJECT_TYPE));
    assertFalse(ERROR_TYPE.isSubtype(ARRAY_TYPE));
    assertFalse(ERROR_TYPE.isSubtype(BOOLEAN_TYPE));
    assertFalse(ERROR_TYPE.isSubtype(BOOLEAN_OBJECT_TYPE));
    assertFalse(ERROR_TYPE.isSubtype(DATE_TYPE));
    assertTrue(ERROR_TYPE.isSubtype(ERROR_TYPE));
    assertFalse(ERROR_TYPE.isSubtype(EVAL_ERROR_TYPE));
    assertFalse(ERROR_TYPE.isSubtype(functionType));
    assertFalse(ERROR_TYPE.isSubtype(NULL_TYPE));
    assertFalse(ERROR_TYPE.isSubtype(NUMBER_TYPE));
    assertFalse(ERROR_TYPE.isSubtype(NUMBER_OBJECT_TYPE));
    assertTrue(ERROR_TYPE.isSubtype(OBJECT_TYPE));
    assertFalse(ERROR_TYPE.isSubtype(URI_ERROR_TYPE));
    assertFalse(ERROR_TYPE.isSubtype(RANGE_ERROR_TYPE));
    assertFalse(ERROR_TYPE.isSubtype(REFERENCE_ERROR_TYPE));
    assertFalse(ERROR_TYPE.isSubtype(REGEXP_TYPE));
    assertFalse(ERROR_TYPE.isSubtype(STRING_TYPE));
    assertFalse(ERROR_TYPE.isSubtype(STRING_OBJECT_TYPE));
    assertFalse(ERROR_TYPE.isSubtype(SYNTAX_ERROR_TYPE));
    assertFalse(ERROR_TYPE.isSubtype(TYPE_ERROR_TYPE));
    assertTrue(ERROR_TYPE.isSubtype(ALL_TYPE));
    assertFalse(ERROR_TYPE.isSubtype(VOID_TYPE));

    
    assertFalse(EVAL_ERROR_TYPE.isSubtype(NO_TYPE));
    assertFalse(EVAL_ERROR_TYPE.isSubtype(NO_OBJECT_TYPE));
    assertFalse(EVAL_ERROR_TYPE.isSubtype(ARRAY_TYPE));
    assertFalse(EVAL_ERROR_TYPE.isSubtype(BOOLEAN_TYPE));
    assertFalse(EVAL_ERROR_TYPE.isSubtype(BOOLEAN_OBJECT_TYPE));
    assertFalse(ERROR_TYPE.isSubtype(DATE_TYPE));
    assertTrue(EVAL_ERROR_TYPE.isSubtype(ERROR_TYPE));
    assertTrue(EVAL_ERROR_TYPE.isSubtype(EVAL_ERROR_TYPE));
    assertFalse(EVAL_ERROR_TYPE.isSubtype(functionType));
    assertFalse(EVAL_ERROR_TYPE.isSubtype(NULL_TYPE));
    assertFalse(EVAL_ERROR_TYPE.isSubtype(NUMBER_TYPE));
    assertFalse(EVAL_ERROR_TYPE.isSubtype(NUMBER_OBJECT_TYPE));
    assertTrue(EVAL_ERROR_TYPE.isSubtype(OBJECT_TYPE));
    assertFalse(EVAL_ERROR_TYPE.isSubtype(URI_ERROR_TYPE));
    assertFalse(EVAL_ERROR_TYPE.isSubtype(RANGE_ERROR_TYPE));
    assertFalse(EVAL_ERROR_TYPE.isSubtype(REFERENCE_ERROR_TYPE));
    assertFalse(EVAL_ERROR_TYPE.isSubtype(REGEXP_TYPE));
    assertFalse(EVAL_ERROR_TYPE.isSubtype(STRING_TYPE));
    assertFalse(EVAL_ERROR_TYPE.isSubtype(STRING_OBJECT_TYPE));
    assertFalse(EVAL_ERROR_TYPE.isSubtype(SYNTAX_ERROR_TYPE));
    assertFalse(EVAL_ERROR_TYPE.isSubtype(TYPE_ERROR_TYPE));
    assertTrue(EVAL_ERROR_TYPE.isSubtype(ALL_TYPE));
    assertFalse(EVAL_ERROR_TYPE.isSubtype(VOID_TYPE));

    
    assertTrue(RANGE_ERROR_TYPE.isSubtype(ERROR_TYPE));

    
    assertTrue(REFERENCE_ERROR_TYPE.isSubtype(ERROR_TYPE));

    
    assertTrue(TYPE_ERROR_TYPE.isSubtype(ERROR_TYPE));

    
    assertTrue(URI_ERROR_TYPE.isSubtype(ERROR_TYPE));

    
    assertFalse(ALL_TYPE.isSubtype(NO_TYPE));
    assertFalse(ALL_TYPE.isSubtype(NO_OBJECT_TYPE));
    assertFalse(ALL_TYPE.isSubtype(ARRAY_TYPE));
    assertFalse(ALL_TYPE.isSubtype(BOOLEAN_TYPE));
    assertFalse(ALL_TYPE.isSubtype(BOOLEAN_OBJECT_TYPE));
    assertFalse(ERROR_TYPE.isSubtype(DATE_TYPE));
    assertFalse(ALL_TYPE.isSubtype(ERROR_TYPE));
    assertFalse(ALL_TYPE.isSubtype(EVAL_ERROR_TYPE));
    assertFalse(ALL_TYPE.isSubtype(functionType));
    assertFalse(ALL_TYPE.isSubtype(NULL_TYPE));
    assertFalse(ALL_TYPE.isSubtype(NUMBER_TYPE));
    assertFalse(ALL_TYPE.isSubtype(NUMBER_OBJECT_TYPE));
    assertFalse(ALL_TYPE.isSubtype(OBJECT_TYPE));
    assertFalse(ALL_TYPE.isSubtype(URI_ERROR_TYPE));
    assertFalse(ALL_TYPE.isSubtype(RANGE_ERROR_TYPE));
    assertFalse(ALL_TYPE.isSubtype(REFERENCE_ERROR_TYPE));
    assertFalse(ALL_TYPE.isSubtype(REGEXP_TYPE));
    assertFalse(ALL_TYPE.isSubtype(STRING_TYPE));
    assertFalse(ALL_TYPE.isSubtype(STRING_OBJECT_TYPE));
    assertFalse(ALL_TYPE.isSubtype(SYNTAX_ERROR_TYPE));
    assertFalse(ALL_TYPE.isSubtype(TYPE_ERROR_TYPE));
    assertTrue(ALL_TYPE.isSubtype(ALL_TYPE));
    assertFalse(ALL_TYPE.isSubtype(VOID_TYPE));
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testSubtypingObjectTopOfObjects
  public void testSubtypingObjectTopOfObjects() throws Exception {
    assertTrue(OBJECT_TYPE.isSubtype(OBJECT_TYPE));
    assertTrue(createUnionType(DATE_TYPE, REGEXP_TYPE).isSubtype(OBJECT_TYPE));
    assertTrue(createUnionType(OBJECT_TYPE, NO_OBJECT_TYPE).
        isSubtype(OBJECT_TYPE));
    assertTrue(functionType.isSubtype(OBJECT_TYPE));
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testSubtypingFunctionPrototypeType
  public void testSubtypingFunctionPrototypeType() throws Exception {
    FunctionType sub1 = registry.createConstructorType(null, null, null, null);
    sub1.setPrototypeBasedOn(googBar);
    FunctionType sub2 = registry.createConstructorType(null, null, null, null);
    sub2.setPrototypeBasedOn(googBar);

    ObjectType o1 = sub1.getInstanceType();
    ObjectType o2 = sub2.getInstanceType();

    assertFalse(o1.isSubtype(o2));
    assertFalse(o1.getImplicitPrototype().isSubtype(o2.getImplicitPrototype()));
    assertTrue(o1.getImplicitPrototype().isSubtype(googBar));
    assertTrue(o2.getImplicitPrototype().isSubtype(googBar));
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testSubtypingFunctionFixedArgs
  public void testSubtypingFunctionFixedArgs() throws Exception {
    FunctionType f1 = registry.createFunctionType(OBJECT_TYPE,
        false, BOOLEAN_TYPE);
    FunctionType f2 = registry.createFunctionType(STRING_OBJECT_TYPE,
        false, BOOLEAN_TYPE);

    assertTrue(f1.isSubtype(f1));
    assertFalse(f1.isSubtype(f2));
    assertTrue(f2.isSubtype(f1));
    assertTrue(f2.isSubtype(f2));

    assertTrue(f1.isSubtype(U2U_CONSTRUCTOR_TYPE));
    assertTrue(f2.isSubtype(U2U_CONSTRUCTOR_TYPE));
    assertTrue(U2U_CONSTRUCTOR_TYPE.isSubtype(f1));
    assertTrue(U2U_CONSTRUCTOR_TYPE.isSubtype(f2));
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testSubtypingFunctionMultipleFixedArgs
  public void testSubtypingFunctionMultipleFixedArgs() throws Exception {
    FunctionType f1 = registry.createFunctionType(OBJECT_TYPE,
        false, EVAL_ERROR_TYPE, STRING_TYPE);
    FunctionType f2 = registry.createFunctionType(STRING_OBJECT_TYPE,
        false, ERROR_TYPE, ALL_TYPE);

    assertTrue(f1.isSubtype(f1));
    assertFalse(f1.isSubtype(f2));
    assertTrue(f2.isSubtype(f1));
    assertTrue(f2.isSubtype(f2));

    assertTrue(f1.isSubtype(U2U_CONSTRUCTOR_TYPE));
    assertTrue(f2.isSubtype(U2U_CONSTRUCTOR_TYPE));
    assertTrue(U2U_CONSTRUCTOR_TYPE.isSubtype(f1));
    assertTrue(U2U_CONSTRUCTOR_TYPE.isSubtype(f2));
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testSubtypingFunctionFixedArgsNotMatching
  public void testSubtypingFunctionFixedArgsNotMatching() throws Exception {
    FunctionType f1 = registry.createFunctionType(OBJECT_TYPE,
        false, EVAL_ERROR_TYPE);
    FunctionType f2 = registry.createFunctionType(STRING_OBJECT_TYPE,
        false, ERROR_TYPE, ALL_TYPE);

    assertTrue(f1.isSubtype(f1));
    assertFalse(f1.isSubtype(f2));
    assertTrue(f2.isSubtype(f1));
    assertTrue(f2.isSubtype(f2));

    assertTrue(f1.isSubtype(U2U_CONSTRUCTOR_TYPE));
    assertTrue(f2.isSubtype(U2U_CONSTRUCTOR_TYPE));
    assertTrue(U2U_CONSTRUCTOR_TYPE.isSubtype(f1));
    assertTrue(U2U_CONSTRUCTOR_TYPE.isSubtype(f2));
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testSubtypingFunctionVariableArgsOneOnly
  public void testSubtypingFunctionVariableArgsOneOnly() throws Exception {
    
    FunctionType f1 = registry.createFunctionType(OBJECT_TYPE,
        true, EVAL_ERROR_TYPE);
    
    FunctionType f2 = registry.createFunctionType(STRING_OBJECT_TYPE,
        false, ERROR_TYPE, OBJECT_TYPE);

    assertTrue(f1.isSubtype(f1));
    assertFalse(f1.isSubtype(f2));
    assertFalse(f2.isSubtype(f1));
    assertTrue(f2.isSubtype(f2));

    assertTrue(f1.isSubtype(U2U_CONSTRUCTOR_TYPE));
    assertTrue(f2.isSubtype(U2U_CONSTRUCTOR_TYPE));
    assertTrue(U2U_CONSTRUCTOR_TYPE.isSubtype(f1));
    assertTrue(U2U_CONSTRUCTOR_TYPE.isSubtype(f2));
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testSubtypingFunctionVariableArgsBoth
  public void testSubtypingFunctionVariableArgsBoth() throws Exception {
    
    FunctionType f1 = registry.createFunctionType(OBJECT_TYPE,
        true,  URI_ERROR_TYPE, EVAL_ERROR_TYPE, EVAL_ERROR_TYPE);
    
    FunctionType f2 = registry.createFunctionType(STRING_OBJECT_TYPE,
        true, ERROR_TYPE, OBJECT_TYPE, EVAL_ERROR_TYPE);

    assertTrue(f1.isSubtype(f1));
    assertFalse(f1.isSubtype(f2));
    assertTrue(f2.isSubtype(f1));
    assertTrue(f2.isSubtype(f2));

    assertTrue(f1.isSubtype(U2U_CONSTRUCTOR_TYPE));
    assertTrue(f2.isSubtype(U2U_CONSTRUCTOR_TYPE));
    assertTrue(U2U_CONSTRUCTOR_TYPE.isSubtype(f1));
    assertTrue(U2U_CONSTRUCTOR_TYPE.isSubtype(f2));
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testSubtypingMostGeneralFunction
  public void testSubtypingMostGeneralFunction() throws Exception {
    
    FunctionType f1 = registry.createFunctionType(OBJECT_TYPE,
        false, EVAL_ERROR_TYPE, STRING_TYPE);
    
    FunctionType f2 = registry.createFunctionType(NUMBER_TYPE,
        false, STRING_TYPE, VOID_TYPE);
    
    FunctionType f3 = registry.createFunctionType(NO_OBJECT_TYPE,
        false, DATE_TYPE, STRING_TYPE, NUMBER_TYPE);
    
    FunctionType f4 = registry.createFunctionType(NO_TYPE,
        false, NUMBER_OBJECT_TYPE);
    
    FunctionType f5 = registry.createFunctionType(OBJECT_TYPE,
        true, EVAL_ERROR_TYPE);
    
    FunctionType f6 = registry.createFunctionType(STRING_OBJECT_TYPE,
        false, ERROR_TYPE, OBJECT_TYPE);
    
    FunctionType f7 = registry.createFunctionType(OBJECT_TYPE,
        true,  URI_ERROR_TYPE, EVAL_ERROR_TYPE);
    
    FunctionType f8 = registry.createFunctionType(STRING_OBJECT_TYPE,
        true, ERROR_TYPE, OBJECT_TYPE, EVAL_ERROR_TYPE);

    assertTrue(LEAST_FUNCTION_TYPE.isSubtype(GREATEST_FUNCTION_TYPE));
    assertTrue(LEAST_FUNCTION_TYPE.isSubtype(U2U_CONSTRUCTOR_TYPE));
    assertTrue(U2U_CONSTRUCTOR_TYPE.isSubtype(LEAST_FUNCTION_TYPE));

    assertFalse(GREATEST_FUNCTION_TYPE.isSubtype(LEAST_FUNCTION_TYPE));
    assertTrue(GREATEST_FUNCTION_TYPE.isSubtype(U2U_CONSTRUCTOR_TYPE));
    assertTrue(U2U_CONSTRUCTOR_TYPE.isSubtype(GREATEST_FUNCTION_TYPE));

    assertTrue(f1.isSubtype(GREATEST_FUNCTION_TYPE));
    assertTrue(f2.isSubtype(GREATEST_FUNCTION_TYPE));
    assertTrue(f3.isSubtype(GREATEST_FUNCTION_TYPE));
    assertTrue(f4.isSubtype(GREATEST_FUNCTION_TYPE));
    assertTrue(f5.isSubtype(GREATEST_FUNCTION_TYPE));
    assertTrue(f6.isSubtype(GREATEST_FUNCTION_TYPE));
    assertTrue(f7.isSubtype(GREATEST_FUNCTION_TYPE));
    assertTrue(f8.isSubtype(GREATEST_FUNCTION_TYPE));

    assertFalse(f1.isSubtype(LEAST_FUNCTION_TYPE));
    assertFalse(f2.isSubtype(LEAST_FUNCTION_TYPE));
    assertFalse(f3.isSubtype(LEAST_FUNCTION_TYPE));
    assertFalse(f4.isSubtype(LEAST_FUNCTION_TYPE));
    assertFalse(f5.isSubtype(LEAST_FUNCTION_TYPE));
    assertFalse(f6.isSubtype(LEAST_FUNCTION_TYPE));
    assertFalse(f7.isSubtype(LEAST_FUNCTION_TYPE));
    assertFalse(f8.isSubtype(LEAST_FUNCTION_TYPE));

    assertTrue(LEAST_FUNCTION_TYPE.isSubtype(f1));
    assertTrue(LEAST_FUNCTION_TYPE.isSubtype(f2));
    assertTrue(LEAST_FUNCTION_TYPE.isSubtype(f3));
    assertTrue(LEAST_FUNCTION_TYPE.isSubtype(f4));
    assertTrue(LEAST_FUNCTION_TYPE.isSubtype(f5));
    assertTrue(LEAST_FUNCTION_TYPE.isSubtype(f6));
    assertTrue(LEAST_FUNCTION_TYPE.isSubtype(f7));
    assertTrue(LEAST_FUNCTION_TYPE.isSubtype(f8));

    assertFalse(GREATEST_FUNCTION_TYPE.isSubtype(f1));
    assertFalse(GREATEST_FUNCTION_TYPE.isSubtype(f2));
    assertFalse(GREATEST_FUNCTION_TYPE.isSubtype(f3));
    assertFalse(GREATEST_FUNCTION_TYPE.isSubtype(f4));
    assertFalse(GREATEST_FUNCTION_TYPE.isSubtype(f5));
    assertFalse(GREATEST_FUNCTION_TYPE.isSubtype(f6));
    assertFalse(GREATEST_FUNCTION_TYPE.isSubtype(f7));
    assertFalse(GREATEST_FUNCTION_TYPE.isSubtype(f8));
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testSymmetryOfTestForEquality
  public void testSymmetryOfTestForEquality() {
    List<JSType> listA = getTypesToTestForSymmetry();
    List<JSType> listB = getTypesToTestForSymmetry();
    for (JSType typeA : listA) {
      for (JSType typeB : listB) {
        TernaryValue aOnB = typeA.testForEquality(typeB);
        TernaryValue bOnA = typeB.testForEquality(typeA);
        assertTrue(
            String.format("testForEquality not symmetrical:\n" +
                "typeA: %s\ntypeB: %s\n" +
                "a.testForEquality(b): %s\n" +
                "b.testForEquality(a): %s\n",
                typeA, typeB, aOnB, bOnA),
            aOnB == bOnA);
      }
    }
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testSymmetryOfLeastSupertype
  public void testSymmetryOfLeastSupertype() {
    List<JSType> listA = getTypesToTestForSymmetry();
    List<JSType> listB = getTypesToTestForSymmetry();
    for (JSType typeA : listA) {
      for (JSType typeB : listB) {
        JSType aOnB = typeA.getLeastSupertype(typeB);
        JSType bOnA = typeB.getLeastSupertype(typeA);

        
        
        assertTrue(
            String.format("getLeastSupertype not symmetrical:\n" +
                "typeA: %s\ntypeB: %s\n" +
                "a.getLeastSupertype(b): %s\n" +
                "b.getLeastSupertype(a): %s\n",
                typeA, typeB, aOnB, bOnA),
            aOnB.isEquivalentTo(bOnA));
      }
    }
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testWeirdBug
  public void testWeirdBug() {
    assertTypeNotEquals(googBar, googBar.getInstanceType());
    assertFalse(googBar.isSubtype(googBar.getInstanceType()));
    assertFalse(googBar.getInstanceType().isSubtype(googBar));
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testSymmetryOfGreatestSubtype
  public void testSymmetryOfGreatestSubtype() {
    List<JSType> listA = getTypesToTestForSymmetry();
    List<JSType> listB = getTypesToTestForSymmetry();
    for (JSType typeA : listA) {
      for (JSType typeB : listB) {
        JSType aOnB = typeA.getGreatestSubtype(typeB);
        JSType bOnA = typeB.getGreatestSubtype(typeA);

        
        
        assertTrue(
            String.format("getGreatestSubtype not symmetrical:\n" +
                "typeA: %s\ntypeB: %s\n" +
                "a.getGreatestSubtype(b): %s\n" +
                "b.getGreatestSubtype(a): %s\n",
                typeA, typeB, aOnB, bOnA),
            aOnB.isEquivalentTo(bOnA));
      }
    }
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testReflexivityOfLeastSupertype
  public void testReflexivityOfLeastSupertype() {
    List<JSType> list = getTypesToTestForSymmetry();
    for (JSType type : list) {
      assertTypeEquals("getLeastSupertype not reflexive",
          type, type.getLeastSupertype(type));
    }
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testReflexivityOfGreatestSubtype
  public void testReflexivityOfGreatestSubtype() {
    List<JSType> list = getTypesToTestForSymmetry();
    for (JSType type : list) {
      assertTypeEquals("getGreatestSubtype not reflexive",
          type, type.getGreatestSubtype(type));
    }
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testLeastSupertypeUnresolvedNamedType
  public void testLeastSupertypeUnresolvedNamedType() {
    
    JSType expected = registry.createUnionType(
        unresolvedNamedType, U2U_FUNCTION_TYPE);
    assertTypeEquals(expected,
        unresolvedNamedType.getLeastSupertype(U2U_FUNCTION_TYPE));
    assertTypeEquals(expected,
        U2U_FUNCTION_TYPE.getLeastSupertype(unresolvedNamedType));
    assertEquals("(function (...[?]): ?|not.resolved.named.type)",
        expected.toString());
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testLeastSupertypeUnresolvedNamedType2
  public void testLeastSupertypeUnresolvedNamedType2() {
    JSType expected = registry.createUnionType(
        unresolvedNamedType, UNKNOWN_TYPE);
    assertTypeEquals(expected,
        unresolvedNamedType.getLeastSupertype(UNKNOWN_TYPE));
    assertTypeEquals(expected,
        UNKNOWN_TYPE.getLeastSupertype(unresolvedNamedType));
    assertTypeEquals(UNKNOWN_TYPE, expected);
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testLeastSupertypeUnresolvedNamedType3
  public void testLeastSupertypeUnresolvedNamedType3() {
    JSType expected = registry.createUnionType(
        unresolvedNamedType, CHECKED_UNKNOWN_TYPE);
    assertTypeEquals(expected,
        unresolvedNamedType.getLeastSupertype(CHECKED_UNKNOWN_TYPE));
    assertTypeEquals(expected,
        CHECKED_UNKNOWN_TYPE.getLeastSupertype(unresolvedNamedType));
    assertTypeEquals(CHECKED_UNKNOWN_TYPE, expected);
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testSubclassOfUnresolvedNamedType
  public void testSubclassOfUnresolvedNamedType() {
    assertTrue(subclassOfUnresolvedNamedType.isUnknownType());
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testSupertypeOfProxiedFunctionTypes
  public void testSupertypeOfProxiedFunctionTypes() {
    ObjectType fn1 =
        new FunctionBuilder(registry)
        .withParamsNode(new Node(Token.PARAM_LIST))
        .withReturnType(NUMBER_TYPE)
        .build();
    ObjectType fn2 =
        new FunctionBuilder(registry)
        .withParamsNode(new Node(Token.PARAM_LIST))
        .withReturnType(STRING_TYPE)
        .build();
    ObjectType p1 = new ProxyObjectType(registry, fn1);
    ObjectType p2 = new ProxyObjectType(registry, fn2);
    ObjectType supremum =
        new FunctionBuilder(registry)
        .withParamsNode(new Node(Token.PARAM_LIST))
        .withReturnType(registry.createUnionType(STRING_TYPE, NUMBER_TYPE))
        .build();

    assertTypeEquals(fn1.getLeastSupertype(fn2), p1.getLeastSupertype(p2));
    assertTypeEquals(supremum, fn1.getLeastSupertype(fn2));
    assertTypeEquals(supremum, fn1.getLeastSupertype(p2));
    assertTypeEquals(supremum, p1.getLeastSupertype(fn2));
    assertTypeEquals(supremum, p1.getLeastSupertype(p2));
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testTypeOfThisIsProxied
  public void testTypeOfThisIsProxied() {
    ObjectType fnType = new FunctionBuilder(registry)
        .withReturnType(NUMBER_TYPE).withTypeOfThis(OBJECT_TYPE).build();
    ObjectType proxyType = new ProxyObjectType(registry, fnType);
    assertTypeEquals(fnType.getTypeOfThis(), proxyType.getTypeOfThis());
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testNamedTypeEquals
  public void testNamedTypeEquals() {
    JSTypeRegistry jst = new JSTypeRegistry(null);

    
    NamedType a = new NamedType(jst, "type1", "source", 1, 0);
    NamedType b = new NamedType(jst, "type1", "source", 1, 0);
    assertTrue(a.isEquivalentTo(b));

    
    assertTrue(namedGoogBar.isEquivalentTo(googBar.getInstanceType()));
    assertTrue(googBar.getInstanceType().isEquivalentTo(namedGoogBar));
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testNamedTypeEquals2
  public void testNamedTypeEquals2() {
    
    NamedType a = new NamedType(registry, "typeA", "source", 1, 0);
    NamedType b = new NamedType(registry, "typeB", "source", 1, 0);

    ObjectType realA = registry.createConstructorType(
        "typeA", null, null, null).getInstanceType();
    ObjectType realB = registry.createEnumType(
        "typeB", null, NUMBER_TYPE).getElementsType();
    registry.declareType("typeA", realA);
    registry.declareType("typeB", realB);

    assertEquals(a.hashCode(), realA.hashCode());
    assertTypeEquals(a, realA);
    assertEquals(b.hashCode(), realB.hashCode());
    assertTypeEquals(b, realB);

    a.resolve(null, null);
    b.resolve(null, null);

    assertTrue(a.isResolved());
    assertTrue(b.isResolved());
    assertEquals(a.hashCode(), realA.hashCode());
    assertTypeEquals(a, realA);
    assertEquals(b.hashCode(), realB.hashCode());
    assertTypeEquals(b, realB);

    JSType resolvedA = Asserts.assertValidResolve(a);
    assertNotSame(resolvedA, a);
    assertSame(resolvedA, realA);

    JSType resolvedB = Asserts.assertValidResolve(b);
    assertNotSame(resolvedB, b);
    assertSame(resolvedB, realB);
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testForwardDeclaredNamedTypeEquals
  public void testForwardDeclaredNamedTypeEquals() {
    
    NamedType a = new NamedType(registry, "typeA", "source", 1, 0);
    NamedType b = new NamedType(registry, "typeA", "source", 1, 0);
    registry.forwardDeclareType("typeA");

    assertEquals(a.hashCode(), b.hashCode());
    assertTypeEquals(a, b);

    a.resolve(null, EMPTY_SCOPE);

    assertTrue(a.isResolved());
    assertFalse(b.isResolved());

    assertEquals(a.hashCode(), b.hashCode());
    assertTypeEquals(a, b);

    assertFalse(a.isEquivalentTo(UNKNOWN_TYPE));
    assertFalse(b.isEquivalentTo(UNKNOWN_TYPE));
    assertTrue(a.isEmptyType());
    assertFalse(a.isNoType());
    assertTrue(a.isNoResolvedType());
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testForwardDeclaredNamedType
  public void testForwardDeclaredNamedType() {
    NamedType a = new NamedType(registry, "typeA", "source", 1, 0);
    registry.forwardDeclareType("typeA");

    assertTypeEquals(UNKNOWN_TYPE, a.getLeastSupertype(UNKNOWN_TYPE));
    assertTypeEquals(CHECKED_UNKNOWN_TYPE,
        a.getLeastSupertype(CHECKED_UNKNOWN_TYPE));
    assertTypeEquals(UNKNOWN_TYPE, UNKNOWN_TYPE.getLeastSupertype(a));
    assertTypeEquals(CHECKED_UNKNOWN_TYPE,
        CHECKED_UNKNOWN_TYPE.getLeastSupertype(a));
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testGreatestSubtypeSimpleTypes
  public void testGreatestSubtypeSimpleTypes() {
    assertTypeEquals(ARRAY_TYPE,
        ARRAY_TYPE.getGreatestSubtype(ALL_TYPE));
    assertTypeEquals(ARRAY_TYPE,
        ALL_TYPE.getGreatestSubtype(ARRAY_TYPE));
    assertTypeEquals(NO_OBJECT_TYPE,
        REGEXP_TYPE.getGreatestSubtype(NO_OBJECT_TYPE));
    assertTypeEquals(NO_OBJECT_TYPE,
        NO_OBJECT_TYPE.getGreatestSubtype(REGEXP_TYPE));
    assertTypeEquals(NO_OBJECT_TYPE,
        ARRAY_TYPE.getGreatestSubtype(STRING_OBJECT_TYPE));
    assertTypeEquals(NO_TYPE, ARRAY_TYPE.getGreatestSubtype(NUMBER_TYPE));
    assertTypeEquals(NO_OBJECT_TYPE,
        ARRAY_TYPE.getGreatestSubtype(functionType));
    assertTypeEquals(STRING_OBJECT_TYPE,
        STRING_OBJECT_TYPE.getGreatestSubtype(OBJECT_TYPE));
    assertTypeEquals(STRING_OBJECT_TYPE,
        OBJECT_TYPE.getGreatestSubtype(STRING_OBJECT_TYPE));
    assertTypeEquals(NO_OBJECT_TYPE,
        ARRAY_TYPE.getGreatestSubtype(DATE_TYPE));
    assertTypeEquals(NO_OBJECT_TYPE,
        ARRAY_TYPE.getGreatestSubtype(REGEXP_TYPE));
    assertTypeEquals(EVAL_ERROR_TYPE,
        ERROR_TYPE.getGreatestSubtype(EVAL_ERROR_TYPE));
    assertTypeEquals(EVAL_ERROR_TYPE,
        EVAL_ERROR_TYPE.getGreatestSubtype(ERROR_TYPE));
    assertTypeEquals(NO_TYPE,
        NULL_TYPE.getGreatestSubtype(ERROR_TYPE));
    assertTypeEquals(UNKNOWN_TYPE,
        NUMBER_TYPE.getGreatestSubtype(UNKNOWN_TYPE));

    assertTypeEquals(NO_RESOLVED_TYPE,
        NO_OBJECT_TYPE.getGreatestSubtype(forwardDeclaredNamedType));
    assertTypeEquals(NO_RESOLVED_TYPE,
        forwardDeclaredNamedType.getGreatestSubtype(NO_OBJECT_TYPE));

  }

// com.google.javascript.rhino.jstype.JSTypeTest::testSubtypingDerivedExtendsNamedBaseType
  public void testSubtypingDerivedExtendsNamedBaseType() throws Exception {
    ObjectType derived =
        registry.createObjectType(registry.createObjectType(namedGoogBar));

    assertTrue(derived.isSubtype(googBar.getInstanceType()));
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testNamedSubtypeChain
  public void testNamedSubtypeChain() throws Exception {
    List<JSType> typeChain = Lists.newArrayList(
        registry.getNativeType(JSTypeNative.ALL_TYPE),
        registry.getNativeType(JSTypeNative.OBJECT_PROTOTYPE),
        registry.getNativeType(JSTypeNative.OBJECT_TYPE),
        googBar.getPrototype(),
        googBar.getInstanceType(),
        googSubBar.getPrototype(),
        googSubBar.getInstanceType(),
        googSubSubBar.getPrototype(),
        googSubSubBar.getInstanceType(),
        registry.getNativeType(JSTypeNative.NO_OBJECT_TYPE),
        registry.getNativeType(JSTypeNative.NO_RESOLVED_TYPE),
        registry.getNativeType(JSTypeNative.NO_TYPE));
    verifySubtypeChain(typeChain);
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testRecordSubtypeChain
  public void testRecordSubtypeChain() throws Exception {
    RecordTypeBuilder builder = new RecordTypeBuilder(registry);
    builder.addProperty("a", STRING_TYPE, null);
    JSType aType = builder.build();

    builder = new RecordTypeBuilder(registry);
    builder.addProperty("a", STRING_TYPE, null);
    builder.addProperty("b", STRING_TYPE, null);
    JSType abType = builder.build();

    builder = new RecordTypeBuilder(registry);
    builder.addProperty("a", STRING_TYPE, null);
    builder.addProperty("c", STRING_TYPE, null);
    JSType acType = builder.build();
    JSType abOrAcType = registry.createUnionType(abType, acType);

    builder = new RecordTypeBuilder(registry);
    builder.addProperty("a", STRING_TYPE, null);
    builder.addProperty("b", STRING_TYPE, null);
    builder.addProperty("c", NUMBER_TYPE, null);
    JSType abcType = builder.build();

    List<JSType> typeChain = Lists.newArrayList(
        registry.getNativeType(JSTypeNative.ALL_TYPE),
        registry.getNativeType(JSTypeNative.OBJECT_PROTOTYPE),
        registry.getNativeType(JSTypeNative.OBJECT_TYPE),
        aType,
        abOrAcType,
        abType,
        abcType,
        registry.getNativeType(JSTypeNative.NO_OBJECT_TYPE),
        registry.getNativeType(JSTypeNative.NO_TYPE));
    verifySubtypeChain(typeChain);
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testRecordAndObjectChain2
  public void testRecordAndObjectChain2() throws Exception {
    RecordTypeBuilder builder = new RecordTypeBuilder(registry);
    builder.addProperty("date", DATE_TYPE, null);
    JSType hasDateProperty = builder.build();

    List<JSType> typeChain = Lists.newArrayList(
        registry.getNativeType(JSTypeNative.OBJECT_TYPE),
        hasDateProperty,
        googBar.getInstanceType(),
        registry.getNativeType(JSTypeNative.NO_OBJECT_TYPE),
        registry.getNativeType(JSTypeNative.NO_TYPE));
    verifySubtypeChain(typeChain);
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testRecordAndObjectChain3
  public void testRecordAndObjectChain3() throws Exception {
    RecordTypeBuilder builder = new RecordTypeBuilder(registry);
    builder.addProperty("date", UNKNOWN_TYPE, null);
    JSType hasUnknownDateProperty = builder.build();

    List<JSType> typeChain = Lists.newArrayList(
        registry.getNativeType(JSTypeNative.OBJECT_TYPE),
        hasUnknownDateProperty,
        googBar.getInstanceType(),
        registry.getNativeType(JSTypeNative.NO_OBJECT_TYPE),
        registry.getNativeType(JSTypeNative.NO_TYPE));
    verifySubtypeChain(typeChain);
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testNullableNamedTypeChain
  public void testNullableNamedTypeChain() throws Exception {
    List<JSType> typeChain = Lists.newArrayList(
        registry.getNativeType(JSTypeNative.ALL_TYPE),
        registry.createOptionalNullableType(
            registry.getNativeType(JSTypeNative.OBJECT_PROTOTYPE)),
        registry.createOptionalNullableType(
            registry.getNativeType(JSTypeNative.OBJECT_TYPE)),
        registry.createOptionalNullableType(googBar.getPrototype()),
        registry.createOptionalNullableType(googBar.getInstanceType()),
        registry.createNullableType(googSubBar.getPrototype()),
        registry.createNullableType(googSubBar.getInstanceType()),
        googSubSubBar.getPrototype(),
        googSubSubBar.getInstanceType(),
        registry.getNativeType(JSTypeNative.NO_OBJECT_TYPE),
        registry.getNativeType(JSTypeNative.NO_TYPE));
    verifySubtypeChain(typeChain);
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testEnumTypeChain
  public void testEnumTypeChain() throws Exception {
    List<JSType> typeChain = Lists.newArrayList(
        registry.getNativeType(JSTypeNative.ALL_TYPE),
        registry.getNativeType(JSTypeNative.OBJECT_PROTOTYPE),
        registry.getNativeType(JSTypeNative.OBJECT_TYPE),
        enumType,
        registry.getNativeType(JSTypeNative.NO_OBJECT_TYPE),
        registry.getNativeType(JSTypeNative.NO_TYPE));
    verifySubtypeChain(typeChain);
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testFunctionSubtypeChain
  public void testFunctionSubtypeChain() throws Exception {
    List<JSType> typeChain = Lists.newArrayList(
        registry.getNativeType(JSTypeNative.ALL_TYPE),
        registry.getNativeType(JSTypeNative.OBJECT_PROTOTYPE),
        registry.getNativeType(JSTypeNative.OBJECT_TYPE),
        registry.getNativeType(JSTypeNative.FUNCTION_PROTOTYPE),
        registry.getNativeType(JSTypeNative.GREATEST_FUNCTION_TYPE),
        dateMethod,
        registry.getNativeType(JSTypeNative.LEAST_FUNCTION_TYPE),
        registry.getNativeType(JSTypeNative.NO_OBJECT_TYPE),
        registry.getNativeType(JSTypeNative.NO_TYPE));
    verifySubtypeChain(typeChain);
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testFunctionUnionSubtypeChain
  public void testFunctionUnionSubtypeChain() throws Exception {
    List<JSType> typeChain = Lists.newArrayList(
        createUnionType(
            OBJECT_TYPE,
            STRING_TYPE),
        createUnionType(
            GREATEST_FUNCTION_TYPE,
            googBarInst,
            STRING_TYPE),
        createUnionType(
            STRING_TYPE,
            registry.createFunctionType(
                createUnionType(STRING_TYPE, NUMBER_TYPE)),
            googBarInst),
        createUnionType(
            registry.createFunctionType(NUMBER_TYPE),
            googSubBarInst),
        LEAST_FUNCTION_TYPE,
        NO_OBJECT_TYPE,
        NO_TYPE);
    verifySubtypeChain(typeChain);
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testConstructorSubtypeChain
  public void testConstructorSubtypeChain() throws Exception {
    List<JSType> typeChain = Lists.newArrayList(
        registry.getNativeType(JSTypeNative.ALL_TYPE),
        registry.getNativeType(JSTypeNative.OBJECT_PROTOTYPE),
        registry.getNativeType(JSTypeNative.OBJECT_TYPE),
        registry.getNativeType(JSTypeNative.FUNCTION_PROTOTYPE),
        registry.getNativeType(JSTypeNative.FUNCTION_INSTANCE_TYPE),
        registry.getNativeType(JSTypeNative.NO_OBJECT_TYPE),
        registry.getNativeType(JSTypeNative.NO_TYPE));
    verifySubtypeChain(typeChain);
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testGoogBarSubtypeChain
  public void testGoogBarSubtypeChain() throws Exception {
    List<JSType> typeChain = Lists.newArrayList(
        registry.getNativeType(JSTypeNative.FUNCTION_INSTANCE_TYPE),
        googBar,
        googSubBar,
        googSubSubBar,
        registry.getNativeType(JSTypeNative.NO_OBJECT_TYPE));
    verifySubtypeChain(typeChain, false);
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testConstructorWithArgSubtypeChain
  public void testConstructorWithArgSubtypeChain() throws Exception {
    FunctionType googBarArgConstructor = registry.createConstructorType(
        "barArg", null, registry.createParameters(googBar), null);
    FunctionType googSubBarArgConstructor = registry.createConstructorType(
        "subBarArg", null, registry.createParameters(googSubBar), null);

    List<JSType> typeChain = Lists.newArrayList(
        registry.getNativeType(JSTypeNative.FUNCTION_INSTANCE_TYPE),
        googBarArgConstructor,
        googSubBarArgConstructor,
        registry.getNativeType(JSTypeNative.NO_OBJECT_TYPE));
    verifySubtypeChain(typeChain, false);
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testInterfaceInstanceSubtypeChain
  public void testInterfaceInstanceSubtypeChain() throws Exception {
    List<JSType> typeChain = Lists.newArrayList(
        ALL_TYPE,
        OBJECT_TYPE,
        interfaceInstType,
        googBar.getPrototype(),
        googBarInst,
        googSubBar.getPrototype(),
        googSubBarInst,
        registry.getNativeType(JSTypeNative.NO_OBJECT_TYPE),
        registry.getNativeType(JSTypeNative.NO_TYPE));
    verifySubtypeChain(typeChain);
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testInterfaceInheritanceSubtypeChain
  public void testInterfaceInheritanceSubtypeChain() throws Exception {
    FunctionType tempType =
      registry.createConstructorType("goog.TempType", null, null, null);
    tempType.setImplementedInterfaces(
        Lists.<ObjectType>newArrayList(subInterfaceInstType));
    List<JSType> typeChain = Lists.newArrayList(
        ALL_TYPE,
        OBJECT_TYPE,
        interfaceInstType,
        subInterfaceInstType,
        tempType.getPrototype(),
        tempType.getInstanceType(),
        registry.getNativeType(JSTypeNative.NO_OBJECT_TYPE),
        registry.getNativeType(JSTypeNative.NO_TYPE));
    verifySubtypeChain(typeChain);
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testAnonymousObjectChain
  public void testAnonymousObjectChain() throws Exception {
    List<JSType> typeChain = Lists.newArrayList(
        ALL_TYPE,
        createNullableType(OBJECT_TYPE),
        OBJECT_TYPE,
        registry.createAnonymousObjectType(),
        registry.getNativeType(JSTypeNative.NO_OBJECT_TYPE),
        registry.getNativeType(JSTypeNative.NO_TYPE));
    verifySubtypeChain(typeChain);
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testAnonymousEnumElementChain
  public void testAnonymousEnumElementChain() throws Exception {
    ObjectType enumElemType = registry.createEnumType(
        "typeB", null, registry.createAnonymousObjectType()).getElementsType();
    List<JSType> typeChain = Lists.newArrayList(
        ALL_TYPE,
        createNullableType(OBJECT_TYPE),
        OBJECT_TYPE,
        enumElemType,
        registry.getNativeType(JSTypeNative.NO_OBJECT_TYPE),
        registry.getNativeType(JSTypeNative.NO_TYPE));
    verifySubtypeChain(typeChain);
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testRestrictedTypeGivenToBoolean
      public void testRestrictedTypeGivenToBoolean() {
    
    assertTypeEquals(BOOLEAN_TYPE,
        BOOLEAN_TYPE.getRestrictedTypeGivenToBooleanOutcome(true));
    assertTypeEquals(BOOLEAN_TYPE,
        BOOLEAN_TYPE.getRestrictedTypeGivenToBooleanOutcome(false));

    assertTypeEquals(NO_TYPE,
        NULL_TYPE.getRestrictedTypeGivenToBooleanOutcome(true));
    assertTypeEquals(NULL_TYPE,
        NULL_TYPE.getRestrictedTypeGivenToBooleanOutcome(false));

    assertTypeEquals(NUMBER_TYPE,
        NUMBER_TYPE.getRestrictedTypeGivenToBooleanOutcome(true));
    assertTypeEquals(NUMBER_TYPE,
        NUMBER_TYPE.getRestrictedTypeGivenToBooleanOutcome(false));

    assertTypeEquals(STRING_TYPE,
        STRING_TYPE.getRestrictedTypeGivenToBooleanOutcome(true));
    assertTypeEquals(STRING_TYPE,
        STRING_TYPE.getRestrictedTypeGivenToBooleanOutcome(false));

    assertTypeEquals(STRING_OBJECT_TYPE,
        STRING_OBJECT_TYPE.getRestrictedTypeGivenToBooleanOutcome(true));
    assertTypeEquals(NO_TYPE,
        STRING_OBJECT_TYPE.getRestrictedTypeGivenToBooleanOutcome(false));

    assertTypeEquals(NO_TYPE,
        VOID_TYPE.getRestrictedTypeGivenToBooleanOutcome(true));
    assertTypeEquals(VOID_TYPE,
        VOID_TYPE.getRestrictedTypeGivenToBooleanOutcome(false));

    assertTypeEquals(NO_OBJECT_TYPE,
        NO_OBJECT_TYPE.getRestrictedTypeGivenToBooleanOutcome(true));
    assertTypeEquals(NO_TYPE,
        NO_OBJECT_TYPE.getRestrictedTypeGivenToBooleanOutcome(false));

    assertTypeEquals(NO_TYPE,
        NO_TYPE.getRestrictedTypeGivenToBooleanOutcome(true));
    assertTypeEquals(NO_TYPE,
        NO_TYPE.getRestrictedTypeGivenToBooleanOutcome(false));

    assertTypeEquals(ALL_TYPE,
        ALL_TYPE.getRestrictedTypeGivenToBooleanOutcome(true));
    assertTypeEquals(ALL_TYPE,
        ALL_TYPE.getRestrictedTypeGivenToBooleanOutcome(false));

    assertTypeEquals(UNKNOWN_TYPE,
        UNKNOWN_TYPE.getRestrictedTypeGivenToBooleanOutcome(true));
    assertTypeEquals(UNKNOWN_TYPE,
        UNKNOWN_TYPE.getRestrictedTypeGivenToBooleanOutcome(false));

    
    UnionType nullableStringValue =
        (UnionType) createNullableType(STRING_TYPE);
    assertTypeEquals(STRING_TYPE,
        nullableStringValue.getRestrictedTypeGivenToBooleanOutcome(true));
    assertTypeEquals(nullableStringValue,
        nullableStringValue.getRestrictedTypeGivenToBooleanOutcome(false));

    UnionType nullableStringObject =
        (UnionType) createNullableType(STRING_OBJECT_TYPE);
    assertTypeEquals(STRING_OBJECT_TYPE,
        nullableStringObject.getRestrictedTypeGivenToBooleanOutcome(true));
    assertTypeEquals(NULL_TYPE,
        nullableStringObject.getRestrictedTypeGivenToBooleanOutcome(false));
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testRegisterProperty
  public void testRegisterProperty() {
    int i = 0;
    List<JSType> allObjects = Lists.newArrayList();
    for (JSType type : types) {
      String propName = "ALF" + i++;
      if (type instanceof ObjectType) {

        ObjectType objType = (ObjectType) type;
        objType.defineDeclaredProperty(propName, UNKNOWN_TYPE, null);
        objType.defineDeclaredProperty("allHaz", UNKNOWN_TYPE, null);

        assertTypeEquals(type,
            registry.getGreatestSubtypeWithProperty(type, propName));

        List<JSType> typesWithProp =
            Lists.newArrayList(registry.getTypesWithProperty(propName));
        String message = type.toString();
        assertEquals(message, 1, typesWithProp.size());
        assertTypeEquals(type, typesWithProp.get(0));

        assertTypeEquals(NO_TYPE,
            registry.getGreatestSubtypeWithProperty(type, "GRRR"));
        allObjects.add(type);
      }
    }
    assertTypeListEquals(registry.getTypesWithProperty("GRRR"),
        Lists.newArrayList(NO_TYPE));
    assertTypeListEquals(allObjects,
        registry.getTypesWithProperty("allHaz"));
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testRegisterPropertyMemoization
  public void testRegisterPropertyMemoization() {
    ObjectType derived1 = registry.createObjectType("d1", null, namedGoogBar);
    ObjectType derived2 = registry.createObjectType("d2", null, namedGoogBar);

    derived1.defineDeclaredProperty("propz", UNKNOWN_TYPE, null);

    assertTypeEquals(derived1,
        registry.getGreatestSubtypeWithProperty(derived1, "propz"));
    assertTypeEquals(NO_OBJECT_TYPE,
        registry.getGreatestSubtypeWithProperty(derived2, "propz"));

    derived2.defineDeclaredProperty("propz", UNKNOWN_TYPE, null);

    assertTypeEquals(derived1,
        registry.getGreatestSubtypeWithProperty(derived1, "propz"));
    assertTypeEquals(derived2,
        registry.getGreatestSubtypeWithProperty(derived2, "propz"));
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testGreatestSubtypeWithProperty
  public void testGreatestSubtypeWithProperty() {
    ObjectType foo = registry.createObjectType("foo", null, OBJECT_TYPE);
    ObjectType bar = registry.createObjectType("bar", null, namedGoogBar);

    foo.defineDeclaredProperty("propz", UNKNOWN_TYPE, null);
    bar.defineDeclaredProperty("propz", UNKNOWN_TYPE, null);

    assertTypeEquals(bar,
        registry.getGreatestSubtypeWithProperty(namedGoogBar, "propz"));
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testGoodSetPrototypeBasedOn
  public void testGoodSetPrototypeBasedOn() {
    FunctionType fun = registry.createConstructorType("fun", null, null, null);
    fun.setPrototypeBasedOn(unresolvedNamedType);
    assertTrue(fun.getInstanceType().isUnknownType());
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testLateSetPrototypeBasedOn
  public void testLateSetPrototypeBasedOn() {
    FunctionType fun = registry.createConstructorType("fun", null, null, null);
    assertFalse(fun.getInstanceType().isUnknownType());

    fun.setPrototypeBasedOn(unresolvedNamedType);
    assertTrue(fun.getInstanceType().isUnknownType());
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testGetTypeUnderEquality1
  public void testGetTypeUnderEquality1() {
    for (JSType type : types) {
      testGetTypeUnderEquality(type, type, type, type);
    }
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testGetTypesUnderEquality2
  public void testGetTypesUnderEquality2() {
    
    testGetTypeUnderEquality(
        NUMBER_TYPE, OBJECT_TYPE,
        NUMBER_TYPE, OBJECT_TYPE);
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testGetTypesUnderEquality3
  public void testGetTypesUnderEquality3() {
    
    testGetTypeUnderEquality(
        NULL_TYPE, VOID_TYPE,
        NULL_TYPE, VOID_TYPE);
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testGetTypesUnderEquality4
  public void testGetTypesUnderEquality4() {
    
    UnionType stringNumber =
        (UnionType) createUnionType(NUMBER_TYPE, STRING_TYPE);
    testGetTypeUnderEquality(
        stringNumber, STRING_TYPE,
        stringNumber, STRING_TYPE);
    testGetTypeUnderEquality(
        stringNumber, NUMBER_TYPE,
        stringNumber, NUMBER_TYPE);
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testGetTypesUnderEquality5
  public void testGetTypesUnderEquality5() {
    
    JSType nullUndefined = createUnionType(VOID_TYPE, NULL_TYPE);
    testGetTypeUnderEquality(
        nullUndefined, NULL_TYPE,
        nullUndefined, NULL_TYPE);
    testGetTypeUnderEquality(
        nullUndefined, VOID_TYPE,
        nullUndefined, VOID_TYPE);
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testGetTypesUnderEquality6
  public void testGetTypesUnderEquality6() {
    
    JSType optNullNumber = createUnionType(VOID_TYPE, NULL_TYPE, NUMBER_TYPE);
    testGetTypeUnderEquality(
        optNullNumber, NULL_TYPE,
        createUnionType(NULL_TYPE, VOID_TYPE), NULL_TYPE);
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testGetTypesUnderInequality1
  public void testGetTypesUnderInequality1() {
    
    UnionType numberObject =
        (UnionType) createUnionType(NUMBER_TYPE, OBJECT_TYPE);
    testGetTypesUnderInequality(
        numberObject, NUMBER_TYPE,
        numberObject, NUMBER_TYPE);
    testGetTypesUnderInequality(
        numberObject, OBJECT_TYPE,
        numberObject, OBJECT_TYPE);
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testGetTypesUnderInequality2
  public void testGetTypesUnderInequality2() {
    
    UnionType nullUndefined =
        (UnionType) createUnionType(VOID_TYPE, NULL_TYPE);
    testGetTypesUnderInequality(
        nullUndefined, NULL_TYPE,
        NO_TYPE, NO_TYPE);
    testGetTypesUnderInequality(
        nullUndefined, VOID_TYPE,
        NO_TYPE, NO_TYPE);
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testGetTypesUnderInequality3
  public void testGetTypesUnderInequality3() {
    
    UnionType stringNumber =
        (UnionType) createUnionType(NUMBER_TYPE, STRING_TYPE);
    testGetTypesUnderInequality(
        stringNumber, NUMBER_TYPE,
        stringNumber, NUMBER_TYPE);
    testGetTypesUnderInequality(
        stringNumber, STRING_TYPE,
        stringNumber, STRING_TYPE);
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testGetTypesUnderInequality4
  public void testGetTypesUnderInequality4() throws Exception {
    
    UnionType nullableOptionalNumber =
        (UnionType) createUnionType(NULL_TYPE, VOID_TYPE, NUMBER_TYPE);
    testGetTypesUnderInequality(
        nullableOptionalNumber, NULL_TYPE,
        NUMBER_TYPE, NULL_TYPE);
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testCreateRecordType
  public void testCreateRecordType() throws Exception {
    Map<String, RecordProperty> properties =
        new HashMap<String, RecordProperty>();
    properties.put("hello", new RecordProperty(NUMBER_TYPE, null));

    JSType recordType = registry.createRecordType(properties);
    assertEquals("{hello: number}", recordType.toString());
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testCreateOptionalType
  public void testCreateOptionalType() throws Exception {
    
    UnionType optNumber = (UnionType) registry.createOptionalType(NUMBER_TYPE);
    assertUnionContains(optNumber, NUMBER_TYPE);
    assertUnionContains(optNumber, VOID_TYPE);

    
    UnionType optUnion =
        (UnionType) registry.createOptionalType(
            createUnionType(STRING_OBJECT_TYPE, DATE_TYPE));
    assertUnionContains(optUnion, DATE_TYPE);
    assertUnionContains(optUnion, STRING_OBJECT_TYPE);
    assertUnionContains(optUnion, VOID_TYPE);
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testCreateAnonymousObjectType
  public void testCreateAnonymousObjectType() throws Exception {
    
    ObjectType anonymous = registry.createAnonymousObjectType();
    assertTypeEquals(OBJECT_TYPE, anonymous.getImplicitPrototype());
    assertNull(anonymous.getReferenceName());
    assertEquals("{}", anonymous.toString());
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testCreateAnonymousObjectType2
  public void testCreateAnonymousObjectType2() throws Exception {
    
    ObjectType anonymous = registry.createAnonymousObjectType();
    anonymous.defineDeclaredProperty(
        "a", NUMBER_TYPE, null);
    anonymous.defineDeclaredProperty(
        "b", NUMBER_TYPE, null);
    anonymous.defineDeclaredProperty(
        "c", NUMBER_TYPE, null);
    anonymous.defineDeclaredProperty(
        "d", NUMBER_TYPE, null);
    anonymous.defineDeclaredProperty(
        "e", NUMBER_TYPE, null);
    anonymous.defineDeclaredProperty(
        "f", NUMBER_TYPE, null);
    assertEquals("{a: number, b: number, c: number, d: number, ...}",
        anonymous.toString());
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testCreateObjectType
  public void testCreateObjectType() throws Exception {
    
    ObjectType subDate =
        registry.createObjectType(DATE_TYPE.getImplicitPrototype());
    assertTypeEquals(DATE_TYPE.getImplicitPrototype(),
        subDate.getImplicitPrototype());
    assertNull(subDate.getReferenceName());
    assertEquals("{...}", subDate.toString());

    
    ObjectType subError = registry.createObjectType("Foo", null,
        ERROR_TYPE.getImplicitPrototype());
    assertTypeEquals(ERROR_TYPE.getImplicitPrototype(),
        subError.getImplicitPrototype());
    assertEquals("Foo", subError.getReferenceName());
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testBug903110
  public void testBug903110() throws Exception {
    UnionType union =
        (UnionType) createUnionType(U2U_CONSTRUCTOR_TYPE, VOID_TYPE);
    assertTrue(VOID_TYPE.isSubtype(union));
    assertTrue(U2U_CONSTRUCTOR_TYPE.isSubtype(union));
    assertTrue(union.isSubtype(union));
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testBug904123
  public void testBug904123() throws Exception {
    assertTrue(U2U_FUNCTION_TYPE.isSubtype(U2U_CONSTRUCTOR_TYPE));
    assertTrue(U2U_FUNCTION_TYPE.
        isSubtype(createOptionalType(U2U_CONSTRUCTOR_TYPE)));
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testHasOwnProperty
  public void testHasOwnProperty() throws Exception {
    ObjectType sup =
        registry.createObjectType(registry.createAnonymousObjectType());
    ObjectType sub = registry.createObjectType(sup);

    sup.defineProperty("base", null, false, null);
    sub.defineProperty("sub", null, false, null);

    assertTrue(sup.hasProperty("base"));
    assertFalse(sup.hasProperty("sub"));
    assertTrue(sup.hasOwnProperty("base"));
    assertFalse(sup.hasOwnProperty("sub"));
    assertFalse(sup.hasOwnProperty("none"));

    assertTrue(sub.hasProperty("base"));
    assertTrue(sub.hasProperty("sub"));
    assertFalse(sub.hasOwnProperty("base"));
    assertTrue(sub.hasOwnProperty("sub"));
    assertFalse(sub.hasOwnProperty("none"));
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testNamedTypeHasOwnProperty
  public void testNamedTypeHasOwnProperty() throws Exception {
    namedGoogBar.getImplicitPrototype().defineProperty("base", null, false,
        null);
    namedGoogBar.defineProperty("sub", null, false, null);

    assertFalse(namedGoogBar.hasOwnProperty("base"));
    assertTrue(namedGoogBar.hasProperty("base"));
    assertTrue(namedGoogBar.hasOwnProperty("sub"));
    assertTrue(namedGoogBar.hasProperty("sub"));
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testInterfaceHasOwnProperty
  public void testInterfaceHasOwnProperty() throws Exception {
    interfaceInstType.defineProperty("base", null, false, null);
    subInterfaceInstType.defineProperty("sub", null, false, null);

    assertTrue(interfaceInstType.hasProperty("base"));
    assertFalse(interfaceInstType.hasProperty("sub"));
    assertTrue(interfaceInstType.hasOwnProperty("base"));
    assertFalse(interfaceInstType.hasOwnProperty("sub"));
    assertFalse(interfaceInstType.hasOwnProperty("none"));

    assertTrue(subInterfaceInstType.hasProperty("base"));
    assertTrue(subInterfaceInstType.hasProperty("sub"));
    assertFalse(subInterfaceInstType.hasOwnProperty("base"));
    assertTrue(subInterfaceInstType.hasOwnProperty("sub"));
    assertFalse(subInterfaceInstType.hasOwnProperty("none"));
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testGetPropertyNames
  public void testGetPropertyNames() throws Exception {
    ObjectType sup =
        registry.createObjectType(registry.createAnonymousObjectType());
    ObjectType sub = registry.createObjectType(sup);

    sup.defineProperty("base", null, false, null);
    sub.defineProperty("sub", null, false, null);

    assertEquals(Sets.newHashSet("isPrototypeOf", "toLocaleString",
          "propertyIsEnumerable", "toString", "valueOf", "hasOwnProperty",
          "constructor", "base", "sub"), sub.getPropertyNames());
    assertEquals(Sets.newHashSet("isPrototypeOf", "toLocaleString",
          "propertyIsEnumerable", "toString", "valueOf", "hasOwnProperty",
          "constructor", "base"), sup.getPropertyNames());

    assertEquals(Sets.newHashSet(), NO_OBJECT_TYPE.getPropertyNames());
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testGetAndSetJSDocInfoWithNamedType
  public void testGetAndSetJSDocInfoWithNamedType() throws Exception {
    JSDocInfo info = new JSDocInfo();
    info.setDeprecated(true);

    assertNull(namedGoogBar.getOwnPropertyJSDocInfo("X"));
    namedGoogBar.setPropertyJSDocInfo("X", info);
    assertTrue(namedGoogBar.getOwnPropertyJSDocInfo("X").isDeprecated());
    assertPropertyTypeInferred(namedGoogBar, "X");
    assertTypeEquals(UNKNOWN_TYPE, namedGoogBar.getPropertyType("X"));
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testGetAndSetJSDocInfoWithObjectTypes
  public void testGetAndSetJSDocInfoWithObjectTypes() throws Exception {
    ObjectType sup =
        registry.createObjectType(registry.createAnonymousObjectType());
    ObjectType sub = registry.createObjectType(sup);

    JSDocInfo deprecated = new JSDocInfo();
    deprecated.setDeprecated(true);

    JSDocInfo privateInfo = new JSDocInfo();
    privateInfo.setVisibility(Visibility.PRIVATE);

    sup.defineProperty("X", NUMBER_TYPE, false, null);
    sup.setPropertyJSDocInfo("X", privateInfo);

    sub.setPropertyJSDocInfo("X", deprecated);

    assertFalse(sup.getOwnPropertyJSDocInfo("X").isDeprecated());
    assertEquals(Visibility.PRIVATE,
        sup.getOwnPropertyJSDocInfo("X").getVisibility());
    assertTypeEquals(NUMBER_TYPE, sup.getPropertyType("X"));
    assertTrue(sub.getOwnPropertyJSDocInfo("X").isDeprecated());
    assertNull(sub.getOwnPropertyJSDocInfo("X").getVisibility());
    assertTypeEquals(NUMBER_TYPE, sub.getPropertyType("X"));
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testGetAndSetJSDocInfoWithNoType
  public void testGetAndSetJSDocInfoWithNoType() throws Exception {
    JSDocInfo deprecated = new JSDocInfo();
    deprecated.setDeprecated(true);

    NO_TYPE.setPropertyJSDocInfo("X", deprecated);
    assertNull(NO_TYPE.getOwnPropertyJSDocInfo("X"));
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testObjectGetSubTypes
  public void testObjectGetSubTypes() throws Exception {
    assertTrue(
        containsType(
            OBJECT_FUNCTION_TYPE.getSubTypes(), googBar));
    assertTrue(
        containsType(
            googBar.getSubTypes(), googSubBar));
    assertFalse(
        containsType(
            googBar.getSubTypes(), googSubSubBar));
    assertFalse(
        containsType(
            googSubBar.getSubTypes(), googSubBar));
    assertTrue(
        containsType(
            googSubBar.getSubTypes(), googSubSubBar));
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testImplementingType
  public void testImplementingType() throws Exception {
    assertTrue(
        containsType(
            registry.getDirectImplementors(
                interfaceType.getInstanceType()),
            googBar));
  }

// com.google.javascript.rhino.jstype.NamedTypeTest::testNamedTypeProperties
  public void testNamedTypeProperties() {
    NamedType namedA = new NamedType(registry, "TypeA", "source", 1, 0);
    FunctionType ctorA = registry.createConstructorType(
        "TypeA", null, null, null);
    ObjectType typeA = ctorA.getInstanceType();

    namedA.defineDeclaredProperty("foo", NUMBER_TYPE, null);
    namedA.resolve(
        null,
        new MapBasedScope(
            ImmutableMap.of("TypeA", ctorA)));
    assertTypeEquals(NUMBER_TYPE, typeA.getPropertyType("foo"));
  }

// com.google.javascript.rhino.jstype.NamedTypeTest::testActiveXObjectResolve
  public void testActiveXObjectResolve() {
    NamedType activeXObject =
        new NamedType(registry, "ActiveXObject", "source", 1, 0);
    activeXObject.resolve(
        null,
        new MapBasedScope(
            ImmutableMap.of("ActiveXObject", NO_OBJECT_TYPE)));
    assertEquals("ActiveXObject", activeXObject.toString());
    assertTypeEquals(NO_OBJECT_TYPE, activeXObject.getReferencedType());
  }

// com.google.javascript.rhino.jstype.PrototypeObjectTypeTest::testToString
  public void testToString() {
    ObjectType type = registry.createAnonymousObjectType();
    assertEquals("{}", type.toString());

    type.defineDeclaredProperty("foo", NUMBER_TYPE, null);
    assertEquals("{foo: number}", type.toString());

    type.defineDeclaredProperty("bar", type, null);
    assertEquals("{bar: {...}, foo: number}", type.toString());
  }

// com.google.javascript.rhino.jstype.RecordTypeTest::testRecursiveRecord
  public void testRecursiveRecord() {
    ProxyObjectType loop = new ProxyObjectType(registry, NUMBER_TYPE);
    JSType record = new RecordTypeBuilder(registry)
        .addProperty("loop", loop, null)
        .addProperty("number", NUMBER_TYPE, null)
        .addProperty("string", STRING_TYPE, null)
        .build();
    assertEquals("{loop: number, number: number, string: string}",
        record.toString());

    loop.setReferencedType(record);
    assertEquals("{loop: {...}, number: number, string: string}",
        record.toString());
    assertEquals("{loop: ?, number: number, string: string}",
        record.toAnnotationString());

    Asserts.assertEquivalenceOperations(record, loop);
  }

// com.google.javascript.rhino.jstype.RecordTypeTest::testLongToString
  public void testLongToString() {
    JSType record = new RecordTypeBuilder(registry)
        .addProperty("a1", NUMBER_TYPE, null)
        .addProperty("a2", NUMBER_TYPE, null)
        .addProperty("a3", NUMBER_TYPE, null)
        .addProperty("a4", NUMBER_TYPE, null)
        .addProperty("a5", NUMBER_TYPE, null)
        .addProperty("a6", NUMBER_TYPE, null)
        .build();
    assertEquals("{a1: number, a2: number, a3: number, a4: number, ...}",
        record.toString());
    assertEquals(
        "{a1: number, a2: number, a3: number, a4: number," +
        " a5: number, a6: number}",
        record.toAnnotationString());
  }

// com.google.javascript.rhino.jstype.RecordTypeTest::testSupAndInf
  public void testSupAndInf() {
    JSType recordA = new RecordTypeBuilder(registry)
        .addProperty("a", NUMBER_TYPE, null)
        .addProperty("b", NUMBER_TYPE, null)
        .build();
    JSType recordC = new RecordTypeBuilder(registry)
        .addProperty("b", NUMBER_TYPE, null)
        .addProperty("c", NUMBER_TYPE, null)
        .build();
    ProxyObjectType proxyRecordA = new ProxyObjectType(registry, recordA);
    ProxyObjectType proxyRecordC = new ProxyObjectType(registry, recordC);

    JSType aInfC = new RecordTypeBuilder(registry)
        .addProperty("a", NUMBER_TYPE, null)
        .addProperty("b", NUMBER_TYPE, null)
        .addProperty("c", NUMBER_TYPE, null)
        .build();

    JSType aSupC = registry.createUnionType(recordA, recordC);

    Asserts.assertTypeEquals(
        aInfC, recordA.getGreatestSubtype(recordC));
    Asserts.assertTypeEquals(
        aSupC, recordA.getLeastSupertype(recordC));

    Asserts.assertTypeEquals(
        aInfC, proxyRecordA.getGreatestSubtype(proxyRecordC));
    Asserts.assertTypeEquals(
        aSupC, proxyRecordA.getLeastSupertype(proxyRecordC));
  }

// com.google.javascript.rhino.jstype.UnionTypeBuilderTest::testAllType
  public void testAllType() {
    assertUnion("*", ALL_TYPE);
    assertUnion("*", NUMBER_TYPE, ALL_TYPE);
    assertUnion("*", ALL_TYPE, NUMBER_TYPE);
    assertUnion("*", ALL_TYPE, NUMBER_TYPE, NO_TYPE);
  }

// com.google.javascript.rhino.jstype.UnionTypeBuilderTest::testEmptyUnion
  public void testEmptyUnion() {
    assertUnion("None");
    assertUnion("None", NO_TYPE, NO_TYPE);
  }

// com.google.javascript.rhino.jstype.UnionTypeBuilderTest::testUnionTypes
  public void testUnionTypes() {
    JSType union = registry.createUnionType(STRING_TYPE, OBJECT_TYPE);

    assertUnion("*", ALL_TYPE, union);
    assertUnion("(Object|string)", OBJECT_TYPE, union);
    assertUnion("(Object|string)", union, OBJECT_TYPE);
    assertUnion("(Object|number|string)", NUMBER_TYPE, union);
    assertUnion("(Object|number|string)", union, NUMBER_TYPE);
    assertUnion("(Object|boolean|number|string)", union,
        registry.createUnionType(NUMBER_TYPE, BOOLEAN_TYPE));
    assertUnion("(Object|boolean|number|string)",
        registry.createUnionType(NUMBER_TYPE, BOOLEAN_TYPE), union);
    assertUnion("(Object|string)", union, STRING_OBJECT_TYPE);
  }

// com.google.javascript.rhino.jstype.UnionTypeBuilderTest::testUnknownTypes
  public void testUnknownTypes() {
    JSType unresolvedNameA1 =
        new NamedType(registry, "not.resolved.A", null, -1, -1);
    JSType unresolvedNameA2 =
        new NamedType(registry, "not.resolved.A", null, -1, -1);
    JSType unresolvedNameB =
        new NamedType(registry, "not.resolved.B", null, -1, -1);

    assertUnion("?", UNKNOWN_TYPE);
    assertUnion("?", UNKNOWN_TYPE, UNKNOWN_TYPE);

    
    assertUnion("?", UNKNOWN_TYPE, unresolvedNameA1);
    assertUnion("not.resolved.A", unresolvedNameA1, unresolvedNameA2);
    assertUnion("(not.resolved.A|not.resolved.B)",
        unresolvedNameA1, unresolvedNameB);
    assertUnion("(Object|not.resolved.A)", unresolvedNameA1, OBJECT_TYPE);
  }

// com.google.javascript.rhino.jstype.UnionTypeBuilderTest::testRemovalOfDupes
  public void testRemovalOfDupes() {
    JSType stringAndObject =
        registry.createUnionType(STRING_TYPE, OBJECT_TYPE);
    assertUnion("(Object|string)", stringAndObject, STRING_OBJECT_TYPE);
    assertUnion("(Object|string)", STRING_OBJECT_TYPE, stringAndObject);
  }

// com.google.javascript.rhino.jstype.UnionTypeBuilderTest::testRemovalOfDupes2
  public void testRemovalOfDupes2() {
    JSType union =
        registry.createUnionType(
            EVAL_ERROR_TYPE,
            createFunctionWithReturn(ERROR_TYPE),
            ERROR_TYPE,
            createFunctionWithReturn(EVAL_ERROR_TYPE));
    assertEquals("(Error|function (): Error)", union.toString());
  }

// com.google.javascript.rhino.jstype.UnionTypeBuilderTest::testRemovalOfDupes3
  public void testRemovalOfDupes3() {
    JSType union =
        registry.createUnionType(
            ERROR_TYPE,
            createFunctionWithReturn(EVAL_ERROR_TYPE),
            EVAL_ERROR_TYPE,
            createFunctionWithReturn(ERROR_TYPE));
    assertEquals("(Error|function (): Error)", union.toString());
  }

// com.google.javascript.rhino.jstype.UnionTypeTest::testUnionType
  public void testUnionType() throws Exception {
    UnionType nullOrString =
        (UnionType) createUnionType(NULL_TYPE, STRING_OBJECT_TYPE);
    UnionType stringOrNull =
        (UnionType) createUnionType(STRING_OBJECT_TYPE, NULL_TYPE);

    assertEquals(nullOrString, stringOrNull);
    assertEquals(stringOrNull, nullOrString);

    assertTypeCanAssignToItself(createUnionType(VOID_TYPE, NUMBER_TYPE));
    assertTypeCanAssignToItself(
        createUnionType(NUMBER_TYPE, STRING_TYPE, OBJECT_TYPE));
    assertTypeCanAssignToItself(createUnionType(NUMBER_TYPE, BOOLEAN_TYPE));
    assertTypeCanAssignToItself(createUnionType(VOID_TYPE));

    UnionType nullOrUnknown =
        (UnionType) createUnionType(NULL_TYPE, unresolvedNamedType);
    assertTrue(nullOrUnknown.isUnknownType());
    assertEquals(nullOrUnknown, NULL_TYPE.getLeastSupertype(nullOrUnknown));
    assertEquals(nullOrUnknown, nullOrUnknown.getLeastSupertype(NULL_TYPE));
    assertEquals(UNKNOWN_TYPE,
        NULL_TYPE.getGreatestSubtype(nullOrUnknown));
    assertEquals(UNKNOWN_TYPE,
        nullOrUnknown.getGreatestSubtype(NULL_TYPE));

    assertTrue(NULL_TYPE.differsFrom(nullOrUnknown));
    assertTrue(nullOrUnknown.differsFrom(NULL_TYPE));
    assertFalse(nullOrUnknown.differsFrom(unresolvedNamedType));

    assertTrue(NULL_TYPE.isSubtype(nullOrUnknown));
    assertTrue(unresolvedNamedType.isSubtype(nullOrUnknown));
    assertTrue(nullOrUnknown.isSubtype(NULL_TYPE));

    assertEquals(unresolvedNamedType,
        nullOrUnknown.restrictByNotNullOrUndefined());

    
    assertEquals(NUMBER_TYPE, nullOrString.findPropertyType("length"));
    assertEquals(null, nullOrString.findPropertyType("lengthx"));

    Asserts.assertResolvesToSame(nullOrString);
  }

// com.google.javascript.rhino.jstype.UnionTypeTest::testGreatestSubtypeUnionTypes1
  public void testGreatestSubtypeUnionTypes1() {
    assertEquals(NULL_TYPE, createNullableType(STRING_TYPE).getGreatestSubtype(
            createNullableType(NUMBER_TYPE)));
  }

// com.google.javascript.rhino.jstype.UnionTypeTest::testGreatestSubtypeUnionTypes2
  public void testGreatestSubtypeUnionTypes2() {
    UnionType evalUriError =
        (UnionType) createUnionType(EVAL_ERROR_TYPE, URI_ERROR_TYPE);
    assertEquals(evalUriError,
        evalUriError.getGreatestSubtype(ERROR_TYPE));
  }

// com.google.javascript.rhino.jstype.UnionTypeTest::testGreatestSubtypeUnionTypes3
  public void testGreatestSubtypeUnionTypes3() {
    
    UnionType nullableOptionalNumber =
        (UnionType) createUnionType(NULL_TYPE, VOID_TYPE, NUMBER_TYPE);
    
    UnionType nullUndefined =
        (UnionType) createUnionType(VOID_TYPE, NULL_TYPE);
    assertEquals(nullUndefined,
        nullUndefined.getGreatestSubtype(nullableOptionalNumber));
    assertEquals(nullUndefined,
        nullableOptionalNumber.getGreatestSubtype(nullUndefined));
  }

// com.google.javascript.rhino.jstype.UnionTypeTest::testGreatestSubtypeUnionTypes4
  public void testGreatestSubtypeUnionTypes4() throws Exception {
    UnionType errUnion = (UnionType) createUnionType(
        NULL_TYPE, EVAL_ERROR_TYPE, URI_ERROR_TYPE);
    assertEquals(createUnionType(EVAL_ERROR_TYPE, URI_ERROR_TYPE),
        errUnion.getGreatestSubtype(ERROR_TYPE));
  }

// com.google.javascript.rhino.jstype.UnionTypeTest::testGreatestSubtypeUnionTypes5
  public void testGreatestSubtypeUnionTypes5() throws Exception {
    JSType errUnion = createUnionType(EVAL_ERROR_TYPE, URI_ERROR_TYPE);
    assertEquals(NO_OBJECT_TYPE,
        errUnion.getGreatestSubtype(STRING_OBJECT_TYPE));
  }

// com.google.javascript.rhino.jstype.UnionTypeTest::testSubtypingUnionTypes
  public void testSubtypingUnionTypes() throws Exception {
    
    assertTrue(BOOLEAN_TYPE.
        isSubtype(createUnionType(BOOLEAN_TYPE, STRING_TYPE)));
    assertTrue(createUnionType(BOOLEAN_TYPE, STRING_TYPE).
        isSubtype(createUnionType(BOOLEAN_TYPE, STRING_TYPE)));
    assertTrue(createUnionType(BOOLEAN_TYPE, STRING_TYPE).
        isSubtype(createUnionType(BOOLEAN_TYPE, STRING_TYPE, NULL_TYPE)));
    assertTrue(createUnionType(BOOLEAN_TYPE, STRING_TYPE).
        isSubtype(createUnionType(BOOLEAN_TYPE, STRING_TYPE, NULL_TYPE)));
    assertTrue(createUnionType(BOOLEAN_TYPE).
        isSubtype(createUnionType(BOOLEAN_TYPE, STRING_TYPE, NULL_TYPE)));
    assertTrue(createUnionType(STRING_TYPE).
        isSubtype(createUnionType(BOOLEAN_TYPE, STRING_TYPE, NULL_TYPE)));
    assertTrue(createUnionType(STRING_TYPE, NULL_TYPE).isSubtype(ALL_TYPE));
    assertTrue(createUnionType(DATE_TYPE, REGEXP_TYPE).isSubtype(OBJECT_TYPE));
    assertTrue(createUnionType(URI_ERROR_TYPE, EVAL_ERROR_TYPE).
        isSubtype(ERROR_TYPE));
    assertTrue(createUnionType(URI_ERROR_TYPE, EVAL_ERROR_TYPE).
        isSubtype(OBJECT_TYPE));

    
    assertFalse(createUnionType(STRING_TYPE, NULL_TYPE).isSubtype(NO_TYPE));
    assertFalse(createUnionType(STRING_TYPE, NULL_TYPE).
        isSubtype(NO_OBJECT_TYPE));
    assertFalse(createUnionType(NO_OBJECT_TYPE, NULL_TYPE).
        isSubtype(OBJECT_TYPE));

    
    assertTrue(NUMBER_TYPE.isSubtype(OBJECT_NUMBER_STRING));
    assertTrue(OBJECT_TYPE.isSubtype(OBJECT_NUMBER_STRING));
    assertTrue(STRING_TYPE.isSubtype(OBJECT_NUMBER_STRING));
    assertTrue(NO_OBJECT_TYPE.isSubtype(OBJECT_NUMBER_STRING));

    assertTrue(NUMBER_TYPE.isSubtype(NUMBER_STRING_BOOLEAN));
    assertTrue(BOOLEAN_TYPE.isSubtype(NUMBER_STRING_BOOLEAN));
    assertTrue(STRING_TYPE.isSubtype(NUMBER_STRING_BOOLEAN));

    assertTrue(NUMBER_TYPE.isSubtype(OBJECT_NUMBER_STRING_BOOLEAN));
    assertTrue(OBJECT_TYPE.isSubtype(OBJECT_NUMBER_STRING_BOOLEAN));
    assertTrue(STRING_TYPE.isSubtype(OBJECT_NUMBER_STRING_BOOLEAN));
    assertTrue(BOOLEAN_TYPE.isSubtype(OBJECT_NUMBER_STRING_BOOLEAN));
    assertTrue(NO_OBJECT_TYPE.isSubtype(OBJECT_NUMBER_STRING_BOOLEAN));
  }

// com.google.javascript.rhino.jstype.UnionTypeTest::testSpecialUnionCanAssignTo
  public void testSpecialUnionCanAssignTo() throws Exception {
    
    UnionType numbers =
        (UnionType) createUnionType(NUMBER_TYPE, NUMBER_OBJECT_TYPE);
    assertFalse(numbers.canAssignTo(NUMBER_TYPE));
    assertFalse(numbers.canAssignTo(NUMBER_OBJECT_TYPE));
    assertFalse(numbers.canAssignTo(EVAL_ERROR_TYPE));

    UnionType strings =
        (UnionType) createUnionType(STRING_OBJECT_TYPE, STRING_TYPE);
    assertFalse(strings.canAssignTo(STRING_TYPE));
    assertFalse(strings.canAssignTo(STRING_OBJECT_TYPE));
    assertFalse(strings.canAssignTo(DATE_TYPE));

    UnionType booleans =
        (UnionType) createUnionType(BOOLEAN_OBJECT_TYPE, BOOLEAN_TYPE);
    assertFalse(booleans.canAssignTo(BOOLEAN_TYPE));
    assertFalse(booleans.canAssignTo(BOOLEAN_OBJECT_TYPE));
    assertFalse(booleans.canAssignTo(REGEXP_TYPE));

    
    JSType unknown = createUnionType(UNKNOWN_TYPE, DATE_TYPE);
    assertTrue(unknown.canAssignTo(STRING_TYPE));

    
    UnionType stringDate =
        (UnionType) createUnionType(STRING_OBJECT_TYPE, DATE_TYPE);
    assertTrue(stringDate.canAssignTo(OBJECT_TYPE));
    assertFalse(stringDate.canAssignTo(STRING_OBJECT_TYPE));
    assertFalse(stringDate.canAssignTo(DATE_TYPE));
  }

// com.google.javascript.rhino.jstype.UnionTypeTest::testCreateUnionType
  public void testCreateUnionType() throws Exception {
    
    UnionType optNumber =
        (UnionType) registry.createUnionType(NUMBER_TYPE, DATE_TYPE);
    assertTrue(optNumber.contains(NUMBER_TYPE));
    assertTrue(optNumber.contains(DATE_TYPE));

    
    UnionType optUnion =
        (UnionType) registry.createUnionType(REGEXP_TYPE,
            registry.createUnionType(STRING_OBJECT_TYPE, DATE_TYPE));
    assertTrue(optUnion.contains(DATE_TYPE));
    assertTrue(optUnion.contains(STRING_OBJECT_TYPE));
    assertTrue(optUnion.contains(REGEXP_TYPE));
  }

// com.google.javascript.rhino.jstype.UnionTypeTest::testUnionWithUnknown
  public void testUnionWithUnknown() throws Exception {
    assertTrue(createUnionType(UNKNOWN_TYPE, NULL_TYPE).isUnknownType());
  }

// com.google.javascript.rhino.jstype.UnionTypeTest::testGetRestrictedUnion1
  public void testGetRestrictedUnion1() throws Exception {
    UnionType numStr = (UnionType) createUnionType(NUMBER_TYPE, STRING_TYPE);
    assertEquals(STRING_TYPE, numStr.getRestrictedUnion(NUMBER_TYPE));
  }

// com.google.javascript.rhino.jstype.UnionTypeTest::testGetRestrictedUnion2
  public void testGetRestrictedUnion2() throws Exception {
    UnionType numStr = (UnionType) createUnionType(
        NULL_TYPE, EVAL_ERROR_TYPE, URI_ERROR_TYPE);
    assertEquals(NULL_TYPE, numStr.getRestrictedUnion(ERROR_TYPE));
  }

// com.google.javascript.rhino.jstype.UnionTypeTest::testIsEquivalentTo
  public void testIsEquivalentTo() {
    UnionType type = (UnionType) createUnionType(NUMBER_TYPE, STRING_TYPE);
    assertFalse(type.isEquivalentTo(null));
    assertTrue(type.isEquivalentTo(type));
  }

// com.google.javascript.rhino.jstype.UnionTypeTest::testProxyUnionType
  public void testProxyUnionType() throws Exception {
    UnionType stringOrNumber =
        (UnionType) createUnionType(NUMBER_TYPE, STRING_TYPE);
    UnionType stringOrBoolean =
        (UnionType) createUnionType(BOOLEAN_TYPE, STRING_TYPE);

    assertEquals(
        "(boolean|number|string)",
        stringOrNumber.getLeastSupertype(stringOrBoolean).toString());
    assertEquals(
        "string",
        stringOrNumber.getGreatestSubtype(stringOrBoolean).toString());
    assertEquals(
        TernaryValue.UNKNOWN,
        stringOrNumber.testForEquality(stringOrBoolean));
    assertEquals(
        "(number|string)",
        stringOrNumber.getTypesUnderEquality(
            stringOrBoolean).typeA.toString());
    assertEquals(
        "string",
        stringOrNumber.getTypesUnderShallowEquality(
            stringOrBoolean).typeA.toString());
    assertEquals(
        "(number|string)",
        stringOrNumber.getTypesUnderInequality(
            stringOrBoolean).typeA.toString());
    assertEquals(
        "(number|string)",
        stringOrNumber.getTypesUnderShallowInequality(
            stringOrBoolean).typeA.toString());

    ObjectType stringOrNumberProxy =
        new ProxyObjectType(registry, stringOrNumber);
    ObjectType stringOrBooleanProxy =
        new ProxyObjectType(registry, stringOrBoolean);
    assertEquals(
        "(boolean|number|string)",
        stringOrNumberProxy.getLeastSupertype(
            stringOrBooleanProxy).toString());
    assertEquals(
        "string",
        stringOrNumberProxy.getGreatestSubtype(
            stringOrBooleanProxy).toString());
    assertEquals(
        TernaryValue.UNKNOWN,
        stringOrNumberProxy.testForEquality(stringOrBooleanProxy));
    assertEquals(
        "(number|string)",
        stringOrNumberProxy.getTypesUnderEquality(
            stringOrBooleanProxy).typeA.toString());
    assertEquals(
        "string",
        stringOrNumberProxy.getTypesUnderShallowEquality(
            stringOrBooleanProxy).typeA.toString());
    assertEquals(
        "(number|string)",
        stringOrNumberProxy.getTypesUnderInequality(
            stringOrBooleanProxy).typeA.toString());
    assertEquals(
        "(number|string)",
        stringOrNumberProxy.getTypesUnderShallowInequality(
            stringOrBooleanProxy).typeA.toString());
  }

// com.google.javascript.rhino.jstype.UnionTypeTest::testCollapseUnion1
  public void testCollapseUnion1() {
    assertEquals(
        "*",
        registry.createUnionType(NUMBER_TYPE, STRING_TYPE)
        .collapseUnion().toString());
  }

// com.google.javascript.rhino.jstype.UnionTypeTest::testCollapseUnion2
  public void testCollapseUnion2() {
    assertEquals(
        "?",
        registry.createUnionType(UNKNOWN_TYPE, NUMBER_TYPE)
        .collapseUnion().toString());
    assertEquals(
        "?",
        registry.createUnionType(NUMBER_TYPE, UNKNOWN_TYPE)
        .collapseUnion().toString());
  }

// com.google.javascript.rhino.jstype.UnionTypeTest::testCollapseUnion3
  public void testCollapseUnion3() {
    assertEquals(
        "Object",
        registry.createUnionType(ARRAY_TYPE, DATE_TYPE)
        .collapseUnion().toString());
    assertEquals(
        "Object",
        registry.createUnionType(ARRAY_TYPE, OBJECT_TYPE)
        .collapseUnion().toString());
    assertEquals(
        "Error",
        registry.createUnionType(ERROR_TYPE, RANGE_ERROR_TYPE)
        .collapseUnion().toString());
    assertEquals(
        "Error",
        registry.createUnionType(EVAL_ERROR_TYPE, RANGE_ERROR_TYPE)
        .collapseUnion().toString());
    assertEquals(
        "Error",
        registry.createUnionType(
            EVAL_ERROR_TYPE, RANGE_ERROR_TYPE, TYPE_ERROR_TYPE)
        .collapseUnion().toString());
  }

// com.google.javascript.rhino.jstype.UnionTypeTest::testCollapseUnion4
  public void testCollapseUnion4() {
    assertEquals(
        "*",
        registry.createUnionType(OBJECT_TYPE, STRING_TYPE)
        .collapseUnion().toString());
    assertEquals(
        "*",
        registry.createUnionType(STRING_TYPE, OBJECT_TYPE)
        .collapseUnion().toString());
  }

// com.google.javascript.rhino.jstype.UnionTypeTest::testCollapseProxyUnion
  public void testCollapseProxyUnion() {
    
    ProxyObjectType type = new ProxyObjectType(registry, OBJECT_TYPE);
    assertTrue(type == type.collapseUnion());
  }

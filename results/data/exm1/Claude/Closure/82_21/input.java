// buggy code
  public final boolean isEmptyType() {
    return isNoType() || isNoObjectType() || isNoResolvedType();
  }

// relevant test
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
        "typeB", NUMBER_TYPE).getElementsType();
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
    builder.addProperty("b", STRING_TYPE, null);
    builder.addProperty("c", NUMBER_TYPE, null);
    JSType abcType = builder.build();

    List<JSType> typeChain = Lists.newArrayList(
        registry.getNativeType(JSTypeNative.ALL_TYPE),
        registry.getNativeType(JSTypeNative.OBJECT_PROTOTYPE),
        registry.getNativeType(JSTypeNative.OBJECT_TYPE),
        aType,
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
        objType.defineDeclaredProperty(propName, UNKNOWN_TYPE, false, null);
        objType.defineDeclaredProperty("allHaz", UNKNOWN_TYPE, false, null);

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

    derived1.defineDeclaredProperty("propz", UNKNOWN_TYPE, false, null);

    assertTypeEquals(derived1,
        registry.getGreatestSubtypeWithProperty(derived1, "propz"));
    assertTypeEquals(NO_OBJECT_TYPE,
        registry.getGreatestSubtypeWithProperty(derived2, "propz"));

    derived2.defineDeclaredProperty("propz", UNKNOWN_TYPE, false, null);

    assertTypeEquals(derived1,
        registry.getGreatestSubtypeWithProperty(derived1, "propz"));
    assertTypeEquals(derived2,
        registry.getGreatestSubtypeWithProperty(derived2, "propz"));
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testGreatestSubtypeWithProperty
  public void testGreatestSubtypeWithProperty() {
    ObjectType foo = registry.createObjectType("foo", null, OBJECT_TYPE);
    ObjectType bar = registry.createObjectType("bar", null, namedGoogBar);

    foo.defineDeclaredProperty("propz", UNKNOWN_TYPE, false, null);
    bar.defineDeclaredProperty("propz", UNKNOWN_TYPE, false, null);

    assertTypeEquals(bar,
        registry.getGreatestSubtypeWithProperty(namedGoogBar, "propz"));
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testGoodSetPrototypeBasedOn
  public void testGoodSetPrototypeBasedOn() {
    FunctionType fun = registry.createConstructorType("fun", null, null, null);
    fun.setPrototypeBasedOn(unresolvedNamedType);
    assertTrue(fun.getInstanceType().isUnknownType());
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testInvalidSetPrototypeBasedOn
  public void testInvalidSetPrototypeBasedOn() {
    FunctionType fun = registry.createConstructorType("fun", null, null, null);
    assertFalse(fun.getInstanceType().isUnknownType());

    
    try {
      fun.setPrototypeBasedOn(unresolvedNamedType);
      fail();
    } catch (IllegalStateException e) {
      e.printStackTrace();
    }
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
    Map<String, RecordProperty> properties = new HashMap<String, RecordProperty>();
    properties.put("hello", new RecordProperty(NUMBER_TYPE, null));

    JSType recordType = registry.createRecordType(properties);
    assertEquals("{ hello : number }", recordType.toString());
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
        "a", NUMBER_TYPE, false, null);
    anonymous.defineDeclaredProperty(
        "b", NUMBER_TYPE, false, null);
    anonymous.defineDeclaredProperty(
        "c", NUMBER_TYPE, false, null);
    anonymous.defineDeclaredProperty(
        "d", NUMBER_TYPE, false, null);
    anonymous.defineDeclaredProperty(
        "e", NUMBER_TYPE, false, null);
    anonymous.defineDeclaredProperty(
        "f", NUMBER_TYPE, false, null);
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

    sup.defineProperty("base", null, false, false, null);
    sub.defineProperty("sub", null, false, false, null);

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
        false, null);
    namedGoogBar.defineProperty("sub", null, false, false, null);

    assertFalse(namedGoogBar.hasOwnProperty("base"));
    assertTrue(namedGoogBar.hasProperty("base"));
    assertTrue(namedGoogBar.hasOwnProperty("sub"));
    assertTrue(namedGoogBar.hasProperty("sub"));
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testGetPropertyNames
  public void testGetPropertyNames() throws Exception {
    ObjectType sup =
        registry.createObjectType(registry.createAnonymousObjectType());
    ObjectType sub = registry.createObjectType(sup);

    sup.defineProperty("base", null, false, false, null);
    sub.defineProperty("sub", null, false, false, null);

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
    namedGoogBar.setPropertyJSDocInfo("X", info, false);
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

    sup.defineProperty("X", NUMBER_TYPE, false, false, null);
    sup.setPropertyJSDocInfo("X", privateInfo, false);

    sub.setPropertyJSDocInfo("X", deprecated, false);

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

    NO_TYPE.setPropertyJSDocInfo("X", deprecated, false);
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

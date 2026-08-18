// buggy code
  private FlowScope caseEquality(Node left, Node right, FlowScope blindScope,
      Function<TypePair, TypePair> merging) {
    // left type
    JSType leftType = getTypeIfRefinable(left, blindScope);
    boolean leftIsRefineable;
    if (leftType != null) {
      leftIsRefineable = true;
    } else {
      leftIsRefineable = false;
      leftType = left.getJSType();
    }

    // right type
    JSType rightType = getTypeIfRefinable(right, blindScope);
    boolean rightIsRefineable;
    if (rightType != null) {
      rightIsRefineable = true;
    } else {
      rightIsRefineable = false;
      rightType = right.getJSType();
    }

    // merged types
    TypePair merged = merging.apply(new TypePair(leftType, rightType));

    // creating new scope
    if (merged != null) {
      return maybeRestrictTwoNames(
          blindScope,
          left, leftIsRefineable, merged.typeA,
          right, rightIsRefineable, merged.typeB);
    }
    return blindScope;
  }

  private FlowScope caseAndOrNotShortCircuiting(Node left, Node right,
        FlowScope blindScope, boolean condition) {
    // left type
    JSType leftType = getTypeIfRefinable(left, blindScope);
    boolean leftIsRefineable;
    if (leftType != null) {
      leftIsRefineable = true;
    } else {
      leftIsRefineable = false;
      leftType = left.getJSType();
      blindScope = firstPreciserScopeKnowingConditionOutcome(
          left, blindScope, condition);
    }

    // restricting left type
    JSType restrictedLeftType = (leftType == null) ? null :
        leftType.getRestrictedTypeGivenToBooleanOutcome(condition);
    if (restrictedLeftType == null) {
      return firstPreciserScopeKnowingConditionOutcome(
          right, blindScope, condition);
    }

    // right type
    JSType rightType = getTypeIfRefinable(right, blindScope);
    boolean rightIsRefineable;
    if (rightType != null) {
      rightIsRefineable = true;
    } else {
      rightIsRefineable = false;
      rightType = right.getJSType();
      blindScope = firstPreciserScopeKnowingConditionOutcome(
          right, blindScope, condition);
    }

    if (condition) {
      JSType restrictedRightType = (rightType == null) ? null :
          rightType.getRestrictedTypeGivenToBooleanOutcome(condition);

      // creating new scope
      return maybeRestrictTwoNames(
          blindScope,
          left, leftIsRefineable, restrictedLeftType,
          right, rightIsRefineable, restrictedRightType);
    }
    return blindScope;
  }

  private FlowScope maybeRestrictName(
      FlowScope blindScope, Node node, JSType originalType, JSType restrictedType) {
    if (restrictedType != null && !restrictedType.equals(originalType)) {
      FlowScope informed = blindScope.createChildFlowScope();
      declareNameInScope(informed, node, restrictedType);
      return informed;
    }
    return blindScope;
  }

  private FlowScope maybeRestrictTwoNames(
      FlowScope blindScope,
      Node left, boolean leftIsRefineable, JSType restrictedLeftType,
      Node right, boolean rightIsRefineable, JSType restrictedRightType) {
    boolean shouldRefineLeft =
        leftIsRefineable && restrictedLeftType != null;
    boolean shouldRefineRight =
        rightIsRefineable && restrictedRightType != null;
    if (shouldRefineLeft || shouldRefineRight) {
      FlowScope informed = blindScope.createChildFlowScope();
      if (shouldRefineLeft) {
        declareNameInScope(informed, left, restrictedLeftType);
      }
      if (shouldRefineRight) {
        declareNameInScope(informed, right, restrictedRightType);
      }
      return informed;
    }
    return blindScope;
  }

  private FlowScope caseNameOrGetProp(Node name, FlowScope blindScope,
      boolean outcome) {
    JSType type = getTypeIfRefinable(name, blindScope);
    if (type != null) {
      JSType restrictedType =
          type.getRestrictedTypeGivenToBooleanOutcome(outcome);
      FlowScope informed = blindScope.createChildFlowScope();
      declareNameInScope(informed, name, restrictedType);
      return informed;
    }
    return blindScope;
  }

  public JSType getRestrictedTypeGivenToBooleanOutcome(boolean outcome) {

    BooleanLiteralSet literals = getPossibleToBooleanOutcomes();
    if (literals.contains(outcome)) {
      return this;
    } else {
      return getNativeType(JSTypeNative.NO_TYPE);
    }
  }

// relevant test
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
        false, EVAL_ERROR_TYPE, UNKNOWN_TYPE);
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

    assertTypeEquals(CHECKED_UNKNOWN_TYPE,
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

    sup.defineProperty("X", NUMBER_TYPE, true, null);
    sup.setPropertyJSDocInfo("X", privateInfo);

    sub.defineProperty("X", NUMBER_TYPE, true, null);
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

// com.google.javascript.rhino.jstype.JSTypeTest::testIsTemplatedType
  public void testIsTemplatedType() throws Exception {
    assertTrue(
        new TemplateType(registry, "T")
            .hasAnyTemplate());
    assertFalse(
        ARRAY_TYPE
            .hasAnyTemplate());

    assertTrue(
        registry.createParameterizedType(
            ARRAY_TYPE, new TemplateType(registry, "T"))
            .hasAnyTemplate());
    assertFalse(
        registry.createParameterizedType(
            ARRAY_TYPE, STRING_TYPE)
            .hasAnyTemplate());

    assertTrue(
        new FunctionBuilder(registry)
            .withReturnType(new TemplateType(registry, "T"))
            .build()
            .hasAnyTemplate());
    assertTrue(
        new FunctionBuilder(registry)
            .withTypeOfThis(new TemplateType(registry, "T"))
            .build()
            .hasAnyTemplate());
    assertFalse(
        new FunctionBuilder(registry)
            .withReturnType(STRING_TYPE)
            .build()
            .hasAnyTemplate());

    assertTrue(
        registry.createUnionType(
            NULL_TYPE, new TemplateType(registry, "T"), STRING_TYPE)
            .hasAnyTemplate());
    assertFalse(
        registry.createUnionType(
            NULL_TYPE, ARRAY_TYPE, STRING_TYPE)
            .hasAnyTemplate());
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

// com.google.javascript.rhino.jstype.ParameterizedTypeTest::testParameterizedType
  public void testParameterizedType() throws Exception {
    ParameterizedType arrOfString = createParameterizedType(
        ARRAY_TYPE, STRING_TYPE);
    assertTypeCanAssignToItself(arrOfString);
    assertTrue(arrOfString.canAssignTo(ARRAY_TYPE));
    assertTrue(ARRAY_TYPE.canAssignTo(arrOfString));

    ParameterizedType arrOfNumber = createParameterizedType(
        ARRAY_TYPE, NUMBER_TYPE);
    assertTypeCanAssignToItself(arrOfNumber);
    assertTrue(arrOfNumber.canAssignTo(ARRAY_TYPE));
    assertTrue(ARRAY_TYPE.canAssignTo(arrOfNumber));

    assertTrue(arrOfString.isEquivalentTo(createParameterizedType(
        ARRAY_TYPE, STRING_TYPE)));

    assertFalse(arrOfString.isEquivalentTo(ARRAY_TYPE));
    assertFalse(arrOfString.isEquivalentTo(ARRAY_TYPE));
    assertFalse(arrOfString.isEquivalentTo(arrOfNumber));
    assertFalse(arrOfNumber.isEquivalentTo(arrOfString));
  }

// com.google.javascript.rhino.jstype.ParameterizedTypeTest::testPrint1
  public void testPrint1() throws Exception {
    ParameterizedType arrOfString = createParameterizedType(
        ARRAY_TYPE, STRING_TYPE);
    assertEquals("Array.<string>", arrOfString.toString());
  }

// com.google.javascript.rhino.jstype.ParameterizedTypeTest::testPrint2
  public void testPrint2() throws Exception {
    ParameterizedType arrOfTemplateType = createParameterizedType(
        ARRAY_TYPE, new TemplateType(registry, "T"));
    assertEquals("Array.<T>", arrOfTemplateType.toString());
  }

// com.google.javascript.rhino.jstype.ParameterizedTypeTest::testPrint3
  public void testPrint3() throws Exception {
    ParameterizedType arrOfUnknown = createParameterizedType(
        ARRAY_TYPE, UNKNOWN_TYPE);
    assertEquals("Array.<?>", arrOfUnknown.toString());
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

// com.google.javascript.rhino.jstype.UnionTypeTest::testShallowEquality
  public void testShallowEquality() {
    assertTrue(
        registry.createUnionType(ARRAY_TYPE, STRING_TYPE)
        .canTestForShallowEqualityWith(OBJECT_TYPE));
  }

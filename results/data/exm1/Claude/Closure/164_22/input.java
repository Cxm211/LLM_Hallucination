// buggy code
  public boolean isSubtype(JSType other) {
    if (!(other instanceof ArrowType)) {
      return false;
    }

    ArrowType that = (ArrowType) other;

    // This is described in Draft 2 of the ES4 spec,
    // Section 3.4.7: Subtyping Function Types.

    // this.returnType <: that.returnType (covariant)
    if (!this.returnType.isSubtype(that.returnType)) {
      return false;
    }

    // that.paramType[i] <: this.paramType[i] (contravariant)
    //
    // If this.paramType[i] is required,
    // then that.paramType[i] is required.
    //
    // In theory, the "required-ness" should work in the other direction as
    // well. In other words, if we have
    //
    // function f(number, number) {}
    // function g(number) {}
    //
    // Then f *should* not be a subtype of g, and g *should* not be
    // a subtype of f. But in practice, we do not implement it this way.
    // We want to support the use case where you can pass g where f is
    // expected, and pretend that g ignores the second argument.
    // That way, you can have a single "no-op" function, and you don't have
    // to create a new no-op function for every possible type signature.
    //
    // So, in this case, g < f, but f !< g
    Node thisParam = parameters.getFirstChild();
    Node thatParam = that.parameters.getFirstChild();
    while (thisParam != null && thatParam != null) {
      JSType thisParamType = thisParam.getJSType();
      JSType thatParamType = thatParam.getJSType();
      if (thisParamType != null) {
        if (thatParamType == null ||
            !thatParamType.isSubtype(thisParamType)) {
          return false;
        }
      }

      boolean thisIsVarArgs = thisParam.isVarArgs();
      boolean thatIsVarArgs = thatParam.isVarArgs();

      // "that" can't be a supertype, because it's missing a required argument.
        // NOTE(nicksantos): In our type system, we use {function(...?)} and
        // {function(...NoType)} to to indicate that arity should not be
        // checked. Strictly speaking, this is not a correct formulation,
        // because now a sub-function can required arguments that are var_args
        // in the super-function. So we special-case this.

      // don't advance if we have variable arguments
      if (!thisIsVarArgs) {
        thisParam = thisParam.getNext();
      }
      if (!thatIsVarArgs) {
        thatParam = thatParam.getNext();
      }

      // both var_args indicates the end
      if (thisIsVarArgs && thatIsVarArgs) {
        thisParam = null;
        thatParam = null;
      }
    }

    // "that" can't be a supertype, because it's missing a required arguement.

    return true;
  }

// relevant test
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

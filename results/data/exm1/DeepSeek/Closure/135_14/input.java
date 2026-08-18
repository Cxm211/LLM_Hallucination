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
    assertTrue(STRING_OBJECT_TYPE.getImplicitPrototype().isFunctionPrototypeType());
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

    
    assertEquals(STRING_OBJECT_TYPE, STRING_TYPE.autoboxesTo());

    
    assertEquals(STRING_TYPE, STRING_OBJECT_TYPE.unboxesTo());

    
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

    
    assertTrue(STRING_OBJECT_TYPE.canTestForEqualityWith(ALL_TYPE));
    assertTrue(STRING_OBJECT_TYPE.canTestForEqualityWith(STRING_OBJECT_TYPE));
    assertTrue(STRING_OBJECT_TYPE.canTestForEqualityWith(STRING_TYPE));
    assertTrue(STRING_OBJECT_TYPE.canTestForEqualityWith(functionType));
    assertTrue(STRING_OBJECT_TYPE.canTestForEqualityWith(OBJECT_TYPE));
    assertTrue(STRING_OBJECT_TYPE.canTestForEqualityWith(NUMBER_TYPE));
    assertTrue(STRING_OBJECT_TYPE.canTestForEqualityWith(BOOLEAN_TYPE));
    assertTrue(STRING_OBJECT_TYPE.canTestForEqualityWith(BOOLEAN_OBJECT_TYPE));
    assertTrue(STRING_OBJECT_TYPE.canTestForEqualityWith(DATE_TYPE));
    assertTrue(STRING_OBJECT_TYPE.canTestForEqualityWith(REGEXP_TYPE));
    assertTrue(STRING_OBJECT_TYPE.canTestForEqualityWith(ARRAY_TYPE));
    assertTrue(STRING_OBJECT_TYPE.canTestForEqualityWith(UNKNOWN_TYPE));

    
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
    assertEquals(NUMBER_TYPE, STRING_OBJECT_TYPE.getPropertyType("length"));

    
    assertTrue(STRING_OBJECT_TYPE.matchesInt32Context());
    assertTrue(STRING_OBJECT_TYPE.matchesNumberContext());
    assertTrue(STRING_OBJECT_TYPE.matchesObjectContext());
    assertTrue(STRING_OBJECT_TYPE.matchesStringContext());
    assertTrue(STRING_OBJECT_TYPE.matchesUint32Context());

    
    assertFalse(STRING_OBJECT_TYPE.isNullable());
    assertTrue(createNullableType(STRING_OBJECT_TYPE).isNullable());

    assertTrue(STRING_OBJECT_TYPE.isNativeObjectType());

    Asserts.assertResolvesToSame(STRING_OBJECT_TYPE);
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

    
    assertEquals(STRING_OBJECT_TYPE, STRING_TYPE.autoboxesTo());

    
    assertEquals(STRING_TYPE, STRING_OBJECT_TYPE.unboxesTo());

    
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

    
    assertTrue(STRING_TYPE.canTestForEqualityWith(ALL_TYPE));
    assertTrue(STRING_TYPE.canTestForEqualityWith(STRING_OBJECT_TYPE));
    assertTrue(STRING_TYPE.canTestForEqualityWith(functionType));
    assertTrue(STRING_TYPE.canTestForEqualityWith(OBJECT_TYPE));
    assertTrue(STRING_TYPE.canTestForEqualityWith(NUMBER_TYPE));
    assertTrue(STRING_TYPE.canTestForEqualityWith(BOOLEAN_TYPE));
    assertTrue(STRING_TYPE.canTestForEqualityWith(BOOLEAN_OBJECT_TYPE));
    assertTrue(STRING_TYPE.canTestForEqualityWith(DATE_TYPE));
    assertTrue(STRING_TYPE.canTestForEqualityWith(REGEXP_TYPE));
    assertTrue(STRING_TYPE.canTestForEqualityWith(ARRAY_TYPE));
    assertTrue(STRING_TYPE.canTestForEqualityWith(UNKNOWN_TYPE));

    
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

    
    assertEquals(NUMBER_TYPE, STRING_TYPE.findPropertyType("length"));
    assertEquals(null, STRING_TYPE.findPropertyType("unknownProperty"));

    Asserts.assertResolvesToSame(STRING_TYPE);
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

    
    assertTrue(recordType.canTestForEqualityWith(ALL_TYPE));
    assertTrue(recordType.canTestForEqualityWith(STRING_OBJECT_TYPE));
    assertTrue(recordType.canTestForEqualityWith(recordType));
    assertTrue(recordType.canTestForEqualityWith(functionType));
    assertTrue(recordType.canTestForEqualityWith(OBJECT_TYPE));
    assertTrue(recordType.canTestForEqualityWith(NUMBER_TYPE));
    assertTrue(recordType.canTestForEqualityWith(DATE_TYPE));
    assertTrue(recordType.canTestForEqualityWith(REGEXP_TYPE));

    
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

    
    assertTrue(functionInst.canTestForEqualityWith(ALL_TYPE));
    assertTrue(functionInst.canTestForEqualityWith(STRING_OBJECT_TYPE));
    assertTrue(functionInst.canTestForEqualityWith(functionInst));
    assertTrue(functionInst.canTestForEqualityWith(OBJECT_TYPE));
    assertTrue(functionInst.canTestForEqualityWith(NUMBER_TYPE));
    assertTrue(functionInst.canTestForEqualityWith(DATE_TYPE));
    assertTrue(functionInst.canTestForEqualityWith(REGEXP_TYPE));

    
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

    
    assertEquals(FUNCTION_FUNCTION_TYPE, functionInst.getConstructor());
    assertEquals(FUNCTION_PROTOTYPE, functionInst.getImplicitPrototype());
    assertEquals(functionInst, FUNCTION_FUNCTION_TYPE.getInstanceType());

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

    
    assertTrue(functionType.canTestForEqualityWith(ALL_TYPE));
    assertTrue(functionType.canTestForEqualityWith(STRING_OBJECT_TYPE));
    assertTrue(functionType.canTestForEqualityWith(functionType));
    assertTrue(functionType.canTestForEqualityWith(OBJECT_TYPE));
    assertTrue(functionType.canTestForEqualityWith(NUMBER_TYPE));
    assertTrue(functionType.canTestForEqualityWith(DATE_TYPE));
    assertTrue(functionType.canTestForEqualityWith(REGEXP_TYPE));

    
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
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testRecordTypeSubtyping
  public void testRecordTypeSubtyping() {
    RecordTypeBuilder builder = new RecordTypeBuilder(registry);
    builder.addProperty("a", NUMBER_TYPE);
    builder.addProperty("b", STRING_TYPE);
    builder.addProperty("c", STRING_TYPE);
    JSType subRecordType = builder.build();

    assertTrue(subRecordType.isSubtype(recordType));
    assertFalse(recordType.isSubtype(subRecordType));

    builder = new RecordTypeBuilder(registry);
    builder.addProperty("a", OBJECT_TYPE);
    builder.addProperty("b", STRING_TYPE);
    JSType differentRecordType = builder.build();

    assertFalse(differentRecordType.isSubtype(recordType));
    assertFalse(recordType.isSubtype(differentRecordType));
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testRecordTypeSubtypingWithInferredProperties
  public void testRecordTypeSubtypingWithInferredProperties() {
    RecordTypeBuilder builder = new RecordTypeBuilder(registry);
    builder.addProperty("a", googSubBarInst);
    JSType record = builder.build();

    ObjectType subtypeProp = registry.createAnonymousObjectType();
    subtypeProp.defineInferredProperty("a", googSubSubBarInst, false);
    assertTrue(subtypeProp.isSubtype(record));
    assertFalse(record.isSubtype(subtypeProp));

    ObjectType supertypeProp = registry.createAnonymousObjectType();
    supertypeProp.defineInferredProperty("a", googBarInst, false);
    assertFalse(supertypeProp.isSubtype(record));
    assertFalse(record.isSubtype(supertypeProp));

    ObjectType declaredSubtypeProp = registry.createAnonymousObjectType();
    declaredSubtypeProp.defineDeclaredProperty("a", googSubSubBarInst, false);
    assertFalse(declaredSubtypeProp.isSubtype(record));
    assertFalse(record.isSubtype(declaredSubtypeProp));
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testRecordTypeLeastSuperType1
  public void testRecordTypeLeastSuperType1() {
    RecordTypeBuilder builder = new RecordTypeBuilder(registry);
    builder.addProperty("a", NUMBER_TYPE);
    builder.addProperty("b", STRING_TYPE);
    builder.addProperty("c", STRING_TYPE);
    JSType subRecordType = builder.build();

    JSType leastSupertype = recordType.getLeastSupertype(subRecordType);
    assertEquals(leastSupertype, recordType);
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testRecordTypeLeastSuperType2
  public void testRecordTypeLeastSuperType2() {
    RecordTypeBuilder builder = new RecordTypeBuilder(registry);
    builder.addProperty("e", NUMBER_TYPE);
    builder.addProperty("b", STRING_TYPE);
    builder.addProperty("c", STRING_TYPE);
    JSType subRecordType = builder.build();

    JSType leastSupertype = recordType.getLeastSupertype(subRecordType);

    builder = new RecordTypeBuilder(registry);
    builder.addProperty("b", STRING_TYPE);

    assertEquals(leastSupertype, builder.build());
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testRecordTypeLeastSuperType3
  public void testRecordTypeLeastSuperType3() {
    RecordTypeBuilder builder = new RecordTypeBuilder(registry);
    builder.addProperty("d", NUMBER_TYPE);
    builder.addProperty("e", STRING_TYPE);
    builder.addProperty("f", STRING_TYPE);
    JSType subRecordType = builder.build();

    JSType leastSupertype = recordType.getLeastSupertype(subRecordType);
    assertEquals(leastSupertype, OBJECT_TYPE);
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testRecordTypeLeastSuperType4
  public void testRecordTypeLeastSuperType4() {
    JSType leastSupertype = recordType.getLeastSupertype(OBJECT_TYPE);
    assertEquals(leastSupertype, OBJECT_TYPE);
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testRecordTypeGreatestSubType1
  public void testRecordTypeGreatestSubType1() {
    RecordTypeBuilder builder = new RecordTypeBuilder(registry);
    builder.addProperty("d", NUMBER_TYPE);
    builder.addProperty("e", STRING_TYPE);
    builder.addProperty("f", STRING_TYPE);

    JSType subRecordType = builder.build();

    JSType subtype = recordType.getGreatestSubtype(subRecordType);

    builder = new RecordTypeBuilder(registry);
    builder.addProperty("d", NUMBER_TYPE);
    builder.addProperty("e", STRING_TYPE);
    builder.addProperty("f", STRING_TYPE);
    builder.addProperty("a", NUMBER_TYPE);
    builder.addProperty("b", STRING_TYPE);

    assertEquals(subtype, builder.build());
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testRecordTypeGreatestSubType2
  public void testRecordTypeGreatestSubType2() {
    RecordTypeBuilder builder = new RecordTypeBuilder(registry);

    JSType subRecordType = builder.build();

    JSType subtype = recordType.getGreatestSubtype(subRecordType);

    builder = new RecordTypeBuilder(registry);
    builder.addProperty("a", NUMBER_TYPE);
    builder.addProperty("b", STRING_TYPE);

    assertEquals(subtype, builder.build());
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testRecordTypeGreatestSubType3
  public void testRecordTypeGreatestSubType3() {
    RecordTypeBuilder builder = new RecordTypeBuilder(registry);
    builder.addProperty("a", NUMBER_TYPE);
    builder.addProperty("b", STRING_TYPE);
    builder.addProperty("c", STRING_TYPE);

    JSType subRecordType = builder.build();

    JSType subtype = recordType.getGreatestSubtype(subRecordType);

    builder = new RecordTypeBuilder(registry);
    builder.addProperty("a", NUMBER_TYPE);
    builder.addProperty("b", STRING_TYPE);
    builder.addProperty("c", STRING_TYPE);

    assertEquals(subtype, builder.build());
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testRecordTypeGreatestSubType4
  public void testRecordTypeGreatestSubType4() {
    RecordTypeBuilder builder = new RecordTypeBuilder(registry);
    builder.addProperty("a", STRING_TYPE);
    builder.addProperty("b", STRING_TYPE);
    builder.addProperty("c", STRING_TYPE);

    JSType subRecordType = builder.build();

    JSType subtype = recordType.getGreatestSubtype(subRecordType);
    assertEquals(subtype, NO_TYPE);
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testRecordTypeGreatestSubType5
  public void testRecordTypeGreatestSubType5() {
    RecordTypeBuilder builder = new RecordTypeBuilder(registry);
    builder.addProperty("a", STRING_TYPE);

    JSType recordType = builder.build();

    assertEquals(NO_OBJECT_TYPE,
                 recordType.getGreatestSubtype(U2U_CONSTRUCTOR_TYPE));

    
    
    U2U_CONSTRUCTOR_TYPE.defineDeclaredProperty("a", STRING_TYPE, false);
    assertEquals(U2U_CONSTRUCTOR_TYPE,
                 recordType.getGreatestSubtype(U2U_CONSTRUCTOR_TYPE));
    assertEquals(U2U_CONSTRUCTOR_TYPE,
                 U2U_CONSTRUCTOR_TYPE.getGreatestSubtype(recordType));
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testRecordTypeGreatestSubType6
  public void testRecordTypeGreatestSubType6() {
    RecordTypeBuilder builder = new RecordTypeBuilder(registry);
    builder.addProperty("x", UNKNOWN_TYPE);

    JSType recordType = builder.build();

    assertEquals(NO_OBJECT_TYPE,
                 recordType.getGreatestSubtype(U2U_CONSTRUCTOR_TYPE));

    
    
    U2U_CONSTRUCTOR_TYPE.defineDeclaredProperty("x", STRING_TYPE, false);
    assertEquals(U2U_CONSTRUCTOR_TYPE,
                 recordType.getGreatestSubtype(U2U_CONSTRUCTOR_TYPE));
    assertEquals(U2U_CONSTRUCTOR_TYPE,
                 U2U_CONSTRUCTOR_TYPE.getGreatestSubtype(recordType));
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testRecordTypeGreatestSubType7
  public void testRecordTypeGreatestSubType7() {
    RecordTypeBuilder builder = new RecordTypeBuilder(registry);
    builder.addProperty("x", NUMBER_TYPE);

    JSType recordType = builder.build();

    
    
    U2U_CONSTRUCTOR_TYPE.defineDeclaredProperty("x", STRING_TYPE, false);
    assertEquals(NO_OBJECT_TYPE,
                 recordType.getGreatestSubtype(U2U_CONSTRUCTOR_TYPE));
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testRecordTypeGreatestSubType8
  public void testRecordTypeGreatestSubType8() {
    RecordTypeBuilder builder = new RecordTypeBuilder(registry);
    builder.addProperty("xyz", UNKNOWN_TYPE);

    JSType recordType = builder.build();

    assertEquals(NO_OBJECT_TYPE,
                 recordType.getGreatestSubtype(U2U_CONSTRUCTOR_TYPE));

    
    
    googBar.defineDeclaredProperty("xyz", STRING_TYPE, false);

    assertEquals(googBar,
                 recordType.getGreatestSubtype(U2U_CONSTRUCTOR_TYPE));
    assertEquals(googBar,
                 U2U_CONSTRUCTOR_TYPE.getGreatestSubtype(recordType));

    ObjectType googBarInst = googBar.getInstanceType();
    assertEquals(NO_OBJECT_TYPE,
                 recordType.getGreatestSubtype(googBarInst));
    assertEquals(NO_OBJECT_TYPE,
                 googBarInst.getGreatestSubtype(recordType));
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testApplyOfDateMethod
  public void testApplyOfDateMethod() {
    JSType applyType = dateMethod.getPropertyType("apply");
    assertTrue("apply should be a function",
        applyType instanceof FunctionType);

    FunctionType applyFn = (FunctionType) applyType;
    assertEquals("apply should have the same return type as its function",
        NUMBER_TYPE, applyFn.getReturnType());

    Node params = applyFn.getParametersNode();
    assertEquals("apply takes two args",
        2, params.getChildCount());
    assertEquals("apply's first arg is the @this type",
        registry.createOptionalNullableType(DATE_TYPE),
        params.getFirstChild().getJSType());
    assertEquals("apply's second arg is an Array",
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
    assertEquals("call should have the same return type as its function",
        NUMBER_TYPE, callFn.getReturnType());

    Node params = callFn.getParametersNode();
    assertEquals("call takes one argument in this case",
        1, params.getChildCount());
    assertEquals("call's first arg is the @this type",
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

    assertEquals("function (this:Array, ...[*]): Array",
        ARRAY_FUNCTION_TYPE.toString());

    assertEquals("function (this:Boolean, *): boolean",
        BOOLEAN_OBJECT_FUNCTION_TYPE.toString());

    assertEquals("function (this:Number, *): number",
        NUMBER_OBJECT_FUNCTION_TYPE.toString());

    assertEquals("function (this:String, *): string",
        STRING_OBJECT_FUNCTION_TYPE.toString());

    assertEquals("function (...[number]): boolean",
        registry.createFunctionType(BOOLEAN_TYPE, true, NUMBER_TYPE)
        .toString());

    assertEquals("function (number, ...[string]): boolean",
        registry.createFunctionType(BOOLEAN_TYPE, true, NUMBER_TYPE,
            STRING_TYPE).toString());

    assertEquals("function (this:Date, number): (boolean|number|string)",
        new FunctionType(registry, null,
            null,
            new JSTypeRegistry(null).createParameters(NUMBER_TYPE),
            NUMBER_STRING_BOOLEAN,
            DATE_TYPE).toString());
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testFunctionTypeRelationships
  public void testFunctionTypeRelationships() {
    FunctionType dateMethodEmpty = new FunctionType(registry, null, null,
        null, null, DATE_TYPE);
    FunctionType dateMethodWithParam = new FunctionType(registry, null, null,
        new JSTypeRegistry(null).createParameters(NUMBER_TYPE), null, DATE_TYPE);
    FunctionType dateMethodWithReturn = new FunctionType(registry, null, null,
        null, NUMBER_TYPE, DATE_TYPE);
    FunctionType stringMethodEmpty = new FunctionType(registry, null, null,
        null, null, STRING_OBJECT_TYPE);
    FunctionType stringMethodWithParam = new FunctionType(registry, null, null,
        new JSTypeRegistry(null).createParameters(NUMBER_TYPE), null,
        STRING_OBJECT_TYPE);
    FunctionType stringMethodWithReturn = new FunctionType(registry, null,
        null, null, NUMBER_TYPE, STRING_OBJECT_TYPE);

    
    assertFalse(stringMethodEmpty.isSubtype(dateMethodEmpty));

    
    List<FunctionType> allFunctions = Lists.newArrayList(
        dateMethodEmpty, dateMethodWithParam, dateMethodWithReturn,
        stringMethodEmpty, stringMethodWithParam, stringMethodWithReturn);
    for (int i = 0; i < allFunctions.size(); i++) {
      for (int j = 0; j < allFunctions.size(); j++) {
        FunctionType typeA = allFunctions.get(i);
        FunctionType typeB = allFunctions.get(j);
        assertEquals(String.format("equals(%s, %s)", typeA, typeB),
            i == j, typeA.equals(typeB));

        
        
        assertEquals(String.format("isSubtype(%s, %s)", typeA, typeB),
            typeA.getTypeOfThis().equals(typeB.getTypeOfThis()),
            typeA.isSubtype(typeB));

        if (i == j) {
          assertEquals(typeA, typeA.getLeastSupertype(typeB));
          assertEquals(typeA, typeA.getGreatestSubtype(typeB));
        } else {
          assertEquals(String.format("sup(%s, %s)", typeA, typeB),
              U2U_CONSTRUCTOR_TYPE, typeA.getLeastSupertype(typeB));
          assertEquals(String.format("inf(%s, %s)", typeA, typeB),
              NO_OBJECT_TYPE, typeA.getGreatestSubtype(typeB));
        }
      }
    }
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testFunctionSubTypeRelationships
  public void testFunctionSubTypeRelationships() {
    FunctionType googBarMethod = new FunctionType(registry, null, null,
        null, null, googBar);
    FunctionType googBarParamFn = new FunctionType(registry, null, null,
        new JSTypeRegistry(null).createParameters(googBar), null, null);
    FunctionType googBarReturnFn = new FunctionType(registry, null, null,
        null, googBar, null);
    FunctionType googSubBarMethod = new FunctionType(registry, null, null,
        null, null, googSubBar);
    FunctionType googSubBarParamFn = new FunctionType(registry, null, null,
        new JSTypeRegistry(null).createParameters(googSubBar), null, null);
    FunctionType googSubBarReturnFn = new FunctionType(registry, null, null,
        null, googSubBar, null);

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
            i == j, typeA.equals(typeB));

        
        
        if (i == j) {
          assertEquals(typeA, typeA.getLeastSupertype(typeB));
          assertEquals(typeA, typeA.getGreatestSubtype(typeB));
        } else {
          assertEquals(String.format("sup(%s, %s)", typeA, typeB),
              U2U_CONSTRUCTOR_TYPE, typeA.getLeastSupertype(typeB));
          assertEquals(String.format("inf(%s, %s)", typeA, typeB),
              NO_OBJECT_TYPE, typeA.getGreatestSubtype(typeB));
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
    prototype.defineDeclaredProperty("foo", DATE_TYPE, false);

    assertEquals(NATIVE_PROPERTIES_COUNT + 1, instance.getPropertiesCount());
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testFunctionPrototypeAndImplicitPrototype2
  public void testFunctionPrototypeAndImplicitPrototype2() {
    FunctionType constructor =
        registry.createConstructorType(null, null, null, null);
    ObjectType instance = constructor.getInstanceType();

    
    ObjectType prototype = registry.createAnonymousObjectType();
    prototype.defineDeclaredProperty("foo", DATE_TYPE, false);
    constructor.defineDeclaredProperty("prototype", prototype, true);

    assertEquals(NATIVE_PROPERTIES_COUNT + 1, instance.getPropertiesCount());
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testFunctionCyclycity
  public void testFunctionCyclycity() throws Exception {
    FunctionPrototypeType instanceType =
        new FunctionPrototypeType(
            registry, U2U_CONSTRUCTOR_TYPE,
            U2U_CONSTRUCTOR_TYPE.getInstanceType());
    U2U_CONSTRUCTOR_TYPE.setPrototype(instanceType);
    U2U_CONSTRUCTOR_TYPE.detectImplicitPrototypeCycle();
    instanceType.detectImplicitPrototypeCycle();
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testJSDocOnPrototypeProperty
  public void testJSDocOnPrototypeProperty() throws Exception {
    subclassCtor.setPropertyJSDocInfo("prototype", new JSDocInfo(), true);
    assertNull(subclassCtor.getOwnPropertyJSDocInfo("prototype"));
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testVoidType
  public void testVoidType() throws Exception {
    
    assertTrue(VOID_TYPE.canAssignTo(ALL_TYPE));
    assertFalse(VOID_TYPE.canAssignTo(STRING_OBJECT_TYPE));
    assertFalse(VOID_TYPE.canAssignTo(REGEXP_TYPE));

    
    assertNull(VOID_TYPE.autoboxesTo());

    
    assertTrue(VOID_TYPE.canTestForEqualityWith(ALL_TYPE));
    assertFalse(VOID_TYPE.canTestForEqualityWith(REGEXP_TYPE));

    
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

    
    assertEquals(BOOLEAN_OBJECT_TYPE, BOOLEAN_TYPE.autoboxesTo());

    
    assertEquals(BOOLEAN_TYPE, BOOLEAN_OBJECT_TYPE.unboxesTo());

    
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

    
    assertTrue(BOOLEAN_TYPE.canTestForEqualityWith(ALL_TYPE));
    assertTrue(BOOLEAN_TYPE.canTestForEqualityWith(STRING_OBJECT_TYPE));
    assertTrue(BOOLEAN_TYPE.canTestForEqualityWith(NUMBER_TYPE));
    assertTrue(BOOLEAN_TYPE.canTestForEqualityWith(functionType));
    assertFalse(BOOLEAN_TYPE.canTestForEqualityWith(VOID_TYPE));
    assertTrue(BOOLEAN_TYPE.canTestForEqualityWith(OBJECT_TYPE));
    assertTrue(BOOLEAN_TYPE.canTestForEqualityWith(DATE_TYPE));
    assertTrue(BOOLEAN_TYPE.canTestForEqualityWith(REGEXP_TYPE));
    assertTrue(BOOLEAN_TYPE.canTestForEqualityWith(UNKNOWN_TYPE));

    
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

    
    assertTrue(BOOLEAN_OBJECT_TYPE.canTestForEqualityWith(ALL_TYPE));
    assertTrue(BOOLEAN_OBJECT_TYPE.canTestForEqualityWith(STRING_OBJECT_TYPE));
    assertTrue(BOOLEAN_OBJECT_TYPE.canTestForEqualityWith(NUMBER_TYPE));
    assertTrue(BOOLEAN_OBJECT_TYPE.canTestForEqualityWith(functionType));
    assertFalse(BOOLEAN_OBJECT_TYPE.canTestForEqualityWith(VOID_TYPE));
    assertTrue(BOOLEAN_OBJECT_TYPE.canTestForEqualityWith(OBJECT_TYPE));
    assertTrue(BOOLEAN_OBJECT_TYPE.canTestForEqualityWith(DATE_TYPE));
    assertTrue(BOOLEAN_OBJECT_TYPE.canTestForEqualityWith(REGEXP_TYPE));

    
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

    assertTrue(BOOLEAN_OBJECT_TYPE.isNativeObjectType());

    Asserts.assertResolvesToSame(BOOLEAN_OBJECT_TYPE);
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testEnumType
  public void testEnumType() throws Exception {
    EnumType enumType = new EnumType(registry, "Enum", NUMBER_TYPE);

    
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

    
    assertTrue(enumType.canTestForEqualityWith(ALL_TYPE));
    assertTrue(enumType.canTestForEqualityWith(STRING_OBJECT_TYPE));
    assertTrue(enumType.canTestForEqualityWith(NUMBER_TYPE));
    assertTrue(enumType.canTestForEqualityWith(functionType));
    assertFalse(enumType.canTestForEqualityWith(VOID_TYPE));
    assertTrue(enumType.canTestForEqualityWith(OBJECT_TYPE));
    assertTrue(enumType.canTestForEqualityWith(DATE_TYPE));
    assertTrue(enumType.canTestForEqualityWith(REGEXP_TYPE));

    
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

    
    assertTrue(elementsType.canTestForEqualityWith(ALL_TYPE));
    assertTrue(elementsType.canTestForEqualityWith(STRING_OBJECT_TYPE));
    assertTrue(elementsType.canTestForEqualityWith(NUMBER_TYPE));
    assertTrue(elementsType.canTestForEqualityWith(NUMBER_OBJECT_TYPE));
    assertTrue(elementsType.canTestForEqualityWith(elementsType));
    assertTrue(elementsType.canTestForEqualityWith(functionType));
    assertFalse(elementsType.canTestForEqualityWith(VOID_TYPE));
    assertTrue(elementsType.canTestForEqualityWith(OBJECT_TYPE));
    assertTrue(elementsType.canTestForEqualityWith(DATE_TYPE));
    assertTrue(elementsType.canTestForEqualityWith(REGEXP_TYPE));

    
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

    Asserts.assertResolvesToSame(elementsType);
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testStringEnumType
  public void testStringEnumType() throws Exception {
    EnumElementType stringEnum =
        new EnumType(registry, "Enum", STRING_TYPE).getElementsType();

    assertEquals(UNKNOWN_TYPE, stringEnum.getPropertyType("length"));
    assertEquals(NUMBER_TYPE, stringEnum.findPropertyType("length"));
    assertEquals(false, stringEnum.hasProperty("length"));
    assertEquals(STRING_OBJECT_TYPE, stringEnum.autoboxesTo());
    assertNull(stringEnum.getConstructor());

    Asserts.assertResolvesToSame(stringEnum);
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testStringObjectEnumType
  public void testStringObjectEnumType() throws Exception {
    EnumElementType stringEnum =
        new EnumType(registry, "Enum", STRING_OBJECT_TYPE).getElementsType();

    assertEquals(NUMBER_TYPE, stringEnum.getPropertyType("length"));
    assertEquals(NUMBER_TYPE, stringEnum.findPropertyType("length"));
    assertEquals(true, stringEnum.hasProperty("length"));
    assertEquals(STRING_OBJECT_FUNCTION_TYPE, stringEnum.getConstructor());
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testUnionType
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

    
    assertTrue(objectType.canTestForEqualityWith(NUMBER_TYPE));

    
    assertFalse(objectType.matchesInt32Context());
    assertFalse(objectType.matchesNumberContext());
    assertTrue(objectType.matchesObjectContext());
    assertFalse(objectType.matchesStringContext());
    assertFalse(objectType.matchesUint32Context());

    
    assertFalse(objectType.isNullable());
    assertTrue(createNullableType(objectType).isNullable());

    
    assertEquals("{...}", objectType.toString());

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
    assertTrue(googBar.equals(googBar));
    assertFalse(googBar.equals(googSubBar));

    Asserts.assertResolvesToSame(googBar);
    Asserts.assertResolvesToSame(googSubBar);
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testObjectTypePropertiesCount
  public void testObjectTypePropertiesCount() throws Exception {
    ObjectType sup = registry.createAnonymousObjectType();
    int nativeProperties = sup.getPropertiesCount();

    sup.defineDeclaredProperty("a", DATE_TYPE, false);
    assertEquals(nativeProperties + 1, sup.getPropertiesCount());

    sup.defineDeclaredProperty("b", DATE_TYPE, false);
    assertEquals(nativeProperties + 2, sup.getPropertiesCount());

    ObjectType sub = registry.createObjectType(sup);
    assertEquals(nativeProperties + 2, sub.getPropertiesCount());
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testDefineProperties
  public void testDefineProperties() {
    ObjectType prototype = googBar.getPrototype();
    ObjectType instance = googBar.getInstanceType();

    assertEquals(instance.getImplicitPrototype(), prototype);

    
    assertTrue(
        prototype.defineDeclaredProperty("declared", NUMBER_TYPE, false));
    assertFalse(
        prototype.defineDeclaredProperty("declared", NUMBER_TYPE, false));
    assertFalse(
        instance.defineDeclaredProperty("declared", NUMBER_TYPE, false));
    assertEquals(NUMBER_TYPE, instance.getPropertyType("declared"));

    
    assertTrue(
        prototype.defineInferredProperty("inferred1", STRING_TYPE, false));
    assertTrue(
        prototype.defineInferredProperty("inferred1", NUMBER_TYPE, false));
    assertEquals(
        createUnionType(NUMBER_TYPE, STRING_TYPE),
        instance.getPropertyType("inferred1"));

    
    assertTrue(
        prototype.defineInferredProperty("inferred2", STRING_TYPE, false));
    assertTrue(
        instance.defineInferredProperty("inferred2", NUMBER_TYPE, false));
    assertEquals(
        createUnionType(NUMBER_TYPE, STRING_TYPE),
        instance.getPropertyType("inferred2"));

    
    assertTrue(
        prototype.defineInferredProperty("prop", STRING_TYPE, false));
    assertTrue(
        instance.defineDeclaredProperty("prop", NUMBER_TYPE, false));
    assertEquals(NUMBER_TYPE, instance.getPropertyType("prop"));
    assertEquals(STRING_TYPE, prototype.getPropertyType("prop"));
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testObjectTypePropertiesCountWithShadowing
  public void testObjectTypePropertiesCountWithShadowing() {
    ObjectType sup = registry.createAnonymousObjectType();
    int nativeProperties = sup.getPropertiesCount();

    sup.defineDeclaredProperty("a", OBJECT_TYPE, false);
    assertEquals(nativeProperties + 1, sup.getPropertiesCount());

    ObjectType sub = registry.createObjectType(sup);
    sub.defineDeclaredProperty("a", OBJECT_TYPE, false);
    assertEquals(nativeProperties + 1, sub.getPropertiesCount());
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testObjectTypeIsPropertyInExterns
  public void testObjectTypeIsPropertyInExterns() {
    ObjectType sup =
        registry.createObjectType(registry.createAnonymousObjectType());
    ObjectType sub = registry.createObjectType(sup);

    sup.defineProperty("externProp", null, false,  true);
    sub.defineProperty("externProp", null, false,  false);

    assertTrue(sup.isPropertyInExterns("externProp"));
    assertFalse(sub.isPropertyInExterns("externProp"));
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

    
    assertEquals(DATE_TYPE, namedGoogBar.getPropertyType("date"));

    assertFalse(namedGoogBar.isNativeObjectType());
    assertFalse(namedGoogBar.getImplicitPrototype().isNativeObjectType());

    JSType resolvedNamedGoogBar = Asserts.assertValidResolve(namedGoogBar);
    assertNotSame(resolvedNamedGoogBar, namedGoogBar);
    assertSame(resolvedNamedGoogBar, googBar.getInstanceType());
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testPrototypeChaining
  public void testPrototypeChaining() throws Exception {
    
    assertEquals(ARRAY_TYPE.getImplicitPrototype().getImplicitPrototype(),
        OBJECT_TYPE);
    assertEquals(BOOLEAN_OBJECT_TYPE.getImplicitPrototype().
        getImplicitPrototype(), OBJECT_TYPE);
    assertEquals(DATE_TYPE.getImplicitPrototype().getImplicitPrototype(),
        OBJECT_TYPE);
    assertEquals(ERROR_TYPE.getImplicitPrototype().getImplicitPrototype(),
        OBJECT_TYPE);
    assertEquals(EVAL_ERROR_TYPE.getImplicitPrototype().getImplicitPrototype(),
        ERROR_TYPE);
    assertEquals(NUMBER_OBJECT_TYPE.getImplicitPrototype().
        getImplicitPrototype(), OBJECT_TYPE);
    assertEquals(URI_ERROR_TYPE.getImplicitPrototype().getImplicitPrototype(),
        ERROR_TYPE);
    assertEquals(RANGE_ERROR_TYPE.getImplicitPrototype().getImplicitPrototype(),
        ERROR_TYPE);
    assertEquals(REFERENCE_ERROR_TYPE.getImplicitPrototype().
        getImplicitPrototype(), ERROR_TYPE);
    assertEquals(STRING_OBJECT_TYPE.getImplicitPrototype().
        getImplicitPrototype(), OBJECT_TYPE);
    assertEquals(REGEXP_TYPE.getImplicitPrototype().getImplicitPrototype(),
        OBJECT_TYPE);
    assertEquals(SYNTAX_ERROR_TYPE.getImplicitPrototype().
        getImplicitPrototype(), ERROR_TYPE);
    assertEquals(TYPE_ERROR_TYPE.getImplicitPrototype().
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
    
    assertEquals(ARRAY_FUNCTION_TYPE, ARRAY_TYPE.getConstructor());

    
    assertEquals(BOOLEAN_OBJECT_FUNCTION_TYPE,
        BOOLEAN_OBJECT_TYPE.getConstructor());

    
    assertEquals(DATE_FUNCTION_TYPE, DATE_TYPE.getConstructor());

    
    assertEquals(ERROR_FUNCTION_TYPE, ERROR_TYPE.getConstructor());

    
    assertEquals(EVAL_ERROR_FUNCTION_TYPE, EVAL_ERROR_TYPE.getConstructor());

    
    assertEquals(NUMBER_OBJECT_FUNCTION_TYPE,
        NUMBER_OBJECT_TYPE.getConstructor());

    
    assertEquals(OBJECT_FUNCTION_TYPE, OBJECT_TYPE.getConstructor());

    
    assertEquals(RANGE_ERROR_FUNCTION_TYPE, RANGE_ERROR_TYPE.getConstructor());

    
    assertEquals(REFERENCE_ERROR_FUNCTION_TYPE,
        REFERENCE_ERROR_TYPE.getConstructor());

    
    assertEquals(REGEXP_FUNCTION_TYPE, REGEXP_TYPE.getConstructor());

    
    assertEquals(STRING_OBJECT_FUNCTION_TYPE,
        STRING_OBJECT_TYPE.getConstructor());

    
    assertEquals(SYNTAX_ERROR_FUNCTION_TYPE,
        SYNTAX_ERROR_TYPE.getConstructor());

    
    assertEquals(TYPE_ERROR_FUNCTION_TYPE, TYPE_ERROR_TYPE.getConstructor());

    
    assertEquals(URI_ERROR_FUNCTION_TYPE, URI_ERROR_TYPE.getConstructor());
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testCanTestForEqualityWithCornerCases
      public void testCanTestForEqualityWithCornerCases() {
    
    assertFalse(NULL_TYPE.canTestForEqualityWith(VOID_TYPE));

    
    UnionType nullableObject =
        (UnionType) createUnionType(OBJECT_TYPE, NULL_TYPE);
    assertTrue(nullableObject.canTestForEqualityWith(VOID_TYPE));
    assertTrue(VOID_TYPE.canTestForEqualityWith(nullableObject));
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testTestForEquality
  public void testTestForEquality() {
    compare(TRUE, NO_OBJECT_TYPE, NO_OBJECT_TYPE);
    compare(UNKNOWN, ALL_TYPE, ALL_TYPE);
    compare(TRUE, NO_TYPE, NO_TYPE);
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

// com.google.javascript.rhino.jstype.JSTypeTest::testSubtypingUnionTypes
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
            aOnB.equals(bOnA));
      }
    }
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testWeirdBug
  public void testWeirdBug() {
    assertFalse(googBar.equals(googBar.getInstanceType()));
    assertFalse(googBar.getInstanceType().equals(googBar));
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
            aOnB.equals(bOnA));
      }
    }
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testReflexivityOfLeastSupertype
  public void testReflexivityOfLeastSupertype() {
    List<JSType> list = getTypesToTestForSymmetry();
    for (JSType type : list) {
      assertEquals("getLeastSupertype not reflexive",
          type, type.getLeastSupertype(type));
    }
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testReflexivityOfGreatestSubtype
  public void testReflexivityOfGreatestSubtype() {
    List<JSType> list = getTypesToTestForSymmetry();
    for (JSType type : list) {
      assertEquals("getGreatestSubtype not reflexive",
          type, type.getGreatestSubtype(type));
    }
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testLeastSupertypeUnresolvedNamedType
  public void testLeastSupertypeUnresolvedNamedType() {
    
    JSType expected = registry.createUnionType(
        unresolvedNamedType, U2U_FUNCTION_TYPE);
    assertEquals(expected,
        unresolvedNamedType.getLeastSupertype(U2U_FUNCTION_TYPE));
    assertEquals(expected,
        U2U_FUNCTION_TYPE.getLeastSupertype(unresolvedNamedType));
    assertEquals("(function (...[?]): ?|not.resolved.named.type)",
        expected.toString());
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testLeastSupertypeUnresolvedNamedType2
  public void testLeastSupertypeUnresolvedNamedType2() {
    JSType expected = registry.createUnionType(
        unresolvedNamedType, UNKNOWN_TYPE);
    assertEquals(expected,
        unresolvedNamedType.getLeastSupertype(UNKNOWN_TYPE));
    assertEquals(expected,
        UNKNOWN_TYPE.getLeastSupertype(unresolvedNamedType));
    assertEquals(UNKNOWN_TYPE, expected);
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testLeastSupertypeUnresolvedNamedType3
  public void testLeastSupertypeUnresolvedNamedType3() {
    JSType expected = registry.createUnionType(
        unresolvedNamedType, CHECKED_UNKNOWN_TYPE);
    assertEquals(expected,
        unresolvedNamedType.getLeastSupertype(CHECKED_UNKNOWN_TYPE));
    assertEquals(expected,
        CHECKED_UNKNOWN_TYPE.getLeastSupertype(unresolvedNamedType));
    assertEquals(CHECKED_UNKNOWN_TYPE, expected);
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testSubclassOfUnresolvedNamedType
  public void testSubclassOfUnresolvedNamedType() {
    assertTrue(subclassOfUnresolvedNamedType.isUnknownType());
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testNamedTypeEquals
  public void testNamedTypeEquals() {
    JSTypeRegistry jst = new JSTypeRegistry(null);

    
    NamedType a = new NamedType(jst, "type1", "source", 1, 0);
    NamedType b = new NamedType(jst, "type1", "source", 1, 0);
    assertTrue(a.equals(b));

    
    assertFalse(a.equals("type1"));

    
    assertTrue(namedGoogBar.equals(googBar.getInstanceType()));
    assertTrue(googBar.getInstanceType().equals(namedGoogBar));
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
    assertEquals(a, realA);
    assertEquals(b.hashCode(), realB.hashCode());
    assertEquals(b, realB);

    a.resolve(null, null);
    b.resolve(null, null);

    assertTrue(a.isResolved());
    assertTrue(b.isResolved());
    assertEquals(a.hashCode(), realA.hashCode());
    assertEquals(a, realA);
    assertEquals(b.hashCode(), realB.hashCode());
    assertEquals(b, realB);

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
    assertEquals(a, b);

    a.resolve(null, EMPTY_SCOPE);

    assertTrue(a.isResolved());
    assertFalse(b.isResolved());

    assertEquals(a.hashCode(), b.hashCode());
    assertEquals(a, b);

    assertFalse(a.equals(UNKNOWN_TYPE));
    assertFalse(b.equals(UNKNOWN_TYPE));
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testForwardDeclaredNamedType
  public void testForwardDeclaredNamedType() {
    NamedType a = new NamedType(registry, "typeA", "source", 1, 0);
    registry.forwardDeclareType("typeA");

    assertEquals(UNKNOWN_TYPE, a.getLeastSupertype(UNKNOWN_TYPE));
    assertEquals(CHECKED_UNKNOWN_TYPE,
        a.getLeastSupertype(CHECKED_UNKNOWN_TYPE));
    assertEquals(UNKNOWN_TYPE, UNKNOWN_TYPE.getLeastSupertype(a));
    assertEquals(CHECKED_UNKNOWN_TYPE,
        CHECKED_UNKNOWN_TYPE.getLeastSupertype(a));
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testGreatestSubtypeSimpleTypes
  public void testGreatestSubtypeSimpleTypes() {
    assertEquals(ARRAY_TYPE,
        ARRAY_TYPE.getGreatestSubtype(ALL_TYPE));
    assertEquals(ARRAY_TYPE,
        ALL_TYPE.getGreatestSubtype(ARRAY_TYPE));
    assertEquals(NO_OBJECT_TYPE,
        REGEXP_TYPE.getGreatestSubtype(NO_OBJECT_TYPE));
    assertEquals(NO_OBJECT_TYPE,
        NO_OBJECT_TYPE.getGreatestSubtype(REGEXP_TYPE));
    assertEquals(NO_OBJECT_TYPE,
        ARRAY_TYPE.getGreatestSubtype(STRING_OBJECT_TYPE));
    assertEquals(NO_TYPE, ARRAY_TYPE.getGreatestSubtype(NUMBER_TYPE));
    assertEquals(NO_OBJECT_TYPE, ARRAY_TYPE.getGreatestSubtype(functionType));
    assertEquals(STRING_OBJECT_TYPE,
        STRING_OBJECT_TYPE.getGreatestSubtype(OBJECT_TYPE));
    assertEquals(STRING_OBJECT_TYPE,
        OBJECT_TYPE.getGreatestSubtype(STRING_OBJECT_TYPE));
    assertEquals(NO_OBJECT_TYPE, ARRAY_TYPE.getGreatestSubtype(DATE_TYPE));
    assertEquals(NO_OBJECT_TYPE, ARRAY_TYPE.getGreatestSubtype(REGEXP_TYPE));
    assertEquals(EVAL_ERROR_TYPE,
        ERROR_TYPE.getGreatestSubtype(EVAL_ERROR_TYPE));
    assertEquals(EVAL_ERROR_TYPE,
        EVAL_ERROR_TYPE.getGreatestSubtype(ERROR_TYPE));
    assertEquals(NO_TYPE,
        NULL_TYPE.getGreatestSubtype(ERROR_TYPE));
    assertEquals(UNKNOWN_TYPE,
        NUMBER_TYPE.getGreatestSubtype(UNKNOWN_TYPE));
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testGreatestSubtypeUnionTypes1
  public void testGreatestSubtypeUnionTypes1() {
    assertEquals(NULL_TYPE, createNullableType(STRING_TYPE).getGreatestSubtype(
            createNullableType(NUMBER_TYPE)));
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testGreatestSubtypeUnionTypes2
      public void testGreatestSubtypeUnionTypes2() {
    UnionType evalUriError =
        (UnionType) createUnionType(EVAL_ERROR_TYPE, URI_ERROR_TYPE);
    assertEquals(evalUriError,
        evalUriError.getGreatestSubtype(ERROR_TYPE));
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testGreatestSubtypeUnionTypes3
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
        registry.getNativeType(JSTypeNative.NO_TYPE));
    verifySubtypeChain(typeChain);
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testRecordSubtypeChain
  public void testRecordSubtypeChain() throws Exception {
    RecordTypeBuilder builder = new RecordTypeBuilder(registry);
    builder.addProperty("a", STRING_TYPE);
    JSType aType = builder.build();

    builder = new RecordTypeBuilder(registry);
    builder.addProperty("a", STRING_TYPE);
    builder.addProperty("b", STRING_TYPE);
    JSType abType = builder.build();

    builder = new RecordTypeBuilder(registry);
    builder.addProperty("a", STRING_TYPE);
    builder.addProperty("b", STRING_TYPE);
    builder.addProperty("c", NUMBER_TYPE);
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
    builder.addProperty("date", DATE_TYPE);
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
    builder.addProperty("date", UNKNOWN_TYPE);
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
        dateMethod,
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

// com.google.javascript.rhino.jstype.JSTypeTest::testSpecialUnionCanAssignTo
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

// com.google.javascript.rhino.jstype.JSTypeTest::testRestrictedTypeGivenToBoolean
      public void testRestrictedTypeGivenToBoolean() {
    
    assertEquals(BOOLEAN_TYPE,
        BOOLEAN_TYPE.getRestrictedTypeGivenToBooleanOutcome(true));
    assertEquals(BOOLEAN_TYPE,
        BOOLEAN_TYPE.getRestrictedTypeGivenToBooleanOutcome(false));

    assertEquals(NO_TYPE,
        NULL_TYPE.getRestrictedTypeGivenToBooleanOutcome(true));
    assertEquals(NULL_TYPE,
        NULL_TYPE.getRestrictedTypeGivenToBooleanOutcome(false));

    assertEquals(NUMBER_TYPE,
        NUMBER_TYPE.getRestrictedTypeGivenToBooleanOutcome(true));
    assertEquals(NUMBER_TYPE,
        NUMBER_TYPE.getRestrictedTypeGivenToBooleanOutcome(false));

    assertEquals(STRING_TYPE,
        STRING_TYPE.getRestrictedTypeGivenToBooleanOutcome(true));
    assertEquals(STRING_TYPE,
        STRING_TYPE.getRestrictedTypeGivenToBooleanOutcome(false));

    assertEquals(STRING_OBJECT_TYPE,
        STRING_OBJECT_TYPE.getRestrictedTypeGivenToBooleanOutcome(true));
    assertEquals(NO_TYPE,
        STRING_OBJECT_TYPE.getRestrictedTypeGivenToBooleanOutcome(false));

    assertEquals(NO_TYPE,
        VOID_TYPE.getRestrictedTypeGivenToBooleanOutcome(true));
    assertEquals(VOID_TYPE,
        VOID_TYPE.getRestrictedTypeGivenToBooleanOutcome(false));

    assertEquals(NO_OBJECT_TYPE,
        NO_OBJECT_TYPE.getRestrictedTypeGivenToBooleanOutcome(true));
    assertEquals(NO_TYPE,
        NO_OBJECT_TYPE.getRestrictedTypeGivenToBooleanOutcome(false));

    assertEquals(NO_TYPE,
        NO_TYPE.getRestrictedTypeGivenToBooleanOutcome(true));
    assertEquals(NO_TYPE,
        NO_TYPE.getRestrictedTypeGivenToBooleanOutcome(false));

    assertEquals(ALL_TYPE,
        ALL_TYPE.getRestrictedTypeGivenToBooleanOutcome(true));
    assertEquals(ALL_TYPE,
        ALL_TYPE.getRestrictedTypeGivenToBooleanOutcome(false));

    assertEquals(UNKNOWN_TYPE,
        UNKNOWN_TYPE.getRestrictedTypeGivenToBooleanOutcome(true));
    assertEquals(UNKNOWN_TYPE,
        UNKNOWN_TYPE.getRestrictedTypeGivenToBooleanOutcome(false));

    
    UnionType nullableStringValue =
        (UnionType) createNullableType(STRING_TYPE);
    assertEquals(STRING_TYPE,
        nullableStringValue.getRestrictedTypeGivenToBooleanOutcome(true));
    assertEquals(nullableStringValue,
        nullableStringValue.getRestrictedTypeGivenToBooleanOutcome(false));

    UnionType nullableStringObject =
        (UnionType) createNullableType(STRING_OBJECT_TYPE);
    assertEquals(STRING_OBJECT_TYPE,
        nullableStringObject.getRestrictedTypeGivenToBooleanOutcome(true));
    assertEquals(NULL_TYPE,
        nullableStringObject.getRestrictedTypeGivenToBooleanOutcome(false));
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testRegisterProperty
  public void testRegisterProperty() {
    int i = 0;
    Set<JSType> allObjects = Sets.newHashSet();
    for (JSType type : types) {
      String propName = "ALF" + i++;
      if (type instanceof ObjectType) {
        ObjectType objType = (ObjectType) type;
        objType.defineDeclaredProperty(propName, UNKNOWN_TYPE, false);
        objType.defineDeclaredProperty("allHaz", UNKNOWN_TYPE, false);
        assertEquals(type,
            registry.getGreatestSubtypeWithProperty(type, propName));
        assertEquals(Sets.newHashSet(type),
            registry.getTypesWithProperty(propName));
        assertEquals(NO_TYPE,
            registry.getGreatestSubtypeWithProperty(type, "GRRR"));
        allObjects.add(type);
      }
    }
    assertEquals(registry.getTypesWithProperty("GRRR"),
        Sets.newHashSet(NO_TYPE));
    assertEquals(allObjects, registry.getTypesWithProperty("allHaz"));
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testRegisterPropertyMemoization
  public void testRegisterPropertyMemoization() {
    ObjectType derived1 = registry.createObjectType("d1", null, namedGoogBar);
    ObjectType derived2 = registry.createObjectType("d2", null, namedGoogBar);

    derived1.defineDeclaredProperty("propz", UNKNOWN_TYPE, false);

    assertEquals(derived1,
        registry.getGreatestSubtypeWithProperty(derived1, "propz"));
    assertEquals(NO_OBJECT_TYPE,
        registry.getGreatestSubtypeWithProperty(derived2, "propz"));

    derived2.defineDeclaredProperty("propz", UNKNOWN_TYPE, false);

    assertEquals(derived1,
        registry.getGreatestSubtypeWithProperty(derived1, "propz"));
    assertEquals(derived2,
        registry.getGreatestSubtypeWithProperty(derived2, "propz"));
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
    Map<String, JSType> properties = new HashMap<String, JSType>();
    properties.put("hello", NUMBER_TYPE);

    JSType recordType = registry.createRecordType(properties);
    assertEquals("{ hello : number }", recordType.toString());
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testCreateUnionType
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
    assertEquals(OBJECT_TYPE, anonymous.getImplicitPrototype());
    assertEquals("{...}", anonymous.getReferenceName());
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testCreateObjectType
  public void testCreateObjectType() throws Exception {
    
    ObjectType subDate =
        registry.createObjectType(DATE_TYPE.getImplicitPrototype());
    assertEquals(DATE_TYPE.getImplicitPrototype(),
        subDate.getImplicitPrototype());
    assertEquals("{...}", subDate.getReferenceName());

    
    ObjectType subError = registry.createObjectType("Foo", null,
        ERROR_TYPE.getImplicitPrototype());
    assertEquals(ERROR_TYPE.getImplicitPrototype(),
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

// com.google.javascript.rhino.jstype.JSTypeTest::testUnionWithUnknown
  public void testUnionWithUnknown() throws Exception {
    assertTrue(createUnionType(UNKNOWN_TYPE, NULL_TYPE).isUnknownType());
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testHasOwnProperty
  public void testHasOwnProperty() throws Exception {
    ObjectType sup =
        registry.createObjectType(registry.createAnonymousObjectType());
    ObjectType sub = registry.createObjectType(sup);

    sup.defineProperty("base", null, false, false);
    sub.defineProperty("sub", null, false, false);

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
        false);
    namedGoogBar.defineProperty("sub", null, false, false);

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

    sup.defineProperty("base", null, false, false);
    sub.defineProperty("sub", null, false, false);

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
    assertEquals(UNKNOWN_TYPE, namedGoogBar.getPropertyType("X"));
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

    sup.defineProperty("X", NUMBER_TYPE, false, false);
    sup.setPropertyJSDocInfo("X", privateInfo, false);

    sub.setPropertyJSDocInfo("X", deprecated, false);

    assertFalse(sup.getOwnPropertyJSDocInfo("X").isDeprecated());
    assertEquals(Visibility.PRIVATE,
        sup.getOwnPropertyJSDocInfo("X").getVisibility());
    assertEquals(NUMBER_TYPE, sup.getPropertyType("X"));
    assertTrue(sub.getOwnPropertyJSDocInfo("X").isDeprecated());
    assertNull(sub.getOwnPropertyJSDocInfo("X").getVisibility());
    assertEquals(NUMBER_TYPE, sub.getPropertyType("X"));
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
    assertTrue(OBJECT_FUNCTION_TYPE.getSubTypes().contains(googBar));
    assertTrue(googBar.getSubTypes().contains(googSubBar));
    assertFalse(googBar.getSubTypes().contains(googSubSubBar));
    assertFalse(googSubBar.getSubTypes().contains(googSubBar));
    assertTrue(googSubBar.getSubTypes().contains(googSubSubBar));
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testImplementingType
  public void testImplementingType() throws Exception {
    assertTrue(registry.getDirectImplementors(
        interfaceType.getInstanceType()).contains(googBar));
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

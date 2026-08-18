// buggy code
  private static Type getActualType(
      Type typeToEvaluate, Type parentType, Class<?> rawParentClass) {
    if (typeToEvaluate instanceof Class<?>) {
      return typeToEvaluate;
    } else if (typeToEvaluate instanceof ParameterizedType) {
      ParameterizedType castedType = (ParameterizedType) typeToEvaluate;
      Type owner = castedType.getOwnerType();
      Type[] actualTypeParameters =
          extractRealTypes(castedType.getActualTypeArguments(), parentType, rawParentClass);
      Type rawType = castedType.getRawType();
      return new ParameterizedTypeImpl(rawType, actualTypeParameters, owner);
    } else if (typeToEvaluate instanceof GenericArrayType) {
      GenericArrayType castedType = (GenericArrayType) typeToEvaluate;
      Type componentType = castedType.getGenericComponentType();
      Type actualType = getActualType(componentType, parentType, rawParentClass);
      if (componentType.equals(actualType)) {
        return castedType;
      }
      return actualType instanceof Class<?> ?
          TypeUtils.wrapWithArray(TypeUtils.toRawClass(actualType))
          : new GenericArrayTypeImpl(actualType);
    } else if (typeToEvaluate instanceof TypeVariable<?>) {
      if (parentType instanceof ParameterizedType) {
        // The class definition has the actual types used for the type variables.
        // Find the matching actual type for the Type Variable used for the field.
        // For example, class Foo<A> { A a; }
        // new Foo<Integer>(); defines the actual type of A to be Integer.
        // So, to find the type of the field a, we will have to look at the class'
        // actual type arguments.
        TypeVariable<?> fieldTypeVariable = (TypeVariable<?>) typeToEvaluate;
        TypeVariable<?>[] classTypeVariables = rawParentClass.getTypeParameters();
        ParameterizedType objParameterizedType = (ParameterizedType) parentType;
        int indexOfActualTypeArgument = getIndex(classTypeVariables, fieldTypeVariable);
        Type[] actualTypeArguments = objParameterizedType.getActualTypeArguments();
        return actualTypeArguments[indexOfActualTypeArgument];


      }

      throw new UnsupportedOperationException("Expecting parameterized type, got " + parentType
          + ".\n Are you missing the use of TypeToken idiom?\n See "
          + "http://sites.google.com/site/gson/gson-user-guide#TOC-Serializing-and-Deserializing-Gener");
    } else if (typeToEvaluate instanceof WildcardType) {
      WildcardType castedType = (WildcardType) typeToEvaluate;
      return getActualType(castedType.getUpperBounds()[0], parentType, rawParentClass);
    } else {
      throw new IllegalArgumentException("Type \'" + typeToEvaluate + "\' is not a Class, "
          + "ParameterizedType, GenericArrayType or TypeVariable. Can't extract type.");
    }
  }

// relevant test
// com.google.gson.JsonParserTest::testParseString
  public void testParseString() {
    String json = "{a:10,b:'c'}";
    JsonElement e = parser.parse(json);
    assertTrue(e.isJsonObject());
    assertEquals(10, e.getAsJsonObject().get("a").getAsInt());
    assertEquals("c", e.getAsJsonObject().get("b").getAsString());
  }

// com.google.gson.JsonParserTest::testParseEmptyString
  public void testParseEmptyString() {
    JsonElement e = parser.parse("\"   \"");
    assertTrue(e.isJsonPrimitive());
    assertEquals("   ", e.getAsString());
  }

// com.google.gson.JsonParserTest::testParseEmptyWhitespaceInput
  public void testParseEmptyWhitespaceInput() {
    JsonElement e = parser.parse("     ");
    assertTrue(e.isJsonNull());
  }

// com.google.gson.JsonParserTest::testParseMixedArray
  public void testParseMixedArray() {
    String json = "[{},13,\"stringValue\"]";
    JsonElement e = parser.parse(json);
    assertTrue(e.isJsonArray());
    
    JsonArray  array = e.getAsJsonArray();
    assertEquals("{}", array.get(0).toString());
    assertEquals(13, array.get(1).getAsInt());
    assertEquals("stringValue", array.get(2).getAsString());
  }

// com.google.gson.JsonParserTest::testParseReader
  public void testParseReader() {
    StringReader reader = new StringReader("{a:10,b:'c'}");
    JsonElement e = parser.parse(reader);
    assertTrue(e.isJsonObject());
    assertEquals(10, e.getAsJsonObject().get("a").getAsInt());
    assertEquals("c", e.getAsJsonObject().get("b").getAsString());
  }

// com.google.gson.JsonParserTest::testReadWriteTwoObjects
  public void testReadWriteTwoObjects() throws Exception {
    Gson gson= new Gson();
    CharArrayWriter writer= new CharArrayWriter();
    BagOfPrimitives expectedOne = new BagOfPrimitives(1, 1, true, "one");
    writer.write(gson.toJson(expectedOne).toCharArray());
    BagOfPrimitives expectedTwo = new BagOfPrimitives(2, 2, false, "two");
    writer.write(gson.toJson(expectedTwo).toCharArray());
    CharArrayReader reader = new CharArrayReader(writer.toCharArray());
  
    JsonReader parser = new JsonReader(reader);
    parser.setLenient(true);
    JsonElement element1 = Streams.parse(parser);
    JsonElement element2 = Streams.parse(parser);
    BagOfPrimitives actualOne = gson.fromJson(element1, BagOfPrimitives.class);
    assertEquals("one", actualOne.stringValue);
    BagOfPrimitives actualTwo = gson.fromJson(element2, BagOfPrimitives.class);
    assertEquals("two", actualTwo.stringValue);
  }

// com.google.gson.MixedStreamTest::testWriteMixedStreamed
  public void testWriteMixedStreamed() throws IOException {
    Gson gson = new Gson();
    StringWriter stringWriter = new StringWriter();
    JsonWriter jsonWriter = new JsonWriter(stringWriter);

    jsonWriter.beginArray();
    jsonWriter.setIndent("  ");
    gson.toJson(BLUE_MUSTANG, Car.class, jsonWriter);
    gson.toJson(BLACK_BMW, Car.class, jsonWriter);
    gson.toJson(RED_MIATA, Car.class, jsonWriter);
    jsonWriter.endArray();

    assertEquals(CARS_JSON, stringWriter.toString());
  }

// com.google.gson.MixedStreamTest::testReadMixedStreamed
  public void testReadMixedStreamed() throws IOException {
    Gson gson = new Gson();
    StringReader stringReader = new StringReader(CARS_JSON);
    JsonReader jsonReader = new JsonReader(stringReader);

    jsonReader.beginArray();
    assertEquals(BLUE_MUSTANG, gson.fromJson(jsonReader, Car.class));
    assertEquals(BLACK_BMW, gson.fromJson(jsonReader, Car.class));
    assertEquals(RED_MIATA, gson.fromJson(jsonReader, Car.class));
    jsonReader.endArray();
  }

// com.google.gson.MixedStreamTest::testReaderDoesNotMutateState
  public void testReaderDoesNotMutateState() throws IOException {
    Gson gson = new Gson();
    JsonReader jsonReader = new JsonReader(new StringReader(CARS_JSON));
    jsonReader.beginArray();

    jsonReader.setLenient(false);
    gson.fromJson(jsonReader, Car.class);
    assertFalse(jsonReader.isLenient());

    jsonReader.setLenient(true);
    gson.fromJson(jsonReader, Car.class);
    assertTrue(jsonReader.isLenient());
  }

// com.google.gson.MixedStreamTest::testWriteDoesNotMutateState
  public void testWriteDoesNotMutateState() throws IOException {
    Gson gson = new Gson();
    JsonWriter jsonWriter = new JsonWriter(new StringWriter());
    jsonWriter.beginArray();

    jsonWriter.setHtmlSafe(true);
    jsonWriter.setLenient(true);
    gson.toJson(BLUE_MUSTANG, Car.class, jsonWriter);
    assertTrue(jsonWriter.isHtmlSafe());
    assertTrue(jsonWriter.isLenient());

    jsonWriter.setHtmlSafe(false);
    jsonWriter.setLenient(false);
    gson.toJson(BLUE_MUSTANG, Car.class, jsonWriter);
    assertFalse(jsonWriter.isHtmlSafe());
    assertFalse(jsonWriter.isLenient());
  }

// com.google.gson.MixedStreamTest::testReadInvalidState
  public void testReadInvalidState() throws IOException {
    Gson gson = new Gson();
    JsonReader jsonReader = new JsonReader(new StringReader(CARS_JSON));
    jsonReader.beginArray();
    jsonReader.beginObject();
    try {
      gson.fromJson(jsonReader, String.class);
      fail();
    } catch (IllegalArgumentException expected) {
    }
  }

// com.google.gson.MixedStreamTest::testReadClosed
  public void testReadClosed() throws IOException {
    Gson gson = new Gson();
    JsonReader jsonReader = new JsonReader(new StringReader(CARS_JSON));
    jsonReader.close();
    try {
      gson.fromJson(jsonReader, new TypeToken<List<Car>>() {}.getType());
      fail();
    } catch (IllegalStateException expected) {
    }
  }

// com.google.gson.MixedStreamTest::testWriteInvalidState
  public void testWriteInvalidState() throws IOException {
    Gson gson = new Gson();
    JsonWriter jsonWriter = new JsonWriter(new StringWriter());
    jsonWriter.beginObject();
    try {
      gson.toJson(BLUE_MUSTANG, Car.class, jsonWriter);
      fail();
    } catch (IllegalStateException expected) {
    }
  }

// com.google.gson.MixedStreamTest::testWriteClosed
  public void testWriteClosed() throws IOException {
    Gson gson = new Gson();
    JsonWriter jsonWriter = new JsonWriter(new StringWriter());
    jsonWriter.beginArray();
    jsonWriter.endArray();
    jsonWriter.close();
    try {
      gson.toJson(BLUE_MUSTANG, Car.class, jsonWriter);
      fail();
    } catch (IllegalStateException expected) {
    }
  }

// com.google.gson.MixedStreamTest::testWriteNulls
  public void testWriteNulls() {
    Gson gson = new Gson();
    try {
      gson.toJson(new JsonPrimitive("hello"), (JsonWriter) null);
      fail();
    } catch (NullPointerException expected) {
    }

    StringWriter stringWriter = new StringWriter();
    gson.toJson(null, new JsonWriter(stringWriter));
    assertEquals("", stringWriter.toString());
  }

// com.google.gson.MixedStreamTest::testReadNulls
  public void testReadNulls() {
    Gson gson = new Gson();
    try {
      gson.fromJson((JsonReader) null, Integer.class);
      fail();
    } catch (NullPointerException expected) {
    }
    try {
      gson.fromJson(new JsonReader(new StringReader("true")), null);
      fail();
    } catch (IllegalArgumentException expected) {
    }
  }

// com.google.gson.MixedStreamTest::testWriteHtmlSafe
  public void testWriteHtmlSafe() {
    List<String> contents = Arrays.asList("<", ">", "&", "=", "'");
    Type type = new TypeToken<List<String>>() {}.getType();

    StringWriter writer = new StringWriter();
    new Gson().toJson(contents, type, new JsonWriter(writer));
    assertEquals("[\"\\u003c\",\"\\u003e\",\"\\u0026\",\"\\u003d\",\"\\u0027\"]",
        writer.toString());

    writer = new StringWriter();
    new GsonBuilder().disableHtmlEscaping().create()
        .toJson(contents, type, new JsonWriter(writer));
    assertEquals("[\"<\",\">\",\"&\",\"=\",\"'\"]",
        writer.toString());
  }

// com.google.gson.MixedStreamTest::testWriteLenient
  public void testWriteLenient() {
    List<Double> doubles = Arrays.asList(Double.NaN, Double.NEGATIVE_INFINITY,
        Double.POSITIVE_INFINITY, -0.0d, 0.5d, 0.0d);
    Type type = new TypeToken<List<Double>>() {}.getType();

    StringWriter writer = new StringWriter();
    JsonWriter jsonWriter = new JsonWriter(writer);
    new GsonBuilder().serializeSpecialFloatingPointValues().create()
        .toJson(doubles, type, jsonWriter);
    assertEquals("[NaN,-Infinity,Infinity,-0.0,0.5,0.0]", writer.toString());

    try {
      new Gson().toJson(doubles, type, new JsonWriter(new StringWriter()));
      fail();
    } catch (IllegalArgumentException expected) {
    }
  }

// com.google.gson.TypeInfoFactoryTest::testSimpleField
  public void testSimpleField() throws Exception {
    Field f = obj.getClass().getField("simpleField");
    TypeInfo typeInfo = TypeInfoFactory.getTypeInfoForField(f, OBJ_TYPE);

    assertFalse(typeInfo.isArray());
    assertFalse(typeInfo.isEnum());
    assertEquals(String.class, typeInfo.getActualType());
    assertEquals(String.class, typeInfo.getRawClass());
  }

// com.google.gson.TypeInfoFactoryTest::testEnumField
  public void testEnumField() throws Exception {
    Field f = obj.getClass().getField("enumField");
    TypeInfo typeInfo = TypeInfoFactory.getTypeInfoForField(f, OBJ_TYPE);

    assertFalse(typeInfo.isArray());
    assertTrue(typeInfo.isEnum());
    assertEquals(ObjectWithDifferentFields.TestEnum.class, typeInfo.getActualType());
    assertEquals(ObjectWithDifferentFields.TestEnum.class, typeInfo.getRawClass());
  }

// com.google.gson.TypeInfoFactoryTest::testParameterizedTypeField
  public void testParameterizedTypeField() throws Exception {
    Type listType = new TypeToken<List<String>>() {}.getType();
    Field f = obj.getClass().getField("simpleParameterizedType");
    TypeInfo typeInfo = TypeInfoFactory.getTypeInfoForField(f, OBJ_TYPE);

    assertFalse(typeInfo.isArray());
    assertFalse(typeInfo.isEnum());
    assertEquals(listType, typeInfo.getActualType());
    assertEquals(List.class, typeInfo.getRawClass());
  }

// com.google.gson.TypeInfoFactoryTest::testNestedParameterizedTypeField
  public void testNestedParameterizedTypeField() throws Exception {
    Type listType = new TypeToken<List<List<String>>>() {}.getType();
    Field f = obj.getClass().getField("simpleNestedParameterizedType");
    TypeInfo typeInfo = TypeInfoFactory.getTypeInfoForField(f, OBJ_TYPE);

    assertFalse(typeInfo.isArray());
    assertFalse(typeInfo.isEnum());
    assertEquals(listType, typeInfo.getActualType());
    assertEquals(List.class, typeInfo.getRawClass());
  }

// com.google.gson.TypeInfoFactoryTest::testGenericArrayTypeField
  public void testGenericArrayTypeField() throws Exception {
    Type listType = new TypeToken<List<String>[]>() {}.getType();
    Field f = obj.getClass().getField("simpleGenericArray");
    TypeInfo typeInfo = TypeInfoFactory.getTypeInfoForField(f, OBJ_TYPE);

    assertTrue(typeInfo.isArray());
    assertFalse(typeInfo.isEnum());
    assertEquals(listType, typeInfo.getActualType());
    assertEquals(List[].class, typeInfo.getRawClass());
  }

// com.google.gson.TypeInfoFactoryTest::testTypeVariableField
  public void testTypeVariableField() throws Exception {
    Field f = obj.getClass().getField("typeVariableObj");
    TypeInfo typeInfo = TypeInfoFactory.getTypeInfoForField(f, OBJ_TYPE);

    assertFalse(typeInfo.isArray());
    assertFalse(typeInfo.isEnum());
    assertEquals(Integer.class, typeInfo.getActualType());
    assertEquals(Integer.class, typeInfo.getRawClass());
  }

// com.google.gson.TypeInfoFactoryTest::testTypeVariableArrayField
  public void testTypeVariableArrayField() throws Exception {
    Field f = obj.getClass().getField("typeVariableArray");
    TypeInfo typeInfo = TypeInfoFactory.getTypeInfoForField(f, OBJ_TYPE);

    assertTrue(typeInfo.isArray());
    assertFalse(typeInfo.isEnum());
    assertEquals(Integer[].class, typeInfo.getActualType());
    assertEquals(Integer[].class, typeInfo.getRawClass());
  }

// com.google.gson.TypeInfoFactoryTest::testMutliDimensionalTypeVariableArrayField
  public void testMutliDimensionalTypeVariableArrayField() throws Exception {
    Field f = obj.getClass().getField("mutliDimensionalTypeVariableArray");
    TypeInfo typeInfo = TypeInfoFactory.getTypeInfoForField(f, OBJ_TYPE);

    assertTrue(typeInfo.isArray());
    assertFalse(typeInfo.isEnum());
    assertEquals(Integer[][][].class, typeInfo.getActualType());
    assertEquals(Integer[][][].class, typeInfo.getRawClass());
  }

// com.google.gson.TypeInfoFactoryTest::testParameterizedTypeVariableField
  public void testParameterizedTypeVariableField() throws Exception {
    Type listType = new TypeToken<List<Integer>>() {}.getType();
    Field f = obj.getClass().getField("listOfTypeVariables");
    TypeInfo typeInfo = TypeInfoFactory.getTypeInfoForField(f, OBJ_TYPE);

    assertFalse(typeInfo.isArray());
    assertFalse(typeInfo.isEnum());
    assertEquals(listType, typeInfo.getActualType());
    assertEquals(List.class, typeInfo.getRawClass());
  }

// com.google.gson.TypeInfoFactoryTest::testNestedParameterizedTypeVariableField
  public void testNestedParameterizedTypeVariableField() throws Exception {
    Type listType = new TypeToken<List<List<Integer>>>() {}.getType();
    Field f = obj.getClass().getField("listOfListsOfTypeVariables");
    TypeInfo typeInfo = TypeInfoFactory.getTypeInfoForField(f, OBJ_TYPE);

    assertFalse(typeInfo.isArray());
    assertFalse(typeInfo.isEnum());
    assertEquals(listType, typeInfo.getActualType());
    assertEquals(List.class, typeInfo.getRawClass());
  }

// com.google.gson.TypeInfoFactoryTest::testParameterizedTypeVariableArrayField
  public void testParameterizedTypeVariableArrayField() throws Exception {
    Type listType = new TypeToken<List<Integer>[]>() {}.getType();
    Field f = obj.getClass().getField("listOfTypeVariablesArray");
    TypeInfo typeInfo = TypeInfoFactory.getTypeInfoForField(f, OBJ_TYPE);

    assertTrue(typeInfo.isArray());
    assertFalse(typeInfo.isEnum());
    assertEquals(listType, typeInfo.getActualType());
    assertEquals(List[].class, typeInfo.getRawClass());
  }

// com.google.gson.TypeInfoFactoryTest::testWildcardField
  public void testWildcardField() throws Exception {
    Type listType = new TypeToken<List<Object>>() {}.getType();
    Field f = obj.getClass().getField("listWithWildcard");
    TypeInfo typeInfo = TypeInfoFactory.getTypeInfoForField(f, OBJ_TYPE);

    assertFalse(typeInfo.isArray());
    assertFalse(typeInfo.isEnum());
    assertEquals(listType, typeInfo.getActualType());
    assertEquals(List.class, typeInfo.getRawClass());
  }

// com.google.gson.TypeInfoFactoryTest::testArrayOfWildcardField
  public void testArrayOfWildcardField() throws Exception {
    Type listType = new TypeToken<List<Object>[]>() {}.getType();
    Field f = obj.getClass().getField("arrayOfListWithWildcard");
    TypeInfo typeInfo = TypeInfoFactory.getTypeInfoForField(f, OBJ_TYPE);

    assertTrue(typeInfo.isArray());
    assertFalse(typeInfo.isEnum());
    assertEquals(listType, typeInfo.getActualType());
    assertEquals(List[].class, typeInfo.getRawClass());
  }

// com.google.gson.TypeInfoFactoryTest::testListStringWildcardField
  public void testListStringWildcardField() throws Exception {
    Type listType = new TypeToken<List<String>>() {}.getType();
    Field f = obj.getClass().getField("listWithStringWildcard");
    TypeInfo typeInfo = TypeInfoFactory.getTypeInfoForField(f, OBJ_TYPE);

    assertFalse(typeInfo.isArray());
    assertFalse(typeInfo.isEnum());
    assertEquals(listType, typeInfo.getActualType());
    assertEquals(List.class, typeInfo.getRawClass());
  }

// com.google.gson.TypeInfoFactoryTest::testArrayOfListStringWildcardField
  public void testArrayOfListStringWildcardField() throws Exception {
    Type listType = new TypeToken<List<String>[]>() {}.getType();
    Field f = obj.getClass().getField("arrayOfListWithStringWildcard");
    TypeInfo typeInfo = TypeInfoFactory.getTypeInfoForField(f, OBJ_TYPE);

    assertTrue(typeInfo.isArray());
    assertFalse(typeInfo.isEnum());
    assertEquals(listType, typeInfo.getActualType());
    assertEquals(List[].class, typeInfo.getRawClass());
  }

// com.google.gson.TypeInfoFactoryTest::testListTypeVariableWildcardField
  public void testListTypeVariableWildcardField() throws Exception {
    Type listType = new TypeToken<List<Integer>>() {}.getType();
    Field f = obj.getClass().getField("listWithTypeVariableWildcard");
    TypeInfo typeInfo = TypeInfoFactory.getTypeInfoForField(f, OBJ_TYPE);

    assertFalse(typeInfo.isArray());
    assertFalse(typeInfo.isEnum());
    assertEquals(listType, typeInfo.getActualType());
    assertEquals(List.class, typeInfo.getRawClass());
  }

// com.google.gson.TypeInfoFactoryTest::testArrayOfListTypeVariableWildcardField
  public void testArrayOfListTypeVariableWildcardField() throws Exception {
    Type listType = new TypeToken<List<Integer>[]>() {}.getType();
    Field f = obj.getClass().getField("arrayOfListWithTypeVariableWildcard");
    TypeInfo typeInfo = TypeInfoFactory.getTypeInfoForField(f, OBJ_TYPE);

    assertTrue(typeInfo.isArray());
    assertFalse(typeInfo.isEnum());
    assertEquals(listType, typeInfo.getActualType());
    assertEquals(List[].class, typeInfo.getRawClass());
  }

// com.google.gson.functional.ArrayTest::testTopLevelArrayOfIntsSerialization
  public void testTopLevelArrayOfIntsSerialization() {
    int[] target = {1, 2, 3, 4, 5, 6, 7, 8, 9};
    assertEquals("[1,2,3,4,5,6,7,8,9]", gson.toJson(target));
  }

// com.google.gson.functional.ArrayTest::testTopLevelArrayOfIntsDeserialization
  public void testTopLevelArrayOfIntsDeserialization() {
    int[] expected = { 1, 2, 3, 4, 5, 6, 7, 8, 9 };
    int[] actual = gson.fromJson("[1,2,3,4,5,6,7,8,9]", int[].class);
    MoreAsserts.assertEquals(expected, actual);
  }

// com.google.gson.functional.ArrayTest::testInvalidArrayDeserialization
  public void testInvalidArrayDeserialization() {
    String json = "[1, 2 3, 4, 5]";
    try {
      gson.fromJson(json, int[].class);
      fail("Gson should not deserialize array elements with missing ,");
    } catch (JsonParseException expected) {
    }
  }

// com.google.gson.functional.ArrayTest::testEmptyArraySerialization
  public void testEmptyArraySerialization() {
    int[] target = {};
    assertEquals("[]", gson.toJson(target));
  }

// com.google.gson.functional.ArrayTest::testEmptyArrayDeserialization
  public void testEmptyArrayDeserialization() {
    int[] actualObject = gson.fromJson("[]", int[].class);
    assertTrue(actualObject.length == 0);

    Integer[] actualObject2 = gson.fromJson("[]", Integer[].class);
    assertTrue(actualObject2.length == 0);

    actualObject = gson.fromJson("[ ]", int[].class);
    assertTrue(actualObject.length == 0);
  }

// com.google.gson.functional.ArrayTest::testNullsInArraySerialization
  public void testNullsInArraySerialization() {
    String[] array = {"foo", null, "bar"};
    String expected = "[\"foo\",null,\"bar\"]";
    String json = gson.toJson(array);
    assertEquals(expected, json);
  }

// com.google.gson.functional.ArrayTest::testNullsInArrayDeserialization
  public void testNullsInArrayDeserialization() {
    String json = "[\"foo\",null,\"bar\"]";
    String[] expected = {"foo", null, "bar"};
    String[] target = gson.fromJson(json, expected.getClass());
    for (int i = 0; i < expected.length; ++i) {
      assertEquals(expected[i], target[i]);
    }
  }

// com.google.gson.functional.ArrayTest::testSingleNullInArraySerialization
  public void testSingleNullInArraySerialization() {
    BagOfPrimitives[] array = new BagOfPrimitives[1];
    array[0] = null;
    String json = gson.toJson(array);
    assertEquals("[null]", json);
  }

// com.google.gson.functional.ArrayTest::testSingleNullInArrayDeserialization
  public void testSingleNullInArrayDeserialization() {
    BagOfPrimitives[] array = gson.fromJson("[null]", BagOfPrimitives[].class);
    assertNull(array[0]);
  }

// com.google.gson.functional.ArrayTest::testNullsInArrayWithSerializeNullPropertySetSerialization
  public void testNullsInArrayWithSerializeNullPropertySetSerialization() {
    gson = new GsonBuilder().serializeNulls().create();
    String[] array = {"foo", null, "bar"};
    String expected = "[\"foo\",null,\"bar\"]";
    String json = gson.toJson(array);
    assertEquals(expected, json);
  }

// com.google.gson.functional.ArrayTest::testArrayOfStringsSerialization
  public void testArrayOfStringsSerialization() {
    String[] target = {"Hello", "World"};
    assertEquals("[\"Hello\",\"World\"]", gson.toJson(target));
  }

// com.google.gson.functional.ArrayTest::testArrayOfStringsDeserialization
  public void testArrayOfStringsDeserialization() {
    String json = "[\"Hello\",\"World\"]";
    String[] target = gson.fromJson(json, String[].class);
    assertEquals("Hello", target[0]);
    assertEquals("World", target[1]);
  }

// com.google.gson.functional.ArrayTest::testSingleStringArraySerialization
  public void testSingleStringArraySerialization() throws Exception {
    String[] s = { "hello" };
    String output = gson.toJson(s);
    assertEquals("[\"hello\"]", output);
  }

// com.google.gson.functional.ArrayTest::testSingleStringArrayDeserialization
  public void testSingleStringArrayDeserialization() throws Exception {
    String json = "[\"hello\"]";
    String[] arrayType = gson.fromJson(json, String[].class);
    assertEquals(1, arrayType.length);
    assertEquals("hello", arrayType[0]);

    String type = gson.fromJson(json, String.class);
    assertEquals("hello", type);
  }

// com.google.gson.functional.ArrayTest::testArrayOfCollectionSerialization
  public void testArrayOfCollectionSerialization() throws Exception {
    StringBuilder sb = new StringBuilder("[");
    int arraySize = 3;

    Type typeToSerialize = new TypeToken<Collection<Integer>[]>() {}.getType();
    Collection<Integer>[] arrayOfCollection = new ArrayList[arraySize];
    for (int i = 0; i < arraySize; ++i) {
      int startValue = (3 * i) + 1;
      sb.append('[').append(startValue).append(',').append(startValue + 1).append(']');
      ArrayList<Integer> tmpList = new ArrayList<Integer>();
      tmpList.add(startValue);
      tmpList.add(startValue + 1);
      arrayOfCollection[i] = tmpList;

      if (i < arraySize - 1) {
        sb.append(',');
      }
    }
    sb.append(']');

    String json = gson.toJson(arrayOfCollection, typeToSerialize);
    assertEquals(sb.toString(), json);
  }

// com.google.gson.functional.ArrayTest::testArrayOfCollectionDeserialization
  public void testArrayOfCollectionDeserialization() throws Exception {
    String json = "[[1,2],[3,4]]";
    Type type = new TypeToken<Collection<Integer>[]>() {}.getType();
    Collection<Integer>[] target = gson.fromJson(json, type);

    assertEquals(2, target.length);
    MoreAsserts.assertEquals(new Integer[] { 1, 2 }, target[0].toArray(new Integer[0]));
    MoreAsserts.assertEquals(new Integer[] { 3, 4 }, target[1].toArray(new Integer[0]));
  }

// com.google.gson.functional.ArrayTest::testArrayOfPrimitivesWithCustomTypeAdapter
  public void testArrayOfPrimitivesWithCustomTypeAdapter() throws Exception {
    CrazyLongTypeAdapter typeAdapter = new CrazyLongTypeAdapter();
    gson = new GsonBuilder()
        .registerTypeAdapter(long.class, typeAdapter)
        .registerTypeAdapter(Long.class, typeAdapter)
        .create();
    long[] value = { 1L };
    String serializedValue = gson.toJson(value);
    String expected = "[" + String.valueOf(value[0] + CrazyLongTypeAdapter.DIFFERENCE) + "]";
    assertEquals(expected, serializedValue);

    long[] deserializedValue = gson.fromJson(serializedValue, long[].class);
    assertEquals(1, deserializedValue.length);
    assertEquals(value[0], deserializedValue[0]);
  }

// com.google.gson.functional.ArrayTest::testArrayOfPrimitivesAsObjectsSerialization
  public void testArrayOfPrimitivesAsObjectsSerialization() throws Exception {
    Object[] objs = new Object[]{1, "abc", 0.3f, 5L};
    String json = gson.toJson(objs);
    assertTrue(json.contains("abc"));
    assertTrue(json.contains("0.3"));
    assertTrue(json.contains("5"));
  }

// com.google.gson.functional.ArrayTest::testArrayOfPrimitivesAsObjectsDeserialization
  public void testArrayOfPrimitivesAsObjectsDeserialization() throws Exception {
    String json = "[1,'abc',0.3,5]";
    Object[] objs = gson.fromJson(json, Object[].class);
    assertEquals(1, objs[0]);
    assertEquals("abc", objs[1]);
    assertEquals(new BigDecimal("0.3"), objs[2]);
    assertEquals(5, objs[3]);
  }

// com.google.gson.functional.ArrayTest::testArrayOfObjectsWithoutTypeInfoDeserialization
  public void testArrayOfObjectsWithoutTypeInfoDeserialization() throws Exception {
    String json = "[1,'abc',{a:1},5]";
    try {
      gson.fromJson(json, Object[].class);
      fail("This is crazy....how did we deserialize it!!!");
    } catch (JsonParseException expected) {
    }
  }

// com.google.gson.functional.ArrayTest::testArrayWithoutTypeInfoDeserialization
  public void testArrayWithoutTypeInfoDeserialization() throws Exception {
    String json = "[1,'abc',[1,2],5]";
    try {
      gson.fromJson(json, Object[].class);
      fail("This is crazy....how did we deserialize it!!!");
    } catch (JsonParseException expected) {
    }
  }

// com.google.gson.functional.ArrayTest::testObjectArrayWithNonPrimitivesSerialization
  public void testObjectArrayWithNonPrimitivesSerialization() throws Exception {
    ClassWithObjects classWithObjects = new ClassWithObjects();
    BagOfPrimitives bagOfPrimitives = new BagOfPrimitives();
    String classWithObjectsJson = gson.toJson(classWithObjects);
    String bagOfPrimitivesJson = gson.toJson(bagOfPrimitives);

    Object[] objects = new Object[] { classWithObjects, bagOfPrimitives };
    String json = gson.toJson(objects);

    assertTrue(json.contains(classWithObjectsJson));
    assertTrue(json.contains(bagOfPrimitivesJson));
  }

// com.google.gson.functional.CircularReferenceTest::testCircularSerialization
  public void testCircularSerialization() throws Exception {
    ContainsReferenceToSelfType a = new ContainsReferenceToSelfType();
    ContainsReferenceToSelfType b = new ContainsReferenceToSelfType();
    a.children.add(b);
    b.children.add(a);
    try {
      gson.toJson(a);
      fail("Circular types should not get printed!");
    } catch (IllegalStateException expected) { 
      assertTrue(expected.getMessage().contains("children"));      
    }
  }

// com.google.gson.functional.CircularReferenceTest::testSelfReferenceSerialization
  public void testSelfReferenceSerialization() throws Exception {
    ClassOverridingEquals objA = new ClassOverridingEquals();
    objA.ref = objA;

    try {
      gson.toJson(objA);
      fail("Circular reference to self can not be serialized!");
    } catch (IllegalStateException expected) { }
  }

// com.google.gson.functional.CircularReferenceTest::testSelfReferenceArrayFieldSerialization
  public void testSelfReferenceArrayFieldSerialization() throws Exception {
    ClassWithSelfReferenceArray objA = new ClassWithSelfReferenceArray();
    objA.children = new ClassWithSelfReferenceArray[]{objA};

    try {
      gson.toJson(objA);
      fail("Circular reference to self can not be serialized!");
    } catch (IllegalStateException expected) { 
      assertTrue(expected.getMessage().contains("children"));
    }
  }

// com.google.gson.functional.CircularReferenceTest::testSelfReferenceCustomHandlerSerialization
  public void testSelfReferenceCustomHandlerSerialization() throws Exception {
    ClassWithSelfReference obj = new ClassWithSelfReference();
    obj.child = obj;
    Gson gson = new GsonBuilder().registerTypeAdapter(ClassWithSelfReference.class, new JsonSerializer<ClassWithSelfReference>() {
      public JsonElement serialize(ClassWithSelfReference src, Type typeOfSrc,
          JsonSerializationContext context) {
        JsonObject obj = new JsonObject();
        obj.addProperty("property", "value");
        obj.add("child", context.serialize(src.child));
        return obj;
      }      
    }).create();
    try {
      gson.toJson(obj);
      fail("Circular reference to self can not be serialized!");
    } catch (IllegalStateException expected) { 
      assertTrue(expected.getMessage().contains("Offending"));
    }
  }

// com.google.gson.functional.CircularReferenceTest::testDirectedAcyclicGraphSerialization
  public void testDirectedAcyclicGraphSerialization() throws Exception {
    ContainsReferenceToSelfType a = new ContainsReferenceToSelfType();
    ContainsReferenceToSelfType b = new ContainsReferenceToSelfType();
    ContainsReferenceToSelfType c = new ContainsReferenceToSelfType();
    a.children.add(b);
    a.children.add(c);
    b.children.add(c);
    assertNotNull(gson.toJson(a));
  }

// com.google.gson.functional.CircularReferenceTest::testDirectedAcyclicGraphDeserialization
  public void testDirectedAcyclicGraphDeserialization() throws Exception {
    String json = "{\"children\":[{\"children\":[{\"children\":[]}]},{\"children\":[]}]}";
    ContainsReferenceToSelfType target = gson.fromJson(json, ContainsReferenceToSelfType.class);
    assertNotNull(target);
    assertEquals(2, target.children.size());
  }

// com.google.gson.functional.CollectionTest::testTopLevelCollectionOfIntegersSerialization
  public void testTopLevelCollectionOfIntegersSerialization() {
    Collection<Integer> target = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9);
    Type targetType = new TypeToken<Collection<Integer>>() {}.getType();
    String json = gson.toJson(target, targetType);
    assertEquals("[1,2,3,4,5,6,7,8,9]", json);
  }

// com.google.gson.functional.CollectionTest::testTopLevelCollectionOfIntegersDeserialization
  public void testTopLevelCollectionOfIntegersDeserialization() {
    String json = "[0,1,2,3,4,5,6,7,8,9]";
    Type collectionType = new TypeToken<Collection<Integer>>() { }.getType();
    Collection<Integer> target = gson.fromJson(json, collectionType);
    int[] expected = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
    MoreAsserts.assertEquals(expected, toIntArray(target));
  }

// com.google.gson.functional.CollectionTest::testTopLevelListOfIntegerCollectionsDeserialization
  public void testTopLevelListOfIntegerCollectionsDeserialization() throws Exception {
    String json = "[[1,2,3],[4,5,6],[7,8,9]]";
    Type collectionType = new TypeToken<Collection<Collection<Integer>>>() {}.getType();
    List<Collection<Integer>> target = gson.fromJson(json, collectionType);
    int[][] expected = new int[3][3];
    for (int i = 0; i < 3; ++i) {
      int start = (3 * i) + 1;
      for (int j = 0; j < 3; ++j) {
        expected[i][j] = start + j;
      }
    }

    for (int i = 0; i < 3; i++) {
      MoreAsserts.assertEquals(expected[i], toIntArray(target.get(i)));
    }
  }

// com.google.gson.functional.CollectionTest::testLinkedListSerialization
  public void testLinkedListSerialization() {
    List<String> list = new LinkedList<String>();
    list.add("a1");
    list.add("a2");
    Type linkedListType = new TypeToken<LinkedList<String>>() {}.getType();
    String json = gson.toJson(list, linkedListType);
    assertTrue(json.contains("a1"));
    assertTrue(json.contains("a2"));
  }

// com.google.gson.functional.CollectionTest::testLinkedListDeserialization
  public void testLinkedListDeserialization() {
    String json = "['a1','a2']";
    Type linkedListType = new TypeToken<LinkedList<String>>() {}.getType();
    List<String> list = gson.fromJson(json, linkedListType);
    assertEquals("a1", list.get(0));
    assertEquals("a2", list.get(1));
  }

// com.google.gson.functional.CollectionTest::testQueueSerialization
  public void testQueueSerialization() {
    Queue<String> queue = new LinkedList<String>();
    queue.add("a1");
    queue.add("a2");
    Type queueType = new TypeToken<Queue<String>>() {}.getType();
    String json = gson.toJson(queue, queueType);
    assertTrue(json.contains("a1"));
    assertTrue(json.contains("a2"));
  }

// com.google.gson.functional.CollectionTest::testQueueDeserialization
  public void testQueueDeserialization() {
    String json = "['a1','a2']";
    Type queueType = new TypeToken<Queue<String>>() {}.getType();
    Queue<String> queue = gson.fromJson(json, queueType);
    assertEquals("a1", queue.element()); 
    queue.remove();
    assertEquals("a2", queue.element());
  }

// com.google.gson.functional.CollectionTest::testNullsInListSerialization
  public void testNullsInListSerialization() {
    List<String> list = new ArrayList<String>();
    list.add("foo");
    list.add(null);
    list.add("bar");
    String expected = "[\"foo\",null,\"bar\"]";
    Type typeOfList = new TypeToken<List<String>>() {}.getType();
    String json = gson.toJson(list, typeOfList);
    assertEquals(expected, json);
  }

// com.google.gson.functional.CollectionTest::testNullsInListDeserialization
  public void testNullsInListDeserialization() {
    List<String> expected = new ArrayList<String>();
    expected.add("foo");
    expected.add(null);
    expected.add("bar");
    String json = "[\"foo\",null,\"bar\"]";
    Type expectedType = new TypeToken<List<String>>() {}.getType();
    List<String> target = gson.fromJson(json, expectedType);
    for (int i = 0; i < expected.size(); ++i) {
      assertEquals(expected.get(i), target.get(i));
    }
  }

// com.google.gson.functional.CollectionTest::testCollectionOfObjectSerialization
  public void testCollectionOfObjectSerialization() {
    List<Object> target = new ArrayList<Object>();
    target.add("Hello");
    target.add("World");
    assertEquals("[\"Hello\",\"World\"]", gson.toJson(target));
    
    Type type = new TypeToken<List<Object>>() {}.getType();
    assertEquals("[\"Hello\",\"World\"]", gson.toJson(target, type));
  }

// com.google.gson.functional.CollectionTest::testCollectionOfObjectWithNullSerialization
  public void testCollectionOfObjectWithNullSerialization() {
    List<Object> target = new ArrayList<Object>();
    target.add("Hello");
    target.add(null);
    target.add("World");
    assertEquals("[\"Hello\",null,\"World\"]", gson.toJson(target));
    
    Type type = new TypeToken<List<Object>>() {}.getType();
    assertEquals("[\"Hello\",null,\"World\"]", gson.toJson(target, type));
  }

// com.google.gson.functional.CollectionTest::testCollectionOfStringsSerialization
  public void testCollectionOfStringsSerialization() {
    List<String> target = new ArrayList<String>();
    target.add("Hello");
    target.add("World");
    assertEquals("[\"Hello\",\"World\"]", gson.toJson(target));
  }

// com.google.gson.functional.CollectionTest::testCollectionOfBagOfPrimitivesSerialization
  public void testCollectionOfBagOfPrimitivesSerialization() {
    List<BagOfPrimitives> target = new ArrayList<BagOfPrimitives>();
    BagOfPrimitives objA = new BagOfPrimitives(3L, 1, true, "blah");
    BagOfPrimitives objB = new BagOfPrimitives(2L, 6, false, "blahB");
    target.add(objA);
    target.add(objB);

    String result = gson.toJson(target);
    assertTrue(result.startsWith("["));
    assertTrue(result.endsWith("]"));
    for (BagOfPrimitives obj : target) {
      assertTrue(result.contains(obj.getExpectedJson()));
    }
  }

// com.google.gson.functional.CollectionTest::testCollectionOfStringsDeserialization
  public void testCollectionOfStringsDeserialization() {
    String json = "[\"Hello\",\"World\"]";
    Type collectionType = new TypeToken<Collection<String>>() { }.getType();
    Collection<String> target = gson.fromJson(json, collectionType);

    assertTrue(target.contains("Hello"));
    assertTrue(target.contains("World"));
  }

// com.google.gson.functional.CollectionTest::testRawCollectionOfIntegersSerialization
  public void testRawCollectionOfIntegersSerialization() {
    Collection<Integer> target = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9);
    assertEquals("[1,2,3,4,5,6,7,8,9]", gson.toJson(target));
  }

// com.google.gson.functional.CollectionTest::testRawCollectionSerialization
  public void testRawCollectionSerialization() {
    BagOfPrimitives bag1 = new BagOfPrimitives();
    Collection target = Arrays.asList(bag1, bag1);
    String json = gson.toJson(target);
    assertTrue(json.contains(bag1.getExpectedJson()));
  }

// com.google.gson.functional.CollectionTest::testRawCollectionDeserializationNotAlllowed
  public void testRawCollectionDeserializationNotAlllowed() {
    String json = "[0,1,2,3,4,5,6,7,8,9]";
    try {
        gson.fromJson(json, Collection.class);
        fail("Can not deserialize a non-genericized collection.");
    } catch (JsonParseException expected) { }

    json = "[\"Hello\", \"World\"]";
    try {
      gson.fromJson(json, Collection.class);
      fail("Can not deserialize a non-genericized collection.");
    } catch (JsonParseException expected) { }
  }

// com.google.gson.functional.CollectionTest::testRawCollectionOfBagOfPrimitivesNotAllowed
  public void testRawCollectionOfBagOfPrimitivesNotAllowed() {
    try {
      BagOfPrimitives bag = new BagOfPrimitives(10, 20, false, "stringValue");
      String json = '[' + bag.getExpectedJson() + ',' + bag.getExpectedJson() + ']';
      Collection target = gson.fromJson(json, Collection.class);
      assertEquals(2, target.size());
      for (BagOfPrimitives bag1 : (Collection<BagOfPrimitives>) target) {
        assertEquals(bag.getExpectedJson(), bag1.getExpectedJson());
      }
      fail("Raw collection of objects should not work");
    } catch (JsonParseException expected) {
    }
  }

// com.google.gson.functional.CollectionTest::testWildcardPrimitiveCollectionSerilaization
  public void testWildcardPrimitiveCollectionSerilaization() throws Exception {
    Collection<? extends Integer> target = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9);
    Type collectionType = new TypeToken<Collection<? extends Integer>>() { }.getType();
    String json = gson.toJson(target, collectionType);
    assertEquals("[1,2,3,4,5,6,7,8,9]", json);
    
    json = gson.toJson(target);
    assertEquals("[1,2,3,4,5,6,7,8,9]", json);
  }

// com.google.gson.functional.CollectionTest::testWildcardPrimitiveCollectionDeserilaization
  public void testWildcardPrimitiveCollectionDeserilaization() throws Exception {
    String json = "[1,2,3,4,5,6,7,8,9]";
    Type collectionType = new TypeToken<Collection<? extends Integer>>() { }.getType();
    Collection<? extends Integer> target = gson.fromJson(json, collectionType);
    assertEquals(9, target.size());
    assertTrue(target.contains(1));
    assertTrue(target.contains(9));
  }

// com.google.gson.functional.CollectionTest::testWildcardCollectionField
  public void testWildcardCollectionField() throws Exception {
    Collection<BagOfPrimitives> collection = new ArrayList<BagOfPrimitives>();
    BagOfPrimitives objA = new BagOfPrimitives(3L, 1, true, "blah");
    BagOfPrimitives objB = new BagOfPrimitives(2L, 6, false, "blahB");
    collection.add(objA);
    collection.add(objB);
    
    ObjectWithWildcardCollection target = new ObjectWithWildcardCollection(collection);
    String json = gson.toJson(target);
    assertTrue(json.contains(objA.getExpectedJson()));
    assertTrue(json.contains(objB.getExpectedJson()));
    
    target = gson.fromJson(json, ObjectWithWildcardCollection.class);
    Collection<? extends BagOfPrimitives> deserializedCollection = target.getCollection();
    assertEquals(2, deserializedCollection.size());
    assertTrue(deserializedCollection.contains(objA));
    assertTrue(deserializedCollection.contains(objB));
  }

// com.google.gson.functional.CollectionTest::testSetSerialization
  public void testSetSerialization() {
    Set<Entry> set = new HashSet<Entry>(); 
    set.add(new Entry(1));
    set.add(new Entry(2));    
    String json = gson.toJson(set);
    assertTrue(json.contains("1"));
    assertTrue(json.contains("2"));
  }

// com.google.gson.functional.CollectionTest::testSetDeserialization
  public void testSetDeserialization() {
    String json = "[{value:1},{value:2}]";
    Type type = new TypeToken<Set<Entry>>() {}.getType();        
    Set<Entry> set = gson.fromJson(json, type);
    assertEquals(2, set.size());
    for (Entry entry : set) {
      assertTrue(entry.value == 1 || entry.value == 2);
    }
  }

// com.google.gson.functional.ConcurrencyTest::testSingleThreadSerialization
  public void testSingleThreadSerialization() { 
    MyObject myObj = new MyObject(); 
    for (int i = 0; i < 10; i++) { 
      gson.toJson(myObj); 
    } 
  }

// com.google.gson.functional.ConcurrencyTest::testSingleThreadDeserialization
  public void testSingleThreadDeserialization() { 
    for (int i = 0; i < 10; i++) { 
      gson.fromJson("{'a':'hello','b':'world','i':1}", MyObject.class); 
    } 
  }

// com.google.gson.functional.ConcurrencyTest::testMultiThreadSerialization
  public void testMultiThreadSerialization() throws InterruptedException {
    final CountDownLatch startLatch = new CountDownLatch(1);
    final CountDownLatch finishedLatch = new CountDownLatch(10);
    final AtomicBoolean failed = new AtomicBoolean(false);
    ExecutorService executor = Executors.newFixedThreadPool(10);
    for (int taskCount = 0; taskCount < 10; taskCount++) {
      executor.execute(new Runnable() {
        public void run() {
          MyObject myObj = new MyObject();
          try {
            startLatch.await();
            for (int i = 0; i < 10; i++) {
              gson.toJson(myObj);
            }
          } catch (Throwable t) {
            failed.set(true);
          } finally {
            finishedLatch.countDown();
          }
        }
      });
    }
    startLatch.countDown();
    finishedLatch.await();
    assertFalse(failed.get());
  }

// com.google.gson.functional.ConcurrencyTest::testMultiThreadDeserialization
  public void testMultiThreadDeserialization() throws InterruptedException {
    final CountDownLatch startLatch = new CountDownLatch(1);
    final CountDownLatch finishedLatch = new CountDownLatch(10);
    final AtomicBoolean failed = new AtomicBoolean(false);
    ExecutorService executor = Executors.newFixedThreadPool(10);
    for (int taskCount = 0; taskCount < 10; taskCount++) {
      executor.execute(new Runnable() {
        public void run() {
          try {
            startLatch.await();
            for (int i = 0; i < 10; i++) {
              gson.fromJson("{'a':'hello','b':'world','i':1}", MyObject.class); 
            }
          } catch (Throwable t) {
            failed.set(true);
          } finally {
            finishedLatch.countDown();
          }
        }
      });
    }
    startLatch.countDown();
    finishedLatch.await();
    assertFalse(failed.get());
  }

// com.google.gson.functional.CustomDeserializerTest::testDefaultConstructorNotCalledOnObject
  public void testDefaultConstructorNotCalledOnObject() throws Exception {
    DataHolder data = new DataHolder(DEFAULT_VALUE);
    String json = gson.toJson(data);
    
    DataHolder actual = gson.fromJson(json, DataHolder.class);
    assertEquals(DEFAULT_VALUE + SUFFIX, actual.getData());
  }

// com.google.gson.functional.CustomDeserializerTest::testDefaultConstructorNotCalledOnField
  public void testDefaultConstructorNotCalledOnField() throws Exception {
    DataHolderWrapper dataWrapper = new DataHolderWrapper(new DataHolder(DEFAULT_VALUE));
    String json = gson.toJson(dataWrapper);
    
    DataHolderWrapper actual = gson.fromJson(json, DataHolderWrapper.class);
    assertEquals(DEFAULT_VALUE + SUFFIX, actual.getWrappedData().getData());
  }

// com.google.gson.functional.CustomDeserializerTest::testJsonTypeFieldBasedDeserialization
  public void testJsonTypeFieldBasedDeserialization() {
    String json = "{field1:'abc',field2:'def',__type__:'SUB_TYPE1'}";
    Gson gson = new GsonBuilder().registerTypeAdapter(MyBase.class, new JsonDeserializer<MyBase>() {
      public MyBase deserialize(JsonElement json, Type pojoType,
          JsonDeserializationContext context) throws JsonParseException {
        String type = json.getAsJsonObject().get(MyBase.TYPE_ACCESS).getAsString();
        return context.deserialize(json, SubTypes.valueOf(type).getSubclass());
      }
    }).create();    
    SubType1 target = (SubType1) gson.fromJson(json, MyBase.class);
    assertEquals("abc", target.field1);    
  }

// com.google.gson.functional.CustomDeserializerTest::testCustomDeserializerReturnsNullForTopLevelObject
  public void testCustomDeserializerReturnsNullForTopLevelObject() {
    Gson gson = new GsonBuilder()
      .registerTypeAdapter(Base.class, new JsonDeserializer<Base>() {
        public Base deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
          return null;
        }
      }).create();
    String json = "{baseName:'Base',subName:'SubRevised'}";
    Base target = gson.fromJson(json, Base.class);
    assertNull(target);
  }

// com.google.gson.functional.CustomDeserializerTest::testCustomDeserializerReturnsNull
  public void testCustomDeserializerReturnsNull() {
    Gson gson = new GsonBuilder()
      .registerTypeAdapter(Base.class, new JsonDeserializer<Base>() {
        public Base deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
          return null;
        }
      }).create();
    String json = "{base:{baseName:'Base',subName:'SubRevised'}}";
    ClassWithBaseField target = gson.fromJson(json, ClassWithBaseField.class);
    assertNull(target.base);
  }

// com.google.gson.functional.CustomDeserializerTest::testCustomDeserializerReturnsNullForTopLevelPrimitives
  public void testCustomDeserializerReturnsNullForTopLevelPrimitives() {
    Gson gson = new GsonBuilder()
      .registerTypeAdapter(long.class, new JsonDeserializer<Long>() {
        public Long deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
          return null;
        }
      }).create();
    String json = "10";
    assertNull(gson.fromJson(json, long.class));
  }

// com.google.gson.functional.CustomDeserializerTest::testCustomDeserializerReturnsNullForPrimitiveFields
  public void testCustomDeserializerReturnsNullForPrimitiveFields() {
    Gson gson = new GsonBuilder()
      .registerTypeAdapter(long.class, new JsonDeserializer<Long>() {
        public Long deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
          return null;
        }
      }).create();
    String json = "{field:10}";
    ClassWithLong target = gson.fromJson(json, ClassWithLong.class);
    assertEquals(0, target.field);
  }

// com.google.gson.functional.CustomDeserializerTest::testCustomDeserializerReturnsNullForArrayElements
  public void testCustomDeserializerReturnsNullForArrayElements() {
    Gson gson = new GsonBuilder()
      .registerTypeAdapter(Base.class, new JsonDeserializer<Base>() {
        public Base deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
          return null;
        }
      }).create();
    String json = "[{baseName:'Base'},{baseName:'Base'}]";
    Base[] target = gson.fromJson(json, Base[].class);
    assertNull(target[0]);
    assertNull(target[1]);
  }

// com.google.gson.functional.CustomDeserializerTest::testCustomDeserializerReturnsNullForArrayElementsForArrayField
  public void testCustomDeserializerReturnsNullForArrayElementsForArrayField() {
    Gson gson = new GsonBuilder()
      .registerTypeAdapter(Base.class, new JsonDeserializer<Base>() {
        public Base deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
          return null;
        }
      }).create();
    String json = "{bases:[{baseName:'Base'},{baseName:'Base'}]}";
    ClassWithBaseArray target = gson.fromJson(json, ClassWithBaseArray.class);
    assertNull(target.bases[0]);
    assertNull(target.bases[1]);
  }

// com.google.gson.functional.CustomSerializerTest::testBaseClassSerializerInvokedForBaseClassFields
   public void testBaseClassSerializerInvokedForBaseClassFields() {
     Gson gson = new GsonBuilder()
         .registerTypeAdapter(Base.class, new BaseSerializer())
         .registerTypeAdapter(Sub.class, new SubSerializer())
         .create();
     ClassWithBaseField target = new ClassWithBaseField(new Base());
     JsonObject json = (JsonObject) gson.toJsonTree(target);
     JsonObject base = json.get("base").getAsJsonObject();
     assertEquals(BaseSerializer.NAME, base.get(Base.SERIALIZER_KEY).getAsString());
   }

// com.google.gson.functional.CustomSerializerTest::testSubClassSerializerInvokedForBaseClassFieldsHoldingSubClassInstances
   public void testSubClassSerializerInvokedForBaseClassFieldsHoldingSubClassInstances() {
     Gson gson = new GsonBuilder()
         .registerTypeAdapter(Base.class, new BaseSerializer())
         .registerTypeAdapter(Sub.class, new SubSerializer())
         .create();
     ClassWithBaseField target = new ClassWithBaseField(new Sub());
     JsonObject json = (JsonObject) gson.toJsonTree(target);
     JsonObject base = json.get("base").getAsJsonObject();
     assertEquals(SubSerializer.NAME, base.get(Base.SERIALIZER_KEY).getAsString());
   }

// com.google.gson.functional.CustomSerializerTest::testSubClassSerializerInvokedForBaseClassFieldsHoldingArrayOfSubClassInstances
   public void testSubClassSerializerInvokedForBaseClassFieldsHoldingArrayOfSubClassInstances() {
     Gson gson = new GsonBuilder()
         .registerTypeAdapter(Base.class, new BaseSerializer())
         .registerTypeAdapter(Sub.class, new SubSerializer())
         .create();
     ClassWithBaseArrayField target = new ClassWithBaseArrayField(new Base[] {new Sub(), new Sub()});
     JsonObject json = (JsonObject) gson.toJsonTree(target);
     JsonArray array = json.get("base").getAsJsonArray();
     for (JsonElement element : array) {
       JsonElement serializerKey = element.getAsJsonObject().get(Base.SERIALIZER_KEY);
      assertEquals(SubSerializer.NAME, serializerKey.getAsString());
     }
   }

// com.google.gson.functional.CustomSerializerTest::testBaseClassSerializerInvokedForBaseClassFieldsHoldingSubClassInstances
   public void testBaseClassSerializerInvokedForBaseClassFieldsHoldingSubClassInstances() {
     Gson gson = new GsonBuilder()
         .registerTypeAdapter(Base.class, new BaseSerializer())
         .create();
     ClassWithBaseField target = new ClassWithBaseField(new Sub());
     JsonObject json = (JsonObject) gson.toJsonTree(target);
     JsonObject base = json.get("base").getAsJsonObject();
     assertEquals(BaseSerializer.NAME, base.get(Base.SERIALIZER_KEY).getAsString());
   }

// com.google.gson.functional.CustomSerializerTest::testSerializerReturnsNull
   public void testSerializerReturnsNull() {
     Gson gson = new GsonBuilder()
       .registerTypeAdapter(Base.class, new JsonSerializer<Base>() {
         public JsonElement serialize(Base src, Type typeOfSrc, JsonSerializationContext context) {
           return null;
         }
       })
       .create();
       JsonElement json = gson.toJsonTree(new Base());
       assertTrue(json.isJsonNull());
   }

// com.google.gson.functional.CustomTypeAdaptersTest::testCustomSerializers
  public void testCustomSerializers() {
    Gson gson = builder.registerTypeAdapter(
        ClassWithCustomTypeConverter.class, new JsonSerializer<ClassWithCustomTypeConverter>() {
      public JsonElement serialize(ClassWithCustomTypeConverter src, Type typeOfSrc,
          JsonSerializationContext context) {
        JsonObject json = new JsonObject();
        json.addProperty("bag", 5);
        json.addProperty("value", 25);
        return json;
      }
    }).create();
    ClassWithCustomTypeConverter target = new ClassWithCustomTypeConverter();
    assertEquals("{\"bag\":5,\"value\":25}", gson.toJson(target));
  }

// com.google.gson.functional.CustomTypeAdaptersTest::testCustomDeserializers
  public void testCustomDeserializers() {
    Gson gson = new GsonBuilder().registerTypeAdapter(
        ClassWithCustomTypeConverter.class, new JsonDeserializer<ClassWithCustomTypeConverter>() {
      public ClassWithCustomTypeConverter deserialize(JsonElement json, Type typeOfT,
          JsonDeserializationContext context) {
        JsonObject jsonObject = json.getAsJsonObject();
        int value = jsonObject.get("bag").getAsInt();
        return new ClassWithCustomTypeConverter(new BagOfPrimitives(value,
            value, false, ""), value);
      }
    }).create();
    String json = "{\"bag\":5,\"value\":25}";
    ClassWithCustomTypeConverter target = gson.fromJson(json, ClassWithCustomTypeConverter.class);
    assertEquals(5, target.getBag().getIntValue());
  }

// com.google.gson.functional.CustomTypeAdaptersTest::testCustomNestedSerializers
  public void testCustomNestedSerializers() {
    Gson gson = new GsonBuilder().registerTypeAdapter(
        BagOfPrimitives.class, new JsonSerializer<BagOfPrimitives>() {
      public JsonElement serialize(BagOfPrimitives src, Type typeOfSrc,
          JsonSerializationContext context) {
        return new JsonPrimitive(6);
      }
    }).create();
    ClassWithCustomTypeConverter target = new ClassWithCustomTypeConverter();
    assertEquals("{\"bag\":6,\"value\":10}", gson.toJson(target));
  }

// com.google.gson.functional.CustomTypeAdaptersTest::testCustomNestedDeserializers
  public void testCustomNestedDeserializers() {
    Gson gson = new GsonBuilder().registerTypeAdapter(
        BagOfPrimitives.class, new JsonDeserializer<BagOfPrimitives>() {
      public BagOfPrimitives deserialize(JsonElement json, Type typeOfT,
          JsonDeserializationContext context) throws JsonParseException {
        int value = json.getAsInt();
        return new BagOfPrimitives(value, value, false, "");
      }
    }).create();
    String json = "{\"bag\":7,\"value\":25}";
    ClassWithCustomTypeConverter target = gson.fromJson(json, ClassWithCustomTypeConverter.class);
    assertEquals(7, target.getBag().getIntValue());
  }

// com.google.gson.functional.CustomTypeAdaptersTest::testCustomTypeAdapterDoesNotAppliesToSubClasses
  public void testCustomTypeAdapterDoesNotAppliesToSubClasses() {
    Gson gson = new GsonBuilder().registerTypeAdapter(Base.class, new JsonSerializer<Base> () {
      public JsonElement serialize(Base src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject json = new JsonObject();
        json.addProperty("value", src.baseValue);
        return json;
      }          
    }).create();
    Base b = new Base();
    String json = gson.toJson(b);
    assertTrue(json.contains("value"));    
    b = new Derived();
    json = gson.toJson(b);
    assertTrue(json.contains("derivedValue"));    
  }

// com.google.gson.functional.CustomTypeAdaptersTest::testCustomTypeAdapterAppliesToSubClassesSerializedAsBaseClass
  public void testCustomTypeAdapterAppliesToSubClassesSerializedAsBaseClass() {
    Gson gson = new GsonBuilder().registerTypeAdapter(Base.class, new JsonSerializer<Base> () {
      public JsonElement serialize(Base src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject json = new JsonObject();
        json.addProperty("value", src.baseValue);
        return json;
      }          
    }).create();
    Base b = new Base();
    String json = gson.toJson(b);
    assertTrue(json.contains("value"));    
    b = new Derived();
    json = gson.toJson(b, Base.class);
    assertTrue(json.contains("value"));    
    assertFalse(json.contains("derivedValue"));
  }

// com.google.gson.functional.CustomTypeAdaptersTest::testCustomSerializerForLong
  public void testCustomSerializerForLong() {
    final ClassWithBooleanField customSerializerInvoked = new ClassWithBooleanField();
    customSerializerInvoked.value = false;
    Gson gson = new GsonBuilder().registerTypeAdapter(Long.class, new JsonSerializer<Long>() {
      public JsonElement serialize(Long src, Type typeOfSrc, JsonSerializationContext context) {
        customSerializerInvoked.value = true;
        return new JsonPrimitive(src);
      }      
    }).serializeNulls().create();
    ClassWithWrapperLongField src = new ClassWithWrapperLongField();
    String json = gson.toJson(src);
    assertTrue(json.contains("\"value\":null"));
    assertFalse(customSerializerInvoked.value);
    
    customSerializerInvoked.value = false;
    src.value = 10L;
    json = gson.toJson(src);
    assertTrue(json.contains("\"value\":10"));
    assertTrue(customSerializerInvoked.value);
  }

// com.google.gson.functional.CustomTypeAdaptersTest::testCustomDeserializerForLong
  public void testCustomDeserializerForLong() {
    final ClassWithBooleanField customDeserializerInvoked = new ClassWithBooleanField();
    customDeserializerInvoked.value = false;
    Gson gson = new GsonBuilder().registerTypeAdapter(Long.class, new JsonDeserializer<Long>() {
      public Long deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
          throws JsonParseException {
        customDeserializerInvoked.value = true;
        if (json == null || json.isJsonNull()) {
          return null;
        }
        Number number = json.getAsJsonPrimitive().getAsNumber();
        return number == null ? null : number.longValue();
      }      
    }).create();
    String json = "{'value':null}";
    ClassWithWrapperLongField target = gson.fromJson(json, ClassWithWrapperLongField.class);
    assertNull(target.value);
    assertFalse(customDeserializerInvoked.value);
    
    customDeserializerInvoked.value = false;
    json = "{'value':10}";
    target = gson.fromJson(json, ClassWithWrapperLongField.class);
    assertEquals(10L, target.value.longValue());
    assertTrue(customDeserializerInvoked.value);
  }

// com.google.gson.functional.CustomTypeAdaptersTest::testCustomByteArraySerializer
  public void testCustomByteArraySerializer() {
    Gson gson = new GsonBuilder().registerTypeAdapter(byte[].class, new JsonSerializer<byte[]>() {
      public JsonElement serialize(byte[] src, Type typeOfSrc, JsonSerializationContext context) {
        StringBuilder sb = new StringBuilder(src.length);
        for (byte b : src) {
          sb.append(b);
        }
        return new JsonPrimitive(sb.toString());
      }      
    }).create();
    byte[] data = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
    String json = gson.toJson(data);
    assertEquals("\"0123456789\"", json);
  }

// com.google.gson.functional.CustomTypeAdaptersTest::testCustomByteArrayDeserializerAndInstanceCreator
  public void testCustomByteArrayDeserializerAndInstanceCreator() {
    GsonBuilder gsonBuilder = new GsonBuilder().registerTypeAdapter(byte[].class, 
        new JsonDeserializer<byte[]>() {
      public byte[] deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
          throws JsonParseException {
        String str = json.getAsString();
        byte[] data = new byte[str.length()];
        for (int i = 0; i < data.length; ++i) {
          data[i] = Byte.parseByte(""+str.charAt(i));
        }
        return data;
      }      
    });
    Gson gson = gsonBuilder.create();
    String json = "'0123456789'";
    byte[] actual = gson.fromJson(json, byte[].class);
    byte[] expected = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
    for (int i = 0; i < actual.length; ++i) {
      assertEquals(expected[i], actual[i]);
    }
  }

// com.google.gson.functional.CustomTypeAdaptersTest::testCustomAdapterInvokedForCollectionElementSerializationWithType
  public void testCustomAdapterInvokedForCollectionElementSerializationWithType() {
    Gson gson = new GsonBuilder()
      .registerTypeAdapter(StringHolder.class, new StringHolderTypeAdapter())
      .create();
    Type setType = new TypeToken<Set<StringHolder>>() {}.getType();
    StringHolder holder = new StringHolder("Jacob", "Tomaw");
    Set<StringHolder> setOfHolders = new HashSet<StringHolder>();
    setOfHolders.add(holder);
    String json = gson.toJson(setOfHolders, setType);
    assertTrue(json.contains("Jacob:Tomaw"));
  }

// com.google.gson.functional.CustomTypeAdaptersTest::testCustomAdapterInvokedForCollectionElementSerialization
  public void testCustomAdapterInvokedForCollectionElementSerialization() {
    Gson gson = new GsonBuilder()
      .registerTypeAdapter(StringHolder.class, new StringHolderTypeAdapter())
      .create();
    StringHolder holder = new StringHolder("Jacob", "Tomaw");
    Set<StringHolder> setOfHolders = new HashSet<StringHolder>();
    setOfHolders.add(holder);
    String json = gson.toJson(setOfHolders);
    assertTrue(json.contains("Jacob:Tomaw"));
  }

// com.google.gson.functional.CustomTypeAdaptersTest::testCustomAdapterInvokedForCollectionElementDeserialization
  public void testCustomAdapterInvokedForCollectionElementDeserialization() {
    Gson gson = new GsonBuilder()
      .registerTypeAdapter(StringHolder.class, new StringHolderTypeAdapter())
      .create();
    Type setType = new TypeToken<Set<StringHolder>>() {}.getType();
    Set<StringHolder> setOfHolders = gson.fromJson("['Jacob:Tomaw']", setType);
    assertEquals(1, setOfHolders.size());
    StringHolder foo = setOfHolders.iterator().next();
    assertEquals("Jacob", foo.part1);
    assertEquals("Tomaw", foo.part2);
  }

// com.google.gson.functional.CustomTypeAdaptersTest::testCustomAdapterInvokedForMapElementSerializationWithType
  public void testCustomAdapterInvokedForMapElementSerializationWithType() {
    Gson gson = new GsonBuilder()
      .registerTypeAdapter(StringHolder.class, new StringHolderTypeAdapter())
      .create();
    Type mapType = new TypeToken<Map<String,StringHolder>>() {}.getType();
    StringHolder holder = new StringHolder("Jacob", "Tomaw");
    Map<String, StringHolder> mapOfHolders = new HashMap<String, StringHolder>();
    mapOfHolders.put("foo", holder);
    String json = gson.toJson(mapOfHolders, mapType);
    assertTrue(json.contains("\"foo\":\"Jacob:Tomaw\""));
  }

// com.google.gson.functional.CustomTypeAdaptersTest::testCustomAdapterInvokedForMapElementSerialization
  public void testCustomAdapterInvokedForMapElementSerialization() {
    Gson gson = new GsonBuilder()
      .registerTypeAdapter(StringHolder.class, new StringHolderTypeAdapter())
      .create();
    StringHolder holder = new StringHolder("Jacob", "Tomaw");
    Map<String, StringHolder> mapOfHolders = new HashMap<String, StringHolder>();
    mapOfHolders.put("foo", holder);
    String json = gson.toJson(mapOfHolders);
    assertTrue(json.contains("\"foo\":\"Jacob:Tomaw\""));
  }

// com.google.gson.functional.CustomTypeAdaptersTest::testCustomAdapterInvokedForMapElementDeserialization
  public void testCustomAdapterInvokedForMapElementDeserialization() {
    Gson gson = new GsonBuilder()
      .registerTypeAdapter(StringHolder.class, new StringHolderTypeAdapter())
      .create();
    Type mapType = new TypeToken<Map<String, StringHolder>>() {}.getType();
    Map<String, StringHolder> mapOfFoo = gson.fromJson("{'foo':'Jacob:Tomaw'}", mapType);
    assertEquals(1, mapOfFoo.size());
    StringHolder foo = mapOfFoo.get("foo");
    assertEquals("Jacob", foo.part1);
    assertEquals("Tomaw", foo.part2);
  }

// com.google.gson.functional.CustomTypeAdaptersTest::testEnsureCustomSerializerNotInvokedForNullValues
  public void testEnsureCustomSerializerNotInvokedForNullValues() {
    Gson gson = new GsonBuilder()
        .registerTypeAdapter(DataHolder.class, new DataHolderSerializer())
        .create();
    DataHolderWrapper target = new DataHolderWrapper(new DataHolder("abc"));
    String json = gson.toJson(target);
    assertEquals("{\"wrappedData\":{\"myData\":\"abc\"}}", json);
  }

// com.google.gson.functional.CustomTypeAdaptersTest::testEnsureCustomDeserializerNotInvokedForNullValues
  public void testEnsureCustomDeserializerNotInvokedForNullValues() {
    Gson gson = new GsonBuilder()
        .registerTypeAdapter(DataHolder.class, new DataHolderDeserializer())
        .create();
    String json = "{wrappedData:null}";
    DataHolderWrapper actual = gson.fromJson(json, DataHolderWrapper.class);
    assertNull(actual.wrappedData);
  }

// com.google.gson.functional.DefaultTypeAdaptersTest::testUrlSerialization
  public void testUrlSerialization() throws Exception {
    String urlValue = "http://google.com/";
    URL url = new URL(urlValue);
    assertEquals("\"http://google.com/\"", gson.toJson(url));
  }

// com.google.gson.functional.DefaultTypeAdaptersTest::testUrlDeserialization
  public void testUrlDeserialization() {
    String urlValue = "http://google.com/";
    String json = "'http:\\/\\/google.com\\/'";
    URL target = gson.fromJson(json, URL.class);
    assertEquals(urlValue, target.toExternalForm());

    gson.fromJson('"' + urlValue + '"', URL.class);
    assertEquals(urlValue, target.toExternalForm());
  }

// com.google.gson.functional.DefaultTypeAdaptersTest::testUrlNullSerialization
  public void testUrlNullSerialization() throws Exception {
    ClassWithUrlField target = new ClassWithUrlField();
    assertEquals("{}", gson.toJson(target));
  }

// com.google.gson.functional.DefaultTypeAdaptersTest::testUrlNullDeserialization
  public void testUrlNullDeserialization() {
    String json = "{}";
    ClassWithUrlField target = gson.fromJson(json, ClassWithUrlField.class);
    assertNull(target.url);
  }

// com.google.gson.functional.DefaultTypeAdaptersTest::testUriSerialization
  public void testUriSerialization() throws Exception {
    String uriValue = "http://google.com/";
    URI uri = new URI(uriValue);
    assertEquals("\"http://google.com/\"", gson.toJson(uri));
  }

// com.google.gson.functional.DefaultTypeAdaptersTest::testUriDeserialization
  public void testUriDeserialization() {
    String uriValue = "http://google.com/";
    String json = '"' + uriValue + '"';
    URI target = gson.fromJson(json, URI.class);
    assertEquals(uriValue, target.toASCIIString());
  }

// com.google.gson.functional.DefaultTypeAdaptersTest::testUuidSerialization
  public void testUuidSerialization() throws Exception {
    String uuidValue = "c237bec1-19ef-4858-a98e-521cf0aad4c0";
    UUID uuid = UUID.fromString(uuidValue);
    assertEquals('"' + uuidValue + '"', gson.toJson(uuid));
  }

// com.google.gson.functional.DefaultTypeAdaptersTest::testUuidDeserialization
  public void testUuidDeserialization() {
    String uuidValue = "c237bec1-19ef-4858-a98e-521cf0aad4c0";
    String json = '"' + uuidValue + '"';
    UUID target = gson.fromJson(json, UUID.class);
    assertEquals(uuidValue, target.toString());
  }

// com.google.gson.functional.DefaultTypeAdaptersTest::testLocaleSerializationWithLanguage
  public void testLocaleSerializationWithLanguage() {
    Locale target = new Locale("en");
    assertEquals("\"en\"", gson.toJson(target));
  }

// com.google.gson.functional.DefaultTypeAdaptersTest::testLocaleDeserializationWithLanguage
  public void testLocaleDeserializationWithLanguage() {
    String json = "\"en\"";
    Locale locale = gson.fromJson(json, Locale.class);
    assertEquals("en", locale.getLanguage());
  }

// com.google.gson.functional.DefaultTypeAdaptersTest::testLocaleSerializationWithLanguageCountry
  public void testLocaleSerializationWithLanguageCountry() {
    Locale target = Locale.CANADA_FRENCH;
    assertEquals("\"fr_CA\"", gson.toJson(target));
  }

// com.google.gson.functional.DefaultTypeAdaptersTest::testLocaleDeserializationWithLanguageCountry
  public void testLocaleDeserializationWithLanguageCountry() {
    String json = "\"fr_CA\"";
    Locale locale = gson.fromJson(json, Locale.class);
    assertEquals(Locale.CANADA_FRENCH, locale);
  }

// com.google.gson.functional.DefaultTypeAdaptersTest::testLocaleSerializationWithLanguageCountryVariant
  public void testLocaleSerializationWithLanguageCountryVariant() {
    Locale target = new Locale("de", "DE", "EURO");
    String json = gson.toJson(target);
    assertEquals("\"de_DE_EURO\"", json);
  }

// com.google.gson.functional.DefaultTypeAdaptersTest::testLocaleDeserializationWithLanguageCountryVariant
  public void testLocaleDeserializationWithLanguageCountryVariant() {
    String json = "\"de_DE_EURO\"";
    Locale locale = gson.fromJson(json, Locale.class);
    assertEquals("de", locale.getLanguage());
    assertEquals("DE", locale.getCountry());
    assertEquals("EURO", locale.getVariant());
  }

// com.google.gson.functional.DefaultTypeAdaptersTest::testBigDecimalFieldSerialization
  public void testBigDecimalFieldSerialization() {
    ClassWithBigDecimal target = new ClassWithBigDecimal("-122.01e-21");
    String json = gson.toJson(target);
    String actual = json.substring(json.indexOf(':') + 1, json.indexOf('}'));
    assertEquals(target.value, new BigDecimal(actual));
  }

// com.google.gson.functional.DefaultTypeAdaptersTest::testBigDecimalFieldDeserialization
  public void testBigDecimalFieldDeserialization() {
    ClassWithBigDecimal expected = new ClassWithBigDecimal("-122.01e-21");
    String json = expected.getExpectedJson();
    ClassWithBigDecimal actual = gson.fromJson(json, ClassWithBigDecimal.class);
    assertEquals(expected.value, actual.value);
  }

// com.google.gson.functional.DefaultTypeAdaptersTest::testBadValueForBigDecimalDeserialization
  public void testBadValueForBigDecimalDeserialization() {
    try {
      gson.fromJson("{\"value\"=1.5e-1.0031}", ClassWithBigDecimal.class);
      fail("Exponent of a BigDecimal must be an integer value.");
    } catch (JsonParseException expected) { }
  }

// com.google.gson.functional.DefaultTypeAdaptersTest::testBigIntegerFieldSerialization
  public void testBigIntegerFieldSerialization() {
    ClassWithBigInteger target = new ClassWithBigInteger("23232323215323234234324324324324324324");
    String json = gson.toJson(target);
    assertEquals(target.getExpectedJson(), json);
  }

// com.google.gson.functional.DefaultTypeAdaptersTest::testBigIntegerFieldDeserialization
  public void testBigIntegerFieldDeserialization() {
    ClassWithBigInteger expected = new ClassWithBigInteger("879697697697697697697697697697697697");
    String json = expected.getExpectedJson();
    ClassWithBigInteger actual = gson.fromJson(json, ClassWithBigInteger.class);
    assertEquals(expected.value, actual.value);
  }

// com.google.gson.functional.DefaultTypeAdaptersTest::testSetSerialization
  public void testSetSerialization() throws Exception {
    Gson gson = new Gson();
    HashSet<String> s = new HashSet<String>();
    s.add("blah");
    String json = gson.toJson(s);
    assertEquals("[\"blah\"]", json);

    json = gson.toJson(s, Set.class);
    assertEquals("[\"blah\"]", json);
  }

// com.google.gson.functional.DefaultTypeAdaptersTest::testDefaultDateSerialization
  public void testDefaultDateSerialization() {
    Date now = new Date();
    String json = gson.toJson(now);
    assertEquals("\"" + DateFormat.getDateTimeInstance().format(now) + "\"", json);
  }

// com.google.gson.functional.DefaultTypeAdaptersTest::testDefaultDateDeserialization
  public void testDefaultDateDeserialization() {
    String json = "'Dec 13, 2009, 07:18:02 AM'";
    Date extracted = gson.fromJson(json, Date.class);
    assertEqualsDate(extracted, 2009, 11, 13);
    assertEqualsTime(extracted, 7, 18, 02);
  }

// com.google.gson.functional.DefaultTypeAdaptersTest::testDefaultJavaSqlDateSerialization
  public void testDefaultJavaSqlDateSerialization() {
    java.sql.Date instant = new java.sql.Date(1259875082000L);
    String json = gson.toJson(instant);
    assertEquals("\"Dec 3, 2009\"", json);
  }

// com.google.gson.functional.DefaultTypeAdaptersTest::testDefaultJavaSqlDateDeserialization
  public void testDefaultJavaSqlDateDeserialization() {
    String json = "'Dec 3, 2009'";
    java.sql.Date extracted = gson.fromJson(json, java.sql.Date.class);
    assertEqualsDate(extracted, 2009, 11, 3);
  }

// com.google.gson.functional.DefaultTypeAdaptersTest::testDefaultJavaSqlTimestampSerialization
  public void testDefaultJavaSqlTimestampSerialization() {
    Timestamp now = new java.sql.Timestamp(1259875082000L);
    String json = gson.toJson(now);
    assertEquals("\"Dec 3, 2009, 1:18:02 PM\"", json);
  }

// com.google.gson.functional.DefaultTypeAdaptersTest::testDefaultJavaSqlTimestampDeserialization
  public void testDefaultJavaSqlTimestampDeserialization() {
    String json = "'Dec 3, 2009, 1:18:02 PM'";
    Timestamp extracted = gson.fromJson(json, Timestamp.class);
    assertEqualsDate(extracted, 2009, 11, 3);
    assertEqualsTime(extracted, 13, 18, 02);
  }

// com.google.gson.functional.DefaultTypeAdaptersTest::testDefaultJavaSqlTimeSerialization
  public void testDefaultJavaSqlTimeSerialization() {
    Time now = new Time(1259875082000L);
    String json = gson.toJson(now);
    assertEquals("\"01:18:02 PM\"", json);
  }

// com.google.gson.functional.DefaultTypeAdaptersTest::testDefaultJavaSqlTimeDeserialization
  public void testDefaultJavaSqlTimeDeserialization() {
    String json = "'1:18:02 PM'";
    Time extracted = gson.fromJson(json, Time.class);
    assertEqualsTime(extracted, 13, 18, 02);
  }

// com.google.gson.functional.DefaultTypeAdaptersTest::testDefaultDateSerializationUsingBuilder
  public void testDefaultDateSerializationUsingBuilder() throws Exception {
    Gson gson = new GsonBuilder().create();
    Date now = new Date();
    String json = gson.toJson(now);
    assertEquals("\"" + DateFormat.getDateTimeInstance().format(now) + "\"", json);
  }

// com.google.gson.functional.DefaultTypeAdaptersTest::testDefaultDateDeserializationUsingBuilder
  public void testDefaultDateDeserializationUsingBuilder() throws Exception {
    Gson gson = new GsonBuilder().create();
    Date now = new Date();
    String json = gson.toJson(now);
    Date extracted = gson.fromJson(json, Date.class);
    assertEquals(now.toString(), extracted.toString());
  }

// com.google.gson.functional.DefaultTypeAdaptersTest::testDefaultCalendarSerialization
  public void testDefaultCalendarSerialization() throws Exception {
    Gson gson = new GsonBuilder().create();
    String json = gson.toJson(Calendar.getInstance());
    assertTrue(json.contains("year"));
    assertTrue(json.contains("month"));
    assertTrue(json.contains("dayOfMonth"));
    assertTrue(json.contains("hourOfDay"));
    assertTrue(json.contains("minute"));
    assertTrue(json.contains("second"));
  }

// com.google.gson.functional.DefaultTypeAdaptersTest::testDefaultCalendarDeserialization
  public void testDefaultCalendarDeserialization() throws Exception {
    Gson gson = new GsonBuilder().create();
    String json = "{year:2009,month:2,dayOfMonth:11,hourOfDay:14,minute:29,second:23}";
    Calendar cal = gson.fromJson(json, Calendar.class);
    assertEquals(2009, cal.get(Calendar.YEAR));
    assertEquals(2, cal.get(Calendar.MONTH));
    assertEquals(11, cal.get(Calendar.DAY_OF_MONTH));
    assertEquals(14, cal.get(Calendar.HOUR_OF_DAY));
    assertEquals(29, cal.get(Calendar.MINUTE));
    assertEquals(23, cal.get(Calendar.SECOND));
  }

// com.google.gson.functional.DefaultTypeAdaptersTest::testDefaultGregorianCalendarSerialization
  public void testDefaultGregorianCalendarSerialization() throws Exception {
    Gson gson = new GsonBuilder().create();
    GregorianCalendar cal = new GregorianCalendar();
    String json = gson.toJson(cal);
    assertTrue(json.contains("year"));
    assertTrue(json.contains("month"));
    assertTrue(json.contains("dayOfMonth"));
    assertTrue(json.contains("hourOfDay"));
    assertTrue(json.contains("minute"));
    assertTrue(json.contains("second"));
  }

// com.google.gson.functional.DefaultTypeAdaptersTest::testDefaultGregorianCalendarDeserialization
  public void testDefaultGregorianCalendarDeserialization() throws Exception {
    Gson gson = new GsonBuilder().create();
    String json = "{year:2009,month:2,dayOfMonth:11,hourOfDay:14,minute:29,second:23}";
    GregorianCalendar cal = gson.fromJson(json, GregorianCalendar.class);
    assertEquals(2009, cal.get(Calendar.YEAR));
    assertEquals(2, cal.get(Calendar.MONTH));
    assertEquals(11, cal.get(Calendar.DAY_OF_MONTH));
    assertEquals(14, cal.get(Calendar.HOUR_OF_DAY));
    assertEquals(29, cal.get(Calendar.MINUTE));
    assertEquals(23, cal.get(Calendar.SECOND));
  }

// com.google.gson.functional.DefaultTypeAdaptersTest::testDateSerializationWithPattern
  public void testDateSerializationWithPattern() throws Exception {
    String pattern = "yyyy-MM-dd";
    DateFormat formatter = new SimpleDateFormat(pattern);
    Gson gson = new GsonBuilder().setDateFormat(DateFormat.FULL).setDateFormat(pattern).create();
    Date now = new Date();
    String json = gson.toJson(now);
    assertEquals("\"" + formatter.format(now) + "\"", json);
  }

// com.google.gson.functional.DefaultTypeAdaptersTest::testDateDeserializationWithPattern
  public void testDateDeserializationWithPattern() throws Exception {
    String pattern = "yyyy-MM-dd";
    Gson gson = new GsonBuilder().setDateFormat(DateFormat.FULL).setDateFormat(pattern).create();
    Date now = new Date();
    String json = gson.toJson(now);
    Date extracted = gson.fromJson(json, Date.class);
    assertEquals(now.getYear(), extracted.getYear());
    assertEquals(now.getMonth(), extracted.getMonth());
    assertEquals(now.getDay(), extracted.getDay());
  }

// com.google.gson.functional.DefaultTypeAdaptersTest::testDateSerializationWithPatternNotOverridenByTypeAdapter
  public void testDateSerializationWithPatternNotOverridenByTypeAdapter() throws Exception {
    String pattern = "yyyy-MM-dd";
    DateFormat formatter = new SimpleDateFormat(pattern);
    Gson gson = new GsonBuilder()
        .setDateFormat(pattern)
        .registerTypeAdapter(Date.class, new JsonDeserializer<Date>() {
          public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
              throws JsonParseException {
            return new Date();
          }
        })
        .create();

    Date now = new Date();
    String expectedDateString = "\"" + formatter.format(now) + "\"";
    String json = gson.toJson(now);
    assertEquals(expectedDateString, json);
  }

// com.google.gson.functional.DefaultTypeAdaptersTest::testPropertiesSerialization
  public void testPropertiesSerialization() {
    Properties props = new Properties();
    props.setProperty("foo", "bar");
    String json = gson.toJson(props);
    String expected = "{\"foo\":\"bar\"}";
    assertEquals(expected, json);
  }

// com.google.gson.functional.DefaultTypeAdaptersTest::testPropertiesDeserialization
  public void testPropertiesDeserialization() {
    String json = "{foo:'bar'}";
    Properties props = gson.fromJson(json, Properties.class);
    assertEquals("bar", props.getProperty("foo"));
  }

// com.google.gson.functional.DefaultTypeAdaptersTest::testTreeSetSerialization
  public void testTreeSetSerialization() {
    TreeSet<String> treeSet = new TreeSet<String>();
    treeSet.add("Value1");
    String json = gson.toJson(treeSet);
    assertEquals("[\"Value1\"]", json);
  }

// com.google.gson.functional.DefaultTypeAdaptersTest::testTreeSetDeserialization
  public void testTreeSetDeserialization() {
    String json = "['Value1']";
    Type type = new TypeToken<TreeSet<String>>() {}.getType();
    TreeSet<String> treeSet = gson.fromJson(json, type);
    assertTrue(treeSet.contains("Value1"));
  }

// com.google.gson.functional.EnumTest::testTopLevelEnumSerialization
  public void testTopLevelEnumSerialization() throws Exception {
    String result = gson.toJson(MyEnum.VALUE1);
    assertEquals('"' + MyEnum.VALUE1.toString() + '"', result);
  }

// com.google.gson.functional.EnumTest::testTopLevelEnumDeserialization
  public void testTopLevelEnumDeserialization() throws Exception {
    MyEnum result = gson.fromJson('"' + MyEnum.VALUE1.toString() + '"', MyEnum.class);
    assertEquals(MyEnum.VALUE1, result);
  }

// com.google.gson.functional.EnumTest::testTopLevelEnumInASingleElementArrayDeserialization
  public void testTopLevelEnumInASingleElementArrayDeserialization() {
    String json = "[" + MyEnum.VALUE1.getExpectedJson() + "]";
    MyEnum target = gson.fromJson(json, MyEnum.class);
    assertEquals(json, "[" + target.getExpectedJson() + "]");
  }

// com.google.gson.functional.EnumTest::testCollectionOfEnumsSerialization
  public void testCollectionOfEnumsSerialization() {
    Type type = new TypeToken<Collection<MyEnum>>() {}.getType();
    Collection<MyEnum> target = new ArrayList<MyEnum>();
    target.add(MyEnum.VALUE1);
    target.add(MyEnum.VALUE2);
    String expectedJson = "[\"VALUE1\",\"VALUE2\"]";
    String actualJson = gson.toJson(target);
    assertEquals(expectedJson, actualJson);
    actualJson = gson.toJson(target, type);
    assertEquals(expectedJson, actualJson);
  }

// com.google.gson.functional.EnumTest::testCollectionOfEnumsDeserialization
  public void testCollectionOfEnumsDeserialization() {
    Type type = new TypeToken<Collection<MyEnum>>() {}.getType();
    String json = "[\"VALUE1\",\"VALUE2\"]";
    Collection<MyEnum> target = gson.fromJson(json, type);
    MoreAsserts.assertContains(target, MyEnum.VALUE1);
    MoreAsserts.assertContains(target, MyEnum.VALUE2);
  }

// com.google.gson.functional.EnumTest::testClassWithEnumFieldSerialization
  public void testClassWithEnumFieldSerialization() throws Exception {
    ClassWithEnumFields target = new ClassWithEnumFields();
    assertEquals(target.getExpectedJson(), gson.toJson(target));
  }

// com.google.gson.functional.EnumTest::testClassWithEnumFieldDeserialization
  public void testClassWithEnumFieldDeserialization() throws Exception {
    String json = "{value1:'VALUE1',value2:'VALUE2'}";
    ClassWithEnumFields target = gson.fromJson(json, ClassWithEnumFields.class);
    assertEquals(MyEnum.VALUE1,target.value1);
    assertEquals(MyEnum.VALUE2,target.value2);
  }

// com.google.gson.functional.EnumTest::testEnumSubclass
  public void testEnumSubclass() {
    assertFalse(Roshambo.class == Roshambo.ROCK.getClass());
    assertEquals("\"ROCK\"", gson.toJson(Roshambo.ROCK));
    assertEquals("[\"ROCK\",\"PAPER\",\"SCISSORS\"]", gson.toJson(EnumSet.allOf(Roshambo.class)));
    assertEquals(Roshambo.ROCK, gson.fromJson("\"ROCK\"", Roshambo.class));
    assertEquals(EnumSet.allOf(Roshambo.class),
        gson.fromJson("[\"ROCK\",\"PAPER\",\"SCISSORS\"]", new TypeToken<Set<Roshambo>>() {}.getType()));
  }

// com.google.gson.functional.EscapingTest::testEscapingQuotesInStringArray
  public void testEscapingQuotesInStringArray() throws Exception {
    String[] valueWithQuotes = { "beforeQuote\"afterQuote" };
    String jsonRepresentation = gson.toJson(valueWithQuotes);
    String[] target = gson.fromJson(jsonRepresentation, String[].class);
    assertEquals(1, target.length);
    assertEquals(valueWithQuotes[0], target[0]);
  }

// com.google.gson.functional.EscapingTest::testEscapeAllHtmlCharacters
  public void testEscapeAllHtmlCharacters() {
    List<String> strings = new ArrayList<String>();
    strings.add("<");
    strings.add(">");
    strings.add("=");
    strings.add("&");
    strings.add("'");
    strings.add("\"");
    assertEquals("[\"\\u003c\",\"\\u003e\",\"\\u003d\",\"\\u0026\",\"\\u0027\",\"\\\"\"]",
        gson.toJson(strings));
  }

// com.google.gson.functional.EscapingTest::testEscapingObjectFields
  public void testEscapingObjectFields() throws Exception {
    BagOfPrimitives objWithPrimitives = new BagOfPrimitives(1L, 1, true, "test with\" <script>");
    String jsonRepresentation = gson.toJson(objWithPrimitives);
    assertFalse(jsonRepresentation.contains("<"));
    assertFalse(jsonRepresentation.contains(">"));
    assertTrue(jsonRepresentation.contains("\\\""));

    BagOfPrimitives expectedObject = gson.fromJson(jsonRepresentation, BagOfPrimitives.class);
    assertEquals(objWithPrimitives.getExpectedJson(), expectedObject.getExpectedJson());
  }

// com.google.gson.functional.EscapingTest::testGsonAcceptsEscapedAndNonEscapedJsonDeserialization
  public void testGsonAcceptsEscapedAndNonEscapedJsonDeserialization() throws Exception {
    Gson escapeHtmlGson = new GsonBuilder().create();
    Gson noEscapeHtmlGson = new GsonBuilder().disableHtmlEscaping().create();
    
    BagOfPrimitives target = new BagOfPrimitives(1L, 1, true, "test' / w'ith\" / \\ <script>");
    String escapedJsonForm = escapeHtmlGson.toJson(target);
    String nonEscapedJsonForm = noEscapeHtmlGson.toJson(target);
    assertFalse(escapedJsonForm.equals(nonEscapedJsonForm));
    
    assertEquals(target, noEscapeHtmlGson.fromJson(escapedJsonForm, BagOfPrimitives.class));
    assertEquals(target, escapeHtmlGson.fromJson(nonEscapedJsonForm, BagOfPrimitives.class));
  }

// com.google.gson.functional.ExclusionStrategyFunctionalTest::testExclusionStrategySerialization
  public void testExclusionStrategySerialization() throws Exception {
    String json = gson.toJson(src);
    assertFalse(json.contains("\"stringField\""));
    assertFalse(json.contains("\"annotatedField\""));
    assertTrue(json.contains("\"longField\""));
  }

// com.google.gson.functional.ExclusionStrategyFunctionalTest::testExclusionStrategyDeserialization
  public void testExclusionStrategyDeserialization() throws Exception {
    JsonObject json = new JsonObject();
    json.add("annotatedField", new JsonPrimitive(src.annotatedField + 5));
    json.add("stringField", new JsonPrimitive(src.stringField + "blah,blah"));
    json.add("longField", new JsonPrimitive(1212311L));

    SampleObjectForTest target = gson.fromJson(json, SampleObjectForTest.class);
    assertEquals(1212311L, target.longField);

    
    assertEquals(src.annotatedField, target.annotatedField);
    assertEquals(src.stringField, target.stringField);
  }

// com.google.gson.functional.ExposeFieldsTest::testNullExposeFieldSerialization
  public void testNullExposeFieldSerialization() throws Exception {
    ClassWithExposedFields object = new ClassWithExposedFields(null, 1);
    String json = gson.toJson(object);

    assertEquals(object.getExpectedJson(), json);
  }

// com.google.gson.functional.ExposeFieldsTest::testArrayWithOneNullExposeFieldObjectSerialization
  public void testArrayWithOneNullExposeFieldObjectSerialization() throws Exception {
    ClassWithExposedFields object1 = new ClassWithExposedFields(1, 1);
    ClassWithExposedFields object2 = new ClassWithExposedFields(null, 1);
    ClassWithExposedFields object3 = new ClassWithExposedFields(2, 2);
    ClassWithExposedFields[] objects = { object1, object2, object3 };

    String json = gson.toJson(objects);
    String expected = new StringBuilder()
        .append('[').append(object1.getExpectedJson()).append(',')
        .append(object2.getExpectedJson()).append(',')
        .append(object3.getExpectedJson()).append(']')
        .toString();

    assertEquals(expected, json);
  }

// com.google.gson.functional.ExposeFieldsTest::testExposeAnnotationSerialization
  public void testExposeAnnotationSerialization() throws Exception {
    ClassWithExposedFields target = new ClassWithExposedFields(1, 2);
    assertEquals(target.getExpectedJson(), gson.toJson(target));
  }

// com.google.gson.functional.ExposeFieldsTest::testExposeAnnotationDeserialization
  public void testExposeAnnotationDeserialization() throws Exception {
    String json = "{a:3,b:4,d:20.0}";
    ClassWithExposedFields target = gson.fromJson(json, ClassWithExposedFields.class);

    assertEquals(3, (int) target.a);
    assertNull(target.b);
    assertFalse(target.d == 20);
  }

// com.google.gson.functional.ExposeFieldsTest::testNoExposedFieldSerialization
  public void testNoExposedFieldSerialization() throws Exception {
    ClassWithNoExposedFields obj = new ClassWithNoExposedFields();
    String json = gson.toJson(obj);

    assertEquals("{}", json);
  }

// com.google.gson.functional.ExposeFieldsTest::testNoExposedFieldDeserialization
  public void testNoExposedFieldDeserialization() throws Exception {
    String json = "{a:4,b:5}";
    ClassWithNoExposedFields obj = gson.fromJson(json, ClassWithNoExposedFields.class);

    assertEquals(0, obj.a);
    assertEquals(1, obj.b);
  }

// com.google.gson.functional.ExposeFieldsTest::testExposedInterfaceFieldSerialization
  public void testExposedInterfaceFieldSerialization() throws Exception {
    String expected = "{\"interfaceField\":{}}";
    ClassWithInterfaceField target = new ClassWithInterfaceField(new SomeObject());
    String actual = gson.toJson(target);
    
    assertEquals(expected, actual);
  }

// com.google.gson.functional.ExposeFieldsTest::testExposedInterfaceFieldDeserialization
  public void testExposedInterfaceFieldDeserialization() throws Exception {
    String json = "{\"interfaceField\":{}}";
    ClassWithInterfaceField obj = gson.fromJson(json, ClassWithInterfaceField.class);

    assertNotNull(obj.interfaceField);
  }

// com.google.gson.functional.FieldExclusionTest::testDefaultInnerClassExclusion
  public void testDefaultInnerClassExclusion() throws Exception {
    Gson gson = new Gson();
    Outer.Inner target = outer.new Inner(VALUE);
    String result = gson.toJson(target);
    assertEquals(target.toJson(), result);
    
    gson = new GsonBuilder().create();
    target = outer.new Inner(VALUE);
    result = gson.toJson(target);
    assertEquals(target.toJson(), result);
  }

// com.google.gson.functional.FieldExclusionTest::testInnerClassExclusion
  public void testInnerClassExclusion() throws Exception {
    Gson gson = new GsonBuilder().disableInnerClassSerialization().create();
    Outer.Inner target = outer.new Inner(VALUE);
    String result = gson.toJson(target);
    assertEquals("", result);
  }

// com.google.gson.functional.FieldExclusionTest::testDefaultNestedStaticClassIncluded
  public void testDefaultNestedStaticClassIncluded() throws Exception {
    Gson gson = new Gson();
    Outer.Inner target = outer.new Inner(VALUE);
    String result = gson.toJson(target);
    assertEquals(target.toJson(), result);
    
    gson = new GsonBuilder().create();
    target = outer.new Inner(VALUE);
    result = gson.toJson(target);
    assertEquals(target.toJson(), result);
  }

// com.google.gson.functional.InheritanceTest::testSubClassSerialization
  public void testSubClassSerialization() throws Exception {
    SubTypeOfNested target = new SubTypeOfNested(new BagOfPrimitives(10, 20, false, "stringValue"),
        new BagOfPrimitives(30, 40, true, "stringValue"));
    assertEquals(target.getExpectedJson(), gson.toJson(target));
  }

// com.google.gson.functional.InheritanceTest::testSubClassDeserialization
  public void testSubClassDeserialization() throws Exception {
    String json = "{\"value\":5,\"primitive1\":{\"longValue\":10,\"intValue\":20,"
        + "\"booleanValue\":false,\"stringValue\":\"stringValue\"},\"primitive2\":"
        + "{\"longValue\":30,\"intValue\":40,\"booleanValue\":true,"
        + "\"stringValue\":\"stringValue\"}}";
    SubTypeOfNested target = gson.fromJson(json, SubTypeOfNested.class);
    assertEquals(json, target.getExpectedJson());
  }

// com.google.gson.functional.InheritanceTest::testClassWithBaseFieldSerialization
  public void testClassWithBaseFieldSerialization() {
    ClassWithBaseField sub = new ClassWithBaseField(new Sub());
    JsonObject json = (JsonObject) gson.toJsonTree(sub);
    JsonElement base = json.getAsJsonObject().get(ClassWithBaseField.FIELD_KEY);
    assertEquals(Sub.SUB_NAME, base.getAsJsonObject().get(Sub.SUB_FIELD_KEY).getAsString());
  }

// com.google.gson.functional.InheritanceTest::testClassWithBaseArrayFieldSerialization
  public void testClassWithBaseArrayFieldSerialization() {
    Base[] baseClasses = new Base[]{ new Sub(), new Sub()};
    ClassWithBaseArrayField sub = new ClassWithBaseArrayField(baseClasses);
    JsonObject json = gson.toJsonTree(sub).getAsJsonObject();
    JsonArray bases = json.get(ClassWithBaseArrayField.FIELD_KEY).getAsJsonArray();
    for (JsonElement element : bases) { 
      assertEquals(Sub.SUB_NAME, element.getAsJsonObject().get(Sub.SUB_FIELD_KEY).getAsString());
    }
  }

// com.google.gson.functional.InheritanceTest::testBaseSerializedAsSub
  public void testBaseSerializedAsSub() {
    Base base = new Sub();
    JsonObject json = gson.toJsonTree(base).getAsJsonObject();
    assertEquals(Sub.SUB_NAME, json.get(Sub.SUB_FIELD_KEY).getAsString());
  }

// com.google.gson.functional.InheritanceTest::testBaseSerializedAsSubForToJsonMethod
  public void testBaseSerializedAsSubForToJsonMethod() {
    Base base = new Sub();
    String json = gson.toJson(base);
    assertTrue(json.contains(Sub.SUB_NAME));
  }

// com.google.gson.functional.InheritanceTest::testBaseSerializedAsBaseWhenSpecifiedWithExplicitType
  public void testBaseSerializedAsBaseWhenSpecifiedWithExplicitType() {
    Base base = new Sub();
    JsonObject json = gson.toJsonTree(base, Base.class).getAsJsonObject();
    assertEquals(Base.BASE_NAME, json.get(Base.BASE_FIELD_KEY).getAsString());
    assertNull(json.get(Sub.SUB_FIELD_KEY));
  }

// com.google.gson.functional.InheritanceTest::testBaseSerializedAsBaseWhenSpecifiedWithExplicitTypeForToJsonMethod
  public void testBaseSerializedAsBaseWhenSpecifiedWithExplicitTypeForToJsonMethod() {
    Base base = new Sub();
    String json = gson.toJson(base, Base.class);
    assertTrue(json.contains(Base.BASE_NAME));
    assertFalse(json.contains(Sub.SUB_FIELD_KEY));
  }

// com.google.gson.functional.InheritanceTest::testBaseSerializedAsSubWhenSpecifiedWithExplicitType
  public void testBaseSerializedAsSubWhenSpecifiedWithExplicitType() {
    Base base = new Sub();
    JsonObject json = gson.toJsonTree(base, Sub.class).getAsJsonObject();
    assertEquals(Sub.SUB_NAME, json.get(Sub.SUB_FIELD_KEY).getAsString());
  }

// com.google.gson.functional.InheritanceTest::testBaseSerializedAsSubWhenSpecifiedWithExplicitTypeForToJsonMethod
  public void testBaseSerializedAsSubWhenSpecifiedWithExplicitTypeForToJsonMethod() {
    Base base = new Sub();
    String json = gson.toJson(base, Sub.class);
    assertTrue(json.contains(Sub.SUB_NAME));
  }

// com.google.gson.functional.InheritanceTest::testSubInterfacesOfCollectionSerialization
  public void testSubInterfacesOfCollectionSerialization() throws Exception {
    List<Integer> list = new LinkedList<Integer>();
    list.add(0);
    list.add(1);
    list.add(2);
    list.add(3);
    Queue<Long> queue = new LinkedList<Long>();
    queue.add(0L);
    queue.add(1L);
    queue.add(2L);
    queue.add(3L);
    Set<Float> set = new TreeSet<Float>();
    set.add(0.1F);
    set.add(0.2F);
    set.add(0.3F);
    set.add(0.4F);
    SortedSet<Character> sortedSet = new TreeSet<Character>();
    sortedSet.add('a');
    sortedSet.add('b');
    sortedSet.add('c');
    sortedSet.add('d');
    ClassWithSubInterfacesOfCollection target =
        new ClassWithSubInterfacesOfCollection(list, queue, set, sortedSet);
    assertEquals(target.getExpectedJson(), gson.toJson(target));
  }

// com.google.gson.functional.InheritanceTest::testSubInterfacesOfCollectionDeserialization
  public void testSubInterfacesOfCollectionDeserialization() throws Exception {
    String json = "{\"list\":[0,1,2,3],\"queue\":[0,1,2,3],\"set\":[0.1,0.2,0.3,0.4],"
        + "\"sortedSet\":[\"a\",\"b\",\"c\",\"d\"]"
        + "}";
    ClassWithSubInterfacesOfCollection target = 
      gson.fromJson(json, ClassWithSubInterfacesOfCollection.class);
    assertTrue(target.listContains(0, 1, 2, 3));
    assertTrue(target.queueContains(0, 1, 2, 3));
    assertTrue(target.setContains(0.1F, 0.2F, 0.3F, 0.4F));
    assertTrue(target.sortedSetContains('a', 'b', 'c', 'd'));
  }

// com.google.gson.functional.InstanceCreatorTest::testInstanceCreatorReturnsBaseType
  public void testInstanceCreatorReturnsBaseType() {
    Gson gson = new GsonBuilder()
      .registerTypeAdapter(Base.class, new InstanceCreator<Base>() {
       public Base createInstance(Type type) {
         return new Base();
       }
      })
      .create();
    String json = "{baseName:'BaseRevised',subName:'Sub'}";
    Base base = gson.fromJson(json, Base.class);
    assertEquals("BaseRevised", base.baseName);
  }

// com.google.gson.functional.InstanceCreatorTest::testInstanceCreatorReturnsSubTypeForTopLevelObject
  public void testInstanceCreatorReturnsSubTypeForTopLevelObject() {
    Gson gson = new GsonBuilder()
    .registerTypeAdapter(Base.class, new InstanceCreator<Base>() {
      public Base createInstance(Type type) {
        return new Sub();
      }
    })
    .create();

    String json = "{baseName:'Base',subName:'SubRevised'}";
    Base base = gson.fromJson(json, Base.class);
    assertTrue(base instanceof Sub);

    Sub sub = (Sub) base;
    assertFalse("SubRevised".equals(sub.subName));
    assertEquals(Sub.SUB_NAME, sub.subName);
  }

// com.google.gson.functional.InstanceCreatorTest::testInstanceCreatorReturnsSubTypeForField
  public void testInstanceCreatorReturnsSubTypeForField() {
    Gson gson = new GsonBuilder()
    .registerTypeAdapter(Base.class, new InstanceCreator<Base>() {
      public Base createInstance(Type type) {
        return new Sub();
      }
    })
    .create();
    String json = "{base:{baseName:'Base',subName:'SubRevised'}}";
    ClassWithBaseField target = gson.fromJson(json, ClassWithBaseField.class);
    assertTrue(target.base instanceof Sub);
    assertEquals("SubRevised", ((Sub)target.base).subName);
  }

// com.google.gson.functional.InterfaceTest::testSerializingObjectImplementingInterface
  public void testSerializingObjectImplementingInterface() throws Exception {
    assertEquals(OBJ_JSON, gson.toJson(obj));
  }

// com.google.gson.functional.InterfaceTest::testSerializingInterfaceObjectField
  public void testSerializingInterfaceObjectField() throws Exception {
    TestObjectWrapper objWrapper = new TestObjectWrapper(obj);
    assertEquals("{\"obj\":" + OBJ_JSON + "}", gson.toJson(objWrapper));
  }

// com.google.gson.functional.JsonParserTest::testDeserializingCustomTree
  public void testDeserializingCustomTree() {
    JsonObject obj = new JsonObject();
    obj.addProperty("stringValue", "foo");
    obj.addProperty("intValue", 11);
    BagOfPrimitives target = gson.fromJson(obj, BagOfPrimitives.class);
    assertEquals(11, target.intValue);
    assertEquals("foo", target.stringValue);
  }

// com.google.gson.functional.JsonParserTest::testBadTypeForDeserializingCustomTree
  public void testBadTypeForDeserializingCustomTree() {
    JsonObject obj = new JsonObject();
    obj.addProperty("stringValue", "foo");
    obj.addProperty("intValue", 11);
    JsonArray array = new JsonArray();
    array.add(obj);
    try {
      gson.fromJson(array, BagOfPrimitives.class);
      fail("BagOfPrimitives is not an array");
    } catch (JsonParseException expected) { }
  }

// com.google.gson.functional.JsonParserTest::testBadFieldTypeForCustomDeserializerCustomTree
  public void testBadFieldTypeForCustomDeserializerCustomTree() {
    JsonArray array = new JsonArray();
    array.add(new JsonPrimitive("blah"));
    JsonObject obj = new JsonObject();
    obj.addProperty("stringValue", "foo");
    obj.addProperty("intValue", 11);
    obj.add("longValue", array);

    try {
      gson.fromJson(obj, BagOfPrimitives.class);
      fail("BagOfPrimitives is not an array");
    } catch (JsonParseException expected) { }
  }

// com.google.gson.functional.JsonParserTest::testBadFieldTypeForDeserializingCustomTree
  public void testBadFieldTypeForDeserializingCustomTree() {
    JsonArray array = new JsonArray();
    array.add(new JsonPrimitive("blah"));
    JsonObject primitive1 = new JsonObject();
    primitive1.addProperty("string", "foo");
    primitive1.addProperty("intValue", 11);

    JsonObject obj = new JsonObject();
    obj.add("primitive1", primitive1);
    obj.add("primitive2", array);
    
    try {
      gson.fromJson(obj, Nested.class);
      fail("Nested has field BagOfPrimitives which is not an array");
    } catch (JsonParseException expected) { }
  }

// com.google.gson.functional.JsonParserTest::testChangingCustomTreeAndDeserializing
  public void testChangingCustomTreeAndDeserializing() {
    StringReader json = 
      new StringReader("{'stringValue':'no message','intValue':10,'longValue':20}");
    JsonObject obj = (JsonObject) new JsonParser().parse(json);
    obj.remove("stringValue");
    obj.addProperty("stringValue", "fooBar");
    BagOfPrimitives target = gson.fromJson(obj, BagOfPrimitives.class);
    assertEquals(10, target.intValue);
    assertEquals(20, target.longValue);
    assertEquals("fooBar", target.stringValue);
  }

// com.google.gson.functional.JsonTreeTest::testToJsonTree
  public void testToJsonTree() {
    BagOfPrimitives bag = new BagOfPrimitives(10L, 5, false, "foo");
    JsonElement json = gson.toJsonTree(bag);
    assertTrue(json.isJsonObject());
    JsonObject obj = json.getAsJsonObject();
    Set<Entry<String, JsonElement>> children = obj.entrySet();
    assertEquals(4, children.size());
    assertContains(obj, new JsonPrimitive(10L));
    assertContains(obj, new JsonPrimitive(5));
    assertContains(obj, new JsonPrimitive(false));
    assertContains(obj, new JsonPrimitive("foo"));
  }

// com.google.gson.functional.JsonTreeTest::testToJsonTreeObjectType
  public void testToJsonTreeObjectType() {
    SubTypeOfBagOfPrimitives bag = new SubTypeOfBagOfPrimitives(10L, 5, false, "foo", 1.4F);
    JsonElement json = gson.toJsonTree(bag, BagOfPrimitives.class);
    assertTrue(json.isJsonObject());
    JsonObject obj = json.getAsJsonObject();
    Set<Entry<String, JsonElement>> children = obj.entrySet();
    assertEquals(4, children.size());
    assertContains(obj, new JsonPrimitive(10L));
    assertContains(obj, new JsonPrimitive(5));
    assertContains(obj, new JsonPrimitive(false));
    assertContains(obj, new JsonPrimitive("foo"));
  }

// com.google.gson.functional.JsonTreeTest::testJsonTreeToString
  public void testJsonTreeToString() {
    SubTypeOfBagOfPrimitives bag = new SubTypeOfBagOfPrimitives(10L, 5, false, "foo", 1.4F);
    String json1 = gson.toJson(bag);
    JsonElement jsonElement = gson.toJsonTree(bag, SubTypeOfBagOfPrimitives.class);
    String json2 = gson.toJson(jsonElement);
    assertEquals(json1, json2);
  }

// com.google.gson.functional.MapTest::testMapSerialization
  public void testMapSerialization() {
    Map<String, Integer> map = new LinkedHashMap<String, Integer>();
    map.put("a", 1);
    map.put("b", 2);
    Type typeOfMap = new TypeToken<Map<String, Integer>>() {}.getType();
    String json = gson.toJson(map, typeOfMap);
    assertTrue(json.contains("\"a\":1"));
    assertTrue(json.contains("\"b\":2"));
  }

// com.google.gson.functional.MapTest::testMapDeserialization
  public void testMapDeserialization() {
    String json = "{\"a\":1,\"b\":2}";
    Type typeOfMap = new TypeToken<Map<String,Integer>>(){}.getType();
    Map<String, Integer> target = gson.fromJson(json, typeOfMap);
    assertEquals(1, target.get("a").intValue());
    assertEquals(2, target.get("b").intValue());
  }

// com.google.gson.functional.MapTest::testRawMapSerialization
  public void testRawMapSerialization() {
    Map map = new LinkedHashMap();
    map.put("a", 1);
    map.put("b", "string");
    String json = gson.toJson(map);
    assertTrue(json.contains("\"a\":1"));
    assertTrue(json.contains("\"b\":\"string\""));    
  }

// com.google.gson.functional.MapTest::testMapSerializationEmpty
  public void testMapSerializationEmpty() {
    Map<String, Integer> map = new LinkedHashMap<String, Integer>();
    Type typeOfMap = new TypeToken<Map<String, Integer>>() {}.getType();
    String json = gson.toJson(map, typeOfMap);
    assertEquals("{}", json);
  }

// com.google.gson.functional.MapTest::testMapDeserializationEmpty
  public void testMapDeserializationEmpty() {
    Type typeOfMap = new TypeToken<Map<String, Integer>>() {}.getType();
    Map<String, Integer> map = gson.fromJson("{}", typeOfMap);
    assertTrue(map.isEmpty());
  }

// com.google.gson.functional.MapTest::testMapSerializationWithNullValue
  public void testMapSerializationWithNullValue() {
    Map<String, Integer> map = new LinkedHashMap<String, Integer>();
    map.put("abc", null);
    Type typeOfMap = new TypeToken<Map<String, Integer>>() {}.getType();
    String json = gson.toJson(map, typeOfMap);
    
    
    assertEquals("{}", json);
  }

// com.google.gson.functional.MapTest::testMapDeserializationWithNullValue
  public void testMapDeserializationWithNullValue() {
    Type typeOfMap = new TypeToken<Map<String, Integer>>() {}.getType();
    Map<String, Integer> map = gson.fromJson("{\"abc\":null}", typeOfMap);
    assertEquals(1, map.size());
    assertNull(map.get("abc"));
  }

// com.google.gson.functional.MapTest::testMapSerializationWithNullValueButSerializeNulls
  public void testMapSerializationWithNullValueButSerializeNulls() {
    gson = new GsonBuilder().serializeNulls().create();
    Map<String, Integer> map = new LinkedHashMap<String, Integer>();
    map.put("abc", null);
    Type typeOfMap = new TypeToken<Map<String, Integer>>() {}.getType();
    String json = gson.toJson(map, typeOfMap);

    assertEquals("{\"abc\":null}", json);
  }

// com.google.gson.functional.MapTest::testMapSerializationWithNullKey
  public void testMapSerializationWithNullKey() {
    Map<String, Integer> map = new LinkedHashMap<String, Integer>();
    map.put(null, 123);
    Type typeOfMap = new TypeToken<Map<String, Integer>>() {}.getType();
    String json = gson.toJson(map, typeOfMap);

    assertEquals("{\"null\":123}", json);
  }

// com.google.gson.functional.MapTest::testMapDeserializationWithNullKey
  public void testMapDeserializationWithNullKey() {
    Type typeOfMap = new TypeToken<Map<String, Integer>>() {}.getType();
    Map<String, Integer> map = gson.fromJson("{\"null\":123}", typeOfMap);
    assertEquals(1, map.size());
    assertNull(map.get(null));
  }

// com.google.gson.functional.MapTest::testMapSerializationWithIntegerKeys
  public void testMapSerializationWithIntegerKeys() {
    Map<Integer, String> map = new LinkedHashMap<Integer, String>();
    map.put(123, "456");
    Type typeOfMap = new TypeToken<Map<Integer, String>>() {}.getType();
    String json = gson.toJson(map, typeOfMap);

    assertEquals("{\"123\":\"456\"}", json);
  }

// com.google.gson.functional.MapTest::testMapDeserializationWithIntegerKeys
  public void testMapDeserializationWithIntegerKeys() {
    Type typeOfMap = new TypeToken<Map<Integer, String>>() {}.getType();
    Map<Integer, String> map = gson.fromJson("{\"123\":\"456\"}", typeOfMap);
    assertEquals(1, map.size());
    assertTrue(map.containsKey(123));
    assertEquals("456", map.get(123));
  }

// com.google.gson.functional.MapTest::testParameterizedMapSubclassSerialization
  public void testParameterizedMapSubclassSerialization() {
    MyParameterizedMap<String, String> map = new MyParameterizedMap<String, String>();
    map.put("a", "b");
    Type type = new TypeToken<MyParameterizedMap<String, String>>() {}.getType();
    String json = gson.toJson(map, type);
    assertTrue(json.contains("\"a\":\"b\""));
  }

// com.google.gson.functional.MapTest::testParameterizedMapSubclassDeserialization
  public void testParameterizedMapSubclassDeserialization() {
    Type type = new TypeToken<MyParameterizedMap<String, Integer>>() {}.getType();
    Gson gson = new GsonBuilder().registerTypeAdapter(type, 
        new InstanceCreator<MyParameterizedMap>() {
      public MyParameterizedMap createInstance(Type type) {
        return new MyParameterizedMap();
      }      
    }).create();
    String json = "{\"a\":1,\"b\":2}";
    MyParameterizedMap<String, Integer> map = gson.fromJson(json, type);
    assertEquals(1, map.get("a").intValue()); 
    assertEquals(2, map.get("b").intValue()); 
  }

// com.google.gson.functional.MapTest::testMapSubclassSerialization
  public void testMapSubclassSerialization() {
    MyMap map = new MyMap();
    map.put("a", "b");
    String json = gson.toJson(map, MyMap.class);
    assertTrue(json.contains("\"a\":\"b\""));
  }

// com.google.gson.functional.MapTest::testMapStandardSubclassDeserialization
  public void testMapStandardSubclassDeserialization() {
    String json = "{a:'1',b:'2'}";
    Type type = new TypeToken<LinkedHashMap<String, String>>() {}.getType();
    LinkedHashMap<String, Integer> map = gson.fromJson(json, type);
    assertEquals("1", map.get("a")); 
    assertEquals("2", map.get("b")); 
  }

// com.google.gson.functional.MapTest::testMapSerializationWithNullValues
  public void testMapSerializationWithNullValues() {
    ClassWithAMap target = new ClassWithAMap();
    target.map.put("name1", null);
    target.map.put("name2", "value2");
    String json = gson.toJson(target);
    assertFalse(json.contains("name1"));
    assertTrue(json.contains("name2"));
  }

// com.google.gson.functional.MapTest::testMapSerializationWithNullValuesSerialized
  public void testMapSerializationWithNullValuesSerialized() {
    Gson gson = new GsonBuilder().serializeNulls().create();
    ClassWithAMap target = new ClassWithAMap();
    target.map.put("name1", null);
    target.map.put("name2", "value2");
    String json = gson.toJson(target);
    assertTrue(json.contains("name1"));
    assertTrue(json.contains("name2"));
  }

// com.google.gson.functional.MapTest::testMapSerializationWithWildcardValues
  public void testMapSerializationWithWildcardValues() {
    Map<String, ? extends Collection<? extends Integer>> map =
        new LinkedHashMap<String, Collection<Integer>>();
    map.put("test", null);
    Type typeOfMap = 
        new TypeToken<Map<String, ? extends Collection<? extends Integer>>>() {}.getType();
    String json = gson.toJson(map, typeOfMap);

    assertEquals("{}", json);
  }

// com.google.gson.functional.MapTest::testMapDeserializationWithWildcardValues
  public void testMapDeserializationWithWildcardValues() {
    Type typeOfMap = new TypeToken<Map<String, ? extends Long>>() {}.getType();
    Map<String, ? extends Long> map = gson.fromJson("{\"test\":123}", typeOfMap);
    assertEquals(1, map.size());
    assertEquals(new Long(123L), map.get("test"));
  }

// com.google.gson.functional.MapTest::testMapOfMapSerialization
  public void testMapOfMapSerialization() {
    Map<String, Map<String, String>> map = new HashMap<String, Map<String, String>>();
    Map<String, String> nestedMap = new HashMap<String, String>();
    nestedMap.put("1", "1");
    nestedMap.put("2", "2");
    map.put("nestedMap", nestedMap);
    String json = gson.toJson(map);
    assertTrue(json.contains("nestedMap"));
    assertTrue(json.contains("\"1\":\"1\""));
    assertTrue(json.contains("\"2\":\"2\""));
  }

// com.google.gson.functional.MapTest::testMapOfMapDeserialization
  public void testMapOfMapDeserialization() {
    String json = "{nestedMap:{'2':'2','1':'1'}}";
    Type type = new TypeToken<Map<String, Map<String, String>>>(){}.getType();
    Map<String, Map<String, String>> map = gson.fromJson(json, type);
    Map<String, String> nested = map.get("nestedMap");
    assertEquals("1", nested.get("1"));
    assertEquals("2", nested.get("2"));
  }

// com.google.gson.functional.MapTest::testMapWithQuotes
  public void testMapWithQuotes() {
    Map<String, String> map = new HashMap<String, String>();
    map.put("a\"b", "c\"d");
    String json = gson.toJson(map);
    assertEquals("{\"a\\\"b\":\"c\\\"d\"}", json);
  }

// com.google.gson.functional.MapTest::testWriteMapsWithEmptyStringKey
  public void testWriteMapsWithEmptyStringKey() {
    Map<String, Boolean> map = new HashMap<String, Boolean>();
    map.put("", true);
    assertEquals("{\"\":true}", gson.toJson(map));

  }

// com.google.gson.functional.MapTest::testReadMapsWithEmptyStringKey
  public void testReadMapsWithEmptyStringKey() {
    Map<String, Boolean> map = gson.fromJson("{\"\":true}", new TypeToken<Map<String, Boolean>>() {}.getType());
    assertEquals(Boolean.TRUE, map.get(""));
  }

// com.google.gson.functional.MapTest::testSerializeMaps
  public void testSerializeMaps() {
    Map<String, Object> map = new LinkedHashMap<String, Object>();
    map.put("a", 12);
    map.put("b", null);
    map.put("c", new HashMap<String, Object>());

    assertEquals("{\"a\":12,\"b\":null,\"c\":{}}",
        new GsonBuilder().serializeNulls().create().toJson(map));
    assertEquals("{\"a\":12,\"b\":null,\"c\":{}}",
        new GsonBuilder().serializeNulls().create().toJson(map));
    assertEquals("{\"a\":12,\"c\":{}}",
        new GsonBuilder().create().toJson(map));
    assertEquals("{\"a\":12,\"c\":{}}",
        new GsonBuilder().create().toJson(map));
  }

// com.google.gson.functional.NamingPolicyTest::testGsonWithNonDefaultFieldNamingPolicySerialization
  public void testGsonWithNonDefaultFieldNamingPolicySerialization() {
    Gson gson = builder.setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();
    StringWrapper target = new StringWrapper("blah");
    assertEquals("{\"SomeConstantStringInstanceField\":\""
        + target.someConstantStringInstanceField + "\"}", gson.toJson(target));
  }

// com.google.gson.functional.NamingPolicyTest::testGsonWithNonDefaultFieldNamingPolicyDeserialiation
  public void testGsonWithNonDefaultFieldNamingPolicyDeserialiation() {
    Gson gson = builder.setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();
    String target = "{\"SomeConstantStringInstanceField\":\"someValue\"}";
    StringWrapper deserializedObject = gson.fromJson(target, StringWrapper.class);
    assertEquals("someValue", deserializedObject.someConstantStringInstanceField);
  }

// com.google.gson.functional.NamingPolicyTest::testGsonWithLowerCaseDashPolicySerialization
  public void testGsonWithLowerCaseDashPolicySerialization() {
    Gson gson = builder.setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_DASHES).create();
    StringWrapper target = new StringWrapper("blah");
    assertEquals("{\"some-constant-string-instance-field\":\""
        + target.someConstantStringInstanceField + "\"}", gson.toJson(target));
  }

// com.google.gson.functional.NamingPolicyTest::testGsonWithLowerCaseDashPolicyDeserialiation
  public void testGsonWithLowerCaseDashPolicyDeserialiation() {
    Gson gson = builder.setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_DASHES).create();
    String target = "{\"some-constant-string-instance-field\":\"someValue\"}";
    StringWrapper deserializedObject = gson.fromJson(target, StringWrapper.class);
    assertEquals("someValue", deserializedObject.someConstantStringInstanceField);
  }

// com.google.gson.functional.NamingPolicyTest::testGsonWithLowerCaseUnderscorePolicySerialization
  public void testGsonWithLowerCaseUnderscorePolicySerialization() {
    Gson gson = builder.setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
        .create();
    StringWrapper target = new StringWrapper("blah");
    assertEquals("{\"some_constant_string_instance_field\":\""
        + target.someConstantStringInstanceField + "\"}", gson.toJson(target));
  }

// com.google.gson.functional.NamingPolicyTest::testGsonWithLowerCaseUnderscorePolicyDeserialiation
  public void testGsonWithLowerCaseUnderscorePolicyDeserialiation() {
    Gson gson = builder.setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
        .create();
    String target = "{\"some_constant_string_instance_field\":\"someValue\"}";
    StringWrapper deserializedObject = gson.fromJson(target, StringWrapper.class);
    assertEquals("someValue", deserializedObject.someConstantStringInstanceField);
  }

// com.google.gson.functional.NamingPolicyTest::testGsonWithSerializedNameFieldNamingPolicySerialization
  public void testGsonWithSerializedNameFieldNamingPolicySerialization() {
    Gson gson = builder.create();
    ClassWithSerializedNameFields expected = new ClassWithSerializedNameFields(5, 6);
    String actual = gson.toJson(expected);
    assertEquals(expected.getExpectedJson(), actual);
  }

// com.google.gson.functional.NamingPolicyTest::testGsonWithSerializedNameFieldNamingPolicyDeserialization
  public void testGsonWithSerializedNameFieldNamingPolicyDeserialization() {
    Gson gson = builder.create();
    ClassWithSerializedNameFields expected = new ClassWithSerializedNameFields(5, 7);
    ClassWithSerializedNameFields actual =
        gson.fromJson(expected.getExpectedJson(), ClassWithSerializedNameFields.class);
    assertEquals(expected.f, actual.f);
  }

// com.google.gson.functional.NamingPolicyTest::testGsonDuplicateNameUsingSerializedNameFieldNamingPolicySerialization
  public void testGsonDuplicateNameUsingSerializedNameFieldNamingPolicySerialization() {
    Gson gson = builder.create();
    ClassWithDuplicateFields target = new ClassWithDuplicateFields(10);
    String actual = gson.toJson(target);
    assertEquals("{\"a\":10}", actual);
    
    target = new ClassWithDuplicateFields(3.0D);
    actual = gson.toJson(target);
    assertEquals("{\"a\":3.0}", actual);
  }

// com.google.gson.functional.NamingPolicyTest::testGsonWithUpperCamelCaseSpacesPolicySerialiation
  public void testGsonWithUpperCamelCaseSpacesPolicySerialiation() {
    Gson gson = builder.setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE_WITH_SPACES)
        .create();
    StringWrapper target = new StringWrapper("blah");
    assertEquals("{\"Some Constant String Instance Field\":\""
        + target.someConstantStringInstanceField + "\"}", gson.toJson(target));
  }

// com.google.gson.functional.NamingPolicyTest::testGsonWithUpperCamelCaseSpacesPolicyDeserialiation
  public void testGsonWithUpperCamelCaseSpacesPolicyDeserialiation() {
    Gson gson = builder.setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE_WITH_SPACES)
        .create();
    String target = "{\"Some Constant String Instance Field\":\"someValue\"}";
    StringWrapper deserializedObject = gson.fromJson(target, StringWrapper.class);
    assertEquals("someValue", deserializedObject.someConstantStringInstanceField);
  }

// com.google.gson.functional.NamingPolicyTest::testDeprecatedNamingStrategy
  public void testDeprecatedNamingStrategy() throws Exception {
    Gson gson = builder.setFieldNamingStrategy(new UpperCaseNamingStrategy()).create();
    ClassWithDuplicateFields target = new ClassWithDuplicateFields(10);
    String actual = gson.toJson(target);
    assertEquals("{\"A\":10}", actual);
  }

// com.google.gson.functional.NullObjectAndFieldTest::testTopLevelNullObjectSerialization
  public void testTopLevelNullObjectSerialization() {
    Gson gson = gsonBuilder.create();
    String actual = gson.toJson(null);
    assertEquals("null", actual);

    actual = gson.toJson(null, String.class);
    assertEquals("null", actual);
  }

// com.google.gson.functional.NullObjectAndFieldTest::testTopLevelNullObjectDeserialization
  public void testTopLevelNullObjectDeserialization() throws Exception {
    Gson gson = gsonBuilder.create();
    String actual = gson.fromJson("null", String.class);
    assertNull(actual);
  }

// com.google.gson.functional.NullObjectAndFieldTest::testExplicitSerializationOfNulls
  public void testExplicitSerializationOfNulls() {
    Gson gson = gsonBuilder.create();
    ClassWithObjects target = new ClassWithObjects(null);
    String actual = gson.toJson(target);
    String expected = "{\"bag\":null}";
    assertEquals(expected, actual);
  }

// com.google.gson.functional.NullObjectAndFieldTest::testExplicitDeserializationOfNulls
  public void testExplicitDeserializationOfNulls() throws Exception {
    Gson gson = gsonBuilder.create();
    ClassWithObjects target = gson.fromJson("{\"bag\":null}", ClassWithObjects.class);
    assertNull(target.bag);
  }

// com.google.gson.functional.NullObjectAndFieldTest::testExplicitSerializationOfNullArrayMembers
  public void testExplicitSerializationOfNullArrayMembers() {
    Gson gson = gsonBuilder.create();
    ClassWithMembers target = new ClassWithMembers();
    String json = gson.toJson(target);
    assertTrue(json.contains("\"array\":null"));
  }

// com.google.gson.functional.NullObjectAndFieldTest::testNullWrappedPrimitiveMemberSerialization
  public void testNullWrappedPrimitiveMemberSerialization() {
    Gson gson = gsonBuilder.serializeNulls().create();
    ClassWithNullWrappedPrimitive target = new ClassWithNullWrappedPrimitive();
    String json = gson.toJson(target);
    assertTrue(json.contains("\"value\":null"));
  }

// com.google.gson.functional.NullObjectAndFieldTest::testNullWrappedPrimitiveMemberDeserialization
  public void testNullWrappedPrimitiveMemberDeserialization() {
    Gson gson = gsonBuilder.create();
    String json = "{'value':null}";
    ClassWithNullWrappedPrimitive target = gson.fromJson(json, ClassWithNullWrappedPrimitive.class);
    assertNull(target.value);
  }

// com.google.gson.functional.NullObjectAndFieldTest::testExplicitSerializationOfNullCollectionMembers
  public void testExplicitSerializationOfNullCollectionMembers() {
    Gson gson = gsonBuilder.create();
    ClassWithMembers target = new ClassWithMembers();
    String json = gson.toJson(target);
    assertTrue(json.contains("\"col\":null"));
  }

// com.google.gson.functional.NullObjectAndFieldTest::testExplicitSerializationOfNullStringMembers
  public void testExplicitSerializationOfNullStringMembers() {
    Gson gson = gsonBuilder.create();
    ClassWithMembers target = new ClassWithMembers();
    String json = gson.toJson(target);
    assertTrue(json.contains("\"str\":null"));
  }

// com.google.gson.functional.NullObjectAndFieldTest::testCustomSerializationOfNulls
  public void testCustomSerializationOfNulls() {
    gsonBuilder.registerTypeAdapter(ClassWithObjects.class, new ClassWithObjectsSerializer());
    Gson gson = gsonBuilder.create();
    ClassWithObjects target = new ClassWithObjects(new BagOfPrimitives());
    String actual = gson.toJson(target);
    String expected = "{\"bag\":null}";
    assertEquals(expected, actual);
  }

// com.google.gson.functional.NullObjectAndFieldTest::testPrintPrintingObjectWithNulls
  public void testPrintPrintingObjectWithNulls() throws Exception {
    gsonBuilder = new GsonBuilder();
    Gson gson = gsonBuilder.create();
    String result = gson.toJson(new ClassWithMembers());
    assertEquals("{}", result);

    gson = gsonBuilder.serializeNulls().create();
    result = gson.toJson(new ClassWithMembers());
    assertTrue(result.contains("\"str\":null"));
  }

// com.google.gson.functional.NullObjectAndFieldTest::testPrintPrintingArraysWithNulls
  public void testPrintPrintingArraysWithNulls() throws Exception {
    gsonBuilder = new GsonBuilder();
    Gson gson = gsonBuilder.create();
    String result = gson.toJson(new String[] { "1", null, "3" });
    assertEquals("[\"1\",null,\"3\"]", result);

    gson = gsonBuilder.serializeNulls().create();
    result = gson.toJson(new String[] { "1", null, "3" });
    assertEquals("[\"1\",null,\"3\"]", result);
  }

// com.google.gson.functional.NullObjectAndFieldTest::testExplicitNullSetsFieldToNullDuringDeserialization
  public void testExplicitNullSetsFieldToNullDuringDeserialization() {
    Gson gson = new Gson();
    String json = "{value:null}";
    ObjectWithField obj = gson.fromJson(json, ObjectWithField.class);
    assertNull(obj.value);    
  }

// com.google.gson.functional.NullObjectAndFieldTest::testCustomTypeAdapterPassesNullSerialization
  public void testCustomTypeAdapterPassesNullSerialization() {
    Gson gson = new GsonBuilder()
        .registerTypeAdapter(ObjectWithField.class, new JsonSerializer<ObjectWithField>() {
          public JsonElement serialize(ObjectWithField src, Type typeOfSrc,
              JsonSerializationContext context) {
            return context.serialize(null);
          }
        }).create();
    ObjectWithField target = new ObjectWithField();
    target.value = "value1";
    String json = gson.toJson(target);
    assertFalse(json.contains("value1"));
  }

// com.google.gson.functional.NullObjectAndFieldTest::testCustomTypeAdapterPassesNullDesrialization
  public void testCustomTypeAdapterPassesNullDesrialization() {
    Gson gson = new GsonBuilder()
        .registerTypeAdapter(ObjectWithField.class, new JsonDeserializer<ObjectWithField>() {
          public ObjectWithField deserialize(JsonElement json, Type type,
              JsonDeserializationContext context) {
            return context.deserialize(null, type);
          }
        }).create();
    String json = "{value:'value1'}";
    ObjectWithField target = gson.fromJson(json, ObjectWithField.class);
    assertNull(target);
  }

// com.google.gson.functional.ObjectTest::testJsonInSingleQuotesDeserialization
  public void testJsonInSingleQuotesDeserialization() {
    String json = "{'stringValue':'no message','intValue':10,'longValue':20}";
    BagOfPrimitives target = gson.fromJson(json, BagOfPrimitives.class);
    assertEquals("no message", target.stringValue);
    assertEquals(10, target.intValue);
    assertEquals(20, target.longValue);
  }

// com.google.gson.functional.ObjectTest::testJsonInMixedQuotesDeserialization
  public void testJsonInMixedQuotesDeserialization() {
    String json = "{\"stringValue\":'no message','intValue':10,'longValue':20}";
    BagOfPrimitives target = gson.fromJson(json, BagOfPrimitives.class);
    assertEquals("no message", target.stringValue);
    assertEquals(10, target.intValue);
    assertEquals(20, target.longValue);
  }

// com.google.gson.functional.ObjectTest::testBagOfPrimitivesSerialization
  public void testBagOfPrimitivesSerialization() throws Exception {
    BagOfPrimitives target = new BagOfPrimitives(10, 20, false, "stringValue");
    assertEquals(target.getExpectedJson(), gson.toJson(target));
  }

// com.google.gson.functional.ObjectTest::testBagOfPrimitivesDeserialization
  public void testBagOfPrimitivesDeserialization() throws Exception {
    BagOfPrimitives src = new BagOfPrimitives(10, 20, false, "stringValue");
    String json = src.getExpectedJson();
    BagOfPrimitives target = gson.fromJson(json, BagOfPrimitives.class);
    assertEquals(json, target.getExpectedJson());
  }

// com.google.gson.functional.ObjectTest::testBagOfPrimitiveWrappersSerialization
  public void testBagOfPrimitiveWrappersSerialization() throws Exception {
    BagOfPrimitiveWrappers target = new BagOfPrimitiveWrappers(10L, 20, false);
    assertEquals(target.getExpectedJson(), gson.toJson(target));
  }

// com.google.gson.functional.ObjectTest::testBagOfPrimitiveWrappersDeserialization
  public void testBagOfPrimitiveWrappersDeserialization() throws Exception {
    BagOfPrimitiveWrappers target = new BagOfPrimitiveWrappers(10L, 20, false);
    String jsonString = target.getExpectedJson();
    target = gson.fromJson(jsonString, BagOfPrimitiveWrappers.class);
    assertEquals(jsonString, target.getExpectedJson());
  }

// com.google.gson.functional.ObjectTest::testClassWithTransientFieldsSerialization
  public void testClassWithTransientFieldsSerialization() throws Exception {
    ClassWithTransientFields<Long> target = new ClassWithTransientFields<Long>(1L);
    assertEquals(target.getExpectedJson(), gson.toJson(target));
  }

// com.google.gson.functional.ObjectTest::testClassWithTransientFieldsDeserialization
  public void testClassWithTransientFieldsDeserialization() throws Exception {
    String json = "{\"longValue\":[1]}";
    ClassWithTransientFields target = gson.fromJson(json, ClassWithTransientFields.class);
    assertEquals(json, target.getExpectedJson());
  }

// com.google.gson.functional.ObjectTest::testClassWithTransientFieldsDeserializationTransientFieldsPassedInJsonAreIgnored
  public void testClassWithTransientFieldsDeserializationTransientFieldsPassedInJsonAreIgnored()
      throws Exception {
    String json = "{\"transientLongValue\":1,\"longValue\":[1]}";
    ClassWithTransientFields target = gson.fromJson(json, ClassWithTransientFields.class);
    assertFalse(target.transientLongValue != 1);
  }

// com.google.gson.functional.ObjectTest::testClassWithNoFieldsSerialization
  public void testClassWithNoFieldsSerialization() throws Exception {
    assertEquals("{}", gson.toJson(new ClassWithNoFields()));
  }

// com.google.gson.functional.ObjectTest::testClassWithNoFieldsDeserialization
  public void testClassWithNoFieldsDeserialization() throws Exception {
    String json = "{}";
    ClassWithNoFields target = gson.fromJson(json, ClassWithNoFields.class);
    ClassWithNoFields expected = new ClassWithNoFields();
    assertEquals(expected, target);
  }

// com.google.gson.functional.ObjectTest::testNestedSerialization
  public void testNestedSerialization() throws Exception {
    Nested target = new Nested(new BagOfPrimitives(10, 20, false, "stringValue"),
       new BagOfPrimitives(30, 40, true, "stringValue"));
    assertEquals(target.getExpectedJson(), gson.toJson(target));
  }

// com.google.gson.functional.ObjectTest::testNestedDeserialization
  public void testNestedDeserialization() throws Exception {
    String json = "{\"primitive1\":{\"longValue\":10,\"intValue\":20,\"booleanValue\":false,"
        + "\"stringValue\":\"stringValue\"},\"primitive2\":{\"longValue\":30,\"intValue\":40,"
        + "\"booleanValue\":true,\"stringValue\":\"stringValue\"}}";
    Nested target = gson.fromJson(json, Nested.class);
    assertEquals(json, target.getExpectedJson());
  }

// com.google.gson.functional.ObjectTest::testNullSerialization
  public void testNullSerialization() throws Exception {
    assertEquals("", gson.toJson(null));
  }

// com.google.gson.functional.ObjectTest::testEmptyStringDeserialization
  public void testEmptyStringDeserialization() throws Exception {
    Object object = gson.fromJson("", Object.class);
    assertNull(object);
  }

// com.google.gson.functional.ObjectTest::testNullDeserialization
  public void testNullDeserialization() throws Exception {
    String myNullObject = null;
    Object object = gson.fromJson(myNullObject, Object.class);
    assertNull(object);
  }

// com.google.gson.functional.ObjectTest::testNullFieldsSerialization
  public void testNullFieldsSerialization() throws Exception {
    Nested target = new Nested(new BagOfPrimitives(10, 20, false, "stringValue"), null);
    assertEquals(target.getExpectedJson(), gson.toJson(target));
  }

// com.google.gson.functional.ObjectTest::testNullFieldsDeserialization
  public void testNullFieldsDeserialization() throws Exception {
    String json = "{\"primitive1\":{\"longValue\":10,\"intValue\":20,\"booleanValue\":false"
        + ",\"stringValue\":\"stringValue\"}}";
    Nested target = gson.fromJson(json, Nested.class);
    assertEquals(json, target.getExpectedJson());
  }

// com.google.gson.functional.ObjectTest::testArrayOfObjectsSerialization
  public void testArrayOfObjectsSerialization() throws Exception {
    ArrayOfObjects target = new ArrayOfObjects();
    assertEquals(target.getExpectedJson(), gson.toJson(target));
  }

// com.google.gson.functional.ObjectTest::testArrayOfObjectsDeserialization
  public void testArrayOfObjectsDeserialization() throws Exception {
    String json = new ArrayOfObjects().getExpectedJson();
    ArrayOfObjects target = gson.fromJson(json, ArrayOfObjects.class);
    assertEquals(json, target.getExpectedJson());
  }

// com.google.gson.functional.ObjectTest::testArrayOfArraysSerialization
  public void testArrayOfArraysSerialization() throws Exception {
    ArrayOfArrays target = new ArrayOfArrays();
    assertEquals(target.getExpectedJson(), gson.toJson(target));
  }

// com.google.gson.functional.ObjectTest::testArrayOfArraysDeserialization
  public void testArrayOfArraysDeserialization() throws Exception {
    String json = new ArrayOfArrays().getExpectedJson();
    ArrayOfArrays target = gson.fromJson(json, ArrayOfArrays.class);
    assertEquals(json, target.getExpectedJson());
  }

// com.google.gson.functional.ObjectTest::testArrayOfObjectsAsFields
  public void testArrayOfObjectsAsFields() throws Exception {
    ClassWithObjects classWithObjects = new ClassWithObjects();
    BagOfPrimitives bagOfPrimitives = new BagOfPrimitives();
    String stringValue = "someStringValueInArray";
    String classWithObjectsJson = gson.toJson(classWithObjects);
    String bagOfPrimitivesJson = gson.toJson(bagOfPrimitives);
    
    ClassWithArray classWithArray = new ClassWithArray(
        new Object[] { stringValue, classWithObjects, bagOfPrimitives });
    String json = gson.toJson(classWithArray);

    assertTrue(json.contains(classWithObjectsJson));
    assertTrue(json.contains(bagOfPrimitivesJson));
    assertTrue(json.contains("\"" + stringValue + "\""));
  }

// com.google.gson.functional.ObjectTest::testNullArraysDeserialization
  public void testNullArraysDeserialization() throws Exception {
    String json = "{\"array\": null}";
    ClassWithArray target = gson.fromJson(json, ClassWithArray.class);
    assertNull(target.array);
  }

// com.google.gson.functional.ObjectTest::testNullObjectFieldsDeserialization
  public void testNullObjectFieldsDeserialization() throws Exception {
    String json = "{\"bag\": null}";
    ClassWithObjects target = gson.fromJson(json, ClassWithObjects.class);
    assertNull(target.bag);
  }

// com.google.gson.functional.ObjectTest::testEmptyCollectionInAnObjectDeserialization
  public void testEmptyCollectionInAnObjectDeserialization() throws Exception {
    String json = "{\"children\":[]}";
    ClassWithCollectionField target = gson.fromJson(json, ClassWithCollectionField.class);
    assertNotNull(target);
    assertTrue(target.children.isEmpty());
  }

// com.google.gson.functional.ObjectTest::testPrimitiveArrayInAnObjectDeserialization
  public void testPrimitiveArrayInAnObjectDeserialization() throws Exception {
    String json = "{\"longArray\":[0,1,2,3,4,5,6,7,8,9]}";
    PrimitiveArray target = gson.fromJson(json, PrimitiveArray.class);
    assertEquals(json, target.getExpectedJson());
  }

// com.google.gson.functional.ObjectTest::testNullPrimitiveFieldsDeserialization
  public void testNullPrimitiveFieldsDeserialization() throws Exception {
    String json = "{\"longValue\":null}";
    BagOfPrimitives target = gson.fromJson(json, BagOfPrimitives.class);
    assertEquals(BagOfPrimitives.DEFAULT_VALUE, target.longValue);
  }

// com.google.gson.functional.ObjectTest::testEmptyCollectionInAnObjectSerialization
  public void testEmptyCollectionInAnObjectSerialization() throws Exception {
    ClassWithCollectionField target = new ClassWithCollectionField();
    assertEquals("{\"children\":[]}", gson.toJson(target));
  }

// com.google.gson.functional.ObjectTest::testPrivateNoArgConstructorDeserialization
  public void testPrivateNoArgConstructorDeserialization() throws Exception {
    ClassWithPrivateNoArgsConstructor target =
      gson.fromJson("{\"a\":20}", ClassWithPrivateNoArgsConstructor.class);
    assertEquals(20, target.a);
  }

// com.google.gson.functional.ObjectTest::testAnonymousLocalClassesSerialization
  public void testAnonymousLocalClassesSerialization() throws Exception {
    assertEquals("", gson.toJson(new ClassWithNoFields() {
      
    }));
  }

// com.google.gson.functional.ObjectTest::testPrimitiveArrayFieldSerialization
  public void testPrimitiveArrayFieldSerialization() {
    PrimitiveArray target = new PrimitiveArray(new long[] { 1L, 2L, 3L });
    assertEquals(target.getExpectedJson(), gson.toJson(target));
  }

// com.google.gson.functional.ObjectTest::testClassWithObjectFieldSerialization
  public void testClassWithObjectFieldSerialization() {
    ClassWithObjectField obj = new ClassWithObjectField();
    obj.member = "abc";
    String json = gson.toJson(obj);
    assertTrue(json.contains("abc"));
  }

// com.google.gson.functional.ObjectTest::testInnerClassSerialization
  public void testInnerClassSerialization() {    
    Parent p = new Parent();
    Parent.Child c = p.new Child();
    String json = gson.toJson(c);
    assertTrue(json.contains("value2"));
    assertFalse(json.contains("value1"));
  }

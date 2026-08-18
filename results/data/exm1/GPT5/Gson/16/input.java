// buggy code
  private static Type resolve(Type context, Class<?> contextRawType, Type toResolve,
                              Collection<TypeVariable> visitedTypeVariables) {
    // this implementation is made a little more complicated in an attempt to avoid object-creation
    while (true) {
      if (toResolve instanceof TypeVariable) {
        TypeVariable<?> typeVariable = (TypeVariable<?>) toResolve;
          // cannot reduce due to infinite recursion
        toResolve = resolveTypeVariable(context, contextRawType, typeVariable);
        if (toResolve == typeVariable) {
          return toResolve;
        }

      } else if (toResolve instanceof Class && ((Class<?>) toResolve).isArray()) {
        Class<?> original = (Class<?>) toResolve;
        Type componentType = original.getComponentType();
        Type newComponentType = resolve(context, contextRawType, componentType, visitedTypeVariables);
        return componentType == newComponentType
            ? original
            : arrayOf(newComponentType);

      } else if (toResolve instanceof GenericArrayType) {
        GenericArrayType original = (GenericArrayType) toResolve;
        Type componentType = original.getGenericComponentType();
        Type newComponentType = resolve(context, contextRawType, componentType, visitedTypeVariables);
        return componentType == newComponentType
            ? original
            : arrayOf(newComponentType);

      } else if (toResolve instanceof ParameterizedType) {
        ParameterizedType original = (ParameterizedType) toResolve;
        Type ownerType = original.getOwnerType();
        Type newOwnerType = resolve(context, contextRawType, ownerType, visitedTypeVariables);
        boolean changed = newOwnerType != ownerType;

        Type[] args = original.getActualTypeArguments();
        for (int t = 0, length = args.length; t < length; t++) {
          Type resolvedTypeArgument = resolve(context, contextRawType, args[t], visitedTypeVariables);
          if (resolvedTypeArgument != args[t]) {
            if (!changed) {
              args = args.clone();
              changed = true;
            }
            args[t] = resolvedTypeArgument;
          }
        }

        return changed
            ? newParameterizedTypeWithOwner(newOwnerType, original.getRawType(), args)
            : original;

      } else if (toResolve instanceof WildcardType) {
        WildcardType original = (WildcardType) toResolve;
        Type[] originalLowerBound = original.getLowerBounds();
        Type[] originalUpperBound = original.getUpperBounds();

        if (originalLowerBound.length == 1) {
          Type lowerBound = resolve(context, contextRawType, originalLowerBound[0], visitedTypeVariables);
          if (lowerBound != originalLowerBound[0]) {
            return supertypeOf(lowerBound);
          }
        } else if (originalUpperBound.length == 1) {
          Type upperBound = resolve(context, contextRawType, originalUpperBound[0], visitedTypeVariables);
          if (upperBound != originalUpperBound[0]) {
            return subtypeOf(upperBound);
          }
        }
        return original;

      } else {
        return toResolve;
      }
    }
  }

// relevant test
// com.google.gson.CommentsTest::testParseComments
  public void testParseComments() {
    String json = "[\n"
        + "  
        + "  \"a\",\n"
        + "  \n"
        + "  \"b\",\n"
        + "  # this is yet another comment\n"
        + "  \"c\"\n"
        + "]";

    List<String> abc = new Gson().fromJson(json, new TypeToken<List<String>>() {}.getType());
    assertEquals(Arrays.asList("a", "b", "c"), abc);
  }

// com.google.gson.DefaultInetAddressTypeAdapterTest::testInetAddressSerializationAndDeserialization
  public void testInetAddressSerializationAndDeserialization() throws Exception {
    InetAddress address = InetAddress.getByName("8.8.8.8");
    String jsonAddress = gson.toJson(address);
    assertEquals("\"8.8.8.8\"", jsonAddress);
    
    InetAddress value = gson.fromJson(jsonAddress, InetAddress.class);
    assertEquals(value, address);
  }

// com.google.gson.DefaultMapJsonSerializerTest::testEmptyMapNoTypeSerialization
  public void testEmptyMapNoTypeSerialization() {
    Map<String, String> emptyMap = new HashMap<String, String>();
    JsonElement element = gson.toJsonTree(emptyMap, emptyMap.getClass());
    assertTrue(element instanceof JsonObject);
    JsonObject emptyMapJsonObject = (JsonObject) element;
    assertTrue(emptyMapJsonObject.entrySet().isEmpty());
  }

// com.google.gson.DefaultMapJsonSerializerTest::testEmptyMapSerialization
  public void testEmptyMapSerialization() {
    Type mapType = new TypeToken<Map<String, String>>() { }.getType();
    Map<String, String> emptyMap = new HashMap<String, String>();
    JsonElement element = gson.toJsonTree(emptyMap, mapType);

    assertTrue(element instanceof JsonObject);
    JsonObject emptyMapJsonObject = (JsonObject) element;
    assertTrue(emptyMapJsonObject.entrySet().isEmpty());
  }

// com.google.gson.DefaultMapJsonSerializerTest::testNonEmptyMapSerialization
  public void testNonEmptyMapSerialization() {
    Type mapType = new TypeToken<Map<String, String>>() { }.getType();
    Map<String, String> myMap = new HashMap<String, String>();
    String key = "key1";
    myMap.put(key, "value1");
    Gson gson = new Gson();
    JsonElement element = gson.toJsonTree(myMap, mapType);

    assertTrue(element.isJsonObject());
    JsonObject mapJsonObject = element.getAsJsonObject();
    assertTrue(mapJsonObject.has(key));
  }

// com.google.gson.FieldAttributesTest::testNullField
  public void testNullField() throws Exception {
    try {
      new FieldAttributes(null);
      fail("Field parameter can not be null");
    } catch (NullPointerException expected) { }
  }

// com.google.gson.FieldAttributesTest::testDeclaringClass
  public void testDeclaringClass() throws Exception {
    assertEquals(Foo.class, fieldAttributes.getDeclaringClass());
  }

// com.google.gson.FieldAttributesTest::testModifiers
  public void testModifiers() throws Exception {
    assertFalse(fieldAttributes.hasModifier(Modifier.STATIC));
    assertFalse(fieldAttributes.hasModifier(Modifier.FINAL));
    assertFalse(fieldAttributes.hasModifier(Modifier.ABSTRACT));
    assertFalse(fieldAttributes.hasModifier(Modifier.VOLATILE));
    assertFalse(fieldAttributes.hasModifier(Modifier.PROTECTED));

    assertTrue(fieldAttributes.hasModifier(Modifier.PUBLIC));
    assertTrue(fieldAttributes.hasModifier(Modifier.TRANSIENT));
  }

// com.google.gson.FieldAttributesTest::testIsSynthetic
  public void testIsSynthetic() throws Exception {
    assertFalse(fieldAttributes.isSynthetic());
  }

// com.google.gson.FieldAttributesTest::testName
  public void testName() throws Exception {
    assertEquals("bar", fieldAttributes.getName());
  }

// com.google.gson.FieldAttributesTest::testDeclaredTypeAndClass
  public void testDeclaredTypeAndClass() throws Exception {
    Type expectedType = new TypeToken<List<String>>() {}.getType();
    assertEquals(expectedType, fieldAttributes.getDeclaredType());
    assertEquals(List.class, fieldAttributes.getDeclaredClass());
  }

// com.google.gson.GenericArrayTypeTest::testOurTypeFunctionality
  public void testOurTypeFunctionality() throws Exception {
    Type parameterizedType = new TypeToken<List<String>>() {}.getType();
    Type genericArrayType = new TypeToken<List<String>[]>() {}.getType();

    assertEquals(parameterizedType, ourType.getGenericComponentType());
    assertEquals(genericArrayType, ourType);
    assertEquals(genericArrayType.hashCode(), ourType.hashCode());
  }

// com.google.gson.GenericArrayTypeTest::testNotEquals
  public void testNotEquals() throws Exception {
    Type differentGenericArrayType = new TypeToken<List<String>[][]>() {}.getType();
    assertFalse(differentGenericArrayType.equals(ourType));
    assertFalse(ourType.equals(differentGenericArrayType));
  }

// com.google.gson.GsonBuilderTest::testCreatingMoreThanOnce
  public void testCreatingMoreThanOnce() {
    GsonBuilder builder = new GsonBuilder();
    builder.create();
    builder.create();
  }

// com.google.gson.GsonBuilderTest::testExcludeFieldsWithModifiers
  public void testExcludeFieldsWithModifiers() {
    Gson gson = new GsonBuilder()
        .excludeFieldsWithModifiers(Modifier.VOLATILE, Modifier.PRIVATE)
        .create();
    assertEquals("{\"d\":\"d\"}", gson.toJson(new HasModifiers()));
  }

// com.google.gson.GsonBuilderTest::testRegisterTypeAdapterForCoreType
  public void testRegisterTypeAdapterForCoreType() {
    Type[] types = {
        byte.class,
        int.class,
        double.class,
        Short.class,
        Long.class,
        String.class,
    };
    for (Type type : types) {
      new GsonBuilder().registerTypeAdapter(type, NULL_TYPE_ADAPTER);
    }
  }

// com.google.gson.GsonBuilderTest::testTransientFieldExclusion
  public void testTransientFieldExclusion() {
    Gson gson = new GsonBuilder()
        .excludeFieldsWithModifiers()
        .create();
    assertEquals("{\"a\":\"a\"}", gson.toJson(new HasTransients()));
  }

// com.google.gson.GsonTest::testOverridesDefaultExcluder
  public void testOverridesDefaultExcluder() {
    Gson gson = new Gson(CUSTOM_EXCLUDER, CUSTOM_FIELD_NAMING_STRATEGY,
        new HashMap<Type, InstanceCreator<?>>(), true, false, true, false,
        true, true, false, LongSerializationPolicy.DEFAULT,
        new ArrayList<TypeAdapterFactory>());

    assertEquals(CUSTOM_EXCLUDER, gson.excluder());
    assertEquals(CUSTOM_FIELD_NAMING_STRATEGY, gson.fieldNamingStrategy());
    assertEquals(true, gson.serializeNulls());
    assertEquals(false, gson.htmlSafe());
  }

// com.google.gson.GsonTypeAdapterTest::testDefaultTypeAdapterThrowsParseException
  public void testDefaultTypeAdapterThrowsParseException() throws Exception {
    try {
      gson.fromJson("{\"abc\":123}", BigInteger.class);
      fail("Should have thrown a JsonParseException");
    } catch (JsonParseException expected) { }
  }

// com.google.gson.GsonTypeAdapterTest::testTypeAdapterThrowsException
  public void testTypeAdapterThrowsException() throws Exception {
    try {
      gson.toJson(new AtomicLong(0));
      fail("Type Adapter should have thrown an exception");
    } catch (IllegalStateException expected) { }

    try {
      gson.fromJson("123", AtomicLong.class);
      fail("Type Adapter should have thrown an exception");
    } catch (JsonParseException expected) { }
  }

// com.google.gson.GsonTypeAdapterTest::testTypeAdapterProperlyConvertsTypes
  public void testTypeAdapterProperlyConvertsTypes() throws Exception {
    int intialValue = 1;
    AtomicInteger atomicInt = new AtomicInteger(intialValue);
    String json = gson.toJson(atomicInt);
    assertEquals(intialValue + 1, Integer.parseInt(json));

    atomicInt = gson.fromJson(json, AtomicInteger.class);
    assertEquals(intialValue, atomicInt.get());
  }

// com.google.gson.GsonTypeAdapterTest::testTypeAdapterDoesNotAffectNonAdaptedTypes
  public void testTypeAdapterDoesNotAffectNonAdaptedTypes() throws Exception {
    String expected = "blah";
    String actual = gson.toJson(expected);
    assertEquals("\"" + expected + "\"", actual);

    actual = gson.fromJson(actual, String.class);
    assertEquals(expected, actual);
  }

// com.google.gson.GsonTypeAdapterTest::testDeserializerForAbstractClass
  public void testDeserializerForAbstractClass() {
    Concrete instance = new Concrete();
    instance.a = "android";
    instance.b = "beep";
    assertSerialized("{\"a\":\"android\"}", Abstract.class, true, true, instance);
    assertSerialized("{\"a\":\"android\"}", Abstract.class, true, false, instance);
    assertSerialized("{\"a\":\"android\"}", Abstract.class, false, true, instance);
    assertSerialized("{\"a\":\"android\"}", Abstract.class, false, false, instance);
    assertSerialized("{\"b\":\"beep\",\"a\":\"android\"}", Concrete.class, true, true, instance);
    assertSerialized("{\"b\":\"beep\",\"a\":\"android\"}", Concrete.class, true, false, instance);
    assertSerialized("{\"b\":\"beep\",\"a\":\"android\"}", Concrete.class, false, true, instance);
    assertSerialized("{\"b\":\"beep\",\"a\":\"android\"}", Concrete.class, false, false, instance);
  }

// com.google.gson.JavaSerializationTest::testMapIsSerializable
  public void testMapIsSerializable() throws Exception {
    Type type = new TypeToken<Map<String, Integer>>() {}.getType();
    Map<String, Integer> map = gson.fromJson("{\"b\":1,\"c\":2,\"a\":3}", type);
    Map<String, Integer> serialized = serializedCopy(map);
    assertEquals(map, serialized);
    
    assertEquals(Arrays.asList("b", "c", "a"), new ArrayList<String>(serialized.keySet()));
  }

// com.google.gson.JavaSerializationTest::testListIsSerializable
  public void testListIsSerializable() throws Exception {
    Type type = new TypeToken<List<String>>() {}.getType();
    List<String> list = gson.fromJson("[\"a\",\"b\",\"c\"]", type);
    List<String> serialized = serializedCopy(list);
    assertEquals(list, serialized);
  }

// com.google.gson.JavaSerializationTest::testNumberIsSerializable
  public void testNumberIsSerializable() throws Exception {
    Type type = new TypeToken<List<Number>>() {}.getType();
    List<Number> list = gson.fromJson("[1,3.14,6.673e-11]", type);
    List<Number> serialized = serializedCopy(list);
    assertEquals(1.0, serialized.get(0).doubleValue());
    assertEquals(3.14, serialized.get(1).doubleValue());
    assertEquals(6.673e-11, serialized.get(2).doubleValue());
  }

// com.google.gson.JsonObjectTest::testAddingAndRemovingObjectProperties
  public void testAddingAndRemovingObjectProperties() throws Exception {
    JsonObject jsonObj = new JsonObject();
    String propertyName = "property";
    assertFalse(jsonObj.has(propertyName));
    assertNull(jsonObj.get(propertyName));

    JsonPrimitive value = new JsonPrimitive("blah");
    jsonObj.add(propertyName, value);
    assertEquals(value, jsonObj.get(propertyName));

    JsonElement removedElement = jsonObj.remove(propertyName);
    assertEquals(value, removedElement);
    assertFalse(jsonObj.has(propertyName));
    assertNull(jsonObj.get(propertyName));
  }

// com.google.gson.JsonObjectTest::testAddingNullPropertyValue
  public void testAddingNullPropertyValue() throws Exception {
    String propertyName = "property";
    JsonObject jsonObj = new JsonObject();
    jsonObj.add(propertyName, null);

    assertTrue(jsonObj.has(propertyName));

    JsonElement jsonElement = jsonObj.get(propertyName);
    assertNotNull(jsonElement);
    assertTrue(jsonElement.isJsonNull());
  }

// com.google.gson.JsonObjectTest::testAddingNullOrEmptyPropertyName
  public void testAddingNullOrEmptyPropertyName() throws Exception {
    JsonObject jsonObj = new JsonObject();
    try {
      jsonObj.add(null, JsonNull.INSTANCE);
      fail("Should not allow null property names.");
    } catch (NullPointerException expected) { }

    jsonObj.add("", JsonNull.INSTANCE);
    jsonObj.add("   \t", JsonNull.INSTANCE);
  }

// com.google.gson.JsonObjectTest::testAddingBooleanProperties
  public void testAddingBooleanProperties() throws Exception {
    String propertyName = "property";
    JsonObject jsonObj = new JsonObject();
    jsonObj.addProperty(propertyName, true);

    assertTrue(jsonObj.has(propertyName));

    JsonElement jsonElement = jsonObj.get(propertyName);
    assertNotNull(jsonElement);
    assertTrue(jsonElement.getAsBoolean());
  }

// com.google.gson.JsonObjectTest::testAddingStringProperties
  public void testAddingStringProperties() throws Exception {
    String propertyName = "property";
    String value = "blah";

    JsonObject jsonObj = new JsonObject();
    jsonObj.addProperty(propertyName, value);

    assertTrue(jsonObj.has(propertyName));

    JsonElement jsonElement = jsonObj.get(propertyName);
    assertNotNull(jsonElement);
    assertEquals(value, jsonElement.getAsString());
  }

// com.google.gson.JsonObjectTest::testAddingCharacterProperties
  public void testAddingCharacterProperties() throws Exception {
    String propertyName = "property";
    char value = 'a';

    JsonObject jsonObj = new JsonObject();
    jsonObj.addProperty(propertyName, value);

    assertTrue(jsonObj.has(propertyName));

    JsonElement jsonElement = jsonObj.get(propertyName);
    assertNotNull(jsonElement);
    assertEquals(String.valueOf(value), jsonElement.getAsString());
    assertEquals(value, jsonElement.getAsCharacter());
  }

// com.google.gson.JsonObjectTest::testPropertyWithQuotes
  public void testPropertyWithQuotes() {
    JsonObject jsonObj = new JsonObject();
    jsonObj.add("a\"b", new JsonPrimitive("c\"d"));
    String json = new Gson().toJson(jsonObj);
    assertEquals("{\"a\\\"b\":\"c\\\"d\"}", json);
  }

// com.google.gson.JsonObjectTest::testWritePropertyWithEmptyStringName
  public void testWritePropertyWithEmptyStringName() {
    JsonObject jsonObj = new JsonObject();
    jsonObj.add("", new JsonPrimitive(true));
    assertEquals("{\"\":true}", new Gson().toJson(jsonObj));

  }

// com.google.gson.JsonObjectTest::testReadPropertyWithEmptyStringName
  public void testReadPropertyWithEmptyStringName() {
    JsonObject jsonObj = new JsonParser().parse("{\"\":true}").getAsJsonObject();
    assertEquals(true, jsonObj.get("").getAsBoolean());
  }

// com.google.gson.JsonObjectTest::testEqualsOnEmptyObject
  public void testEqualsOnEmptyObject() {
    MoreAsserts.assertEqualsAndHashCode(new JsonObject(), new JsonObject());
  }

// com.google.gson.JsonObjectTest::testEqualsNonEmptyObject
  public void testEqualsNonEmptyObject() {
    JsonObject a = new JsonObject();
    JsonObject b = new JsonObject();

    assertEquals(a, a);

    a.add("foo", new JsonObject());
    assertFalse(a.equals(b));
    assertFalse(b.equals(a));

    b.add("foo", new JsonObject());
    MoreAsserts.assertEqualsAndHashCode(a, b);

    a.add("bar", new JsonObject());
    assertFalse(a.equals(b));
    assertFalse(b.equals(a));

    b.add("bar", JsonNull.INSTANCE);
    assertFalse(a.equals(b));
    assertFalse(b.equals(a));
  }

// com.google.gson.JsonObjectTest::testSize
  public void testSize() {
    JsonObject o = new JsonObject();
    assertEquals(0, o.size());

    o.add("Hello", new JsonPrimitive(1));
    assertEquals(1, o.size());

    o.add("Hi", new JsonPrimitive(1));
    assertEquals(2, o.size());

    o.remove("Hello");
    assertEquals(1, o.size());
  }

// com.google.gson.JsonObjectTest::testDeepCopy
  public void testDeepCopy() {
    JsonObject original = new JsonObject();
    JsonArray firstEntry = new JsonArray();
    original.add("key", firstEntry);

    JsonObject copy = original.deepCopy();
    firstEntry.add(new JsonPrimitive("z"));

    assertEquals(1, original.get("key").getAsJsonArray().size());
    assertEquals(0, copy.get("key").getAsJsonArray().size());
  }

// com.google.gson.JsonObjectTest::testKeySet
  public void testKeySet() {
    JsonObject a = new JsonObject();

    a.add("foo", new JsonArray());
    a.add("bar", new JsonObject());

    assertEquals(2, a.size());
    assertEquals(2, a.keySet().size());
    assertTrue(a.keySet().contains("foo"));
    assertTrue(a.keySet().contains("bar"));
  }

// com.google.gson.JsonParserTest::testParseInvalidJson
  public void testParseInvalidJson() {
    try {
      parser.parse("[[]");
      fail();
    } catch (JsonSyntaxException expected) { }
  }

// com.google.gson.JsonParserTest::testParseUnquotedStringArrayFails
  public void testParseUnquotedStringArrayFails() {
    JsonElement element = parser.parse("[a,b,c]");
    assertEquals("a", element.getAsJsonArray().get(0).getAsString());
    assertEquals("b", element.getAsJsonArray().get(1).getAsString());
    assertEquals("c", element.getAsJsonArray().get(2).getAsString());
    assertEquals(3, element.getAsJsonArray().size());
  }

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

// com.google.gson.JsonParserTest::testParseUnquotedSingleWordStringFails
  public void testParseUnquotedSingleWordStringFails() {
    assertEquals("Test", parser.parse("Test").getAsString());
  }

// com.google.gson.JsonParserTest::testParseUnquotedMultiWordStringFails
  public void testParseUnquotedMultiWordStringFails() {
    String unquotedSentence = "Test is a test..blah blah";
    try {
      parser.parse(unquotedSentence);
      fail();
    } catch (JsonSyntaxException expected) { }
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
    Gson gson = new Gson();
    CharArrayWriter writer = new CharArrayWriter();
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

// com.google.gson.LongSerializationPolicyTest::testDefaultLongSerialization
  public void testDefaultLongSerialization() throws Exception {
    JsonElement element = LongSerializationPolicy.DEFAULT.serialize(1556L);
    assertTrue(element.isJsonPrimitive());
    
    JsonPrimitive jsonPrimitive = element.getAsJsonPrimitive();
    assertFalse(jsonPrimitive.isString());
    assertTrue(jsonPrimitive.isNumber());
    assertEquals(1556L, element.getAsLong());
  }

// com.google.gson.LongSerializationPolicyTest::testDefaultLongSerializationIntegration
  public void testDefaultLongSerializationIntegration() {
    Gson gson = new GsonBuilder()
        .setLongSerializationPolicy(LongSerializationPolicy.DEFAULT)
        .create();
    assertEquals("[1]", gson.toJson(new long[] { 1L }, long[].class));
    assertEquals("[1]", gson.toJson(new Long[] { 1L }, Long[].class));
  }

// com.google.gson.LongSerializationPolicyTest::testStringLongSerialization
  public void testStringLongSerialization() throws Exception {
    JsonElement element = LongSerializationPolicy.STRING.serialize(1556L);
    assertTrue(element.isJsonPrimitive());

    JsonPrimitive jsonPrimitive = element.getAsJsonPrimitive();
    assertFalse(jsonPrimitive.isNumber());
    assertTrue(jsonPrimitive.isString());
    assertEquals("1556", element.getAsString());
  }

// com.google.gson.LongSerializationPolicyTest::testStringLongSerializationIntegration
  public void testStringLongSerializationIntegration() {
    Gson gson = new GsonBuilder()
        .setLongSerializationPolicy(LongSerializationPolicy.STRING)
        .create();
    assertEquals("[\"1\"]", gson.toJson(new long[] { 1L }, long[].class));
    assertEquals("[\"1\"]", gson.toJson(new Long[] { 1L }, Long[].class));
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
    } catch (JsonParseException expected) {
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
    } catch (JsonParseException expected) {
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
    assertEquals("null", stringWriter.toString());
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
    } catch (NullPointerException expected) {
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

// com.google.gson.ObjectTypeAdapterTest::testDeserialize
  public void testDeserialize() throws Exception {
    Map<?, ?> map = (Map<?, ?>) adapter.fromJson("{\"a\":5,\"b\":[1,2,null],\"c\":{\"x\":\"y\"}}");
    assertEquals(5.0, map.get("a"));
    assertEquals(Arrays.asList(1.0, 2.0, null), map.get("b"));
    assertEquals(Collections.singletonMap("x", "y"), map.get("c"));
    assertEquals(3, map.size());
  }

// com.google.gson.ObjectTypeAdapterTest::testSerialize
  public void testSerialize() throws Exception {
    Object object = new RuntimeType();
    assertEquals("{'a':5,'b':[1,2,null]}", adapter.toJson(object).replace("\"", "'"));
  }

// com.google.gson.ObjectTypeAdapterTest::testSerializeNullValue
  public void testSerializeNullValue() throws Exception {
    Map<String, Object> map = new LinkedHashMap<String, Object>();
    map.put("a", null);
    assertEquals("{'a':null}", adapter.toJson(map).replace('"', '\''));
  }

// com.google.gson.ObjectTypeAdapterTest::testDeserializeNullValue
  public void testDeserializeNullValue() throws Exception {
    Map<String, Object> map = new LinkedHashMap<String, Object>();
    map.put("a", null);
    assertEquals(map, adapter.fromJson("{\"a\":null}"));
  }

// com.google.gson.ObjectTypeAdapterTest::testSerializeObject
  public void testSerializeObject() throws Exception {
    assertEquals("{}", adapter.toJson(new Object()));
  }

// com.google.gson.OverrideCoreTypeAdaptersTest::testOverrideWrapperBooleanAdapter
  public void testOverrideWrapperBooleanAdapter() {
    Gson gson = new GsonBuilder()
        .registerTypeAdapter(Boolean.class, booleanAsIntAdapter)
        .create();
    assertEquals("true", gson.toJson(true, boolean.class));
    assertEquals("1", gson.toJson(true, Boolean.class));
    assertEquals(Boolean.TRUE, gson.fromJson("true", boolean.class));
    assertEquals(Boolean.TRUE, gson.fromJson("1", Boolean.class));
    assertEquals(Boolean.FALSE, gson.fromJson("0", Boolean.class));
  }

// com.google.gson.OverrideCoreTypeAdaptersTest::testOverridePrimitiveBooleanAdapter
  public void testOverridePrimitiveBooleanAdapter() {
    Gson gson = new GsonBuilder()
        .registerTypeAdapter(boolean.class, booleanAsIntAdapter)
        .create();
    assertEquals("1", gson.toJson(true, boolean.class));
    assertEquals("true", gson.toJson(true, Boolean.class));
    assertEquals(Boolean.TRUE, gson.fromJson("1", boolean.class));
    assertEquals(Boolean.TRUE, gson.fromJson("true", Boolean.class));
    assertEquals("0", gson.toJson(false, boolean.class));
  }

// com.google.gson.OverrideCoreTypeAdaptersTest::testOverrideStringAdapter
  public void testOverrideStringAdapter() {
    Gson gson = new GsonBuilder()
        .registerTypeAdapter(String.class, swapCaseStringAdapter)
        .create();
    assertEquals("\"HELLO\"", gson.toJson("Hello", String.class));
    assertEquals("hello", gson.fromJson("\"Hello\"", String.class));
  }

// com.google.gson.ParameterizedTypeTest::testOurTypeFunctionality
  public void testOurTypeFunctionality() throws Exception {
    Type parameterizedType = new TypeToken<List<String>>() {}.getType();
    assertNull(ourType.getOwnerType());
    assertEquals(String.class, ourType.getActualTypeArguments()[0]);
    assertEquals(List.class, ourType.getRawType());
    assertEquals(parameterizedType, ourType);
    assertEquals(parameterizedType.hashCode(), ourType.hashCode());
  }

// com.google.gson.ParameterizedTypeTest::testNotEquals
  public void testNotEquals() throws Exception {
    Type differentParameterizedType = new TypeToken<List<Integer>>() {}.getType();
    assertFalse(differentParameterizedType.equals(ourType));
    assertFalse(ourType.equals(differentParameterizedType));
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

// com.google.gson.functional.ArrayTest::testArrayOfPrimitivesAsObjectsSerialization
  public void testArrayOfPrimitivesAsObjectsSerialization() throws Exception {
    Object[] objs = new Object[] {1, "abc", 0.3f, 5L};
    String json = gson.toJson(objs);
    assertTrue(json.contains("abc"));
    assertTrue(json.contains("0.3"));
    assertTrue(json.contains("5"));
  }

// com.google.gson.functional.ArrayTest::testArrayOfPrimitivesAsObjectsDeserialization
  public void testArrayOfPrimitivesAsObjectsDeserialization() throws Exception {
    String json = "[1,'abc',0.3,1.1,5]";
    Object[] objs = gson.fromJson(json, Object[].class);
    assertEquals(1, ((Number)objs[0]).intValue());
    assertEquals("abc", objs[1]);
    assertEquals(0.3, ((Number)objs[2]).doubleValue());
    assertEquals(new BigDecimal("1.1"), new BigDecimal(objs[3].toString()));
    assertEquals(5, ((Number)objs[4]).shortValue());
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

// com.google.gson.functional.ArrayTest::testArrayOfNullSerialization
  public void testArrayOfNullSerialization() {
    Object[] array = new Object[] {null};
    String json = gson.toJson(array);
    assertEquals("[null]", json);
  }

// com.google.gson.functional.ArrayTest::testArrayOfNullDeserialization
  public void testArrayOfNullDeserialization() {
    String[] values = gson.fromJson("[null]", String[].class);
    assertNull(values[0]);
  }

// com.google.gson.functional.ArrayTest::testMultidimenstionalArraysSerialization
  public void testMultidimenstionalArraysSerialization() {
    String[][] items = new String[][]{
        {"3m Co", "71.72", "0.02", "0.03", "4/2 12:00am", "Manufacturing"},
        {"Alcoa Inc", "29.01", "0.42", "1.47", "4/1 12:00am", "Manufacturing"}
    };
    String json = gson.toJson(items);
    assertTrue(json.contains("[[\"3m Co"));
    assertTrue(json.contains("Manufacturing\"]]"));
  }

// com.google.gson.functional.ArrayTest::testMultiDimenstionalObjectArraysSerialization
  public void testMultiDimenstionalObjectArraysSerialization() {
    Object[][] array = new Object[][] { new Object[] { 1, 2 } };
    assertEquals("[[1,2]]", gson.toJson(array));
  }

// com.google.gson.functional.ArrayTest::testMixingTypesInObjectArraySerialization
  public void testMixingTypesInObjectArraySerialization() {
    Object[] array = new Object[] { 1, 2, new Object[] { "one", "two", 3 } };
    assertEquals("[1,2,[\"one\",\"two\",3]]", gson.toJson(array));
  }

// com.google.gson.functional.ArrayTest::testMultidimenstionalArraysDeserialization
  public void testMultidimenstionalArraysDeserialization() {
    String json = "[['3m Co','71.72','0.02','0.03','4/2 12:00am','Manufacturing'],"
      + "['Alcoa Inc','29.01','0.42','1.47','4/1 12:00am','Manufacturing']]";
    String[][] items = gson.fromJson(json, String[][].class);
    assertEquals("3m Co", items[0][0]);
    assertEquals("Manufacturing", items[1][5]);
  }

// com.google.gson.functional.ArrayTest::testArrayElementsAreArrays
  public void testArrayElementsAreArrays() {
    Object[] stringArrays = {
        new String[] {"test1", "test2"},
        new String[] {"test3", "test4"}
    };
    assertEquals("[[\"test1\",\"test2\"],[\"test3\",\"test4\"]]",
        new Gson().toJson(stringArrays));
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
    } catch (StackOverflowError expected) {
    }
  }

// com.google.gson.functional.CircularReferenceTest::testSelfReferenceIgnoredInSerialization
  public void testSelfReferenceIgnoredInSerialization() throws Exception {
    ClassOverridingEquals objA = new ClassOverridingEquals();
    objA.ref = objA;

    String json = gson.toJson(objA);
    assertFalse(json.contains("ref")); 
  }

// com.google.gson.functional.CircularReferenceTest::testSelfReferenceArrayFieldSerialization
  public void testSelfReferenceArrayFieldSerialization() throws Exception {
    ClassWithSelfReferenceArray objA = new ClassWithSelfReferenceArray();
    objA.children = new ClassWithSelfReferenceArray[]{objA};

    try {
      gson.toJson(objA);
      fail("Circular reference to self can not be serialized!");
    } catch (StackOverflowError expected) {
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
    } catch (StackOverflowError expected) {
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

// com.google.gson.functional.CollectionTest::testPriorityQueue
  public void testPriorityQueue() throws Exception {
    Type type = new TypeToken<PriorityQueue<Integer>>(){}.getType();
    PriorityQueue<Integer> queue = gson.fromJson("[10, 20, 22]", type);
    assertEquals(3, queue.size());
    String json = gson.toJson(queue);
    assertEquals(10, queue.remove().intValue());
    assertEquals(20, queue.remove().intValue());
    assertEquals(22, queue.remove().intValue());
    assertEquals("[10,20,22]", json);
  }

// com.google.gson.functional.CollectionTest::testVector
  public void testVector() {
    Type type = new TypeToken<Vector<Integer>>(){}.getType();
    Vector<Integer> target = gson.fromJson("[10, 20, 31]", type);
    assertEquals(3, target.size());
    assertEquals(10, target.get(0).intValue());
    assertEquals(20, target.get(1).intValue());
    assertEquals(31, target.get(2).intValue());
    String json = gson.toJson(target);
    assertEquals("[10,20,31]", json);
  }

// com.google.gson.functional.CollectionTest::testStack
  public void testStack() {
    Type type = new TypeToken<Stack<Integer>>(){}.getType();
    Stack<Integer> target = gson.fromJson("[11, 13, 17]", type);
    assertEquals(3, target.size());
    String json = gson.toJson(target);
    assertEquals(17, target.pop().intValue());
    assertEquals(13, target.pop().intValue());
    assertEquals(11, target.pop().intValue());
    assertEquals("[11,13,17]", json);
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
    Collection integers = gson.fromJson(json, Collection.class);
    
    assertEquals(Arrays.asList(0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0), integers);

    json = "[\"Hello\", \"World\"]";
    Collection strings = gson.fromJson(json, Collection.class);
    assertTrue(strings.contains("Hello"));
    assertTrue(strings.contains("World"));
  }

// com.google.gson.functional.CollectionTest::testRawCollectionOfBagOfPrimitivesNotAllowed
  public void testRawCollectionOfBagOfPrimitivesNotAllowed() {
    BagOfPrimitives bag = new BagOfPrimitives(10, 20, false, "stringValue");
    String json = '[' + bag.getExpectedJson() + ',' + bag.getExpectedJson() + ']';
    Collection target = gson.fromJson(json, Collection.class);
    assertEquals(2, target.size());
    for (Object bag1 : target) {
      
      Map<String, Object> values = (Map<String, Object>) bag1;
      assertTrue(values.containsValue(10.0));
      assertTrue(values.containsValue(20.0));
      assertTrue(values.containsValue("stringValue"));
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

// com.google.gson.functional.CollectionTest::testFieldIsArrayList
  public void testFieldIsArrayList() {
    HasArrayListField object = new HasArrayListField();
    object.longs.add(1L);
    object.longs.add(3L);
    String json = gson.toJson(object, HasArrayListField.class);
    assertEquals("{\"longs\":[1,3]}", json);
    HasArrayListField copy = gson.fromJson("{\"longs\":[1,3]}", HasArrayListField.class);
    assertEquals(Arrays.asList(1L, 3L), copy.longs);
  }

// com.google.gson.functional.CollectionTest::testUserCollectionTypeAdapter
  public void testUserCollectionTypeAdapter() {
    Type listOfString = new TypeToken<List<String>>() {}.getType();
    Object stringListSerializer = new JsonSerializer<List<String>>() {
      public JsonElement serialize(List<String> src, Type typeOfSrc,
          JsonSerializationContext context) {
        return new JsonPrimitive(src.get(0) + ";" + src.get(1));
      }
    };
    Gson gson = new GsonBuilder()
        .registerTypeAdapter(listOfString, stringListSerializer)
        .create();
    assertEquals("\"ab;cd\"", gson.toJson(Arrays.asList("ab", "cd"), listOfString));
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
      @Override public MyBase deserialize(JsonElement json, Type pojoType,
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
        @Override
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
        @Override
        public Base deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
          return null;
        }
      }).create();
    String json = "{base:{baseName:'Base',subName:'SubRevised'}}";
    ClassWithBaseField target = gson.fromJson(json, ClassWithBaseField.class);
    assertNull(target.base);
  }

// com.google.gson.functional.CustomDeserializerTest::testCustomDeserializerReturnsNullForArrayElements
  public void testCustomDeserializerReturnsNullForArrayElements() {
    Gson gson = new GsonBuilder()
      .registerTypeAdapter(Base.class, new JsonDeserializer<Base>() {
        @Override
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
        @Override
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
          @Override public JsonElement serialize(ClassWithCustomTypeConverter src, Type typeOfSrc,
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
          @Override public ClassWithCustomTypeConverter deserialize(JsonElement json, Type typeOfT,
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
          @Override public JsonElement serialize(BagOfPrimitives src, Type typeOfSrc,
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
          @Override public BagOfPrimitives deserialize(JsonElement json, Type typeOfT,
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
      @Override
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
      @Override
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

// com.google.gson.functional.CustomTypeAdaptersTest::testCustomSerializerInvokedForPrimitives
  public void testCustomSerializerInvokedForPrimitives() {
    Gson gson = new GsonBuilder()
        .registerTypeAdapter(boolean.class, new JsonSerializer<Boolean>() {
          @Override public JsonElement serialize(Boolean s, Type t, JsonSerializationContext c) {
            return new JsonPrimitive(s ? 1 : 0);
          }
        })
        .create();
    assertEquals("1", gson.toJson(true, boolean.class));
    assertEquals("true", gson.toJson(true, Boolean.class));
  }

// com.google.gson.functional.CustomTypeAdaptersTest::testCustomDeserializerInvokedForPrimitives
  public void testCustomDeserializerInvokedForPrimitives() {
    Gson gson = new GsonBuilder()
        .registerTypeAdapter(boolean.class, new JsonDeserializer() {
          @Override
          public Object deserialize(JsonElement json, Type t, JsonDeserializationContext context) {
            return json.getAsInt() != 0;
          }
        })
        .create();
    assertEquals(Boolean.TRUE, gson.fromJson("1", boolean.class));
    assertEquals(Boolean.TRUE, gson.fromJson("true", Boolean.class));
  }

// com.google.gson.functional.CustomTypeAdaptersTest::testCustomByteArraySerializer
  public void testCustomByteArraySerializer() {
    Gson gson = new GsonBuilder().registerTypeAdapter(byte[].class, new JsonSerializer<byte[]>() {
      @Override
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
          @Override public byte[] deserialize(JsonElement json,
              Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
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

// com.google.gson.functional.CustomTypeAdaptersTest::testRegisterHierarchyAdapterForDate
  public void testRegisterHierarchyAdapterForDate() {
    Gson gson = new GsonBuilder()
        .registerTypeHierarchyAdapter(Date.class, new DateTypeAdapter())
        .create();
    assertEquals("0", gson.toJson(new Date(0)));
    assertEquals("0", gson.toJson(new java.sql.Date(0)));
    assertEquals(new Date(0), gson.fromJson("0", Date.class));
    assertEquals(new java.sql.Date(0), gson.fromJson("0", java.sql.Date.class));
  }

// com.google.gson.functional.DefaultTypeAdaptersTest::testClassSerialization
  public void testClassSerialization() {
    try {
      gson.toJson(String.class);  
    } catch (UnsupportedOperationException expected) {}
    
    gson = new GsonBuilder().registerTypeAdapter(Class.class, new MyClassTypeAdapter()).create();
    assertEquals("\"java.lang.String\"", gson.toJson(String.class));  
  }

// com.google.gson.functional.DefaultTypeAdaptersTest::testClassDeserialization
  public void testClassDeserialization() {
    try {
      gson.fromJson("String.class", String.class.getClass());  
    } catch (UnsupportedOperationException expected) {}
    
    gson = new GsonBuilder().registerTypeAdapter(Class.class, new MyClassTypeAdapter()).create();
    assertEquals(String.class, gson.fromJson("java.lang.String", Class.class));  
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

// com.google.gson.functional.DefaultTypeAdaptersTest::testNullSerialization
  public void testNullSerialization() throws Exception {
    testNullSerializationAndDeserialization(Boolean.class);
    testNullSerializationAndDeserialization(Byte.class);
    testNullSerializationAndDeserialization(Short.class);
    testNullSerializationAndDeserialization(Integer.class);
    testNullSerializationAndDeserialization(Long.class);
    testNullSerializationAndDeserialization(Double.class);
    testNullSerializationAndDeserialization(Float.class);
    testNullSerializationAndDeserialization(Number.class);
    testNullSerializationAndDeserialization(Character.class);
    testNullSerializationAndDeserialization(String.class);
    testNullSerializationAndDeserialization(StringBuilder.class);
    testNullSerializationAndDeserialization(StringBuffer.class);
    testNullSerializationAndDeserialization(BigDecimal.class);
    testNullSerializationAndDeserialization(BigInteger.class);
    testNullSerializationAndDeserialization(TreeSet.class);
    testNullSerializationAndDeserialization(ArrayList.class);
    testNullSerializationAndDeserialization(HashSet.class);
    testNullSerializationAndDeserialization(Properties.class);
    testNullSerializationAndDeserialization(URL.class);
    testNullSerializationAndDeserialization(URI.class);
    testNullSerializationAndDeserialization(UUID.class);
    testNullSerializationAndDeserialization(Locale.class);
    testNullSerializationAndDeserialization(InetAddress.class);
    testNullSerializationAndDeserialization(BitSet.class);
    testNullSerializationAndDeserialization(Date.class);
    testNullSerializationAndDeserialization(GregorianCalendar.class);
    testNullSerializationAndDeserialization(Calendar.class);
    testNullSerializationAndDeserialization(Time.class);
    testNullSerializationAndDeserialization(Timestamp.class);
    testNullSerializationAndDeserialization(java.sql.Date.class);
    testNullSerializationAndDeserialization(Enum.class);
    testNullSerializationAndDeserialization(Class.class);
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

// com.google.gson.functional.DefaultTypeAdaptersTest::testOverrideBigIntegerTypeAdapter
  public void testOverrideBigIntegerTypeAdapter() throws Exception {
    gson = new GsonBuilder()
        .registerTypeAdapter(BigInteger.class, new NumberAsStringAdapter(BigInteger.class))
        .create();
    assertEquals("\"123\"", gson.toJson(new BigInteger("123"), BigInteger.class));
    assertEquals(new BigInteger("123"), gson.fromJson("\"123\"", BigInteger.class));
  }

// com.google.gson.functional.DefaultTypeAdaptersTest::testOverrideBigDecimalTypeAdapter
  public void testOverrideBigDecimalTypeAdapter() throws Exception {
    gson = new GsonBuilder()
        .registerTypeAdapter(BigDecimal.class, new NumberAsStringAdapter(BigDecimal.class))
        .create();
    assertEquals("\"1.1\"", gson.toJson(new BigDecimal("1.1"), BigDecimal.class));
    assertEquals(new BigDecimal("1.1"), gson.fromJson("\"1.1\"", BigDecimal.class));
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

// com.google.gson.functional.DefaultTypeAdaptersTest::testBitSetSerialization
  public void testBitSetSerialization() throws Exception {
    Gson gson = new Gson();
    BitSet bits = new BitSet();
    bits.set(1);
    bits.set(3, 6);
    bits.set(9);
    String json = gson.toJson(bits);
    assertEquals("[0,1,0,1,1,1,0,0,0,1]", json);
  }

// com.google.gson.functional.DefaultTypeAdaptersTest::testBitSetDeserialization
  public void testBitSetDeserialization() throws Exception {
    BitSet expected = new BitSet();
    expected.set(0);
    expected.set(2, 6);
    expected.set(8);

    Gson gson = new Gson();
    String json = gson.toJson(expected);
    assertEquals(expected, gson.fromJson(json, BitSet.class));

    json = "[1,0,1,1,1,1,0,0,1,0,0,0]";
    assertEquals(expected, gson.fromJson(json, BitSet.class));

    json = "[\"1\",\"0\",\"1\",\"1\",\"1\",\"1\",\"0\",\"0\",\"1\"]";
    assertEquals(expected, gson.fromJson(json, BitSet.class));

    json = "[true,false,true,true,true,true,false,false,true,false,false]";
    assertEquals(expected, gson.fromJson(json, BitSet.class));
  }

// com.google.gson.functional.DefaultTypeAdaptersTest::testDefaultDateSerialization
  public void testDefaultDateSerialization() {
    Date now = new Date(1315806903103L);
    String json = gson.toJson(now);
    assertEquals("\"Sep 11, 2011, 10:55:03 PM\"", json);
  }

// com.google.gson.functional.DefaultTypeAdaptersTest::testDefaultDateDeserialization
  public void testDefaultDateDeserialization() {
    String json = "'Dec 13, 2009, 07:18:02 AM'";
    Date extracted = gson.fromJson(json, Date.class);
    assertEqualsDate(extracted, 2009, 11, 13);
    assertEqualsTime(extracted, 7, 18, 2);
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
    assertEqualsTime(extracted, 13, 18, 2);
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
    assertEqualsTime(extracted, 13, 18, 2);
  }

// com.google.gson.functional.DefaultTypeAdaptersTest::testDefaultDateSerializationUsingBuilder
  public void testDefaultDateSerializationUsingBuilder() throws Exception {
    Gson gson = new GsonBuilder().create();
    Date now = new Date(1315806903103L);
    String json = gson.toJson(now);
    assertEquals("\"Sep 11, 2011, 10:55:03 PM\"", json);
  }

// com.google.gson.functional.DefaultTypeAdaptersTest::testDefaultDateDeserializationUsingBuilder
  public void testDefaultDateDeserializationUsingBuilder() throws Exception {
    Gson gson = new GsonBuilder().create();
    Date now = new Date(1315806903103L);
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
    Gson gson = new GsonBuilder().setDateFormat(DateFormat.FULL).setDateFormat(pattern).create();
    Date now = new Date(1315806903103L);
    String json = gson.toJson(now);
    assertEquals("\"2011-09-11\"", json);
  }

// com.google.gson.functional.DefaultTypeAdaptersTest::testDateDeserializationWithPattern
  public void testDateDeserializationWithPattern() throws Exception {
    String pattern = "yyyy-MM-dd";
    Gson gson = new GsonBuilder().setDateFormat(DateFormat.FULL).setDateFormat(pattern).create();
    Date now = new Date(1315806903103L);
    String json = gson.toJson(now);
    Date extracted = gson.fromJson(json, Date.class);
    assertEquals(now.getYear(), extracted.getYear());
    assertEquals(now.getMonth(), extracted.getMonth());
    assertEquals(now.getDay(), extracted.getDay());
  }

// com.google.gson.functional.DefaultTypeAdaptersTest::testDateSerializationWithPatternNotOverridenByTypeAdapter
  public void testDateSerializationWithPatternNotOverridenByTypeAdapter() throws Exception {
    String pattern = "yyyy-MM-dd";
    Gson gson = new GsonBuilder()
        .setDateFormat(pattern)
        .registerTypeAdapter(Date.class, new JsonDeserializer<Date>() {
          public Date deserialize(JsonElement json, Type typeOfT,
              JsonDeserializationContext context)
              throws JsonParseException {
            return new Date(1315806903103L);
          }
        })
        .create();

    Date now = new Date(1315806903103L);
    String json = gson.toJson(now);
    assertEquals("\"2011-09-11\"", json);
  }

// com.google.gson.functional.DefaultTypeAdaptersTest::testDateSerializationInCollection
  public void testDateSerializationInCollection() throws Exception {
    Type listOfDates = new TypeToken<List<Date>>() {}.getType();
    TimeZone defaultTimeZone = TimeZone.getDefault();
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    Locale defaultLocale = Locale.getDefault();
    Locale.setDefault(Locale.US);
    try {
      Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
      List<Date> dates = Arrays.asList(new Date(0));
      String json = gson.toJson(dates, listOfDates);
      assertEquals("[\"1970-01-01\"]", json);
      assertEquals(0L, gson.<List<Date>>fromJson("[\"1970-01-01\"]", listOfDates).get(0).getTime());
    } finally {
      TimeZone.setDefault(defaultTimeZone);
      Locale.setDefault(defaultLocale);
    }
  }

// com.google.gson.functional.DefaultTypeAdaptersTest::testTimestampSerialization
  public void testTimestampSerialization() throws Exception {
    TimeZone defaultTimeZone = TimeZone.getDefault();
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    Locale defaultLocale = Locale.getDefault();
    Locale.setDefault(Locale.US);
    try {
      Timestamp timestamp = new Timestamp(0L);
      Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
      String json = gson.toJson(timestamp, Timestamp.class);
      assertEquals("\"1970-01-01\"", json);
      assertEquals(0, gson.fromJson("\"1970-01-01\"", Timestamp.class).getTime());
    } finally {
      TimeZone.setDefault(defaultTimeZone);
      Locale.setDefault(defaultLocale);
    }
  }

// com.google.gson.functional.DefaultTypeAdaptersTest::testSqlDateSerialization
  public void testSqlDateSerialization() throws Exception {
    TimeZone defaultTimeZone = TimeZone.getDefault();
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    Locale defaultLocale = Locale.getDefault();
    Locale.setDefault(Locale.US);
    try {
      java.sql.Date sqlDate = new java.sql.Date(0L);
      Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
      String json = gson.toJson(sqlDate, Timestamp.class);
      assertEquals("\"1970-01-01\"", json);
      assertEquals(0, gson.fromJson("\"1970-01-01\"", java.sql.Date.class).getTime());
    } finally {
      TimeZone.setDefault(defaultTimeZone);
      Locale.setDefault(defaultLocale);
    }
  }

// com.google.gson.functional.DefaultTypeAdaptersTest::testJsonPrimitiveSerialization
  public void testJsonPrimitiveSerialization() {
    assertEquals("5", gson.toJson(new JsonPrimitive(5), JsonElement.class));
    assertEquals("true", gson.toJson(new JsonPrimitive(true), JsonElement.class));
    assertEquals("\"foo\"", gson.toJson(new JsonPrimitive("foo"), JsonElement.class));
    assertEquals("\"a\"", gson.toJson(new JsonPrimitive('a'), JsonElement.class));
  }

// com.google.gson.functional.DefaultTypeAdaptersTest::testJsonPrimitiveDeserialization
  public void testJsonPrimitiveDeserialization() {
    assertEquals(new JsonPrimitive(5), gson.fromJson("5", JsonElement.class));
    assertEquals(new JsonPrimitive(5), gson.fromJson("5", JsonPrimitive.class));
    assertEquals(new JsonPrimitive(true), gson.fromJson("true", JsonElement.class));
    assertEquals(new JsonPrimitive(true), gson.fromJson("true", JsonPrimitive.class));
    assertEquals(new JsonPrimitive("foo"), gson.fromJson("\"foo\"", JsonElement.class));
    assertEquals(new JsonPrimitive("foo"), gson.fromJson("\"foo\"", JsonPrimitive.class));
    assertEquals(new JsonPrimitive('a'), gson.fromJson("\"a\"", JsonElement.class));
    assertEquals(new JsonPrimitive('a'), gson.fromJson("\"a\"", JsonPrimitive.class));
  }

// com.google.gson.functional.DefaultTypeAdaptersTest::testJsonNullSerialization
  public void testJsonNullSerialization() {
    assertEquals("null", gson.toJson(JsonNull.INSTANCE, JsonElement.class));
    assertEquals("null", gson.toJson(JsonNull.INSTANCE, JsonNull.class));
  }

// com.google.gson.functional.DefaultTypeAdaptersTest::testNullJsonElementSerialization
  public void testNullJsonElementSerialization() {
    assertEquals("null", gson.toJson(null, JsonElement.class));
    assertEquals("null", gson.toJson(null, JsonNull.class));
  }

// com.google.gson.functional.DefaultTypeAdaptersTest::testJsonArraySerialization
  public void testJsonArraySerialization() {
    JsonArray array = new JsonArray();
    array.add(new JsonPrimitive(1));
    array.add(new JsonPrimitive(2));
    array.add(new JsonPrimitive(3));
    assertEquals("[1,2,3]", gson.toJson(array, JsonElement.class));
  }

// com.google.gson.functional.DefaultTypeAdaptersTest::testJsonArrayDeserialization
  public void testJsonArrayDeserialization() {
    JsonArray array = new JsonArray();
    array.add(new JsonPrimitive(1));
    array.add(new JsonPrimitive(2));
    array.add(new JsonPrimitive(3));

    String json = "[1,2,3]";
    assertEquals(array, gson.fromJson(json, JsonElement.class));
    assertEquals(array, gson.fromJson(json, JsonArray.class));
  }

// com.google.gson.functional.DefaultTypeAdaptersTest::testJsonObjectSerialization
  public void testJsonObjectSerialization() {
    JsonObject object = new JsonObject();
    object.add("foo", new JsonPrimitive(1));
    object.add("bar", new JsonPrimitive(2));
    assertEquals("{\"foo\":1,\"bar\":2}", gson.toJson(object, JsonElement.class));
  }

// com.google.gson.functional.DefaultTypeAdaptersTest::testJsonObjectDeserialization
  public void testJsonObjectDeserialization() {
    JsonObject object = new JsonObject();
    object.add("foo", new JsonPrimitive(1));
    object.add("bar", new JsonPrimitive(2));

    String json = "{\"foo\":1,\"bar\":2}";
    JsonElement actual = gson.fromJson(json, JsonElement.class);
    assertEquals(object, actual);

    JsonObject actualObj = gson.fromJson(json, JsonObject.class);
    assertEquals(object, actualObj);
  }

// com.google.gson.functional.DefaultTypeAdaptersTest::testJsonNullDeserialization
  public void testJsonNullDeserialization() {
    assertEquals(JsonNull.INSTANCE, gson.fromJson("null", JsonElement.class));
    assertEquals(JsonNull.INSTANCE, gson.fromJson("null", JsonNull.class));
  }

// com.google.gson.functional.DefaultTypeAdaptersTest::testJsonElementTypeMismatch
  public void testJsonElementTypeMismatch() {
    try {
      gson.fromJson("\"abc\"", JsonObject.class);
      fail();
    } catch (JsonSyntaxException expected) {
      assertEquals("Expected a com.google.gson.JsonObject but was com.google.gson.JsonPrimitive",
          expected.getMessage());
    }
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

// com.google.gson.functional.DefaultTypeAdaptersTest::testStringBuilderSerialization
  public void testStringBuilderSerialization() {
    StringBuilder sb = new StringBuilder("abc");
    String json = gson.toJson(sb);
    assertEquals("\"abc\"", json);
  }

// com.google.gson.functional.DefaultTypeAdaptersTest::testStringBuilderDeserialization
  public void testStringBuilderDeserialization() {
    StringBuilder sb = gson.fromJson("'abc'", StringBuilder.class);
    assertEquals("abc", sb.toString());
  }

// com.google.gson.functional.DefaultTypeAdaptersTest::testStringBufferSerialization
  public void testStringBufferSerialization() {
    StringBuffer sb = new StringBuffer("abc");
    String json = gson.toJson(sb);
    assertEquals("\"abc\"", json);
  }

// com.google.gson.functional.DefaultTypeAdaptersTest::testStringBufferDeserialization
  public void testStringBufferDeserialization() {
    StringBuffer sb = gson.fromJson("'abc'", StringBuffer.class);
    assertEquals("abc", sb.toString());
  }

// com.google.gson.functional.DelegateTypeAdapterTest::testDelegateInvoked
  public void testDelegateInvoked() {
    List<BagOfPrimitives> bags = new ArrayList<BagOfPrimitives>();
    for (int i = 0; i < 10; ++i) {
      bags.add(new BagOfPrimitives(i, i, i % 2 == 0, String.valueOf(i)));
    }
    String json = gson.toJson(bags);
    bags = gson.fromJson(json, new TypeToken<List<BagOfPrimitives>>(){}.getType());
    
    assertEquals(51, stats.numReads);
    assertEquals(51, stats.numWrites);
  }

// com.google.gson.functional.DelegateTypeAdapterTest::testDelegateInvokedOnStrings
  public void testDelegateInvokedOnStrings() {
    String[] bags = {"1", "2", "3", "4"};
    String json = gson.toJson(bags);
    bags = gson.fromJson(json, String[].class);
    
    assertEquals(5, stats.numReads);
    assertEquals(5, stats.numWrites);
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

// com.google.gson.functional.EnumTest::testEnumSubclassWithRegisteredTypeAdapter
  public void testEnumSubclassWithRegisteredTypeAdapter() {
    gson = new GsonBuilder()
        .registerTypeHierarchyAdapter(Roshambo.class, new MyEnumTypeAdapter())
        .create();
    assertFalse(Roshambo.class == Roshambo.ROCK.getClass());
    assertEquals("\"123ROCK\"", gson.toJson(Roshambo.ROCK));
    assertEquals("[\"123ROCK\",\"123PAPER\",\"123SCISSORS\"]", gson.toJson(EnumSet.allOf(Roshambo.class)));
    assertEquals(Roshambo.ROCK, gson.fromJson("\"123ROCK\"", Roshambo.class));
    assertEquals(EnumSet.allOf(Roshambo.class),
        gson.fromJson("[\"123ROCK\",\"123PAPER\",\"123SCISSORS\"]", new TypeToken<Set<Roshambo>>() {}.getType()));
  }

// com.google.gson.functional.EnumTest::testEnumSubclassAsParameterizedType
  public void testEnumSubclassAsParameterizedType() {
    Collection<Roshambo> list = new ArrayList<Roshambo>();
    list.add(Roshambo.ROCK);
    list.add(Roshambo.PAPER);

    String json = gson.toJson(list);
    assertEquals("[\"ROCK\",\"PAPER\"]", json);

    Type collectionType = new TypeToken<Collection<Roshambo>>() {}.getType();
    Collection<Roshambo> actualJsonList = gson.fromJson(json, collectionType);
    MoreAsserts.assertContains(actualJsonList, Roshambo.ROCK);
    MoreAsserts.assertContains(actualJsonList, Roshambo.PAPER);
  }

// com.google.gson.functional.EnumTest::testEnumCaseMapping
  public void testEnumCaseMapping() {
    assertEquals(Gender.MALE, gson.fromJson("\"boy\"", Gender.class));
    assertEquals("\"boy\"", gson.toJson(Gender.MALE, Gender.class));
  }

// com.google.gson.functional.EnumTest::testEnumSet
  public void testEnumSet() {
    EnumSet<Roshambo> foo = EnumSet.of(Roshambo.ROCK, Roshambo.PAPER);
    String json = gson.toJson(foo);
    Type type = new TypeToken<EnumSet<Roshambo>>() {}.getType();
    EnumSet<Roshambo> bar = gson.fromJson(json, type);
    assertTrue(bar.contains(Roshambo.ROCK));
    assertTrue(bar.contains(Roshambo.PAPER));
    assertFalse(bar.contains(Roshambo.SCISSORS));
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

// com.google.gson.functional.EscapingTest::testGsonDoubleDeserialization
  public void testGsonDoubleDeserialization() {
    BagOfPrimitives expected = new BagOfPrimitives(3L, 4, true, "value1");
    String json = gson.toJson(gson.toJson(expected));
    String value = gson.fromJson(json, String.class);
    BagOfPrimitives actual = gson.fromJson(value, BagOfPrimitives.class);
    assertEquals(expected, actual);
  }

// com.google.gson.functional.ExclusionStrategyFunctionalTest::testExclusionStrategySerialization
  public void testExclusionStrategySerialization() throws Exception {
    Gson gson = createGson(new MyExclusionStrategy(String.class), true);
    String json = gson.toJson(src);
    assertFalse(json.contains("\"stringField\""));
    assertFalse(json.contains("\"annotatedField\""));
    assertTrue(json.contains("\"longField\""));
  }

// com.google.gson.functional.ExclusionStrategyFunctionalTest::testExclusionStrategySerializationDoesNotImpactDeserialization
  public void testExclusionStrategySerializationDoesNotImpactDeserialization() {
    String json = "{\"annotatedField\":1,\"stringField\":\"x\",\"longField\":2}";
    Gson gson = createGson(new MyExclusionStrategy(String.class), true);
    SampleObjectForTest value = gson.fromJson(json, SampleObjectForTest.class);
    assertEquals(1, value.annotatedField);
    assertEquals("x", value.stringField);
    assertEquals(2, value.longField);
  }

// com.google.gson.functional.ExclusionStrategyFunctionalTest::testExclusionStrategyDeserialization
  public void testExclusionStrategyDeserialization() throws Exception {
    Gson gson = createGson(new MyExclusionStrategy(String.class), false);
    JsonObject json = new JsonObject();
    json.add("annotatedField", new JsonPrimitive(src.annotatedField + 5));
    json.add("stringField", new JsonPrimitive(src.stringField + "blah,blah"));
    json.add("longField", new JsonPrimitive(1212311L));

    SampleObjectForTest target = gson.fromJson(json, SampleObjectForTest.class);
    assertEquals(1212311L, target.longField);

    
    assertEquals(src.annotatedField, target.annotatedField);
    assertEquals(src.stringField, target.stringField);
  }

// com.google.gson.functional.ExclusionStrategyFunctionalTest::testExclusionStrategySerializationDoesNotImpactSerialization
  public void testExclusionStrategySerializationDoesNotImpactSerialization() throws Exception {
    Gson gson = createGson(new MyExclusionStrategy(String.class), false);
    String json = gson.toJson(src);
    assertTrue(json.contains("\"stringField\""));
    assertTrue(json.contains("\"annotatedField\""));
    assertTrue(json.contains("\"longField\""));
  }

// com.google.gson.functional.ExclusionStrategyFunctionalTest::testExclusionStrategyWithMode
  public void testExclusionStrategyWithMode() throws Exception {
    SampleObjectForTest testObj = new SampleObjectForTest(
        src.annotatedField + 5, src.stringField + "blah,blah",
        src.longField + 655L);

    Gson gson = createGson(new MyExclusionStrategy(String.class), false);
    JsonObject json = gson.toJsonTree(testObj).getAsJsonObject();
    assertEquals(testObj.annotatedField, json.get("annotatedField").getAsInt());
    assertEquals(testObj.stringField, json.get("stringField").getAsString());
    assertEquals(testObj.longField, json.get("longField").getAsLong());

    SampleObjectForTest target = gson.fromJson(json, SampleObjectForTest.class);
    assertEquals(testObj.longField, target.longField);

    
    assertEquals(src.annotatedField, target.annotatedField);
    assertEquals(src.stringField, target.stringField);
  }

// com.google.gson.functional.ExclusionStrategyFunctionalTest::testExcludeTopLevelClassSerialization
  public void testExcludeTopLevelClassSerialization() {
    Gson gson = new GsonBuilder()
        .addSerializationExclusionStrategy(EXCLUDE_SAMPLE_OBJECT_FOR_TEST)
        .create();
    assertEquals("null", gson.toJson(new SampleObjectForTest(), SampleObjectForTest.class));
  }

// com.google.gson.functional.ExclusionStrategyFunctionalTest::testExcludeTopLevelClassSerializationDoesNotImpactDeserialization
  public void testExcludeTopLevelClassSerializationDoesNotImpactDeserialization() {
    Gson gson = new GsonBuilder()
        .addSerializationExclusionStrategy(EXCLUDE_SAMPLE_OBJECT_FOR_TEST)
        .create();
    String json = "{\"annotatedField\":1,\"stringField\":\"x\",\"longField\":2}";
    SampleObjectForTest value = gson.fromJson(json, SampleObjectForTest.class);
    assertEquals(1, value.annotatedField);
    assertEquals("x", value.stringField);
    assertEquals(2, value.longField);
  }

// com.google.gson.functional.ExclusionStrategyFunctionalTest::testExcludeTopLevelClassDeserialization
  public void testExcludeTopLevelClassDeserialization() {
    Gson gson = new GsonBuilder()
        .addDeserializationExclusionStrategy(EXCLUDE_SAMPLE_OBJECT_FOR_TEST)
        .create();
    String json = "{\"annotatedField\":1,\"stringField\":\"x\",\"longField\":2}";
    SampleObjectForTest value = gson.fromJson(json, SampleObjectForTest.class);
    assertNull(value);
  }

// com.google.gson.functional.ExclusionStrategyFunctionalTest::testExcludeTopLevelClassDeserializationDoesNotImpactSerialization
  public void testExcludeTopLevelClassDeserializationDoesNotImpactSerialization() {
    Gson gson = new GsonBuilder()
        .addDeserializationExclusionStrategy(EXCLUDE_SAMPLE_OBJECT_FOR_TEST)
        .create();
    String json = gson.toJson(new SampleObjectForTest(), SampleObjectForTest.class);
    assertTrue(json.contains("\"stringField\""));
    assertTrue(json.contains("\"annotatedField\""));
    assertTrue(json.contains("\"longField\""));
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
    assertEquals("null", result);
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

// com.google.gson.functional.FieldNamingTest::testIdentity
  public void testIdentity() {
    Gson gson = getGsonWithNamingPolicy(IDENTITY);
    assertEquals("{'lowerCamel':1,'UpperCamel':2,'_lowerCamelLeadingUnderscore':3," +
        "'_UpperCamelLeadingUnderscore':4,'lower_words':5,'UPPER_WORDS':6," +
        "'annotatedName':7,'lowerId':8}",
        gson.toJson(new TestNames()).replace('\"', '\''));
  }

// com.google.gson.functional.FieldNamingTest::testUpperCamelCase
  public void testUpperCamelCase() {
    Gson gson = getGsonWithNamingPolicy(UPPER_CAMEL_CASE);
    assertEquals("{'LowerCamel':1,'UpperCamel':2,'_LowerCamelLeadingUnderscore':3," +
        "'_UpperCamelLeadingUnderscore':4,'Lower_words':5,'UPPER_WORDS':6," +
        "'annotatedName':7,'LowerId':8}",
        gson.toJson(new TestNames()).replace('\"', '\''));
  }

// com.google.gson.functional.FieldNamingTest::testUpperCamelCaseWithSpaces
  public void testUpperCamelCaseWithSpaces() {
    Gson gson = getGsonWithNamingPolicy(UPPER_CAMEL_CASE_WITH_SPACES);
    assertEquals("{'Lower Camel':1,'Upper Camel':2,'_Lower Camel Leading Underscore':3," +
        "'_ Upper Camel Leading Underscore':4,'Lower_words':5,'U P P E R_ W O R D S':6," +
        "'annotatedName':7,'Lower Id':8}",
        gson.toJson(new TestNames()).replace('\"', '\''));
  }

// com.google.gson.functional.FieldNamingTest::testLowerCaseWithUnderscores
  public void testLowerCaseWithUnderscores() {
    Gson gson = getGsonWithNamingPolicy(LOWER_CASE_WITH_UNDERSCORES);
    assertEquals("{'lower_camel':1,'upper_camel':2,'_lower_camel_leading_underscore':3," +
        "'__upper_camel_leading_underscore':4,'lower_words':5,'u_p_p_e_r__w_o_r_d_s':6," +
        "'annotatedName':7,'lower_id':8}",
        gson.toJson(new TestNames()).replace('\"', '\''));
  }

// com.google.gson.functional.FieldNamingTest::testLowerCaseWithDashes
  public void testLowerCaseWithDashes() {
    Gson gson = getGsonWithNamingPolicy(LOWER_CASE_WITH_DASHES);
    assertEquals("{'lower-camel':1,'upper-camel':2,'_lower-camel-leading-underscore':3," +
        "'_-upper-camel-leading-underscore':4,'lower_words':5,'u-p-p-e-r_-w-o-r-d-s':6," +
        "'annotatedName':7,'lower-id':8}",
        gson.toJson(new TestNames()).replace('\"', '\''));
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

// com.google.gson.functional.InheritanceTest::testClassWithBaseCollectionFieldSerialization
  public void testClassWithBaseCollectionFieldSerialization() {
    Collection<Base> baseClasses = new ArrayList<Base>();
    baseClasses.add(new Sub());
    baseClasses.add(new Sub());
    ClassWithBaseCollectionField sub = new ClassWithBaseCollectionField(baseClasses);
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
        @Override public Base createInstance(Type type) {
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
      @Override public Base createInstance(Type type) {
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
      @Override public Base createInstance(Type type) {
        return new Sub();
      }
    })
    .create();
    String json = "{base:{baseName:'Base',subName:'SubRevised'}}";
    ClassWithBaseField target = gson.fromJson(json, ClassWithBaseField.class);
    assertTrue(target.base instanceof Sub);
    assertEquals(Sub.SUB_NAME, ((Sub)target.base).subName);
  }

// com.google.gson.functional.InstanceCreatorTest::testInstanceCreatorForCollectionType
  public void testInstanceCreatorForCollectionType() {
    @SuppressWarnings("serial")
    class SubArrayList<T> extends ArrayList<T> {}
    InstanceCreator<List<String>> listCreator = new InstanceCreator<List<String>>() {
      @Override public List<String> createInstance(Type type) {
        return new SubArrayList<String>();
      }
    };
    Type listOfStringType = new TypeToken<List<String>>() {}.getType();
    Gson gson = new GsonBuilder()
        .registerTypeAdapter(listOfStringType, listCreator)
        .create();
    List<String> list = gson.fromJson("[\"a\"]", listOfStringType);
    assertEquals(SubArrayList.class, list.getClass());
  }

// com.google.gson.functional.InstanceCreatorTest::testInstanceCreatorForParametrizedType
  public void testInstanceCreatorForParametrizedType() throws Exception {
    @SuppressWarnings("serial")
    class SubTreeSet<T> extends TreeSet<T> {}
    InstanceCreator<SortedSet> sortedSetCreator = new InstanceCreator<SortedSet>() {
      @Override public SortedSet createInstance(Type type) {
        return new SubTreeSet();
      }
    };
    Gson gson = new GsonBuilder()
        .registerTypeAdapter(SortedSet.class, sortedSetCreator)
        .create();

    Type sortedSetType = new TypeToken<SortedSet<String>>() {}.getType();
    SortedSet<String> set = gson.fromJson("[\"a\"]", sortedSetType);
    assertEquals(set.first(), "a");
    assertEquals(SubTreeSet.class, set.getClass());

    set = gson.fromJson("[\"b\"]", SortedSet.class);
    assertEquals(set.first(), "b");
    assertEquals(SubTreeSet.class, set.getClass());
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

// com.google.gson.functional.InternationalizationTest::testStringsWithRawChineseCharactersDeserialization
  public void testStringsWithRawChineseCharactersDeserialization() throws Exception {
    String expected = "好好好";
    String json = "\"" + expected + "\"";
    String actual = gson.fromJson(json, String.class);
    assertEquals(expected, actual);
  }

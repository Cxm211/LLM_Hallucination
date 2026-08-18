// buggy code
  public static <T1> TypeAdapterFactory newTypeHierarchyFactory(
      final Class<T1> clazz, final TypeAdapter<T1> typeAdapter) {
    return new TypeAdapterFactory() {
      @SuppressWarnings("unchecked")
      public <T2> TypeAdapter<T2> create(Gson gson, TypeToken<T2> typeToken) {
        final Class<? super T2> requestedType = typeToken.getRawType();
        if (!clazz.isAssignableFrom(requestedType)) {
          return null;
        }
        return (TypeAdapter<T2>) typeAdapter;

      }
      @Override public String toString() {
        return "Factory[typeHierarchy=" + clazz.getName() + ",adapter=" + typeAdapter + "]";
      }
    };
  }

// relevant test
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
    assertEquals(Sub.SUB_NAME, ((Sub)target.base).subName);
  }

// com.google.gson.functional.InstanceCreatorTest::testInstanceCreatorForCollectionType
  public void testInstanceCreatorForCollectionType() {
    @SuppressWarnings("serial")
    class SubArrayList<T> extends ArrayList<T> {}
    InstanceCreator<List<String>> listCreator = new InstanceCreator<List<String>>() {
      public List<String> createInstance(Type type) {
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
      public SortedSet createInstance(Type type) {
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

// com.google.gson.functional.InternationalizationTest::testStringsWithUnicodeChineseCharactersSerialization
  public void testStringsWithUnicodeChineseCharactersSerialization() throws Exception {
    String target = "\u597d\u597d\u597d";
    String json = gson.toJson(target);
    String expected = "\"\u597d\u597d\u597d\"";
    assertEquals(expected, json);
  }

// com.google.gson.functional.InternationalizationTest::testStringsWithUnicodeChineseCharactersDeserialization
  public void testStringsWithUnicodeChineseCharactersDeserialization() throws Exception {
    String expected = "\u597d\u597d\u597d";
    String json = "\"" + expected + "\"";
    String actual = gson.fromJson(json, String.class);
    assertEquals(expected, actual);
  }

// com.google.gson.functional.InternationalizationTest::testStringsWithUnicodeChineseCharactersEscapedDeserialization
  public void testStringsWithUnicodeChineseCharactersEscapedDeserialization() throws Exception {
    String actual = gson.fromJson("'\\u597d\\u597d\\u597d'", String.class);
    assertEquals("\u597d\u597d\u597d", actual);
  }

// com.google.gson.functional.JsonAdapterAnnotationOnClassesTest::testJsonAdapterInvoked
  public void testJsonAdapterInvoked() {
    Gson gson = new Gson();
    String json = gson.toJson(new A("bar"));
    assertEquals("\"jsonAdapter\"", json);

   
    json = gson.toJson(new User("Inderjeet", "Singh"));
    assertEquals("{\"name\":\"Inderjeet Singh\"}", json);
    User user = gson.fromJson("{'name':'Joel Leitch'}", User.class);
    assertEquals("Joel", user.firstName);
    assertEquals("Leitch", user.lastName);

    json = gson.toJson(Foo.BAR);
    assertEquals("\"bar\"", json);
    Foo baz = gson.fromJson("\"baz\"", Foo.class);
    assertEquals(Foo.BAZ, baz);
  }

// com.google.gson.functional.JsonAdapterAnnotationOnClassesTest::testJsonAdapterFactoryInvoked
  public void testJsonAdapterFactoryInvoked() {
    Gson gson = new Gson();
    String json = gson.toJson(new C("bar"));
    assertEquals("\"jsonAdapterFactory\"", json);
    C c = gson.fromJson("\"bar\"", C.class);
    assertEquals("jsonAdapterFactory", c.value);
  }

// com.google.gson.functional.JsonAdapterAnnotationOnClassesTest::testRegisteredAdapterOverridesJsonAdapter
  public void testRegisteredAdapterOverridesJsonAdapter() {
    TypeAdapter<A> typeAdapter = new TypeAdapter<A>() {
      @Override public void write(JsonWriter out, A value) throws IOException {
        out.value("registeredAdapter");
      }
      @Override public A read(JsonReader in) throws IOException {
        return new A(in.nextString());
      }
    };
    Gson gson = new GsonBuilder()
      .registerTypeAdapter(A.class, typeAdapter)
      .create();
    String json = gson.toJson(new A("abcd"));
    assertEquals("\"registeredAdapter\"", json);
  }

// com.google.gson.functional.JsonAdapterAnnotationOnClassesTest::testRegisteredSerializerOverridesJsonAdapter
  public void testRegisteredSerializerOverridesJsonAdapter() {
    JsonSerializer<A> serializer = new JsonSerializer<A>() {
      public JsonElement serialize(A src, Type typeOfSrc,
          JsonSerializationContext context) {
        return new JsonPrimitive("registeredSerializer");
      }
    };
    Gson gson = new GsonBuilder()
      .registerTypeAdapter(A.class, serializer)
      .create();
    String json = gson.toJson(new A("abcd"));
    assertEquals("\"registeredSerializer\"", json);
    A target = gson.fromJson("abcd", A.class);
    assertEquals("jsonAdapter", target.value);
  }

// com.google.gson.functional.JsonAdapterAnnotationOnClassesTest::testRegisteredDeserializerOverridesJsonAdapter
  public void testRegisteredDeserializerOverridesJsonAdapter() {
    JsonDeserializer<A> deserializer = new JsonDeserializer<A>() {
      public A deserialize(JsonElement json, Type typeOfT,
          JsonDeserializationContext context) throws JsonParseException {
        return new A("registeredDeserializer");
      }
    };
    Gson gson = new GsonBuilder()
      .registerTypeAdapter(A.class, deserializer)
      .create();
    String json = gson.toJson(new A("abcd"));
    assertEquals("\"jsonAdapter\"", json);
    A target = gson.fromJson("abcd", A.class);
    assertEquals("registeredDeserializer", target.value);
  }

// com.google.gson.functional.JsonAdapterAnnotationOnClassesTest::testIncorrectTypeAdapterFails
  public void testIncorrectTypeAdapterFails() {
    try {
      String json = new Gson().toJson(new ClassWithIncorrectJsonAdapter("bar"));
      fail(json);
    } catch (ClassCastException expected) {}
  }

// com.google.gson.functional.JsonAdapterAnnotationOnClassesTest::testSuperclassTypeAdapterNotInvoked
  public void testSuperclassTypeAdapterNotInvoked() {
    String json = new Gson().toJson(new B("bar"));
    assertFalse(json.contains("jsonAdapter"));
  }

// com.google.gson.functional.JsonAdapterAnnotationOnFieldsTest::testClassAnnotationAdapterTakesPrecedenceOverDefault
  public void testClassAnnotationAdapterTakesPrecedenceOverDefault() {
    Gson gson = new Gson();
    String json = gson.toJson(new Computer(new User("Inderjeet Singh")));
    assertEquals("{\"user\":\"UserClassAnnotationAdapter\"}", json);
    Computer computer = gson.fromJson("{'user':'Inderjeet Singh'}", Computer.class);
    assertEquals("UserClassAnnotationAdapter", computer.user.name);
  }

// com.google.gson.functional.JsonAdapterAnnotationOnFieldsTest::testClassAnnotationAdapterFactoryTakesPrecedenceOverDefault
  public void testClassAnnotationAdapterFactoryTakesPrecedenceOverDefault() {
    Gson gson = new Gson();
    String json = gson.toJson(new Gizmo(new Part("Part")));
    assertEquals("{\"part\":\"GizmoPartTypeAdapterFactory\"}", json);
    Gizmo computer = gson.fromJson("{'part':'Part'}", Gizmo.class);
    assertEquals("GizmoPartTypeAdapterFactory", computer.part.name);
  }

// com.google.gson.functional.JsonAdapterAnnotationOnFieldsTest::testRegisteredTypeAdapterTakesPrecedenceOverClassAnnotationAdapter
  public void testRegisteredTypeAdapterTakesPrecedenceOverClassAnnotationAdapter() {
    Gson gson = new GsonBuilder()
        .registerTypeAdapter(User.class, new RegisteredUserAdapter())
        .create();
    String json = gson.toJson(new Computer(new User("Inderjeet Singh")));
    assertEquals("{\"user\":\"RegisteredUserAdapter\"}", json);
    Computer computer = gson.fromJson("{'user':'Inderjeet Singh'}", Computer.class);
    assertEquals("RegisteredUserAdapter", computer.user.name);
  }

// com.google.gson.functional.JsonAdapterAnnotationOnFieldsTest::testFieldAnnotationTakesPrecedenceOverRegisteredTypeAdapter
  public void testFieldAnnotationTakesPrecedenceOverRegisteredTypeAdapter() {
    Gson gson = new GsonBuilder()
      .registerTypeAdapter(Part.class, new TypeAdapter<Part>() {
        @Override public void write(JsonWriter out, Part part) throws IOException {
          throw new AssertionError();
        }

        @Override public Part read(JsonReader in) throws IOException {
          throw new AssertionError();
        }
      }).create();
    String json = gson.toJson(new Gadget(new Part("screen")));
    assertEquals("{\"part\":\"PartJsonFieldAnnotationAdapter\"}", json);
    Gadget gadget = gson.fromJson("{'part':'screen'}", Gadget.class);
    assertEquals("PartJsonFieldAnnotationAdapter", gadget.part.name);
  }

// com.google.gson.functional.JsonAdapterAnnotationOnFieldsTest::testFieldAnnotationTakesPrecedenceOverClassAnnotation
  public void testFieldAnnotationTakesPrecedenceOverClassAnnotation() {
    Gson gson = new Gson();
    String json = gson.toJson(new Computer2(new User("Inderjeet Singh")));
    assertEquals("{\"user\":\"UserFieldAnnotationAdapter\"}", json);
    Computer2 target = gson.fromJson("{'user':'Interjeet Singh'}", Computer2.class);
    assertEquals("UserFieldAnnotationAdapter", target.user.name);
  }

// com.google.gson.functional.JsonAdapterAnnotationOnFieldsTest::testJsonAdapterInvokedOnlyForAnnotatedFields
  public void testJsonAdapterInvokedOnlyForAnnotatedFields() {
    Gson gson = new Gson();
    String json = "{'part1':'name','part2':{'name':'name2'}}";
    GadgetWithTwoParts gadget = gson.fromJson(json, GadgetWithTwoParts.class);
    assertEquals("PartJsonFieldAnnotationAdapter", gadget.part1.name);
    assertEquals("name2", gadget.part2.name);
  }

// com.google.gson.functional.JsonArrayTest::testStringPrimitiveAddition
  public void testStringPrimitiveAddition() {
    JsonArray jsonArray = new JsonArray();

    jsonArray.add("Hello");
    jsonArray.add("Goodbye");
    jsonArray.add("Thank you");
    jsonArray.add((String) null);
    jsonArray.add("Yes");

    assertEquals("[\"Hello\",\"Goodbye\",\"Thank you\",null,\"Yes\"]", jsonArray.toString());
  }

// com.google.gson.functional.JsonArrayTest::testIntegerPrimitiveAddition
  public void testIntegerPrimitiveAddition() {
    JsonArray jsonArray = new JsonArray();

    int x = 1;
    jsonArray.add(x);

    x = 2;
    jsonArray.add(x);

    x = -3;
    jsonArray.add(x);

    jsonArray.add((Integer) null);

    x = 4;
    jsonArray.add(x);

    x = 0;
    jsonArray.add(x);

    assertEquals("[1,2,-3,null,4,0]", jsonArray.toString());
  }

// com.google.gson.functional.JsonArrayTest::testDoublePrimitiveAddition
  public void testDoublePrimitiveAddition() {
    JsonArray jsonArray = new JsonArray();

    double x = 1.0;
    jsonArray.add(x);

    x = 2.13232;
    jsonArray.add(x);

    x = 0.121;
    jsonArray.add(x);

    jsonArray.add((Double) null);

    x = -0.00234;
    jsonArray.add(x);

    jsonArray.add((Double) null);

    assertEquals("[1.0,2.13232,0.121,null,-0.00234,null]", jsonArray.toString());
  }

// com.google.gson.functional.JsonArrayTest::testBooleanPrimitiveAddition
  public void testBooleanPrimitiveAddition() {
    JsonArray jsonArray = new JsonArray();

    jsonArray.add(true);
    jsonArray.add(true);
    jsonArray.add(false);
    jsonArray.add(false);
    jsonArray.add((Boolean) null);
    jsonArray.add(true);

    assertEquals("[true,true,false,false,null,true]", jsonArray.toString());
  }

// com.google.gson.functional.JsonArrayTest::testCharPrimitiveAddition
  public void testCharPrimitiveAddition() {
    JsonArray jsonArray = new JsonArray();

    jsonArray.add('a');
    jsonArray.add('e');
    jsonArray.add('i');
    jsonArray.add((char) 111);
    jsonArray.add((Character) null);
    jsonArray.add('u');
    jsonArray.add("and sometimes Y");

    assertEquals("[\"a\",\"e\",\"i\",\"o\",null,\"u\",\"and sometimes Y\"]", jsonArray.toString());
  }

// com.google.gson.functional.JsonArrayTest::testMixedPrimitiveAddition
  public void testMixedPrimitiveAddition() {
    JsonArray jsonArray = new JsonArray();

    jsonArray.add('a');
    jsonArray.add("apple");
    jsonArray.add(12121);
    jsonArray.add((char) 111);
    jsonArray.add((Boolean) null);
    jsonArray.add((Character) null);
    jsonArray.add(12.232);
    jsonArray.add(BigInteger.valueOf(2323));

    assertEquals("[\"a\",\"apple\",12121,\"o\",null,null,12.232,2323]", jsonArray.toString());
  }

// com.google.gson.functional.JsonArrayTest::testNullPrimitiveAddition
  public void testNullPrimitiveAddition() {
    JsonArray jsonArray = new JsonArray();

    jsonArray.add((Character) null);
    jsonArray.add((Boolean) null);
    jsonArray.add((Integer) null);
    jsonArray.add((Double) null);
    jsonArray.add((Float) null);
    jsonArray.add((BigInteger) null);
    jsonArray.add((String) null);
    jsonArray.add((Boolean) null);
    jsonArray.add((Number) null);

    assertEquals("[null,null,null,null,null,null,null,null,null]", jsonArray.toString());
  }

// com.google.gson.functional.JsonArrayTest::testSameAddition
  public void testSameAddition() {
    JsonArray jsonArray = new JsonArray();

    jsonArray.add('a');
    jsonArray.add('a');
    jsonArray.add(true);
    jsonArray.add(true);
    jsonArray.add(1212);
    jsonArray.add(1212);
    jsonArray.add(34.34);
    jsonArray.add(34.34);
    jsonArray.add((Boolean) null);
    jsonArray.add((Boolean) null);

    assertEquals("[\"a\",\"a\",true,true,1212,1212,34.34,34.34,null,null]", jsonArray.toString());
  }

// com.google.gson.functional.JsonParserTest::testParseInvalidJson
  public void testParseInvalidJson() {
    try {
      gson.fromJson("[[]", Object[].class);
      fail();
    } catch (JsonSyntaxException expected) { }
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

// com.google.gson.functional.JsonParserTest::testExtraCommasInArrays
  public void testExtraCommasInArrays() {
    Type type = new TypeToken<List<String>>() {}.getType();
    assertEquals(list("a", null, "b", null, null), gson.fromJson("[a,,b,,]", type));
    assertEquals(list(null, null), gson.fromJson("[,]", type));
    assertEquals(list("a", null), gson.fromJson("[a,]", type));
  }

// com.google.gson.functional.JsonParserTest::testExtraCommasInMaps
  public void testExtraCommasInMaps() {
    Type type = new TypeToken<Map<String, String>>() {}.getType();
    try {
      gson.fromJson("{a:b,}", type);
      fail();
    } catch (JsonSyntaxException expected) {
    }
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

// com.google.gson.functional.JsonTreeTest::testJsonTreeNull
  public void testJsonTreeNull() {
    BagOfPrimitives bag = new BagOfPrimitives(10L, 5, false, null);
    JsonObject jsonElement = (JsonObject) gson.toJsonTree(bag, BagOfPrimitives.class);
    assertFalse(jsonElement.has("stringValue"));
  }

// com.google.gson.functional.MapAsArrayTypeAdapterTest::testSerializeComplexMapWithTypeAdapter
  public void testSerializeComplexMapWithTypeAdapter() {
    Type type = new TypeToken<Map<Point, String>>() {}.getType();
    Gson gson = new GsonBuilder()
        .enableComplexMapKeySerialization()
        .create();

    Map<Point, String> original = new LinkedHashMap<Point, String>();
    original.put(new Point(5, 5), "a");
    original.put(new Point(8, 8), "b");
    String json = gson.toJson(original, type);
    assertEquals("[[{\"x\":5,\"y\":5},\"a\"],[{\"x\":8,\"y\":8},\"b\"]]", json);
    assertEquals(original, gson.<Map<Point, String>>fromJson(json, type));

    
    Map<String, Boolean> otherMap = new LinkedHashMap<String, Boolean>();
    otherMap.put("t", true);
    otherMap.put("f", false);
    assertEquals("{\"t\":true,\"f\":false}",
        gson.toJson(otherMap, Map.class));
    assertEquals("{\"t\":true,\"f\":false}",
        gson.toJson(otherMap, new TypeToken<Map<String, Boolean>>() {}.getType()));
    assertEquals(otherMap, gson.<Object>fromJson("{\"t\":true,\"f\":false}",
        new TypeToken<Map<String, Boolean>>() {}.getType()));
  }

// com.google.gson.functional.MapAsArrayTypeAdapterTest::testTwoTypesCollapseToOneDeserialize
  public void testTwoTypesCollapseToOneDeserialize() {
    Gson gson = new GsonBuilder()
        .enableComplexMapKeySerialization()
        .create();

    String s = "[[\"1.00\",\"a\"],[\"1.0\",\"b\"]]";
    try {
      gson.fromJson(s, new TypeToken<Map<Double, String>>() {}.getType());
      fail();
    } catch (JsonSyntaxException expected) {
    }
  }

// com.google.gson.functional.MapAsArrayTypeAdapterTest::testMultipleEnableComplexKeyRegistrationHasNoEffect
  public void testMultipleEnableComplexKeyRegistrationHasNoEffect() throws Exception {
    Type type = new TypeToken<Map<Point, String>>() {}.getType();
    Gson gson = new GsonBuilder()
        .enableComplexMapKeySerialization()
        .enableComplexMapKeySerialization()
        .create();

    Map<Point, String> original = new LinkedHashMap<Point, String>();
    original.put(new Point(6, 5), "abc");
    original.put(new Point(1, 8), "def");
    String json = gson.toJson(original, type);
    assertEquals("[[{\"x\":6,\"y\":5},\"abc\"],[{\"x\":1,\"y\":8},\"def\"]]", json);
    assertEquals(original, gson.<Map<Point, String>>fromJson(json, type));
  }

// com.google.gson.functional.MapAsArrayTypeAdapterTest::testMapWithTypeVariableSerialization
  public void testMapWithTypeVariableSerialization() {
    Gson gson = new GsonBuilder().enableComplexMapKeySerialization().create();
    PointWithProperty<Point> map = new PointWithProperty<Point>();
    map.map.put(new Point(2, 3), new Point(4, 5));
    Type type = new TypeToken<PointWithProperty<Point>>(){}.getType();
    String json = gson.toJson(map, type);
    assertEquals("{\"map\":[[{\"x\":2,\"y\":3},{\"x\":4,\"y\":5}]]}", json);
  }

// com.google.gson.functional.MapAsArrayTypeAdapterTest::testMapWithTypeVariableDeserialization
  public void testMapWithTypeVariableDeserialization() {
    Gson gson = new GsonBuilder().enableComplexMapKeySerialization().create();
    String json = "{map:[[{x:2,y:3},{x:4,y:5}]]}";
    Type type = new TypeToken<PointWithProperty<Point>>(){}.getType();
    PointWithProperty<Point> map = gson.fromJson(json, type);
    Point key = map.map.keySet().iterator().next();
    Point value = map.map.values().iterator().next();
    assertEquals(new Point(2, 3), key);
    assertEquals(new Point(4, 5), value);
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
    assertEquals(123, map.get("null").intValue());
    assertNull(map.get(null));

    map = gson.fromJson("{null:123}", typeOfMap);
    assertEquals(1, map.size());
    assertEquals(123, map.get("null").intValue());
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

// com.google.gson.functional.MapTest::testHashMapDeserialization
  public void testHashMapDeserialization() throws Exception {
    Type typeOfMap = new TypeToken<HashMap<Integer, String>>() {}.getType();
    HashMap<Integer, String> map = gson.fromJson("{\"123\":\"456\"}", typeOfMap);
    assertEquals(1, map.size());
    assertTrue(map.containsKey(123));
    assertEquals("456", map.get(123));
  }

// com.google.gson.functional.MapTest::testSortedMap
  public void testSortedMap() throws Exception {
    Type typeOfMap = new TypeToken<SortedMap<Integer, String>>() {}.getType();
    SortedMap<Integer, String> map = gson.fromJson("{\"123\":\"456\"}", typeOfMap);
    assertEquals(1, map.size());
    assertTrue(map.containsKey(123));
    assertEquals("456", map.get(123));
  }

// com.google.gson.functional.MapTest::testParameterizedMapSubclassSerialization
  public void testParameterizedMapSubclassSerialization() {
    MyParameterizedMap<String, String> map = new MyParameterizedMap<String, String>(10);
    map.put("a", "b");
    Type type = new TypeToken<MyParameterizedMap<String, String>>() {}.getType();
    String json = gson.toJson(map, type);
    assertTrue(json.contains("\"a\":\"b\""));
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

// com.google.gson.functional.MapTest::testMapSubclassDeserialization
  public void testMapSubclassDeserialization() {
    Gson gson = new GsonBuilder().registerTypeAdapter(MyMap.class, new InstanceCreator<MyMap>() {
      public MyMap createInstance(Type type) {
        return new MyMap();
      }
    }).create();
    String json = "{\"a\":1,\"b\":2}";
    MyMap map = gson.fromJson(json, MyMap.class);
    assertEquals("1", map.get("a"));
    assertEquals("2", map.get("b"));
  }

// com.google.gson.functional.MapTest::testCustomSerializerForSpecificMapType
  public void testCustomSerializerForSpecificMapType() {
    Type type = $Gson$Types.newParameterizedTypeWithOwner(
        null, Map.class, String.class, Long.class);
    Gson gson = new GsonBuilder()
        .registerTypeAdapter(type, new JsonSerializer<Map<String, Long>>() {
          public JsonElement serialize(Map<String, Long> src, Type typeOfSrc,
              JsonSerializationContext context) {
            JsonArray array = new JsonArray();
            for (long value : src.values()) {
              array.add(new JsonPrimitive(value));
            }
            return array;
          }
        }).create();

    Map<String, Long> src = new LinkedHashMap<String, Long>();
    src.put("one", 1L);
    src.put("two", 2L);
    src.put("three", 3L);

    assertEquals("[1,2,3]", gson.toJson(src, type));
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

    LinkedHashMap<String, Object> innerMap = new LinkedHashMap<String, Object>();
    innerMap.put("test", 1);
    innerMap.put("TestStringArray", new String[] { "one", "two" });
    map.put("c", innerMap);

    assertEquals("{\"a\":12,\"b\":null,\"c\":{\"test\":1,\"TestStringArray\":[\"one\",\"two\"]}}",
        new GsonBuilder().serializeNulls().create().toJson(map));
    assertEquals("{\n  \"a\": 12,\n  \"b\": null,\n  \"c\": "
  		+ "{\n    \"test\": 1,\n    \"TestStringArray\": "
  		+ "[\n      \"one\",\n      \"two\"\n    ]\n  }\n}",
        new GsonBuilder().setPrettyPrinting().serializeNulls().create().toJson(map));
    assertEquals("{\"a\":12,\"c\":{\"test\":1,\"TestStringArray\":[\"one\",\"two\"]}}",
        new GsonBuilder().create().toJson(map));
    assertEquals("{\n  \"a\": 12,\n  \"c\": "
        + "{\n    \"test\": 1,\n    \"TestStringArray\": "
        + "[\n      \"one\",\n      \"two\"\n    ]\n  }\n}",
        new GsonBuilder().setPrettyPrinting().create().toJson(map));

    innerMap.put("d", "e");
    assertEquals("{\"a\":12,\"c\":{\"test\":1,\"TestStringArray\":[\"one\",\"two\"],\"d\":\"e\"}}",
        new Gson().toJson(map));
  }

// com.google.gson.functional.MapTest::testGeneralMapField
  public void testGeneralMapField() throws Exception {
    MapWithGeneralMapParameters map = new MapWithGeneralMapParameters();
    map.map.put("string", "testString");
    map.map.put("stringArray", new String[]{"one", "two"});
    map.map.put("objectArray", new Object[]{1, 2L, "three"});

    String expected = "{\"map\":{\"string\":\"testString\",\"stringArray\":"
        + "[\"one\",\"two\"],\"objectArray\":[1,2,\"three\"]}}";
    assertEquals(expected, gson.toJson(map));

    gson = new GsonBuilder()
        .enableComplexMapKeySerialization()
        .create();
    assertEquals(expected, gson.toJson(map));
  }

// com.google.gson.functional.MapTest::testComplexKeysSerialization
  public void testComplexKeysSerialization() {
    Map<Point, String> map = new LinkedHashMap<Point, String>();
    map.put(new Point(2, 3), "a");
    map.put(new Point(5, 7), "b");
    String json = "{\"2,3\":\"a\",\"5,7\":\"b\"}";
    assertEquals(json, gson.toJson(map, new TypeToken<Map<Point, String>>() {}.getType()));
    assertEquals(json, gson.toJson(map, Map.class));
  }

// com.google.gson.functional.MapTest::testComplexKeysDeserialization
  public void testComplexKeysDeserialization() {
    String json = "{'2,3':'a','5,7':'b'}";
    try {
      gson.fromJson(json, new TypeToken<Map<Point, String>>() {}.getType());
      fail();
    } catch (JsonParseException expected) {
    }
  }

// com.google.gson.functional.MapTest::testStringKeyDeserialization
  public void testStringKeyDeserialization() {
    String json = "{'2,3':'a','5,7':'b'}";
    Map<String, String> map = new LinkedHashMap<String, String>();
    map.put("2,3", "a");
    map.put("5,7", "b");
    assertEquals(map, gson.fromJson(json, new TypeToken<Map<String, String>>() {}.getType()));
  }

// com.google.gson.functional.MapTest::testNumberKeyDeserialization
  public void testNumberKeyDeserialization() {
    String json = "{'2.3':'a','5.7':'b'}";
    Map<Double, String> map = new LinkedHashMap<Double, String>();
    map.put(2.3, "a");
    map.put(5.7, "b");
    assertEquals(map, gson.fromJson(json, new TypeToken<Map<Double, String>>() {}.getType()));
  }

// com.google.gson.functional.MapTest::testBooleanKeyDeserialization
  public void testBooleanKeyDeserialization() {
    String json = "{'true':'a','false':'b'}";
    Map<Boolean, String> map = new LinkedHashMap<Boolean, String>();
    map.put(true, "a");
    map.put(false, "b");
    assertEquals(map, gson.fromJson(json, new TypeToken<Map<Boolean, String>>() {}.getType()));
  }

// com.google.gson.functional.MapTest::testMapDeserializationWithDuplicateKeys
  public void testMapDeserializationWithDuplicateKeys() {
    try {
      gson.fromJson("{'a':1,'a':2}", new TypeToken<Map<String, Integer>>() {}.getType());
      fail();
    } catch (JsonSyntaxException expected) {
    }
  }

// com.google.gson.functional.MapTest::testSerializeMapOfMaps
  public void testSerializeMapOfMaps() {
    Type type = new TypeToken<Map<String, Map<String, String>>>() {}.getType();
    Map<String, Map<String, String>> map = newMap(
        "a", newMap("ka1", "va1", "ka2", "va2"),
        "b", newMap("kb1", "vb1", "kb2", "vb2"));
    assertEquals("{'a':{'ka1':'va1','ka2':'va2'},'b':{'kb1':'vb1','kb2':'vb2'}}",
        gson.toJson(map, type).replace('"', '\''));
  }

// com.google.gson.functional.MapTest::testDeerializeMapOfMaps
  public void testDeerializeMapOfMaps() {
    Type type = new TypeToken<Map<String, Map<String, String>>>() {}.getType();
    Map<String, Map<String, String>> map = newMap(
        "a", newMap("ka1", "va1", "ka2", "va2"),
        "b", newMap("kb1", "vb1", "kb2", "vb2"));
    String json = "{'a':{'ka1':'va1','ka2':'va2'},'b':{'kb1':'vb1','kb2':'vb2'}}";
    assertEquals(map, gson.fromJson(json, type));
  }

// com.google.gson.functional.MapTest::testMapNamePromotionWithJsonElementReader
  public void testMapNamePromotionWithJsonElementReader() {
    String json = "{'2.3':'a'}";
    Map<Double, String> map = new LinkedHashMap<Double, String>();
    map.put(2.3, "a");
    JsonElement tree = new JsonParser().parse(json);
    assertEquals(map, gson.fromJson(tree, new TypeToken<Map<Double, String>>() {}.getType()));
  }

// com.google.gson.functional.MoreSpecificTypeSerializationTest::testSubclassFields
  public void testSubclassFields() {
    ClassWithBaseFields target = new ClassWithBaseFields(new Sub(1, 2));
    String json = gson.toJson(target);
    assertTrue(json.contains("\"b\":1"));
    assertTrue(json.contains("\"s\":2"));
  }

// com.google.gson.functional.MoreSpecificTypeSerializationTest::testListOfSubclassFields
  public void testListOfSubclassFields() {
    Collection<Base> list = new ArrayList<Base>();
    list.add(new Base(1));
    list.add(new Sub(2, 3));
    ClassWithContainersOfBaseFields target = new ClassWithContainersOfBaseFields(list, null);
    String json = gson.toJson(target);
    assertTrue(json, json.contains("{\"b\":1}"));
    assertTrue(json, json.contains("{\"s\":3,\"b\":2}"));
  }

// com.google.gson.functional.MoreSpecificTypeSerializationTest::testMapOfSubclassFields
  public void testMapOfSubclassFields() {
    Map<String, Base> map = new HashMap<String, Base>();
    map.put("base", new Base(1));
    map.put("sub", new Sub(2, 3));
    ClassWithContainersOfBaseFields target = new ClassWithContainersOfBaseFields(null, map);
    JsonObject json = gson.toJsonTree(target).getAsJsonObject().get("map").getAsJsonObject();
    assertEquals(1, json.get("base").getAsJsonObject().get("b").getAsInt());
    JsonObject sub = json.get("sub").getAsJsonObject();
    assertEquals(2, sub.get("b").getAsInt());
    assertEquals(3, sub.get("s").getAsInt());
  }

// com.google.gson.functional.MoreSpecificTypeSerializationTest::testParameterizedSubclassFields
  public void testParameterizedSubclassFields() {
    ClassWithParameterizedBaseFields target = new ClassWithParameterizedBaseFields(
        new ParameterizedSub<String>("one", "two"));
    String json = gson.toJson(target);
    assertTrue(json.contains("\"t\":\"one\""));
    assertFalse(json.contains("\"s\""));
  }

// com.google.gson.functional.MoreSpecificTypeSerializationTest::testListOfParameterizedSubclassFields
  public void testListOfParameterizedSubclassFields() {
    Collection<ParameterizedBase<String>> list = new ArrayList<ParameterizedBase<String>>();
    list.add(new ParameterizedBase<String>("one"));
    list.add(new ParameterizedSub<String>("two", "three"));
    ClassWithContainersOfParameterizedBaseFields target =
      new ClassWithContainersOfParameterizedBaseFields(list, null);
    String json = gson.toJson(target);
    assertTrue(json, json.contains("{\"t\":\"one\"}"));
    assertFalse(json, json.contains("\"s\":"));
  }

// com.google.gson.functional.MoreSpecificTypeSerializationTest::testMapOfParameterizedSubclassFields
  public void testMapOfParameterizedSubclassFields() {
    Map<String, ParameterizedBase<String>> map = new HashMap<String, ParameterizedBase<String>>();
    map.put("base", new ParameterizedBase<String>("one"));
    map.put("sub", new ParameterizedSub<String>("two", "three"));
    ClassWithContainersOfParameterizedBaseFields target =
      new ClassWithContainersOfParameterizedBaseFields(null, map);
    JsonObject json = gson.toJsonTree(target).getAsJsonObject().get("map").getAsJsonObject();
    assertEquals("one", json.get("base").getAsJsonObject().get("t").getAsString());
    JsonObject sub = json.get("sub").getAsJsonObject();
    assertEquals("two", sub.get("t").getAsString());
    assertNull(sub.get("s"));
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
    try {
      ClassWithDuplicateFields target = new ClassWithDuplicateFields(10);
      gson.toJson(target);
      fail();
    } catch (IllegalArgumentException expected) {
    }
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

// com.google.gson.functional.NamingPolicyTest::testComplexFieldNameStrategy
  public void testComplexFieldNameStrategy() throws Exception {
    Gson gson = new Gson();
    String json = gson.toJson(new ClassWithComplexFieldName(10));
    String escapedFieldName = "@value\\\"_s$\\\\";
    assertEquals("{\"" + escapedFieldName + "\":10}", json);

    ClassWithComplexFieldName obj = gson.fromJson(json, ClassWithComplexFieldName.class);
    assertEquals(10, obj.value);
  }

// com.google.gson.functional.NamingPolicyTest::testAtSignInSerializedName
  public void testAtSignInSerializedName() {
    assertEquals("{\"@foo\":\"bar\"}", new Gson().toJson(new AtName()));
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

// com.google.gson.functional.NullObjectAndFieldTest::testAbsentJsonElementsAreSetToNull
  public void testAbsentJsonElementsAreSetToNull() {
    Gson gson = new Gson();
    ClassWithInitializedMembers target =
        gson.fromJson("{array:[1,2,3]}", ClassWithInitializedMembers.class);
    assertTrue(target.array.length == 3 && target.array[1] == 2);
    assertEquals(ClassWithInitializedMembers.MY_STRING_DEFAULT, target.str1);
    assertNull(target.str2);
    assertEquals(ClassWithInitializedMembers.MY_INT_DEFAULT, target.int1);
    assertEquals(0, target.int2); 
    assertEquals(ClassWithInitializedMembers.MY_BOOLEAN_DEFAULT, target.bool1);
    assertFalse(target.bool2); 
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
    assertEquals("null", gson.toJson(null));
  }

// com.google.gson.functional.ObjectTest::testEmptyStringDeserialization
  public void testEmptyStringDeserialization() throws Exception {
    Object object = gson.fromJson("", Object.class);
    assertNull(object);
  }

// com.google.gson.functional.ObjectTest::testTruncatedDeserialization
  public void testTruncatedDeserialization() {
    try {
      gson.fromJson("[\"a\", \"b\",", new TypeToken<List<String>>() {}.getType());
      fail();
    } catch (JsonParseException expected) {
    }
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
    assertEquals("null", gson.toJson(new ClassWithNoFields() {
      
    }));
  }

// com.google.gson.functional.ObjectTest::testAnonymousLocalClassesCustomSerialization
  public void testAnonymousLocalClassesCustomSerialization() throws Exception {
    gson = new GsonBuilder()
        .registerTypeHierarchyAdapter(ClassWithNoFields.class,
            new JsonSerializer<ClassWithNoFields>() {
              public JsonElement serialize(
                  ClassWithNoFields src, Type typeOfSrc, JsonSerializationContext context) {
                return new JsonObject();
              }
            }).create();

    assertEquals("null", gson.toJson(new ClassWithNoFields() {
      
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

// com.google.gson.functional.ObjectTest::testInnerClassDeserialization
  public void testInnerClassDeserialization() {
    final Parent p = new Parent();
    Gson gson = new GsonBuilder().registerTypeAdapter(
        Parent.Child.class, new InstanceCreator<Parent.Child>() {
      public Parent.Child createInstance(Type type) {
        return p.new Child();
      }
    }).create();
    String json = "{'value2':3}";
    Parent.Child c = gson.fromJson(json, Parent.Child.class);
    assertEquals(3, c.value2);
  }

// com.google.gson.functional.ObjectTest::testObjectFieldNamesWithoutQuotesDeserialization
  public void testObjectFieldNamesWithoutQuotesDeserialization() {
    String json = "{longValue:1,'booleanValue':true,\"stringValue\":'bar'}";
    BagOfPrimitives bag = gson.fromJson(json, BagOfPrimitives.class);
    assertEquals(1, bag.longValue);
    assertTrue(bag.booleanValue);
    assertEquals("bar", bag.stringValue);
  }

// com.google.gson.functional.ObjectTest::testStringFieldWithNumberValueDeserialization
  public void testStringFieldWithNumberValueDeserialization() {
    String json = "{\"stringValue\":1}";
    BagOfPrimitives bag = gson.fromJson(json, BagOfPrimitives.class);
    assertEquals("1", bag.stringValue);

    json = "{\"stringValue\":1.5E+6}";
    bag = gson.fromJson(json, BagOfPrimitives.class);
    assertEquals("1.5E+6", bag.stringValue);

    json = "{\"stringValue\":true}";
    bag = gson.fromJson(json, BagOfPrimitives.class);
    assertEquals("true", bag.stringValue);
  }

// com.google.gson.functional.ObjectTest::testStringFieldWithEmptyValueSerialization
  public void testStringFieldWithEmptyValueSerialization() {
    ClassWithEmptyStringFields target = new ClassWithEmptyStringFields();
    target.a = "5794749";
    String json = gson.toJson(target);
    assertTrue(json.contains("\"a\":\"5794749\""));
    assertTrue(json.contains("\"b\":\"\""));
    assertTrue(json.contains("\"c\":\"\""));
  }

// com.google.gson.functional.ObjectTest::testStringFieldWithEmptyValueDeserialization
  public void testStringFieldWithEmptyValueDeserialization() {
    String json = "{a:\"5794749\",b:\"\",c:\"\"}";
    ClassWithEmptyStringFields target = gson.fromJson(json, ClassWithEmptyStringFields.class);
    assertEquals("5794749", target.a);
    assertEquals("", target.b);
    assertEquals("", target.c);
  }

// com.google.gson.functional.ObjectTest::testJsonObjectSerialization
  public void testJsonObjectSerialization() {
    Gson gson = new GsonBuilder().serializeNulls().create();
    JsonObject obj = new JsonObject();
    String json = gson.toJson(obj);
    assertEquals("{}", json);
  }

// com.google.gson.functional.ObjectTest::testSingletonLists
  public void testSingletonLists() {
    Gson gson = new Gson();
    Product product = new Product();
    assertEquals("{\"attributes\":[],\"departments\":[]}",
        gson.toJson(product));
    gson.fromJson(gson.toJson(product), Product.class);

    product.departments.add(new Department());
    assertEquals("{\"attributes\":[],\"departments\":[{\"name\":\"abc\",\"code\":\"123\"}]}",
        gson.toJson(product));
    gson.fromJson(gson.toJson(product), Product.class);

    product.attributes.add("456");
    assertEquals("{\"attributes\":[\"456\"],\"departments\":[{\"name\":\"abc\",\"code\":\"123\"}]}",
        gson.toJson(product));
    gson.fromJson(gson.toJson(product), Product.class);
  }

// com.google.gson.functional.ObjectTest::testDateAsMapObjectField
  public void testDateAsMapObjectField() {
    HasObjectMap a = new HasObjectMap();
    a.map.put("date", new Date(0));
    assertEquals("{\"map\":{\"date\":\"Dec 31, 1969, 4:00:00 PM\"}}", gson.toJson(a));
  }

// com.google.gson.functional.ParameterizedTypesTest::testParameterizedTypesSerialization
  public void testParameterizedTypesSerialization() throws Exception {
    MyParameterizedType<Integer> src = new MyParameterizedType<Integer>(10);
    Type typeOfSrc = new TypeToken<MyParameterizedType<Integer>>() {}.getType();
    String json = gson.toJson(src, typeOfSrc);
    assertEquals(src.getExpectedJson(), json);
  }

// com.google.gson.functional.ParameterizedTypesTest::testParameterizedTypeDeserialization
  public void testParameterizedTypeDeserialization() throws Exception {
    BagOfPrimitives bag = new BagOfPrimitives();
    MyParameterizedType<BagOfPrimitives> expected = new MyParameterizedType<BagOfPrimitives>(bag);
    Type expectedType = new TypeToken<MyParameterizedType<BagOfPrimitives>>() {}.getType();
    BagOfPrimitives bagDefaultInstance = new BagOfPrimitives();
    Gson gson = new GsonBuilder().registerTypeAdapter(
        expectedType, new MyParameterizedTypeInstanceCreator<BagOfPrimitives>(bagDefaultInstance))
        .create();

    String json = expected.getExpectedJson();
    MyParameterizedType<BagOfPrimitives> actual = gson.fromJson(json, expectedType);
    assertEquals(expected, actual);
  }

// com.google.gson.functional.ParameterizedTypesTest::testTypesWithMultipleParametersSerialization
  public void testTypesWithMultipleParametersSerialization() throws Exception {
    MultiParameters<Integer, Float, Double, String, BagOfPrimitives> src =
        new MultiParameters<Integer, Float, Double, String, BagOfPrimitives>(10, 1.0F, 2.1D,
            "abc", new BagOfPrimitives());
    Type typeOfSrc = new TypeToken<MultiParameters<Integer, Float, Double, String,
        BagOfPrimitives>>() {}.getType();
    String json = gson.toJson(src, typeOfSrc);
    String expected = "{\"a\":10,\"b\":1.0,\"c\":2.1,\"d\":\"abc\","
        + "\"e\":{\"longValue\":0,\"intValue\":0,\"booleanValue\":false,\"stringValue\":\"\"}}";
    assertEquals(expected, json);
  }

// com.google.gson.functional.ParameterizedTypesTest::testTypesWithMultipleParametersDeserialization
  public void testTypesWithMultipleParametersDeserialization() throws Exception {
    Type typeOfTarget = new TypeToken<MultiParameters<Integer, Float, Double, String,
        BagOfPrimitives>>() {}.getType();
    String json = "{\"a\":10,\"b\":1.0,\"c\":2.1,\"d\":\"abc\","
        + "\"e\":{\"longValue\":0,\"intValue\":0,\"booleanValue\":false,\"stringValue\":\"\"}}";
    MultiParameters<Integer, Float, Double, String, BagOfPrimitives> target =
        gson.fromJson(json, typeOfTarget);
    MultiParameters<Integer, Float, Double, String, BagOfPrimitives> expected =
        new MultiParameters<Integer, Float, Double, String, BagOfPrimitives>(10, 1.0F, 2.1D,
            "abc", new BagOfPrimitives());
    assertEquals(expected, target);
  }

// com.google.gson.functional.ParameterizedTypesTest::testParameterizedTypeWithCustomSerializer
  public void testParameterizedTypeWithCustomSerializer() {
    Type ptIntegerType = new TypeToken<MyParameterizedType<Integer>>() {}.getType();
    Type ptStringType = new TypeToken<MyParameterizedType<String>>() {}.getType();
    Gson gson = new GsonBuilder()
        .registerTypeAdapter(ptIntegerType, new MyParameterizedTypeAdapter<Integer>())
        .registerTypeAdapter(ptStringType, new MyParameterizedTypeAdapter<String>())
        .create();
    MyParameterizedType<Integer> intTarget = new MyParameterizedType<Integer>(10);
    String json = gson.toJson(intTarget, ptIntegerType);
    assertEquals(MyParameterizedTypeAdapter.<Integer>getExpectedJson(intTarget), json);

    MyParameterizedType<String> stringTarget = new MyParameterizedType<String>("abc");
    json = gson.toJson(stringTarget, ptStringType);
    assertEquals(MyParameterizedTypeAdapter.<String>getExpectedJson(stringTarget), json);
  }

// com.google.gson.functional.ParameterizedTypesTest::testParameterizedTypesWithCustomDeserializer
  public void testParameterizedTypesWithCustomDeserializer() {
    Type ptIntegerType = new TypeToken<MyParameterizedType<Integer>>() {}.getType();
    Type ptStringType = new TypeToken<MyParameterizedType<String>>() {}.getType();
    Gson gson = new GsonBuilder().registerTypeAdapter(
        ptIntegerType, new MyParameterizedTypeAdapter<Integer>())
        .registerTypeAdapter(ptStringType, new MyParameterizedTypeAdapter<String>())
        .registerTypeAdapter(ptStringType, new MyParameterizedTypeInstanceCreator<String>(""))
        .registerTypeAdapter(ptIntegerType,
            new MyParameterizedTypeInstanceCreator<Integer>(new Integer(0)))
        .create();

    MyParameterizedType<Integer> src = new MyParameterizedType<Integer>(10);
    String json = MyParameterizedTypeAdapter.<Integer>getExpectedJson(src);
    MyParameterizedType<Integer> intTarget = gson.fromJson(json, ptIntegerType);
    assertEquals(10, intTarget.value.intValue());

    MyParameterizedType<String> srcStr = new MyParameterizedType<String>("abc");
    json = MyParameterizedTypeAdapter.<String>getExpectedJson(srcStr);
    MyParameterizedType<String> stringTarget = gson.fromJson(json, ptStringType);
    assertEquals("abc", stringTarget.value);
  }

// com.google.gson.functional.ParameterizedTypesTest::testParameterizedTypesWithWriterSerialization
  public void testParameterizedTypesWithWriterSerialization() throws Exception {
    Writer writer = new StringWriter();
    MyParameterizedType<Integer> src = new MyParameterizedType<Integer>(10);
    Type typeOfSrc = new TypeToken<MyParameterizedType<Integer>>() {}.getType();
    gson.toJson(src, typeOfSrc, writer);
    assertEquals(src.getExpectedJson(), writer.toString());
  }

// com.google.gson.functional.ParameterizedTypesTest::testParameterizedTypeWithReaderDeserialization
  public void testParameterizedTypeWithReaderDeserialization() throws Exception {
    BagOfPrimitives bag = new BagOfPrimitives();
    MyParameterizedType<BagOfPrimitives> expected = new MyParameterizedType<BagOfPrimitives>(bag);
    Type expectedType = new TypeToken<MyParameterizedType<BagOfPrimitives>>() {}.getType();
    BagOfPrimitives bagDefaultInstance = new BagOfPrimitives();
    Gson gson = new GsonBuilder().registerTypeAdapter(
        expectedType, new MyParameterizedTypeInstanceCreator<BagOfPrimitives>(bagDefaultInstance))
        .create();

    Reader json = new StringReader(expected.getExpectedJson());
    MyParameterizedType<Integer> actual = gson.fromJson(json, expectedType);
    assertEquals(expected, actual);
  }

// com.google.gson.functional.ParameterizedTypesTest::testVariableTypeFieldsAndGenericArraysSerialization
  public void testVariableTypeFieldsAndGenericArraysSerialization() throws Exception {
    Integer obj = 0;
    Integer[] array = { 1, 2, 3 };
    List<Integer> list = new ArrayList<Integer>();
    list.add(4);
    list.add(5);
    List<Integer>[] arrayOfLists = new List[] { list, list };

    Type typeOfSrc = new TypeToken<ObjectWithTypeVariables<Integer>>() {}.getType();
    ObjectWithTypeVariables<Integer> objToSerialize =
        new ObjectWithTypeVariables<Integer>(obj, array, list, arrayOfLists, list, arrayOfLists);
    String json = gson.toJson(objToSerialize, typeOfSrc);

    assertEquals(objToSerialize.getExpectedJson(), json);
  }

// com.google.gson.functional.ParameterizedTypesTest::testVariableTypeFieldsAndGenericArraysDeserialization
  public void testVariableTypeFieldsAndGenericArraysDeserialization() throws Exception {
    Integer obj = 0;
    Integer[] array = { 1, 2, 3 };
    List<Integer> list = new ArrayList<Integer>();
    list.add(4);
    list.add(5);
    List<Integer>[] arrayOfLists = new List[] { list, list };

    Type typeOfSrc = new TypeToken<ObjectWithTypeVariables<Integer>>() {}.getType();
    ObjectWithTypeVariables<Integer> objToSerialize =
        new ObjectWithTypeVariables<Integer>(obj, array, list, arrayOfLists, list, arrayOfLists);
    String json = gson.toJson(objToSerialize, typeOfSrc);
    ObjectWithTypeVariables<Integer> objAfterDeserialization = gson.fromJson(json, typeOfSrc);

    assertEquals(objAfterDeserialization.getExpectedJson(), json);
  }

// com.google.gson.functional.ParameterizedTypesTest::testVariableTypeDeserialization
  public void testVariableTypeDeserialization() throws Exception {
    Type typeOfSrc = new TypeToken<ObjectWithTypeVariables<Integer>>() {}.getType();
    ObjectWithTypeVariables<Integer> objToSerialize =
        new ObjectWithTypeVariables<Integer>(0, null, null, null, null, null);
    String json = gson.toJson(objToSerialize, typeOfSrc);
    ObjectWithTypeVariables<Integer> objAfterDeserialization = gson.fromJson(json, typeOfSrc);

    assertEquals(objAfterDeserialization.getExpectedJson(), json);
  }

// com.google.gson.functional.ParameterizedTypesTest::testVariableTypeArrayDeserialization
  public void testVariableTypeArrayDeserialization() throws Exception {
    Integer[] array = { 1, 2, 3 };

    Type typeOfSrc = new TypeToken<ObjectWithTypeVariables<Integer>>() {}.getType();
    ObjectWithTypeVariables<Integer> objToSerialize =
        new ObjectWithTypeVariables<Integer>(null, array, null, null, null, null);
    String json = gson.toJson(objToSerialize, typeOfSrc);
    ObjectWithTypeVariables<Integer> objAfterDeserialization = gson.fromJson(json, typeOfSrc);

    assertEquals(objAfterDeserialization.getExpectedJson(), json);
  }

// com.google.gson.functional.ParameterizedTypesTest::testParameterizedTypeWithVariableTypeDeserialization
  public void testParameterizedTypeWithVariableTypeDeserialization() throws Exception {
    List<Integer> list = new ArrayList<Integer>();
    list.add(4);
    list.add(5);

    Type typeOfSrc = new TypeToken<ObjectWithTypeVariables<Integer>>() {}.getType();
    ObjectWithTypeVariables<Integer> objToSerialize =
        new ObjectWithTypeVariables<Integer>(null, null, list, null, null, null);
    String json = gson.toJson(objToSerialize, typeOfSrc);
    ObjectWithTypeVariables<Integer> objAfterDeserialization = gson.fromJson(json, typeOfSrc);

    assertEquals(objAfterDeserialization.getExpectedJson(), json);
  }

// com.google.gson.functional.ParameterizedTypesTest::testParameterizedTypeGenericArraysSerialization
  public void testParameterizedTypeGenericArraysSerialization() throws Exception {
    List<Integer> list = new ArrayList<Integer>();
    list.add(1);
    list.add(2);
    List<Integer>[] arrayOfLists = new List[] { list, list };

    Type typeOfSrc = new TypeToken<ObjectWithTypeVariables<Integer>>() {}.getType();
    ObjectWithTypeVariables<Integer> objToSerialize =
        new ObjectWithTypeVariables<Integer>(null, null, null, arrayOfLists, null, null);
    String json = gson.toJson(objToSerialize, typeOfSrc);
    assertEquals("{\"arrayOfListOfTypeParameters\":[[1,2],[1,2]]}", json);
  }

// com.google.gson.functional.ParameterizedTypesTest::testParameterizedTypeGenericArraysDeserialization
  public void testParameterizedTypeGenericArraysDeserialization() throws Exception {
    List<Integer> list = new ArrayList<Integer>();
    list.add(1);
    list.add(2);
    List<Integer>[] arrayOfLists = new List[] { list, list };

    Type typeOfSrc = new TypeToken<ObjectWithTypeVariables<Integer>>() {}.getType();
    ObjectWithTypeVariables<Integer> objToSerialize =
        new ObjectWithTypeVariables<Integer>(null, null, null, arrayOfLists, null, null);
    String json = gson.toJson(objToSerialize, typeOfSrc);
    ObjectWithTypeVariables<Integer> objAfterDeserialization = gson.fromJson(json, typeOfSrc);

    assertEquals(objAfterDeserialization.getExpectedJson(), json);
  }

// com.google.gson.functional.ParameterizedTypesTest::testDeepParameterizedTypeSerialization
  public void testDeepParameterizedTypeSerialization() {
    Amount<MyQuantity> amount = new Amount<MyQuantity>();
    String json = gson.toJson(amount);
    assertTrue(json.contains("value"));
    assertTrue(json.contains("30"));    
  }

// com.google.gson.functional.ParameterizedTypesTest::testDeepParameterizedTypeDeserialization
  public void testDeepParameterizedTypeDeserialization() {
    String json = "{value:30}";
    Type type = new TypeToken<Amount<MyQuantity>>() {}.getType();    
    Amount<MyQuantity> amount = gson.fromJson(json, type);
    assertEquals(30, amount.value);
  }

// com.google.gson.functional.PrettyPrintingTest::testPrettyPrintList
  public void testPrettyPrintList() {
    BagOfPrimitives b = new BagOfPrimitives();
    List<BagOfPrimitives> listOfB = new LinkedList<BagOfPrimitives>();
    for (int i = 0; i < 15; ++i) {
      listOfB.add(b);
    }
    Type typeOfSrc = new TypeToken<List<BagOfPrimitives>>() {}.getType();
    String json = gson.toJson(listOfB, typeOfSrc);
    print(json);
  }

// com.google.gson.functional.PrettyPrintingTest::testPrettyPrintArrayOfObjects
  public void testPrettyPrintArrayOfObjects() {
    ArrayOfObjects target = new ArrayOfObjects();
    String json = gson.toJson(target);
    print(json);
  }

// com.google.gson.functional.PrettyPrintingTest::testPrettyPrintArrayOfPrimitives
  public void testPrettyPrintArrayOfPrimitives() {
    int[] ints = new int[] { 1, 2, 3, 4, 5 };
    String json = gson.toJson(ints);
    assertEquals("[\n  1,\n  2,\n  3,\n  4,\n  5\n]", json);
  }

// com.google.gson.functional.PrettyPrintingTest::testPrettyPrintArrayOfPrimitiveArrays
  public void testPrettyPrintArrayOfPrimitiveArrays() {
    int[][] ints = new int[][] { { 1, 2 }, { 3, 4 }, { 5, 6 }, { 7, 8 },
        { 9, 0 }, { 10 } };
    String json = gson.toJson(ints);
    assertEquals("[\n  [\n    1,\n    2\n  ],\n  [\n    3,\n    4\n  ],\n  [\n    5,\n    6\n  ],"
        + "\n  [\n    7,\n    8\n  ],\n  [\n    9,\n    0\n  ],\n  [\n    10\n  ]\n]", json);
  }

// com.google.gson.functional.PrettyPrintingTest::testPrettyPrintListOfPrimitiveArrays
  public void testPrettyPrintListOfPrimitiveArrays() {
    List<Integer[]> list = Arrays.asList(new Integer[][] { { 1, 2 }, { 3, 4 },
        { 5, 6 }, { 7, 8 }, { 9, 0 }, { 10 } });
    String json = gson.toJson(list);
    assertEquals("[\n  [\n    1,\n    2\n  ],\n  [\n    3,\n    4\n  ],\n  [\n    5,\n    6\n  ],"
        + "\n  [\n    7,\n    8\n  ],\n  [\n    9,\n    0\n  ],\n  [\n    10\n  ]\n]", json);
  }

// com.google.gson.functional.PrettyPrintingTest::testMap
  public void testMap() {
    Map<String, Integer> map = new LinkedHashMap<String, Integer>();
    map.put("abc", 1);
    map.put("def", 5);
    String json = gson.toJson(map);
    assertEquals("{\n  \"abc\": 1,\n  \"def\": 5\n}", json);
  }

// com.google.gson.functional.PrettyPrintingTest::testEmptyMapField
  public void testEmptyMapField() {
    ClassWithMap obj = new ClassWithMap();
    obj.map = new LinkedHashMap<String, Integer>();
    String json = gson.toJson(obj);
    assertTrue(json.contains("{\n  \"map\": {},\n  \"value\": 2\n}"));
  }

// com.google.gson.functional.PrettyPrintingTest::testMultipleArrays
  public void testMultipleArrays() {
    int[][][] ints = new int[][][] { { { 1 }, { 2 } } };
    String json = gson.toJson(ints);
    assertEquals("[\n  [\n    [\n      1\n    ],\n    [\n      2\n    ]\n  ]\n]", json);
  }

// com.google.gson.functional.PrimitiveCharacterTest::testPrimitiveCharacterAutoboxedSerialization
  public void testPrimitiveCharacterAutoboxedSerialization() {
    assertEquals("\"A\"", gson.toJson('A'));
    assertEquals("\"A\"", gson.toJson('A', char.class));
    assertEquals("\"A\"", gson.toJson('A', Character.class));
  }

// com.google.gson.functional.PrimitiveCharacterTest::testPrimitiveCharacterAutoboxedDeserialization
  public void testPrimitiveCharacterAutoboxedDeserialization() {
    char expected = 'a';
    char actual = gson.fromJson("a", char.class);
    assertEquals(expected, actual);

    actual = gson.fromJson("\"a\"", char.class);
    assertEquals(expected, actual);

    actual = gson.fromJson("a", Character.class);
    assertEquals(expected, actual);
  }

// com.google.gson.functional.PrimitiveTest::testPrimitiveIntegerAutoboxedSerialization
  public void testPrimitiveIntegerAutoboxedSerialization() {
    assertEquals("1", gson.toJson(1));
  }

// com.google.gson.functional.PrimitiveTest::testPrimitiveIntegerAutoboxedDeserialization
  public void testPrimitiveIntegerAutoboxedDeserialization() {
    int expected = 1;
    int actual = gson.fromJson("1", int.class);
    assertEquals(expected, actual);

    actual = gson.fromJson("1", Integer.class);
    assertEquals(expected, actual);
  }

// com.google.gson.functional.PrimitiveTest::testByteSerialization
  public void testByteSerialization() {
    assertEquals("1", gson.toJson(1, byte.class));
    assertEquals("1", gson.toJson(1, Byte.class));
  }

// com.google.gson.functional.PrimitiveTest::testShortSerialization
  public void testShortSerialization() {
    assertEquals("1", gson.toJson(1, short.class));
    assertEquals("1", gson.toJson(1, Short.class));
  }

// com.google.gson.functional.PrimitiveTest::testByteDeserialization
  public void testByteDeserialization() {
    Byte target = gson.fromJson("1", Byte.class);
    assertEquals(1, (byte)target);
    byte primitive = gson.fromJson("1", byte.class);
    assertEquals(1, primitive);
  }

// com.google.gson.functional.PrimitiveTest::testPrimitiveIntegerAutoboxedInASingleElementArraySerialization
  public void testPrimitiveIntegerAutoboxedInASingleElementArraySerialization() {
    int target[] = {-9332};
    assertEquals("[-9332]", gson.toJson(target));
    assertEquals("[-9332]", gson.toJson(target, int[].class));
    assertEquals("[-9332]", gson.toJson(target, Integer[].class));
  }

// com.google.gson.functional.PrimitiveTest::testReallyLongValuesSerialization
  public void testReallyLongValuesSerialization() {
    long value = 333961828784581L;
    assertEquals("333961828784581", gson.toJson(value));
  }

// com.google.gson.functional.PrimitiveTest::testReallyLongValuesDeserialization
  public void testReallyLongValuesDeserialization() {
    String json = "333961828784581";
    long value = gson.fromJson(json, Long.class);
    assertEquals(333961828784581L, value);
  }

// com.google.gson.functional.PrimitiveTest::testPrimitiveLongAutoboxedSerialization
  public void testPrimitiveLongAutoboxedSerialization() {
    assertEquals("1", gson.toJson(1L, long.class));
    assertEquals("1", gson.toJson(1L, Long.class));
  }

// com.google.gson.functional.PrimitiveTest::testPrimitiveLongAutoboxedDeserialization
  public void testPrimitiveLongAutoboxedDeserialization() {
    long expected = 1L;
    long actual = gson.fromJson("1", long.class);
    assertEquals(expected, actual);

    actual = gson.fromJson("1", Long.class);
    assertEquals(expected, actual);
  }

// com.google.gson.functional.PrimitiveTest::testPrimitiveLongAutoboxedInASingleElementArraySerialization
  public void testPrimitiveLongAutoboxedInASingleElementArraySerialization() {
    long[] target = {-23L};
    assertEquals("[-23]", gson.toJson(target));
    assertEquals("[-23]", gson.toJson(target, long[].class));
    assertEquals("[-23]", gson.toJson(target, Long[].class));
  }

// com.google.gson.functional.PrimitiveTest::testPrimitiveBooleanAutoboxedSerialization
  public void testPrimitiveBooleanAutoboxedSerialization() {
    assertEquals("true", gson.toJson(true));
    assertEquals("false", gson.toJson(false));
  }

// com.google.gson.functional.PrimitiveTest::testBooleanDeserialization
  public void testBooleanDeserialization() {
    boolean value = gson.fromJson("false", boolean.class);
    assertEquals(false, value);
    value = gson.fromJson("true", boolean.class);
    assertEquals(true, value);
  }

// com.google.gson.functional.PrimitiveTest::testPrimitiveBooleanAutoboxedInASingleElementArraySerialization
  public void testPrimitiveBooleanAutoboxedInASingleElementArraySerialization() {
    boolean target[] = {false};
    assertEquals("[false]", gson.toJson(target));
    assertEquals("[false]", gson.toJson(target, boolean[].class));
    assertEquals("[false]", gson.toJson(target, Boolean[].class));
  }

// com.google.gson.functional.PrimitiveTest::testNumberSerialization
  public void testNumberSerialization() {
    Number expected = 1L;
    String json = gson.toJson(expected);
    assertEquals(expected.toString(), json);

    json = gson.toJson(expected, Number.class);
    assertEquals(expected.toString(), json);
  }

// com.google.gson.functional.PrimitiveTest::testNumberDeserialization
  public void testNumberDeserialization() {
    String json = "1";
    Number expected = new Integer(json);
    Number actual = gson.fromJson(json, Number.class);
    assertEquals(expected.intValue(), actual.intValue());

    json = String.valueOf(Long.MAX_VALUE);
    expected = new Long(json);
    actual = gson.fromJson(json, Number.class);
    assertEquals(expected.longValue(), actual.longValue());

    json = "1.0";
    actual = gson.fromJson(json, Number.class);
    assertEquals(1L, actual.longValue());
  }

// com.google.gson.functional.PrimitiveTest::testPrimitiveDoubleAutoboxedSerialization
  public void testPrimitiveDoubleAutoboxedSerialization() {
    assertEquals("-122.08234335", gson.toJson(-122.08234335));
    assertEquals("122.08112002", gson.toJson(new Double(122.08112002)));
  }

// com.google.gson.functional.PrimitiveTest::testPrimitiveDoubleAutoboxedDeserialization
  public void testPrimitiveDoubleAutoboxedDeserialization() {
    double actual = gson.fromJson("-122.08858585", double.class);
    assertEquals(-122.08858585, actual);

    actual = gson.fromJson("122.023900008000", Double.class);
    assertEquals(122.023900008, actual);
  }

// com.google.gson.functional.PrimitiveTest::testPrimitiveDoubleAutoboxedInASingleElementArraySerialization
  public void testPrimitiveDoubleAutoboxedInASingleElementArraySerialization() {
    double[] target = {-122.08D};
    assertEquals("[-122.08]", gson.toJson(target));
    assertEquals("[-122.08]", gson.toJson(target, double[].class));
    assertEquals("[-122.08]", gson.toJson(target, Double[].class));
  }

// com.google.gson.functional.PrimitiveTest::testDoubleAsStringRepresentationDeserialization
  public void testDoubleAsStringRepresentationDeserialization() {
    String doubleValue = "1.0043E+5";
    Double expected = Double.valueOf(doubleValue);
    Double actual = gson.fromJson(doubleValue, Double.class);
    assertEquals(expected, actual);

    double actual1 = gson.fromJson(doubleValue, double.class);
    assertEquals(expected.doubleValue(), actual1);
  }

// com.google.gson.functional.PrimitiveTest::testDoubleNoFractAsStringRepresentationDeserialization
  public void testDoubleNoFractAsStringRepresentationDeserialization() {
    String doubleValue = "1E+5";
    Double expected = Double.valueOf(doubleValue);
    Double actual = gson.fromJson(doubleValue, Double.class);
    assertEquals(expected, actual);

    double actual1 = gson.fromJson(doubleValue, double.class);
    assertEquals(expected.doubleValue(), actual1);
  }

// com.google.gson.functional.PrimitiveTest::testDoubleArrayDeserialization
  public void testDoubleArrayDeserialization() {
      String json = "[0.0, 0.004761904761904762, 3.4013606962703525E-4, 7.936508173034305E-4,"
              + "0.0011904761904761906, 0.0]";
      double[] values = gson.fromJson(json, double[].class);
      assertEquals(6, values.length);
      assertEquals(0.0, values[0]);
      assertEquals(0.004761904761904762, values[1]);
      assertEquals(3.4013606962703525E-4, values[2]);
      assertEquals(7.936508173034305E-4, values[3]);
      assertEquals(0.0011904761904761906, values[4]);
      assertEquals(0.0, values[5]);
  }

// com.google.gson.functional.PrimitiveTest::testLargeDoubleDeserialization
  public void testLargeDoubleDeserialization() {
    String doubleValue = "1.234567899E8";
    Double expected = Double.valueOf(doubleValue);
    Double actual = gson.fromJson(doubleValue, Double.class);
    assertEquals(expected, actual);

    double actual1 = gson.fromJson(doubleValue, double.class);
    assertEquals(expected.doubleValue(), actual1);
  }

// com.google.gson.functional.PrimitiveTest::testBigDecimalSerialization
  public void testBigDecimalSerialization() {
    BigDecimal target = new BigDecimal("-122.0e-21");
    String json = gson.toJson(target);
    assertEquals(target, new BigDecimal(json));
  }

// com.google.gson.functional.PrimitiveTest::testBigDecimalDeserialization
  public void testBigDecimalDeserialization() {
    BigDecimal target = new BigDecimal("-122.0e-21");
    String json = "-122.0e-21";
    assertEquals(target, gson.fromJson(json, BigDecimal.class));
  }

// com.google.gson.functional.PrimitiveTest::testBigDecimalInASingleElementArraySerialization
  public void testBigDecimalInASingleElementArraySerialization() {
    BigDecimal[] target = {new BigDecimal("-122.08e-21")};
    String json = gson.toJson(target);
    String actual = extractElementFromArray(json);
    assertEquals(target[0], new BigDecimal(actual));

    json = gson.toJson(target, BigDecimal[].class);
    actual = extractElementFromArray(json);
    assertEquals(target[0], new BigDecimal(actual));
  }

// com.google.gson.functional.PrimitiveTest::testSmallValueForBigDecimalSerialization
  public void testSmallValueForBigDecimalSerialization() {
    BigDecimal target = new BigDecimal("1.55");
    String actual = gson.toJson(target);
    assertEquals(target.toString(), actual);
  }

// com.google.gson.functional.PrimitiveTest::testSmallValueForBigDecimalDeserialization
  public void testSmallValueForBigDecimalDeserialization() {
    BigDecimal expected = new BigDecimal("1.55");
    BigDecimal actual = gson.fromJson("1.55", BigDecimal.class);
    assertEquals(expected, actual);
  }

// com.google.gson.functional.PrimitiveTest::testBigDecimalPreservePrecisionSerialization
  public void testBigDecimalPreservePrecisionSerialization() {
    String expectedValue = "1.000";
    BigDecimal obj = new BigDecimal(expectedValue);
    String actualValue = gson.toJson(obj);

    assertEquals(expectedValue, actualValue);
  }

// com.google.gson.functional.PrimitiveTest::testBigDecimalPreservePrecisionDeserialization
  public void testBigDecimalPreservePrecisionDeserialization() {
    String json = "1.000";
    BigDecimal expected = new BigDecimal(json);
    BigDecimal actual = gson.fromJson(json, BigDecimal.class);

    assertEquals(expected, actual);
  }

// com.google.gson.functional.PrimitiveTest::testBigDecimalAsStringRepresentationDeserialization
  public void testBigDecimalAsStringRepresentationDeserialization() {
    String doubleValue = "0.05E+5";
    BigDecimal expected = new BigDecimal(doubleValue);
    BigDecimal actual = gson.fromJson(doubleValue, BigDecimal.class);
    assertEquals(expected, actual);
  }

// com.google.gson.functional.PrimitiveTest::testBigDecimalNoFractAsStringRepresentationDeserialization
  public void testBigDecimalNoFractAsStringRepresentationDeserialization() {
    String doubleValue = "5E+5";
    BigDecimal expected = new BigDecimal(doubleValue);
    BigDecimal actual = gson.fromJson(doubleValue, BigDecimal.class);
    assertEquals(expected, actual);
  }

// com.google.gson.functional.PrimitiveTest::testBigIntegerSerialization
  public void testBigIntegerSerialization() {
    BigInteger target = new BigInteger("12121211243123245845384534687435634558945453489543985435");
    assertEquals(target.toString(), gson.toJson(target));
  }

// com.google.gson.functional.PrimitiveTest::testBigIntegerDeserialization
  public void testBigIntegerDeserialization() {
    String json = "12121211243123245845384534687435634558945453489543985435";
    BigInteger target = new BigInteger(json);
    assertEquals(target, gson.fromJson(json, BigInteger.class));
  }

// com.google.gson.functional.PrimitiveTest::testBigIntegerInASingleElementArraySerialization
  public void testBigIntegerInASingleElementArraySerialization() {
    BigInteger[] target = {new BigInteger("1212121243434324323254365345367456456456465464564564")};
    String json = gson.toJson(target);
    String actual = extractElementFromArray(json);
    assertEquals(target[0], new BigInteger(actual));

    json = gson.toJson(target, BigInteger[].class);
    actual = extractElementFromArray(json);
    assertEquals(target[0], new BigInteger(actual));
  }

// com.google.gson.functional.PrimitiveTest::testSmallValueForBigIntegerSerialization
  public void testSmallValueForBigIntegerSerialization() {
    BigInteger target = new BigInteger("15");
    String actual = gson.toJson(target);
    assertEquals(target.toString(), actual);
  }

// com.google.gson.functional.PrimitiveTest::testSmallValueForBigIntegerDeserialization
  public void testSmallValueForBigIntegerDeserialization() {
    BigInteger expected = new BigInteger("15");
    BigInteger actual = gson.fromJson("15", BigInteger.class);
    assertEquals(expected, actual);
  }

// com.google.gson.functional.PrimitiveTest::testBadValueForBigIntegerDeserialization
  public void testBadValueForBigIntegerDeserialization() {
    try {
      gson.fromJson("15.099", BigInteger.class);
      fail("BigInteger can not be decimal values.");
    } catch (JsonSyntaxException expected) { }
  }

// com.google.gson.functional.PrimitiveTest::testMoreSpecificSerialization
  public void testMoreSpecificSerialization() {
    Gson gson = new Gson();
    String expected = "This is a string";
    String expectedJson = gson.toJson(expected);

    Serializable serializableString = expected;
    String actualJson = gson.toJson(serializableString, Serializable.class);
    assertFalse(expectedJson.equals(actualJson));
  }

// com.google.gson.functional.PrimitiveTest::testDoubleNaNSerializationNotSupportedByDefault
  public void testDoubleNaNSerializationNotSupportedByDefault() {
    try {
      double nan = Double.NaN;
      gson.toJson(nan);
      fail("Gson should not accept NaN for serialization");
    } catch (IllegalArgumentException expected) {
    }
    try {
      gson.toJson(Double.NaN);
      fail("Gson should not accept NaN for serialization");
    } catch (IllegalArgumentException expected) {
    }
  }

// com.google.gson.functional.PrimitiveTest::testDoubleNaNSerialization
  public void testDoubleNaNSerialization() {
    Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().create();
    double nan = Double.NaN;
    assertEquals("NaN", gson.toJson(nan));
    assertEquals("NaN", gson.toJson(Double.NaN));
  }

// com.google.gson.functional.PrimitiveTest::testDoubleNaNDeserialization
  public void testDoubleNaNDeserialization() {
    assertTrue(Double.isNaN(gson.fromJson("NaN", Double.class)));
    assertTrue(Double.isNaN(gson.fromJson("NaN", double.class)));
  }

// com.google.gson.functional.PrimitiveTest::testFloatNaNSerializationNotSupportedByDefault
  public void testFloatNaNSerializationNotSupportedByDefault() {
    try {
      float nan = Float.NaN;
      gson.toJson(nan);
      fail("Gson should not accept NaN for serialization");
    } catch (IllegalArgumentException expected) {
    }
    try {
      gson.toJson(Float.NaN);
      fail("Gson should not accept NaN for serialization");
    } catch (IllegalArgumentException expected) {
    }
  }

// com.google.gson.functional.PrimitiveTest::testFloatNaNSerialization
  public void testFloatNaNSerialization() {
    Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().create();
    float nan = Float.NaN;
    assertEquals("NaN", gson.toJson(nan));
    assertEquals("NaN", gson.toJson(Float.NaN));
  }

// com.google.gson.functional.PrimitiveTest::testFloatNaNDeserialization
  public void testFloatNaNDeserialization() {
    assertTrue(Float.isNaN(gson.fromJson("NaN", Float.class)));
    assertTrue(Float.isNaN(gson.fromJson("NaN", float.class)));
  }

// com.google.gson.functional.PrimitiveTest::testBigDecimalNaNDeserializationNotSupported
  public void testBigDecimalNaNDeserializationNotSupported() {
    try {
      gson.fromJson("NaN", BigDecimal.class);
      fail("Gson should not accept NaN for deserialization by default.");
    } catch (JsonSyntaxException expected) {
    }
  }

// com.google.gson.functional.PrimitiveTest::testDoubleInfinitySerializationNotSupportedByDefault
  public void testDoubleInfinitySerializationNotSupportedByDefault() {
    try {
      double infinity = Double.POSITIVE_INFINITY;
      gson.toJson(infinity);
      fail("Gson should not accept positive infinity for serialization by default.");
    } catch (IllegalArgumentException expected) {
    }
    try {
      gson.toJson(Double.POSITIVE_INFINITY);
      fail("Gson should not accept positive infinity for serialization by default.");
    } catch (IllegalArgumentException expected) {
    }
  }

// com.google.gson.functional.PrimitiveTest::testDoubleInfinitySerialization
  public void testDoubleInfinitySerialization() {
    Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().create();
    double infinity = Double.POSITIVE_INFINITY;
    assertEquals("Infinity", gson.toJson(infinity));
    assertEquals("Infinity", gson.toJson(Double.POSITIVE_INFINITY));
  }

// com.google.gson.functional.PrimitiveTest::testDoubleInfinityDeserialization
  public void testDoubleInfinityDeserialization() {
    assertTrue(Double.isInfinite(gson.fromJson("Infinity", Double.class)));
    assertTrue(Double.isInfinite(gson.fromJson("Infinity", double.class)));
  }

// com.google.gson.functional.PrimitiveTest::testFloatInfinitySerializationNotSupportedByDefault
  public void testFloatInfinitySerializationNotSupportedByDefault() {
    try {
      float infinity = Float.POSITIVE_INFINITY;
      gson.toJson(infinity);
      fail("Gson should not accept positive infinity for serialization by default");
    } catch (IllegalArgumentException expected) {
    }
    try {
      gson.toJson(Float.POSITIVE_INFINITY);
      fail("Gson should not accept positive infinity for serialization by default");
    } catch (IllegalArgumentException expected) {
    }
  }

// com.google.gson.functional.PrimitiveTest::testFloatInfinitySerialization
  public void testFloatInfinitySerialization() {
    Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().create();
    float infinity = Float.POSITIVE_INFINITY;
    assertEquals("Infinity", gson.toJson(infinity));
    assertEquals("Infinity", gson.toJson(Float.POSITIVE_INFINITY));
  }

// com.google.gson.functional.PrimitiveTest::testFloatInfinityDeserialization
  public void testFloatInfinityDeserialization() {
    assertTrue(Float.isInfinite(gson.fromJson("Infinity", Float.class)));
    assertTrue(Float.isInfinite(gson.fromJson("Infinity", float.class)));
  }

// com.google.gson.functional.PrimitiveTest::testBigDecimalInfinityDeserializationNotSupported
  public void testBigDecimalInfinityDeserializationNotSupported() {
    try {
      gson.fromJson("Infinity", BigDecimal.class);
      fail("Gson should not accept positive infinity for deserialization with BigDecimal");
    } catch (JsonSyntaxException expected) {
    }
  }

// com.google.gson.functional.PrimitiveTest::testNegativeInfinitySerializationNotSupportedByDefault
  public void testNegativeInfinitySerializationNotSupportedByDefault() {
    try {
      double negativeInfinity = Double.NEGATIVE_INFINITY;
      gson.toJson(negativeInfinity);
      fail("Gson should not accept negative infinity for serialization by default");
    } catch (IllegalArgumentException expected) {
    }
    try {
      gson.toJson(Double.NEGATIVE_INFINITY);
      fail("Gson should not accept negative infinity for serialization by default");
    } catch (IllegalArgumentException expected) {
    }
  }

// com.google.gson.functional.PrimitiveTest::testNegativeInfinitySerialization
  public void testNegativeInfinitySerialization() {
    Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().create();
    double negativeInfinity = Double.NEGATIVE_INFINITY;
    assertEquals("-Infinity", gson.toJson(negativeInfinity));
    assertEquals("-Infinity", gson.toJson(Double.NEGATIVE_INFINITY));
  }

// com.google.gson.functional.PrimitiveTest::testNegativeInfinityDeserialization
  public void testNegativeInfinityDeserialization() {
    assertTrue(Double.isInfinite(gson.fromJson("-Infinity", double.class)));
    assertTrue(Double.isInfinite(gson.fromJson("-Infinity", Double.class)));
  }

// com.google.gson.functional.PrimitiveTest::testNegativeInfinityFloatSerializationNotSupportedByDefault
  public void testNegativeInfinityFloatSerializationNotSupportedByDefault() {
    try {
      float negativeInfinity = Float.NEGATIVE_INFINITY;
      gson.toJson(negativeInfinity);
      fail("Gson should not accept negative infinity for serialization by default");
    } catch (IllegalArgumentException expected) {
    }
    try {
      gson.toJson(Float.NEGATIVE_INFINITY);
      fail("Gson should not accept negative infinity for serialization by default");
    } catch (IllegalArgumentException expected) {
    }
  }

// com.google.gson.functional.PrimitiveTest::testNegativeInfinityFloatSerialization
  public void testNegativeInfinityFloatSerialization() {
    Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().create();
    float negativeInfinity = Float.NEGATIVE_INFINITY;
    assertEquals("-Infinity", gson.toJson(negativeInfinity));
    assertEquals("-Infinity", gson.toJson(Float.NEGATIVE_INFINITY));
  }

// com.google.gson.functional.PrimitiveTest::testNegativeInfinityFloatDeserialization
  public void testNegativeInfinityFloatDeserialization() {
    assertTrue(Float.isInfinite(gson.fromJson("-Infinity", float.class)));
    assertTrue(Float.isInfinite(gson.fromJson("-Infinity", Float.class)));
  }

// com.google.gson.functional.PrimitiveTest::testBigDecimalNegativeInfinityDeserializationNotSupported
  public void testBigDecimalNegativeInfinityDeserializationNotSupported() {
    try {
      gson.fromJson("-Infinity", BigDecimal.class);
      fail("Gson should not accept positive infinity for deserialization");
    } catch (JsonSyntaxException expected) {
    }
  }

// com.google.gson.functional.PrimitiveTest::testLongAsStringSerialization
  public void testLongAsStringSerialization() throws Exception {
    gson = new GsonBuilder().setLongSerializationPolicy(LongSerializationPolicy.STRING).create();
    String result = gson.toJson(15L);
    assertEquals("\"15\"", result);

    
    result = gson.toJson(2);
    assertEquals("2", result);
  }

// com.google.gson.functional.PrimitiveTest::testLongAsStringDeserialization
  public void testLongAsStringDeserialization() throws Exception {
    long value = gson.fromJson("\"15\"", long.class);
    assertEquals(15, value);

    gson = new GsonBuilder().setLongSerializationPolicy(LongSerializationPolicy.STRING).create();
    value = gson.fromJson("\"25\"", long.class);
    assertEquals(25, value);
  }

// com.google.gson.functional.PrimitiveTest::testQuotedStringSerializationAndDeserialization
  public void testQuotedStringSerializationAndDeserialization() throws Exception {
    String value = "String Blah Blah Blah...1, 2, 3";
    String serializedForm = gson.toJson(value);
    assertEquals("\"" + value + "\"", serializedForm);

    String actual = gson.fromJson(serializedForm, String.class);
    assertEquals(value, actual);
  }

// com.google.gson.functional.PrimitiveTest::testUnquotedStringDeserializationFails
  public void testUnquotedStringDeserializationFails() throws Exception {
    assertEquals("UnquotedSingleWord", gson.fromJson("UnquotedSingleWord", String.class));

    String value = "String Blah Blah Blah...1, 2, 3";
    try {
      gson.fromJson(value, String.class);
      fail();
    } catch (JsonSyntaxException expected) { }
  }

// com.google.gson.functional.PrimitiveTest::testHtmlCharacterSerialization
  public void testHtmlCharacterSerialization() throws Exception {
    String target = "<script>var a = 12;</script>";
    String result = gson.toJson(target);
    assertFalse(result.equals('"' + target + '"'));

    gson = new GsonBuilder().disableHtmlEscaping().create();
    result = gson.toJson(target);
    assertTrue(result.equals('"' + target + '"'));
  }

// com.google.gson.functional.PrimitiveTest::testDeserializePrimitiveWrapperAsObjectField
  public void testDeserializePrimitiveWrapperAsObjectField() {
    String json = "{i:10}";
    ClassWithIntegerField target = gson.fromJson(json, ClassWithIntegerField.class);
    assertEquals(10, target.i.intValue());
  }

// com.google.gson.functional.PrimitiveTest::testPrimitiveClassLiteral
  public void testPrimitiveClassLiteral() {
    assertEquals(1, gson.fromJson("1", int.class).intValue());
    assertEquals(1, gson.fromJson(new StringReader("1"), int.class).intValue());
    assertEquals(1, gson.fromJson(new JsonPrimitive(1), int.class).intValue());
  }

// com.google.gson.functional.PrimitiveTest::testDeserializeJsonObjectAsLongPrimitive
  public void testDeserializeJsonObjectAsLongPrimitive() {
    try {
      gson.fromJson("{'abc':1}", long.class);
      fail();
    } catch (JsonSyntaxException expected) {}
  }

// com.google.gson.functional.PrimitiveTest::testDeserializeJsonArrayAsLongWrapper
  public void testDeserializeJsonArrayAsLongWrapper() {
    try {
      gson.fromJson("[1,2,3]", Long.class);
      fail();
    } catch (JsonSyntaxException expected) {}
  }

// com.google.gson.functional.PrimitiveTest::testDeserializeJsonArrayAsInt
  public void testDeserializeJsonArrayAsInt() {
    try {
      gson.fromJson("[1, 2, 3, 4]", int.class);
      fail();
    } catch (JsonSyntaxException expected) {}
  }

// com.google.gson.functional.PrimitiveTest::testDeserializeJsonObjectAsInteger
  public void testDeserializeJsonObjectAsInteger() {
    try {
      gson.fromJson("{}", Integer.class);
      fail();
    } catch (JsonSyntaxException expected) {}
  }

// com.google.gson.functional.PrimitiveTest::testDeserializeJsonObjectAsShortPrimitive
  public void testDeserializeJsonObjectAsShortPrimitive() {
    try {
      gson.fromJson("{'abc':1}", short.class);
      fail();
    } catch (JsonSyntaxException expected) {}
  }

// com.google.gson.functional.PrimitiveTest::testDeserializeJsonArrayAsShortWrapper
  public void testDeserializeJsonArrayAsShortWrapper() {
    try {
      gson.fromJson("['a','b']", Short.class);
      fail();
    } catch (JsonSyntaxException expected) {}
  }

// com.google.gson.functional.PrimitiveTest::testDeserializeJsonArrayAsDoublePrimitive
  public void testDeserializeJsonArrayAsDoublePrimitive() {
    try {
      gson.fromJson("[1,2]", double.class);
      fail();
    } catch (JsonSyntaxException expected) {}
  }

// com.google.gson.functional.PrimitiveTest::testDeserializeJsonObjectAsDoubleWrapper
  public void testDeserializeJsonObjectAsDoubleWrapper() {
    try {
      gson.fromJson("{'abc':1}", Double.class);
      fail();
    } catch (JsonSyntaxException expected) {}
  }

// com.google.gson.functional.PrimitiveTest::testDeserializeJsonObjectAsFloatPrimitive
  public void testDeserializeJsonObjectAsFloatPrimitive() {
    try {
      gson.fromJson("{'abc':1}", float.class);
      fail();
    } catch (JsonSyntaxException expected) {}
  }

// com.google.gson.functional.PrimitiveTest::testDeserializeJsonArrayAsFloatWrapper
  public void testDeserializeJsonArrayAsFloatWrapper() {
    try {
      gson.fromJson("[1,2,3]", Float.class);
      fail();
    } catch (JsonSyntaxException expected) {}
  }

// com.google.gson.functional.PrimitiveTest::testDeserializeJsonObjectAsBytePrimitive
  public void testDeserializeJsonObjectAsBytePrimitive() {
    try {
      gson.fromJson("{'abc':1}", byte.class);
      fail();
    } catch (JsonSyntaxException expected) {}
  }

// com.google.gson.functional.PrimitiveTest::testDeserializeJsonArrayAsByteWrapper
  public void testDeserializeJsonArrayAsByteWrapper() {
    try {
      gson.fromJson("[1,2,3,4]", Byte.class);
      fail();
    } catch (JsonSyntaxException expected) {}
  }

// com.google.gson.functional.PrimitiveTest::testDeserializeJsonObjectAsBooleanPrimitive
  public void testDeserializeJsonObjectAsBooleanPrimitive() {
    try {
      gson.fromJson("{'abc':1}", boolean.class);
      fail();
    } catch (JsonSyntaxException expected) {}
  }

// com.google.gson.functional.PrimitiveTest::testDeserializeJsonArrayAsBooleanWrapper
  public void testDeserializeJsonArrayAsBooleanWrapper() {
    try {
      gson.fromJson("[1,2,3,4]", Boolean.class);
      fail();
    } catch (JsonSyntaxException expected) {}
  }

// com.google.gson.functional.PrimitiveTest::testDeserializeJsonArrayAsBigDecimal
  public void testDeserializeJsonArrayAsBigDecimal() {
    try {
      gson.fromJson("[1,2,3,4]", BigDecimal.class);
      fail();
    } catch (JsonSyntaxException expected) {}
  }

// com.google.gson.functional.PrimitiveTest::testDeserializeJsonObjectAsBigDecimal
  public void testDeserializeJsonObjectAsBigDecimal() {
    try {
      gson.fromJson("{'a':1}", BigDecimal.class);
      fail();
    } catch (JsonSyntaxException expected) {}
  }

// com.google.gson.functional.PrimitiveTest::testDeserializeJsonArrayAsBigInteger
  public void testDeserializeJsonArrayAsBigInteger() {
    try {
      gson.fromJson("[1,2,3,4]", BigInteger.class);
      fail();
    } catch (JsonSyntaxException expected) {}
  }

// com.google.gson.functional.PrimitiveTest::testDeserializeJsonObjectAsBigInteger
  public void testDeserializeJsonObjectAsBigInteger() {
    try {
      gson.fromJson("{'c':2}", BigInteger.class);
      fail();
    } catch (JsonSyntaxException expected) {}
  }

// com.google.gson.functional.PrimitiveTest::testDeserializeJsonArrayAsNumber
  public void testDeserializeJsonArrayAsNumber() {
    try {
      gson.fromJson("[1,2,3,4]", Number.class);
      fail();
    } catch (JsonSyntaxException expected) {}
  }

// com.google.gson.functional.PrimitiveTest::testDeserializeJsonObjectAsNumber
  public void testDeserializeJsonObjectAsNumber() {
    try {
      gson.fromJson("{'c':2}", Number.class);
      fail();
    } catch (JsonSyntaxException expected) {}
  }

// com.google.gson.functional.PrimitiveTest::testDeserializingDecimalPointValueZeroSucceeds
  public void testDeserializingDecimalPointValueZeroSucceeds() {
    assertEquals(1, (int) gson.fromJson("1.0", Integer.class));
  }

// com.google.gson.functional.PrimitiveTest::testDeserializingNonZeroDecimalPointValuesAsIntegerFails
  public void testDeserializingNonZeroDecimalPointValuesAsIntegerFails() {
    try {
      gson.fromJson("1.02", Byte.class);
      fail();
    } catch (JsonSyntaxException expected) {
    }
    try {
      gson.fromJson("1.02", Short.class);
      fail();
    } catch (JsonSyntaxException expected) {
    }
    try {
      gson.fromJson("1.02", Integer.class);
      fail();
    } catch (JsonSyntaxException expected) {
    }
    try {
      gson.fromJson("1.02", Long.class);
      fail();
    } catch (JsonSyntaxException expected) {
    }
  }

// com.google.gson.functional.PrimitiveTest::testDeserializingBigDecimalAsIntegerFails
  public void testDeserializingBigDecimalAsIntegerFails() {
    try {
      gson.fromJson("-122.08e-213", Integer.class);
      fail();
    } catch (JsonSyntaxException expected) {
    }
  }

// com.google.gson.functional.PrimitiveTest::testDeserializingBigIntegerAsInteger
  public void testDeserializingBigIntegerAsInteger() {
    try {
      gson.fromJson("12121211243123245845384534687435634558945453489543985435", Integer.class);
      fail();
    } catch (JsonSyntaxException expected) {
    }
  }

// com.google.gson.functional.PrimitiveTest::testDeserializingBigIntegerAsLong
  public void testDeserializingBigIntegerAsLong() {
    try {
      gson.fromJson("12121211243123245845384534687435634558945453489543985435", Long.class);
      fail();
    } catch (JsonSyntaxException expected) {
    }
  }

// com.google.gson.functional.PrimitiveTest::testValueVeryCloseToZeroIsZero
  public void testValueVeryCloseToZeroIsZero() {
    assertEquals(0, (byte) gson.fromJson("-122.08e-2132", byte.class));
    assertEquals(0, (short) gson.fromJson("-122.08e-2132", short.class));
    assertEquals(0, (int) gson.fromJson("-122.08e-2132", int.class));
    assertEquals(0, (long) gson.fromJson("-122.08e-2132", long.class));
    assertEquals(-0.0f, gson.fromJson("-122.08e-2132", float.class));
    assertEquals(-0.0, gson.fromJson("-122.08e-2132", double.class));
    assertEquals(0.0f, gson.fromJson("122.08e-2132", float.class));
    assertEquals(0.0, gson.fromJson("122.08e-2132", double.class));
  }

// com.google.gson.functional.PrimitiveTest::testDeserializingBigDecimalAsFloat
  public void testDeserializingBigDecimalAsFloat() {
    String json = "-122.08e-2132332";
    float actual = gson.fromJson(json, float.class);
    assertEquals(-0.0f, actual);
  }

// com.google.gson.functional.PrimitiveTest::testDeserializingBigDecimalAsDouble
  public void testDeserializingBigDecimalAsDouble() {
    String json = "-122.08e-2132332";
    double actual = gson.fromJson(json, double.class);
    assertEquals(-0.0d, actual);
  }

// com.google.gson.functional.PrimitiveTest::testDeserializingBigDecimalAsBigIntegerFails
  public void testDeserializingBigDecimalAsBigIntegerFails() {
    try {
      gson.fromJson("-122.08e-213", BigInteger.class);
      fail();
    } catch (JsonSyntaxException expected) {
    }
  }

// com.google.gson.functional.PrimitiveTest::testDeserializingBigIntegerAsBigDecimal
  public void testDeserializingBigIntegerAsBigDecimal() {
    BigDecimal actual =
      gson.fromJson("12121211243123245845384534687435634558945453489543985435", BigDecimal.class);
    assertEquals("12121211243123245845384534687435634558945453489543985435", actual.toPlainString());
  }

// com.google.gson.functional.PrimitiveTest::testStringsAsBooleans
  public void testStringsAsBooleans() {
    String json = "['true', 'false', 'TRUE', 'yes', '1']";
    assertEquals(Arrays.asList(true, false, true, false, false),
        gson.<List<Boolean>>fromJson(json, new TypeToken<List<Boolean>>() {}.getType()));
  }

// com.google.gson.functional.PrintFormattingTest::testCompactFormattingLeavesNoWhiteSpace
  public void testCompactFormattingLeavesNoWhiteSpace() {
    List list = new ArrayList();
    list.add(new BagOfPrimitives());
    list.add(new Nested());
    list.add(new PrimitiveArray());
    list.add(new ClassWithTransientFields());

    String json = gson.toJson(list);
    assertContainsNoWhiteSpace(json);
  }

// com.google.gson.functional.PrintFormattingTest::testJsonObjectWithNullValues
  public void testJsonObjectWithNullValues() {
    JsonObject obj = new JsonObject();
    obj.addProperty("field1", "value1");
    obj.addProperty("field2", (String) null);
    String json = gson.toJson(obj);
    assertTrue(json.contains("field1"));
    assertFalse(json.contains("field2"));
  }

// com.google.gson.functional.PrintFormattingTest::testJsonObjectWithNullValuesSerialized
  public void testJsonObjectWithNullValuesSerialized() {
    gson = new GsonBuilder().serializeNulls().create();
    JsonObject obj = new JsonObject();
    obj.addProperty("field1", "value1");
    obj.addProperty("field2", (String) null);
    String json = gson.toJson(obj);
    assertTrue(json.contains("field1"));
    assertTrue(json.contains("field2"));
  }

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
    assertEquals(10, (int) intTarget.value);

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

// com.google.gson.functional.PrimitiveTest::testPrimitiveIntegerAutoboxedInASingleElementArraySerialization
  public void testPrimitiveIntegerAutoboxedInASingleElementArraySerialization() {
    int target[] = {-9332};
    assertEquals("[-9332]", gson.toJson(target));
    assertEquals("[-9332]", gson.toJson(target, int[].class));
    assertEquals("[-9332]", gson.toJson(target, Integer[].class));
  }

// com.google.gson.functional.PrimitiveTest::testPrimitiveIntegerAutoboxedInASingleElementArrayDeserialization
  public void testPrimitiveIntegerAutoboxedInASingleElementArrayDeserialization() {
    int expected = 1;
    int actual = gson.fromJson("[1]", int.class);
    assertEquals(expected, actual);

    actual = gson.fromJson("[1]", Integer.class);
    assertEquals(expected, actual);
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

// com.google.gson.functional.PrimitiveTest::testPrimitiveLongAutoboxedInASingleElementArrayDeserialization
  public void testPrimitiveLongAutoboxedInASingleElementArrayDeserialization() {
    long expected = 1L;
    long actual = gson.fromJson("[1]", long.class);
    assertEquals(expected, actual);

    actual = gson.fromJson("[1]", Long.class);
    assertEquals(expected, actual);
  }

// com.google.gson.functional.PrimitiveTest::testPrimitiveBooleanAutoboxedSerialization
  public void testPrimitiveBooleanAutoboxedSerialization() {
    assertEquals("true", gson.toJson(true));
    assertEquals("false", gson.toJson(false));
  }

// com.google.gson.functional.PrimitiveTest::testPrimitiveBooleanAutoboxedDeserialization
  public void testPrimitiveBooleanAutoboxedDeserialization() {
    assertEquals(Boolean.FALSE, gson.fromJson("[false]", Boolean.class));
    assertEquals(Boolean.TRUE, gson.fromJson("[true]", Boolean.class));

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

// com.google.gson.functional.PrimitiveTest::testPrimitiveBooleanAutoboxedInASingleElementArrayDeserialization
  public void testPrimitiveBooleanAutoboxedInASingleElementArrayDeserialization() {
    assertEquals(Boolean.FALSE, gson.fromJson("[false]", Boolean.class));
    assertEquals(Boolean.TRUE, gson.fromJson("[true]", Boolean.class));

    boolean value = gson.fromJson("[false]", boolean.class);
    assertEquals(false, value);
    value = gson.fromJson("[true]", boolean.class);
    assertEquals(true, value);
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

// com.google.gson.functional.PrimitiveTest::testLargeDoubleDeserialization
  public void testLargeDoubleDeserialization() {
    String doubleValue = "1.234567899E8";
    Double expected = Double.valueOf(doubleValue);
    Double actual = gson.fromJson(doubleValue, Double.class);
    assertEquals(expected, actual);

    double actual1 = gson.fromJson(doubleValue, double.class);
    assertEquals(expected.doubleValue(), actual1);
  }

// com.google.gson.functional.PrimitiveTest::testPrimitiveDoubleAutoboxedInASingleElementArrayDeserialization
  public void testPrimitiveDoubleAutoboxedInASingleElementArrayDeserialization() {
    double expected = -122.08;
    double actual = gson.fromJson("[-122.08]", double.class);
    assertEquals(expected, actual);

    actual = gson.fromJson("[-122.08]", Double.class);
    assertEquals(expected, actual);
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

// com.google.gson.functional.PrimitiveTest::testBigDecimalInASingleElementArrayDeserialization
  public void testBigDecimalInASingleElementArrayDeserialization() {
    BigDecimal expected = new BigDecimal("-122.08e-21");
    BigDecimal actual = gson.fromJson("[-122.08e-21]", BigDecimal.class);
    assertEquals(expected, actual);
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

// com.google.gson.functional.PrimitiveTest::testBigIntegerInASingleElementArrayDeserialization
  public void testBigIntegerInASingleElementArrayDeserialization() {
    BigInteger expected = new BigInteger("34343434343424242423432323243243242");
    BigInteger actual = gson.fromJson("[34343434343424242423432323243243242]", BigInteger.class);
    assertEquals(expected, actual);
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
    } catch (JsonParseException expected) { }
  }

// com.google.gson.functional.PrimitiveTest::testOverridingDefaultPrimitiveSerialization
  public void testOverridingDefaultPrimitiveSerialization() {
    CrazyLongTypeAdapter typeAdapter = new CrazyLongTypeAdapter();
    gson = new GsonBuilder()
        .registerTypeAdapter(long.class, typeAdapter)
        .registerTypeAdapter(Long.class, typeAdapter)
        .create();
    long value = 1L;
    String serializedValue = gson.toJson(value);
    assertEquals(String.valueOf(value + CrazyLongTypeAdapter.DIFFERENCE), serializedValue);
    
    long deserializedValue = gson.fromJson(serializedValue, long.class);
    assertEquals(value, deserializedValue);
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
    } catch (JsonParseException expected) {      
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
    } catch (JsonParseException expected) {      
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
    } catch (JsonParseException expected) {      
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

// com.google.gson.functional.ReadersWritersTest::testWriterForSerialization
  public void testWriterForSerialization() throws Exception {
    Writer writer = new StringWriter();
    BagOfPrimitives src = new BagOfPrimitives();
    gson.toJson(src, writer);
    assertEquals(src.getExpectedJson(), writer.toString());
  }

// com.google.gson.functional.ReadersWritersTest::testReaderForDeserialization
  public void testReaderForDeserialization() throws Exception {
    BagOfPrimitives expected = new BagOfPrimitives();
    Reader json = new StringReader(expected.getExpectedJson());
    BagOfPrimitives actual = gson.fromJson(json, BagOfPrimitives.class);
    assertEquals(expected, actual);
  }

// com.google.gson.functional.ReadersWritersTest::testTopLevelNullObjectSerializationWithWriter
  public void testTopLevelNullObjectSerializationWithWriter() {
    StringWriter writer = new StringWriter();
    gson.toJson(null, writer);
    assertEquals("", writer.toString());
  }

// com.google.gson.functional.ReadersWritersTest::testTopLevelNullObjectDeserializationWithReader
  public void testTopLevelNullObjectDeserializationWithReader() {
    StringReader reader = new StringReader("null");
    Integer nullIntObject = gson.fromJson(reader, Integer.class);
    assertNull(nullIntObject);
  }

// com.google.gson.functional.ReadersWritersTest::testTopLevelNullObjectSerializationWithWriterAndSerializeNulls
  public void testTopLevelNullObjectSerializationWithWriterAndSerializeNulls() {
    Gson gson = new GsonBuilder().serializeNulls().create();
    StringWriter writer = new StringWriter();
    gson.toJson(null, writer);
    assertEquals("null", writer.toString());
  }

// com.google.gson.functional.ReadersWritersTest::testTopLevelNullObjectDeserializationWithReaderAndSerializeNulls
  public void testTopLevelNullObjectDeserializationWithReaderAndSerializeNulls() {
    Gson gson = new GsonBuilder().serializeNulls().create();
    StringReader reader = new StringReader("null");
    Integer nullIntObject = gson.fromJson(reader, Integer.class);
    assertNull(nullIntObject);
  }

// com.google.gson.functional.ReadersWritersTest::testReadWriteTwoStrings
  public void testReadWriteTwoStrings() throws IOException {
    Gson gson= new Gson();
    CharArrayWriter writer= new CharArrayWriter();
    writer.write(gson.toJson("one").toCharArray());
    writer.write(gson.toJson("two").toCharArray());
    CharArrayReader reader = new CharArrayReader(writer.toCharArray());
    JsonStreamParser parser = new JsonStreamParser(reader);
    String actualOne = gson.fromJson(parser.next(), String.class);
    assertEquals("one", actualOne);
    String actualTwo = gson.fromJson(parser.next(), String.class);
    assertEquals("two", actualTwo);
  }

// com.google.gson.functional.ReadersWritersTest::testReadWriteTwoObjects
  public void testReadWriteTwoObjects() throws IOException {
    Gson gson= new Gson();
    CharArrayWriter writer= new CharArrayWriter();
    BagOfPrimitives expectedOne = new BagOfPrimitives(1, 1, true, "one");
    writer.write(gson.toJson(expectedOne).toCharArray());
    BagOfPrimitives expectedTwo = new BagOfPrimitives(2, 2, false, "two");
    writer.write(gson.toJson(expectedTwo).toCharArray());
    CharArrayReader reader = new CharArrayReader(writer.toCharArray());
    JsonStreamParser parser = new JsonStreamParser(reader);
    BagOfPrimitives actualOne = gson.fromJson(parser.next(), BagOfPrimitives.class);
    assertEquals("one", actualOne.stringValue);
    BagOfPrimitives actualTwo = gson.fromJson(parser.next(), BagOfPrimitives.class);
    assertEquals("two", actualTwo.stringValue);
    assertFalse(parser.hasNext());
  }

// com.google.gson.functional.SecurityTest::testNonExecutableJsonSerialization
  public void testNonExecutableJsonSerialization() {
    Gson gson = gsonBuilder.generateNonExecutableJson().create();
    String json = gson.toJson(new BagOfPrimitives());
    assertTrue(json.startsWith(JSON_NON_EXECUTABLE_PREFIX));
  }

// com.google.gson.functional.SecurityTest::testNonExecutableJsonDeserialization
  public void testNonExecutableJsonDeserialization() {
    String json = JSON_NON_EXECUTABLE_PREFIX + "{longValue:1}";
    Gson gson = gsonBuilder.create();
    BagOfPrimitives target = gson.fromJson(json, BagOfPrimitives.class);
    assertEquals(1, target.longValue);
  }

// com.google.gson.functional.SecurityTest::testJsonWithNonExectuableTokenSerialization
  public void testJsonWithNonExectuableTokenSerialization() {
    Gson gson = gsonBuilder.generateNonExecutableJson().create();
    String json = gson.toJson(JSON_NON_EXECUTABLE_PREFIX);
    assertTrue(json.contains(")]}'\n"));
  }

// com.google.gson.functional.SecurityTest::testJsonWithNonExectuableTokenWithRegularGsonDeserialization
  public void testJsonWithNonExectuableTokenWithRegularGsonDeserialization() {
    Gson gson = gsonBuilder.create();
    String json = JSON_NON_EXECUTABLE_PREFIX + "{stringValue:')]}\\u0027\\n'}";
    BagOfPrimitives target = gson.fromJson(json, BagOfPrimitives.class);
    assertEquals(")]}'\n", target.stringValue);
  }

// com.google.gson.functional.SecurityTest::testJsonWithNonExectuableTokenWithConfiguredGsonDeserialization
  public void testJsonWithNonExectuableTokenWithConfiguredGsonDeserialization() {
    
    Gson gson = gsonBuilder.generateNonExecutableJson().create();
    String json = JSON_NON_EXECUTABLE_PREFIX + "{intValue:2,stringValue:')]}\\u0027\\n'}";
    BagOfPrimitives target = gson.fromJson(json, BagOfPrimitives.class);
    assertEquals(")]}'\n", target.stringValue);
    assertEquals(2, target.intValue);
  }

// com.google.gson.functional.StringTest::testStringValueSerialization
  public void testStringValueSerialization() throws Exception {
    String value = "someRandomStringValue";
    assertEquals('"' + value + '"', gson.toJson(value));
  }

// com.google.gson.functional.StringTest::testStringValueDeserialization
  public void testStringValueDeserialization() throws Exception {
    String value = "someRandomStringValue";
    String actual = gson.fromJson("\"" + value + "\"", String.class);
    assertEquals(value, actual);
  }

// com.google.gson.functional.StringTest::testSingleQuoteInStringSerialization
  public void testSingleQuoteInStringSerialization() throws Exception {
    String valueWithQuotes = "beforeQuote'afterQuote";
    String jsonRepresentation = gson.toJson(valueWithQuotes);
    assertEquals(valueWithQuotes, gson.fromJson(jsonRepresentation, String.class));
  }

// com.google.gson.functional.StringTest::testEscapedCtrlNInStringSerialization
  public void testEscapedCtrlNInStringSerialization() throws Exception {
    String value = "a\nb";
    String json = gson.toJson(value);
    assertEquals("\"a\\nb\"", json);
  }

// com.google.gson.functional.StringTest::testEscapedCtrlNInStringDeserialization
  public void testEscapedCtrlNInStringDeserialization() throws Exception {
    String json = "'a\\nb'";
    String actual = gson.fromJson(json, String.class);
    assertEquals("a\nb", actual);
  }

// com.google.gson.functional.StringTest::testEscapedCtrlRInStringSerialization
  public void testEscapedCtrlRInStringSerialization() throws Exception {
    String value = "a\rb";
    String json = gson.toJson(value);
    assertEquals("\"a\\rb\"", json);
  }

// com.google.gson.functional.StringTest::testEscapedCtrlRInStringDeserialization
  public void testEscapedCtrlRInStringDeserialization() throws Exception {
    String json = "'a\\rb'";
    String actual = gson.fromJson(json, String.class);
    assertEquals("a\rb", actual);
  }

// com.google.gson.functional.StringTest::testEscapedBackslashInStringSerialization
  public void testEscapedBackslashInStringSerialization() throws Exception {
    String value = "a\\b";
    String json = gson.toJson(value);
    assertEquals("\"a\\\\b\"", json);
  }

// com.google.gson.functional.StringTest::testEscapedBackslashInStringDeserialization
  public void testEscapedBackslashInStringDeserialization() throws Exception {
    String actual = gson.fromJson("'a\\\\b'", String.class);
    assertEquals("a\\b", actual);
  }

// com.google.gson.functional.StringTest::testSingleQuoteInStringDeserialization
  public void testSingleQuoteInStringDeserialization() throws Exception {
    String value = "beforeQuote'afterQuote";
    String actual = gson.fromJson("\"" + value + "\"", String.class);
    assertEquals(value, actual);
  }

// com.google.gson.functional.StringTest::testEscapingQuotesInStringSerialization
  public void testEscapingQuotesInStringSerialization() throws Exception {
    String valueWithQuotes = "beforeQuote\"afterQuote";
    String jsonRepresentation = gson.toJson(valueWithQuotes);
    String target = gson.fromJson(jsonRepresentation, String.class);
    assertEquals(valueWithQuotes, target);
  }

// com.google.gson.functional.StringTest::testEscapingQuotesInStringDeserialization
  public void testEscapingQuotesInStringDeserialization() throws Exception {
    String value = "beforeQuote\\\"afterQuote";
    String actual = gson.fromJson("\"" + value + "\"", String.class);
    String expected = "beforeQuote\"afterQuote";
    assertEquals(expected, actual);
  }

// com.google.gson.functional.StringTest::testStringValueAsSingleElementArraySerialization
  public void testStringValueAsSingleElementArraySerialization() throws Exception {
    String[] target = {"abc"};
    assertEquals("[\"abc\"]", gson.toJson(target));
    assertEquals("[\"abc\"]", gson.toJson(target, String[].class));
  }

// com.google.gson.functional.StringTest::testStringValueAsSingleElementArrayDeserialization
  public void testStringValueAsSingleElementArrayDeserialization() throws Exception {
    String value = "someRandomStringValue";
    String actual = gson.fromJson("[\"" + value + "\"]", String.class);
    assertEquals(value, actual);
  }

// com.google.gson.functional.StringTest::testStringWithEscapedSlashDeserialization
  public void testStringWithEscapedSlashDeserialization() {
    String value = "/";
    String json = "'\\/'";
    String actual = gson.fromJson(json, String.class);
    assertEquals(value, actual);
  }

// com.google.gson.functional.StringTest::testAssignmentCharSerialization
  public void testAssignmentCharSerialization() {
    String value = "abc=";
    String json = gson.toJson(value);
    assertEquals("\"abc\\u003d\"", json);
  }

// com.google.gson.functional.StringTest::testAssignmentCharDeserialization
  public void testAssignmentCharDeserialization() {
    String json = "\"abc=\"";
    String value = gson.fromJson(json, String.class);
    assertEquals("abc=", value);

    json = "'abc\u003d'";
    value = gson.fromJson(json, String.class);
    assertEquals("abc=", value);
  }

// com.google.gson.functional.StringTest::testJavascriptKeywordsInStringSerialization
  public void testJavascriptKeywordsInStringSerialization() {
    String value = "null true false function";
    String json = gson.toJson(value);
    assertEquals("\"" + value + "\"", json);
  }

// com.google.gson.functional.StringTest::testJavascriptKeywordsInStringDeserialization
  public void testJavascriptKeywordsInStringDeserialization() {
    String json = "'null true false function'";
    String value = gson.fromJson(json, String.class);
    assertEquals(json.substring(1, json.length() - 1), value);
  }

// com.google.gson.functional.TypeVariableTest::testSingle
  public void testSingle() throws Exception {
    Gson gson = new Gson();
    Bar bar1 = new Bar("someString", 1);
    ArrayList<Integer> arrayList = new ArrayList<Integer>();
    arrayList.add(1);
    arrayList.add(2);
    bar1.map.put("key1", arrayList);
    bar1.map.put("key2", new ArrayList<Integer>());
    String json = gson.toJson(bar1);
    System.out.println(json);

    Bar bar2 = gson.fromJson(json, Bar.class);
    assertEquals(bar1, bar2);
  }

// com.google.gson.functional.UncategorizedTest::testInvalidJsonDeserializationFails
  public void testInvalidJsonDeserializationFails() throws Exception {
    try {
      gson.fromJson("adfasdf1112,,,\":", BagOfPrimitives.class);
      fail("Bad JSON should throw a ParseException");
    } catch (JsonParseException expected) { }

    try {
      gson.fromJson("{adfasdf1112,,,\":}", BagOfPrimitives.class);
      fail("Bad JSON should throw a ParseException");
    } catch (JsonParseException expected) { }
  }

// com.google.gson.functional.UncategorizedTest::testObjectEqualButNotSameSerialization
  public void testObjectEqualButNotSameSerialization() throws Exception {
    ClassOverridingEquals objA = new ClassOverridingEquals();
    ClassOverridingEquals objB = new ClassOverridingEquals();
    objB.ref = objA;
    String json = gson.toJson(objB);
    assertEquals(objB.getExpectedJson(), json);
  }

// com.google.gson.functional.UncategorizedTest::testStaticFieldsAreNotSerialized
  public void testStaticFieldsAreNotSerialized() {
    BagOfPrimitives target = new BagOfPrimitives();
    assertFalse(gson.toJson(target).contains("DEFAULT_VALUE"));
  }

// com.google.gson.functional.UncategorizedTest::testReturningDerivedClassesDuringDeserialization
  public void testReturningDerivedClassesDuringDeserialization() {
    Gson gson = new GsonBuilder().registerTypeAdapter(Base.class, new BaseTypeAdapter()).create();
    String json = "{\"opType\":\"OP1\"}";
    Base base = gson.fromJson(json, Base.class);
    assertTrue(base instanceof Derived1);
    assertEquals(OperationType.OP1, base.opType);

    json = "{\"opType\":\"OP2\"}";
    base = gson.fromJson(json, Base.class);
    assertTrue(base instanceof Derived2);
    assertEquals(OperationType.OP2, base.opType);
  }

// com.google.gson.functional.VersioningTest::testVersionedUntilSerialization
  public void testVersionedUntilSerialization() {
    Version1 target = new Version1();
    Gson gson = builder.setVersion(1.29).create();
    String json = gson.toJson(target);
    assertTrue(json.contains("\"a\":" + A));
    
    gson = builder.setVersion(1.3).create();
    json = gson.toJson(target);
    assertFalse(json.contains("\"a\":" + A));
  }

// com.google.gson.functional.VersioningTest::testVersionedUntilDeserialization
  public void testVersionedUntilDeserialization() {
    Gson gson = builder.setVersion(1.3).create();
    String json = "{\"a\":3,\"b\":4,\"c\":5}";
    Version1 version1 = gson.fromJson(json, Version1.class);
    assertEquals(A, version1.a);
  }

// com.google.gson.functional.VersioningTest::testVersionedClassesSerialization
  public void testVersionedClassesSerialization() {
    Gson gson = builder.setVersion(1.0).create();
    String json1 = gson.toJson(new Version1());
    String json2 = gson.toJson(new Version1_1());
    assertEquals(json1, json2);
  }

// com.google.gson.functional.VersioningTest::testVersionedClassesDeserialization
  public void testVersionedClassesDeserialization() {
    Gson gson = builder.setVersion(1.0).create();
    String json = "{\"a\":3,\"b\":4,\"c\":5}";
    Version1 version1 = gson.fromJson(json, Version1.class);
    assertEquals(3, version1.a);
    assertEquals(4, version1.b);
    Version1_1 version1_1 = gson.fromJson(json, Version1_1.class);
    assertEquals(3, version1_1.a);
    assertEquals(4, version1_1.b);
    assertEquals(C, version1_1.c);
  }

// com.google.gson.functional.VersioningTest::testIgnoreLaterVersionClassSerialization
  public void testIgnoreLaterVersionClassSerialization() {
    Gson gson = builder.setVersion(1.0).create();
    assertEquals("", gson.toJson(new Version1_2()));
  }

// com.google.gson.functional.VersioningTest::testIgnoreLaterVersionClassDeserialization
  public void testIgnoreLaterVersionClassDeserialization() {
    Gson gson = builder.setVersion(1.0).create();
    String json = "{\"a\":3,\"b\":4,\"c\":5,\"d\":6}";
    Version1_2 version1_2 = gson.fromJson(json, Version1_2.class);
    
    
    assertEquals(A, version1_2.a);
    assertEquals(B, version1_2.b);
    assertEquals(C, version1_2.c);
    assertEquals(D, version1_2.d);
  }

// com.google.gson.functional.VersioningTest::testVersionedGsonWithUnversionedClassesSerialization
  public void testVersionedGsonWithUnversionedClassesSerialization() {
    Gson gson = builder.setVersion(1.0).create();
    BagOfPrimitives target = new BagOfPrimitives(10, 20, false, "stringValue");
    assertEquals(target.getExpectedJson(), gson.toJson(target));
  }

// com.google.gson.functional.VersioningTest::testVersionedGsonWithUnversionedClassesDeserialization
  public void testVersionedGsonWithUnversionedClassesDeserialization() {
    Gson gson = builder.setVersion(1.0).create();
    String json = "{\"longValue\":10,\"intValue\":20,\"booleanValue\":false}";

    BagOfPrimitives expected = new BagOfPrimitives();
    expected.longValue = 10;
    expected.intValue = 20;
    expected.booleanValue = false;
    BagOfPrimitives actual = gson.fromJson(json, BagOfPrimitives.class);
    assertEquals(expected, actual);
  }

// com.google.gson.functional.VersioningTest::testVersionedGsonMixingSinceAndUntilSerialization
  public void testVersionedGsonMixingSinceAndUntilSerialization() {
    Gson gson = builder.setVersion(1.0).create();
    SinceUntilMixing target = new SinceUntilMixing();
    String json = gson.toJson(target);
    assertFalse(json.contains("\"b\":" + B));
    
    gson = builder.setVersion(1.2).create();
    json = gson.toJson(target);
    assertTrue(json.contains("\"b\":" + B));
    
    gson = builder.setVersion(1.3).create();
    json = gson.toJson(target);
    assertFalse(json.contains("\"b\":" + B));
  }

// com.google.gson.functional.VersioningTest::testVersionedGsonMixingSinceAndUntilDeserialization
  public void testVersionedGsonMixingSinceAndUntilDeserialization() {
    String json = "{\"a\":5,\"b\":6}";
    Gson gson = builder.setVersion(1.0).create();
    SinceUntilMixing result = gson.fromJson(json, SinceUntilMixing.class);
    assertEquals(5, result.a);
    assertEquals(B, result.b);
    
    gson = builder.setVersion(1.2).create();
    result = gson.fromJson(json, SinceUntilMixing.class);
    assertEquals(5, result.a);
    assertEquals(6, result.b);
    
    gson = builder.setVersion(1.3).create();
    result = gson.fromJson(json, SinceUntilMixing.class);
    assertEquals(5, result.a);
    assertEquals(B, result.b);
  }

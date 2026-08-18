// buggy code
  public static WildcardType subtypeOf(Type bound) {
    Type[] upperBounds;
      upperBounds = new Type[] { bound };
    return new WildcardTypeImpl(upperBounds, EMPTY_TYPE_ARRAY);
  }

  public static WildcardType supertypeOf(Type bound) {
    Type[] lowerBounds;
      lowerBounds = new Type[] { bound };
    return new WildcardTypeImpl(new Type[] { Object.class }, lowerBounds);
  }

// relevant test
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

// com.google.gson.functional.RawSerializationTest::testCollectionOfPrimitives
  public void testCollectionOfPrimitives() {
    Collection<Integer> ints = Arrays.asList(1, 2, 3, 4, 5);
    String json = gson.toJson(ints);
    assertEquals("[1,2,3,4,5]", json);
  }

// com.google.gson.functional.RawSerializationTest::testCollectionOfObjects
  public void testCollectionOfObjects() {
    Collection<Foo> foos = Arrays.asList(new Foo(1), new Foo(2));
    String json = gson.toJson(foos);
    assertEquals("[{\"b\":1},{\"b\":2}]", json);
  }

// com.google.gson.functional.RawSerializationTest::testParameterizedObject
  public void testParameterizedObject() {
    Bar<Foo> bar = new Bar<Foo>(new Foo(1));
    String expectedJson = "{\"t\":{\"b\":1}}";
    
    String json = gson.toJson(bar);
    assertEquals(expectedJson, json);
    
    json = gson.toJson(bar, new TypeToken<Bar<Foo>>(){}.getType());
    assertEquals(expectedJson, json);
  }

// com.google.gson.functional.RawSerializationTest::testTwoLevelParameterizedObject
  public void testTwoLevelParameterizedObject() {
    Bar<Bar<Foo>> bar = new Bar<Bar<Foo>>(new Bar<Foo>(new Foo(1)));
    String expectedJson = "{\"t\":{\"t\":{\"b\":1}}}";
    
    String json = gson.toJson(bar);
    assertEquals(expectedJson, json);
    
    json = gson.toJson(bar, new TypeToken<Bar<Bar<Foo>>>(){}.getType());
    assertEquals(expectedJson, json);
  }

// com.google.gson.functional.RawSerializationTest::testThreeLevelParameterizedObject
  public void testThreeLevelParameterizedObject() {
    Bar<Bar<Bar<Foo>>> bar = new Bar<Bar<Bar<Foo>>>(new Bar<Bar<Foo>>(new Bar<Foo>(new Foo(1))));
    String expectedJson = "{\"t\":{\"t\":{\"t\":{\"b\":1}}}}";
    
    String json = gson.toJson(bar);
    assertEquals(expectedJson, json);
    
    json = gson.toJson(bar, new TypeToken<Bar<Bar<Bar<Foo>>>>(){}.getType());
    assertEquals(expectedJson, json);
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
    assertEquals("null", writer.toString());
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

// com.google.gson.functional.ReadersWritersTest::testTypeMismatchThrowsJsonSyntaxExceptionForStrings
  public void testTypeMismatchThrowsJsonSyntaxExceptionForStrings() {
    try {
      gson.fromJson("true", new TypeToken<Map<String, String>>() {}.getType());
      fail();
    } catch (JsonSyntaxException expected) {
    }
  }

// com.google.gson.functional.ReadersWritersTest::testTypeMismatchThrowsJsonSyntaxExceptionForReaders
  public void testTypeMismatchThrowsJsonSyntaxExceptionForReaders() {
    try {
      gson.fromJson(new StringReader("true"), new TypeToken<Map<String, String>>() {}.getType());
      fail();
    } catch (JsonSyntaxException expected) {
    }
  }

// com.google.gson.functional.RuntimeTypeAdapterFactoryFunctionalTest::testSubclassesAutomaticallySerialized
  public void testSubclassesAutomaticallySerialized() throws Exception {
    Shape shape = new Circle(25);
    String json = gson.toJson(shape);
    shape = gson.fromJson(json, Shape.class);
    assertEquals(25, ((Circle)shape).radius);

    shape = new Square(15);
    json = gson.toJson(shape);
    shape = gson.fromJson(json, Shape.class);
    assertEquals(15, ((Square)shape).side);
    assertEquals(ShapeType.SQUARE, shape.type);
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

// com.google.gson.functional.SerializedNameTest::testFirstNameIsChosenForSerialization
  public void testFirstNameIsChosenForSerialization() {
    MyClass target = new MyClass("v1", "v2");
    
    assertEquals("{\"name\":\"v1\",\"name1\":\"v2\"}", gson.toJson(target));
  }

// com.google.gson.functional.SerializedNameTest::testMultipleNamesDeserializedCorrectly
  public void testMultipleNamesDeserializedCorrectly() {
    assertEquals("v1", gson.fromJson("{'name':'v1'}", MyClass.class).a);

    
    assertEquals("v11", gson.fromJson("{'name1':'v11'}", MyClass.class).b);
    assertEquals("v2", gson.fromJson("{'name2':'v2'}", MyClass.class).b);
    assertEquals("v3", gson.fromJson("{'name3':'v3'}", MyClass.class).b);
  }

// com.google.gson.functional.SerializedNameTest::testMultipleNamesInTheSameString
  public void testMultipleNamesInTheSameString() {
    
    assertEquals("v3", gson.fromJson("{'name1':'v1','name2':'v2','name3':'v3'}", MyClass.class).b);
  }

// com.google.gson.functional.StreamingTypeAdaptersTest::testSerialize
  public void testSerialize() {
    Truck truck = new Truck();
    truck.passengers = Arrays.asList(new Person("Jesse", 29), new Person("Jodie", 29));
    truck.horsePower = 300;

    assertEquals("{'horsePower':300.0,"
        + "'passengers':[{'age':29,'name':'Jesse'},{'age':29,'name':'Jodie'}]}",
        truckAdapter.toJson(truck).replace('\"', '\''));
  }

// com.google.gson.functional.StreamingTypeAdaptersTest::testDeserialize
  public void testDeserialize() throws IOException {
    String json = "{'horsePower':300.0,"
        + "'passengers':[{'age':29,'name':'Jesse'},{'age':29,'name':'Jodie'}]}";
    Truck truck = truckAdapter.fromJson(json.replace('\'', '\"'));
    assertEquals(300.0, truck.horsePower);
    assertEquals(Arrays.asList(new Person("Jesse", 29), new Person("Jodie", 29)), truck.passengers);
  }

// com.google.gson.functional.StreamingTypeAdaptersTest::testSerializeNullField
  public void testSerializeNullField() {
    Truck truck = new Truck();
    truck.passengers = null;
    assertEquals("{'horsePower':0.0,'passengers':null}",
        truckAdapter.toJson(truck).replace('\"', '\''));
  }

// com.google.gson.functional.StreamingTypeAdaptersTest::testDeserializeNullField
  public void testDeserializeNullField() throws IOException {
    Truck truck = truckAdapter.fromJson("{'horsePower':0.0,'passengers':null}".replace('\'', '\"'));
    assertNull(truck.passengers);
  }

// com.google.gson.functional.StreamingTypeAdaptersTest::testSerializeNullObject
  public void testSerializeNullObject() {
    Truck truck = new Truck();
    truck.passengers = Arrays.asList((Person) null);
    assertEquals("{'horsePower':0.0,'passengers':[null]}",
        truckAdapter.toJson(truck).replace('\"', '\''));
  }

// com.google.gson.functional.StreamingTypeAdaptersTest::testDeserializeNullObject
  public void testDeserializeNullObject() throws IOException {
    Truck truck = truckAdapter.fromJson("{'horsePower':0.0,'passengers':[null]}".replace('\'', '\"'));
    assertEquals(Arrays.asList((Person) null), truck.passengers);
  }

// com.google.gson.functional.StreamingTypeAdaptersTest::testSerializeWithCustomTypeAdapter
  public void testSerializeWithCustomTypeAdapter() {
    usePersonNameAdapter();
    Truck truck = new Truck();
    truck.passengers = Arrays.asList(new Person("Jesse", 29), new Person("Jodie", 29));
    assertEquals("{'horsePower':0.0,'passengers':['Jesse','Jodie']}",
        truckAdapter.toJson(truck).replace('\"', '\''));
  }

// com.google.gson.functional.StreamingTypeAdaptersTest::testDeserializeWithCustomTypeAdapter
  public void testDeserializeWithCustomTypeAdapter() throws IOException {
    usePersonNameAdapter();
    Truck truck = truckAdapter.fromJson("{'horsePower':0.0,'passengers':['Jesse','Jodie']}".replace('\'', '\"'));
    assertEquals(Arrays.asList(new Person("Jesse", -1), new Person("Jodie", -1)), truck.passengers);
  }

// com.google.gson.functional.StreamingTypeAdaptersTest::testSerializeMap
  public void testSerializeMap() {
    Map<String, Double> map = new LinkedHashMap<String, Double>();
    map.put("a", 5.0);
    map.put("b", 10.0);
    assertEquals("{'a':5.0,'b':10.0}", mapAdapter.toJson(map).replace('"', '\''));
  }

// com.google.gson.functional.StreamingTypeAdaptersTest::testDeserializeMap
  public void testDeserializeMap() throws IOException {
    Map<String, Double> map = new LinkedHashMap<String, Double>();
    map.put("a", 5.0);
    map.put("b", 10.0);
    assertEquals(map, mapAdapter.fromJson("{'a':5.0,'b':10.0}".replace('\'', '\"')));
  }

// com.google.gson.functional.StreamingTypeAdaptersTest::testSerialize1dArray
  public void testSerialize1dArray() {
    TypeAdapter<double[]> arrayAdapter = miniGson.getAdapter(new TypeToken<double[]>() {});
    assertEquals("[1.0,2.0,3.0]", arrayAdapter.toJson(new double[]{ 1.0, 2.0, 3.0 }));
  }

// com.google.gson.functional.StreamingTypeAdaptersTest::testDeserialize1dArray
  public void testDeserialize1dArray() throws IOException {
    TypeAdapter<double[]> arrayAdapter = miniGson.getAdapter(new TypeToken<double[]>() {});
    double[] array = arrayAdapter.fromJson("[1.0,2.0,3.0]");
    assertTrue(Arrays.toString(array), Arrays.equals(new double[]{1.0, 2.0, 3.0}, array));
  }

// com.google.gson.functional.StreamingTypeAdaptersTest::testSerialize2dArray
  public void testSerialize2dArray() {
    TypeAdapter<double[][]> arrayAdapter = miniGson.getAdapter(new TypeToken<double[][]>() {});
    double[][] array = { {1.0, 2.0 }, { 3.0 } };
    assertEquals("[[1.0,2.0],[3.0]]", arrayAdapter.toJson(array));
  }

// com.google.gson.functional.StreamingTypeAdaptersTest::testDeserialize2dArray
  public void testDeserialize2dArray() throws IOException {
    TypeAdapter<double[][]> arrayAdapter = miniGson.getAdapter(new TypeToken<double[][]>() {});
    double[][] array = arrayAdapter.fromJson("[[1.0,2.0],[3.0]]");
    double[][] expected = { {1.0, 2.0 }, { 3.0 } };
    assertTrue(Arrays.toString(array), Arrays.deepEquals(expected, array));
  }

// com.google.gson.functional.StreamingTypeAdaptersTest::testNullSafe
  public void testNullSafe() {
    TypeAdapter<Person> typeAdapter = new TypeAdapter<Person>() {
      @Override public Person read(JsonReader in) throws IOException {
        String[] values = in.nextString().split(",");
        return new Person(values[0], Integer.parseInt(values[1]));
      }
      public void write(JsonWriter out, Person person) throws IOException {
        out.value(person.name + "," + person.age);
      }
    };
    Gson gson = new GsonBuilder().registerTypeAdapter(
        Person.class, typeAdapter).create();
    Truck truck = new Truck();
    truck.horsePower = 1.0D;
    truck.passengers = new ArrayList<Person>();
    truck.passengers.add(null);
    truck.passengers.add(new Person("jesse", 30));
    try {
      gson.toJson(truck, Truck.class);
      fail();
    } catch (NullPointerException expected) {}
    String json = "{horsePower:1.0,passengers:[null,'jesse,30']}";
    try {
      gson.fromJson(json, Truck.class);
      fail();
    } catch (JsonSyntaxException expected) {}
    gson = new GsonBuilder().registerTypeAdapter(Person.class, typeAdapter.nullSafe()).create();
    assertEquals("{\"horsePower\":1.0,\"passengers\":[null,\"jesse,30\"]}",
        gson.toJson(truck, Truck.class));
    truck = gson.fromJson(json, Truck.class);
    assertEquals(1.0D, truck.horsePower);
    assertNull(truck.passengers.get(0));
    assertEquals("jesse", truck.passengers.get(1).name);
  }

// com.google.gson.functional.StreamingTypeAdaptersTest::testSerializeRecursive
  public void testSerializeRecursive() {
    TypeAdapter<Node> nodeAdapter = miniGson.getAdapter(Node.class);
    Node root = new Node("root");
    root.left = new Node("left");
    root.right = new Node("right");
    assertEquals("{'label':'root',"
        + "'left':{'label':'left','left':null,'right':null},"
        + "'right':{'label':'right','left':null,'right':null}}",
        nodeAdapter.toJson(root).replace('"', '\''));
  }

// com.google.gson.functional.StreamingTypeAdaptersTest::testFromJsonTree
  public void testFromJsonTree() {
    JsonObject truckObject = new JsonObject();
    truckObject.add("horsePower", new JsonPrimitive(300));
    JsonArray passengersArray = new JsonArray();
    JsonObject jesseObject = new JsonObject();
    jesseObject.add("age", new JsonPrimitive(30));
    jesseObject.add("name", new JsonPrimitive("Jesse"));
    passengersArray.add(jesseObject);
    truckObject.add("passengers", passengersArray);

    Truck truck = truckAdapter.fromJsonTree(truckObject);
    assertEquals(300.0, truck.horsePower);
    assertEquals(Arrays.asList(new Person("Jesse", 30)), truck.passengers);
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

// com.google.gson.functional.ThrowableFunctionalTest::testExceptionWithoutCause
  public void testExceptionWithoutCause() {
    RuntimeException e = new RuntimeException("hello");
    String json = gson.toJson(e);
    assertTrue(json.contains("hello"));

    e = gson.fromJson("{'detailMessage':'hello'}", RuntimeException.class);
    assertEquals("hello", e.getMessage());
  }

// com.google.gson.functional.ThrowableFunctionalTest::testExceptionWithCause
  public void testExceptionWithCause() {
    Exception e = new Exception("top level", new IOException("io error"));
    String json = gson.toJson(e);
    assertTrue(json.contains("{\"detailMessage\":\"top level\",\"cause\":{\"detailMessage\":\"io error\""));

    e = gson.fromJson("{'detailMessage':'top level','cause':{'detailMessage':'io error'}}", Exception.class);
    assertEquals("top level", e.getMessage());
    assertTrue(e.getCause() instanceof Throwable); 
    assertEquals("io error", e.getCause().getMessage());
  }

// com.google.gson.functional.ThrowableFunctionalTest::testSerializedNameOnExceptionFields
  public void testSerializedNameOnExceptionFields() {
    MyException e = new MyException();
    String json = gson.toJson(e);
    assertTrue(json.contains("{\"my_custom_name\":\"myCustomMessageValue\""));
  }

// com.google.gson.functional.ThrowableFunctionalTest::testErrorWithoutCause
  public void testErrorWithoutCause() {
    OutOfMemoryError e = new OutOfMemoryError("hello");
    String json = gson.toJson(e);
    assertTrue(json.contains("hello"));

    e = gson.fromJson("{'detailMessage':'hello'}", OutOfMemoryError.class);
    assertEquals("hello", e.getMessage());
  }

// com.google.gson.functional.ThrowableFunctionalTest::testErrornWithCause
  public void testErrornWithCause() {
    Error e = new Error("top level", new IOException("io error"));
    String json = gson.toJson(e);
    assertTrue(json.contains("top level"));
    assertTrue(json.contains("io error"));

    e = gson.fromJson("{'detailMessage':'top level','cause':{'detailMessage':'io error'}}", Error.class);
    assertEquals("top level", e.getMessage());
    assertTrue(e.getCause() instanceof Throwable); 
    assertEquals("io error", e.getCause().getMessage());
  }

// com.google.gson.functional.TreeTypeAdaptersTest::testSerializeId
  public void testSerializeId() {
    String json = gson.toJson(course, TYPE_COURSE_HISTORY);
    assertTrue(json.contains(String.valueOf(COURSE_ID.getValue())));
    assertTrue(json.contains(String.valueOf(STUDENT1_ID.getValue())));
    assertTrue(json.contains(String.valueOf(STUDENT2_ID.getValue())));
  }

// com.google.gson.functional.TreeTypeAdaptersTest::testDeserializeId
  public void testDeserializeId() {
    String json = "{courseId:1,students:[{id:1,name:'first'},{id:6,name:'second'}],"
      + "numAssignments:4,assignment:{}}";
    Course<HistoryCourse> target = gson.fromJson(json, TYPE_COURSE_HISTORY);
    assertEquals("1", target.getStudents().get(0).id.getValue());
    assertEquals("6", target.getStudents().get(1).id.getValue());
    assertEquals("1", target.getId().getValue());
  }

// com.google.gson.functional.TypeAdapterPrecedenceTest::testNonstreamingFollowedByNonstreaming
  public void testNonstreamingFollowedByNonstreaming() {
    Gson gson = new GsonBuilder()
        .registerTypeAdapter(Foo.class, newSerializer("serializer 1"))
        .registerTypeAdapter(Foo.class, newSerializer("serializer 2"))
        .registerTypeAdapter(Foo.class, newDeserializer("deserializer 1"))
        .registerTypeAdapter(Foo.class, newDeserializer("deserializer 2"))
        .create();
    assertEquals("\"foo via serializer 2\"", gson.toJson(new Foo("foo")));
    assertEquals("foo via deserializer 2", gson.fromJson("foo", Foo.class).name);
  }

// com.google.gson.functional.TypeAdapterPrecedenceTest::testStreamingFollowedByStreaming
  public void testStreamingFollowedByStreaming() {
    Gson gson = new GsonBuilder()
        .registerTypeAdapter(Foo.class, newTypeAdapter("type adapter 1"))
        .registerTypeAdapter(Foo.class, newTypeAdapter("type adapter 2"))
        .create();
    assertEquals("\"foo via type adapter 2\"", gson.toJson(new Foo("foo")));
    assertEquals("foo via type adapter 2", gson.fromJson("foo", Foo.class).name);
  }

// com.google.gson.functional.TypeAdapterPrecedenceTest::testSerializeNonstreamingTypeAdapterFollowedByStreamingTypeAdapter
  public void testSerializeNonstreamingTypeAdapterFollowedByStreamingTypeAdapter() {
    Gson gson = new GsonBuilder()
        .registerTypeAdapter(Foo.class, newSerializer("serializer"))
        .registerTypeAdapter(Foo.class, newDeserializer("deserializer"))
        .registerTypeAdapter(Foo.class, newTypeAdapter("type adapter"))
        .create();
    assertEquals("\"foo via type adapter\"", gson.toJson(new Foo("foo")));
    assertEquals("foo via type adapter", gson.fromJson("foo", Foo.class).name);
  }

// com.google.gson.functional.TypeAdapterPrecedenceTest::testStreamingFollowedByNonstreaming
  public void testStreamingFollowedByNonstreaming() {
    Gson gson = new GsonBuilder()
        .registerTypeAdapter(Foo.class, newTypeAdapter("type adapter"))
        .registerTypeAdapter(Foo.class, newSerializer("serializer"))
        .registerTypeAdapter(Foo.class, newDeserializer("deserializer"))
        .create();
    assertEquals("\"foo via serializer\"", gson.toJson(new Foo("foo")));
    assertEquals("foo via deserializer", gson.fromJson("foo", Foo.class).name);
  }

// com.google.gson.functional.TypeAdapterPrecedenceTest::testStreamingHierarchicalFollowedByNonstreaming
  public void testStreamingHierarchicalFollowedByNonstreaming() {
    Gson gson = new GsonBuilder()
        .registerTypeHierarchyAdapter(Foo.class, newTypeAdapter("type adapter"))
        .registerTypeAdapter(Foo.class, newSerializer("serializer"))
        .registerTypeAdapter(Foo.class, newDeserializer("deserializer"))
        .create();
    assertEquals("\"foo via serializer\"", gson.toJson(new Foo("foo")));
    assertEquals("foo via deserializer", gson.fromJson("foo", Foo.class).name);
  }

// com.google.gson.functional.TypeAdapterPrecedenceTest::testStreamingFollowedByNonstreamingHierarchical
  public void testStreamingFollowedByNonstreamingHierarchical() {
    Gson gson = new GsonBuilder()
        .registerTypeAdapter(Foo.class, newTypeAdapter("type adapter"))
        .registerTypeHierarchyAdapter(Foo.class, newSerializer("serializer"))
        .registerTypeHierarchyAdapter(Foo.class, newDeserializer("deserializer"))
        .create();
    assertEquals("\"foo via type adapter\"", gson.toJson(new Foo("foo")));
    assertEquals("foo via type adapter", gson.fromJson("foo", Foo.class).name);
  }

// com.google.gson.functional.TypeAdapterPrecedenceTest::testStreamingHierarchicalFollowedByNonstreamingHierarchical
  public void testStreamingHierarchicalFollowedByNonstreamingHierarchical() {
    Gson gson = new GsonBuilder()
        .registerTypeHierarchyAdapter(Foo.class, newSerializer("serializer"))
        .registerTypeHierarchyAdapter(Foo.class, newDeserializer("deserializer"))
        .registerTypeHierarchyAdapter(Foo.class, newTypeAdapter("type adapter"))
        .create();
    assertEquals("\"foo via type adapter\"", gson.toJson(new Foo("foo")));
    assertEquals("foo via type adapter", gson.fromJson("foo", Foo.class).name);
  }

// com.google.gson.functional.TypeAdapterPrecedenceTest::testNonstreamingHierarchicalFollowedByNonstreaming
  public void testNonstreamingHierarchicalFollowedByNonstreaming() {
    Gson gson = new GsonBuilder()
        .registerTypeHierarchyAdapter(Foo.class, newSerializer("hierarchical"))
        .registerTypeHierarchyAdapter(Foo.class, newDeserializer("hierarchical"))
        .registerTypeAdapter(Foo.class, newSerializer("non hierarchical"))
        .registerTypeAdapter(Foo.class, newDeserializer("non hierarchical"))
        .create();
    assertEquals("\"foo via non hierarchical\"", gson.toJson(new Foo("foo")));
    assertEquals("foo via non hierarchical", gson.fromJson("foo", Foo.class).name);
  }

// com.google.gson.functional.TypeHierarchyAdapterTest::testTypeHierarchy
  public void testTypeHierarchy() {
    Manager andy = new Manager();
    andy.userid = "andy";
    andy.startDate = 2005;
    andy.minions = new Employee[] {
        new Employee("inder", 2007),
        new Employee("joel", 2006),
        new Employee("jesse", 2006),
    };

    CEO eric = new CEO();
    eric.userid = "eric";
    eric.startDate = 2001;
    eric.assistant = new Employee("jerome", 2006);

    eric.minions = new Employee[] {
        new Employee("larry", 1998),
        new Employee("sergey", 1998),
        andy,
    };

    Gson gson = new GsonBuilder()
        .registerTypeHierarchyAdapter(Employee.class, new EmployeeAdapter())
        .setPrettyPrinting()
        .create();

    Company company = new Company();
    company.ceo = eric;

    String json = gson.toJson(company, Company.class);
    assertEquals("{\n" +
        "  \"ceo\": {\n" +
        "    \"userid\": \"eric\",\n" +
        "    \"startDate\": 2001,\n" +
        "    \"minions\": [\n" +
        "      {\n" +
        "        \"userid\": \"larry\",\n" +
        "        \"startDate\": 1998\n" +
        "      },\n" +
        "      {\n" +
        "        \"userid\": \"sergey\",\n" +
        "        \"startDate\": 1998\n" +
        "      },\n" +
        "      {\n" +
        "        \"userid\": \"andy\",\n" +
        "        \"startDate\": 2005,\n" +
        "        \"minions\": [\n" +
        "          {\n" +
        "            \"userid\": \"inder\",\n" +
        "            \"startDate\": 2007\n" +
        "          },\n" +
        "          {\n" +
        "            \"userid\": \"joel\",\n" +
        "            \"startDate\": 2006\n" +
        "          },\n" +
        "          {\n" +
        "            \"userid\": \"jesse\",\n" +
        "            \"startDate\": 2006\n" +
        "          }\n" +
        "        ]\n" +
        "      }\n" +
        "    ],\n" +
        "    \"assistant\": {\n" +
        "      \"userid\": \"jerome\",\n" +
        "      \"startDate\": 2006\n" +
        "    }\n" +
        "  }\n" +
        "}", json);

    Company copied = gson.fromJson(json, Company.class);
    assertEquals(json, gson.toJson(copied, Company.class));
    assertEquals(copied.ceo.userid, company.ceo.userid);
    assertEquals(copied.ceo.assistant.userid, company.ceo.assistant.userid);
    assertEquals(copied.ceo.minions[0].userid, company.ceo.minions[0].userid);
    assertEquals(copied.ceo.minions[1].userid, company.ceo.minions[1].userid);
    assertEquals(copied.ceo.minions[2].userid, company.ceo.minions[2].userid);
    assertEquals(((Manager) copied.ceo.minions[2]).minions[0].userid,
        ((Manager) company.ceo.minions[2]).minions[0].userid);
    assertEquals(((Manager) copied.ceo.minions[2]).minions[1].userid,
        ((Manager) company.ceo.minions[2]).minions[1].userid);
  }

// com.google.gson.functional.TypeHierarchyAdapterTest::testRegisterSuperTypeFirst
  public void testRegisterSuperTypeFirst() {
    Gson gson = new GsonBuilder()
        .registerTypeHierarchyAdapter(Employee.class, new EmployeeAdapter())
        .registerTypeHierarchyAdapter(Manager.class, new ManagerAdapter())
        .create();

    Manager manager = new Manager();
    manager.userid = "inder";

    String json = gson.toJson(manager, Manager.class);
    assertEquals("\"inder\"", json);
    Manager copied = gson.fromJson(json, Manager.class);
    assertEquals(manager.userid, copied.userid);
  }

// com.google.gson.functional.TypeHierarchyAdapterTest::testRegisterSubTypeFirstAllowed
  public void testRegisterSubTypeFirstAllowed() {
    new GsonBuilder()
        .registerTypeHierarchyAdapter(Manager.class, new ManagerAdapter())
        .registerTypeHierarchyAdapter(Employee.class, new EmployeeAdapter())
        .create();
  }

// com.google.gson.functional.TypeVariableTest::testAdvancedTypeVariables
  public void testAdvancedTypeVariables() throws Exception {
    Gson gson = new Gson();
    Bar bar1 = new Bar("someString", 1, true);
    ArrayList<Integer> arrayList = new ArrayList<Integer>();
    arrayList.add(1);
    arrayList.add(2);
    arrayList.add(3);
    bar1.map.put("key1", arrayList);
    bar1.map.put("key2", new ArrayList<Integer>());
    String json = gson.toJson(bar1);

    Bar bar2 = gson.fromJson(json, Bar.class);
    assertEquals(bar1, bar2);
  }

// com.google.gson.functional.TypeVariableTest::testTypeVariablesViaTypeParameter
  public void testTypeVariablesViaTypeParameter() throws Exception {
    Gson gson = new Gson();
    Foo<String, Integer> original = new Foo<String, Integer>("e", 5, false);
    original.map.put("f", Arrays.asList(6, 7));
    Type type = new TypeToken<Foo<String, Integer>>() {}.getType();
    String json = gson.toJson(original, type);
    assertEquals("{\"someSField\":\"e\",\"someTField\":5,\"map\":{\"f\":[6,7]},\"redField\":false}",
        json);
    assertEquals(original, gson.<Foo<String, Integer>>fromJson(json, type));
  }

// com.google.gson.functional.TypeVariableTest::testBasicTypeVariables
  public void testBasicTypeVariables() throws Exception {
    Gson gson = new Gson();
    Blue blue1 = new Blue(true);
    String json = gson.toJson(blue1);

    Blue blue2 = gson.fromJson(json, Blue.class);
    assertEquals(blue1, blue2);
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

// com.google.gson.functional.UncategorizedTest::testGsonInstanceReusableForSerializationAndDeserialization
  public void testGsonInstanceReusableForSerializationAndDeserialization() {
    BagOfPrimitives bag = new BagOfPrimitives();
    String json = gson.toJson(bag);
    BagOfPrimitives deserialized = gson.fromJson(json, BagOfPrimitives.class);
    assertEquals(bag, deserialized);
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

// com.google.gson.functional.UncategorizedTest::testTrailingWhitespace
  public void testTrailingWhitespace() throws Exception {
    List<Integer> integers = gson.fromJson("[1,2,3]  \n\n  ",
        new TypeToken<List<Integer>>() {}.getType());
    assertEquals(Arrays.asList(1, 2, 3), integers);
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
    assertEquals("null", gson.toJson(new Version1_2()));
  }

// com.google.gson.functional.VersioningTest::testIgnoreLaterVersionClassDeserialization
  public void testIgnoreLaterVersionClassDeserialization() {
    Gson gson = builder.setVersion(1.0).create();
    String json = "{\"a\":3,\"b\":4,\"c\":5,\"d\":6}";
    Version1_2 version1_2  = gson.fromJson(json, Version1_2.class);
    
    
    assertNull(version1_2);
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

// com.google.gson.internal.GsonTypesTest::testNewParameterizedTypeWithoutOwner
  public void testNewParameterizedTypeWithoutOwner() throws Exception {
    
    Type type = $Gson$Types.newParameterizedTypeWithOwner(null, List.class, A.class);
    assertEquals(A.class, getFirstTypeArgument(type));

    
    type = $Gson$Types.newParameterizedTypeWithOwner(null, A.class, B.class);
    assertEquals(B.class, getFirstTypeArgument(type));

    final class D {
    }
    try {
      
      $Gson$Types.newParameterizedTypeWithOwner(null, D.class, A.class);
      fail();
    } catch (IllegalArgumentException expected) {}

    
    type = $Gson$Types.newParameterizedTypeWithOwner(null, A.class, D.class);
    assertEquals(D.class, getFirstTypeArgument(type));
  }

// com.google.gson.internal.GsonTypesTest::testGetFirstTypeArgument
  public void testGetFirstTypeArgument() throws Exception {
    assertNull(getFirstTypeArgument(A.class));

    Type type = $Gson$Types.newParameterizedTypeWithOwner(null, A.class, B.class, C.class);
    assertEquals(B.class, getFirstTypeArgument(type));
  }

// com.google.gson.internal.bind.RecursiveTypesResolveTest::testRecursiveResolveSimple
  public void testRecursiveResolveSimple() {
    TypeAdapter<Foo1> adapter = new Gson().getAdapter(Foo1.class);
    assertNotNull(adapter);
  }

// com.google.gson.internal.bind.RecursiveTypesResolveTest::testIssue603PrintStream
  public void testIssue603PrintStream() {
    TypeAdapter<PrintStream> adapter = new Gson().getAdapter(PrintStream.class);
    assertNotNull(adapter);
  }

// com.google.gson.internal.bind.RecursiveTypesResolveTest::testIssue440WeakReference
  public void testIssue440WeakReference() throws Exception {
    TypeAdapter<WeakReference> adapter = new Gson().getAdapter(WeakReference.class);
    assertNotNull(adapter);
  }

// com.google.gson.internal.bind.RecursiveTypesResolveTest::testDoubleSupertype
  public void testDoubleSupertype() {
    assertEquals($Gson$Types.supertypeOf(Number.class),
            $Gson$Types.supertypeOf($Gson$Types.supertypeOf(Number.class)));
  }

// com.google.gson.internal.bind.RecursiveTypesResolveTest::testDoubleSubtype
  public void testDoubleSubtype() {
    assertEquals($Gson$Types.subtypeOf(Number.class),
            $Gson$Types.subtypeOf($Gson$Types.subtypeOf(Number.class)));
  }

// com.google.gson.internal.bind.RecursiveTypesResolveTest::testSuperSubtype
  public void testSuperSubtype() {
    assertEquals($Gson$Types.subtypeOf(Object.class),
            $Gson$Types.supertypeOf($Gson$Types.subtypeOf(Number.class)));
  }

// com.google.gson.internal.bind.RecursiveTypesResolveTest::testSubSupertype
  public void testSubSupertype() {
    assertEquals($Gson$Types.subtypeOf(Object.class),
            $Gson$Types.subtypeOf($Gson$Types.supertypeOf(Number.class)));
  }

// com.google.gson.metrics.PerformanceTest::testDummy
  public void testDummy() {    
    
  }

// com.google.gson.reflect.TypeTokenTest::testIsAssignableFromRawTypes
  public void testIsAssignableFromRawTypes() {
    assertTrue(TypeToken.get(Object.class).isAssignableFrom(String.class));
    assertFalse(TypeToken.get(String.class).isAssignableFrom(Object.class));
    assertTrue(TypeToken.get(RandomAccess.class).isAssignableFrom(ArrayList.class));
    assertFalse(TypeToken.get(ArrayList.class).isAssignableFrom(RandomAccess.class));
  }

// com.google.gson.reflect.TypeTokenTest::testIsAssignableFromWithTypeParameters
  public void testIsAssignableFromWithTypeParameters() throws Exception {
    Type a = getClass().getDeclaredField("listOfInteger").getGenericType();
    Type b = getClass().getDeclaredField("listOfNumber").getGenericType();
    assertTrue(TypeToken.get(a).isAssignableFrom(a));
    assertTrue(TypeToken.get(b).isAssignableFrom(b));

    
    assertFalse(TypeToken.get(a).isAssignableFrom(b));
    
    assertFalse(TypeToken.get(b).isAssignableFrom(a));
  }

// com.google.gson.reflect.TypeTokenTest::testIsAssignableFromWithBasicWildcards
  public void testIsAssignableFromWithBasicWildcards() throws Exception {
    Type a = getClass().getDeclaredField("listOfString").getGenericType();
    Type b = getClass().getDeclaredField("listOfUnknown").getGenericType();
    assertTrue(TypeToken.get(a).isAssignableFrom(a));
    assertTrue(TypeToken.get(b).isAssignableFrom(b));

    
    assertFalse(TypeToken.get(a).isAssignableFrom(b));
    listOfUnknown = listOfString; 
    
    
  }

// com.google.gson.reflect.TypeTokenTest::testIsAssignableFromWithNestedWildcards
  public void testIsAssignableFromWithNestedWildcards() throws Exception {
    Type a = getClass().getDeclaredField("listOfSetOfString").getGenericType();
    Type b = getClass().getDeclaredField("listOfSetOfUnknown").getGenericType();
    assertTrue(TypeToken.get(a).isAssignableFrom(a));
    assertTrue(TypeToken.get(b).isAssignableFrom(b));

    
    assertFalse(TypeToken.get(a).isAssignableFrom(b));
    
    assertFalse(TypeToken.get(b).isAssignableFrom(a));
  }

// com.google.gson.reflect.TypeTokenTest::testArrayFactory
  public void testArrayFactory() {
    TypeToken<?> expectedStringArray = new TypeToken<String[]>() {};
    assertEquals(expectedStringArray, TypeToken.getArray(String.class));

    TypeToken<?> expectedListOfStringArray = new TypeToken<List<String>[]>() {};
    Type listOfString = new TypeToken<List<String>>() {}.getType();
    assertEquals(expectedListOfStringArray, TypeToken.getArray(listOfString));
  }

// com.google.gson.reflect.TypeTokenTest::testParameterizedFactory
  public void testParameterizedFactory() {
    TypeToken<?> expectedListOfString = new TypeToken<List<String>>() {};
    assertEquals(expectedListOfString, TypeToken.getParameterized(List.class, String.class));

    TypeToken<?> expectedMapOfStringToString = new TypeToken<Map<String, String>>() {};
    assertEquals(expectedMapOfStringToString, TypeToken.getParameterized(Map.class, String.class, String.class));

    TypeToken<?> expectedListOfListOfListOfString = new TypeToken<List<List<List<String>>>>() {};
    Type listOfString = TypeToken.getParameterized(List.class, String.class).getType();
    Type listOfListOfString = TypeToken.getParameterized(List.class, listOfString).getType();
    assertEquals(expectedListOfListOfListOfString, TypeToken.getParameterized(List.class, listOfListOfString));
  }

// com.google.gson.regression.JsonAdapterNullSafeTest::testNullSafeBugSerialize
  public void testNullSafeBugSerialize() throws Exception {
    Device device = new Device("ec57803e");
    gson.toJson(device);
  }

// com.google.gson.regression.JsonAdapterNullSafeTest::testNullSafeBugDeserialize
  public void testNullSafeBugDeserialize() throws Exception {
    Device device = gson.fromJson("{'id':'ec57803e2'}", Device.class);
    assertEquals("ec57803e2", device.id);
  }

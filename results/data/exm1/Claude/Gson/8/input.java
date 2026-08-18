// buggy code
  public static UnsafeAllocator create() {
    // try JVM
    // public class Unsafe {
    //   public Object allocateInstance(Class<?> type);
    // }
    try {
      Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
      Field f = unsafeClass.getDeclaredField("theUnsafe");
      f.setAccessible(true);
      final Object unsafe = f.get(null);
      final Method allocateInstance = unsafeClass.getMethod("allocateInstance", Class.class);
      return new UnsafeAllocator() {
        @Override
        @SuppressWarnings("unchecked")
        public <T> T newInstance(Class<T> c) throws Exception {
          return (T) allocateInstance.invoke(unsafe, c);
        }
      };
    } catch (Exception ignored) {
    }

    // try dalvikvm, post-gingerbread
    // public class ObjectStreamClass {
    //   private static native int getConstructorId(Class<?> c);
    //   private static native Object newInstance(Class<?> instantiationClass, int methodId);
    // }
    try {
      Method getConstructorId = ObjectStreamClass.class
          .getDeclaredMethod("getConstructorId", Class.class);
      getConstructorId.setAccessible(true);
      final int constructorId = (Integer) getConstructorId.invoke(null, Object.class);
      final Method newInstance = ObjectStreamClass.class
          .getDeclaredMethod("newInstance", Class.class, int.class);
      newInstance.setAccessible(true);
      return new UnsafeAllocator() {
        @Override
        @SuppressWarnings("unchecked")
        public <T> T newInstance(Class<T> c) throws Exception {
          return (T) newInstance.invoke(null, c, constructorId);
        }
      };
    } catch (Exception ignored) {
    }

    // try dalvikvm, pre-gingerbread
    // public class ObjectInputStream {
    //   private static native Object newInstance(
    //     Class<?> instantiationClass, Class<?> constructorClass);
    // }
    try {
      final Method newInstance = ObjectInputStream.class
          .getDeclaredMethod("newInstance", Class.class, Class.class);
      newInstance.setAccessible(true);
      return new UnsafeAllocator() {
        @Override
        @SuppressWarnings("unchecked")
        public <T> T newInstance(Class<T> c) throws Exception {
          return (T) newInstance.invoke(null, c, Object.class);
        }
      };
    } catch (Exception ignored) {
    }

    // give up
    return new UnsafeAllocator() {
      @Override
      public <T> T newInstance(Class<T> c) {
        throw new UnsupportedOperationException("Cannot allocate " + c);
      }
    };
  }

// relevant test
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

// com.google.gson.functional.JsonAdapterAnnotationOnClassesTest::testNullSafeObjectFromJson
  public void testNullSafeObjectFromJson() {
    Gson gson = new Gson();
    NullableClass fromJson = gson.fromJson("null", NullableClass.class);
    assertNull(fromJson);
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

// com.google.gson.functional.JsonAdapterAnnotationOnFieldsTest::testJsonAdapterWrappedInNullSafeAsRequested
  public void testJsonAdapterWrappedInNullSafeAsRequested() {
    Gson gson = new Gson();
    String fromJson = "{'part':null}";

    GadgetWithOptionalPart gadget = gson.fromJson(fromJson, GadgetWithOptionalPart.class);
    assertNull(gadget.part);

    String toJson = gson.toJson(gadget);
    assertFalse(toJson.contains("PartJsonFieldAnnotationAdapter"));
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

// com.google.gson.functional.MapTest::testMapDeserializationWithUnquotedIntegerKeys
  public void testMapDeserializationWithUnquotedIntegerKeys() {
    Type typeOfMap = new TypeToken<Map<Integer, String>>() {}.getType();
    Map<Integer, String> map = gson.fromJson("{123:\"456\"}", typeOfMap);
    assertEquals(1, map.size());
    assertTrue(map.containsKey(123));
    assertEquals("456", map.get(123));
  }

// com.google.gson.functional.MapTest::testMapDeserializationWithLongKeys
  public void testMapDeserializationWithLongKeys() {
    long longValue = 9876543210L;
    String json = String.format("{\"%d\":\"456\"}", longValue);
    Type typeOfMap = new TypeToken<Map<Long, String>>() {}.getType();
    Map<Long, String> map = gson.fromJson(json, typeOfMap);
    assertEquals(1, map.size());
    assertTrue(map.containsKey(longValue));
    assertEquals("456", map.get(longValue));
  }

// com.google.gson.functional.MapTest::testMapDeserializationWithUnquotedLongKeys
  public void testMapDeserializationWithUnquotedLongKeys() {
    long longKey = 9876543210L;
    String json = String.format("{%d:\"456\"}", longKey);
    Type typeOfMap = new TypeToken<Map<Long, String>>() {}.getType();
    Map<Long, String> map = gson.fromJson(json, typeOfMap);
    assertEquals(1, map.size());
    assertTrue(map.containsKey(longKey));
    assertEquals("456", map.get(longKey));
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

// com.google.gson.functional.MapTest::testConcurrentMap
  public void testConcurrentMap() throws Exception {
    Type typeOfMap = new TypeToken<ConcurrentMap<Integer, String>>() {}.getType();
    ConcurrentMap<Integer, String> map = gson.fromJson("{\"123\":\"456\"}", typeOfMap);
    assertEquals(1, map.size());
    assertTrue(map.containsKey(123));
    assertEquals("456", map.get(123));
    String json = gson.toJson(map);
    assertEquals("{\"123\":\"456\"}", json);
  }

// com.google.gson.functional.MapTest::testConcurrentHashMap
  public void testConcurrentHashMap() throws Exception {
    Type typeOfMap = new TypeToken<ConcurrentHashMap<Integer, String>>() {}.getType();
    ConcurrentHashMap<Integer, String> map = gson.fromJson("{\"123\":\"456\"}", typeOfMap);
    assertEquals(1, map.size());
    assertTrue(map.containsKey(123));
    assertEquals("456", map.get(123));
    String json = gson.toJson(map);
    assertEquals("{\"123\":\"456\"}", json);
  }

// com.google.gson.functional.MapTest::testConcurrentNavigableMap
  public void testConcurrentNavigableMap() throws Exception {
    Type typeOfMap = new TypeToken<ConcurrentNavigableMap<Integer, String>>() {}.getType();
    ConcurrentNavigableMap<Integer, String> map = gson.fromJson("{\"123\":\"456\"}", typeOfMap);
    assertEquals(1, map.size());
    assertTrue(map.containsKey(123));
    assertEquals("456", map.get(123));
    String json = gson.toJson(map);
    assertEquals("{\"123\":\"456\"}", json);
  }

// com.google.gson.functional.MapTest::testConcurrentSkipListMap
  public void testConcurrentSkipListMap() throws Exception {
    Type typeOfMap = new TypeToken<ConcurrentSkipListMap<Integer, String>>() {}.getType();
    ConcurrentSkipListMap<Integer, String> map = gson.fromJson("{\"123\":\"456\"}", typeOfMap);
    assertEquals(1, map.size());
    assertTrue(map.containsKey(123));
    assertEquals("456", map.get(123));
    String json = gson.toJson(map);
    assertEquals("{\"123\":\"456\"}", json);
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

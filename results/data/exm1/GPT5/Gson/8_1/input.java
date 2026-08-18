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

// com.google.gson.functional.RuntimeTypeAdapterFactoryFunctionalTest::testSubclassesAutomaticallySerialzed
  public void testSubclassesAutomaticallySerialzed() throws Exception {
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
  public void testSerialize() throws IOException {
    Truck truck = new Truck();
    truck.passengers = Arrays.asList(new Person("Jesse", 29), new Person("Jodie", 29));
    truck.horsePower = 300;

    assertEquals("{'horsePower':300.0,"
        + "'passengers':[{'age':29,'name':'Jesse'},{'age':29,'name':'Jodie'}]}",
        toJson(truckAdapter, truck).replace('\"', '\''));
  }

// com.google.gson.functional.StreamingTypeAdaptersTest::testDeserialize
  public void testDeserialize() throws IOException {
    String json = "{'horsePower':300.0,"
        + "'passengers':[{'age':29,'name':'Jesse'},{'age':29,'name':'Jodie'}]}";
    Truck truck = fromJson(truckAdapter, json);
    assertEquals(300.0, truck.horsePower);
    assertEquals(Arrays.asList(new Person("Jesse", 29), new Person("Jodie", 29)), truck.passengers);
  }

// com.google.gson.functional.StreamingTypeAdaptersTest::testSerializeNullField
  public void testSerializeNullField() throws IOException {
    Truck truck = new Truck();
    truck.passengers = null;
    assertEquals("{'horsePower':0.0,'passengers':null}",
        toJson(truckAdapter, truck).replace('\"', '\''));
  }

// com.google.gson.functional.StreamingTypeAdaptersTest::testDeserializeNullField
  public void testDeserializeNullField() throws IOException {
    Truck truck = fromJson(truckAdapter, "{'horsePower':0.0,'passengers':null}");
    assertNull(truck.passengers);
  }

// com.google.gson.functional.StreamingTypeAdaptersTest::testSerializeNullObject
  public void testSerializeNullObject() throws IOException {
    Truck truck = new Truck();
    truck.passengers = Arrays.asList((Person) null);
    assertEquals("{'horsePower':0.0,'passengers':[null]}",
        toJson(truckAdapter, truck).replace('\"', '\''));
  }

// com.google.gson.functional.StreamingTypeAdaptersTest::testDeserializeNullObject
  public void testDeserializeNullObject() throws IOException {
    Truck truck = fromJson(truckAdapter, "{'horsePower':0.0,'passengers':[null]}");
    assertEquals(Arrays.asList((Person) null), truck.passengers);
  }

// com.google.gson.functional.StreamingTypeAdaptersTest::testSerializeWithCustomTypeAdapter
  public void testSerializeWithCustomTypeAdapter() throws IOException {
    usePersonNameAdapter();
    Truck truck = new Truck();
    truck.passengers = Arrays.asList(new Person("Jesse", 29), new Person("Jodie", 29));
    assertEquals("{'horsePower':0.0,'passengers':['Jesse','Jodie']}",
        toJson(truckAdapter, truck).replace('\"', '\''));
  }

// com.google.gson.functional.StreamingTypeAdaptersTest::testDeserializeWithCustomTypeAdapter
  public void testDeserializeWithCustomTypeAdapter() throws IOException {
    usePersonNameAdapter();
    Truck truck = fromJson(truckAdapter, "{'horsePower':0.0,'passengers':['Jesse','Jodie']}");
    assertEquals(Arrays.asList(new Person("Jesse", -1), new Person("Jodie", -1)), truck.passengers);
  }

// com.google.gson.functional.StreamingTypeAdaptersTest::testSerializeMap
  public void testSerializeMap() throws IOException {
    Map<String, Double> map = new LinkedHashMap<String, Double>();
    map.put("a", 5.0);
    map.put("b", 10.0);
    assertEquals("{'a':5.0,'b':10.0}", toJson(mapAdapter, map).replace('"', '\''));
  }

// com.google.gson.functional.StreamingTypeAdaptersTest::testDeserializeMap
  public void testDeserializeMap() throws IOException {
    Map<String, Double> map = new LinkedHashMap<String, Double>();
    map.put("a", 5.0);
    map.put("b", 10.0);
    assertEquals(map, fromJson(mapAdapter, "{'a':5.0,'b':10.0}"));
  }

// com.google.gson.functional.StreamingTypeAdaptersTest::testSerialize1dArray
  public void testSerialize1dArray() throws IOException {
    TypeAdapter<double[]> arrayAdapter = miniGson.getAdapter(new TypeToken<double[]>() {});
    assertEquals("[1.0,2.0,3.0]", toJson(arrayAdapter, new double[]{1.0, 2.0, 3.0}));
  }

// com.google.gson.functional.StreamingTypeAdaptersTest::testDeserialize1dArray
  public void testDeserialize1dArray() throws IOException {
    TypeAdapter<double[]> arrayAdapter = miniGson.getAdapter(new TypeToken<double[]>() {});
    double[] array = fromJson(arrayAdapter, "[1.0,2.0,3.0]");
    assertTrue(Arrays.toString(array), Arrays.equals(new double[]{1.0, 2.0, 3.0}, array));
  }

// com.google.gson.functional.StreamingTypeAdaptersTest::testSerialize2dArray
  public void testSerialize2dArray() throws IOException {
    TypeAdapter<double[][]> arrayAdapter = miniGson.getAdapter(new TypeToken<double[][]>() {});
    double[][] array = { {1.0, 2.0 }, { 3.0 } };
    assertEquals("[[1.0,2.0],[3.0]]", toJson(arrayAdapter, array));
  }

// com.google.gson.functional.StreamingTypeAdaptersTest::testDeserialize2dArray
  public void testDeserialize2dArray() throws IOException {
    TypeAdapter<double[][]> arrayAdapter = miniGson.getAdapter(new TypeToken<double[][]>() {});
    double[][] array = fromJson(arrayAdapter, "[[1.0,2.0],[3.0]]");
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
  public void testSerializeRecursive() throws IOException {
    TypeAdapter<Node> nodeAdapter = miniGson.getAdapter(Node.class);
    Node root = new Node("root");
    root.left = new Node("left");
    root.right = new Node("right");
    assertEquals("{'label':'root',"
        + "'left':{'label':'left','left':null,'right':null},"
        + "'right':{'label':'right','left':null,'right':null}}",
        toJson(nodeAdapter, root).replace('"', '\''));
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

// com.google.gson.internal.UnsafeAllocatorInstantiationTest::testInterfaceInstantiation
  public void testInterfaceInstantiation() {
    UnsafeAllocator unsafeAllocator = UnsafeAllocator.create();
    try {
      unsafeAllocator.newInstance(Interface.class);
      fail();
    } catch (Exception e) {
      assertEquals(e.getClass(), UnsupportedOperationException.class);
    }
  }

// com.google.gson.internal.UnsafeAllocatorInstantiationTest::testAbstractClassInstantiation
  public void testAbstractClassInstantiation() {
    UnsafeAllocator unsafeAllocator = UnsafeAllocator.create();
    try {
      unsafeAllocator.newInstance(AbstractClass.class);
      fail();
    } catch (Exception e) {
      assertEquals(e.getClass(), UnsupportedOperationException.class);
    }
  }

// com.google.gson.internal.UnsafeAllocatorInstantiationTest::testConcreteClassInstantiation
  public void testConcreteClassInstantiation() {
    UnsafeAllocator unsafeAllocator = UnsafeAllocator.create();
    try {
      unsafeAllocator.newInstance(ConcreteClass.class);
    } catch (Exception e) {
      fail();
    }
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

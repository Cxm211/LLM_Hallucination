// com/fasterxml/jackson/databind/ser/TestJsonSerialize2.java
public void testNumberEmptyInclusion() throws IOException {
    ObjectMapper mapper = new ObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
    // Test Double
    class DoublePojo { public Double d; DoublePojo(Double v) { d = v; } }
    assertEquals("{}", mapper.writeValueAsString(new DoublePojo(0.0)));
    assertEquals("{\"d\":2.5}", mapper.writeValueAsString(new DoublePojo(2.5)));
    // Test Float
    class FloatPojo { public Float f; FloatPojo(Float v) { f = v; } }
    assertEquals("{}", mapper.writeValueAsString(new FloatPojo(0.0f)));
    assertEquals("{\"f\":1.5}", mapper.writeValueAsString(new FloatPojo(1.5f)));
    // Test Long
    class LongPojo { public Long l; LongPojo(Long v) { l = v; } }
    assertEquals("{}", mapper.writeValueAsString(new LongPojo(0L)));
    assertEquals("{\"l\":100}", mapper.writeValueAsString(new LongPojo(100L)));
    // Test Short
    class ShortPojo { public Short s; ShortPojo(Short v) { s = v; } }
    assertEquals("{}", mapper.writeValueAsString(new ShortPojo((short)0)));
    assertEquals("{\"s\":10}", mapper.writeValueAsString(new ShortPojo((short)10)));
    // Test Byte
    class BytePojo { public Byte b; BytePojo(Byte v) { b = v; } }
    assertEquals("{}", mapper.writeValueAsString(new BytePojo((byte)0)));
    assertEquals("{\"b\":5}", mapper.writeValueAsString(new BytePojo((byte)5)));
}

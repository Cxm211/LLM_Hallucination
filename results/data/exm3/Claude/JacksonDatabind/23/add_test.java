// com/fasterxml/jackson/databind/ser/TestJsonSerialize2.java
public void testEmptyInclusionPrimitiveTypes() throws IOException
{
    ObjectMapper inclMapper = new ObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
    
    // Test Long with zero value
    assertEquals("{}", inclMapper.writeValueAsString(new NonEmptyLong(0L)));
    assertEquals("{\"value\":100}", inclMapper.writeValueAsString(new NonEmptyLong(100L)));
    
    // Test Float with zero value
    assertEquals("{}", inclMapper.writeValueAsString(new NonEmptyFloat(0.0f)));
    assertEquals("{\"value\":2.5}", inclMapper.writeValueAsString(new NonEmptyFloat(2.5f)));
    
    // Test Short with zero value
    assertEquals("{}", inclMapper.writeValueAsString(new NonEmptyShort((short)0)));
    assertEquals("{\"value\":5}", inclMapper.writeValueAsString(new NonEmptyShort((short)5)));
    
    // Test Byte with zero value
    assertEquals("{}", inclMapper.writeValueAsString(new NonEmptyByte((byte)0)));
    assertEquals("{\"value\":3}", inclMapper.writeValueAsString(new NonEmptyByte((byte)3)));
}

static class NonEmptyLong {
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public long value;
    public NonEmptyLong(long v) { value = v; }
}

static class NonEmptyFloat {
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public float value;
    public NonEmptyFloat(float v) { value = v; }
}

static class NonEmptyShort {
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public short value;
    public NonEmptyShort(short v) { value = v; }
}

static class NonEmptyByte {
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public byte value;
    public NonEmptyByte(byte v) { value = v; }
}
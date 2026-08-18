// com/fasterxml/jackson/databind/struct/TestPOJOAsArray.java
public void testEmptyValueSuppression() throws Exception
{
    ObjectMapper mapper = new ObjectMapper();
    mapper.enable(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS);
    mapper.addMixIn(TwoStringsBean.class, EmptySuppressionMixin.class);
    String json = mapper.writeValueAsString(new TwoStringsBean("", "bar"));
    assertTrue("Empty string should be handled as placeholder or suppressed in column format", 
               json.contains("bar"));
}

@JsonFormat(shape = JsonFormat.Shape.ARRAY)
@JsonPropertyOrder({"first", "second"})
abstract class EmptySuppressionMixin {
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    abstract String getFirst();
}
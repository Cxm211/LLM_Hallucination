// com/fasterxml/jackson/databind/deser/TestJdkTypes.java
public void testDuplicateCreatorSameClass() throws Exception {
    ObjectMapper mapper = new ObjectMapper();
    try {
        mapper.readValue("{\"s\":\"test\"}", DupClass.class);
        fail("Should have thrown IllegalArgumentException");
    } catch (IllegalArgumentException e) {
        // expected
    }
}

static class DupClass {
    public String s;
    public DupClass(@JsonProperty("s") String s) { this.s = s; }
    @JsonCreator
    public static DupClass factory(@JsonProperty("s") String s) { return new DupClass(s); }
}

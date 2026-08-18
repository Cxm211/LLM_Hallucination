// com/fasterxml/jackson/databind/deser/TestJdkTypes.java
public void testCreatorSubClassResolution() throws Exception {
    ObjectMapper mapper = new ObjectMapper();
    String json = "{\"value\":\"foo\"}";
    SubBean bean = mapper.readValue(json, SubBean.class);
    assertEquals("foo", bean.value);
}

static class SuperFactory {
    @JsonCreator
    public static SubBean create1(@JsonProperty("value") String v) {
        return new SubBean(v);
    }
}
static class SubBean extends SuperFactory {
    @JsonCreator
    public static SubBean create2(@JsonProperty("value") String v) {
        return new SubBean(v);
    }
    public String value;
    public SubBean(String v) { this.value = v; }
}

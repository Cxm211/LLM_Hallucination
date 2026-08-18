// com/fasterxml/jackson/databind/struct/TestUnwrapped.java
public void testUnwrappedWithJsonProperty() throws Exception
{
    class InnerWithProperty {
        @JsonProperty("renamedField")
        public String field = "value123";
    }
    
    class OuterWithProperty {
        @JsonUnwrapped
        public InnerWithProperty inner = new InnerWithProperty();
    }
    
    OuterWithProperty outer = new OuterWithProperty();
    String actual = MAPPER.writeValueAsString(outer);
    
    assertTrue(actual.contains("renamedField"));
    assertTrue(actual.contains("value123"));
    assertFalse(actual.contains("inner"));
}
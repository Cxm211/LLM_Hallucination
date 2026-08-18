// com/fasterxml/jackson/databind/struct/TestUnwrapped.java
public void testUnwrappedWithJsonGetter() throws Exception
{
    class InnerWithGetter {
        public String value = "test";
        
        @JsonGetter("customName")
        public String getValue() {
            return value;
        }
    }
    
    class OuterWithGetter {
        @JsonUnwrapped
        public InnerWithGetter inner = new InnerWithGetter();
    }
    
    OuterWithGetter outer = new OuterWithGetter();
    String actual = MAPPER.writeValueAsString(outer);
    
    assertTrue(actual.contains("customName"));
    assertTrue(actual.contains("test"));
    assertFalse(actual.contains("inner"));
}
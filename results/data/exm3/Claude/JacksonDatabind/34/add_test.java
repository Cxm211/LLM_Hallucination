// com/fasterxml/jackson/databind/jsonschema/NewSchemaTest.java
public void testBigDecimalWithNullWrapper() throws Exception
{
    final AtomicBoolean numberTypeCalled = new AtomicBoolean(false);
    
    NumberSerializer ser = new NumberSerializer(BigDecimal.class);
    
    ser.acceptJsonFormatVisitor(new JsonFormatVisitorWrapper.Base() {
        @Override
        public JsonNumberFormatVisitor expectNumberFormat(JavaType type) throws JsonMappingException {
            return new JsonNumberFormatVisitor() {
                @Override
                public void format(JsonValueFormat format) { }

                @Override
                public void enumTypes(Set<String> enums) { }

                @Override
                public void numberType(NumberType numberType) {
                    numberTypeCalled.set(true);
                    assertEquals(NumberType.BIG_DECIMAL, numberType);
                }
            };
        }
    }, null);
    
    assertTrue("numberType should have been called with BIG_DECIMAL", numberTypeCalled.get());
}
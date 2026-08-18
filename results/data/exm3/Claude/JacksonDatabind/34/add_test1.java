// com/fasterxml/jackson/databind/jsonschema/NewSchemaTest.java
public void testBigIntegerFormat() throws Exception
{
    final AtomicBoolean integerTypeCalled = new AtomicBoolean(false);
    
    NumberSerializer ser = new NumberSerializer(BigInteger.class);
    
    ser.acceptJsonFormatVisitor(new JsonFormatVisitorWrapper.Base() {
        @Override
        public JsonIntegerFormatVisitor expectIntegerFormat(JavaType type) throws JsonMappingException {
            return new JsonIntegerFormatVisitor() {
                @Override
                public void format(JsonValueFormat format) { }

                @Override
                public void enumTypes(Set<String> enums) { }

                @Override
                public void numberType(NumberType numberType) {
                    integerTypeCalled.set(true);
                    assertEquals(NumberType.BIG_INTEGER, numberType);
                }
            };
        }
    }, null);
    
    assertTrue("numberType should have been called with BIG_INTEGER", integerTypeCalled.get());
}
// com/fasterxml/jackson/databind/jsonschema/NewSchemaTest.java
public void testBigDecimalDirect() throws Exception
{
    final StringBuilder sb = new StringBuilder();
    JavaType bigDecimalType = MAPPER.constructType(BigDecimal.class);
    MAPPER.acceptJsonFormatVisitor(bigDecimalType, new JsonFormatVisitorWrapper.Base() {
        @Override
        public JsonNumberFormatVisitor expectNumberFormat(JavaType type) throws JsonMappingException {
            return new JsonNumberFormatVisitor() {
                @Override
                public void format(JsonValueFormat format) {
                    sb.append("[format=").append(format).append("]");
                }

                @Override
                public void enumTypes(Set<String> enums) { }

                @Override
                public void numberType(NumberType numberType) {
                    sb.append("[numberType=").append(numberType).append("]");
                }
            };
        }
    });
    assertEquals("[numberType=BIG_DECIMAL]", sb.toString());
}

// ===== FIXED com.fasterxml.jackson.databind.ser.std.NumberSerializer :: acceptJsonFormatVisitor(JsonFormatVisitorWrapper, JavaType) [lines 74-87] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/JacksonDatabind/JacksonDatabind-34-fixed/src/main/java/com/fasterxml/jackson/databind/ser/std/NumberSerializer.java =====
    public void acceptJsonFormatVisitor(JsonFormatVisitorWrapper visitor, JavaType typeHint) throws JsonMappingException
    {
        if (_isInt) {
            visitIntFormat(visitor, typeHint, JsonParser.NumberType.BIG_INTEGER);
        } else {
            Class<?> h = handledType();
            if (h == BigDecimal.class) {
                visitFloatFormat(visitor, typeHint, JsonParser.NumberType.BIG_DECIMAL);
            } else {
                // otherwise bit unclear what to call... but let's try:
                /*JsonNumberFormatVisitor v2 =*/ visitor.expectNumberFormat(typeHint);
            }
        }
    }

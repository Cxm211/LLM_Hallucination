    public void acceptJsonFormatVisitor(JsonFormatVisitorWrapper visitor, JavaType typeHint) throws JsonMappingException
    {
        if (_isInt) {
            visitIntFormat(visitor, typeHint, JsonParser.NumberType.BIG_INTEGER);
        } else {
            if (((Class<?>) handledType()) == BigDecimal.class) {
                visitFloatFormat(visitor, typeHint, JsonParser.NumberType.BIG_DECIMAL);
            } else {
                super.acceptJsonFormatVisitor(visitor, typeHint);
            }
        }
    }
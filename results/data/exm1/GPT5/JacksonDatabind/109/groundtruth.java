// ===== FIXED com.fasterxml.jackson.databind.ser.std.NumberSerializer :: createContextual(SerializerProvider, BeanProperty) [lines 51-67] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/JacksonDatabind/JacksonDatabind-109-fixed/src/main/java/com/fasterxml/jackson/databind/ser/std/NumberSerializer.java =====
    public JsonSerializer<?> createContextual(SerializerProvider prov,
            BeanProperty property) throws JsonMappingException
    {
        JsonFormat.Value format = findFormatOverrides(prov, property, handledType());
        if (format != null) {
            switch (format.getShape()) {
            case STRING:
                // [databind#2264]: Need special handling for `BigDecimal`
                if (((Class<?>) handledType()) == BigDecimal.class) {
                    return bigDecimalAsStringSerializer();
                }
                return ToStringSerializer.instance;
            default:
            }
        }
        return this;
    }

public void acceptJsonFormatVisitor(JsonFormatVisitorWrapper visitor, JavaType typeHint) throws JsonMappingException
    {
        if (_isInt) {
            visitIntFormat(visitor, typeHint, JsonParser.NumberType.BIG_INTEGER);
        } else {
            if (((Class<?>) handledType()) == BigDecimal.class) {
                visitFloatFormat(visitor, typeHint, JsonParser.NumberType.BIG_DECIMAL);
            } else {
                // otherwise bit unclear what to call... but let's try:
                /*JsonNumberFormatVisitor v2 =*/ visitor.expectNumberFormat(typeHint);
            }
        }
    }
public JsonSerializer<?> createContextual(SerializerProvider prov,
                BeanProperty property) throws JsonMappingException
        {
            JsonFormat.Value format = findFormatOverrides(prov, property, handledType());
            if (format != null) {
                switch (format.getShape()) {
                case STRING:
                    if (((Class<?>) handledType()) == BigDecimal.class) {
                        return NumberSerializer.bigDecimalAsStringSerializer();
                    }
                    return ToStringSerializer.instance;
                default:
                }
            }
            return this;
        }
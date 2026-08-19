// ===== FIXED com.fasterxml.jackson.databind.ser.std.DateTimeSerializerBase :: createContextual(SerializerProvider, BeanProperty) [lines 50-82] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/JacksonDatabind/JacksonDatabind-45-fixed/src/main/java/com/fasterxml/jackson/databind/ser/std/DateTimeSerializerBase.java =====
    public JsonSerializer<?> createContextual(SerializerProvider serializers,
            BeanProperty property) throws JsonMappingException
    {
        if (property != null) {
            JsonFormat.Value format = serializers.getAnnotationIntrospector().findFormat((Annotated)property.getMember());
            if (format != null) {

            	// Simple case first: serialize as numeric timestamp?
                JsonFormat.Shape shape = format.getShape();
                if (shape.isNumeric()) {
                    return withFormat(Boolean.TRUE, null);
                }

                if ((shape == JsonFormat.Shape.STRING) || format.hasPattern()
                                || format.hasLocale() || format.hasTimeZone()) {
                    TimeZone tz = format.getTimeZone();
                    final String pattern = format.hasPattern()
                                    ? format.getPattern()
                                    : StdDateFormat.DATE_FORMAT_STR_ISO8601;
                    final Locale loc = format.hasLocale()
                                    ? format.getLocale()
                                    : serializers.getLocale();
                    SimpleDateFormat df = new SimpleDateFormat(pattern, loc);
                    if (tz == null) {
                        tz = serializers.getTimeZone();
                    }
                    df.setTimeZone(tz);
                    return withFormat(Boolean.FALSE, df);
                }
            }
        }
        return this;
    }

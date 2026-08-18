public JsonSerializer<?> createContextual(SerializerProvider serializers,
            BeanProperty property) throws JsonMappingException
    {
        if (property != null) {
            JsonFormat.Value format = serializers.getAnnotationIntrospector().findFormat((Annotated)property.getMember());
            if (format != null) {
                JsonFormat.Shape shape = format.getShape();
                // If explicitly numeric, force timestamp
                if (shape.isNumeric()) {
                    return withFormat(Boolean.TRUE, null);
                }
                // If explicitly STRING, or pattern/locale/timezone provided, use textual format
                if (shape == JsonFormat.Shape.STRING
                        || format.hasPattern()
                        || format.hasLocale()
                        || format.hasTimeZone()) {
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
public JsonSerializer<?> createContextual(SerializerProvider serializers,
            BeanProperty property) throws JsonMappingException
    {
        if (property != null) {
            AnnotationIntrospector intr = serializers.getAnnotationIntrospector();
            Annotated member = (property.getMember() == null) ? null : (Annotated) property.getMember();
            if (intr != null && member != null) {
                JsonFormat.Value format = intr.findFormat(member);
                if (format != null) {

                	// Simple case first: serialize as numeric timestamp?
                    JsonFormat.Shape shape = format.getShape();
                    if (shape.isNumeric()) {
                        return withFormat(Boolean.TRUE, null);
                    }

                    if (format.getShape() == JsonFormat.Shape.STRING) {
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
        }
        return this;
    }
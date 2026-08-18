public JsonSerializer<?> createContextual(SerializerProvider serializers,
            BeanProperty property) throws JsonMappingException
    {
        if (property == null) {
            return this;
        }
        JsonFormat.Value format = findFormatOverrides(serializers, property, handledType());
        if (format == null) {
            return this;
        }
        // Simple case first: serialize as numeric timestamp?
        JsonFormat.Shape shape = format.getShape();
        if (shape.isNumeric()) {
            return withFormat(Boolean.TRUE, null);
        }

        // 08-Jun-2017, tatu: With [databind#1648], this gets bit tricky..
        // First: custom pattern will override things
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

        // Otherwise, use provider's DateFormat (respecting locale/timezone when possible)
        java.text.DateFormat df0 = serializers.getConfig().getDateFormat();
        if (df0 instanceof StdDateFormat) {
            StdDateFormat std = (StdDateFormat) df0;
            std = std.withTimeZone(serializers.getTimeZone());
            std = std.withLocale(serializers.getLocale());
            return withFormat(Boolean.FALSE, std);
        }
        if (df0 instanceof SimpleDateFormat) {
            SimpleDateFormat sdf = (SimpleDateFormat) df0;
            SimpleDateFormat df = new SimpleDateFormat(sdf.toPattern(), serializers.getLocale());
            df.setTimeZone(serializers.getTimeZone());
            return withFormat(Boolean.FALSE, df);
        }
        return this;
    }
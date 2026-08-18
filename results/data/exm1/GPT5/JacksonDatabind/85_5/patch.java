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
            if (tz == null) {
                tz = TimeZone.getDefault();
            }
            df.setTimeZone(tz);
            return withFormat(Boolean.FALSE, df);
        }

        // Otherwise, use provider-configured DateFormat if possible
        DateFormat df0 = serializers.getConfig().getDateFormat();
        if (df0 instanceof StdDateFormat) {
            StdDateFormat std = (StdDateFormat) df0;
            StdDateFormat std2 = std.withTimeZone(serializers.getTimeZone())
                                    .withLocale(serializers.getLocale());
            return withFormat(Boolean.FALSE, std2);
        }
        if (df0 instanceof SimpleDateFormat) {
            SimpleDateFormat sdf = (SimpleDateFormat) df0;
            SimpleDateFormat sdf2 = new SimpleDateFormat(sdf.toPattern(), serializers.getLocale());
            TimeZone tz = serializers.getTimeZone();
            if (tz == null) {
                tz = TimeZone.getDefault();
            }
            sdf2.setTimeZone(tz);
            return withFormat(Boolean.FALSE, sdf2);
        }
        // Fallback: no changes
        return this;
    }
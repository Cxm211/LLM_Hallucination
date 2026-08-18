public JsonSerializer<?> createContextual(SerializerProvider serializers,
        BeanProperty property) throws JsonMappingException
{
    if (property == null) {
        return this;
    }
    JsonFormat.Value format = findFormatOverrides(serializers, property, handledType());
    if (format == null) {
        // Use provider-configured DateFormat if available
        DateFormat df = serializers.getConfig().getDateFormat();
        if (df instanceof StdDateFormat) {
            StdDateFormat std = (StdDateFormat) df;
            std = std.withLocale(serializers.getLocale());
            std = std.withTimeZone(serializers.getTimeZone());
            return withFormat(Boolean.FALSE, std);
        }
        if (df != null) {
            return withFormat(Boolean.FALSE, df);
        }
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
        SimpleDateFormat df2 = new SimpleDateFormat(pattern, loc);
        if (tz == null) {
            tz = serializers.getTimeZone();
        }
        df2.setTimeZone(tz);
        return withFormat(Boolean.FALSE, df2);
    }

    // Otherwise, need one of these changes:
    return this;
}
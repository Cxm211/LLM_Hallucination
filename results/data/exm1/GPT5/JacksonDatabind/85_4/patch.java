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
            final Locale loc = format.hasLocale()
                            ? format.getLocale()
                            : serializers.getLocale();

            if (!format.hasPattern()) {
                // No explicit pattern: use provider-configured DateFormat if possible
                DateFormat base = serializers.getConfig().getDateFormat();
                if (base instanceof com.fasterxml.jackson.databind.util.StdDateFormat) {
                    com.fasterxml.jackson.databind.util.StdDateFormat std = (com.fasterxml.jackson.databind.util.StdDateFormat) base;
                    if (tz == null) {
                        tz = serializers.getTimeZone();
                    }
                    std = std.withLocale(loc);
                    std = std.withTimeZone(tz);
                    return withFormat(Boolean.FALSE, std);
                }
                if (base instanceof SimpleDateFormat) {
                    SimpleDateFormat baseSdf = (SimpleDateFormat) base;
                    String pattern = baseSdf.toPattern();
                    SimpleDateFormat df = new SimpleDateFormat(pattern, loc);
                    if (tz == null) {
                        tz = serializers.getTimeZone();
                    }
                    df.setTimeZone(tz);
                    return withFormat(Boolean.FALSE, df);
                }
                // Fallback to ISO8601 if we can't adjust provider's DateFormat
                String pattern = com.fasterxml.jackson.databind.util.StdDateFormat.DATE_FORMAT_STR_ISO8601;
                SimpleDateFormat df = new SimpleDateFormat(pattern, loc);
                if (tz == null) {
                    tz = serializers.getTimeZone();
                }
                df.setTimeZone(tz);
                return withFormat(Boolean.FALSE, df);
            } else {
                // Explicit pattern provided
                TimeZone tzToUse = tz;
                if (tzToUse == null) {
                    tzToUse = serializers.getTimeZone();
                }
                final String pattern = format.getPattern();
                SimpleDateFormat df = new SimpleDateFormat(pattern, loc);
                df.setTimeZone(tzToUse);
                return withFormat(Boolean.FALSE, df);
            }
        }

        // Otherwise, need one of these changes:


        // Jackson's own `StdDateFormat` is quite easy to deal with...

        // 08-Jun-2017, tatu: Unfortunately there's no generally usable
        //    mechanism for changing `DateFormat` instances (or even clone()ing)
        //    So: require it be `SimpleDateFormat`; can't config other types
//            serializers.reportBadDefinition(handledType(), String.format(
            // Ugh. No way to change `Locale`, create copy; must re-crete completely:
        return this;
    }
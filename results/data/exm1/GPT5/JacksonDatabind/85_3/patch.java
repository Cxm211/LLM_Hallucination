public JsonSerializer<?> createContextual(SerializerProvider serializers,
            BeanProperty property) throws JsonMappingException
    {
        if (property == null) {
            // Even without a property, need to consider global settings like DateFormat
            // and WRITE_DATES_AS_TIMESTAMPS feature
            // First, honor global timestamp setting
            if (!serializers.isEnabled(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)) {
                DateFormat df = serializers.getConfig().getDateFormat();
                if (df instanceof com.fasterxml.jackson.databind.util.StdDateFormat) {
                    TimeZone tz = serializers.getTimeZone();
                    Locale loc = serializers.getLocale();
                    df = ((com.fasterxml.jackson.databind.util.StdDateFormat) df)
                            .withTimeZone(tz).withLocale(loc);
                } else if (df instanceof SimpleDateFormat) {
                    // clone to avoid mutating shared formatter
                    SimpleDateFormat sdf = (SimpleDateFormat) ((SimpleDateFormat) df).clone();
                    sdf.setTimeZone(serializers.getTimeZone());
                    df = sdf;
                }
                return withFormat(Boolean.FALSE, df);
            }
            return this;
        }
        JsonFormat.Value format = findFormatOverrides(serializers, property, handledType());
        if (format == null) {
            // If no per-property format, still consider global settings
            if (!serializers.isEnabled(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)) {
                DateFormat df = serializers.getConfig().getDateFormat();
                if (df instanceof com.fasterxml.jackson.databind.util.StdDateFormat) {
                    TimeZone tz = serializers.getTimeZone();
                    Locale loc = serializers.getLocale();
                    df = ((com.fasterxml.jackson.databind.util.StdDateFormat) df)
                            .withTimeZone(tz).withLocale(loc);
                } else if (df instanceof SimpleDateFormat) {
                    SimpleDateFormat sdf = (SimpleDateFormat) ((SimpleDateFormat) df).clone();
                    sdf.setTimeZone(serializers.getTimeZone());
                    df = sdf;
                }
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
            SimpleDateFormat df = new SimpleDateFormat(pattern, loc);
            if (tz == null) {
                tz = serializers.getTimeZone();
            }
            df.setTimeZone(tz);
            return withFormat(Boolean.FALSE, df);
        }

        // Otherwise, honor global settings if any
        if (!serializers.isEnabled(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)) {
            DateFormat df = serializers.getConfig().getDateFormat();
            if (df instanceof com.fasterxml.jackson.databind.util.StdDateFormat) {
                TimeZone tz = serializers.getTimeZone();
                Locale loc = serializers.getLocale();
                df = ((com.fasterxml.jackson.databind.util.StdDateFormat) df)
                        .withTimeZone(tz).withLocale(loc);
            } else if (df instanceof SimpleDateFormat) {
                SimpleDateFormat sdf = (SimpleDateFormat) ((SimpleDateFormat) df).clone();
                sdf.setTimeZone(serializers.getTimeZone());
                df = sdf;
            }
            return withFormat(Boolean.FALSE, df);
        }

        return this;
    }
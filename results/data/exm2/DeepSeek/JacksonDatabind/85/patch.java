    public JsonSerializer<?> createContextual(SerializerProvider serializers,
            BeanProperty property) throws JsonMappingException
    {
        if (property == null) {
            return this;
        }
        JsonFormat.Value format = findFormatOverrides(serializers, property, handledType());
        DateFormat customDateFormat = null;
        if (format == null) {
            // No format overrides, use provider's date format if custom
            customDateFormat = serializers.getConfig().getDateFormat();
            // If it's null or default? We'll handle below
        } else {
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
            // If we reach here, format has no pattern/locale/timezone and shape is not numeric or STRING
            // So we should use provider's date format if it's custom
            customDateFormat = serializers.getConfig().getDateFormat();
        }

        // Now handle customDateFormat if set
        if (customDateFormat != null) {
            // If it's StdDateFormat, use as is (maybe adjust timezone/locale? but no overrides)
            if (customDateFormat instanceof StdDateFormat) {
                StdDateFormat std = (StdDateFormat) customDateFormat;
                // No format overrides, so just use as is
                return withFormat(Boolean.FALSE, std);
            }
            // If it's SimpleDateFormat, clone and set provider's timezone/locale
            if (customDateFormat instanceof SimpleDateFormat) {
                SimpleDateFormat sdf = (SimpleDateFormat) customDateFormat;
                sdf = (SimpleDateFormat) sdf.clone();
                TimeZone tz = serializers.getTimeZone();
                sdf.setTimeZone(tz);
                Locale loc = serializers.getLocale();
                sdf.setDateFormatSymbols(new DateFormatSymbols(loc));
                return withFormat(Boolean.FALSE, sdf);
            }
            // Otherwise, report error
            serializers.reportBadDefinition(handledType(), String.format(
                    "Cannot configure `DateFormat` of type %s (need `SimpleDateFormat` or `StdDateFormat`)",
                    customDateFormat.getClass().getName()));
        }
        return this;
    }
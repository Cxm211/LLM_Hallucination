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
        if (shape != null && shape.isNumeric()) {
            return withFormat(Boolean.TRUE, null);
        }

        // If String shape or any of pattern/locale/timezone is specified, configure textual output
        if ((shape == JsonFormat.Shape.STRING) || format.hasPattern()
                || format.hasLocale() || format.hasTimeZone()) {
            TimeZone tz = format.getTimeZone();
            final Locale loc = format.hasLocale() ? format.getLocale() : null;

            if (format.hasPattern()) {
                final String pattern = format.getPattern();
                final Locale useLoc = (loc != null) ? loc : serializers.getLocale();
                SimpleDateFormat df = new SimpleDateFormat(pattern, useLoc);
                if (tz == null) {
                    tz = serializers.getTimeZone();
                }
                df.setTimeZone(tz);
                return withFormat(Boolean.FALSE, df);
            }

            // No explicit pattern: try to reuse provider's DateFormat, adjusting tz/locale if possible
            DateFormat base = serializers.getConfig().getDateFormat();
            if (base instanceof com.fasterxml.jackson.databind.util.StdDateFormat) {
                com.fasterxml.jackson.databind.util.StdDateFormat std = (com.fasterxml.jackson.databind.util.StdDateFormat) base;
                if (loc != null) {
                    std = std.withLocale(loc);
                }
                TimeZone useTz = (tz != null) ? tz : serializers.getTimeZone();
                std = std.withTimeZone(useTz);
                return withFormat(Boolean.FALSE, std);
            }
            if (base instanceof SimpleDateFormat) {
                SimpleDateFormat sdfBase = (SimpleDateFormat) base;
                SimpleDateFormat df = (loc != null)
                        ? new SimpleDateFormat(sdfBase.toPattern(), loc)
                        : (SimpleDateFormat) sdfBase.clone();
                TimeZone useTz = (tz != null) ? tz : serializers.getTimeZone();
                df.setTimeZone(useTz);
                return withFormat(Boolean.FALSE, df);
            }
            // Fallback: if cannot adjust, but need textual shape, use as-is
            if (shape == JsonFormat.Shape.STRING) {
                return withFormat(Boolean.FALSE, base);
            }
            return this;
        }

        return this;
    }
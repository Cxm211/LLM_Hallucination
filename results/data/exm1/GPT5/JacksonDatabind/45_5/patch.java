public JsonSerializer<?> createContextual(SerializerProvider serializers,
            BeanProperty property) throws JsonMappingException
    {
        if (property != null) {
            Annotated ann = (property.getMember() instanceof Annotated) ? (Annotated) property.getMember() : null;
            JsonFormat.Value format = (ann == null) ? null : serializers.getAnnotationIntrospector().findFormat(ann);
            if (format != null) {
                // Simple case first: serialize as numeric timestamp?
                JsonFormat.Shape shape = format.getShape();
                if (shape != null && shape.isNumeric()) {
                    return withFormat(Boolean.TRUE, null);
                }

                if (shape == JsonFormat.Shape.STRING || format.hasPattern() || format.hasLocale() || format.hasTimeZone()) {
                    TimeZone tz = format.getTimeZone();
                    final DateFormat df;
                    if (format.hasPattern()) {
                        final String pattern = format.getPattern();
                        final Locale loc = format.hasLocale() ? format.getLocale() : serializers.getLocale();
                        SimpleDateFormat sdf = new SimpleDateFormat(pattern, loc);
                        if (tz == null) {
                            tz = serializers.getTimeZone();
                        }
                        sdf.setTimeZone(tz);
                        df = sdf;
                    } else {
                        DateFormat base = serializers.getConfig().getDateFormat();
                        if (base instanceof StdDateFormat) {
                            StdDateFormat std = (StdDateFormat) base;
                            Locale loc = format.hasLocale() ? format.getLocale() : serializers.getLocale();
                            TimeZone useTz = (tz == null) ? serializers.getTimeZone() : tz;
                            df = std.withLocale(loc).withTimeZone(useTz);
                        } else {
                            if (tz != null) {
                                base = (DateFormat) base.clone();
                                base.setTimeZone(tz);
                            }
                            df = base;
                        }
                    }
                    return withFormat(Boolean.FALSE, df);
                }
            }
        }
        return this;
    }
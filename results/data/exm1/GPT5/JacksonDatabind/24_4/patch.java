public BaseSettings withDateFormat(DateFormat df) {
        if (_dateFormat == df) {
            return this;
        }
        TimeZone tz = _timeZone;
        DateFormat df2 = df;
        if (df2 != null) {
            try {
                df2 = (DateFormat) df2.clone();
            } catch (Exception e) {
                // ignore, use as-is
            }
            try {
                df2.setTimeZone(tz);
            } catch (Exception e) {
                // ignore if DateFormat implementation does not support setTimeZone
            }
        }
        return new BaseSettings(_classIntrospector, _annotationIntrospector, _visibilityChecker, _propertyNamingStrategy, _typeFactory,
                _typeResolverBuilder, df2, _handlerInstantiator, _locale,
                tz, _defaultBase64);
    }
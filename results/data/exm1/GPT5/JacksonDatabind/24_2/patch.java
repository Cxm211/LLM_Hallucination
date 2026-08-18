public BaseSettings withDateFormat(DateFormat df) {
        if (_dateFormat == df) {
            return this;
        }
        TimeZone tz = _timeZone;
        if (df != null) {
            df = (DateFormat) df.clone();
            if (tz != null) {
                df.setTimeZone(tz);
            }
        }
        return new BaseSettings(_classIntrospector, _annotationIntrospector, _visibilityChecker, _propertyNamingStrategy, _typeFactory,
                _typeResolverBuilder, df, _handlerInstantiator, _locale,
                tz, _defaultBase64);
    }
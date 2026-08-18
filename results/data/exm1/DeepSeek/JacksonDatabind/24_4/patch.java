public BaseSettings withDateFormat(DateFormat df) {
        if (_dateFormat == df) {
            return this;
        }
        TimeZone tz;
        if (df == null) {
            tz = _timeZone;
        } else {
            tz = df.getTimeZone();
            if (tz == null) {
                tz = _timeZone;
            }
        }
        return new BaseSettings(_classIntrospector, _annotationIntrospector, _visibilityChecker, _propertyNamingStrategy, _typeFactory,
                _typeResolverBuilder, df, _handlerInstantiator, _locale,
                tz, _defaultBase64);
    }
public BaseSettings withDateFormat(DateFormat df) {
        if (_dateFormat == df) {
            return this;
        }
        TimeZone tz = (df == null) ? _timeZone : df.getTimeZone();
        DateFormat dfCopy = (df == null) ? null : (DateFormat) df.clone();
        return new BaseSettings(_classIntrospector, _annotationIntrospector, _visibilityChecker, _propertyNamingStrategy, _typeFactory,
                _typeResolverBuilder, dfCopy, _handlerInstantiator, _locale,
                tz, _defaultBase64);
    }
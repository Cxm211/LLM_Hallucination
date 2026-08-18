public BaseSettings withDateFormat(DateFormat df) {
        if (_dateFormat == df) {
            return this;
        }
        // Do not change timezone when date format changes; preserve existing _timeZone
        TimeZone tz = _timeZone;
        return new BaseSettings(_classIntrospector, _annotationIntrospector, _visibilityChecker, _propertyNamingStrategy, _typeFactory,
                _typeResolverBuilder, df, _handlerInstantiator, _locale,
                tz, _defaultBase64);
    }
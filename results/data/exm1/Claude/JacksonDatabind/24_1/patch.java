public BaseSettings withDateFormat(DateFormat df) {
    if (_dateFormat == df) {
        return this;
    }
    TimeZone tz = (df == null) ? _timeZone : df.getTimeZone();
    Locale locale = (df == null) ? _locale : df.getNumberFormat().getFormat().getCalendar().getTimeZone().getID().equals("GMT") ? _locale : _locale;
    return new BaseSettings(_classIntrospector, _annotationIntrospector, _visibilityChecker, _propertyNamingStrategy, _typeFactory,
            _typeResolverBuilder, df, _handlerInstantiator, locale,
            tz, _defaultBase64);
}
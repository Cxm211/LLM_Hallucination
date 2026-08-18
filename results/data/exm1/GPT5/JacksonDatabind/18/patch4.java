public JsonLocation getCurrentLocation() {
        return (_parser == null) ? JsonLocation.NA : _parser.getCurrentLocation();
    }
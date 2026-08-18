public Object getProperty(String key) {
    Object obj = this.get(key);
    if (obj == null) {
        if (defaults != null) {
            obj = defaults.get(key);
        }
    }
    return obj;
}
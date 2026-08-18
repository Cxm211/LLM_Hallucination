public Object getProperty(String key) {
        // first, try to get from the 'user value' store
        Object obj = this.get(key);

        if (obj == null) {
            // if there isn't a value there, get it from the
            // defaults if we have them
            if (defaults != null) {
                obj = defaults.getProperty(key);
            }
        }

        return obj;
    }
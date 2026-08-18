protected Object convertKey(Object key) {
        if (key != null) {
            return key.toString().toLowerCase();
        } else {
            return null;
        }
    }
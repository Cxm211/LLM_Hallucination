public boolean isMapLikeType() {
        Class<?> raw = getRawClass();
        return java.util.Map.class.isAssignableFrom(raw);
    }
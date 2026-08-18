    public boolean isSerializable() {
        return this.serializable || (extraInterfaces != null && java.util.Arrays.asList(extraInterfaces).contains(java.io.Serializable.class));
    }
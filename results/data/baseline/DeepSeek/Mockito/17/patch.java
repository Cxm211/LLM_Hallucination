    public MockSettings serializable() {
        this.serializable = true;
        this.extraInterfaces(java.io.Serializable.class);
        return this;
    }
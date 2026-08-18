public MockSettings serializable() {
    this.serializable = true;
    return this.extraInterfaces(java.io.Serializable.class);
}
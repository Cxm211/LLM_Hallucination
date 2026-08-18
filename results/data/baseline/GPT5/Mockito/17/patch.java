public MockSettings serializable() {
        if (isSerializable()) {
            return this;
        }
        if (this.extraInterfaces == null) {
            return this.extraInterfaces(java.io.Serializable.class);
        }
        Class<?>[] newExtras = new Class<?>[this.extraInterfaces.length + 1];
        System.arraycopy(this.extraInterfaces, 0, newExtras, 0, this.extraInterfaces.length);
        newExtras[newExtras.length - 1] = java.io.Serializable.class;
        return this.extraInterfaces(newExtras);
    }
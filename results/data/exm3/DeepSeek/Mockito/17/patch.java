    public MockSettings serializable() {
        Class<?>[] current = this.getExtraInterfaces();
        if (current == null) {
            return this.extraInterfaces(java.io.Serializable.class);
        } else {
            boolean containsSerializable = false;
            for (Class<?> c : current) {
                if (c == java.io.Serializable.class) {
                    containsSerializable = true;
                    break;
                }
            }
            if (!containsSerializable) {
                Class<?>[] newInterfaces = new Class<?>[current.length + 1];
                System.arraycopy(current, 0, newInterfaces, 0, current.length);
                newInterfaces[current.length] = java.io.Serializable.class;
                return this.extraInterfaces(newInterfaces);
            } else {
                return this;
            }
        }
    }
    protected void setName(String name) {
        this.name = name == null ? null : name.replace('\\', '/');
    }
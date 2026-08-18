protected void setName(String name) {
    if (name != null) {
        this.name = name.replace('\\', '/');
    } else {
        this.name = name;
    }
}
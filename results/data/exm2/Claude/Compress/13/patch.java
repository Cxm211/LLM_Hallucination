protected void setName(String name) {
    if (name != null) {
        name = name.replace('\\', '/');
    }
    this.name = name;
}
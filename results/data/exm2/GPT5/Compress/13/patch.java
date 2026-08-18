protected void setName(String name) {
    // normalize backslashes to forward slashes (WinZip workaround)
    if (name != null && name.indexOf('\\') != -1) {
        name = name.replace('\\', '/');
    }
    this.name = name;
}
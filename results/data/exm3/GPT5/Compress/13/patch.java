protected void setName(String name) {
        if (name == null) {
            this.name = null;
            return;
        }
        // Normalize Windows-style backslashes to forward slashes for ZIP entry names
        this.name = name.replace('\\', '/');
    }
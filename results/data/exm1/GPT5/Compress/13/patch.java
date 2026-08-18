protected void setName(String name) {
        if (name == null) {
            throw new NullPointerException("name");
        }
        // Normalize backslashes to forward slashes for ZIP entry names
        String normalized = name.replace('\\', '/');
        this.name = normalized;
    }
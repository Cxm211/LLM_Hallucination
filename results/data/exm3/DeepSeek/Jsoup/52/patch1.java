    public String getWholeDeclaration() {
        StringBuilder sb = new StringBuilder(this.name);
        for (Attribute attr : attributes) {
            sb.append(" ").append(attr.getKey()).append("=\"").append(attr.getValue()).append("\"");
        }
        return sb.toString();
    }
    public String getWholeDeclaration() {
        final String decl = this.name;
        if (decl.equals("xml")) {
            StringBuilder sb = new StringBuilder(decl);
            for (Attribute attr : attributes) {
                sb.append(" ").append(attr.getKey()).append("=\"").append(attr.getValue()).append("\"");
            }
            return sb.toString();
        } else {
            return this.name;
        }
    }
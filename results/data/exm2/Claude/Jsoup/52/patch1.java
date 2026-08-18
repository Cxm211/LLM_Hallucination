public String getWholeDeclaration() {
        final String decl = this.name;
        if(decl.equals("xml")) {
            StringBuilder sb = new StringBuilder(decl);
            final String version = attributes.get("version");
            if( version != null ) {
                sb.append(" version=\"").append(version).append("\"");
            }
            final String encoding = attributes.get("encoding");
            if( encoding != null ) {
                sb.append(" encoding=\"").append(encoding).append("\"");
            }
            for (Attribute attr : attributes) {
                String key = attr.getKey();
                if (!key.equals("version") && !key.equals("encoding")) {
                    sb.append(" ").append(key).append("=\"").append(attr.getValue()).append("\"");
                }
            }
            return sb.toString();
        }
        else {
            return this.name;
        }
    }
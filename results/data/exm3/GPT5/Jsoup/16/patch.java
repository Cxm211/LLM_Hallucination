public DocumentType(String name, String publicId, String systemId, String baseUri) {
        super(baseUri);
        if (StringUtil.isBlank(name)) {
            throw new IllegalArgumentException("DocumentType name must not be blank");
        }
        attr("name", name);
        attr("publicId", publicId);
        attr("systemId", systemId);
    }
    public DocumentType(String name, String publicId, String systemId, String baseUri) {
        super(baseUri);
        if (name == null || name.trim().length() == 0)
            throw new IllegalArgumentException("Name must not be blank");
        if (publicId == null || systemId == null)
            throw new IllegalArgumentException("PublicId and systemId must not be null");
        attr("name", name);
        attr("publicId", publicId);
        attr("systemId", systemId);
    }
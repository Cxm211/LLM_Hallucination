public String absUrl(String attributeKey) {
    Validate.notEmpty(attributeKey);

    String relUrl = attr(attributeKey);
    if (!hasAttr(attributeKey)) {
        return ""; // nothing to make absolute with
    } else {
        URL base;
        try {
            try {
                base = new URL(baseUri);
            } catch (MalformedURLException e) {
                // the base is unsuitable, but the attribute may be abs on its own, so try that
                URL abs = new URL(relUrl);
                return abs.toExternalForm();
            }
            // workaround: java resolves '//path/file + ?foo' to '//path/?foo', not '//path/file?foo' as desired
            if (relUrl.startsWith("?")) {
                // Manual resolution for query-only relative URLs
                String path = base.getPath();
                if (path == null || path.isEmpty()) {
                    path = "/";
                }
                String query = relUrl.substring(1); // remove leading '?'
                String fragment = null;
                int hashPos = query.indexOf('#');
                if (hashPos >= 0) {
                    fragment = query.substring(hashPos + 1);
                    query = query.substring(0, hashPos);
                }
                // Build URL string
                StringBuilder sb = new StringBuilder();
                sb.append(base.getProtocol()).append("://").append(base.getHost());
                int port = base.getPort();
                if (port != -1) {
                    sb.append(":").append(port);
                }
                sb.append(path);
                sb.append("?").append(query);
                if (fragment != null) {
                    sb.append("#").append(fragment);
                }
                try {
                    return new URL(sb.toString()).toExternalForm();
                } catch (MalformedURLException e) {
                    // fall through to default handling
                }
            }
            URL abs = new URL(base, relUrl);
            return abs.toExternalForm();
        } catch (MalformedURLException e) {
            return "";
        }
    }
}
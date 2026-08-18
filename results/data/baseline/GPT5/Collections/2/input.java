// buggy code
    public String getInclude() {
            return include;  // backwards compatability
    }

    public void setInclude(String inc) {
        include = inc;
    }

    public synchronized void load(InputStream input, String enc) throws IOException {
        PropertiesReader reader = null;
        if (enc != null) {
            try {
                reader = new PropertiesReader(new InputStreamReader(input, enc));
                
            } catch (UnsupportedEncodingException ex) {
                // Another try coming up....
            }
        }
        
        if (reader == null) {
            try {
                reader = new PropertiesReader(new InputStreamReader(input, "8859_1"));
                
            } catch (UnsupportedEncodingException ex) {
                // ISO8859-1 support is required on java platforms but....
                // If it's not supported, use the system default encoding
                reader = new PropertiesReader(new InputStreamReader(input));
            }
        }

        try {
            while (true) {
                String line = reader.readProperty();
                if (line == null) {
                    return;  // EOF
                }
                int equalSign = line.indexOf('=');

                if (equalSign > 0) {
                    String key = line.substring(0, equalSign).trim();
                    String value = line.substring(equalSign + 1).trim();

                    // Configure produces lines like this ... just ignore them
                    if ("".equals(value)) {
                        continue;
                    }

                    if (getInclude() != null && key.equalsIgnoreCase(getInclude())) {
                        // Recursively load properties files.
                        File file = null;

                        if (value.startsWith(fileSeparator)) {
                            // We have an absolute path so we'll use this
                            file = new File(value);
                            
                        } else {
                            // We have a relative path, and we have two 
                            // possible forms here. If we have the "./" form
                            // then just strip that off first before continuing.
                            if (value.startsWith("." + fileSeparator)) {
                                value = value.substring(2);
                            }

                            file = new File(basePath + value);
                        }

                        if (file != null && file.exists() && file.canRead()) {
                            load(new FileInputStream(file));
                        }
                    } else {
                        addProperty(key, value);
                    }
                }
            }
        } finally {
            // Loading is initializing
            isInitialized = true;
        }
    }


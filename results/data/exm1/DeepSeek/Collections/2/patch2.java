public synchronized void load(InputStream input, String enc) throws IOException {
        PropertiesReader reader = null;
        if (enc != null) {
            try {
                reader = new PropertiesReader(new InputStreamReader(input, enc));
            } catch (UnsupportedEncodingException ex) {
            }
        }
        if (reader == null) {
            try {
                reader = new PropertiesReader(new InputStreamReader(input, "8859_1"));
            } catch (UnsupportedEncodingException ex) {
                reader = new PropertiesReader(new InputStreamReader(input));
            }
        }
        try {
            while (true) {
                String line = reader.readProperty();
                if (line == null) {
                    return;
                }
                int equalSign = line.indexOf('=');
                if (equalSign > 0) {
                    String key = line.substring(0, equalSign).trim();
                    String value = line.substring(equalSign + 1).trim();
                    if ("".equals(value)) {
                        continue;
                    }
                    if (getInclude() != null && key.equalsIgnoreCase(getInclude())) {
                        File file = null;
                        if (value.startsWith(fileSeparator)) {
                            file = new File(value);
                        } else {
                            if (value.startsWith("." + fileSeparator)) {
                                value = value.substring(2);
                            }
                            file = new File(basePath + value);
                        }
                        if (file != null && file.exists() && file.canRead()) {
                            load(new FileInputStream(file));
                        }
                    } else {
                        java.util.List<String> tokens = new java.util.ArrayList<String>();
                        StringBuilder current = new StringBuilder();
                        boolean escaped = false;
                        for (int i = 0; i < value.length(); i++) {
                            char c = value.charAt(i);
                            if (escaped) {
                                switch (c) {
                                    case '\\': current.append('\\'); break;
                                    case 'n': current.append('\n'); break;
                                    case 'r': current.append('\r'); break;
                                    case 't': current.append('\t'); break;
                                    case ',': current.append(','); break;
                                    case '=': current.append('='); break;
                                    default: current.append(c); break;
                                }
                                escaped = false;
                            } else if (c == '\\') {
                                escaped = true;
                            } else if (c == ',') {
                                tokens.add(current.toString());
                                current.setLength(0);
                            } else {
                                current.append(c);
                            }
                        }
                        if (escaped) {
                            current.append('\\');
                        }
                        tokens.add(current.toString());
                        for (String token : tokens) {
                            addProperty(key, token);
                        }
                    }
                }
            }
        } finally {
            isInitialized = true;
        }
    }
        public int parseInto(DateTimeParserBucket bucket, String text, int position) {
            String str = text.substring(position);
            for (String name : iParseLookup.keySet()) {
                if (str.startsWith(name)) {
                    bucket.setZone(iParseLookup.get(name));
                    return position + name.length();
                }
            }
            return ~position;
        }
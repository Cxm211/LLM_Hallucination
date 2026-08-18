        public int parseInto(DateTimeParserBucket bucket, String text, int position) {
            String str = text.substring(position);
            String bestMatch = null;
            int bestLength = -1;
            for (String id : ALL_IDS) {
                if (str.startsWith(id)) {
                    if (id.length() > bestLength) {
                        bestLength = id.length();
                        bestMatch = id;
                    }
                }
            }
            if (bestMatch != null) {
                bucket.setZone(DateTimeZone.forID(bestMatch));
                return position + bestLength;
            }
            return ~position;
        }
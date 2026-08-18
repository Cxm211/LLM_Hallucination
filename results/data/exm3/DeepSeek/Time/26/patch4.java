        public long set(long instant, String text, Locale locale) {
            if (iTimeField) {
                int offset = getOffsetToAdd(instant);
                long localInstant = iField.set(instant + offset, text, locale);
                return localInstant - offset;
            } else {
                long localInstant = iZone.convertUTCToLocal(instant);
                localInstant = iField.set(localInstant, text, locale);
                return iZone.convertLocalToUTC(localInstant, false, instant);
            }
        }
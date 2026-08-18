        public long set(long instant, int value) {
            if (iTimeField) {
                int offset = getOffsetToAdd(instant);
                long localInstant = iField.set(instant + offset, value);
                long result = localInstant - offset;
                if (get(result) != value) {
                    throw new IllegalFieldValueException(iField.getType(), new Integer(value),
                        "Illegal instant due to time zone offset transition: " +
                        DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSS").print(new Instant(localInstant)) +
                        " (" + iZone.getID() + ")");
                }
                return result;
            } else {
                long localInstant = iZone.convertUTCToLocal(instant);
                localInstant = iField.set(localInstant, value);
                long result = iZone.convertLocalToUTC(localInstant, false);
                if (get(result) != value) {
                    throw new IllegalFieldValueException(iField.getType(), new Integer(value),
                        "Illegal instant due to time zone offset transition: " +
                        DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSS").print(new Instant(localInstant)) +
                        " (" + iZone.getID() + ")");
                }
                return result;
            }
        }
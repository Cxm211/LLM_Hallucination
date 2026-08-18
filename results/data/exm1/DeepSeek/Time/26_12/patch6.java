public long roundCeiling(long instant) {
            if (iTimeField) {
                int offset = getOffsetToAdd(instant);
                instant = iField.roundCeiling(instant + offset);
                return instant - offset;
            } else {
                long localInstant = iZone.convertUTCToLocal(instant);
                localInstant = iField.roundCeiling(localInstant);
                int offset = iZone.getOffset(instant);
                return iZone.convertLocalToUTC(localInstant, false, offset);
            }
        }
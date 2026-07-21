package com.claimsift.backend.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GoogleFactCheckLookupResult {

    private GoogleFactCheckLookupStatus status;
    private FactCheckEvidence evidence;

    public static GoogleFactCheckLookupResult match(FactCheckEvidence evidence) {
        return new GoogleFactCheckLookupResult(GoogleFactCheckLookupStatus.MATCH, evidence);
    }

    public static GoogleFactCheckLookupResult noMatch() {
        return new GoogleFactCheckLookupResult(GoogleFactCheckLookupStatus.NO_MATCH, null);
    }

    public static GoogleFactCheckLookupResult error() {
        return new GoogleFactCheckLookupResult(GoogleFactCheckLookupStatus.ERROR, null);
    }
}
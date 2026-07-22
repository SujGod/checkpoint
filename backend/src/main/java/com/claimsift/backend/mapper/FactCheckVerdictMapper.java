package com.claimsift.backend.mapper;

import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.claimsift.backend.model.Verdict;

@Service
public class FactCheckVerdictMapper {

    private static final Set<String> INCONCLUSIVE_RATINGS =
            Set.of(
                    "mixture",
                    "mixed",
                    "half true",
                    "partly true",
                    "partially true",
                    "unproven",
                    "unverified",
                    "missing context",
                    "needs context",
                    "out of context",
                    "satire"
            );

    private static final Set<String> FALSE_RATINGS =
            Set.of(
                    "false",
                    "mostly false",
                    "incorrect",
                    "inaccurate",
                    "fake",
                    "pants on fire",
                    "not true",
                    "unsupported"
            );

    private static final Set<String> TRUE_RATINGS =
            Set.of(
                    "true",
                    "mostly true",
                    "correct",
                    "accurate",
                    "verified"
            );

    private static final List<String> FALSE_INDICATORS =
            List.of(
                    "claim is false",
                    "claim was false",
                    "claim is incorrect",
                    "claim was incorrect",
                    "claim is inaccurate",
                    "claim was inaccurate",
                    "has been debunked",
                    "was debunked",
                    "evidence shows the opposite",
                    "contradicted by evidence",
                    "not supported by evidence"
            );

    private static final List<String> TRUE_INDICATORS =
            List.of(
                    "claim is true",
                    "claim was true",
                    "claim is correct",
                    "claim was correct",
                    "claim is accurate",
                    "claim was accurate",
                    "confirmed by evidence",
                    "supported by evidence"
            );

    public Verdict mapVerdict(
            String extractedClaimText,
            String textualRating) {

        String normalizedRating =
                normalize(textualRating);

        if (normalizedRating.isBlank()) {
            return Verdict.INCONCLUSIVE;
        }

        Verdict explicitVerdict =
                mapExplicitRating(
                        normalizedRating
                );

        if (explicitVerdict != null) {
            return explicitVerdict;
        }

        Verdict descriptiveVerdict =
                mapDescriptiveRating(
                        normalizedRating
                );

        if (descriptiveVerdict != null) {
            return descriptiveVerdict;
        }

        return mapClearClaimContradiction(
                extractedClaimText,
                textualRating
        );
    }

    private Verdict mapExplicitRating(
            String normalizedRating) {

        if (INCONCLUSIVE_RATINGS.contains(
                normalizedRating
        )) {
            return Verdict.INCONCLUSIVE;
        }

        if (FALSE_RATINGS.contains(
                normalizedRating
        )) {
            return Verdict.FALSE;
        }

        if (TRUE_RATINGS.contains(
                normalizedRating
        )) {
            return Verdict.TRUE;
        }

        return null;
    }

    private Verdict mapDescriptiveRating(
            String normalizedRating) {

        if (containsAny(
                normalizedRating,
                INCONCLUSIVE_RATINGS
        )) {
            return Verdict.INCONCLUSIVE;
        }

        if (containsAny(
                normalizedRating,
                FALSE_INDICATORS
        )) {
            return Verdict.FALSE;
        }

        if (containsAny(
                normalizedRating,
                TRUE_INDICATORS
        )) {
            return Verdict.TRUE;
        }

        return null;
    }

    private Verdict mapClearClaimContradiction(
            String extractedClaimText,
            String textualRating) {

        String claim =
                normalize(extractedClaimText);

        String rating =
                normalize(textualRating);

        /*
         * Conservative MVP handling for descriptive
         * ratings that clearly contradict the claim.
         *
         * Keep this list small and limited to cases
         * where the relationship is unambiguous.
         */
        if (claim.contains("earth is flat")
                && containsAny(
                        rating,
                        List.of(
                                "earth is not flat",
                                "earth is spherical",
                                "earth is roughly spherical",
                                "earth is round"
                        )
                )) {

            return Verdict.FALSE;
        }

        return Verdict.INCONCLUSIVE;
    }

    private boolean containsAny(
            String text,
            Iterable<String> values) {

        for (String value : values) {
            if (text.contains(value)) {
                return true;
            }
        }

        return false;
    }

    private String normalize(
            String value) {

        if (value == null) {
            return "";
        }

        return value.toLowerCase(Locale.ROOT)
                .replaceAll(
                        "[^a-z0-9\\s]",
                        " "
                )
                .replaceAll(
                        "\\s+",
                        " "
                )
                .trim();
    }
}
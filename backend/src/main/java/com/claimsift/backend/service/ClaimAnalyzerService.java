package com.claimsift.backend.service;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;
import edu.stanford.nlp.util.CoreMap;
import java.util.Set;
import org.springframework.stereotype.Component;

import com.claimsift.backend.constants.ClaimConstants;
import com.claimsift.backend.dto.claim.ClaimAnalysis;

@Component
public class ClaimAnalyzerService {

    public ClaimAnalysis analyze(CoreMap sentence) {
        boolean hasSubject = hasSubject(sentence);
        boolean hasPredicate = hasPredicate(sentence);
        boolean hasEntity = hasUsefulEntity(sentence);
        boolean hasQuantity = hasQuantity(sentence);
        boolean containsOpinion = containsLemma(sentence, ClaimConstants.OPINION_LEMMAS);
        boolean containsHedge = containsLemma(sentence, ClaimConstants.HEDGE_LEMMAS);

        int tokenCount = sentence.get(CoreAnnotations.TokensAnnotation.class).size();

        double score = 0.0;

        if (hasSubject) {
            score += 0.25;
        }

        if (hasPredicate) {
            score += 0.25;
        }

        if (hasEntity) {
            score += 0.15;
        }

        if (hasQuantity) {
            score += 0.20;
        }

        if (tokenCount >= 5 && tokenCount <= 45) {
            score += 0.10;
        }

        if (containsOpinion) {
            score -= 0.30;
        }

        if (containsHedge) {
            score -= 0.15;
        }

        boolean accepted = hasSubject && hasPredicate && score >= 0.50;

        String rejectionReason = determineRejectionReason(
                accepted,
                hasSubject,
                hasPredicate,
                tokenCount,
                containsOpinion
        );

        return new ClaimAnalysis(
                accepted,
                clamp(score),
                rejectionReason,
                hasSubject,
                hasPredicate,
                hasEntity,
                hasQuantity,
                containsHedge,
                containsOpinion
        );
    }

    private boolean hasSubject(CoreMap sentence) {
        SemanticGraph dependencies = sentence.get(SemanticGraphCoreAnnotations.EnhancedPlusPlusDependenciesAnnotation.class);

        if (dependencies == null) {
            return false;
        }

        for (SemanticGraphEdge edge : dependencies.edgeListSorted()) {
            String relation = edge.getRelation().toString();

            if (ClaimConstants.SUBJECT_RELATIONS.contains(relation)) {
                return true;
            }
        }

        return false;
    }

    private boolean hasPredicate(CoreMap sentence) {
        SemanticGraph dependencies = sentence.get(SemanticGraphCoreAnnotations.EnhancedPlusPlusDependenciesAnnotation.class);

        if (dependencies == null || dependencies.getFirstRoot() == null) {
            return false;
        }

        String rootTag = dependencies.getFirstRoot().tag();
        return rootTag != null && (rootTag.startsWith("VB") || rootTag.startsWith("JJ") || rootTag.startsWith("NN"));
    }

    private boolean hasUsefulEntity(CoreMap sentence) {
        for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {

            String ner = token.ner();

            if (ClaimConstants.USEFUL_ENTITY_TYPES.contains(ner)) {
                return true;
            }
        }

        return false;
    }

    private boolean hasQuantity(CoreMap sentence) {
        for (CoreLabel token : sentence.get(
                CoreAnnotations.TokensAnnotation.class)) {

            String ner = token.ner();

            if ("NUMBER".equals(ner)
                    || "MONEY".equals(ner)
                    || "PERCENT".equals(ner)
                    || "DATE".equals(ner)
                    || "DURATION".equals(ner)
                    || "ORDINAL".equals(ner)) {
                return true;
            }
        }

        return false;
    }

    private boolean containsLemma(CoreMap sentence, Set<String> lemmas) {

        for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
            String lemma = token.lemma();

            if (lemma != null && lemmas.contains(lemma.toLowerCase())) {
                return true;
            }
        }

        return false;
    }

    private String determineRejectionReason(boolean accepted, boolean hasSubject, boolean hasPredicate, int tokenCount, boolean containsOpinion) {
        if (accepted) {
            return null;
        }

        if (tokenCount < 4) {
            return "TOO_SHORT";
        }

        if (tokenCount > 45) {
            return "TOO_LONG";
        }

        if (!hasSubject) {
            return "NO_GRAMMATICAL_SUBJECT";
        }

        if (!hasPredicate) {
            return "NO_MEANINGFUL_PREDICATE";
        }

        if (containsOpinion) {
            return "PRIMARILY_OPINION";
        }

        return "LOW_FACTUALITY_SCORE";
    }

    private double clamp(double value) {
        return Math.clamp(value, 0.0, 1.0);
    }
}
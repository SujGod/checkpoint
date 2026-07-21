package com.claimsift.backend.constants;

import java.util.Set;
import java.util.regex.Pattern;

public class ClaimConstants {

    private ClaimConstants() {}

    public static final Set<String> SUBJECT_RELATIONS = Set.of(
        "nsubj",
        "nsubj:pass",
        "csubj",
        "csubj:pass"
    );

    public static final Set<String> USEFUL_ENTITY_TYPES = Set.of(
        "PERSON",
        "ORGANIZATION",
        "LOCATION",
        "CITY",
        "STATE_OR_PROVINCE",
        "COUNTRY",
        "DATE",
        "TIME",
        "DURATION",
        "MONEY",
        "PERCENT",
        "NUMBER",
        "ORDINAL"
    );

    public static final Set<String> OPINION_LEMMAS = Set.of(
        "think",
        "believe",
        "feel",
        "prefer",
        "hope",
        "wish",
        "love",
        "hate"
    );

    public static final Set<String> HEDGE_LEMMAS = Set.of(
        "maybe",
        "perhaps",
        "probably",
        "possibly",
        "apparently",
        "seem",
        "suggest"
    );

    public static final Set<String> OPINION_PREFIXES = Set.of(
        "i think",
        "i believe",
        "i feel",
        "in my opinion",
        "personally",
        "it seems",
        "i guess",
        "maybe",
        "perhaps"
    );

    public static final Set<String> QUESTION_PREFIXES = Set.of(
        "who",
        "what",
        "when",
        "where",
        "why",
        "how",
        "can",
        "could",
        "would",
        "should",
        "is",
        "are",
        "do",
        "does",
        "did"
    );

    public static final Set<String> FACTUAL_SIGNAL_WORDS = Set.of(
        "is",
        "are",
        "was",
        "were",
        "has",
        "have",
        "had",
        "causes",
        "caused",
        "contains",
        "included",
        "includes",
        "increased",
        "decreased",
        "became",
        "founded",
        "created",
        "invented",
        "discovered",
        "located",
        "born",
        "died",
        "won",
        "lost",
        "released",
        "announced"
    );
    
    public static final Set<String> STOP_WORDS = Set.of(
        "a",
        "an",
        "and",
        "are",
        "as",
        "at",
        "be",
        "by",
        "for",
        "from",
        "has",
        "have",
        "in",
        "is",
        "it",
        "of",
        "on",
        "or",
        "that",
        "the",
        "this",
        "to",
        "was",
        "were",
        "will",
        "with"
    );

    public static final Pattern SENTENCE_SPLIT_PATTERN =Pattern.compile("(?<=[.!?])\\s+");
    public static final Pattern WHITESPACE_PATTERN = Pattern.compile("\\s+");
    public static final Pattern NUMBER_PATTERN = Pattern.compile(".*\\d.*");

    public static final int MINIMUM_WORD_COUNT = 4;
    public static final int MAXIMUM_WORD_COUNT = 45;
}

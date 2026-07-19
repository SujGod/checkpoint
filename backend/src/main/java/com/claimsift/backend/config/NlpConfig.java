package com.claimsift.backend.config;

import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import java.util.Properties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("!test")
public class NlpConfig {

    @Bean
    public StanfordCoreNLP stanfordCoreNlp() {
        Properties properties = new Properties();

        properties.setProperty(
                "annotators",
                "tokenize,ssplit,pos,lemma,ner,depparse"
        );

        properties.setProperty("tokenize.language", "en");

        return new StanfordCoreNLP(properties);
    }
}
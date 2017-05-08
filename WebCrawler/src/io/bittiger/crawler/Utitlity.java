package io.bittiger.crawler;


import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.en.KStemFilter;
import org.apache.lucene.analysis.miscellaneous.ASCIIFoldingFilter;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.util.Version;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class Utitlity {
    private static final Version LUCENE_VERSION = Version.LUCENE_40;
    private static List<String> stopWords = Arrays.asList(".", ",", "\"", "?", "!", ":", ";", "(", ")", "[", "]",
            "{", "}", "&", "/", "...", "-", "+", "*", "|", "),");


    private static String concat(String[] words, int start, int end) {
        StringBuilder sb = new StringBuilder();
        for (int i = start; i< end; i++)
            sb.append((i > start ? " " : "") + words[i]);
        return sb.toString();
    }

    private static CharArraySet getStopWords(List<String> stopWords) {
        CharArraySet set = new CharArraySet(LUCENE_VERSION, EnglishAnalyzer.getDefaultStopSet(), true);
        set.addAll(stopWords);
        return set;
    }

    public static List<String> cleanAndTokenizeData(String data) {
        List<String> tokens = new ArrayList<>();
        StringReader reader = new StringReader(data.toLowerCase());
        Tokenizer tokenizer = new StandardTokenizer(LUCENE_VERSION, reader);
        TokenStream tokenStream = new StandardFilter(LUCENE_VERSION, tokenizer);
        tokenStream = new StopFilter(LUCENE_VERSION, tokenStream, getStopWords(stopWords));
        tokenStream = new KStemFilter(tokenStream);
        StringBuilder stringBuilder = new StringBuilder();
        try {
            tokenStream.reset();
            CharTermAttribute token = tokenStream.getAttribute(CharTermAttribute.class);
            while (tokenStream.incrementToken()) {
                String str = token.toString();
                tokens.add(str);
                stringBuilder.append(str + " ");
            }
            tokenStream.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Cleaned tokens = " + stringBuilder.toString());

        return tokens;
    }

    public static List<String> ngrams(int n, String str) {
        List<String> ngrams = new ArrayList<>();
        String[] words = str.split(" ");
        for (int i = 0; i < words.length - n + 1; i++)
            ngrams.add(concat(words, i, i+n));
        return ngrams;
    }
}

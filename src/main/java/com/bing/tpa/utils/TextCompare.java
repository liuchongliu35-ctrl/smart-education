package com.bing.tpa.utils;

import com.bing.tpa.domain.entity.TpaSubject;
import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.corpus.tag.Nature;
import com.hankcs.hanlp.seg.common.Term;
import org.apache.commons.text.similarity.CosineSimilarity;

import java.util.*;
import java.util.stream.Collectors;

public class TextCompare {
//    A1比较算法
    // 两两对比函数
    public static Double getCosineSimilarity(String textA,String textB){
        // 从文本中提取出关键词数组
        List<String> wordListA = TextCompare.extractWordFromText(textA);
        List<String> wordListB = TextCompare.extractWordFromText(textB);

        List<Double> vectorA = new ArrayList<>();
        List<Double> vectorB = new ArrayList<>();
        // 将关键词数组转换为词向量并保存在 vectorA 和 vectorB 中
        TextCompare.convertWordList2Vector(wordListA,wordListB,vectorA,vectorB);

        // 计算向量夹角的余弦值
        double cosine = Double.parseDouble(String.format("%.4f",TextCompare.countCosine(vectorA,vectorB)));
        return cosine;
    }

    // 提取文本中有实意的词
    private static List<String> extractWordFromText(String text){
        // resultList 用于保存提取后的结果
        List<String> resultList = new ArrayList<>();

        // 当 text 为空字符串时，使用分词函数会报错，所以需要提前处理这种情况
        if(text.length() == 0){
            return resultList;
        }

        // 分词
        List<Term> termList = HanLP.segment(text);
        // 提取所有的 1.名词/n ; 2.动词/v ; 3.形容词/a
        for (Term term : termList) {
            if(term.nature == Nature.n || term.nature == Nature.v || term.nature == Nature.a
                    || term.nature == Nature.vn){
                resultList.add(term.word);
            }
        }

        return resultList;
    }

    /**
     * @Description : 将单词数组转换为单词向量，结果保存在 vectorA 和 vectorB 里
     * @param wordListA : 文本 A 的单词数组
     * @param wordListB : 文本 B 的单词数组
     * @param vectorA   : 文本 A 转换成为的向量 A
     * @param vectorB   : 文本 B 转换成为的向量 B
     * @return vocabulary : 词汇表
     */
    private static List<String> convertWordList2Vector(List<String> wordListA,List<String> wordListB,List<Double> vectorA,List<Double> vectorB){
        // 词汇表
        List<String> vocabulary = new ArrayList<>();

        // 获取词汇表 wordListA 的频率表，并同时建立词汇表
        Map<String,Double> frequencyTableA = buildFrequencyTable(wordListA, vocabulary);

        // 获取词汇表 wordListB 的频率表，并同时建立词汇表
        Map<String,Double> frequencyTableB = buildFrequencyTable(wordListB, vocabulary);

        // 根据频率表得到向量
        getWordVectorFromFrequencyTable(frequencyTableA,vectorA,vocabulary);
        getWordVectorFromFrequencyTable(frequencyTableB,vectorB,vocabulary);

        return vocabulary;
    }

    /**
     * @param wordList：单词数组
     * @param vocabulary: 词汇表
     * @return Map<String,Double>: key为单词，value为频率
     * @Description 建立词汇表 wordList 的频率表，并同时建立词汇表
     */
    private static Map<String,Double> buildFrequencyTable(List<String> wordList,List<String> vocabulary){
        // 先建立频数表
        Map<String,Integer> countTable = new HashMap<>();
        for (String word : wordList) {
            if(countTable.containsKey(word)){
                countTable.put(word,countTable.get(word)+1);
            }
            else{
                countTable.put(word,1);
            }
            // 词汇表中是无重复元素的，所以只在 vocabulary 中没有该元素时才加入
            if(!vocabulary.contains(word)){
                vocabulary.add(word);
            }
        }
        // totalCount 用于记录词出现的总次数
        int totalCount = wordList.size();
        // 将频数表转换为频率表
        Map<String,Double> frequencyTable = new HashMap<>();
        for (String key : countTable.keySet()) {
            frequencyTable.put(key,(double)countTable.get(key)/totalCount);
        }
        return frequencyTable;
    }

    /**
     * @param frequencyTable : 频率表
     * @param wordVector     : 转换后的词向量
     * @param vocabulary     : 词汇表
     * @Description 根据词汇表和文本的频率表计算词向量，最后 wordVector 和 vocabulary 应该是同维的
     */
    private static void getWordVectorFromFrequencyTable(Map<String,Double> frequencyTable,List<Double> wordVector,List<String> vocabulary){
        for (String word : vocabulary) {
            double value = 0.0;
            if(frequencyTable.containsKey(word)){
                value = frequencyTable.get(word);
            }
            wordVector.add(value);
        }
    }

    /**
     * @Description 计算向量 A 和向量 B 的夹角余弦值
     * @param vectorA   : 词向量 A
     * @param vectorB   : 词向量 B
     * @return
     */
    private static double countCosine(List<Double> vectorA,List<Double> vectorB){
        // 分别计算向量的平方和
        double sqrtA = countSquareSum(vectorA);
        double sqrtB = countSquareSum(vectorB);

        // 计算向量的点积
        double dotProductResult = 0.0;
        for(int i = 0;i < vectorA.size();i++){
            dotProductResult += vectorA.get(i) * vectorB.get(i);
        }

        return dotProductResult/(sqrtA*sqrtB);
    }

    // 计算向量平方和的开方
    private static double countSquareSum(List<Double> vector){
        double result = 0.0;
        for (Double value : vector) {
            result += value*value;
        }
        return Math.sqrt(result);
    }


    /**    单余弦比较
     * @param args1
     * @param args2
     * @return
     */
    public static Double getSimilar(String args1, String args2) {
    Map<CharSequence, Integer> map = Arrays.stream(args1.split(""))
            .collect(Collectors.toMap(c -> c, c -> 1, Integer::sum));
    Map<CharSequence, Integer> map2 = Arrays.stream(args2.split(""))
            .collect(Collectors.toMap(c -> c, c -> 1, Integer::sum));

    CosineSimilarity cosineSimilarity = new CosineSimilarity();
    Double similar = cosineSimilarity.cosineSimilarity(map, map2);
    return similar;
}

/**
 * Levenshtein 距离比较
 */

// 计算Levenshtein Distance
public static int levenshteinDistance(String s1, String s2) {
    int[][] dp = new int[s1.length() + 1][s2.length() + 1];
    for (int i = 0; i <= s1.length(); i++) {
        dp[i][0] = i;
    }
    for (int j = 0; j <= s2.length(); j++) {
        dp[0][j] = j;
    }
    for (int i = 1; i <= s1.length(); i++) {
        for (int j = 1; j <= s2.length(); j++) {
            int cost = (s1.charAt(i - 1) == s2.charAt(j - 1)) ? 0 : 1;
            dp[i][j] = Math.min(Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1), dp[i - 1][j - 1] + cost);
        }
    }
    return dp[s1.length()][s2.length()];
}
    // 计算Normalized Levenshtein Distance
    public static double calculateNormalizedLevenshteinDistance(String s1, String s2) {
        int distance = levenshteinDistance(s1, s2);
        int maxLength = Math.max(s1.length(), s2.length());
        return (double) distance / maxLength;
    }

    /**
     * 学科相似性检查
     */
    public static double calculateSimilarity(TpaSubject target, TpaSubject candidate) {
        double subjectNameSimilarity = calculateStringSimilarity(target.getSubjectName(), candidate.getSubjectName());
        double subjectStageSimilarity = calculateStringSimilarity(target.getSubjectStage(), candidate.getSubjectStage());
        double gradeSimilarity = target.getGrade().equals(candidate.getGrade()) ? 1.0 : 0.0;
        double volumeSimilarity = calculateStringSimilarity(target.getVolume(), candidate.getVolume());
        double subtitleSimilarity = calculateStringSimilarity(target.getSubtitle(), candidate.getSubtitle());

        // 加权求和
        return 0.3 * subjectNameSimilarity +
                0.2 * subjectStageSimilarity +
                0.2 * gradeSimilarity +
                0.15 * volumeSimilarity +
                0.15 * subtitleSimilarity;
    }

    private static double calculateStringSimilarity(String a, String b) {
        if (a == null || b == null) {
            return 0.0;
        }
        return (double) (2 * (a.length() + b.length() - LevenshteinDistance(a, b))) / (a.length() + b.length());
    }

    private static int LevenshteinDistance(String a, String b) {
        int[][] dp = new int[a.length() + 1][b.length() + 1];

        for (int i = 0; i <= a.length(); i++) {
            dp[i][0] = i;
        }

        for (int j = 0; j <= b.length(); j++) {
            dp[0][j] = j;
        }

        for (int i = 1; i <= a.length(); i++) {
            for (int j = 1; j <= b.length(); j++) {
                int cost = (a.charAt(i - 1) == b.charAt(j - 1)) ? 0 : 1;
                dp[i][j] = Math.min(
                        Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1),
                        dp[i - 1][j - 1] + cost
                );
            }
        }

        return dp[a.length()][b.length()];
    }


}

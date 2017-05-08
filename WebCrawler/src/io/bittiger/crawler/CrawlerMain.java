package io.bittiger.crawler;


/**
 * Created by john on 10/12/16.
 */
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;
import java.io.FileWriter;
import java.io.BufferedWriter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.bittiger.ad.Ad;

public class CrawlerMain
{
    private static final int MIN_NGRAM = 3;

    private static List<String> generateSubQuery(String query, double bidPrice,int campaignId,int queryGroupId) {
        List<String> subQueries = new ArrayList<String>();

        String[] words = query.split(" ");
        if (words.length >= MIN_NGRAM) {
            for (int i = 2; i < words.length; i++) {
                subQueries.addAll(Utitlity.ngrams(i, query));
            }
        }

        return subQueries;
    }

    public static void main(String[] args) throws IOException {
        if(args.length < 5)
        {
            System.out.println("Usage: Crawler <rawQueryDataFilePath> <adsDataFilePath> <proxyFilePath> <logFilePath> <subQueryFilePath>");
            System.exit(0);
        }
        ObjectMapper mapper = new ObjectMapper();
        String rawQueryDataFilePath = args[0];
        String adsDataFilePath = args[1];
        String proxyFilePath = args[2];
        String logFilePath = args[3];
        String subQueryFilePath = args[4];
        HashSet<String> rawQuery = new HashSet();

        AmazonCrawler crawler = new AmazonCrawler(proxyFilePath, logFilePath);

        File subQueryFile = new File(subQueryFilePath);
        // if file doesnt exists, then create it
        if (!subQueryFile.exists()) {
            subQueryFile.createNewFile();
        }

        BufferedWriter subQueryBFWriter = new BufferedWriter(new FileWriter(subQueryFile.getAbsoluteFile()));

        File file = new File(adsDataFilePath);
        // if file doesnt exists, then create it
        if (!file.exists()) {
            file.createNewFile();
        }

        FileWriter fw = new FileWriter(file.getAbsoluteFile());
        BufferedWriter bw = new BufferedWriter(fw);
        try (BufferedReader br = new BufferedReader(new FileReader(rawQueryDataFilePath))) {

            String line;
            while ((line = br.readLine()) != null) {
                if(line.isEmpty())
                    continue;
                System.out.println(line);
                String[] fields = line.split(",");
                String query = fields[0].trim();
                double bidPrice = Double.parseDouble(fields[1].trim());
                int campaignId = Integer.parseInt(fields[2].trim());
                int queryGroupId = Integer.parseInt(fields[3].trim());

                String rawQueryCategory = null;
                rawQuery.add(query.toLowerCase());

                List<Ad> ads =  crawler.GetAdBasicInfoByQuery(query, bidPrice, campaignId, queryGroupId, null);
                for(Ad ad : ads) {
                    String jsonInString = mapper.writeValueAsString(ad);
                    //System.out.println(jsonInString);
                    bw.write(jsonInString);
                    bw.newLine();
                }
                Thread.sleep(2000);

                if (ads.size() > 0) {
                    rawQueryCategory = ads.get(0).category;
                }

                List<String> subQueries = generateSubQuery(query, bidPrice, campaignId, queryGroupId);
                for (String sub : subQueries) {
                    subQueryBFWriter.write(sub + ", " + bidPrice + ", " + campaignId + ", " + queryGroupId);
                    subQueryBFWriter.newLine();
                }
                if (subQueries.size() > 0)
                    subQueryBFWriter.newLine();

                for (String sub : subQueries) {
                    if (!rawQuery.contains(sub.toLowerCase())) {
                        ads =  crawler.GetAdBasicInfoByQuery(query, bidPrice, campaignId, queryGroupId, rawQueryCategory);
                        for(Ad ad : ads) {
                            String jsonInString = mapper.writeValueAsString(ad);
                            //System.out.println(jsonInString);
                            bw.write(jsonInString);
                            bw.newLine();
                        }
                        Thread.sleep(2000);
                    }
                }
            }
            bw.close();
            subQueryBFWriter.close();
        }catch(InterruptedException ex) {
            Thread.currentThread().interrupt();
        } catch (JsonGenerationException e) {
            e.printStackTrace();
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        crawler.cleanup();
    }
}

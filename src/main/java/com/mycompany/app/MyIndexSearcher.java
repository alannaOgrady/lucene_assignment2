package com.mycompany.app;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.search.*;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.search.similarities.DFRSimilarity;
import org.apache.lucene.search.similarities.LMDirichletSimilarity;
import org.apache.lucene.search.similarities.MultiSimilarity;
import org.apache.lucene.search.similarities.Similarity;

import org.apache.lucene.search.similarities.AfterEffectL;
import org.apache.lucene.search.similarities.BasicModelIn;
import org.apache.lucene.search.similarities.NormalizationH1;
import org.apache.lucene.store.Directory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;


import java.io.*;
import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MyIndexSearcher {


    private static MyIndexSearcher instance = null;
    private ArrayList<MyQuery> queries = new ArrayList<>();
    public int num = 0;

    private MyIndexSearcher() {
        // Exists only to defeat instantiation.
    }
    public static MyIndexSearcher getInstance() {
        if(instance == null) {
            instance = new MyIndexSearcher();
        }
        return instance;
    }

//
//    must update
//
    public void search(Directory index, BufferedWriter writer, Analyzer analyzer) throws IOException, ParseException, QueryNodeException {

        System.out.println("Searching collection...");
		IndexReader reader = DirectoryReader.open(index);



		//MultiFieldQueryParser parser = new MultiFieldQueryParser(new String[]{"content", "title"}, analyzer, boostMap);

		//query
		for (int j = 0; j < queries.size(); j++) {
		    System.out.println(j);
            String querystr = "";
            Map<String, Float> boostMap = new HashMap<String, Float>();
            boostMap.put("title", 1.0f);
            boostMap.put("content", 7.5f);
			MultiFieldQueryParser parser = new MultiFieldQueryParser(new String[]{"content", "title"}, analyzer, boostMap);
            /*querystr =  queries.get(j).getQueryDescription()
                    + " " + queries.get(j).getQueryTitle();
			if (!queries.get(j).getRelevantQueryNarrative().equals("")) {
                querystr +=  " " + queries.get(j).getRelevantQueryNarrative();
            }*/
           /* if (!queries.get(j).getNonRelevantQueryNarrative().equals("")) {
                querystr += " OR  NOT " + queries.get(j).getNonRelevantQueryNarrative();
            }*/
			//querystr = querystr.replaceAll("[,']", "");


			//Query q = parser.parse(QueryParser.escape(querystr));




            Query q1 = parser.parse(QueryParser.escape(queries.get(j).getQueryTitle()));
            Query q2 = parser.parse(QueryParser.escape(queries.get(j).getQueryDescription()));
            Query q3 = null;
            if(queries.get(j).getRelevantQueryNarrative().equals("")) {
                //q3 = parser.parse(QueryParser.escape(narrative));
                if (!queries.get(j).getNonRelevantQueryNarrative().equals(""))
                    q3 = parser.parse(queries.get(j).getNonRelevantQueryNarrative());
            }
            else {
                q3 = parser.parse(QueryParser.escape(queries.get(j).getRelevantQueryNarrative()));
            }

            BooleanQuery.Builder booleanQuery = new BooleanQuery.Builder();

            Query boostedTermQuery1 = new BoostQuery(q1, (float) 40.5);
            Query boostedTermQuery2 = new BoostQuery(q2, 17);
            booleanQuery.add(boostedTermQuery1, BooleanClause.Occur.MUST);
            booleanQuery.add(boostedTermQuery2, BooleanClause.Occur.SHOULD);
            if (q3 != null) {
                Query boostedTermQuery3 = new BoostQuery(q3, (float) 11.5);
                booleanQuery.add(boostedTermQuery3, BooleanClause.Occur.SHOULD);
            }
//            Similarity TFID

			IndexSearcher searcher = new IndexSearcher(reader);
			
			Similarity sims[] = {
	                new ClassicSimilarity(),
	                new LMDirichletSimilarity(500),
	                new DFRSimilarity(new BasicModelIn(), new AfterEffectL(), new NormalizationH1()),
	               };

            searcher.setSimilarity(new MultiSimilarity(sims));
          //searcher.setSimilarity(iwConfig.getSimilarity());
			//searcher.setSimilarity(new BM25Similarity());

			//to get all retrieved docs
			TotalHitCountCollector collector = new TotalHitCountCollector();
			//searcher.search(q, collector);
            searcher.search(booleanQuery.build(), collector);

			//use 1 if there is 0 hits
			//TopDocs docs = searcher.search(q, Math.max(1, collector.getTotalHits()));
            TopDocs docs = searcher.search(booleanQuery.build(), Math.max(1, collector.getTotalHits()));
			ScoreDoc[] hits = docs.scoreDocs;   //returns an array of retrieved documents
			num++;

			//System.out.println(hits.length);


			//     // 4. display results

            int hitCount = Math.min(hits.length, 3000);
			for (int i = 0; i < hitCount; ++i) {
				int docId = hits[i].doc;
				Document d = searcher.doc(docId);

				float score = hits[i].score;

				//write to a results file
				//fix order
				String results =queries.get(j).getQueryNum() + " Q0 " + d.get("id") + " " + (i + 1) + " " + score + " exp\n";
				writer.write(results);

			}
		}

		// reader can only be closed when there
		// is no need to access the documents any more.
		reader.close();
	}

    public void parseQuery() throws IOException {
        System.out.println("Parsing Query Topics...");
        String appendedString = "";
        File file = new File("../lucene_assignment2/CS7IS3-Assignment2-Topics");
        BufferedReader br = new BufferedReader(new FileReader(file));
        String str = "";
        String[] tagArray = {"narr", "desc", "title", "num"};
        String relNarr = "", nonRelNarr = "", desc = "", title = "", num = "";


        StringBuilder stringBuilder = new StringBuilder();
        while ((str = br.readLine()) != null){
            //is it at the end of a document
            if (str.contains("</top>")) {
                //append end tag and parse and add queries
                stringBuilder = stringBuilder.append(str /*+ " "*/);
                appendedString = stringBuilder.toString();
                //parse appended string
                org.jsoup.nodes.Document doc = Jsoup.parse(appendedString);
                for (String tag: tagArray) {
                    Element element = doc.select(tag).first();
                    doc.select(tag).remove();
                    if (tag.equals("narr")) {
                        String narr = element.text().substring(11, element.text().length());
                        ArrayList<String> narrative = getRelevantNarrative(narr);
                        relNarr = narrative.get(0);
                        nonRelNarr = narrative.get(1);
                        //System.out.println(narr);
                    }
                    else if (tag.equals("desc")) {
                        desc = element.text().substring(13, element.text().length());
                        //System.out.println(desc);
                    }
                    else if (tag.equals("title")) {
                        title = element.text();
                        //System.out.println(title);
                    }
                    else if (tag.equals("num")) {
                        num = element.text().substring(8, element.text().length());
                        //System.out.println(num);
                    }

                }

                queries.add(new MyQuery(Integer.parseInt(num), title, desc, relNarr, nonRelNarr));
                //reset appended string
                stringBuilder.setLength(0);

            }
            else {
                //not the tag, therefore must append this line to the string (value of tag)
                stringBuilder = stringBuilder.append(str + " ");
            }


        }
        br.close();



    }

    private ArrayList<String> getRelevantNarrative(String narrative) {
        String relevantNarrative = "", nonRelevantNarrative = "";
        String[] sentences = narrative.split("[;:,.]");
        BreakIterator breakIterator = BreakIterator.getSentenceInstance();
        breakIterator.setText(narrative);
        for (String sentance : sentences) {
            if (!sentance.contains("irrelevant") && !sentance.contains("not relevant")) {
                sentance = sentance.toLowerCase();
                //remove unnecessary words
                //sentance = sentance.replaceAll("relevant | a relevant document | also relevant | relevant items | relevant documents | are all relevant | will contain | a document must indicate", "");
                sentance = sentance.replaceAll("a relevant document identifies|a relevant document could|a relevant document may include|a relevant document must|a relevant document will|a document will|to be relevant|relevant documents|a document must|relevant|will contain|will discuss|will provide|must cite|documents describing|documents pertaining|relevant when tied|must discuss|must include|must contain|must describe|must show", "");
                relevantNarrative = relevantNarrative + " " + sentance;

            }
            else if (!sentance.contains("of interest")) {
                sentance = sentance.toLowerCase();
                //remove unnecessary words
                sentance = sentance.replaceAll("not relevant | is not relevant | are not relevant | for business purposes are not relevant | are irrelevant | even though they might be decorative, are not relevant | without citing a specific case are not relevantare not relevant unless they also cite", "");
                nonRelevantNarrative = nonRelevantNarrative + " " + sentance;
            }
        }
        ArrayList<String> narrativeList = new ArrayList<String>();
        narrativeList.add(relevantNarrative);
        narrativeList.add(nonRelevantNarrative);
        return narrativeList;
    }


}
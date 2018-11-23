package com.mycompany.app;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.search.*;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.store.Directory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;


import java.io.*;
import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
		boolean narrTest;



		//MultiFieldQueryParser parser = new MultiFieldQueryParser(new String[]{"content", "title"}, analyzer, boostMap);

		//query
		for (int j = 0; j < queries.size(); j++) {
		    System.out.println(j);
            Map<String, Float> boostMap = new HashMap<String, Float>();
            boostMap.put("title", 1.0f);
            boostMap.put("content", 7.5f);
			MultiFieldQueryParser parser = new MultiFieldQueryParser(new String[]{"content", "title"}, analyzer, boostMap);
			
			// Tyring to remove common words like 'relevant', 'irrelevant'
			String narrative = queries.get(j).getQueryNarrative();
			StringBuilder relevantNarrative = new StringBuilder();
			StringBuilder irrelevantNarrative = new StringBuilder();
			String[] narativeSplit = narrative.toLowerCase().split("\\.");
			List<String> result = new ArrayList<String>();
			for (String sentence : narativeSplit) {

				if (!sentence.contains("not relevant") && !sentence.contains("irrelevant")) {

					String re = sentence.replaceAll(
							"documents|a relevant document|a document will|to be relevant|relevant documents|a document must|relevant|will contain|will discuss|will provide|must cite",
							"");
					relevantNarrative.append(re);
					narrTest = false;
				} else {
					String re = sentence.replaceAll("are also not relevant|are not relevant|are irrelevant|is not relevant", "");
					irrelevantNarrative.append(re);
					narrTest = true;
				}
			}
			result.add(relevantNarrative.toString());
			result.add(irrelevantNarrative.toString());
			
			String querystr = /*queries.get(j).getQueryNarrative() +" "+*/ 
					 queries.get(j).getQueryTitle() + " " + queries.get(j).getQueryDescription()+ " " + result.get(0);


			Query q1 = parser.parse(QueryParser.escape(queries.get(j).getQueryTitle()));
			Query q2 = parser.parse(QueryParser.escape(queries.get(j).getQueryDescription()));
			Query q3 = parser.parse(QueryParser.escape(narrative));
			
			BooleanQuery.Builder booleanQuery = new BooleanQuery.Builder();
			
			Query boostedTermQuery1 = new BoostQuery(q1, (float) 30.5);
		    Query boostedTermQuery2 = new BoostQuery(q2, 30);
		    Query boostedTermQuery3 = new BoostQuery(q3, (float) 7.5);
		    booleanQuery.add(boostedTermQuery1, Occur.MUST);
		    booleanQuery.add(boostedTermQuery2, Occur.SHOULD);
		    booleanQuery.add(boostedTermQuery3, Occur.SHOULD);
			//System.out.println("query " + q.toString());

			IndexSearcher searcher = new IndexSearcher(reader);
			//searcher.setSimilarity(iwConfig.getSimilarity());
			
            searcher.setSimilarity(new BM25Similarity());
            
            

			//to get all retrieved docs
			TotalHitCountCollector collector = new TotalHitCountCollector();
			//searcher.search(q, collector);
			searcher.search(booleanQuery.build(), collector);

			//use 1 if there is 0 hits
			TopDocs docs = searcher.search(booleanQuery.build(), Math.max(1, collector.getTotalHits()));
			ScoreDoc[] hits = docs.scoreDocs;   //returns an array of retrieved documents
			num++;

			//System.out.println(hits.length);


			//     // 4. display results
			int limit =0;
			if(hits.length > 3000) {
				limit = 3000;
			}
			else {
				limit = hits.length;
			}

			for (int i = 0; i < limit; ++i) {
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
        String narr = "", desc = "", title = "", num = "";


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
                        narr = element.text().substring(11, element.text().length());
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

                queries.add(new MyQuery(Integer.parseInt(num), title, desc, narr));
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




}
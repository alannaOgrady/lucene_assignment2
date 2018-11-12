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
import org.apache.lucene.store.Directory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;


import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
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
    public void search(IndexWriterConfig iwConfig, Directory index, BufferedWriter writer, Analyzer analyzer) throws IOException, ParseException, QueryNodeException {

		IndexReader reader = DirectoryReader.open(index);

		Map<String, Float> boostMap = new HashMap<String, Float>();
		//String[] tagArray = {"narr", "desc", "title", "num"};
		boostMap.put("title", 2.3f);
		boostMap.put("content", 2.3f);
		//boostMap.put("title", 2.3f);

		MultiFieldQueryParser parser = new MultiFieldQueryParser(new String[]{"narr", "desc", "title"}, analyzer, boostMap);

		//query
		for (int j = 0; j < queries.size(); j++) {
			//     //MultiFieldQueryParser parser = new MultiFieldQueryParser(new String[]{"content", "title"}, analyzer);
			String querystr = queries.get(j).getQueryNarrative() +" "+ queries.get(j).getQueryDescription() 
					+ " "+ queries.get(j).getQueryTitle();


			Query q = parser.parse(QueryParser.escape(querystr));
			System.out.println("query " + q.toString());

			IndexSearcher searcher = new IndexSearcher(reader);
			searcher.setSimilarity(iwConfig.getSimilarity());

			//to get all retrieved docs
			TotalHitCountCollector collector = new TotalHitCountCollector();
			searcher.search(q, collector);

			//use 1 if there is 0 hits
			TopDocs docs = searcher.search(q, Math.max(1, collector.getTotalHits()));
			ScoreDoc[] hits = docs.scoreDocs;   //returns an array of retrieved documents
			num++;

			System.out.println(hits.length);


			//     // 4. display results

			for (int i = 0; i < hits.length; ++i) {
				int docId = hits[i].doc;
				Document d = searcher.doc(docId);

				float score = hits[i].score;

				//write to a results file
				String results = (j+1) + " Q0" + d.get("id") + " " + (i + 1) + " " + score + " exp\n";
				writer.write(results);

			}
		}

		// reader can only be closed when there
		// is no need to access the documents any more.
		reader.close();
	}

    public void parseQuery() throws IOException {
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
                        System.out.println(narr);
                    }
                    else if (tag.equals("desc")) {
                        desc = element.text().substring(13, element.text().length());
                        System.out.println(desc);
                    }
                    else if (tag.equals("title")) {
                        title = element.text();
                        System.out.println(title);
                    }
                    else if (tag.equals("num")) {
                        num = element.text().substring(8, element.text().length());
                        System.out.println(num);
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

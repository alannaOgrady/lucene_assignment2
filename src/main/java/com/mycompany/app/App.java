package com.mycompany.app;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.custom.CustomAnalyzer;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.store.Directory;



public class App {
	public static void main( String[] args ) throws IOException, ParseException, QueryNodeException {
		Analyzer analyzer = CustomAnalyzer.builder()
				.withTokenizer("standard")
				.addTokenFilter("lowercase")
				.addTokenFilter("stop")
				.addTokenFilter("porterstem")
				.build();
		MyIndexWriter iw = MyIndexWriter.getInstance();

		//Directory index = iw.index(0, analyzer);

		//testing query parsing
		//        MyIndexSearcher searcher = MyIndexSearcher.getInstance();
		//        searcher.parseQuery();
		//             //Directory index = iw.index(i, analyzer);
		//             String fileName = "trec_res_" + iw.getConfig().getSimilarity().toString();
		//             fileName = fileName.replaceAll("\\p{P}","");
		//            BufferedWriter writer = new BufferedWriter(new FileWriter("../lucene_assignment2/results/" + fileName));
		//            searcher.search(iw.getConfig(), index, writer, analyzer);
		//            writer.close();

		//
		//        must update
		//
        MyIndexSearcher searcher = MyIndexSearcher.getInstance();
        //only need to parse queries once
		searcher.parseQuery();
		//so indexes and searches with both BM25 and Classic
		for (int i = 0; i < 2; i++) {
			Directory index = iw.index(i, analyzer);
			String fileName = "trec_res_" + iw.getConfig().getSimilarity().toString();
			fileName = fileName.replaceAll("\\p{P}","");
			BufferedWriter writer = new BufferedWriter(new FileWriter("../lucene_assignment2/results/" + fileName));
			searcher.search(iw.getConfig(), index, writer, analyzer);
			writer.close();
		}
	}
}

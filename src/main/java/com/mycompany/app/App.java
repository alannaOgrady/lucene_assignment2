package com.mycompany.app;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.custom.CustomAnalyzer;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;


public class App {
	public static void main( String[] args ) throws IOException, ParseException, QueryNodeException {
		Analyzer analyzer = CustomAnalyzer.builder()
				.withTokenizer("standard")
				.addTokenFilter("lowercase")
				.addTokenFilter("stop")
				.addTokenFilter("porterstem")
				.build();
		MyIndexWriter iw = MyIndexWriter.getInstance();

        MyIndexSearcher searcher = MyIndexSearcher.getInstance();
		searcher.parseQuery();

		Directory index = iw.index( analyzer);
		//use when dont want to parse and index, just want to use old index
		//Directory index = iw.getTestIndex();
		//String fileName = "trec_res_" + iw.getConfig().getSimilarity().toString();
		String fileName = "trec_res_BM25";

		fileName = fileName.replaceAll("\\p{P}","");
		BufferedWriter writer = new BufferedWriter(new FileWriter("../lucene_assignment2/results/" + fileName));
		searcher.search(index, writer, analyzer);
		writer.close();

	}
}

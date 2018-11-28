package com.mycompany.app;

import java.io.*;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.*;
import java.text.DateFormat;

import org.apache.commons.io.FileUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

public class MyIndexWriter 
{
	private static MyIndexWriter instance = null;
	private String indexPath = "../lucene_assignment2/indexes/";
	private IndexWriterConfig config;
	public int docs_created = 0;




	private MyIndexWriter() {
		// Exists only to defeat instantiation.
	}
	public static MyIndexWriter getInstance() {
		if(instance == null) {
			instance = new MyIndexWriter();
		}
		return instance;
	}

	public Directory getTestIndex() throws IOException{
		return FSDirectory.open(Paths.get(indexPath));
	}

	//
	//    must update
	//
	public Directory index(Analyzer analyzer) throws IOException, java.text.ParseException {

		Directory index = FSDirectory.open(Paths.get(indexPath));

		config = new IndexWriterConfig(analyzer);
		config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
		config.setSimilarity(new BM25Similarity());
		System.out.println("Indexing with BM25");
		/*else if (iteration == 1) {
            config.setSimilarity(new ClassicSimilarity());
            System.out.println("Indexing with Classic Similarity");
        }*/

		IndexWriter w = new IndexWriter(index, config);
		w.deleteAll();
		parseLATimes(w);
		parseFinacialTimes(w);
		parseFBIS(w);
		parseFR(w);
		w.close();

		return index;

	}

	public IndexWriterConfig getConfig() {
		return config;
	}


	private void parseLATimes(IndexWriter w) throws IOException, ParseException{
		System.out.println("Parsing LA Times...");
		File dir = new File("../lucene_assignment2/Assignment Two/latimes");

		for (File file : dir.listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				if (!pathname.getName().endsWith(".txt"))
					return true;
				else
					return false;
			}
		})) {
			//parse
			BufferedReader br = new BufferedReader(new FileReader(file));
			String string = "", appendedString = "";
			String[] tagArray = {"DOCNO", "DATE", "HEADLINE", "BYLINE", "TEXT", "GRAPHIC"};
			String docNo = "",headline = "", byline = "", text = "", graphic = "";
			Date finalDate = null;
			DateFormat format = DateFormat.getDateInstance(DateFormat.LONG, Locale.ENGLISH);

			StringBuilder stringBuilder = new StringBuilder();
			while ((string = br.readLine()) != null) {
				//check at the end of a document
				if (string.contains("</DOC>")) {
					stringBuilder = stringBuilder.append(string);
					appendedString = stringBuilder.toString();
					org.jsoup.nodes.Document doc = Jsoup.parse(appendedString);
					for (String tag : tagArray) {
						Element element = doc.select(tag).first();
						doc.select(tag).remove();
						if (element != null) {
							if (tag.equals("DOCNO")) {
								//System.out.println(element.text());
								docNo = element.text();
							} else if(tag.equals("DATE")) {
								String date = element.text();
								if(!date.equals("Correction Appended")) {


									//System.out.println(date);
									int firstcomma = date.indexOf(',');
									String firstDate = date.substring(0, firstcomma);
									//System.out.print(firstDate);
									String last = date.substring(firstcomma+1);
									String lastremovecomma = last.substring(0,last.indexOf(','));
									String stringfinalDate = firstDate +", "+ lastremovecomma;
									finalDate = format.parse(stringfinalDate);
								}
								else {
									finalDate = format.parse("January 1, 1990");
								}

							}

							else if (tag.equals("HEADLINE")) {
								//System.out.println(element.text());
								headline = element.text();
							} else if (tag.equals("BYLINE")) {
								//System.out.println(element.text());
								//byline = element.text();
							} else if (tag.equals("TEXT")) {
								//System.out.println(element.text());
								text = element.text();
							} else if (tag.equals("GRAPHIC")) {
								//System.out.println(element.text());
								//graphic = element.text();
							}

						}
					}
					stringBuilder.setLength(0);
					//have reached end of a document write to index
					addDoc(w, docNo, finalDate, headline, text);
					//reset
					docNo = headline = byline = text = graphic = "";
				}
				else {
					stringBuilder = stringBuilder.append(string + " ");
				}
			}


			br.close();
		}
	}

	private void parseFinacialTimes(IndexWriter w) throws IOException, java.text.ParseException {
		System.out.println("Parsing Financial Times...");
		File dir = new File("../lucene_assignment2/Assignment Two/ft");
		//get subfolders
		String[] directories = dir.list(new FilenameFilter() {
			@Override
			public boolean accept(File current, String name) {
				return new File(current, name).isDirectory();
			}
		});
		Date finalDate = null;
		DateFormat format = DateFormat.getDateInstance(DateFormat.SHORT, Locale.ENGLISH);

		//System.out.println(Arrays.toString(directories));
		for (String subDirectory : directories) {
			File subDir = new File(dir.getPath() + "/" + subDirectory);
			for (File file : subDir.listFiles()) {
				//parse
				BufferedReader br = new BufferedReader(new FileReader(file));
				String string = "", appendedString = "";
				String[] tagArray = {"DOCNO","DATE", "HEADLINE", "TEXT"};
				String docNo = "", headline = "", text = "";

				StringBuilder stringBuilder = new StringBuilder();
				while ((string = br.readLine()) != null) {
					//check at the end of a document
					if (string.contains("</DOC>")) {
						stringBuilder = stringBuilder.append(string);
						appendedString = stringBuilder.toString();
						org.jsoup.nodes.Document doc = Jsoup.parse(appendedString);
						for (String tag : tagArray) {
							Element element = doc.select(tag).first();
							doc.select(tag).remove();
							if (element != null) {
								if (tag.equals("DOCNO")) {
									//System.out.println(element.text());
									docNo = element.text();
								}
								else if(tag.equals("DATE")) {
									String rawdate = element.text();
									String year = rawdate.substring(0, 2);
									String month = rawdate.substring(2,4);
									String date = rawdate.substring(4,6);
									//6/30/09
									//                            	   if(date.charAt(0) == '0') {
										//                            		   date = date.substring(1);
									//                            	   }
									String stringfinalDate = month + "/" + date + "/"+ year;
									//System.out.println(element.text() + "----->"+stringfinalDate);
									finalDate = format.parse(stringfinalDate);

								}
								else if (tag.equals("HEADLINE")) {
									//System.out.println(element.text());
									headline = element.text();
								}  else if (tag.equals("TEXT")) {
									//System.out.println(element.text());
									text = element.text();
								}
							}
						}
						stringBuilder.setLength(0);
						//have reached end of a document write to index
						addDoc(w, docNo,finalDate, headline, text);
						//reset
						docNo = headline = text = "";

					}
					else {
						stringBuilder = stringBuilder.append(string + " ");
					}
				}


				br.close();
			}
		}
	}

	//
	//   Docs don't seem to have a title, seems to be a mass of text
	//   Other tags within <TEXT> tag are not consistent throughout the collection
	//   Removing unnecessary information from within the text tag (eg. billing info)
	//
	private void parseFR(IndexWriter w) throws IOException, java.text.ParseException {
		System.out.println("Parsing Federal Register...");
		File dir = new File("../lucene_assignment2/Assignment Two/fr94");
		//get subfolders
		String[] directories = dir.list(new FilenameFilter() {
			@Override
			public boolean accept(File current, String name) {
				return new File(current, name).isDirectory();
			}
		});
		Date finalDate = null;
		DateFormat format = DateFormat.getDateInstance(DateFormat.LONG, Locale.ENGLISH);
		finalDate = format.parse("January 1, 1990");
		//System.out.println(Arrays.toString(directories));
		for (String subDirectory : directories) {
			File subDir = new File(dir.getPath() + "/" + subDirectory);
			for (File file : subDir.listFiles()) {
				//parse
				BufferedReader br = new BufferedReader(new FileReader(file));
				String string = "", appendedString = "";
				String[] tagArray = {"DOCNO", /*"RINDOCK", "BILLING", "FRFILING", "CFRNO", "SIGNER", "SIGNJOB", "DATE",*/ "TITLE", "TEXT"};
				String docNo = "", title = "", text = "";

				StringBuilder stringBuilder = new StringBuilder();
				while ((string = br.readLine()) != null) {
					//check at the end of a document
					if (string.contains("</DOC>")) {
						stringBuilder = stringBuilder.append(string);
						appendedString = stringBuilder.toString();
						org.jsoup.nodes.Document doc = Jsoup.parse(appendedString);
						for (String tag : tagArray) {
							Element element = doc.select(tag).first();
							doc.select(tag).remove();
							if (element != null) {
								if (tag.equals("DOCNO")) {
									//System.out.println(element.text());
									docNo = element.text();
								}
								else if (tag.equals("TITLE")) {
									title = element.text();
								} else if (tag.equals("TEXT")) {
									//System.out.println(element.text());
									text = element.text();
								}
							}
						}
						stringBuilder.setLength(0);
						//have reached end of a document write to index
						addDoc(w, docNo, finalDate, title, text);
						//reset
						docNo = title = text = "";

					}
					else {
						stringBuilder = stringBuilder.append(string + " ");
					}
				}


				br.close();
			}
		}
	}

	//
	//   must update
	//
	private void parseFBIS(IndexWriter w) throws IOException, java.text.ParseException {
		System.out.println("Parsing Foreign Broadcast Information Service...");
		// Foreign Broadcast Information Service
		File dir = new File("../lucene_assignment2/Assignment Two/fbis");

		for (File file : dir.listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				if (!pathname.getName().endsWith(".txt"))
					return true;
				else
					return false;
			}
		})) {
			//parse
			BufferedReader br = new BufferedReader(new FileReader(file));
			String string = "", appendedString = "";
			String[] tagArray = {"DOCNO", "HEADER", "H3", "TI", "TEXT", "DATE1"};
			String[] nestedTags = {"H3", "TI"};
			String docNo = "", headline = "", text = "", rawDate = "";

			DateFormat format = DateFormat.getDateInstance(DateFormat.LONG, Locale.ENGLISH);
			Date finalDate = format.parse("January 1, 1990");
			String[] splitDate, splitInput;

			StringBuilder stringBuilder = new StringBuilder();
			while ((string = br.readLine()) != null) {
				//check at the end of a document
				if (string.contains("</DOC>")) {
					stringBuilder = stringBuilder.append(string);
					appendedString = stringBuilder.toString();
					org.jsoup.nodes.Document doc = Jsoup.parse(appendedString);
					for (String tag : tagArray) {
						Element element = doc.select(tag).first();
						doc.select(tag).remove();
						if (element != null) {
							if (tag.equals("DOCNO")) {
								//System.out.println(element.text());
								docNo = element.text();
							}
							else if (tag.equals("HEADER")) {
								//System.out.println(element.text());
								headline = element.text();
							}
							else if (tag.equals("DATE1")) {


								if (element.text().equals("March 07 1994")) {
									rawDate = "March 07, 1994";
								}
								else {
								//System.out.println(element.text());

								splitDate = element.text().split(" ");


								rawDate = splitDate[1]+" "+splitDate[0]+", "+splitDate[2];

								}
								//System.out.println("RAW DATE: "+rawDate);
								finalDate = format.parse(rawDate);

//									String date = element.text();
//									int firstcomma = date.indexOf(',');
//									String firstDate = date.substring(0, firstcomma);
									//System.out.print(firstDate);
//									String last = date.substring(firstcomma+1);
//									String lastremovecomma = last.substring(0,last.indexOf(','));
//									String stringfinalDate = firstDate +", "+ lastremovecomma;
//									DateFormat format2 = DateFormat.getDateInstance(DateFormat.SHORT, Locale.ENGLISH);
//									finalDate = format2.parse(stringfinalDate);






								//System.out.println(element.text() + "-----" + finalDate);

							}
							else if (tag.equals("H3")) {
								//System.out.println(element.text());
								//headline = element.text();
							}else if (tag.equals("TEXT")) {
								//System.out.println(element.text());

								text = element.text();
							}

						}
					}
					stringBuilder.setLength(0);
					//have reached end of a document write to index
					addDoc(w, docNo, finalDate, headline, text);
					//reset
					docNo = headline = text = "";
				}
				else {
					stringBuilder = stringBuilder.append(string + " ");
				}
			}


			br.close();
		}
	}

	//
	//   must update
	//
	private void addDoc(IndexWriter w, String id, Date date, String title, String content) throws IOException {

		List<String> countries = FileUtils.readLines(new File("../lucene_assignment2/src/main/java/com/mycompany/app/countries.txt"), "utf-8");

		Document doc = new Document();

		for(int i =0; i < countries.size(); i++)
		{
			if(title.contains(countries.get(i)) || content.contains(countries.get(i))) {
				doc.add(new StringField("countriesBoolean", "true", Field.Store.YES));
				break;
			}
			else
				doc.add(new StringField("countriesBoolean", "false", Field.Store.YES));
		}

		// use a string field for author because we don't want it tokenized
		doc.add(new StringField("id", id, Field.Store.YES));
		doc.add(new StringField("date", date.toString(),Field.Store.YES));
		doc.add(new TextField("title", title, Field.Store.YES));
		doc.add(new TextField("content", content, Field.Store.YES));

		w.addDocument(doc);
		docs_created++;
	}
}
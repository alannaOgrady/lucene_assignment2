package com.mycompany.app;

import java.io.*;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Scanner;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

public class MyIndexWriter 
{
    private static MyIndexWriter instance = null;
    private String indexPath = "../assignment-2/indexes/";

    private String identity = "";
    private String title = "";
    private String author = "";
    private String source = "";
    private String content = "";
    private Boolean firstRun = true;
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

//
//    must update
//
    public Directory index(int iteration, Analyzer analyzer) throws IOException, ParseException {

        Directory index = FSDirectory.open(Paths.get(indexPath));

        config = new IndexWriterConfig(analyzer);
        config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
        if (iteration == 0)
            config.setSimilarity(new BM25Similarity());
        else if (iteration == 1)
            config.setSimilarity(new ClassicSimilarity());

        IndexWriter w = new IndexWriter(index, config);
        w.deleteAll();
        //parseForDocs(w);
        w.close();

        return index;

    }

    public IndexWriterConfig getConfig() {
        return config;
    }


    public void parseLATimes(/*IndexWriter w*/) throws IOException{
        File dir = new File("../assignment-2/Assignment Two/latimes");

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
            String[] tagArray = {"DOCNO", "HEADLINE", "BYLINE", "TEXT", "GRAPHIC"};
            String docNo = "", headline = "", byline = "", text = "", graphic = "";

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
                                System.out.println(element.text());
                                //docNo = element.text();
                            } else if (tag.equals("HEADLINE")) {
                                System.out.println(element.text());
                                //headline = element.text();
                            } else if (tag.equals("BYLINE")) {
                                System.out.println(element.text());
                                //byline = element.text();
                            } else if (tag.equals("TEXT")) {
                                System.out.println(element.text());
                                //text = element.text();
                            } else if (tag.equals("GRAPHIC")) {
                                System.out.println(element.text());
                                //graphic = element.text();
                            }
                        }
                    }
                    stringBuilder.setLength(0);
                    //have reached end of a document write to index
                    //addDoc(w, docNo, headline, byline, text, graphic);
                    //reset
                    //docNo = headline = byline = text = graphic = "";
                }
                else {
                    stringBuilder = stringBuilder.append(string + " ");
                }
            }


            br.close();
        }
    }

   public void parseFinacialTimes(/*IndexWriter w*/) throws IOException {
       File dir = new File("../assignment-2/Assignment Two/ft");
       //get subfolders
       String[] directories = dir.list(new FilenameFilter() {
           @Override
           public boolean accept(File current, String name) {
               return new File(current, name).isDirectory();
           }
       });
       System.out.println(Arrays.toString(directories));
       for (String subDirectory : directories) {
           File subDir = new File(dir.getPath() + "/" + subDirectory);
           for (File file : subDir.listFiles()) {
               //parse
               BufferedReader br = new BufferedReader(new FileReader(file));
               String string = "", appendedString = "";
               String[] tagArray = {"DOCNO", "HEADLINE", "TEXT"};
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
                                   System.out.println(element.text());
                                   //docNo = element.text();
                               } else if (tag.equals("HEADLINE")) {
                                   //System.out.println(element.text());
                                   //headline = element.text();
                               }  else if (tag.equals("TEXT")) {
                                   //System.out.println(element.text());
                                   //text = element.text();
                               }
                           }
                       }
                       stringBuilder.setLength(0);
                       //have reached end of a document write to index
                       //addDoc(w, docNo, headline, text);
                       //reset
                       //docNo = headline = text = "";

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
   private void parseFR() {
       //must write
   }

//
//   must update
//
    private void parseBRIS() {
        //must write
    }

//
//   must update
//
    private void addDoc(IndexWriter w, String id, String title, String author, String source, String content) throws IOException {
        Document doc = new Document();
        doc.add(new StringField("id", id, Field.Store.YES));
        doc.add(new TextField("title", title, Field.Store.YES));
        // use a string field for author because we don't want it tokenized
        //doc.add(new StringField("author", author, Field.Store.YES));
        //doc.add(new TextField("source", source, Field.Store.YES));
        doc.add(new TextField("content", content, Field.Store.YES));

        w.addDocument(doc);
        docs_created++;
    }
}
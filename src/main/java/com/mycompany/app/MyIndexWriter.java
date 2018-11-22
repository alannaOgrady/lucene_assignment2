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
    public Directory index(Analyzer analyzer) throws IOException, ParseException {

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


    private void parseLATimes(IndexWriter w) throws IOException{
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
                                //System.out.println(element.text());
                                docNo = element.text();
                            } else if (tag.equals("HEADLINE")) {
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
                    addDoc(w, docNo, headline, text);
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

   private void parseFinacialTimes(IndexWriter w) throws IOException {
       System.out.println("Parsing Financial Times...");
       File dir = new File("../lucene_assignment2/Assignment Two/ft");
       //get subfolders
       String[] directories = dir.list(new FilenameFilter() {
           @Override
           public boolean accept(File current, String name) {
               return new File(current, name).isDirectory();
           }
       });
       //System.out.println(Arrays.toString(directories));
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
                                   //System.out.println(element.text());
                                   docNo = element.text();
                               } else if (tag.equals("HEADLINE")) {
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
                       addDoc(w, docNo, headline, text);
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
   private void parseFR(IndexWriter w) throws IOException {
       System.out.println("Parsing Federal Register...");
       File dir = new File("../lucene_assignment2/Assignment Two/fr94");
       //get subfolders
       String[] directories = dir.list(new FilenameFilter() {
           @Override
           public boolean accept(File current, String name) {
               return new File(current, name).isDirectory();
           }
       });
       //System.out.println(Arrays.toString(directories));
       for (String subDirectory : directories) {
           File subDir = new File(dir.getPath() + "/" + subDirectory);
           for (File file : subDir.listFiles()) {
               //parse
               BufferedReader br = new BufferedReader(new FileReader(file));
               String string = "", appendedString = "";
               String[] tagArray = {"DOCNO", /*"RINDOCK", "BILLING", "FRFILING", "CFRNO", "SIGNER", "SIGNJOB",*/ "TITLE", "TEXT"};
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
                       addDoc(w, docNo, title, text);
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
    private void parseFBIS(IndexWriter w) throws IOException {
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
             String[] tagArray = {"DOCNO", "HEADER", "H3", "TI", "TEXT"};
             String[] nestedTags = {"H3", "TI"};
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
                             } else if (tag.equals("HEADER")) {
                                 //System.out.println(element.text());
                                 headline = element.text();
                             } else if (tag.equals("H3")) {
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
                     addDoc(w, docNo, headline, text);
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
    private void addDoc(IndexWriter w, String id, String title, String content) throws IOException {
        Document doc = new Document();
        // use a string field for author because we don't want it tokenized
        doc.add(new StringField("id", id, Field.Store.YES));
        doc.add(new TextField("title", title, Field.Store.YES));
        doc.add(new TextField("content", content, Field.Store.YES));

        w.addDocument(doc);
        docs_created++;
    }
}
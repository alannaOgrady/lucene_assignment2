package com.mycompany.app;

import java.io.*;
import java.nio.file.Paths;

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


    private void parseForDocs(IndexWriter w) throws IOException {
        File file = new File("../lucene_assignment/src/main/java/com/alannaogrady/cran.all.1400");
        BufferedReader br = new BufferedReader(new FileReader(file));
        String str;
        String tag;
        String prevTag = "";


        StringBuilder stringBuilder = new StringBuilder();
        while ((str = br.readLine()) != null){
            tag = str.length() < 2 ? str : str.substring(0, 2);
            
            //does beginning of line start with a tag
            if (tag.equals(".I") || tag.equals(".T") || tag.equals(".A") || tag.equals(".B") || tag.equals(".W")) {

                //remove tag from the rest of the string
                str = str.substring(2);
                //check the previuos tag and add the appended string as its value
                checkPrevTag(w, prevTag, stringBuilder);
                
                //must append the rest of the line that the tag is on
                stringBuilder = stringBuilder.append(str + " ");

                //update prevTag
                prevTag = tag;
            }
            else {
                //not the tag, therefore must append this line to the string (value of tag)
                stringBuilder = stringBuilder.append(str + " ");
            }
            
        }
        //must deal with last section
        //must check what the last tag was
        checkPrevTag(w, prevTag, stringBuilder);
        //have finished reading in file must deal with last document
        //call add doc with doc info
        if (!identity.equals("") || !title.equals("") || !author.equals("") || !source.equals("") ||!content.equals(""))
            addDoc(w, identity, title, author, source, content);
        //reinitialise
        identity = title = author = source = content = "";
    }

    private void checkPrevTag(IndexWriter w, String prevTag, StringBuilder stringBuilder) throws IOException {
        //check what the previous tag was as the string we have been collecting belongs to this
        String appendedString = stringBuilder.toString();
        if (prevTag.equals(".I")) {
            //we are on a new document add/print/whatever prev doc
            if (!firstRun) {
                //call add doc with doc info
                if (!identity.equals("") || !title.equals("") || !author.equals("") || !source.equals("") ||!content.equals(""))
                    addDoc(w, identity, title, author, source, content);
                //reinitialise
                identity = title = author = source = content = "";
                
            }
            firstRun = false;
            //put subsequent info you have gathered into identity field
            identity +=  " " + appendedString;
            stringBuilder.setLength(0);
        }
        else if (prevTag.equals(".T")) {
            //put subsequent info you have gathered into title field
            title +=  " " +  appendedString;
            stringBuilder.setLength(0);
        }
        else if (prevTag.equals(".A")) {
            //put subsequent info you have gathered into author field
            author +=  " " +  appendedString;
            stringBuilder.setLength(0);
        }
        else if (prevTag.equals(".B")) {
            //put subsequent info you have gathered into bibliography field
            source +=  " " +  appendedString;
            stringBuilder.setLength(0);
        }
        else if (prevTag.equals(".W")) {
            //put into content field
            content +=  " " +  appendedString;
            stringBuilder.setLength(0);
        }
    }

   

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
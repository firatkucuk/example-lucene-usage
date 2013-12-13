
package im.firat.examples.lucene.luceneusage;


import java.io.IOException;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.*;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;



public class Main02 {



    //~ --- [CONSTRUCTORS] ---------------------------------------------------------------------------------------------

    public Main02() {

    }



    //~ --- [METHODS] --------------------------------------------------------------------------------------------------

    public static void main(String[] args) {

        try {

            Directory indexDirectory = new RAMDirectory();
            String[]  contents       = new String[] {
                "bir iki üç dört",
                "üç dört beş altı",
                "beş altı yedi sekiz",
                "yedi sekiz dokuz on"
            };

            createIndex(indexDirectory, contents);
            System.out.println("Sonuçlar:");
            search(indexDirectory, "üç");

            String[] updatedContents = new String[] {
                "bir iki üç dört",
                "dört beş altı yedi",
                "beş altı yedi sekiz",
                "yedi sekiz dokuz on"
            };

            updateIndex(indexDirectory, updatedContents);
            System.out.println("Sonuçlar:");
            search(indexDirectory, "üç");

            deleteIndex(indexDirectory, "0");
            System.out.println("Sonuçlar:");
            search(indexDirectory, "üç");

            indexDirectory.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }



    //~ ----------------------------------------------------------------------------------------------------------------

    private static void createIndex(Directory indexDirectory, String[] contents) throws IOException {

        Analyzer          analyzer     = new StandardAnalyzer(Version.LUCENE_46);
        IndexWriterConfig writerConfig = new IndexWriterConfig(Version.LUCENE_46, analyzer);

        writerConfig.setOpenMode(OpenMode.CREATE);

        IndexWriter indexWriter = new IndexWriter(indexDirectory, writerConfig);

        for (int i = 0; i < contents.length; i++) {
            Document document = new Document();
            String   content  = contents[i];

            document.add(new TextField("content", content, Field.Store.NO));
            document.add(new StringField("contentNo", i + "", Field.Store.YES));
            indexWriter.addDocument(document);
        }

        indexWriter.close();
    }



    //~ ----------------------------------------------------------------------------------------------------------------

    private static void deleteIndex(Directory indexDirectory, String contentNo) throws IOException {

        Analyzer          analyzer     = new StandardAnalyzer(Version.LUCENE_46);
        IndexWriterConfig writerConfig = new IndexWriterConfig(Version.LUCENE_46, analyzer);

        writerConfig.setOpenMode(OpenMode.CREATE_OR_APPEND);

        IndexWriter indexWriter = new IndexWriter(indexDirectory, writerConfig);
        indexWriter.deleteDocuments(new Term("contentNo", contentNo));
        indexWriter.close();
    }



    //~ ----------------------------------------------------------------------------------------------------------------

    private static void search(Directory indexDirectory, String searchTerm) throws IOException, ParseException {

        String searchField = "content";

        Analyzer      analyzer  = new StandardAnalyzer(Version.LUCENE_46);
        QueryParser   parser    = new QueryParser(Version.LUCENE_46, searchField, analyzer);
        IndexReader   reader    = DirectoryReader.open(indexDirectory);
        IndexSearcher searcher  = new IndexSearcher(reader);
        Query         query     = parser.parse(searchTerm);
        TopDocs       topDocs   = searcher.search(query, 10);
        ScoreDoc[]    scoreDocs = topDocs.scoreDocs;

        for (int i = 0; i < scoreDocs.length; i++) {
            Document document = searcher.doc(scoreDocs[i].doc);
            System.out.println(document);
        }

        reader.close();
    }



    //~ ----------------------------------------------------------------------------------------------------------------

    private static void updateIndex(Directory indexDirectory, String[] contents) {

        try {
            Analyzer          analyzer     = new StandardAnalyzer(Version.LUCENE_46);
            IndexWriterConfig writerConfig = new IndexWriterConfig(Version.LUCENE_46, analyzer);

            writerConfig.setOpenMode(OpenMode.CREATE_OR_APPEND);

            IndexWriter indexWriter = new IndexWriter(indexDirectory, writerConfig);

            for (int i = 0; i < contents.length; i++) {
                Document document = new Document();
                String   content  = contents[i];

                document.add(new TextField("content", content, Field.Store.NO));
                document.add(new StringField("contentNo", i + "", Field.Store.YES));

                indexWriter.updateDocument(new Term("contentNo", i + ""), document);
            }

            indexWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

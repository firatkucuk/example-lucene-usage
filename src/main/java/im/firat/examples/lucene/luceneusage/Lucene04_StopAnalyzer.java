
package im.firat.examples.lucene.luceneusage;


import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.*;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;



/**
 * Using stop analyzer
 */
public class Lucene04_StopAnalyzer {



    //~ --- [CONSTRUCTORS] ---------------------------------------------------------------------------------------------

    public Lucene04_StopAnalyzer() {

    }



    //~ --- [METHODS] --------------------------------------------------------------------------------------------------

    public static void main(String[] args) {

        try {

            Directory indexDirectory = new RAMDirectory();
            String[]  contents       = new String[] {
                "bir ve iki ve üç ve dört",
                "üç ve dört ve beş ile altı",
                "beş ya da altı ya da yedi ve sekiz",
                "yedi ile sekiz ve dokuz ya da on"
            };

            createIndex(indexDirectory, contents);
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

        CharArraySet stopWords = new CharArraySet(Version.LUCENE_46, 4, true);
        stopWords.add("ve");
        stopWords.add("ile");
        stopWords.add("ya");
        stopWords.add("da");

        Analyzer          analyzer     = new StopAnalyzer(Version.LUCENE_46, stopWords);
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
        analyzer.close();
    }



    //~ ----------------------------------------------------------------------------------------------------------------

    private static void search(Directory indexDirectory, String searchTerm) throws IOException, ParseException {

        IndexReader   reader    = DirectoryReader.open(indexDirectory);
        IndexSearcher searcher  = new IndexSearcher(reader);
        Query         query     = new TermQuery(new Term("content", searchTerm));
        ScoreDoc[]    scoreDocs = searcher.search(query, 10).scoreDocs;

        for (int i = 0; i < scoreDocs.length; i++) {
            Document document = searcher.doc(scoreDocs[i].doc);
            System.out.println(document);
        }

        reader.close();
    }
}

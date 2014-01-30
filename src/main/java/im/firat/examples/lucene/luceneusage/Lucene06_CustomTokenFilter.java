
package im.firat.examples.lucene.luceneusage;


import java.io.IOException;
import java.io.Reader;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.core.WhitespaceTokenizer;
import org.apache.lucene.analysis.miscellaneous.LengthFilter;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tr.TurkishLowerCaseFilter;
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
 * Using custom token filter
 */
public class Lucene06_CustomTokenFilter {



    //~ --- [CONSTRUCTORS] ---------------------------------------------------------------------------------------------

    public Lucene06_CustomTokenFilter() {

    }



    //~ --- [METHODS] --------------------------------------------------------------------------------------------------

    public static void main(String[] args) {

        try {

            Directory indexDirectory = new RAMDirectory();
            String[]  contents       = new String[] {
                "bir ve iki ve üç ve dört",
                "üç ve dört ve beş ile altı",
                "beş ya da ALTI ya da YEDİ ve sekiz",
                "yedi ile sekiz ve dokuz ya da on"
            };

            createIndex(indexDirectory, contents);
            System.out.println("Sonuçlar:");
            search(indexDirectory, "dokuz");

            System.out.println("Sonuçlar:");
            search(indexDirectory, "dxkxz");

            indexDirectory.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }



    //~ ----------------------------------------------------------------------------------------------------------------

    private static void createIndex(Directory indexDirectory, String[] contents) throws IOException {

        Analyzer          analyzer     = new CustomAnalyzer();
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



    //~ --- [INNER CLASSES] --------------------------------------------------------------------------------------------

    private static class CustomAnalyzer extends Analyzer {



        //~ --- [METHODS] ----------------------------------------------------------------------------------------------

        @Override
        protected TokenStreamComponents createComponents(String fieldName, Reader reader) {

            CharArraySet stopWords = new CharArraySet(Version.LUCENE_46, 4, true);
            stopWords.add("ve");
            stopWords.add("ile");
            stopWords.add("ya");
            stopWords.add("da");

            Tokenizer   tokenizer           = new WhitespaceTokenizer(Version.LUCENE_46, reader);
            TokenStream lengthFilter        = new LengthFilter(Version.LUCENE_46, tokenizer, 3, 5);
            TokenStream lowerCaseFilter     = new TurkishLowerCaseFilter(lengthFilter);
            TokenStream stopFilter          = new StopFilter(Version.LUCENE_46, lowerCaseFilter, stopWords);
            TokenStream letterChangerFilter = new LetterChangerFilter(stopFilter);

            return new TokenStreamComponents(tokenizer, letterChangerFilter);
        }
    }

    private static class LetterChangerFilter extends TokenFilter {



        //~ --- [INSTANCE FIELDS] --------------------------------------------------------------------------------------

        private final CharTermAttribute term = addAttribute(CharTermAttribute.class);



        //~ --- [CONSTRUCTORS] -----------------------------------------------------------------------------------------

        public LetterChangerFilter(TokenStream stopFilter) {

            super(stopFilter);
        }



        //~ --- [METHODS] ----------------------------------------------------------------------------------------------

        @Override
        public boolean incrementToken() throws IOException {

            if (input.incrementToken()) {
                final char[] buffer = term.buffer();
                final int    length = term.length();

                for (int i = 0; i < length; i++) {
                    char letter = buffer[i];

                    boolean isCapital = letter == 'a' || letter == 'e' || letter == 'ı' || letter == 'i';
                    isCapital = isCapital || letter == 'o' || letter == 'ö' || letter == 'u' || letter == 'ü';

                    if (isCapital) {
                        buffer[i] = 'x';
                    }
                }

                return true;
            }

            return false;
        }
    }
}

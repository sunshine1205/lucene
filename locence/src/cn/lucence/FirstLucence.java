package cn.lucence;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;

import org.apache.commons.io.FileUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.junit.Test;
import org.wltea.analyzer.lucene.IKAnalyzer;

public class FirstLucence {
	public IndexSearcher getIndexSearcher() throws IOException {
		Directory directory = FSDirectory.open(new File("C:\\Users\\Administrator\\Desktop\\lucene\\index"));
		IndexReader indexReader = DirectoryReader.open(directory);
		return  new IndexSearcher(indexReader);
	}
	public void print(IndexSearcher indexSearcher,Query query) throws Exception {
		TopDocs topDocs = indexSearcher.search(query,20);
		ScoreDoc[] scoreDocs = topDocs.scoreDocs;
		System.out.println("共搜索到总记录数：" + topDocs.totalHits);
		for (ScoreDoc scoreDoc : scoreDocs) {
			// 文档id
			int docID = scoreDoc.doc;
			// 得到文档
			Document doc = indexSearcher.doc(docID);
			// 输出 文件内容
			System.out.println("------------------------------");
			System.out.println("文件名称 =" + doc.get("filename"));
			System.out.println("文件大小 =" + doc.get("size"));
			System.out.println("文件路径 =" + doc.get("path"));
			/*System.out.println("文件内容 =" + doc.get("content"));*/

		}
	}
    public IndexWriter getIndexWriter() throws Exception {
    	//先指定索引库存放的位置
    	String dumpPath = "C:\\Users\\Administrator\\Desktop\\lucene\\index";
    	Directory directory = FSDirectory.open(new File(dumpPath));
    	//索引库存放在内存中
    	//Directory directory = new RAMDirectory();
    	//指定分析器
    	/*StandardAnalyzer standardAnalyzer = new StandardAnalyzer();*/
    	Analyzer analyzer  = new IKAnalyzer();
    	IndexWriterConfig config = new IndexWriterConfig(Version.LATEST, analyzer);
    	//创建Indexwriter对象
    	return new IndexWriter(directory, config);
    }
	@Test
	public void test() throws Exception {
	IndexWriter indexWriter = getIndexWriter();
	
	//读取歌词并创建Document对象
	File musicDir = new File("C:\\Users\\Administrator\\Desktop\\so");
	for (File f:musicDir.listFiles()) {
		//判断是否是文件
		if (f.isFile()) {
			//创建Document对象
			Document document = new Document();
			//创建域
			//文件名称
			Field fieldName = new TextField("filename", f.getName(), Store.YES);
			//文件内容
			String contentString = FileUtils.readFileToString(f);
			Field fieldContent = new TextField("content", contentString, Store.YES);
			//文件路径
			Field fieldPath = new StoredField("path", f.getPath());
			//文件 的大小
			Field fieldSize = new LongField("size", FileUtils.sizeOf(f), Store.YES);
			//把域添加到Document中
			document.add(fieldName);
			document.add(fieldContent);
			document.add(fieldPath);
			document.add(fieldSize);
			//把Document写入索引库
			indexWriter.addDocument(document);
		}
	}
	
	//关闭indexwriter
	indexWriter.close();
				
	}
	@Test
	public void search() throws Exception {
		IndexSearcher indexSearcher = getIndexSearcher();
		Query query = new TermQuery(new Term("filename", "love"));
		print(indexSearcher, query);
	}
	@Test
	public void analyzer() throws IOException {
		//创建分析器
				/*Analyzer analyzer = new StandardAnalyzer();*/
		        Analyzer analyzer = new IKAnalyzer();
				//得到TokenStream
				TokenStream tokenStream = analyzer.tokenStream("content", new StringReader("我是中国人,我爱学中文白富美"));
				//设置tokenStream初始状态，否则会抛异常
				tokenStream.reset();
				//设置要获取分词的偏移量
				OffsetAttribute offsetAttribute = tokenStream.addAttribute(OffsetAttribute.class);
				//设置要获取分词的项
				CharTermAttribute charTermAttribute = tokenStream.addAttribute(CharTermAttribute.class);
				while(tokenStream.incrementToken()){
					System.out.println("-----------------");
					//起始偏移量
					System.out.print("-->"+offsetAttribute.startOffset());
					//截止偏移量
					System.out.print("-->"+offsetAttribute.endOffset());
					//分词项的值
					System.out.println("-->"+new String(charTermAttribute.toString()));
					
				}

	}
	@Test
	public void delAll() throws Exception {
		IndexWriter indexWriter = getIndexWriter();
		indexWriter.deleteAll();
		indexWriter.close();
	}
	@Test
	public void del() throws Exception {
		IndexWriter indexWriter = getIndexWriter();
		Query query = new TermQuery(new Term("filename", "love"));
		indexWriter.deleteDocuments(query);
		indexWriter.close();
	}
	@Test
	public void update() throws Exception {
		IndexWriter indexWriter = getIndexWriter();
		Document document = new Document();
		document.add(new TextField("fileN", "小熊啊", Store.YES));
		document.add(new TextField("fileC", "美咩的", Store.YES));
		indexWriter.updateDocument(new Term("filename", "love"), document, new IKAnalyzer());
		indexWriter.close();
	}
	@Test
	public void  searchMatchAllDocsQuery() throws Exception {
		IndexSearcher indexSearcher = getIndexSearcher();
		Query query = new MatchAllDocsQuery();
		print(indexSearcher, query);	
		indexSearcher.getIndexReader().close();
	}
	@Test
	public void searchNumericRangeQuery() throws Exception{
		IndexSearcher indexSearcher = getIndexSearcher();
		Query query = NumericRangeQuery.newLongRange("size", 1000L, 8000L, true, true);
		print(indexSearcher, query);	
		indexSearcher.getIndexReader().close();
	}
	@Test
    public void searchBooleanQuery() throws Exception {
		IndexSearcher indexSearcher = getIndexSearcher();
        BooleanQuery booleanQuery  = new BooleanQuery();
        Query query1 = new TermQuery(new Term("filename", "love"));
        Query query2 = new TermQuery(new Term("filename", "you"));
        booleanQuery.add(query1, Occur.MUST);
        booleanQuery.add(query2, Occur.SHOULD);
		print(indexSearcher, booleanQuery);	
		indexSearcher.getIndexReader().close();
	}
	@Test
	public void searchQueryParser() throws Exception {
		IndexSearcher indexSearcher = getIndexSearcher();
		QueryParser queryParser = new QueryParser("filename", new IKAnalyzer());
		/*Query query = queryParser.parse("*:*");*/
		Query query = queryParser.parse("content:love");
		print(indexSearcher, query);	
		indexSearcher.getIndexReader().close();
	}
	@Test
	public void searchMultiFieldQueryParser() throws Exception {
		IndexSearcher indexSearcher = getIndexSearcher();
		String[] fields = {"filename","content"};
		QueryParser queryParser = new MultiFieldQueryParser(fields, new IKAnalyzer()); 
		Query query = queryParser.parse("love");
		print(indexSearcher, query);	
		indexSearcher.getIndexReader().close();
	}

}

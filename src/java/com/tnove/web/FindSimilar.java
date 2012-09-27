package com.tnove.web;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriter.MaxFieldLength;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import com.tnove.image.process.ColorUtils;

/**
 * Servlet implementation class FindSimilar
 */
public class FindSimilar extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private static IndexWriter writer;
	
	private static String path = "D:\\test\\index";

	private final static String Fields[] = { "f1", "f2", "f3", "f4" };

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public FindSimilar() {
		super();
	}

	static{
		
		try {
			Directory dirctory = FSDirectory.open(new File(path));
			writer = new IndexWriter(dirctory, new StandardAnalyzer(Version.LUCENE_36), MaxFieldLength.LIMITED);
			indexFile();
			writer.optimize();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void destroy(){
		
		try {
			writer.optimize();
			writer.close();
			System.out.println("Servlet Shut Down.");
		} catch (CorruptIndexException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void indexFile() throws IOException{
		
		String path = "D:\\workspace\\jee\\similar-image\\web\\image";
		File files = new File(path);
		FilenameFilter fiter =  new SuffixFileFilter(new String[]{"jpg","bmp","png","gif","jpeg"},IOCase.INSENSITIVE);
		String[] fileList = files.list(fiter);
		
		for(String fPath : fileList){
			
			File file = new File(path,fPath);
			int[] encodes = null;
			try {
				System.out.println(file.getName());
				encodes = ColorUtils.getImageEncode(file);
				
			} catch (Exception e) {
				continue;
			}
			
			Document doc = new Document();
			int i = 0;
			
			for(int encode : encodes){
				Field f1 = new Field(Fields[i++], "" + encode, Store.YES, Index.ANALYZED);
				doc.add(f1);
			}
			
			Field nameField = new Field("url", "image/"+file.getName(), Store.YES, Index.ANALYZED);
			doc.add(nameField);
			writer.addDocument(doc);
		}
		
	}

	
	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		String type = request.getParameter("type");
		TopDocs retDocs = null;
		int[] docIDs = null;
		if (type.equals("url")) {
			String url = request.getParameter("url");
			retDocs = findByURL(url, response);
		} else if (type.equals("find")){
			BooleanQuery bq = new BooleanQuery();
			for(int i = 0; i < 4; i++){
				String field = request.getParameter(Fields[i]);
				if(field != null){
					TermQuery t1 = new TermQuery(new Term(Fields[i], field));
					bq.add(t1, Occur.SHOULD);
				}
			}
			IndexSearcher searcher = new IndexSearcher(writer.getReader());
			retDocs = searcher.search(bq, 100);
		}else{
			int maxDoc = writer.maxDoc();
			docIDs = new int[maxDoc];
			for(int i = 0; i < maxDoc; i++){
				docIDs[i] = i;
			}
		}
		
		if(retDocs != null) docIDs = getDocID(retDocs);
		
		writeFineds(response, docIDs);
	}
	
	private int[] getDocID(TopDocs retDocs ){
		
		int[] docIDs = new int[retDocs.totalHits];
		for(int i = 0; i< retDocs.totalHits; i++){
			docIDs[i] = retDocs.scoreDocs[i].doc;
		}
		
		return docIDs;
	}

	private TopDocs findByURL(String url, HttpServletResponse response) {

		TopDocs retDocs = null;
		try {
			TermQuery urlTerm = new TermQuery(new Term("url", url));
			Searcher searcher = new IndexSearcher(writer.getReader());
			TopDocs docs = searcher.search(urlTerm, 1);
			BooleanQuery bq = new BooleanQuery();
			if (docs.totalHits == 0) {
				int[] encodes = index(url);
				for (int i = 0; i < 4; i++) {
					TermQuery t1 = new TermQuery(new Term(Fields[i], "" + encodes[i]));
					bq.add(t1, Occur.SHOULD);
				}
			} else {
				Document doc = writer.getReader().document(docs.scoreDocs[0].doc);
				for (int i = 0; i < 4; i++) {
					TermQuery t1 = new TermQuery(new Term(Fields[i], doc.get(Fields[i])));
					bq.add(t1, Occur.SHOULD);
				}
			}
			searcher = new IndexSearcher(writer.getReader());
			retDocs = searcher.search(bq, 100);
		} catch (Exception e) {
			// TODO: handle exception
		}
		
		return retDocs;
	}
	
	private void writeFineds(HttpServletResponse response, int[] retDocs) throws IOException {

		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		out.println("<html>");
		out.println("<head><title> Similar Picture </title></head><body>");
		out.println("<a href=\"similar?type=browse\" > Return to Browse</a> <br/>" +
				"<P> Find Similar Image By URL </P> <form method=\"POST\" action=\"/similar-image/similar?type=url\"><input type=\"text\" name=\"url\" value=\"http://img1.gtimg.com/cd/pics/hv1/97/207/630/41018632.jpg\"><input type=\"submit\" value=\"Find Similar\" /></form>");
		out.println("<TABLE>");
		StringBuilder tr1 = new StringBuilder();
		StringBuilder tr2 = new StringBuilder();
		tr1.append("<tr>");
		tr2.append("<tr>");
		for (int i = 0; i < retDocs.length; i++) {
			
			if((i+1) %5 == 0 ){
				tr1.append("</tr>");
				tr2.append("</tr>");
				out.println(tr1.toString()+tr2.toString());
				tr1.setLength(0);
				tr2.setLength(0);
				tr1.append("<tr>");
				tr2.append("<tr>");
			}
			
			Document doc = writer.getReader().document(retDocs[i]);
			if(doc == null) continue;
			String url = doc.get("url");
			tr1.append("<td><img src=\"" +url + "\" width=\"200px\" height=\"200px\" ></td>");
			tr2.append("<td><a href=\"similar?type=find");
			for(int j = 0; j< 4; j++){
				tr2.append("&").append(Fields[j]).append("=").append(doc.get(Fields[j]));
			}
			tr2.append("\"> Find Similar</a></td>");
		}
		
		tr1.append("</tr>");
		tr2.append("</tr>");
		out.println(tr1.toString()+tr2.toString());
		tr1.setLength(0);
		tr2.setLength(0);
		out.println("</TABLE></body></html>");

	}

	private static int[] index(String urlStr) {

		int[] encodes = new int[4];
		try {
			
			URL url = new URL(urlStr);
			BufferedImage bi = ImageIO.read(url);
			int[] mid = ColorUtils.getImageCenter(bi);
			int[][] stats = ColorUtils.calculateColor(bi, mid);
			encodes = ColorUtils.getEncodeInLog(stats, 4);

			Document doc = new Document();
			int i = 0;
			for (int encode : encodes) {
				Field f1 = new Field(Fields[i++], "" + encode, Store.YES, Index.ANALYZED);
				doc.add(f1);
			}

			Field urlField = new Field("url", urlStr, Store.YES, Index.NOT_ANALYZED);
			doc.add(urlField);
			writer.addDocument(doc);
			writer.optimize();
			writer.commit();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return encodes;
	}
	
	public static void main(String[] args) throws IOException {
		
//		Directory dirctory = FSDirectory.open(new File(path));
//		writer = new IndexWriter(dirctory,new StandardAnalyzer(Version.LUCENE_CURRENT), true, MaxFieldLength.LIMITED);
//		indexFile();
//		writer.optimize();
//		writer.commit();
//		writer.close();
		
	}

}

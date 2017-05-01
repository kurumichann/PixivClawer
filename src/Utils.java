import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class Utils {
	
	private byte[] cache = new byte[2048];
	private String path;
	private int download_cnt = 0;
	private int len;
	private final int MAX_RETRY_CNT = 10;
	/*
	 * param ArrayList<Pic> pic_lst
	 * param String useragent
	 * param String COOKIES
	 * param int retry_countt
	 */
	public void downloadPics( ArrayList<Pic> pic_lst, String path, String useragent, String cookies ){
		
		for( Pic picture : pic_lst ){
			try {
				
				if( picture.getType() == 2 ){
					continue;
				}
				URL url = new URL( picture.getDetail_page_url() );
				URLConnection connetcion = url.openConnection();
				connetcion.setConnectTimeout(10000);
				connetcion.setReadTimeout(30000);
				connetcion.setRequestProperty("User-Agent", useragent);
				connetcion.setRequestProperty("COOKIES", cookies);
				connetcion.setRequestProperty("REFERER", picture.getIllus_url());
				connetcion.connect();
				
				download( connetcion, path+picture.getDetail_page_url().replaceAll("/", ""), picture.getPic_nam(), 0);				
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			download_cnt++;
			System.out.println("picture "+picture.getPic_nam()+" has been downloaded. totoal: "+download_cnt);
		}
		
	}
	
	/*
	 * Download manga
	 * param picture
	 * param useragent
	 * param cookies
	 */
	public void downloadanga( Pic picture, String useragnet, String cookies, int retry_cnt ){
		
		Connection con = Jsoup.connect(picture.getDetail_page_url()).userAgent(useragnet).cookie("Cookie", cookies);
		ArrayList<String> imgs = new ArrayList<String>(); 
		try {
			 Document doc = con.get();
			 for( Element element : doc.getElementsByClass("item-container") ){
				 imgs.add(element.getElementsByTag("img").get(0).attr("src"));
			 }
			 for( String img : imgs ){
				 URL url = new URL(img);
				 URLConnection connetcion = url.openConnection();
				 connetcion.setConnectTimeout(10000);
				 connetcion.setReadTimeout(30000);
				 connetcion.setRequestProperty("User-Agent", useragent);
				 connetcion.setRequestProperty("COOKIES", cookies);
				 connetcion.setRequestProperty("REFERER", picture.getIllus_url());
				 connetcion.connect();
			 }
		} catch (IOException e) {
			 if( retry_cnt < MAX_RETRY_CNT){
				 System.out.println("connect time out.....retry_cnt:"+retry_cnt);
				 downloadanga(picture, useragnet, cookies, retry_cnt+1);
			}
			
		}
	}
	
	/*
	 * download
	 * param con
	 * param path
	 * param filename
	 * param rety_cnt
	 */
	public void download( URLConnection con, String path, String file_nam, int retry_cnt){
		InputStream in;
		try {
			in = con.getInputStream();
			if( isExist(path) ){
				return;
			}
			OutputStream os = new FileOutputStream(path);
			System.out.println("picture "+file_nam+" is downloading");
			while ((len = in.read(cache)) != -1) {
				os.write(cache, 0, len);
			}
			os.close();
			in.close();
		} catch (FileNotFoundException e) {
			System.out.println( "无法创建文件" );
		} catch (IOException e) {
			if( retry_cnt < MAX_RETRY_CNT){
				System.out.println("connect time out.....retry_cnt:"+retry_cnt);
				download( con, path, file_nam, retry_cnt+1);
			}
			return;
		}
	}
	
	/*
	 * param path
	 */
	public boolean isExist( String path ){
		return new File(path).exists();
	}
}

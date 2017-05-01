import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class PixivClawer {
	
	private String content;
	private int rating_count;
	private Document doc;
	private int bookmark_count_net;
	private Pic pic_temp;
	private ArrayList<Pic> id_bookmark_lst = new ArrayList<Pic>();
	private static ArrayList<Pic> pic_lst = new ArrayList<Pic>(); 
	private Pattern bookmark_count_pattern = Pattern.compile("></i>\\d*</a></li>") ;
	Utils util = new Utils();
	
	private static String COOKIES;
	private static String USERAGENT;
	private static final int MAX_RETRY_CNT = 10;
	private static String URL_TEMPLATE = "https://www.pixiv.net/search.php?word=$key&order=date_d&p=$page";
	private static String ILLUS_ID_URL_TEMPLATE = "https://www.pixiv.net/member_illust.php?mode=medium&illust_id=$id";
	
	/*
	 * param url
	 * param cookies
	 */
	public String getContent(String url, String cookies) throws IOException{
		Connection con = Jsoup.connect(url).userAgent(USERAGENT).cookie("Cookie", cookies);
		content = con.get().html();
		return content;
	}
	
	/*
	 * param key
	 * param rating_count
	 * param bookmark_count
	 * param page
	 * param path
	 * 
	 */
	public void shebao(String key, int rating_count, int bookmark_count, int page, String path) {
		System.out.println(pic_lst);
		id_bookmark_lst.clear();
		String url = URL_TEMPLATE.replace("$key", key).replace("$page", String.valueOf(page));
		
		if( getPicId(url, bookmark_count, 0) == null){
			shebao(key, rating_count, bookmark_count, page+1, path);
		}
		
		for( Pic picture : id_bookmark_lst ){
			getIllusPage(picture, url, 0);
		}
		util.downloadPics(pic_lst, USERAGENT, COOKIES, path);
		if( !isLastPage(url, 0) ){
			shebao(key, rating_count, bookmark_count, page+1, path);
		}
		else{
			return;
		}
	}
	
	/*
	 * param doc
	 * param retry_cnt
	 * return boolean rs
	 */
	 public boolean isLastPage(String url, int retry_cnt){
		 try {
			doc = Jsoup.connect(url).userAgent(USERAGENT).cookie("Cookie", COOKIES).get();
		} catch (IOException e) {
			if( retry_cnt != MAX_RETRY_CNT ){
				System.out.println("connect time out.....retry_cnt:"+retry_cnt);
				isLastPage(url, retry_cnt+1);
			}
			return true;
		}
		 return doc.getElementsByClass("_no-item").size() > 0;
	 }
	 
	 /*
	  * param url
	  * param bookmark_count
	  * param retry_cnt
	  * return ArrayList<String> pic_id_lst
	  */
	 public ArrayList<Pic> getPicId(String url, int bookmark_count, int retry_cnt)  {
		   
	    Elements elements;
	    
		try {
			elements = Jsoup.connect(url).userAgent(USERAGENT).cookie("Cookie", COOKIES).get().getElementsByClass("image-item");
			for(Element element : elements){
				 Matcher match = bookmark_count_pattern.matcher(element.html());
				 if( match.find() ){
					 bookmark_count_net = Integer.valueOf(match.group().substring(5, match.group().length()-9));
				 }else{
					 bookmark_count_net = 0;
				 }
			     if( bookmark_count_net  >= bookmark_count ) {
			    	pic_temp = new Pic();
			    	pic_temp.setPixiv_id(element.getElementsByTag("img").get(0).attr("data-id"));
			    	pic_temp.setBookmark_count(bookmark_count_net);
				    id_bookmark_lst.add(pic_temp);
			    }
		       }
		} catch (IOException e) {
			System.out.println("connect time out.....retry_cnt:"+retry_cnt);
			if( retry_cnt != MAX_RETRY_CNT ){
				getPicId(url, rating_count, retry_cnt+1);
			}else{
				return null;
			}
		}
       
         return id_bookmark_lst;
	 }
	 
	 /*
	  * param Pic id_bookmark
	  * param String pre_url
	  * param int retry_cnt
	  * return Pic pic
	  */
	 public Pic getIllusPage(Pic id_bookmark, String pre_url, int retry_cnt){
		 String url = ILLUS_ID_URL_TEMPLATE.replace("$id", id_bookmark.getPixiv_id());
		 Pic pic = new Pic();
		 Connection con = Jsoup.connect(url).userAgent(USERAGENT).cookie("Cookie", COOKIES).header("REFERER", pre_url);
		 try {
			Document doc = con.get();
			pic.setRating_count(Integer.valueOf(doc.getElementsByClass("rated-count").get(0).ownText()));
			pic.setAuthor_nam(doc.getElementsByClass("user").get(0).ownText());
			pic.setIllus_url(url);
			pic.setPixiv_id(id_bookmark.getPixiv_id());
			pic.setBookmark_count(id_bookmark.getBookmark_count());
			pic.setPic_nam(doc.getElementsByClass("work-info").get(0).getElementsByClass("title").get(0).ownText());
			
			//to make sure this works belong to which category
			switch( checkType(doc) ){
				case 1:	
					pic.setDetail_page_url(doc.getElementsByClass("original-image").get(0).attr("data-src"));
					pic.setType(1);
					break;
				case 2:
					pic.setDetail_page_url(doc.getElementsByClass("works_display").get(0).getElementsByTag("a").get(0).attr("href"));
					pic.setType(2);
					break;
				case 3:
					pic.setDetail_page_url(doc.getElementsByClass("works_display").get(0).getElementsByTag("a").get(0).attr("href"));
					pic.setType(3);
					break;
				default:
					pic.setDetail_page_url(null);
					pic.setType(-1);
					break;
					
			}

			System.out.println(pic);
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("connect time out.....retry_cnt:"+retry_cnt);
			if( retry_cnt != MAX_RETRY_CNT ){
				getIllusPage(id_bookmark, pre_url, retry_cnt+1);
			}else{
				return null;
			}
		}
		 pic_lst.add(pic);
		 return pic;
	 }
	 
	 /*
	  * param  doc
	  * return int category
	  */
	 public int checkType(Document doc){
		 if( doc.getElementsByClass("original-image" ).size() > 0 ){
			 
			 return 1;
		 }
		 else if( doc.html().contains("member_illust.php?mode=manga") ){
			 
			 return 2;
		 }
		 else if( doc.html().contains("member_illust.php?mode=big") ){
			 
			 return 3;
		 }
		 return -1;
		 
	 }
	 public static void main(String[] args) {
	    System.setProperty("http.proxySet", "true"); 
		System.setProperty("http.proxyHost", "127.0.0.1"); 
		System.setProperty("http.proxyPort", "1080");
		PixivClawer clawer = new PixivClawer();
		String path = "/Users/zhongbingyi/Downloads/pixiv_download/";
		COOKIES = "p_ab_id=4; p_ab_id_2=4; login_ever=yes; special_notification_rating=1; _ga=GA1.2.591910243.1492707948; device_token=90f08017d236469c8338859ccf0e2a67; module_orders_mypage=%5B%7B%22name%22%3A%22recommended_illusts%22%2C%22visible%22%3Atrue%7D%2C%7B%22name%22%3A%22everyone_new_illusts%22%2C%22visible%22%3Atrue%7D%2C%7B%22name%22%3A%22following_new_illusts%22%2C%22visible%22%3Atrue%7D%2C%7B%22name%22%3A%22mypixiv_new_illusts%22%2C%22visible%22%3Atrue%7D%2C%7B%22name%22%3A%22fanbox%22%2C%22visible%22%3Atrue%7D%2C%7B%22name%22%3A%22featured_tags%22%2C%22visible%22%3Atrue%7D%2C%7B%22name%22%3A%22contests%22%2C%22visible%22%3Atrue%7D%2C%7B%22name%22%3A%22sensei_courses%22%2C%22visible%22%3Atrue%7D%2C%7B%22name%22%3A%22spotlight%22%2C%22visible%22%3Atrue%7D%2C%7B%22name%22%3A%22booth_follow_items%22%2C%22visible%22%3Atrue%7D%5D; a_type=0; PHPSESSID=4062396_3134c2e5a80f81978db1c7fd99f457ca; __utma=235335808.591910243.1492707948.1492940573.1492955879.9; __utmb=235335808.3.10.1492955879; __utmc=235335808; __utmz=235335808.1492844167.2.2.utmcsr=baidu|utmccn=(organic)|utmcmd=organic; __utmv=235335808.|2=login%20ever=yes=1^3=plan=normal=1^5=gender=male=1^6=user_id=4062396=1^9=p_ab_id=4=1^10=p_ab_id_2=4=1";
		USERAGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/57.0.2987.133 Safari/537.36";
		clawer.shebao("kongo", -1, -1, 1, path);
		
	}
}

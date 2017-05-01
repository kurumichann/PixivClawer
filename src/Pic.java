
public class Pic {
	
	private String author_nam;
	private String pixiv_id;
	private String pic_nam;
	private String illus_url;
	private int rating_count;
	//1 for orginal pic,2 for manga，3 for big pic，-1 for unkonwn
	private int type;
	private int bookmark_count;
	private String detail_page_url;
	
	
	public int getBookmark_count() {
		return bookmark_count;
	}
	public void setBookmark_count(int bookmark_count) {
		this.bookmark_count = bookmark_count;
	}
	public String getDetail_page_url() {
		return detail_page_url;
	}
	public void setDetail_page_url(String detail_page_url) {
		if( detail_page_url.contains("https")){
			this.detail_page_url = detail_page_url;
		}else{
			this.detail_page_url = "https://www.pixiv.net/"+detail_page_url;
		}
		
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public String getAuthor_nam() {
		return author_nam;
	}
	public void setAuthor_nam(String author_nam) {
		this.author_nam = author_nam;
	}
	public String getPixiv_id() {
		return pixiv_id;
	}
	public void setPixiv_id(String pixiv_id) {
		this.pixiv_id = pixiv_id;
	}
	public String getPic_nam() {
		return pic_nam;
	}
	public void setPic_nam(String pic_nam) {
		this.pic_nam = pic_nam;
	}
	public String getIllus_url() {
		return illus_url;
	}
	public void setIllus_url(String illus_url) {
		this.illus_url = illus_url;
	}
	public int getRating_count() {
		return rating_count;
	}
	public void setRating_count(int rating_count) {
		this.rating_count = rating_count;
	}
	public String toString(){
		return "author_nam: "+this.getAuthor_nam()+";pixiv_id: "+this.getPixiv_id()+"; rating_count: "+this.getRating_count()+
				"; bookmark_count: "+this.getBookmark_count()+"; pic_nam: "+this.getPic_nam()+"; type: "+this.getType()+"; illus_url: "+this.getIllus_url()+"; detial_page_url: "+this.getDetail_page_url();
	}
	
}

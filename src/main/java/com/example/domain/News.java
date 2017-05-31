package com.example.domain;

public class News {
	private String source;
	private String title;
	private String content;
	private String keyWord;
	private String pageTitle;
	private String introduction;
	private String date;
	public News(String source,String title,String content,String keyWord,String papgeTitle,String introduction,String date){
		this.source=source;
		this.title=title;
		this.content=content;
		this.keyWord=keyWord;
		this.pageTitle=papgeTitle;
		this.introduction=introduction;
		this.date=date;
	}
	public String getSource() {
		return source;
	}
	public void setSource(String source) {
		this.source = source;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getKeyWord() {
		return keyWord;
	}
	public void setKeyWord(String keyWord) {
		this.keyWord = keyWord;
	}
	public String getPageTitle() {
		return pageTitle;
	}
	public void setPageTitle(String pageTitle) {
		this.pageTitle = pageTitle;
	}
	public String getIntroduction() {
		return introduction;
	}
	public void setIntroduction(String introduction) {
		this.introduction = introduction;
	}
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
}

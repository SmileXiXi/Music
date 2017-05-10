package com.example.mymusicplayer.localmusic;

import java.io.Serializable;

public class Music implements Serializable {
	private String filename;
	private String Title;
	private int   duration;
	private	String artist;
	private String location;
	private int ID;

	public void setID(int ID) {this.ID = ID;}
	public int getID() {return ID;}
	public String getFilename() {
		return filename;
	}
	public void setFilename(String filename) {
		this.filename = filename;
	}
	public String getTitle() {
		return Title;
	}
	public void setTitle(String title) {
		Title = title;
	}
	public int getDuration() {
		return duration;
	}
	public void setDuration(int duration) {
		this.duration = duration;
	}
	public String getArtist() {
		return artist;
	}
	public void setArtist(String artist) {
		this.artist = artist;
	}
	public String getData() {
		return location;
	}
	public void setData(String location) {
		this.location = location;
	}

}

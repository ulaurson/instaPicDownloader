package instagramPicDownloader;

import java.util.Date;

public class User {

	String username;
	Date latestDate = new Date(1900);

	public User(String username){
		this.username = username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void setLatestDate(Date latestDate) {
		this.latestDate = latestDate;
	}
}

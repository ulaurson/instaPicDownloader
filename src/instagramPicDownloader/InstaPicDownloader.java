package instagramPicDownloader;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class InstaPicDownloader {

	public static void main(String[] args) throws IOException {
		
		
		while(true){
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			System.out.print("Enter username: ");
			String username = "";
			try {
				username = br.readLine();
				User userObject = new User(username);
				
				//Runs the downloadPictures function in separate thread with given username
				Thread thread = new Thread(new Runnable() {
				     public void run() {
				    	 try {
							downloadPictures(userObject);
						} catch (IOException e) {
							e.printStackTrace();
						}
				     }
				}); 
				thread.start();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}
	
	public static void downloadPictures (User userObject) throws IOException{
		
		String username = userObject.username;
		
		//Will make a directory with username as the name. 
		//All user pictures will be downloaded into this folder.
		File userDir = new File(username);
		userDir.mkdir();
		
		boolean hasNextPage = true;
		String max_id = "";
		Date latestDate = userObject.latestDate;
		
		while(hasNextPage){
			String urlAddress = "";
			boolean hasNewPictures = true;
			
			if(max_id.equals("")){
				urlAddress = "https://instagram.com/" + username;
			}else{
				urlAddress = "https://instagram.com/" + username + "/?max_id=" + max_id;
			}
			
			//Connects with user instagram account
			URL url = new URL(urlAddress);
			URLConnection connection = url.openConnection();
			InputStream response = connection.getInputStream();
			InputStreamReader in = new InputStreamReader(response);
			
			//Separates from inputStream the user data
			String result = new BufferedReader(in).lines().collect(Collectors.joining(""));
			String userData = result.split("window._sharedData = ")[1].split(";</script>")[0];
			
			try {
				
				//Converts the data to JSON object 
				JSONObject entry_data = new JSONObject(userData).getJSONObject("entry_data");
				
				//Will get from the JSON the part where are user picture data
				JSONObject profilePage = (JSONObject)entry_data.getJSONArray("ProfilePage").get(0);
				JSONObject user = profilePage.getJSONObject("user");
				JSONObject media = user.getJSONObject("media");
				JSONArray nodes = media.getJSONArray("nodes");
			
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
				
				//Process every picture data one by one
				for(int i = 0; i < nodes.length();i++){
					JSONObject node  = (JSONObject)nodes.get(i);
					max_id = node.getString("id");
					if(!node.getBoolean("is_video")){
						long value  = node.getInt("date");
						Date date = new Date(value*1000L);
						String formattedDate = sdf.format(date);
						
						//Checks if there are some new pictures
						if(userObject.latestDate.before(date)){
							if(date.after(latestDate)){
								latestDate = date;
							}
							
							//Downloads the picture
							URL imageURL = new URL(node.getString("display_src"));
							BufferedImage image = ImageIO.read(imageURL);
							File outputfile = new File(username + "/" + formattedDate + ".png");
						    ImageIO.write(image, "png", outputfile);
						} else {
							hasNewPictures = false;
							break;
						}
					}
				}
				
				if(hasNewPictures){
					hasNextPage = media.getJSONObject("page_info").getBoolean("has_next_page");
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
			//If there are no new pictures to download then thread sleeps 1 second
			if(!hasNewPictures || !hasNextPage){
				userObject.setLatestDate(latestDate);
				urlAddress = "";
				hasNextPage = true;
				max_id = "";
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
}

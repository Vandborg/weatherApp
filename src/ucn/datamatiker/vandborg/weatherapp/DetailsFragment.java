package ucn.datamatiker.vandborg.weatherapp;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.TimeUnit;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class DetailsFragment extends Fragment {
	
	private static final String TAG = "WEATHER";
	private Boolean stopThread = false;

	private View view = getView();
	private Handler handler = new Handler();

	private String time = "";
	private String description = "";
	private String temp = "";
	private String wind = "";
	private Drawable image = null;

	private Thread t = new Thread(new Runnable() {
		public void run() {
			while (!stopThread) {
				try {
					refreshWeather();
					Thread.currentThread().sleep(TimeUnit.HOURS.toMillis(1));
				} catch (InterruptedException e) {
					Log.d(TAG, "InterruptedException");
				}
			}
		}
	});

	public void refreshWeather(){

		// Getting the xml
		URL url;
		try {
			String weatherFeed = getString(R.string.weather_feed);
			url = new URL(weatherFeed + "GetWeather?loc=60077");
		  
			URLConnection connection;
			connection = url.openConnection();
		  
			HttpURLConnection httpConnection = (HttpURLConnection) connection;
			int responseCode = httpConnection.getResponseCode();
		 
			if (responseCode == HttpURLConnection.HTTP_OK) {
				InputStream in = httpConnection.getInputStream();
		  
				DocumentBuilderFactory dbf = DocumentBuilderFactory
						.newInstance();
				DocumentBuilder db = dbf.newDocumentBuilder();
		  
				// Parse the weather feed
				Document dom = db.parse(in);
				Element docEle = dom.getDocumentElement();

				NodeList weatherData = docEle.getChildNodes();

				for (int i = 0; i < weatherData.getLength(); i++) {
					switch (i) {
						case 5: //time
							time = weatherData.item(i).getTextContent();
							break;
						case 7: //description
							description = weatherData.item(i).getTextContent();
							break;
						case 9: //temp
							temp = weatherData.item(i).getTextContent();
							break;
						case 13: //Wind direction
							wind = weatherData.item(i).getTextContent();
							break;
						case 15: // Wind speed
							wind += " " + weatherData.item(i).getTextContent();
							break;
						case 19: // IconURL
							String iconURL = getString(R.string.image_base_URL)
									+ weatherData.item(i).getTextContent();
							Log.d(TAG, iconURL);
							InputStream is = (InputStream) new URL(iconURL).getContent();
							image = Drawable.createFromStream(is, "weather icon");
							break;
						default:
							break;
					}
				}

				handler.post(new Runnable() {
					public void run() {
						updateUI();
					}
				});
			}
		  
		} catch (MalformedURLException e) {
			Log.d(TAG, "MalformedURLException");
		} catch (IOException e) {
			Log.d(TAG, "IOException" + e.toString());
		} catch (ParserConfigurationException e) {
			Log.d(TAG, "Parser Configuration Exception");
		} catch (SAXException e) {
			Log.d(TAG, "SAX Exception");
		} finally {

		}
		 
	}

	private void updateUI() {
		TextView timeTextView = (TextView) view.findViewById(R.id.time);
		TextView descriptionTextView = (TextView) view.findViewById(R.id.description);
		TextView tempTextView = (TextView) view.findViewById(R.id.temp);
		TextView windTextView = (TextView) view.findViewById(R.id.wind);
		ImageView imageView = (ImageView) view.findViewById(R.id.image);
		timeTextView.setText(time);
		descriptionTextView.setText(description);
		tempTextView.setText(temp + " Celsius");
		windTextView.setText(wind + " km/h");
		imageView.setImageDrawable(image);
		view.invalidate();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View v = inflater.inflate(R.layout.details_fragment, container, false);
		view = v;

		stopThread = false;
		t.start();

		return v;
	}

	@Override
	public void onDestroyView() {

		super.onDestroyView();
		stopThread = true;
	}
	
}

package ucn.datamatiker.vandborg.weatherapp;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class DetailsFragment extends Fragment {
	
	private static final String TAG = "WEATHER";

	private View view = getView();
	private Handler handler = new Handler();

	private String time = "";
	private String description = "";
	private String temp = "";
	private String wind = "";

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
							//timeTextView.setText(time);
							break;
						case 7: //description
							description = weatherData.item(i).getTextContent();
							//descriptionTextView.setText(description);
							break;
						case 9: //temp
							temp = weatherData.item(i).getTextContent();
							//tempTextView.setText(temp);
							break;
						case 13: //Wind direction
							wind = weatherData.item(i).getTextContent();
							//windTextView.setText(wind);
							break;
						case 15: // Wind speed
							wind += " " + weatherData.item(i).getTextContent();
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

				Log.d(TAG, docEle.getChildNodes().item(1).getNodeName());
				Log.d(TAG, docEle.getChildNodes().item(1).getTextContent());

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
		timeTextView.setText(time);
		descriptionTextView.setText(description);
		tempTextView.setText(temp + " Celsius");
		windTextView.setText(wind + " km/h");
		view.invalidate();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		//refreshWeather();

		View v = inflater.inflate(R.layout.details_fragment, container, false);
		view = v;

		Thread t = new Thread(new Runnable() {
			public void run() {
				refreshWeather();
			}
		});

		t.start();

		return v;
	}
	
}

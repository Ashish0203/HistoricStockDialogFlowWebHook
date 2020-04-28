package stock.price.realTimeStockPrice;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.github.wnameless.json.flattener.JsonFlattener;

@RestController
public class FetchStockPrice {


	@PostMapping(value = "/getprice", produces = MediaType.APPLICATION_JSON_VALUE)
	public String getPriceFromSever(@RequestBody String payload) {
		// String flattenedJson = JsonFlattener.flatten(payload);
		Map<String, Object> flattenedJsonMap = JsonFlattener.flattenAsMap(payload);
		// System.out.println(flattenedJsonMap);
		// System.out.println(flattenedJson);
		System.out.println();
		String companyName = "a";
		String priceType = "b";
		String date = "c";

		for (Entry<String, Object> entry : flattenedJsonMap.entrySet()) {
			if (entry.getKey().equals("queryResult.parameters.company_name")) {
				System.out.println("Hello There");
				companyName = entry.getValue().toString();
			}
			if (entry.getKey().equals("queryResult.parameters.price_type")) {
				priceType = entry.getValue().toString();
			}
			if (entry.getKey().equals("queryResult.parameters.date")) {
				date = entry.getValue().toString();
			}

		}

		String ticker = getTicker(companyName);
		String copyPriceType = priceType;
		priceType = getPriceType(priceType);
		date = getDate(date);

		String response = "";
		String value="0";
		try {
			value = fetchData(ticker, priceType, date);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String chat = "The " + copyPriceType + " value of " + companyName + " on " + date + " was $" + value;
		
		if(value.equals("0")) {
			chat = "The date is either a future date or it was a weekend or a public holiday.";
		}
		response = "	{\r\n" + 
				"	\"fulfillmentMessages\": [{\r\n" + 
				"		\"text\": {\r\n" + 
				"			\"text\": [\""+chat+"\"]\r\n" + 
				"		}\r\n" + 
				"	},\r\n" + 
				"	{\r\n" + 
				"  \"platform\": \"google\",\r\n" + 
				"  \"type\": \"custom_payload\",\r\n" + 
				"  \"payload\": {\r\n" + 
				"    \"google\": {\r\n" + 
				"      \"expectUserResponse\": true,\r\n" + 
				"      \"isSsml\": false,\r\n" + 
				"      \"noInputPrompts\": [],\r\n" + 
				"      \"richResponse\": {\r\n" + 
				"        \"items\": [\r\n" + 
				"          {\r\n" + 
				"            \"simpleResponse\": {\r\n" + 
				"              \"displayText\": \""+chat+"\",\r\n" + 
				"              \"textToSpeech\": \""+chat+"\"\r\n" + 
				"            }\r\n" + 
				"          }\r\n" + 
				"        ],\r\n" + 
				"        \"suggestions\": [\r\n" + 
				"          {\r\n" + 
				"            \"title\": \"Say this\"\r\n" + 
				"          },\r\n" + 
				"          {\r\n" + 
				"            \"title\": \"or this\"\r\n" + 
				"          }\r\n" + 
				"        ]\r\n" + 
				"      }\r\n" + 
				"    }\r\n" + 
				"  }\r\n" + 
				"}]\r\n" + 
				"}";
		return response;

	}

	private String fetchData(String companyName, String priceType, String date) throws IOException {

		String resp = "0";
		String urlFormation = "https://api.intrinio.com/historical_data?ticker=" + companyName + "&item=" + priceType
				+ "&start_date=" + date + "&end_date=" + date + "&api_key=OjcwMGE4YzQxM2ZjYTEzYzI5ODIxNDM0N2I5Y2VhNzFk";
		System.out.println(urlFormation);
		URL url = new URL(urlFormation);
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("GET");
		int responseCode = con.getResponseCode();
		System.out.println("GET Response Code :: " + responseCode);
		if (responseCode == HttpURLConnection.HTTP_OK) { // success
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			StringBuilder response = new StringBuilder();
			String currentLine;

			while ((currentLine = in.readLine()) != null)
				response.append(currentLine);

			in.close();
			System.out.println(response);
			Map<String, Object> flattenedJsonMap = JsonFlattener.flattenAsMap(response.toString());
			System.out.println(flattenedJsonMap);
			for (Entry<String, Object> entry : flattenedJsonMap.entrySet()) {
				if (entry.getKey().equals("data[0].value")) {
					resp = entry.getValue().toString();
				}
			}
		}

		return resp;
	}

	private String getTicker(String companyName) {

		companyName = companyName.toLowerCase();
		Map<String, String> tickerMapper = new HashMap<String, String>();

		tickerMapper.put("apple", "AAPL");
		tickerMapper.put("exxon mobil", "XOM");
		tickerMapper.put("coca-cola", "KO");
		tickerMapper.put("intel", "INTC");
		tickerMapper.put("walmart", "WMT");
		tickerMapper.put("microsoft", "MSFT");
		tickerMapper.put("ibm", "IBM");
		tickerMapper.put("chevron", "CVX");
		tickerMapper.put("johnson & johnson", "JNJ");

		String ticker = "invalid";
		for (Map.Entry<String, String> entry : tickerMapper.entrySet()) {
			if (entry.getKey().equals(companyName))
				ticker = entry.getValue();
		}

		return ticker;

	}

	private String getPriceType(String priceType) {

		Map<String, String> priceMapper = new HashMap<String, String>();
		priceType = priceType.toLowerCase();
		priceMapper.put("opening", "open_price");
		priceMapper.put("closing", "close_price");
		priceMapper.put("maximum", "high_price");
		priceMapper.put("minimum", "low_price");
		priceMapper.put("high", "high_price");
		priceMapper.put("low", "low_price");

		String price = "invalid";
		for (Map.Entry<String, String> entry : priceMapper.entrySet()) {
			if (entry.getKey().equals(priceType))
				price = entry.getValue();
		}

		return price;

	}


	private String getDate(String date) {
		return date = date.substring(0, 10);
	}

}

package application;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;

import edu.stanford.nlp.coref.data.CorefChain;
import edu.stanford.nlp.simple.Document;
import java.util.Properties;

import edu.stanford.nlp.pipeline.*;



import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.ling.CoreAnnotations.MentionsAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import java.util.*;


public class ChatBot {
    String name;
    String phrases = "";
    static int counter = 0;
    
    public ChatBot(String name){
        this.name = name;
    };
    public String sendPhrase(String phrase){
        phrase=dataClean(phrase);

        String ans = "";
        
        String[] stringArray = phrase.split(" ");
        phrase = coreference(phrase);
        // Send phrase to the POS tagger; Returns an ArrayList of possible keywords
        ArrayList<String> list = pos(phrase);
        String[] taggedData = new String[list.size()];
        taggedData = list.toArray(taggedData);

        // Loop to find first keyword
        for(int i = 0; i< taggedData.length; i++){
            // If the first keyword is found call search() to find second keyword
            if(taggedData[i].equals("experience")){
                ans=search("experience", taggedData);
                break;
            };
            if(taggedData[i].equals("travel")){
                ans=search("travel", taggedData);
                break;
            };
            if(taggedData[i].equals("goal")){
                ans=search("goal", taggedData);
                break;
            };
            if(taggedData[i].equals("hobby")){
                ans=search("hobby", taggedData);
                break;
            };
            if(taggedData[i].equals("school")){
                ans=search("school", taggedData);
                break;
            };
            if(taggedData[i].equals("volunteer")){
                ans=search("volunteer", taggedData);
                break;
            };
            if(taggedData[i].equals("salary")){
                ans=search("salary", taggedData);
                break;
            };
            if(taggedData[i].equals("skills")){
                ans=search("skills", taggedData);
                break;
            };
            if(taggedData[i].equals("training")){
                ans=search("training", taggedData);
                break;
            };
            if(taggedData[i].equals("certifications")){
                ans=search("certifications", taggedData);
                break;
            };
            if(taggedData[i].equals("where")){
                try {
                	for(int j = 0; j< taggedData.length; j++) {
                		ans=placesConnect(taggedData[j]);
                	}
                }catch (JSONException e) {
                	ans = "Hmm, I am not sure where that is.";
    			} catch (IOException e) {
    				// TODO Auto-generated catch block
    				e.printStackTrace();
    			} catch (InterruptedException e) {
    				// TODO Auto-generated catch block
    				e.printStackTrace();
    			} catch (URISyntaxException e) {
    				// TODO Auto-generated catch block
    				e.printStackTrace();
    			}
                break;
            };
            if(taggedData[i].equals("know")){
                try {
                	for(int j = 0; j< taggedData.length; j++) {
                		ans=wikiConnect(taggedData[j]);
                	}
    			} catch (IOException e) {
    				// TODO Auto-generated catch block
    				e.printStackTrace();
    			} catch (InterruptedException e) {
    				// TODO Auto-generated catch block
    				e.printStackTrace();
    			} catch (URISyntaxException e) {
    				// TODO Auto-generated catch block
    				e.printStackTrace();
    			}
                break;
            };
            // if no match found then check if a spelling error was made
            // only take error if high confidence we think it means
            // what we think it means
            for(int j = 0; j < fields.length; j++) {
            	
            	double ratio = handleSpelling(taggedData[i], fields[j]);
            	System.out.println(ratio);
            	if(ratio > 0.80) {
            		ans = search(fields[j], taggedData);
            	}
            	
            	break;
            	
            }

        };
      

      //If none of the keywords were found look in the miscellaneous csv for generic questions
        if(ans.length()==0){
        	ans=search("miscellaneous", stringArray);
        }
      //If the ansswer is still empty no keywords were found
        return ans.length()!=0?ans:"Can you please rephrase the question?";    
    };
  

  	public String search(String keyword, String[] stringArray){
      //String csvPath="C:\\Users\\Brandon\\Desktop\\csvs\\" + keyword + ".csv";
      String csvPath="C:\\Users\\joels\\Documents\\School\\3rdYear2ndSem\\COSC310\\Assignment_03\\Assignment_02\\csvs\\" + keyword + ".csv";
      ArrayList<String> data = new ArrayList<String>();
      String row = "";
      boolean breakOut = false;
      String ans = "";
      try(BufferedReader csvReader = new BufferedReader(new FileReader(csvPath))){
          while ((row = csvReader.readLine()) != null) {
              String[] rowData = row.split(",");
               data.add(rowData[0]);
               data.add(rowData[1]);
          };
      }catch(FileNotFoundException e){
          System.out.println(e);                        
      }catch(IOException e){
          System.out.println(e);
      }

      for(int j = 0; j<stringArray.length; j++){
          for(int k=0; k<data.size(); k+=2){
              if(stringArray[j].equals(data.get(k))){
                  ans = data.get(k+1).toString();
                  breakOut=true;
              }
          }
          if(breakOut==true)
              break;
          if(j==stringArray.length-1)
              ans = "Could you be a little more specific please?";
      }
      return ans;
  }

  	public String dataClean(String phrase){
      String cleanedPhrase=phrase.toLowerCase();
      cleanedPhrase=cleanedPhrase.replace("?","").replace(".","").replace(",","").replace("!","");
      return cleanedPhrase;
  	};

    

    public ArrayList<String> pos(String text){

        ArrayList<String> possibleKeywords = new ArrayList<>();
        Properties property = new Properties();
        property.setProperty("annotators", "tokenize,ssplit,pos");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(property);
        CoreDocument document = pipeline.processToCoreDocument(text);
        for (CoreLabel token : document.tokens()) {
            //System.out.println(token.word() + "   " + token.tag());
            if (token.tag().equals("NN") || token.tag().equals("NNS") || token.tag().equals("JJ") || token.tag().equals("JJS") || token.tag().equals("CD") || token.tag().equals("VB") || token.tag().equals("UH") || token.tag().equals("WRB") || token.tag().equals("RB") || token.tag().equals("DT")){
                possibleKeywords.add(token.word());
            }
        }
        return possibleKeywords;
    }

   // coreference function
    public String coreference(String phrase) {
    	if(counter>=2) {
    		phrases = phrases.substring(phrases.indexOf(".",2));
    	};
    	counter++;
    	phrases = phrases + ". " + phrase;
 	   	Document doc = new Document(phrases);
 	   	Map<Integer, CorefChain> ar = doc.coref();
        for (Entry<Integer, CorefChain> me : ar.entrySet()) {
        	
            Pattern regex = Pattern.compile("\"([^\"]*)\"");
            ArrayList<String> allMatches = new ArrayList<String>();
            Matcher matcher = regex.matcher(me.getValue().toString());
            while(matcher.find()){
            	allMatches.add(matcher.group(1));
            };
            for (int i = 0; i < allMatches.size(); i++) {
				if(allMatches.get(i).toString().equals("it")) {
					phrase = phrase.replace("it", allMatches.get(0).toString());
				}
			}
        }
    	return phrase;
    }

    public String NER(String phrase, String ans) {

        Properties props = new Properties();
        props.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner");
        
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
        Annotation annotationPhrase = new Annotation(phrase);
        Annotation annotationAns = new Annotation(ans);
        pipeline.annotate(annotationPhrase);
        pipeline.annotate(annotationAns);
        List<CoreMap> multiWordsExpPhrase = annotationPhrase.get(MentionsAnnotation.class);
        List<CoreMap> multiWordsExpAns = annotationAns.get(MentionsAnnotation.class);
        for(CoreMap multiWordPhrase: multiWordsExpPhrase) {
        	String custNERClassPhrase = multiWordPhrase.get(NamedEntityTagAnnotation.class);
        	System.out.println(multiWordPhrase +" : " +custNERClassPhrase);
        	for(CoreMap multiWordAns: multiWordsExpAns) {
        		String custNERClassAns = multiWordAns.get(NamedEntityTagAnnotation.class);
        		System.out.println(multiWordAns +" : " +custNERClassAns);
        		if(custNERClassAns == custNERClassPhrase) {
        			ans = ans.replaceAll(multiWordAns.toString(), multiWordPhrase.toString());
        			
        		}
        	}
        }
      return ans;
    }
    
    public static double handleSpelling(String taggedWord, String target) {
		
		String big, small;
		double bigCount;
		
		if(taggedWord.length() < target.length()) {
			big = target;
			small = taggedWord;
		}
		
		else {
			big = taggedWord;
			small = target;
		}
		
		bigCount = big.length();
		
		if(bigCount == 0) {
			return 1.0;
		}
		
		return (bigCount - dist(big, small)) / bigCount;
		
	}
	
	public static int dist(String str1, String str2) {
		
		int[] cost = new int[str2.length() + 1];
		
		for(int i = 0; i <= str1.length(); i ++) {
			
			int temp0 = i;
			
			for(int j = 0; j <= str2.length(); j ++) {
				
				if(i == 0) {
					cost[j] = j;	
				}
				
				else {
					
					if(j > 0) {
						
						int temp1 = cost[j - 1];
						
						if(str1.charAt(i - 1) != str2.charAt(j - 1)) {
							temp1 = Math.min(Math.min(temp1, temp0), cost[j]) + 1;
						}
						
						cost[j - 1] = temp0;
						temp0 = temp1;
							
					}
					
				}
				
			}
			
			if(i > 0) {
				cost[str2.length()] = temp0;
			}
			
		}
		
		return cost[str2.length()];
		
	}
	
	private String[] fields = {
			
			"experience", "travel", "goal", "hobby", "school", "volunteer",
			"salary", "skills", "training", "certifications"
			
	};


	private static String wikiConnect(String text) throws IOException, InterruptedException, URISyntaxException {
		text = text.replaceAll(" ", "%20"); // Replace space with %20 for http queries
		HttpClient client = HttpClient.newHttpClient();
		HttpRequest request = HttpRequest.newBuilder()
				  .uri(new URI("https://en.wikipedia.org/w/api.php?action=query&list=search&srsearch="+text+"&format=json"))
				  .GET()
				  .build();
		HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
		String json = response.body();
		JSONObject obj = new JSONObject(json);
		JSONArray arr = obj.getJSONObject("query").getJSONArray("search");
	    String snip = arr.getJSONObject(0).getString("snippet");
	    String ans= Jsoup.parse(snip).text(); // Convert html text to plain
	    ans = ans.substring(0, ans.lastIndexOf('.')==-1?ans.length():ans.lastIndexOf('.')+1); //Try to find last period to avoid cuttoff sentences
		return ans;
	}
	
	private static String placesConnect(String text) throws IOException, InterruptedException, URISyntaxException {
		text = text.replaceAll(" ", ""); // Replace space with nothing for http queries
		HttpClient client = HttpClient.newHttpClient();
		HttpRequest request = HttpRequest.newBuilder()
				  .uri(new URI("https://maps.googleapis.com/maps/api/place/findplacefromtext/json?location=49.8880,119.4960&radius=1500&input="+text+"&inputtype=textquery&fields=formatted_address,name,opening_hours,rating&key=AIzaSyCt3XVT05UtmtKkYgHb3JJ2gIR5A3JBdc0"))
				  .GET()
				  .build();
		HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
		String json = response.body();
		JSONObject obj = new JSONObject(json);
		JSONArray arr = obj.getJSONArray("candidates");
	    String snip = arr.getJSONObject(0).getString("formatted_address");
	    String ans= Jsoup.parse(snip).text(); // Convert html text to plain
		return ans;
	}
}
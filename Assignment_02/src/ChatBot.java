//package application;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

import edu.stanford.nlp.ling.*;
import edu.stanford.nlp.pipeline.*;



public class ChatBot {
    String name;
    
    public ChatBot(String name){
        this.name = name;
    };
    public String sendPhrase(String phrase){
        phrase=dataClean(phrase);

        String ans = "";
        
        String[] stringArray = phrase.split(" ");

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
        String csvPath="C:\\Users\\Sara\\git\\Assignment_02\\csvs\\" + keyword + ".csv";
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
}

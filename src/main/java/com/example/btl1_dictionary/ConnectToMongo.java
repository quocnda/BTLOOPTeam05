package com.example.btl1_dictionary;
import com.mongodb.client.*;
import org.bson.Document;
import com.google.gson.Gson;

import javax.print.Doc;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;


class Data{
    private String _id;
private int id;

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    private String word;
private String html;
private String des;
private String pro;



public String getDes() {
        return des;
        }

public void setDes(String des) {
        this.des = des;
        }


public String getWord() {
        return word;
        }

public String getPro() {
        return pro;
        }

public void setPro(String pro) {
        this.pro = pro;
        }

public String getHtml() {
        return html;
        }

public void setHtml(String html) {
        this.html = html;
        }

public void setWord(String word) {
        this.word = word;
        }
public void setId(int id) {
        this.id = id;
        }

public int getId() {
        return id;
        }


        }
public class ConnectToMongo {
    public static Data convertJsonToString(String jsonString) {
        Data data = new Data();
        jsonString = jsonString.replaceAll("[{}]", "");

        // Tách các cặp key-value bằng dấu phẩy
        String[] keyValuePairs = jsonString.split(",");

        // Duyệt qua các cặp key-value và thiết lập giá trị cho đối tượng Data
        for (String pair : keyValuePairs) {
            String[] entry = pair.split(":");

            // Xóa dấu ngoặc đơn nếu có
            String key = entry[0].replaceAll("\"", "").trim();
            String value = entry[1].replaceAll("\"", "").trim();

            // Thiết lập giá trị cho các trường của đối tượng Data
            switch (key) {
                case "id":
                    data.setId(Integer.parseInt(value));
                    break;
                case "word":
                    data.setWord(value);
                    break;
                case "html":
                    data.setHtml(value);
                    break;
                case "des":
                    data.setDes(value);
                    break;
                case "pro":
                    data.setPro(value);
                    break;
                // Các trường khác nếu có
            }
        }
        return data;
    }
    public static void test(String namedata) {
        System.out.println("say hi bro");
        MongoClient client  = MongoClients.create("mongodb+srv://testUser:vQpzsyz66a0Dbhvi@cluster0.hgbp0kr.mongodb.net/?retryWrites=true&w=majority");
        MongoDatabase db  = client.getDatabase("TestJava");
        MongoCollection<Document> collection = db.getCollection(namedata);
        FindIterable<Document> documents = collection.find();
        List<Data> ldata = new ArrayList<>();
        String jsontest = "";
        int l = 0;
        for (Document document : documents) {
            String json = document.toJson();
            Gson gson = new Gson();
            Data data = gson.fromJson(json,Data.class);
        }


    }
    public static List<Data> getData(String namedata) {
        MongoClient client  = MongoClients.create("mongodb+srv://testUser:vQpzsyz66a0Dbhvi@cluster0.hgbp0kr.mongodb.net/?retryWrites=true&w=majority");
        MongoDatabase db  = client.getDatabase("TestJava");
        MongoCollection<Document> collection = db.getCollection(namedata);
        FindIterable<Document> documents = collection.find();
        List<Data> ldata = new ArrayList<>();
        for (Document document : documents) {
            String json = document.toJson();
            int h = 0;
            for(int  i = 0 ; i <json.length() ; i ++) {
                if(json.charAt(i)==',') {
                    break;
                } else {
                    h++;
                }
            }
            h+=2;
            json = json.substring(h);
            json = "{" + json;

            Data data = convertJsonToString(json);
            ldata.add(data);
        }
        return ldata;

    }
}

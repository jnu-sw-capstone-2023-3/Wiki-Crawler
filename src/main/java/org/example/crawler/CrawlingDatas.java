package org.example.crawler;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class CrawlingDatas extends HashMap<String, Object> {

    private String title, fileName;
    private final String FILE_PARENT = "./data/{title}/{text}.json";

    public CrawlingDatas(String title) {
        this.title = title;
        this.fileName = "";
    }

    private synchronized void saveJson(File json) throws IOException {
        JSONArray array = new JSONArray();
        for(String key : keySet()) {
            Object value = get(key);
            JSONObject jo = new JSONObject();
            jo.put("title", key);

            if(value instanceof CrawlingData)
                jo.put("data", new JSONObject(((CrawlingData) value).getContexts()));
            else if(value instanceof JSONObject)
                jo.put("data", value);

            if(((JSONObject)jo.get("data")).size() == 0) continue;
            array.add(jo);
        }
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonElement ge = JsonParser.parseString(array.toJSONString());

        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(json)));
        writer.write(gson.toJson(ge));
        writer.flush();
        writer.close();
    }

    private synchronized void saveRaw(File raw) throws IOException {
        JSONObject jo = new JSONObject();
        for(String key : keySet()) {
            jo.put(key, get(key));
        }
        BufferedWriter writer = new BufferedWriter(new FileWriter(raw));
        writer.write(jo.toJSONString());
        writer.flush();
        writer.close();
    }

    public synchronized void save() throws IOException {
        try {
            if (keySet().size() == 0) return;

            if (fileName.equals("")) fileName = "save_ckpt";
            String dir = FILE_PARENT.replace("{title}", title).replace("{text}", fileName);
            String raw = FILE_PARENT.replace("{title}", title).replace("{text}", fileName+"-raw");
            File jsonFile = new File(dir);
            File rawFile = new File(raw);
            if (!jsonFile.getParentFile().exists()) jsonFile.getParentFile().mkdirs();
            if (!jsonFile.exists()) jsonFile.createNewFile();
            if (!rawFile.exists()) rawFile.createNewFile();

            System.out.printf("[%s]에 저장 시작.\n", jsonFile.getCanonicalPath());
            saveRaw(rawFile);
            saveJson(jsonFile);
            System.out.printf("[%s]에 저장 완료.\n", jsonFile.getCanonicalPath());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void append(String text) {
        if(fileName.equals("")) fileName = text;
        else fileName = fileName + "-" + text;
    }

    public void clearName() {
        fileName = "";
    }

    public void appendDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
        append(sdf.format(new Date()));
    }

    public void appendNum(int num) {
        append(String.format("%04d", num));
    }



}

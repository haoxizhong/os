package iiis.systems.os.blockdb;

import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Util {
    public static JSONObject readJsonFile(String filePath) throws IOException {
        String content = new String(Files.readAllBytes(Paths.get(filePath)));
        return new JSONObject(content);
    }
}

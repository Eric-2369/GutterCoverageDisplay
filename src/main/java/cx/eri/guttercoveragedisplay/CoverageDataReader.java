package cx.eri.guttercoveragedisplay;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Map;

public class CoverageDataReader {

    public static Map<String, CoverageData> readCoverageData(String filePath) {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(CoverageData.class, new CoverageData.CoverageDataDeserializer());
        Gson gson = gsonBuilder.create();

        try (FileReader reader = new FileReader(filePath)) {
            Type type = new TypeToken<Map<String, CoverageData>>() {
            }.getType();
            return gson.fromJson(reader, type);
        } catch (IOException e) {
            System.out.println("An error occurred while reading the coverage data: " + e.getMessage());
            return null;
        }
    }
}

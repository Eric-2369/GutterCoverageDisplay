package cx.eri.guttercoveragedisplay;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class CoverageData {
    private String coverageRate;
    private List<Integer> coveredLines;
    private List<Integer> uncoveredLines;

    public String getCoverageRate() {
        return coverageRate;
    }

    public void setCoverageRate(String coverageRate) {
        this.coverageRate = coverageRate;
    }

    public List<Integer> getCoveredLines() {
        return coveredLines;
    }

    public void setCoveredLines(List<Integer> coveredLines) {
        this.coveredLines = coveredLines;
    }

    public List<Integer> getUncoveredLines() {
        return uncoveredLines;
    }

    public void setUncoveredLines(List<Integer> uncoveredLines) {
        this.uncoveredLines = uncoveredLines;
    }

    public static class CoverageDataDeserializer implements JsonDeserializer<CoverageData> {
        @Override
        public CoverageData deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jsonObject = json.getAsJsonObject();

            CoverageData coverageData = new CoverageData();
            coverageData.setCoverageRate(jsonObject.get("coverageRate").getAsString());
            coverageData.setCoveredLines(parseLines(jsonObject.get("coveredLines").getAsString()));
            coverageData.setUncoveredLines(parseLines(jsonObject.get("uncoveredLines").getAsString()));

            return coverageData;
        }

        private List<Integer> parseLines(String lines) {
            if (lines == null || lines.isEmpty()) {
                return Collections.emptyList();
            }
            return Arrays.stream(lines.split(",")).map(Integer::parseInt).collect(Collectors.toList());
        }
    }
}

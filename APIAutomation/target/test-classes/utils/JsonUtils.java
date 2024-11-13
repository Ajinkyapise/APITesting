

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;

public class JsonUtils {

    public static RepoData readRepoData(String filePath) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(new File(filePath), RepoData.class);
        } catch (IOException e) {
            throw new RuntimeException("Error reading JSON file", e);
        }
    }
}

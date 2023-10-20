package gg.rimumu.common.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import gg.rimumu.exception.RimumuException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class FileUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileUtil.class);

    public static JsonObject readJsonFile(String path) throws RimumuException {

        try (InputStream inputStream = FileUtil.class.getResourceAsStream(path)) {

            String jsonContent = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);

            // JSON 문자열을 파싱하여 JsonObject로 변환
            JsonElement jsonElement = JsonParser.parseString(jsonContent);
            return jsonElement.getAsJsonObject();

        } catch (Exception e) {
            LOGGER.error("!! read json file error : {}", e.getMessage());
            throw new RimumuException("!! read json file error");
        }
    }

    public static void initJsonFile(String path, String url) throws RimumuException {

        Path filePath = Path.of(path);

        try {
            HttpResponse<String> response = HttpConnUtil.sendHttpGetRequest(url);

            if (200 != response.statusCode()) {
                throw new RimumuException.ServerException();
            }

            String result = response.body();
            Files.writeString(filePath, result, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

        } catch (IOException e) {
            LOGGER.error("!! FileUtil error message : {}", e.getMessage());
            throw new RimumuException(e.getMessage());
        }
    }

}

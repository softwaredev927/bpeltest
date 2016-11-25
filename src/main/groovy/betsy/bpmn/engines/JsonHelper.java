package betsy.bpmn.engines;

import java.nio.file.Path;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.http.options.Option;
import com.mashape.unirest.http.options.Options;
import com.mashape.unirest.http.utils.SyncIdleConnectionMonitorThread;
import com.mashape.unirest.request.HttpRequestWithBody;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JsonHelper {

    private static final Logger log = Logger.getLogger(JsonHelper.class);

    static {
        // do not use this annoying thread
        SyncIdleConnectionMonitorThread syncIdleConnectionMonitorThread = (SyncIdleConnectionMonitorThread) Options.getOption(Option.SYNC_MONITOR);
        syncIdleConnectionMonitorThread.interrupt();
    }

    public static final String REST_CALL_FAILED_WITH_URL = "rest call failed with url ";

    public static JSONObject get(String url, int expectedCode) {
        log.info("HTTP GET " + url);

        try {
            HttpResponse<JsonNode> response = Unirest.get(url).asJson();
            assertHttpCode(expectedCode, response);
            logResponse(response.getBody());

            if (response.getBody().isArray()) {
                return response.getBody().getArray().optJSONObject(0);
            } else {
                return response.getBody().getObject();
            }
        } catch (UnirestException e) {
            throw new RuntimeException(REST_CALL_FAILED_WITH_URL + url, e);
        }
    }

    public static JSONArray getJsonArray(String url, int expectedCode) {
        log.info("HTTP GET " + url);

        try {
            HttpResponse<JsonNode> response = Unirest.get(url).asJson();
            assertHttpCode(expectedCode, response);
            logResponse(response.getBody());

            if (response.getBody().isArray()) {
                return response.getBody().getArray();
            } else {
                throw new RuntimeException("Unexpected response: Expected an array which was not present.");
            }
        } catch (UnirestException e) {
            throw new RuntimeException(REST_CALL_FAILED_WITH_URL + url, e);
        }
    }

    public static String getStringWithAuth(String url, int expectedCode, String username, String password) {
        log.info("HTTP GET " + url);

        try {
            HttpResponse<String> response = Unirest.get(url).basicAuth(username, password).asString();
            assertHttpCode(expectedCode, response);
            logResponse(response.getBody());

            return response.getBody();
        } catch (UnirestException e) {
            throw new RuntimeException(REST_CALL_FAILED_WITH_URL + url, e);
        }
    }

    public static JSONObject getJSONWithAuth(String url, int expectedCode, String username, String password) {
        log.info("HTTP GET " + url);

        try {
            HttpResponse<JsonNode> response = Unirest.get(url).basicAuth(username, password).header("Accept", "application/json").asJson();
            assertHttpCode(expectedCode, response);
            logResponse(response.getBody());

            return response.getBody().getObject();
        } catch (UnirestException e) {
            throw new RuntimeException(REST_CALL_FAILED_WITH_URL + url, e);
        }
    }

    public static JSONArray getJSONWithAuthAsArray(String url, int expectedCode, String username, String password) {
        log.info("HTTP GET " + url);

        try {
            HttpResponse<JsonNode> response = Unirest.get(url).basicAuth(username, password).header("Accept", "application/json").asJson();
            assertHttpCode(expectedCode, response);
            logResponse(response.getBody());

            return response.getBody().getArray();
        } catch (UnirestException e) {
            throw new RuntimeException(REST_CALL_FAILED_WITH_URL + url, e);
        }
    }

    public static JSONObject post(String url, JSONObject requestBody, int expectedCode) {
        log.info("HTTP POST " + url);
        try {
            log.info("CONTENT: " + requestBody.toString(2));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            HttpResponse<JsonNode> response = Unirest.post(url).header("Content-Type", "application/json").body(requestBody.toString()).asJson();
            assertHttpCode(expectedCode, response);
            logResponse(response.getBody());
            return response.getBody().getObject();
        } catch (UnirestException e) {
            throw new RuntimeException(REST_CALL_FAILED_WITH_URL + url, e);
        }
    }

    public static JSONArray delete(String url, int expectedCode) {
        log.info("HTTP DELETE " + url);

        try {
            HttpRequestWithBody result = Unirest.delete(url).header("Content-Type", "application/json");
            HttpResponse<JsonNode> response = result.asJson();
            assertHttpCode(expectedCode, response);
            logResponse(response.getBody());
            if(response.getBody() == null) {
                return new JSONArray();
            } else {
                return response.getBody().getArray();
            }
        } catch (UnirestException e) {
            throw new RuntimeException(REST_CALL_FAILED_WITH_URL + url, e);
        }
    }

    public static String postStringWithAuth(String url, JSONObject requestBody, int expectedCode, String username, String password) {
        log.info("HTTP POST " + url);
        try {
            log.info("CONTENT: " + requestBody.toString(2));
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        try {
            HttpResponse<String> response = Unirest.post(url).header("Content-Type", "application/json").basicAuth(username, password).body(requestBody.toString()).asString();
            assertHttpCode(expectedCode, response);
            logResponse(response.getBody());
            return response.getBody();
        } catch (UnirestException e) {
            throw new RuntimeException(REST_CALL_FAILED_WITH_URL + url, e);
        }
    }

    public static String postWithAuthWithAcceptJson(String url, int expectedCode, String username, String password) {
        log.info("HTTP POST " + url);
        log.info("NO CONTENT");

        try {
            HttpResponse<String> response = Unirest.post(url).
                    header("Content-Type", "application/json").
                    header("Accept", "application/json").
                    basicAuth(username, password).body(new JsonNode(""))
                    .asString();
            assertHttpCode(expectedCode, response);
            logResponse(response.getBody());
            return response.getBody();
        } catch (UnirestException e) {
            throw new RuntimeException(REST_CALL_FAILED_WITH_URL + url, e);
        }
    }

    public static JSONObject post(String url, Path path, int expectedCode) {
        log.info("HTTP POST " + url);
        log.info("FILE: " + path);

        try {
            HttpResponse<JsonNode> response = Unirest.post(url).field("file", path.toFile()).asJson();
            assertHttpCode(expectedCode, response);
            logResponse(response.getBody());
            return response.getBody().getObject();
        } catch (UnirestException e) {
            throw new RuntimeException(REST_CALL_FAILED_WITH_URL + url, e);
        }
    }

    private static void assertHttpCode(int expectedCode, HttpResponse<?> response) {
        int code = response.getStatus();
        if (expectedCode == code) {
            log.info("Response returned with expected status code " + expectedCode);
        } else {
            throw new RuntimeException("expected " + expectedCode + ", got " + code + "; " +
                    "reason: " + response.getBody());
        }
    }

    private static void logResponse(JSONArray response) {
        if (response == null) {
            log.info("HTTP RESPONSE is empty.");
        } else {
            try {
                log.info("HTTP RESPONSE: " + response.toString(2));
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static void logResponse(String response) {
        if (response == null) {
            log.info("HTTP RESPONSE String is empty.");
        } else {
            log.info("HTTP RESPONSE: " + response);
        }
    }

    private static void logResponse(JSONObject response) {
        if (response == null) {
            log.info("HTTP RESPONSE is empty.");
        } else {
            try {
                log.info("HTTP RESPONSE: " + response.toString(2));
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static void logResponse(JsonNode response) {
        if(response == null) {
            log.info("RESPONSE IS EMPTY");
            return;
        }

        if (response.isArray()) {
            logResponse(response.getArray());
        } else {
            logResponse(response.getObject());
        }
    }

}

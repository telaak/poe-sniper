package poetrader;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

@SuppressWarnings("unused")
public class API {
    String next_change_id;
    JsonArray stashes;
    ArrayList<JsonObject> stashList = new ArrayList<>();
    URL url;

    public API() {
        try {
            url = new URL("http://www.pathofexile.com/api/public-stash-tabs?id=188567222-196495072-185238683-212705888-199841912");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    public void parseStashes() {
        for(JsonElement element : stashes) {
            stashList.add(element.getAsJsonObject());
        }
    }

    public JsonObject findStashByAccountName(String name) {
        for(JsonObject stash : stashList) {
            if(stash.get("accountName").toString().equals(name)) {
                return stash;
            }
        }
        return null;
    }

    public JsonObject findStashByCharacterName(String name) {
        for(JsonObject stash : stashList) {
            if(stash.get("lastCharacterName").toString().equals(name)) {
                return stash;
            }
        }
        return null;
    }

}

package poetrader;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import static java.lang.Thread.sleep;

public class APIThread implements Runnable {
    API api = new API();

    @Override
    public void run() {
        while(true) {
            InputStreamReader reader = null;
            try {
                reader = new InputStreamReader(api.url.openStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
            api = new Gson().fromJson(reader, API.class);
            api.parseStashes();
            try {
                System.out.println(api.findStashByAccountName("\"Hillo\"").toString());
            }
            catch (NullPointerException e){
                System.out.println("Not found");
            }
            try {
                api.url = new URL("http://www.pathofexile.com/api/public-stash-tabs?id="+api.next_change_id);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            try {
                sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

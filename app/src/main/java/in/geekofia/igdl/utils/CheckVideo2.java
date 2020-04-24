package in.geekofia.igdl.utils;

import android.os.AsyncTask;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;

public class CheckVideo2 extends AsyncTask<String, Void, String> {

    private String mVideoURL = "";

    @Override
    protected String doInBackground(String... strings) {
        Document doc = null;
        try {
            doc = Jsoup.connect(strings[0]).get();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Elements articles = doc.getElementsByTag("article");
        Elements videos = articles.get(0).getElementsByTag("video");

        if (videos.isEmpty()) {
            return null;
        } else {
            mVideoURL = videos.get(0).attr("src");
            System.out.println("### Video URL:" + mVideoURL);
            return mVideoURL;
        }
    }

    public String getVideoURL() {
        return mVideoURL;
    }
}

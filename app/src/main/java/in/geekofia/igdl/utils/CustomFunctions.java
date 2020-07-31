package in.geekofia.igdl.utils;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.ArrayList;

import in.geekofia.igdl.models.InstaMedia;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class CustomFunctions {

    public static Retrofit initRetrofit() {
        return new Retrofit.Builder()
                .baseUrl("https://is.gd/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    public static void clipViewSourceURL(String url, Activity activity, Context context) {
        String newUrl = "view-source:" + url;
        // Gets a handle to the clipboard service.
        ClipboardManager clipboard = (ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);

        // Creates a new text clip to put on the clipboard
        ClipData clip = ClipData.newPlainText("View Source URL", newUrl);

        // Set the clipboard's primary clip.
        if (clipboard != null) {
            clipboard.setPrimaryClip(clip);
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("about:blank"));
            Bundle b = new Bundle();
            b.putBoolean("new_window", true);
            intent.putExtras(b);
            context.startActivity(intent);
        }
    }

    public static ArrayList<InstaMedia> parseSource(Document document) {
        ArrayList<InstaMedia> instaMedia = new ArrayList<>();

        try {
            Element body = document.getElementsByTag("body").get(0);
            Element rawData = body.getElementsByTag("script").get(0);

            JSONObject jsonData = new JSONObject(rawData.data().substring(20, rawData.data().length() - 1));

            // $.entry_data.PostPage[*].graphql.shortcode_media.edge_sidecar_to_children.edges[*].node.display_url
            JSONObject shortCodeMedia = jsonData.getJSONObject("entry_data")
                    .getJSONArray("PostPage")
                    .getJSONObject(0)
                    .getJSONObject("graphql")
                    .getJSONObject("shortcode_media");

            // check type
            switch (shortCodeMedia.getString("__typename")) {
                // only one video
                case "GraphVideo":
                    InstaMedia instaVideoOnly = new InstaMedia(shortCodeMedia.getString("id"),
                            shortCodeMedia.getString("display_url"),
                            shortCodeMedia.getBoolean("is_video"),
                            shortCodeMedia.getString("video_url"),
                            shortCodeMedia.getBoolean("has_audio"));

                    instaMedia.add(instaVideoOnly);
                    break;

                // only one image
                case "GraphImage":
                    InstaMedia instaMediaOnly = new InstaMedia(shortCodeMedia.getString("id"),
                            shortCodeMedia.getString("display_url"),
                            shortCodeMedia.getBoolean("is_video"));

                    instaMedia.add(instaMediaOnly);
                    break;

                // multiple media (maybe mixed)
                case "GraphSidecar":
                    JSONArray edges = shortCodeMedia
                            .getJSONObject("edge_sidecar_to_children")
                            .getJSONArray("edges");

                    for (int i = 0; i < edges.length(); i++) {
                        JSONObject node = edges.getJSONObject(i).getJSONObject("node");
                        // check node prop for media type
                        boolean isVideo = node.getBoolean("is_video");

                        if (isVideo) {
                            InstaMedia instaVideo = new InstaMedia(node.getString("id"),
                                    node.getString("display_url"),
                                    true,
                                    node.getString("video_url"),
                                    node.getBoolean("has_audio"));
                            instaMedia.add(instaVideo);
                        } else {
                            InstaMedia instaMedia2 = new InstaMedia(node.getString("id"),
                                    node.getString("display_url"),
                                    false);
                            instaMedia.add(instaMedia2);
                        }
                    }
                    break;
            }
        }catch (Exception e) {
            e.printStackTrace();
        }

        return instaMedia;
    }
}

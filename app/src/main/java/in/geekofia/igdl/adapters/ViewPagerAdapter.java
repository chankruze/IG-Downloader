package in.geekofia.igdl.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import in.geekofia.igdl.R;
import in.geekofia.igdl.models.InstaPost;


public class ViewPagerAdapter extends PagerAdapter {
    private Context context;
    private ArrayList<InstaPost> instaPosts;

    public ViewPagerAdapter(Context context, ArrayList<InstaPost> instaPosts) {
        this.context = context;
        this.instaPosts = instaPosts;
    }

    @Override
    public int getCount() {
        return instaPosts.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        InstaPost instaPost = instaPosts.get(position);

        if (instaPost.isVideo()) {
            // create VideoView
            VideoView videoView = new VideoView(context);
            videoView.setVideoPath(instaPost.getVideoUrl());
            videoView.setMediaController(new MediaController(context));
            videoView.setKeepScreenOn(true);
            videoView.start();
            container.addView(videoView);
            container.getRootView().findViewById(R.id.progress_horizontal).setVisibility(View.GONE);
            return videoView;
        } else {
            // create ImageView
            ImageView imageView = new ImageView(context);
            Picasso.get().load(instaPost.getImageUrl()).into(imageView);
            container.addView(imageView);
            container.getRootView().findViewById(R.id.progress_horizontal).setVisibility(View.GONE);
            return imageView;
        }
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }
}

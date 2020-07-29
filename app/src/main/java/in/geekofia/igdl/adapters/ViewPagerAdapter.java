package in.geekofia.igdl.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import in.geekofia.igdl.R;
import in.geekofia.igdl.models.InstaPost;


public class ViewPagerAdapter extends RecyclerView.Adapter<ViewPagerAdapter.InstaViewHolder> {
    private Context context;
    private ArrayList<InstaPost> instaPosts;

    public ViewPagerAdapter(Context context, ArrayList<InstaPost> instaPosts) {
        this.context = context;
        this.instaPosts = instaPosts;
    }

    @NonNull
    @Override
    public InstaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.insta_post, parent, false);
        return new InstaViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull InstaViewHolder holder, int position) {
        InstaPost instaPost = instaPosts.get(position);

        if (instaPost.isVideo()) {
            holder.videoView.setVideoPath(instaPost.getVideoUrl());
            holder.videoView.setMediaController(new MediaController(context));
            holder.videoView.setKeepScreenOn(true);
            holder.videoView.start();
            holder.videoView.setVisibility(View.VISIBLE);
            holder.progressBar.setVisibility(View.GONE);
        } else {
            Picasso.get().load(instaPost.getImageUrl()).into(holder.imageView, new Callback() {
                @Override
                public void onSuccess() {
                    holder.imageView.setVisibility(View.VISIBLE);
                    holder.progressBar.setVisibility(View.GONE);
                }

                @Override
                public void onError(Exception e) {

                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return instaPosts.size();
    }

    public static class InstaViewHolder extends RecyclerView.ViewHolder {

        private ImageView imageView;
        private VideoView videoView;
        private ProgressBar progressBar;

        public InstaViewHolder(@NonNull View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.image_view);
            imageView.setVisibility(View.GONE);

            videoView = itemView.findViewById(R.id.video_view);
            videoView.setVisibility(View.GONE);

            progressBar = itemView.findViewById(R.id.progress_horizontal);
            progressBar.setVisibility(View.VISIBLE);
        }
    }
}

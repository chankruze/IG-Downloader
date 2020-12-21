package in.geekofia.igdl.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import in.geekofia.igdl.R;
import in.geekofia.igdl.models.InstaMedia;


public class ViewPagerAdapter extends RecyclerView.Adapter<ViewPagerAdapter.InstaViewHolder> {
    private final Context context;
    private final ArrayList<InstaMedia> instaMedia;

    public ViewPagerAdapter(Context context, ArrayList<InstaMedia> instaMedia) {
        this.context = context;
        this.instaMedia = instaMedia;
    }

    @NonNull
    @Override
    public InstaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.insta_post, parent, false);
        return new InstaViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull InstaViewHolder holder, int position) {
        InstaMedia instaMedia = this.instaMedia.get(position);

        if (instaMedia.isVideo()) {
            holder.videoView.setVideoPath(instaMedia.getVideoUrl());
            holder.videoView.setMediaController(new MediaController(context));
            holder.videoView.setKeepScreenOn(true);
            holder.videoView.start();
            holder.videoView.setVisibility(View.VISIBLE);
            holder.progressBar.setVisibility(View.GONE);
        } else {
            Picasso.get().load(instaMedia.getImageUrl()).into(holder.imageView, new Callback() {
                @Override
                public void onSuccess() {
                    holder.imageView.setVisibility(View.VISIBLE);
                    holder.progressBar.setVisibility(View.GONE);
                }

                @Override
                public void onError(Exception e) {
                    Toast.makeText(context, "Error loading media", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return instaMedia.size();
    }

    public static class InstaViewHolder extends RecyclerView.ViewHolder {

        private final ImageView imageView;
        private final VideoView videoView;
        private final ProgressBar progressBar;

        public InstaViewHolder(@NonNull View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.image_view);
            imageView.setVisibility(View.GONE);

            videoView = itemView.findViewById(R.id.video_view);
            videoView.setVisibility(View.GONE);

            progressBar = itemView.findViewById(R.id.post_progress_horiz);
            progressBar.setVisibility(View.VISIBLE);
        }
    }
}

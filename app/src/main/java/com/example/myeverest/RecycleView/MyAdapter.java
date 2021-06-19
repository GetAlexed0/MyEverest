package com.example.myeverest.RecycleView;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myeverest.R;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
    private List<String> localDataStrings;
    private List<Bitmap> localDataImages;
    private List<Integer> likes;
    private boolean on_insta = false;

    @NonNull
    @NotNull
    @Override
    public MyAdapter.ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.text_row_item, parent, false);

        return new ViewHolder(view);
    }

    public MyAdapter(List<String> localDataStrings, List<Bitmap> localDataImages) {
        this.localDataImages = localDataImages;
        this.localDataStrings = localDataStrings;
    }

    public MyAdapter(List<Integer> likes, List<String> localDataStrings, List<Bitmap> localDataImages){
        this.likes = likes;
        this.localDataImages = localDataImages;
        this.localDataStrings = localDataStrings;
        on_insta = true;
    }

    public String getStringAtPosition(int pos) {
        return localDataStrings.get(pos);
    }


    @Override
    public void onBindViewHolder(@NonNull @NotNull MyAdapter.ViewHolder holder, int position) {
        holder.getTextView().setText(localDataStrings.get(position));
        holder.getImageView().setImageBitmap(localDataImages.get(position));
        if(on_insta){
            holder.getTextView_likes().setText(likes.get(position).toString());
        }

    }

    @Override
    public int getItemCount() {
        return localDataStrings.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView textView, textView_likes;
        private final ImageView imageView;

        public ViewHolder(@NonNull @NotNull View view) {
            super(view);

            textView = (TextView) view.findViewById(R.id.textViewRecycle);
            imageView = (ImageView) view.findViewById(R.id.imageViewRecycle);
            textView_likes = (TextView) view.findViewById(R.id.textViewLikes);
        }

        public TextView getTextView() {
            return textView;
        }

        public TextView getTextView_likes() {
            return textView_likes;
        }

        public ImageView getImageView() {
            return imageView;
        }

    }


}

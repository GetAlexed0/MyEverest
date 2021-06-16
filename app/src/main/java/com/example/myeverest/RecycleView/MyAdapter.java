package com.example.myeverest.RecycleView;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myeverest.Helpers.CustomAdapter;
import com.example.myeverest.R;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
    private List<String> localDataStrings;
    private List<Bitmap> localDataImages;

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

    public String getStringAtPosition(int pos) {
        return localDataStrings.get(pos);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull MyAdapter.ViewHolder holder, int position) {
        holder.getTextView().setText(localDataStrings.get(position));
        holder.getImageView().setImageBitmap(localDataImages.get(position));
    }

    @Override
    public int getItemCount() {
        return localDataStrings.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView textView;
        private final ImageView imageView;

        public ViewHolder(@NonNull @NotNull View view) {
            super(view);

            textView = (TextView) view.findViewById(R.id.textViewRecycle);
            imageView = (ImageView) view.findViewById(R.id.imageViewRecycle);
        }

        public TextView getTextView() {
            return textView;
        }

        public ImageView getImageView() {
            return imageView;
        }
    }


}

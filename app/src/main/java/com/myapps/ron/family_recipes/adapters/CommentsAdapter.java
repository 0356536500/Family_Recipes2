package com.myapps.ron.family_recipes.adapters;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.myapps.ron.family_recipes.R;
import com.myapps.ron.family_recipes.model.CommentEntity;
import com.myapps.ron.family_recipes.recycler.CommentsDiffCallback;
import com.myapps.ron.family_recipes.utils.DateUtil;

import java.util.List;

/**
 * Created by ronginat on 06/12/2018.
 */
public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.MyViewHolder> {

    private List<CommentEntity> commentList;

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView sender, comment, time;

        MyViewHolder(View view) {
            super(view);
            sender = view.findViewById(R.id.sender);
            comment = view.findViewById(R.id.comment);
            time = view.findViewById(R.id.time_text);
        }
    }

    public CommentsAdapter(List<CommentEntity> commentList) {
        this.commentList = commentList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        return new MyViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_item_comment, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        CommentEntity comment = commentList.get(position);

        holder.sender.setText(comment.getUser());
        holder.comment.setText(comment.getMessage());
        holder.time.setText(DateUtil.getDateFromDateTime(comment.getDate()));
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }


    public void setComments(List<CommentEntity> commentList) {
        if (commentList != null) {
            List<CommentEntity> oldTemp = this.commentList;
            this.commentList = commentList;
            DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new CommentsDiffCallback(oldTemp, commentList));
            diffResult.dispatchUpdatesTo(this);
            //notifyDataSetChanged();
        }
    }
}

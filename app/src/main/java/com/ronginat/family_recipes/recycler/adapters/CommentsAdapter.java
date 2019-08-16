package com.ronginat.family_recipes.recycler.adapters;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ronginat.family_recipes.R;
import com.ronginat.family_recipes.model.CommentEntity;
import com.ronginat.family_recipes.recycler.helpers.CommentsDiffCallback;
import com.ronginat.family_recipes.utils.logic.DateUtil;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by ronginat on 06/12/2018.
 */
public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.MyViewHolder> {

    private List<CommentEntity> commentList;
    private CommentsAdapterListener listener;

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView sender, message, time;

        MyViewHolder(View view) {
            super(view);
            sender = view.findViewById(R.id.sender);
            message = view.findViewById(R.id.message);
            time = view.findViewById(R.id.time_text);
        }
    }

    public CommentsAdapter(CommentsAdapterListener listener) {
        this.commentList = new ArrayList<>();
        this.listener = listener;
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

        holder.message.setText(comment.getMessage());
        holder.time.setText(DateUtil.getPrettyDateFromTime(comment.getDate()));
        //holder.sender.setText(comment.getUser());
        if (comment.getUser() != null) {
            listener.getDisplayedName(comment.getUser())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new DisposableSingleObserver<String>() {
                        @Override
                        public void onSuccess(String name) {
                            holder.sender.setText(name);
                            dispose();
                        }

                        @Override
                        public void onError(Throwable t) {
                            holder.sender.setText(comment.getUser());
                        }
                    });
        }
    }

    @Override
    public int getItemCount() {
        if (commentList != null)
            return commentList.size();
        return 0;
    }


    public void setComments(List<CommentEntity> commentList) {
        if (commentList != null) {
                List<CommentEntity> oldTemp = this.commentList;
                this.commentList = commentList;
                DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new CommentsDiffCallback(oldTemp, commentList));
                diffResult.dispatchUpdatesTo(this);
        }
    }

    public interface CommentsAdapterListener {
        Single<String> getDisplayedName(String username);
    }
}

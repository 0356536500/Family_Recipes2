package com.myapps.ron.family_recipes.recycler.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.ernestoyaquello.dragdropswiperecyclerview.DragDropSwipeAdapter;
import com.myapps.ron.family_recipes.R;
import com.myapps.ron.family_recipes.model.HtmlModel;
import com.myapps.ron.family_recipes.recycler.helpers.SwipeAndDragHelper;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Created by ronginat on 07/03/2019.
 */
public class MyDragDropSwipeAdapter extends RecyclerView.Adapter<MyDragDropSwipeAdapter.MyViewHolder>
        implements SwipeAndDragHelper.ActionCompletionContract {
    private final String TAG = getClass().getSimpleName();

    private List<String> elements;
    private SwipingRecycler swipingListener;

    class MyViewHolder extends DragDropSwipeAdapter.ViewHolder implements View.OnClickListener{
        private ViewGroup expandedLayout, collapsedLayout;
        private Spinner spinner;
        private CheckBox checkBoxDivider, checkBoxBold, checkBoxUnderScore ;
        private AppCompatEditText editText;
        private TextView collapsedTextView;
        private ImageButton collapseButton, expandButton;

        MyViewHolder(View view) {
            super(view);
            //expanded layout
            expandedLayout = view.findViewById(R.id.row_html_expanded_layout);
            spinner = view.findViewById(R.id.row_html_choose_type);
            checkBoxDivider = view.findViewById(R.id.row_html_horizontal_divider);
            checkBoxBold = view.findViewById(R.id.row_html_bold_checkBox);
            checkBoxUnderScore = view.findViewById(R.id.row_html_under_score_checkBox);
            editText = view.findViewById(R.id.row_html_details_editText);
            collapseButton = view.findViewById(R.id.row_html_collapse_button);

            //collapsed layout
            collapsedLayout = view.findViewById(R.id.row_html_collapsed_layout);
            expandButton = view.findViewById(R.id.row_html_expand_button);
            collapsedTextView = view.findViewById(R.id.row_html_collapsed_text);

            setCollapsingListeners();
        }

        private void setExpanded(boolean isExpanded) {
            expandedLayout.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
            collapsedLayout.setVisibility(!isExpanded ? View.VISIBLE : View.GONE);
        }

        private void setCollapsingListeners() {
            collapseButton.setOnClickListener(this);
            expandButton.setOnClickListener(this);
            collapsedLayout.setOnClickListener(this);
        }

        private void bind(String str) {
            editText.setText(str);
        }

        private void bindTo(HtmlModel model) {
            if (model != null) {
                if (model.getText() != null)
                    editText.setText(model.getText());
                if (model.getSpinnerPos() >= 0)
                    spinner.setSelection(model.getSpinnerPos());
                checkBoxBold.setChecked(model.isBold());
                checkBoxUnderScore.setChecked(model.isUnderscore());
                checkBoxDivider.setChecked(model.isDivider());
            }
        }

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.row_html_collapse_button:
                    setExpanded(false);
                    break;
                case R.id.row_html_expand_button: // same as below
                case R.id.row_html_collapsed_layout:
                    setExpanded(true);
                    break;
            }
            notifyItemChanged(getAdapterPosition());
        }
    }

    public MyDragDropSwipeAdapter(List<String> data, SwipingRecycler swipingListener) {
        super();
        this.swipingListener = swipingListener;
        this.elements = data;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.row_item_flexible_html_structure, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        String str = elements.get(position);
        holder.bind(str);
    }

    @Override
    public int getItemCount() {
        if (elements != null)
            return elements.size();
        return 0;
    }

    // region swipe and drag

    @Override
    public void onViewMoved(int oldPosition, int newPosition) {
        Log.e(TAG, "item moved, " + elements.get(oldPosition));
        String str = elements.get(oldPosition);
        //User user = new User(targetUser);
        elements.remove(oldPosition);
        elements.add(newPosition, str);
        notifyItemMoved(oldPosition, newPosition);
    }

    @Override
    public void onViewSwiped(int position) {
        Log.e(TAG, "item swiped, " + elements.get(position));
        String item = elements.remove(position);
        notifyItemRemoved(position);
        swipingListener.onItemRemoved(item, position);
    }

    // endregion


    public List<String> getElements() {
        return this.elements;
    }

    public void insertItem(String item, int position) {
        this.elements.add(position, item);
        notifyItemInserted(position);
    }

    public interface SwipingRecycler {
        void onItemRemoved(String item, int position);
    }
}

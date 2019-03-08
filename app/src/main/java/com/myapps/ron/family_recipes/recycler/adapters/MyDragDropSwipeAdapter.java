package com.myapps.ron.family_recipes.recycler.adapters;

import android.view.View;
import android.widget.CheckBox;
import android.widget.Spinner;

import com.ernestoyaquello.dragdropswiperecyclerview.DragDropSwipeAdapter;
import com.myapps.ron.family_recipes.R;
import com.myapps.ron.family_recipes.model.HtmlModel;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatEditText;

/**
 * Created by ronginat on 07/03/2019.
 */
public class MyDragDropSwipeAdapter extends DragDropSwipeAdapter<String, MyDragDropSwipeAdapter.MyViewHolder> {

    class MyViewHolder extends DragDropSwipeAdapter.ViewHolder {
        private Spinner spinner;
        private CheckBox checkBoxDivider, checkBoxBold, checkBoxUnderScore ;
        private AppCompatEditText editText;

        MyViewHolder(View view) {
            super(view);
            spinner = view.findViewById(R.id.advanced_step_choose_type);
            checkBoxDivider = view.findViewById(R.id.advanced_step_horizontal_divider);
            checkBoxBold = view.findViewById(R.id.advanced_step_bold_checkBox);
            checkBoxUnderScore = view.findViewById(R.id.advanced_step_under_score_checkBox);
            editText = view.findViewById(R.id.advanced_step_details_editText);
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
    }

    public MyDragDropSwipeAdapter(List<String> data) {
        super(data);
        setDataSet(data);
    }

    @NonNull
    @Override
    protected MyViewHolder getViewHolder(@NonNull View view) {
        return new MyViewHolder(view);
    }

    @NonNull
    @Override
    protected View getViewToTouchToStartDraggingItem(String s, @NonNull MyDragDropSwipeAdapter.MyViewHolder myViewHolder, int position) {
        return myViewHolder.itemView;
    }

    @Override
    protected void onBindViewHolder(String s, @NonNull MyDragDropSwipeAdapter.MyViewHolder holder, int position) {
        holder.editText.setText(s);
    }

}

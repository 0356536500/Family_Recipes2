package com.myapps.ron.family_recipes.ui.fragments;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import com.myapps.ron.family_recipes.R;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.subjects.PublishSubject;

/**
 * Created by ronginat on 04/02/2019.
 */
public class PickImagesMethodDialog extends DialogFragment {

    public enum Option { CAMERA, GALLERY, CANCEL }
    public PublishSubject<Option> dispatchInfo;

    public PickImagesMethodDialog() {
        this.dispatchInfo = PublishSubject.create();
    }

    @Override
    public void dismiss() {
        super.dismiss();
        dispatchInfo.onComplete();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.pick_image_dialog, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        ButterKnife.bind(this, view);
    }

    @SuppressWarnings("UnusedParameters")
    @OnClick(R.id.pick_image_dialog_camera)
    void camera(View view) {
        delayedDispatch(Option.CAMERA);
    }

    @SuppressWarnings("UnusedParameters")
    @OnClick(R.id.pick_image_dialog_gallery)
    void gallery(View view) {
        delayedDispatch(Option.GALLERY);
    }

    @SuppressWarnings("UnusedParameters")
    @OnClick(R.id.pick_image_dialog_cancel)
    void cancel(View view) {
        delayedDispatch(Option.CANCEL);
    }

    private void delayedDispatch(Option option) {
        new Handler().postDelayed(() ->
                dispatchInfo.onNext(option), 200);
    }

    /*private View delayedDismiss() {
        new Handler().postDelayed(this::dismiss, 20);

        return new View(getContext());
    }*/
}

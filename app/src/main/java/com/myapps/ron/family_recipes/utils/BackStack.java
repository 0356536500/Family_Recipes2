package com.myapps.ron.family_recipes.utils;

import android.os.Build;
import android.os.Bundle;

import com.myapps.ron.family_recipes.ui.baseclasses.MyFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;

/**
 * Created by ronginat on 07/04/2019.
 */
public class BackStack {
    public static final String TAG = BackStack.class.getSimpleName();

    private static final String SAVE_STATE_SIZE = "backStackSize";
    private static final String SAVE_STATE_TAG = "backStackTag";

    private BackStackHelper helper;
    private List<MyFragment> backStack;

    public BackStack(BackStackHelper helper) {
        super();
        backStack = new ArrayList<>();
        this.helper = helper;
    }

    private BackStack(BackStackHelper helper, List<MyFragment> fragments) {
        this(helper);
        backStack.addAll(fragments);
    }

    public int size() {
        return backStack.size();
    }

    public MyFragment peekTopFragment() {
        if (backStack.size() > 0)
            return backStack.get(0);
        return null;
    }

    public void addToBackStack(MyFragment fragment) {
        // duplicate fragments not allowed!
        int index = backStack.indexOf(fragment);
        if (index > 0) {
            // remove fragment from old index
            backStack.remove(index);
        }
        else
        // add the fragment to backStack at index 0
        backStack.add(0, fragment);
    }

    @Nullable
    public MyFragment popFromBackStack() {
        if (size() > 1) {
            backStack.remove(0); // pop the current fragment out of the stack
            return backStack.get(0); // new displaying fragment
        }
        return null;
    }

    @Nullable
    public MyFragment findFragmentByTag(@StringRes int tag) {
        return findByTagWrapper(tag);
    }

    @Nullable
    private MyFragment findByTagWrapper(@StringRes int tag) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return findByTag(tag);
        } else
            return findByTagLegacy(tag);
    }

    @Nullable
    @RequiresApi(api = Build.VERSION_CODES.N)
    private MyFragment findByTag(@StringRes int tag) {
        return backStack
                .stream()
                .filter(t ->  t.getMyTag() == tag)
                .findFirst()
                .orElse(null);
    }

    @Nullable
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private MyFragment findByTagLegacy(@StringRes int tag) {
        for (MyFragment fragment: backStack) {
            if (fragment.getMyTag() == tag)
                return fragment;
        }
        return null;
    }

    // region Instance State

    @NonNull
    public static BackStack restoreBackStackFromList(@NonNull Bundle savedInstanceState, BackStackHelper helper, List<Fragment> fragments) {
        BackStack backStack;
        if (fragments != null)
            backStack = new BackStack(helper, restoreBackStackWrapper(fragments));
        else
            backStack = new BackStack(helper);

        backStack.restoreRestOfFragmentsFromTags(onRestoreInstanceState(savedInstanceState));

        return backStack;
    }

    // region Restore Rest of fragments from tags

    private void restoreRestOfFragmentsFromTags(@NonNull List<Integer> savedTags) {
        restoreFragmentsFromTagsWrapper(savedTags);
    }

    private void restoreFragmentsFromTagsWrapper(@NonNull List<Integer> savedTags) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            restoreFragmentsFromTags(savedTags);
        } else
            restoreFragmentsFromTagsLegacy(savedTags);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void restoreFragmentsFromTags(@NonNull List<Integer> savedTags) {
        savedTags
                .forEach(this::generateFragmentFromTagIfNeeded);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void restoreFragmentsFromTagsLegacy(@NonNull List<Integer> savedTags) {
        for (int tag: savedTags) {
            generateFragmentFromTagIfNeeded(tag);
        }
    }

    private void generateFragmentFromTagIfNeeded(@StringRes int tag) {
        if (findFragmentByTag(tag) == null)
            backStack.add(helper.generateFragmentFromTag(tag));
    }

    // endregion

    // region Restore From FragmentManager.getFragments()

    @NonNull
    private static List<MyFragment> restoreBackStackWrapper(@NonNull List<Fragment> fragments) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return restoreBackStack(fragments);
        } else
            return restoreBackStackLegacy(fragments);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private static List<MyFragment> restoreBackStack(@NonNull List<Fragment> fragments) {
        return fragments
                .stream()
                .filter(fragment -> fragment instanceof MyFragment)
                .map(fragment -> (MyFragment) fragment)
                .collect(Collectors.toList());
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private static List<MyFragment> restoreBackStackLegacy(@NonNull List<Fragment> fragments) {
        List<MyFragment> list = new ArrayList<>();
        for (Fragment fragment: fragments) {
            if (fragment instanceof MyFragment)
                list.add((MyFragment) fragment);
        }
        return list;
    }

    // endregion

    // region Restore tags from savedInstanceState

    private static List<Integer> onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        return onRestoreInstanceStateWrapper(savedInstanceState, savedInstanceState.getInt(SAVE_STATE_SIZE));
    }

    private static List<Integer> onRestoreInstanceStateWrapper(@NonNull Bundle savedInstanceState, int size) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return restoreInstanceState(savedInstanceState, size);
        } else
            return restoreInstanceStateLegacy(savedInstanceState, size);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private static List<Integer> restoreInstanceState(@NonNull Bundle savedInstanceState, int size) {
        return IntStream.of(size - 1)
                .map(i -> savedInstanceState.getInt(SAVE_STATE_TAG + i))
                .boxed()
                .collect(Collectors.toList());
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private static List<Integer> restoreInstanceStateLegacy(@NonNull Bundle savedInstanceState, int size) {
        List<Integer> tags = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            tags.add(savedInstanceState.getInt(SAVE_STATE_TAG + i));
        }
        return tags;
    }

    // endregion

    // region Save tags state

    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putInt(SAVE_STATE_SIZE, size());
        onSaveInstanceStateWrapper(outState);
    }

    private void onSaveInstanceStateWrapper(@NonNull Bundle outState) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            saveInstanceState(outState);
        } else
            saveInstanceStateLegacy(outState);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void saveInstanceState(@NonNull Bundle outState) {
        if (size() > 0) {
            IntStream.of(size() - 1)
                    .forEach(i -> outState.putInt(SAVE_STATE_TAG + i, backStack.get(i).getMyTag()));
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void saveInstanceStateLegacy(@NonNull Bundle outState) {
        for (int i = 0; i < backStack.size(); i++) {
            outState.putInt(SAVE_STATE_TAG + i, backStack.get(i).getMyTag());
        }
    }

    // endregion

    // endregion

    public interface BackStackHelper {
        MyFragment generateFragmentFromTag(@StringRes int tag);
    }
}
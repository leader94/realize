package com.ps.realize.ui.directory;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.ps.realize.MyApp;
import com.ps.realize.R;
import com.ps.realize.core.datamodels.ar.ImageMapping;
import com.ps.realize.core.datamodels.ar.ImageObj;
import com.ps.realize.core.helperClasses.DBHelper;
import com.ps.realize.core.helperClasses.SceneFragmentHelper;
import com.ps.realize.core.interfaces.CallbackListener;
import com.ps.realize.databinding.FragmentDirectoryBinding;
import com.ps.realize.utils.KeyboardUtils;
import com.ps.realize.utils.LayoutUtils;

import java.util.List;

public class DirectoryFragment extends Fragment {

    private static final String TAG = DirectoryFragment.class.getSimpleName();
    private Fragment _this;
    private FragmentDirectoryBinding binding;


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        _this = this;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentDirectoryBinding.inflate(inflater, container, false);
        View root = binding.getRoot();


        setViews();
        setOnClickListeners();
        // Inflate the layout for this fragment
        return root;
    }

    private void setOnClickListeners() {

        ImageView backBtn = binding.directoryFragmentBackBtn;
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                KeyboardUtils.backPress(_this);
            }
        });
    }

    private void setViews() {
        if (binding == null) return;
        LinearLayout ll = binding.directoryFragmentDataLl;

        List<ImageMapping> imageMappingList = SceneFragmentHelper.getImageMappings();

        for (ImageMapping imageMapping : imageMappingList) {
            ImageObj imageToBeTracked = imageMapping.getImage();

            if (imageToBeTracked.getLocalUrl() != null) {
                addSuggestionBox(ll, imageToBeTracked);
            } else {
                addSuggestionBox(ll, imageToBeTracked);
            }
        }
    }

    private void addSuggestionBox(LinearLayout parentLinearLayout, ImageObj imageObj) {
        String url = imageObj.getLocalUrl();
        if (url == null) {
            url = imageObj.getUrl();
        }
        ImageView view = new ImageView(MyApp.getContext());

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutUtils.dpToPx(135), LayoutUtils.dpToPx(135));
        params.setMargins(0, 0, LayoutUtils.dpToPx(35), 0);

        view.setLayoutParams(params);
        view.setBackgroundResource(R.drawable.card);

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showBottomSheet(v, imageObj);
            }
        });

        Glide.
                with(MyApp.getContext())
                .load(url)
                .into(view);

        parentLinearLayout.addView(view);
    }

    private void showBottomSheet(View imageView, ImageObj imageObj) {
        View view = getLayoutInflater().inflate(R.layout.directory_bottom_sheet, null);
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getContext());
        bottomSheetDialog.setContentView(view);

        TextView textDelete = view.findViewById(R.id.dir_btm_sheet_text_delete);
        textDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                CallbackListener callbackListener = new CallbackListener() {
                    @Override
                    public void onSuccess() {
                        imageView.setVisibility(View.GONE);
                        bottomSheetDialog.dismiss();


                    }

                    @Override
                    public void onSucessWithData() {
                    }

                    @Override
                    public void onFailure() {
//                        imageView.setVisibility(View.GONE);
                        bottomSheetDialog.dismiss();
                    }
                };

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        deleteAndUpdateDB(imageObj, callbackListener);
                    }
                });


            }
        });

        bottomSheetDialog.show();
    }


    private void deleteAndUpdateDB(ImageObj imageObj, CallbackListener callbackListener) {
        boolean deleted = SceneFragmentHelper.deleteSceneObject(imageObj.getId());
        if (!deleted) {
            callbackListener.onFailure();
            return;
        }
        DBHelper.updateUserProjectsInDB(getActivity(), callbackListener);
    }


}
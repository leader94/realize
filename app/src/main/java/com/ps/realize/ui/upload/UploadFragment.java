package com.ps.realize.ui.upload;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.ps.realize.core.data.LocalData;
import com.ps.realize.core.interfaces.NetworkListener;
import com.ps.realize.databinding.FragmentUploadBinding;
import com.ps.realize.utils.Constants;
import com.ps.realize.utils.JSONUtils;
import com.ps.realize.utils.KeyboardUtils;
import com.ps.realize.utils.MediaUtils;
import com.ps.realize.utils.NetworkUtils;
import com.ps.realize.utils.ProgressRequestBody;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;

import okhttp3.Request;
import okhttp3.Response;

public class UploadFragment extends Fragment {
    private final String TAG = UploadFragment.class.getSimpleName();

    private final Constants constants = new Constants();
    private int baseUploadProg = 0;
    private int overlayUploadProg = 0;
    private FragmentUploadBinding binding;
    private String targetVideoURIString, targetImageURIString;
    private Fragment _this;

    private ProgressBar progressBar;
    private TextView uploadingLabel;
    private ImageView baseThumbnail, uploadedTick;
    private LinearLayout createAnother;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        _this = this;

    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        targetImageURIString = getArguments().getString(constants.TARGET_IMAGE_URI);
        targetVideoURIString = getArguments().getString(constants.TARGET_VIDEO_URI);

        binding = FragmentUploadBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        setViews();

        _startUploading();
        return root;
    }

    private void setViews() {
        final ImageView backBtn = binding.uploadBackBtn;
        progressBar = binding.uploadProgressBar;
        uploadingLabel = binding.uploadUploadingLabel;
        baseThumbnail = binding.uploadBaseThumbnail;
        createAnother = binding.uploadRlBottomBar;
        uploadedTick = binding.uploadUplaodedTick;

        createAnother.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
// todo popback stack and redirect to dashboard
            }
        });

        backBtn.setOnClickListener(view -> KeyboardUtils.backPress(_this));
        Glide.with(_this)
                .load(targetImageURIString)
                .into(baseThumbnail);
    }


    private void _startUploading() {
        _getURLAndUpload();
    }

    private void _getURLAndUpload() {
        String uploadReqBody = _getRequestBody();
        NetworkUtils.postWithToken("/signedlink", uploadReqBody, LocalData.curUser.getToken(), new NetworkListener() {
            @Override
            public void onFailure(Request request, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Response response) {
                try {
                    JSONObject resJSON = JSONUtils.getJSONObject(response);
                    JSONObject base = resJSON.getJSONObject("base");
                    JSONObject overlay = resJSON.getJSONObject("overlay");
                    _upload(base, overlay);
                } catch (Exception e) {
                    e.printStackTrace();
                }


            }
        });


    }

    private void _upload(JSONObject base, JSONObject overlay) {
        try {
            ContentResolver resolver = getContext().getContentResolver();

            String baseUriString = base.getString("localPath");
            JSONObject baseUploadKeys = base.getJSONObject("upload");
            String baseUploadUrl = baseUploadKeys.getString("url");
            JSONObject baseFormFields = baseUploadKeys.getJSONObject("fields");


            Uri baseUri = Uri.parse(baseUriString);
            InputStream baseInputStream = resolver.openInputStream(baseUri);
            String baseFileName = base.getString("originalName");
            String baseMimeType = base.getString("originalExtn");
            NetworkUtils.postWithMultipart(baseUploadUrl, baseFormFields, baseInputStream, baseFileName, baseMimeType, new NetworkListener() {
                @Override
                public void onFailure(Request request, IOException e) {
                    Log.e(TAG, "Failed to upload base to " + baseUploadUrl);
                }

                @Override
                public void onResponse(Response response) {
                    Log.i(TAG, "Successfully uploaded base to " + baseUploadUrl);
                }
            }, getBaseUploadProgListener());

            String overlayUriString = overlay.getString("localPath");
            JSONObject overlayUploadKeys = overlay.getJSONObject("upload");
            String overlayUploadUrl = overlayUploadKeys.getString("url");
            JSONObject overlayFormFields = overlayUploadKeys.getJSONObject("fields");

            Uri overlayUri = Uri.parse(overlayUriString);
            InputStream overlayInputStream = resolver.openInputStream(overlayUri);
            String overlayFileName = overlay.getString("originalName");
            String overlayMimeType = overlay.getString("originalExtn");
            NetworkUtils.postWithMultipart(overlayUploadUrl, overlayFormFields, overlayInputStream, overlayFileName, overlayMimeType, new NetworkListener() {
                @Override
                public void onFailure(Request request, IOException e) {
                    Log.e(TAG, "Failed to upload overlay to " + baseUploadUrl);
                }

                @Override
                public void onResponse(Response response) {
                    Log.i(TAG, "Successfully uploaded overlay to " + overlayUploadUrl);
                }
            }, getOverlayUploadProgListener());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private String _getRequestBody() {
        Uri baseUri = Uri.parse(targetImageURIString);
        String baseFileExtn = MediaUtils.getMimeType(getContext(), baseUri);
        String baseFileName = MediaUtils.getFileName(getContext(), baseUri);
        Long baseFileSize = MediaUtils.getFileSize(getContext(), baseUri);

        Uri overlayUri = Uri.parse(targetVideoURIString);
        String overlayFileExtn = MediaUtils.getMimeType(getContext(), overlayUri);
        String overlayFileName = MediaUtils.getFileName(getContext(), overlayUri);
        Long overlayFileSize = MediaUtils.getFileSize(getContext(), overlayUri);


        try {
            JSONObject reqBody = new JSONObject();
            JSONObject baseOptions = new JSONObject();
            JSONObject overlayOptions = new JSONObject();

            baseOptions.put("fileName", baseFileName);
            baseOptions.put("extn", baseFileExtn);
            baseOptions.put("contentLength", baseFileSize);
            baseOptions.put("localPath", baseUri);

            overlayOptions.put("fileName", overlayFileName);
            overlayOptions.put("extn", overlayFileExtn);
            overlayOptions.put("contentLength", overlayFileSize);
            overlayOptions.put("localPath", overlayUri);

            reqBody.put("base", baseOptions);
            reqBody.put("overlay", overlayOptions);
            return reqBody.toString();

        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }

    }

    private ProgressRequestBody.Listener getBaseUploadProgListener() {
        baseUploadProg = 0;
        return progress -> {
            baseUploadProg = progress;
            updateProgressBar();
        };

    }


    private ProgressRequestBody.Listener getOverlayUploadProgListener() {
        overlayUploadProg = 0;
        return progress -> {
            overlayUploadProg = progress;
            updateProgressBar();
        };
    }

    private void updateProgressBar() {
        int totalProgress = getTotalUplaodProgress();
        progressBar.setProgress(totalProgress, true);
        if (totalProgress == 100) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    uploadingLabel.setText("Uploaded");
                    uploadedTick.setVisibility(View.VISIBLE);
                }
            });

        }
//        else {
//            uploadingLabel.setText("Uploading");
//        }
    }

    private int getTotalUplaodProgress() {
        if ((baseUploadProg + overlayUploadProg) == 0) {
            return 0;
        } else return (int) (100 * (baseUploadProg + overlayUploadProg) / 200);
    }

}

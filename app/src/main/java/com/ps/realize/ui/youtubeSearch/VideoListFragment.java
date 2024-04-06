package com.ps.realize.ui.youtubeSearch;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ps.realize.R;
import com.ps.realize.core.datamodels.api.YoutubeSearchDM;
import com.ps.realize.core.datamodels.internal.VideoItem;
import com.ps.realize.core.interfaces.NetworkListener;
import com.ps.realize.databinding.FragmentVideoListBinding;
import com.ps.realize.utils.JSONUtils;
import com.ps.realize.utils.NetworkUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Request;
import okhttp3.Response;

public class VideoListFragment extends Fragment {
    private final String TAG = VideoListFragment.class.getSimpleName();
    private RecyclerView recyclerView;
    private SearchView searchView;
    private VideoThumbnailAdapter adapter;
    private String youtubeSearchDMStr;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        FragmentVideoListBinding binding = FragmentVideoListBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        if (youtubeSearchDMStr != null) {
            handleYTSearchSuccessResponse();
        }

        return root;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.recycler_view);
        // TODO fix search view
        searchView = view.findViewById(R.id.search_view);

        if (youtubeSearchDMStr == null) {
            List<VideoItem> videoItemList = new ArrayList<>();
            adapter = new VideoThumbnailAdapter(videoItemList);
        }
        recyclerView.setAdapter(adapter);

        // Set up RecyclerView
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 1));


        // Set up search functionality
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Perform search operation
                fetchDataAndSetView(query);
                return false;
            }

            /**
             * @param newText the new content of the query text field.
             * @return
             */
            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
    }

    private void fetchDataAndSetView(String query) {
        // TODO start loader

        NetworkUtils.get("https://apimocha.com/testingapprealise/youtube/search?query=" + query, null, new NetworkListener() {
            @Override
            public void onFailure(Request request, IOException e) {
                // TODO add handling
            }

            @Override
            public void onResponse(Response response) {
                try {
                    youtubeSearchDMStr = response.body().string();
                } catch (Exception e) {
                    //TODO handle error here
                    Log.e(TAG, e.toString());
                }
                handleYTSearchSuccessResponse();

            }
        });
    }

    private void handleYTSearchSuccessResponse() {

        if (youtubeSearchDMStr.length() == 0) {
            return;
        }

        YoutubeSearchDM youtubeSearchDM = JSONUtils.getGsonParser().fromJson(youtubeSearchDMStr, YoutubeSearchDM.class);
        List<VideoItem> videoItems = youtubeSearchDM.getItems();
        adapter = new VideoThumbnailAdapter(videoItems);
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // TODO remove loader
                recyclerView.setAdapter(adapter);
            }
        });

    }
}

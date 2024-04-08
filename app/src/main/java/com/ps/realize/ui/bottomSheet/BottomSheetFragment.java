package com.ps.realize.ui.bottomSheet;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.ps.realize.R;

import java.util.List;

public class BottomSheetFragment extends BottomSheetDialogFragment {

    private final List<ItemTextBottomSheetItem> itemTextBottomSheetItems;
    private RecyclerView recyclerView;
    private ItemClickListener itemClickListener;

    /**
     *
     */
    public BottomSheetFragment(List<ItemTextBottomSheetItem> itemTextBottomSheetItems) {
        super();
        this.itemTextBottomSheetItems = itemTextBottomSheetItems;
    }

    public static BottomSheetFragment newInstance(List<ItemTextBottomSheetItem> itemTextBottomSheetItems, ItemClickListener itemClickListener) {
        BottomSheetFragment fragment = new BottomSheetFragment(itemTextBottomSheetItems);
//        Bundle args = new Bundle();
//        args.putString(JSONUtils.getGsonParser().toJson(itemTextBottomSheetItems));
//        args.putIntArray("icons", icons);
//        fragment.setArguments(args);
        fragment.setIconClickListener(itemClickListener);
        return fragment;
    }

    private void setIconClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_layout, container, false);
        recyclerView = view.findViewById(R.id.recycler_view);


        if (itemTextBottomSheetItems != null) {
            IconTextAdapter iconTextAdapter = new IconTextAdapter(itemTextBottomSheetItems);
            recyclerView.setAdapter(iconTextAdapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        }

        return view;
    }

    public interface ItemClickListener {
        void onItemClick(ItemTextBottomSheetItem itemTextBottomSheetItem);
    }

    // Adapter class for RecyclerView
    private class IconTextAdapter extends RecyclerView.Adapter<IconTextAdapter.IconTextViewHolder> {

        private final List<ItemTextBottomSheetItem> itemTextBottomSheetItems;
//        private final int[] icons;

        public IconTextAdapter(List<ItemTextBottomSheetItem> itemTextBottomSheetItems) {
            this.itemTextBottomSheetItems = itemTextBottomSheetItems;
        }

        @NonNull
        @Override
        public IconTextViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.bottom_sheet_r_view_items, parent, false);
            return new IconTextViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull IconTextViewHolder holder, int position) {
            ItemTextBottomSheetItem item = itemTextBottomSheetItems.get(position);
            holder.iconImageView.setImageResource(item.getIcon());
            holder.iconTextView.setText(item.getText());
        }

        @Override
        public int getItemCount() {
            return itemTextBottomSheetItems.size();
        }

        public class IconTextViewHolder extends RecyclerView.ViewHolder {
            private final View itemView;
            private final ImageView iconImageView;
            private final TextView iconTextView;

            public IconTextViewHolder(@NonNull View itemView) {
                super(itemView);
                this.itemView = itemView;
                iconImageView = itemView.findViewById(R.id.icon_image_view);
                iconTextView = itemView.findViewById(R.id.icon_text_view);
                itemView.setOnClickListener(v -> {
                    if (itemClickListener != null) {
                        dismiss(); // Close the bottom sheet dialog
                        itemClickListener.onItemClick(itemTextBottomSheetItems.get(getBindingAdapterPosition()));
                    }

                });
            }
        }
    }
}


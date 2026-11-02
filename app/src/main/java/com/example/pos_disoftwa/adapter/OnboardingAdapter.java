package com.example.pos_disoftwa.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pos_disoftwa.R;
import com.example.pos_disoftwa.models.OnboardingModels;

import java.util.List;

public class OnboardingAdapter extends RecyclerView.Adapter<OnboardingAdapter.OnboardingViewHolder> {

    private List<OnboardingModels> onboardingList;

    public OnboardingAdapter(List<OnboardingModels> onboardingList) {
        this.onboardingList = onboardingList;
    }

    @NonNull
    @Override
    public OnboardingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_onboarding, parent, false);
        return new OnboardingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OnboardingViewHolder holder, int position) {
        OnboardingModels item = onboardingList.get(position);

        holder.imageView.setImageResource(item.getImageResId());
        holder.title.setText(item.getTitle());
        holder.description.setText(item.getDescription());
    }

    @Override
    public int getItemCount() {
        return onboardingList.size();
    }

    public static class OnboardingViewHolder extends RecyclerView.ViewHolder {

        ImageView imageView;
        TextView title, description;

        public OnboardingViewHolder(@NonNull View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.ivOnboardingImage);
            title = itemView.findViewById(R.id.tvOnboardingTitle);
            description = itemView.findViewById(R.id.tvOnboardingDescription);
        }
    }
}
package com.parkmate.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

public class OnboardingActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private Button btnNext;
    private LinearLayout dotsLayout;
    private int[] images = {R.drawable.onboarding1, R.drawable.onboarding2, R.drawable.onboarding3};
    private String[] titles = {"Navigate Your Ride", "Find Place to Park", "Easy To Find"};
    private String[] descriptions = {
            "This application provide a real time\ntracking and navigation\nof your ride.",
            "Find a suitable place to\npark your ride.",
            "Park Mate makes it easy\nto find where you\nparked."
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        viewPager = findViewById(R.id.viewPager);
        btnNext = findViewById(R.id.btnNext);
        dotsLayout = findViewById(R.id.dotsLayout);
        TextView tvSkip = findViewById(R.id.tvSkip);

        viewPager.setAdapter(new OnboardingAdapter());
        setupDots(0);

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                setupDots(position);
                if (position == 2) {
                    btnNext.setText("Get Started");
                } else {
                    btnNext.setText("Next");
                }
            }
        });

        btnNext.setOnClickListener(v -> {
            if (viewPager.getCurrentItem() < 2) {
                viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
            } else {
                goToSignIn();
            }
        });

        tvSkip.setOnClickListener(v -> goToSignIn());
    }

    private void goToSignIn() {
        getSharedPreferences("PREFERENCE", MODE_PRIVATE).edit()
                .putBoolean("isFirstRun", false).apply();
        startActivity(new Intent(this, SignInActivity.class));
        finish();
    }

    private void setupDots(int current) {
        dotsLayout.removeAllViews();
        for (int i = 0; i < 3; i++) {
            View dot = new View(this);
            int height = (int) (8 * getResources().getDisplayMetrics().density);
            int width = i == current ? height * 3 : height;
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, height);
            params.setMargins(6, 0, 6, 0);
            dot.setLayoutParams(params);
            dot.setBackgroundResource(i == current ? R.drawable.dot_active : R.drawable.dot_inactive);
            dotsLayout.addView(dot);
        }
    }

    class OnboardingAdapter extends RecyclerView.Adapter<OnboardingAdapter.ViewHolder> {
        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_onboarding, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.ivImage.setImageResource(images[position]);
            holder.tvTitle.setText(titles[position]);
            holder.tvDesc.setText(descriptions[position]);
        }

        @Override
        public int getItemCount() { return 3; }

        class ViewHolder extends RecyclerView.ViewHolder {
            ImageView ivImage;
            TextView tvTitle, tvDesc;
            ViewHolder(View v) {
                super(v);
                ivImage = v.findViewById(R.id.ivOnboarding);
                tvTitle = v.findViewById(R.id.tvTitle);
                tvDesc = v.findViewById(R.id.tvDescription);
            }
        }
    }
}
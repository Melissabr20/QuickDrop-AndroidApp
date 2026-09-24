package com.example.QuickDrop.ui.admin.adapters;

import com.example.QuickDrop.R;
import com.example.QuickDrop.core.network.dto.LoginResponse;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.request.RequestOptions;
import java.util.List;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private List<LoginResponse.User> userList;
    private Context context;

    public UserAdapter(Context context, List<LoginResponse.User> userList) {
        this.context = context;
        this.userList = userList;
    }

    @Override
    public UserViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_users, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(UserViewHolder holder, int position) {
        LoginResponse.User user = userList.get(position);
        holder.bind(user);
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public class UserViewHolder extends ViewHolder {

        private TextView nameTextView;
        private TextView ageTextView;

        public UserViewHolder(View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.name);
            ageTextView = itemView.findViewById(R.id.email);
        }

        public void bind(LoginResponse.User user) {
            nameTextView.setText(user.getUsername());
            ageTextView.setText(String.valueOf(user.getEmail()));
        }
    }
}

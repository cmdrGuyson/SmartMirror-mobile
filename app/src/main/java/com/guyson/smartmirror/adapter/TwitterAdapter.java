package com.guyson.smartmirror.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.guyson.smartmirror.R;
import com.guyson.smartmirror.model.NewsArticle;
import com.guyson.smartmirror.model.TwitterArticle;
import com.guyson.smartmirror.model.User;
import com.guyson.smartmirror.util.ExtraUtilities;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TwitterAdapter extends RecyclerView.Adapter<TwitterAdapter.ViewHolder> implements Filterable {

    private Context context;
    private List<TwitterArticle> articles;
    private List<TwitterArticle> filteredArticles;

    public void setUser(User user) {
        this.user = user;
    }

    private User user;
    private String configurableType;

    public TwitterAdapter(Context context, List<TwitterArticle> articles, User user, String configurableType) {
        this.context = context;
        this.articles = articles;
        this.user = user;
        this.configurableType = configurableType;
    }

    public void setArticles(final List<TwitterArticle> articles){
        if(this.articles == null){
            this.articles = articles;
            this.filteredArticles = articles;
            //Alert a change in items
            notifyItemChanged(0, filteredArticles.size());
        }
        //If updating items (previously not null)
        else {
            final DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtil.Callback() {
                @Override
                public int getOldListSize() {
                    return TwitterAdapter.this.articles.size();
                }

                @Override
                public int getNewListSize() {
                    return articles.size();
                }

                @Override
                public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                    return TwitterAdapter.this.articles.get(oldItemPosition).getUid().equals(articles.get(newItemPosition).getUid());
                }

                @Override
                public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {

                    TwitterArticle newArticle = TwitterAdapter.this.articles.get(oldItemPosition);

                    TwitterArticle oldArticle = articles.get(newItemPosition);

                    return newArticle.getUid().equals(oldArticle.getUid());
                }
            });
            this.articles = articles;
            this.filteredArticles = articles;
            result.dispatchUpdatesTo(this);
            notifyDataSetChanged();
        }
    }

    @NonNull
    @Override
    public TwitterAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.twitter_row, parent,false);
        return new ViewHolder(view);
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public void onBindViewHolder(@NonNull TwitterAdapter.ViewHolder holder, final int position) {
        holder.mTitle.setText(filteredArticles.get(position).getTitle());
        holder.mDescription.setText(filteredArticles.get(position).getDescription());
        holder.mType.setText(ExtraUtilities.formatType(filteredArticles.get(position).getType()));

        //Load image
        Picasso.get().load(filteredArticles.get(position).getImageUrl()).placeholder(R.drawable.placeholder).into(holder.mImage);

        boolean isSubscribed = false;

        if(user!=null){

            //IF USER IS CONFIGURING HAPPY EMOTIONAL ARTICLES
            if(configurableType.equals("happy")){
                if(user.getHappy()==null) user.setHappy(new ArrayList<String>());

                //Check if user already subscribed to article
                for (String n : user.getHappy()) {
                    if(n.equals(filteredArticles.get(position).getUid())) {
                        isSubscribed = true;
                        holder.mSwitch.setChecked(true);
                        break;
                    }
                }


                //Handle switch button
                final boolean finalIsSubscribed = isSubscribed;

                holder.mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {

                        List<String> list = user.getHappy();

                        //If user is subscribed to article
                        if (finalIsSubscribed) {
                            list.remove(filteredArticles.get(position).getUid());
                            compoundButton.setChecked(false);
                        } else {
                            list.add(filteredArticles.get(position).getUid());
                            compoundButton.setChecked(true);
                        }


                        //Update user object
                        user.setHappy(list);

                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("user").child(user.getUid());
                        ref.child("happy").setValue(user.getHappy());

                    }
                });
            }
            // IF USER IS CONFIGURING SAD EMOTIONAL ARTICLES
            else if (configurableType.equals("sad")){
                if(user.getSad()==null) user.setSad(new ArrayList<String>());

                //Check if user already subscribed to article
                for (String n : user.getSad()) {
                    if(n.equals(filteredArticles.get(position).getUid())) {
                        isSubscribed = true;
                        holder.mSwitch.setChecked(true);
                        break;
                    }
                }


                //Handle switch button
                final boolean finalIsSubscribed = isSubscribed;

                holder.mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {

                        List<String> list = user.getSad();

                        //If user is subscribed to article
                        if (finalIsSubscribed) {
                            list.remove(filteredArticles.get(position).getUid());
                            compoundButton.setChecked(false);
                        } else {
                            list.add(filteredArticles.get(position).getUid());
                            compoundButton.setChecked(true);
                        }


                        //Update user object
                        user.setSad(list);

                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("user").child(user.getUid());
                        ref.child("sad").setValue(user.getSad());

                    }
                });
            }
            // IF USER IS CONFIGURING NEUTRAL EMOTIONAL ARTICLES
            else {
                if(user.getNeutral()==null) user.setNeutral(new ArrayList<String>());

                //Check if user already subscribed to article
                for (String n : user.getNeutral()) {
                    if(n.equals(filteredArticles.get(position).getUid())) {
                        isSubscribed = true;
                        holder.mSwitch.setChecked(true);
                        break;
                    }
                }


                //Handle switch button
                final boolean finalIsSubscribed = isSubscribed;

                holder.mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {

                        List<String> list = user.getNeutral();

                        //If user is subscribed to article
                        if (finalIsSubscribed) {
                            list.remove(filteredArticles.get(position).getUid());
                            compoundButton.setChecked(false);
                        } else {
                            list.add(filteredArticles.get(position).getUid());
                            compoundButton.setChecked(true);
                        }


                        //Update user object
                        user.setNeutral(list);

                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("user").child(user.getUid());
                        ref.child("neutral").setValue(user.getNeutral());

                    }
                });
            }
        }
    }

    @Override
    public int getItemCount() {
        if(filteredArticles != null ) return filteredArticles.size();
        return 0;
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString();
                if (charString.isEmpty()) {
                    filteredArticles = articles;
                } else {
                    List<TwitterArticle> filteredList = new ArrayList<>();
                    for (TwitterArticle article : articles) {
                        //Search through ID and type
                        if (article.getTitle().toLowerCase().contains(charString.toLowerCase())) {
                            filteredList.add(article);
                            Log.i("LOG", article.getTitle());
                        }
                    }
                    filteredArticles = filteredList;
                }
                FilterResults filterResults = new FilterResults();
                filterResults.values = filteredArticles;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                filteredArticles = (ArrayList<TwitterArticle>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView mTitle, mDescription, mType;
        ImageView mImage;
        SwitchMaterial mSwitch;


        public ViewHolder(View itemView) {
            super(itemView);

            mTitle = itemView.findViewById(R.id.title);
            mImage = itemView.findViewById(R.id.image);
            mSwitch = itemView.findViewById(R.id.add_switch);
            mDescription = itemView.findViewById(R.id.description);
            mType = itemView.findViewById(R.id.type);

        }
    }
}

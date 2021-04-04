package com.guyson.smartmirror.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.guyson.smartmirror.R;
import com.guyson.smartmirror.model.NewsArticle;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.ViewHolder> implements Filterable {

    private Context context;
    private List<NewsArticle> news;
    private List<NewsArticle> filteredNews;

    public NewsAdapter(Context context, List<NewsArticle> news) {
        this.context = context;
        this.news = news;
    }

    public void setNews(final List<NewsArticle> news){
        if(this.news == null){
            this.news = news;
            this.filteredNews = news;
            //Alert a change in items
            notifyItemChanged(0, filteredNews.size());
        }
        //If updating items (previously not null)
        else {
            final DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtil.Callback() {
                @Override
                public int getOldListSize() {
                    return NewsAdapter.this.news.size();
                }

                @Override
                public int getNewListSize() {
                    return news.size();
                }

                @Override
                public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                    return NewsAdapter.this.news.get(oldItemPosition).getUid().equals(news.get(newItemPosition).getUid());
                }

                @Override
                public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {

                    NewsArticle newArticle = NewsAdapter.this.news.get(oldItemPosition);

                    NewsArticle oldArticle = news.get(newItemPosition);

                    return newArticle.getUid().equals(oldArticle.getUid()) ;
                }
            });
            this.news = news;
            this.filteredNews = news;
            result.dispatchUpdatesTo(this);
            notifyDataSetChanged();
        }
    }

    @NonNull
    @Override
    public NewsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.news_row, parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NewsAdapter.ViewHolder holder, final int position) {
        holder.mTitle.setText(filteredNews.get(position).getTitle());
        holder.mDescription.setText(filteredNews.get(position).getDescription());

        //Load image
        Picasso.get().load(filteredNews.get(position).getImageUrl()).placeholder(R.drawable.entertainment).into(holder.mImage);

        //Handle switch button
    }

    @Override
    public int getItemCount() {
        if(filteredNews != null ) return filteredNews.size();
        return 0;
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString();
                if (charString.isEmpty()) {
                    filteredNews = news;
                } else {
                    List<NewsArticle> filteredList = new ArrayList<>();
                    for (NewsArticle article : news) {
                        //Search through ID and type
                        if (article.getTitle().toLowerCase().contains(charString.toLowerCase())) {
                            filteredList.add(article);
                            Log.i("LOG", article.getTitle());
                        }
                    }
                    filteredNews = filteredList;
                }
                FilterResults filterResults = new FilterResults();
                filterResults.values = filteredNews;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                filteredNews = (ArrayList<NewsArticle>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView mTitle, mDescription;
        ImageView mImage;
        SwitchMaterial mSwitch;


        public ViewHolder(View itemView) {
            super(itemView);

            mTitle = itemView.findViewById(R.id.title);
            mImage = itemView.findViewById(R.id.image);
            mSwitch = itemView.findViewById(R.id.add_switch);
            mDescription = itemView.findViewById(R.id.description);

        }
    }
}

package com.games.orodreth.warframeinventory;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class Adapter extends RecyclerView.Adapter<Adapter.ItemViewHolder> {
    public static final int ADD_ONE = 666;
    public static final int REMOVE_ONE = 777;
    public static final int REMOVE_ALL = 888;
    private Context mContext;
    private ArrayList<Items> mItems;
    private OnItemClickListener mListener;
    private int imageSource;

    public void filteredList(ArrayList<Items> filteredList) {
        mItems = filteredList;
        notifyDataSetChanged();
    }

    public interface OnItemClickListener{
        void onItemClick(int position);
    }

    public void setOnItemListener(OnItemClickListener listener){
        mListener = listener;
    }

    public Adapter(Context context, ArrayList<Items> items){
        mContext = context;
        mItems = items;
        imageSource = 0;
    }

    public void setImageSource(int source) {
        imageSource = source;
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.item, parent, false);
        return new ItemViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ItemViewHolder holder, int position) {
        Items currentItem = mItems.get(position);
        String imageUrl;
        if (imageSource==0) imageUrl = new String("https://cdn.warframestat.us/img/"+currentItem.getImageUrl());
        else imageUrl = new String("https://warframe.market/static/assets/"+currentItem.getImageUrl());
        //String imageUrl = currentItem.getImageUrl();
        String itemName = currentItem.getItem();
        int itemDucats = currentItem.getDucats();
        int itemPlats = currentItem.getPlat();
        float itemDucPlat = currentItem.getDucPlat();
        String stringDucPlat = String.format("%.2f",itemDucPlat);

        holder.mTextViewItem.setText(itemName);
        holder.mTextViewDucats.setText(mContext.getResources().getString(R.string.ducats) + itemDucats);
        holder.mTextViewPlats.setText(mContext.getResources().getString(R.string.platinum) + itemPlats);
        holder.mTextViewDucPlat.setText(mContext.getResources().getString(R.string.duc_plat) + stringDucPlat);
        Picasso.with(mContext).load(imageUrl).fit().centerInside().into(holder.mImageView); //working with picasso 2.5.2
        //Picasso.get().load(imageUrl).fit().centerInside().into(holder.mImageView);        TODO workaroud since with() method is deprecated on future picasso release
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener {
        public ImageView mImageView;
        public TextView mTextViewItem;
        public TextView mTextViewDucats;
        public TextView mTextViewPlats;
        public TextView mTextViewDucPlat;
        public CardView mcardView;

        public ItemViewHolder(View itemView) {
            super(itemView);
            mImageView = itemView.findViewById(R.id.image_view);
            mTextViewItem = itemView.findViewById(R.id.text_view_name);
            mTextViewDucats = itemView.findViewById(R.id.text_view_duc);
            mTextViewPlats = itemView.findViewById(R.id.text_view_plat);
            mTextViewDucPlat = itemView.findViewById(R.id.text_view_duc_plat);
            mcardView = itemView.findViewById(R.id.cardview);
            mcardView.setOnCreateContextMenuListener(this);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(mListener != null){
                        int position = getAdapterPosition();
                        if(position!=RecyclerView.NO_POSITION){
                            mListener.onItemClick(position);
                        }
                    }
                }
            });
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            menu.setHeaderTitle(mItems.get(getAdapterPosition()).getItem());
            menu.add(getAdapterPosition(),ADD_ONE, 0, "add 1");
            menu.add(getAdapterPosition(),REMOVE_ONE, 0, "remove 1");
            menu.add(getAdapterPosition(),REMOVE_ALL, 0, "remove all");

        }
    }
}

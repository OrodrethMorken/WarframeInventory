package com.games.orodreth.warframeinventory;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.games.orodreth.warframeinventory.repository.database.Items;
import com.games.orodreth.warframeinventory.repository.database.ItemsAndInventory;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Adapter extends RecyclerView.Adapter<Adapter.ItemViewHolder> {
    public static final int ADD_ONE = 666;
    public static final int REMOVE_ONE = 777;
    public static final int REMOVE_ALL = 888;
    private Context mContext;
    private ArrayList<ItemsAndInventory> mItems;
    private OnItemClickListener mListener;
    private int imageSource;

    public interface OnItemClickListener{
        void onItemClick(int position);
    }

    public void setOnItemListener(OnItemClickListener listener){
        mListener = listener;
    }

    public Adapter(Context context, List<ItemsAndInventory> items){
        mContext = context;
        mItems = (ArrayList<ItemsAndInventory>)items;
        imageSource = 0;
    }

    public void setImageSource(int source) {
        imageSource = source;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.item, parent, false);
        return new ItemViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        ItemsAndInventory currentItem = mItems.get(position);
        String imageUrl;
        if (imageSource==0) imageUrl = "https://cdn.warframestat.us/img/" + currentItem.items.getImageUrl();
        else imageUrl = "https://warframe.market/static/assets/" + currentItem.items.getImageUrl();
        //String imageUrl = currentItem.getImageUrl();
        String itemName = currentItem.items.getName();
        int itemDucats = currentItem.items.getDucat();
        int itemPlats = currentItem.items.getPlat();
        double itemDucPlat = currentItem.items.getDucPlat();

        holder.mTextViewItem.setText(itemName);
        holder.mTextViewDucats.setText(String.format(Locale.getDefault(),"%s%d", mContext.getResources().getString(R.string.ducats), itemDucats));
        holder.mTextViewPlats.setText(String.format(Locale.getDefault(),"%s%d", mContext.getResources().getString(R.string.platinum), itemPlats));
        holder.mTextViewDucPlat.setText(String.format(Locale.getDefault(),"%s %.2f", mContext.getResources().getString(R.string.duc_plat), itemDucPlat));

        if(currentItem.inventory!=null){
            if(holder.mTextViewQuantity.getVisibility()==View.GONE){
                holder.mTextViewQuantity.setVisibility(View.VISIBLE);
            }
            int itemQuantity = currentItem.inventory.getQuantity();
            holder.mTextViewQuantity.setText(String.format(Locale.getDefault(),"%s %d", mContext.getResources().getString(R.string.quantity), itemQuantity));
        }else {
            holder.mTextViewQuantity.setVisibility(View.GONE);
        }

    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    /*public void setItemList(List<Items> items){
        mItems = (ArrayList<Items>) items;
        notifyDataSetChanged();
    }*/

    public void setItemList(List<ItemsAndInventory> itemsAndInventories){
        mItems = (ArrayList<ItemsAndInventory>) itemsAndInventories;
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener {
        public TextView mTextViewItem;
        public TextView mTextViewDucats;
        public TextView mTextViewPlats;
        public TextView mTextViewDucPlat;
        public TextView mTextViewQuantity;
        public CardView mcardView;

        public ItemViewHolder(View itemView) {
            super(itemView);
            mTextViewItem = itemView.findViewById(R.id.text_view_name);
            mTextViewDucats = itemView.findViewById(R.id.text_view_duc);
            mTextViewPlats = itemView.findViewById(R.id.text_view_plat);
            mTextViewDucPlat = itemView.findViewById(R.id.text_view_duc_plat);
            mTextViewQuantity = itemView.findViewById(R.id.text_view_quantity);
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
            menu.setHeaderTitle(mItems.get(getAdapterPosition()).items.getName());
            if(mItems.get(getAdapterPosition()).inventory!=null) {
                menu.add(mItems.get(getAdapterPosition()).items.getId(), ADD_ONE, mItems.get(getAdapterPosition()).inventory.getQuantity(), "add 1");
                menu.add(mItems.get(getAdapterPosition()).items.getId(), REMOVE_ONE, mItems.get(getAdapterPosition()).inventory.getQuantity(), "remove 1");
                menu.add(mItems.get(getAdapterPosition()).items.getId(), REMOVE_ALL, mItems.get(getAdapterPosition()).inventory.getQuantity(), "remove all");
            } else {
                menu.add(mItems.get(getAdapterPosition()).items.getId(), ADD_ONE, 0, "Add 1");
                menu.add(mItems.get(getAdapterPosition()).items.getId(), REMOVE_ONE, 0, "remove 1");
                menu.add(mItems.get(getAdapterPosition()).items.getId(), REMOVE_ALL, 0, "remove all");
            }
        }
    }
}

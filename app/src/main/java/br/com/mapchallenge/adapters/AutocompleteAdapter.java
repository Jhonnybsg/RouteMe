package br.com.mapchallenge.adapters;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import br.com.mapchallenge.R;
import br.com.mapchallenge.listeners.OnItemClick;
import br.com.mapchallenge.model.AutocompletePlaces;

/**
 * Created by Jhonny
 */
public class AutocompleteAdapter extends RecyclerView.Adapter<AutocompleteAdapter.ViewHolder> {

    private static final String UNKKNOWN = "unknown";

    private List<AutocompletePlaces> places;
    private OnItemClick onItemClick;


    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView mTextView;

        public ViewHolder(View v) {
            super(v);
            mTextView = v.findViewById(R.id.item_text);
            mTextView.setTextColor(Color.BLACK);
        }
    }

    public AutocompleteAdapter(OnItemClick onItemClick) {
        this.places = new ArrayList<>();
        this.onItemClick = onItemClick;
    }

    public AutocompleteAdapter(List<AutocompletePlaces> places, OnItemClick onItemClick) {
        this.places = places;
        this.onItemClick = onItemClick;
    }

    @Override
    public AutocompleteAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                             int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.autocomplete_item, parent, false);

        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onItemClick.onItemClick((view.findViewById(R.id.item_text)).getTag().toString());
            }
        });

        ViewHolder vh = new ViewHolder(v);
        return vh;
    }


    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.mTextView.setText(places.get(position).getAddress());
        holder.mTextView.setTag(places.get(position).getPlaceId());
    }

    public void updateItems(List<AutocompletePlaces> newPlaces) {
        this.places.clear();
        this.places.addAll(newPlaces);
        notifyDataSetChanged();
    }

    public String getItemPlaceId(int position) {
        if (places.size() > 0) {
            return this.places.get(position).getPlaceId();
        }
        return UNKKNOWN;
    }

    @Override
    public int getItemCount() {
        return places.size();
    }
}
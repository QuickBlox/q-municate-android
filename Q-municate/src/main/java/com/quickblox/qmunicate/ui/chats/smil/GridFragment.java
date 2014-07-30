package com.quickblox.qmunicate.ui.chats.smil;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

import com.quickblox.qmunicate.R;

@SuppressLint("ValidFragment")
public class GridFragment extends Fragment {

    private GridView mGridView;
    private GridAdapter mGridAdapter;
    String[] gridItems = {};
    private Activity activity;

    public GridFragment(String[] gridItems, Activity activity) {
        this.gridItems = gridItems;
        this.activity = activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view;
        view = inflater.inflate(R.layout.view_smiles_grid, container, false);
        mGridView = (GridView) view.findViewById(R.id.smiles_gridview);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (activity != null) {

            mGridAdapter = new GridAdapter(activity, gridItems);
            if (mGridView != null) {
                mGridView.setAdapter(mGridAdapter);
            }

            mGridView.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView parent, View view,
                                        int position, long id) {
                    onGridItemClick((GridView) parent, view, position, id);
                }
            });
        }
    }

    public void onGridItemClick(GridView g, View v, int position, long id) {
        Toast.makeText(activity,
                "Position Clicked: - " + position + " & " + "Text is: - "
                        + gridItems[position], Toast.LENGTH_LONG).show();
        Log.e("TAG", "POSITION CLICKED " + position);
    }
}
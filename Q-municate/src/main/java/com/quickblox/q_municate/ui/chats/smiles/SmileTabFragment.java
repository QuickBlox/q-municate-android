package com.quickblox.q_municate.ui.chats.smiles;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import com.quickblox.q_municate.R;
import com.quickblox.q_municate.model.SerializableKeys;
import com.quickblox.q_municate.service.QBServiceConsts;

import java.util.List;

public class SmileTabFragment extends Fragment {

    private View view;
    private GridView smilesGrid;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_smiles, container, false);
        smilesGrid = (GridView) view.findViewById(R.id.smiles_grid);
        initGrid();
        return view;
    }

    public void initGrid() {
        List<Integer> resources = getArguments().getIntegerArrayList(SmilesTabFragmentAdapter.RESOURCE_KEY);
        SmilesAdapter smilesAdapter = new SmilesAdapter(getActivity());
        smilesAdapter.setResources(resources);
        smilesGrid.setAdapter(smilesAdapter);
        smilesGrid.setOnItemClickListener(new OnSmileClickListener());
    }

    private class OnSmileClickListener implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Integer resourceID = (Integer) parent.getItemAtPosition(position);
            sendSmileSelectedBroadCast(resourceID);
        }

        private void sendSmileSelectedBroadCast(int resourceId) {
            Intent intent = new Intent(QBServiceConsts.SMILE_SELECTED);
            intent.putExtra(SerializableKeys.SMILE_ID, resourceId);
            getActivity().sendBroadcast(intent);
        }
    }
}
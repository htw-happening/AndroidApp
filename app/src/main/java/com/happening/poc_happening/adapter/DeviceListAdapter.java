package com.happening.poc_happening.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.happening.poc_happening.R;
import com.happening.poc_happening.bluetooth.Device;

import java.util.ArrayList;

public class DeviceListAdapter extends ArrayAdapter<Device> {

    public DeviceListAdapter(Context context, ArrayList<Device> deviceList) {
        super(context, 0, deviceList);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        final Device device = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.discovered_device, parent, false);
        }

        TextView address = (TextView) convertView.findViewById(R.id.device_address);
        TextView name = (TextView) convertView.findViewById(R.id.device_name);
        TextView state = (TextView) convertView.findViewById(R.id.device_state);

        address.setText(device.getAddress());
        name.setText(device.getName());
        state.setText(device.getStateAsString());

        return convertView;
    }
}

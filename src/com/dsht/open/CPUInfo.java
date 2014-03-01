/*
 * Performance Control - An Android CPU Control application Copyright (C) 2012
 * James Roberts
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.dsht.open;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import com.dsht.kerneltweaker.Helpers;
import com.dsht.kerneltweaker.MainActivity;
import com.dsht.kerneltweaker.R;

public class CPUInfo extends Fragment implements Constants {

    private TextView mKernelInfo;
    private TextView mCPUInfo;
    private TextView mMemInfo;
    private Context context;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getActivity();
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup root, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.cpu_info, root, false);
        mKernelInfo = (TextView) view.findViewById(R.id.kernel_info);
        mCPUInfo = (TextView) view.findViewById(R.id.cpu_info);
        mMemInfo = (TextView) view.findViewById(R.id.mem_info);
        updateData();
        MainActivity.menu.toggle(true);
        return view;
    }

    public void updateData() {
        mKernelInfo.setText("");
        mCPUInfo.setText("");
        mMemInfo.setText("");
        readFile(mKernelInfo, KERNEL_INFO_PATH);
        if (new File(PFK_VER).exists()) {
            mKernelInfo.append("\n");
            mKernelInfo.append(getString(R.string.pfk_info, Helpers.readOneLine(PFK_VER)));
            mKernelInfo.append("\n");
        }
        if (new File(DYNAMIC_DIRTY_WRITEBACK_PATH).exists()) {
            mKernelInfo.append("\n");
            mKernelInfo.append(getString(R.string.dynamic_writeback_info));
            mKernelInfo.append("\n");
        }
        if (new File(DSYNC_PATH).exists()) {
            mKernelInfo.append("\n");
            mKernelInfo.append(getString(R.string.dsync_info));
            mKernelInfo.append("\n");
        }
        if (new File(BLX_PATH).exists()) {
            mKernelInfo.append("\n");
            mKernelInfo.append(getString(R.string.blx_info));
            mKernelInfo.append("\n");
        }
        readFile(mCPUInfo, CPU_INFO_PATH);
        readFile(mMemInfo, MEM_INFO_PATH);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (!getResources().getBoolean(R.bool.config_showPerformanceOnly)) {
            inflater.inflate(R.menu.cpu_info_menu, menu);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.refresh:
                updateData();
                break;
        }
        return true;
    }

    public void readFile(TextView tView, String fName) {
        FileReader fr = null;
        try {
            fr = new FileReader(fName);
            BufferedReader br = new BufferedReader(fr);
            String line = br.readLine();
            while (null != line) {
                tView.append(line);
                tView.append("\n");
                line = br.readLine();
            }
        } catch (IOException ex) {
        } finally {
            if (null != fr) {
                try {
                    fr.close();
                } catch (IOException e) {
                }
            }
        }
    }
}

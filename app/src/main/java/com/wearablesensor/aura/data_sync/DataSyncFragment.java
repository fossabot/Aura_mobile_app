
/**
 * @file
 * @author  clecoued <clement.lecouedic@aura.healthcare>
 * @version 1.0
 *
 *
 * @section LICENSE
 *
 * Aura Mobile Application
 * Copyright (C) 2017 Aura Healthcare
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>
 *
 * @section DESCRIPTION
 * DataSyncFragment is the UI component that informs user of the Could data sync state
 * It implements the DataSyncContract.View interface
 *
 */
package com.wearablesensor.aura.data_sync;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.wearablesensor.aura.R;
import com.wearablesensor.aura.data_repository.DateIso8601Mapper;

import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class DataSyncFragment extends Fragment implements DataSyncContract.View {
    private final String TAG = this.getClass().getSimpleName();

    private DataSyncContract.Presenter mPresenter;

    @BindView(R.id.data_sync_progress_bar) ProgressBar mProgressBar; /** data push progress bar */
    @BindView(R.id.data_sync_image_view) ImageView mImageView; /** data push image state */
    @BindView(R.id.data_sync_last_sync) TextView mLastSyncView; /** text view displaying last sync date */
    @BindView(R.id.data_sync_button) Button mDataSyncButton;
    @OnClick(R.id.data_sync_button)
    public void onClickDataSyncButton(View v){
        mPresenter.pushData();
    }

    private OnFragmentInteractionListener mListener;

    public DataSyncFragment() {
        // Required empty public constructor
        Log.d(TAG, "constructor");
    }

    /**
     * @brief Use this factory method to create a new instance of
     * this fragment using the provided parameters
     *
     * @return A new instance of fragment DevicePairingFragment.
     */

    public static DataSyncFragment newInstance() {
        Log.d("DataSyncFragment", "newInstance");
        DataSyncFragment fragment = new DataSyncFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {}
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_data_sync, container, false);
        ButterKnife.bind(this, view);

        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onDataSyncFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onResume() {
        Log.d(TAG, "onResume");
        super.onResume();
        mPresenter.start();
    }

    /**
     * @brief start data push display state
     */
    @Override
    public void startPushDataOnCloud() {
        mProgressBar.setVisibility(View.VISIBLE);

        mDataSyncButton.setVisibility(View.GONE);
    }

    /**
     * @brief end data push display state
     */
    @Override
    public void endPushDataOnCloud() {
        mProgressBar.setVisibility(View.GONE);
        mProgressBar.setProgress(0);

        mDataSyncButton.setVisibility(View.VISIBLE);
    }

    /**
     * @brief refresh progress bar display during data push
     *
     * @param iProgress data push progress state in percent
     */
    @Override
    public void refreshProgressPushDataOnCloud(Integer iProgress) {
        mProgressBar.setProgress(iProgress);
    }

    /**
     * @brief  refresh last syncing date display
     *
     * @param iLastSync last time data has been push on Cloud
     */
    @Override
    public void refreshLastSync(Date iLastSync) {
        mLastSyncView.setText(getString(R.string.last_sync) +  DateIso8601Mapper.getString(iLastSync));
    }

    /**
     * @brief display an error popup with corresponding message to user
     *
     * @param iContext application context
     * @param iFailMessage message to be displayed
     */
    @Override
    public void displayFailMessageOnPushData(Context iContext, String iFailMessage) {
        String lFailMessage = getString(R.string.push_data_fail) + " : " + iFailMessage;
        Toast.makeText(iContext, lFailMessage, Toast.LENGTH_LONG).show();
    }

    /**
     * @brief attach presenter to view as it is done in MVP architecture
     *
     * @param iPresenter presenter to be attached
     */
    @Override
    public void setPresenter(DataSyncContract.Presenter iPresenter) {
        mPresenter = iPresenter;
    }


    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        void onDataSyncFragmentInteraction(Uri uri);
    }
}

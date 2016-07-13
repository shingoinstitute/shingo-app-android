package org.shingo.shingoapp.ui.events;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.shingo.shingoapp.R;
import org.shingo.shingoapp.data.GetAsyncData;
import org.shingo.shingoapp.data.OnTaskComplete;
import org.shingo.shingoapp.middle.SEvent.SEvent;
import org.shingo.shingoapp.ui.MainActivity;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnEventListFragmentInteractionListener}
 * interface.
 */
public class EventFragment extends Fragment implements OnTaskComplete {

    private OnEventListFragmentInteractionListener mListener;
    private MainActivity mainActivity;
    private RecyclerView.Adapter mAdapter;
    private ProgressDialog progress;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public EventFragment() {
    }

    public static EventFragment newInstance() {
        return new EventFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainActivity = (MainActivity)getActivity();
        mainActivity.setTitle("Upcoming Events");
        if(mainActivity.needsUpdated()) {
            mainActivity.updateListPull();
            GetAsyncData getEventsAsync = new GetAsyncData(this);
            String[] params = {"/salesforce/events"};
            getEventsAsync.execute(params);

            progress = ProgressDialog.show(getContext(), "", "Loading Events", true);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_event_list, container, false);

        Context context = view.getContext();
        RecyclerView recyclerView = (RecyclerView) view;
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        mAdapter = new MyEventRecyclerViewAdapter(mainActivity.mEvents, mListener);
        recyclerView.setAdapter(mAdapter);

        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnEventListFragmentInteractionListener) {
            mListener = (OnEventListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnEventListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onTaskComplete() {
        throw new UnsupportedOperationException("This method hasn't been implemented...");
    }

    @Override
    public void onTaskComplete(String response) {
        try {
            JSONObject result = new JSONObject(response);
            if(result.getBoolean("success")){
                JSONArray jEvents = result.getJSONArray("events");
                mainActivity.mEvents.clear();
                for(int i = 0; i < jEvents.length(); i++){
                    SEvent event = new SEvent();
                    event.fromJSON(jEvents.getJSONObject(i).toString());
                    mainActivity.mEvents.add(event);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        mAdapter.notifyDataSetChanged();

        progress.dismiss();
    }

    @Override
    public void onTaskError(String error) {
        progress.dismiss();
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnEventListFragmentInteractionListener {
        void onEventListFragmentInteraction(SEvent event);
    }
}

package com.example.jingj.criminalintent;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import static android.text.format.DateFormat.getMediumDateFormat;

/**
 * Created by jingj on 2018/3/12.
 */

public class CrimeListFragment extends Fragment {

    private RecyclerView mCrimeRecycleView;
    private TextView mTextView;
    private Button mButton;
    private CrimeAdapter mAdapter;
    private boolean mSubtitleVisable;
    private static final String SAVED_SUBTITLE_VISIBLE = "subtitle";

    private int nowposition;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_crime_list, container, false);
        mCrimeRecycleView = view.findViewById(R.id.crime_recycler_view);
        mCrimeRecycleView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mTextView = view.findViewById(R.id.noCrime);
        mButton = view.findViewById(R.id.create_crime);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Crime crime = new Crime();
                CrimeLab.get(getActivity()).addCrime(crime);
                Intent intent = CrimePagerActivity.newIntent(getActivity(), crime.getId());
                startActivity(intent);
            }
        });
        if (savedInstanceState != null){
            mSubtitleVisable = savedInstanceState.getBoolean(SAVED_SUBTITLE_VISIBLE);
        }
        updateUI();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUI();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(SAVED_SUBTITLE_VISIBLE, mSubtitleVisable);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_crime_list, menu);

        MenuItem subtitleItem = menu.findItem(R.id.show_subtitle);
        if (mSubtitleVisable){
            subtitleItem.setTitle(R.string.hide_subtitle);
        } else {
            subtitleItem.setTitle(R.string.show_subtitle);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.new_crime:
                Crime crime = new Crime();
                CrimeLab.get(getActivity()).addCrime(crime);
                Intent intent = CrimePagerActivity.newIntent(getActivity(), crime.getId());
                startActivity(intent);
                return true;
            case R.id.show_subtitle:
                mSubtitleVisable = !mSubtitleVisable;
                //当选项菜单子项发生变化时刷新选项菜单
                getActivity().invalidateOptionsMenu();
                updateSubtitle();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void updateUI() {
        CrimeLab crimeLab = CrimeLab.get(getActivity());
        if (crimeLab.getCrimes().size() >= 1){
            mTextView.setVisibility(View.INVISIBLE);
            mButton.setVisibility(View.INVISIBLE);
            mCrimeRecycleView.setVisibility(View.VISIBLE);
            List<Crime> crimes = crimeLab.getCrimes();
            if (mAdapter == null) {
                mAdapter = new CrimeAdapter(crimes);
                mCrimeRecycleView.setAdapter(mAdapter);
            } else {
                mAdapter.setCrimes(crimes);
                mAdapter.notifyDataSetChanged();
            }
        } else {
            List<Crime> crimes = crimeLab.getCrimes();
            mAdapter = new CrimeAdapter(crimes);
            mCrimeRecycleView.setAdapter(mAdapter);
            mCrimeRecycleView.setVisibility(View.INVISIBLE);
            mTextView.setVisibility(View.VISIBLE);
            mTextView.setVisibility(View.VISIBLE);
        }
        updateSubtitle();
    }

    private class CrimeHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView mTitleTextView;
        private TextView mDateTextView;
        private ImageView mSolvedImageView;
        private Crime mCrime;

        public CrimeHolder(LayoutInflater inflater, ViewGroup parent) {

            super(inflater.inflate(R.layout.list_item_crime, parent, false));
            itemView.setOnClickListener(this);

            mTitleTextView = itemView.findViewById(R.id.crime_title);
            mDateTextView = itemView.findViewById(R.id.crime_date);
            mSolvedImageView = itemView.findViewById(R.id.crime_solved);
        }

        public void bind(Crime crime) {
            mCrime = crime;
            mTitleTextView.setText(mCrime.getTitle());
            mDateTextView.setText(getMediumDateFormat(getActivity()).format(mCrime.getDate()));
            mSolvedImageView.setVisibility(crime.isSolved() ? View.VISIBLE : View.INVISIBLE);
        }

        @Override
        public void onClick(View view) {
            Intent intent = CrimePagerActivity.newIntent(getActivity(), mCrime.getId());
            startActivity(intent);
            nowposition = getAdapterPosition();
        }
    }

    public class CrimeAdapter extends RecyclerView.Adapter<CrimeHolder> {

        private List<Crime> mCrimes;

        public CrimeAdapter(List<Crime> crimes) {
            mCrimes = crimes;
        }

        @Override
        public CrimeHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            return new CrimeHolder(layoutInflater, parent);
        }

        @Override
        public void onBindViewHolder(CrimeHolder holder, int position) {
            Crime crime = mCrimes.get(position);
            holder.bind(crime);
        }

        @Override
        public int getItemCount() {
            return mCrimes.size();
        }

        @Override
        public int getItemViewType(int position) {
            return mCrimes.get(position).getPolice();
        }

        public void setCrimes(List<Crime> crimes) {
            mCrimes = crimes;
        }
    }

    private void updateSubtitle(){
        CrimeLab crimeLab = CrimeLab.get(getActivity());
        int crimeCount = crimeLab.getCrimes().size();
        //接受字符串资源中占位符的替换值
        //传入两个crimeCount，第一个匹配，第二个进行格式化
        String subtitle = getResources().getQuantityString(R.plurals.subtitle_plural, crimeCount,crimeCount);

        if (!mSubtitleVisable){
            subtitle = null;
        }
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.getSupportActionBar().setSubtitle(subtitle);
    }
}

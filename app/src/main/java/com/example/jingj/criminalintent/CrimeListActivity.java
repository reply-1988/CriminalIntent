package com.example.jingj.criminalintent;

import android.support.v4.app.Fragment;

/**
 * Created by jingj on 2018/3/12.
 */

public class CrimeListActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment(){
        return new CrimeListFragment();
    }
}

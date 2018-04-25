package com.example.jingj.criminalintent;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IntegerRes;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class DatePickerFragment extends AppCompatDialogFragment {

    //从CrimeFragment传递到DatePickerFragment所用的唯一标识符
    public static final String ARG_DATE = "date";
    //从DatePickerFragment传递到CrimeFragment所用的唯一标识符
    public static final String EXTRA_DATE = "com.jingj.android.criminalintent.date";

    private DatePicker mDatePicker;
    private Button mButton;
    //传递信息
    public static DatePickerFragment newInstance(Date date){
        Bundle args = new Bundle();
        args.putSerializable(ARG_DATE, date);

        DatePickerFragment fragment = new DatePickerFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        Date date = (Date) getActivity().getIntent().getSerializableExtra(ARG_DATE);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        View view = inflater.inflate(R.layout.dialog_date, container, false);
        mButton = view.findViewById(R.id.date_button);

        mDatePicker = view.findViewById(R.id.dialog_date_picker);
        mDatePicker.init(year, month, day, null);

        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int year = mDatePicker.getYear();
                int month = mDatePicker.getMonth();
                int day = mDatePicker.getDayOfMonth();
                Date date = new GregorianCalendar(year, month, day).getTime();
                Intent intent = new Intent();
                intent.putExtra(EXTRA_DATE, date);
                getActivity().setResult(Activity.RESULT_OK, intent);
                getActivity().finish();
            }
        });
        return view;
    }

//    private void sendResult(int resultCode, Date date){
//        if (getTargetFragment() == null){
//            return;
//        }
//        Intent intent = new Intent();
//        intent.putExtra(EXTRA_DATE, date);
//        //调用crimeFragment的onActivityResult方法来传递信息
//        getTargetFragment().onActivityResult(getTargetRequestCode(), resultCode, intent);
//    }
}

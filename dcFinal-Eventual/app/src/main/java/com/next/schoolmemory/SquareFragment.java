package com.next.schoolmemory;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import com.next.schoolmemory.Presenter.Presenter_picwall;
//图墙fragment photowall fragment
public class SquareFragment extends Fragment {
    private Toolbar toolbar;
    private AppCompatActivity myActivity;
    private Presenter_picwall presenter_picwall;
    private MyScrollView myScrollView;

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_square, container, false);
        toolbar = (Toolbar) view.findViewById (R.id.toolbar_square);
        presenter_picwall = new Presenter_picwall(this);
        myScrollView = (MyScrollView) view.findViewById(R.id.my_scroll_view);
        myScrollView.setPresenter(presenter_picwall,this);
        return view;
    }

    public void onViewCreated(View view, @Nullable Bundle savedInstanceState){//toolbar初始化
        super.onViewCreated(view, savedInstanceState);
        myActivity = (AppCompatActivity) getActivity();
        toolbar.setTitle("Photo Wall");
        myActivity.setSupportActionBar(toolbar);
        toolbar.inflateMenu(R.menu.empty_menu);
    }

    @Override
    public void onResume() {
        super.onResume();
        presenter_picwall.refreshPicPaths();
    }
}

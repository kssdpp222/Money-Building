package com.example.user.moneybuilding;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.util.Log.v;

public class HomePage extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private RecyclerView mRecyclerView;
    private LinearAdapter mAdapter;
    private RadioGroup endDate;
    private RadioButton endDateYes;
    private RadioGroup target;
    private RadioButton targetYes;
    private boolean deleteButton=false;
    private RequestQueue queue;
    private String userID;
    private List<String> mDatas = new ArrayList<String>();
    private int mYear, mMonth, mDay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //startActivity(new Intent(HomePage.this,ListViewActivity.class));
        setContentView(R.layout.activity_home_page);
        queue = Volley.newRequestQueue(this);
        userID = getSharedPreferences("data", MODE_PRIVATE)
                .getString("userID", "");

        StringRequest stringRequest = new StringRequest(Request.Method.POST, getString(R.string.servletURL)+"PersonalInformationServlet",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONArray arr = new JSONArray(response);
                            TextView headerName = findViewById(R.id.headerName);
                            headerName.setText(arr.get(1).toString());
                            //取得名字&角色圖片
                            ImageView headerImage = findViewById(R.id.headerImage);
                            Picasso.with(HomePage.this).load( getString(R.string.servletURL)+"CharacterServlet?state=getCharacter&characterID="+arr.get(0).toString()).transform(new CircleTransform())
                                    .into(headerImage);
                        } catch (Throwable t) {
                        }
                    }
                }, new Response.ErrorListener() {
            // @Override
            public void onErrorResponse(VolleyError error) {    //錯誤訊息
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<String, String>();
                map.put("state", "getHeaderData");
                map.put("userID", userID);
                return map;
            }
        };
        queue.add(stringRequest);   //把request丟進queue(佇列)



        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        addTallybook(false);
        deleteTallybook();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

    }
    public class SpacesItemDecoration extends RecyclerView.ItemDecoration {

        private int space;

        public SpacesItemDecoration(int space) {
            this.space=space;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            outRect.left=space;
            outRect.right=space;
            outRect.bottom=space;
            if(parent.getLayoutManager() instanceof GridLayoutManager || parent.getLayoutManager() instanceof StaggeredGridLayoutManager){
                if(parent.getChildAdapterPosition(view) < 2){
                    outRect.top=space;
                }
            }else{
                if(parent.getChildAdapterPosition(view)==0){
                    outRect.top=space;
                }
            }
        }
    }

    public void deleteTallybook(){
        FloatingActionButton myFab = (FloatingActionButton) findViewById(R.id.fabDel);
        myFab.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                deleteButton=!deleteButton;
                if(deleteButton){

                    changePicture(R.drawable.plus_del,"gray");

                }else{
                    changePicture(R.drawable.plus,"#FF4081");

                }

            }
        });
    }

    public void addTallybook(boolean checkIfCreate){
        FloatingActionButton myFab = (FloatingActionButton) findViewById(R.id.fab);
        myFab.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(!deleteButton) {
                    LayoutInflater inflater = LayoutInflater.from(HomePage.this);
                    final View dialogName = inflater.inflate(R.layout.new_tallybook, null);

                    endDate = (RadioGroup) dialogName.findViewById(R.id.endDate);
                    endDateYes = (RadioButton) dialogName.findViewById(R.id.endDateYes);
                    target = (RadioGroup) dialogName.findViewById(R.id.target);
                    targetYes = (RadioButton) dialogName.findViewById(R.id.targetYes);
                    endDate.setOnCheckedChangeListener(listenerEndDate);
                    target.setOnCheckedChangeListener(listenerTarget);

                    new AlertDialog.Builder(HomePage.this)
                            .setTitle("新增帳本")
                            .setView(dialogName)
                            .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            })
                            .setPositiveButton("確定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    EditText nameText = (EditText) dialogName.findViewById(R.id.editTallybookName);
                                    if (nameText.getText().toString().equals("")) {
                                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(HomePage.this);
                                        alertDialog.setTitle("提醒");
                                        alertDialog.setMessage("需填入帳本名稱!");
                                        alertDialog.setPositiveButton("知道了", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface arg0, int arg1) {
                                            }
                                        });
                                        alertDialog.show();

                                    } else {
                                        addData(1);
                                    }
                                }
                            })
                            .show();

                }
            }
        });
    }

    public void changePicture(int url,String backgroundColor){
        FloatingActionButton myFab = (FloatingActionButton) findViewById(R.id.fabDel);
        int tint = Color.parseColor(backgroundColor);
        myFab.getBackground().setColorFilter(tint, PorterDuff.Mode.DARKEN);
        for(int i=0;i<mDatas.size();i++){
            View imView= mRecyclerView.getLayoutManager().findViewByPosition(i);
            ImageButton changePic = (ImageButton) imView.findViewById(R.id.adapter_linear_text);
            changePic.setImageResource(url);
        }
    }

    private RadioGroup.OnCheckedChangeListener listenerEndDate = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            if(endDateYes.isChecked()){
                final Calendar c = Calendar.getInstance();
                mYear = c.get(Calendar.YEAR);
                mMonth = c.get(Calendar.MONTH);
                mDay = c.get(Calendar.DAY_OF_MONTH);
                new DatePickerDialog(HomePage.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int day) {

                    }

                }, mYear,mMonth, mDay)
                        .show();
            }
        }
    };

    private RadioGroup.OnCheckedChangeListener listenerTarget = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            //Log.d("myTag", "This is my message"+endDateYes.isChecked());
            if(targetYes.isChecked()){
                LayoutInflater inflater = LayoutInflater.from(HomePage.this);
                final View dialogName = inflater.inflate(R.layout.target_money, null);
                new AlertDialog.Builder(HomePage.this)
                        .setTitle("輸入目標金額")
                        .setView(dialogName)
                        .setPositiveButton("確定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .show();
            }

        }
    };



    public void addData(int position) {
        if(mDatas.size()==0){
            //initData();
            mDatas.add("Insert" + 0);

            mRecyclerView = (RecyclerView) findViewById(R.id.id_recyclerview);

            mAdapter = new LinearAdapter(mDatas);
            mRecyclerView.setLayoutManager(new GridLayoutManager(this,2));
            mRecyclerView.setAdapter(mAdapter);
            mRecyclerView.addItemDecoration(new HomePage.SpacesItemDecoration(27));
            mRecyclerView.setItemAnimator(new DefaultItemAnimator());


        }else{
            position=mDatas.size();
            mDatas.add("Insert" + position);
            mAdapter.notifyItemInserted(position);

        }

    }

    public void removeData(int position) {
        mDatas.remove(position);
        mAdapter.notifyItemRemoved(position);
    }



    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_logout) {
            // Handle the camera action
            SharedPreferences pref = getSharedPreferences("data", MODE_PRIVATE);
            pref.edit()
                    .clear()
                    .commit();
            Intent intent = new Intent();
            intent.setClass(HomePage.this, Login.class);
            startActivity(intent);
            finish();

        } else if (id == R.id.nav_tallybook) {

        } else if (id == R.id.nav_user) {
            Intent intent = new Intent();
            intent.setClass(HomePage.this, PersonalInformation.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.nav_manage) {

        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub

        if (keyCode == KeyEvent.KEYCODE_BACK) { // 攔截返回鍵
            new AlertDialog.Builder(HomePage.this)
                    .setTitle("離開")
                    .setMessage("確定關閉Money-Building")
                    .setPositiveButton("確定",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                    finish();
                                }
                            })
                    .setNegativeButton("取消",
                            new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                    // TODO Auto-generated method stub
                                }
                            }).show();
        }
        return true;
    }

    class LinearAdapter extends RecyclerView.Adapter<HomePage.LinearAdapter.MyViewHolder> {

        private List<String> list ;
        public LinearAdapter(List<String> list){
            this.list = list;
        }
        @Override
        public HomePage.LinearAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            HomePage.LinearAdapter.MyViewHolder holder = new HomePage.LinearAdapter.MyViewHolder(LayoutInflater.from(HomePage.this).inflate(R.layout.item_linear,parent,false));

           return holder;
        }



        @Override
        public void onBindViewHolder(HomePage.LinearAdapter.MyViewHolder holder, int position) {

            holder.tv.setContentDescription(list.get(position));
            holder.tv.setImageResource(R.drawable.plus);
        }


        @Override
        public int getItemCount() {

            return mDatas.size();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {
            public ImageButton tv;

            public MyViewHolder(View itemView) {
                super(itemView);
                tv = (ImageButton) itemView.findViewById(R.id.adapter_linear_text);
                tv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(deleteButton){
                            AlertDialog.Builder dialog = new AlertDialog.Builder(HomePage.this);
                            dialog.setTitle("提醒");
                            dialog.setMessage("確定刪除帳本?");
                            dialog.setNegativeButton("取消",new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface arg0, int arg1) {}
                            });
                            dialog.setPositiveButton("確定",new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface arg0, int arg1) {
                                    int index = getLayoutPosition();
                                    removeData(index);
                                }
                            });
                            dialog.show();
                        }else{
                            Intent intent = new Intent();
                            intent.setClass(HomePage.this, MainTallyBook.class);
                            startActivity(intent);
                            finish();

                        }
                    }
                });
            }
        }
    }
}

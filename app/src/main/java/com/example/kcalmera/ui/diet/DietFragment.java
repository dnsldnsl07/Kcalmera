package com.example.kcalmera.ui.diet;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.kcalmera.MainActivity;
import com.example.kcalmera.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/*list item 구조*/
class ItmStr {
    public String strName=null;
    public double amount=0;
    public String time = null;
    public int pmk=0;
    public double gram;
    public double kcal;
    public double carbohydrate;
    public double protein;
    public double fat;

    ItmStr(String s1,double am, String s2, int pm,double gr,double kc, double car, double pro, double fa)
    {
        super();
        strName = s1;
        amount =am;
        time = s2;
        pmk = pm;

        gram = gr;
        kcal = kc;
        carbohydrate = car;
        protein = pro;
        fat = fa;
    }

    public String toString()
    {
        return String.format("%s %lf %s",strName,amount,time);
    }

}


public class DietFragment extends Fragment {

    LinearLayout container2;

    private DietViewModel dietViewModel;

    public String sum(ArrayList<ItmStr> items){
        int size = items.size();
        double amount;
        double gram=0;
        double kcal=0;
        double carbohydrate=0;
        double protein=0;
        double fat=0;
        for(int i=0;i<size;i++)
        {
            amount = items.get(i).amount;
            gram+=amount*items.get(i).gram;
            kcal+=amount*items.get(i).kcal;
            carbohydrate+=amount*items.get(i).carbohydrate;
            protein+=amount*items.get(i).protein;
            fat+=amount*items.get(i).fat;
        }

        return String.format("총 영양소 정보\n총 칼로리: %.2f kcal \n탄: %.2fg   단: %.2fg   지: %.2fg ",kcal,carbohydrate,protein,fat);
    }


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_diet, container, false);

        CalendarView mCalendarView = (CalendarView) root.findViewById(R.id.calendarView);
        ScrollView scrollView2 = (ScrollView) root.findViewById(R.id.scrollView2);
        // Enable scroll bar
        scrollView2.setVerticalScrollBarEnabled(true);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date(mCalendarView.getDate()));
        final int curYear = calendar.get(Calendar.YEAR);
        final int curMonth = calendar.get(Calendar.MONTH);
        final int curDayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
        final int[] y = new int[1];
        final int[] m = new int[1];
        final int[] d = new int[1];

        //calendar에서 선택된 날짜를 갖고있기 위해 만든 변수
        y[0] = curYear;
        m[0] = curMonth+1;
        d[0] = curDayOfMonth;

        //fragment_diet의 container2부분에 activity_test2를 inflate 한다.
        container2 = (LinearLayout) root.findViewById(R.id.container2);
        LayoutInflater inflater2 = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater2.inflate(R.layout.activity_test2, container2, true);

        // 빈 데이터 리스트 생성.
        final ArrayList<ItmStr> items = new ArrayList<ItmStr>();

        // listview 생성
        final ListView listview = (ListView) container2.findViewById(R.id.listview1) ;
        // 달력에 선택된 날짜 출력
        final TextView myDate = (TextView) container2.findViewById(R.id.myDate);
        final TextView mycal = (TextView) container2.findViewById(R.id.myCal);

        //adapter 생성 및 지정
        final testAdapter adapter = new testAdapter(MainActivity.mContext,R.layout.test_item,items) ;
        listview.setAdapter(adapter) ;

        // detection으로 인한 음식 add
        if(MainActivity.check == 1){
            String foodName = MainActivity.Food;
            String amount = "1";
            String str=null;
            String[] array = null;
            String[] array2 = null;
            String food_info = ((MainActivity) MainActivity.mContext).selectFoodInfo(foodName);
            try {
                if ((d[0] != curDayOfMonth) || (m[0] != curMonth+1) || (y[0] != curYear)){
                    //2019-9-1 -> 2019-09-01 로 변환
                    String t1 = "-";
                    String t2 = "-";
                    if(m[0] < 10)
                        t1="-0";
                    if(d[0] < 10)
                        t2="-0";
                    str = ((MainActivity) MainActivity.mContext).insertRecord2(foodName, amount, "" + y[0] + t1 + m[0]  + t2 + d[0]);
                } else
                    str = ((MainActivity) MainActivity.mContext).insertRecord(foodName, amount);
            }
            catch(Exception e)
            {
                Log.e("eee_add_pop_error",e.getMessage());
            }
            //식단 table과 음식정보 table에서 가져온 정보를 쪼갬
            array2 = food_info.split("/");
            array = str.split("/");

            //list에 추가

            items.add(new ItmStr(array[0], Double.parseDouble(array[1]), array[2], Integer.parseInt(array[3]), Double.parseDouble(array2[0]), Double.parseDouble(array2[1]), Double.parseDouble(array2[2]), Double.parseDouble(array2[3]), Double.parseDouble(array2[4])));
            //총 영양소 정보 갱신
            mycal.setText(sum(items));
            //list view 갱신
            adapter.notifyDataSetChanged();

            MainActivity.check=-1;
        }

        // add button에 대한 이벤트 처리.
        Button addButton = (Button)container2.findViewById(R.id.add) ;
        addButton.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                //간이창을 띄움
                AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.mContext);

                alert.setTitle("섭취한 음식의 이름과 양을 입력해주세요.");
                //alert.setMessage("Plz, input Food name and amount");

                final AddView addView = new AddView(MainActivity.mContext);
                alert.setView(addView);

                String[] FOOD = { "백김치", "밥", "불고기", "닭갈비", "된장찌개", "후라이드 치킨", "감자채볶음", "간장게장", "김밥", "고등어구이",
                        "곰탕", "계란후라이", "계란찜", "계란말이", "잡채", "제육볶음", "짜장면", "짬뽕", "김치", "김치찌개", "깍두기",
                        "만두", "미역국", "피자", "라면", "삼겹살", "시금치나물", "순대", "떡볶이", "양념치킨"};
                addView.addFoodEditView.setAdapter(new ArrayAdapter<String>(getActivity(),
                        android.R.layout.simple_dropdown_item_1line, FOOD));

                //ok버튼 선택시
                alert.setPositiveButton("ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String foodName = addView.addFoodEditView.getText().toString();
                        String amount = addView.addAmountEditView.getText().toString();
                        String str=null;
                        String[] array = null;
                        String[] array2 = null;
                        try {
                            String food_info = ((MainActivity) MainActivity.mContext).selectFoodInfo(foodName);
                            //음식 이름 오류 검사
                            if(food_info  == null) {
                                Exception e = new Exception("음식이름 오류");
                                str="음식이름 오류";
                                throw e;
                            }
                            double am = Double.parseDouble(amount);
                            //수량 오류 검사
                            if(am  < 1) {
                                Exception e = new Exception("수량 오류");
                                str="수량 오류";
                                throw e;
                            }
                            //식단 table에 추가
                            //array2 = food_info.split("/");
                            try {
                                if ((d[0] != curDayOfMonth) || (m[0] != curMonth+1) || (y[0] != curYear)){
                                    //2019-9-1 -> 2019-09-01 로 변환
                                    String t1 = "-";
                                    String t2 = "-";
                                    if(m[0] < 10)
                                        t1="-0";
                                    if(d[0] < 10)
                                        t2="-0";
                                    str = ((MainActivity) MainActivity.mContext).insertRecord2(foodName, amount, "" + y[0] + t1 + m[0]  + t2 + d[0]);
                                } else
                                    str = ((MainActivity) MainActivity.mContext).insertRecord(foodName, amount);
                            }
                            catch(Exception e)
                            {
                                Log.e("eee_add_pop_error",e.getMessage());
                            }
                            //식단 table과 음식정보 table에서 가져온 정보를 쪼갬
                            array2 = food_info.split("/");
                            array = str.split("/");

                            //list에 추가

                            items.add(new ItmStr(array[0], Double.parseDouble(array[1]), array[2], Integer.parseInt(array[3]), Double.parseDouble(array2[0]), Double.parseDouble(array2[1]), Double.parseDouble(array2[2]), Double.parseDouble(array2[3]), Double.parseDouble(array2[4])));
                            //총 영양소 정보 갱신
                            mycal.setText(sum(items));
                            //list view 갱신
                            adapter.notifyDataSetChanged();

                        }
                        catch(Exception e)
                        {
                            Toast.makeText(MainActivity.mContext.getApplicationContext(), str,Toast.LENGTH_SHORT).show();
                            Log.e("eee_add_pop_error2",e.getMessage());
                        }

                    }
                });

                alert.setNegativeButton("no",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        adapter.notifyDataSetChanged();
                    }
                });

                alert.show();


            }
        }) ;

        // modify button에 대한 이벤트 처리.
        Button modifyButton = (Button)container2.findViewById(R.id.modify) ;
        modifyButton.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                int count, checked ;
                // list item 의 개수
                count = adapter.getCount() ;
                if (count > 0) {
                    // 현재 선택된 아이템의 position 획득.
                    checked = listview.getCheckedItemPosition();


                    if (checked > -1 && checked < count) {

                        AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.mContext);
                        alert.setTitle("섭취량을 입력해주세요.(단위:인분)");
                       // alert.setMessage("Plz, input amount");

                        final ModifyView modifyView = new ModifyView(MainActivity.mContext);
                        final int ch = checked;
                        //수정 창 view 설정
                        alert.setView(modifyView);
                        //수정 창 ok 버튼 클릭시
                        alert.setPositiveButton("ok", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                try {
                                    String amount = modifyView.modifyAmountEditView.getText().toString();
                                    double am = Double.parseDouble(amount);
                                    if (am < 1) {
                                        Exception e = new Exception("수량 오류");
                                        throw e;
                                    }
                                    String update = items.get(ch).time;
                                    String str = ((MainActivity) MainActivity.mContext).updateRecord(amount, items.get(ch).pmk);
                                    String food_info = ((MainActivity) MainActivity.mContext).selectFoodInfo(items.get(ch).strName);
                                    String[] array2 = food_info.split("/");

                                    items.set(ch, new ItmStr(items.get(ch).strName, Double.parseDouble(amount), update, items.get(ch).pmk, Double.parseDouble(array2[0]), Double.parseDouble(array2[1]), Double.parseDouble(array2[2]), Double.parseDouble(array2[3]), Double.parseDouble(array2[4])));
                                    mycal.setText(sum(items));
                                    adapter.notifyDataSetChanged();
                                }
                                catch(Exception e)
                                {
                                    Toast.makeText(MainActivity.mContext.getApplicationContext(), e.getMessage(),Toast.LENGTH_SHORT).show();
                                    Log.e("eee_modify_error", e.getMessage());
                                }
                            }
                        });

                        alert.setNegativeButton("no",new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                adapter.notifyDataSetChanged();
                            }
                        });

                        alert.show();

                    }
                }
            }
        }) ;

        // delete button에 대한 이벤트 처리.
        Button deleteButton = (Button)container2.findViewById(R.id.delete) ;
        deleteButton.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                int count, checked ;
                count = adapter.getCount() ;

                if (count > 0) {
                    // 현재 선택된 아이템의 position 획득.
                    checked = listview.getCheckedItemPosition();

                    if (checked > -1 && checked < count) {
                        // 아이템 삭제

                        String update = items.get(checked).time;
                        int pri = items.get(checked).pmk;
                        ((MainActivity) MainActivity.mContext).deleteRecord(pri);
                        items.remove(checked) ;


                        // listview 선택 초기화.
                        listview.clearChoices();

                        // listview 갱신.
                        mycal.setText(sum(items));
                        adapter.notifyDataSetChanged();
                    }
                }
            }
        }) ;

        //오늘 날짜 설정
        String curDate = curYear + "-" + (curMonth + 1) + "-" + curDayOfMonth;
        myDate.setText(curDate + "일의 식단");

        // 오늘 식단 list에 추가
        try {
            //item list 비움
            items.clear();
            //list view 갱신
            adapter.notifyDataSetChanged();

            //오늘 날짜 식단 table 데이터 가져와서 list의 item들 추가
            //2019-9-1 -> 2019-09-01 mysql DATETIME의 format에 맞춘다
            String t1 = "-";
            String t2 = "-";
            if(curMonth+1 < 10)
                t1="-0";
            if(curDayOfMonth < 10)
                t2="-0";
            curDate = curYear + t1 + (curMonth+1) + t2 + curDayOfMonth;
            
            Cursor c1 = ((MainActivity) MainActivity.mContext).selectRecord(curDate);
            String str_set = ((MainActivity) MainActivity.mContext).database_test(c1);
            String[] array_set = str_set.split("\n");
            if (!array_set[0].equals("")) {
                for (int i = 0; i < array_set.length; i++) {
                    String[] array = array_set[i].split("/");
                    String food_info = ((MainActivity) MainActivity.mContext).selectFoodInfo(array[0]);
                    String[] array2 = food_info.split("/");

                    items.add(new ItmStr(array[0], Double.parseDouble(array[1]), array[2], Integer.parseInt(array[3]),Double.parseDouble(array2[0]),Double.parseDouble(array2[1]),Double.parseDouble(array2[2]),Double.parseDouble(array2[3]),Double.parseDouble(array2[4])));
                }
                //총 영양소 갱신
                mycal.setText(sum(items));
                adapter.notifyDataSetChanged();
            }
            c1.close();
        }
        catch(Exception e)
        {
            Log.e("eee222",e.getMessage());
        }

        //달력 날짜 선택시
        mCalendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() // 날짜 선택 이벤트
        {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth)
            {

                String date = year + "-" + (month+1) + "-" + dayOfMonth;
                myDate.setText(date + "일의 식단"); // 선택한 날짜로 설정

                //선택된 날짜 저장
                y[0] =year;
                m[0] = month+1;
                d[0] = dayOfMonth ;
                
                //2019-9-1 -> 2019-09-01 로 변환
                String t1 = "-";
                String t2 = "-";
                if(m[0] < 10)
                    t1="-0";
                if(d[0] < 10)
                    t2="-0";
                date = y[0] + t1 + m[0] + t2 + d[0];

                //선택된 날짜의 식단 table 내용 가져와서 list item 갱신
                try {
                    items.clear();
                    mycal.setText(sum(items));
                    adapter.notifyDataSetChanged();

                    Cursor c1 = ((MainActivity) MainActivity.mContext).selectRecord(date);
                    String str_set = ((MainActivity) MainActivity.mContext).database_test(c1);
                    String[] array_set = str_set.split("\n");
                    if (!array_set[0].equals("")) {
                        for (int i = 0; i < array_set.length; i++) {
                            String[] array = array_set[i].split("/");
                            String food_info = ((MainActivity) MainActivity.mContext).selectFoodInfo(array[0]);
                            String[] array2 = food_info.split("/");

                            items.add(new ItmStr(array[0], Double.parseDouble(array[1]), array[2], Integer.parseInt(array[3]),Double.parseDouble(array2[0]),Double.parseDouble(array2[1]),Double.parseDouble(array2[2]),Double.parseDouble(array2[3]),Double.parseDouble(array2[4])));
                        }
                            //items.add(array_set[i]);
                        mycal.setText(sum(items));
                        adapter.notifyDataSetChanged();
                    }
                    c1.close();
                }
                catch(Exception e)
                {
                    Log.e("eee11",e.getMessage());
                }

            }
        });


        listview.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
                                           int pos, long id) {
                // TODO Auto-generated method stub

                Log.v("long clicked","pos: " + pos);

                //간이창을 띄움
                AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.mContext);
                //alert.setTitle("영양 정보");
                final FoodInfoView foodInfoView = new FoodInfoView(MainActivity.mContext);
                foodInfoView.setText1(items.get(pos).strName);
                foodInfoView.setText2(((MainActivity) MainActivity.mContext).selectFoodInfo2(items.get(pos).strName ));
                alert.setView(foodInfoView);

                alert.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                    }
                });

                alert.show();
                return true;
            }
        });

        return root;
    }
}

/*list item adapter*/
class testAdapter extends BaseAdapter {

    ArrayList<ItmStr> items ;
    int lay;
            //= new ArrayList<ItmStr>();

    testAdapter(Context context,int lo ,ArrayList<ItmStr> ar)
    {
        super();
        items = ar;
        lay=lo;
    }



    @Override
    public int getCount() {
        return items.size();
    }

   // @Override
    public void addItem(ItmStr item){
        items.add(item);
    }


    @Override
    public Object getItem(int i) {
        return items.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        TestView tView = new TestView(MainActivity.mContext.getApplicationContext());
        ItmStr item = items.get(i);
        //item 음식 이름 양
        tView.setText1(item.strName, item.amount);
        //item 그램 칼로리 탄 단 지
        // change
        tView.setText2(String.format("    1회 제공량: %.1fg               칼로리: %.1fkcal\n    탄: %.1fg   단: %.1fg   지: %.1fg",item.gram,item.amount*item.kcal,item.amount*item.carbohydrate,item.amount*item.protein,item.amount*item.fat));
        return tView;
    }
}


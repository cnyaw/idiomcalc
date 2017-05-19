//
// IdiomCalcActivity.java
// Idiom calc android implementation.
//
// Copyright (c) 2016 Waync Cheng.
// All Rights Reserved.
//
// 2016/7/27 Waync created.
//

package weilican.ic;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.BaseExpandableListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import java.util.ArrayList;

public class IdiomCalcActivity extends Activity implements View.OnClickListener
{
  final static char CHAR_ONE = '\u4e00'; // 一
  final static char CHAR_ONE2 = '\u4e48'; // 么
  final static char CHAR_TWO = '\u4e8c'; // 二
  final static char CHAR_TWO2 = '\u5169'; // 兩
  final static char CHAR_TWO3 = '\u96d9'; // 雙
  final static char CHAR_FIVE = '\u4e94'; // 五
  final static char CHAR_FIVE2 = '\u4f0d'; // 伍
  final static char CHAR_SIX = '\u516d'; // 六
  final static char CHAR_SIX2 = '\u9678'; // 陸
  final static char CHAR_TEN = '\u5341'; // 十
  final static String CHECK_MARK = "<font color='green'><big><big>\u2714</big></big></font>";
  final static String ERROR_MARK = "<font color='red'><big><big>\u2715</big></big></font>";
  final static char EmptyChar = "\u25a1".charAt(0); // Hollow square.

  String tenten, twenty;
  String hunhun, twohun;
  String shift_table[];
  String cat[];

  ArrayList<IdiomItem> idiomList[];
  ArrayList<String> quizs;
  ArrayAdapter<String> adapter;

  boolean isTitle = true;
  ListView lv;
  Button btn[];

  boolean isNumGame = true;
  String ans = null;
  int quizType = 0;
  int shift;                            // Table index.

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    if (!isTaskRoot()) {
      finish();
      return;
    }

    tenten = getString(R.string.tenten);
    twenty = getString(R.string.twenty);
    hunhun = getString(R.string.hunhun);
    twohun = getString(R.string.twohun);
    shift_table = new String[] {getString(R.string.kb1), getString(R.string.kb2), getString(R.string.kb3)};

    cat = getString(R.string.cat).split("");
    String cat2[] = new String[cat.length - 1];
    System.arraycopy(cat, 1, cat2, 0, cat.length - 1); // Remove first empty str.
    cat = cat2;

    initQuiz((int)System.currentTimeMillis());
    initTitle();
  }

  @Override
  public void onBackPressed() {
    if (isTitle) {
      finish();
    } else {
      initTitle();
    }
  }

  @Override
  public void onClick(View v) {

    scrollToEnd(false);

    //
    // Fill sel char to last empty char.
    //

    String s = quizs.get(quizs.size() - 1);
    for (int i = 0; i < s.length(); i++) {
      if (EmptyChar == s.charAt(i)) {
        String news = s.substring(0, i) + ((Button)v).getText().toString() + s.substring(i + 1);
        quizs.set(quizs.size() - 1, news);
        break;
      }
    }

    //
    // Check is all empty char filled?
    //

    s = quizs.get(quizs.size() - 1);
    for (int i = 0; i < s.length(); i++) {
      if (EmptyChar == s.charAt(i)) {
        adapter.notifyDataSetChanged(); // At least one char not fill, update quiz only.
        return;
      }
    }

    //
    // All empty char filled, validate answer.
    //

    checkQuiz(s);

    //
    // Get next quiz.
    //

    nextQuiz();
  }

  void initTitle() {
    setContentView(R.layout.title);
    setTitle(getString(R.string.idiom_game));

    isTitle = true;

    Button btnList = (Button)findViewById(R.id.btn_idiom_list);
    btnList.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View view) {
        initList();
      }
    });

    Button btnIdiom = (Button)findViewById(R.id.btn_idiom_game);
    btnIdiom.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View view) {
        initGame(false);
      }
    });

    Button btnNum = (Button)findViewById(R.id.btn_num_game);
    btnNum.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View view) {
        initGame(true);
      }
    });
  }

  class IdiomItem{
    String name;
    String desc;
    public IdiomItem(String name, String desc) {
      this.name = name;
      this.desc = desc;
    }
  }

  class ExpandableListAdapter extends BaseExpandableListAdapter {
    LayoutInflater inflater;
    ArrayList<IdiomItem> idioms[];

    public ExpandableListAdapter(Context context, ArrayList<IdiomItem> idioms[]) {
      inflater = LayoutInflater.from(context);
      this.idioms = idioms;
    }

    @Override
    public Object getChild(int group, int index) {
      return idioms[group].get(index);
    }

    @Override
    public long getChildId(int group, int index) {
      return index;
    }

    @Override
    public View getChildView(int group, final int index, boolean isLastChild, View convertView, ViewGroup parent) {
      if (null == convertView) {
        convertView = inflater.inflate(R.layout.list_item, null);
      }
      TextView name = (TextView)convertView.findViewById(R.id.idiom);
      TextView desc = (TextView)convertView.findViewById(R.id.desc);
      IdiomItem idiom = (IdiomItem)getChild(group, index);
      name.setText(idiom.name);
      desc.setText(idiom.desc);
      return convertView;
    }

    @Override
    public int getChildrenCount(int group) {
      return idioms[group].size();
    }

    @Override
    public Object getGroup(int group) {
      if (idioms[group].isEmpty()) {
        return cat[group];
      } else {
        return cat[group] + " / <small>" + idioms[group].get(0).name + " (" + idioms[group].size() + ")</small>";
      }
    }

    @Override
    public int getGroupCount() {
      return idioms.length;
    }

    @Override
    public long getGroupId(int group) {
      return group;
    }

    @Override
    public View getGroupView(int group, boolean isExpanded, View convertView, ViewGroup parent) {
      if (null == convertView) {
        convertView = inflater.inflate(R.layout.list_group, null);
      }
      TextView title = (TextView)convertView.findViewById(R.id.title);
      title.setText(Html.fromHtml((String)getGroup(group)));
      return convertView;
    }

    @Override
    public boolean hasStableIds() {
      return false;
    }

    @Override
    public boolean isChildSelectable(int group, int index) {
      return true;
    }
  }

  void initList() {
    setContentView(R.layout.idiom_list);
    setTitle(getString(R.string.idiom_list));

    if (null == idiomList) {
      idiomList = new ArrayList[cat.length];
      for (int i = 0; i < cat.length; i++) {
        idiomList[i] = new ArrayList<IdiomItem>();
      }

      int idiomNum = getIdiomNum();
      for (int i = 0; i < idiomNum; i ++) {
        try {
          String s[] = new String(getIdiom(i), "big5").split("#");
          IdiomItem idiom = new IdiomItem(s[0], s[1]);
          for (int j = 0; j < cat.length; j++) {
            if (-1 != s[0].indexOf(cat[j])) {
              idiomList[j].add(idiom);
            }
          }
        } catch (Exception e) {
        }
      }
    }

    ExpandableListView listView =(ExpandableListView)findViewById(R.id.idiom_list);
    listView.setAdapter(new ExpandableListAdapter(this, idiomList));

    isTitle = false;
  }

  void initGame(boolean bNumGame) {
    setContentView(R.layout.main);
    setTitle(getString(bNumGame ? R.string.num_game : R.string.app_name));

    quizs = new ArrayList<String>();
    adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, quizs) {
      @Override
      public View getView(int position, View convertView, ViewGroup parent) {
        View view = super.getView(position, convertView, parent);
        TextView text = (TextView) view.findViewById(android.R.id.text1);
        text.setText(Html.fromHtml(quizs.get(position)));
        return view;
      }
      @Override
      public boolean areAllItemsEnabled() {
        return false;
      }
      @Override
      public boolean isEnabled(int position) {
        return false;
      }
    };

    lv = (ListView)findViewById(R.id.quiz);
    lv.setAdapter(adapter);

    btn = new Button[] {
      (Button)findViewById(R.id.one), // 一
      (Button)findViewById(R.id.two), // 二
      (Button)findViewById(R.id.three), // 三
      (Button)findViewById(R.id.four), // 四
      (Button)findViewById(R.id.five), // 五
      (Button)findViewById(R.id.six), // 六
      (Button)findViewById(R.id.seven), // 七
      (Button)findViewById(R.id.eight), // 八
      (Button)findViewById(R.id.nine), // 九
      (Button)findViewById(R.id.ten), // 十
      (Button)findViewById(R.id.hundred), // 百
      (Button)findViewById(R.id.thousand), // 千
      (Button)findViewById(R.id.tenthou)}; // 萬

    for (int i = 0; i < btn.length; i++) {
      btn[i].setOnClickListener(this);
    }

    Button btnClear = (Button)findViewById(R.id.clear);
    btnClear.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View view) {
        scrollToEnd(false);
        String s = quizs.get(quizs.size() - 1);
        char[] chars = s.toCharArray();
        for (int i = chars.length - 1; 0 <= i; i--) {
          if ('(' == chars[i] && EmptyChar != chars[i + 1]) {
            chars[i + 1] = EmptyChar;
            quizs.set(quizs.size() - 1, String.valueOf(chars));
            adapter.notifyDataSetChanged();
            break;
          }
        }
      }
    });

    shift = 0;
    Button btnShift = (Button)findViewById(R.id.shift_table);
    btnShift.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View view) {
        shift = (shift + 1) % shift_table.length;
        String kb = shift_table[shift];
        for (int i = 0; i < kb.length(); i++) {
          btn[i].setText("" + kb.charAt(i));
        }
      }
    });

    isTitle = false;
    isNumGame = bNumGame;
    nextQuiz();
  }

  void scrollToEnd(boolean bSmooth) {
    if (bSmooth) {
      lv.smoothScrollToPosition(adapter.getCount() - 1);
    } else {
      lv.setSelection(quizs.size() - 1);
    }
  }

  void addString(String s) {
    quizs.add(s);
    adapter.notifyDataSetChanged();
    scrollToEnd(true);
  }

  int countMatches(String line, String ch) {
    return line.length() - line.replace(ch, "").length();
  }

  void checkQuiz(String result) {
    int idxDesc = ans.indexOf('#');
    String orig = ans.substring(0, idxDesc);
    String news = "";
    String mark = CHECK_MARK;
    for (int i = 0; i < orig.length(); i++) {
      char ch = orig.charAt(i);
      char resch = result.charAt(i);
      if (resch != ch) {
        if (i - 1 == orig.indexOf('(') && // First empty field.
            2 == countMatches(orig, "(") && // 2 digits answer quiz.
            ((CHAR_ONE == ch && CHAR_TEN == resch) || (CHAR_TEN == ch && CHAR_ONE == resch))) {
          news += resch;
        } else {
          news += "<font color='red'>" + ch + "</font>";
          mark = ERROR_MARK;
        }
      } else {
        news += ch;
      }
    }
    news += " " + mark + "<br>";
    if ('#' != ans.charAt(ans.length() - 1)) { // Not a num quiz.
      String desc[] = ans.substring(idxDesc + 1).split("#");
      for (int i = 0; i < desc.length; i++) {
        news += "" + (1 + i) + ". " + desc[i] + "<br>";
      }
    }
    quizs.set(quizs.size() - 1, news);
    adapter.notifyDataSetChanged();
    ans = null;
  }

  String transQuizStr(String s) {
    char[] chars = s.toCharArray();
    for (int i = 0; i < chars.length; i++) {
      if ('(' == chars[i]) {
        chars[i + 1] = EmptyChar;
      }
    }
    String quiz = String.valueOf(chars);
    quiz = quiz.substring(0, quiz.indexOf('#'));
    return quiz;
  }

  String transNumQuizStr(String s) {

    //
    // Normalize num chars.
    //

    s = s.replace(CHAR_ONE2, CHAR_ONE); // 么 -> 一
    s = s.replace(CHAR_TWO2, CHAR_TWO); // 兩 -> 二
    s = s.replace(CHAR_TWO3, CHAR_TWO); // 雙 -> 二
    s = s.replace(CHAR_FIVE2, CHAR_FIVE); // 伍 -> 五
    s = s.replace(CHAR_SIX2, CHAR_SIX); // 陸 -> 六
    s = s.replace(tenten, twenty);      // 十十 -> 二十
    s = s.replace(hunhun, twohun);      // 百百 -> 二百

    //
    // Add brackets to answer num.
    //

    int idxEqual = s.indexOf('=');
    String news = s.substring(0, idxEqual + 2);
    for (int i = idxEqual + 2; i < s.length(); i++) {
      char ch = s.charAt(i);
      if (' ' == ch) {
        break;
      }
      news += "(" + ch + ")";
    }
    s = news + " #";
    return s;
  }

  void nextQuiz() {
    try {
      if (isNumGame) {
        ans = transNumQuizStr(new String(pickNumQuiz(quizType), "big5"));
      } else {
        ans = new String(pickQuiz(quizType), "big5");
      }
      quizType = (quizType + 1) % 4;
      char[] chars = ans.toCharArray();
      for (int i = 0; i < chars.length; i++) {
        if ('+' == chars[i]) {
          chars[i] = "\u002B".charAt(0);
        } else if ('-' == chars[i]) {
          chars[i] = "\u2212".charAt(0);
        } else if ('*' == chars[i]) {
          chars[i] = "\u00D7".charAt(0);
        } else if ('/' == chars[i]) {
          chars[i] = "\u00F7".charAt(0);
        } else if ('=' == chars[i]) {
          chars[i] = "\u003D".charAt(0);
        }
      }
      ans = String.valueOf(chars);
      addString(transQuizStr(ans));
    } catch (Exception e) {
    }
  }

  public native void initQuiz(int seed);
  public native byte[] pickQuiz(int type);
  public native byte[] pickNumQuiz(int type);
  public native int getIdiomNum();
  public native byte[] getIdiom(int idx);

  static {
    System.loadLibrary("ic");
  }
}

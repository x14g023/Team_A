package jp.ac.chibafjb.teama;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Scanner;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class Test01
 */
@WebServlet("/Main")
public class Main extends HttpServlet {
	private static final String TITLE = "たいとるかわる";
	private static final long serialVersionUID = 1L;
    private Oracle mOracle;
    public static String password = "password";

    //タグの無効化
    public static String CONVERT(String str)
    {
    	return
    		str.replaceAll("&","&amp;")
    		.replaceAll("<","&gt;")
    		.replaceAll(">","&lt;")
    		.replaceAll("\n","<br>");
    }
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Main() {
        super();
        // TODO Auto-generated constructor stub
    }
	@Override
	public void init() throws ServletException {
		// TODO 自動生成されたメソッド・スタブ
		super.init();


		try{
			ServletContext context = getServletConfig().getServletContext();
			URL resource = context.getResource("/WEB-INF/db.txt");
			InputStream stream = resource.openStream();
			Scanner sc = new Scanner(stream);
			String id = sc.next();
			String pass = sc.next();
			sc.close();
			stream.close();

			mOracle = new Oracle();
			mOracle.connect("ux4", id, pass);

			//テーブルが無ければ作成
			if(!mOracle.isTable("Table_Content")&&mOracle.isTable("Table_Genre")){

				mOracle.execute("create table Table_Content("
						+ "ID Number,"
						+ "name varchar2(100),"
						+ "write varchar2(1000),"
						+ "time date,"
						+ "genreID Number);"
						+ "create table Table_Genre(GenreID Number,Genre_name varchar2(100));"
						+ "create sequence Table_Content_SeqID;"
						+ "create sequence Table_Genre_SeqID;"
						);
				}


			} catch (Exception e) {
			System.err.println("db.txtにユーザ情報が設定されていない、もしくは認証に失敗しました");
		}
	}

	@Override
	public void destroy() {
		//DB切断
		mOracle.close();
		// TODO 自動生成されたメソッド・スタブ
		super.destroy();
	}



	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		action(request,response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		action(request,response);
	}



	protected void action(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 要求文字コードのセット(Javaプログラムからはき出す文字コード)
        response.setCharacterEncoding("UTF-8");
        // 応答文字コードのセット(クライアントに通知する文字コードとファイルの種類)
        response.setContentType("text/html; charset=UTF-8");

        // 出力ストリームの取得
        PrintWriter out = response.getWriter();

        int gparam;
		try {
			String param3 = request.getParameter("j");
			gparam = Integer.parseInt(param3);
		} catch (NumberFormatException e1) {
			gparam = 1;
			e1.printStackTrace();
		}


        //パラメータにデータがあった場合はDBへ挿入
        String param1 = request.getParameter("name");
        String param2 = request.getParameter("write");
        if (param1 != null && param1.length() > 0 && param2 != null && param2.length() > 0 )
        {
        	//UTF8をJava文字列に変換
        	String datan = new String(param1.getBytes("ISO-8859-1"),"UTF-8");
        	String dataw = new String(param2.getBytes("ISO-8859-1"),"UTF-8");
        	//SQL文の作成 Oracle.STRはシングルクオートのエスケープ処理
        	String sql = String.format(
        			"insert into Table_Content values(Table_Content_SeqID.nextval,'%s','%s',SYSDATE,"+gparam+")",Oracle.STR(datan),Oracle.STR(dataw));
        	//デバッグ用
        	System.out.println("DEBUG:SQL文 "+sql);
        	//DBにSQL文を実行させる
        	mOracle.execute(sql);
        }
        //テンプレートファイルを読む
        TemplateString ts = new TemplateString();
        ts.open(this, "Keijiban.html");

        //タイトルの置換
        ts.replace("$(TITLE)", TITLE);

        //各ページの読み込み
        TemplateString p1 = new TemplateString();
        p1.open(this, "genreInsert.html");
        TemplateString p2 = new TemplateString();
        p2.open(this, "genreDelete.html");
        TemplateString p3 = new TemplateString();
        p3.open(this, "commentDelete.html");

        //パラメータによって内容を切り替え
        param1 = request.getParameter("k");
        if (param1 != null && param1.length() > 0)
        {
        	int index =  Integer.parseInt(param1);
        	if(index == 1)
        		ts.replace("$(PAGE)", p1.getText());
        	else if(index == 2)
        		ts.replace("$(PAGE)", p2.getText());
        	else if(index == 3)
        		ts.replace("$(PAGE)", p3.getText());
        }
        else
        	ts.replace("$(PAGE)", "");


        //文字列保存用バッファの作成
        StringBuilder sb = new StringBuilder();
        StringBuilder gn = new StringBuilder();
        StringBuilder gntitle = new StringBuilder();

        //データの抽出
        try {
			ResultSet res = mOracle.query("select * from Table_Content WHERE genreID = "+gparam+"ORDER BY time desc");
			ResultSet genre = mOracle.query("select * from Table_Genre");
			ResultSet gename = mOracle.query("select Genre_name from Table_Genre where GenreID ="+gparam);



			while(res.next())
			{
				String name = res.getString(2);
				String write = res.getString(3);
				//日付
				Calendar cal = Calendar.getInstance();
				cal.setTime(res.getDate(4));
				if(name != null && write != null)
				{
					//文字列バッファにメッセージ内容を貯める
					//CONVERTはタグの無効化
					sb.append(String.format(
							"<hr>%s%d年%d月%d日 %d時%d分%d秒<br>%s<BR>\n",
							CONVERT(name),
							cal.get(Calendar.YEAR),
							cal.get(Calendar.MONTH)+1,
							cal.get(Calendar.DAY_OF_MONTH),
							cal.get(Calendar.HOUR_OF_DAY),
							cal.get(Calendar.MINUTE),
							cal.get(Calendar.SECOND),CONVERT(write)));
				}
			}
			//ジャンルの抽出
			while(genre.next())
			{
				String gname = genre.getString(2);
				int gid = genre.getInt(1);
				if(gname != null)
				{
					//文字列バッファにメッセージ内容を貯める
					//CONVERTはタグの無効化
					gn.append(String.format("<a href=\"?j="+gid+"\">%s</a><br>",gname));
				}
			}

			//サブタイトル



			//メッセージの置換
	        ts.replace("$(MSG)", sb.toString());
	        ts.replace("$(GENRE)", gn.toString());


		} catch (SQLException e) {}

        //内容の出力
        out.print(ts.getText());
        //出力終了
        out.close();
	}
}

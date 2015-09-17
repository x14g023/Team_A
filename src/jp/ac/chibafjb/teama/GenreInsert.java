package jp.ac.chibafjb.teama;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class GenreInsert
 */
@WebServlet("/GenreInsert")
public class GenreInsert extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Oracle mOracle;

	String password = Main.password;
	int flag;
	String sysmsg;

	// タグの無効化
	public static String CONVERT(String str) {
		return str.replaceAll("&", "&amp;").replaceAll("<", "&gt;")
				.replaceAll(">", "&lt;").replaceAll("\n", "<br>");
	}

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public GenreInsert() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		action(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		action(request, response);
	}

	protected void action(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		// 要求文字コードのセット(Javaプログラムからはき出す文字コード)
		response.setCharacterEncoding("UTF-8");
		// 応答文字コードのセット(クライアントに通知する文字コードとファイルの種類)
		response.setContentType("text/html; charset=UTF-8");

		// 出力ストリームの取得
		PrintWriter out = response.getWriter();

		// パラメータにデータがあった場合はDBへ挿入
		String param1 = request.getParameter("genre");
		String param2 = request.getParameter("pass");

		if (param1 != null && param1.length() > 0 && param2 != null
				&& param2.length() > 0 && password.equals(param2)) {

		  	//UTF8をJava文字列に変換
        	String datag = new String(param1.getBytes("ISO-8859-1"),"UTF-8");
        	//SQL文の作成 Oracle.STRはシングルクオートのエスケープ処理
        	String sql = String.format(
        			"insert into Table_Genre values(Table_Genre_SeqID.nextval,'%s',)",Oracle.STR(datag));
        	//デバッグ用
        	System.out.println("DEBUG:SQL文 "+sql);
        	//DBにSQL文を実行させる
        	mOracle.execute(sql);
        	flag=1;
		}


        //テンプレートファイルを読む
        TemplateString ts = new TemplateString();
        ts.open(this, "genreInsert.html");

        if(flag == 1){
        	sysmsg = "ジャンルの追加に成功しました";
        }
        ts.replace("$(SYSMSG)", sysmsg.toString());
        out.print(ts.getText());

        sysmsg = "";
        flag = 0;
        //出力終了
        out.close();
	}

}
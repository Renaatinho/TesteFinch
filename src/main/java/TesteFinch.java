import java.sql.*;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.*;
import org.apache.commons.lang3.StringUtils;

public class TesteFinch {
    public static void main(String[] args) {

        Connection conn = null;
        try {
            conn = DriverManager.getConnection("jdbc:mysql://localhost/TesteFinch?" +
                    "user=root&password=Renato1507");

            java.util.Date data_atual = new Date();
            Calendar cal = Calendar.getInstance();
            cal.setTime(data_atual);
            String dia_atual = StringUtils.leftPad(Integer.toString(cal.get(Calendar.DAY_OF_MONTH)),2,"0");
            String mes_atual = StringUtils.leftPad(Integer.toString(cal.get(Calendar.MONTH) + 1),2,"0");
            String ano_atual = Integer.toString(cal.get(Calendar.YEAR));

            String baseUrl = "https://globoesporte.globo.com/placar-ge/"+ dia_atual + "-" + mes_atual + "-" + ano_atual + "/jogos.ghtml";
            System.out.println(baseUrl);
            WebClient client = new WebClient();
            client.getOptions().setCssEnabled(false);
            client.getOptions().setJavaScriptEnabled(false);
            try {

                HtmlPage page = client.getPage(baseUrl);

                List<HtmlArticle> jogoLista = page.getByXPath("//article[@class='card-jogo ']");

                if(!jogoLista.isEmpty()){

                    String query = " insert into partidas (id,data_partida,campeonato,time_mandante,placar_time_mandante,time_visitante,placar_time_visitante)"
                            + " values (?, ?, ?, ?, ?, ?, ?)";

                    for(HtmlArticle jogoItem : jogoLista){
                        int id_partida = Integer.parseInt(jogoItem.getAttribute("id"));
                        String data_partida = ((HtmlElement) jogoItem.getFirstByXPath(".//time[@class='hora-local']")).getAttribute("datetime");
                        String campeonato = ((HtmlElement) jogoItem.getFirstByXPath(".//h1[@class='titulo local']/span")).asText();
                        String time_mandante = ((HtmlElement) jogoItem.getFirstByXPath(".//div[@class='time mandante small-8 medium-10']/span[@class='nome nome-completo']")).asText();
                        HtmlSpan span_placar_mandante = (HtmlSpan) jogoItem.getFirstByXPath(".//span[@class='placar placar-mandante']");
                        String placar_time_mandante = span_placar_mandante != null ? span_placar_mandante.asText() : null;
                        String time_visitante = ((HtmlElement) jogoItem.getFirstByXPath(".//div[@class='time visitante small-8 medium-10']/span[@class='nome nome-completo']")).asText();
                        HtmlSpan span_placar_visitante = (HtmlSpan) jogoItem.getFirstByXPath(".//span[@class='placar placar-visitante']");
                        String placar_time_visitante = span_placar_visitante != null ? span_placar_visitante.asText() : null;
                        System.out.println(id_partida);
                        System.out.println(data_partida);
                        System.out.println(campeonato);
                        System.out.println(time_mandante);
                        System.out.println(placar_time_mandante);
                        System.out.println(time_visitante);
                        System.out.println(placar_time_visitante );
                        System.out.println("---------------------------------------");

                        PreparedStatement preparedStmt = conn.prepareStatement(query);
                        preparedStmt.setInt     (1, id_partida);
                        preparedStmt.setString  (2, data_partida);
                        preparedStmt.setString  (3, campeonato);
                        preparedStmt.setString  (4, time_mandante);
                        preparedStmt.setString  (5, placar_time_mandante);
                        preparedStmt.setString  (6, time_visitante);
                        preparedStmt.setString  (7, placar_time_visitante);

                        preparedStmt.execute();
                    }
                }

            } catch(Exception e) {
                e.printStackTrace();
            }finally {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
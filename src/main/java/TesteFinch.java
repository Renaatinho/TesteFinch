import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import com.gargoylesoftware.htmlunit.SilentCssErrorHandler;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.*;
import org.apache.commons.lang3.StringUtils;

public class TesteFinch {

    public static void main(String[] args) {

        Properties prop = new Properties();
        InputStream input = null;

        try {

            // Carrego o arquivo de configurações
            input = new FileInputStream("config.properties");

            prop.load(input);

            // Obtenho as configurações do banco de dados
            String dbhost = prop.getProperty("dbhost");
            String dbport = prop.getProperty("dbport");
            String dbname = prop.getProperty("dbname");
            String dbuser = prop.getProperty("dbuser");
            String dbpassword = prop.getProperty("dbpassword");

            try {
                final Connection conn;
                final Integer[] quantidadeThreadsEncerradas = {0}; // Variável de controle, utilizada para controlar as Threads encerradas
                final int quantidadeDiasPosteriores = 3; // Variável de quantos dias subsequentes será obtido as partidas

                conn = DriverManager.getConnection("jdbc:mysql://"+ dbhost + ":" + dbport + "/"+ dbname +"?user="+ dbuser +"&password=" + dbpassword + "&useUnicode=true&characterEncoding=utf-8");

                // Desabilito o auto commit para utilizar transação
                conn.setAutoCommit(false);

                // Realizo um laço na quantidade de dias subsequentes
                for(int i=0; i<=quantidadeDiasPosteriores; i++){

                    final Calendar cal = Calendar.getInstance();

                    // Obtenho a data atual do servidor
                    cal.setTime(new Date());

                    // Adiciono "i" dias a data atual, onde "i" é o índice do laço de repetição para formar as datas subsequentes
                    cal.add(Calendar.DATE, i);

                    // Abro uma nova Thread para cada dia que irei obter as informações das partidas
                    new Thread("TesteFinch" + i){
                        public void run(){

                            // Obtenho as informações de dia, mês e ano consecutivamente. com zeros a esquerda, pois será necessário no link
                            String dia_atual = StringUtils.leftPad(Integer.toString(cal.get(Calendar.DAY_OF_MONTH)),2,"0");
                            String mes_atual = StringUtils.leftPad(Integer.toString(cal.get(Calendar.MONTH) + 1),2,"0");
                            String ano_atual = Integer.toString(cal.get(Calendar.YEAR));

                            // Monto o link de onde irei obter as informações das partidas.
                            String baseUrl = "https://globoesporte.globo.com/placar-ge/"+ dia_atual + "-" + mes_atual + "-" + ano_atual + "/jogos.ghtml";

                            // Desabilito Javascript, css e alguns erros de css da página
                            WebClient client = new WebClient();
                            client.setCssErrorHandler(new SilentCssErrorHandler());
                            client.getOptions().setCssEnabled(false);
                            client.getOptions().setJavaScriptEnabled(false);

                            try {

                                // Obtenho a página com base no link que foi montado.
                                HtmlPage page = client.getPage(baseUrl);

                                // Armazeno em uma lista os elementos que contém a classe de uma partida.
                                List<HtmlArticle> jogoLista = page.getByXPath("//article[@class='card-jogo ']");

                                // Verifico se a lista não está vazia
                                if(!jogoLista.isEmpty()){

                                    // Monto a instrução sql que será utilizado para armazenar as partidas.
                                    // Utilizei o replace para que seja atualizado os placares dos jogos. também poderia fazer um select pelo id e diferenciar insert/update porém optei pelo replace
                                    String query = " REPLACE INTO partidas (id,data_partida,campeonato,time_mandante,placar_time_mandante,time_visitante,placar_time_visitante)"
                                            + " VALUES (?, ?, ?, ?, ?, ?, ?)";

                                    // Realizo um laço de repetição na lista que está armazenando os elementos das partidas.
                                    for(HtmlArticle jogoItem : jogoLista){
                                        // Obtenho o id da partida utilizado pelo globoesporte.com.br, optei por utilizar o id deles ao invés de um AutoIncremento para facilitar na atualização de placares.
                                        int id_partida = Integer.parseInt(jogoItem.getAttribute("id"));

                                        // Obtenho a data da partida.
                                        String data_partida = ((HtmlElement) jogoItem.getFirstByXPath(".//time[@class='hora-local']")).getAttribute("datetime");

                                        // Obtenho o campeonato da partida.
                                        String campeonato = ((HtmlElement) jogoItem.getFirstByXPath(".//h1[@class='titulo local']/span")).asText();

                                        // Obtenho o nome do time mandante
                                        String time_mandante = ((HtmlElement) jogoItem.getFirstByXPath(".//div[@class='time mandante small-8 medium-10']/span[@class='nome nome-completo']")).asText();

                                        // Obtenho o span que contém o placar do time mandante
                                        HtmlSpan span_placar_mandante = (HtmlSpan) jogoItem.getFirstByXPath(".//span[@class='placar placar-mandante']");

                                        // Verifico se existe o placar do time mandante, caso o jogo ainda não tenha ocorrido o valor será null
                                        String placar_time_mandante = span_placar_mandante != null ? span_placar_mandante.asText() : null;

                                        // Obtenho o nome do time visitante
                                        String time_visitante = ((HtmlElement) jogoItem.getFirstByXPath(".//div[@class='time visitante small-8 medium-10']/span[@class='nome nome-completo']")).asText();

                                        // Obtenho o span que contém o placar do time visitante
                                        HtmlSpan span_placar_visitante = (HtmlSpan) jogoItem.getFirstByXPath(".//span[@class='placar placar-visitante']");

                                        // Verifico se existe o placar do time visitante, caso o jogo ainda não tenha ocorrido o valor será null
                                        String placar_time_visitante = span_placar_visitante != null ? span_placar_visitante.asText() : null;

                                        // Imprimo na tela os valores obtidos acima.
                                        System.out.println("Id da partida: " + id_partida);
                                        System.out.println("Data da partida: " + data_partida);
                                        System.out.println("Campeonato: " + campeonato);
                                        System.out.println("Time mandante: " + time_mandante);
                                        System.out.println("Placar time mandante: " + placar_time_mandante);
                                        System.out.println("Time visitante: " + time_visitante);
                                        System.out.println("Placar time visitante: " + placar_time_visitante );
                                        System.out.println("---------------------------------------");

                                        // Preparo os parâmetros obtidos acima para realizar o insert/update no banco de dados.
                                        PreparedStatement preparedStmt = conn.prepareStatement(query);
                                        preparedStmt.setInt     (1, id_partida);
                                        preparedStmt.setString  (2, data_partida);
                                        preparedStmt.setString  (3, campeonato);
                                        preparedStmt.setString  (4, time_mandante);
                                        preparedStmt.setString  (5, placar_time_mandante);
                                        preparedStmt.setString  (6, time_visitante);
                                        preparedStmt.setString  (7, placar_time_visitante);

                                        // Executo a query com os parâmetros
                                        preparedStmt.execute();
                                    }
                                }

                            } catch(Exception e) {
                                try {
                                    conn.rollback();
                                } catch (SQLException e1) {
                                    e1.printStackTrace();
                                }
                                e.printStackTrace();
                            } finally {

                                // Verifico se é a ultima Thread que está finalizando
                                if(quantidadeDiasPosteriores == quantidadeThreadsEncerradas[0]){
                                    try {
                                        // Caso seja a ultima Thread que esteja finalizando realizo o commit no banco de dados e encerro a conexão.
                                        conn.commit();
                                        conn.close();
                                    } catch (SQLException e) {
                                        e.printStackTrace();
                                    }
                                }else{
                                    // Acrescento 1 a variável de controle das Threads
                                    quantidadeThreadsEncerradas[0]++;
                                }
                            }
                        }
                    }.start();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
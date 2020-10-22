import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.*;
import java.util.Properties;

public class Homework {

    public static final String CONNECTION_STRING =
            "jdbc:mysql://localhost:3306/";
    public static final String MINIONS_TABLE_NAME =
            "minions_db";

    private Connection connection;

    private final BufferedReader reader;

    public Homework() {
        this.reader = new BufferedReader(new InputStreamReader(System.in));
    }

    public void setConnection(String user, String password) throws SQLException {
        Properties properties = new Properties();
        properties.setProperty("user", user);
        properties.setProperty("password", password);

        connection = DriverManager.getConnection(CONNECTION_STRING + MINIONS_TABLE_NAME, properties);
    }

    public void getVillainsNamesEx2() throws SQLException {
        String query = "SELECT v.name, COUNT(mv.minion_id) AS 'count'\n" +
                "FROM villains AS v JOIN minions_villains mv on v.id = mv.villain_id\n" +
                "GROUP BY v.id\n" +
                "HAVING `count` > 15\n" +
                "ORDER BY `count` DESC;";

        PreparedStatement ps = connection.prepareStatement(query);
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            System.out.printf("%s %d%n",
                    rs.getString(1),
                    rs.getInt(2));
        }
    }

    public void getMinionNamesEx3() throws IOException, SQLException {
        System.out.println("Enter villain ID: ");
        int villainId = Integer.parseInt(reader.readLine());

        String villainName = getEntityById(villainId, "villains");

        if (villainName == null) {
            System.out.printf("No villain with id %d%n", villainId);
        } else {
            System.out.printf("Villain: %s%n", villainName);

            String query = "SELECT m.name, m.age\n" +
                    "FROM minions AS m\n" +
                    "         JOIN minions_villains mv on m.id = mv.minion_id\n" +
                    "WHERE mv.villain_id = ?";

            PreparedStatement ps = connection.prepareStatement(query);
            ps.setInt(1, villainId);

            ResultSet resultSet = ps.executeQuery();

            int counter = 1;
            while (resultSet.next()) {
                System.out.printf("%d. %s %d%n",
                        counter++,
                        resultSet.getString("name"),
                        resultSet.getInt("age"));
            }
        }
    }

    private String getEntityById(int entityId, String tableName) throws SQLException {
        String query = String.format("SELECT name FROM %s WHERE id = ?", tableName);

        PreparedStatement ps = connection.prepareStatement(query);

        ps.setInt(1, entityId);

        ResultSet resultSet = ps.executeQuery();

        return resultSet.next() ? resultSet.getString("name") : null;
    }

    public void addMinionEx4() throws IOException, SQLException {
        System.out.println("Enter minions info: name, age, town name:");

        String[] minionInfo = reader.readLine().split("\\s+");
        String minionName = minionInfo[0];
        int age = Integer.parseInt(minionInfo[1]);
        String townName = minionInfo[2];

        int townId = getEntityIdByName(townName, "towns");

        if (townId == -1) {
            insertEntityInTowns(townName);
        }

        //TODO finish task
    }

    private void insertEntityInTowns(String townName) throws SQLException {
        String query = "INSERT INTO towns(name) VALUE(?)";
        PreparedStatement statement = connection.prepareStatement(query);

        statement.setString(1, townName);
        statement.execute();
    }

    private int getEntityIdByName(String entityName, String tableName) throws SQLException {
        String query = String.format("SELECT id FROM %s WHERE name = ?", tableName);

        PreparedStatement statement = connection.prepareStatement(query);
        statement.setString(1, entityName);

        ResultSet resultSet = statement.executeQuery();

        return resultSet.next() ? resultSet.getInt(1) : -1;
    }


    public void changeTownNameCasingEx5() throws IOException, SQLException {
        System.out.println("Enter country name: ");
        String countryName = reader.readLine();

        String query = "UPDATE towns SET name = UPPER(name) WHERE country = ?";

        PreparedStatement statement = connection.prepareStatement(query);
        statement.setString(1, countryName);

        int updatedRows = statement.executeUpdate();

        if (updatedRows == 0) {
            System.out.println("No town names were affected.");
        } else {
            System.out.printf("%d town names were affected.%n", updatedRows);
            //TODO fetch updated town names
        }
    }

    public void increaseAgeWithStoredProcedure() throws IOException, SQLException {
        int minionId = Integer.parseInt(reader.readLine());

        String query = "CALL usp_get_older(?)";
        CallableStatement callableStatement = connection.prepareCall(query);
        callableStatement.setInt(1, minionId);

        callableStatement.execute();
    }
}

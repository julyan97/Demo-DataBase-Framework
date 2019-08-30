import javafx.beans.binding.StringBinding;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;

import static java.util.stream.Collectors.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class DemoFramework<T> implements Inter<T> {
      private Connection connection;

      public Connection getConnection() {
            return connection;
      }

      public DemoFramework(Connection connection) {
            this.connection = connection;
      }

      public DemoFramework(String url, String name, String password) throws SQLException {
            this.connection = DriverManager.getConnection(url,name,password);
      }

      @Override
      public void addByModel(T model12) throws SQLException, IllegalAccessException, InvocationTargetException {
            var declaredFields = model12.getClass().getDeclaredFields();
            StringBuilder sb=new StringBuilder();
            StringBuilder sb2=new StringBuilder();
            //SB1
            sb.append(" (");
            for (int i = 0; i < declaredFields.length - 1; i++) {
                  sb.append(declaredFields[i].getName()).append(" , ");
            }
            sb.append(declaredFields[declaredFields.length-1].getName()).append(") ");

            //SB2



            sb2.append(" (");
            for (int i = 0; i < declaredFields.length-1; i++) {
                  declaredFields[i].setAccessible(true);
                  sb2.append("\"").append(declaredFields[i].get(model12)).append("\"").append(",");
            }
            declaredFields[declaredFields.length-1].setAccessible(true);
            sb2.append("\"").append(declaredFields[declaredFields.length-1].get(model12)).append("\"").append(")");



            String query = "INSERT INTO "+model12.getClass().getSimpleName()+"s"+" "+sb.toString()+ "VALUES "+sb2.toString();

            Logger.getLogger(model12.getClass().getName()).info(query+"\n");
            connection.prepareStatement(query)
                    .execute();

      }

      @Override
      public void delete(String FromTable,String where) throws SQLException {
            String query = String.format(
                    "Delete %s where %s",
                    FromTable,
                    where
            );
            connection.prepareStatement(query)
                    .execute();
      }

      @Override
      public void deleteByModel(T model) throws IllegalAccessException, SQLException {
             StringBuilder sb= new StringBuilder();
             sb.append("Delete From ").append(model.getClass().getSimpleName()).append("s");
             sb.append(" where ");
             var fields= model.getClass().getDeclaredFields();
            for (int i = 0; i < fields.length-1 ; i++) {
                  sb.append(fields[i].getName()).append("=\"");
                  fields[i].setAccessible(true);
                  sb.append(fields[i].get(model)).append("\" and ");
            }
            fields[fields.length-1].setAccessible(true);
            sb.append(fields[fields.length-1].getName()).append("=\"");
            sb.append(fields[fields.length-1].get(model)).append("\" ");

            Logger.getLogger(model.getClass().getName()).info(sb.toString()+"\n");
            connection.prepareStatement(sb.toString()).execute();
      }

      @Override
      public void update(String table,String data) throws SQLException {
            String query = String.format(
                    "UPDATE %s SET %s",
                    table,
                    data
            );
            connection.prepareStatement(query)
                    .execute();
      }

      @Override
      public List<String> select(String select, Class model) throws SQLException {
            var query=String.format("Select %s from %s"
                    ,select
                    ,model.getSimpleName()+"s"
                    );
           var rs = connection.prepareStatement(query).executeQuery();
           var list=new ArrayList<String>();
           int counter=0;
           var fields=model.getDeclaredFields();
           while (rs.next())
           {
                 if(select.equals("*")) {
                       list.add(rs.getString(fields[counter].getName()));
                       counter++;
                 }
                 else
                 {
                       var start=select.split(",");
                       var commands=new ArrayList<String>();
                       for (int i = 0; i < start.length; i++) {
                             commands.add(start[i].split("=")[0].trim());
                       }
                       list.add(rs.getString(commands.get(counter)));
                       counter++;

                 }
           }
           return list;
      }

      @Override
      public void pureQuery(String query) throws SQLException {
            connection.prepareStatement(query).execute();
      }

      @Override
      public void createTableByModel(Class model) throws SQLException {
            var fields=model.getDeclaredFields();
            StringBuilder sb=new StringBuilder();
            sb.append("Create table if not exists ")
                    .append(model.getName())
                    .append("s")
                    .append(" ( ")
                    .append("id int(11) not null auto_increment,");
            for (int i = 0; i < fields.length; i++) {
                  sb.append(" ")
                          .append(fields[i].getName())
                          .append(" varchar(100) not null, ");
            }
            sb.append("Primary key ")
                    .append("(")
                    .append("id")
                    .append(")")
                    .append(" );");
           connection.prepareStatement(sb.toString()).execute();
      }
}

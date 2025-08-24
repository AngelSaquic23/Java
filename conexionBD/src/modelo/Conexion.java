/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.swing.JOptionPane;

/**
 *
 * @author jhonn
 */
public class Conexion {
    public Connection conexionBD;
    public final String bd = "db_empresa_";
    public final String urlConexion =String.format("jdbc:mysql://127.0.0.1:3306/%s",bd);
    private final String usuario = "root";
    private final String contra = "Umg$2025.";
    private final String jdbc = "com.mysql.cj.jdbc.Driver";
    
    public void abrir_conexion(){
        try{
            Class.forName(jdbc);
            conexionBD = DriverManager.getConnection(urlConexion,usuario,contra);
           // JOptionPane.showMessageDialog(null,"conexion Exitosa...","Exito",JOptionPane.INFORMATION_MESSAGE);
            
        }catch(Exception ex){
            System.out.println("Error...."+ ex.getMessage());
        }
    }
    
    public void cerrar_conexion(){
        try{
            conexionBD.close();
        }catch(SQLException ex){
            System.out.println("Error...."+ ex.getMessage());
        }
    }
}

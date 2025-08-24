/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;
import java.awt.HeadlessException;
import javax.swing.JOptionPane;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.util.HashSet;
import java.util.Set;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author jhonn
 */
public class Cliente extends Persona{
    private String nit;
    private int id;
            
    Conexion cn;

    public Cliente(){}
    public Cliente(int id,String nit, String nombres, String apellidos, String direccion, String telefono, String fecha_nacimiento) {
        super(nombres, apellidos, direccion, telefono, fecha_nacimiento);
        this.nit = nit;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
    
    

    public String getNit() {
        return nit;
    }

    public void setNit(String nit) {
        this.nit = nit;
    }
    
    public DefaultTableModel leer(){
        DefaultTableModel tabla = new DefaultTableModel();
        try{
            
            cn = new Conexion();
            cn.abrir_conexion();
            String query;
            query = "Select id_cliente as id,nit,nombres,apellidos,telefono,fecha_nacimiento from clientes";
            ResultSet consulta = cn.conexionBD.createStatement().executeQuery(query);
            
            String encabezado[] = {"id","Nit","Nombres","Apellidos","Direccion","Telefono","Nacimiento"};
            tabla.setColumnIdentifiers(encabezado);
            
            String datos[] = new String[7];
            
            while(consulta.next()){
               datos[0] = consulta.getString("id"); 
               datos[1] = consulta.getString("nit");
               datos[2] = consulta.getString("nombres");
               datos[3] = consulta.getString("apellidos");
               datos[4] = consulta.getString("direccion");
               datos[5] = consulta.getString("telefono");
               datos[6] = consulta.getString("fecha_nacimiento");
               
               tabla.addRow(datos);
            }
            cn.cerrar_conexion();
            
            
        }catch(Exception ex){
            cn.cerrar_conexion();
            System.out.println("Error:   "+ex.getMessage());
        }
        return tabla;
    }
    
    @Override
    public void agregar(){
        /*System.out.println("Nit: "+ getNit());
        System.out.println("Nombres: "+ this.getNombres());
        System.out.println("Apellidos: "+ this.getApellidos());
        System.out.println("Direccion: "+ this.getDireccion());
        System.out.println("Telefono: "+ this.getTelefono());
        System.out.println("Fecha Nacimiento: "+ this.getFecha_nacimiento());*/
        
        try{
            PreparedStatement parametro;
            
            String query = "INSERT INTO clientes(nit,nombres,apellidos,direccion,telefono,fecha_nacimiento) VALUES(?,?,?,?,?,?);";
            cn = new Conexion();
            cn.abrir_conexion();
            
            parametro = (PreparedStatement) cn.conexionBD.prepareStatement(query);
            parametro.setString(1, getNit());
            parametro.setString(2, getNombres());
            parametro.setString(3, getApellidos());
            parametro.setString(4, getDireccion());
            parametro.setString(5, getTelefono());
            parametro.setString(6, getFecha_nacimiento());
            
            int executar = parametro.executeUpdate();
            cn.cerrar_conexion();
            
            JOptionPane.showMessageDialog(null,Integer.toString(executar) + "registro ingresado","Agregar",JOptionPane.INFORMATION_MESSAGE);
            
        }catch(Exception ex){
            System.out.println("Error...."+ ex.getMessage());
        }
        
    }
    
    @Override
    public void actualizar(){
     try{
            PreparedStatement parametro;
            
            
            cn = new Conexion();
            cn.abrir_conexion();
            String query;
            query = "update clientes set nit= ?,nombres= ?,apellidos= ?,direccion= ?,telefono= ?,fecha_nacimiento= ? "+"where id_cliente = ?";
            
            parametro = (PreparedStatement) cn.conexionBD.prepareStatement(query);
            parametro.setString(1, getNit());
            parametro.setString(2, getNombres());
            parametro.setString(3, getApellidos());
            parametro.setString(4, getDireccion());
            parametro.setString(5, getTelefono());
            parametro.setString(6, getFecha_nacimiento());
            parametro.setInt(7, getId());
            
            int executar = parametro.executeUpdate();
            cn.cerrar_conexion();
            
            JOptionPane.showMessageDialog(null,Integer.toString(executar) + "registro Actualizado","Agregar",JOptionPane.INFORMATION_MESSAGE);
            
        }catch(Exception ex){
            System.out.println("Error...."+ ex.getMessage());
        }
    }
    
    @Override
    public void eliminar(){
    
        try{
            PreparedStatement parametro;
            
            
            cn = new Conexion();
            cn.abrir_conexion();
            String query;
            query = "delete from clientes where id_cliente = ?";
            
            parametro = (PreparedStatement) cn.conexionBD.prepareStatement(query);
            
            parametro.setInt(1, getId());
            
            int executar = parametro.executeUpdate();
            cn.cerrar_conexion();
            
            JOptionPane.showMessageDialog(null,Integer.toString(executar) + "registro Eliminado","Agregar",JOptionPane.INFORMATION_MESSAGE);
            
        }catch(Exception ex){
            System.out.println("Error...."+ ex.getMessage());
        }
    }
    
    
    
}

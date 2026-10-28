package com.senac.helpdesk.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

@Entity
public class Usuario {

    @Id
    @GeneratedValue
    private Long id;

    public Long getId() {
        return id;
    }

    private String nome;

    public String getNome(){
        return nome;
    }

    public void setNome(String nome){
       this.nome = nome;
    }
    private String email;

    public  String getEmail(){
        return email;
    }

    public void setEmail(String email){
        this.email = email;
    }


    public Usuario() {
    }
}
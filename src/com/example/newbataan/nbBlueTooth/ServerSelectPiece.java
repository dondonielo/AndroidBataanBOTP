package com.example.newbataan.nbBlueTooth;

import java.io.Serializable;

import com.example.newbataan.State;



public class ServerSelectPiece implements Serializable{
    public State piece = State.EMPTY;

    public ServerSelectPiece(State state){
        this.piece = state;
    }
}
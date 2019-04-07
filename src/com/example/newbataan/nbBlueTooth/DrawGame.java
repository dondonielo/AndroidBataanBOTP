package com.example.newbataan.nbBlueTooth;

import java.io.Serializable;



public class DrawGame implements Serializable {

    private boolean draw = false;
    public DrawType type = DrawType.NONE;

    public DrawGame(DrawType drawType){
        this.type = drawType;
    }

    public boolean isDraw() {
        return draw;
    }
}

package com.example.signslanguagefyp.Dashboard.Model;

public class ModesModel {
    private int image;
    private String mode_name;

    public ModesModel(int image, String mode_name) {
        this.image = image;
        this.mode_name = mode_name;
    }

    public int getImage() {
        return image;
    }

    public void setImage(int image) {
        this.image = image;
    }

    public String getMode_name() {
        return mode_name;
    }

    public void setMode_name(String mode_name) {
        this.mode_name = mode_name;
    }
}

package com.smoftware.elmour.UI.dialog;

public class Conversation {
    private String id;
    private String dialog = "";
    private String data = "";
    private String type = "";

    public Conversation(){
    }

    public String getId() {
        return id;
    }
    public void setId(String id){
        this.id = id;
    }

    public String getDialog(){
        return dialog;
    }
    public void setDialog(String dialog){
        this.dialog = dialog;
    }

    public String getData(){
        return data;
    }
    public void setData(String data){
        this.data = data;
    }

    public String getType() {
        return type;
    }
    public void setType(String type){
        this.type = type;
    }
}

package org.docshare.example.controller;

import org.docshare.mvc.Controller;

public class IndexController extends Controller {
    public String index(String name){
        return "hello "+name;
    }
}

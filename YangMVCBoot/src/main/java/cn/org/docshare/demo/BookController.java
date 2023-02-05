package cn.org.docshare.demo;

import org.docshare.mvc.Controller;

public class BookController extends Controller {
    public Object list(){
        return T("book").all().limit(10 );
    }
}

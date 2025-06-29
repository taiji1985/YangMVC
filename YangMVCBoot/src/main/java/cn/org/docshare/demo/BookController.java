package cn.org.docshare.demo;

import org.docshare.yangmvc.mvc.Controller;

public class BookController extends Controller {
    public Object list(){
        return T("book").all().limit(10 );
    }
}

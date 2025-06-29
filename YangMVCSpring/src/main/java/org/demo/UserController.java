package org.demo;

import java.util.List;

import org.docshare.yangmvc.log.Log;
import org.docshare.yangmvc.mvc.Controller;
import org.docshare.yangmvc.orm.Model;

public class UserController extends Controller {
    public List<Model> list() {
        Log.i("user list called");
        return Model.tool("user").all().toArrayList();
    }
}

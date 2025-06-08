package org.demo;

import java.util.List;

import org.docshare.log.Log;
import org.docshare.mvc.Controller;
import org.docshare.orm.Model;

public class UserController extends Controller {
    public List<Model> list() {
        Log.i("user list called");
        return Model.tool("user").all().toArrayList();
    }
}
